# Tests for the PSDFScheduler class
#
# @Author: Shuvra S. Bhattacharyya based on a file by Steve Neuendorffer
#
# @Version: $Id$
#
# @Copyright (c) 1999-2003 The Regents of the University of California.
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
proc _initialize {toplevel} {
    [$toplevel getManager] initialize
#   [$toplevel getManager] wrapup
}

proc _getSchedule {scheduler} {
    list [objectsToNames [iterToObjects [[$scheduler getSchedule] actorIterator]]]
}

proc setTokenConsumptionRate {port rate} {
    set attribute [$port getAttribute tokenConsumptionRate]
    set parameter [java::cast ptolemy.data.expr.Parameter $attribute]
    $parameter setExpression $rate
    $parameter getToken
}

proc setTokenProductionRate {port rate} {
    set attribute [$port getAttribute tokenProductionRate]
    set parameter [java::cast ptolemy.data.expr.Parameter $attribute]
    $parameter setExpression $rate
    $parameter getToken
}

######################################################################
####
#
test PSDFScheduler-2.1 {Constructor tests} {
    set s1 [java::new ptolemy.domains.psdf.kernel.PSDFScheduler]
    set w [java::new ptolemy.kernel.util.Workspace W]
    set s2 [java::new ptolemy.domains.psdf.kernel.PSDFScheduler $w]
    set s3 [java::new ptolemy.domains.psdf.kernel.PSDFScheduler $w]
    $s3 setName S3
    list [$s1 getFullName] [$s2 getFullName] [$s3 getFullName]
} {.Scheduler .Scheduler .S3}

######################################################################
####
#
test PSDFScheduler-3.1 {Test clone} {
    # NOTE: Uses the setup above
    set s4 [java::cast ptolemy.domains.psdf.kernel.PSDFScheduler \
            [$s2 clone $w]]
    $s4 setName S4
    enumToFullNames [$w directory]
} {.Scheduler .S3}

######################################################################
####
#
test PSDFScheduler-4.1 {Test setScheduler and getScheduler} {
    # NOTE: Uses the setup above
    set e0 [java::new ptolemy.actor.TypedCompositeActor $w]
    $e0 setName E0
    set d0 [java::new ptolemy.domains.psdf.kernel.PSDFDirector $e0 D1]
    set s4 [java::new ptolemy.domains.psdf.kernel.PSDFScheduler $w]
    $s4 setName "TestScheduler"
    $d0 setScheduler $s4
    set d1 [$s4 getContainer]
    set e1 [java::new ptolemy.actor.TypedAtomicActor $e0 E1]
    set p1 [java::new ptolemy.actor.TypedIOPort $e1 P1]

    list [$d0 getFullName] [$d1 getFullName] [$s4 getFullName]
} {.E0.D1 .E0.D1 .E0.D1.TestScheduler}


######################################################################
####
#
# Tests 5.* test some simple scheduling tasks without hierarchy
test PSDFScheduler-5.1 {Scheduling tests} {
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set toplevel [java::new ptolemy.actor.TypedCompositeActor $w]
    set director [java::new ptolemy.domains.psdf.kernel.PSDFDirector $toplevel Director]
    $toplevel setName Toplevel
    $toplevel setManager $manager
    $toplevel setDirector $director
    set scheduler [java::cast ptolemy.domains.psdf.kernel.PSDFScheduler [$director getScheduler]]

    set a1 [java::new ptolemy.actor.lib.Ramp $toplevel Ramp]
    set a2 [java::new ptolemy.domains.sdf.lib.UpSample $toplevel UpSample]
    set a3 [java::new ptolemy.actor.lib.Discard $toplevel Discard]
    $toplevel connect [java::field $a1 {output ptolemy.actor.lib.Source}] [java::field $a2 {input ptolemy.domains.sdf.lib.SDFTransformer}] R1
    $toplevel connect [java::field $a2 {output ptolemy.domains.sdf.lib.SDFTransformer}] [java::field $a3 {input ptolemy.actor.lib.Sink}] R2
    $scheduler setValid false

    set schedule [$scheduler getSchedule]
    $schedule toString
} {Execute Symbolic Schedule{
Execute Symbolic Schedule{
Fire Actor ptolemy.actor.lib.Ramp {.Toplevel.Ramp}[(UpSample::input::tokenConsumptionRate) / (gcd(Ramp::output::tokenProductionRate, UpSample::input::tokenConsumptionRate))] times
Fire Actor ptolemy.domains.sdf.lib.UpSample {.Toplevel.UpSample}[(Ramp::output::tokenProductionRate) / (gcd(Ramp::output::tokenProductionRate, UpSample::input::tokenConsumptionRate))] times
}[(Discard::input::tokenConsumptionRate) / (gcd(((Ramp::output::tokenProductionRate) * (UpSample::output::tokenProductionRate)) / gcd(Ramp::output::tokenProductionRate, UpSample::input::tokenConsumptionRate), Discard::input::tokenConsumptionRate))] times
Fire Actor ptolemy.actor.lib.Discard {.Toplevel.Discard}[(((Ramp::output::tokenProductionRate) * (UpSample::output::tokenProductionRate)) / gcd(Ramp::output::tokenProductionRate, UpSample::input::tokenConsumptionRate)) / (gcd(((Ramp::output::tokenProductionRate) * (UpSample::output::tokenProductionRate)) / gcd(Ramp::output::tokenProductionRate, UpSample::input::tokenConsumptionRate), Discard::input::tokenConsumptionRate))] times
}[1] times}

