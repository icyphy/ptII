# Tests for the CTDirector and CTMultiSolverDirector class
#
# @Author: Jie Liu
#
# @Version: $Id$
#
# @Copyright (c) 1998-2006 The Regents of the University of California.
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

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####  Test constructors.
#
test CTDirector-1.1 {Construct a Director and get name} {
    set d1 [java::new ptolemy.domains.ct.kernel.CTMultiSolverDirector]
    list  [$d1 getName]
} {{}}

test CTDirector-1.2 {Construct a Director in a workspace} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set d1 [java::new ptolemy.domains.ct.kernel.CTMultiSolverDirector $w]
    list  [$d1 getFullName]
} {.}

test CTDirector-1.3 {Construct with a name and a container} {
    set ca [java::new ptolemy.actor.TypedCompositeActor $w]
    $ca setName CA
    set d2 [java::new ptolemy.domains.ct.kernel.CTMultiSolverDirector $ca DIR2]
    list [$d2 getFullName]
} {.CA.DIR2}


######################################################################
####  Test methods in (abstract) CTDirector
#
test CTDirector-2.1 {Get default values} {
    set sys [java::new ptolemy.actor.TypedCompositeActor]
    $sys setName System
    set dir [java::new ptolemy.domains.ct.kernel.CTMultiSolverDirector $sys DIR]
    $dir preinitialize
    $dir initialize
    list [[$dir getCurrentODESolver] getFullName] \
	    [$dir getCurrentStepSize] \
	    [$dir getCurrentTime] \
	    [[$dir getIterationBeginTime] getDoubleValue] \
	    [$dir getInitialStepSize] \
	    [$dir getErrorTolerance] \
	    [$dir getMaxIterations] \
	    [$dir getMaxStepSize] \
	    [$dir getMinStepSize] \
	    [$dir getNextIterationTime] \
	    [$dir getStopTime] \
	    [$dir getSuggestedNextStepSize] \
	    [$dir getTimeResolution] \
	    [$dir getValueResolution]
} {.System.DIR.CT_Runge_Kutta_2_3_Solver 0.1 0.0 0.0 0.1 0.0001 20 1.0 1e-05 0.1 Infinity 0.1 1e-10 1e-06}


######################################################################
####  Test set parameters.
#    
test CTDirector-2.2 {set Parameters by expression} {
    #Note: Use above set up.
    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute ODESolver]]
    set token [java::new ptolemy.data.StringToken \
	    ptolemy.domains.ct.kernel.solver.BackwardEulerSolver]
    $param setToken $token
    $param getToken

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute initStepSize]]
    $param setExpression 0.5
    $param getToken

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute errorTolerance]]
    $param setExpression 0.4
    $param getToken

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute maxIterations]]
    $param setExpression 10
    $param getToken

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute maxStepSize]]
    $param setExpression 0.3
    $param getToken

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute minStepSize]]
    $param setExpression 0.2
    $param getToken

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute startTime]]
    $param setExpression 10.0
    $param getToken

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute stopTime]]
    $param setExpression 100.0
    $param getToken

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute timeResolution]]
    $param setExpression {1E-11}
    $param getToken

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute valueResolution]]
    $param setExpression 0.1
    $param getToken

    $dir preinitialize
    list [[$dir getCurrentODESolver] getFullName] \
	    [$dir getCurrentStepSize] \
	    [$dir getCurrentTime] \
	    [[$dir getIterationBeginTime] getDoubleValue] \
	    [$dir getInitialStepSize] \
	    [$dir getErrorTolerance] \
	    [$dir getMaxIterations] \
	    [$dir getMaxStepSize] \
	    [$dir getMinStepSize] \
	    [$dir getNextIterationTime] \
	    [$dir getStopTime] \
	    [$dir getSuggestedNextStepSize] \
	    [$dir getTimeResolution] \
	    [$dir getValueResolution]
} {.System.DIR.CT_Backward_Euler_Solver 0.5 0.0 10.0 0.5 0.4 10 0.3 0.2 10.5 100.0 0.5 1e-11 0.1}

######################################################################
####  Test set parameters, same as above, but uses setToken
#   
#     
test CTDirector-2.2a {set Parameters} {
    #Note: Use above set up.
    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute ODESolver]]
    set token [java::new ptolemy.data.StringToken ExplicitRK23Solver]
    $param setToken $token

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute initStepSize]]
    set token [java::new ptolemy.data.DoubleToken 0.5]
    $param setToken $token

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute errorTolerance]]
    set token [java::new ptolemy.data.DoubleToken 0.4]
    $param setToken $token

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute maxIterations]]
    set token [java::new ptolemy.data.IntToken 10]
    $param setToken $token 

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute maxStepSize]]
    set token [java::new ptolemy.data.DoubleToken 0.3]
    $param setToken $token

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute minStepSize]]
    set token [java::new ptolemy.data.DoubleToken 0.2]
    $param setToken $token

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute startTime]]
    set token [java::new ptolemy.data.DoubleToken 10.0]
    $param setToken $token

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute stopTime]]
    set token [java::new ptolemy.data.DoubleToken 100.0]
    $param setToken $token

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute timeResolution]]
    $param setToken {1E-11}

    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute valueResolution]]
    set token [java::new ptolemy.data.DoubleToken 0.1]
    $param setToken $token

    $dir preinitialize
    $dir initialize
    list [[$dir getCurrentODESolver] getFullName] \
	    [$dir getCurrentStepSize] \
	    [$dir getCurrentTime] \
	    [[$dir getIterationBeginTime] getDoubleValue] \
	    [$dir getInitialStepSize] \
	    [$dir getErrorTolerance] \
	    [$dir getMaxIterations] \
	    [$dir getMaxStepSize] \
	    [$dir getMinStepSize] \
	    [$dir getNextIterationTime] \
	    [$dir getStopTime] \
	    [$dir getSuggestedNextStepSize] \
	    [$dir getTimeResolution] \
	    [$dir getValueResolution]
} {.System.DIR.CT_Runge_Kutta_2_3_Solver 0.5 10.0 10.0 0.5 0.4 10 0.3 0.2 10.5 100.0 0.3 1e-11 0.1}


test CTDirector-2.3 {sets and gets} {
    #Note: Use above set up.
    set time [$dir getCurrentTime]
    $dir setCurrentTime [expr $time+0.1]
    $dir setCurrentStepSize 0.2
    list [$dir getCurrentTime] \
	    [$dir getCurrentStepSize]
} {10.1 0.2}

#############################################################################
#### Test set suggested next step size, it is larger than the maximum
#    step size, so nothing has changed.
test CTDirector-2.4 {suggested next step greater than max step} {
    #Note: Use above set up.
    $dir setSuggestedNextStepSize 0.5
    list [$dir getSuggestedNextStepSize]
} {0.3}

#############################################################################
#### Test set suggested next step size, it is less than the maximum
#    step size, so it is effective.
test CTDirector-2.5 {suggested next step less than max step} {
    #Note: Use above set up.
    # Max step size is 0.3
    $dir setSuggestedNextStepSize 0.1
    list [$dir getSuggestedNextStepSize]
} {0.1}

######################################################################
####  Test Breakpoints
#  
test CTDirector-3.1 {register a breakpoint} {     
    #Note: new set up.
    set sys [java::new ptolemy.actor.TypedCompositeActor]
    $sys setName System
    set dir [java::new ptolemy.domains.ct.kernel.CTMultiSolverDirector $sys DIR]
    set stoptime [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute stopTime]]
    set token [java::new ptolemy.data.DoubleToken 1.0]
    $stoptime setToken $token
    $dir preinitialize
    $dir initialize
    set timeResolution [$dir getTimeResolution]
    set currentTime [$dir getCurrentTime]
    $dir {fireAt ptolemy.actor.Actor double} $sys 0.1
    $dir {fireAt ptolemy.actor.Actor double} $sys 0.4
    $dir {fireAt ptolemy.actor.Actor double} $sys 0.2
    set bptable [$dir getBreakPoints]
    set starttime [[java::cast ptolemy.actor.util.Time [$bptable first]] getDoubleValue]
    $bptable removeFirst
    set first [[java::cast ptolemy.actor.util.Time [$bptable first]] getDoubleValue]
    set firstAgain [[java::cast ptolemy.actor.util.Time [$bptable first]] getDoubleValue]
    $bptable removeFirst
    set second [[java::cast ptolemy.actor.util.Time [$bptable first]] getDoubleValue]
    $bptable removeFirst
    set third [[java::cast ptolemy.actor.util.Time [$bptable first]] getDoubleValue]
    list $starttime $first $firstAgain $second $third
} {0.1 0.2 0.2 0.4 1.0}

test CTDirector-3.2 {access empty breakpoint table} {     
    #Note: use above set up.
    $bptable removeFirst
    set nextone [$bptable first]
    list [expr {$nextone == [java::null]}]
} {1}

test CTDirector-3.3 {BreakpointODESolver} {
    #Note: use above set up.
    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute ODESolver]]
    set token [java::new ptolemy.data.StringToken \
	    ptolemy.domains.ct.kernel.solver.DerivativeResolver]
    catch {$param setToken $token} msg
    #catch {$param getToken} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: ptolemy.domains.ct.kernel.solver.DerivativeResolver can only be used as a breakpoint ODE solver.
  in .System.DIR}}

test CTDirector-3.4 {BreakpointODESolver} {
    #Note: use above set up.
    set integrator [java::new ptolemy.domains.ct.kernel.CTBaseIntegrator $sys integrator]
    set param [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute breakpointODESolver]]
    set token [java::new ptolemy.data.StringToken \
	    ptolemy.domains.ct.kernel.solver.DerivativeResolver]
	set solver [$dir getBreakpointSolver]    
	set integratorIsAccurate [$solver integratorIsAccurate $integrator]
	set integratorPredictedStepSize [$solver integratorPredictedStepSize $integrator]
    list $integratorIsAccurate $integratorPredictedStepSize
} {1 0.1}

######################################################################
####  Test methods
#  

test CTDirector-4.1 {Test the canBeInsideDirector() method} {
	# CTMultisolverDirector can not be inside director
	list [$dir canBeInsideDirector]
} {0}

test CTDirector-4.1 {Test the getExecutiveCTGeneralDirector() method} {
	# CTMultisolverDirector can not have executive CTGeneralDirector
	set executiveCTGeneralDirector [$dir getExecutiveCTGeneralDirector]
	list [expr {$executiveCTGeneralDirector == [java::null]}]
} {1}
