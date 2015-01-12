# Tests for the SmoothToken class
#
# @Author: Edward A. Lee, contributor: Christopher Brooks.  Based on DoubleToken.tcl by Neil Smyth.
#
# @Version $Id$
#
# @Copyright (c) 1997-2015 The Regents of the University of California.
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
test SmoothToken-1.0 {Create an empty instance} {
    set s0 [java::new ptolemy.data.SmoothToken 0.0]
    $s0 toString
} {0.0}

######################################################################
####
# 
test SmoothToken-1.1 {Create a single derivative} {
    set d [java::new {double[]} {1} {1.0}]
    set s1 [java::new ptolemy.data.SmoothToken 1.0 $d]
    $s1 toString
} {smoothToken(1.0, {1.0})}

######################################################################
####
# 
test SmoothToken-1.2 {Test adding a double} {
    set d1 [java::new ptolemy.data.DoubleToken 1.0]
    set result [$s1 add $d1]
    $result toString
} {smoothToken(2.0, {1.0})}

######################################################################
####
# 
test SmoothToken-1.3 {Test adding in reverse} {
    set result [$d1 add $s1]
    $result toString
} {smoothToken(2.0, {1.0})}

######################################################################
####
# 
test SmoothToken-1.4 {Test subtracting a double} {
    set result [$s1 subtract $d1]
    $result toString
} {smoothToken(0.0, {1.0})}

######################################################################
####
# 
test SmoothToken-1.5 {Test subtracting in reverse} {
    set result [$d1 subtract $s1]
    $result toString
} {smoothToken(0.0, {-1.0})}

######################################################################
####
# 
test SmoothToken-1.6 {Test adding two SmoothTokens of different lengths} {
    set derivatives [java::new {double[]} {2} {2.0 3.0}]
    set s2 [java::new ptolemy.data.SmoothToken 2.0 $derivatives]
    set result [$s1 add $s2]
    $result toString
} {smoothToken(3.0, {3.0,3.0})}

######################################################################
####
# 
test SmoothToken-1.7 {Test adding in reverse} {
    set result [$s2 add $s1]
    $result toString
} {smoothToken(3.0, {3.0,3.0})}

######################################################################
####
# 
test SmoothToken-1.8 {Test subtracting two SmoothTokens of different lengths} {
    set result [$s1 subtract $s2]
    $result toString
} {smoothToken(-1.0, {-1.0,-3.0})}

######################################################################
####
# 
test SmoothToken-1.9 {Test subtracting in reverse} {
    set result [$s2 subtract $s1]
    $result toString
} {smoothToken(1.0, {1.0,3.0})}

######################################################################
####
# 
test SmoothToken-1.10 {Test multiplying a double} {
    set d2 [java::new ptolemy.data.DoubleToken 2.0]
    set result [$s1 multiply $d2]
    $result toString
} {smoothToken(2.0, {2.0})}

######################################################################
####
# 
test SmoothToken-1.11 {Test multiplying in reverse} {
    set result [$d2 multiply $s1]
    $result toString
} {smoothToken(2.0, {2.0})}

######################################################################
####
# 
test SmoothToken-1.8 {Test multiplying two SmoothTokens of different lengths} {
    # s1 is smoothToken(1.0, {1.0})
    # s2 is smoothToken(2.0, {2.0, 3.0})
    set result [$s1 multiply $s2]
    $result toString
} {smoothToken(2.0, {4.0,7.0})}




# Tests below here are based on data/test/DoubleToken.tcl by Neil Smyth.
# There is some duplicateion of the above.


######################################################################
####
# 
test SmoothToken-1b.1 {Create a non-empty instance from an double} {
    set p [java::new {ptolemy.data.SmoothToken double} 5.5]
    $p toString
} {5.5}

######################################################################
####
# 
test SmoothToken-1b.2 {Create a non-empty instance from an String} {
    set p [java::new {ptolemy.data.SmoothToken String} "7.77"]
    $p toString
} {7.77}

######################################################################
####
# 
test SmoothToken-1b.3.1 {NIL} { 
    set nil [java::field ptolemy.data.SmoothToken NIL]
    list [$nil toString]
} {nil}

######################################################################
####
# 
test SmoothToken-1b.3 {Create a non-empty instance from an String} {
    set p [java::new {ptolemy.data.SmoothToken String} "7.56E-10"]
    $p toString
} {7.56E-10}

######################################################################
####
# 
test SmoothToken-1b.4 {Create a non-empty instance from an String} {
    set p [java::new {ptolemy.data.SmoothToken String} "7.56E10"]
    $p toString
} {7.56E10}

######################################################################
####
# 
test SmoothToken-1b.5 {Create a nil Token from a null token} {
    catch {java::new ptolemy.data.SmoothToken [java::null]} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Creating a nil token with SmoothToken(null) is not supported.  Use SmoothToken.NIL, or the nil Constant.}}

######################################################################
####
# 
test SmoothToken-1b.6 {Create a nil Token from an String} {
    catch {java::new {ptolemy.data.SmoothToken String} nil} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Creating a nil token with SmoothToken("nil") is not supported.  Use SmoothToken.NIL, or the nil Constant.}}

######################################################################
####
# 
test SmoothToken-1b.7 {Create a bogus Token from a bogus String} {
    catch {java::new {ptolemy.data.SmoothToken String} "not-a-number"} \
	errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Failed to parse "not-a-number" as a number.
Because:
For input string: "not-a-number"}}

######################################################################
####
# 
test SmoothToken-2.0 {Create a non-empty instance and query its value as a Complex} {
    set p [java::new {ptolemy.data.SmoothToken double} 3.3]
    set res [$p complexValue]
    list [$res toString]
} {{3.3 + 0.0i}}

######################################################################
####
# 
test SmoothToken-2.1 {Create a non-empty instance and query its value as a double} {
    set p [java::new {ptolemy.data.SmoothToken double} 3.3]
    set res1 [$p doubleValue]
    list $res1
} {3.3}

######################################################################
####
# 
test SmoothToken-2.2 {Create a non-empty instance and query its value as an int} {
    set p [java::new {ptolemy.data.SmoothToken double} 12]
    catch {$p intValue} errmsg

    list $errmsg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.SmoothToken '12.0' to the type int.}}

######################################################################
####
# 
test SmoothToken-2.3 {Create a non-empty instance and query its value as a long} {
    set p [java::new {ptolemy.data.SmoothToken double} 12]
   catch {$p longValue} errmsg

    list $errmsg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.SmoothToken '12.0' to the type long.}}

######################################################################
####
# 
test SmoothToken-2.4 {Create a non-empty instance and query its value as a string} {
    set p [java::new {ptolemy.data.SmoothToken double} 12.2]
    $p toString
} {12.2}

######################################################################
####
# 
test SmoothToken-2.5 {Test additive identity} {
    set p [java::new {ptolemy.data.SmoothToken double} 12.2]
    set token [$p zero]

    list [$token toString]
} {0.0}
######################################################################
####
# 
test SmoothToken-2.6 {Test multiplicative identity} {
    set p [java::new {ptolemy.data.SmoothToken double} 12.2]
    set token [$p one]

    list [$token toString]
} {1.0}

######################################################################
####
# Test addition of doubles to Token types below it in the lossless 
# type hierarchy, and with other doubles.
test SmoothToken-3.0 {Test adding doubles.} {
    set p [java::new {ptolemy.data.SmoothToken double} 12.2]
    set res1 [$p add $p]
    set res2 [$p addReverse $p]

    list [$res1 toString] [$res2 toString]
} {24.4 24.4}
######################################################################
####
# 
test SmoothToken-3.1 {Test adding doubles and ints.} {
    set tok1 [java::new {ptolemy.data.SmoothToken double} 12.2]
    set tok2 [java::new {ptolemy.data.IntToken int} 2]
    set res1 [$tok1 add $tok2]
    set res2 [$tok1 addReverse $tok2]

    set res3 [$tok2 add $tok1]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {14.2 14.2 14.2}

######################################################################
####
# Test division of doubles with Token types below it in the lossless 
# type hierarchy, and with other doubles.
test SmoothToken-4.0 {Test dividing doubles.} {
    set p [java::new {ptolemy.data.SmoothToken double} 12.2]
    set res1 [$p divide $p]
    set res2 [$p divideReverse $p]

    list [$res1 toString] [$res2 toString]
} {1.0 1.0}
######################################################################
####
# 
test SmoothToken-4.1 {Test dividing doubles and ints.} {
    set tok1 [java::new {ptolemy.data.SmoothToken double} 12.2]
    set tok2 [java::new {ptolemy.data.IntToken int} 2]
    set res1 [$tok1 divide $tok2]
    set resultToken [java::new {ptolemy.data.SmoothToken double} \
	    0.1639344262295]
    set res2 [[$tok1 divideReverse $tok2] isCloseTo $resultToken]

    set res3 [[$tok2 divide $tok1] isCloseTo $resultToken]
 
    list [$res1 toString] [$res2 toString] [$res3 toString]
} {6.1 true true}

######################################################################
####
# Test isEqualTo operator applied to other doubles and Tokens types 
# below it in the lossless type hierarchy.
test SmoothToken-5.0 {Test equality between doubles.} {
    set tok1 [java::new {ptolemy.data.SmoothToken double} 12.2]
    set tok2 [java::new {ptolemy.data.SmoothToken double} 2.2]

    set res1 [$tok1 {isEqualTo ptolemy.data.Token} $tok1]
    set res2 [$tok1 {isEqualTo ptolemy.data.Token} $tok2]

    list [$res1 toString] [$res2 toString]
} {true false}

######################################################################
####
# 
test SmoothToken-5.1 {Test equality between doubles and ints.} {
    set tok1 [java::new {ptolemy.data.SmoothToken double} 12]
    set tok2 [java::new {ptolemy.data.IntToken int} 12]
    set tok3 [java::new {ptolemy.data.SmoothToken double} 2]
    set tok4 [java::new {ptolemy.data.IntToken int} 2]

    set res1 [$tok1 {isEqualTo ptolemy.data.Token} $tok2]
    set res2 [$tok1 {isEqualTo ptolemy.data.Token} $tok4]

    set res3 [$tok2 {isEqualTo ptolemy.data.Token} $tok1]

    # For DoubleTokens, which are in the type lattice, this would return
    # {true false true}
    list [$res1 toString] [$res2 toString] [$res3 toString]
} {false false false}

######################################################################
####
# Test isCloseTo operator applied to other doubles and Tokens types 
# below it in the lossless type hierarchy.
test SmoothToken-5.5 {Test closeness between doubles. \
    This test should be the same as the similar SmoothToken-5.0 \
    isEquals test. \
} {
    set tok1 [java::new {ptolemy.data.SmoothToken double} 12.2]
    set tok2 [java::new {ptolemy.data.SmoothToken double} 2.2]

    set res1 [$tok1 {isCloseTo ptolemy.data.Token} $tok1]
    set res2 [$tok1 {isCloseTo ptolemy.data.Token} $tok2]

    list [$res1 toString] [$res2 toString]
} {true false}
######################################################################
####
# 
test SmoothToken-5.6 {Test closeness between doubles and ints. \
    This test should be the same as the similar SmoothToken-5.0 \
    isEquals test. \
} {
    set tok1 [java::new {ptolemy.data.SmoothToken double} 12]
    set tok2 [java::new {ptolemy.data.IntToken int} 12]
    set tok3 [java::new {ptolemy.data.SmoothToken double} 2]
    set tok4 [java::new {ptolemy.data.IntToken int} 2]

    set res1 [$tok1 {isCloseTo ptolemy.data.Token} $tok2]
    set res2 [$tok1 {isCloseTo ptolemy.data.Token} $tok4]

    set res3 [$tok2 {isCloseTo ptolemy.data.Token} $tok1]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {true false true}

######################################################################
####
# 
test SmoothToken-5.7 {Test closeness between doubles} {
    set epsilon 0.001
    set oldEpsilon [java::field ptolemy.math.Complex EPSILON]
    java::field ptolemy.math.Complex EPSILON $epsilon

    set token1 [java::new {ptolemy.data.SmoothToken double} 12.0]
    set notCloseToken1 [java::new {ptolemy.data.SmoothToken double} \
	    [expr {12.0 + $epsilon*100.0} ] ]
    set closeToken1 [java::new {ptolemy.data.SmoothToken double} \
	    [expr {12.0 - (0.9 * $epsilon)}] ]

    #puts "5.7: $epsilon [$token1 toString] [$notCloseToken1 toString] \
    #	    [$closeToken1 toString]"
    set res1 [$token1 {isCloseTo ptolemy.data.Token} $notCloseToken1]
    set res2 [$token1 {isCloseTo ptolemy.data.Token} $closeToken1]
    set res3 [$notCloseToken1 {isCloseTo ptolemy.data.Token} $token1]
    set res4 [$closeToken1 {isCloseTo ptolemy.data.Token} $token1]

    java::field ptolemy.math.Complex EPSILON $oldEpsilon

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString]
} {false true false true}

test SmoothToken-5.8 {Test closeness between doubles around 0} {
    set epsilon 0.001
    set oldEpsilon [java::field ptolemy.math.Complex EPSILON]
    java::field ptolemy.math.Complex EPSILON $epsilon

    set token1 [java::new {ptolemy.data.SmoothToken double} 0.0]
    set notCloseToken1 [java::new {ptolemy.data.SmoothToken double} \
	    [expr {0.0 + 1.1 * $epsilon} ] ]
    set anotherNotCloseToken1 [java::new {ptolemy.data.SmoothToken double} \
	    [expr {0.0 - 1.1 * $epsilon} ] ]
    set closeToken1 [java::new {ptolemy.data.SmoothToken double} \
	    [expr {0.0 - 0.9 * $epsilon}] ]

    set res1 [$token1 {isCloseTo ptolemy.data.Token} $notCloseToken1]
    set res2 [$token1 {isCloseTo ptolemy.data.Token} $anotherNotCloseToken1]
    set res3 [$token1 {isCloseTo ptolemy.data.Token} $closeToken1]
    set res4 [$notCloseToken1 {isCloseTo ptolemy.data.Token} $token1]
    set res5 [$anotherNotCloseToken1 {isCloseTo ptolemy.data.Token} $token1]
    set res6 [$closeToken1 {isCloseTo ptolemy.data.Token} $token1]

    java::field ptolemy.math.Complex EPSILON $oldEpsilon

    list [$res1 toString] [$res2 toString] [$res3 toString] \
	    [$res4 toString] [$res5 toString] [$res6 toString]
} {false false true false false true}

test SmoothToken-5.9 {Test closeness between a double and a String} {
    set doubleToken [java::new {ptolemy.data.SmoothToken double} 12.0]
    set stringToken [java::new ptolemy.data.StringToken "12.0"]
    catch {[$doubleToken {isCloseTo ptolemy.data.Token} $stringToken] toString} errMsg1
    catch {[$stringToken {isCloseTo ptolemy.data.Token} $doubleToken] toString} errMsg2
    list [lrange $errMsg2 0 10] [lrange $errMsg2 0 10]
} {true true}

test SmoothToken-5.10 {Test closeness between doubles and ints.} {
    set epsilon 0.001
    set oldEpsilon [java::field ptolemy.math.Complex EPSILON]
    java::field ptolemy.math.Complex EPSILON $epsilon

    set tok1 [java::new {ptolemy.data.SmoothToken double} \
	      [expr {12.0 + 0.5 * $epsilon} ]]
    set tok2 [java::new {ptolemy.data.IntToken int} 12]

    set res1 [$tok1 {isCloseTo ptolemy.data.Token} $tok2]
    set res2 [$tok2 {isCloseTo ptolemy.data.Token} $tok1]

    java::field ptolemy.math.Complex EPSILON $oldEpsilon

    list [$res1 toString] [$res2 toString]
} {true true}


######################################################################
####
# Test modulo operator between doubles and ints.
test SmoothToken-6.0 {Test modulo between doubles.} {
    set tok1 [java::new {ptolemy.data.SmoothToken double} 12.2]
    set tok2 [java::new {ptolemy.data.SmoothToken double} 2.2]

    set res1 [$tok1 modulo $tok1]
    set res2 [$tok1 moduloReverse $tok2]

    list [$res1 toString] [$res2 toString]
} {0.0 2.2}
######################################################################
####
# 
test SmoothToken-6.1 {Test modulo operator between doubles and ints.} {
    set tok1 [java::new {ptolemy.data.SmoothToken double} 12.2]
    set tok2 [java::new {ptolemy.data.IntToken int} 3]
    
    set res1 [$tok1 modulo $tok2]
    set res2 [$tok1 moduloReverse $tok2]

    set res3 [$tok2 modulo $tok1]
   
    list [$res1 toString] [$res2 toString] [$res3 toString] 
} {0.2 3.0 3.0}

######################################################################
####
# Test multiply operator between doubles and ints.
test SmoothToken-7.0 {Test multiply operator between doubles.} {
    set tok1 [java::new {ptolemy.data.SmoothToken double} 12.2]
    set tok2 [java::new {ptolemy.data.SmoothToken double} 2.2]

    set res1 [$tok1 multiply $tok1]
    set res2 [$tok1 multiplyReverse $tok2]

    list [$res1 toString] [$res2 toString]
} {148.84 26.84}
######################################################################
####
# 
test SmoothToken-7.1 {Test multiply operator between doubles and ints.} {
    set tok1 [java::new {ptolemy.data.SmoothToken double} 12.2]
    set tok2 [java::new {ptolemy.data.IntToken int} 3]
    
    set res1 [$tok1 multiply $tok2]
    set res2 [$tok1 multiplyReverse $tok2]

    set res3 [$tok2 multiply $tok1]
   
    list [$res1 toString] [$res2 toString] [$res3 toString] 
} {36.6 36.6 36.6}


######################################################################
####
# Test subtract operator between doubles and ints.
test SmoothToken-8.0 {Test subtract operator between doubles.} {
    set tok1 [java::new {ptolemy.data.SmoothToken double} 12.2]
    set tok2 [java::new {ptolemy.data.SmoothToken double} 2.2]

    set res1 [$tok1 subtract $tok1]
    set res2 [$tok1 subtractReverse $tok2]

    list [$res1 toString] [$res2 toString]
} {0.0 -10.0}
######################################################################
####
# 
test SmoothToken-8.1 {Test subtract operator between doubles and ints.} {
    set tok1 [java::new {ptolemy.data.SmoothToken double} 12.2]
    set tok2 [java::new {ptolemy.data.IntToken int} 3]
    
    set res1 [$tok1 subtract $tok2]
    set res2 [$tok1 subtractReverse $tok2]

    set res3 [$tok2 subtract $tok1]
   
    list [$res1 toString] [$res2 toString] [$res3 toString] 
} {9.2 -9.2 -9.2}

######################################################################
####
# 
test SmoothToken-11.0 {Test equals} {
    set t1 [java::new {ptolemy.data.SmoothToken double} 3.5]
    set t2 [java::new {ptolemy.data.SmoothToken double} 3.5]
    set t3 [java::new {ptolemy.data.SmoothToken double} -8.0]
    list [$t1 equals $t1] [$t1 equals $t2] [$t1 equals $t3]
} {1 1 0}


######################################################################
####
# 
test SmoothToken-11.1 {Test equals on nil} {
    set tu [java::field ptolemy.data.SmoothToken NIL]
    set t2 [java::new ptolemy.data.SmoothToken 2]
    set t [java::field ptolemy.data.Token NIL]
    list [$tu equals $tu] [$tu equals $t2] [$t2 equals $tu] \
	[$t2 equals $t2] [$t equals $tu] [$tu equals $t]
} {0 0 0 1 0 0} 

######################################################################
####
# 
test SmoothToken-12.0 {Test hashCode} {
    set t1 [java::new {ptolemy.data.SmoothToken double} 3.5]
    set t2 [java::new {ptolemy.data.SmoothToken double} 3.5]
    set t3 [java::new {ptolemy.data.SmoothToken double} -8.0]
    list [$t1 hashCode] [$t2 hashCode] [$t3 hashCode]
} {3 3 -8}

######################################################################
####
# 
test SmoothToken-13.0 {Test convert from BooleanToken} {
    set t [java::new {ptolemy.data.BooleanToken boolean} false]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.SmoothToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.BooleanToken 'false' to the type double because the type of the token is higher or incomparable with the given type.}}

test SmoothToken-13.1 {Test convert from UnsignedByteToken} {
    set t [java::new {ptolemy.data.UnsignedByteToken byte} 1]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.SmoothToken convert $t] toString]} msg
    list $msg
} {1.0}

test SmoothToken-13.2 {Test convert from ComplexToken} {
    set o [java::new {ptolemy.math.Complex} 1.0 1.0]
    set t [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $o]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.SmoothToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.ComplexToken '1.0 + 1.0i' to the type double because the type of the token is higher or incomparable with the given type.}}

test SmoothToken-13.3 {Test convert from SmoothToken} {
    set t [java::new {ptolemy.data.SmoothToken double} 1.0]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.SmoothToken convert $t] toString]} msg
    list $msg
} {1.0}

test SmoothToken-13.3.5 {Test convert from FloatToken} {
    set t [java::new {ptolemy.data.FloatToken float} 1.0]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.FloatToken convert $t] toString]} msg
    list $msg
} {1.0f}

test SmoothToken-13.4 {Test convert from FixToken} {
    set t [java::new {ptolemy.data.FixToken java.lang.String} "fix(1.0,8,4)"]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.SmoothToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.FixToken 'fix(1.0,8,4)' to the type double because the type of the token is higher or incomparable with the given type.}}

test SmoothToken-13.5 {Test convert from IntToken} {
    set t [java::new {ptolemy.data.IntToken int} 1]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.SmoothToken convert $t] toString]} msg
    list $msg
} {1.0}

test SmoothToken-13.6 {Test convert from LongToken} {
    set t [java::new {ptolemy.data.LongToken long} 1]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.SmoothToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.LongToken '1L' to the type double because the type of the token is higher or incomparable with the given type.}}

test SmoothToken-13.7 {Test convert from StringToken} {
    set t [java::new {ptolemy.data.StringToken java.lang.String} "One"]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.SmoothToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.StringToken '"One"' to the type double because the type of the token is higher or incomparable with the given type.}}
    
test SmoothToken-13.8 {Test convert from FloatToken} {
    set t [java::new {ptolemy.data.FloatToken float} 1.0]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.SmoothToken convert $t] toString]} msg
    list $msg
} {1.0}

######################################################################
####
# 
test SmoothToken-13.8 {Test convert from PetiteToken} {
    set t [java::new {ptolemy.data.PetiteToken double} 0.1]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.SmoothToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.PetiteToken '0.1p' to the type double.}}


######################################################################
####
# 
test SmoothToken-16.0 {call leftShift and get coverage in the parent class} {
    set p [java::new {ptolemy.data.SmoothToken double} 16.16]
    catch {$p leftShift 1} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: leftShift operation not supported between ptolemy.data.SmoothToken '16.16' and ptolemy.data.IntToken '1'}}

######################################################################
####
# 
test SmoothToken-17.0 {call logicalRightShift and get coverage in the parent class} {
    set p [java::new {ptolemy.data.SmoothToken double} 16.16]
    catch {$p logicalRightShift 1} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: logicalRightShift operation not supported between ptolemy.data.SmoothToken '16.16' and ptolemy.data.IntToken '1'}}


######################################################################
####
# 
test SmoothToken-18.0 {call rightShift and get coverage in the parent class} {
    set p [java::new {ptolemy.data.SmoothToken double} 16.16]
    catch {$p rightShift 1} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: rightShift operation not supported between ptolemy.data.SmoothToken '16.16' and ptolemy.data.IntToken '1'}}

