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
set subin [java::new ptolemy.actor.TypedIOPort $ctsub Pin]
#set ptype [java::call Class forName ptolemy.data.DoubleToken]
$subin setInput 1
#$subin setDeclaredType $ptype
set subout [java::new ptolemy.actor.TypedIOPort $ctsub Pout]
$subout setOutput 1
#$subout setDeclaredType $ptype

set ctdir [java::new ptolemy.domains.ct.kernel.CTMixedSignalDirector $ctsub CTEmbDIR]

# construct the sub system
set hold [java::new ptolemy.domains.ct.lib.ZeroOrderHold $ctsub Hold]
set integral [java::new ptolemy.domains.ct.lib.Integrator $ctsub Integrator]
set print [java::new ptolemy.actor.lib.gui.TimedPlotter $ctsub CTPlot]
set sampler [java::new ptolemy.domains.ct.lib.CTPeriodicalSampler $ctsub Sample]

set holdin [$hold getPort input]
set holdout [$hold getPort output]
set intglin [$integral getPort input]
set intglout [$integral getPort output]
set printin [$print getPort input]
set sampin [$sampler getPort input]
set sampout [$sampler getPort output]

set rc0 [$ctsub connect $subin $holdin RC0]
set rc1 [$ctsub connect $holdout $intglin RC1]
set rc2 [$ctsub connect $intglout $printin RC2]
set rc3 [java::new ptolemy.actor.TypedIORelation $ctsub RC3]

$sampin link $rc2
$sampout link $rc3
$subout link $rc3

# construct the DE system
set poisson [java::new ptolemy.actor.lib.Poisson $sys Poisson]
set lambda [$poisson getAttribute lambda]
$lambda setExpression 1.0
set ramp [java::new ptolemy.domains.de.lib.DERamp $sys Ramp]
set deplot [java::new ptolemy.domains.de.lib.DEPlot $sys DEPLOT]

# Identify the ports
set poissonOutEnum [$poisson outputPorts]
set poissonOut [$poissonOutEnum nextElement]

set plotInEnum [$deplot inputPorts]
set plotIn [$plotInEnum nextElement]

set rampInEnum [$ramp inputPorts]
set rampIn [$rampInEnum nextElement]

set rampOutEnum [$ramp outputPorts]
set rampOut [$rampOutEnum nextElement]

# Connect the ports
set r1 [$sys connect $poissonOut $rampIn R1]
set r2 [$sys connect $rampOut $subin R2]

set r4 [java::new ptolemy.actor.TypedIORelation $sys R4]
$subout link $r4
$plotIn link $r4

# DE parameters
$dedir setStopTime 20.0

# CT paramters
set solver1 [$ctdir getAttribute breakpointODESolver]
set token [java::new ptolemy.data.StringToken ptolemy.domains.ct.kernel.solver.BackwardEulerSolver]
$solver1 setToken $token

set solver2 [$ctdir getAttribute ODESolver]
set token [java::new ptolemy.data.StringToken ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver]
$solver2 setToken $token

set initstep [$ctdir getAttribute initStepSize]
$initstep setExpression 0.1

$man run
