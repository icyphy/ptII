# Tests for the MatrixMath Class
#
# @Author: Edward A. Lee, Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1998-1999 The Regents of the University of California.
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

set a1 [java::new {double[]} 3 [list 3.7 -6.6 0.0003]]
set a2 [java::new {double[]} 3 [list 4826.2 236.1 -36.21]]
set a3 [java::new {double[]} 3 [list -5.7 0.0036 30.3]]

set b2 [java::new {double[]} 3 [list -56.4 -26.3 4.9]] 

set m3 [java::new {double[][]} 3 [list [list 3.7 -6.6 0.0003] \
                                       [list 4862.2 236.1 -36.25] \
                                       [list -56.4 -26.3 4.9]]]
set m3_2 [java::new {double[][]} 3 [list [list -3.2 -6.6 6.3] \
                                         [list 0.1 9.0 -5.25] \
                                         [list 34.2 26.2 5.1]]]
set m1 [java::new {double[][]} 1 [list [list 25.0]]]

proc javaPrintArray {javaArrayObj} {
    set result {}
    for {set i 0} {$i < [$javaArrayObj length]} {incr i} {
	lappend result [[$javaArrayObj get $i] toString]
    }
    return $result
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

####################################################################
test MatrixMath-1.2 {add double[][] double} {
    set mr [java::call ptolemy.math.MatrixMath {add double[][] double} $m1 -1.7]
    # jdkPrintArray is defined in $PTII/util/testsuite/testDefs.tcl
    set s [java::call ptolemy.math.MatrixMath toString $mr]
} {{{23.3}}}

####################################################################
test MatrixMath-1.2 {add double[][] double} {
    set mr [java::call ptolemy.math.MatrixMath {add double[][] double} $m3 0.25]
    set s [java::call ptolemy.math.MatrixMath toString $mr]
} {{{3.95, -6.35, 0.2503}, {4862.45, 236.35, -36.0}, {-56.15, -26.05, 5.15}}}

####################################################################
test MatrixMath-1.2 {add double[][] double[][]} {
    set mr [java::call ptolemy.math.MatrixMath {add double[][] double[][]} \
    $m3 $m3_2]
    set s [java::call ptolemy.math.MatrixMath toString $mr]
} {{{0.5 -13.2 6.3003}, {4862.3 245.1 -41.5}, {-22.2 -0.1 10.0}}}



