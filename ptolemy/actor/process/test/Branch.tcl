# Tests for the Branch class
#
# @Author: John S. Davis II
#
# @Version: : Branch.tcl,v 1.33 1998/12/05 05:37:32 cxh Exp $
#
# @Copyright (c) 1999-2000 The Regents of the University of California.
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
test Branch-2.1 {Test BranchController constructors, reset() and 
pre-activation state} {
   
    # Instantiate Directors and Composite Actors
    set topLevel [java::new ptolemy.actor.CompositeActor]
    $topLevel setName "topLevel"
    set compAct [java::new ptolemy.actor.CompositeActor $topLevel "compAct"]
    set outerDir [java::new ptolemy.actor.process.ProcessDirector $topLevel "outerDir"]
    set innerDir [java::new ptolemy.actor.process.ProcessDirector $compAct "innerDir"]
    
    # Instantiate Atomic Actors
    set act1 [java::new ptolemy.actor.AtomicActor $topLevel "act1"] 
    set act2 [java::new ptolemy.actor.AtomicActor $compAct "act2"] 
    set act3 [java::new ptolemy.actor.AtomicActor $topLevel "act3"] 
    
    # Instantiate Ports
    set act1OutPort [java::new ptolemy.actor.IOPort $act1 "act1OutPort" false true]
    set compInPort [java::new ptolemy.actor.IOPort $compAct "compInPort" true false]
    set compOutPort [java::new ptolemy.actor.IOPort $compAct "compOutPort" false true]
    set act2InPort [java::new ptolemy.actor.IOPort $act2 "act2InPort" true false]
    set act2OutPort [java::new ptolemy.actor.IOPort $act2 "act2OutPort" false true]
    set act3InPort [java::new ptolemy.actor.IOPort $act3 "act3InPort" true false]
    
    # Make Connections
    $topLevel connect $act1OutPort $compInPort
    $compAct connect $compInPort $act2InPort
    $compAct connect $compOutPort $act2OutPort
    $topLevel connect $compOutPort $act3InPort
    
    # Create Receivers
    $topLevel preinitialize
    $topLevel initialize
    
    set cntlrIn [$innerDir getInputController]
    
    set val 1
    if { [$cntlrIn hasBranches] != 1 } {
	set val 0
    }
    if { [$cntlrIn isIterationOver] != 1 } {
    	set val 0
    }
    if { [$cntlrIn isBlocked] != 0 } {
    	set val 0
    }
    if { [$cntlrIn isActive] != 0 } {
    	set val 0
    }
    if { [$cntlrIn getParent] != $compAct } {
    	set val 0
    }
    
    set cntlrOut [$innerDir getOutputController]
    
    if { [$cntlrOut hasBranches] != 1 } {
	set val 0
    }
    if { [$cntlrOut isIterationOver] != 1 } {
    	set val 0
    }
    if { [$cntlrOut isBlocked] != 0 } {
    	set val 0
    }
    if { [$cntlrOut isActive] != 0 } {
    	set val 0
    }
    if { [$cntlrOut getParent] != $compAct } {
    	set val 0
    }
    
   list $val

} {1}

######################################################################
#### Continued from above
#
test Branch-2.2 {Test Branch constructors, reset() and pre-activation state} {
   
    set branchList [$cntlrIn getBranchList]
    set branch [java::cast ptolemy.actor.process.Branch [$branchList get 0]]

    set val 1
    if { [$branch isActive] != 0 } {
     	set val 0
    }
    if { [$branch isBranchPermitted] != 0 } {
    	set val 0
    }
    if { [$branch numberOfCompletedEngagements] != 0 } {
     	set val 0
    }
    if { [$branch isIterationOver] != 1 } {
     	set val 0
    }
    
    $branch reset
   
    if { [$branch isActive] != 1 } {
    	$val = 0
    }
    if { [$branch isIterationOver] != 1 } {
     	set val 0
    }
    
   list $val

} {1}











