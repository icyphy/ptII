# Test Pulse
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997-2000 The Regents of the University of California.
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
test Pulse-1.1 {test constructor and clone with default values} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set pulse [java::new ptolemy.actor.lib.Pulse $e0 pulse]

    set values [getParameter $pulse values]
    set indexes [getParameter $pulse indexes]

    set newobj [java::cast ptolemy.actor.lib.Pulse [$pulse clone]]
    set newValues [getParameter $newobj values]
    set newIndexes [getParameter $newobj indexes]
    set valuesVal [[$newValues getToken] toString]
    set indexesVal [[$newIndexes getToken] toString]

    list $valuesVal $indexesVal
} {{array[1, 0]} {[0, 1]}} {array is not supported by expression language yet.}

######################################################################
#### Check type of values parameter
#
test Pulse-1.2 {check type} {
    list [[$values getType] toString] [[$newValues getType] toString]
} {(int)array (int)array}

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
 
    set indexes [java::new {int[][]} {1 3} [list [list 0 2 3]]]
    set indexesParam [getParameter $pulse indexes]
    $indexesParam setToken [java::new ptolemy.data.IntMatrixToken $indexes]

    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {0.1 0.0 0.2 0.3 0.0}

test Pulse-2.3 {test using setExpression} {
    $valuesParam setExpression {[5l, 6, 7]}
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {5 0 6 7 0}

test Pulse-2.4 {test with two-dimensional output values} {
    set values [java::new {int[][]} {3 2} \
            [list [list 1 2] [list 3 4] [list 5 6]]]
    set valuesParam [getParameter $pulse values]
    $valuesParam setToken [java::new ptolemy.data.IntMatrixToken $values]
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {{array[1, 2]} {array[0, 0]} {array[3, 4]} {array[5, 6]} {array[0, 0]}} {array is not supported by expression language yet.}

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
} {AB {} CD EF {}} 

######################################################################
#### Test error conditions
#
test Pulse-3.1 {test indexes that are out of order} {
    set indexes [java::new {int[][]} {1 3} [list [list 0 3 2]]]
    set indexesParam [getParameter $pulse indexes]
    catch {
        $indexesParam setToken [java::new ptolemy.data.IntMatrixToken $indexes]
    } msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: .top.pulse:
Value of indexes must be an array of nonnegative integers increasing in value.}}

test Pulse-3.2 {test negative indexes} {
    set indexes [java::new {int[][]} {1 3} [list [list -1 0 1]]]
    set indexesParam [getParameter $pulse indexes]
    catch {
        $indexesParam setToken [java::new ptolemy.data.IntMatrixToken $indexes]
    } msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: .top.pulse:
Value of indexes must be an array of nonnegative integers increasing in value.}}

test Pulse-3.3 {test values and indexes of different dimensions} {
    set indexes [java::new {int[][]} {1 3} [list [list 1 2 3]]]
    set indexesParam [getParameter $pulse indexes]
    $indexesParam setToken [java::new ptolemy.data.IntMatrixToken $indexes]
    set values [java::new {int[][]} {1 2} [list [list 0 3]]]
    set valuesParam [getParameter $pulse values]
    $valuesParam setToken [java::new ptolemy.data.IntMatrixToken $values]
    catch {
        [$e0 getManager] execute
    } msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: .top.pulse:
Parameters values and indexes have different lengths.}}

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
 
    set indexes [java::new {int[][]} {1 3} [list [list 0 2 3]]]
    set indexesParam [getParameter $pulse indexes]
    $indexesParam setToken [java::new ptolemy.data.IntMatrixToken $indexes]

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
} {AB {} CD EF AB {} CD EF AB {} CD EF AB {} CD} 
