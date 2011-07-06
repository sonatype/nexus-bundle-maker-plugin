package org.sonatype.nexus.plugins.bundlemaker.its.bm01;

import org.sonatype.nexus.plugins.bundlemaker.internal.capabilities.EagerFormField;
import org.sonatype.nexus.plugins.bundlemaker.its.BundleMakerIT;
import org.testng.annotations.Test;

public class BM0101RecipeWithoutUsingMavenModelIT
    extends BundleMakerIT
{

    public BM0101RecipeWithoutUsingMavenModelIT()
    {
        super( "bm01" );
    }

    /**
     * Match recipe when maven model is not used.
     */
    @Test
    public void test()
        throws Exception
    {
        createCapability( property( EagerFormField.ID, "true" ) );

        deployArtifacts( getTestResourceAsFile( "artifacts/jars" ) );

        assertRecipeFor( "commons-logging", "commons-logging", "1.1.1" ).matches(
            getTestResourceAsFile( "manifests/bm0101/commons-logging-1.1.1.osgi.properties" ) );

        assertBundleFor( "commons-logging", "commons-logging", "1.1.1" ).matches(
            getTestResourceAsFile( "manifests/bm0101/commons-logging-1.1.1-osgi.jar.properties" ) );
    }

}
