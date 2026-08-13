#!/bin/sh

######################
# Command line options
######################

# $1: BUILD_RUN: The build to be published (<strong>Be sure to select only successful builds with all tests passed!</strong>), must be an URL formated like this one: https://hudson.eclipse.org/sphinx/job/sphinx-0.8-luna/99/
# $2: BUILD_TYPE: The type as which the selected build is going to be published, must be one of: I(ntegration), M(ilestone), R(elease)C(andidate) R(elease), T(est)
# $3: BUILD_ID: The id under which the selected build is going to be published, use following convention: I: yyyymmdd-hhmm, M: n RC: n, R: none
# $4: SERVICE_RELEASE_NUMBER: The service release number of the build to be published (will be used to complement the major and minor version number in the name of the build to be published)
# $5: MERGE_UPDATE_SITE: Whether to keep all previously published builds in project update site and merge the build to be published into it (project update site will be wiped out and replaced by the update site of the build to be published otherwise), must be one of: true, false

###############################
# Resulting downloads structure
###############################

# Downloads:
# http://download.eclipse.org/sphinx/sphinx-Update-0.9.0*.zip
#
# Update sites/JavaDoc for upcoming new releases (service release = 0):
# http://download.eclipse.org/sphinx/previews
# http://download.eclipse.org/sphinx/previews/javadoc
#
# Update sites/JavaDoc for upcoming update releases (service release > 0):
# http://download.eclipse.org/sphinx/updates/0.9.x
# http://download.eclipse.org/sphinx/updates/0.9.x/javadoc
#
# Update sites/JavaDoc for releases:
# http://download.eclipse.org/sphinx/releases/0.9.x
# http://download.eclipse.org/sphinx/releases/0.9.x/javadoc

echo "------------------------------------------------------------------------"
echo "Echoing command line options"
echo "------------------------------------------------------------------------"

echo BUILD_RUN=$BUILD_RUN
echo BUILD_TYPE=$BUILD_TYPE
echo BUILD_ID=$BUILD_ID
echo SERVICE_RELEASE_NUMBER=$SERVICE_RELEASE_NUMBER
echo MERGE_UPDATE_SITE=$MERGE_UPDATE_SITE

#####################
# Adjustable settings
#####################

relengProjectPath=releng/org.eclipse.sphinx.releng.builds
buildUpdateSitePath=$relengProjectPath/repository/target/repository

javadocProjectPath=docs/org.eclipse.sphinx.doc.isv
buildJavadocSitePath=$javadocProjectPath/target/javadoc/reference/api

eclipsePackageVersion=4.6.2
eclipsePackageBuildId=201611241400

targetBasePath=sphinx
targetDownloadsPath=$targetBasePath
updateSiteArchiveFileNamePrefix=sphinx-Update

##################
# Derived settings
##################

buildLocation=${WORKSPACE}/../../$(echo "$BUILD_RUN" | perl -ne 's#.+/([^/]+)/(\d+)/$#\1/builds/\2#; print;')
buildUpdateSiteLocation=$buildLocation/archive/$buildUpdateSitePath
buildUpdateSiteURL=$BUILD_RUN/artifact/$buildUpdateSitePath
buildJavadocSiteLocation=$buildLocation/archive/$buildJavadocSitePath

releaseStream=$(echo "$BUILD_RUN" | perl -ne 's#.+/[a-z]+-(\d+\.\d+)-[a-z]+/\d+/$#\1#; print;')
releaseStreamName=$releaseStream.x
release=$releaseStream.$SERVICE_RELEASE_NUMBER

localRelengProjectLocation=${WORKSPACE}/$relengProjectPath
localUpdateSiteLocation=$localRelengProjectLocation/updates
localDownloadSiteLocation=$localRelengProjectLocation/downloads

eclipseDownloadsLocation=/home/data/httpd/download.eclipse.org
eclipsePackageFileName=eclipse-platform-$eclipsePackageVersion-linux-gtk-x86_64.tar.gz
eclipsePackageLocation=$eclipseDownloadsLocation/eclipse/downloads/drops4/R-$eclipsePackageVersion-$eclipsePackageBuildId/$eclipsePackageFileName
eclipseInstallLocation=$localRelengProjectLocation/eclipse

targetDownloadsLocation=$eclipseDownloadsLocation/$targetDownloadsPath
targetBackupLocation=$localRelengProjectLocation/backup

##################
# Runtime settings
##################

case $BUILD_TYPE in
    I) if [ "$SERVICE_RELEASE_NUMBER" == "0" ];
			then
				targetUpdateSitePath=$targetBasePath/previews
				targetJavadocSitePath=$targetBasePath/previews/javadoc
			else
				targetUpdateSitePath=$targetBasePath/updates/$releaseStreamName
				targetJavadocSitePath=$targetBasePath/updates/$releaseStreamName/javadoc
	   fi
       updateSiteArchiveFileName=$updateSiteArchiveFileNamePrefix-$release.$BUILD_TYPE$BUILD_ID.zip
       ;;
    M|RC) if [ "$SERVICE_RELEASE_NUMBER" == "0" ];
			then
				targetUpdateSitePath=$targetBasePath/previews
				targetJavadocSitePath=$targetBasePath/previews/javadoc
			else
				targetUpdateSitePath=$targetBasePath/updates/$releaseStreamName
				targetJavadocSitePath=$targetBasePath/updates/$releaseStreamName/javadoc
		  fi
          updateSiteArchiveFileName=$updateSiteArchiveFileNamePrefix-$release$BUILD_TYPE$BUILD_ID.zip
          ;;
    R) targetUpdateSitePath=$targetBasePath/releases/$releaseStreamName
       targetJavadocSitePath=$targetBasePath/releases/$releaseStreamName/javadoc
       if [ "$SERVICE_RELEASE_NUMBER" == "0" ];
			then
		       companionUpdateSitePath=$targetBasePath/updates/$releaseStreamName
		       companionJavadocSitePath=$targetBasePath/updates/$releaseStreamName/javadoc
	   fi
       updateSiteArchiveFileName=$updateSiteArchiveFileNamePrefix-$release.zip
       ;;
    T) targetUpdateSitePath=$targetBasePath/test/$releaseStreamName
       targetJavadocSitePath=$targetBasePath/test/$releaseStreamName/javadoc
       updateSiteArchiveFileName=$updateSiteArchiveFileNamePrefix-$release.$BUILD_TYPE$BUILD_ID.zip
       ;;
    *) exit 0
       ;;
esac
targetUpdateSiteLocation="$eclipseDownloadsLocation/$targetUpdateSitePath"
targetJavadocSiteLocation="$eclipseDownloadsLocation/$targetJavadocSitePath"
if [ -n "$companionUpdateSitePath" ];
	then
		companionUpdateSiteLocation="$eclipseDownloadsLocation/$companionUpdateSitePath"
fi
if [ -n "$companionJavadocSitePath" ];
	then
		companionJavadocSiteLocation="$eclipseDownloadsLocation/$companionJavadocSitePath"
fi
localUpdateSiteArchiveLocation=$localDownloadSiteLocation/$updateSiteArchiveFileName

#############################################################################################
# Eclipse installation (required to create merged update site and set p2.mirrorsURL property)
#############################################################################################

if [ ! -f "$eclipseInstallLocation/eclipse" ];
	then
		echo "------------------------------------------------------------------------"
		echo "Installing Eclipse"
		echo "------------------------------------------------------------------------"

		echo "Copying $eclipsePackageLocation to $localRelengProjectLocation"
		cp $eclipsePackageLocation $localRelengProjectLocation
		echo "Unpacking $localRelengProjectLocation/$eclipsePackageFileName"
		tar -xzf $localRelengProjectLocation/$eclipsePackageFileName -C $localRelengProjectLocation
		chmod 700 $eclipseInstallLocation/eclipse
		if [ -f "$eclipseInstallLocation/eclipse" ];
        	then
				echo "Removing $localRelengProjectLocation/$eclipsePackageFileName"
        		rm $localRelengProjectLocation/$eclipsePackageFileName
        	else
                echo "ERROR: Failed to install Eclipse package required for publishing."
                exit
		fi

		echo "Installing WTP Releng tools"
		$eclipseInstallLocation/eclipse -nosplash --launcher.suppressErrors -clean -application org.eclipse.equinox.p2.director -repository http://download.eclipse.org/webtools/releng/repository/ -installIUs org.eclipse.wtp.releng.tools.feature.feature.group
fi

####################
# Publishing process
####################

echo "------------------------------------------------------------------------"
echo "Retrieving build update site"
echo "------------------------------------------------------------------------"

echo "Copying $buildUpdateSiteLocation/* to $localUpdateSiteLocation"
rm -rf $localUpdateSiteLocation
mkdir $localUpdateSiteLocation
cp -r $buildUpdateSiteLocation/* $localUpdateSiteLocation

# Alternative approach:
# echo "Downloading $buildUpdateSiteURL/* to $localUpdateSiteLocation"
# rm -rf $localUpdateSiteLocation
# wget --mirror --execute robots=off --directory-prefix=$localUpdateSiteLocation --no-host-directories --cut-dirs=11 --no-parent --reject="index.html*,*zip*" --timestamping $buildUpdateSiteURL/

echo "------------------------------------------------------------------------"
echo "Creating archived update site"
echo "------------------------------------------------------------------------"

echo "Archiving $localUpdateSiteLocation/* into $localUpdateSiteArchiveLocation"
rm -rf $localDownloadSiteLocation
mkdir $localDownloadSiteLocation
zip -rq $localUpdateSiteArchiveLocation $localUpdateSiteLocation/*

echo "------------------------------------------------------------------------"
echo "Publishing archived update site"
echo "------------------------------------------------------------------------"

echo "Copying $localUpdateSiteArchiveLocation to $targetDownloadsLocation"
mkdir -p $targetDownloadsLocation
cp $localUpdateSiteArchiveLocation $targetDownloadsLocation

if [ "$MERGE_UPDATE_SITE" != "false" ] && [ -f "$targetUpdateSiteLocation/content.jar" ];
	then
		echo "------------------------------------------------------------------------"
		echo "Merging project update site into build update site"
		echo "------------------------------------------------------------------------"

        echo "Merging $targetUpdateSiteLocation into $localUpdateSiteLocation"
        $eclipseInstallLocation/eclipse -nosplash --launcher.suppressErrors -clean -application org.eclipse.equinox.p2.metadata.repository.mirrorApplication -source file:$targetUpdateSiteLocation -destination file:$localUpdateSiteLocation
        $eclipseInstallLocation/eclipse -nosplash --launcher.suppressErrors -clean -application org.eclipse.equinox.p2.artifact.repository.mirrorApplication -source file:$targetUpdateSiteLocation -destination file:$localUpdateSiteLocation
fi

echo "------------------------------------------------------------------------"
echo "Setting p2.mirrorsURL property"
echo "------------------------------------------------------------------------"

echo "Setting p2.mirrorsURL property of $localUpdateSiteLocation to http://www.eclipse.org/downloads/download.php?format=xml&file=/$targetUpdateSitePath (see https://wiki.eclipse.org/WTP/Releng/Tools/addRepoProperties for details)"
$eclipseInstallLocation/eclipse -nosplash --launcher.suppressErrors -clean -application org.eclipse.wtp.releng.tools.addRepoProperties -vmargs -DartifactRepoDirectory=$localUpdateSiteLocation -Dp2MirrorsURL="http://www.eclipse.org/downloads/download.php?format=xml&file=/$targetUpdateSitePath"

if [ ! -f "$localUpdateSiteLocation/p2.index" ];
    then
		echo "------------------------------------------------------------------------"
		echo "Creating p2.index file"
		echo "------------------------------------------------------------------------"

        echo "Creating p2.index file for $localUpdateSiteLocation"
        echo "version = 1" > $localUpdateSiteLocation/p2.index
        echo "metadata.repository.factory.order = content.xml,\!" >> $localUpdateSiteLocation/p2.index
        echo "artifact.repository.factory.order = artifacts.xml,\!" >> $localUpdateSiteLocation/p2.index
fi

if [ -f "$targetUpdateSiteLocation/content.jar" ];
    then
		echo "------------------------------------------------------------------------"
		echo "Creating backup of target update site and JavaDoc"
		echo "------------------------------------------------------------------------"

		echo "Copying $targetUpdateSiteLocation/* to $targetBackupLocation"
        rm -rf $targetBackupLocation
        mkdir $targetBackupLocation
        cp -r $targetUpdateSiteLocation/* $targetBackupLocation/
fi

echo "------------------------------------------------------------------------"
echo "Publishing update site"
echo "------------------------------------------------------------------------"

echo "Removing $targetUpdateSiteLocation"
rm -rf $targetUpdateSiteLocation
echo "Copying $localUpdateSiteLocation/* to $targetUpdateSiteLocation"
mkdir -p $targetUpdateSiteLocation
cp -r $localUpdateSiteLocation/* $targetUpdateSiteLocation
if [ -n "$companionUpdateSitePath" ];
	then
		echo "Removing $companionUpdateSiteLocation"
		rm -rf $companionUpdateSiteLocation
		echo "Copying $localUpdateSiteLocation/* to $companionUpdateSiteLocation"
		mkdir -p $companionUpdateSiteLocation
		cp -r $localUpdateSiteLocation/* $companionUpdateSiteLocation
fi


echo "------------------------------------------------------------------------"
echo "Publishing JavaDoc"
echo "------------------------------------------------------------------------"

echo "Removing $targetJavadocSiteLocation"
rm -rf $targetJavadocSiteLocation
echo "Copying $buildJavadocSiteLocation/* to $targetJavadocSiteLocation"
mkdir -p $targetJavadocSiteLocation
cp -r $buildJavadocSiteLocation/* $targetJavadocSiteLocation
if [ -n "$companionJavadocSitePath" ];
	then
		echo "Removing $companionJavadocSiteLocation"
		rm -rf $companionJavadocSiteLocation
		echo "Copying $buildJavadocSiteLocation/* to $companionJavadocSiteLocation"
		mkdir -p $companionJavadocSiteLocation
		cp -r $buildJavadocSiteLocation/* $companionJavadocSiteLocation
fi