# Test Expression
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2006-2007 The Regents of the University of California.
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

if {[string compare sdfModel [info procs sdfModel]] != 0} \
        then {
    source [file join $PTII util testsuite models.tcl]
} {}


######################################################################
####
#
test Expression-1.1 { VariableScope.get()} {
    set e0 [sdfModel 3]
    set expressionActor [java::new ptolemy.actor.lib.Expression $e0 expr]
    set testExpression [java::new ptolemy.codegen.c.actor.lib.test.TestExpression $expressionActor]
    set variableScope [java::field $testExpression variableScope]
    list \
	[[$variableScope get time] toString] \
	[[$variableScope get iteration] toString] \
	[java::isnull [$variableScope get foo] ]
} {0.0 {object($actorSymbol(iterationCount))} 1}


######################################################################
####
#
test Expression-2.1 { VariableScope.getType()} {
    # Uses 1.1 above
    list \
	[[$variableScope getType time] toString] \
	[[$variableScope getType iteration] toString] \
	[java::isnull [$variableScope getType foo] ]
} {double int 1}

######################################################################
####
#
test Expression-3.1 { VariableScope.getTypeTerm()} {
    # Uses 1.1 above
    list \
	[[[$variableScope getTypeTerm time] getValue] toString] \
	[[[$variableScope getTypeTerm iteration] getValue] toString] \
	[java::isnull [$variableScope getTypeTerm foo] ]
} {double int 1}

######################################################################
####
#
test Expression-4.1 { VariableScope.identifierSet()} {
    # Uses 1.1 above
    set identifierSet [$variableScope identifierSet]
    list \
	[$identifierSet size]
} {0}

######################################################################
####
#
test Expression-5.1 { VariableScope with a real variable)} {
    set in1 [java::new ptolemy.actor.TypedIOPort $expressionActor \
	 in1 true false]
    set ramp1 [java::new ptolemy.actor.lib.Ramp $e0 ramp1]   
    set r1 [$e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $ramp1] output] \
            $in1]
    set expression [java::field $expressionActor expression]
    $expression setExpression "in1 + 5"
    set identifierSet [$variableScope identifierSet]
    list \
	[[$variableScope get in1] toString] \
	[[$variableScope getType in1] toString] \
	[[[$variableScope getTypeTerm in1] getValue] toString] \
	[$identifierSet size]
} {{object($ref(in1))} unknown unknown 0}

######################################################################
####
#
test Expression-5.2 { VariableScope after running the model} {
    set m [$e0 getManager]
    $m execute
    list \
	[[$variableScope get in1] toString] \
	[[$variableScope getType in1] toString] \
	[[[$variableScope getTypeTerm in1] getValue] toString] \
	[$identifierSet size]
} {{object($ref(in1))} int int 0}
