# CT Ramp followed by DE sampling.
#
# @Author: Jie Liu
#
# @Version: $Id$
#
# @Copyright (c) 1997-2000 The Regents of the University of California.
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
#  A ramp system simulation uses a Backward Euler director and ODE
#  solver.

set sys [java::new ptolemy.actor.TypedCompositeActor]
$sys setName DESystem
set man [java::new ptolemy.actor.Manager]
$sys setManager $man
set dedir [java::new ptolemy.domains.de.kernel.DEDirector $sys DELocalDirector]

set ctsub [java::new ptolemy.actor.TypedCompositeActor $sys CTSubsystem]
set subout [java::new ptolemy.actor.TypedIOPort $ctsub P1]
#set ptype [java::call Class forName ptolemy.data.DoubleToken]
$subout setOutput 1
#$subout setDeclaredType $ptype
set ctdir [java::new ptolemy.domains.ct.kernel.CTMixedSignalDirector $ctsub CTEmbDIR]
# construct the sub system
set const [java::new ptolemy.domains.ct.lib.CTConst $ctsub Const]
set integral [java::new ptolemy.domains.ct.lib.Integrator $ctsub Integrator]
set print [java::new ptolemy.domains.ct.lib.CTPlot $ctsub CTPlot]
set sampler [java::new ptolemy.domains.ct.lib.CTPeriodicalSampler $ctsub Sample]

set constout [$const getPort output]
set intglin [$integral getPort input]
set intglout [$integral getPort output]
set printin [$print getPort input]
set sampin [$sampler getPort input]
set sampout [$sampler getPort output]

set r1 [$ctsub connect $constout $intglin R1]
set r2 [$ctsub connect $intglout $printin R2]
set r3 [java::new ptolemy.actor.TypedIORelation $ctsub R3]

$sampin link $r2
$sampout link $r3
$subout link $r3

# construct the DE system
set deplot [java::new ptolemy.domains.de.lib.DEPlot $sys DEPLOT]
set depin [$deplot getPort input]
set r4 [java::new ptolemy.actor.TypedIORelation $sys R4]
$subout link $r4
$depin link $r4

# DE parameters
$dedir setStopTime 20.0

# CT parameters
set solver1 [$ctdir getAttribute breakpointODESolver]
set token [java::new ptolemy.data.StringToken ptolemy.domains.ct.kernel.solver.BackwardEulerSolver]
$solver1 setToken $token

set solver2 [$ctdir getAttribute ODESolver]
set token [java::new ptolemy.data.StringToken ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver]
$solver2 setToken $token

set initstep [$ctdir getAttribute initStepSize]
$initstep setExpression 0.1

set constval [$const getAttribute Value]
$constval setExpression 1.0

$man run
