# Test Clock.
#
# @Author: Edward A. Lee
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

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
#### Constructors and Clone
#
test Clock-1.0 {test constructor and initial value} {
    set e0 [deModel 4.0]
    set clockmaster [java::new ptolemy.actor.lib.Clock $e0 clock]
    [$clockmaster getAttribute values] toString
} {ptolemy.data.expr.Parameter {.top.clock.values} array[1, 0]}

test Clock-1.1 {test clone and initial value} {
    set clock [java::cast ptolemy.actor.lib.Clock [$clockmaster clone]]
    $clockmaster setContainer [java::null]
    $clock setContainer $e0
    [$clock getAttribute values] toString
} {ptolemy.data.expr.Parameter {.top.clock.values} array[1, 0]}

######################################################################
#### Test Clock in a DE model
#
test Clock-2.1 {test with the default output value} {
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $clock] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    set manager [$e0 getManager]
    $manager addExecutionListener \
            [java::new ptolemy.actor.StreamExecutionListener]
    $manager execute
    enumToTokenValues [$rec getRecord 0]
} {1 0 1 0 1}

test Clock-2.2 {check times} {
    enumToObjects [$rec getTimeRecord]
} {0.0 1.0 2.0 3.0 4.0}

test Clock-2.3 {change output value and type and rerun} {
    set p [getParameter $clock values]
    $p setExpression {[0.5, -0.5]}
    set mt [java::cast ptolemy.data.ArrayToken [$p getToken]]
    $mt length
    $mt toString
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {0.5 -0.5 0.5 -0.5 0.5}

test Clock-2.4 {test string output} {
    set val0 [java::new ptolemy.data.StringToken AB]
    set val1 [java::new ptolemy.data.StringToken CD]
    set valArray [java::new {ptolemy.data.Token[]} 2 [list $val0 $val1]]
    set valToken [java::new {ptolemy.data.ArrayToken} $valArray]

    set valuesParam [getParameter $clock values]
    $valuesParam setToken $valToken
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {AB CD AB CD AB} 

