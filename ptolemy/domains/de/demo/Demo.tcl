# Queueing System with a Blocking Policy
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

# A demo taken from Ptolemy 0.x.

###############################
# CompositeActor and Directors
###############################


# Create the top level Composite Actor
set sys [java::new ptolemy.actor.CompositeActor]
$sys setName DEDemo

# Create directors and associate them with the top level composite actor.
set dir [java::new ptolemy.domains.de.kernel.DECQDirector DELocalDirector]
$sys setDirector $dir
set exec [java::new ptolemy.actor.Director]
$sys setExecutiveDirector $exec


#####################
# Create the actors
#####################

set clock [java::new ptolemy.domains.de.lib.DEClock $sys Clock 1.0 1.0]
set ramp [java::new ptolemy.domains.de.lib.DERamp $sys Ramp 0 1.0]

# num demands pending = 1, consolidate demands = 1, capacity = 10
set fifo1 [java::new ptolemy.domains.de.lib.DEFIFOQueue 1 1 10 $sys FIFO1]

set plot1 [java::new ptolemy.domains.de.lib.DEPlot $sys "Queue 1 Size"]

# service time of server 1 is equal to 1.0
set server1 [java::new ptolemy.domains.de.lib.DEServer 1.0 $sys Server1]

set passgate [java::new ptolemy.domains.de.lib.DEPassGate $sys PassGate]

set delta [java::new ptolemy.domains.de.lib.DEDelay $sys DEDelay 0.0]

# num demands pending = 1, consolidate demands = 1, capacity = -1
set fifo2 [java::new ptolemy.domains.de.lib.DEFIFOQueue 1 1 100 $sys FIFO2]

set plot2 [java::new ptolemy.domains.de.lib.DEPlot $sys "Queue 2 Size"]

# crossingsOnly = true, threshold = 4
set testlevel [java::new ptolemy.domains.de.lib.DETestLevel 1 4 $sys TestLevel]

set not [java::new ptolemy.domains.de.lib.DENot $sys Not]

# service time of server 2 is equal to 3.0
set server2 [java::new ptolemy.domains.de.lib.DEServer 3.0 $sys Server2]

set plot3 [java::new ptolemy.domains.de.lib.DEPlot $sys "Blocking signal"]
set plot4 [java::new ptolemy.domains.de.lib.DEPlot $sys "Dispositions of inputs"]

######################
# Identify the ports
######################

set clockOutEnum [$clock outputPorts]
set clockOut [$clockOutEnum nextElement]

# Ramp

set rampInEnum [$ramp inputPorts]
set rampIn [$rampInEnum nextElement]

set rampOutEnum [$ramp outputPorts]
set rampOut [$rampOutEnum nextElement]

# FIFO queue 1

set fifo1InEnum [$fifo1 inputPorts]
set fifo1InData [$fifo1InEnum nextElement]
set fifo1Demand [$fifo1InEnum nextElement]

set fifo1OutEnum [$fifo1 outputPorts]
set fifo1OutData [$fifo1OutEnum nextElement]
set fifo1OverFlow [$fifo1OutEnum nextElement]
set fifo1Size [$fifo1OutEnum nextElement]

# Plot1

set plot1InEnum [$plot1 inputPorts]
set plot1In [$plot1InEnum nextElement]

# Server 1

set server1InEnum [$server1 inputPorts]
set server1In [$server1InEnum nextElement]

set server1OutEnum [$server1 outputPorts]
set server1Out [$server1OutEnum nextElement]

# Passgate

set passgateInEnum [$passgate inputPorts]
set passgateIn [$passgateInEnum nextElement]
set passgateGate [$passgateInEnum nextElement]

set passgateOutEnum [$passgate outputPorts]
set passgateOut [$passgateOutEnum nextElement]

# DEDelta

set deltaInEnum [$delta inputPorts]
set deltaIn [$deltaInEnum nextElement]

set deltaOutEnum [$delta outputPorts]
set deltaOut [$deltaOutEnum nextElement]

# FIFO queue 2
set fifo2InEnum [$fifo2 inputPorts]
set fifo2InData [$fifo2InEnum nextElement]
set fifo2Demand [$fifo2InEnum nextElement]

set fifo2OutEnum [$fifo2 outputPorts]
set fifo2OutData [$fifo2OutEnum nextElement]
set fifo2OverFlow [$fifo2OutEnum nextElement]
set fifo2Size [$fifo2OutEnum nextElement]

# Plot2

set plot2InEnum [$plot2 inputPorts]
set plot2In [$plot2InEnum nextElement]

# Test Level

set testlevelInEnum [$testlevel inputPorts]
set testlevelIn [$testlevelInEnum nextElement]

set testlevelOutEnum [$testlevel outputPorts]
set testlevelOut [$testlevelOutEnum nextElement]

# Not

set notInEnum [$not inputPorts]
set notIn [$notInEnum nextElement]

set notOutEnum [$not outputPorts]
set notOut [$notOutEnum nextElement]

# Server 2

set server2InEnum [$server2 inputPorts]
set server2In [$server2InEnum nextElement]

set server2OutEnum [$server2 outputPorts]
set server2Out [$server2OutEnum nextElement]

# Plot 3
set plot3InEnum [$plot3 inputPorts]
set plot3In [$plot3InEnum nextElement]

# Plot 4
set plot4InEnum [$plot4 inputPorts]
set plot4In [$plot4InEnum nextElement]


####################################
# Connect the ports
####################################

set r1 [$sys connect $clockOut $rampIn R1]
set r2 [$sys connect $rampOut $fifo1InData R2]
set r3 [$sys connect $fifo1Size $plot1In R3]

set r4 [$sys connect $passgateOut $fifo1Demand R4]
$fifo2InData link $r4

set r5 [$sys connect $fifo1OutData $server1In R5]
set r6 [$sys connect $fifo1OverFlow $plot4In R6]

set r7 [$sys connect $server1Out $passgateIn R7]
set r8 [$sys connect $deltaOut $passgateGate R8]

set r9 [$sys connect $notOut $deltaIn R9]

set r14 [$sys connect $testlevelOut $notIn R14]
$plot3In link $r14

set r10 [$sys connect $server2Out $plot4In R10]
$fifo2Demand link $r10

set r12 [$sys connect $fifo2Size $testlevelIn R12]
$plot2In link $r12

set r13 [$sys connect $fifo2OutData $server2In R13]

# Set the stop time
$dir setStopTime 30.0

# Run it
$exec run
