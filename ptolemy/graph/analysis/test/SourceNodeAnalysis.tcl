# Tests for SourceNodeAnalysis class
#
# @Author: Mingyung Ko
#
# $Id$
#
# @Copyright (c) 1997-2005 The Regents of the University of California.
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
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

#
#


######################################################################
####
#
test SourceNodeAnalysis-2.1 {empty graph} {
    set dirg [java::new ptolemy.graph.DirectedGraph]
    set srcana [java::new ptolemy.graph.analysis.SourceNodeAnalysis $dirg]
    set srcnodes [$srcana nodes]
    set numnodes [$srcnodes size]
    list $numnodes
} {0}

######################################################################
####
#
test SourceNodeAnalysis-2.2 {4-node graph with 2 source nodes} {
    set nw1 [java::new {java.lang.String String} node1]
    set nw2 [java::new {java.lang.String String} node2]
    set nw3 [java::new {java.lang.String String} node3]
    set nw4 [java::new {java.lang.String String} node4]
    set n1  [java::new ptolemy.graph.Node $nw1]
    set n2  [java::new ptolemy.graph.Node $nw2]
    set n3  [java::new ptolemy.graph.Node $nw3]
    set n4  [java::new ptolemy.graph.Node $nw4]
    set e1  [java::new ptolemy.graph.Edge $n1 $n2]
    set e2  [java::new ptolemy.graph.Edge $n1 $n3]
    set e3  [java::new ptolemy.graph.Edge $n3 $n2]
    set e4  [java::new ptolemy.graph.Edge $n4 $n3]
    $dirg addNode $n1
    $dirg addNode $n2
    $dirg addNode $n3
    $dirg addNode $n4
    $dirg addEdge $e1
    $dirg addEdge $e2
    $dirg addEdge $e3
    $dirg addEdge $e4
    set srcnodes [$srcana nodes]
    set numnodes [$srcnodes size]
    set rslt0 [$srcnodes get 0]
    set rslt1 [$srcnodes get 1]
    list $numnodes [$rslt0 toString] [$rslt1 toString]
} {2 node1 node4}

######################################################################
####
#
test SourceNodeAnalysis-2.3 {graph without source node} {
    set e5  [java::new ptolemy.graph.Edge $n2 $n4]
    set e6  [java::new ptolemy.graph.Edge $n4 $n1]
    $dirg addEdge $e5
    $dirg addEdge $e6
    set srcnodes [$srcana nodes]
    set numnodes [$srcnodes size]
    list $numnodes
} {0}

