# Tests for the AtomicActor class
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997-2003 The Regents of the University of California.
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
test AtomicActor-2.1 {Constructor tests} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setManager $manager
    $e0 setDirector $director
    $e0 setName E0
    set w [java::new ptolemy.kernel.util.Workspace W]
    set e1 [java::new ptolemy.actor.AtomicActor]
    set e2 [java::new ptolemy.actor.AtomicActor $w]
    set e3 [java::new ptolemy.actor.AtomicActor $e0 E3]
    list [$e1 getFullName] [$e2 getFullName] [$e3 getFullName]
} {. . .E0.E3}

######################################################################
####
#
test AtomicActor-3.1 {Test getDirector} {
    # NOTE: Uses the setup above
    list [expr {[$e1 getDirector] == [java::null]}] \
            [expr {[$e2 getDirector] == [java::null]}] \
            [expr {[$e3 getDirector] == $director}]
} {1 1 1}

######################################################################
####
#
test AtomicActor-3.2 {Test getManager} {
    # NOTE: Uses the setup in 2.1
    list [expr {[$e1 getManager] == [java::null]}] \
            [expr {[$e2 getManager] == [java::null]}] \
            [expr {[$e3 getManager] == $manager}]
} {1 1 1}

######################################################################
####
#
test AtomicActor-4.1 {Test input/output lists} {
    # NOTE: Uses the setup above
    set p1 [java::new ptolemy.actor.IOPort $e3 P1]
    set p2 [java::new ptolemy.actor.IOPort $e3 P2 true true]
    set p3 [java::new ptolemy.actor.IOPort $e3 P3 false true]
    set p4 [java::new ptolemy.actor.IOPort $e3 P4 true false]
    list [listToFullNames [$e3 inputPortList]] [listToFullNames [$e3 outputPortList]]
} {{.E0.E3.P2 .E0.E3.P4} {.E0.E3.P2 .E0.E3.P3}}

######################################################################
####
#
test AtomicActor-5.1 {Test newPort} {
    # NOTE: Uses the setup above
    set p5 [$e3 newPort P5]
    enumToFullNames [$e3 getPorts]
} {.E0.E3.P1 .E0.E3.P2 .E0.E3.P3 .E0.E3.P4 .E0.E3.P5}

######################################################################
####
#
test AtomicActor-6.1 {Invoke all the action methods} {
     # NOTE: Uses the setup above
     $e3 preinitialize
     $e3 initialize
     $e3 prefire
     $e3 fire
     $e3 postfire
     $e3 wrapup
     $e3 terminate
} {}

######################################################################
####
#
test AtomicActor-7.1 {Test clone and description} {
    # NOTE: Uses the setup above
    set e4 [java::cast ptolemy.actor.AtomicActor [$e3 clone $w]]
    $e4 description
} {ptolemy.actor.AtomicActor {.E3} attributes {
    {ptolemy.kernel.util.SingletonConfigurableAttribute {.E3._iconDescription} attributes {
    }}
} ports {
    {ptolemy.actor.IOPort {.E3.P1} attributes {
    } links {
    } insidelinks {
    } configuration {opaque {width 0}} receivers {
    } remotereceivers {
    }}
    {ptolemy.actor.IOPort {.E3.P2} attributes {
    } links {
    } insidelinks {
    } configuration {input output opaque {width 0}} receivers {
    } remotereceivers {
    }}
    {ptolemy.actor.IOPort {.E3.P3} attributes {
    } links {
    } insidelinks {
    } configuration {output opaque {width 0}} receivers {
    } remotereceivers {
    }}
    {ptolemy.actor.IOPort {.E3.P4} attributes {
    } links {
    } insidelinks {
    } configuration {input opaque {width 0}} receivers {
    } remotereceivers {
    }}
    {ptolemy.actor.IOPort {.E3.P5} attributes {
    } links {
    } insidelinks {
    } configuration {opaque {width 0}} receivers {
    } remotereceivers {
    }}
}}

######################################################################
####
#
test AtomicActor-8.1 {Test newReceiver} {
    # NOTE: Uses the setup above
    set r [$e3 newReceiver]
    set token [java::new ptolemy.data.StringToken foo]
    $r put $token
    set received [$r get]
    $received toString
} {"foo"}

######################################################################
####
#
test AtomicActor-9.1 {Test setContainer tolerance} {
    # NOTE: Uses the setup above
    set entity [java::new ptolemy.kernel.CompositeEntity]
    catch {$e1 setContainer $entity} msg
    list $msg
} {{}}

######################################################################
####
#
test AtomicActor-9.2 {Test remove a AtomicActor} {
    # NOTE: Uses the setup above
    set entity [java::new ptolemy.actor.AtomicActor $e0 ENTITY]
    $entity {setContainer ptolemy.kernel.CompositeEntity} [java::null]
    list [$e0 deepContains $entity]
} {0}
