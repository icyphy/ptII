# MEMS test using Jacl
#
# @Author: Allen Miu
#
# @Version: @(#)tset.tcl	1.10 10/08/98
#
# @Copyright (c) 1998-2000 The Regents of the University of California.
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

# Create the top level Composite Actor
set sys [java::new ptolemy.actor.TypedCompositeActor]
$sys setName DESystem

# Create directors and associate them with the top level composite actor.
set dir [java::new ptolemy.domains.de.kernel.DECQDirector DELocalDirector]
$sys setDirector $dir
set exec [java::new ptolemy.actor.Manager]
$sys setManager $exec

# Build the system
set mems1 [java::new ptolemy.domains.de.demo.mems.lib.MEMSDevice $sys Device]
set mems2 [java::new ptolemy.domains.de.demo.mems.lib.MEMSDevice $sys Device]
set mems3 [java::new ptolemy.domains.de.demo.mems.lib.MEMSDevice $sys Device]
set envr1 [java::new ptolemy.domains.de.demo.mems.lib.MEMSEnvir_alpha $sys Enviro $mems1 0 0 0 74]
set envr2 [java::new ptolemy.domains.de.demo.mems.lib.MEMSEnvir $sys Enviro $mems2 7.0 7.0 0 69]
set envr3 [java::new ptolemy.domains.de.demo.mems.lib.MEMSEnvir $sys Enviro $mems3 8.0 8.0 0 69]


# Identify the ports
set mems1msgIO [java::field $mems1 msgIO]
set mems2msgIO [java::field $mems2 msgIO]
set mems3msgIO [java::field $mems3 msgIO]

set mems1sysIO [java::field $mems1 sysIO]
set mems2sysIO [java::field $mems2 sysIO]
set mems3sysIO [java::field $mems3 sysIO]

set envr1deviceMsgIO [java::field $envr1 deviceMsgIO]
set envr2deviceMsgIO [java::field $envr2 deviceMsgIO]
set envr3deviceMsgIO [java::field $envr3 deviceMsgIO]

set envr1carrierMsgIO [java::field $envr1 carrierMsgIO]
set envr2carrierMsgIO [java::field $envr2 carrierMsgIO]
set envr3carrierMsgIO [java::field $envr3 carrierMsgIO]

set envr1sysIO [java::field $envr1 sysIO]
set envr2sysIO [java::field $envr2 sysIO]
set envr3sysIO [java::field $envr3 sysIO]

set r1 [$sys connect $mems1msgIO $envr1deviceMsgIO R1]
set r2 [$sys connect $mems2msgIO $envr2deviceMsgIO R2]
set r3 [$sys connect $mems3msgIO $envr3deviceMsgIO R3]

# set r4 [$sys connect $mems1sysIO $envr1sysIO R4]
# set r5 [$sys connect $mems2sysIO $envr2sysIO R5]
# set r6 [$sys connect $mems3sysIO $envr3sysIO R6]

set r7 [$sys connect $envr1carrierMsgIO $envr2carrierMsgIO R7]
$envr3carrierMsgIO link $r7

$dir setStopTime 5.0
$exec run
