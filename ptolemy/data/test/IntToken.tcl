# Tests for the IntToken class
#
# @Author: Neil Smyth, Yuhong Xiong, contributor: Christopher Brooks
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
test IntToken-1.0 {Create an empty instance} {
    set p [java::new ptolemy.data.IntToken]
    $p toString
} {0}

######################################################################
####
# 
test IntToken-1.1 {Create a non-empty instance from an int} {
    set p [java::new {ptolemy.data.IntToken int} 5]
    $p toString
} {5}

######################################################################
####
# 
test IntToken-1.2 {Create a non-empty instance from an String} {
    set p [java::new {ptolemy.data.IntToken String} "7"]
    $p toString
} {7}

######################################################################
####
# 
test IntToken-1.3 {NIL} { 
    set nil [java::field ptolemy.data.IntToken NIL]
    list [$nil toString]
} {nil}

######################################################################
####
# 
test IntToken-1.5 {Create a nil Token from a null token} {
    catch {java::new ptolemy.data.IntToken [java::null]} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Creating a nil token with IntToken(null) is not supported.  Use IntToken.NIL, or the nil Constant.}}

######################################################################
####
# 
test IntToken-1.6 {Create a nil Token from an String} {
    catch {java::new {ptolemy.data.IntToken String} nil} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Creating a nil token with IntToken("nil") is not supported.  Use IntToken.NIL, or the nil Constant.}}


######################################################################
####
# 
test IntToken-1.7 {Create a bogus Token from a bogus String} {
    catch {java::new {ptolemy.data.IntToken String} "not-a-number"} \
	errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Failed to parse "not-a-number" as a number.
Because:
For input string: "not-a-number"}}

######################################################################
####
# 
test IntToken-2.0 {Create a non-empty instance and query its value as an int} {
    set p [java::new {ptolemy.data.IntToken int} 3]
    set res1 [$p intValue]
    list $res1
} {3}

######################################################################
####
# 
test IntToken-2.1 {Create a non-empty instance and query its value as a double} {
    set p [java::new {ptolemy.data.IntToken int} 12]
    $p doubleValue
} {12.0}

######################################################################
####
# 
test IntToken-2.2 {Create a non-empty instance and query its value as a long} {
    set p [java::new {ptolemy.data.IntToken int} 12]
    $p longValue
} {12}

######################################################################
####
# 
test IntToken-2.3 {Create a non-empty instance and query its value as a string} {
    set p [java::new {ptolemy.data.IntToken int} 12]
    $p toString
} {12}

######################################################################
####
#
test IntToken-2.4 {Create a non-empty instance and query its value as a complex#} {
    set p [java::new {ptolemy.data.IntToken int} 12]
    [$p complexValue] toString
} {12.0 + 0.0i}

######################################################################
####
# 
test IntToken-2.5 {Test additive identity} {
    set p [java::new {ptolemy.data.IntToken int} 7]
    set token [$p zero]

    list [$token toString]
} {0}
######################################################################
####
# 
test IntToken-2.6 {Test multiplicative identity} {
    set p [java::new {ptolemy.data.IntToken int} 7]
    set token [$p one]

    list [$token toString]
} {1}

######################################################################
####
# Test addition of ints to Token types below it in the lossless 
# type hierarchy, and with other ints.
test IntToken-3.0 {Test adding ints.} {
    set p [java::new {ptolemy.data.IntToken int} 7]
    set res1 [$p add $p]
    set res2 [$p addReverse $p]

    list [$res1 toString] [$res2 toString]
} {14 14}

######################################################################
####
# Test division of ints with Token types below it in the lossless 
# type hierarchy, and with other ints. Note that dividing ints could 
# give a double.
test IntToken-4.0 {Test dividing ints.} {
    set tok1 [java::new {ptolemy.data.IntToken int} 7]
    set tok2 [java::new {ptolemy.data.IntToken int} 14]
 
    set res1 [$tok1 divide $tok1]
    set res2 [$tok1 divideReverse $tok1]

    set res3 [$tok1 divide $tok2]
    set res4 [$tok1 divideReverse $tok2]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString]
} {1 1 0 2}

######################################################################
####
# Test isEqualTo operator applied to other ints and Tokens types 
# below it in the lossless type hierarchy.
test IntToken-5.0 {Test equality between ints.} {
    set tok1 [java::new {ptolemy.data.IntToken int} 7]
    set tok2 [java::new {ptolemy.data.IntToken int} 4]

    set res1 [$tok1 {isEqualTo ptolemy.data.Token} $tok1]
    set res2 [$tok1 {isEqualTo ptolemy.data.Token} $tok2]

    list [$res1 toString] [$res2 toString]
} {true false}

######################################################################
####
# Test modulo operator between ints and ints.
test IntToken-6.0 {Test modulo between ints.} {
    set tok1 [java::new {ptolemy.data.IntToken int} 7]
    set tok2 [java::new {ptolemy.data.IntToken int} 2]

    set res1 [$tok1 modulo $tok2]
    set res2 [$tok1 moduloReverse $tok2]

    list [$res1 toString] [$res2 toString]
} {1 2}

######################################################################
####
# Test multiply operator between ints and ints.
test intToken-7.0 {Test multiply operator between ints.} {
    set tok1 [java::new {ptolemy.data.IntToken int} 7]
    set tok2 [java::new {ptolemy.data.IntToken int} 2]

    set res1 [$tok1 multiply $tok2]
    set res2 [$tok1 multiplyReverse $tok2]

    list [$res1 toString] [$res2 toString]
} {14 14}

######################################################################
####
# Test subtract operator between ints and ints.
test IntToken-8.0 {Test subtract operator between ints.} {
    set tok1 [java::new {ptolemy.data.IntToken int} 7]
    set tok2 [java::new {ptolemy.data.IntToken int} 2]

    set res1 [$tok1 subtract $tok2]
    set res2 [$tok1 subtractReverse $tok2]

    list [$res1 toString] [$res2 toString]
} {5 -5}

######################################################################
####
# Test shift operator between ints and ints.
test IntToken-8.1 {Test shift operator between ints.} {
    set tok1 [java::new {ptolemy.data.IntToken int} 7]
    set tok2 [java::new {ptolemy.data.IntToken int} -7]

    set res1 [$tok1 leftShift 1]
    set res2 [$tok2 leftShift 1]
    set res3 [$tok1 rightShift 1]
    set res4 [$tok2 rightShift 1]
    set res5 [$tok1 logicalRightShift 1]
    set res6 [$tok2 logicalRightShift 1]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] [$res5 toString] [$res6 toString]
} {14 -14 3 -4 3 2147483644}

######################################################################
####
# Test shift operator between NIL unsigned bytes
test UnsignedByteToken-8.1 {Test shift operator between ints.} {
    set nil [java::field ptolemy.data.IntToken NIL]

    set res1 [$nil leftShift 1]
    set res2 [$nil rightShift 1]
    set res3 [$nil logicalRightShift 1]

    list [$res1 isNil] [$res2 isNil] [$res3 isNil]
} {1 1 1}


######################################################################
####
# Do not really need this test, but leave in for now.
test IntToken-9.0 {Create an non-empty instance and add it to Strings} {
    set token1 [java::new ptolemy.data.StringToken "value is " ]
    set token2 [java::new {ptolemy.data.IntToken int} 23]
    set token3 [java::new ptolemy.data.StringToken "....." ]

    set token4 [$token1 add $token2]
    set token5 [$token2 add $token3]
    
    set token6 [$token4 add $token5]

    list [$token6 toString]
} {{"value is 2323....."}}

######################################################################
####
# test isLessThan
test IntToken-10.0 {test isLessThan} {
    set i2 [java::new ptolemy.data.IntToken 2]
    set i3 [java::new ptolemy.data.IntToken 3]
    set d2 [java::new ptolemy.data.DoubleToken 2.0]
    set d3 [java::new ptolemy.data.DoubleToken 3.0]

    list [[$i2 isLessThan $i3] booleanValue] \
         [[$i2 isLessThan $d2] booleanValue] \
	 [[$i2 isLessThan $d3] booleanValue] \
         [[$i3 isLessThan $i2] booleanValue] \
         [[$d2 isLessThan $i2] booleanValue] \
	 [[$d3 isLessThan $i2] booleanValue] \
	 [[$i3 isLessThan $d2] booleanValue] \
	 [[$i3 isLessThan $d3] booleanValue] \
	 [[$d2 isLessThan $i3] booleanValue] \
	 [[$d3 isLessThan $i3] booleanValue] \
	 [[$d2 isLessThan $d3] booleanValue] \
	 [[$d3 isLessThan $d2] booleanValue]
} {1 0 1 0 0 0 0 0 1 0 1 0}

test IntToken-10.1 {test isLessThan} {
    set i2 [java::new ptolemy.data.IntToken 2]
    set i3 [java::new ptolemy.data.IntToken 3]
    set l2 [java::new ptolemy.data.LongToken 2]
    set l3 [java::new ptolemy.data.LongToken 3]

    list [[$i2 isLessThan $i3] booleanValue] \
         [[$i2 isLessThan $l2] booleanValue] \
	 [[$i2 isLessThan $l3] booleanValue] \
         [[$i3 isLessThan $i2] booleanValue] \
         [[$l2 isLessThan $i2] booleanValue] \
	 [[$l3 isLessThan $i2] booleanValue] \
	 [[$i3 isLessThan $l2] booleanValue] \
	 [[$i3 isLessThan $l3] booleanValue] \
	 [[$l2 isLessThan $i3] booleanValue] \
	 [[$l3 isLessThan $i3] booleanValue] \
	 [[$l2 isLessThan $l3] booleanValue] \
	 [[$l3 isLessThan $l2] booleanValue]
} {1 0 1 0 0 0 0 0 1 0 1 0}

######################################################################
####
# 
test IntToken-11.0 {Test equals} {
    set t1 [java::new {ptolemy.data.IntToken int} 1]
    set t2 [java::new {ptolemy.data.IntToken int} 1]
    set t3 [java::new {ptolemy.data.IntToken int} 2]
    list [$t1 equals $t1] [$t1 equals $t2] [$t1 equals $t3]
} {1 1 0}

######################################################################
####
# 
test IntToken-11.1 {Test equals on nil} {
    set u [java::field ptolemy.data.IntToken NIL]
    set u2 [java::new ptolemy.data.IntToken 2]
    set t [java::field ptolemy.data.Token NIL]
    list [$u equals $u] [$u equals $u2] [$u2 equals $u] [$t equals $u] [$u equals $t]
} {0 0 0 0 0} 

######################################################################
####
# 
test IntToken-12.0 {Test hashCode} {
    set t1 [java::new {ptolemy.data.IntToken int} 1]
    set t2 [java::new {ptolemy.data.IntToken int} 1]
    set t3 [java::new {ptolemy.data.IntToken int} 2]
    list [$t1 hashCode] [$t2 hashCode] [$t3 hashCode]
} {1 1 2}

######################################################################
####
# 
test IntToken-13.0 {Test convert from BooleanToken} {
    set t [java::new {ptolemy.data.BooleanToken boolean} false]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.IntToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.BooleanToken 'false' to the type int because the type of the token is higher or incomparable with the given type.}}

######################################################################
####
# 
test IntToken-13.1 {Test convert from UnsignedByteToken} {
    set t [java::new {ptolemy.data.UnsignedByteToken byte} 1]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.IntToken convert $t] toString]} msg
    list $msg
} {1}

######################################################################
####
# 
test IntToken-13.2 {Test convert from ComplexToken} {
    set o [java::new {ptolemy.math.Complex} 1.0 1.0]
    set t [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $o]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.IntToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.ComplexToken '1.0 + 1.0i' to the type int because the type of the token is higher or incomparable with the given type.}}


######################################################################
####
# 
test IntToken-13.3 {Test convert from DoubleToken} {
    set t [java::new {ptolemy.data.DoubleToken double} 1.0]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.IntToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.DoubleToken '1.0' to the type int because the type of the token is higher or incomparable with the given type.}}


######################################################################
####
# 
test IntToken-13.4 {Test convert from FixToken} {
    set t [java::new {ptolemy.data.FixToken java.lang.String} "fix(1.0,8,4)"]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.IntToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.FixToken 'fix(1.0,8,4)' to the type int because the type of the token is higher or incomparable with the given type.}}

######################################################################
####
# 
test IntToken-13.5 {Test convert from IntToken} {
    set t [java::new {ptolemy.data.IntToken int} 1]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.IntToken convert $t] toString]} msg
    list $msg
} {1}


######################################################################
####
# 
test IntToken-13.6 {Test convert from LongToken} {
    set t [java::new {ptolemy.data.LongToken long} 1]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.IntToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.LongToken '1L' to the type int because the type of the token is higher or incomparable with the given type.}}

######################################################################
####
# 
test IntToken-13.7 {Test convert from StringToken} {
    set t [java::new {ptolemy.data.StringToken java.lang.String} "One"]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.IntToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.StringToken '"One"' to the type int because the type of the token is higher or incomparable with the given type.}}
    
######################################################################
####
# 
test IntToken-14.0 {call byteValue and get coverage in the parent class} {
    set p [java::new {ptolemy.data.IntToken int} 5]
    catch {$p byteValue} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.IntToken '5' to the type byte.}}


######################################################################
####
# 
test IntToken-15.0 {call fixValue and get coverage in the parent class} {
    set p [java::new {ptolemy.data.IntToken int} 5]
    catch {$p fixValue} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.IntToken '5' to the type fixedpoint.}}


######################################################################
####
# 
test IntToken-20.0 {call unitsString and get coverage in the parent class} {
    set p [java::new {ptolemy.data.IntToken int} 5]
    list [$p unitsString]
} {{}}
