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

import java.util.HashMap;
import java.util.Map;

import org.sonatype.nexus.plugins.bundlemaker.internal.BundleInterceptor;
import org.sonatype.nexus.plugins.capabilities.CapabilityContext;
import org.sonatype.nexus.plugins.requestinterceptor.RequestInterceptorConfiguration;
import org.sonatype.nexus.plugins.requestinterceptor.RequestInterceptors;
import org.sonatype.nexus.plugins.requestinterceptor.capabilities.RequestInterceptorActionFormField;
import org.sonatype.nexus.plugins.requestinterceptor.capabilities.RequestInterceptorCapability;
import org.sonatype.nexus.plugins.requestinterceptor.capabilities.RequestInterceptorGeneratorFormField;
import org.sonatype.nexus.plugins.requestinterceptor.capabilities.RequestInterceptorMappingFormField;
import org.sonatype.nexus.proxy.access.Action;

public class BundleRequestInterceptorCapability
    extends RequestInterceptorCapability
{

    public BundleRequestInterceptorCapability( final CapabilityContext context,
                                               final RequestInterceptors requestInterceptors )
    {
        super( context, requestInterceptors );
    }

    @Override
    protected RequestInterceptorConfiguration createConfiguration( final Map<String, String> properties )
    {
        final HashMap<String, String> props = new HashMap<String, String>( properties );
        props.put( RequestInterceptorActionFormField.ID, Action.read.toString() );
        props.put( RequestInterceptorMappingFormField.ID, "/**/*-osgi.jar" );
        props.put( RequestInterceptorGeneratorFormField.ID, BundleInterceptor.ID );

        return super.createConfiguration( props );
    }

}
