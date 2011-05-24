package com.sonatype.nexus.plugins.bundlemaker.bm02;

import org.testng.annotations.Test;

import com.sonatype.nexus.plugins.bundlemaker.BundleMakerProxyIT;

public class BM0201ProxyNotEagerlyIT
    extends BundleMakerProxyIT
{

    @Test
    public void proxyNotEagerly()
        throws Exception
    {
        createCapability();

        assertRecipeFor( "commons-logging", "commons-logging", "1.1.1" ).matches(
            getTestResourceAsFile( "manifests/bm0201/commons-logging-1.1.1.osgi.properties" ) );

        assertBundleFor( "commons-logging", "commons-logging", "1.1.1" ).matches(
            getTestResourceAsFile( "manifests/bm0201/commons-logging-1.1.1-osgi.jar.properties" ) );
    }
}
