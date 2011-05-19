# Test Pulse
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997-2007 The Regents of the University of California.
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
test Pulse-1.1 {test constructor and clone with default values} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set pulse [java::new ptolemy.actor.lib.Pulse $e0 pulse]

    set values [getParameter $pulse values]
    set indexes [getParameter $pulse indexes]

    set newObject [java::cast ptolemy.actor.lib.Pulse \
		       [$pulse clone [$e0 workspace]]]
    set newValues [getParameter $newObject values]
    set newIndexes [getParameter $newObject indexes]
    set valuesVal [[$newValues getToken] toString]
    set indexesVal [[$newIndexes getToken] toString]

    list $valuesVal $indexesVal
} {{{1, 0}} {{0, 1}}}

######################################################################
#### Check type of values parameter
#
test Pulse-1.2 {check type} {
    list [[$values getType] toString] [[$newValues getType] toString]
} {arrayType(int,2) arrayType(int,2)}

######################################################################
#### Test Pulse in an SDF model
#
test Pulse-2.1 {test with the default output values} {
    set e0 [sdfModel 5]
    set pulse [java::new ptolemy.actor.lib.Pulse $e0 pulse]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $pulse] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {1 0 0 0 0}

test Pulse-2.2 {test with the non-default output values} {
    set val0 [java::new {ptolemy.data.DoubleToken double} 0.1]
    set val1 [java::new {ptolemy.data.DoubleToken double} 0.2]
    set val2 [java::new {ptolemy.data.DoubleToken double} 0.3]
    set valArray [java::new {ptolemy.data.Token[]} 3 [list $val0 $val1 $val2]]
    set valToken [java::new {ptolemy.data.ArrayToken} $valArray]

    set valuesParam [getParameter $pulse values]
    $valuesParam setToken $valToken
 
    set indexesParam [getParameter $pulse indexes]
    $indexesParam setExpression {{0, 2, 3}}

    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {0.1 0.0 0.2 0.3 0.0}

test Pulse-2.3 {test using setExpression} {
    $valuesParam setExpression {{5l, 6l, 7l}}
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {5L 0L 6L 7L 0L}

test Pulse-2.4 {test with two-dimensional output values} {
    set valuesParam [getParameter $pulse values]
    $valuesParam setExpression {{{1, 2}, {3, 4}, {5, 6}}}

    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {{{1, 2}} {{0, 0}} {{3, 4}} {{5, 6}} {{0, 0}}}

test Pulse-2.5 {test string output} {
    set val0 [java::new ptolemy.data.StringToken AB]
    set val1 [java::new ptolemy.data.StringToken CD]
    set val2 [java::new ptolemy.data.StringToken EF]
    set valArray [java::new {ptolemy.data.Token[]} 3 [list $val0 $val1 $val2]]
    set valToken [java::new {ptolemy.data.ArrayToken} $valArray]

    set valuesParam [getParameter $pulse values]
    $valuesParam setToken $valToken
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {{"AB"} {""} {"CD"} {"EF"} {""}} 

######################################################################
#### Test error conditions
#
test Pulse-3.1 {test indexes that are out of order} {
    set indexesParam [getParameter $pulse indexes]
    catch {
        $indexesParam setExpression {{0, 3, 2}}
        $indexesParam getToken
    } msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Error evaluating expression: {0, 3, 2}
  in .top.pulse.indexes
Because:
Value of indexes is not nondecreasing and nonnegative.
  in .top.pulse}}

test Pulse-3.2 {test negative indexes} {
    set indexesParam [getParameter $pulse indexes]
    catch {
        $indexesParam setExpression {{-1, 0, 1}}
        $indexesParam getToken
    } msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Error evaluating expression: {-1, 0, 1}
  in .top.pulse.indexes
Because:
Value of indexes is not nondecreasing and nonnegative.
  in .top.pulse}}

test Pulse-3.3 {test values and indexes of different dimensions} {
    set indexesParam [getParameter $pulse indexes]
    $indexesParam setExpression {{1, 2, 3}}
    set valuesParam [getParameter $pulse values]
    $valuesParam setExpression {{{0, 3}}}
    catch {
        [$e0 getManager] execute
    } msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Parameters values and indexes have different lengths.  Length of values = 1. Length of indexes = 3.
  in .top.pulse}}

######################################################################
#### Test repeat function

test Pulse-4.1 {test with the default output values} {
    set e0 [sdfModel 15]
    set pulse [java::new ptolemy.actor.lib.Pulse $e0 pulse]

    set repeatParam [getParameter $pulse repeat]
    $repeatParam setToken [java::new ptolemy.data.BooleanToken true]

    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $pulse] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {1 0 1 0 1 0 1 0 1 0 1 0 1 0 1}

test Pulse-4.2 {test with the non-default output values} {
    set val0 [java::new {ptolemy.data.DoubleToken double} 0.1]
    set val1 [java::new {ptolemy.data.DoubleToken double} 0.2]
    set val2 [java::new {ptolemy.data.DoubleToken double} 0.3]
    set valArray [java::new {ptolemy.data.Token[]} 3 [list $val0 $val1 $val2]]
    set valToken [java::new {ptolemy.data.ArrayToken} $valArray]

    set valuesParam [getParameter $pulse values]
    $valuesParam setToken $valToken
 
    set indexesParam [getParameter $pulse indexes]
    $indexesParam setExpression {{0, 2, 3}}

    set repeatParam [getParameter $pulse repeat]
    $repeatParam setToken [java::new ptolemy.data.BooleanToken true]

    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {0.1 0.0 0.2 0.3 0.1 0.0 0.2 0.3 0.1 0.0 0.2 0.3 0.1 0.0 0.2}

test Pulse-4.3 {test string output} {
    set val0 [java::new ptolemy.data.StringToken AB]
    set val1 [java::new ptolemy.data.StringToken CD]
    set val2 [java::new ptolemy.data.StringToken EF]
    set valArray [java::new {ptolemy.data.Token[]} 3 [list $val0 $val1 $val2]]
    set valToken [java::new {ptolemy.data.ArrayToken} $valArray]

    set repeatParam [getParameter $pulse repeat]
    $repeatParam setToken [java::new ptolemy.data.BooleanToken true]

    set valuesParam [getParameter $pulse values]
    $valuesParam setToken $valToken
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {{"AB"} {""} {"CD"} {"EF"} {"AB"} {""} {"CD"} {"EF"} {"AB"} {""} {"CD"} {"EF"} {"AB"} {""} {"CD"}} 
