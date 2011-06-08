package org.sonatype.nexus.plugins.bundlemaker.its.bm03;

import org.sonatype.nexus.plugins.bundlemaker.its.BundleMakerProxyIT;
import org.testng.annotations.Test;


public class BM0301ProxyExistentNotEagerlyIT
    extends BundleMakerProxyIT
{

    @Test
    public void proxyExistentNotEagerly()
        throws Exception
    {
        createCapability();

        deployArtifact( getFakeCentralRepositoryId(),
            getTestResourceAsFile( "projects/commons-logging/commons-logging.osgi" ),
            "commons-logging/commons-logging/1.1.1/commons-logging-1.1.1.osgi" );
        deployArtifact( getFakeCentralRepositoryId(),
            getTestResourceAsFile( "projects/commons-logging/commons-logging-osgi.jar" ),
            "commons-logging/commons-logging/1.1.1/commons-logging-1.1.1-osgi.jar" );

        assertRecipeFor( "commons-logging", "commons-logging", "1.1.1" ).matches(
            getTestResourceAsFile( "manifests/bm0301/commons-logging-1.1.1.osgi.properties" ) );

        assertBundleFor( "commons-logging", "commons-logging", "1.1.1" ).matches(
            getTestResourceAsFile( "manifests/bm0301/commons-logging-1.1.1-osgi.jar.properties" ) );
    }
}
