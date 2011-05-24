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
