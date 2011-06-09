# Tests for the NCCompositeActor class
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
test NCCompositeActor-1.1 {test creating an NCCompositeActor} {
    set ncCompositeActor0 [java::new ptolemy.domains.ptinyos.kernel.NCCompositeActor]

    set ws [java::new ptolemy.kernel.util.Workspace]
    set ncCompositeActor1 [java::new ptolemy.domains.ptinyos.kernel.NCCompositeActor $ws]

    set e2 [java::new ptolemy.actor.CompositeActor]
    set ncCompositeActor2 [java::new ptolemy.domains.ptinyos.kernel.NCCompositeActor $e2 ncCompositeActor2]

    list \
	[$ncCompositeActor0 getFullName] \
	[$ncCompositeActor1 getFullName] \
	[$ncCompositeActor2 getFullName] "\n" \
	[$ncCompositeActor0 getClassName] \
	[$ncCompositeActor1 getClassName] \
	[$ncCompositeActor2 getClassName] "\n" \
	[$ncCompositeActor0 getDirector] \
	[$ncCompositeActor1 getDirector] \
        [[[$ncCompositeActor2 getDirector] getClass] getName]
} {. . ..ncCompositeActor2 {
} ptolemy.domains.ptinyos.kernel.NCCompositeActor ptolemy.domains.ptinyos.kernel.NCCompositeActor ptolemy.domains.ptinyos.kernel.NCCompositeActor {
} java0x0 java0x0 ptolemy.domains.ptinyos.kernel.PtinyOSDirector}









