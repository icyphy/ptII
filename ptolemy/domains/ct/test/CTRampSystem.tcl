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
####  The tests in this file construct and simulate a simple CT system
#     as the following:
#
#        +-------+           +------------+           +-----------+
#        | Const |---------->| Integrator |---------->| TestValue |
#        +-------+           +------------+           +-----------+
#
#     Test different ODE solvers and directors.


######################################################################
####  Test Director and solvers by execute a demo system
#  
# Note: Not depends on above set up. Can be moved to anywhere.
test CTRampSystem-4.1 {Ramp with ForwardEulerSolver} {
    set sys [java::new ptolemy.actor.TypedCompositeActor]
    $sys setName System
    set man [java::new ptolemy.actor.Manager]
    $sys setManager $man
    set dir [java::new ptolemy.domains.ct.kernel.CTMultiSolverDirector $sys DIR]
    set const [java::new ptolemy.actor.lib.Const $sys Const]
    set integral [java::new ptolemy.domains.ct.lib.Integrator $sys Integ]
    set print [java::new ptolemy.domains.ct.test.CTTestValueSink\
    	    $sys Sink]
    #set sink [java::new ptolemy.actor.gui.TimedPlotter $sys pl]
    set constout [$const getPort output]
    set intglin [$integral getPort input]
    set intglout [$integral getPort output]
    set printin [$print getPort input]
    #set sinkin [$sink getPort input]

    set r1 [$sys connect $constout $intglin R1]
    set r2 [$sys connect $intglout $printin R2]
    #$sinkin link $r2

    set solver [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute ODESolver]]
    set token [java::new ptolemy.data.StringToken ptolemy.domains.ct.kernel.solver.ForwardEulerSolver]
    $solver setToken $token

    set initstate [java::cast ptolemy.data.expr.Parameter \
	    [$integral getAttribute InitialState]]
    set token [java::new ptolemy.data.DoubleToken 0.0]
    $initstate setToken $token

    set starttime [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute StartTime]]
    set token [java::new ptolemy.data.DoubleToken 0.0]
    $starttime setToken $token

    set stoptime [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute StopTime]]
    set token [java::new ptolemy.data.DoubleToken 1.0]
    $stoptime setToken $token

    set initstep [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute InitialStepSize]]
    set token [java::new ptolemy.data.DoubleToken 0.1]
    $initstep setToken $token

    set constval [java::cast ptolemy.data.expr.Parameter \
	    [$const getAttribute value]]
    set token [java::new ptolemy.data.DoubleToken 1.0]
    $constval setToken $token

    $man run
    list [$print isSuccessful]
} {1}

test CTRampSystem-4.2 {Ramp with BackwardEulerSolver} {
    #Note: use above setup. reset parameters.

    set solver [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute ODESolver]]
    set token [java::new ptolemy.data.StringToken\
	    ptolemy.domains.ct.kernel.solver.BackwardEulerSolver]
    $solver setToken $token

    set initstate [java::cast ptolemy.data.expr.Parameter \
	    [$integral getAttribute InitialState]]
    set token [java::new ptolemy.data.DoubleToken 0.0]
    $initstate setToken $token

    set starttime [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute StartTime]]
    set token [java::new ptolemy.data.DoubleToken 0.0]
    $starttime setToken $token

    set stoptime [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute StopTime]]
    set token [java::new ptolemy.data.DoubleToken 1.0]
    $stoptime setToken $token

    set initstep [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute InitialStepSize]]
    set token [java::new ptolemy.data.DoubleToken 0.1]
    $initstep setToken $token

    set constval [java::cast ptolemy.data.expr.Parameter \
	    [$const getAttribute value]]
    set token [java::new ptolemy.data.DoubleToken 1.0]
    $constval setToken $token

    $man run
    list [$print isSuccessful]
} {1}

test CTRampSystem-4.3 {Ramp with ExplicitRK23Solver and DerivativeResolver} {
    #Note: use above setup. reset parameters.
    set dir [java::new ptolemy.domains.ct.kernel.CTMultiSolverDirector $sys DIR]
    set solver [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute ODESolver]]
    set token [java::new ptolemy.data.StringToken\
	    ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver]
    $solver setToken $token

    set bpsolver [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute BreakpointODESolver]]
    set token [java::new ptolemy.data.StringToken\
	    ptolemy.domains.ct.kernel.solver.DerivativeResolver]
    $bpsolver setToken $token

    set initstate [java::cast ptolemy.data.expr.Parameter \
	    [$integral getAttribute InitialState]]
    set token [java::new ptolemy.data.DoubleToken 0.0]
    $initstate setToken $token

    set starttime [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute StartTime]]
    set token [java::new ptolemy.data.DoubleToken 0.0]
    $starttime setToken $token

    set stoptime [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute StopTime]]
    set token [java::new ptolemy.data.DoubleToken 1.0]
    $stoptime setToken $token

    set initstep [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute InitialStepSize]]
    set token [java::new ptolemy.data.DoubleToken 0.1]
    $initstep setToken $token

    set constval [java::cast ptolemy.data.expr.Parameter \
	    [$const getAttribute value]]
    set token [java::new ptolemy.data.DoubleToken 1.0]
    $constval setToken $token

    #set debugger [java::cast ptolemy.data.expr.Parameter \
    #	    [$print getAttribute Print]]
    #   set token [java::new ptolemy.data.BooleanToken true]
    #  $debugger setToken $token

    #set dl [java::new ptolemy.kernel.util.StreamListener]
    #$dir addDebugListener $dl

    $man run
    list [$print isSuccessful] 
} {1}
