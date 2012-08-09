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
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.plugins.bundlemaker.internal.capabilities.EagerFormField;
import org.sonatype.nexus.plugins.bundlemaker.internal.capabilities.RemoteRepositoriesFormField;
import org.sonatype.nexus.plugins.bundlemaker.internal.capabilities.UseMavenModelFormField;
import com.google.common.base.Throwables;

public class BundleMakerHostedIT
    extends BundleMakerITSupport
{

    static final String NO_CLASSIFIER = null;

    public BundleMakerHostedIT( final String nexusBundleCoordinates )
    {
        super( "bundle-maker", nexusBundleCoordinates );
    }

    @Before
    public void createHostedRepositoryOnStart()
    {
        try
        {
            repositoriesNRC().createMavenHostedReleaseRepository( getTestRepositoryId() );
        }
        catch ( IOException e )
        {
            throw Throwables.propagate( e );
        }
    }

    /**
     * Match recipe when maven model is not used.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void recipeWithoutUsingMavenModel()
        throws Exception
    {
        createCapability( property( EagerFormField.ID, "true" ) );

        deployNRC().deployUsingPomWithRest(
            getTestRepositoryId(),
            testData().resolveFile( "artifacts/commons-logging.jar" ),
            testData().resolveFile( "artifacts/commons-logging.pom" ),
            NO_CLASSIFIER,
            "jar"
        );

        assertRecipeFor( "commons-logging", "commons-logging", "1.1.1" )
            .matches( testData().resolveFile( "manifests/commons-logging-1.1.1.osgi.properties" ) );

        assertBundleFor( "commons-logging", "commons-logging", "1.1.1" )
            .matches( testData().resolveFile( "manifests/commons-logging-1.1.1-osgi.jar.properties" ) );
    }

    /**
     * Match recipe when maven model is used.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void recipeUsingMavenModel()
        throws Exception
    {
        repositoriesNRC().createMavenHostedReleaseRepository( getTestRepositoryId() + "-central" );

        createCapability(
            property( EagerFormField.ID, "true" ),
            property( UseMavenModelFormField.ID, "true" ),
            property( RemoteRepositoriesFormField.ID, getTestRepositoryId() + "-central" )
        );

        deployNRC().deployPomWithRest(
            getTestRepositoryId(),
            testData().resolveFile( "artifacts/apache.pom" )
        );

        deployNRC().deployPomWithRest(
            getTestRepositoryId(),
            testData().resolveFile( "artifacts/commons-parent.pom" )
        );

        deployNRC().deployUsingPomWithRest(
            getTestRepositoryId(),
            testData().resolveFile( "artifacts/commons-logging.jar" ),
            testData().resolveFile( "artifacts/commons-logging.pom" ),
            NO_CLASSIFIER,
            "jar"
        );

        assertRecipeFor( "commons-logging", "commons-logging", "1.1.1" )
            .matches( testData().resolveFile( "manifests/commons-logging-1.1.1.osgi.properties" ) );

        assertBundleFor( "commons-logging", "commons-logging", "1.1.1" )
            .matches( testData().resolveFile( "manifests/commons-logging-1.1.1-osgi.jar.properties" ) );
    }

    /**
     * When a jar is deployed and bundle maker capability is eager, recipe/bundle is created on deploy.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void createBundleEagerly()
        throws Exception
    {
        createCapability( property( EagerFormField.ID, "true" ) );

        deployNRC().deployUsingPomWithRest(
            getTestRepositoryId(),
            testData().resolveFile( "artifacts/commons-logging.jar" ),
            testData().resolveFile( "artifacts/commons-logging.pom" ),
            NO_CLASSIFIER,
            "jar"
        );

        assertRecipeFor( "commons-logging", "commons-logging", "1.1.1" )
            .matches( testData().resolveFile( "manifests/commons-logging-1.1.1.osgi.properties" ) );

        assertBundleFor( "commons-logging", "commons-logging", "1.1.1" )
            .matches( testData().resolveFile( "manifests/commons-logging-1.1.1-osgi.jar.properties" ) );
    }

    /**
     * When an jar gets deployed and bundle maker capability is not eager the recipe/bundle is created only on download.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void createBundleNotEagerly()
        throws Exception
    {
        createCapability();

        deployNRC().deployUsingPomWithRest(
            getTestRepositoryId(),
            testData().resolveFile( "artifacts/commons-logging.jar" ),
            testData().resolveFile( "artifacts/commons-logging.pom" ),
            NO_CLASSIFIER,
            "jar"
        );

        final File recipe = new File(
            nexus().getWorkDirectory(),
            "storage/" + getTestRepositoryId() + "/commons-logging/commons-logging/1.1.1/commons-logging-1.1.1.osgi"
        );
        assertThat( "Recipe " + recipe.getPath() + " created", recipe.exists(), is( false ) );

        final File bundle = new File(
            nexus().getWorkDirectory(),
            "storage/" + getTestRepositoryId() + "/commons-logging/commons-logging/1.1.1/commons-logging-1.1.1-osgi.jar"
        );
        assertThat( "Bundle " + bundle.getPath() + " created", bundle.exists(), is( false ) );

        assertRecipeFor( "commons-logging", "commons-logging", "1.1.1" )
            .matches( testData().resolveFile( "manifests/commons-logging-1.1.1.osgi.properties" ) );

        assertBundleFor( "commons-logging", "commons-logging", "1.1.1" )
            .matches( testData().resolveFile( "manifests/commons-logging-1.1.1-osgi.jar.properties" ) );
    }

    /**
     * When an OSGi bundle is deployed and bundle maker capability is eager a bundle link is created on deploy. Recipe
     * should not be created.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void alreadyABundleEagerly()
        throws Exception
    {
        createCapability( property( EagerFormField.ID, "true" ) );

        deployNRC().deployUsingPomWithRest(
            getTestRepositoryId(),
            testData().resolveFile( "artifacts/ops4j-base-lang.jar" ),
            testData().resolveFile( "artifacts/ops4j-base-lang.pom" ),
            NO_CLASSIFIER,
            "jar"
        );

        final File recipe = new File(
            nexus().getWorkDirectory(),
            "storage/" + getTestRepositoryId() + "/org/ops4j/base/ops4j-base-lang/1.2.3/ops4j-base-lang-1.2.3.osgi"
        );
        assertThat( "Recipe " + recipe.getPath() + " created", recipe.exists(), is( false ) );

        final File bundle = new File(
            nexus().getWorkDirectory(),
            "storage/" + getTestRepositoryId() + "/org/ops4j/base/ops4j-base-lang/1.2.3/ops4j-base-lang-1.2.3-osgi.jar"
        );
        assertThat( "Bundle " + bundle.getPath() + " created", bundle.exists(), is( false ) );

        try
        {
            downloadArtifact( "org.ops4j.base", "ops4j-base-lang", "1.2.3", "osgi", NO_CLASSIFIER );
            assertThat( "Expected a FileNotFoundException", false );
        }
        catch ( final FileNotFoundException expected )
        {
        }

        downloadArtifact( "org.ops4j.base", "ops4j-base-lang", "1.2.3", "jar", "osgi" );
    }

    /**
     * When an OSGI bundle is deployed and bundle maker capability is not eager a bundle link is created only on
     * download. Recipe should not be created.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void alreadyABundleNotEagerly()
        throws Exception
    {
        createCapability();

        deployNRC().deployUsingPomWithRest(
            getTestRepositoryId(),
            testData().resolveFile( "artifacts/ops4j-base-lang.jar" ),
            testData().resolveFile( "artifacts/ops4j-base-lang.pom" ),
            NO_CLASSIFIER,
            "jar"
        );

        final File recipe = new File(
            nexus().getWorkDirectory(),
            "storage/" + getTestRepositoryId() + "/org/ops4j/base/ops4j-base-lang/1.2.3/ops4j-base-lang-1.2.3.osgi"
        );
        assertThat( "Recipe " + recipe.getPath() + " created", recipe.exists(), is( false ) );

        final File bundle = new File(
            nexus().getWorkDirectory(),
            "storage/" + getTestRepositoryId() + "/org/ops4j/base/ops4j-base-lang/1.2.3/ops4j-base-lang-1.2.3-osgi.jar"
        );
        assertThat( "Bundle " + bundle.getPath() + " created", bundle.exists(), is( false ) );

        try
        {
            downloadArtifact( "org.ops4j.base", "ops4j-base-lang", "1.2.3", "osgi", NO_CLASSIFIER );
            assertThat( "Expected a FileNotFoundException", false );
        }
        catch ( final FileNotFoundException expected )
        {
        }

        downloadArtifact( "org.ops4j.base", "ops4j-base-lang", "1.2.3", "jar", "osgi" );
    }

}
