# Tests for the BranchController class
#
# @Author: John S. Davis II
#
# @Version: : BranchController.tcl,v 1.33 1998/12/05 05:37:32 cxh Exp $
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
test BranchController-2.1 {addBranches()} {
   
    set topLevel [java::new ptolemy.actor.CompositeActor]
    $topLevel setName "topLevel"
    set cntlr1 [java::new ptolemy.actor.process.BranchController $topLevel]
    set cntlr2 [java::new ptolemy.actor.process.BranchController $topLevel]
    set cntlr3 [java::new ptolemy.actor.process.BranchController $topLevel]
    
    set inport [java::new ptolemy.actor.IOPort $topLevel inport true false]
    set outport [java::new ptolemy.actor.IOPort $topLevel outport false true]
    
    $cntlr1 addBranches $inport
    $cntlr2 addBranches $outport
   
    set val 1
   
    list $val

} {1}


######################################################################
####
#
test BranchController-2.2 {Multiple addBranches() invocations} {
   
    set topLevel [java::new ptolemy.actor.CompositeActor]
    set cntlr1 [java::new ptolemy.actor.process.BranchController $topLevel]
    set cntlr2 [java::new ptolemy.actor.process.BranchController $topLevel]
    
    set inport [java::new ptolemy.actor.IOPort $topLevel inport true false]
    set outport [java::new ptolemy.actor.IOPort $topLevel outport false true]
    set port [java::new ptolemy.actor.IOPort]
    
    $cntlr1 addBranches $inport
    $cntlr2 addBranches $outport
    catch { $cntlr1 addBranches $inport } msg1
    catch { $cntlr1 addBranches $port } msg2
    catch { $cntlr1 addBranches $outport } msg3
    catch { $cntlr2 addBranches $inport } msg4
   
    list $msg1 $msg2 $msg3 $msg4

} {{ptolemy.kernel.util.IllegalActionException: This port is already controlled by this BranchController
  in .<Unnamed Object>.inport} {ptolemy.kernel.util.IllegalActionException: Can not contain a port that is not contained by this BranchController's container.} {ptolemy.kernel.util.IllegalActionException: BranchControllers must contain only input ports or only output ports; not both} {ptolemy.kernel.util.IllegalActionException: BranchControllers must contain only input ports or only output ports; not both}}



######################################################################
####
#
test BranchController-3.1 {Check pre-activation state} {
   
    set topLevel [java::new ptolemy.actor.CompositeActor]
    set cntlr [java::new ptolemy.actor.process.BranchController $topLevel]
    set branch [java::new ptolemy.actor.process.Branch $cntlr]
    
    set val 1
    set parent [$cntlr getParent]
    if { $parent != $topLevel } {
    	set val 0
    }
    
    list $val

} {1}

######################################################################
####
#
test BranchController-4.1 {activateBranches() with no branches} {
   
    set topLevel [java::new ptolemy.actor.CompositeActor]
    set cntlr [java::new ptolemy.actor.process.BranchController $topLevel]
    set branch [java::new ptolemy.actor.process.Branch $cntlr]
    
    set val 1
    
    if { [$cntlr isActive] != 0  } {
    	set val 0
    }
    
    $cntlr activateBranches
    
    if { [$cntlr isActive] != 0  } {
        set val 0
    }
    
    
    list $val

} {1}





