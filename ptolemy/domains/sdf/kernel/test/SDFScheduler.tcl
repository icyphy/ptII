# Tests for the SDFScheduler class
#
# @Author: Christopher Hylands
#
# @Version: : $Id$
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
test SDFScheduler-2.1 {Constructor tests} {
    set d1 [java::new ptolemy.domains.sdf.kernel.SDFDirector D1]
    set s1 [java::new ptolemy.domains.sdf.kernel.SDFScheduler]
    $s1 setName S1
    $d1 setScheduler s1
    set w [java::new ptolemy.kernel.util.Workspace W]
    set d3 [java::new ptolemy.domains.sdf.kernel.SDFDirector $w D3]
    set s3 [java::new ptolemy.domains.sdf.kernel.SDFScheduler $w]
    $s3 setName S3
    $d3 setScheduler s3
   list [$s1 getFullName] [$s3 getFullName]
} {}

######################################################################
####
#
test SDFScheduler-3.1 {Test clone} {
    # NOTE: Uses the setup above
    set s4 [java::cast ptolemy.domains.sdf.kernel.SDFScheduler [$s3 clone]]
    $s4 setName S4
    enumToFullNames [$w directory]
} {}

######################################################################
####
#
test SDFScheduler-4.1 {Test _makeDirectorOf} {
    # NOTE: Uses the setup above
    set e0 [java::new ptolemy.actor.TypedCompositeActor $w]
    $e0 setName E0
    $e0 setManager $manager
    $e0 setDirector $d3
    list [$d3 getFullName] [$d4 getFullName] [enumToFullNames [$w directory]]
} {W.E0.D3 W.D4 W.E0}

######################################################################
####
#
test SDFScheduler-5.1 {Test action methods} {
    # NOTE: Uses the setup above
    set a1 [java::new ptolemy.domains.sdf.lib.SDFRamp $e0 Ramp]
    set a2 [java::new ptolemy.domains.sdf.lib.SDFConsumer $e0 Consumer]
    $e0 connect [java::field $a1 output] [java::field $a2 input] R1
    set iter [$d3 getAttribute Iterations]
    $iter setToken [java::new {ptolemy.data.IntToken int} 6]
    $manager run
    list [$a2 getHistory]
} {{ptolemy.data.IntToken(0)
ptolemy.data.IntToken(1)
ptolemy.data.IntToken(2)
ptolemy.data.IntToken(3)
ptolemy.data.IntToken(4)
ptolemy.data.IntToken(5)
}}

test SDFScheduler-5.2 {Test action methods} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set d3 [java::new ptolemy.domains.sdf.kernel.SDFDirector $w D3]
    set e0 [java::new ptolemy.actor.TypedCompositeActor $w]
    $e0 setName E0
    $e0 setManager $manager
    $e0 setDirector $d3
    set a1 [java::new ptolemy.domains.sdf.lib.SDFRamp $e0 Ramp]
    set a2 [java::new ptolemy.domains.sdf.lib.SDFDelay $e0 Delay]
    set a3 [java::new ptolemy.domains.sdf.lib.SDFConsumer $e0 Consumer]
    $e0 connect [java::field $a1 output] [java::field $a2 input] R1
    $e0 connect [java::field $a2 output] [java::field $a3 input] R2
    set iter [$d3 getAttribute Iterations]
    $iter setToken [java::new {ptolemy.data.IntToken int} 6]
    $manager run
    list [$a3 getHistory]
} {{ptolemy.data.IntToken(0)
ptolemy.data.IntToken(1)
ptolemy.data.IntToken(2)
ptolemy.data.IntToken(3)
ptolemy.data.IntToken(4)
ptolemy.data.IntToken(5)
}}

test SDFScheduler-5.3 {Test action methods} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set d3 [java::new ptolemy.domains.sdf.kernel.SDFDirector $w D3]
    set e0 [java::new ptolemy.actor.TypedCompositeActor $w]
    $e0 setName E0
    $e0 setManager $manager
    $e0 setDirector $d3
    set a1 [java::new ptolemy.domains.sdf.lib.SDFRamp $e0 Ramp]
    set a2 [java::new ptolemy.domains.sdf.lib.SDFSplit $e0 Dist]
    set a3 [java::new ptolemy.domains.sdf.lib.SDFConsumer $e0 Consumer1]
    set a4 [java::new ptolemy.domains.sdf.lib.SDFConsumer $e0 Consumer2]
    $e0 connect [java::field $a1 output] [java::field $a2 input] R1
    $e0 connect [java::field $a2 output1] [java::field $a3 input] R2
    $e0 connect [java::field $a2 output2] [java::field $a4 input] R3
    set iter [$d3 getAttribute Iterations]
    $iter setToken [java::new {ptolemy.data.IntToken int} 6]
    $manager run
    list [$a3 getHistory] [$a4 getHistory]
} {{ptolemy.data.IntToken(0)
ptolemy.data.IntToken(2)
ptolemy.data.IntToken(4)
ptolemy.data.IntToken(6)
ptolemy.data.IntToken(8)
ptolemy.data.IntToken(10)
} {ptolemy.data.IntToken(1)
ptolemy.data.IntToken(3)
ptolemy.data.IntToken(5)
ptolemy.data.IntToken(7)
ptolemy.data.IntToken(9)
ptolemy.data.IntToken(11)
}}

test SDFScheduler-5.4 {Test action methods} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set d3 [java::new ptolemy.domains.sdf.kernel.SDFDirector $w D3]
    set e0 [java::new ptolemy.actor.TypedCompositeActor $w]
    $e0 setName E0
    $e0 setManager $manager
    $e0 setDirector $d3
    set a1 [java::new ptolemy.domains.sdf.lib.SDFRamp $e0 Ramp]
    set a2 [java::new ptolemy.domains.sdf.lib.SDFSplit $e0 Dist]
    set a3 [java::new ptolemy.domains.sdf.lib.SDFJoin $e0 Comm]
    set a4 [java::new ptolemy.domains.sdf.lib.SDFConsumer $e0 Consumer1]
    $e0 connect [java::field $a1 output] [java::field $a2 input] R1
    $e0 connect [java::field $a2 output1] [java::field $a3 input1] R2a
    $e0 connect [java::field $a2 output2] [java::field $a3 input2] R2d
    $e0 connect [java::field $a3 output] [java::field $a4 input] R3
    set iter [$d3 getAttribute Iterations]
    $iter setToken [java::new {ptolemy.data.IntToken int} 6]
    $manager run
    list [$a4 getHistory]
} {{ptolemy.data.IntToken(0)
ptolemy.data.IntToken(1)
ptolemy.data.IntToken(2)
ptolemy.data.IntToken(3)
ptolemy.data.IntToken(4)
ptolemy.data.IntToken(5)
ptolemy.data.IntToken(6)
ptolemy.data.IntToken(7)
ptolemy.data.IntToken(8)
ptolemy.data.IntToken(9)
ptolemy.data.IntToken(10)
ptolemy.data.IntToken(11)
}}

######################################################################
####
#
test SDFScheduler-6.1 {Test wormhole activation} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set d3 [java::new ptolemy.domains.sdf.kernel.SDFDirector $w D3]
    set e0 [java::new ptolemy.actor.TypedCompositeActor $w]
    $e0 setName E0
    $e0 setManager $manager
    $e0 setDirector $d3
    set a1 [java::new ptolemy.domains.sdf.lib.SDFRamp $e0 Ramp]
    set c1 [java::new ptolemy.actor.TypedCompositeActor $e0 Cont]
    set p1 [java::new ptolemy.domains.sdf.kernel.SDFIOPort $c1 p1]
    $p1 setInput 1
    set p2 [java::new ptolemy.domains.sdf.kernel.SDFIOPort $c1 p2]
    $p2 setOutput 1
    set d5 [java::new ptolemy.domains.sdf.kernel.SDFDirector $w d5]
    $c1 setDirector $d5
    set a2 [java::new ptolemy.domains.sdf.lib.SDFDelay $c1 Delay]
    set a3 [java::new ptolemy.domains.sdf.lib.SDFConsumer $e0 Consumer]
    $e0 connect [java::field $a1 output] $p1 R1
    $c1 connect $p1 [java::field $a2 input] R2
    $c1 connect [java::field $a2 output] $p2 R3
    $e0 connect $p2 [java::field $a3 input] R4

    set iter [$d3 getAttribute Iterations]
    $iter setToken [java::new {ptolemy.data.IntToken int} 6]
    $manager run
    list [$a3 getHistory] 
} {{ptolemy.data.IntToken(0)
ptolemy.data.IntToken(1)
ptolemy.data.IntToken(2)
ptolemy.data.IntToken(3)
ptolemy.data.IntToken(4)
ptolemy.data.IntToken(5)
}}

######################################################################
####
#
test SDFScheduler-7.1 {Test mutations (adding an actor} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set d3 [java::new ptolemy.domains.sdf.kernel.SDFDirector $w D3]
    set e0 [java::new ptolemy.actor.TypedCompositeActor $w]
    $e0 setName E0
    $e0 setManager $manager
    $e0 setDirector $d3
    set a1 [java::new ptolemy.domains.sdf.lib.SDFRamp $e0 Ramp]
    set a2 [java::new ptolemy.domains.sdf.lib.SDFDelay $e0 Delay]
    set a3 [java::new ptolemy.domains.sdf.lib.SDFConsumer $e0 Consumer]
    $e0 connect [java::field $a1 output] [java::field $a2 input] R1
    $e0 connect [java::field $a2 output] [java::field $a3 input] R2
    set iter [$d3 getAttribute Iterations]
    $iter setToken [java::new {ptolemy.data.IntToken int} 6]
} {
} {REWRITE}

