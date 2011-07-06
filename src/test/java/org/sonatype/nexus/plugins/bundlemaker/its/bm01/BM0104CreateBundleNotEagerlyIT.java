package org.sonatype.nexus.plugins.bundlemaker.its.bm01;

import static org.testng.Assert.assertFalse;

import java.io.File;

import org.sonatype.nexus.plugins.bundlemaker.its.BundleMakerIT;
import org.testng.annotations.Test;

public class BM0104CreateBundleNotEagerlyIT
    extends BundleMakerIT
{

    public BM0104CreateBundleNotEagerlyIT()
    {
        super( "bm01" );
    }

    /**
     * When an jar gets deployed and bundle maker capability is not eager the recipe/bundle is created only on download.
     */
    @Test
    public void test()
        throws Exception
    {
        createCapability();

        deployArtifacts( getTestResourceAsFile( "artifacts/jars" ) );

        final File recipe =
            new File( new File( nexusWorkDir ), "storage/" + getTestRepositoryId()
                + "/commons-logging/commons-logging/1.1.1/commons-logging-1.1.1.osgi" );
        assertFalse( recipe.exists(), "Recipe " + recipe.getPath() + " created" );

        final File bundle =
            new File( new File( nexusWorkDir ), "storage/" + getTestRepositoryId()
                + "/commons-logging/commons-logging/1.1.1/commons-logging-1.1.1-osgi.jar" );
        assertFalse( bundle.exists(), "Bundle " + bundle.getPath() + " created" );

        assertRecipeFor( "commons-logging", "commons-logging", "1.1.1" ).matches(
            getTestResourceAsFile( "manifests/bm0101/commons-logging-1.1.1.osgi.properties" ) );

        assertBundleFor( "commons-logging", "commons-logging", "1.1.1" ).matches(
            getTestResourceAsFile( "manifests/bm0101/commons-logging-1.1.1-osgi.jar.properties" ) );
    }

}
