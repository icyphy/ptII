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

# Load up Tcl procs to print out enums
if {[info procs enumToFullNames] == "" } then { 
    source [file join $TYCHO java pt kernel test enums.tcl]
}

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
#     set n [java::new pt.actors.IOPort]
#     list [getJavaInfo $n]
# } {{
# }}

# FIXME: Should the Component and Composite Entities be Actors?

######################################################################
####
# 
test IOPort-2.1 {Construct Ports} {
    set e1 [java::new pt.kernel.ComponentEntity]
    set p1 [java::new pt.actors.IOPort]
    set p2 [java::new pt.actors.IOPort $e1 P2]
    list [$p1 getFullName] [$p2 getFullName]
} {. ..P2}

######################################################################
####
# 
test IOPort-2.2 {Construct Ports} {
    set e1 [java::new pt.kernel.ComponentEntity]
    $e1 setName E1
    set p1 [java::new pt.actors.IOPort $e1 P1]
    set p2 [java::new pt.actors.IOPort $e1 P2]
    list [$p1 getFullName] [$p2 getFullName]
} {.E1.P1 .E1.P2}

######################################################################
####
# 
test IOPort-3.1 {Test input/output predicates} {
    set e1 [java::new pt.kernel.ComponentEntity]
    set p1 [java::new pt.actors.IOPort $e1 P1]
    set p2 [java::new pt.actors.IOPort $e1 P2 true true]
    list [$p1 isInput] [$p1 isOutput] [$p2 isInput] [$p2 isOutput]
} {0 0 1 1}

test IOPort-3.2 {Test input/output changes} {
    set e1 [java::new pt.kernel.ComponentEntity]
    set p1 [java::new pt.actors.IOPort $e1 P1 true true]
    set result [list [$p1 isInput] [$p1 isOutput]]
    $p1 makeInput false
    $p1 makeOutput false
    lappend result [$p1 isInput] [$p1 isOutput]
} {1 1 0 0}

test IOPort-3.3 {Test input/output predicates on transparent ports} {
    set e0 [java::new pt.kernel.CompositeEntity]
    set e1 [java::new pt.kernel.ComponentEntity $e0 E1]
    set p0 [java::new pt.actors.IOPort $e0 P0]
    set p1 [java::new pt.actors.IOPort $e1 P1 true true]
    set r1 [java::new pt.actors.IORelation $e0 R1]
    $p1 link $r1
    $p0 link $r1
    list [$p0 isInput] [$p0 isOutput]
} {1 1}

######################################################################
####
# 
test IOPort-4.1 {Test multiport predicate} {
    set e0 [java::new pt.kernel.CompositeEntity]
    set e1 [java::new pt.kernel.ComponentEntity $e0 E1]
    set p0 [java::new pt.actors.IOPort $e0 P0]
    set p1 [java::new pt.actors.IOPort $e1 P1 true true]
    set r1 [java::new pt.actors.IORelation $e0 R1]
    $p1 link $r1
    $p0 link $r1
    $p1 makeMultiport true
    $p0 isMultiport
} {0}

test IOPort-4.2 {Test multiport predicate on transparent port} {
    set e1 [java::new pt.kernel.ComponentEntity]
    set p1 [java::new pt.actors.IOPort $e1 P1 true false]
    set temp [$p1 isMultiport]
    $p1 makeMultiport true
    list $temp [$p1 isMultiport]
} {0 1}

######################################################################
####
# 
test IOPort-5.1 {Test getWidth} {
    set e0 [java::new pt.kernel.CompositeEntity]
    set e1 [java::new pt.kernel.ComponentEntity $e0 E1]
    set p1 [java::new pt.actors.IOPort $e1 P1 true false]
    set r1 [java::new pt.actors.IORelation $e0 R1]
    set temp [$p1 getWidth]
    $p1 liberalLink $r1
    list $temp [$p1 getWidth]
} {0 1}

test IOPort-5.2 {Test getWidth} {
    set e0 [java::new pt.kernel.CompositeEntity]
    set e1 [java::new pt.kernel.ComponentEntity $e0 E1]
    set p1 [java::new pt.actors.IOPort $e1 P1 true true]
    set r1 [java::new pt.actors.IORelation $e0 R1]
    set r2 [java::new pt.actors.IORelation $e0 R2]
    $p1 makeMultiport true
    $p1 liberalLink $r1
    $p1 liberalLink $r2
    $p1 getWidth
} {2}

test IOPort-5.3 {Test getWidth} {
    set e0 [java::new pt.kernel.CompositeEntity]
    set e1 [java::new pt.kernel.ComponentEntity $e0 E1]
    set p1 [java::new pt.actors.IOPort $e1 P1 true true]
    set r1 [java::new pt.actors.IORelation $e0 R1]
    $r1 setWidth 2
    set r2 [java::new pt.actors.IORelation $e0 R2]
    $p1 makeMultiport true
    $p1 liberalLink $r1
    $p1 liberalLink $r2
    $p1 getWidth
} {3}

test IOPort-5.4 {Test getWidth after unlinking} {
    set e0 [java::new pt.kernel.CompositeEntity]
    # sending entity
    set e1 [java::new pt.kernel.ComponentEntity $e0 E1]
    set p1 [java::new pt.actors.IOPort $e1 P1 false true]
    # receiving entity
    set e2 [java::new pt.kernel.ComponentEntity $e0 E2]
    set p2 [java::new pt.actors.IOPort $e2 P2 true false]
    # connection
    # Can't use this because it uses a plain relation.
    # set r1 [$e0 connect $p1 $p2 R1]
    set r1 [java::new pt.actors.IORelation $e0 R1]
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
    set e0 [java::new pt.kernel.CompositeEntity]
    set e1 [java::new pt.kernel.ComponentEntity $e0 E1]
    set p1 [java::new pt.actors.IOPort $e1 P1 true true]
    set r1 [java::new pt.actors.IORelation $e0 R1]
    set r2 [java::new pt.actors.IORelation $e0 R2]
    $p1 liberalLink $r1
    catch {$p1 liberalLink $r2} msg
    list $msg
} {{pt.kernel.IllegalActionException: ..E1.P1: Attempt to link more than one relation to a single port.}}

######################################################################
####
# 
test IOPort-7.1 {Check getReceivers on an unlinked port} {
    set e0 [java::new pt.kernel.CompositeEntity]
    set e1 [java::new pt.kernel.ComponentEntity $e0 E1]
    set p1 [java::new pt.actors.IOPort $e1 P1 true true]
    expr {[$p1 getReceivers] == [java::null]}
} {1}

test IOPort-7.2 {Check getReceivers} {
    set e0 [java::new pt.kernel.CompositeEntity]
    set e1 [java::new pt.kernel.ComponentEntity $e0 E1]
    set p1 [java::new pt.actors.IOPort $e1 P1 true true]
    set r1 [java::new pt.actors.IORelation $e0 R1]
    catch {$p1 getReceivers $r1} msg
    list $msg
} {{pt.kernel.IllegalActionException: ..E1.P1: getReceivers: Relation argument is not linked to me.}}

######################################################################
####
# 
test IOPort-8.1 {Check getRemoteReceivers on a port with no links} {
    set e0 [java::new pt.kernel.CompositeEntity]
    set e1 [java::new pt.kernel.ComponentEntity $e0 E1]
    set p1 [java::new pt.actors.IOPort $e1 P1 true true]
    expr {[$p1 getRemoteReceivers] == [java::null]}
} {1}

test IOPort-8.2 {Check getRemoteReceivers on a port after unlinking} {
    set e0 [java::new pt.kernel.CompositeEntity]
    # sending entity
    set e1 [java::new pt.kernel.ComponentEntity $e0 E1]
    set p1 [java::new pt.actors.IOPort $e1 P1 false true]
    # receiving entity
    set e2 [java::new pt.kernel.ComponentEntity $e0 E2]
    set p2 [java::new pt.actors.IOPort $e1 P2 true false]
    # connection
    # Can't use this because it uses a plain relation.
    # set r1 [$e0 connect $p1 $p2 R1]
    set r1 [java::new pt.actors.IORelation $e0 R1]
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
    set e0 [java::new pt.kernel.CompositeEntity]
    # sending entity
    set e1 [java::new pt.kernel.ComponentEntity $e0 E1]
    set p1 [java::new pt.actors.IOPort $e1 P1 false true]
    # receiving entity
    set e2 [java::new pt.kernel.ComponentEntity $e0 E2]
    set p2 [java::new pt.actors.IOPort $e1 P2 true false]
    # connection
    # Can't use this because it uses a plain relation.
    # set r1 [$e0 connect $p1 $p2 R1]
    set r1 [java::new pt.actors.IORelation $e0 R1]
    $p1 link $r1
    $p2 link $r1
    # token to send
    set token [java::new pt.data.StringToken foo]
    $p1 send 0 $token
    set received [$p2 get 0]
    $received toString
} {foo}

test IOPort-9.2 {Check unlink and send to dangling relation} {
    set e0 [java::new pt.kernel.CompositeEntity]
    # sending entity
    set e1 [java::new pt.kernel.ComponentEntity $e0 E1]
    set p1 [java::new pt.actors.IOPort $e1 P1 false true]
    # receiving entity
    set e2 [java::new pt.kernel.ComponentEntity $e0 E2]
    set p2 [java::new pt.actors.IOPort $e1 P2 true false]
    # connection
    # Can't use connect because it uses a plain relation.
    # set r1 [$e0 connect $p1 $p2 R1]
    set r1 [java::new pt.actors.IORelation $e0 R1]
    $p1 link $r1
    $p2 link $r1
    # unlink one end
    $p2 unlinkAll
    # token to send
    set token [java::new pt.data.StringToken foo]
    $p1 send 0 $token
    catch {$p2 get 0} msg
    list [$p2 getWidth] $msg
} {0 {pt.kernel.NoSuchItemException: ..E1.P2: get: channel index is out of range.}}

test IOPort-9.3 {Check unlink and get from unlinked port} {
    set e0 [java::new pt.kernel.CompositeEntity]
    # sending entity
    set e1 [java::new pt.kernel.ComponentEntity $e0 E1]
    set p1 [java::new pt.actors.IOPort $e1 P1 false true]
    # receiving entity
    set e2 [java::new pt.kernel.ComponentEntity $e0 E2]
    set p2 [java::new pt.actors.IOPort $e1 P2 true false]
    # connection
    # Can't use connect because it uses a plain relation.
    # set r1 [$e0 connect $p1 $p2 R1]
    set r1 [java::new pt.actors.IORelation $e0 R1]
    $p1 link $r1
    $p2 link $r1
    # unlink one end
    $p1 unlink $r1
    # token to send
    set token [java::new pt.data.StringToken foo]
    catch {$p1 send 0 $token} msg1
    catch {$p2 get 0} msg2
    list [$p2 getWidth] $msg1 $msg2
} {1 {pt.kernel.IllegalActionException: ..E1.P1: send: channel index is out of range.} {pt.kernel.NoSuchItemException: ..E1.P2: Attempt to get data from an empty mailbox.}}
