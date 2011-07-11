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
package org.sonatype.nexus.plugins.bundlemaker.its.bm03;

import org.sonatype.nexus.plugins.bundlemaker.internal.capabilities.EagerFormField;
import org.sonatype.nexus.plugins.bundlemaker.its.BundleMakerProxyIT;
import org.testng.annotations.Test;

public class BM0302ProxyExistentEagerlyIT
    extends BundleMakerProxyIT
{

    public BM0302ProxyExistentEagerlyIT()
    {
        super( "bm03" );
    }

    /**
     * Recipe bundle are not created when those already exists, bundle maker capability not eager.
     */
    @Test
    public void test()
        throws Exception
    {
        createCapability( property( EagerFormField.ID, "true" ) );

        deployArtifact( getProxiedRepositoryId(),
            getTestResourceAsFile( "projects/commons-logging/commons-logging.osgi" ),
            "commons-logging/commons-logging/1.1.1/commons-logging-1.1.1.osgi" );
        deployArtifact( getProxiedRepositoryId(),
            getTestResourceAsFile( "projects/commons-logging/commons-logging-osgi.jar" ),
            "commons-logging/commons-logging/1.1.1/commons-logging-1.1.1-osgi.jar" );

        assertRecipeFor( "commons-logging", "commons-logging", "1.1.1" ).matches(
            getTestResourceAsFile( "manifests/bm0301/commons-logging-1.1.1.osgi.properties" ) );

        assertBundleFor( "commons-logging", "commons-logging", "1.1.1" ).matches(
            getTestResourceAsFile( "manifests/bm0301/commons-logging-1.1.1-osgi.jar.properties" ) );
    }
}
