# A second order CT system example using TclBlend
#
# @Author: Jie Liu
#
# @Version: %Id$
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

package require java

#######################################################################
#
# This implements the example from coyote systems
# The one without lookup table. It uses the default ODE solver
# which is a Forward Euler solver.

set sys [java::new ptolemy.actor.TypedCompositeActor]
$sys setName DESystem
set man [java::new ptolemy.actor.Manager]
$sys setManager $man
set dedir [java::new ptolemy.domains.de.kernel.DEDirector $sys DELocalDirector]

############################################################
### CT subsystem
#
set ctsub [java::new ptolemy.actor.TypedCompositeActor $sys CTSubsystem]
set subin [java::new ptolemy.actor.TypedIOPort $ctsub Pin]
$subin setInput 1
set subout [java::new ptolemy.actor.TypedIOPort $ctsub Pout]
$subout setOutput 1
set ctdir [java::new ptolemy.domains.ct.kernel.CTMixedSignalDirector $ctsub CTEmbDIR]

#CTActors
set sine [java::new ptolemy.domains.ct.lib.CTSin $ctsub SIN]
set hold [java::new ptolemy.domains.ct.lib.CTZeroOrderHold $ctsub Hold]
set add1 [java::new ptolemy.domains.ct.lib.CTAdd $ctsub Add1]
#set add2 [java::new ptolemy.domains.ct.lib.CTAdd $ctsub Add2]
set intgl1 [java::new ptolemy.domains.ct.lib.CTIntegrator $ctsub Integrator1]
set intgl2 [java::new ptolemy.domains.ct.lib.CTIntegrator $ctsub Integrator2]
set gain0 [java::new ptolemy.domains.ct.lib.CTGain $ctsub Gain0]
set gain1 [java::new ptolemy.domains.ct.lib.CTGain $ctsub Gain1]
set gain2 [java::new ptolemy.domains.ct.lib.CTGain $ctsub Gain2]
set gain3 [java::new ptolemy.domains.ct.lib.CTGain $ctsub Gain3]
#set const [java::new ptolemy.domains.ct.lib.CTConst $ctsub Bias]
set plot [java::new ptolemy.domains.ct.lib.CTPlot $ctsub CTPlot]
set sampler [java::new ptolemy.domains.ct.lib.CTPeriodicalSampler \
	$ctsub Sample]

#CTports
set sineout [$sine getPort output]
set add1in [$add1 getPort input]
set add1out [$add1 getPort output]
#set add2in [$add2 getPort input]
#set add2out [$add2 getPort output]
set intgl1in [$intgl1 getPort input]
set intgl1out [$intgl1 getPort output]
set intgl2in [$intgl2 getPort input]
set intgl2out [$intgl2 getPort output]
set gain0in [$gain0 getPort input]
set gain0out [$gain0 getPort output]
set gain1in [$gain1 getPort input]
set gain1out [$gain1 getPort output]
set gain2in [$gain2 getPort input]
set gain2out [$gain2 getPort output]
set gain3in [$gain3 getPort input]
set gain3out [$gain3 getPort output]
#set constout [$const getPort output]
set plotin [$plot getPort input]
set sampin [$sampler getPort input]
set sampout [$sampler getPort output]
set holdin [$hold getPort input]
set holdout [$hold getPort output]

#CTConnections
set cr0 [$ctsub connect $sineout $gain0in CR0]
set cr1 [$ctsub connect $gain0out $add1in CR1]
set cr2 [$ctsub connect $add1out $intgl1in CR2]
set cr3 [$ctsub connect $intgl1out $intgl2in CR3]
set cr4 [$ctsub connect $intgl2out $plotin CR4]
$gain1in link $cr3
$gain2in link $cr4
$sampin link $cr4
set cr5 [java::new ptolemy.actor.TypedIORelation $ctsub CR5]
$sampout link $cr5
$subout link $cr5
set cr6 [$ctsub connect $gain1out $add1in CR6]
set cr7 [$ctsub connect $gain2out $add1in CR7]
set cr8 [$ctsub connect $gain3out $add1in CR8]
set cr9 [java::new ptolemy.actor.TypedIORelation $ctsub CR9]
$holdin link $cr9
$subin link $cr9
#set cr10 [$ctsub connect $gain3in $add2out CR10]
#set cr11 [$ctsub connect $constout $add2in CR11]
#set cr12 [$ctsub connect $holdout $add2in CR12]
set cr10 [$ctsub connect $holdout $gain3in CR110]
$plotin link $cr0
$plotin link $cr10

############################################################
### DE system
#  approximate the FIR filter by a delay and a gain
set fir [java::new {ptolemy.domains.de.lib.DEFIRfilter \
	ptolemy.actor.TypedCompositeActor String String}\
	$sys FIR [list 0.7 0.3]]
set firdelay [$fir getAttribute Delay]
$firdelay setExpression 0.02

set quan [java::new ptolemy.domains.de.lib.DETestLevel $sys Quantizer]
set accu [java::new ptolemy.domains.de.lib.DEStatistics $sys Accumulator]
set clk [java::new ptolemy.domains.de.lib.DEClock $sys ADClock 1 1]
set deplot [java::new ptolemy.domains.de.lib.DEPlot $sys DEPLOT]
set mav [java::new {ptolemy.domains.de.lib.DEFIRfilter \
	ptolemy.actor.TypedCompositeActor String String}\
	$sys MAV [list 0.1 0.1 0.1 0.1 0.1 0.05 0.05 0.05 0.05 0.05\
	0.05 0.05 0.05 0.05 0.05]]
# DE ports
set firin [$fir getPort input]
set firout [$fir getPort output]
set quanin [$quan getPort input]
set quanout [$quan getPort output]
set accin [$accu getPort input]
set accout [$accu getPort average]
set demand [$accu getPort demand]
set reset [$accu getPort reset]
set clkout [$clk getPort output]
set mavin [$mav getPort input]
set mavout [$mav getPort output]
set deplotin [$deplot getPort input]

#DE connections
set dr1 [$sys connect $subout $firin DR1]
set dr2 [$sys connect $firout $quanin DR2]
set dr3 [$sys connect $quanout $subin DR3]

set dr4 [$sys connect $clkout $demand DR4]
$reset link $dr4
$mavin link $dr3
set dr5 [$sys connect $accin $mavout DR5]
set dr6 [$sys connect $deplotin $accout DR6]
$deplotin link $dr3 

############################################################
### DEParameters
# 
$dedir setStopTime 15.0


############################################################
### CT Director Parameters
#
set initstep [$ctdir getAttribute initStepSize]
$initstep setExpression 0.000001

set minstep [$ctdir getAttribute minStepSize]
$minstep setExpression 1e-6

set solver1 [$ctdir getAttribute breakpointODESolver]
set token [java::new ptolemy.data.StringToken ptolemy.domains.ct.kernel.solver.BackwardEulerSolver]
$solver1 setToken $token

set solver2 [$ctdir getAttribute ODESolver]
set token [java::new ptolemy.data.StringToken ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver]
$solver2 setToken $token

############################################################
### CT Actor Parameters
#
set freq [$sine getAttribute AngleFrequency]
$freq setExpression 0.5

set g0 [$gain0 getAttribute Gain]
$g0 setExpression 50.0

set g1 [$gain1 getAttribute Gain]
$g1 setExpression -2.50

set g2 [$gain2 getAttribute Gain]
$g2 setExpression -250.0

set g3 [$gain3 getAttribute Gain]
$g3 setExpression -20.0

#set con [$const getAttribute Value]
#$con setExpression -0.5

set ts [$sampler getAttribute SamplePeriod]
$ts setExpression 0.02

#plot parameters
set ctxmin [$plot getAttribute X_Min]
$ctxmin setExpression 0.0

set ctxmax [$plot getAttribute X_Max]
$ctxmax setExpression 15.0


#legends: 
set ctlegs [$plot getAttribute Legends]
$ctlegs setExpression {"Position Output Feedback"}

set delegs [$deplot getAttribute Legends]
$delegs setExpression {"Feedback DigitalOutput"}


$man startRun

#source SigmaDelta.tcl
