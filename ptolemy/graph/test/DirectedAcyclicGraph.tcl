# Tests for the DirectedAcyclicGraph class
#
# @Author: Yuhong Xiong
#
# $Id$
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
#                       PT_COPYRIGHT_VERSION_2
#                       COPYRIGHTENDKEY
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
test DirectedAcyclicGraph-2.1 {Create an empty instance} {
    set p [java::new ptolemy.graph.DirectedAcyclicGraph]
    $p containsNodeWeight null
} {0}

######################################################################
####
# 
test DirectedAcyclicGraph-2.2 {test methods on the above empty instance} {
    catch {$p leastUpperBound null null} msg
    list [$p bottom] [$p top] $msg
} {java0x0 java0x0 {ptolemy.graph.GraphWeightException: Invalid weight argument, the number of elements for this weight is zero.
The weight is of class java.lang.String and its description follows:
null
A Dump of the offending graph follows.
{ptolemy.graph.DirectedAcyclicGraph
Node Set:

Edge Set:

}

}}

######################################################################
####
# 
test DirectedAcyclicGraph-2.3 {a 3 point CPO forming a triangle} {
    set p [java::new ptolemy.graph.DirectedAcyclicGraph 3]
    set n1 [java::new {java.lang.String String} node1]
    set n2 [java::new {java.lang.String String} node2]
    set n3 [java::new {java.lang.String String} node3]
    $p addNodeWeight $n1
    $p addNodeWeight $n2
    $p addNodeWeight $n3
    $p addEdge $n1 $n2
    $p addEdge $n1 $n3
    set downN2 [$p downSet $n2]
    set subset [java::new {Object[]} {2} {node2 node3}]
    list [$p bottom] \
     [$p compare $n1 $n2] [$p compare $n2 $n2] \
     [$p compare $n3 $n1] [$p compare $n2 $n3] \
     [$downN2 get 0] [$downN2 get 1] \
     [$p greatestLowerBound $n2 $n1] [$p greatestLowerBound $n2 $n3] \
     [$p greatestLowerBound $subset]
} {node1 -1 0 1 2 node2 node1 node1 node1 node1}

######################################################################
####
#
test DirectedAcyclicGraph-2.4 {a 3 point CPO forming a triangle} {
    set p [java::new ptolemy.graph.DirectedAcyclicGraph 3]
    set n1 [java::new {java.lang.String String} node1]
    set n2 [java::new {java.lang.String String} node2]
    set n3 [java::new {java.lang.String String} node3]
    $p addNodeWeight $n1
    $p addNodeWeight $n2
    $p addNodeWeight $n3
    $p addEdge $n1 $n2
    $p addEdge $n1 $n3
    set subset [java::new {Object[]} {2} {node2 node3}]
    set subset2 [java::new {Object[]} {2} {node1 node3}]
    set upN1 [$p upSet $n1]
    list [$p greatestElement $subset] \
     [$p leastElement $subset2] \
     [$p leastUpperBound $n2 $n3] [$p leastUpperBound $n1 $n2] \
     [$p top] \
     [$upN1 get 0] [$upN1 get 1] [$upN1 get 2] \
     [$p isLattice]
} {java0x0 node1 java0x0 node2 java0x0 node1 node2 node3 0}

######################################################################
####
#
test DirectedAcyclicGraph-2.5 {lub/glb of empty subset, use the CPO in 2.3} {
    set subset [java::new {Object[]} {0} {}]
    list [$p leastUpperBound $subset] [$p greatestLowerBound $subset]
} {node1 java0x0}

######################################################################
####
#
test DirectedAcyclicGraph-2.6 {catch exception on self loop} {
    # use the CPO above
    catch {$p addEdge $n1 $n1} msg
    list $msg
} {{ptolemy.graph.GraphConstructionException: Cannot add a self loop in an acyclic graph.
A self loop was attempted on the following node.
node1}}

######################################################################
####
# 
test DirectedAcyclicGraph-2.7 {a 5 point CPO that's not a lattice} {
    set p [java::new ptolemy.graph.DirectedAcyclicGraph]
    set n1 [java::new {java.lang.String String} node1]
    set n2 [java::new {java.lang.String String} node2]
    set n3 [java::new {java.lang.String String} node3]
    set n4 [java::new {java.lang.String String} node4]
    set n5 [java::new {java.lang.String String} node5]
    $p addNodeWeight $n1
    $p addNodeWeight $n2
    $p addNodeWeight $n3
    $p addNodeWeight $n4
    $p addNodeWeight $n5
    $p addEdge $n2 $n1
    $p addEdge $n3 $n1
    $p addEdge $n4 $n2
    $p addEdge $n4 $n3
    $p addEdge $n5 $n2
    $p addEdge $n5 $n3
    set subset [java::new {Object[]} {3} {node4 node5 node1}]
    list [$p leastUpperBound $subset] \
     [$p greatestLowerBound $subset] \
     [$p leastUpperBound $n4 $n5] \
     [$p greatestLowerBound $n4 $n5] \
     [$p top] \
     [$p bottom]
} {node1 java0x0 java0x0 java0x0 node1 java0x0}

######################################################################
####
# 
test DirectedAcyclicGraph-2.8 {a 6 point CPO that's not a lattice} {
    # add a bottom to the above lattice
    set n6 [java::new {java.lang.String String} node6]
    $p addNodeWeight $n6
    $p addEdge $n6 $n4
    $p addEdge $n6 $n5
    $p isLattice
} {0}

######################################################################
####
# 
test DirectedAcyclicGraph-2.9 {test least element } {
    # construct a CPO with 3 elements on a chain, and an isolated one.
    set p [java::new ptolemy.graph.DirectedAcyclicGraph]
    set n1 [java::new {java.lang.String String} node1]
    set n2 [java::new {java.lang.String String} node2]
    set n3 [java::new {java.lang.String String} node3]
    set n4 [java::new {java.lang.String String} node4]
    $p addNodeWeight $n1
    $p addNodeWeight $n2
    $p addNodeWeight $n3
    $p addNodeWeight $n4
    $p addEdge $n2 $n1
    $p addEdge $n3 $n2

    set subset [java::new {Object[]} {4} {node2 node4 node1 node3}]
    list [$p leastElement $subset] \
     [$p greatestElement $subset] \
     [$p leastUpperBound $subset] \
     [$p greatestLowerBound $subset]
} {java0x0 java0x0 java0x0 java0x0}

######################################################################
####
# 
test DirectedAcyclicGraph-3.1 {a DAG with 4 nodes forming a diamond} {
    set p [java::new ptolemy.graph.DirectedAcyclicGraph]
    set n1 [java::new {java.lang.String String} node1]
    set n2 [java::new {java.lang.String String} node2]
    set n3 [java::new {java.lang.String String} node3]
    set n4 [java::new {java.lang.String String} node4]
    $p addNodeWeight $n1
    $p addNodeWeight $n2
    $p addNodeWeight $n3
    $p addNodeWeight $n4
    $p addEdge $n1 $n2
    $p addEdge $n1 $n3
    $p addEdge $n2 $n4
    $p addEdge $n3 $n4
    set sort [$p topologicalSort]
    list [$sort get 0] [$sort get 1] [$sort get 2] [$sort get 3] \
     [$p isLattice]
} {node1 node2 node3 node4 1}

######################################################################
####
#
test DirectedAcyclicGraph-3.2 { topologicalSort part of the graph } {
    set p [java::new ptolemy.graph.DirectedAcyclicGraph]
    set n1 [java::new {java.lang.String String} node1]
    set n2 [java::new {java.lang.String String} node2]
    set n3 [java::new {java.lang.String String} node3]
    set n4 [java::new {java.lang.String String} node4]
    set n5 [java::new {java.lang.String String} node5]
    set n6 [java::new {java.lang.String String} node6]
    $p addNodeWeight $n1
    $p addNodeWeight $n2
    $p addNodeWeight $n3
    $p addNodeWeight $n4
    $p addNodeWeight $n5
    $p addNodeWeight $n6
    $p addEdge $n1 $n2
    $p addEdge $n1 $n3
    $p addEdge $n2 $n4
    $p addEdge $n3 $n4
    $p addEdge $n3 $n5
    $p addEdge $n4 $n6
    set nodeArray [java::new {java.lang.String[]} 5 \
        [list $n4 $n3 $n1 $n5 $n2]]
    set sort [$p {topologicalSort java.lang.Object[]} $nodeArray]
    list [$sort get 0] [$sort get 1] [$sort get 2] [$sort get 3] [$sort get 4]
} {node1 node3 node2 node5 node4}

######################################################################
####
#
test DirectedAcyclicGraph-3.3 { top. sort cyclic graph, catch exceptin } {
    set p [java::new ptolemy.graph.DirectedAcyclicGraph]
    set n1 [java::new {java.lang.String String} node1]
    set n2 [java::new {java.lang.String String} node2]
    set n3 [java::new {java.lang.String String} node3]
    $p addNodeWeight $n1
    $p addNodeWeight $n2
    $p addNodeWeight $n3
    $p addEdge $n1 $n2
    $p addEdge $n2 $n3
    $p addEdge $n3 $n1

    catch {$p topologicalSort} msg
    list $msg
} {{ptolemy.graph.GraphStateException: DirectedAcyclicGraph._validate: Graph is cyclic.}}

######################################################################
####
#
test DirectedAcyclicGraph-3.4 { reachable nodes on cyclic graph } {
    # use graph above
    catch {$p compare $n1 $n2} msg
    list $msg
} {{ptolemy.graph.GraphStateException: DirectedAcyclicGraph._validate: Graph is cyclic.}}

######################################################################
####
#
test DirectedAcyclicGraph-4.1 { clone } {
    set ag [java::new ptolemy.graph.DirectedAcyclicGraph]
    set n1 [java::new ptolemy.graph.Node]
    set n2 [java::new ptolemy.graph.Node]
    set n3 [java::new ptolemy.graph.Node]
    set e1 [java::new ptolemy.graph.Edge $n1 $n2]
    set e2 [java::new ptolemy.graph.Edge $n2 $n3]
    $ag addNode $n1
    $ag addNode $n2
    $ag addNode $n3
    $ag addEdge $e1
    $ag addEdge $e2
    set clone [$ag clone]
    set cg [java::cast ptolemy.graph.DirectedAcyclicGraph $clone]
    set hasn1 [$cg containsNode $n1]
    set hasn2 [$cg containsNode $n2]
    set hasn3 [$cg containsNode $n3]
    set hase1 [$cg containsEdge $e1]
    set hase2 [$cg containsEdge $e2]
    set equal [$cg equals $ag]
    list $hasn1 $hasn2 $hasn3 $hase1 $hase2 $equal
} {1 1 1 1 1 1}

