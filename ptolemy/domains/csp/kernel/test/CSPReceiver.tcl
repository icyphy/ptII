# Tests for the CSPReceiver class
#
# @Author: John S. Davis II
#
# @Version: : CSPReceiver.tcl,v 1.33 1998/12/05 05:37:32 cxh Exp $
#
# @Copyright (c) 1999 The Regents of the University of California.
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
test CSPReceiver-2.1 {Constructors and Containers} {
    set rcvr1 [java::new ptolemy.domains.csp.kernel.CSPReceiver]
    set val1 [$rcvr1 getContainer]

    set port [java::new ptolemy.actor.IOPort]
    set rcvr2 [java::new ptolemy.domains.csp.kernel.CSPReceiver $port]
    set val2 [$rcvr2 getContainer]

    set rcvr3 [java::new ptolemy.domains.csp.kernel.CSPReceiver]
    $rcvr3 setContainer $port
    set val3 [$rcvr3 getContainer]

    list [expr {$val1 == [java::null]}] [expr {$val2 == $port}] [expr {$val3 == $port}]
} {1 1 1}

######################################################################
####
#
test CSPReceiver-3.1 {get(), put(), No tokens - deadlock!} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.CompositeActor $wspc]
    set manager [java::new ptolemy.actor.Manager $wspc "manager"]
    set dir [java::new ptolemy.domains.csp.kernel.CSPDirector $wspc "director"]
    $topLevel setDirector $dir
    $topLevel setManager $manager
    set actorA [java::new ptolemy.domains.csp.kernel.test.CSPPut $topLevel "actorA" 0] 
    set actorB [java::new ptolemy.domains.csp.kernel.test.CSPGet $topLevel "actorB" 1] 

    set input [java::new ptolemy.data.Token]
    
    set portA [java::field $actorA outputPort]
    #set portA [$actorA getPort "output"]
    set portB [java::field $actorB inputPort]
    # set portB [$actorB getPort "input"]

    set rel [$topLevel connect $portB $portA "rel"]

    $manager run

    set output [$actorB getToken 0] 

    list [expr {$output == [java::null]}]
} {1}

######################################################################
####
#
test CSPReceiver-3.2 {get(), put(), One token} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.CompositeActor $wspc]
    set manager [java::new ptolemy.actor.Manager $wspc "manager"]
    set dir [java::new ptolemy.domains.csp.kernel.CSPDirector $wspc "director"]
    $topLevel setDirector $dir
    $topLevel setManager $manager
    set actorA [java::new ptolemy.domains.csp.kernel.test.CSPPut $topLevel "actorA" 1] 
    set actorB [java::new ptolemy.domains.csp.kernel.test.CSPGet $topLevel "actorB" 1] 

    set input [java::new ptolemy.data.Token]
    $actorA setToken $input 0
    
    set portA [java::field $actorA outputPort]
    set portB [java::field $actorB inputPort]

    set rel [$topLevel connect $portB $portA "rel"]

    $manager run

    set output [$actorB getToken 0] 

    list [expr {$output == $input}]
} {1}

######################################################################
####
#
test CSPReceiver-3.3 {get(), put(), Two tokens and then deadlock!} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.CompositeActor $wspc]
    set manager [java::new ptolemy.actor.Manager $wspc "manager"]
    set dir [java::new ptolemy.domains.csp.kernel.CSPDirector $wspc "director"]
    $topLevel setDirector $dir
    $topLevel setManager $manager
    set actorA [java::new ptolemy.domains.csp.kernel.test.CSPPut $topLevel "actorA" 2] 
    set actorB [java::new ptolemy.domains.csp.kernel.test.CSPGet $topLevel "actorB" 3] 

    set input1 [java::new ptolemy.data.Token]
    set input2 [java::new ptolemy.data.Token]
    $actorA setToken $input1 0
    $actorA setToken $input2 1
    
    set portA [java::field $actorA outputPort]
    set portB [java::field $actorB inputPort]

    set rel [$topLevel connect $portB $portA "rel"]

    $manager run

    set output1 [$actorB getToken 0] 
    set output2 [$actorB getToken 1] 
    set output3 [$actorB getToken 2] 

    list [expr {$output1 == $input1}] [expr {$output2 == $input2}] [expr {$output3 == [java::null]}]
} {1 1 1}








