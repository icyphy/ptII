# Tests for the Delay class
#
# @Author: Christopher Hylands
#
# @Version: : NamedObj.tcl,v 1.33 1998/12/05 05:37:32 cxh Exp $
#
# @Copyright (c) 1999-2000 The Regents of the University of California.
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
test Delay-2.1 {test constructor and clone} {
    set e0 [sdfModel 5]
    set delaybase [java::new ptolemy.domains.sdf.lib.Delay $e0 delay]
    # FIXME: If I use a clone instead of the original, the original is
    # tested for type satisfaction!
    set delay [java::cast ptolemy.domains.sdf.lib.Delay [$delaybase clone]]
    $delaybase setContainer [java::null]
    $delay setContainer $e0
    set initialOutputs [getParameter $delay initialOutputs]
    # Success here is just not throwing an exception.
    list {}
} {{}}

test Delay-2.2 {test with the default parameter values} {
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 ramp]
    set init [getParameter $ramp init]
    set step [getParameter $ramp step]
    set gain [getParameter $delay gain]
    # Use clone of delay to make sure that is ok.
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $ramp] output] \
       [java::field [java::cast ptolemy.actor.lib.Transformer $delay] input]
    set relation [$e0 connect \
       [java::field \
       [java::cast ptolemy.actor.lib.Transformer $delay] output] \
       [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]]

#set debugger [java::new ptolemy.kernel.util.StreamListener]
#set director [java::cast ptolemy.domains.sdf.kernel.SDFDirector [$e0 getDirector]]
#$director addDebugListener $debugger
#set scheduler [$director getScheduler]
#$scheduler addDebugListener $debugger
    
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {0 0 1 2 3}

test Delay-2.3 {test with more than one output token} {
    $initialOutputs setExpression {[5, 5]}
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {5 5 0 1 2}

test Delay-2.4 {test with type change} {
    $initialOutputs setExpression {[7.0, 4.0]}
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {7.0 4.0 0 1 2}

test Delay-2.5 {test with type change to error condition} {
    $initialOutputs setExpression {[true, false]}
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true false 0 1 2}

test Delay-3.0 {test in feedback loop} {
    $ramp setContainer [java::null]
    set input \
            [java::field [java::cast ptolemy.actor.lib.Transformer $delay] \
            input]
    $input unlinkAll
    $input link $relation
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {true false true false true}
