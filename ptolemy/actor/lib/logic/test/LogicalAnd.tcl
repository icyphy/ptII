# Test LogicalAnd.
#
# @Author: John Li
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
test LogicalAnd-1.1 {test constructor and clone} {
    set e0 [sdfModel 1]
    set logic [java::new ptolemy.actor.lib.logic.LogicalAnd $e0 logic]
    set newobj [java::cast ptolemy.actor.lib.logic.LogicalAnd [$logic clone]]
    # Success here is just not throwing an exception.
    list {}
} {{}}


######################################################################
#### Test Equals in an SDF model
#
test LogicalAnd-2.1 {Truth table: True, True} {
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
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true}

test LogicalAnd-2.2 {Truth table: True, False} {
    $in2value setToken [java::new ptolemy.data.BooleanToken false]
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test LogicalAnd-2.3 {Truth table: False, False} {
    $in1value setToken [java::new ptolemy.data.BooleanToken false]
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test LogicalAnd-2.4 {Truth table: False, True} {
    $in2value setToken [java::new ptolemy.data.BooleanToken true]
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test LogicalAnd-2.5 {Multiple values: False, True, False} {
    set in3 [java::new ptolemy.actor.lib.Const $e0 in3]
    set in3value [getParameter $in3 value]
    $in3value setToken [java::new ptolemy.data.BooleanToken false]

    $in2value setToken [java::new ptolemy.data.BooleanToken true]
    set r3 [$e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $in3] output] \
       $input]
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {false}

test LogicalAnd-2.6 {Multiple values: True, True, True} {
    $in1value setToken [java::new ptolemy.data.BooleanToken true]
    $in3value setToken [java::new ptolemy.data.BooleanToken true]
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true}


