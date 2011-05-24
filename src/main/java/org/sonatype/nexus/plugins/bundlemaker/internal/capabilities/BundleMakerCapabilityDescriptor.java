package org.sonatype.nexus.plugins.bundlemaker.internal.capabilities;

import java.util.Arrays;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.RepoOrGroupComboFormField;
import org.sonatype.nexus.plugins.capabilities.api.descriptor.CapabilityDescriptor;

@Singleton
@Named( BundleMakerCapability.ID )
public class BundleMakerCapabilityDescriptor
    implements CapabilityDescriptor
{

    public static final String ID = BundleMakerCapability.ID;

    public static final String REPO_OR_GROUP_ID = "repoOrGroup";

    private final FormField repoOrGroup;

    private final FormField remoteRepositories;

    private final FormField eager;

    private final FormField useMavenModel;

    public BundleMakerCapabilityDescriptor()
    {
        repoOrGroup = new RepoOrGroupComboFormField( REPO_OR_GROUP_ID, FormField.MANDATORY );
        eager = new EagerFormField();
        useMavenModel = new UseMavenModelFormField();
        remoteRepositories = new RemoteRepositoriesFormField();
    }

    @Override
    public String id()
    {
        return ID;
    }

    @Override
    public String name()
    {
        return "Bundle Maker capability";
    }

    @Override
    public List<FormField> formFields()
    {
        return Arrays.asList( repoOrGroup, eager, useMavenModel, remoteRepositories );
    }

    @Override
    public boolean isExposed()
    {
        return true;
    }

}
