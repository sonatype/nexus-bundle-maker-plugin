package org.sonatype.nexus.plugins.bundlemaker.its.bm04;

import static org.testng.Assert.assertTrue;

import java.io.File;

import org.sonatype.nexus.plugins.bundlemaker.its.BundleMakerIT;
import org.testng.annotations.Test;

public class BM0402ForcedRebuildBundlesTaskIT
    extends BundleMakerIT
{

    public BM0402ForcedRebuildBundlesTaskIT()
    {
        super( "bm04" );
    }

    /**
     * Deploy jars before bundle maker capability is enabled, and check recipe/bundle after rebuild task. Verify that a
     * forced rebuild task recreates them.
     */
    @Test
    public void test()
        throws Exception
    {
        // capability is not eagerly so bundle is not created when jars are deployed
        createCapability();

        deployArtifacts( getTestResourceAsFile( "artifacts/jars" ) );

        runTask( false );

        File recipe = storageRecipeFor( "commons-logging", "commons-logging", "1.1.1" );
        assertTrue( recipe.exists(), "Recipe " + recipe.getPath() + "created" );
        final long lastModified = recipe.lastModified();

        // make a pause so we do not regenerate recipe in same second
        Thread.sleep( 1000 );

        runTask( true );

        recipe = storageRecipeFor( "commons-logging", "commons-logging", "1.1.1" );
        assertTrue( recipe.exists(), "Recipe " + recipe.getPath() + "created" );

        assertTrue( recipe.lastModified() > lastModified, "Recipe was recreated" );

        assertStorageRecipeFor( "commons-logging", "commons-logging", "1.1.1" ).matches(
            getTestResourceAsFile( "manifests/bm0401/commons-logging-1.1.1.osgi.properties" ) );

        assertStorageBundleFor( "commons-logging", "commons-logging", "1.1.1" ).matches(
            getTestResourceAsFile( "manifests/bm0401/commons-logging-1.1.1-osgi.jar.properties" ) );
    }

}
