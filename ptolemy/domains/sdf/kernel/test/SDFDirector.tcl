# Tests for the SDFDirector class
#
# @Author: Christopher Hylands
#
# @Version: : NamedObj.tcl,v 1.33 1998/12/05 05:37:32 cxh Exp $
#
# @Copyright (c) 1999 The Regents of the University of California.
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
set manager [java::new ptolemy.actor.Manager]


######################################################################
####
#
test SDFDirector-2.1 {Constructor tests} {
    set d1 [java::new ptolemy.domains.sdf.kernel.SDFDirector]
    $d1 setName D1
    set d2 [java::new ptolemy.domains.sdf.kernel.SDFDirector D2]
    set w [java::new ptolemy.kernel.util.Workspace W]
    set d3 [java::new ptolemy.domains.sdf.kernel.SDFDirector $w D3]
    list [$d1 getFullName] [$d2 getFullName] [$d3 getFullName]
} {.D1 .D2 W.D3}

######################################################################
####
#
test SDFDirector-3.1 {Test clone} {
    # NOTE: Uses the setup above
    set d4 [java::cast ptolemy.domains.sdf.kernel.SDFDirector [$d2 clone $w]]
    $d4 setName D4
    enumToFullNames [$w directory]
} {W.D3}

######################################################################
####
#
test SDFDirector-4.1 {Test _makeDirectorOf} {
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
test SDFDirector-5.1 {Test action methods} {
    # NOTE: Uses the setup above
    set a1 [java::new ptolemy.domains.sdf.lib.SDFRamp $e0 Ramp]
    set a2 [java::new ptolemy.domains.sdf.lib.SDFPrint $e0 Print]
    $e0 connect [a1 output] [a2 input] R1
    set iter [$d3 getAttribute Iterations]
    $iter setToken [java::new ptolemy.data.IntToken 6]
    $manager run
} {}

test SDFDirector-5.2 {Test action methods} {
    # NOTE: Uses the setup above
    set a1 [java::new ptolemy.domains.sdf.lib.SDFRamp $e0 Ramp]
    set a2 [java::new ptolemy.domains.sdf.lib.SDFDelay $e0 Delay]
    set a3 [java::new ptolemy.domains.sdf.lib.SDFPrint $e0 Print]
    $e0 connect [a1 output] [a2 input] R1
    $e0 connect [a2 output] [a3 input] R2
    set iter [$d3 getAttribute Iterations]
    $iter setToken [java::new ptolemy.data.IntToken 6]
    $manager run
} {}

test SDFDirector-5.3 {Test action methods} {
    # NOTE: Uses the setup above
    set a1 [java::new ptolemy.domains.sdf.lib.SDFRamp $e0 Ramp]
    set a2 [java::new ptolemy.actor.lib.Distributor $e0 Dist]
    set a3 [java::new ptolemy.domains.sdf.lib.SDFPrint $e0 Print1]
    set a4 [java::new ptolemy.domains.sdf.lib.SDFPrint $e0 Print1]
    $e0 connect [a1 output] [a2 input] R1
    $e0 connect [a2 output] [a3 input] R2
    $e0 connect [a2 output] [a4 input] R3
    set iter [$d3 getAttribute Iterations]
    $iter setToken [java::new ptolemy.data.IntToken 6]
    $manager run
} {}

test SDFDirector-5.4 {Test action methods} {
    # NOTE: Uses the setup above
    set a1 [java::new ptolemy.domains.sdf.lib.SDFRamp $e0 Ramp]
    set a2 [java::new ptolemy.actor.lib.Distributor $e0 Dist]
    set a3 [java::new ptolemy.actor.lib.Commutator $e0 Comm]
    set a4 [java::new ptolemy.domains.sdf.lib.SDFPrint $e0 Print1]
    $e0 connect [a1 output] [a2 input] R1
    $e0 connect [a2 output] [a3 input] R2a
    $e0 connect [a2 output] [a3 input] R2b
    $e0 connect [a2 output] [a3 input] R2c
    $e0 connect [a2 output] [a3 input] R2d
    $e0 connect [a3 output] [a4 input] R3
    set iter [$d3 getAttribute Iterations]
    $iter setToken [java::new ptolemy.data.IntToken 6]
    $manager run
} {}

######################################################################
####
#
test SDFDirector-6.1 {Test wormhole activation} {
    set a1 [java::new ptolemy.domains.sdf.lib.SDFRamp $e0 Ramp]
    set c1 [java::new ptolemy.domains.sdf.kernel.SDFCompositeActor $e0 Cont]
    set p1 [java::new ptolemy.domains.sdf.kernel.SDFIOPort $c1 p1]
    set p2 [java::new ptolemy.domains.sdf.kernel.SDFIOPort $c1 p2]
    set d5 [java::new ptolemy.domains.sdf.kernel.SDFDirector $c1 d5]
    $c1 setDirector $d5
    set a2 [java::new ptolemy.domains.sdf.lib.SDFDelay $c1 Delay]
    set a3 [java::new ptolemy.domains.sdf.lib.SDFPrint $e0 Print]
    $e0 connect [a1 output] $p1 R1
    $e0 connect $p1 [a2 input] R2
    $e0 connect [a2 output] $p1 R3
    $e0 connect $p2 [a3 input] R4

    set iter [$d3 getAttribute Iterations]
    $iter setToken [java::new ptolemy.data.IntToken 6]
    $manager run
} {
}

######################################################################
####
#
test SDFDirector-7.1 {Test mutations (adding an actor} {
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
} {KNOWN_FAILED}

