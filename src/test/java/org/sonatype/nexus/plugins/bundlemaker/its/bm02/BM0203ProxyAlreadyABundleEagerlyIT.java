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
package org.sonatype.nexus.plugins.bundlemaker.its.bm02;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;

import org.sonatype.nexus.plugins.bundlemaker.internal.capabilities.EagerFormField;
import org.sonatype.nexus.plugins.bundlemaker.its.BundleMakerProxyIT;
import org.testng.annotations.Test;

public class BM0203ProxyAlreadyABundleEagerlyIT
    extends BundleMakerProxyIT
{

    public BM0203ProxyAlreadyABundleEagerlyIT()
    {
        super( "bm02" );
    }

    /**
     * For an OSGi bundle recipe is not created and there is a link to original bundle, bundle maker capability not
     * eager.
     */
    @Test
    public void test()
        throws Exception
    {
        createCapability( property( EagerFormField.ID, "true" ) );

        final File recipe =
            new File( new File( nexusWorkDir ), "storage/" + getTestRepositoryId()
                + "/org/ops4j/base/ops4j-base-lang/1.2.3/ops4j-base-lang-1.2.3.osgi" );
        assertFalse( recipe.exists(), "Recipe " + recipe.getPath() + " created" );

        final File bundle =
            new File( new File( nexusWorkDir ), "storage/" + getTestRepositoryId()
                + "/org/ops4j/base/ops4j-base-lang/1.2.3/ops4j-base-lang-1.2.3-osgi.jar" );
        assertFalse( bundle.exists(), "Bundle " + bundle.getPath() + " created" );

        final File downloadDir = new File( "target/downloads/" + this.getClass().getSimpleName() );

        try
        {
            downloadArtifact( "org.ops4j.base", "ops4j-base-lang", "1.2.3", "osgi", null, downloadDir.getPath() );
            fail( "Expected a FileNotFoundException" );
        }
        catch ( final FileNotFoundException expected )
        {
        }

        // there should be a link to original
        downloadArtifact( "org.ops4j.base", "ops4j-base-lang", "1.2.3", "jar", "osgi", downloadDir.getPath() );
    }
}
