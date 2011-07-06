package org.sonatype.nexus.plugins.bundlemaker.its.bm01;

import org.sonatype.nexus.plugins.bundlemaker.internal.capabilities.EagerFormField;
import org.sonatype.nexus.plugins.bundlemaker.its.BundleMakerIT;
import org.testng.annotations.Test;

public class BM0103CreateBundleEagerlyIT
    extends BundleMakerIT
{

    public BM0103CreateBundleEagerlyIT()
    {
        super( "bm01" );
    }

    /**
     * When a jar is deployed and bundle maker capability is eager, recipe/bundle is created on deploy.
     */
    @Test
    public void test()
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
