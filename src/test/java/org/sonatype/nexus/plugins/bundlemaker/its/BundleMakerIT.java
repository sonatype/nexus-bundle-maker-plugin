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
package org.sonatype.nexus.plugins.bundlemaker.its;

import static org.sonatype.nexus.plugins.bundlemaker.internal.capabilities.BundleMakerCapabilityDescriptor.REPO_OR_GROUP_ID;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.plugins.bundlemaker.internal.capabilities.BundleMakerCapability;
import org.sonatype.nexus.plugins.bundlemaker.internal.tasks.BundleMakerRebuildTaskDescriptor;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityPropertyResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.CapabilitiesMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class BundleMakerIT
    extends AbstractNexusIntegrationTest
{

    public BundleMakerIT()
    {
    }

    public BundleMakerIT( final String repoId )
    {
        super( repoId );
    }

    protected void createCapability( final CapabilityPropertyResource... properties )
        throws Exception
    {
        final CapabilityPropertyResource[] cprs = new CapabilityPropertyResource[properties.length + 1];
        cprs[0] = property( REPO_OR_GROUP_ID, getTestRepositoryId() );
        System.arraycopy( properties, 0, cprs, 1, properties.length );
        final CapabilityResource capability =
            capability( BundleMakerIT.class.getName(), BundleMakerCapability.ID, cprs );
        CapabilitiesMessageUtil.create( capability );
    }

    protected void runTask( final boolean forceRegeneration )
        throws Exception
    {
        final List<ScheduledServicePropertyResource> properties = new ArrayList<ScheduledServicePropertyResource>();

        final ScheduledServicePropertyResource repo =
            TaskScheduleUtil.newProperty( BundleMakerRebuildTaskDescriptor.REPO_OR_GROUP_FIELD_ID,
                getTestRepositoryId() );

        properties.add( repo );

        if ( forceRegeneration )
        {
            final ScheduledServicePropertyResource forced =
                TaskScheduleUtil.newProperty( BundleMakerRebuildTaskDescriptor.FORCED_REGENERATION_FIELD_ID, "true" );

            properties.add( forced );
        }

        TaskScheduleUtil.runTask( BundleMakerRebuildTaskDescriptor.ID + System.currentTimeMillis(),
            BundleMakerRebuildTaskDescriptor.ID,
            properties.toArray( new ScheduledServicePropertyResource[properties.size()] ) );
    }

    protected void deployFakeCentralArtifacts()
        throws Exception
    {
        deployArtifacts( getTestResourceAsFile( "artifacts/central" ) );
    }

    protected void deployArtifact( final String repoId, final File fileToDeploy, final String path )
        throws Exception
    {
        final String deployUrl = getNexusTestRepoUrl( repoId );
        final String deployUrlProtocol = deployUrl.substring( 0, deployUrl.indexOf( ":" ) );
        final String wagonHint = getWagonHintForDeployProtocol( deployUrlProtocol );
        getDeployUtils().deployWithWagon( wagonHint, deployUrl, fileToDeploy, path );
    }

    protected String getFakeCentralRepositoryId()
    {
        return getTestRepositoryId() + "-central";
    }

    protected ManifestAsserter assertRecipeFor( final String groupId, final String artifact, final String version )
        throws IOException
    {
        return assertRecipeFor( groupId, artifact, version, null );
    }

    protected ManifestAsserter assertRecipeFor( final String groupId, final String artifactId, final String version,
                                                final String classifier )
        throws IOException
    {
        final File downloadDir = new File( "target/downloads/" + this.getClass().getSimpleName() );
        final File recipe = downloadArtifact( groupId, artifactId, version, "osgi", classifier, downloadDir.getPath() );
        return ManifestAsserter.fromProperties( recipe );
    }

    protected ManifestAsserter assertBundleFor( final String groupId, final String artifact, final String version )
        throws IOException
    {
        return assertBundleFor( groupId, artifact, version, null );
    }

    protected ManifestAsserter assertBundleFor( final String groupId, final String artifactId, final String version,
                                                final String classifier )
        throws IOException
    {
        final File downloadDir = new File( "target/downloads/" + this.getClass().getSimpleName() );
        String bundleClassifier = "osgi";
        if ( !StringUtils.isEmpty( classifier ) )
        {
            bundleClassifier = classifier + "-osgi";
        }
        final File bundle =
            downloadArtifact( groupId, artifactId, version, "jar", bundleClassifier, downloadDir.getPath() );
        return assertBundleManifestOf( bundle );
    }

    protected final ManifestAsserter assertBundleManifestOf( final File bundle )
        throws IOException
    {
        return ManifestAsserter.fromJar( bundle );
    }

    protected ManifestAsserter assertStorageRecipeFor( final String groupId, final String artifact, final String version )
        throws IOException
    {
        return assertStorageRecipeFor( groupId, artifact, version, null );
    }

    protected ManifestAsserter assertStorageRecipeFor( final String groupId, final String artifactId,
                                                       final String version, final String classifier )
        throws IOException
    {
        final File recipe = storageRecipeFor( groupId, artifactId, version, classifier );
        assertTrue( recipe.exists(), "Recipe " + recipe.getPath() + "created" );

        return ManifestAsserter.fromProperties( recipe );
    }

    protected ManifestAsserter assertStorageBundleFor( final String groupId, final String artifact, final String version )
        throws IOException
    {
        return assertStorageBundleFor( groupId, artifact, version, null );
    }

    protected ManifestAsserter assertStorageBundleFor( final String groupId, final String artifactId,
                                                       final String version, final String classifier )
        throws IOException
    {
        final File bundle =
            new File( new File( nexusWorkDir ), "storage/" + getTestRepositoryId() + "/" + groupId + "/" + artifactId
                + "/" + version + "/" + artifactId + "-" + version + ( classifier == null ? "" : "-" + classifier )
                + "-osgi.jar" );
        assertTrue( bundle.exists(), "Bundle " + bundle.getPath() + "created" );

        return ManifestAsserter.fromJar( bundle );
    }

    protected File storageRecipeFor( final String groupId, final String artifactId, final String version )
    {
        return storageRecipeFor( groupId, artifactId, version, null );
    }

    protected File storageRecipeFor( final String groupId, final String artifactId, final String version,
                                     final String classifier )
    {
        final File recipe =
            new File( new File( nexusWorkDir ), "storage/" + getTestRepositoryId() + "/" + groupId + "/" + artifactId
                + "/" + version + "/" + artifactId + "-" + version + ( classifier == null ? "" : "-" + classifier )
                + ".osgi" );
        return recipe;
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
