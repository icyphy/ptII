# Tests for the GraphReader class
#
# @Author: Christopher Hylands, Contributor: Xiaojun Liu (example from design doc)
#
# $Id$
#
# @Copyright (c) 2002-2005 The Regents of the University of California.
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

# Ptolemy II test bed, see $TYCHO/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# NOTE:  All of the following tests use this director,
# pretty much as a dummy.
set director [java::new ptolemy.actor.Director]
set manager [java::new ptolemy.actor.Manager]

######################################################################
####
#
test GraphReader-1.1 {Run GraphReader.convert on an example similar to the design document} {

    # Create objects
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    $e0 setName E0
    set e1 [java::new ptolemy.actor.AtomicActor $e0 "E1"]
    set e2 [java::new ptolemy.actor.CompositeActor $e0 "E2"]
    set e3 [java::new ptolemy.actor.AtomicActor $e2 "E3"]
    set e4 [java::new ptolemy.actor.AtomicActor $e0 "E4"]
    set p1 [java::new ptolemy.actor.IOPort $e1 "P1"]
    set p2 [java::new ptolemy.actor.IOPort $e2 "P2"]
    set p3 [java::new ptolemy.actor.IOPort $e3 "P3"]
    set p4 [java::new ptolemy.actor.IOPort $e2 "P4"]
    set p5 [java::new ptolemy.actor.IOPort $e4 "P5"]
    set r1 [java::new ptolemy.actor.IORelation $e0 "R1"]
    set r2 [java::new ptolemy.actor.IORelation $e2 "R2"]
    set r3 [java::new ptolemy.actor.IORelation $e0 "R3"]

    # Connect
    $p1 link $r1
    $p2 link $r1
    $p2 link $r2
    $p3 link $r2
    $p4 link $r2
    $p4 link $r3
    $p5 link $r3

    # make P1, P3 output, P5 input
    $p1 setInput false
    $p1 setOutput true
    $p3 setInput false
    $p3 setOutput true
    $p5 setInput true
    $p5 setOutput false

    set graphReader [java::new ptolemy.actor.GraphReader]
    set graph [$graphReader convert $e0]
    $graph toString
} {{ptolemy.graph.DirectedGraph
Node Set:
0: ptolemy.actor.AtomicActor {.E0.E1}
1: ptolemy.actor.AtomicActor {.E0.E2.E3}
2: ptolemy.actor.AtomicActor {.E0.E4}
Edge Set:
0: (ptolemy.actor.AtomicActor {.E0.E1}, ptolemy.actor.AtomicActor {.E0.E4}, ptolemy.actor.IOPort {.E0.E4.P5})
1: (ptolemy.actor.AtomicActor {.E0.E2.E3}, ptolemy.actor.AtomicActor {.E0.E4}, ptolemy.actor.IOPort {.E0.E4.P5})
}
}


