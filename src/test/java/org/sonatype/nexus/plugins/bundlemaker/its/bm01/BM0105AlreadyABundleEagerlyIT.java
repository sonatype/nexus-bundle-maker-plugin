package org.sonatype.nexus.plugins.bundlemaker.its.bm01;

import static org.sonatype.nexus.plugins.bundlemaker.its.CapabilitiesServiceClient.property;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;

import org.sonatype.nexus.plugins.bundlemaker.internal.capabilities.EagerFormField;
import org.sonatype.nexus.plugins.bundlemaker.its.BundleMakerIT;
import org.testng.annotations.Test;

public class BM0105AlreadyABundleEagerlyIT
    extends BundleMakerIT
{

    @Test
    public void existingBundleEagerly()
        throws Exception
    {
        createCapability( property( EagerFormField.ID, "true" ) );

        deployArtifacts( getTestResourceAsFile( "artifacts/jars" ) );
        final File recipe =
            new File( new File( nexusWorkDir ), "storage/" + getTestRepositoryId()
                + "/org/ops4j/base/ops4j-base-lang/1.2.3/ops4j-base-lang-1.2.3.osgi" );
        assertFalse( recipe.exists(), "Recipe " + recipe.getPath() + " created" );

        final File bundle =
            new File( new File( nexusWorkDir ), "storage/" + getTestRepositoryId()
                + "/org/ops4j/base/ops4j-base-lang/1.2.3/ops4j-base-lang-1.2.3-osgi.jar" );
        assertFalse( bundle.exists(), "Bundle " + bundle.getPath() + " created" );

        final File downloadDir = new File( "target/downloads/" + this.getClass().getSimpleName() );

        try
        {
            downloadArtifact( "org.ops4j.base", "ops4j-base-lang", "1.2.3", "osgi", null, downloadDir.getPath() );
            fail( "Expected a FileNotFoundException" );
        }
        catch ( final FileNotFoundException expected )
        {
        }

        downloadArtifact( "org.ops4j.base", "ops4j-base-lang", "1.2.3", "jar", "osgi", downloadDir.getPath() );
    }

}
