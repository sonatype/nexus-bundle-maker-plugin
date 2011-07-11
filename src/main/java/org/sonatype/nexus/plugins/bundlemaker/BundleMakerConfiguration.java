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
package org.sonatype.nexus.plugins.bundlemaker;

import java.util.Arrays;
import java.util.Map;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.plugins.bundlemaker.internal.capabilities.BundleMakerCapabilityDescriptor;
import org.sonatype.nexus.plugins.bundlemaker.internal.capabilities.EagerFormField;
import org.sonatype.nexus.plugins.bundlemaker.internal.capabilities.RemoteRepositoriesFormField;
import org.sonatype.nexus.plugins.bundlemaker.internal.capabilities.UseMavenModelFormField;

public class BundleMakerConfiguration
{

    private final String repositoryId;

    private final String[] mavenRemoteRepositoriesIds;

    private final boolean eager;

    private final boolean useMavenModel;

    public BundleMakerConfiguration( final Map<String, String> properties )
    {
        repositoryId = repository( properties );
        mavenRemoteRepositoriesIds = mavenRemoteRepositoriesIds( properties );
        eager = eager( properties );
        useMavenModel = useMavenModel( properties );
    }

    public String repositoryId()
    {
        return repositoryId;
    }

    public String[] mavenRemoteRepositoriesIds()
    {
        return mavenRemoteRepositoriesIds;
    }

    public String[] mavenResolverRepositoriesIds()
    {
        if ( mavenRemoteRepositoriesIds.length == 0 )
        {
            return new String[] { repositoryId };
        }

        final String[] ids = new String[mavenRemoteRepositoriesIds.length + 1];
        ids[0] = repositoryId;
        System.arraycopy( mavenRemoteRepositoriesIds, 0, ids, 1, mavenRemoteRepositoriesIds.length );

        return ids;
    }

    public boolean isEager()
    {
        return eager;
    }

    public boolean useMavenModel()
    {
        return useMavenModel;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( eager ? 1231 : 1237 );
        result = prime * result + Arrays.hashCode( mavenRemoteRepositoriesIds );
        result = prime * result + ( ( repositoryId == null ) ? 0 : repositoryId.hashCode() );
        return result;
    }

    @Override
    public boolean equals( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        final BundleMakerConfiguration other = (BundleMakerConfiguration) obj;
        if ( eager != other.eager )
        {
            return false;
        }
        if ( !Arrays.equals( mavenRemoteRepositoriesIds, other.mavenRemoteRepositoriesIds ) )
        {
            return false;
        }
        if ( repositoryId == null )
        {
            if ( other.repositoryId != null )
            {
                return false;
            }
        }
        else if ( !repositoryId.equals( other.repositoryId ) )
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append( "BundleMakerConfiguration [" );
        if ( repositoryId != null )
        {
            builder.append( "repositoryId=" );
            builder.append( repositoryId );
            builder.append( ", " );
        }
        if ( mavenRemoteRepositoriesIds != null )
        {
            builder.append( "remoteRepositoriesIds=" );
            builder.append( Arrays.toString( mavenRemoteRepositoriesIds ) );
            builder.append( ", " );
        }
        builder.append( "eager=" );
        builder.append( eager );
        builder.append( "]" );
        return builder.toString();
    }

    private static String repository( final Map<String, String> properties )
    {
        String repositoryId = properties.get( BundleMakerCapabilityDescriptor.REPO_OR_GROUP_ID );
        repositoryId = repositoryId.replaceFirst( "repo_", "" );
        repositoryId = repositoryId.replaceFirst( "group_", "" );
        return repositoryId;
    }

    private static String[] mavenRemoteRepositoriesIds( final Map<String, String> properties )
    {
        final String remotes = properties.get( RemoteRepositoriesFormField.ID );
        if ( StringUtils.isBlank( remotes ) )
        {
            return new String[0];
        }

        final String[] remoteRepositories = remotes.split( "," );
        return remoteRepositories;
    }

    private static boolean eager( final Map<String, String> properties )
    {
        final String eager = properties.get( EagerFormField.ID );
        return Boolean.parseBoolean( eager );
    }

    private static boolean useMavenModel( final Map<String, String> properties )
    {
        final String value = properties.get( UseMavenModelFormField.ID );
        return Boolean.parseBoolean( value );
    }

}
