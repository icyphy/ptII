# Tests for the type event handling
#
# @Author: Yuhong Xiong
#
# @Version: $Id$
#
# @Copyright (c) 1997-2000 The Regents of the University of California.
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

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
#
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#

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

    set ti [[java::new ptolemy.data.IntToken] getClass]
    $p1 setTypeEquals $ti

    $listener getMessage
} {.E1.P1/NaT/int}

######################################################################
####
#
test TypeEvent-1.2 {remove listener} {
    $p1 removeTypeListener $listener
    set td [[java::new ptolemy.data.DoubleToken] getClass]
    $p1 setTypeEquals $td

    $listener getMessage
} {no type change}

######################################################################
####
#
test TypeEvent-1.3 {re-add listener} {
    $p1 addTypeListener $listener
    set ts [[java::new ptolemy.data.StringToken] getClass]
    $p1 setTypeEquals $ts

    $listener getMessage
} {.E1.P1/double/string}

