# Tests for the IOPort class
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
# FIXME: set the following test
# test IOPort-1.1 {Get information about an instance of IOPort} {
#     # If anything changes, we want to know about it so we can write tests.
#     set n [java::new pt.actor.IOPort]
#     list [getJavaInfo $n]
# } {{
# }}

# NOTE:  All of the following tests use this director,
# pretty much as a dummy.
set director [java::new pt.actor.Director]

######################################################################
####
#
test IOPort-2.1 {Construct Ports} {
    set e0 [java::new pt.actor.CompositeActor [java::null] $director]
    set e1 [java::new pt.actor.Actor $e0 E1]
    set p1 [java::new pt.actor.IOPort]
    set p2 [java::new pt.actor.IOPort $e1 P2]
    list [$p1 getFullName] [$p2 getFullName]
} {. ..E1.P2}

test IOPort-2.2 {Construct Ports} {
    set e0 [java::new pt.actor.CompositeActor [java::null] $director]
    set e1 [java::new pt.actor.Actor $e0 E1]
    set p1 [java::new pt.actor.IOPort $e1 P1]
    set p2 [java::new pt.actor.IOPort $e1 P2]
    list [$p1 getFullName] [$p2 getFullName]
} {..E1.P1 ..E1.P2}

test IOPort-2.3 {Attempt to set erroneous container} {
    set e0 [java::new pt.actor.CompositeActor [java::null] $director]
    set e1 [java::new pt.kernel.ComponentEntity $e0 E1]
    set p1 [java::new pt.actor.IOPort]
    catch {$p1 setContainer $e1} msg
    list $msg
} {{pt.kernel.util.IllegalActionException: ..E1 and .: IOPort can only be contained by Actor or CompositeActor}}

######################################################################
####
#
test IOPort-3.1 {Test input/output predicates} {
    set e0 [java::new pt.actor.CompositeActor [java::null] $director]
    set e1 [java::new pt.actor.Actor $e0 E1]
    set p1 [java::new pt.actor.IOPort $e1 P1]
    set p2 [java::new pt.actor.IOPort $e1 P2 true true]
    list [$p1 isInput] [$p1 isOutput] [$p2 isInput] [$p2 isOutput]
} {0 0 1 1}

test IOPort-3.2 {Test input/output changes} {
    set e0 [java::new pt.actor.CompositeActor [java::null] $director]
    set e1 [java::new pt.actor.Actor $e0 E1]
    set p1 [java::new pt.actor.IOPort $e1 P1 true true]
    set result [list [$p1 isInput] [$p1 isOutput]]
    $p1 makeInput false
    $p1 makeOutput false
    lappend result [$p1 isInput] [$p1 isOutput]
} {1 1 0 0}

test IOPort-3.3 {Test input/output predicates on transparent ports} {
    set e0 [java::new pt.actor.CompositeActor [java::null] $director]
    set e1 [java::new pt.actor.Actor $e0 E1]
    set p0 [java::new pt.actor.IOPort $e0 P0]
    set p1 [java::new pt.actor.IOPort $e1 P1 true true]
    set r1 [java::new pt.actor.IORelation $e0 R1]
    $p1 link $r1
    $p0 link $r1
    list [$p0 isInput] [$p0 isOutput]
} {1 1}

######################################################################
####
#
test IOPort-4.1 {Test multiport predicate} {
    set e0 [java::new pt.actor.CompositeActor [java::null] $director]
    set e1 [java::new pt.actor.Actor $e0 E1]
    set p0 [java::new pt.actor.IOPort $e0 P0]
    set p1 [java::new pt.actor.IOPort $e1 P1 true true]
    set r1 [java::new pt.actor.IORelation $e0 R1]
    $p1 link $r1
    $p0 link $r1
    $p1 makeMultiport true
    $p0 isMultiport
} {0}

test IOPort-4.2 {Test multiport predicate on transparent port} {
    set e0 [java::new pt.actor.CompositeActor [java::null] $director]
    set e1 [java::new pt.actor.Actor $e0 E1]
    set p1 [java::new pt.actor.IOPort $e1 P1 true false]
    set temp [$p1 isMultiport]
    $p1 makeMultiport true
    list $temp [$p1 isMultiport]
} {0 1}

######################################################################
####
#
test IOPort-5.1 {Test getWidth} {
    set e0 [java::new pt.actor.CompositeActor [java::null] $director]
    set e1 [java::new pt.actor.Actor $e0 E1]
    set p1 [java::new pt.actor.IOPort $e1 P1 true false]
    set r1 [java::new pt.actor.IORelation $e0 R1]
    set temp [$p1 getWidth]
    $p1 liberalLink $r1
    list $temp [$p1 getWidth]
} {0 1}

test IOPort-5.2 {Test getWidth} {
    set e0 [java::new pt.actor.CompositeActor [java::null] $director]
    set e1 [java::new pt.actor.Actor $e0 E1]
    set p1 [java::new pt.actor.IOPort $e1 P1 true true]
    set r1 [java::new pt.actor.IORelation $e0 R1]
    set r2 [java::new pt.actor.IORelation $e0 R2]
    $p1 makeMultiport true
    $p1 liberalLink $r1
    $p1 liberalLink $r2
    $p1 getWidth
} {2}

test IOPort-5.3 {Test getWidth} {
    set e0 [java::new pt.actor.CompositeActor [java::null] $director]
    set e1 [java::new pt.actor.Actor $e0 E1]
    set p1 [java::new pt.actor.IOPort $e1 P1 true true]
    set r1 [java::new pt.actor.IORelation $e0 R1]
    $r1 setWidth 2
    set r2 [java::new pt.actor.IORelation $e0 R2]
    $p1 makeMultiport true
    $p1 liberalLink $r1
    $p1 liberalLink $r2
    $p1 getWidth
} {3}

test IOPort-5.4 {Test getWidth after unlinking} {
    set e0 [java::new pt.actor.CompositeActor [java::null] $director]
    # sending entity
    set e1 [java::new pt.actor.Actor $e0 E1]
    set p1 [java::new pt.actor.IOPort $e1 P1 false true]
    # receiving entity
    set e2 [java::new pt.actor.Actor $e0 E2]
    set p2 [java::new pt.actor.IOPort $e2 P2 true false]
    # connection
    # Can't use this because it uses a plain relation.
    # set r1 [$e0 connect $p1 $p2 R1]
    set r1 [java::new pt.actor.IORelation $e0 R1]
    $p1 link $r1
    $p2 link $r1
    $p1 getRemoteReceivers
    $p2 unlink $r1
    list [$p1 getWidth] [$p2 getWidth]
} {1 0}

######################################################################
####
#
test IOPort-6.1 {Make sure multiple links not allowed on single ports} {
    set e0 [java::new pt.actor.CompositeActor [java::null] $director]
    set e1 [java::new pt.actor.Actor $e0 E1]
    set p1 [java::new pt.actor.IOPort $e1 P1 true true]
    set r1 [java::new pt.actor.IORelation $e0 R1]
    set r2 [java::new pt.actor.IORelation $e0 R2]
    $p1 liberalLink $r1
    catch {$p1 liberalLink $r2} msg
    list $msg
} {{pt.kernel.util.IllegalActionException: ..E1.P1 and ..R2: Attempt to link more than one relation to a single port.}}

######################################################################
####
#
test IOPort-7.1 {Check getReceivers on an unlinked port} {
    set e0 [java::new pt.actor.CompositeActor [java::null] $director]
    set e1 [java::new pt.actor.Actor $e0 E1]
    set p1 [java::new pt.actor.IOPort $e1 P1 true true]
    expr {[$p1 getReceivers] == [java::null]}
} {1}

test IOPort-7.2 {Check getReceivers} {
    set e0 [java::new pt.actor.CompositeActor [java::null] $director]
    set e1 [java::new pt.actor.Actor $e0 E1]
    set p1 [java::new pt.actor.IOPort $e1 P1 true true]
    set r1 [java::new pt.actor.IORelation $e0 R1]
    catch {$p1 getReceivers $r1} msg
    list $msg
} {{pt.kernel.util.IllegalActionException: ..E1.P1: getReceivers: Relation argument is not linked to me.}}

######################################################################
####
#
test IOPort-8.1 {Check getRemoteReceivers on a port with no links} {
    set e0 [java::new pt.actor.CompositeActor [java::null] $director]
    set e1 [java::new pt.actor.Actor $e0 E1]
    set p1 [java::new pt.actor.IOPort $e1 P1 true true]
    expr {[$p1 getRemoteReceivers] == [java::null]}
} {1}

test IOPort-8.2 {Check getRemoteReceivers on a port after unlinking} {
    set e0 [java::new pt.actor.CompositeActor [java::null] $director]
    # sending entity
    set e1 [java::new pt.actor.Actor $e0 E1]
    set p1 [java::new pt.actor.IOPort $e1 P1 false true]
    # receiving entity
    set e2 [java::new pt.actor.Actor $e0 E2]
    set p2 [java::new pt.actor.IOPort $e1 P2 true false]
    # connection
    # Can't use this because it uses a plain relation.
    # set r1 [$e0 connect $p1 $p2 R1]
    set r1 [java::new pt.actor.IORelation $e0 R1]
    $p1 link $r1
    $p2 link $r1
    $p1 getRemoteReceivers
    $p2 unlink $r1
    expr {[$p1 getRemoteReceivers] == [java::null]}
} {1}

######################################################################
####
#
test IOPort-9.1 {Check connectivity via send} {
    set e0 [java::new pt.actor.CompositeActor [java::null] $director]
    # sending entity
    set e1 [java::new pt.actor.Actor $e0 E1]
    set p1 [java::new pt.actor.IOPort $e1 P1 false true]
    # receiving entity
    set e2 [java::new pt.actor.Actor $e0 E2]
    set p2 [java::new pt.actor.IOPort $e1 P2 true false]
    # connection
    # Can't use this because it uses a plain relation.
    # set r1 [$e0 connect $p1 $p2 R1]
    set r1 [java::new pt.actor.IORelation $e0 R1]
    $p1 link $r1
    $p2 link $r1
    # token to send
    set token [java::new pt.data.StringToken foo]
    $p1 send 0 $token
    set received [$p2 get 0]
    $received toString
} {pt.data.StringToken(foo)}

test IOPort-9.2 {Check unlink and send to dangling relation} {
    set e0 [java::new pt.actor.CompositeActor [java::null] $director]
    # sending entity
    set e1 [java::new pt.actor.Actor $e0 E1]
    set p1 [java::new pt.actor.IOPort $e1 P1 false true]
    # receiving entity
    set e2 [java::new pt.actor.Actor $e0 E2]
    set p2 [java::new pt.actor.IOPort $e1 P2 true false]
    # connection
    # Can't use connect because it uses a plain relation.
    # set r1 [$e0 connect $p1 $p2 R1]
    set r1 [java::new pt.actor.IORelation $e0 R1]
    $p1 link $r1
    $p2 link $r1
    # unlink one end
    $p2 unlinkAll
    # token to send
    set token [java::new pt.data.StringToken foo]
    $p1 send 0 $token
    catch {$p2 get 0} msg
    list [$p2 getWidth] $msg
} {0 {pt.kernel.util.NoSuchItemException: ..E1.P2: get: channel index is out of range.}}

test IOPort-9.3 {Check unlink and get from unlinked port} {
    set e0 [java::new pt.actor.CompositeActor [java::null] $director]
    # sending entity
    set e1 [java::new pt.actor.Actor $e0 E1]
    set p1 [java::new pt.actor.IOPort $e1 P1 false true]
    # receiving entity
    set e2 [java::new pt.actor.Actor $e0 E2]
    set p2 [java::new pt.actor.IOPort $e1 P2 true false]
    # connection
    # Can't use connect because it uses a plain relation.
    # set r1 [$e0 connect $p1 $p2 R1]
    set r1 [java::new pt.actor.IORelation $e0 R1]
    $p1 link $r1
    $p2 link $r1
    # unlink one end
    $p1 unlink $r1
    # token to send
    set token [java::new pt.data.StringToken foo]
    catch {$p1 send 0 $token} msg1
    catch {$p2 get 0} msg2
    list [$p2 getWidth] $msg1 $msg2
} {1 {pt.kernel.util.IllegalActionException: ..E1.P1: send: channel index is out of range.} {pt.kernel.util.NoSuchItemException: ..E1.P2: Attempt to get data from an empty mailbox.}}

######################################################################
####
#   TEST description(). Note 7 = RECEIVERS; 8 = REMOTE_RECEIVERS
test IOPort-10.1 {Check description on a new IOPort} {
    set p0 [java::new pt.actor.IOPort]
    list [$p0 description 7] [$p0 description 8]
} {{null
} {null
}}

test IOPort-10.2 {Check description use test-7.1 topology} {
    set e0 [java::new pt.actor.CompositeActor [java::null] $director]
    set e1 [java::new pt.actor.Actor $e0 E1]
    set p1 [java::new pt.actor.IOPort $e1 P1 true true]
    list [$p1 description 7] [$p1 description 8]
} {{null
} {null
}}

test IOPort-10.3 {Check description use test-9.1 topology} {
    set e0 [java::new pt.actor.CompositeActor [java::null] $director]
    # sending entity
    set e1 [java::new pt.actor.Actor $e0 E1]
    set p1 [java::new pt.actor.IOPort $e1 P1 false true]
    # receiving entity
    set e2 [java::new pt.actor.Actor $e0 E2]
    set p2 [java::new pt.actor.IOPort $e1 P2 true false]
    # connection
    # Can't use this because it uses a plain relation.
    # set r1 [$e0 connect $p1 $p2 R1]
    set r1 [java::new pt.actor.IORelation $e0 R1]
    $p1 link $r1
    $p2 link $r1
    list [$p1 description 7] [$p1 description 8] [$p2 description 7] [$p2 description 8]
} {{null
} {..E1.P2.pt.actor.Mailbox
} {..E1.P2.pt.actor.Mailbox
} {null
}}

test IOPort-10.4 {Check description use 1 sender 2 destinaton topology} {
    set e0 [java::new pt.actor.CompositeActor [java::null] $director]
    # sender
    set e1 [java::new pt.actor.Actor $e0 E1]
    set p1 [java::new pt.actor.IOPort $e1 P1 false true]
    # receiver 1
    set e2 [java::new pt.actor.Actor $e0 E2]
    set p2 [java::new pt.actor.IOPort $e2 P2 true false]
    # receiver 2
    set e3 [java::new pt.actor.Actor $e0 E3]
    set p3 [java::new pt.actor.IOPort $e3 P3 true false]
    # connection
    set r1 [java::new pt.actor.IORelation $e0 R1]
    $p1 link $r1
    $p2 link $r1
    $p3 link $r1
    list [$p1 description 7] [$p1 description 8] [$p2 description 7] [$p2 description 8] [$p3 description 7] [$p3 description 8]
} {{null
} {..E2.P2.pt.actor.Mailbox ..E3.P3.pt.actor.Mailbox
} {..E2.P2.pt.actor.Mailbox
} {null
} {..E3.P3.pt.actor.Mailbox
} {null
}}

test IOPort-10.5 {Check description use multi-output port} {
    set e0 [java::new pt.actor.CompositeActor [java::null] $director]
    # sender
    set e1 [java::new pt.actor.Actor $e0 E1]
    set p1 [java::new pt.actor.IOPort $e1 P1 false true]
    $p1 makeMultiport true
    # receiver 1
    set e2 [java::new pt.actor.Actor $e0 E2]
    set p2 [java::new pt.actor.IOPort $e2 P2 true false]
    # receiver 2
    set e3 [java::new pt.actor.Actor $e0 E3]
    set p3 [java::new pt.actor.IOPort $e3 P3 true false]
    # connection
    set r1 [java::new pt.actor.IORelation $e0 R1]
    $p1 link $r1
    $p2 link $r1
    set r2 [java::new pt.actor.IORelation $e0 R2]
    $p1 link $r2
    $p3 link $r2
    list [$p1 description 7] [$p1 description 8] \
          [$p2 description 7] [$p2 description 8] \
          [$p3 description 7] [$p3 description 8]
} {{null
} {..E2.P2.pt.actor.Mailbox
..E3.P3.pt.actor.Mailbox
} {..E2.P2.pt.actor.Mailbox
} {null
} {..E3.P3.pt.actor.Mailbox
} {null
}}

test IOPort-10.6 {Check description use the example in design doc} {
    set e0 [java::new pt.actor.CompositeActor [java::null] $director]
    set e1 [java::new pt.actor.CompositeActor $e0 E1 [java::null]]
    set e2 [java::new pt.actor.Actor $e1 E2]
    set p1 [java::new pt.actor.IOPort $e2 P1 false true]
    $p1 makeMultiport true
    set p2 [java::new pt.actor.IOPort $e1 P2 false true]
    $p2 makeMultiport true
    set p3 [java::new pt.actor.IOPort $e1 P3 false true]
    $p3 makeMultiport true
    set p4 [java::new pt.actor.IOPort $e1 P4 false true]
    $p4 makeMultiport true
    set e3 [java::new pt.actor.CompositeActor $e0 E3 [java::null]]
    set e4 [java::new pt.actor.Actor $e3 E4]
    set e5 [java::new pt.actor.Actor $e3 E5]
    # set p5 without specify input/output
    set p5 [java::new pt.actor.IOPort $e3 P5]
    $p5 makeMultiport true
    set p6 [java::new pt.actor.IOPort $e3 P6 true false]
    set p8 [java::new pt.actor.IOPort $e4 P8 true false]
    $p8 makeMultiport true
    set p9 [java::new pt.actor.IOPort $e5 P9 true false]
    $p9 makeMultiport true

    set e6 [java::new pt.actor.Actor $e0 E6]
    set p7 [java::new pt.actor.IOPort $e6 P7 true false]
    $p7 makeMultiport true

    set e7 [java::new pt.actor.Actor $e0 E7]
    set p10 [java::new pt.actor.IOPort $e7 P10 false true]
    $p10 makeMultiport true

    # connection
    set r1 [java::new pt.actor.IORelation $e1 R1]
    $r1 setWidth 0
    $p1 link $r1
    $p2 link $r1
    $p3 link $r1
    $p4 link $r1
    set r2 [java::new pt.actor.IORelation $e0 R2]
    $r2 setWidth 3
    $p2 link $r2
    $p5 link $r2
    set r3 [java::new pt.actor.IORelation $e0 R3]
    $p2 link $r3
    $p5 link $r3
    $p6 link $r3
    set r4 [java::new pt.actor.IORelation $e0 R4]
    $r4 setWidth 2
    $p3 link $r4
    $p7 link $r4
    $p10 link $r4
    set r5 [java::new pt.actor.IORelation $e3 R5]
    $r5 setWidth 0
    $p5 link $r5
    $p8 link $r5
    set r6 [java::new pt.actor.IORelation $e3 R6]
    $p5 link $r6
    $p9 link $r6
    set r7 [java::new pt.actor.IORelation $e3 R7]
    $p6 link $r7
    $p9 link $r7
    set r8 [java::new pt.actor.IORelation $e0 R8]
    $p3 link $r8
    list [$p1 description 7] [$p1 description 8] \
         [$p10 description 7] [$p10 description 8]
} {{null
} {..E3.E4.P8.pt.actor.Mailbox ..E6.P7.pt.actor.Mailbox
..E3.E4.P8.pt.actor.Mailbox ..E6.P7.pt.actor.Mailbox
..E3.E4.P8.pt.actor.Mailbox
..E3.E5.P9.pt.actor.Mailbox ..E3.E5.P9.pt.actor.Mailbox
} {null
} {..E3.E4.P8.pt.actor.Mailbox ..E6.P7.pt.actor.Mailbox
..E3.E4.P8.pt.actor.Mailbox ..E6.P7.pt.actor.Mailbox
}}


######################################################################
####
#   Check liberalLink with galaxy

test IOPort-11.1 {Check liberalLink on transparent multiport and inferred width} {
    set ex [java::new pt.actor.CompositeActor [java::null] $director]
    set e0 [java::new pt.actor.CompositeActor $ex E0 [java::null]]
    # transparent port
    set p0 [java::new pt.actor.IOPort $e0 P0 false true]
    $p0 makeMultiport true
    # inside entity
    set e1 [java::new pt.actor.Actor $e0 E1]
    set p1 [java::new pt.actor.IOPort $e1 P1 false true]
    $p1 makeMultiport true
    # outside entoty
    set e2 [java::new pt.actor.Actor $e0 E2]
    set p2 [java::new pt.actor.IOPort $e2 P2 true false]
    $p2 makeMultiport true
    # connection
    # inside relation with unspecified width
    set r1 [java::new pt.actor.IORelation $e0 R1]
    $r1 setWidth 0
    $p1 link $r1
    $p0 link $r1
    # outside relation
    set r2 [java::new pt.actor.IORelation $e0 R2]
    $r2 setWidth 3
    $p0 link $r2
    $p2 link $r2
    list [$p0 getWidth] [$p1 getWidth] [$p2 getWidth]
} {0 1 3}

# NOTE: Builds on previous example.
test IOPort-11.15 {Check inferred width} {
    set r3 [java::new pt.actor.IORelation $ex R3]
    $r3 setWidth 3
    $p0 link $r3
    set result [list [$p0 getWidth] [$p1 getWidth] [$p2 getWidth]]
    $r3 setWidth 4
    lappend result [$p0 getWidth] [$p1 getWidth] [$p2 getWidth]
    $r3 setWidth 5
    lappend result [$p0 getWidth] [$p1 getWidth] [$p2 getWidth]
} {3 1 3 4 1 3 5 2 3}

test IOPort-11.2 {Check liberalLink: link a linked relation from inside } {
    set e0 [java::new pt.actor.CompositeActor [java::null] $director]
    set p0 [java::new pt.actor.IOPort $e0 P0 false true]
    $p0 makeMultiport true
    set r1 [java::new pt.actor.IORelation $e0 R1]
    $r1 setWidth 0
    $p0 link $r1
    catch {$p0 link $r1} msg1
    list $msg1
} {{}}

test IOPort-11.3 {Check liberalLink multi-*-relation from inside } {
    set e0 [java::new pt.actor.CompositeActor [java::null] $director]
    set p0 [java::new pt.actor.IOPort $e0 P0 false true]
    $p0 makeMultiport true
    set r1 [java::new pt.actor.IORelation $e0 R1]
    $r1 setWidth 0
    $p0 link $r1
    set r2 [java::new pt.actor.IORelation $e0 R2]
    $r2 setWidth 0
    catch {$p0 link $r2} msg1
    list $msg1
} {{pt.kernel.util.IllegalActionException: ..P0 and ..R2: Attempt to link a second bus relation with unspecified width to the inside of a port.}}

test IOPort-11.4 {Check liberalLink multi-*-relation from outside } {
    set e0 [java::new pt.actor.CompositeActor [java::null] $director]
    set p0 [java::new pt.actor.IOPort $e0 P0 false true]
    $p0 makeMultiport true
    # inside relation, fixed width
    set r1 [java::new pt.actor.IORelation $e0 R1]
    $r1 setWidth 3
    $p0 link $r1
    # ourside relation, *
    set r2 [java::new pt.actor.IORelation]
    $r2 setName R2
    $r2 setWidth 0
    $p0 link $r2
    set r3 [java::new pt.actor.IORelation]
    $r3 setName R3
    $r3 setWidth 0
    catch {$p0 link $r3} msg1
    list $msg1
} {{pt.kernel.util.IllegalActionException: ..P0 and .R3: Attempt to link a second bus relation with unspecified width to the outside of a port.}}

test IOPort-11.5 {Check liberalLink *-relation from both inside and outside } {
    set e0 [java::new pt.actor.CompositeActor [java::null] $director]
    set p0 [java::new pt.actor.IOPort $e0 P0 false true]
    $p0 makeMultiport true
    # inside relation, *
    set r1 [java::new pt.actor.IORelation $e0 R1]
    $r1 setWidth 0
    $p0 link $r1
    # ourside relation, *
    set r2 [java::new pt.actor.IORelation]
    $r2 setName R2
    $r2 setWidth 0
    catch {$p0 link $r2} msg1
    list $msg1
} {{}}


