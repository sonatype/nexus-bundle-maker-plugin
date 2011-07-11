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
package org.sonatype.nexus.plugins.bundlemaker.internal.capabilities;

import java.util.Map;

import org.sonatype.nexus.plugins.bundlemaker.BundleMaker;
import org.sonatype.nexus.plugins.bundlemaker.BundleMakerConfiguration;
import org.sonatype.nexus.plugins.capabilities.api.CompositeCapability;

public class BundleMakerCapability
    extends CompositeCapability
{

    public static final String ID = "bundleMakerCapability";

    private final BundleMaker bundleMaker;

    private BundleMakerConfiguration configuration;

    public BundleMakerCapability( final String id, final BundleMaker bundleMaker )
    {
        super( id );
        this.bundleMaker = bundleMaker;
    }

    @Override
    public void create( final Map<String, String> properties )
    {
        configuration = new BundleMakerConfiguration( properties );
        bundleMaker.addConfiguration( configuration );

        super.create( properties );
    }

    @Override
    public void load( final Map<String, String> properties )
    {
        configuration = new BundleMakerConfiguration( properties );
        bundleMaker.addConfiguration( configuration );

        super.load( properties );
    }

    @Override
    public void update( final Map<String, String> properties )
    {
        final BundleMakerConfiguration newConfiguration = new BundleMakerConfiguration( properties );
        if ( !configuration.equals( newConfiguration ) )
        {
            remove();
            create( properties );
        }

        super.update( properties );
    }

    @Override
    public void remove()
    {
        bundleMaker.removeConfiguration( configuration );

        super.remove();
    }

}
