# Tests for the ComplexToken class
#
# @Author: Yuhong Xiong
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
test ComplexToken-1.0 {Create an empty instance} {
    set p [java::new ptolemy.data.ComplexToken]
    $p toString
} {0.0 + 0.0i}

######################################################################
####
# 
test ComplexToken-1.1 {Create a non-empty instance from a Complex} {
    set c [java::new {ptolemy.math.Complex double double} 1.1 2.2]
    set p [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c]
    $p toString
} {1.1 + 2.2i}

######################################################################
####
# 
test ComplexToken-1.2 {Create a non-empty instance from an String} {
    set p [java::new {ptolemy.data.ComplexToken String} "1.0+2.0i"]
    $p toString
} {1.0 + 2.0i}

######################################################################
####
# 
test ComplexToken-1.7 {Create a bogus Token from a bogus String} {
    catch {java::new {ptolemy.data.ComplexToken String} "\"A String\""} \
	errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: A ComplexToken cannot be created from the expression '"A String"'}}

######################################################################
####
# 
test ComplexToken-2.0 {Test complexValue} {
    set c [java::new {ptolemy.math.Complex double double} 3.3 4.4]
    set p [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c]
    set res1 [$p complexValue]
    list [$res1 toString]
} {{3.3 + 4.4i}}

######################################################################
####
# 
test ComplexToken-2.1 {Test doubleValue} {
    # use the Complex above
    catch {$p doubleValue} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.ComplexToken '3.3 + 4.4i' to the type double.}}

######################################################################
####
# 
test ComplexToken-2.2 {Test intValue} {
    # use the Complex above
    catch {$p intValue} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.ComplexToken '3.3 + 4.4i' to the type int.}}

######################################################################
####
# 
test ComplexToken-2.3 {Test longValue} {
    # use the Complex above
    catch {$p longValue} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.ComplexToken '3.3 + 4.4i' to the type long.}}

######################################################################
####
# 
test ComplexToken-2.4 {Test toString} {
    # use the Complex above
    $p toString
} {3.3 + 4.4i}

######################################################################
####
# 
test ComplexToken-2.5 {Test additive identity} {
    set token [$p zero]

    list [$token toString]
} {{0.0 + 0.0i}}

######################################################################
####
# 
test ComplexToken-2.6 {Test multiplicative identity} {
    set p [java::new {ptolemy.data.ComplexToken}]
    set token [$p one]

    list [$token toString]
} {{1.0 + 0.0i}}

######################################################################
####
# Test addition of Complex to Token types below it in the lossless 
# type hierarchy, and with other Complex.
test ComplexToken-3.0 {Test adding Complex} {
    set c [java::new {ptolemy.math.Complex double double} 5.5 6.6]
    set p [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c]
    set res [$p add $p]

    list [$res toString]
} {{11.0 + 13.2i}}

######################################################################
####
# 
test ComplexToken-3.1 {Test adding Complex and double} {
    # use the Complex above
    set d [java::new {ptolemy.data.DoubleToken double} 2.5]
    set res1 [$p add $d]
    set res2 [$p addReverse $d]

    set res3 [$d add $p]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {{8.0 + 6.6i} {8.0 + 6.6i} {8.0 + 6.6i}}

######################################################################
####
# 
test ComplexToken-3.2 {Test adding Complex and int} {
    # use the Complex above
    set i [java::new {ptolemy.data.IntToken int} 2]
    set res1 [$p add $i]
    set res2 [$p addReverse $i]

    set res3 [$i add $p]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {{7.5 + 6.6i} {7.5 + 6.6i} {7.5 + 6.6i}}

######################################################################
####
# Test division of Complex with Token types below it in the lossless 
# type hierarchy, and with other Complex.
test ComplexToken-4.0 {Test dividing Complex} {
    set c [java::new {ptolemy.math.Complex double double} 7.7 8.8]
    set p [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c]
    set res [$p divide $p]

    list [$res toString]
} {{1.0 + 0.0i}}

######################################################################
####
# 
test ComplexToken-4.1 {Test dividing Complex and double} {
    set c [java::new {ptolemy.math.Complex double double} 8.0 4.0]
    set p [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c]
    set d [java::new {ptolemy.data.DoubleToken double} 2.0]
    set res1 [$p divide $d]
    set res2 [$p divideReverse $d]

    list [$res1 toString] [$res2 toString]
} {{4.0 + 2.0i} {0.2 - 0.1i}}

######################################################################
####
# 
test ComplexToken-4.2 {Test dividing Complex and int} {
    # use the Complex above
    set i [java::new {ptolemy.data.IntToken int} 2]
    set res1 [$p divide $i]
    set res2 [$p divideReverse $i]

    set res3 [$i divide $p]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {{4.0 + 2.0i} {0.2 - 0.1i} {0.2 - 0.1i}}

######################################################################
####
# Test isEqualTo operator applied to other Complex and Tokens types 
# below it in the lossless type hierarchy.
test ComplexToken-5.0 {Test equality between Complex} {
    set c1 [java::new {ptolemy.math.Complex double double} 7.7 8.8]
    set p1 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c1]
    set c2 [java::new {ptolemy.math.Complex double double} 0.5 0.6]
    set p2 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c2]

    set res1 [$p1 {isEqualTo ptolemy.data.Token} $p1]
    set res2 [$p1 {isEqualTo ptolemy.data.Token} $p2]

    list [$res1 toString] [$res2 toString]
} {true false}

######################################################################
####
# 
test ComplexToken-5.1 {Test equality between Complex and double} {
    set c1 [java::new {ptolemy.math.Complex double double} 8.0 0.0]
    set p1 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c1]
    set c2 [java::new {ptolemy.math.Complex double double} 4.0 0.0]
    set p2 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c2]

    set d1 [java::new {ptolemy.data.DoubleToken double} 8]
    set d2 [java::new {ptolemy.data.DoubleToken double} 4]

    set res1 [$p1 {isEqualTo ptolemy.data.Token} $d1]
    set res2 [$p1 {isEqualTo ptolemy.data.Token} $d2]

    set res3 [$d1 {isEqualTo ptolemy.data.Token} $p1]
    set res4 [$d1 {isEqualTo ptolemy.data.Token} $p2]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString]
} {true false true false}

######################################################################
####
# 
test ComplexToken-5.2 {Test equality between Complex and int} {
    # use the Complex above

    set i1 [java::new {ptolemy.data.IntToken int} 8]
    set i2 [java::new {ptolemy.data.IntToken int} 4]

    set res1 [$p1 {isEqualTo ptolemy.data.Token} $i1]
    set res2 [$p1 {isEqualTo ptolemy.data.Token} $i2]

    set res3 [$i1 {isEqualTo ptolemy.data.Token} $p1]
    set res4 [$i1 {isEqualTo ptolemy.data.Token} $p2]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString]
} {true false true false}


######################################################################
####
# Test isCloseTo operator applied to other Complex and Tokens types 
# below it in the lossless type hierarchy.
test ComplexToken-5.5 {Test equality between Complex} {
    set c1 [java::new {ptolemy.math.Complex double double} 7.7 8.8]
    set p1 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c1]
    set c2 [java::new {ptolemy.math.Complex double double} 0.5 0.6]
    set p2 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c2]

    set res1 [$p1 {isCloseTo ptolemy.data.Token} $p1]
    set res2 [$p1 {isCloseTo ptolemy.data.Token} $p2]

    list [$res1 toString] [$res2 toString]
} {true false}

######################################################################
####
# 
test ComplexToken-5.6 {Test closeness between Complex and double} {
    set c1 [java::new {ptolemy.math.Complex double double} 8.0 0.0]
    set p1 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c1]
    set c2 [java::new {ptolemy.math.Complex double double} 4.0 0.0]
    set p2 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c2]

    set d1 [java::new {ptolemy.data.DoubleToken double} 8]
    set d2 [java::new {ptolemy.data.DoubleToken double} 4]

    set res1 [$p1 {isCloseTo ptolemy.data.Token} $d1]
    set res2 [$p1 {isCloseTo ptolemy.data.Token} $d2]

    set res3 [$d1 {isCloseTo ptolemy.data.Token} $p1]
    set res4 [$d1 {isCloseTo ptolemy.data.Token} $p2]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString]
} {true false true false}

######################################################################
####
# 
test ComplexToken-5.7 {Test closeness between Complex and int} {
    # use the Complex above

    set i1 [java::new {ptolemy.data.IntToken int} 8]
    set i2 [java::new {ptolemy.data.IntToken int} 4]

    set res1 [$p1 {isCloseTo ptolemy.data.Token} $i1]
    set res2 [$p1 {isCloseTo ptolemy.data.Token} $i2]

    set res3 [$i1 {isCloseTo ptolemy.data.Token} $p1]
    set res4 [$i1 {isCloseTo ptolemy.data.Token} $p2]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString]
} {true false true false}

######################################################################
####
# 
test ComplexToken-5.8 {Test closeness between two Complex} {

    set epsilon 0.001
    set oldEpsilon [java::field ptolemy.math.Complex EPSILON]
    java::field ptolemy.math.Complex EPSILON $epsilon

    set c1 [java::new {ptolemy.math.Complex double double} -10.0 0.0]
    set p1 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c1]

    set c2 [java::new {ptolemy.math.Complex double double} \
	    [expr {-10.0 + 0.5 * $epsilon}] \
	    [expr {0.0 + 0.5 * $epsilon}]]

    set p2 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c2]

    set c3 [java::new {ptolemy.math.Complex double double} \
	    [expr {-10.0 + 2.0 * $epsilon}] \
	    [expr {0.0 + 2.0* $epsilon}]]

    set p3 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c3]

    set res1 [$p1 {isCloseTo ptolemy.data.Token} $p1]
    set res2 [$p1 {isCloseTo ptolemy.data.Token} $p2]
    set res3 [$p1 {isCloseTo ptolemy.data.Token} $p3]

    set res4 [$p2 {isCloseTo ptolemy.data.Token} $p1]
    set res5 [$p2 {isCloseTo ptolemy.data.Token} $p2]
    set res6 [$p2 {isCloseTo ptolemy.data.Token} $p3]

    set res7 [$p3 {isCloseTo ptolemy.data.Token} $p1]
    set res8 [$p3 {isCloseTo ptolemy.data.Token} $p2]
    set res9 [$p3 {isCloseTo ptolemy.data.Token} $p3]

    java::field ptolemy.math.Complex EPSILON $oldEpsilon

    list [$res1 toString] [$res2 toString] [$res3 toString] \
	    [$res4 toString] [$res5 toString] [$res6 toString] \
	    [$res7 toString] [$res8 toString] [$res9 toString]

} {true true false true true false false false true}

######################################################################
####
#
test ComplexToken-6.0 {Test modulo} {
    # use the Complex  above
    catch {res [$p1 modulo $p1]} msg

    list $msg
} {{ptolemy.kernel.util.IllegalActionException: modulo operation not supported between ptolemy.data.ComplexToken '-10.0 + 0.0i' and ptolemy.data.ComplexToken '-10.0 + 0.0i'}}

######################################################################
####
# Test multiply
test ComplexToken-7.0 {Test multiply operator between Complex} {
    set c1 [java::new {ptolemy.math.Complex double double} 2.0 3.0]
    set p1 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c1]
    set c2 [java::new {ptolemy.math.Complex double double} 4.0 5.0]
    set p2 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c2]

    set res1 [$p1 multiply $p2]

    list [$res1 toString]
} {{-7.0 + 22.0i}}

######################################################################
####
#
test ComplexToken-7.1 {Test multiply between Complex and double} {
    # use the Complex above
    set d [java::new {ptolemy.data.DoubleToken double} 6]

    set res1 [$p1 multiply $d]
    set res2 [$p1 multiplyReverse $d]
    set res3 [$d multiply $p1]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {{12.0 + 18.0i} {12.0 + 18.0i} {12.0 + 18.0i}}

######################################################################
####
# 
test ComplexToken-7.2 {Test multiply between Complex and int} {
    # use the Complex above
    set i [java::new {ptolemy.data.IntToken int} 6]

    set res1 [$p1 multiply $i]
    set res2 [$p1 multiplyReverse $i]
    set res3 [$i multiply $p1]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {{12.0 + 18.0i} {12.0 + 18.0i} {12.0 + 18.0i}}

######################################################################
####
# Test subtract operator between complex
test ComplexToken-8.0 {Test subtract between Complex} {
    set c1 [java::new {ptolemy.math.Complex double double} 6.0 7.0]
    set p1 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c1]
    set c2 [java::new {ptolemy.math.Complex double double} 8.0 9.0]
    set p2 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c2]

    set res1 [$p1 subtract $p2]

    list [$res1 toString]
} {{-2.0 - 2.0i}}

######################################################################
####
# 
test ComplexToken-8.1 {Test subtract operator between Complex and double} {
    # use the complex above
    set d [java::new {ptolemy.data.DoubleToken double} 12.0]

    set res1 [$p1 subtract $d]
    set res2 [$p1 subtractReverse $d]

    set res3 [$d subtract $p1]

    list [$res1 toString] [$res2 toString] [$res3 toString] 
} {{-6.0 + 7.0i} {6.0 - 7.0i} {6.0 - 7.0i}}

######################################################################
####
# 
test ComplexToken-9.0 {test isLessThan} {
    set cc67 [java::new {ptolemy.math.Complex double double} 6.0 7.0]
    set c67 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $cc67]
    set cc34 [java::new {ptolemy.math.Complex double double} 3.0 4.0]
    set c34 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $cc34]

    catch {$c67 isLessThan $c34} msg

    list $msg
} {{ptolemy.kernel.util.IllegalActionException: isLessThan operation not supported between ptolemy.data.ComplexToken '6.0 + 7.0i' and ptolemy.data.ComplexToken '3.0 + 4.0i' because complex numbers cannot be compared.}}

######################################################################
####
# 
test ComplexToken-10.0 {Test equals} {
    set c1 [java::new {ptolemy.math.Complex double double} 6.0 7.0]
    set t1 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c1]
    set c2 [java::new {ptolemy.math.Complex double double} 6.0 7.0]
    set t2 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c2]
    set c3 [java::new {ptolemy.math.Complex double double} 1.0 2.0]
    set t3 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c3]
    list [$t1 equals $t1] [$t1 equals $t2] [$t1 equals $t3]
} {1 1 0}

######################################################################
####
# 
test ComplexToken-11.0 {Test hashCode} {
    set c1 [java::new {ptolemy.math.Complex double double} 6.0 7.0]
    set t1 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c1]
    set c2 [java::new {ptolemy.math.Complex double double} 6.0 7.0]
    set t2 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c2]
    set c3 [java::new {ptolemy.math.Complex double double} 1.0 2.0]
    set t3 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c3]
    list [$t1 hashCode] [$t2 hashCode] [$t3 hashCode]
} {9 9 2}

######################################################################
####
# 
test ComplexToken-13.0 {Test convert from BooleanToken} {
    set t [java::new {ptolemy.data.BooleanToken boolean} false]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.ComplexToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.BooleanToken 'false' to the type complex because the type of the token is higher or incomparable with the given type.}}

test ComplexToken-13.1 {Test convert from UnsignedByteToken} {
    set t [java::new {ptolemy.data.UnsignedByteToken byte} 1]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.ComplexToken convert $t] toString]} msg
    list $msg
} {{1.0 + 0.0i}}

test ComplexToken-13.2 {Test convert from ComplexToken} {
    set o [java::new {ptolemy.math.Complex} 1.0 1.0]
    set t [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $o]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.ComplexToken convert $t] toString]} msg
    list $msg
} {{1.0 + 1.0i}}

test ComplexToken-13.3 {Test convert from DoubleToken} {
    set t [java::new {ptolemy.data.DoubleToken double} 1.0]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.ComplexToken convert $t] toString]} msg
    list $msg
} {{1.0 + 0.0i}}

test ComplexToken-13.4 {Test convert from FixToken} {
    set t [java::new {ptolemy.data.FixToken java.lang.String} "fix(1.0,8,4)"]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.ComplexToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.FixToken 'fix(1.0,8,4)' to the type complex because the type of the token is higher or incomparable with the given type.}}

test ComplexToken-13.5 {Test convert from IntToken} {
    set t [java::new {ptolemy.data.IntToken int} 1]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.ComplexToken convert $t] toString]} msg
    list $msg
} {{1.0 + 0.0i}}

test ComplexToken-13.6 {Test convert from LongToken} {
    set t [java::new {ptolemy.data.LongToken long} 1]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.ComplexToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.LongToken '1L' to the type complex because the type of the token is higher or incomparable with the given type.}}

test ComplexToken-13.7 {Test convert from StringToken} {
    set t [java::new {ptolemy.data.StringToken java.lang.String} "One"]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.ComplexToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.StringToken '"One"' to the type complex because the type of the token is higher or incomparable with the given type.}}
    
######################################################################
####
# 
test BooleanToken-14.1 {Test bitwise ops} {
    set token [java::new ptolemy.data.ComplexToken]
    catch {$token bitwiseAnd $token} errMsg
    catch {$token bitwiseOr $token} errMsg1
    catch {$token bitwiseXor $token} errMsg2
    catch {$token bitwiseNot} errMsg3
    list "$errMsg\n $errMsg1\n $errMsg2\n $errMsg3"
} {{ptolemy.kernel.util.IllegalActionException: bitwiseAnd operation not supported between ptolemy.data.ComplexToken '0.0 + 0.0i' and ptolemy.data.ComplexToken '0.0 + 0.0i'
 ptolemy.kernel.util.IllegalActionException: bitwiseOr operation not supported between ptolemy.data.ComplexToken '0.0 + 0.0i' and ptolemy.data.ComplexToken '0.0 + 0.0i'
 ptolemy.kernel.util.IllegalActionException: bitwiseXor operation not supported between ptolemy.data.ComplexToken '0.0 + 0.0i' and ptolemy.data.ComplexToken '0.0 + 0.0i'
 ptolemy.kernel.util.IllegalActionException: bitwiseNot operation not supported between ptolemy.data.ComplexToken '0.0 + 0.0i' and ptolemy.data.ComplexToken '0.0 + 0.0i'}}


