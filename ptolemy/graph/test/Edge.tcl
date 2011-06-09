# Tests for the Edge class
#
# @Author: Shuvra S. Bhattacharyya 
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
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
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
test Edge-2.1 {Create an unweighted edge} {
    set nw1 [java::new {java.lang.String String} node1]
    set nw2 [java::new {java.lang.String String} node2]
    set n1 [java::new ptolemy.graph.Node $nw1]
    set n2 [java::new ptolemy.graph.Node $nw2]
    set e1 [java::new ptolemy.graph.Edge $n1 $n2]
    set hasWeight [$e1 hasWeight]
    set representation [$e1 toString]
    set source [[$e1 source] toString]
    set sink [[$e1 sink] toString]
    set selfLoop [$e1 isSelfLoop]
    list $hasWeight $representation $source $sink $selfLoop 
} {0 {(node1, node2)} node1 node2 0}

######################################################################
####
# 
test Edge-2.2 {Create a weighted edge} {
    set ew1 [java::new {java.lang.String String} edge1]
    set e2 [java::new ptolemy.graph.Edge $n2 $n1 $ew1]
    set hasWeight [$e2 hasWeight]
    set representation [$e2 toString]
    set source [[$e2 source] toString]
    set sink [[$e2 sink] toString]
    set selfLoop [$e2 isSelfLoop]
    list $hasWeight $representation $source $sink $selfLoop 
} {1 {(node2, node1, edge1)} node2 node1 0}

######################################################################
####
# 
test Edge-2.3 {Create a self-loop edge} {
    set ew2 [java::new {java.lang.String String} edge2]
    set e2 [java::new ptolemy.graph.Edge $n2 $n2 $ew2]
    set representation [$e2 toString]
    set selfLoop [$e2 isSelfLoop]
    list $representation $selfLoop 
} {{(node2, node2, edge2)} 1}

######################################################################
####
# 
test Edge-2.4 {Attempt to access the weight of an unweighted edge} {
    catch {$e1 getWeight} msg
    list $msg
} {{java.lang.IllegalStateException: Attempt to access the weight of the following unweighted edge: (node1, node2)
}}

######################################################################
####
# 
test Edge-2.5 {Attempt to create a weighted edge with a null weight} {

    catch {set newEdge [java::new ptolemy.graph.Edge $n1 $n2 [java::null]]} msg
    list $msg
} {{java.lang.IllegalArgumentException: Attempt to assign a null weight to the following edge: (node1, node2)
}}
