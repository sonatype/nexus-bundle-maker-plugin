package org.sonatype.nexus.plugins.bundlemaker.its;

import java.net.URI;
import java.net.URISyntaxException;

import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityPropertyResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityRequestResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResource;
import org.sonatype.restsimple.client.WebProxy;

public class CapabilitiesServiceClient
{

    private CapabilitiesService client;

    public CapabilitiesServiceClient( final String baseUrl )
    {
        try
        {
            client = WebProxy.createProxy( CapabilitiesService.class, new URI( baseUrl ) );
        }
        catch ( final URISyntaxException e )
        {
            throw new IllegalArgumentException( e );
        }
    }

    public void add( final CapabilityResource capability )
    {
        final CapabilityRequestResource crr = new CapabilityRequestResource();
        crr.setData( capability );

        client.add( crr );
    }

    public static CapabilityResource capability( final String name, final String type,
                                                 final CapabilityPropertyResource... properties )
    {
        final CapabilityResource cr = new CapabilityResource();

        cr.setName( name );
        cr.setTypeId( type );

        for ( final CapabilityPropertyResource cpr : properties )
        {
            cr.addProperty( cpr );
        }

        return cr;
    }

    public static CapabilityPropertyResource property( final String key, final String value )
    {
        final CapabilityPropertyResource cpr = new CapabilityPropertyResource();

        cpr.setKey( key );
        cpr.setValue( value );

        return cpr;
    }

}
