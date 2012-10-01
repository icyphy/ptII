# Tests for the ParameterPort class
#
# @Author: Christopher Brooks, based on Port.tcl by Christopher Hylands, Edward A. Lee, Jie Liu
#
# @Version: $Id$
#
# @Copyright (c) 1997-2012 The Regents of the University of California.
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
test Port-2.1 {Construct Ports} {
    set e1 [java::new ptolemy.actor.TypedCompositeActor]
    set p1 [java::new ptolemy.actor.parameters.ParameterPort $e1 "My Port"]
    list [$p1 getName] [$p1 numLinks]
} {{My Port} 0}

######################################################################
####
#
test Port-13.1 {Test clone} {
    set w [java::new ptolemy.kernel.util.Workspace]
    set e1 [java::new ptolemy.actor.TypedCompositeActor]
    set p1 [java::new ptolemy.actor.parameters.ParameterPort $e1 P1]
    set r1 [java::new ptolemy.actor.TypedIORelation $e1 R1]
    $p1 link $r1
    set p2 [java::cast ptolemy.actor.parameters.ParameterPort [$p1 clone]]
    $p2 description 7
} {ptolemy.actor.parameters.ParameterPort {.P1} links {
} insidelinks {
}}



set cloneTest {<?xml version="1.0" standalone="no"?>
<!DOCTYPE class PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<class name="cloneTest" extends="ptolemy.actor.TypedCompositeActor">
    <entity name="CompositeActor" class="ptolemy.actor.TypedCompositeActor">
        <property name="PortParameter" class="ptolemy.actor.parameters.PortParameter">
            <property name="_hideName" class="ptolemy.kernel.util.SingletonAttribute">
            </property>
        </property>
        <port name="PortParameter" class="ptolemy.actor.parameters.ParameterPort">
            <property name="input"/>
            <property name="_showName" class="ptolemy.data.expr.SingletonParameter" value="true">
            </property>
        </port>
    </entity>
</class>
}

######################################################################
####
#
test Port-13.2 {Clone a ParameterPort in a class} {
    # This is an attempt to reproduce the bug where 
    # 1. $PTII/bin/vergil $PTII/ptolemy/actor/lib/Sinewave.xml
    # 2. Graph -> Save In Library
    # 3. An exception appears
    # The problem was that the ParameterPort ctor was not 
    # PortParameter
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $cloneTest]
    set composite [[java::cast ptolemy.actor.TypedCompositeActor $toplevel] getEntity CompositeActor]
    set port [[java::cast ptolemy.actor.TypedCompositeActor $composite] getAttribute PortParameter]
    set w2 [java::new ptolemy.kernel.util.Workspace]
    set clonedPort [java::cast ptolemy.actor.parameters.PortParameter [$port clone $w2]]
    set hideName [$clonedPort getAttribute _hideName]
    set result [$hideName propagateExistence]
    list [$result size]
} {0}

