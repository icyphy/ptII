# Tests for the CompositeProcessDirector class
#
# @Author: John S. Davis II
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
set wkSpace [java::new ptolemy.kernel.util.Workspace "workspace"]
set manager [java::new ptolemy.actor.Manager $wkSpace "manager"]


######################################################################
####
#
test CompositeProcessDirector-2.1 {Test BranchController methods in flat hierarchy} {
    # Instantiate Directors and Composite Actors
    set topLevel [java::new ptolemy.actor.TypedCompositeActor $wkSpace]
    $topLevel setManager $manager
    $topLevel setName "topLevel"
    set dir [java::new ptolemy.actor.process.CompositeProcessDirector $topLevel "dir"]

    # Debug Listeners
    # $manager addDebugListener [java::new ptolemy.kernel.util.StreamListener]
    # $topLevel addDebugListener [java::new ptolemy.kernel.util.StreamListener]
    # $dir addDebugListener [java::new ptolemy.kernel.util.StreamListener]
    
    # Instantiate Atomic Actors
    set act1 [java::new ptolemy.actor.lib.Ramp $topLevel "act1"] 
    set act2 [java::new ptolemy.actor.process.test.ProcessSink $topLevel "act2"] 
    
    # Set Parameters
    set act1Limit [java::cast ptolemy.data.expr.Parameter [$act1 getAttribute firingCountLimit]]
    $act1Limit setToken [java::new ptolemy.data.IntToken 1]
    
    # Instantiate Ports
    set act1OutPort [$act1 getPort "output"]
    set act2InPort [$act2 getPort "input"]
    
    # Make Connections 
    $topLevel connect $act2InPort $act1OutPort
    
    $manager run
    
    set inCntlr [$dir getInputController]
    set outCntlr [$dir getOutputController]
    
    set val 1
    
    if { [$inCntlr hasBranches] != 0 } {
        $val = 0
    }
    if { [$outCntlr hasBranches] != 0 } {
        $val = 0
    }

    if { [$inCntlr isBlocked] != 1 } {
        $val = 0
    }
    if { [$outCntlr isBlocked] != 1 } {
        $val = 0
    }

    if { [$inCntlr isActive] != 0 } {
        $val = 0
    }
    if { [$outCntlr isActive] != 0 } {
        $val = 0
    }
    
    list $val

} {1}


