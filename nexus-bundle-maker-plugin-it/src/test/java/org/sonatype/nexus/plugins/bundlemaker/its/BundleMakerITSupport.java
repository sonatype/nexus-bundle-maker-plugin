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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.sonatype.nexus.plugins.bundlemaker.internal.capabilities.BundleMakerCapabilityDescriptor.REPO_OR_GROUP_ID;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.inject.Inject;
import javax.inject.Named;

import org.codehaus.plexus.util.StringUtils;
import org.junit.Before;
import org.sonatype.inject.BeanScanning;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.bundle.launcher.NexusRunningITSupport;
import org.sonatype.nexus.integrationtests.NexusRestClient;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.plugins.bundlemaker.internal.capabilities.BundleMakerCapability;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityPropertyResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResource;
import org.sonatype.nexus.plugins.capabilities.test.CapabilitiesNexusRestClient;
import org.sonatype.nexus.test.utils.DeployUtils;
import org.sonatype.nexus.test.utils.EventInspectorsUtil;
import org.sonatype.nexus.test.utils.GavUtil;
import org.sonatype.nexus.test.utils.RepositoriesNexusRestClient;
import org.sonatype.nexus.test.utils.TasksNexusRestClient;

public class BundleMakerITSupport
    extends NexusRunningITSupport
{

    @Inject
    @Named( "${NexusITSupport.nexus-bundle-maker-plugin-coordinates}" )
    private String bundleMakerPluginCoordinates;

    @Inject
    @Named( "${NexusITSupport.nexus-capabilities-plugin-coordinates}" )
    private String capabilitiesPluginCoordinates;

    @Inject
    @Named( "${NexusITSupport.nexus-request-interceptor-plugin-coordinates}" )
    private String requestInterceptorPluginCoordinates;

    @Inject
    @Named( "${NexusITSupport.nexus-maven-bridge-plugin-coordinates}" )
    private String mavenBridgePluginCoordinates;

    private final String testRepositoryId;

    private CapabilitiesNexusRestClient capabilitiesNRC;

    private TasksNexusRestClient tasksNRC;

    private RepositoriesNexusRestClient repositoriesNRC;

    private DeployUtils deployNRC;

    private NexusRestClient nexusRestClient;

    protected BundleMakerITSupport( final String testRepositoryId )
    {
        this.testRepositoryId = testRepositoryId;
    }

    protected String getTestRepositoryId()
    {
        return testRepositoryId;
    }

    @Override
    public BeanScanning scanning()
    {
        return BeanScanning.INDEX;
    }

    @Override
    protected NexusBundleConfiguration configureNexus( final NexusBundleConfiguration configuration )
    {
        return configuration.addPlugins(
            resolveArtifact( requestInterceptorPluginCoordinates ),
            resolveArtifact( mavenBridgePluginCoordinates ),
            resolveArtifact( capabilitiesPluginCoordinates ),
            resolveArtifact( bundleMakerPluginCoordinates )
        );
    }

    @Before
    @Override
    public void setUp()
    {
        super.setUp();
        nexusRestClient = new NexusRestClient(
            new TestContext()
                .setNexusUrl( nexus().getUrl().toExternalForm() )
                .setSecureTest( true )
        );
        capabilitiesNRC = new CapabilitiesNexusRestClient( nexusRestClient );
        tasksNRC = new TasksNexusRestClient( nexusRestClient );
        final EventInspectorsUtil events = new EventInspectorsUtil( nexusRestClient );
        repositoriesNRC = new RepositoriesNexusRestClient( nexusRestClient, tasksNRC, events );
        deployNRC = new DeployUtils( nexusRestClient );
    }

    protected void createCapability( final CapabilityPropertyResource... properties )
        throws Exception
    {
        final CapabilityPropertyResource[] cprs = new CapabilityPropertyResource[properties.length + 1];
        cprs[0] = property( REPO_OR_GROUP_ID, getTestRepositoryId() );
        System.arraycopy( properties, 0, cprs, 1, properties.length );
        final CapabilityResource capability =
            capability( BundleMakerCapability.TYPE_ID, BundleMakerITSupport.class.getName(), cprs );
        getCapabilitiesNRC().create( capability );
    }

    protected ManifestAssert assertRecipeFor( final String groupId, final String artifact, final String version )
        throws IOException
    {
        return assertRecipeFor( groupId, artifact, version, null );
    }

    protected ManifestAssert assertRecipeFor( final String groupId, final String artifactId, final String version,
                                              final String classifier )
        throws IOException
    {
        final File recipe = downloadArtifact( groupId, artifactId, version, "osgi", classifier );
        return ManifestAssert.fromProperties( recipe );
    }

    protected ManifestAssert assertBundleFor( final String groupId, final String artifact, final String version )
        throws IOException
    {
        return assertBundleFor( groupId, artifact, version, null );
    }

    protected ManifestAssert assertBundleFor( final String groupId, final String artifactId, final String version,
                                              final String classifier )
        throws IOException
    {
        String bundleClassifier = "osgi";
        if ( !StringUtils.isEmpty( classifier ) )
        {
            bundleClassifier = classifier + "-osgi";
        }
        final File bundle = downloadArtifact( groupId, artifactId, version, "jar", bundleClassifier );
        return assertBundleManifestOf( bundle );
    }

    protected final ManifestAssert assertBundleManifestOf( final File bundle )
        throws IOException
    {
        return ManifestAssert.fromJar( bundle );
    }

    protected ManifestAssert assertStorageRecipeFor( final String groupId, final String artifact,
                                                     final String version )
        throws IOException
    {
        return assertStorageRecipeFor( groupId, artifact, version, null );
    }

    protected ManifestAssert assertStorageRecipeFor( final String groupId, final String artifactId,
                                                     final String version, final String classifier )
        throws IOException
    {
        final File recipe = storageRecipeFor( groupId, artifactId, version, classifier );
        assertThat( "Recipe " + recipe.getPath() + "created", recipe.exists(), is( true ) );

        return ManifestAssert.fromProperties( recipe );
    }

    protected ManifestAssert assertStorageBundleFor( final String groupId, final String artifact,
                                                     final String version )
        throws IOException
    {
        return assertStorageBundleFor( groupId, artifact, version, null );
    }

    protected ManifestAssert assertStorageBundleFor( final String groupId, final String artifactId,
                                                     final String version, final String classifier )
        throws IOException
    {
        final File bundle = new File(
            nexus().getWorkDirectory(),
            "storage/"
                + getTestRepositoryId()
                + "/" + groupId
                + "/" + artifactId
                + "/" + version
                + "/" + artifactId + "-" + version + ( classifier == null ? "" : "-" + classifier ) + "-osgi.jar"
        );
        assertThat( "Bundle " + bundle.getPath() + "created", bundle.exists(), is( true ) );

        return ManifestAssert.fromJar( bundle );
    }

    protected File storageRecipeFor( final String groupId, final String artifactId, final String version )
    {
        return storageRecipeFor( groupId, artifactId, version, null );
    }

    protected File storageRecipeFor( final String groupId, final String artifactId, final String version,
                                     final String classifier )
    {
        return new File(
            nexus().getWorkDirectory(),
            "storage/"
                + getTestRepositoryId()
                + "/" + groupId
                + "/" + artifactId
                + "/" + version
                + "/" + artifactId + "-" + version + ( classifier == null ? "" : "-" + classifier ) + ".osgi"
        );
    }

    protected File downloadArtifact( String groupId, String artifact, String version, String type, String classifier )
        throws IOException
    {
        final URL url = new URL(
            nexus().getUrl().toExternalForm() + "content/repositories/" + getTestRepositoryId() + "/"
                + GavUtil.getRelitiveArtifactPath( groupId, artifact, version, type, classifier )
        );

        String classifierPart = ( classifier != null ) ? "-" + classifier : "";

        return nexusRestClient.downloadFile(
            url,
            methodSpecificDirectory( "downloads" ) + "/" + artifact + "-" + version + classifierPart + "." + type
        );
    }

    public static CapabilityResource capability( final String type,
                                                 final String notes,
                                                 final CapabilityPropertyResource... properties )
    {
        final CapabilityResource cr = new CapabilityResource();

        cr.setTypeId( type );
        cr.setNotes( notes );

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

    public CapabilitiesNexusRestClient getCapabilitiesNRC()
    {
        return capabilitiesNRC;
    }

    public DeployUtils deployNRC()
    {
        return deployNRC;
    }

    public RepositoriesNexusRestClient repositoriesNRC()
    {
        return repositoriesNRC;
    }

    protected TasksNexusRestClient tasksNRC()
    {
        return tasksNRC;
    }

}
