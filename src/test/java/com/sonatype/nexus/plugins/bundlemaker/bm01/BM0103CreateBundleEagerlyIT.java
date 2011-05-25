package com.sonatype.nexus.plugins.bundlemaker.bm01;

import static com.sonatype.nexus.plugins.bundlemaker.CapabilitiesServiceClient.property;

import org.sonatype.nexus.plugins.bundlemaker.internal.capabilities.EagerFormField;
import org.testng.annotations.Test;

import com.sonatype.nexus.plugins.bundlemaker.BundleMakerIT;

public class BM0103CreateBundleEagerlyIT
    extends BundleMakerIT
{

    @Test
    public void createBundleEagerly()
        throws Exception
    {
        createCapability( property( EagerFormField.ID, "true" ) );

        deployArtifacts( getTestResourceAsFile( "artifacts/jars" ) );

        assertStorageRecipeFor( "commons-logging", "commons-logging", "1.1.1" ).matches(
            getTestResourceAsFile( "manifests/bm0101/commons-logging-1.1.1.osgi.properties" ) );

        assertStorageBundleFor( "commons-logging", "commons-logging", "1.1.1" ).matches(
            getTestResourceAsFile( "manifests/bm0101/commons-logging-1.1.1-osgi.jar.properties" ) );
    }
}
