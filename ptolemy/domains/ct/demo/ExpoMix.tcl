# CT example using TclBlend
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
#  A Exponential system simulation uses a 4th order fixed step
#  RK solver
#

set sys [java::new ptolemy.actor.TypedCompositeActor]
$sys setName System
set man [java::new ptolemy.actor.Manager]
$sys setManager $man
set dir [java::new ptolemy.domains.ct.kernel.CTMixedSignalDirector DIR]
$sys setDirector $dir

set const [java::new ptolemy.domains.ct.lib.CTConst $sys Const]
set add [java::new ptolemy.domains.ct.lib.CTAdd $sys Add]
set integral [java::new ptolemy.domains.ct.lib.CTIntegrator $sys Integrator]
set plot [java::new ptolemy.domains.ct.lib.CTPrintln $sys Printer]
set constout [$const getPort output]
set addin [$add getPort input]
set addout [$add getPort output]
set intglin [$integral getPort input]
set intglout [$integral getPort output]
set plotin [$plot getPort input]

set r1 [$sys connect $constout $addin R1]
set r2 [$sys connect $addout $intglin R2]
set r3 [$sys connect $intglout $plotin R3]
$addin link $r3

set starttime [$dir getAttribute StartTime]
$starttime setExpression 0.0
$starttime parameterChanged [java::null]

set initstep [$dir getAttribute InitialStepSize]
$initstep setExpression 0.01
$initstep parameterChanged [java::null]

set stoptime [$dir getAttribute StopTime]
$stoptime setExpression 1.0
$stoptime parameterChanged [java::null]

#set solver1 [$dir getAttribute BreakpointODESolver]
#set token [java::new ptolemy.data.StringToken ptolemy.domains.ct.kernel.solver.BackwardEulerSolver]
#$solver1 setToken $token
#$solver1 parameterChanged [java::null]

#set solver2 [$dir getAttribute DefaultODESolver]
#set token [java::new ptolemy.data.StringToken ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver]
#$solver2 setToken $token
#$solver2 parameterChanged [java::null]

#set initstate [$integral getAttribute InitialState]
#$initstate setExpression 1.0
#$initstate parameterChanged [java::null]

set constval [$const getAttribute Value]
$constval setExpression -1.0
$constval parameterChanged [java::null]

$man startRun




