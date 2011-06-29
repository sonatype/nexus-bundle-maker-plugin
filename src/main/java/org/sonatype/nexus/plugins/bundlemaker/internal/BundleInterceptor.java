package org.sonatype.nexus.plugins.bundlemaker.internal;

import static org.sonatype.nexus.plugins.bundlemaker.internal.BundleMakerUtils.GENERATED_AT_ATTRIBUTE;
import static org.sonatype.nexus.plugins.bundlemaker.internal.BundleMakerUtils.isAlreadyAnOSGiBundle;
import static org.sonatype.nexus.plugins.bundlemaker.internal.BundleMakerUtils.jarPathForBundle;
import static org.sonatype.nexus.plugins.bundlemaker.internal.BundleMakerUtils.recipePathForBundle;
import static org.sonatype.nexus.plugins.bundlemaker.internal.NexusUtils.createLink;
import static org.sonatype.nexus.plugins.bundlemaker.internal.NexusUtils.retrieveFile;
import static org.sonatype.nexus.plugins.bundlemaker.internal.NexusUtils.retrieveItem;
import static org.sonatype.nexus.plugins.bundlemaker.internal.NexusUtils.safeRetrieveItemBypassingChecks;
import static org.sonatype.nexus.plugins.bundlemaker.internal.NexusUtils.storeItem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.sonatype.nexus.mime.MimeUtil;
import org.sonatype.nexus.plugins.bundlemaker.BundleMaker;
import org.sonatype.nexus.plugins.bundlemaker.BundleMakerConfiguration;
import org.sonatype.nexus.plugins.requestinterceptor.RequestInterceptor;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.RepositoryItemUidLock;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.repository.Repository;

import aQute.lib.osgi.Builder;
import aQute.lib.osgi.Jar;

@Named( BundleInterceptor.ID )
@Singleton
public class BundleInterceptor
    implements RequestInterceptor
{

    public static final String ID = "bundleInterceptor";

    @Inject
    private Logger logger;

    private final BundleMaker bundleMaker;

    private final MimeUtil mimeUtil;

    @Inject
    BundleInterceptor( final BundleMaker bundleMaker, final MimeUtil mimeUtil )
    {
        this.bundleMaker = bundleMaker;
        this.mimeUtil = mimeUtil;
    }

    @Override
    public void execute( final Repository repository, final String path, final Action action )
    {
        logger.debug( "Handling request for OSGi bundle [{}:{}]", repository.getId(), path );

        // check if the capability is enabled for this repository
        final BundleMakerConfiguration configuration = bundleMaker.getConfiguration( repository.getId() );
        if ( configuration == null )
        {
            logger.debug( "OSGi bundle [{}] not created as Bundle Maker capability is not enabled for repository [{}]",
                path, repository.getId() );
            return;
        }

        // first try to retrieve itself.
        // This will trigger download from remote if the capability is on top of a proxy repository

        // use the bypass method so request processors do not trigger again and get into a cycle
        StorageItem bundle = safeRetrieveItemBypassingChecks( repository, path );

        // It could be that the bundle was proxied case when we do not re-generate it
        if ( bundle != null && !bundle.getAttributes().containsKey( GENERATED_AT_ATTRIBUTE ) )
        {
            // TODO if bundle is uploaded and it was generated before this attrib will remain
            logger.debug( "OSGi bundle [{}] was not generated by this plugin. Bailing out.", path );
            return;
        }

        RepositoryItemUidLock jarLock = null;
        RepositoryItemUidLock recipeLock = null;
        RepositoryItemUidLock bundleLock = null;

        try
        {
            // retrieve the jar for which the recipe should be created
            final String jarPath = jarPathForBundle( path );
            StorageItem jar = null;
            File jarFile = null;
            try
            {
                // Lock the jar before getting it so once we get it we know it does not change or is removed
                jarLock = repository.createUid( jarPath ).getLock();
                jarLock.lock( Action.read );

                jar = retrieveItem( repository, jarPath );
                jarFile = retrieveFile( repository, jarPath );
            }
            catch ( final Exception e )
            {
                logger.warn( String.format( "OSGi bundle [%s] not created as jar [%s] was not available due to [%s]",
                    path, jarPath, e.getMessage() ), e );
                return;
            }

            // do not create a bundle if jar is already an OSGi bundle
            if ( isAlreadyAnOSGiBundle( jarFile ) )
            {
                logger.debug( "[{}] is already an OSGi bundle. Creating an link.", jarPath );
                if ( bundle == null || !( bundle instanceof StorageLinkItem ) )
                {
                    try
                    {
                        createLink( repository, jar, path );
                    }
                    catch ( final Exception e )
                    {
                        logger.warn(
                            String.format( "OSGi bundle link [%s] not created due to [%s]", path, e.getMessage() ), e );
                    }
                }
                return;
            }

            // retrieve the recipe. This will trigger its creation / refresh
            final String recipePath = recipePathForBundle( path );
            StorageItem recipe = null;
            File recipeFile = null;
            try
            {
                // Lock the recipe before getting it so once we get it we know it does not change or is removed
                recipeLock = repository.createUid( recipePath ).getLock();
                recipeLock.lock( Action.read );

                recipe = retrieveItem( repository, recipePath );
                recipeFile = retrieveFile( repository, recipePath );
            }
            catch ( final Exception e )
            {
                logger.warn( String.format(
                    "OSGi bundle [%s] not created as recipe [%s] was not available due to [%s]", path, recipePath,
                    e.getMessage() ), e );
                return;
            }

            // do not regenerated the bundle if is newer then jar and pom
            if ( bundle != null && bundle.getStoredLocally() >= jar.getStoredLocally()
                && bundle.getStoredLocally() >= recipe.getStoredLocally() )
            {
                logger.debug( "OSGi bundle [{}] is up to date. Bailing out.", path );
                return;
            }

            // Acquire write lock
            bundleLock = repository.createUid( path ).getLock();
            bundleLock.lock( Action.create );

            // Now re-check that we still have to generate the recipe
            bundle = safeRetrieveItemBypassingChecks( repository, path );

            // It could be that the bundle was proxied case when we do not re-generate it
            if ( bundle != null && !bundle.getAttributes().containsKey( GENERATED_AT_ATTRIBUTE ) )
            {
                logger.debug( "OSGi bundle [{}] was not generated by this plugin. Bailing out.", path );
                return;
            }
            // do not regenerated the bundle if is newer then jar and pom
            if ( bundle != null && bundle.getStoredLocally() >= jar.getStoredLocally()
                && bundle.getStoredLocally() >= recipe.getStoredLocally() )
            {
                logger.debug( "OSGi bundle [{}] is up to date. Bailing out.", path );
                return;
            }

            createOSGiBundle( repository, path, recipeFile, jarFile );
        }
        finally
        {
            unlock( jarLock, recipeLock, bundleLock );
        }
    }

    private static void unlock( final RepositoryItemUidLock... locks )
    {
        for ( final RepositoryItemUidLock lock : locks )
        {
            if ( lock != null )
            {
                lock.unlock();
            }
        }
    }

    private void createOSGiBundle( final Repository repository, final String path, final File recipeFile,
                                   final File jarFile )
    {
        logger.debug( "Generating OSGi bundle [{}:{}]", repository.getId(), path );

        final Builder builder = new Builder();
        try
        {
            builder.setProperties( recipeFile );
            builder.setJar( jarFile );
            builder.mergeManifest( builder.getJar().getManifest() );
            builder.calcManifest();

            for ( final String msg : builder.getWarnings() )
            {
                logger.warn( "Warning in manifest for [{}] : [{}] ", path, msg );
            }
            final StringBuilder errors = new StringBuilder();
            for ( final String msg : builder.getWarnings() )
            {
                final String error = String.format( "Error in manifest for [%s] : [%s] ", path, msg );
                logger.error( error );
                errors.append( error );
            }
            if ( errors.length() > 0 )
            {
                logger.warn( String.format( "OSGi bundle [%s] not created due to: \\n[%s]", path, errors.toString() ) );
                return;
            }

            final ResourceStoreRequest request = new ResourceStoreRequest( path );

            InputStream in = null;
            try
            {
                in = createInputStream( builder.getJar(), path );
                final Map<String, String> attributes = new HashMap<String, String>();
                attributes.put( GENERATED_AT_ATTRIBUTE, new Date().toString() );

                storeItem( repository, request, in, mimeUtil.getMimeType( request.getRequestPath() ), attributes );
            }
            finally
            {
                IOUtil.close( in );
            }
        }
        catch ( final Exception e )
        {
            logger.warn( String.format( "OSGi bundle [%s] not created due to [%s]", path, e.getMessage() ), e );
        }
        finally
        {
            builder.close();
        }
    }

    private InputStream createInputStream( final Jar jar, final String path )
        throws IOException
    {
        final PipedInputStream pin = new PipedInputStream();
        final PipedOutputStream pout = new PipedOutputStream( pin );

        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    jar.write( pout );
                    pout.flush();
                }
                catch ( final Exception e )
                {
                    logger.error( String.format( "Could not generate bundle [%s] due to [%s]", path, e.getMessage() ),
                        e );
                }
                finally
                {
                    IOUtil.close( pout );
                }
            }
        }.start();

        return pin;
    }

}
