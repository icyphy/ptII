# Tests for SelfLoopAnalysis class
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
test SelfLoopAnalysis-2.1 {empty graph} {
    set g [java::new ptolemy.graph.Graph]
    set selfana [java::new ptolemy.graph.analysis.SelfLoopAnalysis $g]
    set selfloops [$selfana edges]
    set numedges [$selfloops size]
    list $numedges
} {0}

######################################################################
####
#
test SelfLoopAnalysis-2.2 {2-node graph with 3 self-loop edges} {
    set n1  [java::new ptolemy.graph.Node]
    set n2  [java::new ptolemy.graph.Node]
    set e1  [java::new ptolemy.graph.Edge $n1 $n1]
    set e2  [java::new ptolemy.graph.Edge $n1 $n1]
    set e3  [java::new ptolemy.graph.Edge $n1 $n2]
    set e4  [java::new ptolemy.graph.Edge $n2 $n2]
    $g addNode $n1
    $g addNode $n2
    $g addEdge $e1
    $g addEdge $e2
    $g addEdge $e3
    $g addEdge $e4
    set selfloops [$selfana edges]
    set numedges [$selfloops size]
    set rslt0 [$selfloops get 0]
    set rslt1 [$selfloops get 1]
    set rslt2 [$selfloops get 2]
    set t0 [$rslt0 equals $e1]
    set t1 [$rslt1 equals $e2]
    set t2 [$rslt2 equals $e4]
    list $numedges $t0 $t1 $t2
} {3 1 1 1}

######################################################################
####
#
test SelfLoopAnalysis-2.3 {graph without self loops} {
    $g removeEdge $e1
    $g removeEdge $e2
    $g removeEdge $e4
    set selfloops [$selfana edges]
    set numedges [$selfloops size]
    list $numedges
} {0}


