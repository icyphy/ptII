# Tests for TransitiveClosureAnalysis.
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
test TransitiveClosureAnalysis-1.1 {a directed graph example} {
    set dcg [java::new ptolemy.graph.DirectedGraph]
    set n1 [$dcg addNodeWeight "1"]
    set n2 [$dcg addNodeWeight "2"]
    set n3 [$dcg addNodeWeight "3"]
    set n4 [$dcg addNodeWeight "4"]
    set n5 [$dcg addNodeWeight "5"]
    set n6 [$dcg addNodeWeight "6"]
    set n7 [$dcg addNodeWeight "7"]
    set e1 [$dcg addEdge $n2 $n1]
    set e2 [$dcg addEdge $n3 $n2]
    set e3 [$dcg addEdge $n6 $n2]
    set e4 [$dcg addEdge $n6 $n3]
    set e5 [$dcg addEdge $n3 $n6]
    set e6 [$dcg addEdge $n4 $n1]
    set e7 [$dcg addEdge $n2 $n4]
    set e8 [$dcg addEdge $n1 $n5]
    set e9 [$dcg addEdge $n5 $n2]
    set e10 [$dcg addEdge $n4 $n4]

    set analysis [java::new ptolemy.graph.analysis.TransitiveClosureAnalysis $dcg]
    set matrix [$analysis transitiveClosureMatrix]
    set row1     [$matrix get 0]
    set row2     [$matrix get 1]
    set row3     [$matrix get 2]
    set row4     [$matrix get 3]
    set row5     [$matrix get 4]
    set row6     [$matrix get 5]
    set row7     [$matrix get 6]
    set a11      [$row1 get 0]
    set a12      [$row1 get 1]
    set a13      [$row1 get 2]
    set a14      [$row1 get 3]
    set a15      [$row1 get 4]
    set a16      [$row1 get 5]
    set a17      [$row1 get 6]
    set a21      [$row2 get 0]
    set a22      [$row2 get 1]
    set a23      [$row2 get 2]
    set a24      [$row2 get 3]
    set a25      [$row2 get 4]
    set a26      [$row2 get 5]
    set a27      [$row2 get 6]
    set a31      [$row3 get 0]
    set a32      [$row3 get 1]
    set a33      [$row3 get 2]
    set a34      [$row3 get 3]
    set a35      [$row3 get 4]
    set a36      [$row3 get 5]
    set a37      [$row3 get 6]
    set a41      [$row4 get 0]
    set a42      [$row4 get 1]
    set a43      [$row4 get 2]
    set a44      [$row4 get 3]
    set a45      [$row4 get 4]
    set a46      [$row4 get 5]
    set a47      [$row4 get 6]
    set a51      [$row5 get 0]
    set a52      [$row5 get 1]
    set a53      [$row5 get 2]
    set a54      [$row5 get 3]
    set a55      [$row5 get 4]
    set a56      [$row5 get 5]
    set a57      [$row5 get 6]
    set a61      [$row6 get 0]
    set a62      [$row6 get 1]
    set a63      [$row6 get 2]
    set a64      [$row6 get 3]
    set a65      [$row6 get 4]
    set a66      [$row6 get 5]
    set a67      [$row6 get 6]
    set a71      [$row7 get 0]
    set a72      [$row7 get 1]
    set a73      [$row7 get 2]
    set a74      [$row7 get 3]
    set a75      [$row7 get 4]
    set a76      [$row7 get 5]
    set a77      [$row7 get 6]
    list $a11 $a12 $a13 $a14 $a15 $a16 $a17\
            $a21 $a22 $a23 $a24 $a25 $a26 $a27\
            $a31 $a32 $a33 $a34 $a35 $a36 $a37\
            $a41 $a42 $a43 $a44 $a45 $a46 $a47\
            $a51 $a52 $a53 $a54 $a55 $a56 $a57\
            $a61 $a62 $a63 $a64 $a65 $a66 $a67\
            $a71 $a72 $a73 $a74 $a75 $a76 $a77
} {1 1 0 1 1 0 0 1 1 0 1 1 0 0 1 1 1 1 1 1 0 1 1 0 1 1 0 0 1 1 0 1 1\
 0 0 1 1 1 1 1 1 0 0 0 0 0 0 0 0}

######################################################################
####
#
test TransitiveClosureAnalysis-1.2 {to string} {
    list [$analysis toString]
} {{Transitive closure analysis using the following analyzer:
Transitive closure analyzer based on the Floyd-Warshall algorithm.}}
