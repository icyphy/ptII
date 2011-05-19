# Tests for the RendezvousReceiver class
#
# @Author: Edward A. Lee, Christopher Brooks, based on a file by John S. Davis II
#
# @Version: : RendezvousReceiver.tcl,v 1.33 1998/12/05 05:37:32 cxh Exp $
#
# @Copyright (c) 2005-2008 The Regents of the University of California.
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

# Ptolemy II bed, see /users/cxh/ptII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs enumToObjects] == "" } then {
     source enums.tcl
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

set toplevel [java::new ptolemy.actor.CompositeActor]
set dir [java::new ptolemy.domains.rendezvous.kernel.RendezvousDirector]
$toplevel setDirector $dir
$toplevel setDirector $dir
set sink [java::new ptolemy.domains.rendezvous.kernel.test.TestSink $toplevel "sink"]
set port [$sink getPort "input"]

######################################################################
####
#
test RendezvousReceiver-2.1 {Constructors and Containers} {
    set rcvr1 [java::new ptolemy.domains.rendezvous.kernel.RendezvousReceiver]
    set val1 [$rcvr1 getContainer]

    set port [java::new ptolemy.actor.TypedIOPort]
    set rcvr2 [java::new ptolemy.domains.rendezvous.kernel.RendezvousReceiver $port]
    set val2 [$rcvr2 getContainer]

    set rcvr3 [java::new ptolemy.domains.rendezvous.kernel.RendezvousReceiver]
    $rcvr3 setContainer $port
    set val3 [$rcvr3 getContainer]

    list [expr {$val1 == [java::null]}] \
	    [$val2 equals $port] \
	    [$val3 equals $port]
} {1 1 1}


######################################################################
####
#
test RendezvousReceiver-2.2 {Check for correct IOPort container in new receiver} {
    set rec [java::new ptolemy.domains.rendezvous.kernel.RendezvousReceiver $port]
    list [ $port equals [$rec getContainer]]
} {1}

######################################################################
####
#
test RendezvousReceiver-3.1 {hasRoom} {
    # hasroom() and hasRoom(int) always returns true
    list [$rcvr1 hasRoom] \
	[$rcvr1 hasRoom -1] [$rcvr1 hasRoom 0] [$rcvr1 hasRoom 1]
} {1 1 1 1}

######################################################################
####
#
test RendezvousReceiver-4.1 {Put and get token when only one token} {
    #$rec setCapacity 1
    catch {$rec put [java::new {ptolemy.data.IntToken int} 2]} errMsg
    #set tok [java::cast ptolemy.data.IntToken [$rec get]]
    #list [$tok intValue ]
    list $errMsg
} {{ptolemy.actor.process.TerminateProcessException: RendezvousReceiver: trying to rendezvous with a receiver with no director => terminate.}}

######################################################################
####
#
# Call the various boundary* methods on the receiver
proc describeBoundary {receiver} {
    return [list [$receiver isConnectedToBoundary] [$receiver isConnectedToBoundaryInside] [$receiver isConnectedToBoundaryOutside] [$receiver isConsumerReceiver] [$receiver isOutsideBoundary] [$receiver isProducerReceiver]]
}

######################################################################
####
#
test RendezvousReceiver-4.2 {isConnectedToBoundary} {
    list [describeBoundary $rcvr1] \
	[describeBoundary $rcvr2] \
	[describeBoundary $rcvr3]
} {{0 0 0 0 0 0} {0 0 0 0 0 0} {0 0 0 0 0 0}}

######################################################################
####
#
test RendezvousReceiver-5.1 {isReadBlocked} {
    catch { $rcvr1 isReadBlocked} errMsg
    list $errMsg
} {{ptolemy.actor.process.TerminateProcessException: RendezvousReceiver: trying to rendezvous with a receiver with no director => terminate.}}


######################################################################
####
#
test RendezvousReceiver-6.1 {isWriteBlocked} {
    catch { $rcvr1 isWriteBlocked} errMsg
    list $errMsg
} {{ptolemy.actor.process.TerminateProcessException: RendezvousReceiver: trying to rendezvous with a receiver with no director => terminate.}}
