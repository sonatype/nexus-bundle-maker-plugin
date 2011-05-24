package org.sonatype.nexus.plugins.bundlemaker.internal.capabilities;

import org.sonatype.nexus.formfields.CheckboxFormField;

public class UseMavenModelFormField
    extends CheckboxFormField
{

    public static final String ID = "useMavenModel";

    public UseMavenModelFormField()
    {
        super(
            ID,
            "Detailed Maven model info",
            "Check this option if the generated bundle should contain detailed info parsed from Maven modle (POM) such as name, description, license,...",
            false );
    }

}
