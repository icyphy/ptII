# DE example using TclBlend
#
# @Author: Lukito Muliadi
#
# @Version: %W  %G
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

# Create the top level Composite Actor
set sys [java::new pt.actor.CompositeActor]
$sys setName DESystem

# Create directors and associate them with the top level composite actor.
set dir [java::new pt.domains.de.kernel.DECQDirector DELocalDirector]
$sys setDirector $dir
set exec [java::new pt.actor.Director]
$sys setExecutiveDirector $exec

# Set the stop time
$dir setStopTime 10.0

# Build the system
set clock [java::new pt.domains.de.lib.DEClock 1.0 $sys Clock]
set plot [java::new pt.domains.de.lib.DEPlot $sys Plot]

# Identify the ports
set outEnum [$clock outputPorts]
set clockOut [$outEnum nextElement]

set inEnum [$plot inputPorts]
set plotIn [$inEnum nextElement]

set r1 [$sys connect $clockOut $plotIn R1]

$dir setStopTime 10.0
$exec run

#set const [java::new pt.domains.ct.lib.CTConst $sys Const]
#set add [java::new pt.domains.ct.lib.CTAdd $sys Add]
#set integral [java::new pt.domains.ct.lib.CTIntegrator $sys Integrator]
#set plot [java::new pt.domains.ct.lib.CTPlot $sys Plot]
#set constout [$const getPort output]
#set addin [$add getPort input]
#set addout [$add getPort output]
#set intglin [$integral getPort input]
#set intglout [$integral getPort output]
#set plotin [$plot getPort input]

#set r1 [$sys connect $constout $addin R1]
#set r2 [$sys connect $addout $intglin R2]
#set r3 [$sys connect $intglout $plotin R3]
#$addin link $r3

#$exec setParam startTime 0
#$exec setParam stopTime 1
#$exec setParam initialStepSize 0.01
#$const setParam value 1
#$exec run




