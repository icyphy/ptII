# Tests for the Delay class
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
test SimpleDelay-1.1 {test constructor and clone} {
    set e0 [deModel 3.0]
    set delaybase [java::new ptolemy.domains.de.lib.test.SimpleDelay $e0 delay]
    set delay [java::cast ptolemy.domains.de.lib.test.SimpleDelay \
		   [$delaybase clone [$e0 workspace]]]
    $delaybase setContainer [java::null]
    $delay setContainer $e0
    # Success here is just not throwing an exception.
    list {}
} {{}}

######################################################################
#### Test Delay in a DE model
#
test SimpleDelay-2.1 {test with the default delay value} {
    set clock [java::new ptolemy.actor.lib.Clock $e0 clock]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
       [java::field [java::cast ptolemy.actor.lib.Source $clock] output] \
       [java::field $delay input]
    $e0 connect \
       [java::field $delay output] \
       [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    [$e0 getManager] execute
    enumToObjects [$rec getTimeRecord]
} {1.0 2.0 3.0}

test SimpleDelay-3.1 {test with the zero delay} {
    set delayAmount [java::field $delay delay]
    $delayAmount setExpression "0.0"
    [$e0 getManager] execute
    enumToObjects [$rec getTimeRecord]
} {0.0 1.0 2.0 3.0}

test SimpleDelay-4.1 {test a self loop with the zero delay} {
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
} {{ptolemy.graph.GraphConstructionException: Cannot add a self loop in an acyclic graph.
A self loop was attempted on the following node.
ptolemy.actor.lib.AddSubtract {.top.add}}}

test SimpleDelay-5.1 {test a more complex loop with the zero delay} {
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
} {{ptolemy.kernel.util.IllegalActionException: Found zero delay loop including: .top.add, .top.gain
  in .top.DEDirector}}

test SimpleDelay-5.2 {fix the zero delay with a non-zero delay} {
    set delay [java::new ptolemy.domains.de.lib.test.SimpleDelay $e0 delay]
    [java::field $add plus] unlink $r
    [java::field $delay input] link $r
    $e0 connect \
            [java::field $delay output] \
            [java::field $add plus]
    [$e0 getManager] execute
    enumToObjects [$rec getTimeRecord]
} {0.0 1.0 2.0 3.0}
