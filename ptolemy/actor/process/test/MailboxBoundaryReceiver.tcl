# Tests for the MailboxBoundaryReceiver.Tcl class
#
# @Author: John S. Davis II
#
# @Version: : MailboxBoundaryReceiver.Tcl.tcl,v 1.33 1998/12/05 05:37:32 cxh Exp $
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
test MailboxBoundaryReceiver.Tcl-2.2 {Unlimited get(Branch) and put(Token,Branch) without calling activate()} {
    # Instantiate Directors and Composite Actors
    set tL [java::new ptolemy.actor.CompositeActor]
    $tL setName "tL"
    set compAct [java::new ptolemy.actor.CompositeActor $tL "compAct"]
    set outDir [java::new ptolemy.actor.process.ProcessDirector $tL "outDir"]
    set inDir [java::new ptolemy.actor.process.ProcessDirector $compAct "inDir"]
    
    # Instantiate Atomic Actors
    set act1 [java::new ptolemy.actor.AtomicActor $tL "act1"] 
    set act2 [java::new ptolemy.actor.AtomicActor $tL "act2"] 
    set act3 [java::new ptolemy.actor.AtomicActor $tL "act3"] 
    set act4 [java::new ptolemy.actor.AtomicActor $compAct "act4"] 
    set act5 [java::new ptolemy.actor.AtomicActor $compAct "act5"] 
    set act6 [java::new ptolemy.actor.AtomicActor $compAct "act6"] 
    
    # Instantiate Ports
    set act1OutPort [java::new ptolemy.actor.IOPort $act1 "act1OutPort" false true]
    set act2OutPort [java::new ptolemy.actor.IOPort $act2 "act2OutPort" false true]
    set act3OutPort [java::new ptolemy.actor.IOPort $act3 "act3OutPort" false true]
    set act4InPort [java::new ptolemy.actor.IOPort $act4 "act4InPort" true false]
    set act5InPort [java::new ptolemy.actor.IOPort $act5 "act5InPort" true false]
    set act6InPort [java::new ptolemy.actor.IOPort $act6 "act6InPort" true false]
    set compIn1Port [java::new ptolemy.actor.IOPort $compAct "compIn1Port" true false]
    set compIn2Port [java::new ptolemy.actor.IOPort $compAct "compIn2Port" true false]
    set compIn3Port [java::new ptolemy.actor.IOPort $compAct "compIn3Port" true false]
    
    # Make Connections 
    $tL connect $compIn1Port $act1OutPort
    $tL connect $compIn2Port $act2OutPort
    $tL connect $compIn3Port $act3OutPort
    $compAct connect $compIn1Port $act4InPort
    $compAct connect $compIn2Port $act5InPort
    $compAct connect $compIn3Port $act6InPort
    
    # Create Receivers
    $tL preinitialize
    $tL initialize
    
    set cntlr [$inDir getInputController]
    set branchList [$cntlr getBranchList]
    set size [$branchList size]
    
    # Get Branches
    set brch1 [java::cast ptolemy.actor.process.Branch [$branchList get 0]]
    set brch2 [java::cast ptolemy.actor.process.Branch [$branchList get 1]]
    set brch3 [java::cast ptolemy.actor.process.Branch [$branchList get 2]]
    
    # Get Receivers
    set pRcvr1 [java::cast ptolemy.actor.process.MailboxBoundaryReceiver [$brch1 getProdReceiver]]
    set pRcvr2 [java::cast ptolemy.actor.process.MailboxBoundaryReceiver [$brch2 getProdReceiver]]
    set pRcvr3 [java::cast ptolemy.actor.process.MailboxBoundaryReceiver [$brch3 getProdReceiver]]
    
    set cRcvr1 [java::cast ptolemy.actor.process.MailboxBoundaryReceiver [$brch1 getConsReceiver]]
    set cRcvr2 [java::cast ptolemy.actor.process.MailboxBoundaryReceiver [$brch2 getConsReceiver]]
    set cRcvr3 [java::cast ptolemy.actor.process.MailboxBoundaryReceiver [$brch3 getConsReceiver]]
    
    $cntlr setActive true
    $cntlr restart
    
    set val 1
    
    if { [$cntlr isBlocked] != 0 } {
        set val 0
    }

    set tok [java::new ptolemy.data.Token]
    
    $pRcvr1 put $tok
    if { [$pRcvr1 hasToken] != 1 } {
        set val 0
    }
    if { [$cRcvr1 hasRoom] != 1 } {
        set val 0
    }
    
    $pRcvr2 put $tok
    if { [$pRcvr2 hasToken] != 1 } {
        set val 0
    }
    if { [$cRcvr2 hasRoom] != 1 } {
        set val 0
    }
    
    $pRcvr3 put $tok
    if { [$pRcvr3 hasToken] != 1 } {
        set val 0
    }
    if { [$cRcvr3 hasRoom] != 1 } {
        set val 0
    }
    
    $brch1 transferTokens
    $brch2 transferTokens
    $brch3 transferTokens
    
    if { [$pRcvr1 hasToken] != 0 } {
        set val 0
    }
    if { [$pRcvr2 hasToken] != 0 } {
        set val 0
    }
    if { [$pRcvr3 hasToken] != 0 } {
        set val 0
    }
    
    list $val 

} {1}
