# Tests for the PtinyOSDirector class
#
# @Author: Christopher Brooks, based on PTINYOSDirector.tcl by Stephen Neuendorffer
#
# @Version: $Id$
#
# @Copyright (c) 1999-2005 The Regents of the University of California.
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
test PtinyOSDirector-2.1 {Constructor tests} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set d1 [java::new ptolemy.domains.ptinyos.kernel.PtinyOSDirector]
    $d1 setName D1
    set d2 [java::new ptolemy.domains.ptinyos.kernel.PtinyOSDirector $w]
    $d2 setName D2
    set e0 [java::new ptolemy.actor.TypedCompositeActor $w]
    $e0 setName E0
    set d3 [java::new ptolemy.domains.ptinyos.kernel.PtinyOSDirector $e0 D3]
    list [$d1 getFullName] [$d2 getFullName] [$d3 getFullName]
} {.D1 .D2 .E0.D3}

######################################################################
####
#
test PtinyOSDirector-3.1 {Test clone} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set d4 [java::cast ptolemy.domains.ptinyos.kernel.PtinyOSDirector [$d2 clone $w]]
    $d4 setName D4
    enumToFullNames [$w directory]
} {.Manager}

######################################################################
####
#
test PtinyOSDirector-4.1 {Test _makeDirectorOf} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set e0 [java::new ptolemy.actor.TypedCompositeActor $w]
    $e0 setName E0
    set d3 [java::new ptolemy.domains.ptinyos.kernel.PtinyOSDirector $e0 D3]
    set e1 [java::new ptolemy.actor.TypedCompositeActor $w]
    $e1 setName E1
    $e1 setManager $manager
    $e1 setDirector $d3
    list [$d3 getFullName] [$d4 getFullName] [enumToFullNames [$w directory]]
} {.E1.D3 .D4 {.E0 .E1}}



