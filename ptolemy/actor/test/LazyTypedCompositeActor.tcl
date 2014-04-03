# Tests for the IOPort class
#
# @Author: Edward A. Lee, Lukito Muliadi, Christopher Hylands
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

# NOTE:  All of the following tests use this director,
# pretty much as a dummy.
set director [java::new ptolemy.actor.Director]
set manager [java::new ptolemy.actor.Manager]

######################################################################
####
# Example similar to figure of design document.
test LazyTypedCompositeActor-12.1 {deepConnectedIn(out)Ports} {
    # Create objects
    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    $e0 setDirector $director
    $e0 setManager $manager
    $e0 setName E0
    set e1 [java::new ptolemy.actor.TypedCompositeActor $e0 "E1"]
    set e2 [java::new ptolemy.actor.LazyTypedCompositeActor $e0 "E2"]
    set e3 [java::new ptolemy.actor.LazyTypedCompositeActor $e2 "E3"]
    set e4 [java::new ptolemy.actor.TypedAtomicActor $e0 "E4"]
    set p1 [java::new ptolemy.actor.TypedIOPort $e1 "P1"]
    set p2 [java::new ptolemy.actor.TypedIOPort $e2 "P2"]
    set p3 [java::new ptolemy.actor.TypedIOPort $e3 "P3"]
    set p4 [java::new ptolemy.actor.TypedIOPort $e2 "P4"]
    set p5 [java::new ptolemy.actor.TypedIOPort $e4 "P5"]
    set r1 [java::new ptolemy.actor.TypedIORelation $e0 "R1"]
    set r2 [java::new ptolemy.actor.TypedIORelation $e2 "R2"]
    set r3 [java::new ptolemy.actor.TypedIORelation $e0 "R3"]
    
    $r1 setWidth 1
    $r2 setWidth 1
    $r3 setWidth 1

    # Connect
    $p1 link $r1
    $p2 link $r1
    $p2 link $r2
    $p3 link $r2
    $p4 link $r2
    $p4 link $r3
    $p5 link $r3

    # make P1, P3 output, P5 input
    $p1 setInput false
    $p1 setOutput true
    $p3 setInput false
    $p3 setOutput true
    $p5 setInput true
    $p5 setOutput false

    list [$e0 exportMoML]
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="E0" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="7.1.devel">
    </property>
    <property name="" class="ptolemy.actor.Director">
        <property name="localClock" class="ptolemy.actor.LocalClock">
            <property name="globalTimeResolution" class="ptolemy.actor.parameters.SharedParameter" value="1E-10">
            </property>
            <property name="clockRate" class="ptolemy.data.expr.Parameter" value="1.0">
            </property>
        </property>
        <property name="startTime" class="ptolemy.data.expr.Parameter">
        </property>
        <property name="stopTime" class="ptolemy.data.expr.Parameter">
        </property>
    </property>
    <entity name="E1" class="ptolemy.actor.TypedCompositeActor">
        <port name="P1" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
            <property name="defaultValue" class="ptolemy.data.expr.Parameter">
            </property>
        </port>
    </entity>
    <entity name="E2" class="ptolemy.actor.LazyTypedCompositeActor">
        <port name="P2" class="ptolemy.actor.TypedIOPort">
            <property name="defaultValue" class="ptolemy.data.expr.Parameter">
            </property>
        </port>
        <port name="P4" class="ptolemy.actor.TypedIOPort">
            <property name="defaultValue" class="ptolemy.data.expr.Parameter">
            </property>
        </port>
        <configure>
            <group>
                <entity name="E3" class="ptolemy.actor.LazyTypedCompositeActor">
                    <port name="P3" class="ptolemy.actor.TypedIOPort">
                        <property name="output"/>
                        <property name="defaultValue" class="ptolemy.data.expr.Parameter">
                        </property>
                    </port>
                    <configure>
                        <group>
                        </group>
                    </configure>
                </entity>
                <relation name="R2" class="ptolemy.actor.TypedIORelation">
                    <property name="width" class="ptolemy.data.expr.Parameter" value="1">
                    </property>
                </relation>
                <link port="P2" relation="R2"/>
                <link port="P4" relation="R2"/>
                <link port="E3.P3" relation="R2"/>
            </group>
        </configure>
    </entity>
    <entity name="E4" class="ptolemy.actor.TypedAtomicActor">
        <port name="P5" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="defaultValue" class="ptolemy.data.expr.Parameter">
            </property>
        </port>
    </entity>
    <relation name="R1" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <relation name="R3" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <link port="E1.P1" relation="R1"/>
    <link port="E2.P2" relation="R1"/>
    <link port="E2.P4" relation="R3"/>
    <link port="E4.P5" relation="R3"/>
</entity>
}}

test LazyTypedCompositeActor-12.1 {deepConnectedIn(out)Ports} {
    # Create objects
    set e0 [java::new ptolemy.actor.LazyTypedCompositeActor]
    $e0 setClassDefinition true
    $e0 exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE class PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<class name="" extends="ptolemy.actor.TypedCompositeActor">
    <configure>
        <group>
        </group>
    </configure>
</class>
}
