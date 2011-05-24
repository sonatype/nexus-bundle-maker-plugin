package org.sonatype.nexus.plugins.bundlemaker.internal.capabilities;

import org.sonatype.nexus.formfields.FormField;

public class RemoteRepositoriesFormField
    implements FormField
{

    public static final String ID = "remoteRepositories";

    public String getId()
    {
        return ID;
    }

    public String getLabel()
    {
        return "Remote repositories";
    }

    public String getType()
    {
        return "string";
    }

    public String getHelpText()
    {
        return "Specify repositories from this instance of Nexus that should be used as remote repositories while resolving the artifacts";
    }

    public String getRegexValidation()
    {
        return null;
    }

    public boolean isRequired()
    {
        return false;
    }

}
