# Tests maximum/minimum cycle mean analysis of a graph.
#
# @Author: Shahrooz Shahparnia
#
# $Id$
#
# @Copyright (c) 2002 The Regents of the University of Maryland.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
#                                           PT_COPYRIGHT_VERSION_2
#                                           COPYRIGHTENDKEY
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
test CycleMeanAnalysis-1.1 {a directed graph example} {
    set dcg [java::new ptolemy.graph.DirectedGraph]
    set n1 [$dcg addNodeWeight "0"]
    set n2 [$dcg addNodeWeight "1"]
    set n3 [$dcg addNodeWeight "2"]
    set n4 [$dcg addNodeWeight "3"]
    set n5 [$dcg addNodeWeight "4"]
    set n6 [$dcg addNodeWeight "5"]
    set n7 [$dcg addNodeWeight "6"]
    set e1 [$dcg addEdge $n1 $n2]
    set e2 [$dcg addEdge $n2 $n3]
    set e3 [$dcg addEdge $n3 $n4]
    set e4 [$dcg addEdge $n4 $n1]
    set e5 [$dcg addEdge $n1 $n3]
    set e6 [$dcg addEdge $n5 $n6]
    set e7 [$dcg addEdge $n6 $n7]
    set e8 [$dcg addEdge $n7 $n5]
    set edgeCost [java::new java.util.HashMap]
    $edgeCost put $e1 [java::new Double 3]
    $edgeCost put $e2 [java::new Double 4]
    $edgeCost put $e3 [java::new Double 7]
    $edgeCost put $e4 [java::new Double 2]
    $edgeCost put $e5 [java::new Double 2]
    $edgeCost put $e6 [java::new Double -3]
    $edgeCost put $e7 [java::new Double -2]
    $edgeCost put $e8 [java::new Double 2]
    set doubleMapping [java::new ptolemy.graph.mapping.ToDoubleMapMapping $edgeCost]
    set analysis [java::new ptolemy.graph.analysis.CycleMeanAnalysis $dcg\
            $doubleMapping]
    set result [$analysis maximumCycleMean]
    list $result
} {4.0}

######################################################################
####
#
test CycleMeanAnalysis-1.2 {the cycle} {
    set cycle [$analysis cycle]
    list [[java::cast {java.lang.Object} $cycle] toString]
} {{[3, 2, 1, 0]}}

######################################################################
####
#
test CycleMeanAnalysis-1.3 {minimum cycle mean} {
    set result [$analysis minimumCycleMean]
    list $result
} {-1.0}

