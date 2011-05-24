package com.sonatype.nexus.plugins.bundlemaker.bm01;

import org.testng.annotations.Test;

import com.sonatype.nexus.plugins.bundlemaker.BundleMakerIT;

public class BM0104BundleCreationNotEagerlyIT
    extends BundleMakerIT
{

    @Test
    public void bundleCreationNotEagerly()
        throws Exception
    {
        createCapability();

        deployArtifacts( getTestResourceAsFile( "artifacts/jars" ) );

        assertRecipeFor( "commons-logging", "commons-logging", "1.1.1" ).matches(
            getTestResourceAsFile( "manifests/bm0101/commons-logging-1.1.1.osgi.properties" ) );

        assertBundleFor( "commons-logging", "commons-logging", "1.1.1" ).matches(
            getTestResourceAsFile( "manifests/bm0101/commons-logging-1.1.1-osgi.jar.properties" ) );
    }

}
