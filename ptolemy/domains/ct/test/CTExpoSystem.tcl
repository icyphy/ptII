# Tests for the Exponential System, using various solvers.
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
#        | Const |---(+)---->| Integrator |---------->| TestValue |
#        +-------+    ^      +------------+     |     +-----------+
#                     |                         |
#                     |                         |
#                     +-------------------------+
#
#
#     Test different ODE solvers and directors.
#     The exact result is e-1 = 1.71828182845905

######################################################################
####  Test Director and solvers by execute a demo system
#  
# Note: Not depends on above set up. Can be moved to anywhere.
test CTExpoSystem-4.1 {Expo with ForwardEulerSolver} {
    set sys [java::new ptolemy.actor.TypedCompositeActor]
    $sys setName System
    set man [java::new ptolemy.actor.Manager]
    $sys setManager $man
    set dir [java::new ptolemy.domains.ct.kernel.CTMultiSolverDirector $sys DIR]
    
    set const [java::new ptolemy.actor.lib.Const $sys Const]
    set add [java::new ptolemy.actor.lib.AddSubtract $sys Add]
    set integral [java::new ptolemy.domains.ct.lib.Integrator $sys Integr]
    set testV [java::new ptolemy.domains.ct.test.CTTestValueSink $sys TestV]

    set constout [$const getPort output]
    set addin [$add getPort plus]
    set addout [$add getPort output]
    set intglin [$integral getPort input]
    set intglout [$integral getPort output]
    set testin [$testV getPort input]

    set r1 [$sys connect $constout $addin R1]
    set r2 [$sys connect $addout $intglin R2]
    set r3 [$sys connect $intglout $testin R3]
    $addin link $r3

    set solver [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute ODESolver]]
    set token [java::new ptolemy.data.StringToken ptolemy.domains.ct.kernel.solver.ForwardEulerSolver]
    $solver setToken $token

    set starttime [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute StartTime]]
    set token [java::new ptolemy.data.DoubleToken 0.0]
    $starttime setToken $token

    set initstep [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute InitialStepSize]]
    set token [java::new ptolemy.data.DoubleToken 0.1]
    $initstep setToken $token

    set stoptime [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute StopTime]]
    set token [java::new ptolemy.data.DoubleToken 1.0]
    $stoptime setToken $token

    set initstate [java::cast ptolemy.data.expr.Parameter \
	    [$integral getAttribute InitialState]]
    set token [java::new ptolemy.data.DoubleToken 0.0]
    $initstate setToken $token
    
    set constval [java::cast ptolemy.data.expr.Parameter \
	    [$const getAttribute value]]
    set token [java::new ptolemy.data.DoubleToken 1.0]
    $constval setToken $token
    
    set testvalue [java::cast ptolemy.data.expr.Parameter \
	    [$testV getAttribute "Value"]]
    $testvalue setToken [java::new ptolemy.data.DoubleToken 1.5937424601000005]

    #set debugger [java::cast ptolemy.data.expr.Parameter \
    #	    [$testV getAttribute Print]]
    #set token [java::new ptolemy.data.BooleanToken true]
    #$debugger setToken $token

    $man run
    list [$testV isSuccessful]  
} {1}

test CTExpoSystem-4.2 {Expo with BackwardEulerSolver} {
    #Note: Use the above set up.

    set solver [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute ODESolver]]
    set token [java::new ptolemy.data.StringToken ptolemy.domains.ct.kernel.solver.BackwardEulerSolver]
    $solver setToken $token

    set testvalue [java::cast ptolemy.data.expr.Parameter \
	    [$testV getAttribute "Value"]]
    $testvalue setToken [java::new ptolemy.data.DoubleToken 1.8679717039952553]

    $man run
    list [$testV isSuccessful]  
} {1}

test CTExpoSystem-4.3 {Expo System with MultiSolver DR RK23} {
    #Note: Use above setup.
    set dir [java::new ptolemy.domains.ct.kernel.CTMultiSolverDirector $sys DIR]
    $sys setDirector $dir
    set starttime [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute StartTime]]
    set token [java::new ptolemy.data.DoubleToken 0.0]
    $starttime setToken $token
    
    set initstep [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute InitialStepSize]]
    set token [java::new ptolemy.data.DoubleToken 0.1]
    $initstep setToken $token

    set stoptime [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute StopTime]]
    set token [java::new ptolemy.data.DoubleToken 1.0]
    $stoptime setToken $token

    set initstate [java::cast ptolemy.data.expr.Parameter \
	    [$integral getAttribute InitialState]]
    set token [java::new ptolemy.data.DoubleToken 0.0]
    $initstate setToken $token
    
    set constval [java::cast ptolemy.data.expr.Parameter \
	    [$const getAttribute value]]
    set token [java::new ptolemy.data.DoubleToken 1.0]
    $constval setToken $token

    set testvalue [java::cast ptolemy.data.expr.Parameter \
	    [$testV getAttribute "Value"]]
    $testvalue setToken [java::new ptolemy.data.DoubleToken 1.7181292429552535]

    $man run
    list [$testV isSuccessful]  
} {1}


test CTExpoSystem-4.4 {Expo system with accuracy 1e-6} {
    #Note: Use the above set up.
    set accu [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute ErrorTolerance]]
    $accu setToken [java::new ptolemy.data.DoubleToken 1e-6]

    set testvalue [java::cast ptolemy.data.expr.Parameter \
	    [$testV getAttribute "Value"]]
    $testvalue setToken [java::new ptolemy.data.DoubleToken  1.718280381223137]

    $man run
    list [$testV isSuccessful]  
} {1}

test CTExpoSystem-4.6 {Expo System with Multi-Solver ImpulseBE RK23} {
    #Note: Use above setup.
    set bps [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute BreakpointODESolver]]
    $bps setToken [java::new ptolemy.data.StringToken \
	    "ptolemy.domains.ct.kernel.solver.ImpulseBESolver"]
   
    set accu [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute ErrorTolerance]]
    $accu setToken [java::new ptolemy.data.DoubleToken 1e-5]


    set testvalue [java::cast ptolemy.data.expr.Parameter \
	    [$testV getAttribute "Value"]]
    $testvalue setToken [java::new ptolemy.data.DoubleToken 1.7182651860702431]

    $man run
    list [$testV isSuccessful]  
} {1}
    
test CTExpoSystem-4.7 {Expo System with CTMixsignalSolver as toplevel} {
    #Note: Use above setup.
    set dir [java::new ptolemy.domains.ct.kernel.CTMixedSignalDirector $sys DIR]
    $sys setDirector $dir
    #set printflag [java::cast ptolemy.data.expr.Parameter \
	    #	    [$testV getAttribute "Print"]]
    #$printflag setToken [java::new ptolemy.data.BooleanToken true]
    set stoptime [java::cast ptolemy.data.expr.Parameter \
	    [$dir getAttribute StopTime]]
    set token [java::new ptolemy.data.DoubleToken 1.0]
    $stoptime setToken $token

    set testvalue [java::cast ptolemy.data.expr.Parameter \
	    [$testV getAttribute "Value"]]
    $testvalue setToken [java::new ptolemy.data.DoubleToken 1.7181292429552535]

    $man run
    list [$testV isSuccessful]
} {1}



    
