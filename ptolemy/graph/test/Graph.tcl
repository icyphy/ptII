# Tests for the Graph class
#
# @Author: Yuhong Xiong
#
# $Id$
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
test Graph-2.1 {Create an empty instance} {
    set p [java::new ptolemy.graph.Graph]
    $p contains null
} {0}

######################################################################
####
# 
test Graph-2.2 {Create a graph with 2 nodes} {
    set p [java::new ptolemy.graph.Graph]
    set n1 [java::new {java.lang.String String} node1]
    set n2 [java::new {java.lang.String String} node2]
    $p add $n1
    $p add $n2
    $p addEdge $n1 $n2
    $p addEdge $n2 $n1
    list [$p contains $n1] [$p getEdgeCount] [$p getNodeCount]
} {1 2 2}

######################################################################
####
# 
test Graph-2.3 {try to add duplicate nodes} {
    # use the graph above
    catch {$p add $n1} msg
    list $msg
} {{java.lang.IllegalArgumentException: Graph.add: Object is already in the graph.}}

######################################################################
####
# 
test Graph-2.4 {Create a graph with 4 nodes forming a diamond} {
    set p [java::new ptolemy.graph.Graph]
    set n1 [java::new {java.lang.String String} node1]
    set n2 [java::new {java.lang.String String} node2]
    set n3 [java::new {java.lang.String String} node3]
    set n4 [java::new {java.lang.String String} node4]
    $p add $n1
    $p add $n2
    $p add $n3
    $p add $n4
    $p addEdge $n1 $n2
    $p addEdge $n1 $n3
    $p addEdge $n2 $n4
    $p addEdge $n3 $n4
    set nodes [$p getNodes]
    set edges [$p getEdges]
    set e0 [$edges get 0]
    set e1 [$edges get 1]
    set e2 [$edges get 2]
    set e3 [$edges get 3]
    list [$nodes get 0] [$nodes get 1] [$nodes get 2] [$nodes get 3] \
	 [$e0 get 0] [$e0 get 1] [$e1 get 0] [$e1 get 1] \
	 [$e2 get 0] [$e2 get 1] [$e3 get 0] [$e3 get 1]
} {node1 node2 node3 node4 node1 node2 node1 node3 node2 node4 node3 node4}

######################################################################
####
# 
test Graph-3.1 {Test description} {
    # use the graph built in 2.3
    list [$p description]
} {{{ptolemy.graph.Graph
  {node1 node2 node3}
  {node2 node4}
  {node3 node4}
  {node4}
}}}

