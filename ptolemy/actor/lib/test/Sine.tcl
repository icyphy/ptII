# Test Sine.
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
test Sine-1.1 {test constructor and clone} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set sinbase [java::new ptolemy.actor.lib.Sine $e0 sin]
    set sin [java::cast ptolemy.actor.lib.Sine [$sinbase clone]]
    # Success here is just not throwing an exception.
    list {}
} {{}}

######################################################################
#### Test Sine in an SDF model
#
test Sine-2.1 {test with the default output values} {
    # This tests the iterate() method of Sine.java.
    set e0 [sdfModel 5]
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    $init setExpression {0.0}
    $step setExpression {1.0}
    # Use clone of sin to make sure that is ok.
    $sin setContainer $e0
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $ramp] output] \
       [java::field [java::cast ptolemy.actor.lib.Transformer $sin] input]
    $e0 connect \
       [java::field \
       [java::cast ptolemy.actor.lib.Transformer $sin] output] \
       [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    [$e0 getManager] execute
    ptclose [enumToTokenValues [$rec getRecord 0]] \
            {0.0 0.841 0.909 0.141 -0.757} \
            0.001
} {1}

######################################################################
#### Test Sine in CT model
#
test Sine-2.2 {test with the default output values} {
    # This tests the fire() method of Sine.java.
    set e1 [java::new ptolemy.actor.TypedCompositeActor]
    set sin2 [java::cast ptolemy.actor.lib.Sine [$sinbase clone]]
    set e1 [ctModel 5]
    set ramp2 [java::new ptolemy.actor.lib.Ramp $e1 ramp2]
    set init [getParameter $ramp2 init]
    set step [getParameter $ramp2 step]
    $init setExpression {0.0}
    $step setExpression {1.0}
    # Use clone of sin to make sure that is ok.
    $sin2 setContainer $e1
    set rec [java::new ptolemy.actor.lib.Recorder $e1 rec]
    $e1 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $ramp2] output] \
       [java::field [java::cast ptolemy.actor.lib.Transformer $sin2] input]
    $e1 connect \
       [java::field \
       [java::cast ptolemy.actor.lib.Transformer $sin2] output] \
       [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    [$e1 getManager] execute
    ptclose [enumToTokenValues [$rec getRecord 0]] \
            {0.0 0.841471 0.9092974 0.14112 -0.7568025 -0.9589243 -0.2794155} \
            0.001
} {1}
