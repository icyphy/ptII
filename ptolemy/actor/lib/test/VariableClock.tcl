# Test VariableClock.
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997-2003 The Regents of the University of California.
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

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
#### Constructors and Clone
#
test VariableClock-1.0 {test constructor and initial value} {
    set e0 [deModel 10.0]
    set clockmaster [java::new ptolemy.actor.lib.VariableClock $e0 clock]
    [$clockmaster getAttribute values] toString
} {ptolemy.data.expr.Parameter {.top.clock.values} {1}}

test VariableClock-1.1 {test clone and initial value} {
    set clock [java::cast ptolemy.actor.lib.VariableClock \
		   [$clockmaster clone [$e0 workspace]]]
    $clockmaster setContainer [java::null]
    $clock setContainer $e0
    [$clock getAttribute values] toString
} {ptolemy.data.expr.Parameter {.top.clock.values} {1}}

######################################################################
#### Test VariableClock in a DE model
#
test VariableClock-2.1 {test with the default output value} {
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $clock] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    set manager [$e0 getManager]
    $manager addExecutionListener \
            [java::new ptolemy.actor.StreamExecutionListener]
    $manager execute
    enumToTokenValues [$rec getRecord 0]
} {1 1 1 1 1 1 1 1 1 1 1}

test VariableClock-2.2 {check times} {
    listToStrings [$rec getTimeHistory]
} {0.0 1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0}

test VariableClock-2.3 {Connect a second clock to the periodControl input} {
    set control [java::new ptolemy.actor.lib.Clock $e0 control]
    set p [getParameter $control values]
    $p setExpression {{2.0, 1.0}}
    set p [getParameter $control offsets]
    $p setExpression {{0.0, 6.0}}
    set p [getParameter $control period]
    $p setExpression {10.0}

    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $control] \
            output] \
            [java::field $clock periodControl]
    # [$e0 getDirector] addDebugListener \
            # [java::new ptolemy.kernel.util.StreamListener]
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {1 1 1 1 1 1 1 1}

test VariableClock-2.4 {check times} {
    listToStrings [$rec getTimeHistory]
} {0.0 2.0 4.0 6.0 7.0 8.0 9.0 10.0}
