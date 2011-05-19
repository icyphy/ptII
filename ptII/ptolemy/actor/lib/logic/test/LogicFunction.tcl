# Test LogicFunction.
#
# @Author: John Li
# @Author: Paul Whitaker
#
# @Version: $Id$
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

######################################################################
####
#
test LogicFunction-1.1 {test constructor and clone} {
    set e0 [sdfModel 1]
    set logicbase [java::new ptolemy.actor.lib.logic.LogicFunction $e0 logic]
    set logic [java::cast ptolemy.actor.lib.logic.LogicFunction \
		   [$logicbase clone [$e0 workspace]]]
    $logicbase setContainer [java::null]
    $logic setContainer $e0
    set function [java::cast ptolemy.kernel.util.StringAttribute \
            [$logic getAttribute function]]
    # Success here is just not throwing an exception.
    list {}
} {{}}

######################################################################
#### Major numbers refer to the truth table
#### Minor numbers refer to the logic function (or 0 for setup)
#
test LogicFunction-2.0 {Truth Table: True, True} {
    set in1 [java::new ptolemy.actor.lib.Const $e0 in1]
    set in1value [getParameter $in1 value]
    $in1value setToken [java::new ptolemy.data.BooleanToken true]
    set in2 [java::new ptolemy.actor.lib.Const $e0 in2]
    set in2value [getParameter $in2 value]
    $in2value setToken [java::new ptolemy.data.BooleanToken true]

    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    set input [java::field [java::cast ptolemy.actor.lib.Transformer \
            $logic] input]
    set r1 [$e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $in1] output] \
       $input]
    set r2 [$e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $in2] output] \
       $input]
    $e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Transformer \
            $logic] output] \
       [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    # Success here is just not throwing an exception.
    list {}
} {{}}

test LogicFunction-2.1 {AND: Truth table: True, True} {
    $function setExpression "and"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true}

test LogicFunction-2.2 {OR: Truth table: True, True} {
    $function setExpression "or"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true}

test LogicFunction-2.3 {XOR: Truth table: True, True} {
    $function setExpression "xor"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test LogicFunction-2.4 {NAND: Truth table: True, True} {
    $function setExpression "nand"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test LogicFunction-2.5 {NOR: Truth table: True, True} {
    $function setExpression "nor"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test LogicFunction-2.6 {XNOR: Truth table: True, True} {
    $function setExpression "xnor"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true}

test LogicFunction-2.7 {NotAFunction: test for bad function name} {
    catch {$function setExpression "notAFunction"} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Unrecognized logic function: notafunction.  Valid functions are 'and', 'or', 'xor', 'nand', 'nor', and 'xnor'.
  in .top.logic}}

test LogicFunction-3.0 {Truth table: True, False} {
    $in2value setToken [java::new ptolemy.data.BooleanToken false]
    list {}
} {{}}

test LogicFunction-3.1 {AND: Truth table: True, False} {
    $function setExpression "and"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test LogicFunction-3.2 {OR: Truth table: True, False} {
    $function setExpression "or"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true}

test LogicFunction-3.3 {XOR: Truth table: True, False} {
    $function setExpression "xor"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true}

test LogicFunction-3.4 {NAND: Truth table: True, False} {
    $function setExpression "nand"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true}

test LogicFunction-3.5 {NOR: Truth table: True, False} {
    $function setExpression "nor"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test LogicFunction-3.6 {XNOR: Truth table: True, False} {
    $function setExpression "xnor"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test LogicFunction-4.0 {Truth table: False, False} {
    $in1value setToken [java::new ptolemy.data.BooleanToken false]
    list {}
} {{}}

test LogicFunction-4.1 {AND: Truth table: False, False} {
    $function setExpression "and"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test LogicFunction-4.2 {OR: Truth table: False, False} {
    $function setExpression "or"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test LogicFunction-4.3 {XOR: Truth table: False, False} {
    $function setExpression "xor"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test LogicFunction-4.4 {NAND: Truth table: False, False} {
    $function setExpression "nand"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true}

test LogicFunction-4.5 {NOR: Truth table: False, False} {
    $function setExpression "nor"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true}

test LogicFunction-4.6 {XNOR: Truth table: False, False} {
    $function setExpression "xnor"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true}

test LogicFunction-5.0 {Truth table: False, True} {
    $in2value setToken [java::new ptolemy.data.BooleanToken true]
    list {}
} {{}}

test LogicFunction-5.1 {AND: Truth table: False, True} {
    $function setExpression "and"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test LogicFunction-5.2 {OR: Truth table: False, True} {
    $function setExpression "or"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true}

test LogicFunction-5.3 {XOR: Truth table: False, True} {
    $function setExpression "xor"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true}

test LogicFunction-5.4 {NAND: Truth table: False, True} {
    $function setExpression "nand"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true}

test LogicFunction-5.5 {NOR: Truth table: False, True} {
    $function setExpression "nor"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test LogicFunction-5.6 {XNOR: Truth table: False, True} {
    $function setExpression "xnor"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test LogicFunction-6.0 {Multiple values: False, True, False} {
    set in3 [java::new ptolemy.actor.lib.Const $e0 in3]
    set in3value [getParameter $in3 value]
    $in3value setToken [java::new ptolemy.data.BooleanToken false]

    set r3 [$e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $in3] output] \
       $input]
    list {}
} {{}}

test LogicFunction-6.1 {AND: Multiple values: False, True, False} {
    $function setExpression "and"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test LogicFunction-6.2 {OR: Multiple values: False, True, False} {
    $function setExpression "or"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true}

test LogicFunction-6.3 {XOR: Multiple values: False, True, False} {
    $function setExpression "xor"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true}

test LogicFunction-6.4 {NAND: Multiple values: False, True, False} {
    $function setExpression "nand"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true}

test LogicFunction-6.5 {NOR: Multiple values: False, True, False} {
    $function setExpression "nor"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test LogicFunction-6.6 {XNOR: Multiple values: False, True, False} {
    $function setExpression "xnor"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test LogicFunction-7.0 {Multiple values: True, True, True} {
    $in1value setToken [java::new ptolemy.data.BooleanToken true]
    $in3value setToken [java::new ptolemy.data.BooleanToken true]
    list {}
} {{}}

test LogicFunction-7.1 {AND: Multiple values: True, True, True} {
    $function setExpression "and"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true}

test LogicFunction-7.2 {OR: Multiple values: True, True, True} {
    $function setExpression "or"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true}

test LogicFunction-7.3 {XOR: Multiple values: True, True, True} {
    $function setExpression "xor"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true}

test LogicFunction-7.4 {NAND: Multiple values: True, True, True} {
    $function setExpression "nand"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test LogicFunction-7.5 {NOR: Multiple values: True, True, True} {
    $function setExpression "nor"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test LogicFunction-7.6 {XNOR: Multiple values: True, True, True} {
    $function setExpression "xnor"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test LogicFunction-8.0 {Multiple values: True, True, False} {
    $in3value setToken [java::new ptolemy.data.BooleanToken false]
    list {}
} {{}}

test LogicFunction-8.1 {AND: Multiple values: True, True, False} {
    $function setExpression "and"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test LogicFunction-8.2 {OR: Multiple values: True, True, False} {
    $function setExpression "or"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true}

test LogicFunction-8.3 {XOR: Multiple values: True, True, False} {
    $function setExpression "xor"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test LogicFunction-8.4 {NAND: Multiple values: True, True, False} {
    $function setExpression "nand"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true}

test LogicFunction-8.5 {NOR: Multiple values: True, True, False} {
    $function setExpression "nor"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test LogicFunction-8.6 {XNOR: Multiple values: True, True, False} {
    $function setExpression "xnor"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true}

test LogicFunction-9.0 {Multiple values: False, False, False} {
    $in1value setToken [java::new ptolemy.data.BooleanToken false]
    $in2value setToken [java::new ptolemy.data.BooleanToken false]
    list {}
} {{}}

test LogicFunction-9.1 {AND: Multiple values: False, False, False} {
    $function setExpression "and"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test LogicFunction-9.2 {OR: Multiple values: False, False, False} {
    $function setExpression "or"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test LogicFunction-9.3 {XOR: Multiple values: False, False, False} {
    $function setExpression "xor"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test LogicFunction-9.4 {NAND: Multiple values: False, False, False} {
    $function setExpression "nand"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true}

test LogicFunction-9.5 {NOR: Multiple values: False, False, False} {
    $function setExpression "nor"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true}

test LogicFunction-9.6 {XNOR: Multiple values: False, False, False} {
    $function setExpression "xnor"
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true}


