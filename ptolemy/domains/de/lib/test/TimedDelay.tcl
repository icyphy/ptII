# Tests for the TimedDelay class
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1999-2003 The Regents of the University of California.
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test TimedDelay-1.1 {test constructor and clone} {
    set e0 [deModel 3.0]
    set timedDelayBase [java::new ptolemy.domains.de.lib.TimedDelay $e0 TimedDelay]
    set timedDelay [java::cast ptolemy.domains.de.lib.TimedDelay \
			[$timedDelayBase clone [$e0 workspace]]]
    $timedDelayBase setContainer [java::null]
    $timedDelay setContainer $e0
    # Success here is just not throwing an exception.
    list {}
} {{}}

######################################################################
#### Test TimedDelay in a DE model
#
test TimedDelay-2.1 {test with the default TimedDelay value} {
    set clock [java::new ptolemy.actor.lib.Clock $e0 clock]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $clock] output] \
       [java::field [java::cast ptolemy.domains.de.lib.DETransformer $timedDelay] \
       input]
    $e0 connect \
       [java::field \
       [java::cast ptolemy.domains.de.lib.DETransformer $timedDelay] output] \
       [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    [$e0 getManager] execute
    #enumToStrings [$rec getTimeRecord]
    enumToObjects [$rec getTimeRecord]
} {1.0 2.0 3.0}

test TimedDelay-3.1 {test with zero TimedDelay} {
    set timedDelayAmount [java::field $timedDelay delay]
    $timedDelayAmount setExpression "0.0"
    [$e0 getManager] execute
    enumToObjects [$rec getTimeRecord]
} {0.0 1.0 2.0 3.0}

test TimedDelay-3.2 {test with negative delay} {
    set TimedDelayAmount [java::field $timedDelay delay]
    $timedDelayAmount setExpression "-1.0"
    catch {[$e0 getManager] execute} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Error evaluating expression: -1.0
  in .top.TimedDelay.delay
Because:
Cannot have negative delay: -1.0
  in .top.TimedDelay}}

test TimedDelay-4.1 {test a self loop with the zero TimedDelay} {
    set e0 [deModel 3.0]
    set clock [java::new ptolemy.actor.lib.Clock $e0 clock]
    set add [java::new ptolemy.actor.lib.AddSubtract $e0 add]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $clock] output] \
            [java::field $add plus]
    set r [$e0 connect \
            [java::field $add output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]]
    [java::field $add plus] link $r
    catch {[$e0 getManager] execute} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Found zero delay loop including: .top.add, .top.add
  in .top}}

test TimedDelay-5.1 {test a more complex loop with the zero TimedDelay} {
    set e0 [deModel 3.0]
    set clock [java::new ptolemy.actor.lib.Clock $e0 clock]
    set add [java::new ptolemy.actor.lib.AddSubtract $e0 add]
    set gain [java::new ptolemy.actor.lib.Scale $e0 gain]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $clock] output] \
            [java::field $add plus]
    $e0 connect \
            [java::field $add output] \
            [java::field [java::cast ptolemy.actor.lib.Transformer $gain] input]
    set r [$e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Transformer $gain] \
            output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]]
    [java::field $add plus] link $r
    catch {[$e0 getManager] execute} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Found zero delay loop including: .top.add, .top.add, .top.gain, .top.gain
  in .top}}

test TimedDelay-5.2 {fix the zero TimedDelay with a non-zero TimedDelay} {
    set timedDelay [java::new ptolemy.domains.de.lib.TimedDelay $e0 TimedDelay]
    [java::field $add plus] unlink $r
    [java::field [java::cast ptolemy.domains.de.lib.DETransformer $timedDelay] \
            input] link $r
    $e0 connect \
            [java::field [java::cast ptolemy.domains.de.lib.DETransformer \
            $timedDelay] output] \
            [java::field $add plus]
    [$e0 getManager] execute
    enumToObjects [$rec getTimeRecord]
} {0.0 1.0 2.0 3.0}
