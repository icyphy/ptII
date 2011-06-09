# Tests for the PSDFScheduler class
#
# @Author: Shuvra S. Bhattacharyya based on a file by Steve Neuendorffer
#
# @Version: $Id$
#
# @Copyright (c) 2004-2009 The Regents of the University of California.
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

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

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
test PSDFSchedulerErrors-1.0 {} {
    catch {createAndExecute "rateConsistency.xml"} foo
    list $foo
} {{ptolemy.kernel.util.IllegalActionException: Actor is not ready to fire.  Perhaps actor.prefire() returned false? Try debugging the actor by selecting "Listen to Actor".  Also, for SDF check moml for tokenConsumptionRate on input.
  in .SDFDirector4.PSDFDirector and .SDFDirector4.actor}}

test PSDFSchedulerErrors-1.1 {} {
    catch {createAndExecute "badRateChanges.xml"} foo
    list $foo
} {{ptolemy.kernel.util.IllegalActionException: Failed to compute schedule:
  in .badRateChanges.SDF Director
Because:
The SDF rate parameter may change. This is not allowed in SDF models that will be run through the code generator.  If you don't care about code generation, then you might consider setting the allowRateChanges parameter of the SDF director to false.
  in .badRateChanges.typed composite actor.port._tokenConsumptionRate}}

test PSDFSchedulerErrors-1.2 {} {
    catch {createAndExecute "badRateChanges2.xml"} foo
    list $foo
} {{ptolemy.actor.NoRoomException: Queue is at capacity of 2. Cannot put a token.
  in .badRateChanges2.NonStrictTest.input}}
