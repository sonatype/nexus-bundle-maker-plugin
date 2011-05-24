package org.sonatype.nexus.plugins.bundlemaker.internal.tasks;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.plugins.bundlemaker.BundleMaker;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesPathAwareTask;
import org.sonatype.scheduling.SchedulerTask;

@Named( BundleMakerRebuildTaskDescriptor.ID )
public class BundleMakerRebuildTask
    extends AbstractNexusRepositoriesPathAwareTask<Object>
    implements SchedulerTask<Object>
{

    private final BundleMaker bundleMaker;

    @Inject
    BundleMakerRebuildTask( final BundleMaker bundleMaker )
    {
        this.bundleMaker = bundleMaker;
    }

    @Override
    protected String getRepositoryFieldId()
    {
        return BundleMakerRebuildTaskDescriptor.REPO_OR_GROUP_FIELD_ID;
    }

    @Override
    protected String getRepositoryPathFieldId()
    {
        return BundleMakerRebuildTaskDescriptor.RESOURCE_STORE_PATH_FIELD_ID;
    }

    @Override
    protected String getAction()
    {
        return "REBUILD";
    }

    @Override
    protected String getMessage()
    {
        if ( getRepositoryId() != null )
        {
            return String.format( "Rebuild repository [%s] bundles from path [%s] and bellow", getRepositoryId(),
                getResourceStorePath() );
        }
        else
        {
            return "Rebuild bundles from all repositories (with a Bundle Maker Capability enabled)";
        }
    }

    @Override
    protected Object doRun()
        throws Exception
    {
        final String repositoryId = getRepositoryId();
        if ( repositoryId != null )
        {
            bundleMaker.scanAndRebuild( repositoryId, getResourceStorePath() );
        }
        else
        {
            bundleMaker.scanAndRebuild( getResourceStorePath() );
        }

        return null;
    }

}
