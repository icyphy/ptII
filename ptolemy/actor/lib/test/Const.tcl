# Test Const.
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
#### Constructors and Clone
#

test Const-1.0 {test constructor and initial value} {
    set e0 [sdfModel]
    set const [java::new ptolemy.actor.lib.Const $e0 const]
    [$const getAttribute value] toString
} {ptolemy.data.expr.Parameter {.top.const.value} 1}

test Const-1.1 {test clone and initial value} {
    set e0 [sdfModel]
    set const [java::new ptolemy.actor.lib.Const $e0 const]
    set newobj [java::cast ptolemy.actor.lib.Const [$const clone]]
    $newobj setName new
    [$newobj getAttribute value] toString
} {ptolemy.data.expr.Parameter {.new.value} 1}

test Const-1.2 {change the original value and verify that the new remains} {
    set orgvalue [java::cast ptolemy.data.expr.Parameter \
            [$const getAttribute value]]
    $orgvalue setToken [java::new {ptolemy.data.DoubleToken double} 3.1]

    list [[$newobj getAttribute value] toString]  \
            [[$const getAttribute value] toString]
} {{ptolemy.data.expr.Parameter {.new.value} 1} {ptolemy.data.expr.Parameter {.top.const.value} 3.1}}

test Const-1.3 {Test clone of Source base class} {
    expr 0 != \ [string compare [$const getPort output] \
            [$newobj getPort output]]
} {1}

######################################################################
#### Test Const in an SDF model
#
test Const-2.1 {test with the default output value} {
    set e0 [sdfModel]
    set const [java::new ptolemy.actor.lib.Const $e0 const]
    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]
    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $const] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {1}

test Const-2.1 {change output value and type and rerun} {
    set p [getParameter $const value]
    $p setExpression 3.0
    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {3.0}

# FIXME: Need a mechanism to test a change in parameter during a run.
