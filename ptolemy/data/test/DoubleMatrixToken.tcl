# Tests for the DoubleMatrixToken class
#
# @Author: Neil Smyth
#
# @Version $Id$
#
# @Copyright (c) 1997-2002 The Regents of the University of California.
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

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then { 
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
# 
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#

######################################################################
####
# 
test DoubleMatrixToken-1.0 {Create an empty instance} {
    set p [java::new ptolemy.data.DoubleMatrixToken]
    $p toString
} {[0.0]}

######################################################################
####
# 
test DoubleMatrixToken-1.1 {Create a non-empty instance from an double} {
    set a [java::new {double[][]} {2 2} {{5 4} {3 2}}]
    set p [java::new {ptolemy.data.DoubleMatrixToken double[][]} $a]
    $p toString
} {[5.0, 4.0; 3.0, 2.0]}

######################################################################
####
# 
test DoubleMatrixToken-1.2 {Create a non-empty instance from an String} {
    set p [java::new {ptolemy.data.DoubleMatrixToken String} "\[5.0, 4.0; 3.0, 2.0\]"]
    $p toString
} {[5.0, 4.0; 3.0, 2.0]}

######################################################################
####
# 
test DoubleMatrixToken-2.0 {Create a non-empty instance and query its value as an double} {
    set res1 [$p doubleMatrix]
    list [jdkPrintArray [$res1 get 0]] [jdkPrintArray [$res1 get 1]]
} {{5.0 4.0} {3.0 2.0}}

######################################################################
####
# 
test DoubleMatrixToken-2.1 {Create a non-empty instance and query its value as a double} {
    set res1 [$p doubleMatrix]
    list [jdkPrintArray [$res1 get 0]] [jdkPrintArray [$res1 get 1]]
} {{5.0 4.0} {3.0 2.0}}

######################################################################
####
# 
test DoubleMatrixToken-2.2 {Create a non-empty instance and query its value as a long} {
    catch {$p longMatrix} result
    list $result
} {{ptolemy.kernel.util.IllegalActionException: ptolemy.data.DoubleMatrixToken cannot be converted to a long matrix.}}

######################################################################
####
# 
test DoubleMatrixToken-2.3 {Create a non-empty instance and query its value as a string} {
    set res1 [$p toString]
    list $res1
} {{[5.0, 4.0; 3.0, 2.0]}}

######################################################################
####
#
test DoubleMatrixToken-2.4 {Create a non-empty instance and query its value as a complex#} {
    set res1 [$p complexMatrix]
    list [jdkPrintArray [$res1 get 0]] [jdkPrintArray [$res1 get 1]]
} {{{5.0 + 0.0i} {4.0 + 0.0i}} {{3.0 + 0.0i} {2.0 + 0.0i}}}

######################################################################
####
# 
test DoubleMatrixToken-2.5 {Test additive identity} {
    set token [$p zero] 
    list [$token toString]
} {{[0.0, 0.0; 0.0, 0.0]}}
######################################################################
####
# 
test DoubleMatrixToken-2.6 {Test multiplicative identity} {
    set token [$p one]
    list [$token toString]
} {{[1.0, 0.0; 0.0, 1.0]}}

######################################################################
####
# Test addition of doubles to Token types below it in the lossless 
# type hierarchy, and with other doubles.
test DoubleMatrixToken-3.0 {Test adding doubles.} {
    set b [java::new {double[][]} {2 2} {{2 1} {3 0}}]
    set q [java::new {ptolemy.data.DoubleMatrixToken double[][]} $b]
    set res1 [$p add $q]

    list [$res1 toString] 
} {{[7.0, 5.0; 6.0, 2.0]}}

######################################################################
####
# Test division of ints with Token types below it in the lossless 
# type hierarchy, and with other doubles. Note that dividing doubles could 
# give a double.
test DoubleMatrixToken-4.0 {Test dividing doubles.} {
    catch {[set res1 [$p divide $q]]} e1

    list $e1
} {{ptolemy.kernel.util.IllegalActionException: Division not supported for ptolemy.data.DoubleMatrixToken divided by ptolemy.data.DoubleMatrixToken.}}

######################################################################
####
# Test equals operator applied to other doubles and Tokens types 
# below it in the lossless type hierarchy.
test DoubleMatrixToken-5.0 {Test equality between doubles.} {
    set q2 [java::new {ptolemy.data.DoubleMatrixToken double[][]} $b]
    set res1 [$q isEqualTo $q2]
    set res2 [$q isEqualTo $p]
    set res3 [$q isCloseTo $q2]
    set res4 [$q isCloseTo $p]

    list [$res1 toString] [$res2 toString] \
	    [$res3 toString] [$res4 toString]
} {true false true false}

test DoubleMatrixToken-5.5 {Test isCloseTo between doubles.} {
    set epsilon 0.001
    set oldEpsilon [java::field ptolemy.math.Complex epsilon]
    java::field ptolemy.math.Complex epsilon $epsilon

    set bClose [java::new {double[][]} {2 2} \
	    [list [list [expr {2.0 + (0.5 * $epsilon) } ] 1] \
	    [list  3 [expr {0.0 - (0.5 * $epsilon) } ] ] ] ]

    set q2Close [java::new {ptolemy.data.DoubleMatrixToken double[][]} \
	    $bClose]
    set res1 [$q2 isCloseTo $q2Close]
    set res2 [$q2Close isCloseTo $q]
    set res3 [$q2Close isCloseTo $p]

    java::field ptolemy.math.Complex epsilon $oldEpsilon

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {true true false}

######################################################################
####
# Test modulo operator between doubles and doubles.
test DoubleMatrixToken-6.0 {Test modulo between doubles.} {
    catch {[set res1 [$p modulo $q]]} e1

    list $e1
} {{ptolemy.kernel.util.IllegalActionException: Modulo operation not supported: ptolemy.data.DoubleMatrixToken modulo ptolemy.data.DoubleMatrixToken.}}

######################################################################
####
# Test multiply operator between doubles and doubles.
test DoubleMatrixToken-7.0 {Test multiply operator between doubles.} {
    set b3 [java::new {double[][]} {2 3} {{2 1 3} {3 1 6}}]
    set q3 [java::new {ptolemy.data.DoubleMatrixToken double[][]} $b3]
    set res1 [$p multiply $q]
    set res2 [$p multiply $q3]
    catch {$q3 multiply $p} res3

    list [$res1 toString] [$res2 toString] $res3
} {{[22.0, 5.0; 12.0, 3.0]} {[22.0, 9.0, 39.0; 12.0, 5.0, 21.0]} {ptolemy.kernel.util.IllegalActionException: Cannot multiply matrix with 3 columns by a matrix with 2 rows.}}

######################################################################
####
# Test subtract operator between doubles and doubles.
test DoubleMatrixToken-8.0 {Test subtract operator between doubles.} {
    set b [java::new {double[][]} {2 2} {{2 1} {3 1}}]
    set q [java::new {ptolemy.data.DoubleMatrixToken double[][]} $b]
    set res1 [$p subtract $q]

    list [$res1 toString] 
} {{[3.0, 3.0; 0.0, 1.0]}}

######################################################################
####
# 
test DoubleMatrixToken-9.0 {Test equals} {
    set p1 [java::new {ptolemy.data.DoubleMatrixToken String} "\[1.0, 2.0; 3.0, 4.0\]"]
    set p2 [java::new {ptolemy.data.DoubleMatrixToken String} "\[1.0, 2.0; 3.0, 4.0\]"]
    set p3 [java::new {ptolemy.data.DoubleMatrixToken String} "\[9.0, 8.0; 7.0, 6.0\]"]
    list [$p1 equals $p1] [$p1 equals $p2] [$p1 equals $p3]
} {1 1 0}

######################################################################
####
# 
test DoubleMatrixToken-10.0 {Test hashCode} {
    set p1 [java::new {ptolemy.data.DoubleMatrixToken String} "\[1.0, 2.0; 3.0, 4.0\]"]
    set p2 [java::new {ptolemy.data.DoubleMatrixToken String} "\[1.0, 2.0; 3.0, 4.0\]"]
    set p3 [java::new {ptolemy.data.DoubleMatrixToken String} "\[9.0, 8.0; 7.0, 6.0\]"]
    list [$p1 hashCode] [$p2 hashCode] [$p3 hashCode]
} {10 10 30}

