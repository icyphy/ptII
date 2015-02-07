# Tests for the ArrayToken class
#
# @Author: Yuhong Xiong, contributor: Christopher Brooks
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

######################################################################
####
# 
test ArrayToken-1.0 {Create a string array} {
    set val0 [java::new ptolemy.data.StringToken AB]
    set val1 [java::new ptolemy.data.StringToken CD]
    set valArray [java::new {ptolemy.data.Token[]} 2 [list $val0 $val1]]
    set valToken [java::new {ptolemy.data.ArrayToken} $valArray]

    $valToken toString
} {{"AB", "CD"}}

######################################################################
####
# 
test ArrayToken-1.1 {Create an int array using expression} {
    set valToken [java::new {ptolemy.data.ArrayToken String} "{1, 2, 3}"]
    $valToken toString
} {{1, 2, 3}}

######################################################################
####
# 
test ArrayToken-1.1.1 {Create an double array using expression} {
    # First element is an int, second is a double, the entire thing is doubles 
    set valToken [java::new {ptolemy.data.ArrayToken String} "{1, 2.0, 3}"]
    $valToken toString
} {{1.0, 2.0, 3.0}}

######################################################################
####
# 
test ArrayToken-1.1.2 {Odd mixture of types} {
    set token [java::new {ptolemy.data.ArrayToken String} "{1, nil, {1}}"]
    $token toString	
} {{{1}, {nil}, {1}}}

######################################################################
####
# 
test ArrayToken-1.1.3 {first element is nil} {
    # coverage in _initialize
    set valToken [java::new {ptolemy.data.ArrayToken String} "{nil, 2, 3}"]
    $valToken toString
} {{nil, 2, 3}}

######################################################################
####
# 
test ArrayToken-1.2 {ArrayTokens of length 0 are supported.  Use the constructor that gives the array a type.} {
    set valArray [java::new {ptolemy.data.IntToken[]} 0 ]
    catch {java::new {ptolemy.data.ArrayToken} $valArray} errMsg
    set intToken [java::new {ptolemy.data.IntToken} 0]
    set valToken [java::new {ptolemy.data.ArrayToken ptolemy.data.type.Type} [$intToken getType]]
    list $errMsg [$valToken toString] [$valToken isNil]
} {{ptolemy.kernel.util.IllegalActionException: ArrayToken(Token[]) called with a an array of length less than 1.  To create an array of length 0, use the ArrayToken(Token) constructor or the "emptyArray(type)" function in the expression language. The reason is that elements in ArrayToken must have a type.} {{}} 0}

######################################################################
####
# 
test ArrayToken-1.3 {Create an int array with conversion} {
    set valToken [java::new {ptolemy.data.ArrayToken String} "{1, 2, 3.0}"]
    catch {java::new {ptolemy.data.ArrayToken String} "1.0"} errMsg
    list [$valToken toString] $errMsg
} {{{1.0, 2.0, 3.0}} {ptolemy.kernel.util.IllegalActionException: An array token cannot be created from the expression '1.0'}}

######################################################################
####
# 
test ArrayToken-1.4 {Create an array of nil DoubleTokens} {
    set val0 [java::field ptolemy.data.DoubleToken NIL]
    set val1 [java::field ptolemy.data.DoubleToken NIL]
    set valArray [java::new {ptolemy.data.Token[]} 2 [list $val0 $val1]]
    set valToken [java::new {ptolemy.data.ArrayToken} $valArray]

    $valToken toString
} {{nil, nil}}

######################################################################
####
# 
test ArrayToken-1.5 {Create an array of DoubleTokens, first one nil} {
    set val0 [java::field ptolemy.data.DoubleToken NIL]
    set val1 [java::new ptolemy.data.DoubleToken 2.0]
    set valArray [java::new {ptolemy.data.Token[]} 2 [list $val0 $val1]]
    set valToken [java::new {ptolemy.data.ArrayToken} $valArray]

    $valToken toString
} {{nil, 2.0}}

######################################################################
####
#
test ArrayToken-1.6 {Create an array of DoubleTokens, second one nil} {
    set val0 [java::new ptolemy.data.DoubleToken 2.0]
    set val1 [java::field ptolemy.data.DoubleToken NIL]
    set valArray [java::new {ptolemy.data.Token[]} 2 [list $val0 $val1]]
    set valToken [java::new {ptolemy.data.ArrayToken} $valArray]

    $valToken toString
} {{2.0, nil}}

######################################################################
####
# 
test ArrayToken-1.7.1 {Create a nil Token, but not a nil ArrayToken} {
    set valToken [java::field ptolemy.data.Token NIL]
    set valToken2 [java::new {ptolemy.data.ArrayToken ptolemy.data.type.Type} \
		      [java::null]]
    list [$valToken toString] [$valToken isNil] \
	[$valToken2 toString] [$valToken2 isNil]
} {nil 1 {{}} 0}

######################################################################
####
# 
test ArrayToken-1.7.2 {Create a Double ArrayToken} {
    set val0 [java::new ptolemy.data.DoubleToken 2.0]
    set valToken [java::new {ptolemy.data.ArrayToken ptolemy.data.type.Type} \
		      [$val0 getType]]
    list [$valToken toString] [$valToken isNil]
} {{{}} 0}

######################################################################
####
# 
test ArrayToken-1.8 {Create an array of nils} {
    set valToken [java::new {ptolemy.data.ArrayToken String} "{nil, nil, nil}"]
    set first [$valToken getElement 1]
    list [$valToken toString] [[$first getType] toString]
} {{{nil, nil, nil}} niltype}

######################################################################
####
# 
test ArrayToken-2.0 {test add, addReverse, elementAdd} {
    set t1 [java::new {ptolemy.data.ArrayToken String} "{1, 2, 3}"]
    set t2 [java::new {ptolemy.data.ArrayToken String} "{4, 5, 6}"]
    set t3 [java::new {ptolemy.data.IntToken String} "5"]
    set t4 [java::new {ptolemy.data.StringToken String} "foo"]
    set t5 [java::new {ptolemy.data.IntMatrixToken String} {[1, 2, 3]}]
    set tadd [$t1 add $t2]
    set tadd1 [$t1 addReverse $t2]
    set tadd2 [$t1 elementAdd $t3]
    set tadd3 [$t4 add $t1]
    set tadd4 [$t1 add $t4]
    set tadd5 [$t5 add $t1]
    set tadd6 [$t1 add $t5]
    list [$tadd toString] [$tadd1 toString] [$tadd2 toString] [$tadd3 toString] [$tadd4 toString] [$tadd5 toString] [$tadd6 toString]
} {{{5, 7, 9}} {{5, 7, 9}} {{6, 7, 8}} {{"foo1", "foo2", "foo3"}} {{"1foo", "2foo", "3foo"}} {{[2, 3, 4], [3, 4, 5], [4, 5, 6]}} {{[2, 3, 4], [3, 4, 5], [4, 5, 6]}}}

######################################################################
####
# 
test ArrayToken-2.0.1 {test add typ errors} {
    # Cover blocks in AbstractNotConvertibleToken
    set t1 [java::new {ptolemy.data.ArrayToken String} "{1, 2, 3}"]
    set t [java::new ptolemy.data.Token] 
    catch {$t1 add $t} errMsg1
    catch {$t1 addReverse $t} errMsg2
    catch {$t1 elementAdd $t} errMsg3
    list "$errMsg1\n $errMsg2\n $errMsg3"
} {{ptolemy.kernel.util.IllegalActionException: add method not supported between ptolemy.data.ArrayToken '{1, 2, 3}' and ptolemy.data.Token 'present' because the tokens have different classes.
 ptolemy.kernel.util.IllegalActionException: addReverse operation not supported between ptolemy.data.ArrayToken '{1, 2, 3}' and ptolemy.data.Token 'present'
Because:
addReverse operation not supported between ptolemy.data.ArrayToken '{1, 2, 3}' and ptolemy.data.Token 'present'
 ptolemy.kernel.util.IllegalActionException: elementAdd operation not supported between ptolemy.data.ArrayToken '{1, 2, 3}' and ptolemy.data.Token 'present'
Because:
addReverse operation not supported between ptolemy.data.Token 'present' and ptolemy.data.IntToken '1'}}

######################################################################
####
# 
test ArrayToken-2.0.2 {test add length errors} {
    # Cover blocks in AbstractNotConvertibleToken
    set t1 [java::new {ptolemy.data.ArrayToken String} "{1, 2, 3}"]
    set t2 [java::new {ptolemy.data.ArrayToken String} "{3, 4}"]
    catch {$t1 add $t2} errMsg1
    catch {$t1 addReverse $t2} errMsg2
    set result [$t1 elementAdd $t2]
    list "$errMsg1\n    $errMsg2\n    [$result toString]"
} {{ptolemy.kernel.util.IllegalActionException: add operation not supported between ptolemy.data.ArrayToken '{1, 2, 3}' and ptolemy.data.ArrayToken '{3, 4}'
Because:
The length of the argument (2) is not the same as the length of this token (3).
    ptolemy.kernel.util.IllegalActionException: addReverse operation not supported between ptolemy.data.ArrayToken '{1, 2, 3}' and ptolemy.data.ArrayToken '{3, 4}'
Because:
add operation not supported between ptolemy.data.ArrayToken '{3, 4}' and ptolemy.data.ArrayToken '{1, 2, 3}'
Because:
The length of the argument (3) is not the same as the length of this token (2).
    {{4, 5}, {5, 6}, {6, 7}}}}
    
######################################################################
####
# 
test ArrayToken-2.0.2 {test add with a nil} {
    set t1 [java::new {ptolemy.data.ArrayToken String} "{1, 2, nil}"]
    set t2 [java::new {ptolemy.data.ArrayToken String} "{4, 5, 6}"]
    set t3 [java::new {ptolemy.data.IntToken String} "5"]
    set tadd [$t1 add $t2]
    set tadd2 [$t1 elementAdd $t3]
    list [$tadd toString] [$tadd2 toString]
} {{{5, 7, nil}} {{6, 7, nil}}}

######################################################################
####
# 
test ArrayToken-2.1 {test subtract} {
    set t1 [java::new {ptolemy.data.ArrayToken String} "{nil, 2, 3}"]
    set t2 [java::new {ptolemy.data.ArrayToken String} "{0.5, 1.5, 6.0}"]
    set t3 [java::new {ptolemy.data.IntToken String} "5"]
    set tadd [$t1 subtract $t2]
    set tadd1 [$t1 subtractReverse $t2]
    set tadd2 [$t1 elementSubtract $t3]
    list [$tadd toString] [$tadd1 toString] [$tadd2 toString]
} {{{nil, 0.5, -3.0}} {{nil, -0.5, 3.0}} {{nil, -3, -2}}}

######################################################################
####
# 
test ArrayToken-2.1.1 {test subtract errors} {
    # Cover blocks in AbstractNotConvertibleToken
    set t1 [java::new {ptolemy.data.ArrayToken String} "{1, 2, 3}"]
    set t [java::new ptolemy.data.Token] 
    catch {$t1 subtract $t} errMsg1
    catch {$t1 subtractReverse $t} errMsg2
    catch {$t1 elementSubtract $t} errMsg3
    list "$errMsg1\n $errMsg2\n $errMsg3"
} {{ptolemy.kernel.util.IllegalActionException: subtract operation not supported between ptolemy.data.ArrayToken '{1, 2, 3}' and ptolemy.data.Token 'present'
Because:
subtractReverse operation not supported between ptolemy.data.Token 'present' and ptolemy.data.IntToken '1'
 ptolemy.kernel.util.IllegalActionException: divide operation not supported between ptolemy.data.ArrayToken '{1, 2, 3}' and ptolemy.data.Token 'present'
Because:
subtract operation not supported between ptolemy.data.Token 'present' and ptolemy.data.IntToken '1'
 ptolemy.kernel.util.IllegalActionException: elementSubtract operation not supported between ptolemy.data.ArrayToken '{1, 2, 3}' and ptolemy.data.Token 'present'
Because:
subtractReverse operation not supported between ptolemy.data.Token 'present' and ptolemy.data.IntToken '1'}}

######################################################################
####
# 
test ArrayToken-2.1.2 {test subtract length errors} {
    # Cover blocks in AbstractNotConvertibleToken
    set t1 [java::new {ptolemy.data.ArrayToken String} "{1, 2, 3}"]
    set t2 [java::new {ptolemy.data.ArrayToken String} "{3, 4}"]
    catch {$t1 subtract $t2} errMsg1
    catch {$t1 subtractReverse $t2} errMsg2
    set result [$t1 elementSubtract $t2]
    list "$errMsg1\n    $errMsg2\n    [$result toString]"
} {{ptolemy.kernel.util.IllegalActionException: subtract operation not supported between ptolemy.data.ArrayToken '{1, 2, 3}' and ptolemy.data.ArrayToken '{3, 4}'
Because:
The length of the argument (2) is not the same as the length of this token (3).
    ptolemy.kernel.util.IllegalActionException: divide operation not supported between ptolemy.data.ArrayToken '{1, 2, 3}' and ptolemy.data.ArrayToken '{3, 4}'
Because:
subtract operation not supported between ptolemy.data.ArrayToken '{3, 4}' and ptolemy.data.ArrayToken '{1, 2, 3}'
Because:
The length of the argument (3) is not the same as the length of this token (2).
    {{-2, -3}, {-1, -2}, {0, -1}}}}

######################################################################
####
# 
test ArrayToken-2.2 {test multiply} {
    set t1 [java::new {ptolemy.data.ArrayToken String} "{1, nil, 3}"]
    set t2 [java::new {ptolemy.data.ArrayToken String} "{0.5, 1.5, 6.0}"]
    set t3 [java::new {ptolemy.data.IntToken String} "5"]
    set tadd [$t1 multiply $t2]
    set tadd1 [$t1 multiplyReverse $t2]
    set tadd2 [$t1 elementMultiply $t3]
    list [$tadd toString] [$tadd1 toString] [$tadd2 toString]
} {{{0.5, nil, 18.0}} {{0.5, nil, 18.0}} {{5, nil, 15}}}

######################################################################
####
# 
test ArrayToken-2.2.1 {test multiply errors} {
    # Cover blocks in AbstractNotConvertibleToken
    set t1 [java::new {ptolemy.data.ArrayToken String} "{1, 2, 3}"]
    set t [java::new ptolemy.data.Token] 
    catch {$t1 multiply $t} errMsg1
    catch {$t1 multiplyReverse $t} errMsg2
    catch {$t1 elementMultiply $t} errMsg3
    list "$errMsg1\n $errMsg2\n $errMsg3"
} {{ptolemy.kernel.util.IllegalActionException: multiply operation not supported between ptolemy.data.ArrayToken '{1, 2, 3}' and ptolemy.data.Token 'present'
Because:
multiplyReverse operation not supported between ptolemy.data.Token 'present' and ptolemy.data.IntToken '1'
 ptolemy.kernel.util.IllegalActionException: multiplyReverse operation not supported between ptolemy.data.ArrayToken '{1, 2, 3}' and ptolemy.data.Token 'present'
Because:
multiplyReverse operation not supported between ptolemy.data.Token 'present' and ptolemy.data.IntToken '1'
 ptolemy.kernel.util.IllegalActionException: elementMultiply operation not supported between ptolemy.data.ArrayToken '{1, 2, 3}' and ptolemy.data.Token 'present'
Because:
multiplyReverse operation not supported between ptolemy.data.Token 'present' and ptolemy.data.IntToken '1'}}

######################################################################
####
# 
test ArrayToken-2.2.2 {test multiply length errors} {
    # Cover blocks in AbstractNotConvertibleToken
    set t1 [java::new {ptolemy.data.ArrayToken String} "{1, 2, 3}"]
    set t2 [java::new {ptolemy.data.ArrayToken String} "{3, 4}"]
    catch {$t1 multiply $t2} errMsg1
    catch {$t1 multiplyReverse $t2} errMsg2
    catch {$t1 elementMultiply $t2} errMsg3
    list "$errMsg1\n    $errMsg2"
} {{ptolemy.kernel.util.IllegalActionException: multiply operation not supported between ptolemy.data.ArrayToken '{1, 2, 3}' and ptolemy.data.ArrayToken '{3, 4}'
Because:
The length of the argument (2) is not the same as the length of this token (3).
    ptolemy.kernel.util.IllegalActionException: multiplyReverse operation not supported between ptolemy.data.ArrayToken '{1, 2, 3}' and ptolemy.data.ArrayToken '{3, 4}'
Because:
The length of the argument (2) is not the same as the length of this token (3).}}

######################################################################
####
# 
test ArrayToken-2.2.2.1 {test elementMultiply by array} {
    # Cover blocks in AbstractNotConvertibleToken
    set t1 [java::new {ptolemy.data.ArrayToken String} "{1, 2, 3}"]
    set t2 [java::new {ptolemy.data.ArrayToken String} "{3, 4}"]
    set result [$t1 elementMultiply $t2]
    list [$result toString]
} {{{{3, 4}, {6, 8}, {9, 12}}}}

######################################################################
####
#
test ArrayToken-2.2.3 {test multiply by a scalar} {
    set t1 [java::new {ptolemy.data.ArrayToken String} "{1, 3, 3}"]
    set t2 [java::new {ptolemy.data.DoubleToken String} "2.0"]
    set tadd [$t1 multiply $t2]
    list [$tadd toString]
} {{{2.0, 6.0, 6.0}}}

######################################################################
####
#
test ArrayToken-2.2.4 {test multiply by a array} {
    set t1 [java::new {ptolemy.data.ArrayToken String} "{{1.0, 2.0}, {3.0, 1.0}}"]
    set t2 [java::new {ptolemy.data.ArrayToken String} "{0.5, 2.0}"]
    set tadd [$t1 multiply $t2]
    set tadd1 [$t1 multiplyReverse $t2]
    list [$tadd toString] [$tadd1 toString]
} {{{{0.5, 1.0}, {6.0, 2.0}}} {{{0.5, 1.0}, {6.0, 2.0}}}}

######################################################################
####
#
test ArrayToken-2.3 {test divide} {
    set t1 [java::new {ptolemy.data.ArrayToken String} "{1, 3, 3}"]
    set t2 [java::new {ptolemy.data.ArrayToken String} "{0.5, 1.5, 6.0}"]
    set t3 [java::new {ptolemy.data.IntToken String} "5"]
    set tadd [$t1 divide $t2]
    set tadd1 [$t1 divideReverse $t2]
    set tadd2 [$t1 elementDivide $t3]
    list [$tadd toString] [$tadd1 toString] [$tadd2 toString]
} {{{2.0, 2.0, 0.5}} {{0.5, 0.5, 2.0}} {{0, 0, 0}}}

######################################################################
####
# 
test ArrayToken-2.3.1 {test divide type errors} {
    # Cover blocks in AbstractNotConvertibleToken
    set t1 [java::new {ptolemy.data.ArrayToken String} "{1, 2, 3}"]
    set t [java::new ptolemy.data.Token] 
    catch {$t1 divide $t} errMsg1
    catch {$t1 divideReverse $t} errMsg2
    catch {$t1 elementDivide $t} errMsg3
    list "$errMsg1\n $errMsg2\n $errMsg3"
} {{ptolemy.kernel.util.IllegalActionException: divide operation not supported between ptolemy.data.ArrayToken '{1, 2, 3}' and ptolemy.data.Token 'present'
Because:
divideReverse operation not supported between ptolemy.data.Token 'present' and ptolemy.data.IntToken '1'
 ptolemy.kernel.util.IllegalActionException: divide operation not supported between ptolemy.data.ArrayToken '{1, 2, 3}' and ptolemy.data.Token 'present'
Because:
divide operation not supported between ptolemy.data.Token 'present' and ptolemy.data.IntToken '1'
 ptolemy.kernel.util.IllegalActionException: elementDivide operation not supported between ptolemy.data.ArrayToken '{1, 2, 3}' and ptolemy.data.Token 'present'
Because:
divideReverse operation not supported between ptolemy.data.Token 'present' and ptolemy.data.IntToken '1'}}

######################################################################
####
#
test ArrayToken-2.3.2 {test divide by a scalar} {
    set t1 [java::new {ptolemy.data.ArrayToken String} "{1, 3, 3}"]
    set t2 [java::new {ptolemy.data.DoubleToken String} "2.0"]
    set tadd [$t1 divide $t2]
    list [$tadd toString]
} {{{0.5, 1.5, 1.5}}}

######################################################################
####
#
test ArrayToken-2.3.3 {test divide by a array} {
    set t1 [java::new {ptolemy.data.ArrayToken String} "{{1.0, 2.0}, {3.0, 1.0}}"]
    set t2 [java::new {ptolemy.data.ArrayToken String} "{0.5, 2.0}"]
    set tadd [$t1 divide $t2]
    list [$tadd toString]
} {{{{2.0, 4.0}, {1.5, 0.5}}}}

######################################################################
####
# 
test ArrayToken-2.3.4 {test divide length errors} {
    # Cover blocks in AbstractNotConvertibleToken
    set t1 [java::new {ptolemy.data.ArrayToken String} "{1, 2, 3}"]
    set t2 [java::new {ptolemy.data.ArrayToken String} "{3, 4}"]
    catch {$t1 divide $t2} errMsg1
    list "$errMsg1"
} {{ptolemy.kernel.util.IllegalActionException: divide operation not supported between ptolemy.data.ArrayToken '{1, 2, 3}' and ptolemy.data.ArrayToken '{3, 4}'
Because:
The length of the argument (2) is not the same as the length of this token (3).}}

######################################################################
####
# 
test ArrayToken-2.4 {test modulo} {
    set t1 [java::new {ptolemy.data.ArrayToken String} "{-1, 1, 5}"]
    set t2 [java::new {ptolemy.data.ArrayToken String} "{3.0, 3.0, -3.0}"]
    set t3 [java::new {ptolemy.data.IntToken String} "5"]
    set tadd [$t1 modulo $t2]
    set tadd1 [$t1 moduloReverse $t2]
    set tadd2 [$t1 elementModulo $t3]
    list [$tadd toString] [$tadd1 toString] [$tadd2 toString]
} {{{-1.0, 1.0, 2.0}} {{0.0, 0.0, -3.0}} {{-1, 1, 0}}}

######################################################################
####
# 
test ArrayToken-2.4.1 {test modulo errors} {
    # Cover blocks in AbstractNotConvertibleToken
    set t1 [java::new {ptolemy.data.ArrayToken String} "{1, 2, 3}"]
    set t [java::new ptolemy.data.Token] 
    catch {$t1 modulo $t} errMsg1
    catch {$t1 moduloReverse $t} errMsg2
    catch {$t1 elementModulo $t} errMsg3
    list "$errMsg1\n $errMsg2\n $errMsg3"
} {{ptolemy.kernel.util.IllegalActionException: modulo operation not supported between ptolemy.data.ArrayToken '{1, 2, 3}' and ptolemy.data.Token 'present'
Because:
moduloReverse operation not supported between ptolemy.data.Token 'present' and ptolemy.data.IntToken '1'
 ptolemy.kernel.util.IllegalActionException: modulo operation not supported between ptolemy.data.ArrayToken '{1, 2, 3}' and ptolemy.data.Token 'present'
Because:
modulo operation not supported between ptolemy.data.Token 'present' and ptolemy.data.IntToken '1'
 ptolemy.kernel.util.IllegalActionException: elementModulo operation not supported between ptolemy.data.ArrayToken '{1, 2, 3}' and ptolemy.data.Token 'present'
Because:
moduloReverse operation not supported between ptolemy.data.Token 'present' and ptolemy.data.IntToken '1'}}

######################################################################
####
# 
test ArrayToken-2.4.2 {test modulo length errors} {
    # Cover blocks in AbstractNotConvertibleToken
    set t1 [java::new {ptolemy.data.ArrayToken String} "{1, 2, 3}"]
    set t2 [java::new {ptolemy.data.ArrayToken String} "{3, 4}"]
    catch {$t1 modulo $t2} errMsg1
    catch {$t1 moduloReverse $t2} errMsg2
    # This is now allowed.
    set result [$t1 elementModulo $t2]
    list "$errMsg1\n    $errMsg2\n[$result toString]"
} {{ptolemy.kernel.util.IllegalActionException: modulo operation not supported between ptolemy.data.ArrayToken '{1, 2, 3}' and ptolemy.data.ArrayToken '{3, 4}'
Because:
The length of the argument (2) is not the same as the length of this token (3).
    ptolemy.kernel.util.IllegalActionException: modulo operation not supported between ptolemy.data.ArrayToken '{1, 2, 3}' and ptolemy.data.ArrayToken '{3, 4}'
Because:
modulo operation not supported between ptolemy.data.ArrayToken '{3, 4}' and ptolemy.data.ArrayToken '{1, 2, 3}'
Because:
The length of the argument (3) is not the same as the length of this token (2).
{{1, 1}, {2, 2}, {0, 3}}}}
    
######################################################################
####
# 
test ArrayToken-3.0 {test equals on an array of Doubles} {
    set t1 [java::new {ptolemy.data.ArrayToken String} "{0.5, 1.5, 6.0}"]
    set t2 [java::new {ptolemy.data.ArrayToken String} "{0.5, 1.5, 6.0}"]
    set t3 [java::new {ptolemy.data.ArrayToken String} "{0.5, 1.5, -6.0}"]
    set t4 [java::new {ptolemy.data.ArrayToken String} "{0.5, 1.5, nil}"]

    list [$t1 equals $t1] [$t1 equals $t2] [$t1 equals $t3] [$t4 equals $t4]
} {1 1 0 0}

######################################################################
####
# 
test ArrayToken-3.1 {test hashCode on an array of Doubles} {
    set t1 [java::new {ptolemy.data.ArrayToken String} "{0.5, 1.5, 6.0}"]
    set t2 [java::new {ptolemy.data.ArrayToken String} "{0.5, 1.5, 6.0}"]
    set t3 [java::new {ptolemy.data.ArrayToken String} "{0.5, 1.5, -6.0}"]


    list [$t1 hashCode] [$t2 hashCode] [$t3 hashCode]
} {0 0 0}

######################################################################
####
# 
test ArrayToken-3.2 {test isEqualTo and isCloseTo on an array of Doubles} {
    set t1 [java::new {ptolemy.data.ArrayToken String} "{0.5, 1.5, 6.0}"]
    set t2 [java::new {ptolemy.data.ArrayToken String} "{0.5, 1.5, 6.0}"]
    set t3 [java::new {ptolemy.data.ArrayToken String} "{0.5, 1.5, -6.0}"]
    set t4 [java::new {ptolemy.data.ArrayToken String} "{0.5, 1.5, nil}"]

    set res1 [$t1 {isEqualTo} $t1]
    set res2 [$t1 {isEqualTo} $t2]
    set res3 [$t1 {isEqualTo} $t3]
    set res4 [$t1 {isCloseTo} $t1]
    set res5 [$t1 {isCloseTo} $t2]
    set res6 [$t1 {isCloseTo} $t3]
    set res7 [$t4 {isCloseTo} $t4]
    list [$res1 toString] [$res2 toString] [$res3 toString] \
	    [$res4 toString] [$res5 toString] [$res6 toString] [$res7 toString]

} {true true false true true false false}

######################################################################
####
# 
test ArrayToken-3.2.1 {test isEqualTo on different types and arrays} {
    set t1 [java::new {ptolemy.data.ArrayToken String} "{0.5, 1.5, 6.0}"]
    set t2 [java::new {ptolemy.data.ArrayToken String} "{0.5, 1.5}" ]
    set t3 [java::new ptolemy.data.Token]

    catch {$t1 {isEqualTo} $t2} errMsg1
    catch {$t2 {isEqualTo} $t1} errMsg2
    catch {$t1 {isEqualTo} $t3} errMsg3
    list "$errMsg1\n   $errMsg2\n   $errMsg3"
} {{ptolemy.kernel.util.IllegalActionException: The length of the argument (2) is not the same as the length of this token (3).
   ptolemy.kernel.util.IllegalActionException: The length of the argument (3) is not the same as the length of this token (2).
   ptolemy.kernel.util.IllegalActionException: isEqualTo method not supported between ptolemy.data.ArrayToken '{0.5, 1.5, 6.0}' and ptolemy.data.Token 'present' because the tokens have different classes.}}


######################################################################
####
#
test ArrayToken-3.3 {test isEqualTo on an array of Complexes} {
    set t1 [java::new {ptolemy.data.ArrayToken String} \
	    "{0.5 - 10.0, 0.0 + 0.0, -10.0 + 10.0}"]

    set t2 [java::new {ptolemy.data.ArrayToken String} \
	    "{0.5 -10.0, 0.0 + 0.0, -10.0 + 10.0}"]
    set t3 [java::new {ptolemy.data.ArrayToken String} \
	    "{0.5 -10.0, 0.0 + 0.0, -10.0 + 10000.0}"]

    set res1 [$t1 {isEqualTo} $t1]
    set res2 [$t1 {isEqualTo} $t2]
    set res3 [$t1 {isEqualTo} $t3]
    set res4 [$t1 {isCloseTo} $t1]
    set res5 [$t1 {isCloseTo} $t2]
    set res6 [$t1 {isCloseTo} $t3]
    list [$res1 toString] [$res2 toString] [$res3 toString] \
	    [$res4 toString] [$res5 toString] [$res6 toString]
} {true true false true true false}


######################################################################
####
# 
test ArrayToken-4.0 {test isCloseTo on an array of Doubles} {
    # A is close to B if abs((a-b)/a)<epsilon  
    set epsilon 0.001
    set oldEpsilon [java::field ptolemy.math.Complex EPSILON]
    java::field ptolemy.math.Complex EPSILON $epsilon

    set t1 [java::new {ptolemy.data.ArrayToken String} "{0.5, 1.5, 6.0}"]

    set a [expr {0.5 - 0.05 * $epsilon}]
    set b [expr {1.5 + 0.05 * $epsilon}]
    set c [expr {6.0 + 0.05 * $epsilon}]
    set t2 [java::new {ptolemy.data.ArrayToken String} "{$a, $b, $c}"]

    set d [expr {0.5 - 2.0 * $epsilon}]
    set e [expr {1.5 + 2.0 * $epsilon}]
    set f [expr {6.0 + 2.0 * $epsilon}]
    set t3 [java::new {ptolemy.data.ArrayToken String} "{$d, $e, $f} "]

    set res1 [$t1 {isEqualTo} $t1]
    set res2 [$t1 {isEqualTo} $t2]
    set res3 [$t1 {isEqualTo} $t3]
    set res4 [$t1 {isCloseTo} $t1]
    set res5 [$t1 {isCloseTo} $t2]
    set res6 [$t1 {isCloseTo} $t3]

    java::field ptolemy.math.Complex EPSILON $oldEpsilon

    list [$res1 toString] [$res2 toString] [$res3 toString] \
	    [$res4 toString] [$res5 toString] [$res6 toString]
} {true false false true true false}

######################################################################
####
#
test ArrayToken-4.1 {test isCloseTo on an array of Complexes} {
    set epsilon 0.001
    set oldEpsilon [java::field ptolemy.math.Complex EPSILON]
    java::field ptolemy.math.Complex EPSILON $epsilon

    set t1 [java::new {ptolemy.data.ArrayToken String} \
	    "{0.5 - 10.0, 0.0 + 0.0, -10.0 + 10.0}"]

    set t2 [java::new {ptolemy.data.ArrayToken String} \
	    "{0.5 -10.0, 0.0 + 0.0, -10.0 + 10.0}"]
    set t3 [java::new {ptolemy.data.ArrayToken String} \
	    "{0.5 -10.0, 0.0 + 0.0, -10.0 + 10000.0}"]

    set res1 [$t1 {isEqualTo} $t1]
    set res2 [$t1 {isEqualTo} $t2]
    set res3 [$t1 {isEqualTo} $t3]
    set res4 [$t1 {isCloseTo} $t1]
    set res5 [$t1 {isCloseTo} $t2]
    set res6 [$t1 {isCloseTo} $t3]

    java::field ptolemy.math.Complex EPSILON $oldEpsilon

    list [$res1 toString] [$res2 toString] [$res3 toString] \
	    [$res4 toString] [$res5 toString] [$res6 toString]
} {true true false true true false}

######################################################################
####
#
test ArrayToken-4.2 {test isCloseTo on two different types} {
    # Cover blocks in AbstractNotConvertibleToken
    set t1 [java::new {ptolemy.data.ArrayToken String} "{1, 2}"]
    set t2 [java::new ptolemy.data.IntToken 1]
    catch {$t1 {isEqualTo} $t2} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: isEqualTo method not supported between ptolemy.data.ArrayToken '{1, 2}' and ptolemy.data.IntToken '1' because the tokens have different classes.}}

######################################################################
####
# 
test ArrayToken-5.1 {Construct an array of doubles with a nil} {
    set t [java::new {ptolemy.data.ArrayToken String} "{1.0, nil, 2.0, NaN}"]
    set nilToken [$t getElement 1]
    #set nilDoubleToken [java::cast ptolemy.data.DoubleToken $nilToken]
    list [$t toString] [$nilToken toString] \
	[[$nilToken getType] toString]
} {{{1.0, nil, 2.0, NaN}} nil double}

######################################################################
####
# 
test ArrayToken-5.2 {Construct an array of doubles with a nil as 1st element} {
    set t [java::new {ptolemy.data.ArrayToken String} "{nil, 2.0}"]
    set nilToken [$t getElement 0]
    #set nilDoubleToken [java::cast ptolemy.data.DoubleToken $nilToken]
    list [$t toString] [$nilToken toString] \
	[[$nilToken getType] toString]

} {{{nil, 2.0}} nil double}

######################################################################
####
# 
test ArrayToken-6.1 {Construct an array of ints with a nil} {
    set t [java::new {ptolemy.data.ArrayToken String} "{1, nil, 2}"]
    set nilToken [$t getElement 1]
    #set nilIntToken [java::cast ptolemy.data.IntToken $nilToken]
    list [$t toString] [$nilToken toString] \
	[[$nilToken getType] toString]

} {{{1, nil, 2}} nil int}

######################################################################
####
# 
test ArrayToken-6.2 {Construct an array of int with a nil as 1st element} {
    set t [java::new {ptolemy.data.ArrayToken String} "{nil, 2}"]
    set nilToken [$t getElement 0]
    #set nilIntToken [java::cast ptolemy.data.IntToken $nilToken]
    list [$t toString] [$nilToken toString] \
	[[$nilToken getType] toString]
} {{{nil, 2}} nil int}

######################################################################
####
# 
test ArrayToken-7.0 {extract} {
    set t [java::new {ptolemy.data.ArrayToken String} \
	       "{\"red\",\"green\",\"blue\"}"]
    set r1 [$t extract [java::new {ptolemy.data.ArrayToken String} \
			    "{true, false, true}"]]
    set r2 [$t extract [java::new {ptolemy.data.ArrayToken String} \
			    "{2,0,1,1}"]]
    list [$r1 toString] [$r2 toString]
} {{{"red", "blue"}} {{"blue", "red", "green", "green"}}}

######################################################################
####
# 
test ArrayToken-7.1 {extract, wrong length of boolean array} {
    # Uses 7.0 above
    catch {$t extract [java::new {ptolemy.data.ArrayToken String} \
			   "{true}"]} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: When the argument is an array of booleans, it must have the same length as this array.}} 

######################################################################
####
# 
test ArrayToken-7.2 {extract, wrong type} {
    # Uses 7.0 above
    catch {$t extract [java::new {ptolemy.data.ArrayToken String} \
			   "{1.0}"]} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: The argument must be {boolean} or {int}.}} 

######################################################################
####
# 
test ArrayToken-7.3 {extract nothing, increase code coverage} {
    set r1 [$t extract [java::new {ptolemy.data.ArrayToken String} \
			    "{false, false, false}"]]
    list [$r1 toString] [[$r1 getElementType] toString]
} {{{}} string}

######################################################################
####
# 
test ArrayToken-8.0 {getElementType} {
    set valToken [java::new {ptolemy.data.ArrayToken String} "{1, 2, 3}"]
    [$valToken getElementType] toString
} {int}

######################################################################
####
# 
test ArrayToken-9.0 {one} {
    set valToken [java::new {ptolemy.data.ArrayToken String} "{1, 2, 3}"]
    [$valToken one] toString
} {{1, 1, 1}}


######################################################################
####
# 
test ArrayToken-9.0 {subarray} {
    set t [java::new {ptolemy.data.ArrayToken String} "{1, 2, 3, 4}"]
    catch {$t subarray -1 1} errMsg
    set r1 [$t subarray 5 3]
    set r2 [$t subarray 2 2]
    set r3 [$t subarray 1 8]
    list $errMsg [$r1 toString] [$r2 toString] [$r3 toString]
} {{ptolemy.kernel.util.IllegalActionException: index argument of subarray() must be non-negative.} {{}} {{3, 4}} {{2, 3, 4}}}

######################################################################
####
# 
test ArrayToken-10.0 {Construct an ArrayToken with 0 elements: a nil ArrayToken} { 
    set t [java::new {ptolemy.data.ArrayToken String} "{}"]
    list [$t toString] [[$t getType] toString] [[$t getElementType] toString] \
	[[$t zero] toString] [$t isNil]
} {{{}} arrayType(niltype,0) niltype {{}} 0}

test ArrayToken-10.1 {nil ArrayToken equals methods} {
    set t [java::new {ptolemy.data.ArrayToken String} "{}"]
    list [$t equals $t] [$t length] [[$t isEqualTo $t] toString] [$t isNil]
} {1 0 true 0}

######################################################################
####
# 
test ArrayToken-11.0 {Test equals} {
    set t1 [java::new {ptolemy.data.ArrayToken String} "{1}"]
    set t2 [java::new {ptolemy.data.ArrayToken String} "{1}"]
    set t3 [java::new {ptolemy.data.ArrayToken String} "{2}"]
    set t4 [java::new {ptolemy.data.ArrayToken String} "{1, 2}"]
    list [$t1 equals $t1] [$t1 equals $t2] [$t1 equals $t3] [$t3 equals $t4]
} {1 1 0 0}

######################################################################
####
# 
test ArrayToken-11.1 {Test equals on nil} {
    set tu [java::field ptolemy.data.ArrayToken NIL]
    set t2 [java::new {ptolemy.data.ArrayToken String} "{2}"]
    set t [java::field ptolemy.data.Token NIL]
    list [$tu equals $tu] [$tu equals $t2] [$t2 equals $tu] \
	[$t2 equals $t2] [$t equals $tu] [$tu equals $t]
} {0 0 0 1 0 0} 

######################################################################
####
# 
test ArrayToken-12.0 {Test hashCode} {
    set t1 [java::new {ptolemy.data.ArrayToken String} "{1}"]
    set t2 [java::new {ptolemy.data.ArrayToken String} "{2}"]
    set t3 [java::new {ptolemy.data.ArrayToken String} "{3, nil}"]
    list [$t1 hashCode] [$t2 hashCode] [$t3 hashCode]
} {1 2 3}

######################################################################
####
# 
test ArrayToken-20.1 {elementMultiplyReturnType} {
    set valToken [java::new {ptolemy.data.ArrayToken String} "{1, 2, 3}"]
    set t1 [java::call ptolemy.data.ArrayToken elementMultiplyReturnType \
		[$valToken getType] [$valToken getType]]
    set intToken [java::new ptolemy.data.IntToken 2]
    set t2 [java::call ptolemy.data.ArrayToken elementMultiplyReturnType \
		[$valToken getType] [$intToken getType]]
    set t3 [java::call ptolemy.data.ArrayToken elementMultiplyReturnType \
		[$intToken getType] [$valToken getType]]
    list [$t1 toString] [$t2 toString] [$t3 toString]
} {arrayType(arrayType(int)) arrayType(int) arrayType(unknown)}

######################################################################
####
# 
test ArrayToken-21.0 {reverse} {
    set valToken [java::new {ptolemy.data.ArrayToken String} "{1, -1, 2, 0}"]
    set t1 [$valToken reverse]
    $t1 toString
} {{0, 2, -1, 1}}

test ArrayToken-21.1 {reverse, one element} {
    set valToken [java::new {ptolemy.data.ArrayToken String} "{0}"]
    set t1 [$valToken reverse]
    $t1 toString
} {{0}}
