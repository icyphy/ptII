# Tests the MirrorTransformation of a graph.
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
test MirrorTransformation-1.1 {mirror() with same weights} {
    set g   [java::new ptolemy.graph.Graph]
    set nw1 [java::new ptolemy.actor.AtomicActor]
    set nw2 [java::new ptolemy.actor.AtomicActor]
    set nw3 [java::new ptolemy.actor.AtomicActor]
    set n1  [java::new ptolemy.graph.Node $nw1]
    set n2  [java::new ptolemy.graph.Node $nw2]
    set n3  [java::new ptolemy.graph.Node $nw3]
    set ew1 [java::new ptolemy.actor.AtomicActor]
    set ew2 [java::new ptolemy.actor.AtomicActor]
    set e1  [java::new ptolemy.graph.Edge $n1 $n2 $ew1]
    set e2  [java::new ptolemy.graph.Edge $n1 $n3 $ew2]
    $g addNode $n1
    $g addNode $n2
    $g addNode $n3
    $g addEdge $e1
    $g addEdge $e2
    set analysis [java::new ptolemy.graph.analysis.MirrorTransformation $g]
    set mg [$analysis mirror $g 0]
    set tn1  [$mg containsNode $n1]
    set tn2  [$mg containsNode $n2]
    set tn3  [$mg containsNode $n3]
    set tnw1 [$mg containsNodeWeight $nw1]
    set tnw2 [$mg containsNodeWeight $nw2]
    set tnw3 [$mg containsNodeWeight $nw3]
    set te1  [$mg containsEdge $e1]
    set te2  [$mg containsEdge $e2]
    set tew1 [$mg containsEdgeWeight $ew1]
    set tew2 [$mg containsEdgeWeight $ew2]
    list $tn1 $tn2 $tn3 $tnw1 $tnw2 $tnw3 $te1 $te2 $tew1 $tew2
} {0 0 0 1 1 1 0 0 1 1}

######################################################################
####
#
test MirrorTransformation-1.2 {mirror() with cloned weights} {
    set mg [$analysis mirror $g 1]
    set tn1  [$mg containsNode $n1]
    set tn2  [$mg containsNode $n2]
    set tn3  [$mg containsNode $n3]
    set tnw1 [$mg containsNodeWeight $nw1]
    set tnw2 [$mg containsNodeWeight $nw2]
    set tnw3 [$mg containsNodeWeight $nw3]
    set te1  [$mg containsEdge $e1]
    set te2  [$mg containsEdge $e2]
    set tew1 [$mg containsEdgeWeight $ew1]
    set tew2 [$mg containsEdgeWeight $ew2]
    list $tn1 $tn2 $tn3 $tnw1 $tnw2 $tnw3 $te1 $te2 $tew1 $tew2
} {0 0 0 0 0 0 0 0 0 0}

######################################################################
####
#
test MirrorTransformation-1.3 {verify the cloned weights} {
    set vnw1      [$mg nodeWeight 0]
    set vnw1cls   [$vnw1 getClass]
    set vnw1clsnm [$vnw1cls toString]
    set vew1      [$mg edgeWeight 0]
    set vew1cls   [$vew1 getClass]
    set vew1clsnm [$vew1cls toString]
    list $vnw1clsnm $vew1clsnm
} {{class ptolemy.actor.AtomicActor} {class ptolemy.actor.AtomicActor}}


######################################################################
####
#
test MirrorTransformation-1.4 {relating mirror and original nodes, edges by labels} {
    set nodes [$mg nodes]
    set mgNodes  [$nodes iterator]
    set mn1    [java::cast {ptolemy.graph.Node} [$mgNodes next]]
    set mn2    [java::cast {ptolemy.graph.Node} [$mgNodes next]]
    set mn3    [java::cast {ptolemy.graph.Node} [$mgNodes next]]
    set nodes  [$g nodes]
    set gNodes  [$nodes iterator]
    set n1    [java::cast {ptolemy.graph.Node} [$gNodes next]]
    set n2    [java::cast {ptolemy.graph.Node} [$gNodes next]]
    set n3    [java::cast {ptolemy.graph.Node} [$gNodes next]]
    set edges [$mg edges]
    set mgEdges  [$edges iterator]
    set me1    [java::cast {ptolemy.graph.Edge} [$mgEdges next]]
    set me2    [java::cast {ptolemy.graph.Edge} [$mgEdges next]]
    set edges  [$g edges]
    set gEdges  [$edges iterator]
    set e1    [java::cast {ptolemy.graph.Edge} [$gEdges next]]
    set e2    [java::cast {ptolemy.graph.Edge} [$gEdges next]]
    list [$g nodeLabel $n1] [$g nodeLabel $n2] [$g nodeLabel $n3]\
    [$mg nodeLabel $mn1] [$mg nodeLabel $mn2] [$mg nodeLabel $mn3]\
    [$g edgeLabel $e1] [$g edgeLabel $e2]\
    [$mg edgeLabel $me1] [$mg edgeLabel $me2]
} {0 1 2 0 1 2 0 1 0 1}

######################################################################
####
#
test MirrorTransformation-1.5 {relating mirror and original nodes, edges
by VersionOf methods} {
    set tn1 [$analysis transformedVersionOf $n1]
    set omn1 [$analysis originalVersionOf $mn1]
    set te1 [$analysis transformedVersionOf $e1]
    set ome1 [$analysis originalVersionOf $me1]
    list [$tn1 equals $mn1] [$omn1 equals $n1] [$te1 equals $me1]\
    [$ome1 equals $e1]
} {1 1 1 1}

######################################################################
####
#
test MirrorTransformation-1.6 {mirrorAs() from Graph to DirectedGraph} {
    set mgcls    [$mg getClass]
    set mgclsstr [$mgcls toString]
    list $mgclsstr
} {{class ptolemy.graph.Graph}}

######################################################################
####
#
test MirrorTransformation-1.7 {mirrorAs() from Graph to DirectedGraph} {
    set dg   [java::new ptolemy.graph.DirectedGraph]
    set mg [$analysis mirror $dg 0]
    set mgcls    [$mg getClass]
    set mgclsstr [$mgcls toString]
    list $mgclsstr
} {{class ptolemy.graph.DirectedGraph}}
