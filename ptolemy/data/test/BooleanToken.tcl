# Tests for the BooleanToken class
#
# @Author: Neil Smyth
#
# @Version $Id$
#
# @Copyright (c) 1998-2000 The Regents of the University of California.
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
test BooleanToken-1.0 {Create an empty instance} {
    set p [java::new ptolemy.data.BooleanToken]
    $p toString
} {false}

######################################################################
####
# 
test BooleanToken-1.1 {Create an instance with a value} {
    set token [java::new {ptolemy.data.BooleanToken boolean} true]
    $token toString
} {true}

######################################################################
####
# 
test BooleanToken-1.2 {Create an instance from a string value} {
    set token [java::new {ptolemy.data.BooleanToken String} "true"]
    $token toString
} {true}

######################################################################
####
# 
test BooleanToken-2.0 {Create a non-empty instance and query its value} {
    set token [java::new {ptolemy.data.BooleanToken boolean} false]
    $token booleanValue
} {0}

######################################################################
####
# 
test BooleanToken-3.0 {Create an non-empty instance and add it to Strings} {
    set token1 [java::new ptolemy.data.StringToken "value is " ]
    set token2 [java::new {ptolemy.data.BooleanToken boolean} true]
    set token3 [java::new ptolemy.data.StringToken "....." ]

    set token4 [$token1 add $token2]
    set token5 [$token2 add $token3]
    
    set token6 [$token4 add $token5]

    list [$token6 toString]
} {{"value is truetrue....."}}

######################################################################
####
# 
test BooleanToken-4.0 {Test addition of booleans} {
    set trueToken [java::field ptolemy.data.BooleanToken TRUE]
    set falseToken [java::field ptolemy.data.BooleanToken FALSE]
    set r1 [$falseToken add $falseToken]
    set r2 [$trueToken add $falseToken]
    set r3 [$falseToken add $trueToken]
    set r4 [$trueToken add $trueToken]
    list [$r1 stringValue] [$r2 stringValue] [$r3 stringValue] [$r4 stringValue]
} {false true true false}

test BooleanToken-4.1 {Test reverse addition of booleans} {
    set r1 [$falseToken addReverse $falseToken]
    set r2 [$trueToken addReverse $falseToken]
    set r3 [$falseToken addReverse $trueToken]
    set r4 [$trueToken addReverse $trueToken]
    list [$r1 stringValue] [$r2 stringValue] [$r3 stringValue] [$r4 stringValue]
} {false true true false}

test BooleanToken-5.0 {Test division of booleans} {
    set r1 [$falseToken divide $trueToken]
    set r2 [$trueToken divide $trueToken]
    list [$r1 stringValue] [$r2 stringValue]
} {false true}

test BooleanToken-5.1 {Test division by zero} {
    catch {[$falseToken divide $falseToken]} msg1
    catch {[$trueToken divide $falseToken]} msg2
    list $msg1 $msg2
} {{java.lang.IllegalArgumentException: BooleanToken: division by false-valued token (analogous to division by zero).} {java.lang.IllegalArgumentException: BooleanToken: division by false-valued token (analogous to division by zero).}}

test BooleanToken-5.2 {Test reverse division of booleans} {
    set r3 [$trueToken divideReverse $falseToken]
    set r4 [$trueToken divideReverse $trueToken]
    list [$r1 stringValue] [$r2 stringValue]
} {false true}

test BooleanToken-5.3 {Test division by zero} {
    catch {[$falseToken divideReverse $falseToken]} msg1
    catch {[$falseToken divideReverse $trueToken]} msg2
    list $msg1 $msg2
} {{java.lang.IllegalArgumentException: BooleanToken: division by false-valued token (analogous to division by zero).} {java.lang.IllegalArgumentException: BooleanToken: division by false-valued token (analogous to division by zero).}}


test BooleanToken-6.0 {Test equality test} {
    set r1 [$falseToken isEqualTo $falseToken]
    set r2 [$trueToken isEqualTo $falseToken]
    set r3 [$falseToken isEqualTo $trueToken]
    set r4 [$trueToken isEqualTo $trueToken]
    list [$r1 stringValue] [$r2 stringValue] [$r3 stringValue] [$r4 stringValue]
} {true false false true}


test BooleanToken-7.0 {Test multiplication} {
    set r1 [$falseToken multiply $falseToken]
    set r2 [$trueToken multiply $falseToken]
    set r3 [$falseToken multiply $trueToken]
    set r4 [$trueToken multiply $trueToken]
    list [$r1 stringValue] [$r2 stringValue] [$r3 stringValue] [$r4 stringValue]
} {false false false true}

test BooleanToken-7.1 {Test reverse multiplication} {
    set r1 [$falseToken multiplyReverse $falseToken]
    set r2 [$trueToken multiplyReverse $falseToken]
    set r3 [$falseToken multiplyReverse $trueToken]
    set r4 [$trueToken multiplyReverse $trueToken]
    list [$r1 stringValue] [$r2 stringValue] [$r3 stringValue] [$r4 stringValue]
} {false false false true}

test BooleanToken-8.0 {Test not} {
    set r1 [$falseToken not]
    set r2 [$trueToken not]
    list [$r1 stringValue] [$r2 stringValue]
} {true false}

test BooleanToken-9.0 {Test identities} {
    set r1 [$falseToken one]
    set r2 [$trueToken zero]
    list [$r1 stringValue] [$r2 stringValue]
} {true false}

test BooleanToken-10.0 {Test subtraction of booleans} {
    set r1 [$falseToken subtract $falseToken]
    set r2 [$trueToken subtract $falseToken]
    set r3 [$falseToken subtract $trueToken]
    set r4 [$trueToken subtract $trueToken]
    list [$r1 stringValue] [$r2 stringValue] [$r3 stringValue] [$r4 stringValue]
} {false true true false}

test BooleanToken-4.1 {Test reverse subtraction of booleans} {
    set r1 [$falseToken subtractReverse $falseToken]
    set r2 [$trueToken subtractReverse $falseToken]
    set r3 [$falseToken subtractReverse $trueToken]
    set r4 [$trueToken subtractReverse $trueToken]
    list [$r1 stringValue] [$r2 stringValue] [$r3 stringValue] [$r4 stringValue]
} {false true true false}
