# Test Pulse
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997-1999 The Regents of the University of California.
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
    set valuesVal [[$newValues getToken] stringValue]
    set indexesVal [[$newIndexes getToken] stringValue]

    list $valuesVal $indexesVal
} {{[[1 0]]} {[[0 1]]}}

######################################################################
#### Test Pulse in an SDF model
#
test Pulse-2.1 {test with the default output values} {
    set e0 [sdfModel 5]
    set pulse [java::new ptolemy.actor.lib.Pulse $e0 pulse]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $pulse] output] \
            [java::field $rec input]
    [$e0 getManager] run
    enumToTokenValues [$rec getRecord 0]
} {1 0 0 0 0}

test Pulse-2.2 {test with the non-default output values} {
    set values [java::new {double[][]} {1 3} [list [list 0.1 0.2 0.3]]]
    set valuesParam [getParameter $pulse values]
    $valuesParam setToken [java::new ptolemy.data.DoubleMatrixToken $values]
    
    set indexes [java::new {int[][]} {1 3} [list [list 0 2 3]]]
    set indexesParam [getParameter $pulse indexes]
    $indexesParam setToken [java::new ptolemy.data.IntMatrixToken $indexes]

    [$e0 getManager] run
    enumToTokenValues [$rec getRecord 0]
} {0.1 0.0 0.2 0.3 0.0}

test Pulse-2.3 {test with two-dimensional output values} {
    set values [java::new {double[][]} {2 2} \
            [list [list 1 2] [list 3 4]]]
    set valuesParam [getParameter $pulse values]
    $valuesParam setToken [java::new ptolemy.data.DoubleMatrixToken $values]
    
    set indexes [java::new {int[][]} {2 2} [list [list 0 1] [list 2 3]]]
    set indexesParam [getParameter $pulse indexes]
    $indexesParam setToken [java::new ptolemy.data.IntMatrixToken $indexes]

    [$e0 getManager] run
    enumToTokenValues [$rec getRecord 0]
} {1.0 2.0 3.0 4.0 0.0}

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
} {{ptolemy.kernel.util.IllegalActionException: .top.pulse: Value of indexes must be an array of nonnegative integers increasing in value.}}

test Pulse-3.2 {test negative indexes} {
    set indexes [java::new {int[][]} {1 3} [list [list -1 0 1]]]
    set indexesParam [getParameter $pulse indexes]
    catch {
        $indexesParam setToken [java::new ptolemy.data.IntMatrixToken $indexes]
    } msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: .top.pulse: Value of indexes must be an array of nonnegative integers increasing in value.}}

test Pulse-3.3 {test values and indexes of different dimensions} {
    set indexes [java::new {int[][]} {1 3} [list [list 1 2 3]]]
    set indexesParam [getParameter $pulse indexes]
    $indexesParam setToken [java::new ptolemy.data.IntMatrixToken $indexes]
    catch {
        [$e0 getManager] run
    } msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: .top.pulse: Parameters values and indexes must be arrays of the same dimension.}}

