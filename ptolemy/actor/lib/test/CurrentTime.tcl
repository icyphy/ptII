# Test CurrentTime.
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1997-2003 The Regents of the University of California.
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

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
#### Constructors and Clone
#
test CurrentTime-1.0 {test constructor and initial value} {
    set e0 [deModel 10.0]
    set currentTimeMaster \
	    [java::new ptolemy.actor.lib.CurrentTime $e0 currentTime]
    [$currentTimeMaster getAttribute stopTime] toString
} {ptolemy.data.expr.Parameter {.top.currentTime.stopTime} 0.0}

test CurrentTime-1.1 {test clone and initial value} {
    set currentTime [java::cast ptolemy.actor.lib.CurrentTime \
			 [$currentTimeMaster clone [$e0 workspace]]]
    $currentTimeMaster setContainer [java::null]
    $currentTime setContainer $e0

    # Make sure that clone is giving us a double
    set output [java::field [java::cast ptolemy.actor.lib.Source\
	    $currentTime ] \
	    output]
    set outputType [$output getType]
    list [$outputType toString] \
	    [[$currentTime getAttribute stopTime] toString]
} {double {ptolemy.data.expr.Parameter {.top.currentTime.stopTime} 0.0}}

######################################################################
#### Test CurrentTime in a DE model
#
test CurrentTime-2.1 {test with the default output value} {
    set clock [java::new ptolemy.actor.lib.Clock $e0 clock]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $clock] output] \
            [java::field [java::cast ptolemy.actor.lib.Source \
	     $currentTime] trigger]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source \
	     $currentTime] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    set manager [$e0 getManager]
    $manager addExecutionListener \
            [java::new ptolemy.actor.StreamExecutionListener]
    $manager execute
    enumToTokenValues [$rec getRecord 0]
} {0.0 1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0}

test CurrentTime-2.2 {check times} {
    listToStrings [$rec getTimeHistory]
} {0.0 1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 10.0}
