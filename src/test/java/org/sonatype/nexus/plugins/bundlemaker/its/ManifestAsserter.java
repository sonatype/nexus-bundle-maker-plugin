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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.codehaus.plexus.util.IOUtil;
import org.testng.Assert;

public class ManifestAsserter
{

    private final Map<String, String> actualManifest;

    public ManifestAsserter( final Map<String, String> actualManifest )
    {
        this.actualManifest = actualManifest;
    }

    public ManifestAsserter( final Properties actualManifest )
    {
        this( convert( actualManifest ) );
    }

    public ManifestAsserter( final Manifest actualManifest )
    {
        this( convert( actualManifest ) );
    }

    public void matches( final File expectedManifest )
        throws IOException
    {
        final Map<String, String> manifest = convert( loadProperties( expectedManifest ) );
        for ( final Map.Entry<String, String> entry : manifest.entrySet() )
        {
            if ( "<any>".equalsIgnoreCase( entry.getValue() ) )
            {
                Assert.assertTrue( actualManifest.containsKey( entry.getKey() ), "Manifest has entry " + entry.getKey() );
            }
            else if ( "<none>".equalsIgnoreCase( entry.getValue() ) )
            {
                Assert.assertFalse( actualManifest.containsKey( entry.getKey() ), "Manifest does not have entry "
                    + entry.getKey() );
            }
            else
            {
                Assert.assertEquals( actualManifest.get( entry.getKey() ), entry.getValue(),
                    "Manifest entry " + entry.getKey() );
            }
        }
    }

    private static Map<String, String> convert( final Properties actualManifest )
    {
        final Map<String, String> manifest = new HashMap<String, String>();

        for ( final Map.Entry<Object, Object> entry : actualManifest.entrySet() )
        {
            manifest.put( entry.getKey().toString(), entry.getValue().toString() );
        }

        return manifest;
    }

    private static Map<String, String> convert( final Manifest actualManifest )
    {
        final Map<String, String> manifest = new HashMap<String, String>();

        for ( final Map.Entry<Object, Object> entry : actualManifest.getMainAttributes().entrySet() )
        {
            manifest.put( entry.getKey().toString(), entry.getValue().toString() );
        }

        return manifest;
    }

    public static ManifestAsserter fromProperties( final File properties )
        throws IOException
    {
        return new ManifestAsserter( loadProperties( properties ) );
    }

    public static ManifestAsserter fromJar( final File jar )
        throws IOException
    {
        final JarFile jarFile = new JarFile( jar.getAbsoluteFile() );
        return new ManifestAsserter( jarFile.getManifest() );
    }

    private static Properties loadProperties( final File properties )
        throws FileNotFoundException, IOException
    {
        final Properties props = new Properties();
        InputStream in = null;
        try
        {
            in = new FileInputStream( properties );
            props.load( in );
            return props;
        }
        finally
        {
            IOUtil.close( in );
        }
    }

}
