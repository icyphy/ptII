# Tests for the StringToken class
#
# @Author: Edward A. Lee, Yuhong Xiong, 
#
# @Version: $Id$
#
# @Copyright (c) 1997-2012 The Regents of the University of California.
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

######################################################################
####
# 
test StringToken-1.5 {Create a nil Token from a null token} {
    set nil [java::field ptolemy.data.StringToken NIL]
    set p2 [java::new ptolemy.data.StringToken "nil"]
    set p3 [java::new ptolemy.data.StringToken [java::null]]
    list [$nil toString] [$nil stringValue] [$nil isNil] \
	[$p2 toString] [$p2 stringValue] [$p2 isNil] \
	[$p2 toString] [$p3 stringValue] [$p3 isNil]
} {nil nil 1 {"nil"} nil 0 {"nil"} {} 0}

######################################################################
####
# 
test StringToken-1.6 {Don't create a nil Token from a String} {
    # The StringToken(String) no longer creates nil tokens
    set p [java::new {ptolemy.data.StringToken String} nil]
    set p2 [java::new {ptolemy.data.StringToken String} [java::null]]
    list [$p toString] [$p isNil] [$p2 toString] [$p2 isNil] 
} {{"nil"} 0 {""} 0}

######################################################################
####
# 
test StringToken-1.7 {Create a String token with value "nil" from a String} {
    set p [java::new {ptolemy.data.StringToken String} "nil"]
    # Note this is not a nil token
    list [$p toString] [$p stringValue] [$p isNil]
} {{"nil"} nil 0}

######################################################################
####
# 
test StringToken-2.1 {Create an empty instance} {
    set p [java::new ptolemy.data.StringToken]
    $p toString
} {""}

######################################################################
####
# 
test StringToken-2.2 {Create an empty instance and query its value} {
    set p [java::new ptolemy.data.StringToken]
    $p stringValue
} {}

######################################################################
####
# 
test StringToken-2.3 {Create an non-empty instance} {
    set p [java::new ptolemy.data.StringToken foo]
    list [$p toString]
} {{"foo"}}

######################################################################
####
# 
test StringToken-3.0 {Test adding Strings} {
    set p1 [java::new ptolemy.data.StringToken foo]
    set p2 [java::new ptolemy.data.StringToken bar]
    set res [$p1 add $p2]

    list [$res toString]
} {{"foobar"}}

######################################################################
####
# 
test StringToken-3.1 {Test adding String and boolean} {
    set tok1 [java::new {ptolemy.data.StringToken} foo]
    set tok2 [java::new {ptolemy.data.BooleanToken boolean} true]
    set res1 [$tok1 add $tok2]
    set res2 [$tok1 addReverse $tok2]

    set res3 [$tok2 add $tok1]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {{"footrue"} {"truefoo"} {"truefoo"}}

######################################################################
####
# 
test StringToken-3.2 {Test adding String and long} {
    set tok1 [java::new {ptolemy.data.StringToken} foo]
    set tok2 [java::new {ptolemy.data.LongToken long} 3]
    set res1 [$tok1 add $tok2]
    set res2 [$tok1 addReverse $tok2]

    set res3 [$tok2 add $tok1]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {{"foo3L"} {"3Lfoo"} {"3Lfoo"}}

######################################################################
####
# 
test StringToken-3.3 {Test adding String and int} {
    set tok1 [java::new {ptolemy.data.StringToken} foo]
    set tok2 [java::new {ptolemy.data.IntToken int} 4]
    set res1 [$tok1 add $tok2]
    set res2 [$tok1 addReverse $tok2]

    set res3 [$tok2 add $tok1]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {{"foo4"} {"4foo"} {"4foo"}}

######################################################################
####
# 
test StringToken-3.4 {Test adding String and Complex} {
    set tok1 [java::new {ptolemy.data.StringToken} foo]
    set c [java::new {ptolemy.math.Complex double double} 3.3 4.4]
    set tok2 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c]

    set res1 [$tok1 add $tok2]
    set res2 [$tok1 addReverse $tok2]

    set res3 [$tok2 add $tok1]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {{"foo3.3 + 4.4i"} {"3.3 + 4.4ifoo"} {"3.3 + 4.4ifoo"}}

######################################################################
####
# 
test StringToken-3.5 {Test adding String and double} {
    set tok1 [java::new {ptolemy.data.StringToken} foo]
    set tok2 [java::new {ptolemy.data.DoubleToken double} 2.5]
    set res1 [$tok1 add $tok2]
    set res2 [$tok1 addReverse $tok2]

    set res3 [$tok2 add $tok1]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {{"foo2.5"} {"2.5foo"} {"2.5foo"}}

######################################################################
####
# 
test StringToken-3.6 {Test subtracting Strings} {
    set p1 [java::new ptolemy.data.StringToken foo]
    set p2 [java::new ptolemy.data.StringToken bar]
    
    catch {[$p1 subtract $p2] toString} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: subtract operation not supported between ptolemy.data.StringToken '"foo"' and ptolemy.data.StringToken '"bar"'}}

######################################################################
####
# 
test StringToken-3.7 {Test subtracting String and boolean} {
    set tok1 [java::new {ptolemy.data.StringToken} foo]
    set tok2 [java::new {ptolemy.data.BooleanToken boolean} true]
    catch {[$tok1 subtract $tok2] toString} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: subtract operation not supported between ptolemy.data.StringToken '"foo"' and ptolemy.data.BooleanToken 'true'
Because:
subtract operation not supported between ptolemy.data.StringToken '"foo"' and ptolemy.data.StringToken '"true"'}}

######################################################################
####
# 
test StringToken-4.0 {Test isEqualTo} {
    set tok1 [java::new {ptolemy.data.StringToken} foo]
    set tok2 [java::new {ptolemy.data.StringToken} foo]
    set tok3 [java::new {ptolemy.data.StringToken} bar]

    set res1 [$tok1 {isEqualTo ptolemy.data.Token} $tok2]
    set res2 [$tok1 {isEqualTo ptolemy.data.Token} $tok3]
    list [$res1 toString] [$res2 toString]
} {true false}

######################################################################
####
# 
test StringToken-4.0 {Test isEqualTo} {
    set tok1 [java::new {ptolemy.data.StringToken} 33]
    set tok2 [java::new {ptolemy.data.IntToken int} 33]

    [$tok1 {isEqualTo ptolemy.data.Token} $tok2] toString
} {true}

######################################################################
####
# Test divide operator between strings
test StringToken-4.1 {Test divide between strings.} {
    set t1 [java::new {ptolemy.data.StringToken} 5]
    set t2 [java::new {ptolemy.data.StringToken} 2]
    catch {$t1 divide $t2} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: divide operation not supported between ptolemy.data.StringToken '"5"' and ptolemy.data.StringToken '"2"'}}

######################################################################
####
# 
test StringToken-5.0 {Test equals} {
    set t1 [java::new {ptolemy.data.StringToken} foo]
    set t2 [java::new {ptolemy.data.StringToken} foo]
    set t3 [java::new {ptolemy.data.StringToken} bar]
    set t4 [java::new ptolemy.data.StringToken "1"]
    set t5 [java::new ptolemy.data.IntToken 1]
    list [$t1 equals $t1] [$t1 equals $t2] [$t1 equals $t3] [$t4 equals $t5]
} {1 1 0 0}


######################################################################
####
# 
test StringToken-5.2 {Test equals on a nil} {
    set nil [java::field ptolemy.data.StringToken NIL]
    set t1 [java::new {ptolemy.data.StringToken} 3]

    list [$nil equals $nil] [$t1 equals $nil] [$nil equals $t1]
} {0 0 0}

######################################################################
####
# 
test StringToken-5.2 {Test hashCode} {
    set t1 [java::new {ptolemy.data.StringToken} 3]
    set t2 [java::new {ptolemy.data.StringToken} 3]
    set t3 [java::new {ptolemy.data.StringToken} 5]
    list [$t1 hashCode] [$t2 hashCode] [$t3 hashCode]
} {51 51 53}

######################################################################
####
# Test modulo operator between strings
test StringToken-6.1 {Test modulo between string.} {
    set t1 [java::new {ptolemy.data.StringToken} 5]
    set t2 [java::new {ptolemy.data.StringToken} 2]
    catch {$t1 modulo $t2} errMsg1
    catch {$t1 moduloReverse $t2} errMsg2
    list "$errMsg1 \n $errMsg2"
} {{ptolemy.kernel.util.IllegalActionException: modulo operation not supported between ptolemy.data.StringToken '"5"' and ptolemy.data.StringToken '"2"' 
 ptolemy.kernel.util.IllegalActionException: modulo operation not supported between ptolemy.data.StringToken '"2"' and ptolemy.data.StringToken '"5"'}} 

######################################################################
####
# 
test StringToken-13.0 {Test convert from BooleanToken} {
    set t [java::new {ptolemy.data.BooleanToken boolean} false]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.StringToken convert $t] toString]} msg
    list $msg
} {{"false"}}

test StringToken-13.1 {Test convert from UnsignedByteToken} {
    set t [java::new {ptolemy.data.UnsignedByteToken byte} 1]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.StringToken convert $t] toString]} msg
    list $msg
} {{"1ub"}}

test StringToken-13.2 {Test convert from ComplexToken} {
    set o [java::new {ptolemy.math.Complex} 1.0 1.0]
    set t [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $o]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.StringToken convert $t] toString]} msg
    list $msg
} {{"1.0 + 1.0i"}}

test StringToken-13.3 {Test convert from DoubleToken} {
    set t [java::new {ptolemy.data.DoubleToken double} 1.0]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.StringToken convert $t] toString]} msg
    list $msg
} {{"1.0"}}

test StringToken-13.4 {Test convert from FixToken} {
    set t [java::new {ptolemy.data.FixToken java.lang.String} "fix(1.0,8,4)"]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.StringToken convert $t] toString]} msg
    list $msg
} {{"fix(1.0,8,4)"}}

test StringToken-13.5 {Test convert from IntToken} {
    set t [java::new {ptolemy.data.IntToken int} 1]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.StringToken convert $t] toString]} msg
    list $msg
} {{"1"}}

test StringToken-13.6 {Test convert from LongToken} {
    set t [java::new {ptolemy.data.LongToken long} 1]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.StringToken convert $t] toString]} msg
    list $msg
} {{"1L"}}

test StringToken-13.7 {Test convert from StringToken} {
    set t [java::new {ptolemy.data.StringToken java.lang.String} "One"]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.StringToken convert $t] toString]} msg
    list $msg
} {{"One"}}

test StringToken-13.8 {test convert from a null and a nil} {
    set nil [java::field ptolemy.data.StringToken NIL]
    set r1 [java::call ptolemy.data.StringToken convert $nil]
    set r2 [java::call ptolemy.data.StringToken convert [java::null]]
    list [$r1 toString] [$r1 isNil] \
	[$r2 toString] [$r2 isNil]
} {nil 1 nil 1}    

test StringToken-13.9 {Test convert from Token} {
    set t [java::new ptolemy.data.Token]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.StringToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.Token 'present' to the type string because the type of the token is higher or incomparable with the given type.}}


test StringToken-13.9 {Test convert from an AWTImageToken} {
    set t [java::new ptolemy.data.AWTImageToken [java::null]]
    set result {}
    catch {set result [[java::call ptolemy.data.StringToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.AWTImageToken '{type="class ptolemy.data.AWTImageToken" width="-1" height="-1"}' to the type string because the type of the token is higher or incomparable with the given type.}}

test StringToken-13.10 {Test convert from ArrayToken} {
    set t [java::new {ptolemy.data.ArrayToken String} "{1.0, 2.0}"]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.StringToken convert $t] toString]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Conversion is not supported from ptolemy.data.ArrayToken '{1.0, 2.0}' to the type string because the type of the token is higher or incomparable with the given type.}}

test StringToken-13.11 {Test convert from IntMatrixToken} {
    set t [java::new {ptolemy.data.IntMatrixToken String} "\[5, 4; 3, 2\]"]
    set msg {}
    set result {}
    catch {set result [[java::call ptolemy.data.StringToken convert $t] toString]} msg
    list $msg
} {{"[5, 4; 3, 2]"}}

test StringToken-13.12 {Test convert from RecordToken} {
    set t [java::new {ptolemy.data.RecordToken String} "{name = \"bar\", value = 6}"]
    set msg {}
    set result {}
    set result [[java::call ptolemy.data.StringToken convert $t] toString]
    list $result
} {{"{name = \"bar\", value = 6}"}}

test StringToken-14.1 {Test embedded double quotes} {
    set tok1 [java::new {ptolemy.data.StringToken} {has embedded "}]
    set tok2 [java::new {ptolemy.data.StringToken} "has embedded \""]
    set tok3 [java::new {ptolemy.data.StringToken} "has embedded \\\""]
    list [$tok1 toString] [$tok1 toString] [$tok3 toString] [$tok3 stringValue]
} {{"has embedded \""} {"has embedded \""} {"has embedded \\\""} {has embedded \"}}

test StringToken-14.2 {Test embedded double quotes preceded by backslash} {
    set tok3 [java::new {ptolemy.data.StringToken} "has embedded \\\""]
    list [$tok3 toString] [$tok3 stringValue]
} {{"has embedded \\\""} {has embedded \"}}

test StringToken-14.3 {To get the same values, construct a token by calling stringValue instead of toString} {
    set tok3 [java::new {ptolemy.data.StringToken} "has embedded \\\""]
    set tok4 [java::new {ptolemy.data.StringToken} [$tok3 stringValue]]
    set tok5 [java::new {ptolemy.data.StringToken} [$tok3 toString]]
    list \
	    [expr {[$tok3 toString] == [$tok4 toString]}] \
	    [expr {[$tok3 toString] == [$tok5 toString]}] \
	    [$tok3 toString] [$tok3 stringValue] \
	    [$tok4 toString] [$tok4 stringValue] \
	    [$tok5 toString] [$tok5 stringValue] \
} {1 0 {"has embedded \\\""} {has embedded \"} {"has embedded \\\""} {has embedded \"} {"\"has embedded \\\\\\\"\""} {"has embedded \\\""}}
