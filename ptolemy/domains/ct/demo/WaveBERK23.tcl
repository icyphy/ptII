# A second order CT system example using TclBlend
#
# @Author: Jie Liu
#
# @Version: $Id$
#
# @Copyright (c) 1997 The Regents of the University of California.
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

#######################################################################
#
# This implements the example from coyote systems
# The one without lookup table. It uses the default ODE solver
# which is a Forward Euler solver.

set sys [java::new ptolemy.actor.TypedCompositeActor]
$sys setName System
set man [java::new ptolemy.actor.Manager]
$sys setManager $man
set dir [java::new ptolemy.domains.ct.kernel.CTMultiSolverDirector DIR]
$sys setDirector $dir

set sqwv [java::new ptolemy.domains.ct.lib.CTSquareWave $sys SQWV]
set add1 [java::new ptolemy.domains.ct.lib.CTAdd $sys Add1]
set intgl1 [java::new ptolemy.domains.ct.lib.CTIntegrator $sys Integrator1]
set intgl2 [java::new ptolemy.domains.ct.lib.CTIntegrator $sys Integrator2]
set gain1 [java::new ptolemy.domains.ct.lib.CTGain $sys Gain1]
set gain2 [java::new ptolemy.domains.ct.lib.CTGain $sys Gain2]
set gain3 [java::new ptolemy.domains.ct.lib.CTGain $sys Gain3]
set plot [java::new ptolemy.domains.ct.lib.CTPlot $sys Plot]

set sqwvout [$sqwv getPort output]
set add1in [$add1 getPort input]
set add1out [$add1 getPort output]
set intgl1in [$intgl1 getPort input]
set intgl1out [$intgl1 getPort output]
set intgl2in [$intgl2 getPort input]
set intgl2out [$intgl2 getPort output]
set gain1in [$gain1 getPort input]
set gain1out [$gain1 getPort output]
set gain2in [$gain2 getPort input]
set gain2out [$gain2 getPort output]
set gain3in [$gain3 getPort input]
set gain3out [$gain3 getPort output]
set plotin [$plot getPort input]

set r1 [$sys connect $sqwvout $gain1in R1]
set r2 [$sys connect $gain1out $add1in R2]
set r3 [$sys connect $add1out $intgl1in R3]
set r4 [$sys connect $intgl1out $intgl2in R4]
set r5 [$sys connect $intgl2out $plotin R5]
$gain2in link $r4
$gain3in link $r5
set r6 [$sys connect $gain2out $add1in R6]
set r7 [$sys connect $gain3out $add1in R7]
$plotin link $r1

set starttime [$dir getAttribute StartTime]
$starttime setExpression 0.0
$starttime parameterChanged [java::null]

set initstep [$dir getAttribute InitialStepSize]
$initstep setExpression 0.000001
$initstep parameterChanged [java::null]

set minstep [$dir getAttribute MinimumStepSize]
$minstep setExpression 1e-6
$minstep parameterChanged [java::null]

set stoptime [$dir getAttribute StopTime]
$stoptime setExpression 10.0
$stoptime parameterChanged [java::null]

set solver1 [$dir getAttribute BreakpointODESolver]
set token [java::new ptolemy.data.StringToken ptolemy.domains.ct.kernel.solver.BackwardEulerSolver]
$solver1 setToken $token
$solver1 parameterChanged [java::null]

set solver2 [$dir getAttribute DefaultODESolver]
set token [java::new ptolemy.data.StringToken ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver]
$solver2 setToken $token
$solver2 parameterChanged [java::null]

#set initstate [$integral getAttribute InitialState]
#$initstate setExpression 0.0
#$initstate parameterChanged [java::null]

set freq [$sqwv getAttribute Frequency]
$freq setExpression 0.25
$freq parameterChanged [java::null]

set g1 [$gain1 getAttribute Gain]
$g1 setExpression 500.0
$g1 parameterChanged [java::null]

set g2 [$gain2 getAttribute Gain]
$g2 setExpression -25.0
$g2 parameterChanged [java::null]

set g3 [$gain3 getAttribute Gain]
$g3 setExpression -2500.0
$g3 parameterChanged [java::null]

#set constval [$const getAttribute Value]
#$constval setExpression 1.0
#$constval parameterChanged [java::null]

$man startRun

