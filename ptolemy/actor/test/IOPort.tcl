# Tests for the IOPort class
#
# @Author: Edward A. Lee, Lukito Muliadi, Christopher Hylands
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

######################################################################
####
#
# FIXME: set the following test
# test IOPort-1.1 {Get information about an instance of IOPort} {
#     # If anything changes, we want to know about it so we can write tests.
#     set n [java::new ptolemy.actor.IOPort]
#     list [getJavaInfo $n]
# } {{
# }}

# NOTE:  All of the following tests use this director,
# pretty much as a dummy.
set director [java::new ptolemy.actor.Director]
set manager [java::new ptolemy.actor.Manager]

######################################################################
####
#
test IOPort-2.1 {Construct Ports} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.AtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.IOPort]
    set p2 [java::new ptolemy.actor.IOPort $e1 P2]
    list [$p1 getFullName] [$p2 getFullName]
} {. ..E1.P2}

test IOPort-2.2 {Construct Ports} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.AtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.IOPort $e1 P1]
    set p2 [java::new ptolemy.actor.IOPort $e1 P2]
    list [$p1 getFullName] [$p2 getFullName]
} {..E1.P1 ..E1.P2}

test IOPort-2.3 {Attempt to set erroneous container} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    set p1 [java::new ptolemy.actor.IOPort]
    catch {$p1 setContainer $e1} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: IOPort can only be contained by objects implementing the Actor interface.
  in .<Unnamed Object> and .<Unnamed Object>}}

######################################################################
####
#
test IOPort-3.1 {Test input/output predicates} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.AtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.IOPort $e1 P1]
    set p2 [java::new ptolemy.actor.IOPort $e1 P2 true true]
    list [$p1 isInput] [$p1 isOutput] [$p2 isInput] [$p2 isOutput]
} {0 0 1 1}

test IOPort-3.2 {Test input/output changes} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.AtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.IOPort $e1 P1 true true]
    set result [list [$p1 isInput] [$p1 isOutput]]
    $p1 setInput false
    $p1 setOutput false
    lappend result [$p1 isInput] [$p1 isOutput]
} {1 1 0 0}

test IOPort-3.3 {Test input/output predicates on transparent ports} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector [java::null]
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.AtomicActor $e0 E1]
    set p0 [java::new ptolemy.actor.IOPort $e0 P0]
    set p1 [java::new ptolemy.actor.IOPort $e1 P1 true true]
    set r1 [java::new ptolemy.actor.IORelation $e0 R1]
    $p1 link $r1
    $p0 link $r1
    list [$p0 isInput] [$p0 isOutput] [$p1 isInput] [$p1 isOutput]
} {1 1 1 1}

######################################################################
####
#
test IOPort-4.1 {Test multiport predicate} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.AtomicActor $e0 E1]
    set p0 [java::new ptolemy.actor.IOPort $e0 P0]
    set p1 [java::new ptolemy.actor.IOPort $e1 P1 true true]
    set r1 [java::new ptolemy.actor.IORelation $e0 R1]
    $p1 link $r1
    $p0 link $r1
    $p1 setMultiport true
    $p0 isMultiport
} {0}

test IOPort-4.2 {Test multiport predicate on transparent port} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.AtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.IOPort $e1 P1 true false]
    set temp [$p1 isMultiport]
    $p1 setMultiport true
    list $temp [$p1 isMultiport]
} {0 1}

######################################################################
####
#
test IOPort-5.1 {Test getWidth} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.AtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.IOPort $e1 P1 true false]
    set r1 [java::new ptolemy.actor.IORelation $e0 R1]
    set temp [$p1 getWidth]
    $p1 liberalLink $r1
    list $temp [$p1 getWidth]
} {0 1}

test IOPort-5.2 {Test getWidth} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.AtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.IOPort $e1 P1 true true]
    set r1 [java::new ptolemy.actor.IORelation $e0 R1]
    set r2 [java::new ptolemy.actor.IORelation $e0 R2]
    $p1 setMultiport true
    $p1 liberalLink $r1
    $p1 liberalLink $r2
    $p1 getWidth
} {2}

test IOPort-5.3 {Test getWidth} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.AtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.IOPort $e1 P1 true true]
    set r1 [java::new ptolemy.actor.IORelation $e0 R1]
    $r1 setWidth 2
    set r2 [java::new ptolemy.actor.IORelation $e0 R2]
    $p1 setMultiport true
    $p1 liberalLink $r1
    $p1 liberalLink $r2
    $p1 getWidth
} {3}

test IOPort-5.4 {Test getWidth after unlinking} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    # sending entity
    set e1 [java::new ptolemy.actor.AtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.IOPort $e1 P1 false true]
    # receiving entity
    set e2 [java::new ptolemy.actor.AtomicActor $e0 E2]
    set p2 [java::new ptolemy.actor.IOPort $e2 P2 true false]
    # connection
    # Can't use this because it uses a plain relation.
    # set r1 [$e0 connect $p1 $p2 R1]
    set r1 [java::new ptolemy.actor.IORelation $e0 R1]
    $p1 link $r1
    $p2 link $r1
    
    # Call preinitialize on the director so that the receivers get created
    # added Neil Smyth. Need to call this as receivers are no longer 
    # created on the fly.
    $director preinitialize

    $p1 getRemoteReceivers
    $p2 unlink $r1
    list [$p1 getWidth] [$p2 getWidth]
} {1 0}

######################################################################
####
#
test IOPort-6.1 {Make sure multiple links not allowed on single ports} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.AtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.IOPort $e1 P1 true true]
    set r1 [java::new ptolemy.actor.IORelation $e0 R1]
    set r2 [java::new ptolemy.actor.IORelation $e0 R2]
    $p1 liberalLink $r1
    catch {$p1 liberalLink $r2} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Attempt to link more than one relation to a single port.
  in .<Unnamed Object>.E1.P1 and .<Unnamed Object>.R2}}

######################################################################
####
#
test IOPort-7.1 {Check getReceivers on an unlinked port} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.AtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.IOPort $e1 P1 true true]
    set p1Rcvrs [$p1 getReceivers]
    expr { [$p1Rcvrs length] == 0 }
} {1}

test IOPort-7.2 {Check getReceivers} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.AtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.IOPort $e1 P1 true true]
    set r1 [java::new ptolemy.actor.IORelation $e0 R1]
    catch {$p1 getReceivers $r1} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: getReceivers: Relation argument is not linked to me.
  in .<Unnamed Object>.E1.P1 and .<Unnamed Object>.R1}}

######################################################################
####
# Test getReceivers() on opaque ports.
# The test uses the description method to represent the results.

test IOPort-7.3 {test getReceivers()} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    $e0 setName E0
    set e1 [java::new ptolemy.actor.CompositeActor $e0 "E1"]
    $e1 setDirector [java::new ptolemy.actor.Director]
    set e2 [java::new ptolemy.actor.CompositeActor $e0 "E2"]
    $e2 setDirector [java::new ptolemy.actor.Director]
    set e3 [java::new ptolemy.actor.AtomicActor $e1 "E3"]
    set e4 [java::new ptolemy.actor.AtomicActor $e2 "E4"]
    set p1 [java::new ptolemy.actor.IOPort $e1 "P1"]
    set p2 [java::new ptolemy.actor.IOPort $e2 "P2"]
    set p3 [java::new ptolemy.actor.IOPort $e3 "P3"]
    set p4 [java::new ptolemy.actor.IOPort $e4 "P4"]
    $p1 setMultiport true
    $p2 setMultiport true
    $p3 setMultiport true
    $p3 setOutput true
    $p4 setMultiport true
    $p4 setInput true
    set r1 [java::cast ptolemy.actor.IORelation [$e0 connect $p1 $p2]]
    $r1 setWidth 3
    set r2 [java::cast ptolemy.actor.IORelation [$e1 connect $p3 $p1]]
    $r2 setWidth 2
    set r3 [java::cast ptolemy.actor.IORelation [$e2 connect $p2 $p4]]
    $r3 setWidth 4

    # Call preinitialize on the director so that the receivers get created
    # added Neil Smyth. Need to call this as receivers are no longer 
    # created on the fly.
    $e0 preinitialize
    
    set receivers [java::field ptolemy.actor.IOPort RECEIVERS]
    set remotereceivers [java::field ptolemy.actor.IOPort REMOTERECEIVERS]
    $p2 description $receivers
} {receivers {
    {
        {ptolemy.actor.Mailbox}
    }
    {
        {ptolemy.actor.Mailbox}
    }
    {
        {ptolemy.actor.Mailbox}
    }
}}

test IOPort-7.4 {test getReceivers()} {
    # NOTE: Uses setup in previous test.
    $p4 description $receivers
} {receivers {
    {
        {ptolemy.actor.Mailbox}
    }
    {
        {ptolemy.actor.Mailbox}
    }
    {
        {ptolemy.actor.Mailbox}
    }
    {
        {ptolemy.actor.Mailbox}
    }
}}

test IOPort-7.5 {test getReceivers()} {
    # NOTE: Uses setup in previous test.
    $p1 description $receivers
} {receivers {
}}

test IOPort-7.6 {test getReceivers()} {
    # NOTE: Uses setup in previous test.
    $p3 description $receivers
} {receivers {
}}

######################################################################
####
# Test getRemoteReceivers() on opaque ports.
# NOTE: Uses the same setup from the previous batch of tests.

test IOPort-7.7 {test getRemoteReceivers()} {
    # NOTE: Uses setup in previous test.
    $p1 description $remotereceivers
} {remotereceivers {
    {
        {ptolemy.actor.Mailbox in .E0.E2.P2}
    }
    {
        {ptolemy.actor.Mailbox in .E0.E2.P2}
    }
    {
        {ptolemy.actor.Mailbox in .E0.E2.P2}
    }
}}

test IOPort-7.8 {test getRemoteReceivers()} {
    # NOTE: Uses setup in previous test.
    $p3 description $remotereceivers
} {remotereceivers {
    {
        {ptolemy.actor.Mailbox in .E0.E1.P1}
    }
    {
        {ptolemy.actor.Mailbox in .E0.E1.P1}
    }
}}

test IOPort-7.9 {test getRemoteReceivers()} {
    # NOTE: Uses setup in previous test.
    $p2 description $remotereceivers
} {remotereceivers {
}}

test IOPort-7.10 {test getRemoteReceivers()} {
    # NOTE: Uses setup in previous test.
    $p4 description $remotereceivers
} {remotereceivers {
}}

######################################################################
####
#
test IOPort-8.1 {Check getRemoteReceivers on a port with no links} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.AtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.IOPort $e1 P1 true true]
    set p1Rcvrs [$p1 getReceivers]
    expr { [$p1Rcvrs length] == 0 }
} {1}

test IOPort-8.2 {Check getRemoteReceivers on a port after unlinking} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    # sending entity
    set e1 [java::new ptolemy.actor.AtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.IOPort $e1 P1 false true]
    # receiving entity
    set e2 [java::new ptolemy.actor.AtomicActor $e0 E2]
    set p2 [java::new ptolemy.actor.IOPort $e1 P2 true false]
    # connection
    # Can't use this because it uses a plain relation.
    # set r1 [$e0 connect $p1 $p2 R1]
    set r1 [java::new ptolemy.actor.IORelation $e0 R1]
    $p1 link $r1
    $p2 link $r1
    
    # Call preinitialize on the director so that the receivers get created
    # added Neil Smyth. Need to call this as receivers are no longer 
    # created on the fly.
    $director preinitialize

    $p1 getRemoteReceivers
    $p2 unlink $r1
    set p1Rcvrs [$p1 getRemoteReceivers]
    $p1Rcvrs length
    expr {[$p1 getRemoteReceivers] == [java::null]}
} {0}

######################################################################
####
#
test IOPort-9.1 {Check connectivity via send} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    # sending entity
    set e1 [java::new ptolemy.actor.AtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.IOPort $e1 P1 false true]
    # receiving entity
    set e2 [java::new ptolemy.actor.AtomicActor $e0 E2]
    set p2 [java::new ptolemy.actor.IOPort $e2 P2 true false]
    # connection
    # Can't use this because it uses a plain relation.
    # set r1 [$e0 connect $p1 $p2 R1]
    set r1 [java::new ptolemy.actor.IORelation $e0 R1]
    $p1 link $r1
    $p2 link $r1
    
    # Call preinitialize on the director so that the receivers get created
    # added Neil Smyth. Need to call this as receivers are no longer 
    # created on the fly.
    $director preinitialize

    # token to send
    set token [java::new ptolemy.data.StringToken foo]
    # Tcl requires a fully qualified method signature for the overloaded
    # send() method.
    $p1 {send int ptolemy.data.Token} 0 $token
    set received [$p2 get 0]
    $received toString
} {"foo"}

######################################################################
####
#
test IOPort-9.1.0 {Check connectivity via vectorized send and get} {
    # Note that vector length of 1 is used here. The generic
    # director uses a mailbox receiver, which can only hold
    # a single token, so larger vector lengths cannot be used
    # here. Domains that support vectorized get() and send()
    # should contain the tests for vector length > 1.
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    # sending entity
    set e1 [java::new ptolemy.actor.AtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.IOPort $e1 P1 false true]
    # receiving entity
    set e2 [java::new ptolemy.actor.AtomicActor $e0 E2]
    set p2 [java::new ptolemy.actor.IOPort $e2 P2 true false]
    # connection
    # Can't use this because it uses a plain relation.
    # set r1 [$e0 connect $p1 $p2 R1]
    set r1 [java::new ptolemy.actor.IORelation $e0 R1]
    $p1 link $r1
    $p2 link $r1
    
    # Call preinitialize on the director so that the receivers get created
    # added Neil Smyth. Need to call this as receivers are no longer 
    # created on the fly.
    $director preinitialize

    # token to send
    set token [java::new ptolemy.data.StringToken foo]
    set token2 [java::new ptolemy.data.StringToken bar]

    # token array to send.
    set tokenArray [java::new {ptolemy.data.Token[]} {1}]
    $tokenArray set 0 $token

    # Tcl requires a fully qualified method signature for the overloaded
    # send() method.
    $p1 {send int ptolemy.data.Token[] int} 0 $tokenArray 1
    set received [$p2 get 0 1]
    jdkPrintArray $received
} {{"foo"}}

test IOPort-9.1.1 {Check hasRoom and hasToken methods} {
    # NOTE: Use previous setup.
    set res1 [$p1 hasRoom 0]
    catch {$p1 hasToken 0} res2
    catch {$p2 hasRoom 0} res3
    set res4 [$p2 hasToken 0]
    # Tcl requires a fully qualified method signature for the overloaded
    # send() method.
    $p1 {send int ptolemy.data.Token} 0 $token
    set res5 [$p1 hasRoom 0]
    catch {$p1 hasToken 0} res6
    catch {$p2 hasRoom 0} res7
    set res8 [$p2 hasToken 0]
    list $res1 $res2 $res3 $res4 $res5 $res6 $res7 $res8
} {1 {ptolemy.kernel.util.IllegalActionException: Port is not an input port!
  in .<Unnamed Object>.E1.P1} {ptolemy.kernel.util.IllegalActionException: hasRoom: channel index is out of range.
  in .<Unnamed Object>.E2.P2} 0 0 {ptolemy.kernel.util.IllegalActionException: Port is not an input port!
  in .<Unnamed Object>.E1.P1} {ptolemy.kernel.util.IllegalActionException: hasRoom: channel index is out of range.
  in .<Unnamed Object>.E2.P2} 1}


test IOPort-9.2 {Check unlink and send to dangling relation} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    # sending entity
    set e1 [java::new ptolemy.actor.AtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.IOPort $e1 P1 false true]
    # receiving entity
    set e2 [java::new ptolemy.actor.AtomicActor $e0 E2]
    set p2 [java::new ptolemy.actor.IOPort $e2 P2 true false]
    # connection
    set r1 [java::new ptolemy.actor.IORelation $e0 R1]
    $p1 link $r1
    $p2 link $r1
    # unlink one end
    $p2 unlinkAll
    # token to send
    set token [java::new ptolemy.data.StringToken foo]
    # Tcl requires a fully qualified method signature for the overloaded
    # send() method.
    $p1 {send int ptolemy.data.Token} 0 $token
    catch {$p2 get 0} msg
    list [$p2 getWidth] $msg
} {0 {ptolemy.kernel.util.IllegalActionException: Channel index 0 is out of range, because width is only 0.
  in .<Unnamed Object>.E2.P2}}

test IOPort-9.3 {Check unlink and get from unlinked port} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    # sending entity
    set e1 [java::new ptolemy.actor.AtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.IOPort $e1 P1 false true]
    # receiving entity
    set e2 [java::new ptolemy.actor.AtomicActor $e0 E2]
    set p2 [java::new ptolemy.actor.IOPort $e2 P2 true false]
    # connection
    # Can't use connect because it uses a plain relation.
    # set r1 [$e0 connect $p1 $p2 R1]
    set r1 [java::new ptolemy.actor.IORelation $e0 R1]
    $p1 link $r1
    $p2 link $r1
    # unlink one end
    $p1 unlink $r1
    
    # Call preinitialize on the director so that the receivers get created
    # added Neil Smyth. Need to call this as receivers are no longer 
    # created on the fly.
    $director preinitialize

    # token to send
    set token [java::new ptolemy.data.StringToken foo]
    catch {$p1 hasRoom 0} msg1
    catch {$p2 get 0} msg2
    list [$p2 getWidth] $msg1 $msg2
} {1 {ptolemy.kernel.util.IllegalActionException: hasRoom: channel index is out of range.
  in .<Unnamed Object>.E1.P1} {ptolemy.actor.NoTokenException: Attempt to get data from an empty mailbox.
  in .<Unnamed Object>.E2.P2}}

test IOPort-9.4 {Check loopback send} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager

    set e1 [java::new ptolemy.actor.AtomicActor $e0 E1]
    # sending port
    set p1 [java::new ptolemy.actor.IOPort $e1 P1 false true]
    # receiving port
    set p2 [java::new ptolemy.actor.IOPort $e1 P2 true false]
    set r1 [$e0 connect $p1 $p2 R1]
    # token to send
    set token [java::new ptolemy.data.StringToken foo]

    # Call preinitialize on the director so that the receivers get created
    # added Neil Smyth. Need to call this as receivers are no longer 
    # created on the fly.
    $director preinitialize

    # Tcl requires a fully qualified method signature for the overloaded
    # send() method.
    $p1 {send int ptolemy.data.Token} 0 $token
    set received [$p2 get 0]
    $received toString
} {"foo"}

######################################################################
####
#   TEST description().

# Set bits to give class, name, receivers, and remotereceivers only
set detail [expr [java::field ptolemy.kernel.util.NamedObj CLASSNAME]|[java::field ptolemy.kernel.util.NamedObj FULLNAME]|[java::field ptolemy.actor.IOPort RECEIVERS]|[java::field ptolemy.actor.IOPort REMOTERECEIVERS]]

test IOPort-10.1 {Check description on a new IOPort} {
    set p0 [java::new ptolemy.actor.IOPort]
    $p0 description $detail
} {ptolemy.actor.IOPort {.} receivers {
} remotereceivers {
}}

test IOPort-10.2 {Check description use test-7.1 topology} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.AtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.IOPort $e1 P1 true true]
    $p1 description $detail
} {ptolemy.actor.IOPort {..E1.P1} receivers {
} remotereceivers {
}}

test IOPort-10.3 {Check description use test-9.1 topology} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    # sending entity
    set e1 [java::new ptolemy.actor.AtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.IOPort $e1 P1 false true]
    # receiving entity
    set e2 [java::new ptolemy.actor.AtomicActor $e0 E2]
    set p2 [java::new ptolemy.actor.IOPort $e2 P2 true false]
    set r1 [$e0 connect $p1 $p2 R1]

    # Call preinitialize on the director so that the receivers get created
    # added Neil Smyth. Need to call this as receivers are no longer 
    # created on the fly.
    $director preinitialize

    list "[$p1 description $detail]\n[$p2 description $detail]"
} {{ptolemy.actor.IOPort {..E1.P1} receivers {
} remotereceivers {
    {
        {ptolemy.actor.Mailbox in ..E2.P2}
    }
}
ptolemy.actor.IOPort {..E2.P2} receivers {
    {
        {ptolemy.actor.Mailbox}
    }
} remotereceivers {
}}}

test IOPort-10.4 {Check description use 1 sender 2 destinaton topology} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    # sender
    set e1 [java::new ptolemy.actor.AtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.IOPort $e1 P1 false true]
    # receiver 1
    set e2 [java::new ptolemy.actor.AtomicActor $e0 E2]
    set p2 [java::new ptolemy.actor.IOPort $e2 P2 true false]
    # receiver 2
    set e3 [java::new ptolemy.actor.AtomicActor $e0 E3]
    set p3 [java::new ptolemy.actor.IOPort $e3 P3 true false]
    # connection
    set r1 [java::new ptolemy.actor.IORelation $e0 R1]
    $p1 link $r1
    $p2 link $r1
    $p3 link $r1

    # Call preinitialize on the director so that the receivers get created
    # added Neil Smyth. Need to call this as receivers are no longer 
    # created on the fly.
    $director preinitialize

    list "[$p1 description $detail]\n[$p2 description $detail]\n[$p3 description $detail]"
} {{ptolemy.actor.IOPort {..E1.P1} receivers {
} remotereceivers {
    {
        {ptolemy.actor.Mailbox in ..E2.P2}
        {ptolemy.actor.Mailbox in ..E3.P3}
    }
}
ptolemy.actor.IOPort {..E2.P2} receivers {
    {
        {ptolemy.actor.Mailbox}
    }
} remotereceivers {
}
ptolemy.actor.IOPort {..E3.P3} receivers {
    {
        {ptolemy.actor.Mailbox}
    }
} remotereceivers {
}}}

test IOPort-10.5 {Check description use multi-output port} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    # sender
    set e1 [java::new ptolemy.actor.AtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.IOPort $e1 P1 false true]
    $p1 setMultiport true
    # receiver 1
    set e2 [java::new ptolemy.actor.AtomicActor $e0 E2]
    set p2 [java::new ptolemy.actor.IOPort $e2 P2 true false]
    # receiver 2
    set e3 [java::new ptolemy.actor.AtomicActor $e0 E3]
    set p3 [java::new ptolemy.actor.IOPort $e3 P3 true false]
    # connection
    set r1 [java::new ptolemy.actor.IORelation $e0 R1]
    $p1 link $r1
    $p2 link $r1
    set r2 [java::new ptolemy.actor.IORelation $e0 R2]
    $p1 link $r2
    $p3 link $r2

    # Call preinitialize on the director so that the receivers get created
    # added Neil Smyth. Need to call this as receivers are no longer 
    # created on the fly.
    $director preinitialize

    list "[$p1 description $detail]\n[$p2 description $detail]\n[$p3 description $detail]"
} {{ptolemy.actor.IOPort {..E1.P1} receivers {
} remotereceivers {
    {
        {ptolemy.actor.Mailbox in ..E2.P2}
    }
    {
        {ptolemy.actor.Mailbox in ..E3.P3}
    }
}
ptolemy.actor.IOPort {..E2.P2} receivers {
    {
        {ptolemy.actor.Mailbox}
    }
} remotereceivers {
}
ptolemy.actor.IOPort {..E3.P3} receivers {
    {
        {ptolemy.actor.Mailbox}
    }
} remotereceivers {
}}}

test IOPort-10.6 {Check description use the example (that used to be) in design doc} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e1 [java::new ptolemy.actor.CompositeActor $e0 E1]
    set e2 [java::new ptolemy.actor.AtomicActor $e1 E2]
    set p1 [java::new ptolemy.actor.IOPort $e2 P1 false true]
    $p1 setMultiport true
    set p2 [java::new ptolemy.actor.IOPort $e1 P2 false true]
    $p2 setMultiport true
    set p3 [java::new ptolemy.actor.IOPort $e1 P3 false true]
    $p3 setMultiport true
    set p4 [java::new ptolemy.actor.IOPort $e1 P4 false true]
    $p4 setMultiport true
    set e3 [java::new ptolemy.actor.CompositeActor $e0 E3]
    set e4 [java::new ptolemy.actor.AtomicActor $e3 E4]
    set e5 [java::new ptolemy.actor.AtomicActor $e3 E5]
    # set p5 without specify input/output
    set p5 [java::new ptolemy.actor.IOPort $e3 P5]
    $p5 setMultiport true
    set p6 [java::new ptolemy.actor.IOPort $e3 P6 true false]
    set p8 [java::new ptolemy.actor.IOPort $e4 P8 true false]
    $p8 setMultiport true
    set p9 [java::new ptolemy.actor.IOPort $e5 P9 true false]
    $p9 setMultiport true

    set e6 [java::new ptolemy.actor.AtomicActor $e0 E6]
    set p7 [java::new ptolemy.actor.IOPort $e6 P7 true false]
    $p7 setMultiport true

    set e7 [java::new ptolemy.actor.AtomicActor $e0 E7]
    set p10 [java::new ptolemy.actor.IOPort $e7 P10 false true]
    $p10 setMultiport true

    # connection
    set r1 [java::new ptolemy.actor.IORelation $e1 R1]
    $r1 setWidth 0
    $p1 link $r1
    $p2 link $r1
    $p3 link $r1
    $p4 link $r1
    set r2 [java::new ptolemy.actor.IORelation $e0 R2]
    $r2 setWidth 3
    $p2 link $r2
    $p5 link $r2
    set r3 [java::new ptolemy.actor.IORelation $e0 R3]
    $p2 link $r3
    $p5 link $r3
    $p6 link $r3
    set r4 [java::new ptolemy.actor.IORelation $e0 R4]
    $r4 setWidth 2
    $p3 link $r4
    $p7 link $r4
    $p10 link $r4
    set r5 [java::new ptolemy.actor.IORelation $e3 R5]
    $r5 setWidth 0
    $p5 link $r5
    $p8 link $r5
    set r6 [java::new ptolemy.actor.IORelation $e3 R6]
    $p5 link $r6
    $p9 link $r6
    set r7 [java::new ptolemy.actor.IORelation $e3 R7]
    $p6 link $r7
    $p9 link $r7
    set r8 [java::new ptolemy.actor.IORelation $e0 R8]
    $p3 link $r8

    # Call preinitialize on the director so that the receivers get created
    # added Neil Smyth. Need to call this as receivers are no longer 
    # created on the fly.
    $director preinitialize

    list "[$p1 description $detail]\n[$p10 description $detail]"
} {{ptolemy.actor.IOPort {..E1.E2.P1} receivers {
} remotereceivers {
    {
        {ptolemy.actor.Mailbox in ..E3.E4.P8}
        {ptolemy.actor.Mailbox in ..E6.P7}
    }
    {
        {ptolemy.actor.Mailbox in ..E3.E4.P8}
        {ptolemy.actor.Mailbox in ..E6.P7}
    }
    {
        {ptolemy.actor.Mailbox in ..E3.E4.P8}
    }
    {
        {ptolemy.actor.Mailbox in ..E3.E5.P9}
        {ptolemy.actor.Mailbox in ..E3.E5.P9}
    }
}
ptolemy.actor.IOPort {..E7.P10} receivers {
} remotereceivers {
    {
        {ptolemy.actor.Mailbox in ..E6.P7}
    }
    {
        {ptolemy.actor.Mailbox in ..E6.P7}
    }
}}}

test IOPort-10.7 {Construct a simple system, then call description} {
    set container [java::new ptolemy.actor.TypedCompositeActor]
    set source [java::new ptolemy.actor.TypedAtomicActor $container source]
    set dest [java::new ptolemy.actor.TypedAtomicActor $container dest]
    $source newPort output
    $dest newPort input
    set output [java::cast ptolemy.actor.IOPort [$source getPort output]]
    $output setOutput true
    set input [java::cast ptolemy.actor.IOPort [$dest getPort input]]
    $input setInput true
    $container connect $output $input edge0
    $container description
} {ptolemy.actor.TypedCompositeActor {.} attributes {
    {ptolemy.kernel.util.SingletonConfigurableAttribute {.._iconDescription} attributes {
    }}
} ports {
} classes {
} entities {
    {ptolemy.actor.TypedAtomicActor {..source} attributes {
        {ptolemy.kernel.util.SingletonConfigurableAttribute {..source._iconDescription} attributes {
        }}
    } ports {
        {ptolemy.actor.TypedIOPort {..source.output} attributes {
        } links {
            {ptolemy.actor.TypedIORelation {..edge0} attributes {
            } configuration {width 1 fixed}}
        } insidelinks {
        } configuration {output opaque {width 1}} receivers {
        } remotereceivers {
            {
            }
        } type {declared unknown resolved unknown}}
    }}
    {ptolemy.actor.TypedAtomicActor {..dest} attributes {
        {ptolemy.kernel.util.SingletonConfigurableAttribute {..dest._iconDescription} attributes {
        }}
    } ports {
        {ptolemy.actor.TypedIOPort {..dest.input} attributes {
        } links {
            {ptolemy.actor.TypedIORelation {..edge0} attributes {
            } configuration {width 1 fixed}}
        } insidelinks {
        } configuration {input opaque {width 1}} receivers {
            {
            }
        } remotereceivers {
        } type {declared unknown resolved unknown}}
    }}
} relations {
    {ptolemy.actor.TypedIORelation {..edge0} attributes {
    } links {
        {ptolemy.actor.TypedIOPort {..source.output} attributes {
        } configuration {output opaque {width 1}} receivers {
        } remotereceivers {
            {
            }
        } type {declared unknown resolved unknown}}
        {ptolemy.actor.TypedIOPort {..dest.input} attributes {
        } configuration {input opaque {width 1}} receivers {
            {
            }
        } remotereceivers {
        } type {declared unknown resolved unknown}}
    } configuration {width 1 fixed}}
}}


######################################################################
####
#   Check liberalLink with galaxy

test IOPort-11.1 {Check liberalLink on transparent multiport and inferred width} {
    set ex [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set e0 [java::new ptolemy.actor.CompositeActor $ex E0]
    # transparent port
    set p0 [java::new ptolemy.actor.IOPort $e0 P0 false true]
    $p0 setMultiport true
    # inside entity
    set e1 [java::new ptolemy.actor.AtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.IOPort $e1 P1 false true]
    $p1 setMultiport true
    # outside entoty
    set e2 [java::new ptolemy.actor.AtomicActor $e0 E2]
    set p2 [java::new ptolemy.actor.IOPort $e2 P2 true false]
    $p2 setMultiport true
    # connection
    # inside relation with unspecified width
    set r1 [java::new ptolemy.actor.IORelation $e0 R1]
    $r1 setWidth 0
    $p1 link $r1
    $p0 link $r1
    # outside relation
    set r2 [java::new ptolemy.actor.IORelation $e0 R2]
    $r2 setWidth 3
    $p0 link $r2
    $p2 link $r2
    list [$p0 getWidth] [$p1 getWidth] [$p2 getWidth]
} {0 1 3}

# NOTE: Builds on previous example.
test IOPort-11.15 {Check inferred width} {
    set r3 [java::new ptolemy.actor.IORelation $ex R3]
    $r3 setWidth 3
    $p0 link $r3
    set result [list [$p0 getWidth] [$p1 getWidth] [$p2 getWidth]]
    $r3 setWidth 4
    lappend result [$p0 getWidth] [$p1 getWidth] [$p2 getWidth]
    $r3 setWidth 5
    lappend result [$p0 getWidth] [$p1 getWidth] [$p2 getWidth]
} {3 1 3 4 1 3 5 2 3}

test IOPort-11.2 {Check liberalLink: link a linked relation from inside } {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set p0 [java::new ptolemy.actor.IOPort $e0 P0 false true]
    $p0 setMultiport true
    set r1 [java::new ptolemy.actor.IORelation $e0 R1]
    $r1 setWidth 1
    $p0 link $r1
    catch {$p0 link $r1} msg1
    list $msg1
} {{}}

test IOPort-11.3 {Check liberalLink multi-*-relation from inside } {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set p0 [java::new ptolemy.actor.IOPort $e0 P0 false true]
    $p0 setMultiport true
    set r1 [java::new ptolemy.actor.IORelation $e0 R1]
    $r1 setWidth 0
    $p0 link $r1
    set r2 [java::new ptolemy.actor.IORelation $e0 R2]
    $r2 setWidth 0
    catch {$p0 link $r2} msg1
    list $msg1
} {{ptolemy.kernel.util.IllegalActionException: Attempt to link a second bus relation with unspecified width to the inside of a port.
  in .<Unnamed Object>.P0 and .<Unnamed Object>.R2}}

test IOPort-11.4 {Check liberalLink multi-*-relation from outside } {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set p0 [java::new ptolemy.actor.IOPort $e0 P0 false true]
    $p0 setMultiport true
    # inside relation, fixed width
    set r1 [java::new ptolemy.actor.IORelation $e0 R1]
    $r1 setWidth 3
    $p0 link $r1
    # ourside relation, *
    set r2 [java::new ptolemy.actor.IORelation]
    $r2 setName R2
    $r2 setWidth 0
    $p0 link $r2
    set r3 [java::new ptolemy.actor.IORelation]
    $r3 setName R3
    $r3 setWidth 0
    catch {$p0 link $r3} msg1
    list $msg1
} {{ptolemy.kernel.util.IllegalActionException: Attempt to link a second bus relation with unspecified width to the outside of a port.
  in .<Unnamed Object>.P0 and .R3}}

test IOPort-11.5 {Check liberalLink *-relation from both inside and outside } {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set p0 [java::new ptolemy.actor.IOPort $e0 P0 false true]
    $p0 setMultiport true
    # inside relation, *
    set r1 [java::new ptolemy.actor.IORelation $e0 R1]
    $r1 setWidth 0
    $p0 link $r1
    # ourside relation, *
    set r2 [java::new ptolemy.actor.IORelation]
    $r2 setName R2
    $r2 setWidth 0
    catch {$p0 link $r2} msg1
    list $msg1
} {{}}

test IOPort-11.6 {Check cannot link a relation twice to a single port} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    set p0 [java::new ptolemy.actor.IOPort $e0 P0 false true]
    set r1 [java::new ptolemy.actor.IORelation $e0 R1]
    $p0 setMultiport false
    $r1 setWidth 1
    $p0 link $r1
    catch {$p0 link $r1} msg1
    list $msg1
} {{ptolemy.kernel.util.IllegalActionException: Attempt to link more than one relation to a single port.
  in .<Unnamed Object>.P0 and .<Unnamed Object>.R1}}

######################################################################
####
# Example similar to figure of design document.
test IOPOrt-12.1 {deepConnectedIn(out)Ports} {
    # Create objects
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    $e0 setName E0
    set e1 [java::new ptolemy.actor.AtomicActor $e0 "E1"]
    set e2 [java::new ptolemy.actor.CompositeActor $e0 "E2"]
    set e3 [java::new ptolemy.actor.AtomicActor $e2 "E3"]
    set e4 [java::new ptolemy.actor.AtomicActor $e0 "E4"]
    set p1 [java::new ptolemy.actor.IOPort $e1 "P1"]
    set p2 [java::new ptolemy.actor.IOPort $e2 "P2"]
    set p3 [java::new ptolemy.actor.IOPort $e3 "P3"]
    set p4 [java::new ptolemy.actor.IOPort $e2 "P4"]
    set p5 [java::new ptolemy.actor.IOPort $e4 "P5"]
    set r1 [java::new ptolemy.actor.IORelation $e0 "R1"]
    set r2 [java::new ptolemy.actor.IORelation $e2 "R2"]
    set r3 [java::new ptolemy.actor.IORelation $e0 "R3"]

    # Connect
    $p1 link $r1
    $p2 link $r1
    $p2 link $r2
    $p3 link $r2
    $p4 link $r2
    $p4 link $r3
    $p5 link $r3

    # make P1, P3 output, P5 input
    $p1 setInput false
    $p1 setOutput true
    $p3 setInput false
    $p3 setOutput true
    $p5 setInput true
    $p5 setOutput false

    list [enumToNames [$p1 deepConnectedInPorts]] \
            [enumToNames [$p1 deepConnectedOutPorts]] \
            [enumToNames [$p2 deepConnectedInPorts]] \
            [enumToNames [$p2 deepConnectedOutPorts]] \
            [enumToNames [$p3 deepConnectedInPorts]] \
            [enumToNames [$p3 deepConnectedOutPorts]] \
            [enumToNames [$p4 deepConnectedInPorts]] \
            [enumToNames [$p4 deepConnectedOutPorts]] \
            [enumToNames [$p5 deepConnectedInPorts]] \
            [enumToNames [$p5 deepConnectedOutPorts]]
} {P5 P3 {} P1 P5 P1 P5 {} {} {P1 P3}}

# NOTE: Uses topology built in 12.1
test IOPort-12.2 {deepConnectedIn(Out)Ports} {

    # make P1 output, P3, P5 input
    $p1 setInput false
    $p1 setOutput true
    $p3 setInput true
    $p3 setOutput false
    $p5 setInput true
    $p5 setOutput false

    list [enumToNames [$p1 deepConnectedInPorts]] \
            [enumToNames [$p1 deepConnectedOutPorts]] \
            [enumToNames [$p2 deepConnectedInPorts]] \
            [enumToNames [$p2 deepConnectedOutPorts]] \
            [enumToNames [$p3 deepConnectedInPorts]] \
            [enumToNames [$p3 deepConnectedOutPorts]] \
            [enumToNames [$p4 deepConnectedInPorts]] \
            [enumToNames [$p4 deepConnectedOutPorts]] \
            [enumToNames [$p5 deepConnectedInPorts]] \
            [enumToNames [$p5 deepConnectedOutPorts]]
} {{P3 P5} {} {} P1 P5 P1 P5 {} P3 P1}

# NOTE: Uses topology built in 12.1
test IOPort-12.3 {deepConnectedIn(Out)Ports} {

    # make P3 output, P1, P5 input
    $p1 setInput true
    $p1 setOutput false
    $p3 setInput false
    $p3 setOutput true
    $p5 setInput true
    $p5 setOutput false

    list [enumToNames [$p1 deepConnectedInPorts]] \
            [enumToNames [$p1 deepConnectedOutPorts]] \
            [enumToNames [$p2 deepConnectedInPorts]] \
            [enumToNames [$p2 deepConnectedOutPorts]] \
            [enumToNames [$p3 deepConnectedInPorts]] \
            [enumToNames [$p3 deepConnectedOutPorts]] \
            [enumToNames [$p4 deepConnectedInPorts]] \
            [enumToNames [$p4 deepConnectedOutPorts]] \
            [enumToNames [$p5 deepConnectedInPorts]] \
            [enumToNames [$p5 deepConnectedOutPorts]]
} {P5 P3 P1 {} {P1 P5} {} P5 {} P1 P3}

###################################################################
##  Opaque output port is not returned bu deepConnectedInports
#
test IOPOrt-12.4 {deepConnectedInPorts from a inside outputport} {
    # Create objects
    set e0 [java::new ptolemy.actor.CompositeActor]
    set exedir [java::new ptolemy.actor.Director]
    $e0 setDirector $exedir
    $e0 setManager $manager
    $e0 setName E0
    set e1 [java::new ptolemy.actor.CompositeActor $e0 "E1"]
    set e2 [java::new ptolemy.actor.AtomicActor $e1 "E2"]
    set e3 [java::new ptolemy.actor.AtomicActor $e0 "E3"]
    $e1 setDirector $director

    set p1 [java::new ptolemy.actor.IOPort $e2 "P1"]
    set p2 [java::new ptolemy.actor.IOPort $e1 "P2"]
    set p3 [java::new ptolemy.actor.IOPort $e3 "P3"]

    set r1 [java::new ptolemy.actor.IORelation $e1 "R1"]
    set r2 [java::new ptolemy.actor.IORelation $e0 "R2"]

    # Connect
    $p1 link $r1
    $p2 link $r1
    $p2 link $r2
    $p3 link $r2
    

    # make P1, P3 output, P5 input
    $p1 setInput false
    $p1 setOutput true
    $p3 setInput true
    $p3 setOutput false

    list [enumToNames [$p1 deepConnectedInPorts]] 
} {{}}

######################################################################
####
# Test getReceivers() on transparent ports.
# In particular, test the following comment:
#      *  For a transparent port (a port of a non-atomic entity), this method
#      *  returns the receivers in ports connected to this port on the inside.
#      *  For an opaque port, the receivers returned are contained directly by
#      *  this port.
#      *  <p>
#      *  The number of channels (rows) is the width of the port.
#      *  If the width is zero, then this method will return null,
#      *  as if the port were not an input port.
# The test uses the description method to represent the results.

test IOPort-13.1 {test getReceivers()} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    $e0 setName E0
    set e1 [java::new ptolemy.actor.CompositeActor $e0 "E1"]
    set e2 [java::new ptolemy.actor.CompositeActor $e0 "E2"]
    set e3 [java::new ptolemy.actor.AtomicActor $e1 "E3"]
    set e4 [java::new ptolemy.actor.AtomicActor $e2 "E4"]
    set p1 [java::new ptolemy.actor.IOPort $e1 "P1"]
    set p2 [java::new ptolemy.actor.IOPort $e2 "P2"]
    set p3 [java::new ptolemy.actor.IOPort $e3 "P3"]
    set p4 [java::new ptolemy.actor.IOPort $e4 "P4"]
    $p1 setMultiport true
    $p2 setMultiport true
    $p3 setMultiport true
    $p3 setOutput true
    $p4 setMultiport true
    $p4 setInput true
    set r1 [java::cast ptolemy.actor.IORelation [$e0 connect $p1 $p2]]
    $r1 setWidth 3
    set r2 [java::cast ptolemy.actor.IORelation [$e1 connect $p3 $p1]]
    $r2 setWidth 2
    set r3 [java::cast ptolemy.actor.IORelation [$e2 connect $p2 $p4]]
    $r3 setWidth 4

    # Call preinitialize on the director so that the receivers get created
    # added Neil Smyth. Need to call this as receivers are no longer 
    # created on the fly.
    $e0 preinitialize
    
    set receivers [java::field ptolemy.actor.IOPort RECEIVERS]
    set remotereceivers [java::field ptolemy.actor.IOPort REMOTERECEIVERS]
    $p2 description $receivers
} {receivers {
    {
        {ptolemy.actor.Mailbox}
    }
    {
        {ptolemy.actor.Mailbox}
    }
    {
        {ptolemy.actor.Mailbox}
    }
    {
        {ptolemy.actor.Mailbox}
    }
}}

test IOPort-13.2 {test getReceivers()} {
    # NOTE: Uses setup in previous test.
    $p4 description $receivers
} {receivers {
    {
        {ptolemy.actor.Mailbox}
    }
    {
        {ptolemy.actor.Mailbox}
    }
    {
        {ptolemy.actor.Mailbox}
    }
    {
        {ptolemy.actor.Mailbox}
    }
}}

test IOPort-13.3 {test getReceivers()} {
    # NOTE: Uses setup in previous test.
    $p1 description $receivers
} {receivers {
}}

test IOPort-13.4 {test getReceivers()} {
    # NOTE: Uses setup in previous test.
    $p3 description $receivers
} {receivers {
}}

######################################################################
####
# Test getRemoteReceivers() on transparent ports.
# NOTE: Uses the same setup from the previous batch of tests.

test IOPort-14.1 {test getRemoteReceivers()} {
    # NOTE: Uses setup in previous test.
    $p1 description $remotereceivers
} {remotereceivers {
    {
        {ptolemy.actor.Mailbox in .E0.E2.E4.P4}
    }
    {
        {ptolemy.actor.Mailbox in .E0.E2.E4.P4}
    }
    {
        {ptolemy.actor.Mailbox in .E0.E2.E4.P4}
    }
}}

test IOPort-14.2 {test getRemoteReceivers()} {
    # NOTE: Uses setup in previous test.
    $p3 description $remotereceivers
} {remotereceivers {
    {
        {ptolemy.actor.Mailbox in .E0.E2.E4.P4}
    }
    {
        {ptolemy.actor.Mailbox in .E0.E2.E4.P4}
    }
}}

test IOPort-14.3 {test getRemoteReceivers()} {
    # NOTE: Uses setup in previous test.
    $p2 description $remotereceivers
} {remotereceivers {
}}

test IOPort-14.3 {test getRemoteReceivers()} {
    # NOTE: Uses setup in previous test.
    $p4 description $remotereceivers
} {remotereceivers {
}}

######################################################################
####
# Test clone().
# NOTE: Uses the same setup from the previous batch of tests.

test IOPort-15.1 {test clone()} {
    # NOTE: Uses setup in previous test.
    set w [java::new ptolemy.kernel.util.Workspace W]
    set p5 [java::cast ptolemy.actor.IOPort [$p4 clone $w]]
    $p5 description
} {ptolemy.actor.IOPort {.P4} attributes {
} links {
} insidelinks {
} configuration {input multiport opaque {width 0}} receivers {
} remotereceivers {
}}

test IOPort-16.1 {test opaque deepInsidePorts} {
    set e1 [java::new ptolemy.actor.CompositeActor]
    $e1 setName E1
    set d1 [java::new ptolemy.actor.Director $e1 D1]
    set p1 [java::new ptolemy.actor.IOPort $e1 P1]
    set e2 [java::new ptolemy.actor.CompositeActor $e1 E2]
    set d2 [java::new ptolemy.actor.Director $e2 D2]
    set p2 [java::new ptolemy.actor.IOPort $e2 P2]
    set e3 [java::new ptolemy.actor.AtomicActor $e2 E3]
    set p3 [java::new ptolemy.actor.IOPort $e3 P3]

    set r1 [java::new ptolemy.actor.IORelation $e1 R1]
    $p1 link $r1
    $p2 link $r1
    set r2 [java::new ptolemy.actor.IORelation $e2 R2]
    $p2 link $r2
    $p3 link $r2

    list [enumToFullNames [$p1 deepInsidePorts]] \
            [enumToFullNames [$p2 deepInsidePorts]]
} {.E1.E2.P2 .E1.E2.E3.P3}

test IOPort-17.1 {test sourcePortList} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setName E0
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 E1]
    set e2 [java::new ptolemy.actor.TypedAtomicActor $e0 E2]
    set p1 [java::new ptolemy.actor.TypedIOPort $e1 P1]
    set p2 [java::new ptolemy.actor.TypedIOPort $e2 P2]
    $p1 setOutput true
    $p2 setInput true
    set r [java::new ptolemy.actor.TypedIORelation $e0 R0]
    $p1 link $r
    $p2 link $r
    set d [java::new ptolemy.actor.Director $e0 D0]
    # p1 should be in p2's sourcePortList
    listToFullNames [$p2 sourcePortList]
} {.E0.E1.P1}

test IOPort-17.1.1 {test numberOfSources} {
    list [$p1 numberOfSources] [$p2 numberOfSources]
} {0 1}

test IOPort-17.2 {test sourcePortList} {
    # NOTE: expands on the above.
    set e3 [java::new ptolemy.actor.TypedAtomicActor $e0 E3]
    set p3 [java::new ptolemy.actor.TypedIOPort $e3 P3]
    $p3 setOutput true
    $p3 link $r
    listToFullNames [$p2 sourcePortList]
} {.E0.E1.P1 .E0.E3.P3}

test IOPort-17.2.1 {test numberOfSources} {
    $p2 numberOfSources
} {2}

test IOPort-17.3 {test sourcePortList} {
    # NOTE: expands on the above.
    set e4 [java::new ptolemy.actor.TypedCompositeActor $e0 E4]
    set p4 [java::new ptolemy.actor.TypedIOPort $e4 P4]
    $p4 setOutput true
    $p4 link $r
    listToFullNames [$p2 sourcePortList]
} {.E0.E1.P1 .E0.E3.P3}

test IOPort-17.3.1 {test numberOfSources} {
    $p2 numberOfSources
} {2}

test IOPort-17.4 {test sourcePortList} {
    # NOTE: expands on the above.
    set e5 [java::new ptolemy.actor.TypedAtomicActor $e4 E5]
    set p5 [java::new ptolemy.actor.TypedIOPort $e5 P5]
    $p5 setOutput true
    set r5 [java::new ptolemy.actor.TypedIORelation $e4 R5]
    $p5 link $r5
    $p4 link $r5
    listToFullNames [$p2 sourcePortList]
} {.E0.E1.P1 .E0.E3.P3 .E0.E4.E5.P5}

test IOPort-17.4.1 {test numberOfSources} {
    $p2 numberOfSources
} {3}

test IOPort-18.1 {test sourcePortList} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setName E0
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 E1]
    set e2 [java::new ptolemy.actor.TypedCompositeActor $e0 E2]
    set p1 [java::new ptolemy.actor.TypedIOPort $e1 P1]
    set p2 [java::new ptolemy.actor.TypedIOPort $e2 P2]
    $p1 setOutput true
    $p2 setInput true
    set r [java::new ptolemy.actor.TypedIORelation $e0 R0]
    $p1 link $r
    $p2 link $r
    set d [java::new ptolemy.actor.Director $e0 D0]
    set e3 [java::new ptolemy.actor.TypedAtomicActor $e2 E3]
    set p3 [java::new ptolemy.actor.TypedIOPort $e3 P3]
    $p3 setInput true
    set r2 [java::new ptolemy.actor.TypedIORelation $e2 R2]
    $p3 link $r2
    $p2 link $r2
    listToFullNames [$p3 sourcePortList]
} {.E0.E1.P1}

test IOPort-18.1.1 {test numberOfSources} {
    $p3 numberOfSources
} {1}


test IOPort-18.2 {test sourcePortList} {
    # NOTE: Builds on the above.
    set d2 [java::new ptolemy.actor.Director $e2 D2]
    listToFullNames [$p3 sourcePortList]
} {.E0.E2.P2}

test IOPort-18.2.1 {test numberOfSources} {
    $p3 numberOfSources
} {1}

test IOPort-19.1 {test sinkPortList} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setName E0
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 E1]
    set e2 [java::new ptolemy.actor.TypedAtomicActor $e0 E2]
    set p1 [java::new ptolemy.actor.TypedIOPort $e1 P1]
    set p2 [java::new ptolemy.actor.TypedIOPort $e2 P2]
    $p1 setOutput true
    $p2 setInput true
    set r [java::new ptolemy.actor.TypedIORelation $e0 R0]
    $p1 link $r
    $p2 link $r
    set d [java::new ptolemy.actor.Director $e0 D0]
    listToFullNames [$p1 sinkPortList]
} {.E0.E2.P2}

test IOPort-19.1.1 {test numberOfSinks} {
    list [$p1 numberOfSinks] [$p2 numberOfSinks]
} {1 0}

test IOPort-19.2 {test sinkPortList} {
    # NOTE: expands on the above.
    set e3 [java::new ptolemy.actor.TypedAtomicActor $e0 E3]
    set p3 [java::new ptolemy.actor.TypedIOPort $e3 P3]
    $p3 setInput true
    $p3 link $r
    listToFullNames [$p1 sinkPortList]
} {.E0.E2.P2 .E0.E3.P3}

test IOPort-19.2.1 {test numberOfSinks} {
    $p1 numberOfSinks
} {2}

test IOPort-19.3 {test sinkPortList} {
    # NOTE: expands on the above.
    set e4 [java::new ptolemy.actor.TypedCompositeActor $e0 E4]
    set p4 [java::new ptolemy.actor.TypedIOPort $e4 P4]
    $p4 setInput true
    $p4 link $r
    listToFullNames [$p1 sinkPortList]
} {.E0.E2.P2 .E0.E3.P3}

test IOPort-19.3.1 {test numberOfSinks} {
    $p1 numberOfSinks
} {2}

test IOPort-19.4 {test sinkPortList} {
    # NOTE: expands on the above.
    set e5 [java::new ptolemy.actor.TypedAtomicActor $e4 E5]
    set p5 [java::new ptolemy.actor.TypedIOPort $e5 P5]
    $p5 setInput true
    set r5 [java::new ptolemy.actor.TypedIORelation $e4 R5]
    $p5 link $r5
    $p4 link $r5
    listToFullNames [$p1 sinkPortList]
} {.E0.E2.P2 .E0.E3.P3 .E0.E4.E5.P5}

test IOPort-19.4.1 {test numberOfSinks} {
    $p1 numberOfSinks
} {3}

test IOPort-20.1 {test sinkPortList} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setName E0
    set e1 [java::new ptolemy.actor.TypedCompositeActor $e0 E1]
    set e2 [java::new ptolemy.actor.TypedAtomicActor $e0 E2]
    set p1 [java::new ptolemy.actor.TypedIOPort $e1 P1]
    set p2 [java::new ptolemy.actor.TypedIOPort $e2 P2]
    $p1 setOutput true
    $p2 setInput true
    set r [java::new ptolemy.actor.TypedIORelation $e0 R0]
    $p1 link $r
    $p2 link $r
    set d [java::new ptolemy.actor.Director $e0 D0]
    set e3 [java::new ptolemy.actor.TypedAtomicActor $e1 E3]
    set p3 [java::new ptolemy.actor.TypedIOPort $e3 P3]
    $p3 setOutput true
    set r2 [java::new ptolemy.actor.TypedIORelation $e1 R2]
    $p3 link $r2
    $p1 link $r2
    listToFullNames [$p3 sinkPortList]
} {.E0.E2.P2}

test IOPort-20.1.1 {test numberOfSinks} {
    $p3 numberOfSinks
} {1}

test IOPort-20.2 {test sinkPortList} {
    # NOTE: Builds on the above.
    set d2 [java::new ptolemy.actor.Director $e1 D2]
    listToFullNames [$p3 sinkPortList]
} {.E0.E1.P1}

test IOPort-20.2.1 {test numberOfSinks} {
    $p3 numberOfSinks
} {1}

test IOPort-21.1 {test insideSinkPortList} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setName E0
    set e1 [java::new ptolemy.actor.TypedCompositeActor $e0 E1]
    set e2 [java::new ptolemy.actor.TypedAtomicActor $e1 E2]
    set p0 [java::new ptolemy.actor.TypedIOPort $e0 P1]
    set p1 [java::new ptolemy.actor.TypedIOPort $e1 P1]
    set p2 [java::new ptolemy.actor.TypedIOPort $e2 P2]
    $p0 setInput true
    $p1 setInput true
    $p2 setInput true
    set r0 [java::new ptolemy.actor.TypedIORelation $e0 R0]
    set r1 [java::new ptolemy.actor.TypedIORelation $e1 R1]
    $p0 link $r0
    $p1 link $r0
    $p1 link $r1
    $p2 link $r1
    set d [java::new ptolemy.actor.Director $e0 D0]
    list [listToFullNames [$p0 insideSinkPortList]] [listToFullNames [$p0 insideSourcePortList]]
} {.E0.E1.E2.P2 {}}

test IOPort-21.2 {test insideSinkPortList} {
    set d1 [java::new ptolemy.actor.Director $e1 D1]
    list [listToFullNames [$p0 insideSinkPortList]] [listToFullNames [$p0 insideSourcePortList]] [listToFullNames [$p1 insideSinkPortList]] [listToFullNames [$p1 insideSourcePortList]]
} {.E0.E1.P1 {} .E0.E1.E2.P2 {}}

test IOPort-21.3 {test insideSourcePortList} {
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setName E0
    set e1 [java::new ptolemy.actor.TypedCompositeActor $e0 E1]
    set e2 [java::new ptolemy.actor.TypedAtomicActor $e1 E2]
    set p0 [java::new ptolemy.actor.TypedIOPort $e0 P0]
    set p1 [java::new ptolemy.actor.TypedIOPort $e1 P1]
    set p2 [java::new ptolemy.actor.TypedIOPort $e2 P2]
    $p0 setOutput true
    $p1 setOutput true
    $p2 setOutput true
    set r0 [java::new ptolemy.actor.TypedIORelation $e0 R0]
    set r1 [java::new ptolemy.actor.TypedIORelation $e1 R1]
    $p0 link $r0
    $p1 link $r0
    $p1 link $r1
    $p2 link $r1
    set d [java::new ptolemy.actor.Director $e0 D0]
    list [listToFullNames [$p0 insideSinkPortList]] [listToFullNames [$p0 insideSourcePortList]]
} {{} .E0.E1.E2.P2}

test IOPort-21.4 {test insideSourcePortList} {
    set d1 [java::new ptolemy.actor.Director $e1 D1]
    list [listToFullNames [$p0 insideSinkPortList]] [listToFullNames [$p0 insideSourcePortList]] [listToFullNames [$p1 insideSinkPortList]] [listToFullNames [$p1 insideSourcePortList]]
} {{} .E0.E1.P1 {} .E0.E1.E2.P2}
