# Tests for SinkNodeAnalysis class
#
# @Author: Mingyung Ko
#
# $Id$
#
# @Copyright (c) 1997-2002 The Regents of the University of California.
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
test SinkNodeAnalysis-2.1 {empty graph} {
    set dirg [java::new ptolemy.graph.DirectedGraph]
    set sinkana [java::new ptolemy.graph.analysis.SinkNodeAnalysis $dirg]
    set sinknodes [$sinkana nodes]
    set numnodes [$sinknodes size]
    list $numnodes
} {0}

######################################################################
####
#
test SinkNodeAnalysis-2.2 {4-node graph with 2 sink nodes} {
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
    set e4  [java::new ptolemy.graph.Edge $n3 $n4]
    $dirg addNode $n1
    $dirg addNode $n2
    $dirg addNode $n3
    $dirg addNode $n4
    $dirg addEdge $e1
    $dirg addEdge $e2
    $dirg addEdge $e3
    $dirg addEdge $e4
    set sinkana [java::new ptolemy.graph.analysis.SinkNodeAnalysis $dirg]
    set sinknodes [$sinkana nodes]
    set numnodes [$sinknodes size]
    set rslt0 [$sinknodes get 0]
    set rslt1 [$sinknodes get 1]
    list $numnodes [$rslt0 toString] [$rslt1 toString]
} {2 node2 node4}

######################################################################
####
#
test SinkNodeAnalysis-2.3 {graph without sink node} {
    set e5  [java::new ptolemy.graph.Edge $n2 $n4]
    set e6  [java::new ptolemy.graph.Edge $n4 $n1]
    $dirg addEdge $e5
    $dirg addEdge $e6
    set sinkana [java::new ptolemy.graph.analysis.SinkNodeAnalysis $dirg]
    set sinknodes [$sinkana nodes]
    set numnodes [$sinknodes size]
    list $numnodes
} {0}


