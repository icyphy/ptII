# Tests for the IOPortEventListener class
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2010 The Regents of the University of California.
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

#
#

# NOTE:  All of the following tests use this director,
# pretty much as a dummy.
set director [java::new ptolemy.actor.Director]
set manager [java::new ptolemy.actor.Manager]

######################################################################
####
#
test IOPortEventListener-1.1 {Construct Ports} {
    set e0 [java::new ptolemy.actor.CompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    # sending entity
    set e1 [java::new ptolemy.actor.AtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.IOPort $e1 P1 false true]

    set listener [java::new ptolemy.actor.test.RemoveIOPortEventListener]
    $p1 addIOPortEventListener $listener

    # receiving entity
    set e2 [java::new ptolemy.actor.AtomicActor $e0 E2]
    set p2 [java::new ptolemy.actor.IOPort $e2 P2 true false]
    # connection
    # Can't use this because it uses a plain relation.
    # set r1 [$e0 connect $p1 $p2 R1]
    set r1 [java::new ptolemy.actor.IORelation $e0 R1]
    $p1 link $r1
    $p2 link $r1
    
    # Call preinitialize on the manager so that the receivers get created
    # added Neil Smyth/Bert Rodiers. Need to call this as receivers are no longer 
    # created on the fly.
    $manager preinitializeAndResolveTypes

    # token to send
    set token [java::new ptolemy.data.StringToken foo]
    # Tcl requires a fully qualified method signature for the overloaded
    # send() method.
    $p1 {send int ptolemy.data.Token} 0 $token
    set received [$p2 get 0]
    $manager wrapup
    $received toString
} {"foo"}
