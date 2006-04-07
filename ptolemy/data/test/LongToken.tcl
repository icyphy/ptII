# Tests for the LongToken class
#
# @Author: Neil Smyth, contributor: Christopher Brooks
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
test LongToken-1.0 {Create an empty instance} {
    set p [java::new ptolemy.data.LongToken]
    $p toString
} {0L}

######################################################################
####
# 
test LongToken-1.1 {Create an instance with a value} {
    set token [java::new {ptolemy.data.LongToken long} 7]
    $token toString
} {7L}

######################################################################
####
# 
test LongToken-1.2 {Create an instance from a string value} {
    set token [java::new {ptolemy.data.LongToken String} "5"]
    $token toString
} {5L}

######################################################################
####
# 
test LongToken-1.2.1 {Create an instance from a string value} {
    set token [java::new {ptolemy.data.LongToken String} "5L"]
    $token toString
} {5L}

######################################################################
####
# 
test LongToken-1.3 {NIL} { 
    set nil [java::field ptolemy.data.LongToken NIL]
    list [$nil toString]
} {nil}

######################################################################
####
# 
test LongToken-1.5 {Create a nil Token from a null token} {
    catch {java::new ptolemy.data.LongToken [java::null]} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Creating a nil token with LongToken(null) is not supported.  Use LongToken.NIL, or the nil Constant.}}

######################################################################
####
# 
test LongToken-1.6 {Create a nil Token from an String} {
    catch {java::new {ptolemy.data.LongToken String} nil} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Creating a nil token with LongToken("nil") is not supported.  Use LongToken.NIL, or the nil Constant.}}


######################################################################
####
# 
test LongToken-1.7 {Create a bogus Token from a bogus String} {
    catch {java::new {ptolemy.data.LongToken String} "not-a-number"} \
	errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Failed to parse "not-a-number" as a number.
Because:
For input string: "not-a-number"}}

######################################################################
####
# 
test LongToken-1.8 {nil toString} {
    list [[java::field ptolemy.data.LongToken NIL] toString]
} {nil}

######################################################################
####
# 
test LongToken-2.0 {Create a non-empty instance and query its value as an long} {
    set p [java::new {ptolemy.data.LongToken long} 3]
    set res1 [$p longValue]
    list $res1
} {3}

######################################################################
####
# 
test LongToken-2.1 {Create a non-empty instance and query its value as a double} {
    set p [java::new {ptolemy.data.LongToken long} 12]
    catch {$p doubleValue} errmsg

    list $errmsg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.LongToken '12L' to the type double.}}

######################################################################
####
# 
test LongToken-2.2 {Create a non-empty instance and query its value as an int} {
    set p [java::new {ptolemy.data.LongToken long} 12]
    catch {$p intValue} errmsg

    list $errmsg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.LongToken '12L' to the type int.}}

######################################################################
####
# 
test LongToken-2.3 {Create a non-empty instance and query its value as a string} {
    set p [java::new {ptolemy.data.LongToken long} 12]
    $p toString
} {12L}

######################################################################
####
# 
test LongToken-2.4 {Create a non-empty instance and query its value as a complex#} {
    set p [java::new {ptolemy.data.LongToken long} 12]
    catch {$p complexValue} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.LongToken '12L' to the type Complex.}}

######################################################################
####
# 
test LongToken-2.5 {Test additive identity} {
    set p [java::new {ptolemy.data.LongToken long} 7]
    set token [$p zero]

    list [$token toString]
} {0L}
######################################################################
####
# 
test LongToken-2.6 {Test multiplicative identity} {
    set p [java::new {ptolemy.data.LongToken long} 7]
    set token [$p one]

    list [$token toString]
} {1L}

######################################################################
####
# Test addition of longs to Token types below it in the lossless 
# type hierarchy, and with other longs.
test LongToken-3.0 {Test adding longs.} {
    set p [java::new {ptolemy.data.LongToken long} 7]
    set res1 [$p add $p]
    set res2 [$p addReverse $p]

    list [$res1 toString] [$res2 toString]
} {14L 14L}
######################################################################
####
# 
test LongToken-3.1 {Test adding longs and ints.} {
    set tok1 [java::new {ptolemy.data.LongToken long} 7]
    set tok2 [java::new {ptolemy.data.IntToken int} 2]
    set res1 [$tok1 add $tok2]
    set res2 [$tok1 addReverse $tok2]

    set res3 [$tok2 add $tok1]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {9L 9L 9L}

######################################################################
####
# Test division of longs with Token types below it in the lossless 
# type hierarchy, and with other ints.
test LongToken-4.0 {Test dividing longs.} {
    set tok1 [java::new {ptolemy.data.LongToken long} 5]
    set tok2 [java::new {ptolemy.data.LongToken long} 12]
 
    set res1 [$tok1 divide $tok1]
    set res2 [$tok1 divideReverse $tok1]

    set res3 [$tok1 divide $tok2]
    set res4 [$tok1 divideReverse $tok2]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString]
} {1L 1L 0L 2L}

######################################################################
####
# 
test LongToken-4.1 {Test dividing longs and ints.} {
    set tok1 [java::new {ptolemy.data.LongToken long} 7]
    set tok2 [java::new {ptolemy.data.IntToken int} 2]
    set res1 [$tok1 divide $tok2]
    set res2 [$tok1 divideReverse $tok2]

    set res3 [$tok2 divide $tok1]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {3L 0L 0L}

######################################################################
####
# Test isEqualTo operator applied to other longs and Tokens types 
# below it in the lossless type hierarchy.
test LongToken-5.0 {Test equality between longs.} {
    set tok1 [java::new {ptolemy.data.LongToken long} 7]
    set tok2 [java::new {ptolemy.data.LongToken long} 2]

    set res1 [$tok1 {isEqualTo ptolemy.data.Token} $tok1]
    set res2 [$tok1 {isEqualTo ptolemy.data.Token} $tok2]

    list [$res1 toString] [$res2 toString]
} {true false}
######################################################################
####
# 
test LongToken-5.1 {Test equality between longs and ints.} {
    set tok1 [java::new {ptolemy.data.LongToken long} 12]
    set tok2 [java::new {ptolemy.data.IntToken int} 12]
    set tok3 [java::new {ptolemy.data.LongToken long} 2]
    set tok4 [java::new {ptolemy.data.IntToken int} 2]

    set res1 [$tok1 {isEqualTo ptolemy.data.Token} $tok2]
    set res2 [$tok1 {isEqualTo ptolemy.data.Token} $tok4]

    set res3 [$tok2 {isEqualTo ptolemy.data.Token} $tok1]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {true false true}

######################################################################
####
# Test modulo operator between longs and ints.
test LongToken-6.0 {Test modulo between longs.} {
    set tok1 [java::new {ptolemy.data.LongToken long} 7]
    set tok2 [java::new {ptolemy.data.LongToken long} 2]

    set res1 [$tok1 modulo $tok2]
    set res2 [$tok1 moduloReverse $tok2]

    list [$res1 toString] [$res2 toString]
} {1L 2L}
######################################################################
####
# 
test LongToken-6.1 {Test modulo operator between longs and ints.} {
    set tok1 [java::new {ptolemy.data.LongToken long} 7]
    set tok2 [java::new {ptolemy.data.IntToken int} 3]
    
    set res1 [$tok1 modulo $tok2]
    set res2 [$tok1 moduloReverse $tok2]

    set res3 [$tok2 modulo $tok1]
   
    list [$res1 toString] [$res2 toString] [$res3 toString] 
} {1L 3L 3L}

######################################################################
####
# Test multiply operator between longs and ints.
test longToken-7.0 {Test multiply operator between longs.} {
    set tok1 [java::new {ptolemy.data.LongToken long} 7]
    set tok2 [java::new {ptolemy.data.LongToken long} 2]

    set res1 [$tok1 multiply $tok2]
    set res2 [$tok1 multiplyReverse $tok2]

    list [$res1 toString] [$res2 toString]
} {14L 14L}
######################################################################
####
# 
test LongToken-7.1 {Test multiply operator between longs and ints.} {
    set tok1 [java::new {ptolemy.data.LongToken long} 7]
    set tok2 [java::new {ptolemy.data.IntToken int} 3]
    
    set res1 [$tok1 multiply $tok2]
    set res2 [$tok1 multiplyReverse $tok2]

    set res3 [$tok2 multiply $tok1]
   
    list [$res1 toString] [$res2 toString] [$res3 toString] 
} {21L 21L 21L}


######################################################################
####
# Test subtract operator between longs and ints.
test LongToken-8.0 {Test subtract operator between longs.} {
    set tok1 [java::new {ptolemy.data.LongToken long} 7]
    set tok2 [java::new {ptolemy.data.LongToken long} 2]

    set res1 [$tok1 subtract $tok2]
    set res2 [$tok1 subtractReverse $tok2]

    list [$res1 toString] [$res2 toString]
} {5L -5L}
######################################################################
####
# 
test LongToken-8.1.1 {Test subtract operator between longs and ints.} {
    set tok1 [java::new {ptolemy.data.LongToken long} 7]
    set tok2 [java::new {ptolemy.data.IntToken int} 3]
    
    set res1 [$tok1 subtract $tok2]
    set res2 [$tok1 subtractReverse $tok2]

    set res3 [$tok2 subtract $tok1]
   
    list [$res1 toString] [$res2 toString] [$res3 toString] 
} {4L -4L -4L}

######################################################################
####
# 
test LongToken-8.2 {leftShift} {
    set p [java::new ptolemy.data.LongToken 2]
    set p2 [java::new ptolemy.data.LongToken [java::field Long MAX_VALUE]]
    set nil [java::field ptolemy.data.LongToken NIL]
    list [[$p leftShift 1] toString] \
	[[$p2 leftShift 1] toString] \
	[[$nil leftShift 1] isNil]
} {4L -2L 1}

######################################################################
####
# 
test LongToken-8.3 {logicalRightShift} {
    # Uses 8.2 above
    list [[$p logicalRightShift 1] toString] \
	[[$p logicalRightShift 1] toString] \
	[[$nil logicalRightShift 1] isNil]
} {1L 1L 1}


######################################################################
####
# Test shift operator between NIL unsigned bytes
test LongToken-8.4 {Test shift operator between ints.} {
    set nil [java::field ptolemy.data.LongToken NIL]

    set res1 [$nil leftShift 1]
    set res2 [$nil rightShift 1]
    set res3 [$nil logicalRightShift 1]

    list [$res1 isNil] [$res2 isNil] [$res3 isNil]
} {1 1 1}



######################################################################
####
# Do not really need this test, but leave in for now.
test LongToken-9.0 {Create an non-empty instance and add it to Strings} {
    set token1 [java::new ptolemy.data.StringToken "value is " ]
    set token2 [java::new {ptolemy.data.LongToken long} 23]
    set token3 [java::new ptolemy.data.StringToken "....." ]

    set token4 [$token1 add $token2]
    set token5 [$token2 add $token3]
    
    set token6 [$token4 add $token5]

    list [$token6 toString]
} {{"value is 23L23L....."}}

######################################################################
####
# 
test LongToken-11.0 {Test equals} {
    set t1 [java::new {ptolemy.data.LongToken long} 1]
    set t2 [java::new {ptolemy.data.LongToken long} 1]
    set t3 [java::new {ptolemy.data.LongToken long} 2]
    list [$t1 equals $t1] [$t1 equals $t2] [$t1 equals $t3]
} {1 1 0}


######################################################################
####
# 
test LongToken-11.1 {Test equals on nil} {
    set tu [java::field ptolemy.data.LongToken NIL]
    set t2 [java::new ptolemy.data.LongToken 2]
    set t [java::field ptolemy.data.Token NIL]
    list [$tu equals $tu] [$tu equals $t2] [$t2 equals $tu] \
	[$t2 equals $t2] [$t equals $tu] [$tu equals $t]
} {0 0 0 1 0 0} 

######################################################################
####
# 
test LongToken-12.0 {Test hashCode} {
    set t1 [java::new {ptolemy.data.LongToken long} 1]
    set t2 [java::new {ptolemy.data.LongToken long} 1]
    set t3 [java::new {ptolemy.data.LongToken long} 2]
    list [$t1 hashCode] [$t2 hashCode] [$t3 hashCode]
} {1 1 2}

######################################################################
####
# 
test LongToken-13.0 {Test convert from BooleanToken} {
    set t [java::new {ptolemy.data.BooleanToken boolean} false]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.LongToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.BooleanToken 'false' to the type long because the type of the token is higher or incomparable with the given type.}}

test LongToken-13.1 {Test convert from UnsignedByteToken} {
    set t [java::new {ptolemy.data.UnsignedByteToken byte} 1]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.LongToken convert $t] toString]} msg
    list $msg
} {1L}

test LongToken-13.2 {Test convert from ComplexToken} {
    set o [java::new {ptolemy.math.Complex} 1.0 1.0]
    set t [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $o]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.LongToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.ComplexToken '1.0 + 1.0i' to the type long because the type of the token is higher or incomparable with the given type.}}

test LongToken-13.3 {Test convert from DoubleToken} {
    set t [java::new {ptolemy.data.DoubleToken double} 1.0]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.LongToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.DoubleToken '1.0' to the type long because the type of the token is higher or incomparable with the given type.}}

test LongToken-13.4 {Test convert from FixToken} {
    set t [java::new {ptolemy.data.FixToken java.lang.String} "fix(1.0,8,4)"]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.LongToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.FixToken 'fix(1.0,8,4)' to the type long because the type of the token is higher or incomparable with the given type.}}

test LongToken-13.5 {Test convert from IntToken} {
    set t [java::new {ptolemy.data.IntToken int} 1]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.LongToken convert $t] toString]} msg
    list $msg
} {1L}

test LongToken-13.6 {Test convert from LongToken} {
    set t [java::new {ptolemy.data.LongToken long} 1]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.LongToken convert $t] toString]} msg
    list $msg
} {1L}

test LongToken-13.7 {Test convert from StringToken} {
    set t [java::new {ptolemy.data.StringToken java.lang.String} "One"]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.LongToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.StringToken '"One"' to the type long because the type of the token is higher or incomparable with the given type.}}
    
######################################################################
####
# 
test LongToken-19.0 {truncatedUnsignedByteValue} {
    set p2 [java::new ptolemy.data.LongToken [java::field Long MAX_VALUE]]
    catch {[[$p2 truncatedUnsignedByteValue] toString]} errMsg
    list [[$p truncatedUnsignedByteValue] toString] \
	$errMsg \
	[[$nil truncatedUnsignedByteValue] isNil]
} {2ub {ptolemy.kernel.util.IllegalActionException: Value cannot be represented as an unsigned Byte} 1}
