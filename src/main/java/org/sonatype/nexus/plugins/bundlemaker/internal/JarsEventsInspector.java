/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.bundlemaker.internal;

import static org.sonatype.nexus.plugins.bundlemaker.internal.BundleMakerUtils.isABundle;
import static org.sonatype.nexus.plugins.bundlemaker.internal.BundleMakerUtils.isAJar;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.plugins.bundlemaker.BundleMaker;
import org.sonatype.nexus.plugins.bundlemaker.BundleMakerConfiguration;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.plexus.appevents.Event;

@Named
@Singleton
public class JarsEventsInspector
    implements EventInspector
{

    private final BundleMaker bundleMaker;

    @Inject
    public JarsEventsInspector( final BundleMaker bundleMaker )
    {
        this.bundleMaker = bundleMaker;
    }

    @Override
    public boolean accepts( final Event<?> evt )
    {
        if ( evt == null
            || !( evt instanceof RepositoryItemEvent )
            || !( evt instanceof RepositoryItemEventStore || evt instanceof RepositoryItemEventCache || evt instanceof RepositoryItemEventDelete ) )
        {
            return false;
        }

        final RepositoryItemEvent event = (RepositoryItemEvent) evt;
        final String path = event.getItem().getPath();

        return isAJar( path ) && !isABundle( path );
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
        else if ( event instanceof RepositoryItemEventDelete )
        {
            onItemRemoved( event );
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
            bundleMaker.createOSGiVersionOfJar( event.getItem() );
        }
    }

    private void onItemRemoved( final RepositoryItemEvent event )
    {
        final BundleMakerConfiguration configuration = bundleMaker.getConfiguration( event.getRepository().getId() );
        if ( configuration == null )
        {
            return;
        }
        bundleMaker.removeOSGiVersionOfJar( event.getItem() );
    }

}
