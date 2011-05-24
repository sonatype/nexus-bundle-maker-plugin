package org.sonatype.nexus.plugins.bundlemaker.internal.capabilities;

import org.sonatype.nexus.formfields.CheckboxFormField;

public class EagerFormField
    extends CheckboxFormField
{

    public static final String ID = "eager";

    public EagerFormField()
    {
        super( ID, "Eager",
            "Check this option if the bundles must be created eagerly in the moment that a new jar is deployed/updated. "
                + "When this option is not enabled the bundle will be generated only when it is requested", false );
    }

}
