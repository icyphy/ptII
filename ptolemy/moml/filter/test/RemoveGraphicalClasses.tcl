# Tests for the RemoveGraphicalClasses class
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

# Load up the test definitions.
if {[string compare test [info procs removeGraphicalClasses]] == 1} then {
    source [file join $PTII util testsuite removeGraphicalClasses.tcl]
}
# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


set header {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">}


set hideMoml  "$header 
<entity name=\"RemoveGraphicalClassesHide\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"annotation1\" class=\"ptolemy.kernel.util.Attribute\">
        <property name=\"_iconDescription\" class=\"ptolemy.kernel.util.SingletonConfigurableAttribute\">
            <configure><svg><text x=\"20\" y=\"20\" style=\"font-size:14; font-family:SansSerif; fill:blue\">A simple example that has an annotation
and some actors with icons.
This example is used to test
out MoMLFilter and
RemoveGraphicalClasses.</text></svg></configure>
        </property>
        <property name=\"_smallIconDescription\" class=\"ptolemy.kernel.util.SingletonConfigurableAttribute\">
            <configure>
      <svg>
        <text x=\"20\" style=\"font-size:14; font-family:SansSerif; fill:blue\" y=\"20\">-A-</text>
      </svg>
    </configure>
        </property>
        <property name=\"_controllerFactory\" class=\"ptolemy.vergil.basic.NodeControllerFactory\">
        </property>
        <property name=\"_editorFactory\" class=\"ptolemy.vergil.toolbox.AnnotationEditorFactory\">
        </property>
        <property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"190.0, 5.0\">
        </property>
        <property name=\"_hideName\" class=\"ptolemy.data.expr.Parameter\">
        </property>
    </property>
</entity>"

######################################################################
####
#
test RemoveGraphicalClasses-1.1 {This annotation already has a _hideName} { 
    set parser [java::new ptolemy.moml.MoMLParser]

    # The list of filters is static, so we reset it in case there
    # filters were already added.
    $parser setMoMLFilters [java::null]

    $parser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    set filter [java::new ptolemy.moml.filter.RemoveGraphicalClasses]

    # ptolemy.copernicus.kernel.KernelMain does this
    $filter put "ptolemy.copernicus.kernel.GeneratorAttribute" [java::null]

    # Test out the remove method by adding a class and then removing it
    $filter put "ptolemy.actor.TypedCompositeActor" [java::null]
    $filter remove "ptolemy.actor.TypedCompositeActor"

    $parser addMoMLFilter $filter
    $parser addMoMLFilter [java::new ptolemy.moml.filter.HideAnnotationNames]
    set toplevel [$parser parse $hideMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="RemoveGraphicalClassesHide" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2">
    </property>
    <property name="annotation1" class="ptolemy.kernel.util.Attribute">
        <property name="_iconDescription" class="ptolemy.kernel.util.SingletonConfigurableAttribute">
            <configure><svg><text x="20" y="20" style="font-size:14; font-family:SansSerif; fill:blue">A simple example that has an annotation
and some actors with icons.
This example is used to test
out MoMLFilter and
RemoveGraphicalClasses.</text></svg></configure>
        </property>
        <property name="_smallIconDescription" class="ptolemy.kernel.util.SingletonConfigurableAttribute">
            <configure>
      <svg>
        <text x="20" style="font-size:14; font-family:SansSerif; fill:blue" y="20">-A-</text>
      </svg>
    </configure>
        </property>
        <property name="_hideName" class="ptolemy.data.expr.Parameter">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="190.0, 5.0">
        </property>
    </property>
</entity>
}}


######################################################################
####
#
test RemoveGraphicalClasses-1.2 {filterAttributeValue} { 
    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parseFile "./RemoveGraphicalClasses.xml"]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="MoMLFilter" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2">
    </property>
    <property name="_vergilSize" class="ptolemy.actor.gui.SizeAttribute" value="[600, 400]">
    </property>
    <property name="_vergilLocation" class="ptolemy.actor.gui.LocationAttribute" value="[104, 127]">
    </property>
    <property name="annotation1" class="ptolemy.kernel.util.Attribute">
        <property name="_iconDescription" class="ptolemy.kernel.util.SingletonConfigurableAttribute">
            <configure><svg><text x="20" y="20" style="font-size:14; font-family:SansSerif; fill:blue">A simple example that has an annotation
and some actors with icons.
This example is used to test
out MoMLFilter and
RemoveGraphicalClasses.</text></svg></configure>
        </property>
        <property name="_smallIconDescription" class="ptolemy.kernel.util.SingletonConfigurableAttribute">
            <configure>
      <svg>
        <text x="20" style="font-size:14; font-family:SansSerif; fill:blue" y="20">-A-</text>
      </svg>
    </configure>
        </property>
        <property name="_hideName" class="ptolemy.data.expr.Parameter">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="190.0, 5.0">
        </property>
    </property>
    <property name="SDF Director" class="ptolemy.domains.sdf.kernel.SDFDirector">
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
        <property name="_location" class="ptolemy.kernel.util.Location" value="100.0, 45.0">
        </property>
    </property>
    <entity name="Const" class="ptolemy.actor.lib.Const">
        <property name="value" class="ptolemy.data.expr.Parameter" value="PI/2">
        </property>
        <doc>Create a constant sequence</doc>
        <property name="_location" class="ptolemy.kernel.util.Location" value="100.0, 165.0">
        </property>
        <port name="output" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
        <port name="trigger" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="multiport"/>
        </port>
    </entity>
    <entity name="TrigFunction" class="ptolemy.actor.lib.TrigFunction">
        <property name="function" class="ptolemy.data.expr.StringParameter" value="sin">
            <property name="style" class="ptolemy.actor.gui.style.ChoiceStyle">
                <property name="acos" class="ptolemy.kernel.util.StringAttribute" value="acos">
                </property>
                <property name="asin" class="ptolemy.kernel.util.StringAttribute" value="asin">
                </property>
                <property name="atan" class="ptolemy.kernel.util.StringAttribute" value="atan">
                </property>
                <property name="cos" class="ptolemy.kernel.util.StringAttribute" value="cos">
                </property>
                <property name="sin" class="ptolemy.kernel.util.StringAttribute" value="sin">
                </property>
                <property name="tan" class="ptolemy.kernel.util.StringAttribute" value="tan">
                </property>
            </property>
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="235.0, 165.0">
        </property>
        <port name="input" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
        </port>
        <port name="output" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
    </entity>
    <entity name="Test" class="ptolemy.actor.lib.Test">
        <property name="correctValues" class="ptolemy.data.expr.Parameter" value="{1.0,1.0,1.0,1.0,1.0}">
        </property>
        <property name="tolerance" class="ptolemy.data.expr.Parameter" value="1.0E-9">
        </property>
        <property name="trainingMode" class="ptolemy.data.expr.Parameter" value="false">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="355.0, 165.0">
        </property>
        <port name="input" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="multiport"/>
        </port>
        <port name="output" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
    </entity>
    <relation name="relation" class="ptolemy.actor.TypedIORelation">
    </relation>
    <relation name="relation2" class="ptolemy.actor.TypedIORelation">
    </relation>
    <link port="Const.output" relation="relation"/>
    <link port="TrigFunction.input" relation="relation"/>
    <link port="TrigFunction.output" relation="relation2"/>
    <link port="Test.input" relation="relation2"/>
</entity>
}}

######################################################################
####
#
set hideMoml  "$header 
<entity name=\"configuration\" class=\"ptolemy.actor.gui.Configuration\">
  <entity name=\"sources\" class=\"ptolemy.moml.EntityLibrary\">
    <configure>
      <group>
        <entity name=\"SketchedSource2\"
	        class=\"ptolemy.actor.lib.gui.SketchedSource\"/>
      </group>
    </configure>
  </entity>
</entity>
"
test RemoveGraphicalClasses-1.3 {Try a configuration has a class that we are going to remove but is <entity name= class=\> instead of <entity name= class=>...</entity>} { 
    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    removeGraphicalClasses $parser
    set toplevel [$parser parse $hideMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="configuration" class="ptolemy.actor.gui.Configuration">
    <entity name="sources" class="ptolemy.moml.EntityLibrary">
        <configure>
            <group>
                <property name="_libraryMarker" class="ptolemy.kernel.util.Attribute">
                </property>
            </group>
        </configure>
    </entity>
</entity>
}}

######################################################################
####
#
set hideMoml  "$header 
<entity name=\"sources\" class=\"ptolemy.moml.EntityLibrary\">
  <configure>
    <?moml
      <group>
<doc>default entity library</doc>

<entity name=\"SketchedSource\" class=\"ptolemy.actor.lib.gui.SketchedSource\">
<doc>bar</doc>
</entity>
          </group>
        ?>
      </configure>
</entity>
"

test RemoveGraphicalClasses-1.4 {Try a configuration has a class that we are going to remove but is <entity name= class=\> instead of <entity name= class=>...</entity>} { 
    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    removeGraphicalClasses $parser
    set toplevel [$parser parse $hideMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="sources" class="ptolemy.moml.EntityLibrary">
    <configure>
        <group>
            <property name="_libraryMarker" class="ptolemy.kernel.util.Attribute">
            </property>
            <doc>default entity library</doc>
        </group>
    </configure>
</entity>
}}

