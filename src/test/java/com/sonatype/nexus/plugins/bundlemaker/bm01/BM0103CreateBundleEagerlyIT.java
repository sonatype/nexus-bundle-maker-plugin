package com.sonatype.nexus.plugins.bundlemaker.bm01;

import static com.sonatype.nexus.plugins.bundlemaker.CapabilitiesServiceClient.property;
import static org.testng.Assert.assertTrue;

import java.io.File;

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
        final File bundle =
            new File( new File( nexusWorkDir ), "storage/" + testRepositoryId
                + "/commons-logging/commons-logging/1.1.1/commons-logging-1.1.1-osgi.jar" );
        assertTrue( bundle.exists(), "Bundle " + bundle.getPath() + "created" );

        assertBundleManifestOf( bundle ).matches(
            getTestResourceAsFile( "manifests/bm0101/commons-logging-1.1.1-osgi.jar.properties" ) );
    }

}
