# Tests for the DoubleMatrixToken class
#
# @Author: Neil Smyth, Shuvra S. Bhattacharyya 
#
# @Version $Id$
#
# @Copyright (c) 1997-2003 The Regents of the University of California.
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
test DoubleMatrixToken-1.1 {Create a non-empty instance from a double} {
    set a [java::new {double[][]} {2 2} {{5 4} {3 2}}]
    set p [java::new {ptolemy.data.DoubleMatrixToken double[][]} $a]
    $p toString
} {[5.0, 4.0; 3.0, 2.0]}

######################################################################
####
# 
test DoubleMatrixToken-1.2 {Create a non-empty instance from a String} {
    set p [java::new {ptolemy.data.DoubleMatrixToken String} "\[5.0, 4.0; 3.0, 2.0\]"]
    $p toString
} {[5.0, 4.0; 3.0, 2.0]}

######################################################################
####
# 
test DoubleMatrixToken-1.3 {Create a non-empty instance from a double array} {
    set a2 [java::new {double[]} {5} {2 5.5 4 3 2}]
    set p2 [java::new {ptolemy.data.DoubleMatrixToken double[] int int} $a2 5 1]
    $p2 toString
} {[2.0; 5.5; 4.0; 3.0; 2.0]}


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
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' to the type long matrix.}}

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
# 
test DoubleMatrixToken-2.7 {Test matrixToArray} {
    set array [java::call ptolemy.data.MatrixToken matrixToArray [$p one]]
    $array toString
} {{1.0, 0.0, 0.0, 1.0}}

######################################################################
####
# Test addition of doubles to Token types below it in the lossless 
# type hierarchy, and with other doubles.
test DoubleMatrixToken-3.0 {Test adding DoubleMatrixToken to IntMatrixToken.} {
    set b [java::new {int[][]} {2 2} {{2 1} {3 1}}]
    set q [java::new {ptolemy.data.IntMatrixToken int[][]} $b]
    set res1 [$p add $q]
    set res2 [$p addReverse $q]
    set res3 [$q add $p]
    set res4 [$q addReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[7.0, 5.0; 6.0, 3.0]} {[7.0, 5.0; 6.0, 3.0]} {[7.0, 5.0; 6.0, 3.0]} {[7.0, 5.0; 6.0, 3.0]}}

test DoubleMatrixToken-3.1 {Test adding DoubleMatrixToken to DoubleMatrixToken.} {
    set b [java::new {double[][]} {2 2} {{2.0 1.0} {3.0 1.0}}]
    set q [java::new {ptolemy.data.DoubleMatrixToken double[][]} $b]
    set res1 [$p add $q]
    set res2 [$p addReverse $q]
    set res3 [$q add $p]
    set res4 [$q addReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[7.0, 5.0; 6.0, 3.0]} {[7.0, 5.0; 6.0, 3.0]} {[7.0, 5.0; 6.0, 3.0]} {[7.0, 5.0; 6.0, 3.0]}}

test DoubleMatrixToken-3.2 {Test adding DoubleMatrixToken to BooleanMatrixToken.} {
    set b [java::new {boolean[][]} {2 2} {{true false} {true false}}]
    set q [java::new {ptolemy.data.BooleanMatrixToken boolean[][]} $b]
    catch {set res1 [$p add $q]} msg1
    catch {set res2 [$p addReverse $q]} msg2
    catch {set res3 [$q add $p]} msg3
    catch {set res4 [$q addReverse $p]} msg4

    list $msg1 $msg2 $msg3 $msg4
} {{ptolemy.kernel.util.IllegalActionException: add method not supported between ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' and ptolemy.data.BooleanMatrixToken '[true, false; true, false]' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: addReverse method not supported between ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' and ptolemy.data.BooleanMatrixToken '[true, false; true, false]' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: add method not supported between ptolemy.data.BooleanMatrixToken '[true, false; true, false]' and ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: addReverse method not supported between ptolemy.data.BooleanMatrixToken '[true, false; true, false]' and ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' because the types are incomparable.}}

test DoubleMatrixToken-3.3 {Test adding DoubleMatrixToken to ComplexMatrixToken.} {
    set c1 [java::new {ptolemy.math.Complex double double} 2.0 0.0]
    set c2 [java::new {ptolemy.math.Complex double double} 1.0 0.0]
    set c3 [java::new {ptolemy.math.Complex double double} 3.0 0.0]
    set c4 [java::new {ptolemy.math.Complex double double} 1.0 0.0]
    set a [java::new {ptolemy.math.Complex[][]} {2 2}]
    $a set {0 0} $c1
    $a set {0 1} $c2
    $a set {1 0} $c3
    $a set {1 1} $c4
    set q [java::new {ptolemy.data.ComplexMatrixToken ptolemy.math.Complex[][]} $a]
    set res1 [$p add $q]
    set res2 [$p addReverse $q]
    set res3 [$q add $p]
    set res4 [$q addReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[7.0 + 0.0i, 5.0 + 0.0i; 6.0 + 0.0i, 3.0 + 0.0i]} {[7.0 + 0.0i, 5.0 + 0.0i; 6.0 + 0.0i, 3.0 + 0.0i]} {[7.0 + 0.0i, 5.0 + 0.0i; 6.0 + 0.0i, 3.0 + 0.0i]} {[7.0 + 0.0i, 5.0 + 0.0i; 6.0 + 0.0i, 3.0 + 0.0i]}}

test DoubleMatrixToken-3.4 {Test adding DoubleMatrixToken to IntToken.} {
    set r [java::new {ptolemy.data.IntToken int} 2]
    set res1 [$p add $r]
    set res2 [$p addReverse $r]
    set res3 [$r add $p]
    set res4 [$r addReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[7.0, 6.0; 5.0, 4.0]} {[7.0, 6.0; 5.0, 4.0]} {[7.0, 6.0; 5.0, 4.0]} {[7.0, 6.0; 5.0, 4.0]}}

test DoubleMatrixToken-3.5 {Test adding DoubleMatrixToken to DoubleToken.} {
    set r [java::new {ptolemy.data.DoubleToken double} 2.0]
    set res1 [$p add $r]
    set res2 [$p addReverse $r]
    set res3 [$r add $p]
    set res4 [$r addReverse $p]
 
    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[7.0, 6.0; 5.0, 4.0]} {[7.0, 6.0; 5.0, 4.0]} {[7.0, 6.0; 5.0, 4.0]} {[7.0, 6.0; 5.0, 4.0]}}

test DoubleMatrixToken-3.6 {Test adding DoubleMatrixToken to BooleanToken.} {
    set r [java::new {ptolemy.data.BooleanToken boolean} true]
    catch {set res1 [$p add $r]} msg1
    catch {set res2 [$p addReverse $r]} msg2
    catch {set res3 [$r add $p]} msg3
    catch {set res4 [$r addReverse $p]} msg4

    list $msg1 $msg2 $msg3 $msg4
} {{ptolemy.kernel.util.IllegalActionException: add method not supported between ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' and ptolemy.data.BooleanToken 'true' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: addReverse method not supported between ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' and ptolemy.data.BooleanToken 'true' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: add method not supported between ptolemy.data.BooleanToken 'true' and ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: addReverse method not supported between ptolemy.data.BooleanToken 'true' and ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' because the types are incomparable.}}

test DoubleMatrixToken-3.7 {Test adding DoubleMatrixToken to ComplexToken.} {
    set c1 [java::new {ptolemy.math.Complex double double} 2.0 0.0]
    set r [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c1]
    set res1 [$p add $r]
    set res2 [$p addReverse $r]
    set res3 [$r add $p]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {{[7.0 + 0.0i, 6.0 + 0.0i; 5.0 + 0.0i, 4.0 + 0.0i]} {[7.0 + 0.0i, 6.0 + 0.0i; 5.0 + 0.0i, 4.0 + 0.0i]} {[7.0 + 0.0i, 6.0 + 0.0i; 5.0 + 0.0i, 4.0 + 0.0i]}}

######################################################################
####
# Test division of ints with Token types below it in the lossless 
# type hierarchy, and with other doubles. Note that dividing doubles could 
# give a double.
test DoubleMatrixToken-4.0 {Test dividing doubles.} {
    set b [java::new {double[][]} {2 2} {{2.0 1.0} {3.0 1.0}}]
    set q [java::new {ptolemy.data.DoubleMatrixToken double[][]} $b]
    catch {[set res1 [$p divide $q]]} e1

    list $e1
} {{ptolemy.kernel.util.IllegalActionException: divide operation not supported between ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' and ptolemy.data.DoubleMatrixToken '[2.0, 1.0; 3.0, 1.0]'}}

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
    set oldEpsilon [java::field ptolemy.math.Complex EPSILON]
    java::field ptolemy.math.Complex EPSILON $epsilon

    set bClose [java::new {double[][]} {2 2} \
	    [list [list [expr {2.0 + (0.5 * $epsilon) } ] 1] \
	    [list  3 [expr {1.0 - (0.5 * $epsilon) } ] ] ] ]

    set q2Close [java::new {ptolemy.data.DoubleMatrixToken double[][]} \
	    $bClose]
    set res1 [$q2 isCloseTo $q2Close]
    set res2 [$q2Close isCloseTo $q]
    set res3 [$q2Close isCloseTo $p]

    java::field ptolemy.math.Complex EPSILON $oldEpsilon

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {true true false}

######################################################################
####
# Test modulo operator between doubles and doubles.
test DoubleMatrixToken-6.0 {Test modulo between doubles.} {
    catch {[set res1 [$p modulo $q]]} e1

    list $e1
} {{ptolemy.kernel.util.IllegalActionException: modulo operation not supported between ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' and ptolemy.data.DoubleMatrixToken '[2.0, 1.0; 3.0, 1.0]'}}

######################################################################
####
# Test multiply operator between doubles and doubles.
test DoubleMatrixToken-7.0 {Test multiply operator between IntMatrixTokens} {
    set b [java::new {int[][]} {2 2} {{2 1} {3 1}}]
    set q [java::new {ptolemy.data.IntMatrixToken int[][]} $b]
    set res1 [$p multiply $q]
    set res2 [$p multiplyReverse $q]
    set res3 [$q multiply $p]
    set res4 [$q multiplyReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[22.0, 9.0; 12.0, 5.0]} {[13.0, 10.0; 18.0, 14.0]} {[13.0, 10.0; 18.0, 14.0]} {[22.0, 9.0; 12.0, 5.0]}}

test DoubleMatrixToken-7.0.1 {Test multiply operator between IntMatrixToken and DoubleMatrixToken of different dimensions} {
    set b [java::new {int[][]} {2 3} {{2 1 3} {3 1 6}}]
    set q [java::new {ptolemy.data.IntMatrixToken int[][]} $b]
    set res1 [$p multiply $q]
    catch {set res2 [$p multiplyReverse $q]} msg2
    catch {set res3 [$q multiply $p]} msg3
    set res4 [$q multiplyReverse $p]
  
    list [$res1 toString] $msg2 $msg3 [$res4 toString]
} {{[22.0, 9.0, 39.0; 12.0, 5.0, 21.0]} {ptolemy.kernel.util.IllegalActionException: multiplyReverse operation not supported between ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' and ptolemy.data.IntMatrixToken '[2, 1, 3; 3, 1, 6]'
Because:
multiply operation not supported between ptolemy.data.DoubleMatrixToken '[2.0, 1.0, 3.0; 3.0, 1.0, 6.0]' and ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' because the matrices have incompatible dimensions.} {ptolemy.kernel.util.IllegalActionException: multiplyReverse operation not supported between ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' and ptolemy.data.IntMatrixToken '[2, 1, 3; 3, 1, 6]'
Because:
multiply operation not supported between ptolemy.data.DoubleMatrixToken '[2.0, 1.0, 3.0; 3.0, 1.0, 6.0]' and ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' because the matrices have incompatible dimensions.} {[22.0, 9.0, 39.0; 12.0, 5.0, 21.0]}}

test DoubleMatrixToken-7.1 {Test multiplying DoubleMatrixToken to DoubleMatrixToken.} {
    set b [java::new {double[][]} {2 2} {{2.0 1.0} {3.0 1.0}}]
    set q [java::new {ptolemy.data.DoubleMatrixToken double[][]} $b]
    set res1 [$p multiply $q]
    set res2 [$p multiplyReverse $q]
    set res3 [$q multiply $p]
    set res4 [$q multiplyReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[22.0, 9.0; 12.0, 5.0]} {[13.0, 10.0; 18.0, 14.0]} {[13.0, 10.0; 18.0, 14.0]} {[22.0, 9.0; 12.0, 5.0]}}

test DoubleMatrixToken-7.1.1 {Test multiply operator between DoubleMatrixToken and DoubleMatrixToken of different dimensions} {
    set b [java::new {double[][]} {2 3} {{2.0 1.0 3.0} {3.0 1.0 6.0}}]
    set q [java::new {ptolemy.data.DoubleMatrixToken double[][]} $b]
    set res1 [$p multiply $q]
    catch {set res2 [$p multiplyReverse $q]} msg2
    catch {set res3 [$q multiply $p]} msg3
    set res4 [$q multiplyReverse $p]
  
    list [$res1 toString] $msg2 $msg3 [$res4 toString]
} {{[22.0, 9.0, 39.0; 12.0, 5.0, 21.0]} {ptolemy.kernel.util.IllegalActionException: multiply operation not supported between ptolemy.data.DoubleMatrixToken '[2.0, 1.0, 3.0; 3.0, 1.0, 6.0]' and ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' because the matrices have incompatible dimensions.} {ptolemy.kernel.util.IllegalActionException: multiply operation not supported between ptolemy.data.DoubleMatrixToken '[2.0, 1.0, 3.0; 3.0, 1.0, 6.0]' and ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' because the matrices have incompatible dimensions.} {[22.0, 9.0, 39.0; 12.0, 5.0, 21.0]}}

test DoubleMatrixToken-7.2 {Test multiplying DoubleMatrixToken to BooleanMatrixToken.} {
    set b [java::new {boolean[][]} {2 2} {{true false} {true false}}]
    set q [java::new {ptolemy.data.BooleanMatrixToken boolean[][]} $b]
    catch {set res1 [$p multiply $q]} msg1
    catch {set res2 [$p multiplyReverse $q]} msg2
    catch {set res3 [$q multiply $p]} msg3
    catch {set res4 [$q multiplyReverse $p]} msg4

    list "$msg1\n$msg2\n$msg3\n$msg4"
} {{ptolemy.kernel.util.IllegalActionException: multiply method not supported between ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' and ptolemy.data.BooleanMatrixToken '[true, false; true, false]' because the types are incomparable.
ptolemy.kernel.util.IllegalActionException: multiplyReverse method not supported between ptolemy.data.BooleanMatrixToken '[true, false; true, false]' and ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' because the types are incomparable.
ptolemy.kernel.util.IllegalActionException: multiply method not supported between ptolemy.data.BooleanMatrixToken '[true, false; true, false]' and ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' because the types are incomparable.
ptolemy.kernel.util.IllegalActionException: multiplyReverse method not supported between ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' and ptolemy.data.BooleanMatrixToken '[true, false; true, false]' because the types are incomparable.}}

test DoubleMatrixToken-7.2.1 {Test multiply operator between DoubleMatrixToken and BooleanMatrixToken of different dimensions} {
    set b [java::new {boolean[][]} {2 3} {{true false true} {false true false}}]
    set q [java::new {ptolemy.data.BooleanMatrixToken boolean[][]} $b]
    catch {set res1 [$p multiply $q]} msg1
    catch {set res2 [$p multiplyReverse $q]} msg2
    catch {set res3 [$q multiply $p]} msg3
    catch {set res4 [$q multiplyReverse $p]} msg4

    list "$msg1\n$msg2\n$msg3\n$msg4"
} {{ptolemy.kernel.util.IllegalActionException: multiply method not supported between ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' and ptolemy.data.BooleanMatrixToken '[true, false, true; false, true, false]' because the types are incomparable.
ptolemy.kernel.util.IllegalActionException: multiplyReverse method not supported between ptolemy.data.BooleanMatrixToken '[true, false, true; false, true, false]' and ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' because the types are incomparable.
ptolemy.kernel.util.IllegalActionException: multiply method not supported between ptolemy.data.BooleanMatrixToken '[true, false, true; false, true, false]' and ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' because the types are incomparable.
ptolemy.kernel.util.IllegalActionException: multiplyReverse method not supported between ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' and ptolemy.data.BooleanMatrixToken '[true, false, true; false, true, false]' because the types are incomparable.}}

test DoubleMatrixToken-7.3 {Test multiplying DoubleMatrixToken to ComplexMatrixToken.} {
    set c1 [java::new {ptolemy.math.Complex double double} 2.0 0.0]
    set c2 [java::new {ptolemy.math.Complex double double} 1.0 0.0]
    set c3 [java::new {ptolemy.math.Complex double double} 3.0 0.0]
    set c4 [java::new {ptolemy.math.Complex double double} 1.0 0.0]
    set a [java::new {ptolemy.math.Complex[][]} {2 2}]
    $a set {0 0} $c1
    $a set {0 1} $c2
    $a set {1 0} $c3
    $a set {1 1} $c4
    set q [java::new {ptolemy.data.ComplexMatrixToken ptolemy.math.Complex[][]} $a]
    set res1 [$p multiply $q]
    set res2 [$p multiplyReverse $q]
    set res3 [$q multiply $p]
    set res4 [$q multiplyReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[22.0 + 0.0i, 9.0 + 0.0i; 12.0 + 0.0i, 5.0 + 0.0i]} {[13.0 + 0.0i, 10.0 + 0.0i; 18.0 + 0.0i, 14.0 + 0.0i]} {[13.0 + 0.0i, 10.0 + 0.0i; 18.0 + 0.0i, 14.0 + 0.0i]} {[22.0 + 0.0i, 9.0 + 0.0i; 12.0 + 0.0i, 5.0 + 0.0i]}}

test DoubleMatrixToken-7.3.1 {Test multiply operator between DoubleMatrixToken and ComplexMatrixToken of different dimensions} {
    set c1 [java::new {ptolemy.math.Complex double double} 2.0 0.0]
    set c2 [java::new {ptolemy.math.Complex double double} 1.0 0.0]
    set c3 [java::new {ptolemy.math.Complex double double} 3.0 0.0]
    set c4 [java::new {ptolemy.math.Complex double double} 3.0 0.0]
    set c5 [java::new {ptolemy.math.Complex double double} 1.0 0.0]
    set c6 [java::new {ptolemy.math.Complex double double} 6.0 0.0]
    set a [java::new {ptolemy.math.Complex[][]} {2 3}]
    $a set {0 0} $c1
    $a set {0 1} $c2
    $a set {0 2} $c3
    $a set {1 0} $c4
    $a set {1 1} $c5
    $a set {1 2} $c6
    set q [java::new {ptolemy.data.ComplexMatrixToken ptolemy.math.Complex[][]} $a]
    set res1 [$p multiply $q]
    catch {set res2 [$p multiplyReverse $q]} msg2
    catch {set res3 [$q multiply $p]} msg3
    set res4 [$q multiplyReverse $p]
  
    list [$res1 toString] $msg2 $msg3 [$res4 toString]
} {{[22.0 + 0.0i, 9.0 + 0.0i, 39.0 + 0.0i; 12.0 + 0.0i, 5.0 + 0.0i, 21.0 + 0.0i]} {ptolemy.kernel.util.IllegalActionException: multiply operation not supported between ptolemy.data.ComplexMatrixToken '[2.0 + 0.0i, 1.0 + 0.0i, 3.0 + 0.0i; 3.0 + 0.0i, 1.0 + 0.0i, 6.0 + 0.0i]' and ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]'
Because:
multiply operation not supported between ptolemy.data.ComplexMatrixToken '[2.0 + 0.0i, 1.0 + 0.0i, 3.0 + 0.0i; 3.0 + 0.0i, 1.0 + 0.0i, 6.0 + 0.0i]' and ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' because the matrices have incompatible dimensions.} {ptolemy.kernel.util.IllegalActionException: multiply operation not supported between ptolemy.data.ComplexMatrixToken '[2.0 + 0.0i, 1.0 + 0.0i, 3.0 + 0.0i; 3.0 + 0.0i, 1.0 + 0.0i, 6.0 + 0.0i]' and ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]'
Because:
multiply operation not supported between ptolemy.data.ComplexMatrixToken '[2.0 + 0.0i, 1.0 + 0.0i, 3.0 + 0.0i; 3.0 + 0.0i, 1.0 + 0.0i, 6.0 + 0.0i]' and ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' because the matrices have incompatible dimensions.} {[22.0 + 0.0i, 9.0 + 0.0i, 39.0 + 0.0i; 12.0 + 0.0i, 5.0 + 0.0i, 21.0 + 0.0i]}}

test DoubleMatrixToken-7.4 {Test multiplying DoubleMatrixToken to IntToken.} {
    set r [java::new {ptolemy.data.IntToken int} 2]
    set res1 [$p multiply $r]
    set res2 [$p multiplyReverse $r]
    set res3 [$r multiply $p]
    set res4 [$r multiplyReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[10.0, 8.0; 6.0, 4.0]} {[10.0, 8.0; 6.0, 4.0]} {[10.0, 8.0; 6.0, 4.0]} {[10.0, 8.0; 6.0, 4.0]}}

test DoubleMatrixToken-7.5 {Test multiplying DoubleMatrixToken to DoubleToken.} {
    set r [java::new {ptolemy.data.DoubleToken double} 2.0]
    set res1 [$p multiply $r]
    set res2 [$p multiplyReverse $r]
    set res3 [$r multiply $p]
    set res4 [$r multiplyReverse $p]
 
    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[10.0, 8.0; 6.0, 4.0]} {[10.0, 8.0; 6.0, 4.0]} {[10.0, 8.0; 6.0, 4.0]} {[10.0, 8.0; 6.0, 4.0]}}

test DoubleMatrixToken-7.6 {Test multiplying DoubleMatrixToken to BooleanToken.} {
    set r [java::new {ptolemy.data.BooleanToken boolean} true]
    catch {set res1 [$p multiply $r]} msg1
    catch {set res2 [$p multiplyReverse $r]} msg2
    catch {set res3 [$r multiply $p]} msg3
    catch {set res4 [$r multiplyReverse $p]} msg4

    list "$msg1\n$msg2\n$msg3\n$msg4"
} {{ptolemy.kernel.util.IllegalActionException: multiply method not supported between ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' and ptolemy.data.BooleanToken 'true' because the types are incomparable.
ptolemy.kernel.util.IllegalActionException: multiplyReverse method not supported between ptolemy.data.BooleanToken 'true' and ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' because the types are incomparable.
ptolemy.kernel.util.IllegalActionException: multiply method not supported between ptolemy.data.BooleanToken 'true' and ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' because the types are incomparable.
ptolemy.kernel.util.IllegalActionException: multiplyReverse method not supported between ptolemy.data.BooleanToken 'true' and ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' because the types are incomparable.}}

test DoubleMatrixToken-7.7 {Test multiplying DoubleMatrixToken to ComplexToken.} {
    set c1 [java::new {ptolemy.math.Complex double double} 2.0 0.0]
    set r [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c1]
    set res1 [$p multiply $r]
    set res2 [$p multiplyReverse $r]
    set res3 [$r multiply $p]
    set res4 [$r multiplyReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[10.0 + 0.0i, 8.0 + 0.0i; 6.0 + 0.0i, 4.0 + 0.0i]} {[10.0 + 0.0i, 8.0 + 0.0i; 6.0 + 0.0i, 4.0 + 0.0i]} {[10.0 + 0.0i, 8.0 + 0.0i; 6.0 + 0.0i, 4.0 + 0.0i]} {[10.0 + 0.0i, 8.0 + 0.0i; 6.0 + 0.0i, 4.0 + 0.0i]}}

######################################################################
####
# Test subtract operator between doubles and doubles.

test DoubleMatrixToken-8.0 {Test subtract operator between  DoubleMatrixToken to IntMatrixToken.} {
    set b [java::new {int[][]} {2 2} {{2 1} {3 1}}]
    set q [java::new {ptolemy.data.IntMatrixToken int[][]} $b]
    set res1 [$p subtract $q]
    set res2 [$p subtractReverse $q]
    set res3 [$q subtract $p]
    set res4 [$q subtractReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[3.0, 3.0; 0.0, 1.0]} {[-3.0, -3.0; 0.0, -1.0]} {[-3.0, -3.0; 0.0, -1.0]} {[3.0, 3.0; 0.0, 1.0]}}

test DoubleMatrixToken-8.1 {Test subtracting DoubleMatrixToken to DoubleMatrixToken.} {
    set b [java::new {double[][]} {2 2} {{2.0 1.0} {3.0 1.0}}]
    set q [java::new {ptolemy.data.DoubleMatrixToken double[][]} $b]
    set res1 [$p subtract $q]
    set res2 [$p subtractReverse $q]
    set res3 [$q subtract $p]
    set res4 [$q subtractReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[3.0, 3.0; 0.0, 1.0]} {[-3.0, -3.0; 0.0, -1.0]} {[-3.0, -3.0; 0.0, -1.0]} {[3.0, 3.0; 0.0, 1.0]}}

test DoubleMatrixToken-8.2 {Test subtracting DoubleMatrixToken to BooleanMatrixToken.} {
    set b [java::new {boolean[][]} {2 2} {{true false} {true false}}]
    set q [java::new {ptolemy.data.BooleanMatrixToken boolean[][]} $b]
    catch {set res1 [$p subtract $q]} msg1
    catch {set res2 [$p subtractReverse $q]} msg2
    catch {set res3 [$q subtract $p]} msg3
    catch {set res4 [$q subtractReverse $p]} msg4

    list $msg1 $msg2 $msg3 $msg4
} {{ptolemy.kernel.util.IllegalActionException: subtract method not supported between ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' and ptolemy.data.BooleanMatrixToken '[true, false; true, false]' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: subtractReverse method not supported between ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' and ptolemy.data.BooleanMatrixToken '[true, false; true, false]' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: subtract method not supported between ptolemy.data.BooleanMatrixToken '[true, false; true, false]' and ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: subtractReverse method not supported between ptolemy.data.BooleanMatrixToken '[true, false; true, false]' and ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' because the types are incomparable.}}

test DoubleMatrixToken-8.3 {Test subtracting DoubleMatrixToken to ComplexMatrixToken.} {
    set c1 [java::new {ptolemy.math.Complex double double} 2.0 0.0]
    set c2 [java::new {ptolemy.math.Complex double double} 1.0 0.0]
    set c3 [java::new {ptolemy.math.Complex double double} 3.0 0.0]
    set c4 [java::new {ptolemy.math.Complex double double} 1.0 0.0]
    set a [java::new {ptolemy.math.Complex[][]} {2 2}]
    $a set {0 0} $c1
    $a set {0 1} $c2
    $a set {1 0} $c3
    $a set {1 1} $c4
    set q [java::new {ptolemy.data.ComplexMatrixToken ptolemy.math.Complex[][]} $a]
    set res1 [$p subtract $q]
    set res2 [$p subtractReverse $q]
    set res3 [$q subtract $p]
    set res4 [$q subtractReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[3.0 + 0.0i, 3.0 + 0.0i; 0.0 + 0.0i, 1.0 + 0.0i]} {[-3.0 + 0.0i, -3.0 + 0.0i; 0.0 + 0.0i, -1.0 + 0.0i]} {[-3.0 + 0.0i, -3.0 + 0.0i; 0.0 + 0.0i, -1.0 + 0.0i]} {[3.0 + 0.0i, 3.0 + 0.0i; 0.0 + 0.0i, 1.0 + 0.0i]}}

test DoubleMatrixToken-8.4 {Test subtracting DoubleMatrixToken to IntToken.} {
    set r [java::new {ptolemy.data.IntToken int} 2]
    set res1 [$p subtract $r]
    set res2 [$p subtractReverse $r]
    set res3 [$r subtract $p]
    set res4 [$r subtractReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[3.0, 2.0; 1.0, 0.0]} {[-3.0, -2.0; -1.0, -0.0]} {[-3.0, -2.0; -1.0, -0.0]} {[3.0, 2.0; 1.0, 0.0]}}

test DoubleMatrixToken-8.5 {Test subtracting DoubleMatrixToken to DoubleToken.} {
    set r [java::new {ptolemy.data.DoubleToken double} 2.0]
    set res1 [$p subtract $r]
    set res2 [$p subtractReverse $r]
    set res3 [$r subtract $p]
    set res4 [$r subtractReverse $p]
 
    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[3.0, 2.0; 1.0, 0.0]} {[-3.0, -2.0; -1.0, -0.0]} {[-3.0, -2.0; -1.0, -0.0]} {[3.0, 2.0; 1.0, 0.0]}}

test DoubleMatrixToken-8.6 {Test subtracting DoubleMatrixToken to BooleanToken.} {
    set r [java::new {ptolemy.data.BooleanToken boolean} true]
    catch {set res1 [$p subtract $r]} msg1
    catch {set res2 [$p subtractReverse $r]} msg2
    catch {set res3 [$r subtract $p]} msg3
    catch {set res4 [$r subtractReverse $p]} msg4

    list $msg1 $msg2 $msg3 $msg4
} {{ptolemy.kernel.util.IllegalActionException: subtract method not supported between ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' and ptolemy.data.BooleanToken 'true' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: subtractReverse method not supported between ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' and ptolemy.data.BooleanToken 'true' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: subtract method not supported between ptolemy.data.BooleanToken 'true' and ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: subtractReverse method not supported between ptolemy.data.BooleanToken 'true' and ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' because the types are incomparable.}}

test DoubleMatrixToken-8.7 {Test subtracting DoubleMatrixToken to ComplexToken.} {
    set c1 [java::new {ptolemy.math.Complex double double} 2.0 0.0]
    set r [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c1]
    set res1 [$p subtract $r]
    set res2 [$p subtractReverse $r]
    set res3 [$r subtract $p]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {{[3.0 + 0.0i, 2.0 + 0.0i; 1.0 + 0.0i, 0.0 + 0.0i]} {[-3.0 + 0.0i, -2.0 + 0.0i; -1.0 + 0.0i, 0.0 + 0.0i]} {[-3.0 + 0.0i, -2.0 + 0.0i; -1.0 + 0.0i, 0.0 + 0.0i]}}

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

