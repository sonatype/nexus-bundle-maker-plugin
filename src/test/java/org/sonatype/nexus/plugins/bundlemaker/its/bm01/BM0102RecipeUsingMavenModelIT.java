package org.sonatype.nexus.plugins.bundlemaker.its.bm01;

import static org.sonatype.nexus.plugins.bundlemaker.its.CapabilitiesServiceClient.property;

import org.sonatype.nexus.plugins.bundlemaker.internal.capabilities.EagerFormField;
import org.sonatype.nexus.plugins.bundlemaker.internal.capabilities.RemoteRepositoriesFormField;
import org.sonatype.nexus.plugins.bundlemaker.internal.capabilities.UseMavenModelFormField;
import org.sonatype.nexus.plugins.bundlemaker.its.BundleMakerIT;
import org.testng.annotations.Test;


public class BM0102RecipeUsingMavenModelIT
    extends BundleMakerIT
{

    @Test
    public void recipeUsingMavenModel()
        throws Exception
    {
        deployFakeCentralArtifacts();
        createCapability( property( EagerFormField.ID, "true" ), property( UseMavenModelFormField.ID, "true" ),
            property( RemoteRepositoriesFormField.ID, getFakeCentralRepositoryId() ) );

        deployArtifacts( getTestResourceAsFile( "artifacts/jars" ) );

        assertRecipeFor( "commons-logging", "commons-logging", "1.1.1" ).matches(
            getTestResourceAsFile( "manifests/bm0102/commons-logging-1.1.1.osgi.properties" ) );

        assertBundleFor( "commons-logging", "commons-logging", "1.1.1" ).matches(
            getTestResourceAsFile( "manifests/bm0102/commons-logging-1.1.1-osgi.jar.properties" ) );
    }

}
