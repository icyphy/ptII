# Tests for the CompositeActor class
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

# NOTE:  All of the following tests use this director,
# pretty much as a dummy.
set director [java::new pt.actor.Director]

######################################################################
####
#
test CompositeActor-2.1 {Constructor tests} {
    set e0 [java::new pt.actor.CompositeActor]
    $e0 setExecutiveDirector $director
    $e0 setName E0
    set w [java::new pt.kernel.util.Workspace W]
    set e1 [java::new pt.actor.CompositeActor]
    set e2 [java::new pt.actor.CompositeActor $w]
    set e3 [java::new pt.actor.CompositeActor $e0 E3]
    list [$e1 getFullName] [$e2 getFullName] [$e3 getFullName]
} {. W. .E0.E3}

######################################################################
####
#
test CompositeActor-3.1 {Test getDirector} {
    # NOTE: Uses the setup above
    list [expr {[$e1 getDirector] == [java::null]}] \
            [expr {[$e2 getDirector] == [java::null]}] \
            [expr {[$e3 getDirector] == $director}]
} {1 1 1}

test CompositeActor-3.2 {Test getDirector and getExecutiveDirector} {
    # NOTE: Extends the setup above
    set e5 [java::new pt.actor.CompositeActor $e3 E5]
    set wormdirect [java::new pt.actor.Director]
    $e5 setDirector $wormdirect
    list [expr {[$e5 getDirector] == $wormdirect}] \
            [expr {[$e5 getExecutiveDirector] == $director}] \
            [expr {[$e3 getDirector] == $director}] \
            [expr {[$e3 getExecutiveDirector] == $director}]
} {1 1 1 1}

test CompositeActor-3.3 {Test failure mode of setExecutiveDirector} {
    # NOTE: Uses the setup above
    set d3 [java::new pt.actor.Director]
    catch {$e5 setExecutiveDirector $d3} msg
    list $msg
} {{pt.kernel.util.IllegalActionException: .E0.E3.E5 and .: Cannot set the executive director of an actor with a container.}}

test CompositeActor-3.4 {Test isAtomic} {
    # NOTE: Uses the setup above
    list [$e5 isAtomic] [$e3 isAtomic] [$e2 isAtomic] [$e1 isAtomic] [$e0 isAtomic]
} {1 0 0 0 0}

######################################################################
####
#
test CompositeActor-4.1 {Test input/output lists} {
    # NOTE: Uses the setup above
    set p1 [java::new pt.actor.IOPort $e3 P1]
    set p2 [java::new pt.actor.IOPort $e3 P2 true true]
    set p3 [java::new pt.actor.IOPort $e3 P3 false true]
    set p4 [java::new pt.actor.IOPort $e3 P4 true false]
    list [enumToFullNames [$e3 inputPorts]] [enumToFullNames [$e3 outputPorts]]
} {{.E0.E3.P2 .E0.E3.P4} {.E0.E3.P2 .E0.E3.P3}}

######################################################################
####
#
test CompositeActor-5.1 {Test newPort} {
    # NOTE: Uses the setup above
    set p5 [$e3 newPort P5]
    enumToFullNames [$e3 getPorts]
} {.E0.E3.P1 .E0.E3.P2 .E0.E3.P3 .E0.E3.P4 .E0.E3.P5}

######################################################################
####
#
test CompositeActor-6.1 {Invoke all the action methods} {
     # NOTE: Uses the setup above
     $e5 initialize
     $e5 prefire
     $e5 fire
     $e5 postfire
     $e5 wrapup
} {}

######################################################################
####
#
test CompositeActor-7.1 {Test clone and description} {
    # NOTE: Uses the setup above
    set e4 [$e3 clone $w]
    $e4 description
} {pt.actor.CompositeActor {W.E3} attributes {
} ports {
    {pt.actor.IOPort {W.E3.P1} attributes {
    } links {
    } insidelinks {
    } configuration {{width 0}} receivers {
    } remotereceivers {
    }}
    {pt.actor.IOPort {W.E3.P2} attributes {
    } links {
    } insidelinks {
    } configuration {input output {width 0}} receivers {
    } remotereceivers {
    }}
    {pt.actor.IOPort {W.E3.P3} attributes {
    } links {
    } insidelinks {
    } configuration {output {width 0}} receivers {
    } remotereceivers {
    }}
    {pt.actor.IOPort {W.E3.P4} attributes {
    } links {
    } insidelinks {
    } configuration {input {width 0}} receivers {
    } remotereceivers {
    }}
    {pt.actor.IOPort {W.E3.P5} attributes {
    } links {
    } insidelinks {
    } configuration {{width 0}} receivers {
    } remotereceivers {
    }}
} entities {
    {pt.actor.CompositeActor {W.E3.E5} attributes {
    } ports {
    } entities {
    } relations {
    } director {
        {pt.actor.Director {.} attributes {
        }}
    } executivedirector {
    }}
} relations {
} director {
} executivedirector {
}}

######################################################################
####
#
test CompositeActor-8.1 {Test newReceiver} {
    # NOTE: Uses the setup above
    set r [$e3 newReceiver]
    set token [java::new pt.data.StringToken foo]
    $r put $token
    set received [$r get]
    $received toString
} {pt.data.StringToken(foo)}

######################################################################
####
#
test CompositeActor-9.1 {Test setContainer error catching} {
    # NOTE: Uses the setup above
    set entity [java::new pt.kernel.CompositeEntity]
    catch {$e1 setContainer $entity} msg
    list $msg
} {{pt.kernel.util.IllegalActionException: . and .: CompositeActor can only be contained by instances of CompositeActor.}}

######################################################################
####
#
test CompositeActor-10.1 {Test wormhole data transfers} {
    set e0 [java::new pt.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setName E0
    # top-level actors
    set e1 [java::new pt.actor.AtomicActor $e0 E1]
    set e2 [java::new pt.actor.CompositeActor $e0 E2]
    set e3 [java::new pt.actor.AtomicActor $e0 E3]

    # wormhole
    set wormdir [java::new pt.actor.Director]
    $e2 setDirector $wormdir

    # inside actor
    set e4 [java::new pt.actor.AtomicActor $e2 E4]

    # ports of outside actors
    set p1 [java::new pt.actor.IOPort $e1 P1 false true]
    set p2 [java::new pt.actor.IOPort $e2 P2 true false]
    set p3 [java::new pt.actor.IOPort $e2 P3 false true]
    set p4 [java::new pt.actor.IOPort $e3 P4 true false]

    # ports inside the wormhole
    set p5 [java::new pt.actor.IOPort $e4 P5 true false]
    set p6 [java::new pt.actor.IOPort $e4 P6 false true]

    # connections at the top level
    $e0 connect $p1 $p2
    $e0 connect $p3 $p4
    $e2 connect $p2 $p5
    $e2 connect $p6 $p3

    set token [java::new pt.data.StringToken foo]
    $p1 send 0 $token
    # check that token got only as far as p2
    set res1 [$p2 hasToken 0]
    set res2 [$p5 hasToken 0]

    $e2 prefire

    set res3 [$p2 hasToken 0]
    set res4 [$p5 hasToken 0]

    # Emulate a firing of e2.
    # Manually transfer the token via the output p6, as actor e2 would do.
    $p6 send 0 [$p5 get 0]

    set res5 [$p5 hasToken 0]
    set res6 [$p6 hasToken 0]
    # Note that the token should now be in an inside receiver of p3, which
    # is not reported by hasToken.
    set res7 [$p3 hasToken 0]

    $e2 postfire
    set res8 [$p4 hasToken 0]
    set res9 [[$p4 get 0] toString]

    list $res1 $res2 $res3 $res4 $res5 $res6 $res7 $res8 $res9
} {1 0 0 1 0 0 0 1 pt.data.StringToken(foo)}

#FIXME: test _removeEntity (using setContainer null).
