# Tests for the Scheduler class
#
# @Author: Steve Neuendorffer
#
# @Version: $Id$
#
# @Copyright (c) 1998-2003 The Regents of the University of California.
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

# _testIterator
# This proc returns a list consisting of the names of all of the
# elements in the iterator returned by calling the method named by itermethod.
# If an element is null, then it return java::null.
#
# itermethod is the name of the method to be called on the object to get
# the enum.
#
# args is one or more objects.
#
proc _testIterator {itermethod args} {
    set results {}
    foreach objecttoenum $args {
	if {$objecttoenum == [java::null]} {
	    lappend results [java::null]
	} else {
	    set lresults {}
	    for {set enum [$objecttoenum $itermethod]} \
		    {$enum != [java::null] && \
		    [$enum hasNext] == 1} \
		    {} {
                set enumelement [$enum next]
		if [ java::instanceof $enumelement ptolemy.kernel.util.NamedObj] {
                         set enumelement \
                                 [java::cast ptolemy.kernel.util.NamedObj \
                                 $enumelement]
		    lappend lresults [$enumelement getName]
		} else {
		    lappend lresults $enumelement
		}
	    }
	    lappend results $lresults
	}
    }
    return $results
}


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
test Scheduler-6.1 {Test actorIterator method of Schedule} {
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
    set schedule [$scheduler getSchedule]
    _testIterator actorIterator $schedule
    
} {{A1 A2}}

######################################################################
####
#
test Scheduler-6.2 {Test firingIterator method of Schedule} {
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
    set schedule [$scheduler getSchedule]
    set firingit [$schedule firingIterator]
    $firingit hasNext
    set firing1 [$firingit next]
    set firing1 [java::cast ptolemy.actor.sched.Firing \
                                 $firing1]
    $firingit hasNext
    set firing2 [$firingit next]
    set firing2 [java::cast ptolemy.actor.sched.Firing \
                                 $firing2]
    set actor1 [$firing1 getActor]
    set actor1 [java::cast ptolemy.actor.AtomicActor \
                                 $actor1]
    set actor2 [$firing2 getActor]
    set actor2 [java::cast ptolemy.actor.AtomicActor \
                                 $actor2]
    list [$actor1 getName] [$actor2 getName]
    
} {A1 A2}

######################################################################
####
#
test Scheduler-6.3 {Test iterator method of Schedule} {
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
    set schedule [$scheduler getSchedule]
    set iter [$schedule iterator]
    set firing1 [$iter next]
    set firing1 [java::cast ptolemy.actor.sched.Firing \
                                 $firing1]
    set firing2 [$iter next]
    set firing2 [java::cast ptolemy.actor.sched.Firing \
                                 $firing2]
    set actor1 [$firing1 getActor]
    set actor1 [java::cast ptolemy.actor.AtomicActor \
                                 $actor1]
    set actor2 [$firing2 getActor]
    set actor2 [java::cast ptolemy.actor.AtomicActor \
                                 $actor2]
    list [$actor1 getName] [$actor2 getName]
    
} {A1 A2}

######################################################################
####
# This is the schedule in the Schedule.java class documentation.
test Scheduler-6.4 {Test Schedule.firingIterator} {
    # Create actors
    set toplevel [java::new ptolemy.actor.CompositeActor $w]
    set a [java::new ptolemy.actor.test.TestActor $toplevel A]
    set b [java::new ptolemy.actor.test.TestActor $toplevel B]
    set c [java::new ptolemy.actor.test.TestActor $toplevel C]
    set d [java::new ptolemy.actor.test.TestActor $toplevel D]

    # Construct schedule (1, A, (3, B, C), (2, D)). 
    set S [java::new ptolemy.actor.sched.Schedule]
    set S1 [java::new ptolemy.actor.sched.Firing]
    set S2 [java::new ptolemy.actor.sched.Schedule]
    set S3 [java::new ptolemy.actor.sched.Firing]
    $S add $S1
    $S add $S2
    $S add $S3
    $S1 setActor $a
    $S2 setIterationCount 3
    set S2_1 [java::new ptolemy.actor.sched.Firing]
    set S2_2 [java::new ptolemy.actor.sched.Firing]
    $S2_1 setActor $b
    $S2_2 setActor $c
    $S2 add $S2_1
    $S2 add $S2_2
    $S3 setIterationCount 2
    $S3 setActor $d

    # Test the schedule

    # Test firingIterator
    set firingIterator [$S firingIterator]

    set firing1 [$firingIterator next]
    set firing1 [java::cast ptolemy.actor.sched.Firing \
                                 $firing1]
    set firing2 [$firingIterator next]
    set firing2 [java::cast ptolemy.actor.sched.Firing \
                                 $firing2]
    set firing3 [$firingIterator next]
    set firing3 [java::cast ptolemy.actor.sched.Firing \
                                 $firing3]
    set firing4 [$firingIterator next]
    set firing4 [java::cast ptolemy.actor.sched.Firing \
                                 $firing4]
    set firing5 [$firingIterator next]
    set firing5 [java::cast ptolemy.actor.sched.Firing \
                                 $firing5]
    set firing6 [$firingIterator next]
    set firing6 [java::cast ptolemy.actor.sched.Firing \
                                 $firing6]
    set firing7 [$firingIterator next]
    set firing7 [java::cast ptolemy.actor.sched.Firing \
                                 $firing7]
    set firing8 [$firingIterator next]
    set firing8 [java::cast ptolemy.actor.sched.Firing \
                                 $firing8]
    set actor1 [$firing1 getActor]
    set actor1 [java::cast ptolemy.actor.AtomicActor \
                                 $actor1]
    set iterationCount1 [$firing1 getIterationCount]

    set actor2 [$firing2 getActor]
    set actor2 [java::cast ptolemy.actor.AtomicActor \
                                 $actor2]
    set iterationCount2 [$firing2 getIterationCount]

    set actor3 [$firing3 getActor]
    set actor3 [java::cast ptolemy.actor.AtomicActor \
                                 $actor3]
    set iterationCount3 [$firing3 getIterationCount]

    set actor4 [$firing4 getActor]
    set actor4 [java::cast ptolemy.actor.AtomicActor \
                                 $actor4]
    set iterationCount4 [$firing4 getIterationCount]

    set actor5 [$firing5 getActor]
    set actor5 [java::cast ptolemy.actor.AtomicActor \
                                 $actor5]
    set iterationCount5 [$firing5 getIterationCount]
    
    set actor6 [$firing6 getActor]
    set actor6 [java::cast ptolemy.actor.AtomicActor \
                                 $actor6]
    set iterationCount6 [$firing6 getIterationCount]
    
    set actor7 [$firing7 getActor]
    set actor7 [java::cast ptolemy.actor.AtomicActor \
                                 $actor7]
    set iterationCount7 [$firing7 getIterationCount]
    
    set actor8 [$firing8 getActor]
    set actor8 [java::cast ptolemy.actor.AtomicActor \
                                 $actor8]
    set iterationCount8 [$firing8 getIterationCount]

    list \
	    $iterationCount1 [$actor1 getName] $iterationCount2 [$actor2 getName] $iterationCount3 [$actor3 getName] $iterationCount4 [$actor4 getName] $iterationCount5 [$actor5 getName] $iterationCount6 [$actor6 getName] $iterationCount7 [$actor7 getName] $iterationCount8 [$actor8 getName]
    
} {1 A 1 B 1 C 1 B 1 C 1 B 1 C 2 D}

######################################################################
####
# 
test Scheduler-6.4.1 {Test Firing.ActorIterator} {
    # Uses test 6.4 above

    # We call _testIterator so as to 
    # get better test coverage of Firing.actorIterator()

    _testIterator actorIterator $firing1 $firing2 $firing3 $firing4 $firing5 $firing6 $firing7 $firing8
} {{A A} {B B} {C C} {B B} {C C} {B B} {C C} {D D D}}

######################################################################
####
# 
test Scheduler-6.4.2 {Test Firing.FiringIterator} {
    # Uses test 6.4 above
    # Cover Firings.firingIterator()
    set firings [_testIterator firingIterator $firing1 $firing2 $firing3 $firing4 $firing5 $firing6 $firing7 $firing8]

    objectsToStrings $firings
} {{Fire Actor ptolemy.actor.test.TestActor {..A}} {Fire Actor ptolemy.actor.test.TestActor {..B}} {Fire Actor ptolemy.actor.test.TestActor {..C}} {Fire Actor ptolemy.actor.test.TestActor {..B}} {Fire Actor ptolemy.actor.test.TestActor {..C}} {Fire Actor ptolemy.actor.test.TestActor {..B}} {Fire Actor ptolemy.actor.test.TestActor {..C}} {Fire Actor ptolemy.actor.test.TestActor {..D} 2 times}}

######################################################################
####
# 
test Scheduler-6.4.3 {Test Firing.ActorIterator.remove()} {
    # Uses test 6.4 above

    # remove() is unsupported
    set firingIterator [$firing8 firingIterator]
    $firingIterator hasNext
    $firingIterator next
    # This should throw an UnsupportedOperationException, but
    # it does not because we do not have our own inner class implementation
    # of this method.
    catch {$firingIterator remove} errMsg1

    set actorIterator [$firing8 actorIterator]
    catch {$actorIterator remove} errMsg2


    list $errMsg1 $errMsg2
} {java.lang.UnsupportedOperationException java.lang.UnsupportedOperationException}

######################################################################
####
# 
test Scheduler-6.4.4 {Test Firing.next(), hasNext()} {
    # Uses test 6.4 above

    set firingIterator [$firing8 firingIterator]
    $firing8 setActor $a

    # This should throw a ConcurrentModificationException
    catch {$firingIterator hasNext} errMsg1
    catch {$firingIterator next} errMsg2

    list $errMsg1 $errMsg2
} {java.util.ConcurrentModificationException java.util.ConcurrentModificationException} {KNOWN_FAILURE, firingIterator.hasNext() should throw ConcurrentModificationException}

######################################################################
####
# 
test Scheduler-6.4.5 {Test Firing.ActorIterator.next(), hasNext} {
    # Uses test 6.4 above

    set actorIterator [$firing8 actorIterator]
    $firing8 setActor $a

    catch {$actorIterator hasNext} errMsg1
    catch {$actorIterator next} errMsg2

    list $errMsg1 $errMsg2
} {{java.util.ConcurrentModificationException: Schedule structure changed while iterator is active.} {java.util.ConcurrentModificationException: Schedule structure changed while iterator is active.}}

######################################################################
####
# 
test Scheduler-6.5 {Test Schedule.firingIterator} {
    # Create actors
    set toplevel [java::new ptolemy.actor.CompositeActor $w]
    set a [java::new ptolemy.actor.test.TestActor $toplevel A]
    set b [java::new ptolemy.actor.test.TestActor $toplevel B]
    set c [java::new ptolemy.actor.test.TestActor $toplevel C]
    set d [java::new ptolemy.actor.test.TestActor $toplevel D]

    # Construct schedule (2, A, (3, B, C), (2, D)). 
    set S [java::new ptolemy.actor.sched.Schedule]
    set S1 [java::new ptolemy.actor.sched.Firing]
    set S2 [java::new ptolemy.actor.sched.Schedule]
    set S3 [java::new ptolemy.actor.sched.Firing]
    $S add $S1
    $S add $S2
    $S add $S3
    $S setIterationCount 2
    $S1 setActor $a
    $S2 setIterationCount 3
    set S2_1 [java::new ptolemy.actor.sched.Firing]
    set S2_2 [java::new ptolemy.actor.sched.Firing]
    $S2_1 setActor $b
    $S2_2 setActor $c
    $S2 add $S2_1
    $S2 add $S2_2
    $S3 setIterationCount 2
    $S3 setActor $d

    # Test the schedule

    # Test firingIterator
    set firingIterator [$S firingIterator]

    # remove() is unsupported
    catch {$firingIterator remove} errMsg

    set firing1 [$firingIterator next]
    set firing1 [java::cast ptolemy.actor.sched.Firing \
                                 $firing1]
    set firing2 [$firingIterator next]
    set firing2 [java::cast ptolemy.actor.sched.Firing \
                                 $firing2]
    set firing3 [$firingIterator next]
    set firing3 [java::cast ptolemy.actor.sched.Firing \
                                 $firing3]
    set firing4 [$firingIterator next]
    set firing4 [java::cast ptolemy.actor.sched.Firing \
                                 $firing4]
    set firing5 [$firingIterator next]
    set firing5 [java::cast ptolemy.actor.sched.Firing \
                                 $firing5]
    set firing6 [$firingIterator next]
    set firing6 [java::cast ptolemy.actor.sched.Firing \
                                 $firing6]
    set firing7 [$firingIterator next]
    set firing7 [java::cast ptolemy.actor.sched.Firing \
                                 $firing7]
    set firing8 [$firingIterator next]
    set firing8 [java::cast ptolemy.actor.sched.Firing \
                                 $firing8]
    set firing9 [$firingIterator next]
    set firing9 [java::cast ptolemy.actor.sched.Firing \
                                 $firing9]
    set firing10 [$firingIterator next]
    set firing10 [java::cast ptolemy.actor.sched.Firing \
                                 $firing10]
    set firing11 [$firingIterator next]
    set firing11 [java::cast ptolemy.actor.sched.Firing \
                                 $firing11]
    set firing12 [$firingIterator next]
    set firing12 [java::cast ptolemy.actor.sched.Firing \
                                 $firing12]
    set firing13 [$firingIterator next]
    set firing13 [java::cast ptolemy.actor.sched.Firing \
                                 $firing13]
    set firing14 [$firingIterator next]
    set firing14 [java::cast ptolemy.actor.sched.Firing \
                                 $firing14]
    set firing15 [$firingIterator next]
    set firing15 [java::cast ptolemy.actor.sched.Firing \
                                 $firing15]
    set firing16 [$firingIterator next]
    set firing16 [java::cast ptolemy.actor.sched.Firing \
                                 $firing16]
    set actor1 [$firing1 getActor]
    set actor1 [java::cast ptolemy.actor.AtomicActor \
                                 $actor1]
    set iterationCount1 [$firing1 getIterationCount]

    set actor2 [$firing2 getActor]
    set actor2 [java::cast ptolemy.actor.AtomicActor \
                                 $actor2]
    set iterationCount2 [$firing2 getIterationCount]

    set actor3 [$firing3 getActor]
    set actor3 [java::cast ptolemy.actor.AtomicActor \
                                 $actor3]
    set iterationCount3 [$firing3 getIterationCount]

    set actor4 [$firing4 getActor]
    set actor4 [java::cast ptolemy.actor.AtomicActor \
                                 $actor4]
    set iterationCount4 [$firing4 getIterationCount]

    set actor5 [$firing5 getActor]
    set actor5 [java::cast ptolemy.actor.AtomicActor \
                                 $actor5]
    set iterationCount5 [$firing5 getIterationCount]
    
    set actor6 [$firing6 getActor]
    set actor6 [java::cast ptolemy.actor.AtomicActor \
                                 $actor6]
    set iterationCount6 [$firing6 getIterationCount]
    
    set actor7 [$firing7 getActor]
    set actor7 [java::cast ptolemy.actor.AtomicActor \
                                 $actor7]
    set iterationCount7 [$firing7 getIterationCount]
    
    set actor8 [$firing8 getActor]
    set actor8 [java::cast ptolemy.actor.AtomicActor \
                                 $actor8]
    set iterationCount8 [$firing8 getIterationCount]
    set actor9 [$firing9 getActor]
    set actor9 [java::cast ptolemy.actor.AtomicActor \
                                 $actor9]
    set iterationCount9 [$firing1 getIterationCount]

    set actor10 [$firing10 getActor]
    set actor10 [java::cast ptolemy.actor.AtomicActor \
                                 $actor10]
    set iterationCount10 [$firing10 getIterationCount]

    set actor11 [$firing11 getActor]
    set actor11 [java::cast ptolemy.actor.AtomicActor \
                                 $actor11]
    set iterationCount11 [$firing11 getIterationCount]

    set actor12 [$firing12 getActor]
    set actor12 [java::cast ptolemy.actor.AtomicActor \
                                 $actor12]
    set iterationCount12 [$firing12 getIterationCount]

    set actor13 [$firing13 getActor]
    set actor13 [java::cast ptolemy.actor.AtomicActor \
                                 $actor13]
    set iterationCount13 [$firing13 getIterationCount]
    
    set actor14 [$firing14 getActor]
    set actor14 [java::cast ptolemy.actor.AtomicActor \
                                 $actor14]
    set iterationCount14 [$firing14 getIterationCount]
    
    set actor15 [$firing15 getActor]
    set actor15 [java::cast ptolemy.actor.AtomicActor \
                                 $actor15]
    set iterationCount15 [$firing15 getIterationCount]
    
    set actor16 [$firing16 getActor]
    set actor16 [java::cast ptolemy.actor.AtomicActor \
                                 $actor16]
    set iterationCount16 [$firing16 getIterationCount]


    list $errMsg $iterationCount1 [$actor1 getName] $iterationCount2 [$actor2 getName] $iterationCount3 [$actor3 getName] $iterationCount4 [$actor4 getName] $iterationCount5 [$actor5 getName] $iterationCount6 [$actor6 getName] $iterationCount7 [$actor7 getName] $iterationCount8 [$actor8 getName] $iterationCount9 [$actor9 getName] $iterationCount10 [$actor10 getName] $iterationCount11 [$actor11 getName] $iterationCount12 [$actor12 getName] $iterationCount13 [$actor13 getName] $iterationCount14 [$actor14 getName] $iterationCount15 [$actor15 getName] $iterationCount16 [$actor16 getName]
    
} {java.lang.UnsupportedOperationException 1 A 1 B 1 C 1 B 1 C 1 B 1 C 2 D 1 A 1 B 1 C 1 B 1 C 1 B 1 C 2 D}


######################################################################
####
#
test Scheduler-6.6 {Test Schedule.actorIterator for simple schedule} {
    # Create actors
    set toplevel [java::new ptolemy.actor.CompositeActor $w]
    set a [java::new ptolemy.actor.test.TestActor $toplevel A]
    set b [java::new ptolemy.actor.test.TestActor $toplevel B]
    #set c [java::new ptolemy.actor.test.TestActor $toplevel C]
    #set d [java::new ptolemy.actor.test.TestActor $toplevel D]

    # Construct schedule (4, A)
    set S [java::new ptolemy.actor.sched.Schedule]
    set S1 [java::new ptolemy.actor.sched.Firing]
    #set S2 [java::new ptolemy.actor.sched.Schedule]
    #set S3 [java::new ptolemy.actor.sched.Firing]
    $S add $S1
    #$S add $S2
    #$S add $S3
    $S setIterationCount 4
    $S1 setActor $a
    #$S2 setIterationCount 3
    #set S2_1 [java::new ptolemy.actor.sched.Firing]
    #set S2_2 [java::new ptolemy.actor.sched.Firing]
    #$S2_1 setActor $b
    #$S2_2 setActor $c
    #$S2 add $S2_1
    #$S2 add $S2_2
    #$S3 setIterationCount 2
    #$S3 setActor $d

    # Test the schedule

    # Test actorIterator
    set actorIterator [$S actorIterator]

    # remove() is unsupported
    catch {$actorIterator remove} errMsg

    set actor1 [$actorIterator next]
    set actor1 [java::cast ptolemy.actor.AtomicActor \
                                 $actor1]

    set actor2 [$actorIterator next]
    set actor2 [java::cast ptolemy.actor.AtomicActor \
                                 $actor2]

    set actor3 [$actorIterator next]
    set actor3 [java::cast ptolemy.actor.AtomicActor \
                                 $actor3]

    set actor4 [$actorIterator next]
    set actor4 [java::cast ptolemy.actor.AtomicActor \
                                 $actor4]
    

    list $errMsg [$actor1 getName] [$actor2 getName] [$actor3 getName] [$actor4 getName]
    
} {java.lang.UnsupportedOperationException A A A A}

######################################################################
####
# This is the schedule in the Schedule.java class documentation.
test Scheduler-6.7 {Test Schedule.actorIterator for complex schedule} {
    # Create actors
    set toplevel [java::new ptolemy.actor.CompositeActor $w]
    set a [java::new ptolemy.actor.test.TestActor $toplevel A]
    set b [java::new ptolemy.actor.test.TestActor $toplevel B]
    set c [java::new ptolemy.actor.test.TestActor $toplevel C]
    set d [java::new ptolemy.actor.test.TestActor $toplevel D]

    # Construct schedule (1, A, (3, B, C), (2, D)). 
    set S [java::new ptolemy.actor.sched.Schedule]
    set S1 [java::new ptolemy.actor.sched.Firing]
    set S2 [java::new ptolemy.actor.sched.Schedule]
    set S3 [java::new ptolemy.actor.sched.Firing]
    $S add $S1
    $S add $S2
    $S add $S3
    $S1 setActor $a
    $S2 setIterationCount 3
    set S2_1 [java::new ptolemy.actor.sched.Firing]
    set S2_2 [java::new ptolemy.actor.sched.Firing]
    $S2_1 setActor $b
    $S2_2 setActor $c
    $S2 add $S2_1
    $S2 add $S2_2
    $S3 setIterationCount 2
    $S3 setActor $d

    # Test the schedule

    # Test actorIterator
    set actorIterator [$S actorIterator]

    set actor1 [$actorIterator next]
    set actor1 [java::cast ptolemy.actor.AtomicActor \
                                 $actor1]

    set actor2 [$actorIterator next]
    set actor2 [java::cast ptolemy.actor.AtomicActor \
                                 $actor2]

    set actor3 [$actorIterator next]
    set actor3 [java::cast ptolemy.actor.AtomicActor \
                                 $actor3]

    set actor4 [$actorIterator next]
    set actor4 [java::cast ptolemy.actor.AtomicActor \
                                 $actor4]

    set actor5 [$actorIterator next]
    set actor5 [java::cast ptolemy.actor.AtomicActor \
                                 $actor5]

    set actor6 [$actorIterator next]
    set actor6 [java::cast ptolemy.actor.AtomicActor \
                                 $actor6]

    set actor7 [$actorIterator next]
    set actor7 [java::cast ptolemy.actor.AtomicActor \
                                 $actor7]

    set actor8 [$actorIterator next]
    set actor8 [java::cast ptolemy.actor.AtomicActor \
                                 $actor8]

    set actor9 [$actorIterator next]
    set actor9 [java::cast ptolemy.actor.AtomicActor \
                                 $actor9]

    list [$actor1 getName] [$actor2 getName] [$actor3 getName] [$actor4 getName] [$actor5 getName] [$actor6 getName] [$actor7 getName] [$actor8 getName] [$actor9 getName]
    
} {A B C B C B C D D}

######################################################################
####
# This is the schedule in the Schedule.java class documentation.
test Scheduler-6.8 {Test Schedule.actorIterator for another schedule} {
    # Create actors
    set toplevel [java::new ptolemy.actor.CompositeActor $w]
    set a [java::new ptolemy.actor.test.TestActor $toplevel A]
    set b [java::new ptolemy.actor.test.TestActor $toplevel B]
    set c [java::new ptolemy.actor.test.TestActor $toplevel C]

    # Construct schedule (1, (1, A, B, C), (1, null)) 
    set S [java::new ptolemy.actor.sched.Schedule]
    set S1 [java::new ptolemy.actor.sched.Schedule]
    set S2 [java::new ptolemy.actor.sched.Schedule]
    set S2_1 [java::new ptolemy.actor.sched.Firing $a]
    set S2_2 [java::new ptolemy.actor.sched.Firing $b]
    set S2_3 [java::new ptolemy.actor.sched.Firing $c]
    $S1 add $S2_1
    $S1 add $S2_2
    $S1 add $S2_3
    $S add $S1
    $S add $S2

    # Test the schedule

    # Test actorIterator on subschedule (1, A, B, C)
    set actorIterator [$S1 actorIterator]

    set actor1 [$actorIterator next]
    set actor1 [java::cast ptolemy.actor.AtomicActor \
                                 $actor1]

    set actor2 [$actorIterator next]
    set actor2 [java::cast ptolemy.actor.AtomicActor \
                                 $actor2]

    set actor3 [$actorIterator next]
    set actor3 [java::cast ptolemy.actor.AtomicActor \
                                 $actor3]
    $actorIterator hasNext

    list [$actor1 getName] [$actor2 getName] [$actor3 getName] 
    
} {A B C}


######################################################################
####
# This is the schedule in the Schedule.java class documentation.
test Scheduler-6.8 { Schedule.actorIterator for another schedule} {
    # Create actors
    set toplevel [java::new ptolemy.actor.CompositeActor $w]
    set a [java::new ptolemy.actor.test.TestActor $toplevel A]
    set b [java::new ptolemy.actor.test.TestActor $toplevel B]
    set c [java::new ptolemy.actor.test.TestActor $toplevel C]

    # Construct schedule (1, (1, A, B, C), (1, null)) 
    set S [java::new ptolemy.actor.sched.Schedule]
    set S1 [java::new ptolemy.actor.sched.Schedule]
    set S2 [java::new ptolemy.actor.sched.Schedule]
    set S2_1 [java::new ptolemy.actor.sched.Firing $a]
    set S2_2 [java::new ptolemy.actor.sched.Firing $b]
    set S2_3 [java::new ptolemy.actor.sched.Firing $c]
    $S1 add $S2_1
    $S1 add $S2_2
    $S1 add $S2_3
    $S add $S1
    $S add $S2

    # Test the schedule

    # Test actorIterator on subschedule (1, A, B, C)
    set actorIterator [$S1 actorIterator]

    $S1 remove 0

    catch {set actor1 [$actorIterator next]} errMsg
    catch {set actor1 [$actorIterator next]} errMsg2
    list $errMsg
} {{java.util.ConcurrentModificationException: Schedule structure changed while iterator is active.}}


