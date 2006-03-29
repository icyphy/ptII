# Tests for the FPDirector class
#
# @Author: Christopher Hylands (based on Director.tcl by Edward A. Lee)
#
# @Version: $Id$
#
# @Copyright (c) 1997-2006 The Regents of the University of California.
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

set w [java::new ptolemy.kernel.util.Workspace W]
set manager [java::new ptolemy.actor.Manager $w M]

######################################################################
####
#
test FPDirector-2.1 {Constructor tests} {
    set d1 [java::new ptolemy.domains.fp.kernel.FPDirector]
    $d1 setName D1
    set d2 [java::new ptolemy.domains.fp.kernel.FPDirector $w]
    $d2 setName D2
    set e0 [java::new ptolemy.actor.CompositeActor $w]
    $e0 setName E0
    set d3 [java::new ptolemy.domains.fp.kernel.FPDirector $e0 D3]

    # These methods could be abstract, but are not for testing purposes
    # so we call them here
    $d1 fireAtCurrentTime $e0

    list [$d1 getFullName] [$d2 getFullName] [$d3 getFullName]
} {.D1 .D2 .E0.D3}

######################################################################
####
#
test FPDirector-3.1 {Test clone} {
    # NOTE: Uses the setup above
    set d4 [java::cast ptolemy.domains.fp.kernel.FPDirector [$d2 clone $w]]
    $d4 setName D4
    enumToFullNames [$w directory]
} {.M .D2 .E0}

######################################################################
####
#
test FPDirector-4.1 {Test _makeDirectorOf} {
    # NOTE: Uses the setup above
    $e0 setManager $manager
    list [$d3 getFullName] [$d4 getFullName] [enumToFullNames [$w directory]]
} {.E0.D3 .D4 {.D2 .E0}}
