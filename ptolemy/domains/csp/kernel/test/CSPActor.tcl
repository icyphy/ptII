# Tests for the CSPActor class
#
# @Author: John S. Davis II
#
# @Version: : CSPActor.tcl,v 1.33 1998/12/05 05:37:32 cxh Exp $
#
# @Copyright (c) 1999-2009 The Regents of the University of California.
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
test CSPActor-2.1 {ConditionalReceive() } {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set manager [java::new ptolemy.actor.Manager $wspc "manager"]
    set dir [java::new ptolemy.domains.csp.kernel.CSPDirector $topLevel "director"]
    $topLevel setManager $manager
    set actorA [java::new ptolemy.domains.csp.kernel.test.CSPPutToken $topLevel "actorA" 1] 
    set actorB [java::new ptolemy.domains.csp.kernel.test.CSPPutToken $topLevel "actorB" 1] 
    set actorC [java::new ptolemy.domains.csp.kernel.test.CSPCondGet $topLevel "actorC" 2] 

    set portA [$actorA getPort "output"]
    set portB [$actorB getPort "output"]
    set portC [$actorC getPort "input"]

    set tokenA [java::new ptolemy.data.Token]
    set tokenB [java::new ptolemy.data.Token]

    $actorA setToken $tokenA 0 
    $actorB setToken $tokenB 0 
    $actorC setTruth 0 true

    set rel [java::cast ptolemy.actor.IORelation [$topLevel connect $portC $portA]]
    $rel setWidth 1
    set rel [java::cast ptolemy.actor.IORelation [$topLevel connect $portC $portB]]
		$rel setWidth 1
		
    $manager run

    set winner [$actorC isWinner 0]

    list $winner 

} {1}

######################################################################
####
#
test CSPActor-2.2 {ConditionalSend() } {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.TypedCompositeActor $wspc]
    set manager [java::new ptolemy.actor.Manager $wspc "manager"]
    set dir [java::new ptolemy.domains.csp.kernel.CSPDirector $topLevel "director"]
    $topLevel setManager $manager
    set actorA [java::new ptolemy.domains.csp.kernel.test.CSPCondPut $topLevel "actorA" 1 2] 
    set actorB [java::new ptolemy.domains.csp.kernel.test.CSPGetToken $topLevel "actorB" 1] 
    set actorC [java::new ptolemy.domains.csp.kernel.test.CSPGetToken $topLevel "actorC" 1] 

    set portA [$actorA getPort "output"]
    set portB [$actorB getPort "input"]
    set portC [$actorC getPort "input"]

    set token [java::new ptolemy.data.Token]

    $actorA setToken $token 0 
    $actorA setTruth 1 true

    set rel [java::cast ptolemy.actor.IORelation [$topLevel connect $portA $portB]]
    $rel setWidth 1
    set rel [java::cast ptolemy.actor.IORelation [$topLevel connect $portA $portC]]
    $rel setWidth 1

    $manager run

    set winner [$actorA isWinner 1]

    list $winner 

} {1}









