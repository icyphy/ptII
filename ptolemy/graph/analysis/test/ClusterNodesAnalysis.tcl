# Tests ClusterNodesAnalysis.
#
# @Author: Mingyung Ko
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
#						PT_COPYRIGHT_VERSION_2
#						COPYRIGHTENDKEY
#######################################################################

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
	source testDefs.tcl
}

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
test ClusterNodesAnalysis-1.1 {Create a graph with 4 nodes forming a diamond} {
	set p1 [java::new ptolemy.graph.Graph]
	set n1 [java::new {java.lang.String String} node1]
	set n2 [java::new {java.lang.String String} node2]
	set n3 [java::new {java.lang.String String} node3]
	set n4 [java::new {java.lang.String String} node4]
	set node1 [$p1 addNodeWeight $n1]
	set node2 [$p1 addNodeWeight $n2]
	set node3 [$p1 addNodeWeight $n3]
	set node4 [$p1 addNodeWeight $n4]
	set e1 [java::new {java.lang.String String} edge1]
	set e2 [java::new {java.lang.String String} edge2]
	set e3 [java::new {java.lang.String String} edge3]
	set e4 [java::new {java.lang.String String} edge4]
	set edge1 [$p1 addEdge $n1 $n2 $e1]
	set edge2 [$p1 addEdge $n1 $n3 $e2]
	set edge3 [$p1 addEdge $n2 $n4 $e3]
	set edge4 [$p1 addEdge $n3 $n4 $e4]
	set nodes [$p1 nodes]
	set nw [java::call ptolemy.graph.Graph weightArray $nodes]
	set edges [$p1 edges]
	set ew [java::call ptolemy.graph.Graph weightArray $edges]
	list [$nw get 0] [$nw get 1] [$nw get 2] [$nw get 3] \
	 [$ew get 0] [$ew get 1] [$ew get 2] [$ew get 3]
} {node1 node2 node3 node4 edge1 edge2 edge3 edge4}

######################################################################
####
#
test ClusterNodesAnalysis-1.2 {the returned graph of clustering nodes 2, 3, 4} {
	set node5 [java::new ptolemy.graph.Node "node5"]
	set cluster1 [java::new java.util.LinkedList]
	$cluster1 add $node2
	$cluster1 add $node3
	$cluster1 add $node4
	set analysis [java::new ptolemy.graph.analysis.ClusterNodesAnalysis $p1 $cluster1 $node5]
	set p2 [$analysis clusterNodes]
	list [$p2 toString]
} {{{ptolemy.graph.Graph
Node Set:
0: node2
1: node3
2: node4
Edge Set:
0: (node2, node4, edge3)
1: (node3, node4, edge4)
}
}}

######################################################################
####
#
test ClusterNodesAnalysis-1.3 {test parent graph} {
	set numnodes [$p1 nodeCount]
	set numedges [$p1 edgeCount]
	set nd1 [$p1 containsNode $node1]
	set nd2 [$p1 containsNode $node2]
	set nd3 [$p1 containsNode $node3]
	set nd4 [$p1 containsNode $node4]
	set nd5 [$p1 containsNode $node5]
	list $numnodes $numedges $nd1 $nd2 $nd3 $nd4 $nd5
} {2 2 1 0 0 0 1}

######################################################################
####
#
test ClusterNodesAnalysis-1.4 {Cluster diagonal nodes of a "diamond"} {
	set p1 [java::new ptolemy.graph.Graph]
	set n1 [java::new {java.lang.String String} node1]
	set n2 [java::new {java.lang.String String} node2]
	set n3 [java::new {java.lang.String String} node3]
	set n4 [java::new {java.lang.String String} node4]
	set node1 [$p1 addNodeWeight $n1]
	set node2 [$p1 addNodeWeight $n2]
	set node3 [$p1 addNodeWeight $n3]
	set node4 [$p1 addNodeWeight $n4]
	set e1 [java::new {java.lang.String String} edge1]
	set e2 [java::new {java.lang.String String} edge2]
	set e3 [java::new {java.lang.String String} edge3]
	set e4 [java::new {java.lang.String String} edge4]
	set edge1 [$p1 addEdge $n1 $n2 $e1]
	set edge2 [$p1 addEdge $n1 $n3 $e2]
	set edge3 [$p1 addEdge $n2 $n4 $e3]
	set edge4 [$p1 addEdge $n3 $n4 $e4]
	set node5 [java::new ptolemy.graph.Node "node5"]
	set cluster1 [java::new java.util.LinkedList]
	$cluster1 add $node2
	$cluster1 add $node3
	set analysis [java::new ptolemy.graph.analysis.ClusterNodesAnalysis $p1 $cluster1 $node5]
	set p2 [$analysis clusterNodes]
	list [$p2 toString]
} {{{ptolemy.graph.Graph
Node Set:
0: node2
1: node3
Edge Set:

}
}}

######################################################################
####
#
test ClusterNodesAnalysis-1.5 {test parent graph} {
	set numnodes [$p1 nodeCount]
	set numedges [$p1 edgeCount]
	set nd1 [$p1 containsNode $node1]
	set nd2 [$p1 containsNode $node2]
	set nd3 [$p1 containsNode $node3]
	set nd4 [$p1 containsNode $node4]
	set nd5 [$p1 containsNode $node5]
	list $numnodes $numedges $nd1 $nd2 $nd3 $nd4 $nd5
} {3 4 1 0 0 1 1}

######################################################################
####
#
test ClusterNodesAnalysis-1.6 {Cluster only one node} {
	set p1 [java::new ptolemy.graph.Graph]
	set n1 [java::new {java.lang.String String} node1]
	set n2 [java::new {java.lang.String String} node2]
	set n3 [java::new {java.lang.String String} node3]
	set node1 [$p1 addNodeWeight $n1]
	set node2 [$p1 addNodeWeight $n2]
	set node3 [$p1 addNodeWeight $n3]
	set e1 [java::new {java.lang.String String} edge1]
	set e2 [java::new {java.lang.String String} edge2]
	set e3 [java::new {java.lang.String String} edge3]
	set e4 [java::new {java.lang.String String} edge4]
	set edge1 [$p1 addEdge $n1 $n2 $e1]
	set edge2 [$p1 addEdge $n1 $n3 $e2]
	set edge3 [$p1 addEdge $n2 $n3 $e3]
	set edge4 [$p1 addEdge $n3 $n3 $e4]
	set node4 [java::new ptolemy.graph.Node "node4"]
	set cluster1 [java::new java.util.LinkedList]
	$cluster1 add $node2
	set analysis [java::new ptolemy.graph.analysis.ClusterNodesAnalysis $p1 $cluster1 $node4]
	set p2 [$analysis clusterNodes]
	list [$p2 toString]
} {{{ptolemy.graph.Graph
Node Set:
0: node2
Edge Set:

}
}}

test ClusterNodesAnalysis-1.7 {Cluster one node with self arc} {
	set node5 [java::new ptolemy.graph.Node "node5"]
	set cluster1 [java::new java.util.LinkedList]
	$cluster1 add $node3
	set analysis [java::new ptolemy.graph.analysis.ClusterNodesAnalysis $p1 $cluster1 $node5]
	set p2 [$analysis clusterNodes]
	set nd1 [$p1 containsNode $node1]
	set nd2 [$p1 containsNode $node2]
	set nd3 [$p1 containsNode $node3]
	set nd4 [$p1 containsNode $node4]
	set nd5 [$p1 containsNode $node5]
	list [$p1 nodeCount] [$p1 edgeCount] $nd1 $nd2 $nd3 $nd4 $nd5
} {3 3 1 0 0 1 1}


