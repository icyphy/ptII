# Tests for the DocAttribute class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2008 The Regents of the University of California.
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

if {[string compare test [info procs test]] == 1} then {
    source [file join $PTII util testsuite testDefs.tcl]
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test DocAttribute1.1 {Create DocAttributes} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set n1 [java::new ptolemy.kernel.util.NamedObj] 
    set sa1 [java::new ptolemy.vergil.basic.DocAttribute] 
    set sa2 [java::new ptolemy.vergil.basic.DocAttribute $w] 
    set sa3 [java::new ptolemy.vergil.basic.DocAttribute $n1 "foo"] 
    list [$sa1 toString] [$sa2 toString ] [$sa3 toString]
} {{ptolemy.vergil.basic.DocAttribute {.}} {ptolemy.vergil.basic.DocAttribute {.}} {ptolemy.vergil.basic.DocAttribute {..foo}}}

test DocAttribute2.1 {setContainer with the same name} {
    set n1 [java::new ptolemy.kernel.util.NamedObj "N1"] 
    set n2 [java::new ptolemy.kernel.util.NamedObj "N2"] 
    set sa3 [java::new ptolemy.vergil.basic.DocAttribute $n1 "foo"] 
    set sa4 [java::new ptolemy.vergil.basic.DocAttribute $n2 "foo"] 
    $sa4 setContainer $n1
    list [$sa3 toString] [$sa4 toString]
} {{ptolemy.vergil.basic.DocAttribute {.foo}} {ptolemy.vergil.basic.DocAttribute {.N1.foo}}}


test DocAttribute-2.1 {setContainer with different workspaces} {
    set w1 [java::new ptolemy.kernel.util.Workspace W1]
    set w2 [java::new ptolemy.kernel.util.Workspace W2]
    set n1 [java::new ptolemy.kernel.util.NamedObj $w1 N1] 
    set n2 [java::new ptolemy.kernel.util.NamedObj $w2 N2] 
    set sa5 [java::new ptolemy.vergil.basic.DocAttribute $n1 foo] 
    set sa6 [java::new ptolemy.vergil.basic.DocAttribute $n2 foo] 
    # Cover the catch block in setContainer
    catch {$sa5 setContainer $n2} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Cannot set container because workspaces are different.
  in .N1.foo and .N2}}

test DocAttribute-3.1 {getParameterDoc, getPortDoc - empty} {
    set w3_1 [java::new ptolemy.kernel.util.Workspace W3_1]
    set n3_1 [java::new ptolemy.kernel.util.NamedObj $w3_1 N3_1] 
    set docAttribute3_1 [java::new ptolemy.vergil.basic.DocAttribute $n3_1 \
		       docAttribute3_1] 
    $docAttribute3_1 refreshParametersAndPorts
    list [$docAttribute3_1 getParameterDoc ""] \
	[$docAttribute3_1 getPortDoc ""]
} {{} {}}

test DocAttribute-3.2 {getParameterDoc, getPortDoc} {
    #Used 3.1 above
    set myParameter [java::new ptolemy.data.expr.StringParameter \
			 $docAttribute3_1 {myParameter (parameter)}]
    $myParameter setExpression {docs for myParameter}
    set myPortParameter [java::new ptolemy.data.expr.StringParameter \
			     $docAttribute3_1 {myPortParameter (port-parameter)}]
    $myPortParameter setExpression {docs for myPortParameter}
    set myPort [java::new ptolemy.kernel.util.StringAttribute \
		    $docAttribute3_1 {myPort (port)}]
    $myPort setExpression {docs for myPort}
    list [$docAttribute3_1 getParameterDoc {myParameter}] \
	[$docAttribute3_1 getParameterDoc {myPortParameter}] \
	[$docAttribute3_1 getPortDoc {myPort}]
} {{docs for myParameter} {docs for myPortParameter} {docs for myPort}}

test DocAttribute-4.1 {refreshParametersAndPorts} {
    set w4_1 [java::new ptolemy.kernel.util.Workspace W4_1]
    set t4_1 [java::new ptolemy.actor.TypedCompositeActor $w4_1] 
    $t4_1 setName myT4_1
    set parameter1_4_1 [java::new ptolemy.data.expr.Parameter \
			    $t4_1 myParameter1_4_1]
    set portParameter1_4_1 [java::new ptolemy.actor.parameters.PortParameter \
				$t4_1 myPortParameter1_4_1]
    set port1_4_1 [java::new ptolemy.actor.TypedIOPort \
		       $t4_1 myPort1_4_1]

    set docAttribute4_1 [java::new ptolemy.vergil.basic.DocAttribute $t4_1 \
		       docAttribute4_1] 
    $docAttribute4_1 refreshParametersAndPorts
    list [$docAttribute4_1 getParameterDoc {myParameter1_4_1}] \
	[$docAttribute4_1 getParameterDoc {myPortParameter1_4_1}] \
	[$docAttribute4_1 getPortDoc {myPort1_4_1}]
} {{} {} {}}

test DocAttribute-4.2 {refreshParametersAndPorts with documentation} {
    # Uses 4.1 above
    set myParameter [java::cast \
			 {ptolemy.kernel.util.StringAttribute} \
			 [$docAttribute4_1 getAttribute {myParameter1_4_1 (parameter)}]]
    $myParameter setExpression {docs for myParameter}

    set myPortParameter [java::cast \
			     {ptolemy.kernel.util.StringAttribute} \
			     [$docAttribute4_1 getAttribute {myPortParameter1_4_1 (port-parameter)}]]
    $myPortParameter setExpression {docs for myPortParameter}

    set myPort [java::cast \
		    {ptolemy.kernel.util.StringAttribute} \
		    [$docAttribute4_1 getAttribute {myPort1_4_1 (port)}]]
    $myPort setExpression {docs for myPort}
    $docAttribute4_1 refreshParametersAndPorts
    list [$docAttribute4_1 getParameterDoc {myParameter1_4_1}] \
	[$docAttribute4_1 getParameterDoc {myPortParameter1_4_1}] \
	[$docAttribute4_1 getPortDoc {myPort1_4_1}]
} {{docs for myParameter} {docs for myPortParameter} {docs for myPort}}

test DocAttribute-4.3 {refreshParametersAndPorts} {
    # Uses 4.2 above
    $parameter1_4_1 setContainer [java::null]
    $port1_4_1 setContainer [java::null]
    $portParameter1_4_1 {setContainer ptolemy.kernel.util.NamedObj} [java::null]
    set result1 [list \
		     [$docAttribute4_1 getParameterDoc {myParameter1_4_1}] \
		     [$docAttribute4_1 getParameterDoc {myPortParameter1_4_1}] \
		     [$docAttribute4_1 getPortDoc {myPort1_4_1}]]

    $docAttribute4_1 refreshParametersAndPorts

    set result2 [list \
		     [$docAttribute4_1 getParameterDoc {myParameter1_4_1}] \
		     [$docAttribute4_1 getParameterDoc {myPortParameter1_4_1}] \
		     [$docAttribute4_1 getPortDoc {myPort1_4_1}]]
    list $result1 $result2
} {{{docs for myParameter} {docs for myPortParameter} {docs for myPort}} {{} {docs for myPortParameter} {}}}

