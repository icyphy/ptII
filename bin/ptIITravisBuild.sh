#!/bin/sh -x

# This script is called by $PTII/.travis.yml as part of the Travis-ci
# build at https://travis-ci.org/icyphy/ptII/

# This script depends on a number of environment variables being passed to it.

# The script invokes various builds depending on various environment variables.

# To test, set the environment variable:
#   PT_TRAVIS_P=true GITHUB_TOKEN=fixme sh -x $PTII/bin/ptIITravisBuild.sh
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

# This shell procedure copies the file or directory named by
# source-file-or-directory to directory-in-gh-pages.  For example
#   updateGhPages logs/installers.txt logs
# will copy logs/installers.txt to logs in the gh-pages and push it.
# The reason we need this is because the Travis deploy to gh-pages seems
# to overwrite everything in the repo.

updateGhPages () {
    if [ -z "$GITHUB_TOKEN" ]; then
        echo "$0: GITHUB_TOKEN was not set, so $1 will not be copied to $2 in the gh-pages repo."
        return 
    fi

    TMP=/tmp/ptIITravisBuild_gh_pages.$$
    if [ ! -d $TMP ]; then
        mkdir $TMP
    fi
    cd $TMP

    # Get to the Travis build directory, configure git and clone the repo
    git config --global user.email "travis@travis-ci.org"
    git config --global user.name "travis-ci"

    # Don't echo GITHUB_TOKEN
    set +x
    git clone --depth=50 --branch=master --single-branch --branch=gh-pages https://${GITHUB_TOKEN}@github.com/icyphy/ptII gh-pages
    set -x

    # Commit and Push the Changes
    cd gh-pages
    echo "$2" | grep '.*/$'
    status=$?
    if [ $status -eq 0 ]; then
        if [ ! -d $2 ]; then
            mkdir -p $2
            echo "$0: Created $2 in [pwd]."
        fi
    fi        
    cp -Rf $1 $2
    git add -f .
    git commit -m "Lastest successful travis build $TRAVIS_BUILD_NUMBER auto-pushed $1 to $2 in gh-pages."
    git push -fq origin gh-pages
    # rm -rf $TMP
}

# Below here, the if statements should be in alphabetical order
# according to variable name.

# These two builds produce less than 10K lines, so we don't save the
# output to a log file.
if [ ! -z "$PT_TRAVIS_BUILD_ALL" ]; then
    ant build-all;
fi

if [ ! -z "$PT_TRAVIS_DOCS" ]; then \
    ant javadoc jsdoc;
    updateGhPages $PTII/doc/codeDoc doc/codeDoc/
    updateGhPages $PTII/doc/jsdoc doc/jsdoc/
fi

# Use this for testing, it quickly runs "ant -p" and then updated the gh-pages repo.
if [ ! -z "$PT_TRAVIS_P" ]; then
    LOG=$PTII/logs/ant_p.txt
    echo "$0: Output will appear in $LOG"
    ant -p | grep -v GITHUB_TOKEN > $LOG 2>&1

    echo "$0: Start of last 100 lines of $LOG"
    tail -100 $LOG
    updateGhPages $LOG logs/
fi

# Build the installers.
if [ ! -z "$PT_TRAVIS_INSTALLERS" ]; then
    LOG=$PTII/logs/installers.txt
    echo "$0: Output will appear in $LOG"
    ant installers | grep -v GITHUB_TOKEN > $LOG 2>&1

    echo "$0: Start of last 100 lines of $LOG"
    tail -100 $LOG
    updateGhPages $LOG logs/

    ls $PTII/adm/gen-11.0
    if [ -f $PTII/adm/gen-11.0/ptII11.0.devel.setup.mac.jar ]; then
        updateGhPages $PTII/adm/gen-11.0/ptII11.0.devel.setup.mac.jar
        downloads/
    fi
fi

# Run the CapeCode tests.
if [ ! -z "$PT_TRAVIS_TEST_CAPECODE_XML" ]; then
    LOG=$PTII/logs/test.capecode.xml
    echo "$0: Output will appear in $LOG"
    ant test.capecode.xml | grep -v GITHUB_TOKEN > $LOG 2>&1

    echo "$0: Start of last 100 lines of $LOG"
    tail -100 $PTII/logs/test.capecode.xml.txt
    updateGhPages $LOG logs/
fi

# Run the short tests.
if [ ! -z "$PT_TRAVIS_TEST_REPORT_SHORT" ]; then
    LOG=$PTII/logs/test.report.short.txt
    echo "$0: Output will appear in $LOG"
    ant test.report.short | grep -v GITHUB_TOKEN > $LOG 2>&1

    echo "$0: Start of last 100 lines of $LOG"
    tail -100 $LOG
    updateGhPages $LOG logs

fi

