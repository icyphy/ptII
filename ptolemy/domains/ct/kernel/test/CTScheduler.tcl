# Tests for the TotallyOrderedSet class
#
# @Author: Jie Liu
#
# @Version: $Id$
#
# @Copyright (c) 1998 The Regents of the University of California.
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

######################################################################
####  Generally used director.
#
set dir [java::new ptolemy.domains.ct.kernel.CTSingleSolverDirector]

######################################################################
####  Test constructors.
#
test CTSchedule-1.1 {Construct a CTScheduler and get name} {
    set a1 [java::new ptolemy.domains.ct.kernel.CTScheduler]
    list  [$a1 getName]
} {CTScheduler}

test CTScheduler-1.2 {Construct a CTScheduler in a workspace} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set a1 [java::new ptolemy.domains.ct.kernel.CTScheduler $w]
    list [$a1 getFullName]
} {W.CTScheduler}

test CTScheduler-1.3 {sheduler and its container} {
    set ca [java::new ptolemy.actor.CompositeActor]
    $ca setName CA
    set dir [java::new ptolemy.domains.ct.kernel.CTSingleSolverDirector Dir]
    $ca setDirector $dir
    list [[$dir getScheduler] getFullName]
} {.CA.Dir.CTScheduler}


######################################################################
####  Test schedules
#  
test CTScheduler-2.1 {schedule a chain of actors} {
    set ca [java::new ptolemy.actor.TypedCompositeActor]
    $ca setName CA
    set dir [java::new ptolemy.domains.ct.kernel.CTSingleSolverDirector Dir]
    $ca setDirector $dir
    set sch [$dir getScheduler]
    #### construct a chain with no feedback.
    set a1 [java::new ptolemy.domains.ct.kernel.test.CTDummySource $ca A1]
    set a2 [java::new ptolemy.domains.ct.kernel.test.CTDummyMISOActor $ca A2]
    set a3 [java::new ptolemy.domains.ct.kernel.test.CTDummySink $ca A3]
    set p1o [$a1 getPort output]
    set p2i [$a2 getPort input]
    set p2o [$a2 getPort output]
    set p3i [$a3 getPort input]
    set r1 [$ca connect $p1o $p2i R1]
    set r2 [$ca connect $p2o $p3i R2]
    list [enumToFullNames [$sch arithmaticActors]] \
	 [enumToFullNames [$sch dynamicActors]] \
	 [enumToFullNames [$sch eventGenerators]] \
	 [enumToFullNames [$sch eventInterpreters]] \
	 [enumToFullNames [$sch statefulActors]] \
	 [enumToFullNames [$sch stepSizeControlActors]] \
	 [enumToFullNames [$sch dynamicActorSchedule]] \
	 [enumToFullNames [$sch stateTransitionSchedule]] \
	 [enumToFullNames [$sch outputSchedule]]
} {{.CA.A1 .CA.A2 .CA.A3} {} {} {} {} {} {} {} {.CA.A1 .CA.A2 .CA.A3}}

test CTScheduler-2.1 {has one dynamic actor} { 
    #Note: use above setup.
    set d1 [java::new ptolemy.domains.ct.kernel.test.CTDummyDynamicActor \
	    $ca Dyn]
    set pd1i [$d1 getPort input]
    set pd1o [$d1 getPort output]
    $r2 setContainer [java::null]
    set r2 [$ca connect $p2o $pd1i R2]
    set r3 [$ca connect $pd1o $p3i R3]
    list [enumToFullNames [$sch arithmaticActors]] \
	 [enumToFullNames [$sch dynamicActors]] \
	 [enumToFullNames [$sch eventGenerators]] \
	 [enumToFullNames [$sch eventInterpreters]] \
	 [enumToFullNames [$sch statefulActors]] \
	 [enumToFullNames [$sch stepSizeControlActors]] \
	 [enumToFullNames [$sch dynamicActorSchedule]] \
	 [enumToFullNames [$sch stateTransitionSchedule]] \
	 [enumToFullNames [$sch outputSchedule]]
} {{.CA.A1 .CA.A2 .CA.A3} .CA.Dyn {} {} {} {} .CA.Dyn {.CA.A1 .CA.A2} .CA.A3}

test CTScheduler-2.1 {with one actor in a feedback} { 
    #Note: use above setup.
    set a4 [java::new ptolemy.domains.ct.kernel.test.CTDummySISOActor \
	    $ca A4]
    set p4i [$a4 getPort input]
    set p4o [$a4 getPort output]
    $p4i link $r3
    set r4 [$ca connect $p2i $p4o R4]
    list [enumToFullNames [$sch arithmaticActors]] \
	 [enumToFullNames [$sch dynamicActors]] \
	 [enumToFullNames [$sch eventGenerators]] \
	 [enumToFullNames [$sch eventInterpreters]] \
	 [enumToFullNames [$sch statefulActors]] \
	 [enumToFullNames [$sch stepSizeControlActors]] \
	 [enumToFullNames [$sch dynamicActorSchedule]] \
	 [enumToFullNames [$sch stateTransitionSchedule]] \
	 [enumToFullNames [$sch outputSchedule]]
} {{.CA.A1 .CA.A2 .CA.A3 .CA.A4} .CA.Dyn {} {} {} {} .CA.Dyn\
	{.CA.A1 .CA.A4 .CA.A2} .CA.A3}

######################################################################
####
#
