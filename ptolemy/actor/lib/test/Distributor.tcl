# Test Distributor
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
test Distributor-1.1 {test clone} {
    set e0 [sdfModel 3]
    set distributormaster [java::new ptolemy.actor.lib.Distributor \
            $e0 distributor]
    set distributor [_testClone $distributormaster]
    $distributormaster setContainer [java::null]
    $distributor setContainer $e0
    $distributor description 1
} {ptolemy.actor.lib.Distributor}

test Distributor-2.1 {run with a single output} {
    set ramp1 [java::new ptolemy.actor.lib.Ramp $e0 ramp1]   
    set rec1 [java::new ptolemy.actor.lib.Recorder $e0 rec1]
    set out1 [java::field [java::cast ptolemy.actor.lib.Transformer \
            $distributor] output]
    set in1 [java::field [java::cast ptolemy.actor.lib.Transformer \
            $distributor] input]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $ramp1] output] \
            $in1
    set r1 [$e0 connect \
            $out1 \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec1] input]]
    set m [$e0 getManager]
    $m execute
    enumToTokenValues [$rec1 getRecord 0]
} {0 1 2}

test Distributor-3.1 {run with two outputs} {
    set dir [$e0 getDirector]
    $dir addDebugListener \
            [java::new ptolemy.kernel.util.StreamListener]
    set rec2 [java::new ptolemy.actor.lib.Recorder $e0 rec2]   
    set r2 [$e0 connect \
            $out1 \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec2] input]]
    $m execute
    list [enumToTokenValues [$rec1 getRecord 0]] \
            [enumToTokenValues [$rec2 getRecord 0]]
} {{0 2 4} {1 3 5}}

test Distributor-4.1 {run with mutations} {
    $m addChangeListener \
            [java::new ptolemy.kernel.event.StreamChangeListener]
    $m initialize
    $m iterate
    set c1 [java::new ptolemy.actor.event.RemoveActor $e0 $rec2]
    set c2 [java::new ptolemy.actor.event.RemoveRelation $e0 $r2]
    $m requestChange $c1
    $m requestChange $c2
    $m iterate
    $m iterate
    $m wrapup
    list [enumToTokenValues [$rec1 getRecord 0]] \
            [enumToTokenValues [$rec2 getRecord 0]]
} {{0 2 3} 1}

test Distributor-5.1 {test under DE} {
    set e0 [deModel 6.0]
    set clock [java::new ptolemy.actor.lib.Clock $e0 clock]
    set distributor [java::new ptolemy.actor.lib.Distributor $e0 distributor]
    set rec1 [java::new ptolemy.actor.lib.Recorder $e0 rec1]
    set rec2 [java::new ptolemy.actor.lib.Recorder $e0 rec2]   
    set out1 [java::field [java::cast ptolemy.actor.lib.Transformer \
            $distributor] output]
    set in1 [java::field [java::cast ptolemy.actor.lib.Transformer \
            $distributor] input]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $clock] output] \
            $in1
    set r1 [$e0 connect \
            $out1 \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec1] input]]
    set r2 [$e0 connect \
            $out1 \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec2] input]]
    set m [$e0 getManager]
    $m execute
    list [enumToObjects [$rec1 getTimeRecord]] \
            [enumToObjects [$rec2 getTimeRecord]]
} {{0.0 2.0 4.0 6.0} {1.0 3.0 5.0}}
