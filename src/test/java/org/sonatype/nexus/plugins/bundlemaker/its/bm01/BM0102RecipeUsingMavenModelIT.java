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
package org.sonatype.nexus.plugins.bundlemaker.its.bm01;

import org.sonatype.nexus.plugins.bundlemaker.internal.capabilities.EagerFormField;
import org.sonatype.nexus.plugins.bundlemaker.internal.capabilities.RemoteRepositoriesFormField;
import org.sonatype.nexus.plugins.bundlemaker.internal.capabilities.UseMavenModelFormField;
import org.sonatype.nexus.plugins.bundlemaker.its.BundleMakerIT;
import org.testng.annotations.Test;

public class BM0102RecipeUsingMavenModelIT
    extends BundleMakerIT
{

    public BM0102RecipeUsingMavenModelIT()
    {
        super( "bm01" );
    }

    /**
     * Match recipe when maven model is used.
     */
    @Test
    public void test()
        throws Exception
    {
        deployFakeCentralArtifacts();
        createCapability( property( EagerFormField.ID, "true" ), property( UseMavenModelFormField.ID, "true" ),
            property( RemoteRepositoriesFormField.ID, getFakeCentralRepositoryId() ) );

        deployArtifacts( getTestResourceAsFile( "artifacts/jars" ) );

        assertRecipeFor( "commons-logging", "commons-logging", "1.1.1" ).matches(
            getTestResourceAsFile( "manifests/bm0102/commons-logging-1.1.1.osgi.properties" ) );

        assertBundleFor( "commons-logging", "commons-logging", "1.1.1" ).matches(
            getTestResourceAsFile( "manifests/bm0102/commons-logging-1.1.1-osgi.jar.properties" ) );
    }

}
