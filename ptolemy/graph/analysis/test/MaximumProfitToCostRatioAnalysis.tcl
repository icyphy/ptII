# Tests the MaximumProfitToCostRatioAnalysis.
#
# @Author: Shahrooz Shahparnia
#
# $Id$
#
# @Copyright (c) 2002-2005 The Regents of the University of Maryland.
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
test MaximumProfitToCostRatioAnalysis-1.1 {a simple graph} {
    set dcg [java::new ptolemy.graph.DirectedGraph]
    set h [$dcg addNodeWeight "h"]
    set i [$dcg addNodeWeight "i"]
    set j [$dcg addNodeWeight "j"]
    set k [$dcg addNodeWeight "k"]
    set l [$dcg addNodeWeight "l"]
    set m [$dcg addNodeWeight "m"]
    set e1 [$dcg addEdge $h $i]
    set e2 [$dcg addEdge $i $l]
    set e3 [$dcg addEdge $h $j]
    set e4 [$dcg addEdge $j $h]
    set e5 [$dcg addEdge $j $l]
    set e6 [$dcg addEdge $j $m]
    set e7 [$dcg addEdge $l $m]
    set e8 [$dcg addEdge $m $k]
    set e9 [$dcg addEdge $k $h]

    set edgeCost [java::new java.util.HashMap]
    set edgeProfit [java::new java.util.HashMap]

    $edgeProfit put $e1 [java::new Double 1]
    $edgeProfit put $e2 [java::new Double 4]
    $edgeProfit put $e3 [java::new Double 1]
    $edgeProfit put $e4 [java::new Double 2]
    $edgeProfit put $e5 [java::new Double 2]
    $edgeProfit put $e6 [java::new Double 2]
    $edgeProfit put $e7 [java::new Double 1]
    $edgeProfit put $e8 [java::new Double 1]
    $edgeProfit put $e9 [java::new Double 2]

    $edgeCost put $e1 [java::new Integer 0]
    $edgeCost put $e2 [java::new Integer 2]
    $edgeCost put $e3 [java::new Integer 1]
    $edgeCost put $e4 [java::new Integer 0]
    $edgeCost put $e5 [java::new Integer 0]
    $edgeCost put $e6 [java::new Integer 0]
    $edgeCost put $e7 [java::new Integer 0]
    $edgeCost put $e8 [java::new Integer 0]
    $edgeCost put $e9 [java::new Integer 1]

    set profitMapping [java::new ptolemy.graph.mapping.ToDoubleMapMapping $edgeProfit]
    set costMapping [java::new ptolemy.graph.mapping.ToIntMapMapping $edgeCost]
    set analysis [java::new ptolemy.graph.analysis.MaximumProfitToCostRatioAnalysis $dcg\
            $profitMapping $costMapping]
    set result [$analysis maximumRatio]
    list $result
} {3.5}

######################################################################
####

test MaximumProfitToCostRatioAnalysis-1.2 {the cycle} {
    set cycle [$analysis cycle]
    list [[java::cast {java.lang.Object} $cycle] toString]
} {{[k, m, l, j, h]}}

