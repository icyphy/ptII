# Tests for the ComplexMatrixToken class
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
} {{ptolemy.kernel.util.IllegalActionException: ptolemy.data.ComplexMatrixToken cannot be converted to a double matrix.}}

######################################################################
####
# 
test ComplexMatrixToken-2.2 {Create a non-empty instance and query its value as a long} {
    catch {$p longMatrix} result
    list $result
} {{ptolemy.kernel.util.IllegalActionException: ptolemy.data.ComplexMatrixToken cannot be converted to a long matrix.}}

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
test ComplexMatrixToken-2.6 {Test multiplicative identity} {
    set token [$p one]
    list [$token toString]
} {{[1.0 + 0.0i, 0.0 + 0.0i; 0.0 + 0.0i, 1.0 + 0.0i]}}

######################################################################
####
# Test addition of Complexs to Token types below it in the lossless 
# type hierarchy, and with other Complexs.
test ComplexMatrixToken-3.0 {Test adding Complexs.} {
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

    list [$res1 toString] 
} {{[7.0 + 0.0i, 5.0 + 0.0i; 6.0 + 0.0i, 3.0 + 0.0i]}}

######################################################################
####
# Test division of ints with Token types below it in the lossless 
# type hierarchy, and with other Complexs. Note that dividing Complexs could 
# give a Complex.
test ComplexMatrixToken-4.0 {Test dividing Complexs.} {
    catch {[set res1 [$p divide $q]]} e1

    list $e1
} {{ptolemy.kernel.util.IllegalActionException: Division not supported for ptolemy.data.ComplexMatrixToken divided by ptolemy.data.ComplexMatrixToken.}}

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
    set oldEpsilon [java::field ptolemy.math.Complex epsilon]
    java::field ptolemy.math.Complex epsilon $epsilon

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

    java::field ptolemy.math.Complex epsilon $oldEpsilon

    list [$res1 toString] [$res2 toString] [$res3 toString] \
	    [$res4 toString] [$res5 toString] \

} {true true true false false}

######################################################################
####
# Test modulo operator between Complexs and Complexs.
test ComplexMatrixToken-6.0 {Test modulo between Complexs.} {
    catch {[set res1 [$p modulo $q]]} e1

    list $e1
} {{ptolemy.kernel.util.IllegalActionException: Modulo operation not supported: ptolemy.data.ComplexMatrixToken modulo ptolemy.data.ComplexMatrixToken.}}

######################################################################
####
# Test multiply operator between Complexs and Complexs.
test ComplexMatrixToken-7.0 {Test multiply operator between Complexs.} {
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

    set q3 [java::new {ptolemy.data.ComplexMatrixToken ptolemy.math.Complex[][]} $a]
    set res1 [$p multiply $q]
    set res2 [$p multiply $q3]
    catch {$q3 multiply $p} res3

    list [$res1 toString] [$res2 toString] $res3
} {{[22.0 + 0.0i, 9.0 + 0.0i; 12.0 + 0.0i, 5.0 + 0.0i]} {[22.0 + 0.0i, 9.0 + 0.0i, 39.0 + 0.0i; 12.0 + 0.0i, 5.0 + 0.0i, 21.0 + 0.0i]} {ptolemy.kernel.util.IllegalActionException: Cannot multiply matrix with 3 columns by a matrix with 2 rows.}}

######################################################################
####
# Test subtract operator between Complexs and Complexs.
test ComplexMatrixToken-8.0 {Test subtract operator between Complexs.} {
    set res1 [$p subtract $q]

    list [$res1 toString] 
} {{[3.0 + 0.0i, 3.0 + 0.0i; 0.0 + 0.0i, 1.0 + 0.0i]}}

######################################################################
####
# 
test ComplexMatrixToken-3.0 {Test equals} {
    set p1 [java::new {ptolemy.data.ComplexMatrixToken String} "\[1+2i, 2+3i; 3+4i, 4+5i\]"]
    set p2 [java::new {ptolemy.data.ComplexMatrixToken String} "\[1+2i, 2+3i; 3+4i, 4+5i\]"]
    set p3 [java::new {ptolemy.data.ComplexMatrixToken String} "\[9+8i, 8+7i; 6+5i, 5+4i\]"]
    list [$p1 equals $p1] [$p1 equals $p2] [$p1 equals $p3]
} {1 1 0}

######################################################################
####
# 
test ComplexMatrixToken-4.0 {Test hashCode} {
    set p1 [java::new {ptolemy.data.ComplexMatrixToken String} "\[1+2i, 2+3i; 3+4i, 4+5i\]"]
    set p2 [java::new {ptolemy.data.ComplexMatrixToken String} "\[1+2i, 2+3i; 3+4i, 4+5i\]"]
    set p3 [java::new {ptolemy.data.ComplexMatrixToken String} "\[9+8i, 8+7i; 6+5i, 5+4i\]"]
    list [$p1 hashCode] [$p2 hashCode] [$p3 hashCode]
} {17 17 36}

