# Tests for the DirectedGraph class
#
# @Author: Yuhong Xiong
#
# $Id$
#
# @Copyright (c) 1997-1998 The Regents of the University of California.
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
test DirectedGraph-1.1 {Get information about the class} {
    # If anything changes, we want to know about it so we can write tests.
    set n [java::new pt.graph.DirectedGraph]
    list [getJavaInfo $n]
} {{
  class:         pt.graph.DirectedGraph
  fields:        
  methods:       {add java.lang.Object} {addEdge java.lang.Object java.l
    ang.Object} allEdges allNodes {contains java.lang.Objec
    t} {equals java.lang.Object} getClass hashCode isAcycli
    c isDirected notify notifyAll numEdges numNodes {reache
    ableNodes java.lang.Object} toString topSort wait {wait
     long} {wait long int}
    
  constructors:  pt.graph.DirectedGraph {pt.graph.DirectedGraph int}
    
  properties:    acyclic class directed
    
  superclass:    pt.graph.Graph
    
}}

######################################################################
####
# 
test DirectedGraph-2.1 {Create an empty instance} {
    set p [java::new pt.graph.DirectedGraph]
    $p contains null
} {0}

######################################################################
####
# 
test DirectedGraph-2.2 {Create a cyclic graph with 2 nodes} {
    set p [java::new pt.graph.DirectedGraph]
    set n1 [java::new {java.lang.String String} node1]
    set n2 [java::new {java.lang.String String} node2]
    $p add $n1
    $p add $n2
    $p addEdge $n1 $n2
    $p addEdge $n2 $n1
    list [$p isDirected] [$p isAcyclic]
} {1 0}

######################################################################
####
# 
test DirectedGraph-2.3 {Create an acyclic graph with 4 nodes} {
    set p [java::new pt.graph.DirectedGraph]
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
    set sort [$p topSort]
    set sn1 [$sort get 1]
    list [$p isDirected] [$p isAcyclic] [$sort get 0]\
         [$sort get 1] [$sort get 2] [$sort get 3]
} {1 1 node1 node2 node3 node4}

