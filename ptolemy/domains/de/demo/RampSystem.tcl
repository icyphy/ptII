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
#  DEClock->DERamp->DEPlot
#

# Create the top level Composite Actor
set sys [java::new ptolemy.actor.CompositeActor]
$sys setName DESystem

# Create directors and associate them with the top level composite actor.
set dir [java::new ptolemy.domains.de.kernel.DECQDirector DELocalDirector]
$sys setDirector $dir
set exec [java::new ptolemy.actor.Director]
$sys setExecutiveDirector $exec

# Set the stop time
$dir setStopTime 10.0

# Build the system
set clock [java::new ptolemy.domains.de.lib.DEClock $sys Clock 1.0 1.0]
set ramp [java::new ptolemy.domains.de.lib.DERamp $sys Ramp 0.0 1.0]
set plot [java::new ptolemy.domains.de.lib.DEPlot $sys Plot]

# Identify the ports
set clockOutEnum [$clock outputPorts]
set clockOut [$clockOutEnum nextElement]

set plotInEnum [$plot inputPorts]
set plotIn [$plotInEnum nextElement]

set rampInEnum [$ramp inputPorts]
set rampIn [$rampInEnum nextElement]

set rampOutEnum [$ramp outputPorts]
set rampOut [$rampOutEnum nextElement]

# Connect the ports
set r1 [$sys connect $clockOut $rampIn R1]
set r2 [$sys connect $rampOut $plotIn R2]

# Run it
$exec run
