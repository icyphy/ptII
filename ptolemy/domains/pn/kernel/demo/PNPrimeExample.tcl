# PNPrimeExample demo
#
# @Author: Christopher Hylands
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


# Tcl Blend code to run the PNPrimeExample from tyjtclsh do: 
#    ptuser@kahn 1% cd $TYCHO/java/pt/domains/pn/kernel/demo
#    ptuser@kahn 2% tyjtclsh PNPrimeExample.tcl
#
# To run the test without Tcl Blend, do:
#    ptuser@kahn 1% cd $TYCHO/java/pt/domains/pn/kernel
#    ptuser@kahn 2% setenv CLASSPATH $TYCHO/java
#    ptuser@kahn 3% java pt.domains.pn.kernel.PNPrimeExample 50

# set PNPrimeExample [java::new pt.domains.pn.kernel.PNPrimeExample]
# set args [java::new {java.lang.String[]} {1} {50}]
# $PNPrimeExample main $args

set myUniverse [java::new pt.domains.pn.kernel.PNUniverse]

# FIXME: the 50 is hardwired in here
set numberOfCycles 50

# FIXME: setNoCycles should be setNumberOfCycles or setCycleNumber 
$myUniverse setNoCycles $numberOfCycles 

set ramp [java::new pt.domains.pn.kernel.PNRamp $myUniverse "ramp"]
# FIXME: what does '2' mean?
$ramp {initialize int} 2
$ramp setCycles $numberOfCycles

set sieve [java::new pt.domains.pn.kernel.PNSieve $myUniverse "2_sieve"]
$sieve {initialize int} 2

set queue [java::new pt.kernel.IORelation $myUniverse "2_queue"]

set port [$sieve getPort "input"]
[$port getQueue] setCapacity 1

$port link $queue

set port [$ramp getPort "output"]
$port link $queue

$myUniverse execute

puts "Bye World"
