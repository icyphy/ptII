# Tests for the PtinyOSActor class
#
# @Author: Christopher Brooks
#
# @Version: : CSPActor.tcl,v 1.33 1998/12/05 05:37:32 cxh Exp $
#
# @Copyright (c) 2005 The Regents of the University of California.
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

# Ptolemy II bed, see /users/cxh/ptII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs enumToObjects] == "" } then {
     source enums.tcl
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test PtinyOSActor-1.1 {test creating an PtinyOSActor} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set ptinyos [java::new ptolemy.domains.ptinyos.kernel.test.PtinyOSTestActor $e0 ptinyos]
    set ptinyos1 [java::new ptolemy.domains.ptinyos.kernel.test.PtinyOSTestActor]
    set ws [java::new ptolemy.kernel.util.Workspace]
    set ptinyos2 [java::new ptolemy.domains.ptinyos.kernel.test.PtinyOSTestActor $ws]
    list [$ptinyos getFullName] [$ptinyos1 getFullName] \
            [$ptinyos2 getFullName]
} {..ptinyos . .}

test PtinyOSActor-1.2 {container must be TypedCompositeActor or null} {
    $ptinyos setContainer [java::null]
    set re0 [$ptinyos getFullName]
    set e1 [java::new ptolemy.actor.CompositeActor]
    $e1 setName testContainer
    $ptinyos setContainer $e1
    list $re0 [[$ptinyos getContainer] getFullName]
} {.ptinyos .testContainer}

######################################################################
####
#
test PtinyOSActor-2.1 {test getDirector} {
    # PtinyOSActor always has a PtinyOSDirector embedded in it
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set dir [java::new ptolemy.actor.Director $e0 dir]
    set ptinyOSActor [java::new ptolemy.domains.ptinyos.kernel.test.PtinyOSTestActor $e0 ptinyos]
    set re0 [expr {[$ptinyOSActor getDirector] == $dir}]
    set re1 [expr {[$ptinyOSActor getExecutiveDirector] == $dir}]
    $ptinyos setContainer [java::null]
    set re2 [expr {[$ptinyOSActor getDirector] == [java::null]}]
    set re3 [[[$ptinyOSActor getDirector] getClass] getName]
    list $re0 $re1 $re2 $re3
} {0 1 0 ptolemy.domains.ptinyos.kernel.PtinyOSDirector}

test PtinyOSActor-2.2 {test getManager} {
    set mag [java::new ptolemy.actor.Manager]
    $e0 setManager $mag
    set re1 [expr {[$ptinyOSActor getManager] == [java::null]}]
    $ptinyOSActor setContainer $e0
    set re2 [expr {[$ptinyOSActor getManager] == $mag}]
    list $re1 $re2
} {0 1}

######################################################################
####
#
test PtinyOSActor-3.1 {test listing input and output ports} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set ptinyOSActor [java::new ptolemy.domains.ptinyos.kernel.test.PtinyOSTestActor $e0 ptinyos]
    set p0 [java::new ptolemy.actor.TypedIOPort $ptinyOSActor p0]
    set p1 [java::new ptolemy.actor.TypedIOPort $ptinyOSActor p1 true true]
    set p2 [java::new ptolemy.actor.TypedIOPort $ptinyOSActor p2 true false]
    set p3 [java::new ptolemy.actor.TypedIOPort $ptinyOSActor p3 false true]
    list [listToFullNames [$ptinyOSActor inputPortList]] \
            [listToFullNames [$ptinyOSActor outputPortList]]
} {{..ptinyos.packetIn ..ptinyos.p1 ..ptinyos.p2} {..ptinyos.packetOut ..ptinyos.p1 ..ptinyos.p3}}

test PtinyOSActor-3.2 {test newPort} {
    set p4 [$ptinyOSActor newPort p4]
    list [java::instanceof $p4 ptolemy.actor.TypedIOPort] \
            [listToFullNames [$ptinyOSActor portList]]
} {1 {..ptinyos.packetOut ..ptinyos.packetIn ..ptinyos.p0 ..ptinyos.p1 ..ptinyos.p2 ..ptinyos.p3 ..ptinyos.p4}}

test PtinyOSActor-3.3 {test newReceiver} {
    set dir [java::new ptolemy.actor.Director $e0 dir]
    set r [$ptinyOSActor newReceiver]
    set tok [java::new ptolemy.data.StringToken foo]
    $r put $tok
    set received [java::cast ptolemy.data.StringToken [$r get]]
    $received stringValue
} {foo}









