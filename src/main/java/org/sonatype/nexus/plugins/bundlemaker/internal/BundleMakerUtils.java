package org.sonatype.nexus.plugins.bundlemaker.internal;

import java.io.File;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.maven.index.artifact.Gav;

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

}
