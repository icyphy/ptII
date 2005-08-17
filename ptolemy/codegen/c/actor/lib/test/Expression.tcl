# Run tests on the Expression actor
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2005 The Regents of the University of California.
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

proc parseTreeTest {expression} {
    set ptParser [java::new ptolemy.data.expr.PtParser]
    set parseTree [$ptParser generateParseTree $expression]
    set parseTreeCodeGenerator \
	[java::new ptolemy.codegen.c.actor.lib.ParseTreeCodeGenerator]
    # We have to eval the parse tree first, though we ignore the value
    set token [$parseTreeCodeGenerator evaluateParseTree $parseTree]
    return [list [$token toString] [$parseTreeCodeGenerator generateFireCode]]
}

test Expression-1.1 {Simple tests of ParseTreeCodeGenerator} {
    parseTreeTest {1+3}
} {4 1+3}

test Expression-1.2 {A more complex example} {
    parseTreeTest {((3+4)+(1|2)+232)}
} {242 3+4+1|2+232}

test Expression-2.1 {Define a variable in a regular parse tree } {
    # This test uses the regular (non codegen) parse tree
    # We need to do something similar for codegen
    # See ptII/ptolemy/data/expr/test/PtParser.tcl
    set namedList [java::new ptolemy.kernel.util.NamedList]
    set variableA [java::new ptolemy.data.expr.Variable]
    $variableA setName "foo"
    $variableA setExpression "42"
    $namedList prepend $variableA
    set scope [java::new ptolemy.data.expr.ExplicitScope $namedList]    
    set evaluator [java::new ptolemy.data.expr.ParseTreeEvaluator]

    #set res1  [ $evaluator evaluateParseTree $root1 $scope]
    set ptParser [java::new ptolemy.data.expr.PtParser]
    set root [ $ptParser generateStringParseTree {1+$foo} ]
    set results  [ $evaluator evaluateParseTree $root $scope]
    list [$results toString]

} {{"1+42"}}


test Expression-2.2 {Define a variable in a codegen parse tree } {
    # We need to do something similar for codegen
    # See ptII/ptolemy/data/expr/test/PtParser.tcl
    set namedList [java::new ptolemy.kernel.util.NamedList]
    set variableA [java::new ptolemy.data.expr.Variable]
    $variableA setName "foo"
    $variableA setExpression "42"
    $namedList prepend $variableA
    set scope [java::new ptolemy.data.expr.ExplicitScope $namedList]    
    #set evaluator [java::new ptolemy.data.expr.ParseTreeEvaluator]
    set evaluator \
	[java::new ptolemy.codegen.c.actor.lib.ParseTreeCodeGenerator]

    #set res1  [ $evaluator evaluateParseTree $root1 $scope]
    set ptParser [java::new ptolemy.data.expr.PtParser]
    set root [ $ptParser generateStringParseTree {1+$foo} ]
    set results  [ $evaluator evaluateParseTree $root $scope]
    list [$results toString] [$evaluator generateFireCode]

} {{"1+42"} {"1+42"}}
