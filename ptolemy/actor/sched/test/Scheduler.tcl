# Tests for the Scheduler class
#
# @Author: Steve Neuendorffer
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


######################################################################
####
#
test Scheduler-2.1 {Constructor tests} {
    set s1 [java::new ptolemy.actor.sched.Scheduler]
    set w [java::new ptolemy.kernel.util.Workspace W]
    set s2 [java::new ptolemy.actor.sched.Scheduler $w]
    set s3 [java::new ptolemy.actor.sched.Scheduler $w]
    $s3 setName S3
    list [$s1 getFullName] [$s2 getFullName] [$s3 getFullName] 
} {.Scheduler .Scheduler .S3}

######################################################################
####
#
test Scheduler-3.1 {Test clone} {
    # NOTE: Uses the setup above
    set s4 [java::cast ptolemy.actor.sched.Scheduler \
            [$s2 clone $w]]
    $s4 setName S4
    enumToFullNames [$w directory]
} {.Scheduler .S3}

######################################################################
####
#
test Scheduler-4.1 {Test setScheduler and getScheduler} {
    # NOTE: Uses the setup above
    set d0 [java::new ptolemy.actor.sched.StaticSchedulingDirector $w]
    $d0 setName D0
    $d0 setScheduler $s2
    set d1 [$s2 getContainer]
    list [$d0 getFullName] [$d1 getFullName] [$s2 getFullName]
} {.D0 .D0 .D0.Scheduler}

######################################################################
####
#

test Scheduler-4.2 {Test setValid and isValid} {
    # NOTE: Uses the setup above
    $s1 setValid true
    set e0 [$s1 isValid]
    $s1 setValid false
    set e1 [$s1 isValid]
    list $e0 $e1
} {1 0}

######################################################################
####
#
test Scheduler-5.1 {Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.CompositeActor $w]
    set director [java::new ptolemy.actor.sched.StaticSchedulingDirector \
	    $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    set scheduler [java::new ptolemy.actor.sched.Scheduler $w]
    $director setScheduler $scheduler

    set a1 [java::new ptolemy.actor.test.TestActor $toplevel A1]
    set a2 [java::new ptolemy.actor.test.TestActor $toplevel A2]
    $scheduler setValid false

    _testEnums schedule $scheduler
    
} {{A1 A2}}
