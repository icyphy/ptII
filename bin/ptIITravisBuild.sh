#!/bin/bash

# Usage ptIITravisBuild.sh CurrentNumberOfTravisSeconds
#
# CurrentNumberOfTravisSeconds is $SECONDS in .travis.yml and
# represents the number of seconds that the job has been running.

# This script is called by $PTII/.travis.yml as part of the Travis-ci
# build at https://travis-ci.org/icyphy/ptII/

# Because we are using if statements, we use a script instead of
# trying to do all of this in $PTII/.travis.yml, see
# https://groups.google.com/forum/#!topic/travis-ci/uaAP9zEdiCg
# http://steven.casagrande.io/articles/travis-ci-and-if-statements/

# The script invokes various builds depending on various environment variables.

# This script depends on a number of environment variables being passed to it.

# To test, set the environment variable(s) and invoke the script.
#
# GITHUB_TOKEN is used to update the gh-pages branch of the ptII repo
# and the deployment area at https://github.com/icyphy/ptII/releases
#
# PT_TRAVIS* Variables that start with "PT_TRAVIS" name targets in this file.
#
# GEOCODING_TOKEN, SPACECADET_TOKEN, WEATHER_TOKEN are used to set up keys

# Below are the comamands to run to try out various targets:
#
#   PT_TRAVIS_P=true GITHUB_TOKEN=fixme sh -x $PTII/bin/ptIITravisBuild.sh
#   PT_TRAVIS_DOCS=true $PTII/bin/ptIITravisBuild.sh
#   PT_TRAVIS_GITHUB_ISSUE_JUNIT=true $PTII/bin/ptIITravisBuild.sh
#   PT_TRAVIS_PRIME_INSTALLER=true $PTII/bin/ptIITravisBuild.sh
#   PT_TRAVIS_TEST_CAPECODE1_XML=true $PTII/bin/ptIITravisBuild.sh
#   PT_TRAVIS_TEST_CAPECODE2_XML=true $PTII/bin/ptIITravisBuild.sh
#   PT_TRAVIS_TEST_CAPECODE3_XML=true $PTII/bin/ptIITravisBuild.sh
#   PT_TRAVIS_TEST_CORE1_XML=true $PTII/bin/ptIITravisBuild.sh
#   PT_TRAVIS_TEST_CORE2_XML=true $PTII/bin/ptIITravisBuild.sh
#   PT_TRAVIS_TEST_CORE3_XML=true $PTII/bin/ptIITravisBuild.sh
#   PT_TRAVIS_TEST_CORE4_XML=true $PTII/bin/ptIITravisBuild.sh
#   PT_TRAVIS_TEST_CORE5_XML=true $PTII/bin/ptIITravisBuild.sh
#   PT_TRAVIS_TEST_INSTALLERS=true $PTII/bin/ptIITravisBuild.sh
#   PT_TRAVIS_TEST_REPORT_SHORT=true $PTII/bin/ptIITravisBuild.sh
#   PT_TRAVIS_JUNITREPORT=true $PTII/bin/ptIITravisBuild.sh

if [ ! -d $PTII/logs ]; then
    mkdir $PTII/logs
fi    

if [ ! -d $PTII/reports/junit ]; then
    mkdir -p $PTII/reports/junit
fi    

# If openCV is not in the cache, then there is not as much time to
# run our tests, so we adjust accordingly

# The timeout Can't be more than 50 minutes or 3000 seconds.  45
# minutes is cutting it a bit close, so we go with a maximum of 35
# minutes or 2100 seconds.  We can't use Travis' timeout feature
# because we want to copy the output to gh-pages. The timeouts should
# vary so as to avoid git conflicts.

# The amount of time after ant completes.  This must be enough time to
# update gh-pages, update the cache and deploy.  If Travis is timing
# out, then bump this up, but look out for a return code of 137 from
# ant
# core3 timed out 150 seconds, so increase the remaining time after the build to 180 seconds.
timeAfterBuild=180
if [ ! -z "$SECONDS" -a "$SECONDS" -gt 100 ]; then
    echo "$0: SECONDS environment variable is $SECONDS."

    maxTimeout=`expr 3000 - $SECONDS - $timeAfterBuild`
else
    if [ $# -eq 1 ]; then
        echo "$0: Using $1 as current seconds since the start of the job."
        SECONDS=$1
        maxTimeout=`expr 3000 - $SECONDS - $timeAfterBuild`
    else
        echo "$0: SECONDS environment variable not present or less than 100 and no argument passed."
        maxTimeout=2500
    fi
fi
echo "$0: maxTimeout: $maxTimeout, which will be at `expr $maxTimeout + $SECONDS`"


# Timeout a process.
#
# We used to use perl to send SIGALARM, but now we use the timeout
# program to use kill -9.
#
# function timeout() { perl -e 'alarm shift; exec @ARGV' "$@"; }

# We use the timeout utility so that we can use kill -9
# Mac: sudo port timeout

case `uname -s` in
    Darwin)
        echo "If necessary, install timeout with 'sudo port install timeout'"
        TIMEOUTCOMMAND='timeout -9'
        ;;
    *)
        TIMEOUTCOMMAND='timeout -s 9'
        ;;
esac     

# Create token or key files that are secret.
#
# The problem is that some of the tests need a key or token to connect
# to a database.  The solution is to use Travis' encryption keys.  See
# https://docs.travis-ci.com/user/encryption-keys/

# SECRET_REGEX is used to filter secrets out of the log files.
SECRET_REGEX='(GITHUB_TOKEN|GEOCODING_TOKEN|env.SECRET|HEARTBEAT_TOKEN|SPACECADET_TOKEN|WEATHER_TOKEN)'

# Use set +x to hide the token
set +x
if [ ! -z "$SPACECADET_TOKEN" -a ! -f ~/.spacecadet ]; then
    echo "$SPACECADET_TOKEN" > ~/.spacecadet
    ls -l ~/.spacecadet
    export SPACECADET_TOKEN=resetByPtIITravisBuild.sh
fi

# Use set +x to hide the token
set +x
if [ ! -z "$GEOCODING_TOKEN" -a ! -f ~/.ptKeystore/geoCodingKey ]; then
    if [ ! -d ~/.ptKeystore ]; then
        mkdir ~/.ptKeystore
    fi
    echo "$GEOCODING_TOKEN" > ~/.ptKeystore/geoCoderKey
    ls -l ~/.ptKeystore/geoCoderKey
    export GEOCODING_TOKEN=resetByPtIITravisBuild.sh
fi

# Use set +x to hide the token
set +x
if [ ! -z "$HEARTBEAT_TOKEN" -a ! -f ~/.ptKeystore/heartbeatKey ]; then
    if [ ! -d ~/.ptKeystore ]; then
        mkdir ~/.ptKeystore
    fi
    echo "$HEARTBEAT_TOKEN" > ~/.ptKeystore/heartbeatKey
    ls -l ~/.ptKeystore/heartbeatKey
    export HEARTBEAT_TOKEN=resetByPtIITravisBuild.sh
fi

# Used by ptolemy/actor/lib/jjs/modules/httpClient/test/auto/GeoCoderWeather.xml 
# Use set +x to hide the token
set +x
if [ ! -z "$WEATHER_TOKEN" -a ! -f ~/.ptKeystore/weatherKey ]; then
    if [ ! -d ~/.ptKeystore ]; then
        mkdir ~/.ptKeystore
    fi
    echo "$WEATHER_TOKEN" > ~/.ptKeystore/weatherKey
    ls -l ~/.ptKeystore/weatherKey
    export WEATHER_TOKEN=resetByPtIITravisBuild.sh
fi

# set -x

# If the output is more than 10k lines, then Travis fails, so we
# redirect voluminuous output into a log file.


# If Travis is running outside of the Travis cron job, then exit.
#
# This means that we just run a smoke build most of the time, yet once
# a day, we run a cron job.  To always run the tests, set RUN_TESTS to
# true in the Travis environment variable settings.  See
# https://docs.travis-ci.com/user/cron-jobs/#Detecting-Builds-Triggered-by-Cron
exitIfNotCron () {
    if [ "$TRAVIS_EVENT_TYPE" != "cron" ]; then
        if [ "$RUN_TESTS" = "true" ]; then
            echo "$0: RUN_TESTS was set to true, so the tests are being run even though TRAVIS_EVENT_TYPE is \"$TRAVIS_EVENT_TYPE\", which is != cron."
        else
            echo "$0: TRAVIS_EVENT_TYPE is \"$TRAVIS_EVENT_TYPE\", so this target is *not* being run."
            echo "$0: If you want to run the tests anyway, then set RUN_TESTS to true in https://travis-ci.org/icyphy/ptII/settings"
            echo "$0: Exiting"
            exit 0
        fi
    fi        
}

# Run the ant target named by the first argument.
runTarget () {
    target=$1

    # Exit if we are not running a Travis cron job and RUN_TESTS is not set to true.
    exitIfNotCron

    # Download the codeDoc*.jar files so that the docManager.tcl test will pass.
    jars="codeDoc.jar codeDocBcvtb.jar codeDocCapeCode.jar codeDocHyVisual.jar codeDocViptos.jar codeDocVisualSense.jar"
    for jar in $jars
    do
        echo "Downloading $jar: `date`"
        wget --quiet -O $PTII/doc/$jar https://github.com/icyphy/ptII/releases/download/nightly/$jar
        ls -l $PTII/doc/$jar
        (cd $PTII; jar -xf $PTII/doc/$jar)
    done

    # Keep the log file in reports/junit so that we only need to
    # invoke updateGhPages once per target.
    log=$PTII/reports/junit/${target}.txt

    # Number of lines to show from the log file.
    lastLines=50

    # FIXME: we probably want to vary the timeout so that we can avoid
    # git conflicts.
    #timeout=`expr $maxTimeout - 120`
    timeout=$maxTimeout
    echo "$0: Output will appear in $log with timeout $timeout"
    
    # Run the command and log the output.
    $TIMEOUTCOMMAND $timeout ant build $target 2>&1 | egrep -v "$SECRET_REGEX" > $log

    # Get the return value of the ant command and exit if it is non-zero.
    # See https://unix.stackexchange.com/questions/14270/get-exit-status-of-process-thats-piped-to-another
    status=${PIPESTATUS[0]}
    if [ $status -ne 0 ]; then
        echo "$0: `date`: At $SECONDS, ant build $target returned $status, which is non-zero. `date`"
        tail -$lastLines $log
        if [ $status = 137 ]; then
            echo "######################################################"
            echo "$0: WARNING! `date`: Ant probably times out because status = $status, which is 128 + 9. Consider updating timeAfterBuild, which is currently $timeAfterBuild seconds."
            echo "See https://github.com/travis-ci/travis-ci/issues/4192"
            echo "######################################################"
        else
            echo "$0: exiting with a value of $status"
            exit $status
        fi
    else
        echo "$0: `date`: ant build $target returned $status"
        echo "$0: Start of last $lastLines lines of $log"
        tail -$lastLines $log
    fi

    date

    # Free up space for clone of gh-pages.
    df -k .
    rm -rf $PTII/adm/dists
    ant clean

    updateGhPages $PTII/reports/junit reports/
}

# Copy the file or directory named by
# source-file-or-directory to directory-in-gh-pages.  For example:
#   updateGhPages logs/installers.txt logs
# will copy logs/installers.txt to logs in the gh-pages and push it.
# If the last argument ends in a /, then a directory by that name is created.
# The reason we need this is because the Travis deploy to gh-pages seems
# to overwrite everything in the repo.
#
# The -clean option removes the .xml files reports/junit.
#
# -clean should be run early in the build.
#
# Usage:
#   updateGhPages [-clean | -junitreport] fileOrDirectory1 [fileOrDirectory2 ...] destinationDirectory
#   -clean is optional, if present, then the remain arguments are ignored and the reports directory is cleaned.
#   -junitreport is optional, and if present, then "ant junitreport" is run.
#   
#
updateGhPages () {
    echo "updateGhPages(): Start: `date`"
    if [ $1 = "-junitreport" ]; then
        length=$(($#-2))
        sources=${@:2:$length}
        destination=${@: -1}
    elif [ $1 = "-clean" ]; then
        echo "$0: cleaning the reports/junit directory."
    else
        length=$(($#-1))
        sources=${@:1:$length}
        destination=${@: -1}
    fi

    echo "updateGhPages() start: length: $length, sources: $sources, destination: $destination `date`";

    if [ -z "$GITHUB_TOKEN" ]; then
        echo "$0: GITHUB_TOKEN was not set, so $sources will not be copied to $destination in the gh-pages repo."
        return 
    fi

    # See https://git-scm.com/docs/git-pull
    export GIT_MERGE_AUTOEDIT=no
    
    df -k .
    TMP=/tmp/ptIITravisBuild_gh_pages.$$
    if [ ! -d $TMP ]; then
        mkdir $TMP
    fi
    lastwd=`pwd`
    cd $TMP

    # Get to the Travis build directory, configure git and clone the repo
    git config --global user.email "travis@travis-ci.org"
    git config --global user.name "travis-ci"

    # Don't echo GITHUB_TOKEN
    set +x
    git clone --depth=1 --single-branch --branch=gh-pages https://${GITHUB_TOKEN}@github.com/icyphy/ptII gh-pages
    set -x

    cd gh-pages

    if [ "$1" = "-clean" ]; then
        # Don't remove the html files, we want to be able to browse them while the tests are being generated.
        git rm -rf reports/junit/*.xml
        # Don't change 'Travis Build gh-branch' because people filter email on that string.
        git commit -m "Travis Build gh-branch: Removed reports/junit/*.xml so that subsequent tests populate an empty directory." -a
        # --no-edit will avoid a commit edit if there is a merge commit
        GIT_TRACE=true git pull -v --no-edit
        git push origin gh-pages
        git push -f origin gh-pages
        GIT_TRACE=true git pull -v --no-edit
    else
        echo "$destination" | grep '.*/$'
        status=$?
        if [ $status -eq 0 ]; then
            if [ ! -d $destination ]; then
                mkdir -p $destination
                echo "$0: Created $destination in [pwd]."
            fi
        fi        
        echo "$0: Copying $sources to $destination"
        cp -Rf $sources $destination
    fi

    if [ $1 = "-junitreport" ]; then
        # Remove any preexisting report files.
        git rm -rf reports/junit/html
        ant junitreport
    fi

    # JUnit xml output will include the values of the environment,
    # which can include GITHUB_TOKEN, which is supposed to be secret.
    # So, we remove any lines checked in to gh-pages that mentions
    # GITHUB_TOKEN.
    echo "Remove any instances of GITHUB_TOKEN and other vars: "
    date
    # Don't echo GITHUB_TOKEN
    set +x
    files=`find . -type f`
    for file in $files
    do
        egrep -e  "$SECRET_REGEX" $file > /dev/null
	retval=$?
	if [ $retval != 1 ]; then
            echo -n "$file "
            egrep -v "$SECRET_REGEX" $file > $file.tmp
            mv $file.tmp $file
        fi
    done        
    echo "Done."
    set -x

    git add -f .
    date
    git status

    # Commit and Push the Changes.
    # Don't change 'Travis Build gh-branch' because people filter email on that string.
    git commit -m "Travis Build gh-branch: Latest successful travis build $TRAVIS_BUILD_NUMBER auto-pushed $1 to $2 in gh-pages."
    GIT_TRACE=true git pull -v --no-edit
    git push origin gh-pages
    git push -f origin gh-pages

    cd $lastwd
    rm -rf $TMP
    echo "updateGhPages(): End: `date`"
}


#########

# Below here, the if statements should be in alphabetical order
# according to variable name.


# Just build the ptII tree.
#
# This target is always run.  This target produces less than 10K
# lines, so we don't save the output to a log file.
if [ ! -z "$PT_TRAVIS_BUILD_ALL" ]; then
    ant build-all;
    status=$?
    if [ $status -ne 0 ]; then
        echo "$0: ant build-all returned $status, which is non-zero."
        echo "$0: exiting with a value of $status"
        exit $status
    fi
fi

# Build the docs, which are used by other targets.
if [ ! -z "$PT_TRAVIS_DOCS" ]; then
    exitIfNotCron

    # This target is run early, so clean the reports directory.
    updateGhPages -clean

    LOG=$PTII/logs/docs.txt
    # Create the Javadoc jar files for use by the installer and deploy
    # them to Github pages.

    # Note that there is a chance that the installer will use javadoc
    # jar files that are slightly out of date.

    ant javadoc jsdoc 2>&1 | egrep -v "$SECRET_REGEX" > $LOG 
    (cd doc; make install) 2>&1 | egrep -v "$SECRET_REGEX" >> $LOG 

    # No need to check in the log each time because this target is
    # easy to re-run.
    # updateGhPages $PTII/doc/codeDoc $PTII/doc/*.jar doc/
fi


# Quickly test Travis.
#
# This target is not regularly run, it quickly runs "ant -p" and then
# updated the gh-pages repo.
if [ ! -z "$PT_TRAVIS_P" ]; then
    runTarget -p
fi


# Prime the cache in $PTII/vendors/installer so that the installers
# target is faster.
#
# This target is not regularly run, but remains if we have issues
# getting the build working with an empty cache
if [ ! -z "$PT_TRAVIS_PRIME_INSTALLER" ]; then
    exitIfNotCron

    make -C $PTII/adm/gen-11.0 USER=travis PTIIHOME=$PTII COMPRESS=gzip prime_installer
    ls $PTII/vendors/installer
fi


# Run the first batch of CapeCode tests.
if [ ! -z "$PT_TRAVIS_TEST_CAPECODE1_XML" ]; then
    runTarget test.capecode1.xml
fi

# Run the second batch of CapeCode tests.
if [ ! -z "$PT_TRAVIS_TEST_CAPECODE2_XML" ]; then
    runTarget test.capecode2.xml
fi

# Run the third batch of CapeCode tests.
if [ ! -z "$PT_TRAVIS_TEST_CAPECODE3_XML" ]; then
    runTarget test.capecode3.xml
fi

# Run the first batch of core tests.
if [ ! -z "$PT_TRAVIS_TEST_CORE1_XML" ]; then
    runTarget test.core1.xml
fi

# Run the second batch of core tests.
if [ ! -z "$PT_TRAVIS_TEST_CORE2_XML" ]; then
    runTarget test.core2.xml
    ant
    (cd $PTII/ptolemy/domains/space/demo/DOPCenter/; ./updateDOPCenterImage)
fi

# Run the third batch of core tests.
if [ ! -z "$PT_TRAVIS_TEST_CORE3_XML" ]; then
    runTarget test.core3.xml
fi


# Run the fourth batch of core tests.
if [ ! -z "$PT_TRAVIS_TEST_CORE4_XML" ]; then
    runTarget test.core4.xml
fi

# Run the fifth batch of core tests.
if [ ! -z "$PT_TRAVIS_TEST_CORE5_XML" ]; then
    runTarget test.core5.xml
fi

# Run the first batch of export demo tests.
if [ ! -z "$PT_TRAVIS_TEST_EXPORT1_XML" ]; then
    runTarget test.export1.xml
fi

# Build the installers.
#
# We use Travis-ci deploy to upload the release because GitHub has a
# 100Mb limit unless we use Git LFS.
if [ ! -z "$PT_TRAVIS_TEST_INSTALLERS" ]; then
    runTarget test.installers
    ls -l $PTII/adm/gen-11.0
fi


# Run the short tests.
# This target is not typically used, we use the core targets instead.
if [ ! -z "$PT_TRAVIS_TEST_REPORT_SHORT" ]; then
    runTarget test.report.short
fi


# Run junitreport and update https://github.com/icyphy/ptII/issues/1
#
# This target is in the deploy stage, which is run after the other
# targets that generate test data using JUnit.  See
# https://docs.travis-ci.com/user/build-stages/
if [ ! -z "$PT_TRAVIS_JUNITREPORT" ]; then
    exitIfNotCron
    updateGhPages -junitreport $PTII/reports/junit reports/

    # Sleep so that the updated pages can appear on the website.
    sleep 70

    # Update https://github.com/icyphy/ptII/issues/1
    # See https://github.com/icyphy/github-issue-junit
    mkdir node_modules
    npm install @icyphy/github-issue-junit
    export JUNIT_LABEL=junit-results
    export JUNIT_RESULTS_NOT_DRY_RUN=false
    export GITHUB_ISSUE_JUNIT=https://api.github.com/repos/icyphy/ptII
    (cd node_modules/@icyphy/github-issue-junit/scripts; node junit-results.js) 

    # Clean JUnit results older than 30 days.
    (cd node_modules/@icyphy/github-issue-junit/scripts; node clean-issues.js) 
fi
