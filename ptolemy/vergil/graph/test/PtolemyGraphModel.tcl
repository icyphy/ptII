# Tests for the PtolemyGraphModel class
#
# @Author: Steve Neuendorffer
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

# This example topology comes from the design doc and CompositeEntity-11.1
   # Create composite entities
    set e0 [java::new ptolemy.kernel.CompositeEntity]
    $e0 setName E0
    set e3 [java::new ptolemy.kernel.CompositeEntity $e0 E3]
    set e4 [java::new ptolemy.kernel.CompositeEntity $e3 E4]
    set e7 [java::new ptolemy.kernel.CompositeEntity $e0 E7]
    set e10 [java::new ptolemy.kernel.CompositeEntity $e0 E10]

    # Create component entities.
    set e1 [java::new ptolemy.kernel.ComponentEntity $e4 E1]
    set e2 [java::new ptolemy.kernel.ComponentEntity $e4 E2]
    set e5 [java::new ptolemy.kernel.ComponentEntity $e3 E5]
    set e6 [java::new ptolemy.kernel.ComponentEntity $e3 E6]
    set e8 [java::new ptolemy.kernel.ComponentEntity $e7 E8]
    set e9 [java::new ptolemy.kernel.ComponentEntity $e10 E9]

    # Create ports.
    set p0 [java::cast ptolemy.kernel.ComponentPort [$e4 newPort P0]]
    set p1 [java::cast ptolemy.kernel.ComponentPort [$e1 newPort P1]]
    set p2 [java::cast ptolemy.kernel.ComponentPort [$e2 newPort P2]]
    set p3 [java::cast ptolemy.kernel.ComponentPort [$e2 newPort P3]]
    set p4 [java::cast ptolemy.kernel.ComponentPort [$e4 newPort P4]]
    set p5 [java::cast ptolemy.kernel.ComponentPort [$e5 newPort P5]]
    set p6 [java::cast ptolemy.kernel.ComponentPort [$e6 newPort P6]]
    set p7 [java::cast ptolemy.kernel.ComponentPort [$e3 newPort P7]]
    set p8 [java::cast ptolemy.kernel.ComponentPort [$e7 newPort P8]]
    set p9 [java::cast ptolemy.kernel.ComponentPort [$e8 newPort P9]]
    set p10 [java::cast ptolemy.kernel.ComponentPort [$e8 newPort P10]]
    set p11 [java::cast ptolemy.kernel.ComponentPort [$e7 newPort P11]]
    set p12 [java::cast ptolemy.kernel.ComponentPort [$e10 newPort P12]]
    set p13 [java::cast ptolemy.kernel.ComponentPort [$e10 newPort P13]]
    set p14 [java::cast ptolemy.kernel.ComponentPort [$e9 newPort P14]]

    # Create links
    set r1 [$e4 connect $p1 $p0 R1]
    set r2 [$e4 connect $p1 $p4 R2]
    $p3 link $r2
    set r3 [$e4 connect $p1 $p2 R3]
    set r4 [$e3 connect $p4 $p7 R4]
    set r5 [$e3 connect $p4 $p5 R5]
    $e3 allowLevelCrossingConnect true
    set r6 [$e3 connect $p3 $p6 R6]
    set r7 [$e0 connect $p7 $p13 R7]
    set r8 [$e7 connect $p9 $p8 R8]
    set r9 [$e7 connect $p10 $p11 R9]
    set r10 [$e0 connect $p8 $p12 R10]
    set r11 [$e10 connect $p12 $p13 R11]
    set r12 [$e10 connect $p14 $p13 R12]
    $p11 link $r7

######################################################################
####
#
test PtolemyGraphModel-2.1 {Constructor tests, check consistency checks} {
    set m1 [java::new ptolemy.vergil.graph.PtolemyGraphModel]
    set m2 [java::new ptolemy.vergil.graph.PtolemyGraphModel $e0]
    list [java::call diva.graph.GraphUtilities checkConsistency [$m1 getRoot] $m1] \
	 [java::call diva.graph.GraphUtilities checkConsistency [$m2 getRoot] $m2] \
} {1 1}

proc checkNode {model node} {
    list [$node getFullName] [$model isNode $node] [$model isComposite $node]
}

test PtolemyGraphModel-2.2 {Check nodes} {
    # Let's set up some aliases for the nodes that are in the graph.
    set n1 [$e3 getAttribute "_icon"]
    set n2 [$e7 getAttribute "_icon"]
    set n3 [$e10 getAttribute "_icon"]
    set n4 [$r7 getAttribute "vertex0"]
    set n5 $p7
    set n6 $p8
    set n7 $p11
    set n8 $p12
    set n9 $p13

    list [checkNode $m2 $n1] \
	    [checkNode $m2 $n2] \
	    [checkNode $m2 $n3] \
	    [checkNode $m2 $n4] \
	    [checkNode $m2 $n5] \
	    [checkNode $m2 $n6] \
	    [checkNode $m2 $n7] \
	    [checkNode $m2 $n8] \
	    [checkNode $m2 $n9] 
} {{.E0.E3._icon 1 1} {.E0.E7._icon 1 1} {.E0.E10._icon 1 1} {.E0.R7.vertex0 1 0} {.E0.E3.P7 1 0} {.E0.E7.P8 1 0} {.E0.E7.P11 1 0} {.E0.E10.P12 1 0} {.E0.E10.P13 1 0}}

proc listContainedNodes {model node} {
    set nodes [$model nodes $node]
    set count [$model getNodeCount $node]
    set result ""
    for {set i 0} {$i < $count} {set i [expr $i + 1]} {
	set n [$nodes next]
	set t [$nodes hasNext]
	lappend result [[java::cast ptolemy.kernel.util.NamedObj $n] getFullName] $t
    }
    list $count $result
}

test PtolemyGraphModel-2.3.0 {nodes, getNodeCount} {
    listContainedNodes $m2 [$m2 getRoot]
} {4 {.E0.R7.vertex0 1 .E0.E10._icon 1 .E0.E3._icon 1 .E0.E7._icon 0}}

test PtolemyGraphModel-2.3.1 {nodes, getNodeCount} {
    listContainedNodes $m2 $n1
} {1 {.E0.E3.P7 0}}

test PtolemyGraphModel-2.3.2 {nodes, getNodeCount} {
    listContainedNodes $m2 $n2
} {2 {.E0.E7.P8 1 .E0.E7.P11 0}}

test PtolemyGraphModel-2.3.3 {nodes, getNodeCount} {
    listContainedNodes $m2 $n3
} {2 {.E0.E10.P12 1 .E0.E10.P13 0}}
