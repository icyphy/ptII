# Tests for the Add class
#
# @Author: Yuhong Xiong
#
# @Version: $Id$
#
# @Copyright (c) 1997-1998 The Regents of the University of California.
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
test Add-1.1 {use default value} {
    set director [java::new ptolemy.actor.Director]
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setDirector $director
    $e0 setName E0
    $e0 setManager $manager

    #create ramp
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 Ramp 0 1]
    set rampOut [java::cast ptolemy.actor.TypedIOPort [$ramp getPort Output]]

    #create const
    set const [java::new ptolemy.actor.lib.Const $e0 Const 1]
    set constOut [java::cast ptolemy.actor.TypedIOPort [$const getPort Output]]


    #create adder
    set adder [java::new ptolemy.actor.lib.Add $e0 Adder]
    set adderIn [java::cast ptolemy.actor.TypedIOPort [$adder getPort Input]]
    set adderOut [java::cast ptolemy.actor.TypedIOPort [$adder getPort Output]]

    #create sink
    set sink [java::new ptolemy.actor.lib.test.TestSink $e0 Sink]
    set sinkIn [java::cast ptolemy.actor.TypedIOPort [$sink getPort Input]]

    #make connections
    set r1 [java::new ptolemy.actor.TypedIORelation $e0 R1]
    $rampOut link $r1
    $adderIn link $r1
    set r2 [java::new ptolemy.actor.TypedIORelation $e0 R2]
    $constOut link $r2
    $adderIn link $r2
    set r3 [java::new ptolemy.actor.TypedIORelation $e0 R3]
    $adderOut link $r3
    $sinkIn link $r3

    #initialize
    $director initialize
    $ramp initialize
    $const initialize

    $manager resolveTypes
    set rtRamp [[$rampOut getResolvedType] getName]
    set rtConst [[$constOut getResolvedType] getName]
    set rtAdderIn [[$adderIn getResolvedType] getName]
    set rtAdderOut [[$adderOut getResolvedType] getName]
    set rtSinkIn [[$sinkIn getResolvedType] getName]
    list $rtRamp $rtConst $rtAdderIn $rtAdderOut $rtSinkIn
} {ptolemy.data.IntToken ptolemy.data.IntToken ptolemy.data.IntToken ptolemy.data.IntToken ptolemy.data.IntToken}

######################################################################
####
#
test Add-1.2 {fire the above topology} {
    $ramp fire
    $const fire
    $adder fire
    $sink fire

    set t [$sink getToken]
    list [$t toString]
} {ptolemy.data.IntToken(1)}

######################################################################
####
#
test Add-1.3 {fire once more} {
    $ramp fire
    $const fire
    $adder fire
    $sink fire

    set t [$sink getToken]
    list [$t toString]
} {ptolemy.data.IntToken(2)}

######################################################################
####
#
test Add-2.1 {change Ramp init value type to double} {
    set initVal [java::cast ptolemy.data.expr.Parameter \
	    [$ramp getAttribute Value]]
    set dToken [java::new {ptolemy.data.DoubleToken double} 0.5]
    $initVal setType [$dToken getClass]
    $initVal setToken $dToken

    $ramp initialize

    $manager resolveTypes
    set rtRamp [[$rampOut getResolvedType] getName]
    set rtConst [[$constOut getResolvedType] getName]
    set rtAdderIn [[$adderIn getResolvedType] getName]
    set rtAdderOut [[$adderOut getResolvedType] getName]
    set rtSinkIn [[$sinkIn getResolvedType] getName]
    list $rtRamp $rtConst $rtAdderIn $rtAdderOut $rtSinkIn
} {ptolemy.data.DoubleToken ptolemy.data.IntToken ptolemy.data.DoubleToken ptolemy.data.DoubleToken ptolemy.data.DoubleToken}

######################################################################
####
#
test Add-2.2 {fire twice} {
    $ramp fire
    $const fire
    $adder fire
    $sink fire
    set t1 [$sink getToken]

    $ramp fire
    $const fire
    $adder fire
    $sink fire
    set t2 [$sink getToken]

    list [$t1 toString] [$t2 toString]
} {ptolemy.data.DoubleToken(1.5) ptolemy.data.DoubleToken(2.5)}

######################################################################
####
#
test Add-3.1 {Add another source that generates type conflict} {
    #create another source
    set source [java::new ptolemy.actor.lib.test.TestSource $e0 Source]
    set sourceOut [java::cast ptolemy.actor.TypedIOPort \
	    [$source getPort Output]]

    #make connections
    set rs [java::new ptolemy.actor.TypedIORelation $e0 RS]
    $sourceOut link $rs
    $adderIn link $rs

    set bToken [java::new {ptolemy.data.BooleanToken boolean} true]
    $source setToken $bToken

    $director initialize
    catch {$manager resolveTypes} msg
    list $msg
} {{ptolemy.actor.TypeConflictException: Type conflicts occurred in .E0}}

######################################################################
####
#
test Add-3.2 {fix above by changing source type to Complex} {
    set com [java::new {ptolemy.math.Complex double double} 2.2 8.8]
    set cToken [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} \
	    $com]
    $source setToken $cToken

    $manager resolveTypes
    set rtSource [[$sourceOut getResolvedType] getName]
    set rtRamp [[$rampOut getResolvedType] getName]
    set rtConst [[$constOut getResolvedType] getName]
    set rtAdderIn [[$adderIn getResolvedType] getName]
    set rtAdderOut [[$adderOut getResolvedType] getName]
    set rtSinkIn [[$sinkIn getResolvedType] getName]
    list $rtSource $rtRamp $rtConst $rtAdderIn $rtAdderOut $rtSinkIn
} {ptolemy.data.ComplexToken ptolemy.data.DoubleToken ptolemy.data.IntToken ptolemy.data.ComplexToken ptolemy.data.ComplexToken ptolemy.data.ComplexToken}

