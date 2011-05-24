package com.sonatype.nexus.plugins.bundlemaker.bm02;

import static com.sonatype.nexus.plugins.bundlemaker.CapabilitiesServiceClient.property;

import org.sonatype.nexus.plugins.bundlemaker.internal.capabilities.EagerFormField;
import org.testng.annotations.Test;

import com.sonatype.nexus.plugins.bundlemaker.BundleMakerProxyIT;

public class BM0202ProxyEagerlyIT
    extends BundleMakerProxyIT
{

    @Test
    public void proxyNotEagerly()
        throws Exception
    {
        createCapability( property( EagerFormField.ID, "true" ) );

        assertRecipeFor( "commons-logging", "commons-logging", "1.1.1" ).matches(
            getTestResourceAsFile( "manifests/bm0201/commons-logging-1.1.1.osgi.properties" ) );

        assertBundleFor( "commons-logging", "commons-logging", "1.1.1" ).matches(
            getTestResourceAsFile( "manifests/bm0201/commons-logging-1.1.1-osgi.jar.properties" ) );
    }
}
