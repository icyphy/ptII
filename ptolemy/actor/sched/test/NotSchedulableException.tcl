# Tests for the NotSchedulableException
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1999-2000 The Regents of the University of California.
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

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test NotSchedulableException-1.0 {Constructor that takes a String arg} {
    set ex [java::new ptolemy.actor.sched.NotSchedulableException \
	    "Detail Message"]
    $ex getMessage
} {Detail Message}

test NotSchedulableException-2.1 {Constructor that takes a Nameable and a String} {
    set ex [java::new {ptolemy.actor.sched.NotSchedulableException \
	    ptolemy.kernel.util.Nameable java.lang.String} \
	    [java::null] "Detail Message"]
    $ex getMessage
} {Detail Message}

test NotSchedulableException-2.2 {Constructor that takes a Nameable and a String} {
    set n [java::new ptolemy.kernel.util.NamedObj "My NMamedObj"]
    set ex [java::new ptolemy.actor.sched.NotSchedulableException \
	    $n \
	     "Detail Message"]
    $ex getMessage
} {.My NMamedObj: Detail Message}

test NotSchedulableException-3.1 {Constructor that takes 2 Nameables and a String} {
    set n1 [java::new ptolemy.kernel.util.NamedObj N1]
    set n2 [java::new ptolemy.kernel.util.NamedObj N2]
    set ex [java::new ptolemy.actor.sched.NotSchedulableException \
	   $n1 $n2 "Detail Message"]
    $ex getMessage
} {.N1 and .N2: Detail Message}

test NotSchedulableException-4.1 {Constructor that takes an Enumeration and a String} {
    set n1 [java::new ptolemy.kernel.util.NamedObj N1]
    set n2 [java::new ptolemy.kernel.util.NamedObj N2]
    set namedList [java::new ptolemy.kernel.util.NamedList]
    $namedList append $n1
    $namedList append $n2

    set ex [java::new ptolemy.actor.sched.NotSchedulableException \
	   [$namedList elements] "Detail Message"]

    $ex getMessage
} {.N1, .N2: Detail Message}
