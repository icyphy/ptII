# Tests for the TypedIOPort class
#
# @Author: Edward A. Lee, Yuhong Xiong
#
# $Id$
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

# NOTE:  All of the following tests use this director,
# pretty much as a dummy.
set director [java::new ptolemy.actor.Director]

######################################################################
####
#
test TypedIOPort-2.1 {Construct Ports} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setExecutiveDirector $director
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.TypedIOPort]
    set p2 [java::new ptolemy.actor.TypedIOPort $e1 P2]
    list [$p1 getFullName] [$p2 getFullName]
} {. ..E1.P2}

test TypedIOPort-2.2 {Construct Ports} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setExecutiveDirector $director
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.TypedIOPort $e1 P1]
    set p2 [java::new ptolemy.actor.TypedIOPort $e1 P2]
    list [$p1 getFullName] [$p2 getFullName]
} {..E1.P1 ..E1.P2}

test TypedIOPort-2.3 {Attempt to set erroneous container} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setExecutiveDirector $director
    set e1 [java::new ptolemy.actor.CompositeActor]
    set p1 [java::new ptolemy.actor.TypedIOPort]
    catch {$p1 setContainer $e1} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: . and .: TypedIOPort can only be contained by objects implementing the TypedActor interface.}}

######################################################################
####
#
test TypedIOPort-3.1 {set declared/resolved types} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setExecutiveDirector $director
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.TypedIOPort $e1 P2]
    set tDouble [java::new ptolemy.data.DoubleToken]
    $p1 setDeclaredType $tDouble

    set dt1 [[[$p1 declaredType] getClass] getName]
    set rt1 [[[$p1 resolvedType] getClass] getName]

    list $dt1 $rt1
} {ptolemy.data.DoubleToken ptolemy.data.DoubleToken}

test TypedIOPort-3.2 {set resolved types} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setExecutiveDirector $director
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.TypedIOPort $e1 P1]
    set tDouble [java::new ptolemy.data.DoubleToken]
    $p1 set [$tDouble getClass]

    set rt1 [[[$p1 resolvedType] getClass] getName]

    list [expr {[$p1 declaredType] == [java::null]}] $rt1
} {1 ptolemy.data.DoubleToken}


######################################################################
####
#   TEST description().

# Set bits to give class, name, receivers, and remotereceivers only
set detail [expr [java::field ptolemy.kernel.util.NamedObj CLASSNAME]|[java::field ptolemy.kernel.util.NamedObj FULLNAME]|[java::field ptolemy.actor.TypedIOPort TYPE]]

test TypedIOPort-4.1 {Check description on a new TypedIOPort} {
    set p0 [java::new ptolemy.actor.TypedIOPort]

    list [expr {$p0 == [java::null]}]
    $p0 description $detail
} {ptolemy.actor.TypedIOPort {.} type {declared null resolved null}}

test TypedIOPort-4.2 {test description} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setExecutiveDirector $director
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.TypedIOPort $e1 P1]
    set tDouble [java::new ptolemy.data.DoubleToken]
    $p1 setDeclaredType $tDouble

    $p1 description $detail
} {ptolemy.actor.TypedIOPort {..E1.P1} type {declared ptolemy.data.DoubleToken resolved ptolemy.data.DoubleToken}}

