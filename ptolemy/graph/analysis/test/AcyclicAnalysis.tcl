# Tests for AcyclicAnalysis class
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
test AcyclicAnalysis-2.1 {empty graph} {
    set dirg [java::new ptolemy.graph.DirectedGraph]
    set acycana [java::new ptolemy.graph.analysis.AcyclicAnalysis $dirg]
    set result  [$acycana result]
    list $result
} {1}

######################################################################
####
#
test AcyclicAnalysis-2.2 {3-node graph, check transitiveClosure()} {
    set n1  [java::new ptolemy.graph.Node]
    set n2  [java::new ptolemy.graph.Node]
    set n3  [java::new ptolemy.graph.Node]
    set e1  [java::new ptolemy.graph.Edge $n1 $n2]
    set e2  [java::new ptolemy.graph.Edge $n2 $n3]
    $dirg addNode $n1
    $dirg addNode $n2
    $dirg addNode $n3
    $dirg addEdge $e1
    $dirg addEdge $e2
    set closure  [$acycana transitiveClosure]
    set tcmatrix [java::cast {boolean[][]} $closure]
    set row1     [$tcmatrix get 0]
    set row2     [$tcmatrix get 1]
    set row3     [$tcmatrix get 2]
    set tc11 [$row1 get 0]
    set tc12 [$row1 get 1]
    set tc13 [$row1 get 2]
    set tc21 [$row2 get 0]
    set tc22 [$row2 get 1]
    set tc23 [$row2 get 2]
    set tc31 [$row3 get 0]
    set tc32 [$row3 get 1]
    set tc33 [$row3 get 2]
    list $tc11 $tc12 $tc13 $tc21 $tc22 $tc23 $tc31 $tc32 $tc33
} {0 1 1 0 0 1 0 0 0}

######################################################################
####
#
test AcyclicAnalysis-2.3 {acyclic graph} {
    set result [$acycana result]
    list $result
} {1}

######################################################################
####
#
test AcyclicAnalysis-2.4 {graph with cycle} {
    set e3 [java::new ptolemy.graph.Edge $n2 $n1]
    $dirg addEdge $e3
    set result [$acycana result]
    list $result
} {0}

######################################################################
####
#
test AcyclicAnalysis-2.5 {compatible: wrong graph class} {
    set g [java::new ptolemy.graph.Graph]
    set incomp [$acycana compatible $g]
    list $incomp
} {0}
