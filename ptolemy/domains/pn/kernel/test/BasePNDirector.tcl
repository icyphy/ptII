# Tests for the BasePNDirector class
#
# @Author: Mudit Goel
#
# @Version: $Id$
#
# @Copyright (c) 1998 The Regents of the University of California.
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

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
#
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#
set manager [java::new ptolemy.actor.Manager]


######################################################################
####
#
test BasePNDirector-2.1 {Constructor tests} {
    set d1 [java::new ptolemy.domains.pn.kernel.BasePNDirector]
    $d1 setName D1
    set d2 [java::new ptolemy.domains.pn.kernel.BasePNDirector D2]
    set w [java::new ptolemy.kernel.util.Workspace W]
    set d3 [java::new ptolemy.domains.pn.kernel.BasePNDirector $w D3]
    list [$d1 getFullName] [$d2 getFullName] [$d3 getFullName]
} {.D1 .D2 W.D3}

######################################################################
####
#
##FIXME: Check this for correctness. Should probably include D4
test BasePNDirector-3.1 {Test clone} {
    # NOTE: Uses the setup above
    set d4 [java::cast ptolemy.domains.pn.kernel.BasePNDirector [$d2 clone $w]]
    $d4 setName D4
    enumToFullNames [$w directory]
} {W.D3}


######################################################################
####
#
test BasePNDirector-4.1 {Test _makeDirectorOf} {
    # NOTE: Uses the setup above
    set e0 [java::new ptolemy.actor.CompositeActor $w]
    $e0 setName E0
    $e0 setManager $manager
    $e0 setDirector $d3
    list [$d3 getFullName] [$d4 getFullName] [enumToFullNames [$w directory]]
} {W.E0.D3 W.D4 W.E0}

######################################################################
####
#
test BasePNDirector-5.1 {Test action methods} {
    # NOTE: Uses the setup above
    set a1 [java::new ptolemy.actor.process.test.TestProcessActor $e0 A1]
    set a2 [java::new ptolemy.actor.process.test.TestProcessActor $e0 A2]
    $a1 clear
    $manager run
    lsort [$a1 getRecord]
    
} {W.E0.A1.fire W.E0.A1.initialize W.E0.A1.postfire W.E0.A1.prefire W.E0.A1.wrapup W.E0.A2.fire W.E0.A2.initialize W.E0.A2.postfire W.E0.A2.prefire W.E0.A2.wrapup}

test BasePNDirector-5.2 (Test creation of a receiver) {
    set r1 [$d3 newReceiver]
    #FIXME: Check if this is correct!
    set p1 [$d4 [getAttribute Initial_queue_capacity]]
    $p1 setToken [new IntToken 5]
    set r2 [$d4 newReceiver]
    list [$r1 getCapacity] [$r2 getCapacity]
} {1 5}

#############################################





