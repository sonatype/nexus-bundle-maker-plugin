package org.sonatype.nexus.plugins.bundlemaker.internal.capabilities;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.nexus.plugins.bundlemaker.internal.BundleInterceptor;
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

    public BundleRequestInterceptorCapability( final String id, final RequestInterceptors requestInterceptors )
    {
        super( id, requestInterceptors );
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
