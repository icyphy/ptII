# Test MathFunction
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2000-2003 The Regents of the University of California.
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

######################################################################
####
#
test MathFunction-1.1 {test constructor and clone} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set mathFunctionbase [java::new ptolemy.actor.lib.MathFunction $e0 \
	    MathFunction]
    set mathFunction [java::cast ptolemy.actor.lib.MathFunction \
			  [$mathFunctionbase clone [$e0 workspace]]]
    # Success here is just not throwing an exception.
    list {}
} {{}}

######################################################################
#### Test MathFunction in an SDF model
#
test MathFunction-2.1 {test with the default output values} {
    # Uses setup from previous examples
    set e0 [sdfModel 5]
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression {0.0}
    $step setExpression {1.0}
    # Use clone of mathFunction to make sure that is ok.
    $mathFunction setContainer $e0
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $ramp] output] \
       [java::field $mathFunction firstOperand]
    $e0 connect \
       [java::field $mathFunction output] \
       [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    [$e0 getManager] execute
    epsilonDiff [enumToTokenValues [$rec getRecord 0]] \
	    {1.0 2.7182818 7.3890561 20.0855369 54.5981500} \
            0.001
} {}

######################################################################
#### Test MathFunction with log
#
test MathFunction-2.3 {test with log} {
    # Uses setup from MathFunction 1-1 and 2-2
    set function [java::cast ptolemy.data.expr.StringParameter \
	    [$mathFunction getAttribute function]]
    $function setExpression "log"
    $init setExpression {1.0}
    [$e0 getManager] execute
    epsilonDiff [enumToTokenValues [$rec getRecord 0]] \
	    {0.0 0.6931472 1.0986123 1.3862944 1.6094379} \
            0.001
} {}

######################################################################
#### Test MathFunction with square
#
test MathFunction-2.4 {test with square} {
    # Uses setup from MathFunction 1-1, 2-2 and 2-3
    $function setExpression "square"
    [$e0 getManager] execute
    epsilonDiff [enumToTokenValues [$rec getRecord 0]] \
	    {1.0 4.0 9.0 16.0 25.0} \
            0.001
} {}


######################################################################
#### Test MathFunction with sqrt
#
test MathFunction-2.5 {test with sqrt} {
    # Uses setup from MathFunction 1-1, 2-2 and 2-3
    $function setExpression "sqrt"
    [$e0 getManager] execute
    epsilonDiff [enumToTokenValues [$rec getRecord 0]] \
	    {1.0 1.4142136 1.7320508 2.0 2.236068} \
            0.001
} {}

######################################################################
#### Test MathFunction with modulo
#
test MathFunction-3.1 {test with modulo} {
    # Uses setup from MathFunction 1-1, 2-2 and 2-3
    $function setExpression "modulo"
    # Have to make sure the expression is processed before we attempt
    # to connect to the port whose creation is triggered by changing
    # the parameter value.
    $function validate
    set const [java::new ptolemy.actor.lib.Const $e0 const]
    set value [getParameter $const value]
    $value setExpression {3.0}
    $step setExpression {1.0}

    $e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $const] output] \
       [java::field $mathFunction secondOperand]

    [$e0 getManager] execute
    epsilonDiff [enumToTokenValues [$rec getRecord 0]] \
	    {1.0 2.0 0.0 1.0 2.0} \
            0.001
} {}


######################################################################
#### Test MathFunction with modulo on clone
#
test MathFunction-3.2 {test with modulo on clone} {
    # Uses setup from MathFunction 1.1, 2.2 and 2.3
    set e1 [java::new ptolemy.actor.TypedCompositeActor]
    set e1 [sdfModel 10]

    # 3.1 set the function to modulo.  The clone method
    # should preserve this and should also have a second port
    set mathFunctionClone [java::cast ptolemy.actor.lib.MathFunction \
			       [$mathFunction clone [$e0 workspace]]]
    $mathFunctionClone setContainer $e1

    set rampClone [java::cast ptolemy.actor.lib.Ramp \
		       [$ramp clone [$e0 workspace]]]
    $rampClone setContainer $e1

    set recClone [java::cast ptolemy.actor.lib.Recorder \
		      [$rec clone [$e0 workspace]]]
    $recClone setContainer $e1
    $e1 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $rampClone] output] \
       [java::field $mathFunctionClone firstOperand]

    $e1 connect \
       [java::field $mathFunctionClone output] \
       [java::field [java::cast ptolemy.actor.lib.Sink $recClone] input]

    set constClone [java::cast ptolemy.actor.lib.Const \
			[$const clone [$e0 workspace]]]
    $constClone setContainer $e1

    $e1 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $constClone] output] \
       [java::field $mathFunctionClone secondOperand]


    [$e1 getManager] execute
    epsilonDiff [enumToTokenValues [$recClone getRecord 0]] \
	    {1.0 2.0 0.0 1.0 2.0 0.0 1.0 2.0 0.0 1.0} \
            0.001
} {}
