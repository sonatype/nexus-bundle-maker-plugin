package org.sonatype.nexus.plugins.bundlemaker.internal.capabilities;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.plugins.bundlemaker.BundleMaker;
import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityFactory;
import org.sonatype.nexus.plugins.requestinterceptor.RequestInterceptors;

@Named( BundleMakerCapability.ID )
@Singleton
public class BundleMakerCapabilityFactory
    implements CapabilityFactory
{

    private final BundleMaker bundleMaker;

    private final RequestInterceptors requestInterceptors;

    @Inject
    BundleMakerCapabilityFactory( final BundleMaker bundleMaker, final RequestInterceptors requestInterceptors )
    {
        this.bundleMaker = bundleMaker;
        this.requestInterceptors = requestInterceptors;
    }

    @Override
    public Capability create( final String id )
    {
        final BundleMakerCapability capability = new BundleMakerCapability( id, bundleMaker );
        capability.add( new RecipeRequestInterceptorCapability( id, requestInterceptors ) );
        capability.add( new BundleRequestInterceptorCapability( id, requestInterceptors ) );
        return capability;
    }
}
