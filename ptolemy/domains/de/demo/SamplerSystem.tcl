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
#
#  Clock--->Ramp1-------->Sampler1------------------------->Plot
#                            ^                            |
#                            |                            |
#  Poisson-------------------O->Ramp2--->Sampler2---------
#                            |                  ^
#                            |                  |
#                             ------------------

# Create the top level Composite Actor
set sys [java::new ptolemy.actor.TypedCompositeActor]
$sys setName DESystem

# Create directors and associate them with the top level composite actor.
set dir [java::new ptolemy.domains.de.kernel.DECQDirector DELocalDirector]
$sys setDirector $dir
set exec [java::new ptolemy.actor.Manager]
$sys setManager $exec

# Build the system
set poisson [java::new ptolemy.domains.de.lib.DEPoisson $sys Poisson 1.0 0.5]
set sampler1 [java::new ptolemy.domains.de.lib.DESampler $sys Sampler1]
set sampler2 [java::new ptolemy.domains.de.lib.DESampler $sys Sampler2]
set ramp1 [java::new {ptolemy.domains.de.lib.Ramp \
    ptolemy.actor.TypedCompositeActor String double double} $sys Ramp1 0 2]
set ramp2 [java::new {ptolemy.domains.de.lib.Ramp \
    ptolemy.actor.TypedCompositeActor String double double} $sys Ramp2 -2 2]
set clock [java::new ptolemy.domains.de.lib.DEClock $sys Clock 1.0 1.0] 
set plot [java::new ptolemy.domains.de.lib.DEPlot $sys Plot]

# Identify the ports
set poissonOut [java::field $poisson output]

set plotIn [java::field $plot input]

set sampler1DataIn [java::field $sampler1 input]
set sampler1ClockIn [java::field $sampler1 clock]

set sampler2DataIn [java::field $sampler2 input]
set sampler2ClockIn [java::field $sampler2 clock]

set sampler1Out [java::field $sampler1 output]

set sampler2Out [java::field $sampler2 output]

set ramp1In [java::field $ramp1 input]

set ramp1Out [java::field $ramp1 output]

set ramp2In [java::field $ramp2 input]

set ramp2Out [java::field $ramp2 output]

set clockOut [java::field $clock output]

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

# Set the stop time
$dir setStopTime 10.0

# Run it
$exec startRun
