# Test Commutator
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
#
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#

######################################################################
####
#
test Commutator-1.1 {test clone} {
    set e0 [sdfModel 3]
    set commutatormaster [java::new ptolemy.actor.lib.Commutator \
            $e0 commutator]
    set commutator [_testClone $commutatormaster]
    $commutatormaster setContainer [java::null]
    $commutator setContainer $e0
    $commutator description 1
} {ptolemy.actor.lib.Commutator}

test Commutator-2.1 {run with a single input} {
    set ramp1 [java::new ptolemy.actor.lib.Ramp $e0 ramp1]   
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    set in1 [java::field [java::cast ptolemy.actor.lib.Transformer \
            $commutator] input]
    set r1 [$e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $ramp1] output] \
            $in1]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Transformer \
            $commutator] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    set m [$e0 getManager]
    $m execute
    enumToTokenValues [$rec getRecord 0]
} {0 1 2}

test Commutator-3.1 {run with two inputs} {
    set ramp2 [java::new ptolemy.actor.lib.Ramp $e0 ramp2]   
    set r2 [$e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $ramp2] output] \
            $in1]
    $m execute
    enumToTokenValues [$rec getRecord 0]
} {0 0 1 1 2 2}

test Commutator-4.1 {run with mutations} {
    $m addChangeListener \
            [java::new ptolemy.kernel.event.StreamChangeListener]
    set dir [$e0 getDirector]
    $dir addDebugListener \
            [java::new ptolemy.kernel.util.StreamListener]
    $m initialize
    $m iterate
    set c1 [java::new ptolemy.actor.event.RemoveActor $e0 $ramp1]
    set c2 [java::new ptolemy.actor.event.RemoveRelation $e0 $r1]
    $m requestChange $c1
    $m requestChange $c2
    $m iterate
    $m wrapup
    enumToTokenValues [$rec getRecord 0]
} {0 0 1}

test Commutator-5.1 {test under DE} {
    set e0 [deModel 6.0]
    set dir [$e0 getDirector]
    set m [$e0 getManager]
    set clock1 [java::new ptolemy.actor.lib.Clock $e0 clock1]
    set p [getParameter $clock1 period]
    $p setExpression {3.0}
    set p [getParameter $clock1 values]
    $p setExpression {[-1, -2]}
    set commutator [java::new ptolemy.actor.lib.Commutator $e0 commutator]
    set in1 [java::field [java::cast ptolemy.actor.lib.Transformer \
            $commutator] input]
    set clock2 [java::new ptolemy.actor.lib.Clock $e0 clock2]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $clock1] output] \
            $in1
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $clock2] output] \
            $in1
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Transformer \
            $commutator] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    $m execute
    list [enumToTokenValues [$rec getRecord 0]] \
            [enumToObjects [$rec getTimeRecord]]
} {{-1 1 -2 0 -1 1 -2 0 -1 1} {0.0 0.0 1.0 1.0 3.0 3.0 4.0 4.0 6.0 6.0}}
