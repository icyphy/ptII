# Tests for the CompositeActor class
#
# @Author: Edward A. Lee, Christopher Hylands
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

# NOTE:  All of the following tests use this director,
# pretty much as a dummy.
set director [java::new ptolemy.actor.Director]
set manager [java::new ptolemy.actor.Manager]

######################################################################
####
#
test CompositeActor-2.1 {Constructor tests} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setManager $manager
    $e0 setDirector $director
    $e0 setName E0
    set w [java::new ptolemy.kernel.util.Workspace W]
    set e1 [java::new ptolemy.actor.CompositeActor]
    set e2 [java::new ptolemy.actor.CompositeActor $w]
    set e3 [java::new ptolemy.actor.CompositeActor $e0 E3]
    list [$e1 getFullName] [$e2 getFullName] [$e3 getFullName]
} {. . .E0.E3}

######################################################################
####
#
test CompositeActor-3.1 {Test getDirector} {
    # NOTE: Uses the setup above
    list [expr {[$e1 getDirector] == [java::null]}]  \
            [expr {[$e2 getDirector] == [java::null]}] \
            [expr {[$e3 getDirector] == $director}]
} {1 1 1}

test CompositeActor-3.2 {Test getDirector and getExecutiveDirector} {
    # NOTE: Extends the setup above
    $e3 getName
    set e5 [java::new ptolemy.actor.CompositeActor $e3 E5]
    set wormdirect [java::new ptolemy.actor.Director $e5 WORMDIR]
    #$e5 setDirector $wormdirect
    list [expr {[$e5 getDirector] == $wormdirect}] \
            [expr {[$e5 getExecutiveDirector] == $director}] \
            [expr {[$e3 getDirector] == $director}] \
            [expr {[$e3 getExecutiveDirector] == $director}] \
            [expr {[$e0 getDirector] == $director}] \
            [expr {[$e0 getExecutiveDirector] == [java::null]}] \
} {1 1 1 1 1 1}

test CompositeActor-3.3 {Test failure mode of setManager} {
    # NOTE: Uses the setup above
    set m3 [java::new ptolemy.actor.Manager]
    set m4 [java::new ptolemy.actor.Manager $w Manager]
    catch {$e5 setManager $m3} msg
    catch {$e0 setManager $m4} msg2
    list $msg $msg2
} {{ptolemy.kernel.util.IllegalActionException: Cannot set the Manager of an actor with a container.
  in .E0.E3.E5 and .<Unnamed Object>} {ptolemy.kernel.util.IllegalActionException: Cannot set manager because workspaces are different.
  in .E0 and .Manager}}

test CompositeActor-3.3a {Test failure mode of setDirector} {
    # NOTE: Uses the setup above
    set d4 [java::new ptolemy.actor.Director $w]
    $d4 setName Director
    catch {$e0 setDirector $d4} msg
    list $msg 
} {{ptolemy.kernel.util.IllegalActionException: Cannot set container because workspaces are different.
  in .Director and .E0}}

test CompositeActor-3.4 {Test isOpaque} {
    # NOTE: Uses the setup above
    list [$e5 isOpaque] [$e3 isOpaque] [$e2 isOpaque] [$e1 isOpaque] [$e0 isOpaque]
} {1 0 0 0 1}

test CompositeActor-3.5 {Test getManager} {
    # NOTE: Uses the setup above
    list    [expr {[$e5 getManager] == $manager}] \
	    [expr {[$e3 getManager] == $manager}] \
	    [expr {[$e2 getManager] == [java::null]}] \
	    [expr {[$e1 getManager] == [java::null]}] \
	    [expr {[$e0 getManager] == $manager}]
} {1 1 1 1 1}

######################################################################
####
#
test CompositeActor-4.1 {Test input/output lists} {
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

    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
            {java.io.PrintStream java.io.OutputStream} $stream]
    set listener [java::new ptolemy.kernel.util.StreamListener $printStream]
    $e5 addDebugListener $listener

    $e5 preinitialize
    $e5 initialize
    $e5 prefire
    $e5 fire
    $e5 postfire
    $e5 wrapup
    $e5 terminate
    $e5 stop

    $printStream flush
    $e5 removeDebugListener $listener
    # This hack is necessary because of problems with crnl under windows
    regsub -all [java::call System getProperty "line.separator"] \
	        [$stream toString] "\n" output
    list $output
} {{Called preinitialize()
Called initialize()
Called prefire()
Prefire returns (from director) true
Called fire()
Called postfire()
Postfire returns (from director) true
Called wrapup()
Called terminate()
Called stop()
}}

######################################################################
####
#
test CompositeActor-7.1 {Test clone and description} {
    # NOTE: Uses the setup above
    set e4 [java::cast ptolemy.actor.CompositeActor [$e3 clone $w]]
    $e4 description
} {ptolemy.actor.CompositeActor {.E3} attributes {
    {ptolemy.kernel.util.SingletonConfigurableAttribute {.E3._iconDescription} attributes {
    }}
} ports {
    {ptolemy.actor.IOPort {.E3.P1} attributes {
    } links {
    } insidelinks {
    } configuration {{width 0}} receivers {
    } remotereceivers {
    }}
    {ptolemy.actor.IOPort {.E3.P2} attributes {
    } links {
    } insidelinks {
    } configuration {input output {width 0}} receivers {
    } remotereceivers {
    }}
    {ptolemy.actor.IOPort {.E3.P3} attributes {
    } links {
    } insidelinks {
    } configuration {output {width 0}} receivers {
    } remotereceivers {
    }}
    {ptolemy.actor.IOPort {.E3.P4} attributes {
    } links {
    } insidelinks {
    } configuration {input {width 0}} receivers {
    } remotereceivers {
    }}
    {ptolemy.actor.IOPort {.E3.P5} attributes {
    } links {
    } insidelinks {
    } configuration {{width 0}} receivers {
    } remotereceivers {
    }}
} classes {
} entities {
    {ptolemy.actor.CompositeActor {.E3.E5} attributes {
        {ptolemy.kernel.util.SingletonConfigurableAttribute {.E3.E5._iconDescription} attributes {
        }}
        {ptolemy.actor.Director {.E3.E5.WORMDIR} attributes {
            {ptolemy.kernel.util.SingletonConfigurableAttribute {.E3.E5.WORMDIR._iconDescription} attributes {
            }}
        }}
    } ports {
    } classes {
    } entities {
    } relations {
    }}
} relations {
}}


######################################################################
####
#
test CompositeActor-8.1 {Test newReceiver} {
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
test CompositeActor-9.1 {Test setContainer tolerance} {
    # NOTE: Uses the setup above
    set entity [java::new ptolemy.kernel.CompositeEntity]
    catch {$e1 setContainer $entity} msg
    list $msg
} {{}}

######################################################################
####
#
test CompositeActor-10.1 {Test wormhole data transfers} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setName E0
    # top-level actors
    set e1 [java::new ptolemy.actor.AtomicActor $e0 E1]
    set e2 [java::new ptolemy.actor.CompositeActor $e0 E2]
    set e3 [java::new ptolemy.actor.AtomicActor $e0 E3]

    # wormhole
    set wormdir [java::new ptolemy.actor.Director]
    $e2 setDirector $wormdir

    # inside actor
    set e4 [java::new ptolemy.actor.test.IdentityActor $e2 IDEN]

    # ports of outside actors
    set p1 [java::new ptolemy.actor.IOPort $e1 P1 false true]
    set p2 [java::new ptolemy.actor.IOPort $e2 P2 true false]
    set p3 [java::new ptolemy.actor.IOPort $e2 P3 false true]
    set p4 [java::new ptolemy.actor.IOPort $e3 P4 true false]

    # ports inside the wormhole
    set p5 [java::cast ptolemy.actor.IOPort [$e4 getPort input]]
    set p6 [java::cast ptolemy.actor.IOPort [$e4 getPort output]]

    # connections at the top level
    $e0 connect $p1 $p2
    $e0 connect $p3 $p4
    $e2 connect $p2 $p5
    $e2 connect $p6 $p3

    
    # Call initialize on the director so that the receivers get created
    # added Neil Smyth. Need to call this as receivers are no longer 
    # created on the fly.
    $director preinitialize
    $director initialize

    set token [java::new ptolemy.data.StringToken foo]
    # Tcl requires a fully qualified method signature for the overloaded
    # send() method.
    $p1 {send int ptolemy.data.Token} 0 $token
    # check that token got only as far as p2
    set res1 [$p2 hasToken 0]
    set res2 [$p5 hasToken 0]

    $e2 prefire
    
    set res3 [$p2 hasToken 0]
    set res4 [$p5 hasToken 0]

    
    # Emulate a fire of e2
    # Manually transfer the token via the output p6, as actor e2 would do.
    $e2 fire

    set res5 [$p5 hasToken 0]
    catch {$p6 hasToken 0} res6
    # Note that the token should now be in an inside receiver of p3, which
    # is not reported by hasToken.
    catch {$p3 hasToken 0} res7

    $e2 postfire
    set res8 [$p4 hasToken 0]
    set res9 [[$p4 get 0] toString]
    
    list $res1 $res2 $res3 $res4 $res5 $res6 $res7 $res8 $res9
} {1 0 1 0 0 {ptolemy.kernel.util.IllegalActionException: Port is not an input port!
  in .E0.E2.IDEN.output} {ptolemy.kernel.util.IllegalActionException: Port is not an input port!
  in .E0.E2.P3} 1 {"foo"}}


######################################################################
#### 
#
test CompositeActor-10.1 {Test all actor list} {
    # use above set up.

    # add one more inside actor which connects to nothing
    set e5 [java::new ptolemy.actor.test.IdentityActor $e2 E5]
    
    list [listToFullNames [$e0 allAtomicEntityList]]
} {{.E0.E1 .E0.E3 .E0.E2.IDEN .E0.E2.E5}}
#FIXME: test _removeEntity (using setContainer null).

