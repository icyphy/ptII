# Tests for the ODReceiver class
#
# @Author: John S. Davis II
#
# @Version: @(#)ODReceiver.tcl	1.3	11/18/98
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
test ODReceiver-2.1 {Single put/get; Check sizes} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.CompositeActor $wspc]
    # Note: Director not really needed since blocking won't occur in this test
    set dir [java::new ptolemy.domains.od.kernel.ODDirector $wspc "director"]
    $topLevel setDirector $dir
    set actor [java::new ptolemy.domains.od.kernel.ODActor $topLevel "actor"] 
    set iop [java::new ptolemy.domains.od.kernel.ODIOPort $actor "port"] 
    set rcvr [java::new ptolemy.domains.od.kernel.ODReceiver $iop]
    set initSize [$rcvr getSize]
    
    set t1 [java::new ptolemy.data.Token]
    $rcvr put $t1 5.0
    set afterPut [$rcvr getSize]
    
    set t2 [$rcvr get] 
    set afterGet [$rcvr getSize]
    
    list [expr { $t1 == $t2 } ] $initSize $afterPut $afterGet
} {1 0 1 0}

######################################################################
####
#
test ODReceiver-3.1 {put, get, put, put, get; Check token, sizes and times} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.CompositeActor $wspc]
    # Note: Director not really needed since blocking won't occur in this test
    set dir [java::new ptolemy.domains.od.kernel.ODDirector $wspc "director"]
    $topLevel setDirector $dir
    set actor [java::new ptolemy.domains.od.kernel.ODActor $topLevel "actor"] 
    set iop [java::new ptolemy.domains.od.kernel.ODIOPort $actor "port"] 
    set rcvr [java::new ptolemy.domains.od.kernel.ODReceiver $iop]
    set initSize [$rcvr getSize]
    
    set t1 [java::new ptolemy.data.Token]
    set t2 [java::new ptolemy.data.Token]
    set t3 [java::new ptolemy.data.Token]

    $rcvr put $t1 2.0
    $rcvr put $t2 2.5
    set time1 [$rcvr getRcvrTime]
    set size1 [$rcvr getSize]
    
    set t4 [$rcvr get]
    set size2 [$rcvr getSize]
    set time2 [$rcvr getRcvrTime]
    
    $rcvr put $t3 8.0
    set size3 [$rcvr getSize]
    set time3 [$rcvr getRcvrTime]
    set lastTime [$rcvr getLastTime]

    list $initSize $size1 $size2 $size3 $time1 $time2 $time3 $lastTime

} {0 2 1 2 2.0 2.5 2.5 8.0}

######################################################################
####
#
test ODReceiver-3.2 {put, put, put, get, get, get; Check token and times} {
    set wspc [java::new ptolemy.kernel.util.Workspace]
    set topLevel [java::new ptolemy.actor.CompositeActor $wspc]
    # Note: Director not really needed since blocking won't occur in this test
    set dir [java::new ptolemy.domains.od.kernel.ODDirector $wspc "director"]
    $topLevel setDirector $dir
    set actor [java::new ptolemy.domains.od.kernel.ODActor $topLevel "actor"] 
    set iop [java::new ptolemy.domains.od.kernel.ODIOPort $actor "port"] 
    set rcvr [java::new ptolemy.domains.od.kernel.ODReceiver $iop]
    set initSize [$rcvr getSize]
    set t1 [java::new ptolemy.data.Token]
    set t2 [java::new ptolemy.data.Token]
    set t3 [java::new ptolemy.data.Token]

    $rcvr put $t1 
    $rcvr put $t2 4.5
    $rcvr put $t3 5.0
    set time1 [$rcvr getRcvrTime]
    
    
    set t4 [$rcvr get]
    set time2 [$rcvr getRcvrTime]
    set t5 [$rcvr get]
    set t6 [$rcvr get]
    
    set size1 [$rcvr getSize]

    list $initSize [expr {$t1 == $t4} ] [expr {$t2 == $t5} ] [expr {$t3 == $t6} ] $time1 $time2
} {0 1 1 1 0.0 4.5}

######################################################################
####
#
#test ODReceiver-4.1 {Single get on an empty queue. Note: This test is commented out because it blocks. Nevertheless I initially ran it to verify that blocking occurs} {
#    set wspc [java::new ptolemy.kernel.util.Workspace]
#    set topLevel [java::new ptolemy.actor.CompositeActor $wspc]
#    set dir [java::new ptolemy.domains.od.kernel.ODDirector $wspc "director"]
#    $topLevel setDirector $dir
#    set actor [java::new ptolemy.domains.od.kernel.ODActor $topLevel "actor"] 
#    set iop [java::new ptolemy.domains.od.kernel.ODIOPort $actor "port"] 
#    set rcvr [java::new ptolemy.domains.od.kernel.ODReceiver $iop]
#    set t1 [java::new ptolemy.data.Token]
#    $rcvr get 
#} {}

######################################################################
####
#
#test ODReceiver-4.2 {Single put on a full queue. Note: This test is commented out because it blocks. Nevertheless I initially ran it to verify that blocking occurs} {
#    set wspc [java::new ptolemy.kernel.util.Workspace]
#    set topLevel [java::new ptolemy.actor.CompositeActor $wspc]
#    set dir [java::new ptolemy.domains.od.kernel.ODDirector $wspc "director"]
#    $topLevel setDirector $dir
#    set actor [java::new ptolemy.domains.od.kernel.ODActor $topLevel "actor"] 
#    set iop [java::new ptolemy.domains.od.kernel.ODIOPort $actor "port"] 
#    set rcvr [java::new ptolemy.domains.od.kernel.ODReceiver $iop]
#    $rcvr setCapacity 0
#    set t [java::new ptolemy.data.Token]
#    $rcvr put $t
#} {}













