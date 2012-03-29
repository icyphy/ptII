# Tests for the DDFDirector class
#
# @Author: Christopher Brooks, based on SDFDirectory by Stephen Neuendorffer
#
# @Version: $Id$
#
# @Copyright (c) 2004-2012 The Regents of the University of California.
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

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs enumToObjects] == "" } then {
     source enums.tcl
}


proc setTokenConsumptionRate {port rate} {
    set attribute [$port getAttribute tokenConsumptionRate]
    set parameter [java::cast ptolemy.data.expr.Parameter $attribute]
    $parameter setExpression $rate
    $parameter validate
}

proc setTokenProductionRate {port rate} {
    set attribute [$port getAttribute tokenProductionRate]
    set parameter [java::cast ptolemy.data.expr.Parameter $attribute]
    $parameter setExpression $rate
    $parameter getToken
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test DDFDirector-2.1 {Constructor tests} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set d1 [java::new ptolemy.domains.ddf.kernel.DDFDirector]
    $d1 setName D1
    set d2 [java::new ptolemy.domains.ddf.kernel.DDFDirector $w]
    $d2 setName D2
    set e0 [java::new ptolemy.actor.TypedCompositeActor $w]
    $e0 setName E0
    set d3 [java::new ptolemy.domains.ddf.kernel.DDFDirector $e0 D3]
    list [$d1 getFullName] [$d2 getFullName] [$d3 getFullName]
} {.D1 .D2 .E0.D3}

######################################################################
####
#
test DDFDirector-3.1 {Test clone} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set d4 [java::cast ptolemy.domains.ddf.kernel.DDFDirector [$d2 clone $w]]
    $d4 setName D4
    enumToFullNames [$w directory]
} {.Manager}

######################################################################
####
#
test DDFDirector-4.1 {Test _makeDirectorOf} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set manager [java::new ptolemy.actor.Manager $w Manager]
    set e0 [java::new ptolemy.actor.TypedCompositeActor $w]
    $e0 setName E0
    set d3 [java::new ptolemy.domains.ddf.kernel.DDFDirector $e0 D3]
    set e1 [java::new ptolemy.actor.TypedCompositeActor $w]
    $e1 setName E1
    $e1 setManager $manager
    $e1 setDirector $d3
    list [$d3 getFullName] [$d4 getFullName] [enumToFullNames [$w directory]]
} {.E1.D3 .D4 {.E0 .E1}}

######################################################################
####
#
test DDFDirector-5.1 {Test action methods} {
    set listener [java::new ptolemy.kernel.util.RecorderListener]
    $d3 addDebugListener $listener
    set a1 [java::new ptolemy.domains.sdf.kernel.test.SDFTestRamp $e1 Ramp]
    set a2 [java::new ptolemy.domains.sdf.kernel.test.SDFTestConsumer $e1 Consumer]
    $e1 connect [java::field $a1 output] [java::field $a2 input] R1
    set iter [$d3 getAttribute iterations]

    # _testSetToken is defined in $PTII/util/testsuite/testParams.tcl
    _testSetToken $iter [java::new {ptolemy.data.IntToken int} 6]

    
    $manager run
    list [$a2 getHistory] [$listener getMessages]
} {{0
1
2
} {attribute ptolemy.data.expr.Parameter {.E1.D3.iterations} 6 changed
.E1.D3 Preinitializing ...
attribute ptolemy.kernel.util.SingletonConfigurableAttribute {.E1.D3._iconDescription} changed
attribute ptolemy.data.expr.Parameter {.E1.D3.startTime} value undefined changed
attribute ptolemy.data.expr.Parameter {.E1.D3.stopTime} value undefined changed
attribute ptolemy.data.expr.Parameter {.E1.D3.iterations} 6 changed
attribute ptolemy.data.expr.Parameter {.E1.D3.maximumReceiverCapacity} 0 changed
Invoking preinitialize():  .E1.Ramp
Invoking preinitialize():  .E1.Consumer
.E1.D3 Finished preinitialize().
Called initialize().
Invoking initialize():  .E1.Ramp
Initializing actor: .E1.Ramp.
Consumer: NOT_ENABLED
Ramp: ENABLED_NOT_DEFERRABLE
Invoking initialize():  .E1.Consumer
Initializing actor: .E1.Consumer.
Ramp: ENABLED_NOT_DEFERRABLE
Consumer: NOT_ENABLED
DDFDirector.initialize() finished.
DDFDirector.prefire()
iterationCount 0
Director: Called prefire().
-- Setting current time to 0.0
DDFDirector.prefire() returns true.
DDFDirector.fire()
The actor .E1.Ramp will be iterated.
The actor .E1.Ramp was iterated.
Consumer: ENABLED_NOT_DEFERRABLE
Ramp: ENABLED_DEFERRABLE
Director: Called postfire().
DDFDirector.prefire()
iterationCount 1
Director: Called prefire().
-- Setting current time to 0.0
DDFDirector.prefire() returns true.
DDFDirector.fire()
The actor .E1.Consumer will be iterated.
The actor .E1.Consumer was iterated.
Ramp: ENABLED_NOT_DEFERRABLE
Consumer: NOT_ENABLED
Director: Called postfire().
DDFDirector.prefire()
iterationCount 2
Director: Called prefire().
-- Setting current time to 0.0
DDFDirector.prefire() returns true.
DDFDirector.fire()
The actor .E1.Ramp will be iterated.
The actor .E1.Ramp was iterated.
Consumer: ENABLED_NOT_DEFERRABLE
Ramp: ENABLED_DEFERRABLE
Director: Called postfire().
DDFDirector.prefire()
iterationCount 3
Director: Called prefire().
-- Setting current time to 0.0
DDFDirector.prefire() returns true.
DDFDirector.fire()
The actor .E1.Consumer will be iterated.
The actor .E1.Consumer was iterated.
Ramp: ENABLED_NOT_DEFERRABLE
Consumer: NOT_ENABLED
Director: Called postfire().
DDFDirector.prefire()
iterationCount 4
Director: Called prefire().
-- Setting current time to 0.0
DDFDirector.prefire() returns true.
DDFDirector.fire()
The actor .E1.Ramp will be iterated.
The actor .E1.Ramp was iterated.
Consumer: ENABLED_NOT_DEFERRABLE
Ramp: ENABLED_DEFERRABLE
Director: Called postfire().
DDFDirector.prefire()
iterationCount 5
Director: Called prefire().
-- Setting current time to 0.0
DDFDirector.prefire() returns true.
DDFDirector.fire()
The actor .E1.Consumer will be iterated.
The actor .E1.Consumer was iterated.
Ramp: ENABLED_NOT_DEFERRABLE
Consumer: NOT_ENABLED
iteration limit reached
Director: Called wrapup().
}}

# See $PTII/ptolemy/domains/sdf/kernel/test/SDFDirector.tcl for other possible tests

