# DE example using TclBlend
#
# @Author: Lukito Muliadi
#
# @Version: @(#)RampSystem.tcl	1.10  09/21/98
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
#  DEClock->DERamp->DEPlot
#

# Create the top level Composite Actor
set sys [java::new ptolemy.actor.CompositeActor]
$sys setName SDFSystem

# Create directors and associate them with the top level composite actor.
set dir [java::new ptolemy.domains.sdf.kernel.SDFDirector SDFLocalDirector]
$sys setDirector $dir
set exec [java::new ptolemy.actor.Director]
$sys setExecutiveDirector $exec
set scheduler [java::new ptolemy.domains.sdf.kernel.SDFScheduler]
$dir setScheduler $scheduler
$dir setScheduleValid false

# Build the system
set ramp [java::new ptolemy.domains.sdf.lib.SDFRamp $sys Ramp]
set wrapper [java::new ptolemy.domains.sdf.kernel.SDFCompositeActor $sys Wrapper]
set dir2 [java::new ptolemy.domains.sdf.kernel.SDFDirector SDFWrapperLocalDirector]
$wrapper setDirector $dir2
set delay [java::new ptolemy.domains.sdf.lib.SDFDelay $wrapper Delay]
$delay setContainer $wrapper
set print [java::new ptolemy.domains.sdf.lib.SDFPrint $sys Print]

# Identify the ports
set printInEnum [$print inputPorts]
set printIn [$printInEnum nextElement]

set delayInEnum [$delay inputPorts]
set delayIn [$delayInEnum nextElement]

set delayOutEnum [$delay outputPorts]
set delayOut [$delayOutEnum nextElement]

set wrapperIn [$wrapper newPort wrapperinput]
$wrapperIn makeInput true

set wrapperOut [$wrapper newPort wrapperoutput]
$wrapperOut makeOutput true

set rampOutEnum [$ramp outputPorts]
set rampOut [$rampOutEnum nextElement]

# Connect the ports

set r1 [$sys connect $rampOut $wrapperIn R1]
set r2 [$sys connect $wrapperOut $printIn R2]

set r1w [$wrapper connect $wrapperIn $delayIn R1w]
set r2w [$wrapper connect $delayOut $wrapperOut R2w]

set debug ptolemy.debug.Debug
set debugger [java::new ptolemy.debug.DebugListener]
java::call $debug register $debugger

# Run it
$dir go 5




