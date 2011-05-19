# Tests CycleExistenceAnalysis.
#
# @Author: Shahrooz Shahparnia
#
# $Id$
#
# @Copyright (c) 2001-2005 The Regents of the University of Maryland.
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
test CycleExistenceAnalysis-1.1 {an acyclic graph} {
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
    set e5 [$dcg addEdge $n1 $n3]
    set analysis [java::new ptolemy.graph.analysis.CycleExistenceAnalysis $dcg]
    set result [$analysis hasCycle]
    list $result
} {0}

######################################################################
####
#
test CycleExistenceAnalysis-1.2 {a cyclic graph} {
    set e4 [$dcg addEdge $n4 $n1]
    set e6 [$dcg addEdge $n5 $n6]
    set e7 [$dcg addEdge $n6 $n7]
    set e8 [$dcg addEdge $n7 $n5]
    set result [$analysis hasCycle]
    list $result
} {1}

