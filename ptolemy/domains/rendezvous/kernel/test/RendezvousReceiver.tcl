# Tests for the RendezvousReceiver class
#
# @Author: John S. Davis II
#
# @Version: : RendezvousReceiver.tcl,v 1.33 1998/12/05 05:37:32 cxh Exp $
#
# @Copyright (c) 1999-2005 The Regents of the University of California.
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

