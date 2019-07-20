# Tests for the FileUtilities class
#
# @Author: Christopher Brooks
#
# @Copyright (c) 2019 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
#######################################################################

# Load the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test TravisTimeout-1.1 {Fail a test signifying a timeout problem with Travis} {
    set travisTest {Unknown}
    puts "Below are the system properties. To determine which Travis test is failing, look for a property that starts with PT_TRAVIS_TEST"

    set envs [java::call System getenv]
    set names [[$envs keySet] iterator]
    while { [$names hasNext] } {
	set name [$names next]
        if {[string first {PT_TRAVIS_TEST} $name] != -1} {
            set travisTest $name
        }
	puts "$name=[$envs get $name]"
    }

    error "$travisTest timed out!\n    This test always fails.  It is called by $PTII/bin/ptIITravisBuild.sh to signify that Travis is having timeout problems.  Typically this is caused by OpenCV being rebuilt.  The problem should go away with the next build after OpenCV is updated in the cache.  See https://wiki.eecs.berkeley.edu/ptexternal/Main/Travis#Caching_2"
} {}
