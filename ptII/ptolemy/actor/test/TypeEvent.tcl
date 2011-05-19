# Tests for the type event handling
#
# @Author: Yuhong Xiong
#
# @Version: $Id$
#
# @Copyright (c) 1997-2005 The Regents of the University of California.
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

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

#
#

######################################################################
####
#
test TypeEvent-1.0 {Test constructor and toString} {
    set actor [java::new ptolemy.actor.TypedAtomicActor]
    set port [java::new ptolemy.actor.TypedIOPort $actor port]

    set ti [[java::new ptolemy.data.IntToken] getType]
    set td [[java::new ptolemy.data.DoubleToken] getType]

    set event [java::new ptolemy.actor.TypeEvent $port $ti $td]
    $event toString
} {The type on ..port has changed from int to double}

######################################################################
####
#
test TypeEvent-1.1 {Test type event} {
    #create e1
    set e1 [java::new ptolemy.actor.TypedAtomicActor]
    $e1 setName E1
    set p1 [java::new ptolemy.actor.TypedIOPort $e1 P1]
    $p1 setOutput true

    #create TypeListener
    set listener [java::new ptolemy.actor.test.TestTypeListener]
    $p1 addTypeListener $listener

    set ti [[java::new ptolemy.data.IntToken] getType]
    $p1 setTypeEquals $ti

    $listener getMessage
} {.E1.P1/unknown/int}

######################################################################
####
#
test TypeEvent-1.2 {remove listener} {
    $p1 removeTypeListener $listener
    set td [[java::new ptolemy.data.DoubleToken] getType]
    $p1 setTypeEquals $td

    $listener getMessage
} {no type change}

######################################################################
####
#
test TypeEvent-1.3 {re-add listener} {
    $p1 addTypeListener $listener
    set ts [[java::new ptolemy.data.StringToken] getType]
    $p1 setTypeEquals $ts

    $listener getMessage
} {.E1.P1/double/string}
