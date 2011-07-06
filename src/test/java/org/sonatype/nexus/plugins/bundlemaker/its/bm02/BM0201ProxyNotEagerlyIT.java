package org.sonatype.nexus.plugins.bundlemaker.its.bm02;

import org.sonatype.nexus.plugins.bundlemaker.its.BundleMakerProxyIT;
import org.testng.annotations.Test;

public class BM0201ProxyNotEagerlyIT
    extends BundleMakerProxyIT
{

    public BM0201ProxyNotEagerlyIT()
    {
        super( "bm02" );
    }

    /**
     * Download a recipe/bundle form a proxy repository, bundle maker capability not eager.
     */
    @Test
    public void test()
        throws Exception
    {
        createCapability();

        assertRecipeFor( "commons-logging", "commons-logging", "1.1.1" ).matches(
            getTestResourceAsFile( "manifests/bm0201/commons-logging-1.1.1.osgi.properties" ) );

        assertBundleFor( "commons-logging", "commons-logging", "1.1.1" ).matches(
            getTestResourceAsFile( "manifests/bm0201/commons-logging-1.1.1-osgi.jar.properties" ) );
    }
}
