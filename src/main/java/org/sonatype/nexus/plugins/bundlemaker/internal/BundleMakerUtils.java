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
package org.sonatype.nexus.plugins.bundlemaker.internal;

import java.io.File;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.sonatype.nexus.proxy.maven.gav.Gav;

class BundleMakerUtils
{

    private static final String JAR = "jar";

    private static final String JAR_EXTENSION = "." + JAR;

    private static final String POM = "pom";

    private static final String POM_EXTENSION = "." + POM;

    private static final String OSGi = "osgi";

    private static final String OSGi_EXTENSION = "." + OSGi;

    private static final String OSGi_CLASSIFIER = "-" + OSGi;

    public static final String GENERATED_AT_ATTRIBUTE = "bundleMaker.generated.timestamp";

    static Gav pomGavFor( final Gav itemGav )
    {
        final Gav gav =
            new Gav( itemGav.getGroupId(), itemGav.getArtifactId(), itemGav.getVersion(), null, "pom",
                itemGav.getSnapshotBuildNumber(), itemGav.getSnapshotTimeStamp(), null, false, null, false, null );
        return gav;
    }

    static String recipePathForBundle( final String bundlePath )
    {
        final String path = bundlePath.replace( OSGi_CLASSIFIER + JAR_EXTENSION, OSGi_EXTENSION );
        return path;
    }

    static String jarPathForBundle( final String bundlePath )
    {
        final String path = bundlePath.replace( OSGi_CLASSIFIER + JAR_EXTENSION, JAR_EXTENSION );
        return path;
    }

    static String jarPathForRecipe( final String recipePath )
    {
        final String path = recipePath.replace( OSGi_EXTENSION, JAR_EXTENSION );
        return path;
    }

    static String bundlePathForJar( final String jarPath )
    {
        final String path = jarPath.replace( JAR_EXTENSION, OSGi_CLASSIFIER + JAR_EXTENSION );
        return path;
    }

    static boolean isAJar( final String path )
    {
        if ( path == null )
        {
            return false;
        }
        return path.endsWith( JAR_EXTENSION ) && !path.endsWith( "-sources" + JAR_EXTENSION )
            && !path.endsWith( "-javadoc" + JAR_EXTENSION );
    }

    static boolean isAPom( final String path )
    {
        if ( path == null )
        {
            return false;
        }
        return path.endsWith( POM_EXTENSION );
    }

    static boolean isABundle( final String path )
    {
        if ( path == null )
        {
            return false;
        }
        return path.endsWith( OSGi_CLASSIFIER + JAR_EXTENSION );
    }

    static boolean isAlreadyAnOSGiBundle( final File file )
    {
        if ( file == null )
        {
            return false;
        }
        try
        {
            final JarFile jarFile = new JarFile( file );
            final Manifest manifest = jarFile.getManifest();
            final Attributes mainAttributes = manifest.getMainAttributes();
            return mainAttributes.getValue( "Bundle-SymbolicName" ) != null;
        }
        catch ( final Exception e )
        {
            return false;
        }
    }

    static boolean isARecipe( final String path )
    {
        if ( path == null )
        {
            return false;
        }
        return path.endsWith( OSGi_EXTENSION );
    }

}
