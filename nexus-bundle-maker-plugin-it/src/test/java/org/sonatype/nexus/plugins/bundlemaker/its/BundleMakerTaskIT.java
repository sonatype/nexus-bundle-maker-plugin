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
package org.sonatype.nexus.plugins.bundlemaker.its;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.plugins.bundlemaker.internal.tasks.BundleMakerRebuildTaskDescriptor;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.TasksNexusRestClient;
import com.google.common.base.Throwables;

public class BundleMakerTaskIT
    extends BundleMakerITSupport
{

    private static final String NO_CLASSIFIER = null;

    @Inject
    @Named( "${NexusITSupport.nexus-it-helper-plugin-coordinates}" )
    private String itHelperPluginCoordinates;

    public BundleMakerTaskIT()
    {
        super( "bundle-maker" );
    }

    @Override
    protected NexusBundleConfiguration configureNexus( final NexusBundleConfiguration configuration )
    {
        super.configureNexus( configuration );
        return configuration.addPlugins(
            resolveArtifact( itHelperPluginCoordinates )
        );
    }

    @Before
    @Override
    public void setUp()
    {
        super.setUp();

        try
        {
            repositoriesNRC().createMavenHostedReleaseRepository( getTestRepositoryId() );
        }
        catch ( IOException e )
        {
            throw Throwables.propagate( e );
        }
    }

    protected void runBundleMakerTask( final boolean forceRegeneration )
        throws Exception
    {
        final List<ScheduledServicePropertyResource> properties = new ArrayList<ScheduledServicePropertyResource>();

        final ScheduledServicePropertyResource repo = TasksNexusRestClient.newProperty(
            BundleMakerRebuildTaskDescriptor.REPOSITORY_FIELD_ID, getTestRepositoryId()
        );

        properties.add( repo );

        if ( forceRegeneration )
        {
            final ScheduledServicePropertyResource forced = TasksNexusRestClient.newProperty(
                BundleMakerRebuildTaskDescriptor.FORCED_REGENERATION_FIELD_ID, "true"
            );

            properties.add( forced );
        }

        tasksNRC().runTask(
            BundleMakerRebuildTaskDescriptor.ID + System.currentTimeMillis(),
            BundleMakerRebuildTaskDescriptor.ID,
            properties.toArray( new ScheduledServicePropertyResource[properties.size()] )
        );
    }

    /**
     * Deploy jars before bundle maker capability is enabled, and check recipe/bundle after rebuild task.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void rebuildBundlesTask()
        throws Exception
    {
        // capability is not eagerly so bundle is not created when jars are deployed
        createCapability();

        deployNRC().deployUsingPomWithRest(
            getTestRepositoryId(),
            resolveTestFile( "artifacts/commons-logging.jar" ),
            resolveTestFile( "artifacts/commons-logging.pom" ),
            NO_CLASSIFIER,
            "jar"
        );

        runBundleMakerTask( false );

        assertStorageRecipeFor( "commons-logging", "commons-logging", "1.1.1" )
            .matches( resolveTestFile( "manifests/commons-logging-1.1.1.osgi.properties" ) );

        assertStorageBundleFor( "commons-logging", "commons-logging", "1.1.1" )
            .matches( resolveTestFile( "manifests/commons-logging-1.1.1-osgi.jar.properties" ) );
    }

    /**
     * Deploy jars before bundle maker capability is enabled, and check recipe/bundle after rebuild task. Verify that a
     * forced rebuild task recreates them.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void forcedRebuildBundlesTask()
        throws Exception
    {
        // capability is not eagerly so bundle is not created when jars are deployed
        createCapability();

        deployNRC().deployUsingPomWithRest(
            getTestRepositoryId(),
            resolveTestFile( "artifacts/commons-logging.jar" ),
            resolveTestFile( "artifacts/commons-logging.pom" ),
            NO_CLASSIFIER,
            "jar"
        );

        runBundleMakerTask( false );

        File recipe = storageRecipeFor( "commons-logging", "commons-logging", "1.1.1" );
        assertThat( "Recipe " + recipe.getPath() + "created", recipe.exists(), is( true ) );
        final long lastModified = recipe.lastModified();

        // make a pause so we do not regenerate recipe in same second
        Thread.sleep( 1000 );

        runBundleMakerTask( true );

        recipe = storageRecipeFor( "commons-logging", "commons-logging", "1.1.1" );
        assertThat( "Recipe " + recipe.getPath() + "created", recipe.exists(), is( true ) );

        assertThat( "Recipe was recreated", recipe.lastModified() > lastModified, is( true ) );

        assertStorageRecipeFor( "commons-logging", "commons-logging", "1.1.1" )
            .matches( resolveTestFile( "manifests/commons-logging-1.1.1.osgi.properties" ) );

        assertStorageBundleFor( "commons-logging", "commons-logging", "1.1.1" )
            .matches( resolveTestFile( "manifests/commons-logging-1.1.1-osgi.jar.properties" ) );
    }

}
