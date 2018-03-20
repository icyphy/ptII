#!/bin/bash -x

# This script is called by $PTII/.travis.yml as part of the Travis-ci
# build at https://travis-ci.org/icyphy/ptII/

# This script depends on a number of environment variables being passed to it.

# The script invokes various builds depending on various environment variables.

# To test, set the environment variable:
#   PT_TRAVIS_P=true GITHUB_TOKEN=fixme sh -x $PTII/bin/ptIITravisBuild.sh
#   PT_TRAVIS_DOCS=true $PTII/bin/ptIITravisBuild.sh
#   PT_TRAVIS_INSTALLERS=true $PTII/bin/ptIITravisBuild.sh
#   PT_TRAVIS_TEST_CAPECODE_XML=true $PTII/bin/ptIITravisBuild.sh
#   PT_TRAVIS_TEST_REPORT_SHORT=true $PTII/bin/ptIITravisBuild.sh

# Because we are using if statements, we use a script, see
# https://groups.google.com/forum/#!topic/travis-ci/uaAP9zEdiCg
# http://steven.casagrande.io/articles/travis-ci-and-if-statements/

if [ ! -d $PTII/logs ]; then
    mkdir $PTII/logs
fi    

# Usage: updateGhPages source-file-or-directory directory-in-gh-pages


TIMEOUT_INSTALLERS=2400

# Copy the file or directory named by
# source-file-or-directory to directory-in-gh-pages.  For example
#   updateGhPages logs/installers.txt logs
# will copy logs/installers.txt to logs in the gh-pages and push it.
# If the last argument ends in a /, then a directory by that name is created.
# The reason we need this is because the Travis deploy to gh-pages seems
# to overwrite everything in the repo.
# Usage:
#   updateGhPages fileOrDirectory1 [fileOrDirectory2 ...] destinationDirectory
#
updateGhPages () {
    length=$(($#-1))
    sources=${@:1:$length}
    destination=${@: -1}

    if [ -z "$GITHUB_TOKEN" ]; then
        echo "$0: GITHUB_TOKEN was not set, so $sources will not be copied to $destination in the gh-pages repo."
        return 
    fi

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
    git clone --depth=50 --single-branch --branch=gh-pages https://${GITHUB_TOKEN}@github.com/icyphy/ptII gh-pages
    set -x

    # Commit and Push the Changes
    cd gh-pages
    echo "$destination" | grep '.*/$'
    status=$?
    if [ $status -eq 0 ]; then
        if [ ! -d $destination ]; then
            mkdir -p $destination
            echo "$0: Created $destination in [pwd]."
        fi
    fi        

    cp -Rf $sources $destination

    # JUnit xml output will include the values of the environment,
    # which can include GITHUB_TOKEN, which is supposed to be secret.
    # So, we remove any lines checked in to gh-pages that mentions
    # GITHUB_TOKEN.
    echo "Remove any instances of GITHUB_TOKEN: "
    date
    # Don't echo GITHUB_TOKEN
    set +x
    files=`find . -type f`
    for file in $files
    do
        egrep -e  "GITHUB_TOKEN" $file > /dev/null
	retval=$?
	if [ $retval != 1 ]; then
            echo -n "$file "
            egrep -v "GITHUB_TOKEN" $file > $file.tmp
            mv $file.tmp $file
        fi
    done        
    echo "Done."
    set -x

    git add -f .
    date
    git pull
    date
    git commit -m "Lastest successful travis build $TRAVIS_BUILD_NUMBER auto-pushed $1 to $2 in gh-pages."
    git pull
    git push origin gh-pages
    git push -f origin gh-pages

    cd $lastwd
    rm -rf $TMP
}

# Timeout a process.
function timeout() { perl -e 'alarm shift; exec @ARGV' "$@"; }

# Below here, the if statements should be in alphabetical order
# according to variable name.

# These two builds produce less than 10K lines, so we don't save the
# output to a log file.
if [ ! -z "$PT_TRAVIS_BUILD_ALL" ]; then
    ant build-all;
fi

if [ ! -z "$PT_TRAVIS_DOCS" ]; then \
    LOG=$PTII/logs/docs.txt
    # Create the Javadoc jar files for use by the installer Note that
    # there is a chance that the installer will use javadoc jar files
    # that are slightly out of date.
    ant javadoc jsdoc 2>&1 | grep -v GITHUB_TOKEN > $LOG 
    (cd doc; make install) 2>&1 | grep -v GITHUB_TOKEN >> $LOG 
    updateGhPages $PTII/doc/codeDoc $PTII/doc/*.jar doc/
fi

# Use this for testing, it quickly runs "ant -p" and then updated the gh-pages repo.
if [ ! -z "$PT_TRAVIS_P" ]; then
    LOG=$PTII/logs/ant_p.txt
    echo "$0: Output will appear in $LOG"
    ant -p 2>&1 | grep -v GITHUB_TOKEN > $LOG 

    echo "$0: Start of last 100 lines of $LOG"
    tail -100 $LOG
    updateGhPages $LOG logs/
fi

# Build the installers.
if [ ! -z "$PT_TRAVIS_INSTALLERS" ]; then
    LOG=$PTII/logs/installers.txt
    echo "$0: Output will appear in $LOG"
    
    # Copy any jar files that may have previously been created so that the build is faster.
    jars="codeDoc.jar codeDocBcvtb.jar codeDocCapeCode.jar codeDocHyVisual.jar codeDocViptos.jar codeDocVisualSense.jar"
    for jar in $jars
    do
        echo $jar
        wget -O $PTII/doc/$jar https://icyphy.github.io/ptII/doc/$jar
        ls -l $PTII/doc/$jar
        (cd $PTII; jar -xf $PTII/doc/$jar)
    done

    # Number of seconds to run the subprocess.  Can't be more than 50
    # minutes or 3000 seconds.  45 minutes is cutting it a bit close,
    # so we go with a maximum of 40 minutes or 2400 seconds.  We can't
    # use Travis' timeout feature because we want to copy the output
    # to gh-pages. The timeouts should vary so as to avoid git
    # conflicts.

    timeout 2400 ant installers 2>&1 | grep -v GITHUB_TOKEN > $LOG
 
    echo "$0: Start of last 100 lines of $LOG"
    tail -100 $LOG
    updateGhPages $LOG logs/

    ls $PTII/adm/gen-11.0
    if [ -f $PTII/adm/gen-11.0/ptII11.0.devel.setup.mac.jar ]; then
        updateGhPages $PTII/adm/gen-11.0/ptII11.0.devel.setup.mac.jar downloads/
    fi
fi

# Run the CapeCode tests.
if [ ! -z "$PT_TRAVIS_TEST_CAPECODE_XML" ]; then
    LOG=$PTII/logs/test.capecode.xml.txt
    echo "$0: Output will appear in $LOG"
    
    timeout 2100 ant build test.capecode.xml 2>&1 | grep -v GITHUB_TOKEN > $LOG 

    echo "$0: Start of last 100 lines of $LOG"
    tail -100 $PTII/logs/test.capecode.xml.txt
    updateGhPages $LOG logs/
    ant junitreport
    updateGhPages $PTII/reports/junit reports/
fi

# Run the short tests.
if [ ! -z "$PT_TRAVIS_TEST_REPORT_SHORT" ]; then
    LOG=$PTII/logs/test.report.short.txt
    echo "$0: Output will appear in $LOG"

    # The timeouts should vary so as to avoid git conflicts.
    timeout 1800 ant build test.report.short 2>&1 | grep -v GITHUB_TOKEN > $LOG 

    echo "$0: Start of last 100 lines of $LOG"
    tail -100 $LOG
    updateGhPages $LOG logs
    ant junitreport
    updateGhPages $PTII/reports/junit reports/
fi

