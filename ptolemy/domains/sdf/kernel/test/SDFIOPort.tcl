# Tests for the SDFIOPort class
#
# @Author: Edward A. Lee, Yuhong Xiong, Steve Neuendorffer
#
# $Id$
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

# NOTE:  All of the following tests use this director,
# pretty much as a dummy.
set director [java::new ptolemy.actor.Director]
set manager [java::new ptolemy.actor.Manager]

######################################################################
####
#
test SDFIOPort-1.1 {Construct Ports} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 E1]
    set p1 [java::new ptolemy.domains.sdf.kernel.SDFIOPort]
    set p2 [java::new ptolemy.domains.sdf.kernel.SDFIOPort $e1 P2]
    list [$p1 getFullName] [$p2 getFullName]
} {. ..E1.P2}

test SDFIOPort-1.2 {Construct Ports} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 E1]
    set p1 [java::new ptolemy.domains.sdf.kernel.SDFIOPort $e1 P1]
    set p2 [java::new ptolemy.domains.sdf.kernel.SDFIOPort $e1 P2]
    list [$p1 getFullName] [$p2 getFullName]
} {..E1.P1 ..E1.P2}

test SDFIOPort-1.3 {Attempt to set erroneous container} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.CompositeActor]
    set p1 [java::new ptolemy.domains.sdf.kernel.SDFIOPort]
    catch {$p1 setContainer $e1} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: . and .:
TypedIOPort can only be contained by objects implementing the TypedActor interface.}}

######################################################################
####
#
test SDFIOPort-2.1 {set declared/resolved types} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 E1]
    set p1 [java::new ptolemy.domains.sdf.kernel.SDFIOPort $e1 P2]
    set tDouble [[java::new ptolemy.data.DoubleToken] getClass]
    $p1 setTypeEquals $tDouble

    set rt1 [[$p1 getType] toString]

    list $rt1
} {double}

######################################################################
####
#
test SDFIOPort-2.2 {set resolved types} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 E1]
    set p1 [java::new ptolemy.domains.sdf.kernel.SDFIOPort $e1 P1]
    set tt [$p1 getTypeTerm]
    $tt setValue [java::field ptolemy.data.type.BaseType DOUBLE]

    set isUndec [[$p1 getTypeTerm] isSettable]
    set rt1 [[$p1 getType] toString]

    list $isUndec $rt1
} {1 double}

######################################################################
####
#
test SDFIOPort-3.1 {test clone} {
    # use set up above
    set p2 [_testClone $p1]
    set rt2 [[$p2 getType] toString]

    set isUndec1 [[$p1 getTypeTerm] isSettable]
    set isUndec2 [[$p2 getTypeTerm] isSettable]
    list $isUndec $rt1 $isUndec2 $rt2
} {1 double 1 double}

######################################################################
####
#
test SDFIOPort-3.2 {test clone} {
    # use set up above

    set tInt [java::new ptolemy.data.IntToken]
    $p1 setTypeEquals [$tInt getClass]

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

test SDFIOPort-4.1 {Check description on a new SDFIOPort} {
    set p0 [java::new ptolemy.domains.sdf.kernel.SDFIOPort]

    list [expr {$p0 == [java::null]}]
    $p0 description $detail
} {ptolemy.domains.sdf.kernel.SDFIOPort {.} type {declared NaT resolved NaT}}

######################################################################
####
#
test SDFIOPort-4.2 {test description} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 E1]
    set p1 [java::new ptolemy.domains.sdf.kernel.SDFIOPort $e1 P1]
    set tDouble [[java::new ptolemy.data.DoubleToken] getClass]
    $p1 setTypeEquals $tDouble

    $p1 description $detail
} {ptolemy.domains.sdf.kernel.SDFIOPort {..E1.P1} type {declared double resolved double}}

######################################################################
####
#
test SDFIOPort-5.1 {test consumption rate methods} {
    set port [java::new ptolemy.domains.sdf.kernel.SDFIOPort]
    $port setInput 1
    $port setTokenConsumptionRate 5
    set rate1 [$port getTokenConsumptionRate]
    $port setTokenConsumptionRate 1
    set rate2 [$port getTokenConsumptionRate]
    list $rate1 $rate2
} {5 1}

test SDFIOPort-5.2 {test production methods} {
    set port [java::new ptolemy.domains.sdf.kernel.SDFIOPort]
    $port setOutput 1
    $port setTokenProductionRate 5
    set rate1 [$port getTokenProductionRate]
    $port setTokenProductionRate 1
    set rate2 [$port getTokenProductionRate]
    list $rate1 $rate2
} {5 1}

test SDFIOPort-5.2 {test init production methods} {
    set port [java::new ptolemy.domains.sdf.kernel.SDFIOPort]
    $port setOutput 1
    $port setTokenInitProduction 5
    set rate1 [$port getTokenInitProduction]
    $port setTokenInitProduction 1
    set rate2 [$port getTokenInitProduction]
    list $rate1 $rate2
} {5 1}
