# Test SetVariable
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2007-2019 The Regents of the University of California.
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

if {[string compare jdkCaptureErr [info procs jdkCaptureErr]] == 1} then { 
    source [file join $PTII util testsuite jdktools.tcl]
} {}

######################################################################
#### Test SetVariable in an SDF model
#
test SetVariable-2.1 {test with the default output values} {
    set e0 [sdfModel 5]

    set sdfDirector [$e0 getDirector]

    	
    set allowDisconnectedGraphsParam \
	[getParameter $sdfDirector allowDisconnectedGraphs]
    $allowDisconnectedGraphsParam setToken \
	[java::new ptolemy.data.BooleanToken true]

    set ramp [java::new ptolemy.actor.lib.Ramp $e0 ramp]

    set myParameter [java::new ptolemy.data.expr.Parameter $e0 myParameter]
    $myParameter setExpression 9

    set setVariable [java::new ptolemy.actor.lib.SetVariable $e0 setVariable]
    set variableName [java::cast ptolemy.kernel.util.StringAttribute \
			  [$setVariable getAttribute variableName]]
    $variableName setExpression myParameter

    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $ramp] output] \
            [java::field $setVariable input]


    set const [java::new ptolemy.actor.lib.Const $e0 const]

    set p [getParameter $const value]
    $p setExpression myParameter

    set rec [java::new ptolemy.actor.lib.Recorder $e0 rec]

    $e0 connect \
            [java::field [java::cast ptolemy.actor.lib.Source $const] output] \
            [java::field [java::cast ptolemy.actor.lib.Sink $rec] input]

    [$e0 getManager] execute
    enumToTokenValues [$rec getRecord 0]
} {9 0 1 2 3}

test SetVariable-3.1 {changeFailed} {
    #Uses 2.1 above
    jdkCaptureErr {
        # This test was failing if we ran Exit.tcl and then this test.
        # MessageHandler.error() now actually throws the Exception.
	catch {$setVariable changeFailed [java::null] \
                   [java::new Exception {Test exception for SetVariable.changeFailed()}]} errMsg
    } results
    list [string range $results 0 89] "\n" $errMsg
} {{Failed to set variable.
java.lang.Exception: Test exception for SetVariable.changeFailed()} {
} {java.lang.RuntimeException: java.lang.Exception: Test exception for SetVariable.changeFailed()}}

