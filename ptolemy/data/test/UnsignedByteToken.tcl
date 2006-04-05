# Tests for the UnsignedByteToken class
#
# @Author: Yuhong Xiong
#
# @Version $Id$
#
# @Copyright (c) 1998-2005 The Regents of the University of California.
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
test UnsignedByteToken-1.0 {Create an empty instance} {
    set p [java::new ptolemy.data.UnsignedByteToken]
    $p toString
} {0ub}

######################################################################
####
# 
test UnsignedByteToken-1.1 {Create an instance with a value} {
    set token [java::new {ptolemy.data.UnsignedByteToken byte} 3]
    set token2 [java::new {ptolemy.data.UnsignedByteToken byte} -1]
    list [$token toString] [$token2 toString]
} {3ub 255ub}

######################################################################
####
# 
test UnsignedByteToken-1.2 {Create an instance from a string value} {
    set token [java::new {ptolemy.data.UnsignedByteToken String} "5"]
    set token2 [java::new {ptolemy.data.UnsignedByteToken String} "255"]
    catch {[[java::new {ptolemy.data.UnsignedByteToken String} "-1"] toString]} res3
    list [$token toString] [$token2 toString] $res3
} {5ub 255ub {ptolemy.kernel.util.IllegalActionException: Value '-1' is out of the range of Unsigned Byte}}

######################################################################
####
# 
test UnsignedByteToken-1.5 {Create a nil Token from a null token} {
    catch {java::new ptolemy.data.UnsignedByteToken [java::null]} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Creating a nil token with UnsignedByteToken(null) is not supported.  Use UnsignedByteToken.NIL, or the nil Constant.}}

######################################################################
####
# 
test UnsignedByteToken-1.6 {Create a nil Token from an String} {
    catch {java::new {ptolemy.data.UnsignedByteToken String} nil} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Creating a nil token with UnsignedByteToken("nil") is not supported.  Use UnsignedByteToken.NIL, or the nil Constant.}}

######################################################################
####
# 
test UnsignedByteToken-2.0 {Create a non-empty instance and query its value} {
    set token [java::new {ptolemy.data.UnsignedByteToken byte} 4]
    $token byteValue
} {4}

######################################################################
####
# 
test UnsignedByteToken-3.0 {Create an non-empty instance and add it to Strings} {
    set token1 [java::new ptolemy.data.StringToken "value is " ]
    set token2 [java::new {ptolemy.data.UnsignedByteToken byte} 6]
    set token3 [java::new ptolemy.data.StringToken "....." ]

    set token4 [$token1 add $token2]
    set token5 [$token2 add $token3]
    
    set token6 [$token4 add $token5]

    list [$token6 toString]
} {{"value is 6ub6ub....."}}

######################################################################
####
# 
test UnsignedByteToken-4.0 {Test equals} {
    set t1 [java::new {ptolemy.data.UnsignedByteToken byte} 1]
    set t2 [java::new {ptolemy.data.UnsignedByteToken byte} 1]
    set t3 [java::new {ptolemy.data.UnsignedByteToken byte} 2]
    list [$t1 equals $t1] [$t1 equals $t2] [$t1 equals $t3]
} {1 1 0}

######################################################################
####
# 
test UnsignedByteToken-5.0 {Test hashCode} {
    set t1 [java::new {ptolemy.data.UnsignedByteToken byte} 1]
    set t2 [java::new {ptolemy.data.UnsignedByteToken byte} 1]
    set f [java::new {ptolemy.data.UnsignedByteToken byte} 2]
    list [$t1 hashCode] [$t2 hashCode] [$f hashCode]
} {1 1 2}

######################################################################
####
# Test subtract operator between unsigned bytes
test UnsignedByteToken-8.0 {Test subtract operator between ints.} {
    set tok1 [java::new {ptolemy.data.UnsignedByteToken int} 7]
    set tok2 [java::new {ptolemy.data.UnsignedByteToken int} 2]

    set res1 [$tok1 subtract $tok2]
    set res2 [$tok1 subtractReverse $tok2]

    list [$res1 toString] [$res2 toString]
} {5ub 251ub}

######################################################################
####
# Test subtract operator between unsigned bytes
test UnsignedByteToken-8.1 {Test shift operator between ints.} {
    set tok1 [java::new {ptolemy.data.UnsignedByteToken int} 7]
    set tok2 [java::new {ptolemy.data.UnsignedByteToken int} -7]

    set res1 [$tok1 leftShift 1]
    set res2 [$tok2 leftShift 1]
    set res3 [$tok1 rightShift 1]
    set res4 [$tok2 rightShift 1]
    set res5 [$tok1 logicalRightShift 1]
    set res6 [$tok2 logicalRightShift 1]

    list [$res1 toString] [$res2 toString] [$res3 toString] [$res4 toString] [$res5 toString] [$res6 toString]
} {14ub 242ub 3ub 252ub 3ub 252ub}

######################################################################
####
# 
test UnsignedByteToken-13.0 {Test convert from BooleanToken} {
    set t [java::new {ptolemy.data.BooleanToken boolean} false]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.UnsignedByteToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.BooleanToken 'false' to the type byte because the type of the token is higher or incomparable with the given type.}}

test UnsignedByteToken-13.1 {Test convert from ByteToken} {
    set t [java::new {ptolemy.data.UnsignedByteToken byte} 1]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.UnsignedByteToken convert $t] toString]} msg
    list $msg
} {1ub}

test UnsignedByteToken-13.2 {Test convert from ComplexToken} {
    set o [java::new {ptolemy.math.Complex} 1.0 1.0]
    set t [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $o]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.UnsignedByteToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.ComplexToken '1.0 + 1.0i' to the type byte because the type of the token is higher or incomparable with the given type.}}

test UnsignedByteToken-13.3 {Test convert from DoubleToken} {
    set t [java::new {ptolemy.data.DoubleToken double} 1.0]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.UnsignedByteToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.DoubleToken '1.0' to the type byte because the type of the token is higher or incomparable with the given type.}}

test UnsignedByteToken-13.4 {Test convert from FixToken} {
    set t [java::new {ptolemy.data.FixToken java.lang.String} "fix(1.0,8,4)"]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.UnsignedByteToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.FixToken 'fix(1.0,8,4)' to the type byte because the type of the token is higher or incomparable with the given type.}}

test UnsignedByteToken-13.5 {Test convert from IntToken} {
    set t [java::new {ptolemy.data.IntToken int} 1]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.UnsignedByteToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.IntToken '1' to the type byte because the type of the token is higher or incomparable with the given type.}}

test UnsignedByteToken-13.6 {Test convert from LongToken} {
    set t [java::new {ptolemy.data.LongToken long} 1]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.UnsignedByteToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.LongToken '1L' to the type byte because the type of the token is higher or incomparable with the given type.}}

test UnsignedByteToken-13.7 {Test convert from StringToken} {
    set t [java::new {ptolemy.data.StringToken java.lang.String} "One"]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.UnsignedByteToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.StringToken '"One"' to the type byte because the type of the token is higher or incomparable with the given type.}}
    
