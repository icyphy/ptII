# Tests for the AbstractCase class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2002-2003 The Regents of the University of California.
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

# Load the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


######################################################################
####
#
set header {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">}


######################################################################
####
#
test AbstractCase-1.1 {call _exportMoMLContents} { 
    set parser [java::new ptolemy.moml.MoMLParser]

    # The list of filters is static, so we reset it in case there
    # filters were already added.
    $parser setMoMLFilters [java::null]
    $parser addMoMLFilter [java::new \
	    ptolemy.moml.filter.RemoveGraphicalClasses]

    set toplevel [$parser parseFile "auto/Case.xml"]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="Case" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="3.1-devel">
    </property>
    <property name="_vergilSize" class="ptolemy.actor.gui.SizeAttribute" value="[604, 454]">
    </property>
    <property name="_vergilLocation" class="ptolemy.actor.gui.LocationAttribute" value="[102, 100]">
    </property>
    <property name="SDF" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="Scheduler" class="ptolemy.domains.sdf.kernel.SDFScheduler">
        </property>
        <property name="allowDisconnectedGraphs" class="ptolemy.data.expr.Parameter" value="false">
        </property>
        <property name="allowRateChanges" class="ptolemy.data.expr.Parameter" value="false">
        </property>
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="5">
        </property>
        <property name="vectorizationFactor" class="ptolemy.data.expr.Parameter" value="1">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="69.0, 36.0">
        </property>
    </property>
    <entity name="Case" class="ptolemy.actor.lib.hoc.Case">
        <property name="_location" class="ptolemy.kernel.util.Location" value="207.0, 146.0">
        </property>
        <property name="_vergilSize" class="ptolemy.actor.gui.SizeAttribute" value="[535, 350]">
        </property>
        <property name="_vergilLocation" class="ptolemy.actor.gui.LocationAttribute">
        </property>
        <property name="_windowProperties" class="ptolemy.actor.gui.WindowPropertiesAttribute" value="{bounds={241, 155, 750, 466}}">
        </property>
        <entity name="typed composite actor" class="ptolemy.actor.TypedCompositeActor">
            <property name="_location" class="ptolemy.kernel.util.Location" value="165.0, 145.0">
            </property>
            <property name="_vergilSize" class="ptolemy.actor.gui.SizeAttribute" value="[604, 454]">
            </property>
            <property name="_vergilLocation" class="ptolemy.actor.gui.LocationAttribute" value="[182, 190]">
            </property>
            <property name="SDF" class="ptolemy.domains.sdf.kernel.SDFDirector">
                <property name="Scheduler" class="ptolemy.domains.sdf.kernel.SDFScheduler">
                </property>
                <property name="allowDisconnectedGraphs" class="ptolemy.data.expr.Parameter" value="false">
                </property>
                <property name="allowRateChanges" class="ptolemy.data.expr.Parameter" value="false">
                </property>
                <property name="iterations" class="ptolemy.data.expr.Parameter" value="0">
                </property>
                <property name="vectorizationFactor" class="ptolemy.data.expr.Parameter" value="1">
                </property>
                <property name="_location" class="ptolemy.kernel.util.Location" value="69.0, 35.0">
                </property>
            </property>
            <entity name="Ramp" class="ptolemy.actor.lib.Ramp">
                <property name="firingCountLimit" class="ptolemy.data.expr.Parameter" value="0">
                </property>
                <property name="init" class="ptolemy.data.expr.Parameter" value="0">
                </property>
                <property name="step" class="ptolemy.actor.parameters.PortParameter" value="1">
                </property>
                <doc>Create a sequence of tokens with increasing value</doc>
                <property name="_location" class="ptolemy.kernel.util.Location" value="151.0, 162.0">
                </property>
                <port name="output" class="ptolemy.actor.TypedIOPort">
                    <property name="output"/>
                </port>
                <port name="trigger" class="ptolemy.actor.TypedIOPort">
                    <property name="input"/>
                    <property name="multiport"/>
                </port>
                <port name="step" class="ptolemy.actor.parameters.ParameterPort">
                    <property name="input"/>
                </port>
            </entity>
            <entity name="FileWriter" class="ptolemy.actor.lib.FileWriter">
                <property name="filename" class="ptolemy.data.expr.Parameter" value="&quot;&quot;">
                </property>
                <doc>Write out tokens to a file or stdout</doc>
                <property name="_location" class="ptolemy.kernel.util.Location" value="290.0, 157.0">
                </property>
                <port name="input" class="ptolemy.actor.TypedIOPort">
                    <property name="input"/>
                    <property name="multiport"/>
                </port>
            </entity>
            <relation name="relation" class="ptolemy.actor.TypedIORelation">
            </relation>
            <link port="Ramp.output" relation="relation"/>
            <link port="FileWriter.input" relation="relation"/>
        </entity>
        <entity name="Copy1:typed composite actor" class="ptolemy.actor.TypedCompositeActor">
            <property name="_location" class="ptolemy.kernel.util.Location" value="347.0, 144.0">
            </property>
            <property name="_vergilSize" class="ptolemy.actor.gui.SizeAttribute" value="[604, 454]">
            </property>
            <property name="_vergilLocation" class="ptolemy.actor.gui.LocationAttribute" value="[102, 100]">
            </property>
            <property name="SDF" class="ptolemy.domains.sdf.kernel.SDFDirector">
                <property name="Scheduler" class="ptolemy.domains.sdf.kernel.SDFScheduler">
                </property>
                <property name="allowDisconnectedGraphs" class="ptolemy.data.expr.Parameter" value="false">
                </property>
                <property name="allowRateChanges" class="ptolemy.data.expr.Parameter" value="false">
                </property>
                <property name="iterations" class="ptolemy.data.expr.Parameter" value="0">
                </property>
                <property name="vectorizationFactor" class="ptolemy.data.expr.Parameter" value="1">
                </property>
                <property name="_location" class="ptolemy.kernel.util.Location" value="69.0, 35.0">
                </property>
            </property>
            <entity name="Ramp" class="ptolemy.actor.lib.Ramp">
                <property name="firingCountLimit" class="ptolemy.data.expr.Parameter" value="0">
                </property>
                <property name="init" class="ptolemy.data.expr.Parameter" value="-1">
                </property>
                <property name="step" class="ptolemy.actor.parameters.PortParameter" value="-1">
                </property>
                <doc>Create a sequence of tokens with increasing value</doc>
                <property name="_location" class="ptolemy.kernel.util.Location" value="151.0, 161.0">
                </property>
                <port name="output" class="ptolemy.actor.TypedIOPort">
                    <property name="output"/>
                </port>
                <port name="trigger" class="ptolemy.actor.TypedIOPort">
                    <property name="input"/>
                    <property name="multiport"/>
                </port>
                <port name="step" class="ptolemy.actor.parameters.ParameterPort">
                    <property name="input"/>
                </port>
            </entity>
            <entity name="FileWriter" class="ptolemy.actor.lib.FileWriter">
                <property name="filename" class="ptolemy.data.expr.Parameter" value="&quot;&quot;">
                </property>
                <doc>Write out tokens to a file or stdout</doc>
                <property name="_location" class="ptolemy.kernel.util.Location" value="290.0, 157.0">
                </property>
                <port name="input" class="ptolemy.actor.TypedIOPort">
                    <property name="input"/>
                    <property name="multiport"/>
                </port>
            </entity>
            <relation name="relation" class="ptolemy.actor.TypedIORelation">
            </relation>
            <link port="Ramp.output" relation="relation"/>
            <link port="FileWriter.input" relation="relation"/>
        </entity>
    </entity>
</entity>
}}

