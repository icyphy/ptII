# Tests for the TotallyOrderedSet class
#
# @Author: Jie Liu
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

######################################################################
####  Generally used director.
#
set dir [java::new ptolemy.domains.ct.kernel.CTMultiSolverDirector]

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
} {.CTScheduler}

test CTScheduler-1.3 {sheduler and its container} {
    set ca [java::new ptolemy.actor.CompositeActor]
    $ca setName CA
    set dir [java::new ptolemy.domains.ct.kernel.CTMultiSolverDirector $ca Dir]
    list [[$dir getScheduler] getFullName]
} {.CA.Dir.CTScheduler}


######################################################################
####  Test schedules
#  
test CTScheduler-2.1 {schedule a chain of actors} {
    set ca [java::new ptolemy.actor.TypedCompositeActor]
    $ca setName CA
    set dir [java::new ptolemy.domains.ct.kernel.CTMultiSolverDirector $ca Dir]
    set sch [java::cast ptolemy.domains.ct.kernel.CTScheduler \
	    [$dir getScheduler]]
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
	 [enumToFullNames [$sch stateTransitionSSCActors]] \
	 [enumToFullNames [$sch outputSSCActors]] \
	 [enumToFullNames [$sch dynamicActorSchedule]] \
	 [enumToFullNames [$sch stateTransitionSchedule]] \
	 [enumToFullNames [$sch outputSchedule]]
} {{.CA.A1 .CA.A2 .CA.A3} {} {} {} {} {} {} {} {} {.CA.A1 .CA.A2 .CA.A3}}

test CTScheduler-2.2 {has one dynamic actor} { 
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
	 [enumToFullNames [$sch stateTransitionSSCActors]] \
	 [enumToFullNames [$sch outputSSCActors]] \
	 [enumToFullNames [$sch dynamicActorSchedule]] \
	 [enumToFullNames [$sch stateTransitionSchedule]] \
	 [enumToFullNames [$sch outputSchedule]]
} {{.CA.A1 .CA.A2 .CA.A3} .CA.Dyn {} {} {} {} {} .CA.Dyn\
	{.CA.A1 .CA.A2} .CA.A3}

test CTScheduler-2.3 {with one actor in a feedback} { 
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
	 [enumToFullNames [$sch stateTransitionSSCActors]] \
	 [enumToFullNames [$sch outputSSCActors]] \
	 [enumToFullNames [$sch dynamicActorSchedule]] \
	 [enumToFullNames [$sch stateTransitionSchedule]] \
	 [enumToFullNames [$sch outputSchedule]]
} {{.CA.A1 .CA.A2 .CA.A3 .CA.A4} .CA.Dyn {} {} {} {} {} .CA.Dyn\
	{.CA.A1 .CA.A4 .CA.A2} .CA.A3}

test CTScheduler-2.4 {chain of dynamic actors with feedback} { 
    #Note: use above setup.
    $pd1o unlink $r3
    set d2 [java::new ptolemy.domains.ct.kernel.test.CTDummyDynamicActor \
	    $ca D2]
    set pd2i [$d2 getPort input]
    set pd2o [$d2 getPort output]
    set rd [$ca connect $pd1o $pd2i RD]
    $pd2o link $r3
    list [enumToFullNames [$sch arithmaticActors]] \
	 [enumToFullNames [$sch dynamicActors]] \
	 [enumToFullNames [$sch eventGenerators]] \
	 [enumToFullNames [$sch eventInterpreters]] \
	 [enumToFullNames [$sch statefulActors]] \
	 [enumToFullNames [$sch stateTransitionSSCActors]] \
	 [enumToFullNames [$sch outputSSCActors]] \
	 [enumToFullNames [$sch dynamicActorSchedule]] \
	 [enumToFullNames [$sch stateTransitionSchedule]] \
	 [enumToFullNames [$sch outputSchedule]]
} {{.CA.A1 .CA.A2 .CA.A3 .CA.A4} {.CA.Dyn .CA.D2} {} {} {} {} {}\
 {.CA.D2 .CA.Dyn} {.CA.A1 .CA.A4 .CA.A2} .CA.A3}

test CTScheduler-2.5 { longer chain of dynamic actors with feedback} { 
    #Note: use above setup.
    $pd1o unlink $rd
    set d3 [java::new ptolemy.domains.ct.kernel.test.CTDummyDynamicActor \
	    $ca D3]
    set pd3i [$d3 getPort input]
    set pd3o [$d3 getPort output]
    set rd2 [$ca connect $pd1o $pd3i RD2]
    $pd3o link $rd
    list [enumToFullNames [$sch arithmaticActors]] \
	 [enumToFullNames [$sch dynamicActors]] \
	 [enumToFullNames [$sch eventGenerators]] \
	 [enumToFullNames [$sch eventInterpreters]] \
	 [enumToFullNames [$sch statefulActors]] \
	 [enumToFullNames [$sch stateTransitionSSCActors]] \
	 [enumToFullNames [$sch outputSSCActors]] \
	 [enumToFullNames [$sch dynamicActorSchedule]] \
	 [enumToFullNames [$sch stateTransitionSchedule]] \
	 [enumToFullNames [$sch outputSchedule]]
} {{.CA.A1 .CA.A2 .CA.A3 .CA.A4} {.CA.Dyn .CA.D2 .CA.D3} {} {} {} {} {}\
 {.CA.D2 .CA.D3 .CA.Dyn} {.CA.A1 .CA.A4 .CA.A2} .CA.A3}


test CTScheduler-2.6 {event generators and event interpreters} { 
    #Note: use above setup.
    set eg [java::new ptolemy.domains.ct.kernel.test.CTDummyEventGenerator \
	    $ca EG]
    set ei [java::new ptolemy.domains.ct.kernel.test.CTDummyWaveformGenerator \
	    $ca EI]
    set sc [java::new ptolemy.domains.ct.kernel.test.CTDummySSControlActor \
	    $ca SSC] 
    set a5 [java::new ptolemy.domains.ct.kernel.test.CTDummySink \
	    $ca A5] 
    #set ed [java::new ptolemy.domains.ct.kernel.test.CTDummyEventGenerator \
	#    $ca ED]
    set pegi [$eg getPort input]
    set pego [$eg getPort output]
    set peii [$ei getPort input]
    set peio [$ei getPort output]
    set psci [$sc getPort input]
    set psco [$sc getPort output]
    set p5i [$a5 getPort input]
    $pegi link $r3
    set rg [$ca connect $pego $psci RG]
    set ri [$ca connect $peii $psco RI]
    set r5 [$ca connect $peio $p5i R5]
    list [enumToFullNames [$sch arithmaticActors]] \
	 [enumToFullNames [$sch dynamicActors]] \
	 [enumToFullNames [$sch eventGenerators]] \
	 [enumToFullNames [$sch eventInterpreters]] \
	 [enumToFullNames [$sch statefulActors]] \
	 [enumToFullNames [$sch stateTransitionSSCActors]] \
	 [enumToFullNames [$sch outputSSCActors]] \
	 [enumToFullNames [$sch dynamicActorSchedule]] \
	 [enumToFullNames [$sch stateTransitionSchedule]] \
	 [enumToFullNames [$sch outputSchedule]]
} {{.CA.A1 .CA.A2 .CA.A3 .CA.A4 .CA.EG .CA.EI .CA.SSC .CA.A5}\
	{.CA.Dyn .CA.D2 .CA.D3} .CA.EG .CA.EI {} {} .CA.SSC\
	{.CA.D2 .CA.D3 .CA.Dyn}\
	{.CA.A1 .CA.A4 .CA.A2} {.CA.EG .CA.SSC .CA.EI .CA.A3 .CA.A5}}

test CTScheduler-2.7 {contained in a composite actor} { 
    #Note: use above setup.
    set pci1 [java::new ptolemy.actor.TypedIOPort $ca PCI1]
    set pci2 [java::new ptolemy.actor.TypedIOPort $ca PCI2]
    set pco1 [java::new ptolemy.actor.TypedIOPort $ca PCO1]
    set pco2 [java::new ptolemy.actor.TypedIOPort $ca PCO2]
    
    set a6 [java::new ptolemy.domains.ct.kernel.test.CTDummyStatefulActor \
	    $ca A6S]
    set a7 [java::new ptolemy.domains.ct.kernel.test.CTDummyStatefulActor \
	    $ca A7S]
    set ed [java::new ptolemy.domains.ct.kernel.test.CTDummyEventGenerator \
	    $ca ED]
    set p6i [$a6 getPort input]
    set p6o [$a6 getPort output]
    set p7i [$a7 getPort input]
    set p7o [$a7 getPort output]
    set pedi [$ed getPort input]
    set pedo [$ed getPort output]
    $p6i link $rd
    $pedi link $r3
    set r6c [java::new ptolemy.actor.TypedIORelation $ca R6C]
    $p6o link $r6c
    $pco1 link $r6c
    set r7 [$ca connect $p7o $p2i R7]
    set r7c [java::new ptolemy.actor.TypedIORelation $ca R7C]
    $p7i link $r7c
    $pci1 link $r7c
    set r2c [java::new ptolemy.actor.TypedIORelation $ca R2C]
    $p2i link $r2c
    $pci2 link $r2c
    set rdc [java::new ptolemy.actor.TypedIORelation $ca RDC]
    $pedo link $rdc
    $pco2 link $rdc
    list [enumToFullNames [$sch arithmaticActors]] \
	 [enumToFullNames [$sch dynamicActors]] \
	 [enumToFullNames [$sch eventGenerators]] \
	 [enumToFullNames [$sch eventInterpreters]] \
	 [enumToFullNames [$sch statefulActors]] \
	 [enumToFullNames [$sch stateTransitionSSCActors]] \
	 [enumToFullNames [$sch outputSSCActors]] \
	 [enumToFullNames [$sch dynamicActorSchedule]] \
	 [enumToFullNames [$sch stateTransitionSchedule]] \
	 [enumToFullNames [$sch outputSchedule]]
} {{.CA.A1 .CA.A2 .CA.A3 .CA.A4 .CA.EG .CA.EI .CA.SSC .CA.A5 .CA.A6S\
	.CA.A7S .CA.ED} {.CA.Dyn .CA.D2 .CA.D3} {.CA.EG .CA.ED} .CA.EI\
	{.CA.A6S .CA.A7S} {} .CA.SSC {.CA.D2 .CA.D3 .CA.Dyn}\
	{.CA.A1 .CA.A4 .CA.A7S .CA.A2}\
	{.CA.EG .CA.SSC .CA.EI .CA.A3 .CA.A5 .CA.A6S .CA.ED}}

test CTScheduler-2.8 {transparent arithmetic actor} { 
    #Note: use above setup.
    set pci3 [java::new ptolemy.actor.TypedIOPort $ca PCI3]
    set pco3 [java::new ptolemy.actor.TypedIOPort $ca PCO3]
    set a8 [java::new ptolemy.domains.ct.kernel.test.CTDummySISOActor \
	    $ca A8]
    set p8i [$a8 getPort input]
    set p8o [$a8 getPort output]
    set r8ic [java::new ptolemy.actor.TypedIORelation $ca R8IC]
    $p8i link $r8ic
    $pci3 link $r8ic
    set r8oc [java::new ptolemy.actor.TypedIORelation $ca R8OC]
    $p8o link $r8oc
    $pco3 link $r8oc
    list [enumToFullNames [$sch arithmaticActors]] \
	 [enumToFullNames [$sch dynamicActors]] \
	 [enumToFullNames [$sch eventGenerators]] \
	 [enumToFullNames [$sch eventInterpreters]] \
	 [enumToFullNames [$sch statefulActors]] \
	 [enumToFullNames [$sch stateTransitionSSCActors]] \
	 [enumToFullNames [$sch outputSSCActors]] \
	 [enumToFullNames [$sch dynamicActorSchedule]] \
	 [enumToFullNames [$sch stateTransitionSchedule]] \
	 [enumToFullNames [$sch outputSchedule]]
} {{.CA.A1 .CA.A2 .CA.A3 .CA.A4 .CA.EG .CA.EI .CA.SSC .CA.A5 .CA.A6S .CA.A7S .CA.ED .CA.A8} {.CA.Dyn .CA.D2 .CA.D3} {.CA.EG .CA.ED} .CA.EI {.CA.A6S .CA.A7S} {} .CA.SSC {.CA.D2 .CA.D3 .CA.Dyn} {.CA.A1 .CA.A4 .CA.A7S .CA.A2} {.CA.EG .CA.SSC .CA.EI .CA.A3 .CA.A5 .CA.A6S .CA.ED .CA.A8}}

test CTScheduler-2.8 {get the schedule again} {
    #Note: use above set up.
    list [enumToFullNames [$sch arithmaticActors]] \
	 [enumToFullNames [$sch dynamicActors]] \
	 [enumToFullNames [$sch eventGenerators]] \
	 [enumToFullNames [$sch eventInterpreters]] \
	 [enumToFullNames [$sch statefulActors]] \
	 [enumToFullNames [$sch stateTransitionSSCActors]] \
	 [enumToFullNames [$sch outputSSCActors]] \
	 [enumToFullNames [$sch dynamicActorSchedule]] \
	 [enumToFullNames [$sch stateTransitionSchedule]] \
	 [enumToFullNames [$sch outputSchedule]]
} {{.CA.A1 .CA.A2 .CA.A3 .CA.A4 .CA.EG .CA.EI .CA.SSC .CA.A5 .CA.A6S .CA.A7S .CA.ED .CA.A8} {.CA.Dyn .CA.D2 .CA.D3} {.CA.EG .CA.ED} .CA.EI {.CA.A6S .CA.A7S} {} .CA.SSC {.CA.D2 .CA.D3 .CA.Dyn} {.CA.A1 .CA.A4 .CA.A7S .CA.A2} {.CA.EG .CA.SSC .CA.EI .CA.A3 .CA.A5 .CA.A6S .CA.ED .CA.A8}}

######################################################################
#### Test toString
#
test CTScheduler-3.1 {get the description} {
    #Note: use above set up.
    list [$sch toString]
} {{CTSchedule {
    arithmaticActors {
	.CA.A1
	.CA.A2
	.CA.A3
	.CA.A4
	.CA.EG
	.CA.EI
	.CA.SSC
	.CA.A5
	.CA.A6S
	.CA.A7S
	.CA.ED
	.CA.A8
    }
    dynamicActors {
	.CA.Dyn
	.CA.D2
	.CA.D3
    }
    stateTransitionSSCActors {
    }
    outputSSCActors {
	.CA.SSC
    }
    eventGenerators {
	.CA.EG
	.CA.ED
    }
    eventInterpreters {
	.CA.EI
    }
    statefulActors {
	.CA.A6S
	.CA.A7S
    }
    sinkActors {
	.CA.A3
	.CA.A5
	.CA.A6S
	.CA.ED
	.CA.A8
    }
    dynamicActorSchedule {
	.CA.D2
	.CA.D3
	.CA.Dyn
    }
    stateTransitionSchedule {
	.CA.A1
	.CA.A4
	.CA.A7S
	.CA.A2
    }
    outputSchedule {
	.CA.EG
	.CA.SSC
	.CA.EI
	.CA.A3
	.CA.A5
	.CA.A6S
	.CA.ED
	.CA.A8
    }
}
}}
