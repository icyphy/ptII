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
set poisson [java::new ptolemy.domains.de.lib.DEPoisson $sys Poisson -1.0 1.0]
set wait [java::new ptolemy.domains.de.lib.DEWaitingTime $sys Wait]
set plot [java::new ptolemy.domains.de.lib.DEPlot $sys Plot]

# Identify the ports

set poissonOutEnum [$poisson outputPorts]
set poissonOut [$poissonOutEnum nextElement]

set plotInEnum [$plot inputPorts]
set plotIn [$plotInEnum nextElement]

set waitee [java::field $wait waitee]
set waiter [java::field $wait waiter]
set waitout [java::field $wait output]

set clockOutEnum [$clock outputPorts]
set clockOut [$clockOutEnum nextElement]

# Connect the ports
set r1 [$sys connect $clockOut $plotIn R1]
set r2 [$sys connect $poissonOut $plotIn R2]
$waitee link $r1
$waiter link $r2
set r3 [$sys connect $waitout $plotIn R3]

# Run it
$exec run

$dir setStopTime 12.0
$exec run 
