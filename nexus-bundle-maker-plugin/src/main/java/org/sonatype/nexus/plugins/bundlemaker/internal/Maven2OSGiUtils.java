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
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import aQute.lib.osgi.Analyzer;

class Maven2OSGiUtils
{

    private Maven2OSGiUtils()
    {
    }

    private static final String FILE_SEPARATOR = System.getProperty( "file.separator" );

    /**
     * Get the symbolic name as groupId + "." + artifactId, with the following exceptions
     * <ul>
     * <li>if artifact.getFile is not null and the jar contains a OSGi Manifest with Bundle-SymbolicName property then
     * that value is returned</li>
     * <li>if groupId has only one section (no dots) and artifact.getFile is not null then the first package name with
     * classes is returned. eg. commons-logging:commons-logging -> org.apache.commons.logging</li>
     * <li>if artifactId is equal to last section of groupId then groupId is returned. eg. org.apache.maven:maven ->
     * org.apache.maven</li>
     * <li>if artifactId starts with last section of groupId that portion is removed. eg. org.apache.maven:maven-core ->
     * org.apache.maven.core</li>
     * </ul>
     */
    static String getBundleSymbolicName( final String groupId, final String artifactId, final File jarFile )
    {
        if ( ( jarFile != null ) && jarFile.isFile() )
        {
            final Analyzer analyzer = new Analyzer();

            JarFile jar = null;
            try
            {
                jar = new JarFile( jarFile, false );

                if ( jar.getManifest() != null )
                {
                    final String symbolicNameAttribute =
                        jar.getManifest().getMainAttributes().getValue( Analyzer.BUNDLE_SYMBOLICNAME );
                    final Map<String, Map<String, String>> bundleSymbolicNameHeader =
                        analyzer.parseHeader( symbolicNameAttribute );

                    final Iterator<String> it = bundleSymbolicNameHeader.keySet().iterator();
                    if ( it.hasNext() )
                    {
                        return it.next();
                    }
                }
            }
            catch ( final IOException ignore )
            {
                // ignore
            }
            finally
            {
                if ( jar != null )
                {
                    try
                    {
                        jar.close();
                    }
                    catch ( final IOException e )
                    {
                    }
                }
            }
        }

        int i = groupId.lastIndexOf( '.' );
        if ( ( i < 0 ) && ( jarFile != null ) && jarFile.isFile() )
        {
            final String groupIdFromPackage = getGroupIdFromPackage( jarFile );
            if ( groupIdFromPackage != null )
            {
                return groupIdFromPackage;
            }
        }
        final String lastSection = groupId.substring( ++i );
        if ( artifactId.equals( lastSection ) )
        {
            return groupId;
        }
        if ( artifactId.startsWith( lastSection ) )
        {
            final String localArtifactId = artifactId.substring( lastSection.length() );
            if ( Character.isLetterOrDigit( localArtifactId.charAt( 0 ) ) )
            {
                return getBundleSymbolicName( groupId, localArtifactId );
            }
            else
            {
                return getBundleSymbolicName( groupId, localArtifactId.substring( 1 ) );
            }
        }
        return getBundleSymbolicName( groupId, artifactId );
    }

    private static String getBundleSymbolicName( final String groupId, final String artifactId )
    {
        return groupId + "." + artifactId;
    }

    private static String getGroupIdFromPackage( final File artifactFile )
    {
        try
        {
            /* get package names from jar */
            final Set<String> packageNames = new HashSet<String>();
            final JarFile jar = new JarFile( artifactFile, false );
            final Enumeration<JarEntry> entries = jar.entries();
            while ( entries.hasMoreElements() )
            {
                final ZipEntry entry = entries.nextElement();
                if ( entry.getName().endsWith( ".class" ) )
                {
                    final File f = new File( entry.getName() );
                    final String packageName = f.getParent();
                    if ( packageName != null )
                    {
                        packageNames.add( packageName );
                    }
                }
            }
            jar.close();

            /* find the top package */
            String[] groupIdSections = null;
            for ( final String packageName : packageNames )
            {
                final String[] packageNameSections = packageName.split( "\\" + FILE_SEPARATOR );
                if ( groupIdSections == null )
                {
                    /* first candidate */
                    groupIdSections = packageNameSections;
                }
                else
                // if ( packageNameSections.length < groupIdSections.length )
                {
                    /*
                     * find the common portion of current package and previous selected groupId
                     */
                    int i;
                    for ( i = 0; ( i < packageNameSections.length ) && ( i < groupIdSections.length ); i++ )
                    {
                        if ( !packageNameSections[i].equals( groupIdSections[i] ) )
                        {
                            break;
                        }
                    }
                    groupIdSections = new String[i];
                    System.arraycopy( packageNameSections, 0, groupIdSections, 0, i );
                }
            }

            if ( ( groupIdSections == null ) || ( groupIdSections.length == 0 ) )
            {
                return null;
            }

            /* only one section as id doesn't seem enough, so ignore it */
            if ( groupIdSections.length == 1 )
            {
                return null;
            }

            final StringBuffer sb = new StringBuffer();
            for ( int i = 0; i < groupIdSections.length; i++ )
            {
                sb.append( groupIdSections[i] );
                if ( i < groupIdSections.length - 1 )
                {
                    sb.append( '.' );
                }
            }
            return sb.toString();
        }
        catch ( final IOException e )
        {
            /* we took all the precautions to avoid this */
            throw new RuntimeException( e );
        }
    }

    static String getVersion( final String version )
    {
        return cleanupVersion( version );
    }

    /**
     * Clean up version parameters. Other builders use more fuzzy definitions of the version syntax. This method cleans
     * up such a version to match an OSGi version.
     * 
     * @param VERSION_STRING
     * @return
     */
    private static final Pattern FUZZY_VERSION = Pattern.compile( "(\\d+)(\\.(\\d+)(\\.(\\d+))?)?([^a-zA-Z0-9](.*))?",
        Pattern.DOTALL );

    private static String cleanupVersion( final String version )
    {
        final StringBuffer result = new StringBuffer();
        final Matcher m = FUZZY_VERSION.matcher( version );
        if ( m.matches() )
        {
            final String major = m.group( 1 );
            final String minor = m.group( 3 );
            final String micro = m.group( 5 );
            final String qualifier = m.group( 7 );

            if ( major != null )
            {
                result.append( major );
                if ( minor != null )
                {
                    result.append( "." );
                    result.append( minor );
                    if ( micro != null )
                    {
                        result.append( "." );
                        result.append( micro );
                        if ( qualifier != null )
                        {
                            result.append( "." );
                            cleanupModifier( result, qualifier );
                        }
                    }
                    else if ( qualifier != null )
                    {
                        result.append( ".0." );
                        cleanupModifier( result, qualifier );
                    }
                    else
                    {
                        result.append( ".0" );
                    }
                }
                else if ( qualifier != null )
                {
                    result.append( ".0.0." );
                    cleanupModifier( result, qualifier );
                }
                else
                {
                    result.append( ".0.0" );
                }
            }
        }
        else
        {
            result.append( "0.0.0." );
            cleanupModifier( result, version );
        }
        return result.toString();
    }

    private static void cleanupModifier( final StringBuffer result, final String modifier )
    {
        for ( int i = 0; i < modifier.length(); i++ )
        {
            final char c = modifier.charAt( i );
            if ( ( c >= '0' && c <= '9' ) || ( c >= 'a' && c <= 'z' ) || ( c >= 'A' && c <= 'Z' ) || c == '_'
                || c == '-' )
            {
                result.append( c );
            }
            else
            {
                result.append( '_' );
            }
        }
    }

}
