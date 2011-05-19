# Tests for the ComplexMatrixToken class
#
# @Author: Neil Smyth
#
# @Version $Id$
#
# @Copyright (c) 1997-2008 The Regents of the University of California.
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
test ComplexMatrixToken-1.0 {Create an empty instance} {
    set p [java::new ptolemy.data.ComplexMatrixToken]
    $p toString
} {[0.0 + 0.0i]}

######################################################################
####
# 
test ComplexMatrixToken-1.1 {Create a non-empty instance from an Complex} {
    set c1 [java::new {ptolemy.math.Complex double double} 5.0 0.0]
    set c2 [java::new {ptolemy.math.Complex double double} 4.0 0.0]
    set c3 [java::new {ptolemy.math.Complex double double} 3.0 0.0]
    set c4 [java::new {ptolemy.math.Complex double double} 2.0 0.0]
    
    set a [java::new {ptolemy.math.Complex[][]} {2 2}]
    $a set {0 0} $c1
    $a set {0 1} $c2
    $a set {1 0} $c3
    $a set {1 1} $c4

    set p [java::new {ptolemy.data.ComplexMatrixToken ptolemy.math.Complex[][]} $a]
    $p toString
} {[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]}

######################################################################
####
# 
test ComplexMatrixToken-1.2 {Create a non-empty instance from an String} {
    set p [java::new {ptolemy.data.ComplexMatrixToken String} "\[5.0+0.0i, 4.0+0.0i; 3.0+0.0i, 2.0+0.0i\]"]
    $p toString
} {[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]}

######################################################################
####
# 
test ComplexMatrixToken-2.0 {Create a non-empty instance and query its value as an Complex} {
    set res1 [$p complexMatrix]
    list [jdkPrintArray [$res1 get 0]] [jdkPrintArray [$res1 get 1]]
} {{{5.0 + 0.0i} {4.0 + 0.0i}} {{3.0 + 0.0i} {2.0 + 0.0i}}}

######################################################################
####
# 
test ComplexMatrixToken-2.1 {Create a non-empty instance and query its value as a double} {
    catch {$p doubleMatrix} result
    list $result
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' to the type double matrix.}}

######################################################################
####
# 
test ComplexMatrixToken-2.2 {Create a non-empty instance and query its value as a long} {
    catch {$p longMatrix} result
    list $result
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' to the type long matrix.}}

######################################################################
####
# 
test ComplexMatrixToken-2.3 {Create a non-empty instance and query its value as a string} {
    set res1 [$p toString]
    list $res1
} {{[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]}}

######################################################################
####
#
test ComplexMatrixToken-2.4 {Create a non-empty instance and query its value as a complex} {
    set res1 [$p complexMatrix]
    list [jdkPrintArray [$res1 get 0]] [jdkPrintArray [$res1 get 1]]
} {{{5.0 + 0.0i} {4.0 + 0.0i}} {{3.0 + 0.0i} {2.0 + 0.0i}}}


######################################################################
####
# 
test ComplexMatrixToken-2.5 {Test additive identity} {
    set token [$p zero] 
    list [$token toString]
} {{[0.0 + 0.0i, 0.0 + 0.0i; 0.0 + 0.0i, 0.0 + 0.0i]}}


######################################################################
####
# 
test ComplexMatrixToken-2.5.1 {Test oneRight} {
    set token [$p oneRight] 
    list [$token toString]
} {{[1.0 + 0.0i, 0.0 + 0.0i; 0.0 + 0.0i, 1.0 + 0.0i]}}

######################################################################
####
# 
test ComplexMatrixToken-2.6 {Test multiplicative identity} {
    set token [$p one]
    list [$token toString]
} {{[1.0 + 0.0i, 0.0 + 0.0i; 0.0 + 0.0i, 1.0 + 0.0i]}}

######################################################################
####
# 
test ComplexMatrixToken-2.7 {Test matrixToArray} {
    set array [java::call ptolemy.data.MatrixToken matrixToArray [java::cast ptolemy.data.MatrixToken [$p one]]]
    $array toString
} {{1.0 + 0.0i, 0.0 + 0.0i, 0.0 + 0.0i, 1.0 + 0.0i}}

######################################################################
####
# Test addition of Complexs to Token types below it in the lossless 
# type hierarchy, and with other Complexs.
test ComplexMatrixToken-3.0 {Test adding ComplexMatrixToken to IntMatrixToken.} {
    set b [java::new {int[][]} {2 2} {{2 1} {3 1}}]
    set q [java::new {ptolemy.data.IntMatrixToken int[][]} $b]
    set res1 [$p add $q]
    set res2 [$p addReverse $q]
    set res3 [$q add $p]
    set res4 [$q addReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[7.0 + 0.0i, 5.0 + 0.0i; 6.0 + 0.0i, 3.0 + 0.0i]} {[7.0 + 0.0i, 5.0 + 0.0i; 6.0 + 0.0i, 3.0 + 0.0i]} {[7.0 + 0.0i, 5.0 + 0.0i; 6.0 + 0.0i, 3.0 + 0.0i]} {[7.0 + 0.0i, 5.0 + 0.0i; 6.0 + 0.0i, 3.0 + 0.0i]}}

test ComplexMatrixToken-3.1 {Test adding ComplexMatrixToken to DoubleMatrixToken.} {
    set b [java::new {double[][]} {2 2} {{2.0 1.0} {3.0 1.0}}]
    set q [java::new {ptolemy.data.DoubleMatrixToken double[][]} $b]
    set res1 [$p add $q]
    set res2 [$p addReverse $q]
    set res3 [$q add $p]
    set res4 [$q addReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[7.0 + 0.0i, 5.0 + 0.0i; 6.0 + 0.0i, 3.0 + 0.0i]} {[7.0 + 0.0i, 5.0 + 0.0i; 6.0 + 0.0i, 3.0 + 0.0i]} {[7.0 + 0.0i, 5.0 + 0.0i; 6.0 + 0.0i, 3.0 + 0.0i]} {[7.0 + 0.0i, 5.0 + 0.0i; 6.0 + 0.0i, 3.0 + 0.0i]}}

test ComplexMatrixToken-3.2 {Test adding ComplexMatrixToken to BooleanMatrixToken.} {
    set b [java::new {boolean[][]} {2 2} {{true false} {true false}}]
    set q [java::new {ptolemy.data.BooleanMatrixToken boolean[][]} $b]
    catch {set res1 [$p add $q]} msg1
    catch {set res2 [$p addReverse $q]} msg2
    catch {set res3 [$q add $p]} msg3
    catch {set res4 [$q addReverse $p]} msg4

    list $msg1 $msg2 $msg3 $msg4
} {{ptolemy.kernel.util.IllegalActionException: add method not supported between ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' and ptolemy.data.BooleanMatrixToken '[true, false; true, false]' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: addReverse method not supported between ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' and ptolemy.data.BooleanMatrixToken '[true, false; true, false]' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: add method not supported between ptolemy.data.BooleanMatrixToken '[true, false; true, false]' and ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: addReverse method not supported between ptolemy.data.BooleanMatrixToken '[true, false; true, false]' and ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' because the types are incomparable.}}

test ComplexMatrixToken-3.3 {Test adding ComplexMatrixToken to ComplexMatrixToken.} {
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

test ComplexMatrixToken-3.4 {Test adding ComplexMatrixToken to IntToken.} {
    set r [java::new {ptolemy.data.IntToken int} 2]
    set res1 [$p add $r]
    set res2 [$p addReverse $r]
    set res3 [$r add $p]
    set res4 [$r addReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[7.0 + 0.0i, 6.0 + 0.0i; 5.0 + 0.0i, 4.0 + 0.0i]} {[7.0 + 0.0i, 6.0 + 0.0i; 5.0 + 0.0i, 4.0 + 0.0i]} {[7.0 + 0.0i, 6.0 + 0.0i; 5.0 + 0.0i, 4.0 + 0.0i]} {[7.0 + 0.0i, 6.0 + 0.0i; 5.0 + 0.0i, 4.0 + 0.0i]}}

test ComplexMatrixToken-3.5 {Test adding ComplexMatrixToken to DoubleToken.} {
    set r [java::new {ptolemy.data.DoubleToken double} 2.0]
    set res1 [$p add $r]
    set res2 [$p addReverse $r]
    set res3 [$r add $p]
    set res4 [$r addReverse $p]
 
    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[7.0 + 0.0i, 6.0 + 0.0i; 5.0 + 0.0i, 4.0 + 0.0i]} {[7.0 + 0.0i, 6.0 + 0.0i; 5.0 + 0.0i, 4.0 + 0.0i]} {[7.0 + 0.0i, 6.0 + 0.0i; 5.0 + 0.0i, 4.0 + 0.0i]} {[7.0 + 0.0i, 6.0 + 0.0i; 5.0 + 0.0i, 4.0 + 0.0i]}}

test ComplexMatrixToken-3.6 {Test adding ComplexMatrixToken to BooleanToken.} {
    set r [java::new {ptolemy.data.BooleanToken boolean} true]
    catch {set res1 [$p add $r]} msg1
    catch {set res2 [$p addReverse $r]} msg2
    catch {set res3 [$r add $p]} msg3
    catch {set res4 [$r addReverse $p]} msg4

    list $msg1 $msg2 $msg3 $msg4
} {{ptolemy.kernel.util.IllegalActionException: add method not supported between ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' and ptolemy.data.BooleanToken 'true' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: addReverse method not supported between ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' and ptolemy.data.BooleanToken 'true' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: addReverse method not supported between ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' and ptolemy.data.BooleanToken 'true' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: addReverse method not supported between ptolemy.data.BooleanToken 'true' and ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' because the types are incomparable.}}

test ComplexMatrixToken-3.7 {Test adding ComplexMatrixToken to ComplexToken.} {
    set c1 [java::new {ptolemy.math.Complex double double} 2.0 0.0]
    set r [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c1]
    set res1 [$p add $r]
    set res2 [$p addReverse $r]
    set res3 [$r add $p]
    set res4 [$r addReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[7.0 + 0.0i, 6.0 + 0.0i; 5.0 + 0.0i, 4.0 + 0.0i]} {[7.0 + 0.0i, 6.0 + 0.0i; 5.0 + 0.0i, 4.0 + 0.0i]} {[7.0 + 0.0i, 6.0 + 0.0i; 5.0 + 0.0i, 4.0 + 0.0i]} {[7.0 + 0.0i, 6.0 + 0.0i; 5.0 + 0.0i, 4.0 + 0.0i]}}

######################################################################
####
# Test division of ints with Token types below it in the lossless 
# type hierarchy, and with other Complexs. Note that dividing Complexs could 
# give a Complex.
test ComplexMatrixToken-4.0 {Test dividing Complexs.} {
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
    catch {[set res1 [$p divide $q]]} e1

    list $e1
} {{ptolemy.kernel.util.IllegalActionException: divide operation not supported between ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' and ptolemy.data.ComplexMatrixToken '[2.0 + 0.0i, 1.0 + 0.0i; 3.0 + 0.0i, 1.0 + 0.0i]'}}

######################################################################
####
# Test equals operator applied to other Complexs and Tokens types 
# below it in the lossless type hierarchy.
test ComplexMatrixToken-5.0 {Test equality between Complexs.} {
    set q2 [java::new {ptolemy.data.ComplexMatrixToken ptolemy.math.Complex[][]} $a]
    set res1 [$q isEqualTo $q2]
    set res2 [$q isEqualTo $p]
    set res3 [$q isCloseTo $q2]
    set res4 [$q isCloseTo $p]
    list [$res1 toString] [$res2 toString] \
	    [$res3 toString] [$res4 toString]
} {true false true false}

test ComplexMatrixToken-5.5 {Test closeness between Complexes} {
    set epsilon 0.001
    set oldEpsilon [java::field ptolemy.math.Complex EPSILON]
    java::field ptolemy.math.Complex EPSILON $epsilon

    set c1Close [java::new {ptolemy.math.Complex double double} \
	    [expr {2.0 + (0.5 * $epsilon)} ] 0.0]
    set c2Close [java::new {ptolemy.math.Complex double double} \
	    1.0 0.0]
    set c3Close [java::new {ptolemy.math.Complex double double} \
	    3.0 [expr {0.0 - (0.5 * $epsilon)} ] ]
    set c4Close [java::new {ptolemy.math.Complex double double} \
	    1.0 0.0]
    set aClose [java::new {ptolemy.math.Complex[][]} {2 2}]
    $aClose set {0 0} $c1Close
    $aClose set {0 1} $c2Close
    $aClose set {1 0} $c3Close
    $aClose set {1 1} $c4Close
    set qClose [java::new {ptolemy.data.ComplexMatrixToken ptolemy.math.Complex[][]} $aClose]
    set res1 [$qClose isCloseTo $qClose]
    set res2 [$qClose isCloseTo $q]
    set res3 [$q isCloseTo $qClose]

    # Ok, try something not Close
    set c1NotClose [java::new {ptolemy.math.Complex double double} \
	    [expr {2.0 + (10.0 * $epsilon)} ] 0.0]
    set aNotClose [java::new {ptolemy.math.Complex[][]} {2 2}]
    $aNotClose set {0 0} $c1NotClose
    # The rest are the same
    $aNotClose set {0 1} $c2Close
    $aNotClose set {1 0} $c3Close
    $aNotClose set {1 1} $c4Close
    set qNotClose [java::new {ptolemy.data.ComplexMatrixToken ptolemy.math.Complex[][]} $aNotClose]

    set res4 [$qNotClose isCloseTo $qClose]
    set res5 [$qClose isCloseTo $qNotClose]

    java::field ptolemy.math.Complex EPSILON $oldEpsilon

    list [$res1 toString] [$res2 toString] [$res3 toString] \
	    [$res4 toString] [$res5 toString] \

} {true true true false false}

######################################################################
####
# Test modulo operator between Complexs and Complexs.
test ComplexMatrixToken-6.0 {Test modulo between Complexs.} {
    catch {[set res1 [$p modulo $q]]} e1

    list $e1
} {{ptolemy.kernel.util.IllegalActionException: modulo operation not supported between ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' and ptolemy.data.ComplexMatrixToken '[2.0 + 0.0i, 1.0 + 0.0i; 3.0 + 0.0i, 1.0 + 0.0i]'}}

######################################################################
####
# Test multiply operator between Complexs and Complexs.
test ComplexMatrixToken-7.0 {Test multiply operator between ComplexMatrixTokens and IntMatrixToken} {
    set b [java::new {int[][]} {2 2} {{2 1} {3 1}}]
    set q [java::new {ptolemy.data.IntMatrixToken int[][]} $b]
    set res1 [$p multiply $q]
    set res2 [$p multiplyReverse $q]
    set res3 [$q multiply $p]
    set res4 [$q multiplyReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[22.0 + 0.0i, 9.0 + 0.0i; 12.0 + 0.0i, 5.0 + 0.0i]} {[13.0 + 0.0i, 10.0 + 0.0i; 18.0 + 0.0i, 14.0 + 0.0i]} {[13.0 + 0.0i, 10.0 + 0.0i; 18.0 + 0.0i, 14.0 + 0.0i]} {[22.0 + 0.0i, 9.0 + 0.0i; 12.0 + 0.0i, 5.0 + 0.0i]}}

test ComplexMatrixToken-7.0.1 {Test multiply operator between IntMatrixToken and ComplexMatrixToken of different dimensions} {
    set b [java::new {int[][]} {2 3} {{2 1 3} {3 1 6}}]
    set q [java::new {ptolemy.data.IntMatrixToken int[][]} $b]
    set res1 [$p multiply $q]
    catch {set res2 [$p multiplyReverse $q]} msg2
    catch {set res3 [$q multiply $p]} msg3
    set res4 [$q multiplyReverse $p]
  
    list [$res1 toString] $msg2 $msg3 [$res4 toString]
} {{[22.0 + 0.0i, 9.0 + 0.0i, 39.0 + 0.0i; 12.0 + 0.0i, 5.0 + 0.0i, 21.0 + 0.0i]} {ptolemy.kernel.util.IllegalActionException: multiplyReverse operation not supported between ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' and ptolemy.data.IntMatrixToken '[2, 1, 3; 3, 1, 6]'
Because:
multiply operation not supported between ptolemy.data.ComplexMatrixToken '[2.0 + 0.0i, 1.0 + 0.0i, 3.0 + 0.0i; 3.0 + 0.0i, 1.0 + 0.0i, 6.0 + 0.0i]' and ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' because the matrices have incompatible dimensions.} {ptolemy.kernel.util.IllegalActionException: multiplyReverse operation not supported between ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' and ptolemy.data.IntMatrixToken '[2, 1, 3; 3, 1, 6]'
Because:
multiply operation not supported between ptolemy.data.ComplexMatrixToken '[2.0 + 0.0i, 1.0 + 0.0i, 3.0 + 0.0i; 3.0 + 0.0i, 1.0 + 0.0i, 6.0 + 0.0i]' and ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' because the matrices have incompatible dimensions.} {[22.0 + 0.0i, 9.0 + 0.0i, 39.0 + 0.0i; 12.0 + 0.0i, 5.0 + 0.0i, 21.0 + 0.0i]}}

test ComplexMatrixToken-7.1 {Test multiplying ComplexMatrixToken to DoubleMatrixToken.} {
    set b [java::new {double[][]} {2 2} {{2.0 1.0} {3.0 1.0}}]
    set q [java::new {ptolemy.data.DoubleMatrixToken double[][]} $b]
    set res1 [$p multiply $q]
    set res2 [$p multiplyReverse $q]
    set res3 [$q multiply $p]
    set res4 [$q multiplyReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[22.0 + 0.0i, 9.0 + 0.0i; 12.0 + 0.0i, 5.0 + 0.0i]} {[13.0 + 0.0i, 10.0 + 0.0i; 18.0 + 0.0i, 14.0 + 0.0i]} {[13.0 + 0.0i, 10.0 + 0.0i; 18.0 + 0.0i, 14.0 + 0.0i]} {[22.0 + 0.0i, 9.0 + 0.0i; 12.0 + 0.0i, 5.0 + 0.0i]}}

test ComplexMatrixToken-7.1.1 {Test multiply operator between ComplexMatrixToken and DoubleMatrixToken of different dimensions} {
    set b [java::new {double[][]} {2 3} {{2.0 1.0 3.0} {3.0 1.0 6.0}}]
    set q [java::new {ptolemy.data.DoubleMatrixToken double[][]} $b]
    set res1 [$p multiply $q]
    catch {set res2 [$p multiplyReverse $q]} msg2
    catch {set res3 [$q multiply $p]} msg3
    set res4 [$q multiplyReverse $p]
  
    list [$res1 toString] $msg2 $msg3 [$res4 toString]
} {{[22.0 + 0.0i, 9.0 + 0.0i, 39.0 + 0.0i; 12.0 + 0.0i, 5.0 + 0.0i, 21.0 + 0.0i]} {ptolemy.kernel.util.IllegalActionException: multiplyReverse operation not supported between ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' and ptolemy.data.DoubleMatrixToken '[2.0, 1.0, 3.0; 3.0, 1.0, 6.0]'
Because:
multiply operation not supported between ptolemy.data.ComplexMatrixToken '[2.0 + 0.0i, 1.0 + 0.0i, 3.0 + 0.0i; 3.0 + 0.0i, 1.0 + 0.0i, 6.0 + 0.0i]' and ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' because the matrices have incompatible dimensions.} {ptolemy.kernel.util.IllegalActionException: multiplyReverse operation not supported between ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' and ptolemy.data.DoubleMatrixToken '[2.0, 1.0, 3.0; 3.0, 1.0, 6.0]'
Because:
multiply operation not supported between ptolemy.data.ComplexMatrixToken '[2.0 + 0.0i, 1.0 + 0.0i, 3.0 + 0.0i; 3.0 + 0.0i, 1.0 + 0.0i, 6.0 + 0.0i]' and ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' because the matrices have incompatible dimensions.} {[22.0 + 0.0i, 9.0 + 0.0i, 39.0 + 0.0i; 12.0 + 0.0i, 5.0 + 0.0i, 21.0 + 0.0i]}}

test ComplexMatrixToken-7.2 {Test multiplying ComplexMatrixToken to BooleanMatrixToken.} {
    set b [java::new {boolean[][]} {2 2} {{true false} {true false}}]
    set q [java::new {ptolemy.data.BooleanMatrixToken boolean[][]} $b]
    catch {set res1 [$p multiply $q]} msg1
    catch {set res2 [$p multiplyReverse $q]} msg2
    catch {set res3 [$q multiply $p]} msg3
    catch {set res4 [$q multiplyReverse $p]} msg4

    list "$msg1\n$msg2\n$msg3\n$msg4"
} {{ptolemy.kernel.util.IllegalActionException: multiply method not supported between ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' and ptolemy.data.BooleanMatrixToken '[true, false; true, false]' because the types are incomparable.
ptolemy.kernel.util.IllegalActionException: multiplyReverse method not supported between ptolemy.data.BooleanMatrixToken '[true, false; true, false]' and ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' because the types are incomparable.
ptolemy.kernel.util.IllegalActionException: multiply method not supported between ptolemy.data.BooleanMatrixToken '[true, false; true, false]' and ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' because the types are incomparable.
ptolemy.kernel.util.IllegalActionException: multiplyReverse method not supported between ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' and ptolemy.data.BooleanMatrixToken '[true, false; true, false]' because the types are incomparable.}}

test ComplexMatrixToken-7.2.1 {Test multiply operator between ComplexMatrixToken and BooleanMatrixToken of different dimensions} {
    set b [java::new {boolean[][]} {2 3} {{true false true} {false true false}}]
    set q [java::new {ptolemy.data.BooleanMatrixToken boolean[][]} $b]
    catch {set res1 [$p multiply $q]} msg1
    catch {set res2 [$p multiplyReverse $q]} msg2
    catch {set res3 [$q multiply $p]} msg3
    catch {set res4 [$q multiplyReverse $p]} msg4

    list "$msg1\n$msg2\n$msg3\n$msg4"
} {{ptolemy.kernel.util.IllegalActionException: multiply method not supported between ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' and ptolemy.data.BooleanMatrixToken '[true, false, true; false, true, false]' because the types are incomparable.
ptolemy.kernel.util.IllegalActionException: multiplyReverse method not supported between ptolemy.data.BooleanMatrixToken '[true, false, true; false, true, false]' and ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' because the types are incomparable.
ptolemy.kernel.util.IllegalActionException: multiply method not supported between ptolemy.data.BooleanMatrixToken '[true, false, true; false, true, false]' and ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' because the types are incomparable.
ptolemy.kernel.util.IllegalActionException: multiplyReverse method not supported between ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' and ptolemy.data.BooleanMatrixToken '[true, false, true; false, true, false]' because the types are incomparable.}}

test ComplexMatrixToken-7.3 {Test multiplying ComplexMatrixToken to ComplexMatrixToken.} {
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

test ComplexMatrixToken-7.3.1 {Test multiply operator between ComplexMatrixToken and ComplexMatrixToken of different dimensions} {
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
} {{[22.0 + 0.0i, 9.0 + 0.0i, 39.0 + 0.0i; 12.0 + 0.0i, 5.0 + 0.0i, 21.0 + 0.0i]} {ptolemy.kernel.util.IllegalActionException: multiply operation not supported between ptolemy.data.ComplexMatrixToken '[2.0 + 0.0i, 1.0 + 0.0i, 3.0 + 0.0i; 3.0 + 0.0i, 1.0 + 0.0i, 6.0 + 0.0i]' and ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' because the matrices have incompatible dimensions.} {ptolemy.kernel.util.IllegalActionException: multiply operation not supported between ptolemy.data.ComplexMatrixToken '[2.0 + 0.0i, 1.0 + 0.0i, 3.0 + 0.0i; 3.0 + 0.0i, 1.0 + 0.0i, 6.0 + 0.0i]' and ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' because the matrices have incompatible dimensions.} {[22.0 + 0.0i, 9.0 + 0.0i, 39.0 + 0.0i; 12.0 + 0.0i, 5.0 + 0.0i, 21.0 + 0.0i]}}

test ComplexMatrixToken-7.4 {Test multiplying ComplexMatrixToken to IntToken.} {
    set r [java::new {ptolemy.data.IntToken int} 2]
    set res1 [$p multiply $r]
    set res2 [$p multiplyReverse $r]
    set res3 [$r multiply $p]
    set res4 [$r multiplyReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[10.0 + 0.0i, 8.0 + 0.0i; 6.0 + 0.0i, 4.0 + 0.0i]} {[10.0 + 0.0i, 8.0 + 0.0i; 6.0 + 0.0i, 4.0 + 0.0i]} {[10.0 + 0.0i, 8.0 + 0.0i; 6.0 + 0.0i, 4.0 + 0.0i]} {[10.0 + 0.0i, 8.0 + 0.0i; 6.0 + 0.0i, 4.0 + 0.0i]}}


test ComplexMatrixToken-7.5 {Test multiplying ComplexMatrixToken to DoubleToken.} {
    set r [java::new {ptolemy.data.DoubleToken double} 2.0]
    set res1 [$p multiply $r]
    set res2 [$p multiplyReverse $r]
    set res3 [$r multiply $p]
    set res4 [$r multiplyReverse $p]
 
    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[10.0 + 0.0i, 8.0 + 0.0i; 6.0 + 0.0i, 4.0 + 0.0i]} {[10.0 + 0.0i, 8.0 + 0.0i; 6.0 + 0.0i, 4.0 + 0.0i]} {[10.0 + 0.0i, 8.0 + 0.0i; 6.0 + 0.0i, 4.0 + 0.0i]} {[10.0 + 0.0i, 8.0 + 0.0i; 6.0 + 0.0i, 4.0 + 0.0i]}}

test ComplexMatrixToken-7.6 {Test multiplying ComplexMatrixToken to BooleanToken.} {
    set r [java::new {ptolemy.data.BooleanToken boolean} true]
    catch {set res1 [$p multiply $r]} msg1
    catch {set res2 [$p multiplyReverse $r]} msg2
    catch {set res3 [$r multiply $p]} msg3
    catch {set res4 [$r multiplyReverse $p]} msg4

    list "$msg1\n$msg2\n$msg3\n$msg4"
} {{ptolemy.kernel.util.IllegalActionException: multiply method not supported between ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' and ptolemy.data.BooleanToken 'true' because the types are incomparable.
ptolemy.kernel.util.IllegalActionException: multiplyReverse method not supported between ptolemy.data.BooleanToken 'true' and ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' because the types are incomparable.
ptolemy.kernel.util.IllegalActionException: multiplyReverse method not supported between ptolemy.data.BooleanToken 'true' and ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' because the types are incomparable.
ptolemy.kernel.util.IllegalActionException: multiply method not supported between ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' and ptolemy.data.BooleanToken 'true' because the types are incomparable.}}

test ComplexMatrixToken-7.7 {Test multiplying ComplexMatrixToken to ComplexToken.} {
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
# Test subtract operator between Complexes and Complexes.
test ComplexMatrixToken-8.0 {Test subtract operator between  ComplexMatrixToken to IntMatrixToken.} {
    set b [java::new {int[][]} {2 2} {{2 1} {3 1}}]
    set q [java::new {ptolemy.data.IntMatrixToken int[][]} $b]
    set res1 [$p subtract $q]
    set res2 [$p subtractReverse $q]
    set res3 [$q subtract $p]
    set res4 [$q subtractReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[3.0 + 0.0i, 3.0 + 0.0i; 0.0 + 0.0i, 1.0 + 0.0i]} {[-3.0 + 0.0i, -3.0 + 0.0i; 0.0 + 0.0i, -1.0 + 0.0i]} {[-3.0 + 0.0i, -3.0 + 0.0i; 0.0 + 0.0i, -1.0 + 0.0i]} {[3.0 + 0.0i, 3.0 + 0.0i; 0.0 + 0.0i, 1.0 + 0.0i]}}

test ComplexMatrixToken-8.1 {Test subtracting ComplexMatrixToken to DoubleMatrixToken.} {
    set b [java::new {double[][]} {2 2} {{2.0 1.0} {3.0 1.0}}]
    set q [java::new {ptolemy.data.DoubleMatrixToken double[][]} $b]
    set res1 [$p subtract $q]
    set res2 [$p subtractReverse $q]
    set res3 [$q subtract $p]
    set res4 [$q subtractReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[3.0 + 0.0i, 3.0 + 0.0i; 0.0 + 0.0i, 1.0 + 0.0i]} {[-3.0 + 0.0i, -3.0 + 0.0i; 0.0 + 0.0i, -1.0 + 0.0i]} {[-3.0 + 0.0i, -3.0 + 0.0i; 0.0 + 0.0i, -1.0 + 0.0i]} {[3.0 + 0.0i, 3.0 + 0.0i; 0.0 + 0.0i, 1.0 + 0.0i]}}

test ComplexMatrixToken-8.2 {Test subtracting ComplexMatrixToken to BooleanMatrixToken.} {
    set b [java::new {boolean[][]} {2 2} {{true false} {true false}}]
    set q [java::new {ptolemy.data.BooleanMatrixToken boolean[][]} $b]
    catch {set res1 [$p subtract $q]} msg1
    catch {set res2 [$p subtractReverse $q]} msg2
    catch {set res3 [$q subtract $p]} msg3
    catch {set res4 [$q subtractReverse $p]} msg4

    list $msg1 $msg2 $msg3 $msg4
} {{ptolemy.kernel.util.IllegalActionException: subtract method not supported between ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' and ptolemy.data.BooleanMatrixToken '[true, false; true, false]' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: subtractReverse method not supported between ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' and ptolemy.data.BooleanMatrixToken '[true, false; true, false]' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: subtract method not supported between ptolemy.data.BooleanMatrixToken '[true, false; true, false]' and ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: subtractReverse method not supported between ptolemy.data.BooleanMatrixToken '[true, false; true, false]' and ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' because the types are incomparable.}}

test ComplexMatrixToken-8.3 {Test subtracting ComplexMatrixToken to ComplexMatrixToken.} {
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

test ComplexMatrixToken-8.4 {Test subtracting ComplexMatrixToken to IntToken.} {
    set r [java::new {ptolemy.data.IntToken int} 2]
    set res1 [$p subtract $r]
    set res2 [$p subtractReverse $r]
    set res3 [$r subtract $p]
    set res4 [$r subtractReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[3.0 + 0.0i, 2.0 + 0.0i; 1.0 + 0.0i, 0.0 + 0.0i]} {[-3.0 + 0.0i, -2.0 + 0.0i; -1.0 + 0.0i, 0.0 + 0.0i]} {[-3.0 + 0.0i, -2.0 + 0.0i; -1.0 + 0.0i, 0.0 + 0.0i]} {[3.0 + 0.0i, 2.0 + 0.0i; 1.0 + 0.0i, 0.0 + 0.0i]}}

test ComplexMatrixToken-8.5 {Test subtracting ComplexMatrixToken to DoubleToken.} {
    set r [java::new {ptolemy.data.DoubleToken double} 2.0]
    set res1 [$p subtract $r]
    set res2 [$p subtractReverse $r]
    set res3 [$r subtract $p]
    set res4 [$r subtractReverse $p]
 
    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[3.0 + 0.0i, 2.0 + 0.0i; 1.0 + 0.0i, 0.0 + 0.0i]} {[-3.0 + 0.0i, -2.0 + 0.0i; -1.0 + 0.0i, 0.0 + 0.0i]} {[-3.0 + 0.0i, -2.0 + 0.0i; -1.0 + 0.0i, 0.0 + 0.0i]} {[3.0 + 0.0i, 2.0 + 0.0i; 1.0 + 0.0i, 0.0 + 0.0i]}}

test ComplexMatrixToken-8.6 {Test subtracting ComplexMatrixToken to BooleanToken.} {
    set r [java::new {ptolemy.data.BooleanToken boolean} true]
    catch {set res1 [$p subtract $r]} msg1
    catch {set res2 [$p subtractReverse $r]} msg2
    catch {set res3 [$r subtract $p]} msg3
    catch {set res4 [$r subtractReverse $p]} msg4

    list $msg1 $msg2 $msg3 $msg4
} {{ptolemy.kernel.util.IllegalActionException: subtract method not supported between ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' and ptolemy.data.BooleanToken 'true' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: subtractReverse method not supported between ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' and ptolemy.data.BooleanToken 'true' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: subtractReverse method not supported between ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' and ptolemy.data.BooleanToken 'true' because the types are incomparable.} {ptolemy.kernel.util.IllegalActionException: subtractReverse method not supported between ptolemy.data.BooleanToken 'true' and ptolemy.data.ComplexMatrixToken '[5.0 + 0.0i, 4.0 + 0.0i; 3.0 + 0.0i, 2.0 + 0.0i]' because the types are incomparable.}}

test ComplexMatrixToken-8.7 {Test subtracting ComplexMatrixToken to ComplexToken.} {
    set c1 [java::new {ptolemy.math.Complex double double} 2.0 0.0]
    set r [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c1]
    set res1 [$p subtract $r]
    set res2 [$p subtractReverse $r]
    set res3 [$r subtract $p]
    set res4 [$r subtractReverse $p]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] 
} {{[3.0 + 0.0i, 2.0 + 0.0i; 1.0 + 0.0i, 0.0 + 0.0i]} {[-3.0 + 0.0i, -2.0 + 0.0i; -1.0 + 0.0i, 0.0 + 0.0i]} {[-3.0 + 0.0i, -2.0 + 0.0i; -1.0 + 0.0i, 0.0 + 0.0i]} {[3.0 + 0.0i, 2.0 + 0.0i; 1.0 + 0.0i, 0.0 + 0.0i]}}

######################################################################
####
# 
test ComplexMatrixToken-9.0 {Test equals} {
    set p1 [java::new {ptolemy.data.ComplexMatrixToken String} "\[1+2i, 2+3i; 3+4i, 4+5i\]"]
    set p2 [java::new {ptolemy.data.ComplexMatrixToken String} "\[1+2i, 2+3i; 3+4i, 4+5i\]"]
    set p3 [java::new {ptolemy.data.ComplexMatrixToken String} "\[9+8i, 8+7i; 6+5i, 5+4i\]"]
    list [$p1 equals $p1] [$p1 equals $p2] [$p1 equals $p3]
} {1 1 0}

######################################################################
####
# 
test ComplexMatrixToken-10.0 {Test hashCode} {
    set p1 [java::new {ptolemy.data.ComplexMatrixToken String} "\[1+2i, 2+3i; 3+4i, 4+5i\]"]
    set p2 [java::new {ptolemy.data.ComplexMatrixToken String} "\[1+2i, 2+3i; 3+4i, 4+5i\]"]
    set p3 [java::new {ptolemy.data.ComplexMatrixToken String} "\[9+8i, 8+7i; 6+5i, 5+4i\]"]
    list [$p1 hashCode] [$p2 hashCode] [$p3 hashCode]
} {17 17 36}

