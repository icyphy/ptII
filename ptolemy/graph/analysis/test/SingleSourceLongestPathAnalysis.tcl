# Tests for SingleSourceLongestPathAnalysis.
#
# @Author: Shahrooz Shahparnia
#
# $Id$
#
# @Copyright (c) 2001-2002 The Regents of the University of Maryland.
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
test SingleSourceLongestPathAnalysis-1.1 {acyclic graph} {
    set dcg [java::new ptolemy.graph.DirectedGraph]
    set n1 [$dcg addNodeWeight "1"]
    set n2 [$dcg addNodeWeight "2"]
    set n3 [$dcg addNodeWeight "3"]
    set n4 [$dcg addNodeWeight "4"]
    set n5 [$dcg addNodeWeight "5"]
    set n6 [$dcg addNodeWeight "6"]
    set e1 [$dcg addEdge $n1 $n2]
    set e2 [$dcg addEdge $n1 $n3]
    set e3 [$dcg addEdge $n3 $n4]
    set e4 [$dcg addEdge $n2 $n3]
    set e5 [$dcg addEdge $n3 $n6]
    set e6 [$dcg addEdge $n4 $n5]
    set e7 [$dcg addEdge $n1 $n4]
    set e8 [$dcg addEdge $n3 $n5]
    set e9 [$dcg addEdge $n5 $n6]
    set e10 [$dcg addEdge $n2 $n4]
    set edgeCost [java::new java.util.HashMap]
    $edgeCost put $e1 [java::new Double 1]
    $edgeCost put $e2 [java::new Double 2]
    $edgeCost put $e3 [java::new Double 5]
    $edgeCost put $e4 [java::new Double 10]
    $edgeCost put $e5 [java::new Double 8]
    $edgeCost put $e6 [java::new Double 4]
    $edgeCost put $e7 [java::new Double 2]
    $edgeCost put $e8 [java::new Double 1]
    $edgeCost put $e9 [java::new Double 7]
    $edgeCost put $e10 [java::new Double 3]
    set doubleMapping [java::new ptolemy.graph.mapping.ToDoubleMapMapping $edgeCost]
    set analysis [java::new ptolemy.graph.analysis.SingleSourceLongestPathAnalysis $dcg\
            $n1 $doubleMapping]
    set row [$analysis distance]
    set a1      [$row get 0]
    set a2      [$row get 1]
    set a3      [$row get 2]
    set a4      [$row get 3]
    set a5      [$row get 4]
    set a6      [$row get 5]
    list $a1 $a2 $a3 $a4 $a5 $a6
} {0.0 1.0 11.0 16.0 20.0 27.0}

######################################################################
####
#
test SingleSourceLongestPathAnalysis-1.2 {longest path} {
    set result [$analysis pathLength $n4]
    list $result
} {16.0}

######################################################################
####
#
test SingleSourceLongestPathAnalysis-1.3 {longest path} {
    set result [$analysis path $n4]
    list [[java::cast {java.lang.Object} $result] toString]
} {{[3, 2, 4]}}

######################################################################
####
#
test SingleSourceLongestPathAnalysis-1.4 {what if cyclic?} {
    set e10 [$dcg addEdge $n6 $n1]
    $edgeCost put $e10 [java::new Double 3]
    list [[java::cast {java.lang.Object} $result] toString]
} {{[3, 2, 4]}}
