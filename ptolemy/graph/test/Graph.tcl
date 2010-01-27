# Tests for the Graph class
#
# @Author: Shuvra S. Bhattacharyya, Yuhong Xiong, Ming-Yung Ko, Fuat Keceli,
# Mainak Sen, Shahrooz Shahparnia
#
# $Id$
#
# @Copyright (c) 1997-2008 The Regents of the University of California.
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

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then { 
    source testDefs.tcl
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

#
#

######################################################################
####
#
test Graph-2.1 {Create an empty instance} {
    set p [java::new ptolemy.graph.Graph]
    $p containsNodeWeight null
} {0}

######################################################################
####
#
test Graph-2.2 {Create a graph with 2 nodes} {
    set p [java::new ptolemy.graph.Graph]
    set n1 [java::new {java.lang.String String} node1]
    set n2 [java::new {java.lang.String String} node2]
    $p addNodeWeight $n1
    $p addNodeWeight $n2
    set newEdges [$p addEdge $n1 $n2]
    $p addEdge $n2 $n1
    set newEdge [[$newEdges iterator] next]
    list [$p containsNodeWeight $n1] [$p edgeCount] [$p nodeCount]
} {1 2 2}

######################################################################
####
#
test Graph-2.3 {try to add duplicate nodes} {
    # use the graph above
    set z [$p addNodeWeight $n1]
    catch {$p {addNode ptolemy.graph.Node} $z} msg
    list $msg
} {{ptolemy.graph.GraphConstructionException: Attempt to add a node that is already contained in the graph.
Dumps of the offending node and graph follow.
The offending node:
node1
The offending graph:
{ptolemy.graph.Graph
Node Set:
0: node1
1: node2
2: node1
Edge Set:
0: (node1, node2)
1: (node2, node1)
}

}}

######################################################################
####
#
test Graph-2.4 {try to add duplicate edges} {
    # use the graph above
    catch {$p {addEdge ptolemy.graph.Edge} [java::cast ptolemy.graph.Edge $newEdge]} msg
    list $msg
} {{ptolemy.graph.GraphConstructionException: Attempt to add an edge that is already in the graph.
Dumps of the offending edge and graph follow.
The offending edge:
(node1, node2)
The offending graph:
{ptolemy.graph.Graph
Node Set:
0: node1
1: node2
2: node1
Edge Set:
0: (node1, node2)
1: (node2, node1)
}

}}

######################################################################
####
#
test Graph-2.4.1 {Create a graph with 4 nodes forming a diamond} {
    set p [java::new ptolemy.graph.Graph]
    set n1 [java::new {java.lang.String String} node1]
    set n2 [java::new {java.lang.String String} node2]
    set n3 [java::new {java.lang.String String} node3]
    set n4 [java::new {java.lang.String String} node4]
    set node1 [$p addNodeWeight $n1]
    set node2 [$p addNodeWeight $n2]
    set node3 [$p addNodeWeight $n3]
    set node4 [$p addNodeWeight $n4]
    set e1 [java::new {java.lang.String String} edge1]
    set e2 [java::new {java.lang.String String} edge2]
    set e3 [java::new {java.lang.String String} edge3]
    set e4 [java::new {java.lang.String String} edge4]
    set edge1 [$p addEdge $n1 $n2 $e1]
    set edge2 [$p addEdge $n1 $n3 $e2]
    set edge3 [$p addEdge $n2 $n4 $e3]
    set edge4 [$p addEdge $n3 $n4 $e4]
    set nodes [$p nodes]
    set nw [java::call ptolemy.graph.Graph weightArray $nodes]
    set edges [$p edges]
    set ew [java::call ptolemy.graph.Graph weightArray $edges]
    list \
	[objectsToStrings [$nw -noconvert getrange 0]] \
	[objectsToStrings [$ew -noconvert getrange 0]]
} {{node1 node2 node3 node4} {edge1 edge2 edge3 edge4}}

######################################################################
####
#
test Graph-3.1 {Test toString()} {
    # use the graph built in 2.3
    list [$p toString]
} {{{ptolemy.graph.Graph
Node Set:
0: node1
1: node2
2: node3
3: node4
Edge Set:
0: (node1, node2, edge1)
1: (node1, node3, edge2)
2: (node2, node4, edge3)
3: (node3, node4, edge4)
}
}}

######################################################################
####
#
test Graph-4.1 {Test construction of an inudced subgraph and toString()} {
    # use the graph built in 2.3
    set nodes [java::new {java.util.ArrayList}]
    $nodes add $node2
    $nodes add $node3
    $nodes add $node4
    set p2 [$p subgraph $nodes]
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
test Graph-4.2 {Test the construction and string representation of a larger
        graph} {
    # Build a new graph
    set p3 [java::new ptolemy.graph.Graph 9 13]
    # FIXME: Can we use some sort of looping here?
    set vw1 [java::new {java.lang.String String} v1]
    set vw2 [java::new {java.lang.String String} v2]
    set vw3 [java::new {java.lang.String String} v3]
    set vw4 [java::new {java.lang.String String} v4]
    set vw5 [java::new {java.lang.String String} v5]
    set vw6 [java::new {java.lang.String String} v6]
    set vw7 [java::new {java.lang.String String} v7]
    set vw8 [java::new {java.lang.String String} v8]
    set vw9 [java::new {java.lang.String String} v9]
    set v1 [$p3 addNodeWeight $vw1]
    set v2 [$p3 addNodeWeight $vw2]
    set v3 [$p3 addNodeWeight $vw3]
    set v4 [$p3 addNodeWeight $vw4]
    set v5 [$p3 addNodeWeight $vw5]
    set v6 [$p3 addNodeWeight $vw6]
    set v7 [$p3 addNodeWeight $vw7]
    set v8 [$p3 addNodeWeight $vw8]
    set v9 [$p3 addNodeWeight $vw9]
    set ew1 [java::new {java.lang.String String} e1]
    set ew2 [java::new {java.lang.String String} e2]
    set ew3 [java::new {java.lang.String String} e3]
    set ew4 [java::new {java.lang.String String} e4]
    set ew5 [java::new {java.lang.String String} e5]
    set ew6 [java::new {java.lang.String String} e6]
    set ew7 [java::new {java.lang.String String} e7]
    set ew8 [java::new {java.lang.String String} e8]
    set ew9 [java::new {java.lang.String String} e9]
    set ew10 [java::new {java.lang.String String} e10]
    set ew11 [java::new {java.lang.String String} e11]
    set ew12 [java::new {java.lang.String String} e12]
    set ew13 [java::new {java.lang.String String} e13]
    set e1 [$p3 addEdge [java::new ptolemy.graph.Edge $v3 $v7 $ew1]]
    set e2 [$p3 addEdge [java::new ptolemy.graph.Edge $v3 $v6 $ew2]]
    set e3 [$p3 addEdge [java::new ptolemy.graph.Edge $v5 $v3 $ew3]]
    set e4 [$p3 addEdge [java::new ptolemy.graph.Edge $v5 $v6 $ew4]]
    set e5 [$p3 addEdge [java::new ptolemy.graph.Edge $v7 $v5 $ew5]]
    set e6 [$p3 addEdge [java::new ptolemy.graph.Edge $v4 $v2 $ew6]]
    set e7 [$p3 addEdge [java::new ptolemy.graph.Edge $v2 $v4 $ew7]]
    set e8 [$p3 addEdge [java::new ptolemy.graph.Edge $v1 $v8 $ew8]]
    set e9 [$p3 addEdge [java::new ptolemy.graph.Edge $v1 $v3 $ew9]]
    set e10 [$p3 addEdge [java::new ptolemy.graph.Edge $v4 $v9 $ew10]]
    set e11 [$p3 addEdge [java::new ptolemy.graph.Edge $v8 $v1 $ew11]]
    set e12 [$p3 addEdge [java::new ptolemy.graph.Edge $v9 $v9 $ew12]]
    set e13 [$p3 addEdge [java::new ptolemy.graph.Edge $v5 $v7 $ew13]]
    list [$p3 toString]
} {{{ptolemy.graph.Graph
Node Set:
0: v1
1: v2
2: v3
3: v4
4: v5
5: v6
6: v7
7: v8
8: v9
Edge Set:
0: (v3, v7, e1)
1: (v3, v6, e2)
2: (v5, v3, e3)
3: (v5, v6, e4)
4: (v7, v5, e5)
5: (v4, v2, e6)
6: (v2, v4, e7)
7: (v1, v8, e8)
8: (v1, v3, e9)
9: (v4, v9, e10)
10: (v8, v1, e11)
11: (v9, v9, e12)
12: (v5, v7, e13)
}
}}

######################################################################
####
#
test Graph-4.3 {Test computation of connected components} {
    set collection [$p3 connectedComponents]
    set obj [java::cast java.util.Collection $collection]
    set result [java::call ptolemy.graph.test.Utilities toSortedString $obj 1]
    list $result
} {{[[v1, v3, v5, v6, v7, v8], [v2, v4, v9]]}}

######################################################################
####
# 
test Graph-4.4 {Test construction of a proper subgraph} {
    # use the graph built in 2.3
    set nodes [java::new {java.util.ArrayList}]
    $nodes add $v1
    $nodes add $v8
    $nodes add $v5
    $nodes add $v3
    $nodes add $v7
    $nodes add $v9
    set edges [java::new {java.util.ArrayList}]
    $edges add $e11
    $edges add $e5
    $edges add $e13
    $edges add $e12
    set p4 [$p3 subgraph $nodes $edges]
    list [$p4 toString]
} {{{ptolemy.graph.Graph
Node Set:
0: v1
1: v8
2: v5
3: v3
4: v7
5: v9
Edge Set:
0: (v8, v1, e11)
1: (v7, v5, e5)
2: (v5, v7, e13)
3: (v9, v9, e12)
}
}}

######################################################################
####
# 
test Graph-4.5 {Test node and edge counts.} {
    list [$p3 nodeCount] [$p3 edgeCount] \
            [$p4 nodeCount] [$p4 edgeCount]  
} {9 13 6 4}

######################################################################
####
# 
test Graph-5.1 {Tests for containment. Use the subgraph p4 constructed above.} {
    list [$p4 containsEdge $e8] [$p4 containsEdge $e11] \
            [$p4 containsNode $v6] [$p4 containsNode $v3] \
            [$p4 containsEdgeWeight $ew8] [$p4 containsEdgeWeight $ew11] \
            [$p4 containsNodeWeight $vw6] [$p4 containsNodeWeight $vw3] 
} {0 1 0 1 0 1 0 1}

######################################################################
####
# 
test Graph-5.2 {Test removal of nodes and edges.} {
    $p4 removeNode $v8
    $p4 removeEdge $e5
    $p4 removeNode $v3
    list [$p4 toString]
} {{{ptolemy.graph.Graph
Node Set:
0: v1
1: v5
2: v7
3: v9
Edge Set:
0: (v5, v7, e13)
1: (v9, v9, e12)
}
}}

######################################################################
####
#
test Graph-5.3 {Test addition of nodes and edges after removal of others.} {
    $p4 addEdge [java::new ptolemy.graph.Edge $v5 $v5]
    $p4 addEdge [java::new ptolemy.graph.Edge $v5 $v5 $ew5]
    $p4 addNode $v8
    $p4 addEdge [java::new ptolemy.graph.Edge $v8 $v8]
    $p4 addEdge [java::new ptolemy.graph.Edge $v5 $v5 $ew5]
    list [$p4 toString]
} {{{ptolemy.graph.Graph
Node Set:
0: v1
1: v5
2: v7
3: v9
4: v8
Edge Set:
0: (v5, v7, e13)
1: (v9, v9, e12)
2: (v5, v5)
3: (v5, v5, e5)
4: (v8, v8)
5: (v5, v5, e5)
}
}}

######################################################################
####
#
test Graph-5.4 {Test self-loop edges} {
    set loops [java::cast java.lang.Object [$p4 selfLoopEdges]]
    list [$loops toString]  
} {{[(v9, v9, e12), (v5, v5), (v5, v5, e5), (v8, v8), (v5, v5, e5)]}}

######################################################################
####
# 
test Graph-5.5 {Test self-loop edges ofindividual nodes} {
    set loops1 [java::cast java.lang.Object [$p4 selfLoopEdges $v5]]
    set loops2 [java::cast java.lang.Object [$p4 selfLoopEdges $v8]]
    set loops3 [java::cast java.lang.Object [$p4 selfLoopEdges $v1]]
    list [$loops1 toString]  [$loops2 toString] [$loops3 toString]  
} {{[(v5, v5), (v5, v5, e5), (v5, v5, e5)]} {[(v8, v8)]} {[]}}


######################################################################
####
# 
test Graph-5.6 { neighbors } {
    set p [java::new ptolemy.graph.Graph]
    set n1 [java::new {java.lang.String String} node1]
    set n2 [java::new {java.lang.String String} node2]
    set n3 [java::new {java.lang.String String} node3]
    set n4 [java::new {java.lang.String String} node4]
    set n5 [java::new {java.lang.String String} node5]
    set z [$p addNodeWeight $n1]
    $p addNodeWeight $n2
    $p addNodeWeight $n3
    $p addNodeWeight $n4
    $p addNodeWeight $n5
    $p addEdge $n1 $n2
    $p addEdge $n1 $n2
    $p addEdge $n1 $n4
    $p addEdge $n4 $n1
    $p addEdge $n4 $n1
    $p addEdge $n4 $n1
    $p addEdge $n1 $n1
    $p addEdge $n1 $n1
    $p addEdge $n3 $n2
    $p addEdge $n3 $n5
    set s [$p neighbors $z]
    set result [java::call ptolemy.graph.test.Utilities toSortedString $s 1]
    list $result
} {{[node1, node2, node4]}}

######################################################################
####
# 
test Graph-5.7 { neighbors with duplicate node weights} {
    set p [java::new ptolemy.graph.Graph]
    set n1 [java::new {java.lang.String String} node1]
    set n2 [java::new {java.lang.String String} node2]
    set n3 [java::new {java.lang.String String} node3]
    set n4 [java::new {java.lang.String String} node4]
    set n5 [java::new {java.lang.String String} node4]
    set z [$p addNodeWeight $n1]
    $p addNodeWeight $n2
    $p addNodeWeight $n3
    $p addNodeWeight $n4
    $p addNodeWeight $n5
    $p addEdge $n1 $n2
    $p addEdge $n1 $n2
# The following addEdge adds two edges (to the two nodes that have weight 
# node4).
    $p addEdge $n1 $n4
    $p addEdge $n1 $n1
    $p addEdge $n1 $n1
    $p addEdge $n3 $n2
    $p addEdge $n3 $n5
    set s [$p neighbors $z]
    set result [java::call ptolemy.graph.test.Utilities toSortedString $s 1]
    list $result
} {{[node1, node2, node4, node4]}}

######################################################################
####
#
test Graph-6.1 {Test neighbor edges} {
    set collection [$p3 neighborEdges $v1 $v8]
    set obj [java::cast java.util.Collection $collection]
    set result1 [java::call ptolemy.graph.test.Utilities toSortedString $obj 1]
    set collection [$p3 neighborEdges $v3 $v6]
    set obj [java::cast java.util.Collection $collection]
    set result2 [java::call ptolemy.graph.test.Utilities toSortedString $obj 1]
    set collection [$p3 neighborEdges $v5 $v9]
    set obj [java::cast java.util.Collection $collection]
    set result3 [java::call ptolemy.graph.test.Utilities toSortedString $obj 1]
    set collection [$p3 neighborEdges $v9 $v9]
    set obj [java::cast java.util.Collection $collection]
    set result4 [java::call ptolemy.graph.test.Utilities toSortedString $obj 1]
    list $result1 $result2 $result3 $result4
} {{[(v1, v8, e8), (v8, v1, e11)]} {[(v3, v6, e2)]} {[]} {[(v4, v9, e10), (v9, v9, e12)]}}

######################################################################
####
#
test Graph-7.1 {test clone()} {
    set og [java::new ptolemy.graph.Graph]
    set n1 [java::new ptolemy.graph.Node]
    set n2 [java::new ptolemy.graph.Node]
    set n3 [java::new ptolemy.graph.Node]
    set e1 [java::new ptolemy.graph.Edge $n1 $n2]
    set e2 [java::new ptolemy.graph.Edge $n1 $n3]
    $og addNode $n1
    $og addNode $n2
    $og addNode $n3
    $og addEdge $e1
    $og addEdge $e2
    set cl [$og clone]
    set cg [java::cast ptolemy.graph.Graph $cl]
    set nc [$cg nodeCount]
    set ec [$cg edgeCount]
    set tn1 [$cg containsNode $n1]
    set tn2 [$cg containsNode $n2]
    set tn3 [$cg containsNode $n3]
    set te1 [$cg containsEdge $e1]
    set te2 [$cg containsEdge $e2]
    list $nc $ec $tn1 $tn2 $tn3 $te1 $te2
} {3 2 1 1 1 1 1}

######################################################################
####
#
test Graph-7.2 {standard clone() tests} {
    set ocls  [$og getClass]
    set ccls  [$cg getClass]
    set oocls [java::cast java.lang.Object $ocls]
    set occls [java::cast java.lang.Object $ccls]
    set rule2 [$oocls equals $occls]
    set rule3 [$cg equals $og]
    list $rule2 $rule3
} {1 1}

######################################################################
####
#
test Graph-7.3 {test equals(): change topology and compare with the clone} {
    set eq1 [$cg equals $og]
    $og removeNode $n2
    set eq2 [$cg equals $og]
    $og addNode $n2
    $og addEdge $e1
    set eq3 [$cg equals $og]
    set n4  [java::new ptolemy.graph.Node]
    $og addNode $n4
    set eq4 [$cg equals $og]
    list $eq1 $eq2 $eq3 $eq4
} {1 0 1 0}

######################################################################
####
#
test Graph-7.4 {test equals(): reflexive, symmetric, transitive, and consistent} {
    set cl1 [$og clone]
    set cl2 [$og clone]
    set cg1 [java::cast ptolemy.graph.Graph $cl1]
    set cg2 [java::cast ptolemy.graph.Graph $cl2]
    set r  [$og equals $og]
    set s1 [$og equals $cg1]
    set s2 [$cg1 equals $og]
    set t1 [$og equals $cg1]
    set t2 [$cg1 equals $cg2]
    set t3 [$og equals $cg2]
    set c1 [$og equals $cg1]
    set c2 [$og equals $cg1]
    set c3 [$og equals $cg1]
    set c4 [$og equals $cg1]
    list $r $s1 $s2 $t1 $t2 $t3 $c1 $c2 $c3 $c4
} {1 1 1 1 1 1 1 1 1 1}

######################################################################
####
#
test Graph-7.5 {test hashCode()} {
    set hog   [$og hashCode]
    set hcg1  [$cg1 hashCode]
    set hiog  [java::new java.lang.Integer $hog]
    set hicg1 [java::new java.lang.Integer $hcg1]
    set equ   [$hiog equals $hicg1]

    $cg1 removeNode $n3
    set hcg1  [$cg1 hashCode]
    set hicg1 [java::new java.lang.Integer $hcg1]
    set nequ  [$hiog equals $hicg1]
    list $equ $nequ
} {1 0}

######################################################################
####
#
test Graph-7.6 {test cloneAs()} {
    set dirg [java::new ptolemy.graph.DirectedGraph]
    set clng [$og cloneAs $dirg]
    set clncls [$clng getClass]
    set clnnam [$clncls getName]
    list $clnnam
} {ptolemy.graph.DirectedGraph}

######################################################################
####
#
test Graph-7.7 {test hashCode() and equals(): graphs of different types with
the same nodes and edges} {
    set sameg    [$og equals $clng]
    set hashog   [$og hashCode]
    set hashclng [$clng hashCode]
    set ogv      [java::new java.lang.Integer $hashog]
    set cgv      [java::new java.lang.Integer $hashclng]
    set ehash    [$cgv equals $ogv]
    list $sameg $ehash
} {0 0}

######################################################################
####
#
test Graph-9.1 {testing the validateWeight(Node) method} {
    set gr  [java::new ptolemy.graph.Graph]
    set nw1 [java::new String "weight1"]
    set nw2 [java::new String "weight2"]
    set n1  [java::new ptolemy.graph.Node $nw1]
    set n2  [java::new ptolemy.graph.Node $nw2]
    set n3  [java::new ptolemy.graph.Node $nw1]
    $gr addNode $n1
    $gr addNode $n2
    $gr addNode $n3
    # There should be 2 elements in set1 and 1 element in set2
    set set1 [[java::new java.util.Vector [$gr nodes $nw1]] size]
    set set2 [[java::new java.util.Vector [$gr nodes $nw2]] size]
    set counter1 [$gr changeCount]
    $n1 setWeight null
    # Result should be 1 since weight has changed
    set result [$gr validateWeight $n1]
    # There should be 1 element at each.
    set set3 [[java::new java.util.Vector [$gr nodes $nw1]] size]
    set set4 [[java::new java.util.Vector [$gr nodes $nw2]] size]
    $n1 setWeight $nw2
    # Result should be 1 since weight has changed
    set result2 [$gr validateWeight $n1]
    # Now there should be 1 element in set1 and 2 elements in set2
    set set5 [[java::new java.util.Vector [$gr nodes $nw1]] size]
    set set6 [[java::new java.util.Vector [$gr nodes $nw2]] size]
    set counter2 [$gr changeCount]
    list $set1 $set2 $result $set3 $set4 $result2 $set5 $set6 $counter1 \
    $counter2
} {2 1 1 1 1 1 1 2 3 5}

######################################################################
####
#
test Graph-9.2 {testing validateWeight(Node) method's effect on the counter} {
    #Using the same graph in 9.1
    set result1 [$gr validateWeight $n1]
    #This shouldn't change the counter
    set counter3 [$gr changeCount]
    #Now the counter should change
    $n1 setWeight $nw1
    set result2 [$gr validateWeight $n1]
    set counter4 [$gr changeCount]
    list $result1 $counter3 $result2 $counter4
} {0 5 1 6}

######################################################################
####
#
test Graph-9.3 {testing the GraphElementException} {
    #Using the graph in 9.1
    set n4 [java::new ptolemy.graph.Node $nw1]
    $gr addNode $n4
    $gr removeNode $n4
    $n4 setWeight $nw2
    catch {$gr validateWeight $n4} msg
    list $msg
} {{ptolemy.graph.GraphElementException: The specified node is not in the graph.
Dumps of the offending node and graph follow.
The offending node:
weight2
The offending graph:
{ptolemy.graph.Graph
Node Set:
0: weight1
1: weight2
2: weight1
Edge Set:

}

}}

######################################################################
####
#
test Graph-9.4 {testing validateWeight(Node, Weight)} {
    set gr  [java::new ptolemy.graph.Graph]
    set nw1 [java::new String "weight1"]
    set nw2 [java::new String "weight2"]
    set n1  [java::new ptolemy.graph.Node $nw1]
    set n2  [java::new ptolemy.graph.Node $nw2]
    set n3  [java::new ptolemy.graph.Node $nw1]
    $gr addNode $n1
    $gr addNode $n2
    $gr addNode $n3
    # There should be 2 elements in set1 and 1 element in set2
    set set1 [[java::new java.util.Vector [$gr nodes $nw1]] size]
    set set2 [[java::new java.util.Vector [$gr nodes $nw2]] size]
    set counter1 [$gr changeCount]
    $n1 setWeight null
    # Result should be 1 since weight has changed
    set result [$gr validateWeight $n1 $nw1]
    # There should be 1 element at each.
    set set3 [[java::new java.util.Vector [$gr nodes $nw1]] size]
    set set4 [[java::new java.util.Vector [$gr nodes $nw2]] size]
    $n1 setWeight $nw2
    # Result should be 1 since weight has changed
    set result2 [$gr validateWeight $n1 null]
    # Now there should be 1 element in set1 and 2 elements in set2
    set set5 [[java::new java.util.Vector [$gr nodes $nw1]] size]
    set set6 [[java::new java.util.Vector [$gr nodes $nw2]] size]
    set counter2 [$gr changeCount]
    list $set1 $set2 $result $set3 $set4 $result2 $set5 $set6 $counter1 \
    $counter2
} {2 1 1 1 1 1 1 2 3 5}

######################################################################
####
#
test Graph-9.5 {testing validateWeight(Node, Weight) method's effect on the counter} {
    #Using the same graph in 9.4
    set result1 [$gr validateWeight $n1 $nw2]
    #This shouldn't change the counter
    set counter3 [$gr changeCount]
    #Now the counter should change
    $n1 setWeight $nw1
    set result2 [$gr validateWeight $n1 $nw2]
    set counter4 [$gr changeCount]
    list $result1 $counter3 $result2 $counter4
} {0 5 1 6}

######################################################################
####
#
test Graph-9.6 {testing validateWeight(Node, Weight) exceptions} {
    #Using the graph in 9.4
    set n4 [java::new ptolemy.graph.Node $nw1]
    $gr addNode $n4
    $gr removeNode $n4
    $n4 setWeight $nw2
    catch {$gr validateWeight $n4 $nw1} msg1
    catch {$gr validateWeight $n1 $nw2} msg2
    catch {$gr validateWeight $n1 null} msg3
    $n1 setWeight null
    catch {$gr validateWeight $n1 null} msg4
    set set7 [[java::new java.util.Vector [$gr nodes $nw1]] size]
    set set8 [[java::new java.util.Vector [$gr nodes $nw2]] size]
    set set9 [[java::new java.util.Vector [$gr nodes null]] size]
    $gr validateWeight $n1 $nw1
    set set10 [[java::new java.util.Vector [$gr nodes $nw1]] size]
    set set11 [[java::new java.util.Vector [$gr nodes $nw2]] size]
    set set12 [[java::new java.util.Vector [$gr nodes null]] size]
    list $msg1 $msg2 $msg3 $msg4 $set7 $set8 $set9 $set10 $set11 $set12
} {{ptolemy.graph.GraphElementException: The specified node is not in the graph.
Dumps of the offending node and graph follow.
The offending node:
weight2
The offending graph:
{ptolemy.graph.Graph
Node Set:
0: weight1
1: weight2
2: weight1
Edge Set:

}

} {ptolemy.graph.GraphWeightException: Incorrect previous weight specified.
The weight is of class java.lang.String and its description follows:
weight2
A Dump of the offending graph follows.
{ptolemy.graph.Graph
Node Set:
0: weight1
1: weight2
2: weight1
Edge Set:

}

} {ptolemy.graph.GraphWeightException: Incorrect previous weight specified.
The weight is of class java.lang.String and its description follows:
null
A Dump of the offending graph follows.
{ptolemy.graph.Graph
Node Set:
0: weight1
1: weight2
2: weight1
Edge Set:

}

} {ptolemy.graph.GraphWeightException: Incorrect previous weight specified.
The weight is of class java.lang.String and its description follows:
null
A Dump of the offending graph follows.
{ptolemy.graph.Graph
Node Set:
0: null
1: weight2
2: weight1
Edge Set:

}

} 2 1 0 1 1 1}

######################################################################
####
#
test Graph-10.1 {tesing hideEdge(Edge) method by counters on edges, null edges are tested} {
    set oneg [java::new ptolemy.graph.Graph]
    set n1  [java::new ptolemy.graph.Node]
    set n2  [java::new ptolemy.graph.Node]
    $oneg addNode $n1
    $oneg addNode $n2
    set edge [$oneg addEdge $n2 $n1]
    $oneg hideEdge $edge
    set result0 [$oneg hideEdge $edge]
    set result1 [$oneg hiddenEdgeCount]
    set result2 [$oneg edgeCount]
    $oneg restoreEdge $edge
    set result3 [$oneg restoreEdge $edge]
    set result4 [$oneg hiddenEdgeCount]
    set result5 [$oneg edgeCount]
    #tests for null edges
    set edge1 [java::new ptolemy.graph.Edge $n2 $n3]
    set result6 [$oneg hideEdge $edge1]
    set result7 [$oneg restoreEdge $edge1]
    set result8 [$oneg changeCount]
    list $result0 $result1 $result2 $result3 $result4 $result5 $result6 $result7 $result8
} {0 1 0 0 0 1 0 0 5}

######################################################################
####
#
test Graph-10.2 {testing hideEdge(Edge) method and hidden(edge)} {
    set oneg [java::new ptolemy.graph.Graph]
    set n1  [java::new ptolemy.graph.Node]
    set n2  [java::new ptolemy.graph.Node]
    $oneg addNode $n1
    $oneg addNode $n2
    set edge [$oneg addEdge $n2 $n1]
    $oneg hideEdge $edge
    set result1 [$oneg containsEdge $edge]
    set result2 [$oneg hidden $edge]
    set result3 [[java::new java.util.Vector [$oneg incidentEdges $n2]] toString]
    set result4 [[java::new java.util.Vector [$oneg edges]] toString]
    set result5 [$oneg removeEdge $edge]
    set result6 [$oneg restoreEdge $edge]
    list $result1 $result2 $result3 $result4 $result5 $result6
} {0 1 {[]} {[]} 1 0}

######################################################################
####
#
test Graph-10.3 {checking restoration of an edge whose one node is deleted} {
    set oneg [java::new ptolemy.graph.Graph]
    set n1  [java::new {java.lang.String String} node1]
    set n2  [java::new {java.lang.String String} node2]
    set node1 [$oneg addNodeWeight $n1]
    set node2 [$oneg addNodeWeight $n2]
    set edge [$oneg addEdge $node1 $node2]
    $oneg hideEdge $edge
    $oneg removeNode $node1
    catch {$oneg {restoreEdge ptolemy.graph.Edge} $edge} msg
    list $msg
} {{ptolemy.graph.GraphElementException: Source node is not in the graph.
Dumps of the offending edge and graph follow.
The offending edge:
(node1, node2)
The offending graph:
{ptolemy.graph.Graph
Node Set:
0: node2
Edge Set:
0: (node1, node2)
}

}}

######################################################################
####
#
test Graph-10.4 {checking hiddenEdges() and edgeCount() and
hiddenEdgeCount()} {
    set oneg [java::new ptolemy.graph.Graph]
    set n1  [java::new {java.lang.String String} node1]
    set n2  [java::new {java.lang.String String} node2]
    set n3  [java::new {java.lang.String String} node3]
    set node1 [$oneg addNodeWeight $n1]
    set node2 [$oneg addNodeWeight $n2]
    set node3 [$oneg addNodeWeight $n3]
    set edge1 [$oneg addEdge $node1 $node2]
    set edge2 [$oneg addEdge $node2 $node3]
    set edge3 [$oneg addEdge $node3 $node2]
    set edge4 [$oneg addEdge $node2 $node1]
    $oneg hideEdge $edge3
    $oneg hideEdge $edge4
    set result1 [$oneg edgeCount]
    set result2 [$oneg hiddenEdgeCount]
    set result3 [[$oneg hiddenEdges] contains $edge3]
    set result4 [[$oneg hiddenEdges] contains $edge4]
    list $result1 $result2 $result3 $result4
} {2 2 1 1}

######################################################################
####
#
test Graph-11.1 {testing addGraph(Graph)} {
    set gr  [java::new ptolemy.graph.Graph]
    set nw1 [java::new String "weight1"]
    set nw2 [java::new String "weight2"]
    set n1  [java::new ptolemy.graph.Node $nw1]
    set n2  [java::new ptolemy.graph.Node $nw2]
    set n3  [java::new ptolemy.graph.Node $nw1]
    set e1  [java::new ptolemy.graph.Edge $n1 $n2]
    $gr addNode $n1
    $gr addNode $n2
    $gr addNode $n3
    $gr addEdge $e1
    set gr2 [java::new ptolemy.graph.Graph]
    set n4  [java::new ptolemy.graph.Node]
    set n5  [java::new ptolemy.graph.Node]
    set n6  [java::new ptolemy.graph.Node]
    set e2  [java::new ptolemy.graph.Edge $n4 $n5]
    set e3  [java::new ptolemy.graph.Edge $n5 $n6]
    set e4  [java::new ptolemy.graph.Edge $n6 $n4]
    $gr2 addNode $n1
    $gr2 addNode $n4
    $gr2 addNode $n5
    $gr2 addNode $n6
    $gr2 addEdge $e2
    $gr2 addEdge $e3
    $gr2 addEdge $e4
    set description1 [$gr  toString]
    set description2 [$gr2 toString]
    # This should throw an exception because n1 is included in both graphs.
    catch {$gr2 addGraph $gr} msg
    $gr2 removeNode $n1
    set returnValue [$gr2 addGraph $gr]
    set description3 [$gr2 toString]
    # This should return false since we are adding an empty graph.
    set returnValue2 [$gr2 addGraph [java::new ptolemy.graph.Graph]]
    list $description1 $description2 $msg $description3 $returnValue \
    $returnValue2
} {{{ptolemy.graph.Graph
Node Set:
0: weight1
1: weight2
2: weight1
Edge Set:
0: (weight1, weight2)
}
} {{ptolemy.graph.Graph
Node Set:
0: weight1
1: <unweighted node>
2: <unweighted node>
3: <unweighted node>
Edge Set:
0: (<unweighted node>, <unweighted node>)
1: (<unweighted node>, <unweighted node>)
2: (<unweighted node>, <unweighted node>)
}
} {ptolemy.graph.GraphConstructionException: Attempt to add a node that is already contained in the graph.
Dumps of the offending node and graph follow.
The offending node:
weight1
The offending graph:
{ptolemy.graph.Graph
Node Set:
0: weight1
1: <unweighted node>
2: <unweighted node>
3: <unweighted node>
Edge Set:
0: (<unweighted node>, <unweighted node>)
1: (<unweighted node>, <unweighted node>)
2: (<unweighted node>, <unweighted node>)
}

} {{ptolemy.graph.Graph
Node Set:
0: <unweighted node>
1: <unweighted node>
2: <unweighted node>
3: weight1
4: weight2
5: weight1
Edge Set:
0: (<unweighted node>, <unweighted node>)
1: (<unweighted node>, <unweighted node>)
2: (<unweighted node>, <unweighted node>)
3: (weight1, weight2)
}
} 1 0}
