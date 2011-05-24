package org.sonatype.nexus.plugins.bundlemaker.internal;

import static org.sonatype.nexus.plugins.bundlemaker.internal.BundleMakerUtils.isAPom;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.plugins.bundlemaker.BundleMaker;
import org.sonatype.nexus.plugins.bundlemaker.BundleMakerConfiguration;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.plexus.appevents.Event;

@Named
@Singleton
public class PomEventsInspector
    implements EventInspector
{

    private final BundleMaker bundleMaker;

    @Inject
    public PomEventsInspector( final BundleMaker bundleMaker )
    {
        this.bundleMaker = bundleMaker;
    }

    @Override
    public boolean accepts( final Event<?> evt )
    {
        if ( evt == null || !( evt instanceof RepositoryItemEvent )
            || !( evt instanceof RepositoryItemEventStore || evt instanceof RepositoryItemEventCache ) )
        {
            return false;
        }

        final RepositoryItemEvent event = (RepositoryItemEvent) evt;
        final String path = event.getItem().getPath();

        return isAPom( path );
    }

    @Override
    public void inspect( final Event<?> evt )
    {
        if ( !accepts( evt ) )
        {
            return;
        }

        final RepositoryItemEvent event = (RepositoryItemEvent) evt;

        if ( event instanceof RepositoryItemEventStore || event instanceof RepositoryItemEventCache )
        {
            onItemAdded( event );
        }
    }

    private void onItemAdded( final RepositoryItemEvent event )
    {
        final BundleMakerConfiguration configuration = bundleMaker.getConfiguration( event.getRepository().getId() );
        if ( configuration == null )
        {
            return;
        }
        if ( configuration.isEager() )
        {
            bundleMaker.createOSGiVersionOfJarsWithPom( event.getItem() );
        }
    }

}
