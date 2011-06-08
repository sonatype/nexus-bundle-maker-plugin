package org.sonatype.nexus.plugins.bundlemaker.its;

import org.restlet.data.MediaType;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;

public class BundleMakerProxyIT
    extends BundleMakerIT
{

    public BundleMakerProxyIT()
    {
        super( REPO_RELEASE_PROXY_REPO1 );
    }

    @Override
    public void runOnce()
        throws Exception
    {
        final RepositoryMessageUtil repoUtil =
            new RepositoryMessageUtil( this, getXMLXStream(), MediaType.APPLICATION_XML );
        final RepositoryProxyResource repo = (RepositoryProxyResource) repoUtil.getRepository( getTestRepositoryId() );
        repo.getRemoteStorage().setRemoteStorageUrl( getNexusTestRepoUrl( getFakeCentralRepositoryId() ) );
        repoUtil.updateRepo( repo );
    }
}
