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
import com.google.common.base.Throwables;

public class BundleMakerProxyIT
    extends BundleMakerITSupport
{

    static final String NO_CLASSIFIER = null;

    public BundleMakerProxyIT(final String nexusBundleCoordinates)
    {
        super( "bundle-maker", nexusBundleCoordinates );
    }

    @Before
    public void createProxyRepositoryOnStart()
    {
        try
        {
            repositoriesNRC().createMavenHostedReleaseRepository( getTestProxiedRepositoryId() );
            repositoriesNRC().createMavenProxyReleaseRepository(
                getTestRepositoryId(),
                nexus().getUrl() + "content/repositories/" + getTestProxiedRepositoryId()
            );
        }
        catch ( IOException e )
        {
            throw Throwables.propagate( e );
        }
    }

    private String getTestProxiedRepositoryId()
    {
        return getTestRepositoryId() + "-proxied";
    }

    /**
     * Download a recipe/bundle form a proxy repository, bundle maker capability eager.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void proxyEagerly()
        throws Exception
    {
        createCapability( property( EagerFormField.ID, "true" ) );

        deployNRC().deployUsingPomWithRest(
            getTestProxiedRepositoryId(),
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
     * Download a recipe/bundle form a proxy repository, bundle maker capability not eager.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void proxyNotEagerly()
        throws Exception
    {
        createCapability();

        deployNRC().deployUsingPomWithRest(
            getTestProxiedRepositoryId(),
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
     * For an OSGi bundle recipe is not created and there is a link to original bundle, bundle maker capability not
     * eager.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void proxyAlreadyABundleEagerly()
        throws Exception
    {
        createCapability( property( EagerFormField.ID, "true" ) );

        deployNRC().deployUsingPomWithRest(
            getTestProxiedRepositoryId(),
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
     * For an OSGi bundle recipe is not created and there is a link to original bundle, bundle maker capability eager.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void proxyAlreadyABundleNotEagerly()
        throws Exception
    {
        createCapability();

        deployNRC().deployUsingPomWithRest(
            getTestProxiedRepositoryId(),
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
     * Recipe bundle are not created when those already exists, bundle maker capability not eager.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void proxyExistentNotEagerly()
        throws Exception
    {
        deployNRC().deployUsingPomWithRest(
            getTestProxiedRepositoryId(),
            testData().resolveFile( "artifacts/commons-logging.jar" ),
            testData().resolveFile( "artifacts/commons-logging.pom" ),
            NO_CLASSIFIER,
            "jar"
        );

        deployNRC().deployWithRest(
            getTestProxiedRepositoryId(),
            "commons-logging",
            "commons-logging",
            "1.1.1",
            NO_CLASSIFIER,
            "osgi",
            testData().resolveFile( "artifacts/commons-logging.osgi" )
        );

        deployNRC().deployWithRest(
            getTestProxiedRepositoryId(),
            "commons-logging",
            "commons-logging",
            "1.1.1",
            "osgi",
            "jar",
            testData().resolveFile( "artifacts/commons-logging-osgi.jar" )
        );

        createCapability();

        assertRecipeFor( "commons-logging", "commons-logging", "1.1.1" )
            .matches( testData().resolveFile( "manifests/commons-logging-1.1.1.osgi.properties" ) );

        assertBundleFor( "commons-logging", "commons-logging", "1.1.1" )
            .matches( testData().resolveFile( "manifests/commons-logging-1.1.1-osgi.jar.properties" ) );
    }

    /**
     * Recipe bundle are not created when those already exists, bundle maker capability eager.
     *
     * @throws Exception re-thrown
     */
    @Test
    public void proxyExistentEagerly()
        throws Exception
    {
        deployNRC().deployUsingPomWithRest(
            getTestProxiedRepositoryId(),
            testData().resolveFile( "artifacts/commons-logging.jar" ),
            testData().resolveFile( "artifacts/commons-logging.pom" ),
            NO_CLASSIFIER,
            "jar"
        );

        deployNRC().deployWithRest(
            getTestProxiedRepositoryId(),
            "commons-logging",
            "commons-logging",
            "1.1.1",
            NO_CLASSIFIER,
            "osgi",
            testData().resolveFile( "artifacts/commons-logging.osgi" )
        );

        deployNRC().deployWithRest(
            getTestProxiedRepositoryId(),
            "commons-logging",
            "commons-logging",
            "1.1.1",
            "osgi",
            "jar",
            testData().resolveFile( "artifacts/commons-logging-osgi.jar" )
        );

        createCapability( property( EagerFormField.ID, "true" ) );

        assertRecipeFor( "commons-logging", "commons-logging", "1.1.1" )
            .matches( testData().resolveFile( "manifests/commons-logging-1.1.1.osgi.properties" ) );

        assertBundleFor( "commons-logging", "commons-logging", "1.1.1" )
            .matches( testData().resolveFile( "manifests/commons-logging-1.1.1-osgi.jar.properties" ) );
    }

}
