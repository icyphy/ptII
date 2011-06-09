# Tests for the DelayLine class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2007 The Regents of the University of California.
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

# Ptolemy II bed, see /users/cxh/ptII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs enumToObjects] == "" } then {
     source enums.tcl
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test DelayLine-2.1 {test constructor and clone} {
    set e0 [sdfModel 5]
    set delayLineBase [java::new ptolemy.domains.sdf.lib.DelayLine $e0 DelayLine]
    # FIXME: If I use a clone instead of the original, the original is
    # tested for type satisfaction!
    set delayLine [java::cast ptolemy.domains.sdf.lib.DelayLine \
			 [$delayLineBase clone [$e0 workspace]]]
    $delayLineBase {setContainer ptolemy.kernel.CompositeEntity} [java::null]
    $delayLine {setContainer ptolemy.kernel.CompositeEntity} $e0
    set initialOutputs [getParameter $delayLine initialOutputs]
    # Success here is just not throwing an exception.
    list {}
} {{}}

test DelayLine-2.2 {test with the default parameter values} {
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    set gain [getParameter $delayLine gain]
    # Use clone of DelayLine to make sure that is ok.
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $ramp] output] \
       [java::field [java::cast ptolemy.domains.sdf.lib.SDFTransformer $delayLine] input]
    set relation [$e0 connect \
       [java::field \
       [java::cast ptolemy.domains.sdf.lib.SDFTransformer $delayLine] output] \
       [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]]

#set debugger [java::new ptolemy.kernel.util.StreamListener]
#set director [java::cast ptolemy.domains.sdf.kernel.SDFDirector [$e0 getDirector]]
#$director addDebugListener $debugger
#set scheduler [$director getScheduler]
#$scheduler addDebugListener $debugger
    
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {{{0, 0, 0, 0}} {{1, 0, 0, 0}} {{2, 1, 0, 0}} {{3, 2, 1, 0}} {{4, 3, 2, 1}}}

test DelayLine-2.2.2 {attributeTypeChanged} {
    set initialValues [getParameter $delayLine initialValues]
    # setExpression calls attributeTypeChanged
    $initialValues setExpression {{5.0, 5.0}}
    [getParameter $delayLine initialValues] toString
} {ptolemy.data.expr.Parameter {.top.DelayLine.initialValues} {5.0, 5.0}}
