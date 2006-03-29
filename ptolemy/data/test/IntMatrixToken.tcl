# Tests for the IntMatrixToken class
#
# @Author: Neil Smyth, Yuhong Xiong
#
# @Version $Id$
#
# @Copyright (c) 1997-2006 The Regents of the University of California.
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

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then { 
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# 
#

######################################################################
####
# 
test IntMatrixToken-1.0 {Create an empty instance} {
    set p [java::new ptolemy.data.IntMatrixToken]
    $p toString
} {[0]}

######################################################################
####
# 
test IntMatrixToken-1.1 {Create a non-empty instance from an int} {
    set a [java::new {int[][]} {2 2} {{5 4} {3 2}}]
    set p [java::new {ptolemy.data.IntMatrixToken int[][]} $a]
    $p toString
} {[5, 4; 3, 2]}

######################################################################
####
# 
test IntMatrixToken-1.2 {Create a non-empty instance from an String} {
    set p [java::new {ptolemy.data.IntMatrixToken String} "\[5, 4; 3, 2\]"]
    $p toString
} {[5, 4; 3, 2]}

######################################################################
####
# 
test IntMatrixToken-2.0 {Create a non-empty instance and query its value as an int} {
    set res1 [$p intMatrix]
    list [jdkPrintArray [$res1 get 0]] [jdkPrintArray [$res1 get 1]]
} {{5 4} {3 2}}

######################################################################
####
# 
test IntMatrixToken-2.1 {Create a non-empty instance and query its value as a double} {
    set res1 [$p doubleMatrix]
    list [jdkPrintArray [$res1 get 0]] [jdkPrintArray [$res1 get 1]]
} {{5.0 4.0} {3.0 2.0}}

######################################################################
####
# 
test IntMatrixToken-2.2 {Create a non-empty instance and query its value as a long} {
    set res1 [$p longMatrix]
   list [jdkPrintArray [$res1 get 0]] [jdkPrintArray [$res1 get 1]]
} {{5 4} {3 2}}

######################################################################
####
# 
test IntMatrixToken-2.3 {Create a non-empty instance and query its value as a string} {
    set res1 [$p toString]
    list $res1
} {{[5, 4; 3, 2]}}

######################################################################
####
#
test IntMatrixToken-2.4 {Create a non-empty instance and query its value as a complex#} {
    set res1 [$p complexMatrix]
    list [jdkPrintArray [$res1 get 0]] [jdkPrintArray [$res1 get 1]]
} {{{5.0 + 0.0i} {4.0 + 0.0i}} {{3.0 + 0.0i} {2.0 + 0.0i}}}

######################################################################
####
# 
test IntMatrixToken-2.5 {Test additive identity} {
    set token [$p zero] 
    list [$token toString]
} {{[0, 0; 0, 0]}}
######################################################################
####
# 
test IntMatrixToken-2.6 {Test multiplicative identity} {
    set token [$p one]
    list [$token toString]
} {{[1, 0; 0, 1]}}

######################################################################
####
# 
test IntMatrixToken-2.7 {Test matrixToArray} {
    set array [java::call ptolemy.data.MatrixToken matrixToArray [$p one]]
    $array toString
} {{1, 0, 0, 1}}

######################################################################
####
# Test addition of ints to Token types below it in the lossless 
# type hierarchy, and with other ints.
test IntMatrixToken-3.0 {Test adding IntMatrixToken to IntMatrixToken.} {
    set b [java::new {int[][]} {2 2} {{2 1} {3 1}}]
    set q [java::new {ptolemy.data.IntMatrixToken int[][]} $b]
    set res1 [$p add $q]
    set res2 [$p addReverse $q]
    set res3 [$q add $p]
    set res4 [$q addReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[7, 5; 6, 3]} {[7, 5; 6, 3]} {[7, 5; 6, 3]} {[7, 5; 6, 3]}}

test IntMatrixToken-3.1 {Test adding IntMatrixToken to DoubleMatrixToken.} {
    set b [java::new {double[][]} {2 2} {{2.0 1.0} {3.0 1.0}}]
    set q [java::new {ptolemy.data.DoubleMatrixToken double[][]} $b]
    set res1 [$p add $q]
    set res2 [$p addReverse $q]
    set res3 [$q add $p]
    set res4 [$q addReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[7.0, 5.0; 6.0, 3.0]} {[7.0, 5.0; 6.0, 3.0]} {[7.0, 5.0; 6.0, 3.0]} {[7.0, 5.0; 6.0, 3.0]}}

test IntMatrixToken-3.2 {Test adding IntMatrixToken to BooleanMatrixToken.} {
    set b [java::new {boolean[][]} {2 2} {{true false} {true false}}]
    set q [java::new {ptolemy.data.BooleanMatrixToken boolean[][]} $b]
    catch {set res1 [$p add $q]} msg1
    catch {set res2 [$p addReverse $q]} msg2
    catch {set res3 [$q add $p]} msg3
    catch {set res4 [$q addReverse $p]} msg4

    list $msg1 $msg2 $msg3 $msg4
} {{ptolemy.kernel.util.IllegalActionException: add method not supported between ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' and ptolemy.data.BooleanMatrixToken '[true, false; true, false]' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: addReverse method not supported between ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' and ptolemy.data.BooleanMatrixToken '[true, false; true, false]' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: add method not supported between ptolemy.data.BooleanMatrixToken '[true, false; true, false]' and ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: addReverse method not supported between ptolemy.data.BooleanMatrixToken '[true, false; true, false]' and ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' because the types are incomparable.}}

test IntMatrixToken-3.3 {Test adding IntMatrixToken to ComplexMatrixToken.} {
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

test IntMatrixToken-3.4 {Test adding IntMatrixToken to IntToken.} {
    set r [java::new {ptolemy.data.IntToken int} 2]
    set res1 [$p add $r]
    set res2 [$p addReverse $r]
    set res3 [$r add $p]
    set res4 [$r addReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[7, 6; 5, 4]} {[7, 6; 5, 4]} {[7, 6; 5, 4]} {[7, 6; 5, 4]}}

test IntMatrixToken-3.5 {Test adding IntMatrixToken to DoubleToken.} {
    set r [java::new {ptolemy.data.DoubleToken double} 2.0]
    set res1 [$p add $r]
    set res2 [$p addReverse $r]
    set res3 [$r add $p]
 
    list [$res1 toString] [$res2 toString] [$res3 toString]
} {{[7.0, 6.0; 5.0, 4.0]} {[7.0, 6.0; 5.0, 4.0]} {[7.0, 6.0; 5.0, 4.0]}}

test IntMatrixToken-3.6 {Test adding IntMatrixToken to BooleanToken.} {
    set r [java::new {ptolemy.data.BooleanToken boolean} true]
    catch {set res1 [$p add $r]} msg1
    catch {set res2 [$p addReverse $r]} msg2
    catch {set res3 [$r add $p]} msg3
    catch {set res4 [$r addReverse $p]} msg4

    list $msg1 $msg2 $msg3 $msg4
} {{ptolemy.kernel.util.IllegalActionException: add method not supported between ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' and ptolemy.data.BooleanToken 'true' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: addReverse method not supported between ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' and ptolemy.data.BooleanToken 'true' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: add method not supported between ptolemy.data.BooleanToken 'true' and ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: addReverse method not supported between ptolemy.data.BooleanToken 'true' and ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' because the types are incomparable.}}

test IntMatrixToken-3.7 {Test adding IntMatrixToken to ComplexToken.} {
    set c1 [java::new {ptolemy.math.Complex double double} 2.0 0.0]
    set r [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c1]
    set res1 [$p add $r]
    set res2 [$p addReverse $r]
    set res3 [$r add $p]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {{[7.0 + 0.0i, 6.0 + 0.0i; 5.0 + 0.0i, 4.0 + 0.0i]} {[7.0 + 0.0i, 6.0 + 0.0i; 5.0 + 0.0i, 4.0 + 0.0i]} {[7.0 + 0.0i, 6.0 + 0.0i; 5.0 + 0.0i, 4.0 + 0.0i]}}

######################################################################
####
test IntMatrixToken-4.0 {Test dividing intMatrices.} {
    set b [java::new {int[][]} {2 2} {{2 1} {3 1}}]
    set q [java::new {ptolemy.data.IntMatrixToken int[][]} $b]
    catch {[set res1 [$p divide $q]]} e1

    list $e1
} {{ptolemy.kernel.util.IllegalActionException: divide operation not supported between ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' and ptolemy.data.IntMatrixToken '[2, 1; 3, 1]'}}

######################################################################
####
# Test equals operator applied to other ints and Tokens types 
# below it in the lossless type hierarchy.
test IntMatrixToken-5.0 {Test equality between ints.} {
    set q2 [java::new {ptolemy.data.IntMatrixToken int[][]} $b]
    set res1 [$q isEqualTo $q2]
    set res2 [$q isEqualTo $p]

    list [$res1 toString] [$res2 toString]
} {true false}

######################################################################
####
# Test modulo operator between ints and ints.
test IntMatrixToken-6.0 {Test modulo between ints.} {
    catch {[set res1 [$p modulo $q]]} e1

    list $e1
} {{ptolemy.kernel.util.IllegalActionException: modulo operation not supported between ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' and ptolemy.data.IntMatrixToken '[2, 1; 3, 1]'}}

######################################################################
####
# Test multiply operator between ints and ints.
test IntMatrixToken-7.0 {Test multiply operator between IntMatrixTokens} {
    set b [java::new {int[][]} {2 2} {{2 1} {3 1}}]
    set q [java::new {ptolemy.data.IntMatrixToken int[][]} $b]
    set res1 [$p multiply $q]
    set res2 [$p multiplyReverse $q]
    set res3 [$q multiply $p]
    set res4 [$q multiplyReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[22, 9; 12, 5]} {[13, 10; 18, 14]} {[13, 10; 18, 14]} {[22, 9; 12, 5]}}

test IntMatrixToken-7.0.1 {Test multiply operator between IntMatrixTokens of different dimensions} {
    set b [java::new {int[][]} {2 3} {{2 1 3} {3 1 6}}]
    set q [java::new {ptolemy.data.IntMatrixToken int[][]} $b]
    set res1 [$p multiply $q]
    catch {set res2 [$p multiplyReverse $q]} msg2
    catch {set res3 [$q multiply $p]} msg3
    set res4 [$q multiplyReverse $p]
  
    list [$res1 toString] $msg2 $msg3 [$res4 toString]
} {{[22, 9, 39; 12, 5, 21]} {ptolemy.kernel.util.IllegalActionException: multiply operation not supported between ptolemy.data.IntMatrixToken '[2, 1, 3; 3, 1, 6]' and ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' because the matrices have incompatible dimensions.} {ptolemy.kernel.util.IllegalActionException: multiply operation not supported between ptolemy.data.IntMatrixToken '[2, 1, 3; 3, 1, 6]' and ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' because the matrices have incompatible dimensions.} {[22, 9, 39; 12, 5, 21]}}

test IntMatrixToken-7.1 {Test multiplying IntMatrixToken to DoubleMatrixToken.} {
    set b [java::new {double[][]} {2 2} {{2.0 1.0} {3.0 1.0}}]
    set q [java::new {ptolemy.data.DoubleMatrixToken double[][]} $b]
    set res1 [$p multiply $q]
    set res2 [$p multiplyReverse $q]
    set res3 [$q multiply $p]
    set res4 [$q multiplyReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[22.0, 9.0; 12.0, 5.0]} {[13.0, 10.0; 18.0, 14.0]} {[13.0, 10.0; 18.0, 14.0]} {[22.0, 9.0; 12.0, 5.0]}}

test IntMatrixToken-7.1.1 {Test multiply operator between IntMatrixToken and DoubleMatrixToken of different dimensions} {
    set b [java::new {double[][]} {2 3} {{2.0 1.0 3.0} {3.0 1.0 6.0}}]
    set q [java::new {ptolemy.data.DoubleMatrixToken double[][]} $b]
    set res1 [$p multiply $q]
    catch {set res2 [$p multiplyReverse $q]} msg2
    catch {set res3 [$q multiply $p]} msg3
    set res4 [$q multiplyReverse $p]
  
    list [$res1 toString] $msg2 $msg3 [$res4 toString]
} {{[22.0, 9.0, 39.0; 12.0, 5.0, 21.0]} {ptolemy.kernel.util.IllegalActionException: multiply operation not supported between ptolemy.data.DoubleMatrixToken '[2.0, 1.0, 3.0; 3.0, 1.0, 6.0]' and ptolemy.data.IntMatrixToken '[5, 4; 3, 2]'
Because:
multiply operation not supported between ptolemy.data.DoubleMatrixToken '[2.0, 1.0, 3.0; 3.0, 1.0, 6.0]' and ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' because the matrices have incompatible dimensions.} {ptolemy.kernel.util.IllegalActionException: multiply operation not supported between ptolemy.data.DoubleMatrixToken '[2.0, 1.0, 3.0; 3.0, 1.0, 6.0]' and ptolemy.data.IntMatrixToken '[5, 4; 3, 2]'
Because:
multiply operation not supported between ptolemy.data.DoubleMatrixToken '[2.0, 1.0, 3.0; 3.0, 1.0, 6.0]' and ptolemy.data.DoubleMatrixToken '[5.0, 4.0; 3.0, 2.0]' because the matrices have incompatible dimensions.} {[22.0, 9.0, 39.0; 12.0, 5.0, 21.0]}}

test IntMatrixToken-7.2 {Test multiplying IntMatrixToken to BooleanMatrixToken.} {
    set b [java::new {boolean[][]} {2 2} {{true false} {true false}}]
    set q [java::new {ptolemy.data.BooleanMatrixToken boolean[][]} $b]
    catch {set res1 [$p multiply $q]} msg1
    catch {set res2 [$p multiplyReverse $q]} msg2
    catch {set res3 [$q multiply $p]} msg3
    catch {set res4 [$q multiplyReverse $p]} msg4

    list "$msg1\n$msg2\n$msg3\n$msg4"
} {{ptolemy.kernel.util.IllegalActionException: multiply method not supported between ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' and ptolemy.data.BooleanMatrixToken '[true, false; true, false]' because the types are incomparable.
ptolemy.kernel.util.IllegalActionException: multiplyReverse method not supported between ptolemy.data.BooleanMatrixToken '[true, false; true, false]' and ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' because the types are incomparable.
ptolemy.kernel.util.IllegalActionException: multiply method not supported between ptolemy.data.BooleanMatrixToken '[true, false; true, false]' and ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' because the types are incomparable.
ptolemy.kernel.util.IllegalActionException: multiplyReverse method not supported between ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' and ptolemy.data.BooleanMatrixToken '[true, false; true, false]' because the types are incomparable.}}

test IntMatrixToken-7.2.1 {Test multiply operator between IntMatrixToken and BooleanMatrixToken of different dimensions} {
    set b [java::new {boolean[][]} {2 3} {{true false true} {false true false}}]
    set q [java::new {ptolemy.data.BooleanMatrixToken boolean[][]} $b]
    catch {set res1 [$p multiply $q]} msg1
    catch {set res2 [$p multiplyReverse $q]} msg2
    catch {set res3 [$q multiply $p]} msg3
    catch {set res4 [$q multiplyReverse $p]} msg4

    list "$msg1\n$msg2\n$msg3\n$msg4"
} {{ptolemy.kernel.util.IllegalActionException: multiply method not supported between ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' and ptolemy.data.BooleanMatrixToken '[true, false, true; false, true, false]' because the types are incomparable.
ptolemy.kernel.util.IllegalActionException: multiplyReverse method not supported between ptolemy.data.BooleanMatrixToken '[true, false, true; false, true, false]' and ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' because the types are incomparable.
ptolemy.kernel.util.IllegalActionException: multiply method not supported between ptolemy.data.BooleanMatrixToken '[true, false, true; false, true, false]' and ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' because the types are incomparable.
ptolemy.kernel.util.IllegalActionException: multiplyReverse method not supported between ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' and ptolemy.data.BooleanMatrixToken '[true, false, true; false, true, false]' because the types are incomparable.}}

test IntMatrixToken-7.3 {Test multiplying IntMatrixToken to ComplexMatrixToken.} {
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

test IntMatrixToken-7.3.1 {Test multiply operator between IntMatrixToken and ComplexMatrixToken of different dimensions} {
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
} {{[22.0 + 0.0i, 9.0 + 0.0i, 39.0 + 0.0i; 12.0 + 0.0i, 5.0 + 0.0i, 21.0 + 0.0i]} {ptolemy.kernel.util.IllegalActionException: multiply operation not supported between ptolemy.data.ComplexMatrixToken '[2.0 + 0.0i, 1.0 + 0.0i, 3.0 + 0.0i; 3.0 + 0.0i, 1.0 + 0.0i, 6.0 + 0.0i]' and ptolemy.data.IntMatrixToken '[5, 4; 3, 2]'
Because:
multiply operation not supported between ptolemy.data.ComplexMatrixToken '[2.0 + 0.0i, 1.0 + 0.0i, 3.0 + 0.0i; 3.0 + 0.0i, 1.0 + 0.0i, 6.0 + 0.0i]' and ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' because the matrices have incompatible dimensions.} {ptolemy.kernel.util.IllegalActionException: multiply operation not supported between ptolemy.data.ComplexMatrixToken '[2.0 + 0.0i, 1.0 + 0.0i, 3.0 + 0.0i; 3.0 + 0.0i, 1.0 + 0.0i, 6.0 + 0.0i]' and ptolemy.data.IntMatrixToken '[5, 4; 3, 2]'
Because:
multiply operation not supported between ptolemy.data.ComplexMatrixToken '[2.0 + 0.0i, 1.0 + 0.0i, 3.0 + 0.0i; 3.0 + 0.0i, 1.0 + 0.0i, 6.0 + 0.0i]' and ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' because the matrices have incompatible dimensions.} {[22.0 + 0.0i, 9.0 + 0.0i, 39.0 + 0.0i; 12.0 + 0.0i, 5.0 + 0.0i, 21.0 + 0.0i]}}

test IntMatrixToken-7.4 {Test multiplying IntMatrixToken to IntToken.} {
    set r [java::new {ptolemy.data.IntToken int} 2]
    set res1 [$p multiply $r]
    set res2 [$p multiplyReverse $r]
    set res3 [$r multiply $p]
    set res4 [$r multiplyReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[10, 8; 6, 4]} {[10, 8; 6, 4]} {[10, 8; 6, 4]} {[10, 8; 6, 4]}}

test IntMatrixToken-7.5 {Test multiplying IntMatrixToken to DoubleToken.} {
    set r [java::new {ptolemy.data.DoubleToken double} 2.0]
    set res1 [$p multiply $r]
    set res2 [$p multiplyReverse $r]
    set res3 [$r multiply $p]
    set res4 [$r multiplyReverse $p]
 
    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[10.0, 8.0; 6.0, 4.0]} {[10.0, 8.0; 6.0, 4.0]} {[10.0, 8.0; 6.0, 4.0]} {[10.0, 8.0; 6.0, 4.0]}}

test IntMatrixToken-7.6 {Test multiplying IntMatrixToken to BooleanToken.} {
    set r [java::new {ptolemy.data.BooleanToken boolean} true]
    catch {set res1 [$p multiply $r]} msg1
    catch {set res2 [$p multiplyReverse $r]} msg2
    catch {set res3 [$r multiply $p]} msg3
    catch {set res4 [$r multiplyReverse $p]} msg4

    list "$msg1\n$msg2\n$msg3\n$msg4"
} {{ptolemy.kernel.util.IllegalActionException: multiply method not supported between ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' and ptolemy.data.BooleanToken 'true' because the types are incomparable.
ptolemy.kernel.util.IllegalActionException: multiplyReverse method not supported between ptolemy.data.BooleanToken 'true' and ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' because the types are incomparable.
ptolemy.kernel.util.IllegalActionException: multiply method not supported between ptolemy.data.BooleanToken 'true' and ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' because the types are incomparable.
ptolemy.kernel.util.IllegalActionException: multiplyReverse method not supported between ptolemy.data.BooleanToken 'true' and ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' because the types are incomparable.}}

test IntMatrixToken-7.7 {Test multiplying IntMatrixToken to ComplexToken.} {
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
# Test multiply operator between ints and ints.
test IntMatrixToken-8.0 {Test subtract operator between ints.} {
    set b [java::new {int[][]} {2 2} {{2 1} {3 1}}]
    set q [java::new {ptolemy.data.IntMatrixToken int[][]} $b]
    set res1 [$p subtract $q]
    set res2 [$p subtractReverse $q]
    set res3 [$q subtract $p]
    set res4 [$q subtractReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[3, 3; 0, 1]} {[-3, -3; 0, -1]} {[-3, -3; 0, -1]} {[3, 3; 0, 1]}}

test IntMatrixToken-8.1 {Test subtracting IntMatrixToken to DoubleMatrixToken.} {
    set b [java::new {double[][]} {2 2} {{2.0 1.0} {3.0 1.0}}]
    set q [java::new {ptolemy.data.DoubleMatrixToken double[][]} $b]
    set res1 [$p subtract $q]
    set res2 [$p subtractReverse $q]
    set res3 [$q subtract $p]
    set res4 [$q subtractReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[3.0, 3.0; 0.0, 1.0]} {[-3.0, -3.0; 0.0, -1.0]} {[-3.0, -3.0; 0.0, -1.0]} {[3.0, 3.0; 0.0, 1.0]}}

test IntMatrixToken-8.2 {Test subtracting IntMatrixToken to BooleanMatrixToken.} {
    set b [java::new {boolean[][]} {2 2} {{true false} {true false}}]
    set q [java::new {ptolemy.data.BooleanMatrixToken boolean[][]} $b]
    catch {set res1 [$p subtract $q]} msg1
    catch {set res2 [$p subtractReverse $q]} msg2
    catch {set res3 [$q subtract $p]} msg3
    catch {set res4 [$q subtractReverse $p]} msg4

    list $msg1 $msg2 $msg3 $msg4
} {{ptolemy.kernel.util.IllegalActionException: subtract method not supported between ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' and ptolemy.data.BooleanMatrixToken '[true, false; true, false]' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: subtractReverse method not supported between ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' and ptolemy.data.BooleanMatrixToken '[true, false; true, false]' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: subtract method not supported between ptolemy.data.BooleanMatrixToken '[true, false; true, false]' and ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: subtractReverse method not supported between ptolemy.data.BooleanMatrixToken '[true, false; true, false]' and ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' because the types are incomparable.}}

test IntMatrixToken-8.3 {Test subtracting IntMatrixToken to ComplexMatrixToken.} {
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

test IntMatrixToken-8.4 {Test subtracting IntMatrixToken to IntToken.} {
    set r [java::new {ptolemy.data.IntToken int} 2]
    set res1 [$p subtract $r]
    set res2 [$p subtractReverse $r]
    set res3 [$r subtract $p]
    set res4 [$r subtractReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[3, 2; 1, 0]} {[-3, -2; -1, 0]} {[-3, -2; -1, 0]} {[3, 2; 1, 0]}}

test IntMatrixToken-8.5 {Test subtracting IntMatrixToken to DoubleToken.} {
    set r [java::new {ptolemy.data.DoubleToken double} 2.0]
    set res1 [$p subtract $r]
    set res2 [$p subtractReverse $r]
    set res3 [$r subtract $p]
 
    list [$res1 toString] [$res2 toString] [$res3 toString]
} {{[3.0, 2.0; 1.0, 0.0]} {[-3.0, -2.0; -1.0, -0.0]} {[-3.0, -2.0; -1.0, -0.0]}}

test IntMatrixToken-8.6 {Test subtracting IntMatrixToken to BooleanToken.} {
    set r [java::new {ptolemy.data.BooleanToken boolean} true]
    catch {set res1 [$p subtract $r]} msg1
    catch {set res2 [$p subtractReverse $r]} msg2
    catch {set res3 [$r subtract $p]} msg3
    catch {set res4 [$r subtractReverse $p]} msg4

    list $msg1 $msg2 $msg3 $msg4
} {{ptolemy.kernel.util.IllegalActionException: subtract method not supported between ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' and ptolemy.data.BooleanToken 'true' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: subtractReverse method not supported between ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' and ptolemy.data.BooleanToken 'true' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: subtract method not supported between ptolemy.data.BooleanToken 'true' and ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: subtractReverse method not supported between ptolemy.data.BooleanToken 'true' and ptolemy.data.IntMatrixToken '[5, 4; 3, 2]' because the types are incomparable.}}

test IntMatrixToken-8.7 {Test subtracting IntMatrixToken to ComplexToken.} {
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
test IntMatrixToken-9.0 {Test equals} {
    set p1 [java::new {ptolemy.data.IntMatrixToken String} "\[1, 2; 3, 4\]"]
    set p2 [java::new {ptolemy.data.IntMatrixToken String} "\[1, 2; 3, 4\]"]
    set p3 [java::new {ptolemy.data.IntMatrixToken String} "\[9, 8; 7, 6\]"]
    list [$p1 equals $p1] [$p1 equals $p2] [$p1 equals $p3]
} {1 1 0}

######################################################################
####
# 
test IntMatrixToken-10.0 {Test hashCode} {
    set p1 [java::new {ptolemy.data.IntMatrixToken String} "\[1, 2; 3, 4\]"]
    set p2 [java::new {ptolemy.data.IntMatrixToken String} "\[1, 2; 3, 4\]"]
    set p3 [java::new {ptolemy.data.IntMatrixToken String} "\[9, 8; 7, 6\]"]
    list [$p1 hashCode] [$p2 hashCode] [$p3 hashCode]
} {10 10 30}

