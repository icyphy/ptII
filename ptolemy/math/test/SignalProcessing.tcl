# Tests for the SignalProcessing Class
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1998 The Regents of the University of California.
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

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

# NOTE: there is way too much resolution in these numeric tests.
#  The results are unlikely to be the same on all platforms.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then { 
    source testDefs.tcl
} {}

set PI [java::field java.lang.Math PI]

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

####################################################################
test SignalProcessing-1.1 {close} {
    set EPSILON [java::field ptolemy.math.SignalProcessing EPSILON]
    set testpairslist [list \
	    [list 1 1] \
	    [list -1 [expr {-1 + $EPSILON/2}]] \
	    [list -1 [expr {-1 - $EPSILON/2}]] \
	    [list 1 2] \
	    [list -1 2] \
	    [list 1 -2] \
	    [list -1 -2] \
	    [list [java::field java.lang.Double POSITIVE_INFINITY] 1] \
	    [list [java::field java.lang.Double NEGATIVE_INFINITY] 1] \
	    [list [java::field java.lang.Double NaN] 1]\
	    [list [java::field java.lang.Double MIN_VALUE] 1] \
	    [list [java::field java.lang.Double MAX_VALUE] 1] \
	    [list [java::field java.lang.Double POSITIVE_INFINITY] \
	          [java::field java.lang.Double POSITIVE_INFINITY]] \
	    [list [java::field java.lang.Double NEGATIVE_INFINITY] \
	          [java::field java.lang.Double NEGATIVE_INFINITY]] \
	    [list [java::field java.lang.Double NaN] \
	          [java::field java.lang.Double NaN]] \
	    [list [java::field java.lang.Double MIN_VALUE] \
	          [java::field java.lang.Double MIN_VALUE]] \
	    [list [java::field java.lang.Double MAX_VALUE] \
	          [java::field java.lang.Double MAX_VALUE]] \
	    ]

    set results {}

    foreach testpair $testpairslist {
	set a [lindex $testpair 0]
	set b [lindex $testpair 1]
	if [catch {set callresults \
		[java::call ptolemy.math.SignalProcessing close  $a $b]} \
		errmsg] {
	    lappend results $errmsg
	} else {
	    lappend results $callresults
	}
    }
    return $results
} {1 1 1 0 0 0 0 0 0 0 0 0 1 1 1 1 1}

