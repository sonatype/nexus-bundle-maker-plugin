package org.sonatype.nexus.plugins.bundlemaker.its;

import org.restlet.data.MediaType;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;

public class BundleMakerProxyIT
    extends BundleMakerIT
{

    public BundleMakerProxyIT( final String repoId )
    {
        super( repoId );
    }

    @Override
    public void runOnce()
        throws Exception
    {
        final RepositoryMessageUtil repoUtil =
            new RepositoryMessageUtil( this, getXMLXStream(), MediaType.APPLICATION_XML );
        final RepositoryProxyResource repo = (RepositoryProxyResource) repoUtil.getRepository( getTestRepositoryId() );
        repo.getRemoteStorage().setRemoteStorageUrl( getNexusTestRepoUrl( getProxiedRepositoryId() ) );
        repoUtil.updateRepo( repo );
    }

    protected String getProxiedRepositoryId()
    {
        return getTestRepositoryId() + "-proxied";
    }

}
