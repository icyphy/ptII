# Tests for the Transition class
#
# @Author: Xiaojun Liu
#
# @Version: $Id$
#
# @Copyright (c) 2000 The Regents of the University of California.
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test Transition-4.1 {test scope of guard and trigger expressions} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set dir [java::new ptolemy.actor.Director $e0 dir]
    set e1 [java::new ptolemy.domains.fsm.kernel.FSMActor $e0 fsm]
    set s0 [java::new ptolemy.domains.fsm.kernel.State $e1 s0]
    set s1 [java::new ptolemy.domains.fsm.kernel.State $e1 s1]
    set t0 [java::new ptolemy.domains.fsm.kernel.Transition $e1 t0]
    [java::field $s0 outgoingPort] link $t0
    [java::field $s1 incomingPort] link $t0
    set tok [java::new ptolemy.data.StringToken s0]
    [java::field $e1 initialStateName] setToken $tok
    set p0 [java::new ptolemy.actor.TypedIOPort $e1 p0]
    $p0 setInput true
    set p1 [java::new ptolemy.actor.TypedIOPort $e1 p1]
    $p1 setInput true
    $p1 setMultiport true
    set e2 [java::new ptolemy.actor.TypedAtomicActor $e0 e2]
    set p2 [java::new ptolemy.actor.TypedIOPort $e2 p2]
    $p2 setMultiport true
    set r0 [java::new ptolemy.actor.TypedIORelation $e0 r0]
    set r1 [java::new ptolemy.actor.TypedIORelation $e0 r1]
    $r1 setWidth 2
    $p0 link $r0
    $p1 link $r1
    $p2 link $r0
    $p2 link $r1
    $dir preinitialize
    set guard [java::field $t0 guard]
    set trigger [java::field $t0 trigger]
    set scope1 [[$guard getScope] elementList]
    set scope2 [[$trigger getScope] elementList]
    list [listToNames $scope1] [listToNames $scope2]
} {{trigger initialStateName p0_S p0_V p1_0_S p1_0_V p1_1_S p1_1_V} {guard initialStateName p0_S p0_V p1_0_S p1_0_V p1_1_S p1_1_V}}
