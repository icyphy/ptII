# Tests for the ODIOPort class
#
# @Author: John S. Davis II
#
# @Version: %W%	%G%
#
# @Copyright (c) 1997-1998 The Regents of the University of California.
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


######################################################################
####
#
test ODIOPort-3.1 {Receive tokens at different times along two channels} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.CompositeActor $wspc]
    # Note: Director not really needed since blocking won't occur in this test
    set dir [java::new ptolemy.domains.od.kernel.ODDirector $wspc "director"]
    $topLevel setDirector $dir
    set actorA [java::new ptolemy.domains.od.kernel.ODActor $topLevel "actorA"] 
    set actorB [java::new ptolemy.domains.od.kernel.ODActor $topLevel "actorB"] 
    set actorC [java::new ptolemy.domains.od.kernel.ODActor $topLevel "actorC"] 
    
    set portA [java::new ptolemy.domains.od.kernel.ODIOPort $actorA "portA"]
    $portA setOutput true
    
    set portB [java::new ptolemy.domains.od.kernel.ODIOPort $actorB "portB"]
    $portB setOutput true
    
    set portC1 [java::new ptolemy.domains.od.kernel.ODIOPort $actorC "portC1"]
    $portC1 setInput true
    
    set portC2 [java::new ptolemy.domains.od.kernel.ODIOPort $actorC "portC2"]
    $portC2 setInput true
    
    set rel1 [$topLevel connect $portA $portC1 "rel1"]
    set rel2 [$topLevel connect $portB $portC2 "rel2"]
    
    $dir initialize
    
    $actorB setPriorities
    
    set t1 [java::new ptolemy.data.Token]
    set t2 [java::new ptolemy.data.Token]
    set endToken [java::new ptolemy.data.Token]
    
    $portA send 0 $t1 10.0
    $portB send 0 $t2 5.0
    $portA send 0 $endToken 1000.0
    $portB send 0 $endToken 1000.0
    
    set t3 [$actorC getNextToken]
    set t4 [$actorC getNextToken]

    list [expr {$t3 == $t2} ] [expr {$t4 == $t1} ] 
} {1 1}














