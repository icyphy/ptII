# Tests for the Director class
#
# @Author: Mudit Goel
#
# @Version: $Id$
#
# @Copyright (c) 1998-2000 The Regents of the University of California.
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
set w [java::new ptolemy.kernel.util.Workspace W]
set manager [java::new ptolemy.actor.Manager $w M]


######################################################################
####
#
test TestProcessDirector-2.1 {Constructor tests} {
    set d1 [java::new ptolemy.actor.process.ProcessDirector]
    $d1 setName D1
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setName E0
    set d2 [java::new ptolemy.actor.process.ProcessDirector $w]
    set d3 [java::new ptolemy.actor.process.ProcessDirector $e0 D3]
    list [$d1 getFullName] [$d2 getFullName] [$d3 getFullName]
} {.D1 . .E0.D3}

######################################################################
####
#
test TestProcessDirector-3.1 {Test clone} {
    # NOTE: Uses the setup above
    set d4 [java::cast ptolemy.actor.process.ProcessDirector [$d3 clone $w]]
    $d4 setName D4
    enumToFullNames [$w directory]
} {.M .}


######################################################################
####
#
test TestProcessDirector-4.1 {Test _makeDirectorOf} {
    # NOTE: Uses the setup above
    set e0 [java::new ptolemy.actor.CompositeActor $w]
    $e0 setName E0
    $e0 setManager $manager
    set d3 [java::new ptolemy.actor.process.ProcessDirector $e0 D3]
    list [$d3 getFullName] [$d4 getFullName] [enumToFullNames [$w directory]]
} {.E0.D3 .D4 {. .E0}}

######################################################################
####
#
test TestProcessDirector-5.1 {Test action methods} {
    # NOTE: Uses the setup above
    set a1 [java::new ptolemy.actor.process.test.TestProcessActor $e0 A1]
    set a2 [java::new ptolemy.actor.process.test.TestProcessActor $e0 A2]
    $manager run
    lsort [$a1 getRecord]

} {.E0.A1.fire .E0.A1.initialize .E0.A1.postfire .E0.A1.prefire .E0.A1.wrapup .E0.A2.fire .E0.A2.initialize .E0.A2.postfire .E0.A2.prefire .E0.A2.wrapup}

######################################################################
####
#
test TestProcessDirector-6.1 {Test action methods} {
    # Instantiate Manager and Workspace
    set wkSpace [java::new ptolemy.kernel.util.Workspace W]
    set manager [java::new ptolemy.actor.Manager $wkSpace M]
    
    # Instantiate Directors and Composite Actors
    set tL [java::new ptolemy.actor.TypedCompositeActor $wkSpace]
    $tL setName "tL"
    $tL setManager $manager
    set compAct [java::new ptolemy.actor.TypedCompositeActor $tL "compAct"]
    set outDir [java::new ptolemy.actor.process.test.TestProcessDirector $tL "outDir"]
    set inDir [java::new ptolemy.actor.process.test.TestProcessDirector $compAct "inDir"]
    
    # Debug Listeners
    $manager addDebugListener [java::new ptolemy.kernel.util.StreamListener]
    $compAct addDebugListener [java::new ptolemy.kernel.util.StreamListener]
    $outDir addDebugListener [java::new ptolemy.kernel.util.StreamListener]
    $inDir addDebugListener [java::new ptolemy.kernel.util.StreamListener]
    
    # Instantiate Atomic Actors
    set act1 [java::new ptolemy.actor.lib.Ramp $tL "act1"] 
    set act2 [java::new ptolemy.actor.lib.Ramp $tL "act2"] 
    set act3 [java::new ptolemy.actor.lib.Ramp $tL "act3"] 
    set act4 [java::new ptolemy.actor.process.test.ProcessSink $compAct "act4"] 
    set act5 [java::new ptolemy.actor.process.test.ProcessSink $compAct "act5"] 
    set act6 [java::new ptolemy.actor.process.test.ProcessSink $compAct "act6"] 
    
    # Set Parameters
    set act1Limit [java::cast ptolemy.data.expr.Parameter [$act1 getAttribute firingCountLimit]]
    $act1Limit setToken [java::new ptolemy.data.IntToken 1]
    set act2Limit [java::cast ptolemy.data.expr.Parameter [$act2 getAttribute firingCountLimit]]
    $act2Limit setToken [java::new ptolemy.data.IntToken 1]
    set act3Limit [java::cast ptolemy.data.expr.Parameter [$act3 getAttribute firingCountLimit]]
    $act3Limit setToken [java::new ptolemy.data.IntToken 1]
    
    # Instantiate Ports
    set act1OutPort [$act1 getPort "output"]
    set act2OutPort [$act2 getPort "output"]
    set act3OutPort [$act3 getPort "output"]
    set act4InPort [$act4 getPort "input"]
    set act5InPort [$act5 getPort "input"]
    set act6InPort [$act6 getPort "input"]
    set compIn1Port [java::new ptolemy.actor.TypedIOPort $compAct "compIn1Port" true false]
    set compIn2Port [java::new ptolemy.actor.TypedIOPort $compAct "compIn2Port" true false]
    set compIn3Port [java::new ptolemy.actor.TypedIOPort $compAct "compIn3Port" true false]
    
    # Make Connections 
    $tL connect $compIn1Port $act1OutPort
    $tL connect $compIn2Port $act2OutPort
    $tL connect $compIn3Port $act3OutPort
    $compAct connect $compIn1Port $act4InPort
    $compAct connect $compIn2Port $act5InPort
    $compAct connect $compIn3Port $act6InPort
    
    $manager run
    
#     # Create Receivers
#     $tL preinitialize
#     $tL initialize
    
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
    
    set vala 1
    set valb 1
    set valc 1
    
    if { [$cRcvr1 hasToken] != 1 } {
        set vala 0
    }
    if { [$cRcvr2 hasToken] != 1 } {
        set valb 0
    }
    if { [$cRcvr3 hasToken] != 1 } {
    	set valc 0
    }
    
    list $vala $valb $valc 

} {1 1 1}
