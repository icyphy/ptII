# Tests for the MatrixMath Class
#
# @Author: Jeff Tsay
#
# @Version: $Id$
#
# @Copyright (c) 1998-2000 The Regents of the University of California.
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

set epsilon [java::field ptolemy.math.SignalProcessing epsilon]


set a1 [java::new {double[]} 3 [list 3.7 -6.6 0.0003]]
set a2 [java::new {double[]} 3 [list 4862.2 236.1 -36.25]]
set a3 [java::new {double[]} 3 [list -5.7 0.0036 30.3]]

set a12 [java::new {double[]} 6 [list 3.7 -6.6 0.0003 4862.2 236.1 -36.25]]

set b2 [java::new {double[]} 3 [list -56.4 -26.3 4.9]] 

set m3 [java::new {double[][]} 3 [list [list 3.7 -6.6 0.0003] \
                                       [list 4862.2 236.1 -36.25] \
                                       [list -56.4 -26.3 4.9]]]
set m3_2 [java::new {double[][]} 3 [list [list -3.2 -6.6 6.3] \
                                         [list 0.1 9.0 -5.25] \
                                         [list 34.2 26.2 5.1]]]
set m23 [java::new {double[][]} 2 [list [list 3.7 -6.6 0.0003] \
 	                                [list 4862.2 236.1 -36.25]]]
set m23_2 [java::new {double[][]} 2 [list [list -3.2 -6.6 6.3] \
                                          [list 0.1 9.0 -5.25]]]

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
test MatrixMath-1.1 {add double[][] double} {
    set mr [java::call ptolemy.math.MatrixMath {add double[][] double} $m1 -1.7]
    # jdkPrintArray is defined in $PTII/util/testsuite/testDefs.tcl
    set s [java::call ptolemy.math.MatrixMath toString $mr]
} {{{23.3}}}

####################################################################
test MatrixMath-1.2 {add double[][] double} {
    set mr [java::call ptolemy.math.MatrixMath {add double[][] double} $m23 \
    0.25]
    set s [java::call ptolemy.math.MatrixMath toString $mr]
} {{{3.95, -6.35, 0.2503}, {4862.45, 236.35, -36.0}}}

####################################################################
test MatrixMath-1.3 {add double[][] double[][]} {
    set mr [java::call ptolemy.math.MatrixMath {add double[][] double[][]} \
    $m23 $m23_2]
    set s [java::call ptolemy.math.MatrixMath toString $mr]
} {{{0.5, -13.2, 6.3003}, {4862.3, 245.1, -41.5}}}

####################################################################
test MatrixMath-2.1 {determinate double[][] not square} {
    catch {set r [java::call ptolemy.math.MatrixMath determinate $m23]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.MatrixMath.determinate() : matrix argument [2 x 3] is not a square matrix.}}

####################################################################
test MatrixMath-2.2 {determinate double[][]} {
   set r [java::call ptolemy.math.MatrixMath determinate $m3]
   set ok [java::call ptolemy.math.SignalProcessing close $r 144468.485554]
} {1}

####################################################################
test MatrixMath-3.1 {identity int 1} {
   set mr [java::call ptolemy.math.MatrixMath identity 1]
   set s [java::call ptolemy.math.MatrixMath toString $mr]
} {{{1.0}}}

####################################################################
test MatrixMath-3.2 {identity int 2} {
   set mr [java::call ptolemy.math.MatrixMath identity 2]
   set s [java::call ptolemy.math.MatrixMath toString $mr]
} {{{1.0, 0.0}, {0.0, 1.0}}}

####################################################################
test MatrixMath-3.3 {identity int 3} {
   set mr [java::call ptolemy.math.MatrixMath identity 3]
   set s [java::call ptolemy.math.MatrixMath toString $mr]
} {{{1.0, 0.0, 0.0}, {0.0, 1.0, 0.0}, {0.0, 0.0, 1.0}}}

####################################################################
test MatrixMath-4.1 {inverse double[][] not square} {
    catch {set r [java::call ptolemy.math.MatrixMath inverse $m23]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.MatrixMath.inverse() : matrix argument [2 x 3] is not a square matrix.}}

####################################################################
test MatrixMath-4.2 {inverse double[][]} {
    set mr [java::call ptolemy.math.MatrixMath inverse $m3]
    set s [java::call ptolemy.math.MatrixMath toString $mr]
    # Get rid of trailing ,
    regsub -all {,} $s {} stmp
    ptclose $stmp {{{0.00140871553556869 0.000223800435617599 0.00165558024009741} {-0.150761461342092 0.000125611616474079 0.000938499905222039} {-0.79297446471244 0.00325018981267364 0.228174953683435}}}
} {1} 

####################################################################
test MatrixMath-4.3 {negative double[][]} {
   set mr [java::call ptolemy.math.MatrixMath negative $m23]
   set s [java::call ptolemy.math.MatrixMath toString $mr]
} {{{-3.7, 6.6, -3.0E-4}, {-4862.2, -236.1, 36.25}}}

####################################################################
test MatrixMath-5.1 {subtract double[][] double[][]} {
   set mr [java::call ptolemy.math.MatrixMath subtract $m23 $m23_2]
   set s [java::call ptolemy.math.MatrixMath toString $mr]
   # Get rid of trailing ,
   regsub -all {,} $s {} stmp
   ptclose $stmp {{{6.9 0.0 -6.2997} {4862.1 227.1 -31.0}}}
} {1}

####################################################################
test MatrixMath-6.1 {toMatrixFromArray double[][] int int} {
   set mr [java::call ptolemy.math.MatrixMath toMatrixFromArray $a12 2 3]
   set ok [java::call ptolemy.math.MatrixMath {within double[][] double[][] \
           double} $mr $m23 $epsilon]
} {1}

####################################################################
test MatrixMath-7.1 {trace double[][] not square} {
    catch {set r [java::call ptolemy.math.MatrixMath trace $m23]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: ptolemy.math.MatrixMath.trace() : matrix argument [2 x 3] is not a square matrix.}}

####################################################################
test MatrixMath-7.2 {trace double[][]} {
   set r [java::call ptolemy.math.MatrixMath trace $m3]
   set ok [java::call ptolemy.math.SignalProcessing close $r 244.7]
} {1}

####################################################################
test MatrixMath-8.1 {transpose double[][]} {
   set mr [java::call ptolemy.math.MatrixMath transpose $m23]
   set s [java::call ptolemy.math.MatrixMath toString $mr]
} {{{3.7, 4862.2}, {-6.6, 236.1}, {3.0E-4, -36.25}}}
