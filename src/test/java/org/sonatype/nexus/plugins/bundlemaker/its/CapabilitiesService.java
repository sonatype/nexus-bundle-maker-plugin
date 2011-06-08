package org.sonatype.nexus.plugins.bundlemaker.its;

import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityRequestResource;
import org.sonatype.restsimple.annotation.Path;
import org.sonatype.restsimple.annotation.Post;
import org.sonatype.restsimple.annotation.Produces;

@Path( "service/local/capabilities" )
public interface CapabilitiesService
{

    @Post
    @Produces( "application/json" )
    public void add( final CapabilityRequestResource crr );

}
