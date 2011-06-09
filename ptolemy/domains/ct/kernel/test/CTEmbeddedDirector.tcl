# Tests for the CTEmbeddedDirector class
#
# @Author: Haiyang Zheng
#
# @Version: $Id$
#
# @Copyright (c) 1998-2005 The Regents of the University of California.
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

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####  Test constructors.
#
test CTEmbeddedDirector-1.1 {Construct a Director and get name} {
    set d1 [java::new ptolemy.domains.ct.kernel.CTEmbeddedDirector]
    list  [$d1 getName]
} {{}}

test CTEmbeddedDirector-1.2 {Construct a Director in a workspace} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set d1 [java::new ptolemy.domains.ct.kernel.CTEmbeddedDirector $w]
    list  [$d1 getFullName]
} {.}

test CTEmbeddedDirector-1.3 {Construct with a name and a container} {
    set ca [java::new ptolemy.actor.TypedCompositeActor $w]
    $ca setName CA
    set d2 [java::new ptolemy.domains.ct.kernel.CTEmbeddedDirector $ca DIR2]
    list [$d2 getFullName]
} {.CA.DIR2}

######################################################################
####  Test the canBeTopLevelDirector() method.
#
test CTEmbeddedDirector-2.1 {CTest the canBeTopLevelDirector() method} {
	# CTEmbeddedDirector can not be top-level director
    set d1 [java::new ptolemy.domains.ct.kernel.CTEmbeddedDirector]
    list  [$d1 canBeTopLevelDirector]
} {0}
