package org.sonatype.nexus.plugins.bundlemaker;

import org.sonatype.nexus.proxy.item.StorageItem;

public interface BundleMaker
{

    void addConfiguration( final BundleMakerConfiguration configuration );

    void removeConfiguration( final BundleMakerConfiguration configuration );

    BundleMakerConfiguration getConfiguration( final String repositoryId );

    void createOSGiVersionOfJar( final StorageItem jar );

    void removeOSGiVersionOfJar( final StorageItem jar );

    void createOSGiVersionOfJarsWithPom( final StorageItem pom );

    void scanAndRebuild( String repositoryId, String resourceStorePath );

    void scanAndRebuild( String resourceStorePath );

}
