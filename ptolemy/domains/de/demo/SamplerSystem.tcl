# DE example using TclBlend
#
# @Author: Lukito Muliadi
#
# @Version: $Id$
#
# @Copyright (c) 1998 The Regents of the University of California.
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
#  DEPoisson->DESampler->DEPlot
#      |          ^
#      |          |
#      ------------
#
#  Clock--->Ramp1-------->Sampler1------------------------->Plot
#                            ^                            |
#                            |                            |
#  Poisson-------------------O->Ramp2--->Sampler2---------
#                            |                  ^
#                            |                  |
#                             ------------------

# Create the top level Composite Actor
set sys [java::new ptolemy.actor.CompositeActor]
$sys setName DESystem

# Create directors and associate them with the top level composite actor.
set dir [java::new ptolemy.domains.de.kernel.DECQDirector DELocalDirector]
$sys setDirector $dir
set exec [java::new ptolemy.actor.Director]
$sys setExecutiveDirector $exec

# Set the stop time
$dir setStopTime 20.0

# Build the system
set poisson [java::new ptolemy.domains.de.lib.DEPoisson $sys Poisson 1.0 0.5]
set sampler1 [java::new ptolemy.domains.de.lib.DESampler $sys Sampler1]
set sampler2 [java::new ptolemy.domains.de.lib.DESampler $sys Sampler2]
set ramp1 [java::new ptolemy.domains.de.lib.Ramp $sys Ramp1 0 2]
set ramp2 [java::new ptolemy.domains.de.lib.Ramp $sys Ramp2 -2 2]
set clock [java::new ptolemy.domains.de.lib.DEClock $sys Clock 1.0 1.0] 
set plot [java::new ptolemy.domains.de.lib.DEPlot $sys Plot]

# Identify the ports
set poissonOutEnum [$poisson outputPorts]
set poissonOut [$poissonOutEnum nextElement]

set plotInEnum [$plot inputPorts]
set plotIn [$plotInEnum nextElement]

set sampler1InEnum [$sampler1 inputPorts]
set sampler1DataIn [$sampler1InEnum nextElement]
set sampler1ClockIn [$sampler1InEnum nextElement]

set sampler2InEnum [$sampler2 inputPorts]
set sampler2DataIn [$sampler2InEnum nextElement]
set sampler2ClockIn [$sampler2InEnum nextElement]

set sampler1OutEnum [$sampler1 outputPorts]
set sampler1Out [$sampler1OutEnum nextElement]

set sampler2OutEnum [$sampler2 outputPorts]
set sampler2Out [$sampler2OutEnum nextElement]

set ramp1InEnum [$ramp1 inputPorts]
set ramp1In [$ramp1InEnum nextElement]

set ramp1OutEnum [$ramp1 outputPorts]
set ramp1Out [$ramp1OutEnum nextElement]

set ramp2InEnum [$ramp2 inputPorts]
set ramp2In [$ramp2InEnum nextElement]

set ramp2OutEnum [$ramp2 outputPorts]
set ramp2Out [$ramp2OutEnum nextElement]

set clockOutEnum [$clock outputPorts]
set clockOut [$clockOutEnum nextElement]

# Connect the ports
set r1 [$sys connect $clockOut $ramp1In R1]
set r2 [$sys connect $ramp1Out $sampler1DataIn R2]
$plotIn link $r2
set r3 [$sys connect $sampler1Out $plotIn R3]
set r4 [$sys connect $poissonOut $sampler1ClockIn R4]
$ramp2In link $r4
$sampler2ClockIn link $r4
set r5 [$sys connect $ramp2Out $sampler2DataIn R5]
set r6 [$sys connect $sampler2Out $plotIn R6]

# Run it
$exec go
