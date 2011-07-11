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

import static org.sonatype.nexus.plugins.bundlemaker.internal.BundleMakerUtils.bundlePathForJar;
import static org.sonatype.nexus.plugins.bundlemaker.internal.BundleMakerUtils.isABundle;
import static org.sonatype.nexus.plugins.bundlemaker.internal.BundleMakerUtils.isAJar;
import static org.sonatype.nexus.plugins.bundlemaker.internal.BundleMakerUtils.isAPom;
import static org.sonatype.nexus.plugins.bundlemaker.internal.BundleMakerUtils.isARecipe;
import static org.sonatype.nexus.plugins.bundlemaker.internal.BundleMakerUtils.isAlreadyAnOSGiBundle;
import static org.sonatype.nexus.plugins.bundlemaker.internal.NexusUtils.deleteItem;
import static org.sonatype.nexus.plugins.bundlemaker.internal.NexusUtils.getRelativePath;
import static org.sonatype.nexus.plugins.bundlemaker.internal.NexusUtils.localStorageOfRepositoryAsFile;
import static org.sonatype.nexus.plugins.bundlemaker.internal.NexusUtils.retrieveFile;
import static org.sonatype.nexus.plugins.bundlemaker.internal.NexusUtils.retrieveItem;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.sonatype.nexus.plugins.bundlemaker.BundleMaker;
import org.sonatype.nexus.plugins.bundlemaker.BundleMakerConfiguration;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.sisu.resource.scanner.helper.ListenerSupport;
import org.sonatype.sisu.resource.scanner.scanners.SerialScanner;

@Named
@Singleton
public class DefaultBundleMaker
    implements BundleMaker
{

    @Inject
    private Logger logger;

    private final Map<String, BundleMakerConfiguration> configurations;

    private final RepositoryRegistry repositories;

    @Inject
    public DefaultBundleMaker( final RepositoryRegistry repositories )
    {
        this.repositories = repositories;
        configurations = new HashMap<String, BundleMakerConfiguration>();
    }

    @Override
    public BundleMakerConfiguration getConfiguration( final String repositoryId )
    {
        return configurations.get( repositoryId );
    }

    @Override
    public void addConfiguration( final BundleMakerConfiguration configuration )
    {
        configurations.put( configuration.repositoryId(), configuration );
    }

    @Override
    public void removeConfiguration( final BundleMakerConfiguration configuration )
    {
        configurations.remove( configuration.repositoryId() );
    }

    @Override
    public void createOSGiVersionOfJar( final StorageItem jar )
    {
        if ( jar == null || !isAJar( jar.getPath() ) )
        {
            logger.warn( "OSGi version of jar [{}] not created as is not a jar", jar.getPath() );
            return;
        }

        File jarFile = null;
        try
        {
            final Repository repository = repositories.getRepository( jar.getRepositoryId() );
            jarFile = retrieveFile( repository, jar.getPath() );
        }
        catch ( final Exception e )
        {
            logger.warn(
                String.format( "OSGi version of jar [%s] not created as it was not available due to [%s]",
                    jar.getPath(), e.getMessage() ), e );
            return;
        }

        if ( isAlreadyAnOSGiBundle( jarFile ) )
        {
            logger.debug( "[{}] is already an OSGi bundle. Bailing out.", jar.getPath() );
            return;
        }

        final BundleMakerConfiguration configuration = getConfiguration( jar.getRepositoryId() );
        if ( configuration == null )
        {
            logger.debug(
                "OSGi version of jar [{}] not created as Bundle Maker capability is not enabled for repository [{}]",
                jar.getPath(), jar.getRepositoryId() );
            return;
        }

        try
        {
            final Repository repository = repositories.getRepository( jar.getRepositoryId() );

            final String bundlePath = bundlePathForJar( jar.getPath() );

            // force repository to generate the bundle
            final ResourceStoreRequest request = new ResourceStoreRequest( bundlePath );
            repository.retrieveItem( request );
        }
        catch ( final Exception e )
        {
            logger.warn(
                String.format( "OSGi version of jar [%s] not created due to [%s]", jar.getPath(), e.getMessage() ), e );
        }
    }

    @Override
    public void removeOSGiVersionOfJar( final StorageItem jar )
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void createOSGiVersionOfJarsWithPom( final StorageItem pom )
    {
        if ( pom == null || !isAPom( pom.getPath() ) )
        {
            logger.warn( "OSGi versions of jars with pom [{}] not created as is not a pom", pom.getPath() );
            return;
        }

        final BundleMakerConfiguration configuration = getConfiguration( pom.getRepositoryId() );
        if ( configuration == null )
        {
            logger.debug(
                "OSGi versions of jars with pom [{}] not created as Bundle Maker capability is not enabled for repository [{}]",
                pom.getPath(), pom.getRepositoryId() );
            return;
        }

        try
        {
            final Repository repository = repositories.getRepository( pom.getRepositoryId() );
            final ResourceStoreRequest request = new ResourceStoreRequest( pom.getParentPath() );
            final Collection<StorageItem> items = repository.list( request );
            if ( items != null )
            {
                for ( final StorageItem item : items )
                {
                    if ( isAJar( item.getPath() ) && !isABundle( item.getPath() ) )
                    {
                        createOSGiVersionOfJar( item );
                    }
                }
            }
        }
        catch ( final Exception e )
        {
            logger.warn( String.format(
                "OSGi versions jars with pom [%s] not created as repository [%s] could not be scanned due to [%s]",
                pom.getPath(), pom.getRepositoryId(), e.getMessage() ), e );
        }
    }

    @Override
    public void scanAndRebuild( final String repositoryId, final String resourceStorePath,
                                final boolean forceRegeneration )
    {
        logger.debug( "Rebuilding OSGi bundles for repository [{}], path [{}]", repositoryId, resourceStorePath );

        final BundleMakerConfiguration configuration = getConfiguration( repositoryId );
        if ( configuration == null )
        {
            logger.warn(
                "Rebuilding OSGi bundles for [{}] not executed as Bundle Maker capability is not enabled for this repository",
                repositoryId );
            return;
        }

        try
        {
            final Repository repository = repositories.getRepository( repositoryId );
            final File localStorage = localStorageOfRepositoryAsFile( repository );
            File scanPath = localStorage;
            if ( resourceStorePath != null )
            {
                scanPath = new File( scanPath, resourceStorePath );
            }

            if ( forceRegeneration )
            {
                new SerialScanner().scan( scanPath, new ListenerSupport()
                {

                    @Override
                    public void onFile( final File file )
                    {
                        if ( isARecipe( file.getPath() ) )
                        {
                            final String path = getRelativePath( localStorage, file );
                            try
                            {
                                deleteItem( repository, path );
                            }
                            catch ( final Exception e )
                            {
                                logger.warn( String.format(
                                    "Recipe [%s] could not be deleted in order to force its regeneration due to [%s]",
                                    path, e.getMessage() ), e );
                            }
                        }
                    }

                } );
            }

            new SerialScanner().scan( scanPath, new ListenerSupport()
            {

                @Override
                public void onFile( final File file )
                {
                    if ( isAJar( file.getPath() ) && !isABundle( file.getPath() ) )
                    {
                        final String path = getRelativePath( localStorage, file );
                        try
                        {
                            final StorageItem jar = retrieveItem( repository, path );
                            createOSGiVersionOfJar( jar );
                        }
                        catch ( final Exception e )
                        {
                            logger.warn(
                                String.format( "OSGi version of jar [%s] not created due to [%s]", path, e.getMessage() ),
                                e );
                        }
                    }
                }

            } );
        }
        catch ( final Exception e )
        {
            logger.warn( String.format(
                "Rebuilding OSGI bundles not executed as repository [%s] could not be scanned due to [%s]",
                repositoryId, e.getMessage() ), e );
        }
    }

    @Override
    public void scanAndRebuild( final String resourceStorePath, final boolean forceRegeneration )
    {
        for ( final Repository repository : repositories.getRepositories() )
        {
            scanAndRebuild( repository.getId(), resourceStorePath, forceRegeneration );
        }
    }

}
