/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.bundlemaker.internal;

import static org.sonatype.nexus.plugins.bundlemaker.internal.BundleMakerUtils.GENERATED_AT_ATTRIBUTE;
import static org.sonatype.nexus.plugins.bundlemaker.internal.BundleMakerUtils.isAlreadyAnOSGiBundle;
import static org.sonatype.nexus.plugins.bundlemaker.internal.BundleMakerUtils.jarPathForRecipe;
import static org.sonatype.nexus.plugins.bundlemaker.internal.BundleMakerUtils.pomGavFor;
import static org.sonatype.nexus.plugins.bundlemaker.internal.NexusUtils.retrieveFile;
import static org.sonatype.nexus.plugins.bundlemaker.internal.NexusUtils.retrieveItem;
import static org.sonatype.nexus.plugins.bundlemaker.internal.NexusUtils.safeRetrieveFile;
import static org.sonatype.nexus.plugins.bundlemaker.internal.NexusUtils.safeRetrieveItem;
import static org.sonatype.nexus.plugins.bundlemaker.internal.NexusUtils.safeRetrieveItemBypassingChecks;
import static org.sonatype.nexus.plugins.bundlemaker.internal.NexusUtils.storeItem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.index.artifact.Gav;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingException;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.mime.MimeUtil;
import org.sonatype.nexus.plugins.bundlemaker.BundleMaker;
import org.sonatype.nexus.plugins.bundlemaker.BundleMakerConfiguration;
import org.sonatype.nexus.plugins.mavenbridge.NexusMavenBridge;
import org.sonatype.nexus.plugins.mavenbridge.internal.FileItemModelSource;
import org.sonatype.nexus.plugins.requestinterceptor.RequestInterceptor;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.RepositoryItemUidLock;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

@Named( RecipeInterceptor.ID )
@Singleton
public class RecipeInterceptor
    extends AbstractLoggingComponent
    implements RequestInterceptor
{

    public static final String ID = "recipeInterceptor";

    private final BundleMaker bundleMaker;

    private final MimeUtil mimeUtil;

    private final NexusMavenBridge mavenBridge;

    private final RepositoryRegistry repositories;

    @Inject
    RecipeInterceptor( final BundleMaker bundleMaker, final MimeUtil mimeUtil, final NexusMavenBridge mavenBridge,
                       final RepositoryRegistry repositories )
    {
        this.bundleMaker = bundleMaker;
        this.mimeUtil = mimeUtil;
        this.mavenBridge = mavenBridge;
        this.repositories = repositories;
    }

    @Override
    public void execute( final Repository repository, final String path, final Action action )
    {
        getLogger().debug( "Handling request for OSGi recipe [{}:{}]", repository.getId(), path );

        // check if the capability is enabled for this repository
        final BundleMakerConfiguration configuration = bundleMaker.getConfiguration( repository.getId() );
        if ( configuration == null )
        {
            getLogger().debug(
                "OSGi recipe [{}] not created as Bundle Maker capability is not enabled for repository [{}]",
                path, repository.getId()
            );
            return;
        }

        // first try to retrieve itself.
        // This will trigger download from remote if the capability is on top of a proxy repository

        // use the bypass method so request processors do not trigger again and get into a cycle
        StorageItem recipe = safeRetrieveItemBypassingChecks( repository, path );

        // It could be that the recipe was proxied case when we do not re-generate it
        if ( recipe != null && !recipe.getAttributes().containsKey( GENERATED_AT_ATTRIBUTE ) )
        {
            // TODO if recipe is uploaded and it was generated before this attrib will remain
            getLogger().debug( "OSGi recipe [{}] was not generated by this plugin. Bailing out.", path );
            return;
        }

        RepositoryItemUidLock jarLock = null;
        RepositoryItemUidLock pomLock = null;
        RepositoryItemUidLock recipeLock = null;

        try
        {
            // Retrieve the jar for which the recipe should be created
            final String jarPath = jarPathForRecipe( path );
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
                getLogger().warn(
                    String.format( "OSGi recipe [%s] not created as jar [%s] was not available due to [%s]",
                    path, jarPath, e.getMessage() ), e
                );
                return;
            }

            // Do not create a recipe if jar is already an OSGi bundle
            if ( isAlreadyAnOSGiBundle( jarFile ) )
            {
                getLogger().debug( "[{}] is already an OSGi bundle. Bailing out.", jarPath );
                return;
            }

            // If the pom should be used for recipe and we have a maven repository get (could trigger proxy download)
            // the pom
            final MavenRepository mavenRepository = repository.adaptToFacet( MavenRepository.class );
            StorageItem pom = null;
            if ( configuration.useMavenModel() && mavenRepository != null )
            {
                final Gav jarGav = mavenRepository.getGavCalculator().pathToGav( jarPath );
                final Gav pomGav = pomGavFor( jarGav );
                final String pomPath = mavenRepository.getGavCalculator().gavToPath( pomGav );

                // Lock the pom before getting it so once we get it we know it does not change or is removed
                pomLock = repository.createUid( pomPath ).getLock();
                pomLock.lock( Action.read );

                pom = safeRetrieveItem( mavenRepository, pomGav );
            }

            // Do not regenerate the recipe if is newer then jar and pom
            if ( recipe != null && recipe.getStoredLocally() >= jar.getStoredLocally()
                && ( pom == null || recipe.getStoredLocally() >= pom.getStoredLocally() ) )
            {
                getLogger().debug( "OSGi recipe [{}] is up to date. Bailing out.", path );
                return;
            }

            // Acquire write lock
            recipeLock = repository.createUid( path ).getLock();
            recipeLock.lock( Action.create );

            // Now re-check that we still have to generate the recipe
            recipe = safeRetrieveItemBypassingChecks( repository, path );

            // It could be that the recipe was proxied case when we do not re-generate it
            if ( recipe != null && !recipe.getAttributes().containsKey( GENERATED_AT_ATTRIBUTE ) )
            {
                getLogger().debug( "OSGi recipe [{}] was not generated by this plugin. Bailing out.", path );
                return;
            }
            // Do not regenerate the recipe if is newer then jar and pom
            if ( recipe != null && recipe.getStoredLocally() >= jar.getStoredLocally()
                && ( pom == null || recipe.getStoredLocally() >= pom.getStoredLocally() ) )
            {
                getLogger().debug( "OSGi recipe [{}] is up to date. Bailing out.", path );
                return;
            }

            // (Re)generate recipe
            Properties instructions = null;
            if ( mavenRepository != null )
            {
                instructions = calculateRecipe( configuration, mavenRepository, jar, pom );
            }
            if ( instructions != null )
            {
                createOSGiRecipe( repository, path, instructions );
            }
        }
        finally
        {
            unlock( jarLock, pomLock, recipeLock );
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

    private void createOSGiRecipe( final Repository repository, final String path, final Properties instructions )
    {
        getLogger().debug( "Generating OSGi recipe [{}:{}]", repository.getId(), path );

        try
        {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            instructions.store( out, null );
            final ByteArrayInputStream in = new ByteArrayInputStream( out.toByteArray() );

            final ResourceStoreRequest request = new ResourceStoreRequest( path );

            final Map<String, String> attributes = new HashMap<String, String>();
            attributes.put( GENERATED_AT_ATTRIBUTE, new Date().toString() );

            storeItem( repository, request, in, mimeUtil.getMimeType( path ), attributes );
        }
        catch ( final Exception e )
        {
            getLogger().warn( "Could not store OSGI recipe [{}] tue to [{}]", path, e.getMessage() );
        }
    }

    private Properties calculateRecipe( final BundleMakerConfiguration configuration, final MavenRepository repository,
                                        final StorageItem jar, final StorageItem pom )
    {
        final Properties recipeProperties = new Properties();
        final Gav jarGav = repository.getGavCalculator().pathToGav( jar.getPath() );
        final File jarFile = safeRetrieveFile( repository, jar.getPath() );

        recipeProperties.setProperty( "Bundle-SymbolicName",
            Maven2OSGiUtils.getBundleSymbolicName( jarGav.getGroupId(), jarGav.getArtifactId(), jarFile ) );
        recipeProperties.setProperty( "Bundle-Version", Maven2OSGiUtils.getVersion( jarGav.getVersion() ) );
        recipeProperties.setProperty( "Import-Package", "*" );
        recipeProperties.setProperty( "Export-Package", "*" );

        if ( pom != null )
        {
            try
            {
                final Model model = buildModel( pom, configuration );
                if ( !StringUtils.isEmpty( model.getName() ) )
                {
                    recipeProperties.setProperty( "Bundle-Name", model.getName() );
                }
                if ( !StringUtils.isEmpty( model.getDescription() ) )
                {
                    recipeProperties.setProperty( "Bundle-Description", model.getDescription() );
                }
                // TODO use license, organization, ...
            }
            catch ( final Exception e )
            {
                getLogger().warn(
                    "Could not parse model (POM) [{}] due to [{}]. OSGI recipe will not contain detailed information.",
                    pom.getPath(), e.getMessage() );
            }
        }

        return recipeProperties;
    }

    private Model buildModel( final StorageItem pom, final BundleMakerConfiguration configuration )
        throws ModelBuildingException
    {
        final FileItemModelSource pomSource = new FileItemModelSource( (StorageFileItem) pom );

        final List<MavenRepository> repos = new ArrayList<MavenRepository>();
        for ( final String repoId : configuration.mavenResolverRepositoriesIds() )
        {
            try
            {
                final MavenRepository mavenRepo = repositories.getRepositoryWithFacet( repoId, MavenRepository.class );
                if ( mavenRepo != null )
                {
                    repos.add( mavenRepo );
                }
            }
            catch ( final NoSuchRepositoryException e )
            {
                getLogger().warn(
                    "Could not find Maven repository [{}]. It will not be used while resolving model.", repoId
                );
            }
        }

        return mavenBridge.buildModel( pomSource, repos );
    }

}
