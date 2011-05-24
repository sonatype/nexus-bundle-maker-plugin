package com.sonatype.nexus.plugins.bundlemaker;

import static com.sonatype.nexus.plugins.bundlemaker.CapabilitiesServiceClient.capability;
import static com.sonatype.nexus.plugins.bundlemaker.CapabilitiesServiceClient.property;
import static org.sonatype.nexus.plugins.bundlemaker.internal.capabilities.BundleMakerCapabilityDescriptor.REPO_OR_GROUP_ID;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.plugins.bundlemaker.internal.capabilities.BundleMakerCapability;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityPropertyResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResource;

public class BundleMakerIT
    extends AbstractNexusIntegrationTest
{

    public BundleMakerIT()
    {
    }

    public BundleMakerIT( final String repoId )
    {
        super( repoId );
    }

    protected void createCapability( final CapabilityPropertyResource... properties )
        throws Exception
    {
        final CapabilitiesServiceClient capabilities = new CapabilitiesServiceClient( nexusBaseUrl );
        final CapabilityPropertyResource[] cprs = new CapabilityPropertyResource[properties.length + 1];
        cprs[0] = property( REPO_OR_GROUP_ID, testRepositoryId );
        System.arraycopy( properties, 0, cprs, 1, properties.length );
        final CapabilityResource capability =
            capability( BundleMakerIT.class.getName(), BundleMakerCapability.ID, cprs );
        capabilities.add( capability );
    }

    protected void deployFakeCentralArtifacts()
        throws Exception
    {
        deployArtifacts( getTestResourceAsFile( "artifacts/central" ) );
    }

    protected void deployArtifact( final String repoId, final File fileToDeploy, final String path )
        throws Exception
    {
        final String deployUrl = getNexusTestRepoUrl( repoId );
        final String deployUrlProtocol = deployUrl.substring( 0, deployUrl.indexOf( ":" ) );
        final String wagonHint = getWagonHintForDeployProtocol( deployUrlProtocol );
        getDeployUtils().deployWithWagon( wagonHint, deployUrl, fileToDeploy, path );
    }

    protected String getFakeCentralRepositoryId()
    {
        return REPO_TEST_HARNESS_REPO2;
    }

    protected ManifestAsserter assertRecipeFor( final String groupId, final String artifact, final String version )
        throws IOException
    {
        return assertRecipeFor( groupId, artifact, version, null );
    }

    protected ManifestAsserter assertRecipeFor( final String groupId, final String artifactId, final String version,
                                                final String classifier )
        throws IOException
    {
        final File downloadDir = new File( "target/downloads/" + this.getClass().getSimpleName() );
        final File recipe = downloadArtifact( groupId, artifactId, version, "osgi", classifier, downloadDir.getPath() );
        return ManifestAsserter.fromProperties( recipe );
    }

    protected ManifestAsserter assertBundleFor( final String groupId, final String artifact, final String version )
        throws IOException
    {
        return assertBundleFor( groupId, artifact, version, null );
    }

    protected ManifestAsserter assertBundleFor( final String groupId, final String artifactId, final String version,
                                                final String classifier )
        throws IOException
    {
        final File downloadDir = new File( "target/downloads/" + this.getClass().getSimpleName() );
        String bundleClassifier = "osgi";
        if ( !StringUtils.isEmpty( classifier ) )
        {
            bundleClassifier = classifier + "-osgi";
        }
        final File bundle =
            downloadArtifact( groupId, artifactId, version, "jar", bundleClassifier, downloadDir.getPath() );
        return assertBundleManifestOf( bundle );
    }

    protected final ManifestAsserter assertBundleManifestOf( final File bundle )
        throws IOException
    {
        return ManifestAsserter.fromJar( bundle );
    }

}
