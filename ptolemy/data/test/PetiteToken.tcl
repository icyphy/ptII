# Tests for the PetiteToken class
#
# @Author: Christopher Brooks, Based on DoubleToken.tcl by Neil Smyth
#
# @Version $Id$
#
# @Copyright (c) 1997-2005 The Regents of the University of California.
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
test PetiteToken-1.0 {Create an empty instance} {
    set p [java::new ptolemy.data.PetiteToken]
    $p toString
} {0.0p}

######################################################################
####
# 
test PetiteToken-1.1 {Create a non-empty instance from an double} {
    set p [java::new {ptolemy.data.PetiteToken double} 5.5]
    set p2 [java::new {ptolemy.data.PetiteToken double} -5.5]
    set p3 [java::new {ptolemy.data.PetiteToken double} 0.55]
    list [$p toString] [$p2 toString] [$p3 toString]
} {1.0p -1.0p 0.55p}

######################################################################
####
# 
test PetiteToken-1.2 {Create a non-empty instance from an String} {
    set p [java::new {ptolemy.data.PetiteToken double} "7.7"]
    set p2 [java::new {ptolemy.data.PetiteToken double} "-7.7"]
    set p3 [java::new {ptolemy.data.PetiteToken double} "0.77"]
    list [$p toString] [$p2 toString] [$p3 toString]
} {1.0p -1.0p 0.77p}

######################################################################
####
# 
test PetiteToken-1.3 {Create a non-empty instance from an String} {
    set p [java::new {ptolemy.data.PetiteToken String} "7.56E-10"]
    $p toString
} {0.000000000756p}

######################################################################
####
# 
test PetiteToken-1.4 {Create a non-empty instance from an String} {
    set p [java::new {ptolemy.data.PetiteToken String} "-0.56E0"]
    $p toString
} {-0.56p}

######################################################################
####
# 
test PetiteToken-2.0 {Create a non-empty instance and query its value as a Complex} {
    set p [java::new {ptolemy.data.PetiteToken double} 0.5]
    set res [$p complexValue]
    list [$res toString]
} {{3.3 + 0.0i}}

######################################################################
####
# 
test PetiteToken-2.1 {Create a non-empty instance and query its value as a double} {
    set p [java::new {ptolemy.data.PetiteToken double} 0.6]
    set res1 [$p doubleValue]
    list $res1
} {3.3}

######################################################################
####
# 
test PetiteToken-2.2 {Create a non-empty instance and query its value as an int} {
    set p [java::new {ptolemy.data.PetiteToken double} 0.2]
    catch {$p intValue} errmsg

    list $errmsg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.PetiteToken '0.2p' to the type int.}}

######################################################################
####
# 
test PetiteToken-2.3 {Create a non-empty instance and query its value as a long} {
    set p [java::new {ptolemy.data.PetiteToken double} 0.1]
   catch {$p longValue} errmsg

    list $errmsg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.PetiteToken '0.1p' to the type long.}}

######################################################################
####
# 
test PetiteToken-2.4 {Create a non-empty instance and query its value as a string} {
    set p [java::new {ptolemy.data.PetiteToken double} 12.2]
    $p toString
} {12.2}

######################################################################
####
# 
test PetiteToken-2.5 {Test additive identity} {
    set p [java::new {ptolemy.data.PetiteToken double} 0.25]
    set token [$p zero]

    list [$token toString]
} {0.0p}
######################################################################
####
# 
test PetiteToken-2.6 {Test multiplicative identity} {
    set p [java::new {ptolemy.data.PetiteToken double} 12.2]
    set token [$p one]

    list [$token toString]
} {1.0}

######################################################################
####
# Test addition of doubles to Token types below it in the lossless 
# type hierarchy, and with other doubles.
test PetiteToken-3.0 {Test adding doubles.} {
    set p [java::new {ptolemy.data.PetiteToken double} 0.1]
    set res1 [$p add $p]
    set res2 [$p addReverse $p]

    list [$res1 toString] [$res2 toString]
} {0.2p 0.2p}
######################################################################
####
# 
test PetiteToken-3.1 {Test adding doubles and ints.} {
    set tok1 [java::new {ptolemy.data.PetiteToken double} 0.31]
    set tok2 [java::new {ptolemy.data.IntToken int} 0]
    set res1 [$tok1 add $tok2]
    set res2 [$tok1 addReverse $tok2]

    set res3 [$tok2 add $tok1]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {14.2 14.2 14.2}

######################################################################
####
# Test division of doubles with Token types below it in the lossless 
# type hierarchy, and with other doubles.
test PetiteToken-4.0 {Test dividing doubles.} {
    set p [java::new {ptolemy.data.PetiteToken double} 0.4]
    set res1 [$p divide $p]
    set res2 [$p divideReverse $p]

    list [$res1 toString] [$res2 toString]
} {1.0p 1.0p}
######################################################################
####
# 
test PetiteToken-4.1 {Test dividing doubles and ints.} {
    set tok1 [java::new {ptolemy.data.PetiteToken double} 0.41]
    set tok2 [java::new {ptolemy.data.IntToken int} 2]
    set res1 [$tok1 divide $tok2]
    set resultToken [java::new {ptolemy.data.PetiteToken double} \
	    0.1639344262295]
    set res2 [[$tok1 divideReverse $tok2] isCloseTo $resultToken]

    set res3 [[$tok2 divide $tok1] isCloseTo $resultToken]
 
    list [$res1 toString] [$res2 toString] [$res3 toString]
} {6.1 true true}

######################################################################
####
# Test isEqualTo operator applied to other doubles and Tokens types 
# below it in the lossless type hierarchy.
test PetiteToken-5.0 {Test equality between doubles.} {
    set tok1 [java::new {ptolemy.data.PetiteToken double} 0.5]
    set tok2 [java::new {ptolemy.data.PetiteToken double} 0.51]

    set res1 [$tok1 {isEqualTo ptolemy.data.Token} $tok1]
    set res2 [$tok1 {isEqualTo ptolemy.data.Token} $tok2]

    list [$res1 toString] [$res2 toString]
} {true false}

######################################################################
####
# 
test PetiteToken-5.1 {Test equality between doubles and ints.} {
    set tok1 [java::new {ptolemy.data.PetiteToken double} 0]
    set tok2 [java::new {ptolemy.data.IntToken int} 0]
    set tok3 [java::new {ptolemy.data.PetiteToken double} -1]
    set tok4 [java::new {ptolemy.data.IntToken int} -1]

    set res1 [$tok1 {isEqualTo ptolemy.data.Token} $tok2]
    set res2 [$tok1 {isEqualTo ptolemy.data.Token} $tok4]
    set res3 [$tok2 {isEqualTo ptolemy.data.Token} $tok1]
    set res3 [$tok3 {isEqualTo ptolemy.data.Token} $tok4]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString]
} {true false true true}

######################################################################
####
# Test isCloseTo operator applied to other doubles and Tokens types 
# below it in the lossless type hierarchy.
test PetiteToken-5.5 {Test closeness between doubles. \
    This test should be the same as the similar PetiteToken-5.0 \
    isEquals test. \
} {
    set tok1 [java::new {ptolemy.data.PetiteToken double} 0.55]
    set tok2 [java::new {ptolemy.data.PetiteToken double} 0.54]

    set res1 [$tok1 {isCloseTo ptolemy.data.Token} $tok1]
    set res2 [$tok1 {isCloseTo ptolemy.data.Token} $tok2]

    list [$res1 toString] [$res2 toString]
} {true false}
######################################################################
####
# 
test PetiteToken-5.6 {Test closeness between doubles and ints. \
    This test should be the same as the similar PetiteToken-5.0 \
    isEquals test. \
} {
    set tok1 [java::new {ptolemy.data.PetiteToken double} 12]
    set tok2 [java::new {ptolemy.data.IntToken int} 12]
    set tok3 [java::new {ptolemy.data.PetiteToken double} 2]
    set tok4 [java::new {ptolemy.data.IntToken int} 2]

    set res1 [$tok1 {isCloseTo ptolemy.data.Token} $tok2]
    set res2 [$tok1 {isCloseTo ptolemy.data.Token} $tok4]

    set res3 [$tok2 {isCloseTo ptolemy.data.Token} $tok1]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {true false true}

######################################################################
####
# 
test PetiteToken-5.7 {Test closeness between doubles} {
    set epsilon 0.001
    set oldEpsilon [java::field ptolemy.math.Complex EPSILON]
    java::field ptolemy.math.Complex EPSILON $epsilon

    set token1 [java::new {ptolemy.data.PetiteToken double} 12.0]
    set notCloseToken1 [java::new {ptolemy.data.PetiteToken double} \
	    [expr {12.0 + $epsilon*100.0} ] ]
    set closeToken1 [java::new {ptolemy.data.PetiteToken double} \
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

test PetiteToken-5.8 {Test closeness between doubles around 0} {
    set epsilon 0.001
    set oldEpsilon [java::field ptolemy.math.Complex EPSILON]
    java::field ptolemy.math.Complex EPSILON $epsilon

    set token1 [java::new {ptolemy.data.PetiteToken double} 0.0]
    set notCloseToken1 [java::new {ptolemy.data.PetiteToken double} \
	    [expr {0.0 + 1.1 * $epsilon} ] ]
    set anotherNotCloseToken1 [java::new {ptolemy.data.PetiteToken double} \
	    [expr {0.0 - 1.1 * $epsilon} ] ]
    set closeToken1 [java::new {ptolemy.data.PetiteToken double} \
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

test PetiteToken-5.9 {Test closeness between a double and a String} {
    set doubleToken [java::new {ptolemy.data.PetiteToken double} 0.59]
    set stringToken [java::new ptolemy.data.StringToken "0.59"]
    catch {[$doubleToken {isCloseTo ptolemy.data.Token} $stringToken] toString} errMsg1
    catch {[$stringToken {isCloseTo ptolemy.data.Token} $doubleToken] toString} errMsg2
    list [lrange $errMsg2 0 10] [lrange $errMsg2 0 10]
} {true true}

test PetiteToken-5.10 {Test closeness between doubles and ints.} {
    set epsilon 0.001
    set oldEpsilon [java::field ptolemy.math.Complex EPSILON]
    java::field ptolemy.math.Complex EPSILON $epsilon

    set tok1 [java::new {ptolemy.data.PetiteToken double} \
	      [expr {-1.0 + 0.5 * $epsilon} ]]
    set tok2 [java::new {ptolemy.data.IntToken int} 0]

    catch {set res1 [$tok1 {isCloseTo ptolemy.data.Token} $tok2]} msg
    #set res2 [$tok2 {isCloseTo ptolemy.data.Token} $tok1]

    java::field ptolemy.math.Complex EPSILON $oldEpsilon

    list $msg
} {{ptolemy.kernel.util.IllegalActionException: isCloseTo method not supported between ptolemy.data.PetiteToken '-0.9995p' and ptolemy.data.IntToken '0' because the types are incomparable.}}


######################################################################
####
# Test modulo operator between doubles and ints.
test PetiteToken-6.0 {Test modulo between doubles.} {
    set tok1 [java::new {ptolemy.data.PetiteToken double} 0.2]
    set tok2 [java::new {ptolemy.data.PetiteToken double} 0.6]

    set res1 [$tok1 modulo $tok1]
    set res2 [$tok1 moduloReverse $tok2]

    list [$res1 toString] [$res2 toString]
} {}

######################################################################
####
# 
test PetiteToken-6.1 {Test modulo operator between doubles and ints.} {
    set tok1 [java::new {ptolemy.data.PetiteToken double} 0.61]
    set tok2 [java::new {ptolemy.data.IntToken int} 3]
    
    catch {set res1 [$tok1 modulo $tok2]} msg
    #set res2 [$tok1 moduloReverse $tok2]
    #set res3 [$tok2 modulo $tok1]
   
    list $msg	
} {{ptolemy.kernel.util.IllegalActionException: modulo method not supported between ptolemy.data.PetiteToken '0.61p' and ptolemy.data.IntToken '3' because the types are incomparable.}}

######################################################################
####
# Test multiply operator between doubles and ints.
test PetiteToken-7.0 {Test multiply operator between doubles.} {
    set tok1 [java::new {ptolemy.data.PetiteToken double} 0.7]
    set tok2 [java::new {ptolemy.data.PetiteToken double} 0.1]

    set res1 [$tok1 multiply $tok1]
    set res2 [$tok1 multiplyReverse $tok2]

    list [$res1 toString] [$res2 toString]
} {148.84 26.84}
######################################################################
####
# 
test PetiteToken-7.1 {Test multiply operator between doubles and ints.} {
    set tok1 [java::new {ptolemy.data.PetiteToken double} 0.71]
    set tok2 [java::new {ptolemy.data.IntToken int} 3]
    
    catch {set res1 [$tok1 multiply $tok2]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: multiply method not supported between ptolemy.data.PetiteToken '0.71p' and ptolemy.data.IntToken '3' because the types are incomparable.}}


######################################################################
####
# Test subtract operator between doubles and ints.
test PetiteToken-8.0 {Test subtract operator between doubles.} {
    set tok1 [java::new {ptolemy.data.PetiteToken double} 0.8]
    set tok2 [java::new {ptolemy.data.PetiteToken double} 0.08]

    set res1 [$tok1 subtract $tok1]
    set res2 [$tok1 subtractReverse $tok2]

    list [$res1 toString] [$res2 toString]
} {0.0 -0.72}
######################################################################
####
# 
test PetiteToken-8.1 {Test subtract operator between doubles and ints.} {
    set tok1 [java::new {ptolemy.data.PetiteToken double} 0.81]
    set tok2 [java::new {ptolemy.data.IntToken int} -1]
    
    catch { set res1 [$tok1 subtract $tok2]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: subtract method not supported between ptolemy.data.PetiteToken '0.81p' and ptolemy.data.IntToken '-1' because the types are incomparable.}}

######################################################################
####
# 
test PetiteToken-9.0 {Test equals} {
    set t1 [java::new {ptolemy.data.PetiteToken double} 0.9]
    set t2 [java::new {ptolemy.data.PetiteToken double} 0.9]
    set t3 [java::new {ptolemy.data.PetiteToken double} 0.8]
    list [$t1 equals $t1] [$t1 equals $t2] [$t1 equals $t3]
} {1 1 0}

######################################################################
####
# 
test PetiteToken-10.0 {Test hashCode} {
    set t1 [java::new {ptolemy.data.PetiteToken double} 0.1]
    set t2 [java::new {ptolemy.data.PetiteToken double} 0.1]
    set t3 [java::new {ptolemy.data.PetiteToken double} -0.1]
    list [$t1 hashCode] [$t2 hashCode] [$t3 hashCode]
} {3 3 -8}

######################################################################
####
# 
test PetiteToken-13.0 {Test convert from BooleanToken} {
    set t [java::new {ptolemy.data.BooleanToken boolean} false]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.PetiteToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.BooleanToken 'false' to the type petite.}}

test PetiteToken-13.1 {Test convert from UnsignedByteToken} {
    set t [java::new {ptolemy.data.UnsignedByteToken byte} 1]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.PetiteToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.UnsignedByteToken '1ub' to the type petite.}}

test PetiteToken-13.2 {Test convert from ComplexToken} {
    set o [java::new {ptolemy.math.Complex} 1.0 1.0]
    set t [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $o]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.PetiteToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.ComplexToken '1.0 + 1.0i' to the type petite.}}

test PetiteToken-13.3 {Test convert from PetiteToken} {
    set t [java::new {ptolemy.data.PetiteToken double} 1.0]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.PetiteToken convert $t] toString]} msg
    list $msg
} {1.0p}

test PetiteToken-13.4 {Test convert from FixToken} {
    set t [java::new {ptolemy.data.FixToken java.lang.String} "fix(1.0,8,4)"]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.PetiteToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.FixToken 'fix(1.0,8,4)' to the type petite.}}

test PetiteToken-13.5 {Test convert from IntToken} {
    set t [java::new {ptolemy.data.IntToken int} -1]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.PetiteToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.IntToken '-1' to the type petite.}}

test PetiteToken-13.6 {Test convert from LongToken} {
    set t [java::new {ptolemy.data.LongToken long} 1]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.PetiteToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.LongToken '1L' to the type petite.}}

test PetiteToken-13.7 {Test convert from StringToken} {
    set t [java::new {ptolemy.data.StringToken java.lang.String} "One"]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.PetiteToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.StringToken '"One"' to the type petite.}}
    
