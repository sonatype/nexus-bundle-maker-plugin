package org.sonatype.nexus.plugins.bundlemaker.its.bm04;

import org.sonatype.nexus.plugins.bundlemaker.its.BundleMakerIT;
import org.testng.annotations.Test;


public class BM0401RebuildBundlesTaskIT
    extends BundleMakerIT
{

    @Test
    public void rebuildBundlesTask()
        throws Exception
    {
        // capability is not eagerly so bundle is not created when jars are deployed
        createCapability();

        deployArtifacts( getTestResourceAsFile( "artifacts/jars" ) );

        runTask();

        assertStorageRecipeFor( "commons-logging", "commons-logging", "1.1.1" ).matches(
            getTestResourceAsFile( "manifests/bm0401/commons-logging-1.1.1.osgi.properties" ) );

        assertStorageBundleFor( "commons-logging", "commons-logging", "1.1.1" ).matches(
            getTestResourceAsFile( "manifests/bm0401/commons-logging-1.1.1-osgi.jar.properties" ) );
    }

}
