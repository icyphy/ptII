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
    set d1 [java::new ptolemy.actor.Director]
    $d1 setName D1
    set d2 [java::new ptolemy.actor.Director D2]
    set w [java::new ptolemy.kernel.util.Workspace W]
    set d3 [java::new ptolemy.actor.Director $w D3]
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
    set e0 [java::new ptolemy.actor.CompositeActor $w]
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
    set a1 [java::new ptolemy.actor.test.TestActor $e0 A1]
    set a2 [java::new ptolemy.actor.test.TestActor $e0 A2]
    $a1 clear
    $d4 go 3
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
    set e1 [java::new ptolemy.actor.CompositeActor $e0 E1]
    set d5 [java::new ptolemy.actor.Director $w D5]
    $e1 setDirector $d5
    $a2 setContainer $e1
    $a1 clear
    $d4 go 3
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

######################################################################
####
#
test Director-8.1 {Test type checking} {
    set director [java::new ptolemy.actor.Director]
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setExecutiveDirector $director
    $e0 setName E0

    #create e1
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.TypedIOPort $e1 P1]
    $p1 makeOutput true
    set t1 [java::new ptolemy.data.IntToken]
    $p1 setDeclaredType $t1

    #create e2
    set e2 [java::new ptolemy.actor.TypedAtomicActor $e0 E2]
    set p2 [java::new ptolemy.actor.TypedIOPort $e2 P2]
    $p2 makeInput true
    set t2 [java::new ptolemy.data.DoubleToken]
    $p2 setDeclaredType $t2

    #link up p1, p2
    set r1 [java::new ptolemy.actor.IORelation $e0 R1]
    $p1 link $r1
    $p2 link $r1

    $director resolveTypes
    set rt1 [[[$p1 resolvedType] getClass] getName]
    set rt2 [[[$p2 resolvedType] getClass] getName]
    list $rt1 $rt2
} {ptolemy.data.IntToken ptolemy.data.DoubleToken}

######################################################################
####
#
test Director-8.2 {Test type resolution} {
    # use the setup above
    $p1 setDeclaredType [java::null]

    $director resolveTypes
    set rt1 [[[$p1 resolvedType] getClass] getName]
    set rt2 [[[$p2 resolvedType] getClass] getName]
    list $rt1 $rt2
} {ptolemy.data.DoubleToken ptolemy.data.DoubleToken}

######################################################################
####
#
test Director-8.3 {Test type resolution} {
    # use the setup above
    $p1 setDeclaredType $t1
    $p2 setDeclaredType [java::null]

    $director resolveTypes
    set rt1 [[[$p1 resolvedType] getClass] getName]
    set rt2 [[[$p2 resolvedType] getClass] getName]
    list $rt1 $rt2
} {ptolemy.data.IntToken ptolemy.data.StringToken}

######################################################################
####
#
test Director-8.4 {Test type resolution} {
    set director [java::new ptolemy.actor.Director]
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setExecutiveDirector $director
    $e0 setName E0

    #create e1, a source actor
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.TypedIOPort $e1 P1]
    $p1 makeOutput true
    set tDouble [java::new ptolemy.data.DoubleToken]
    $p1 setDeclaredType $tDouble

    #create e2, a fork
    set e2 [java::new ptolemy.actor.TypedAtomicActor $e0 E2]
    set p21 [java::new ptolemy.actor.TypedIOPort $e2 P21]
    set p22 [java::new ptolemy.actor.TypedIOPort $e2 P22]
    set p23 [java::new ptolemy.actor.TypedIOPort $e2 P23]
    $p21 makeInput true
    $p22 makeOutput true
    $p23 makeOutput true

    #create e3, a sink actor
    set e3 [java::new ptolemy.actor.TypedAtomicActor $e0 E3]
    set p3 [java::new ptolemy.actor.TypedIOPort $e3 P3]
    $p3 makeInput true

    #create e4, a sink actor
    set e4 [java::new ptolemy.actor.TypedAtomicActor $e0 E4]
    set p4 [java::new ptolemy.actor.TypedIOPort $e4 P4]
    $p4 makeInput true
    $p4 setDeclaredType $tDouble

    #link up p1-p21, p22-p3, p23-p4
    set r12 [java::new ptolemy.actor.IORelation $e0 R12]
    $p1 link $r12
    $p21 link $r12

    set r23 [java::new ptolemy.actor.IORelation $e0 R23]
    $p22 link $r23
    $p3 link $r23

    set r24 [java::new ptolemy.actor.IORelation $e0 R24]
    $p23 link $r24
    $p4 link $r24

    $director resolveTypes
    set rt1 [[[$p1 resolvedType] getClass] getName]
    set rt21 [[[$p21 resolvedType] getClass] getName]
    set rt22 [[[$p22 resolvedType] getClass] getName]
    set rt23 [[[$p23 resolvedType] getClass] getName]
    set rt3 [[[$p3 resolvedType] getClass] getName]
    set rt4 [[[$p4 resolvedType] getClass] getName]

    list $rt1 $rt21 $rt22 $rt23 $rt3 $rt4
} {ptolemy.data.DoubleToken ptolemy.data.DoubleToken ptolemy.data.StringToken\
ptolemy.data.DoubleToken ptolemy.data.StringToken ptolemy.data.DoubleToken}

######################################################################
####
#
test Director-8.5 {Test type resolution} {
    # use the setup above
    set tInt [java::new ptolemy.data.IntToken]
    $p1 setDeclaredType $tDouble
    $p4 setDeclaredType $tInt

    catch {$director resolveTypes} msg
    list $msg
} {{ptolemy.kernel.util.InvalidStateException: Type Conflict.}}

