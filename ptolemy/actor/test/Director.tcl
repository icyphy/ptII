# Tests for the Director class
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997- The Regents of the University of California.
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


######################################################################
####
#
test Director-2.1 {Constructor tests} {
    set d1 [java::new pt.actor.Director]
    $d1 setName D1
    set d2 [java::new pt.actor.Director D2]
    set w [java::new pt.kernel.util.Workspace W]
    set d3 [java::new pt.actor.Director $w D3]
    list [$d1 getFullName] [$d2 getFullName] [$d3 getFullName]
} {.D1 .D2 W.D3}

######################################################################
####
#
test Director-3.1 {Test clone} {
    # NOTE: Uses the setup above
    set d4 [$d2 clone $w]
    $d4 setName D4
    enumToFullNames [$w directory]
} {W.D3}

######################################################################
####
#
test Director-4.1 {Test _makeDirectorOf and _makeExecDirectorOf} {
    # NOTE: Uses the setup above
    set e0 [java::new pt.actor.CompositeActor $w]
    $e0 setName E0
    $e0 setDirector $d3
    $e0 setExecutiveDirector $d4
    list [$d3 getFullName] [$d4 getFullName] [enumToFullNames [$w directory]]
} {W.E0.D3 W.E0.D4 W.E0}

######################################################################
####
#
test Director-5.1 {Test action methods} {
    # NOTE: Uses the setup above
    set a1 [java::new pt.actor.test.TestActor $e0 A1]
    set a2 [java::new pt.actor.test.TestActor $e0 A2]
    $a1 clear
    $d4 run 3
    $a1 getRecord
} {W.E0.A1.initialize
W.E0.A2.initialize
W.E0.A1.prefire
W.E0.A2.prefire
W.E0.A1.fire
W.E0.A2.fire
W.E0.A1.postfire
W.E0.A2.postfire
W.E0.A1.prefire
W.E0.A2.prefire
W.E0.A1.fire
W.E0.A2.fire
W.E0.A1.postfire
W.E0.A2.postfire
W.E0.A1.prefire
W.E0.A2.prefire
W.E0.A1.fire
W.E0.A2.fire
W.E0.A1.postfire
W.E0.A2.postfire
W.E0.A1.wrapup
W.E0.A2.wrapup
}

######################################################################
####
#
test Director-6.1 {Test wormhole activation} {
    set e1 [java::new pt.actor.CompositeActor $e0 E1]
    set d5 [java::new pt.actor.Director $w D5]
    $e1 setDirector $d5
    $a2 setContainer $e1
    $a1 clear
    $d4 run 3
    $a1 getRecord
} {W.E0.A1.initialize
W.E0.E1.A2.initialize
W.E0.A1.prefire
W.E0.E1.A2.prefire
W.E0.A1.fire
W.E0.E1.A2.fire
W.E0.A1.postfire
W.E0.E1.A2.postfire
W.E0.A1.prefire
W.E0.E1.A2.prefire
W.E0.A1.fire
W.E0.E1.A2.fire
W.E0.A1.postfire
W.E0.E1.A2.postfire
W.E0.A1.prefire
W.E0.E1.A2.prefire
W.E0.A1.fire
W.E0.E1.A2.fire
W.E0.A1.postfire
W.E0.E1.A2.postfire
W.E0.A1.wrapup
W.E0.E1.A2.wrapup
}

######################################################################
####
#
test Director-7.1 {Test mutations (adding an actor} {
    $a1 clear
    $d4 initialize
    $d4 iterate
    $a2 addActor A3
    $d4 iterate
    $d4 wrapup
    $a1 getRecord
} {W.E0.A1.initialize
W.E0.E1.A2.initialize
W.E0.A1.prefire
W.E0.E1.A2.prefire
W.E0.A1.fire
W.E0.E1.A2.fire
W.E0.A1.postfire
W.E0.E1.A2.postfire
W.E0.A1.prefire
W.E0.E1.A3.initialize
W.E0.E1.A2.prefire
W.E0.E1.A3.prefire
W.E0.A1.fire
W.E0.E1.A2.fire
W.E0.E1.A3.fire
W.E0.A1.postfire
W.E0.E1.A2.postfire
W.E0.E1.A3.postfire
W.E0.A1.wrapup
W.E0.E1.A2.wrapup
W.E0.E1.A3.wrapup
}
