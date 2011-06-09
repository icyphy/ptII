# Tests for the TypedIOPort class
#
# @Author: Edward A. Lee, Yuhong Xiong, Christopher Hylands
#
# $Id$
#
# @Copyright (c) 1997-2009 The Regents of the University of California.
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

# NOTE:  All of the following tests use this director,
# pretty much as a dummy.
set director [java::new ptolemy.actor.Director]
set manager [java::new ptolemy.actor.Manager]

######################################################################
####
#
test TypedIOPort-1.1 {Construct Ports} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.TypedIOPort]
    set p2 [java::new ptolemy.actor.TypedIOPort $e1 P2]
    list [$p1 getFullName] [$p2 getFullName]
} {. ..E1.P2}

test TypedIOPort-1.2 {Construct Ports} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.TypedIOPort $e1 P1]
    set p2 [java::new ptolemy.actor.TypedIOPort $e1 P2]
    list [$p1 getFullName] [$p2 getFullName]
} {..E1.P1 ..E1.P2}

test TypedIOPort-1.3 {Attempt to set erroneous container} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.CompositeActor]
    set p1 [java::new ptolemy.actor.TypedIOPort]
    catch {$p1 setContainer $e1} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: TypedIOPort can only be contained by objects implementing the TypedActor interface.
  in .<Unnamed Object> and .<Unnamed Object>}}


######################################################################
####
#
test TypedIOPort-2.1 {set declared/resolved types} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.TypedIOPort $e1 P2]
    set tDouble [[java::new ptolemy.data.DoubleToken] getType]
    $p1 setTypeEquals $tDouble

    set rt1 [[$p1 getType] toString]

    list $rt1
} {double}

######################################################################
####
#
test TypedIOPort-2.2 {set resolved types} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.TypedIOPort $e1 P1]
    set tt [$p1 getTypeTerm]
    $tt setValue [java::field ptolemy.data.type.BaseType DOUBLE]

    set isUndec [[$p1 getTypeTerm] isSettable]
    set rt1 [[$p1 getType] toString]

    list $isUndec $rt1
} {1 double}

######################################################################
####
#
test TypedIOPort-3.1 {test clone} {
    # use set up above
    set p2 [_testClone $p1 [$e0 workspace]]
    set rt2 [[$p2 getType] toString]

    set isUndec1 [[$p1 getTypeTerm] isSettable]
    set isUndec2 [[$p2 getTypeTerm] isSettable]
    list $isUndec $rt1 $isUndec2 $rt2
} {1 double 1 double}

######################################################################
####
#
test TypedIOPort-3.2 {test clone} {
    # use set up above

    set tInt [java::new ptolemy.data.IntToken]
    $p1 setTypeEquals [$tInt getType]

    set tt2 [$p2 getTypeTerm]
    $tt2 setValue [java::field ptolemy.data.type.BaseType STRING]

    set rt1 [[$p1 getType] toString]
    set rt2 [[$p2 getType] toString]
    set isUndec2 [[$p2 getTypeTerm] isSettable]
    list $rt1 $isUndec2 $rt2
} {int 1 string}

######################################################################
####
#   TEST description().

######################################################################
####
#
# Set bits to give class, name, receivers, and remotereceivers only
set detail [expr [java::field ptolemy.kernel.util.NamedObj CLASSNAME]|[java::field ptolemy.kernel.util.NamedObj FULLNAME]|[java::field ptolemy.actor.TypedIOPort TYPE]]

test TypedIOPort-4.1 {Check description on a new TypedIOPort} {
    set p0 [java::new ptolemy.actor.TypedIOPort]

    list [expr {$p0 == [java::null]}]
    $p0 description $detail
} {ptolemy.actor.TypedIOPort {.} type {declared unknown resolved unknown}}

######################################################################
####
#
test TypedIOPort-4.2 {test description} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.TypedIOPort $e1 P1]
    set tDouble [[java::new ptolemy.data.DoubleToken] getType]
    $p1 setTypeEquals $tDouble

    $p1 description $detail
} {ptolemy.actor.TypedIOPort {..E1.P1} type {declared double resolved double}}


######################################################################
####
#
test TypedIOPort-5.0 {test Listeners } {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.TypedIOPort]
    set p2 [java::new ptolemy.actor.TypedIOPort $e1 P2]

    # No listeners have been added, and no scheduler is present
    set listener [java::new ptolemy.actor.test.TestTypeListener]

    # Try remove when there are no TypeListeners added yet
    $p1 removeTypeListener $listener

    $p1 addTypeListener $listener

    # Try adding it twice
    $p1 addTypeListener $listener

    set tDouble [[java::new ptolemy.data.DoubleToken] getType]
    $p1 setTypeEquals $tDouble

    $p1 removeTypeListener $listener

    set tInt [[java::new ptolemy.data.IntToken] getType]

    # We removed the type listener, so this should not add a message
    # to the listener

    $p1 setTypeEquals $tInt

    $listener getMessage
} {./unknown/double}

######################################################################
####
#
test TypedIOPort-9.1.0.2 {test that AbstractReceiver.putArrayToAll() calls convert} {
    # Note that vector length of 1 is used here. The generic
    # director uses a mailbox receiver, which can only hold
    # a single token, so larger vector lengths cannot be used
    # here. Domains that support vectorized get() and send()
    # should contain the tests for vector length > 1.
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    # sending entity
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.TypedIOPort $e1 P1 false true]
    set tInt [[java::new ptolemy.data.IntToken] getType]
    $p1 setTypeEquals $tInt

    # receiving entity
    set e2 [java::new ptolemy.actor.TypedAtomicActor $e0 E2]
    set p2 [java::new ptolemy.actor.TypedIOPort $e2 P2 true false]
    set tString [[java::new ptolemy.data.StringToken] getType]
    $p2 setTypeEquals $tString

    # connection
    # Can't use this because it uses a plain relation.
    # set r1 [$e0 connect $p1 $p2 R1]
    set r1 [java::new ptolemy.actor.TypedIORelation $e0 R1]
    $p1 link $r1
    $p2 link $r1
    
    # Call preinitialize on the manager so that the receivers get created
    # added Neil Smyth/Bert Rodiers. Need to call this as receivers are no longer 
    # created on the fly.
    $manager preinitializeAndResolveTypes

    # token to send
    set token [java::new ptolemy.data.IntToken 1]
    set token2 [java::new ptolemy.data.IntToken 42]

    # token array to send.
    set tokenArray [java::new {ptolemy.data.Token[]} {1}]
    $tokenArray set 0 $token

    # Tcl requires a fully qualified method signature for the overloaded
    # send() method.
    $p1 {send int ptolemy.data.Token[] int} 0 $tokenArray 1
    set received [$p2 get 0 1]
    $manager wrapup
    jdkPrintArray $received
} {{"1"}}


