# Tests for the RemoveClasses class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2002-2012 The Regents of the University of California.
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

if {[string compare test [info procs jdkCaptureErr]] == 1} then {
   source [file join $PTII util testsuite jdktools.tcl]
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


set header {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">}


set hideMoml  "$header 
<entity name=\"RemoveClassesHide\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"annotation1\" class=\"ptolemy.kernel.util.Attribute\">
        <property name=\"_iconDescription\" class=\"ptolemy.kernel.util.SingletonConfigurableAttribute\">
            <configure><svg><text x=\"20\" y=\"20\" style=\"font-size:14; font-family:SansSerif; fill:blue\">A simple example that has an annotation
and some actors with icons.
This example is used to test
out MoMLFilter and
RemoveClasses.</text></svg></configure>
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
test RemoveClasses-1.1 {This annotation already has a _hideName} { 
    set parser [java::new ptolemy.moml.MoMLParser]

    # The list of filters is static, so we reset it in case there
    # filters were already added.
    set filter [java::new ptolemy.moml.filter.RemoveClasses]
    java::call ptolemy.moml.MoMLParser addMoMLFilter $filter

    java::call ptolemy.moml.MoMLParser addMoMLFilters \
    	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    # ptolemy.copernicus.kernel.KernelMain does this
    $filter put "ptolemy.copernicus.kernel.GeneratorAttribute" [java::null]
    $filter put "ptolemy.vergil.basic.NodeControllerFactory" [java::null]
    $filter put "ptolemy.vergil.toolbox.AnnotationEditorFactory" [java::null]

    # Test out the remove method by adding a class and then removing it
    $filter put "ptolemy.actor.TypedCompositeActor" [java::null]
    $filter remove "ptolemy.actor.TypedCompositeActor"

    java::call ptolemy.moml.MoMLParser addMoMLFilter $filter
    java::call ptolemy.moml.MoMLParser addMoMLFilter [java::new ptolemy.moml.filter.HideAnnotationNames]
    set toplevel [$parser parse $hideMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="RemoveClassesHide" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="9.0.devel">
    </property>
    <property name="annotation1" class="ptolemy.kernel.util.Attribute">
        <property name="_iconDescription" class="ptolemy.kernel.util.SingletonConfigurableAttribute">
            <configure><svg><text x="20" y="20" style="font-size:14; font-family:SansSerif; fill:blue">A simple example that has an annotation
and some actors with icons.
This example is used to test
out MoMLFilter and
RemoveClasses.</text></svg></configure>
        </property>
        <property name="_smallIconDescription" class="ptolemy.kernel.util.SingletonConfigurableAttribute">
            <configure>
      <svg>
        <text x="20" style="font-size:14; font-family:SansSerif; fill:blue" y="20">-A-</text>
      </svg>
    </configure>
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="190.0, 5.0">
        </property>
        <property name="_hideName" class="ptolemy.kernel.util.SingletonAttribute">
        </property>
    </property>
</entity>
}}


######################################################################
####
#
test RemoveClasses-1.2 {filterAttributeValue} { 
    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    $filter put "ptolemy.actor.gui.SizeAttribute" [java::null]
    $filter put "ptolemy.actor.gui.LocationAttribute" [java::null]
    $filter put "ptolemy.actor.gui.style.ChoiceStyle" [java::null]
    $filter put "ptolemy.vergil.icon.AttributeValueIcon" [java::null]
    $filter put "ptolemy.vergil.icon.BoxedValueIcon" [java::null]
    set toplevel [$parser parseFile "./RemoveGraphicalClasses.xml"]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="MoMLFilter" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="9.0.devel">
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
        <property name="_hideName" class="ptolemy.data.expr.SingletonParameter" value="true">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="190.0, 5.0">
        </property>
    </property>
    <property name="SDF Director" class="ptolemy.domains.sdf.kernel.SDFDirector">
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
    </entity>
    <entity name="TrigFunction" class="ptolemy.actor.lib.TrigFunction">
        <property name="function" class="ptolemy.data.expr.StringParameter" value="sin">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="235.0, 165.0">
        </property>
    </entity>
    <entity name="Test" class="ptolemy.actor.lib.Test">
        <property name="correctValues" class="ptolemy.data.expr.Parameter" value="{1.0,1.0,1.0,1.0,1.0}">
        </property>
        <property name="tolerance" class="ptolemy.data.expr.Parameter" value="1.0E-9">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="355.0, 165.0">
        </property>
    </entity>
    <relation name="relation" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <relation name="relation2" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
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
test RemoveClasses-1.3 {Try a configuration has a class that we are going to remove but is <entity name= class=\> instead of <entity name= class=>...</entity>} { 
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

test RemoveClasses-1.4 {Try a configuration has a class that we are going to remove but is <entity name= class=\> instead of <entity name= class=>...</entity>} { 
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
            <doc>default entity library</doc>
        </group>
    </configure>
</entity>
}}

######################################################################
####
#
set hideMoml  {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="DisplayTest" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="7.1.devel">
    </property>
    <property name="SDF Director" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="5">
        </property>
    </property>
    <entity name="Ramp" class="ptolemy.actor.lib.Ramp">
    </entity>
    <entity name="Display" class="ptolemy.actor.lib.gui.Display">
        <property name="_windowProperties" class="ptolemy.actor.gui.WindowPropertiesAttribute" value="{bounds={477, 346, 484, 208}, maximized=false}">
        </property>
        <property name="_paneSize" class="ptolemy.actor.gui.SizeAttribute" value="[484, 164]">
        </property>
        <property name="rowsDisplayed" class="ptolemy.data.expr.Parameter" value="7">
        </property>
        <property name="columnsDisplayed" class="ptolemy.data.expr.Parameter" value="10">
        </property>
        <property name="title" class="ptolemy.kernel.util.StringAttribute" value="My Display">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="{290.0, 220.0}">
        </property>
    </entity>
    <relation name="relation2" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <link port="Ramp.output" relation="relation2"/>
    <link port="Display.input" relation="relation2"/>
</entity>
}

test RemoveClasses-2.1 {Filter a Display} {
    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    removeGraphicalClasses $parser
    set toplevel [java::cast ptolemy.actor.CompositeActor [$parser parse $hideMoml]]
    set newMoML [$toplevel exportMoML]

    set workspace [$toplevel workspace]
    set manager [java::new ptolemy.actor.Manager \
	    $workspace "compatibilityChecking"]
    
    $toplevel setManager $manager
    $manager execute
    
    list $newMoML

} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="DisplayTest" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="9.0.devel">
    </property>
    <property name="SDF Director" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="5">
        </property>
    </property>
    <entity name="Ramp" class="ptolemy.actor.lib.Ramp">
    </entity>
    <entity name="Display" class="ptolemy.actor.lib.Discard">
        <property name="rowsDisplayed" class="ptolemy.data.expr.Parameter" value="7">
        </property>
        <property name="columnsDisplayed" class="ptolemy.data.expr.Parameter" value="10">
        </property>
        <property name="title" class="ptolemy.data.expr.StringParameter" value="My Display">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="{290.0, 220.0}">
        </property>
    </entity>
    <relation name="relation2" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <link port="Ramp.output" relation="relation2"/>
    <link port="Display.input" relation="relation2"/>
</entity>
}}


######################################################################
####
#
set hideMoml {
<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="RemovePlots" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="7.1.devel">
    </property>
    <property name="SDF Director" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="100">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="{120, 60}">
        </property>
    </property>
    <property name="_windowProperties" class="ptolemy.actor.gui.WindowPropertiesAttribute" value="{bounds={227, 353, 813, 506}, maximized=false}">
    </property>
    <property name="_vergilSize" class="ptolemy.actor.gui.SizeAttribute" value="[600, 400]">
    </property>
    <property name="_vergilZoomFactor" class="ptolemy.data.expr.ExpertParameter" value="1.0">
    </property>
    <property name="_vergilCenter" class="ptolemy.data.expr.ExpertParameter" value="{300.0, 200.0}">
    </property>
    <entity name="Ramp" class="ptolemy.actor.lib.Ramp">
        <doc>Create a sequence of tokens with increasing value</doc>
        <property name="_location" class="ptolemy.kernel.util.Location" value="{115, 200}">
        </property>
    </entity>
    <entity name="Distributor" class="ptolemy.actor.lib.Distributor">
        <property name="_location" class="ptolemy.kernel.util.Location" value="[255.0, 200.0]">
        </property>
    </entity>
    <entity name="RealTimePlotter" class="ptolemy.actor.lib.gui.RealTimePlotter">
        <property name="_windowProperties" class="ptolemy.actor.gui.WindowPropertiesAttribute" value="{bounds={470, 278, 500, 344}, maximized=false}">
        </property>
        <property name="_plotSize" class="ptolemy.actor.gui.SizeAttribute" value="[500, 300]">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="[500.0, 65.0]">
        </property>
        <configure>
<?plotml <!DOCTYPE plot PUBLIC "-//UC Berkeley//DTD PlotML 1//EN"
"http://ptolemy.eecs.berkeley.edu/xml/dtd/PlotML_1.dtd">
<plot>
<title>RealTimePlotter</title>
<xRange min="0.108" max="0.179"/>
<yRange min="0.0" max="796.0"/>
</plot>?>
        </configure>
    </entity>
    <entity name="ArrayPlotter" class="ptolemy.actor.lib.gui.ArrayPlotter">
        <property name="_windowProperties" class="ptolemy.actor.gui.WindowPropertiesAttribute" value="{bounds={470, 277, 500, 344}, maximized=false}">
        </property>
        <property name="_plotSize" class="ptolemy.actor.gui.SizeAttribute" value="[500, 300]">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="[505.0, 355.0]">
        </property>
        <configure>
<?plotml <!DOCTYPE plot PUBLIC "-//UC Berkeley//DTD PlotML 1//EN"
"http://ptolemy.eecs.berkeley.edu/xml/dtd/PlotML_1.dtd">
<plot>
<title>ArrayPlotter</title>
<xRange min="0.0" max="1.0"/>
<yRange min="795.0" max="799.0"/>
</plot>?>
        </configure>
    </entity>
    <entity name="SequencePlotter" class="ptolemy.actor.lib.gui.SequencePlotter">
        <property name="_windowProperties" class="ptolemy.actor.gui.WindowPropertiesAttribute" value="{bounds={762, 30, 500, 344}, maximized=false}">
        </property>
        <property name="_plotSize" class="ptolemy.actor.gui.SizeAttribute" value="[500, 300]">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="[505.0, 210.0]">
        </property>
        <configure>
<?plotml <!DOCTYPE plot PUBLIC "-//UC Berkeley//DTD PlotML 1//EN"
"http://ptolemy.eecs.berkeley.edu/xml/dtd/PlotML_1.dtd">
<plot>
<title>SequencePlotter</title>
<xRange min="0.0" max="199.0"/>
<yRange min="2.0" max="798.0"/>
</plot>?>
        </configure>
    </entity>
    <entity name="HistogramPlotter" class="ptolemy.actor.lib.gui.HistogramPlotter">
        <property name="_windowProperties" class="ptolemy.actor.gui.WindowPropertiesAttribute" value="{bounds={849, 103, 500, 344}, maximized=false}">
        </property>
        <property name="_plotSize" class="ptolemy.actor.gui.SizeAttribute" value="[500, 300]">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="{505.0, 130.0}">
        </property>
        <configure>
<?plotml <!DOCTYPE plot PUBLIC "-//UC Berkeley//DTD PlotML 1//EN"
"http://ptolemy.eecs.berkeley.edu/xml/dtd/PlotML_1.dtd">
<plot>
<title>HistogramPlotter</title>
<xRange min="1.5" max="798.0"/>
<yRange min="0.0" max="1.0"/>
<barGraph width="0.5" offset="0.15"/>
<bin width="1.0" offset="0.5"/>
</plot>?>
        </configure>
    </entity>
    <entity name="BarGraph" class="ptolemy.actor.lib.gui.BarGraph">
        <property name="_windowProperties" class="ptolemy.actor.gui.WindowPropertiesAttribute" value="{bounds={846, 501, 500, 344}, maximized=false}">
        </property>
        <property name="_plotSize" class="ptolemy.actor.gui.SizeAttribute" value="[500, 300]">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="[505.0, 290.0]">
        </property>
        <configure>
<?plotml <!DOCTYPE plot PUBLIC "-//UC Berkeley//DTD PlotML 1//EN"
"http://ptolemy.eecs.berkeley.edu/xml/dtd/PlotML_1.dtd">
<plot>
<title>BarGraph</title>
<xRange min="0.0" max="1.0"/>
<yRange min="0.0" max="799.0"/>
<default connected="no"/>
<barGraph width="0.5" offset="0.05"/>
</plot>?>
        </configure>
    </entity>
    <entity name="SequenceToArray" class="ptolemy.domains.sdf.lib.SequenceToArray">
        <property name="arrayLength" class="ptolemy.actor.parameters.PortParameter" value="2">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="[350.0, 270.0]">
        </property>
    </entity>
    <relation name="relation2" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <relation name="relation" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <relation name="relation3" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <relation name="relation5" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <relation name="relation6" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <relation name="relation4" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <relation name="relation7" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <link port="Ramp.output" relation="relation2"/>
    <link port="Distributor.input" relation="relation2"/>
    <link port="Distributor.output" relation="relation"/>
    <link port="Distributor.output" relation="relation3"/>
    <link port="Distributor.output" relation="relation5"/>
    <link port="Distributor.output" relation="relation7"/>
    <link port="RealTimePlotter.input" relation="relation"/>
    <link port="ArrayPlotter.input" relation="relation4"/>
    <link port="SequencePlotter.input" relation="relation5"/>
    <link port="HistogramPlotter.input" relation="relation3"/>
    <link port="BarGraph.input" relation="relation6"/>
    <link port="SequenceToArray.input" relation="relation7"/>
    <link port="SequenceToArray.output" relation="relation6"/>
    <link port="SequenceToArray.output" relation="relation4"/>
</entity>
}

test RemoveClasses-2.2 {Filter a SequencePlotter} {
    set parser [java::new ptolemy.moml.MoMLParser]

    # Note that 1.1 added the filter for all the parsers
    removeGraphicalClasses $parser
    set toplevel [java::cast ptolemy.actor.CompositeActor [$parser parse $hideMoml]]
    set newMoML [$toplevel exportMoML]

    set workspace [$toplevel workspace]
    set manager [java::new ptolemy.actor.Manager \
	    $workspace "compatibilityChecking"]
    
    $toplevel setManager $manager
    $manager execute
    
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="RemovePlots" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="11.0.devel">
    </property>
    <property name="SDF Director" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="100">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="{120, 60}">
        </property>
    </property>
    <property name="_vergilZoomFactor" class="ptolemy.data.expr.ExpertParameter" value="1.0">
    </property>
    <property name="_vergilCenter" class="ptolemy.data.expr.ExpertParameter" value="{300.0, 200.0}">
    </property>
    <entity name="Ramp" class="ptolemy.actor.lib.Ramp">
        <doc>Create a sequence of tokens with increasing value</doc>
        <property name="_location" class="ptolemy.kernel.util.Location" value="{115, 200}">
        </property>
    </entity>
    <entity name="Distributor" class="ptolemy.actor.lib.Distributor">
        <property name="_location" class="ptolemy.kernel.util.Location" value="[255.0, 200.0]">
        </property>
    </entity>
    <entity name="RealTimePlotter" class="ptolemy.moml.filter.DiscardDoubles">
        <property name="_location" class="ptolemy.kernel.util.Location" value="[500.0, 65.0]">
        </property>
    </entity>
    <entity name="ArrayPlotter" class="ptolemy.moml.filter.DiscardDoublesArray">
        <property name="_location" class="ptolemy.kernel.util.Location" value="[505.0, 355.0]">
        </property>
    </entity>
    <entity name="SequencePlotter" class="ptolemy.moml.filter.DiscardDoubles">
        <property name="_location" class="ptolemy.kernel.util.Location" value="[505.0, 210.0]">
        </property>
    </entity>
    <entity name="HistogramPlotter" class="ptolemy.moml.filter.DiscardDoubles">
        <property name="_location" class="ptolemy.kernel.util.Location" value="{505.0, 130.0}">
        </property>
    </entity>
    <entity name="BarGraph" class="ptolemy.moml.filter.DiscardDoublesArray">
        <property name="_location" class="ptolemy.kernel.util.Location" value="[505.0, 290.0]">
        </property>
    </entity>
    <entity name="SequenceToArray" class="ptolemy.domains.sdf.lib.SequenceToArray">
        <property name="arrayLength" class="ptolemy.actor.parameters.PortParameter" value="2">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="[350.0, 270.0]">
        </property>
    </entity>
    <relation name="relation2" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <relation name="relation" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <relation name="relation3" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <relation name="relation5" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <relation name="relation6" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <relation name="relation4" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <relation name="relation7" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <link port="Ramp.output" relation="relation2"/>
    <link port="Distributor.input" relation="relation2"/>
    <link port="Distributor.output" relation="relation"/>
    <link port="Distributor.output" relation="relation3"/>
    <link port="Distributor.output" relation="relation5"/>
    <link port="Distributor.output" relation="relation7"/>
    <link port="RealTimePlotter.input" relation="relation"/>
    <link port="ArrayPlotter.input" relation="relation4"/>
    <link port="SequencePlotter.input" relation="relation5"/>
    <link port="HistogramPlotter.input" relation="relation3"/>
    <link port="BarGraph.input" relation="relation6"/>
    <link port="SequenceToArray.input" relation="relation7"/>
    <link port="SequenceToArray.output" relation="relation6"/>
    <link port="SequenceToArray.output" relation="relation4"/>
</entity>
}}

# This should be the last test

test RemoveClasses-1.5 {clear} {
    # This removes the graphical classes for all subsequent runs
    set filter [java::new ptolemy.moml.filter.RemoveClasses]
    java::call ptolemy.moml.filter.RemoveClasses clear
    $filter toString
} {ptolemy.moml.filter.RemoveClasses: Remove or replace classes.
The following actors are affected:
}

# The list of filters is static, so we reset it
java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
