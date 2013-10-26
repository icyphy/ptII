# Tests for the WatchDog class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2003-2013 The Regents of the University of California.
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

# Tycho test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test WatchDog-1.0 {create a 1 second watchDog} {
    jdkCaptureErr {
	set watchDog [java::new util.testsuite.WatchDog 1000]
	$watchDog setExitOnTimeOut false
	sleep 2   
    } results
    # If we print the results, then the nightly build will see the ***
    # and report an extraneous error in the email summary.
    # Uncomment the next line for debugging only.
    #puts $results
    list \
	    [regexp "util.testsuite.WatchDog went off after 1000ms" \
	    $results] \
	[java::field [java::cast ptolemy.util.test.WatchDog $watchDog] watchDogWentOff]
} {1 1}


test WatchDog-2.0 {create a 2 second watchDog, but cancel after 1 second} {
    jdkCapture {
	set watchDog [java::new util.testsuite.WatchDog 2000]
	$watchDog setExitOnTimeOut false
	sleep 1   
	$watchDog cancel
    } results
	puts $results
    list \
	    [regexp "util.testsuite.WatchDog.cancel().*canceling" $results] \
	[java::field [java::cast ptolemy.util.test.WatchDog $watchDog] watchDogWentOff]
} {1 0}

