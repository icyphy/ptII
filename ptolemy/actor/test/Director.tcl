# Tests for the Director class
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997-2000 The Regents of the University of California.
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
set w [java::new ptolemy.kernel.util.Workspace W]
set manager [java::new ptolemy.actor.Manager $w M]

######################################################################
####
#
test Director-2.1 {Constructor tests} {
    set d1 [java::new ptolemy.actor.Director]
    $d1 setName D1
    set d2 [java::new ptolemy.actor.Director $w]
    $d2 setName D2
    set e0 [java::new ptolemy.actor.CompositeActor $w]
    $e0 setName E0
    set d3 [java::new ptolemy.actor.Director $e0 D3]
    list [$d1 getFullName] [$d2 getFullName] [$d3 getFullName]
} {.D1 .D2 .E0.D3}

######################################################################
####
#
test Director-3.1 {Test clone} {
    # NOTE: Uses the setup above
    set d4 [java::cast ptolemy.actor.Director [$d2 clone $w]]
    $d4 setName D4
    enumToFullNames [$w directory]
} {.M .D2 .E0}

######################################################################
####
#
test Director-4.1 {Test _makeDirectorOf} {
    # NOTE: Uses the setup above
    $e0 setManager $manager
    list [$d3 getFullName] [$d4 getFullName] [enumToFullNames [$w directory]]
} {.E0.D3 .D4 {.D2 .E0}}

######################################################################
####
#
test Director-5.1 {Test action methods} {
    # NOTE: Uses the setup above
    set a1 [java::new ptolemy.actor.test.TestActor $e0 A1]
    set a2 [java::new ptolemy.actor.test.TestActor $e0 A2]
    $a1 clear
    set r1 [$d4 getNextIterationTime]
    $manager run
    list $r1 [$a1 getRecord] [$d4 getNextIterationTime]
} {0.0 {.E0.A1.initialize
.E0.A2.initialize
.E0.A1.prefire
.E0.A1.fire
.E0.A1.postfire
.E0.A2.prefire
.E0.A2.fire
.E0.A2.postfire
.E0.A1.wrapup
.E0.A2.wrapup
} 0.0}

######################################################################
####
#
test Director-6.1 {Test wormhole activation} {
    set e1 [java::new ptolemy.actor.CompositeActor $e0 E1]
    set d5 [java::new ptolemy.actor.Director $e1 D5]
    $a2 setContainer $e1
    $a1 clear
    $manager run
    $a1 getRecord
} {.E0.A1.initialize
.E0.E1.A2.initialize
.E0.A1.prefire
.E0.A1.fire
.E0.A1.postfire
.E0.E1.A2.prefire
.E0.E1.A2.fire
.E0.E1.A2.postfire
.E0.A1.wrapup
.E0.E1.A2.wrapup
}
