package com.sonatype.nexus.plugins.bundlemaker.bm04;

import org.testng.annotations.Test;

import com.sonatype.nexus.plugins.bundlemaker.BundleMakerIT;

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
