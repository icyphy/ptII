# Tests for the Vertex class
#
# @Author: Christopher Hylands, Based on Location.tcl by Steve Neuendorffer
#
# @Version: $Id$
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
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
#######################################################################

# Load the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test Vertex-1.1 {Call workspace constructor, exportMoML and toString } {
    set w0 [java::new ptolemy.kernel.util.Workspace "myWorkspace"]
    set v1 [java::new ptolemy.moml.Vertex $w0]
    set output [java::new java.io.StringWriter]
    $v1 exportMoML $output 1
    list [$v1 toString] [$output toString]
} {{(ptolemy.moml.Vertex, Location = {0.0, 0.0})} {    <vertex name="" value="{0.0, 0.0}">
    </vertex>
}}

test Vertex-2.1 {addLinkedPort, linkedPorts removeLinkedPort} {
    set w0 [java::new ptolemy.kernel.util.Workspace "myWorkspace"]
    set e0 [java::new ptolemy.kernel.Entity]
    set v1 [java::new ptolemy.moml.Vertex $w0]
    set p1 [java::new ptolemy.kernel.Port $e0 "p1"]
    set p2 [java::new ptolemy.kernel.Port $e0 "p2"]
    set p3 [java::new ptolemy.kernel.Port $e0 "p3"]

    $v1 addLinkedPort $p1
    $v1 addLinkedPort $p2
    set r1 [listToNames [$v1 linkedPorts]]

    $v1 removeLinkedPort $p1
    $v1 addLinkedPort $p3
    set r2 [listToNames [$v1 linkedPorts]]
    
    list $r1 $r2
} {{p1 p2} {p2 p3}}


test Vertex-3.1 {addLinkedPort, linkedPorts removeLinkedPort} {
    set w0 [java::new ptolemy.kernel.util.Workspace "myWorkspace"]
    set v1 [java::new ptolemy.moml.Vertex $w0]
    set v2 [java::new ptolemy.moml.Vertex $w0]

    $v1 setLinkedVertex $v1
    list [[$v1 getLinkedVertex] toString] \
	    [expr {[$v2 getLinkedVertex] == [java::null]}]
} {{(ptolemy.moml.Vertex, Location = {0.0, 0.0})} 1}
