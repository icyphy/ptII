# Tests for the UpdateAnnotations class
#
# @Author: Christopher Brooks
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


set annotationMoML  "$header 
<entity name=\"Expression\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"4.0-beta\">
    </property>
    <property name=\"annotation1\" class=\"ptolemy.kernel.util.Attribute\">
        <property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"140.0, 5.0\">
        </property>
        <property name=\"_iconDescription\" class=\"ptolemy.kernel.util.SingletonConfigurableAttribute\">
            <configure>
           <svg>
           <text x=\"20\" style=\"font-size:14; font-family:sanserif; fill:blue\" y=\"20\">This model repeatedly evaluates an expression, a function of two ramp
signals, slow and fast. Try right clicking on the expr actor, select
\"Configure\" and change it to \"cos(slow)*cos(fast)\" and then run
the demo with View->Run Window->Go. Other interesting alternatives
include:
    \"cos(fast*cos(slow))\"
    \"0.2*slow + cos(fast)\"</text>
           </svg>
           </configure>
        </property>
        <property name=\"_hideName\" class=\"ptolemy.kernel.util.SingletonAttribute\">
        </property>
    </property>
</entity>"


######################################################################
####
#
test UpdateAnnotation-1.1 {Fix an old style annotation} {
    set parser [java::new ptolemy.moml.MoMLParser]

    # The list of filters is static, so we reset it in case there
    # filters were already added.
    $parser setMoMLFilters [java::null]

    $parser addMoMLFilter \
	[java::new ptolemy.moml.filter.UpdateAnnotations]

    set toplevel [$parser parse $annotationMoML]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="Expression" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="7.1.devel">
    </property>
    <property name="annotation1" class="ptolemy.vergil.kernel.attributes.TextAttribute">
        <property name="_hideName" class="ptolemy.data.expr.SingletonParameter" value="true">
        </property>
        <property name="_hideAllParameters" class="ptolemy.data.expr.Parameter" value="true">
        </property>
        <property name="textSize" class="ptolemy.data.expr.Parameter" value="14">
        </property>
        <property name="textColor" class="ptolemy.actor.gui.ColorAttribute" value="{0.0, 0.0, 1.0, 1.0}">
        </property>
        <property name="fontFamily" class="ptolemy.data.expr.StringParameter" value="SansSerif">
        </property>
        <property name="bold" class="ptolemy.data.expr.Parameter" value="false">
        </property>
        <property name="italic" class="ptolemy.data.expr.Parameter" value="false">
        </property>
        <property name="text" class="ptolemy.kernel.util.StringAttribute" value="This model repeatedly evaluates an expression, a function of two ramp&#10;signals, slow and fast. Try right clicking on the expr actor, select&#10;&quot;Configure&quot; and change it to &quot;cos(slow)*cos(fast)&quot; and then run&#10;the demo with View-&gt;Run Window-&gt;Go. Other interesting alternatives&#10;include:&#10;    &quot;cos(fast*cos(slow))&quot;&#10;    &quot;0.2*slow + cos(fast)&quot;">
            <property name="_style" class="ptolemy.actor.gui.style.TextStyle">
                <property name="height" class="ptolemy.data.expr.Parameter" value="20">
                </property>
                <property name="width" class="ptolemy.data.expr.Parameter" value="80">
                </property>
            </property>
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="140.0, 5.0">
        </property>
    </property>
</entity>
}}

set authorMoML  "$header 
<entity name=\"Expression\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"4.0-beta\">
    </property>
    <property name=\"annotation\" class=\"ptolemy.kernel.util.Attribute\">
        <property name=\"_hideName\" class=\"ptolemy.kernel.util.SingletonAttribute\">
        </property>
        <property name=\"_iconDescription\" class=\"ptolemy.kernel.util.SingletonConfigurableAttribute\">
            <configure><svg><text x=\"20\" y=\"20\" style=\"font-size:14; font-family:SansSerif; fill:darkgray\">Author: Edward A. Lee</text></svg></configure>
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
        <property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"\[435.0, 220.0\]\">
        </property>
    </property>
</entity>"

######################################################################
####
#
test UpdateAnnotation-1.2 {Fix an old style author annotation} {
    set parser [java::new ptolemy.moml.MoMLParser]

    # The list of filters is static, so we reset it in case there
    # filters were already added.
    $parser setMoMLFilters [java::null]

    $parser addMoMLFilter \
	[java::new ptolemy.moml.filter.UpdateAnnotations]

    set toplevel [$parser parse $authorMoML]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="Expression" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="7.1.devel">
    </property>
    <property name="annotation" class="ptolemy.vergil.kernel.attributes.TextAttribute">
        <property name="_hideName" class="ptolemy.data.expr.SingletonParameter" value="true">
        </property>
        <property name="_hideAllParameters" class="ptolemy.data.expr.Parameter" value="true">
        </property>
        <property name="textSize" class="ptolemy.data.expr.Parameter" value="14">
        </property>
        <property name="textColor" class="ptolemy.actor.gui.ColorAttribute" value="{0.0, 0.0, 1.0, 1.0}">
        </property>
        <property name="fontFamily" class="ptolemy.data.expr.StringParameter" value="SansSerif">
        </property>
        <property name="bold" class="ptolemy.data.expr.Parameter" value="false">
        </property>
        <property name="italic" class="ptolemy.data.expr.Parameter" value="false">
        </property>
        <property name="text" class="ptolemy.kernel.util.StringAttribute" value="Author: Edward A. Lee">
            <property name="_style" class="ptolemy.actor.gui.style.TextStyle">
                <property name="height" class="ptolemy.data.expr.Parameter" value="20">
                </property>
                <property name="width" class="ptolemy.data.expr.Parameter" value="80">
                </property>
            </property>
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="[435.0, 220.0]">
        </property>
    </property>
</entity>
}}


set expressionDemoMoML  {
<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="Expression" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="4.0-beta">
    </property>
    <property name="annotation1" class="ptolemy.kernel.util.Attribute">
        <property name="_location" class="ptolemy.kernel.util.Location" value="140.0, 5.0">
        </property>
        <property name="_iconDescription" class="ptolemy.kernel.util.SingletonConfigurableAttribute">
            <configure>
           <svg>
           <text x="20" style="font-size:14; font-family:sanserif; fill:blue" y="20">This model repeatedly evaluates an expression, a function of two ramp
signals, slow and fast. Try right clicking on the expr actor, select
"Configure" and change it to "cos(slow)*cos(fast)" and then run
the demo with View->Run Window->Go. Other interesting alternatives
include:
    "cos(fast*cos(slow))"
    "0.2*slow + cos(fast)"</text>
           </svg>
           </configure>
        </property>
        <property name="_hideName" class="ptolemy.kernel.util.SingletonAttribute">
        </property>
    </property>
    <property name="SDFDirector" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="allowDisconnectedGraphs" class="ptolemy.data.expr.Parameter" value="false">
        </property>
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="200">
        </property>
        <property name="vectorizationFactor" class="ptolemy.data.expr.Parameter" value="1">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="80.0, 50.0">
        </property>
    </property>
    <property name="_vergilSize" class="ptolemy.actor.gui.SizeAttribute" value="[619, 256]">
    </property>
    <property name="_vergilLocation" class="ptolemy.actor.gui.LocationAttribute" value="[76, 152]">
    </property>
    <property name="annotation" class="ptolemy.kernel.util.Attribute">
        <property name="_hideName" class="ptolemy.kernel.util.SingletonAttribute">
        </property>
        <property name="_iconDescription" class="ptolemy.kernel.util.SingletonConfigurableAttribute">
            <configure><svg><text x="20" y="20" style="font-size:14; font-family:SansSerif; fill:darkgray">Author: Edward A. Lee</text></svg></configure>
        </property>
        <property name="_smallIconDescription" class="ptolemy.kernel.util.SingletonConfigurableAttribute">
            <configure>
      <svg>
        <text x="20" style="font-size:14; font-family:SansSerif; fill:blue" y="20">-A-</text>
      </svg>
    </configure>
        </property>
        <property name="_controllerFactory" class="ptolemy.vergil.basic.NodeControllerFactory">
        </property>
        <property name="_editorFactory" class="ptolemy.vergil.toolbox.AnnotationEditorFactory">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="[435.0, 220.0]">
        </property>
    </property>
    <property name="_windowProperties" class="ptolemy.actor.gui.WindowPropertiesAttribute" value="{bounds = {76, 154, 834, 365}, maximized = false}">
    </property>
    <property name="_vergilZoomFactor" class="ptolemy.data.expr.ExpertParameter" value="1.0">
    </property>
    <property name="_vergilCenter" class="ptolemy.data.expr.ExpertParameter" value="{309.5, 128.0}">
    </property>
    <entity name="slow" class="ptolemy.actor.lib.Ramp">
        <property name="firingCountLimit" class="ptolemy.data.expr.Parameter" value="0">
        </property>
        <property name="init" class="ptolemy.data.expr.Parameter" value="0.0">
        </property>
        <property name="step" class="ptolemy.actor.parameters.PortParameter" value="PI/100.0">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="63.0, 113.0">
        </property>
    </entity>
    <entity name="fast" class="ptolemy.actor.lib.Ramp">
        <property name="firingCountLimit" class="ptolemy.data.expr.Parameter" value="0">
        </property>
        <property name="init" class="ptolemy.data.expr.Parameter" value="0.0">
        </property>
        <property name="step" class="ptolemy.actor.parameters.PortParameter" value="PI/10.0">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="65.0, 223.5">
        </property>
    </entity>
    <entity name="Plot" class="ptolemy.actor.lib.gui.SequencePlotter">
        <property name="fillOnWrapup" class="ptolemy.data.expr.Parameter" value="true">
        </property>
        <property name="startingDataset" class="ptolemy.data.expr.Parameter" value="0">
        </property>
        <property name="xInit" class="ptolemy.data.expr.Parameter" value="0.0">
        </property>
        <property name="xUnit" class="ptolemy.data.expr.Parameter" value="1.0">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="375.0, 190.0">
        </property>
        <configure>
<?plotml <!DOCTYPE plot PUBLIC "-//UC Berkeley//DTD PlotML 1//EN"
"http://ptolemy.eecs.berkeley.edu/xml/dtd/PlotML_1.dtd">
<plot>
<title></title>
<xRange min="0.0" max="199.0"/>
<yRange min="-1.9510565162951554" max="2.0"/>
<noGrid/>
</plot>?>
        </configure>
    </entity>
    <entity name="Expression2" class="ptolemy.actor.lib.Expression">
        <property name="expression" class="ptolemy.kernel.util.StringAttribute" value="cos(slow) + cos(fast)">
        </property>
        <property name="_icon" class="ptolemy.vergil.icon.BoxedValueIcon">
            <property name="attributeName" class="ptolemy.kernel.util.StringAttribute" value="expression">
            </property>
            <property name="displayWidth" class="ptolemy.data.expr.Parameter" value="60">
            </property>
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="230.0, 190.0">
        </property>
        <port name="slow" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
        </port>
        <port name="fast" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
        </port>
    </entity>
    <relation name="relation" class="ptolemy.actor.TypedIORelation">
    </relation>
    <relation name="relation3" class="ptolemy.actor.TypedIORelation">
    </relation>
    <relation name="relation4" class="ptolemy.actor.TypedIORelation">
    </relation>
    <link port="slow.output" relation="relation"/>
    <link port="fast.output" relation="relation3"/>
    <link port="Plot.input" relation="relation4"/>
    <link port="Expression2.output" relation="relation4"/>
    <link port="Expression2.slow" relation="relation"/>
    <link port="Expression2.fast" relation="relation3"/>
</entity>
}


######################################################################
####
#
test UpdateAnnotation-1.3 {Fix the Expression example} {
    set parser [java::new ptolemy.moml.MoMLParser]

    # The list of filters is static, so we reset it in case there
    # filters were already added.
    $parser setMoMLFilters [java::null]

    $parser addMoMLFilter \
	[java::new ptolemy.moml.filter.UpdateAnnotations]

    set toplevel [$parser parse $expressionDemoMoML]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="Expression" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="7.1.devel">
    </property>
    <property name="annotation1" class="ptolemy.vergil.kernel.attributes.TextAttribute">
        <property name="_hideName" class="ptolemy.data.expr.SingletonParameter" value="true">
        </property>
        <property name="_hideAllParameters" class="ptolemy.data.expr.Parameter" value="true">
        </property>
        <property name="textSize" class="ptolemy.data.expr.Parameter" value="14">
        </property>
        <property name="textColor" class="ptolemy.actor.gui.ColorAttribute" value="{0.0, 0.0, 1.0, 1.0}">
        </property>
        <property name="fontFamily" class="ptolemy.data.expr.StringParameter" value="SansSerif">
        </property>
        <property name="bold" class="ptolemy.data.expr.Parameter" value="false">
        </property>
        <property name="italic" class="ptolemy.data.expr.Parameter" value="false">
        </property>
        <property name="text" class="ptolemy.kernel.util.StringAttribute" value="This model repeatedly evaluates an expression, a function of two ramp&#10;signals, slow and fast. Try right clicking on the expr actor, select&#10;&quot;Configure&quot; and change it to &quot;cos(slow)*cos(fast)&quot; and then run&#10;the demo with View-&gt;Run Window-&gt;Go. Other interesting alternatives&#10;include:&#10;    &quot;cos(fast*cos(slow))&quot;&#10;    &quot;0.2*slow + cos(fast)&quot;">
            <property name="_style" class="ptolemy.actor.gui.style.TextStyle">
                <property name="height" class="ptolemy.data.expr.Parameter" value="20">
                </property>
                <property name="width" class="ptolemy.data.expr.Parameter" value="80">
                </property>
            </property>
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="140.0, 5.0">
        </property>
    </property>
    <property name="SDFDirector" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="200">
        </property>
        <property name="vectorizationFactor" class="ptolemy.data.expr.Parameter" value="1">
        </property>
        <property name="allowDisconnectedGraphs" class="ptolemy.data.expr.Parameter" value="false">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="80.0, 50.0">
        </property>
    </property>
    <property name="_vergilSize" class="ptolemy.actor.gui.SizeAttribute" value="[619, 256]">
    </property>
    <property name="_vergilLocation" class="ptolemy.actor.gui.LocationAttribute" value="[76, 152]">
    </property>
    <property name="annotation" class="ptolemy.vergil.kernel.attributes.TextAttribute">
        <property name="_hideName" class="ptolemy.data.expr.SingletonParameter" value="true">
        </property>
        <property name="_hideAllParameters" class="ptolemy.data.expr.Parameter" value="true">
        </property>
        <property name="textSize" class="ptolemy.data.expr.Parameter" value="14">
        </property>
        <property name="textColor" class="ptolemy.actor.gui.ColorAttribute" value="{0.0, 0.0, 1.0, 1.0}">
        </property>
        <property name="fontFamily" class="ptolemy.data.expr.StringParameter" value="SansSerif">
        </property>
        <property name="bold" class="ptolemy.data.expr.Parameter" value="false">
        </property>
        <property name="italic" class="ptolemy.data.expr.Parameter" value="false">
        </property>
        <property name="text" class="ptolemy.kernel.util.StringAttribute" value="Author: Edward A. Lee">
            <property name="_style" class="ptolemy.actor.gui.style.TextStyle">
                <property name="height" class="ptolemy.data.expr.Parameter" value="20">
                </property>
                <property name="width" class="ptolemy.data.expr.Parameter" value="80">
                </property>
            </property>
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="[435.0, 220.0]">
        </property>
    </property>
    <property name="_windowProperties" class="ptolemy.actor.gui.WindowPropertiesAttribute" value="{bounds = {76, 154, 834, 365}, maximized = false}">
    </property>
    <property name="_vergilZoomFactor" class="ptolemy.data.expr.ExpertParameter" value="1.0">
    </property>
    <property name="_vergilCenter" class="ptolemy.data.expr.ExpertParameter" value="{309.5, 128.0}">
    </property>
    <entity name="slow" class="ptolemy.actor.lib.Ramp">
        <property name="firingCountLimit" class="ptolemy.data.expr.Parameter" value="0">
        </property>
        <property name="init" class="ptolemy.data.expr.Parameter" value="0.0">
        </property>
        <property name="step" class="ptolemy.actor.parameters.PortParameter" value="PI/100.0">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="63.0, 113.0">
        </property>
    </entity>
    <entity name="fast" class="ptolemy.actor.lib.Ramp">
        <property name="firingCountLimit" class="ptolemy.data.expr.Parameter" value="0">
        </property>
        <property name="init" class="ptolemy.data.expr.Parameter" value="0.0">
        </property>
        <property name="step" class="ptolemy.actor.parameters.PortParameter" value="PI/10.0">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="65.0, 223.5">
        </property>
    </entity>
    <entity name="Plot" class="ptolemy.actor.lib.gui.SequencePlotter">
        <property name="fillOnWrapup" class="ptolemy.data.expr.Parameter" value="true">
        </property>
        <property name="_windowProperties" class="ptolemy.actor.gui.WindowPropertiesAttribute">
        </property>
        <property name="_plotSize" class="ptolemy.actor.gui.SizeAttribute">
        </property>
        <property name="startingDataset" class="ptolemy.data.expr.Parameter" value="0">
        </property>
        <property name="xInit" class="ptolemy.data.expr.Parameter" value="0.0">
        </property>
        <property name="xUnit" class="ptolemy.data.expr.Parameter" value="1.0">
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="375.0, 190.0">
        </property>
        <configure>
<?plotml <!DOCTYPE plot PUBLIC "-//UC Berkeley//DTD PlotML 1//EN"
"http://ptolemy.eecs.berkeley.edu/xml/dtd/PlotML_1.dtd">
<plot>
<title></title>
<xRange min="0.0" max="199.0"/>
<yRange min="-1.9510565162951554" max="2.0"/>
<noGrid/>
</plot>?>
        </configure>
    </entity>
    <entity name="Expression2" class="ptolemy.actor.lib.Expression">
        <property name="expression" class="ptolemy.kernel.util.StringAttribute" value="cos(slow) + cos(fast)">
        </property>
        <property name="_icon" class="ptolemy.vergil.icon.BoxedValueIcon">
            <property name="attributeName" class="ptolemy.kernel.util.StringAttribute" value="expression">
            </property>
            <property name="displayWidth" class="ptolemy.data.expr.Parameter" value="60">
            </property>
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="230.0, 190.0">
        </property>
        <port name="slow" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
        </port>
        <port name="fast" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
        </port>
    </entity>
    <relation name="relation" class="ptolemy.actor.TypedIORelation">
    </relation>
    <relation name="relation3" class="ptolemy.actor.TypedIORelation">
    </relation>
    <relation name="relation4" class="ptolemy.actor.TypedIORelation">
    </relation>
    <link port="slow.output" relation="relation"/>
    <link port="fast.output" relation="relation3"/>
    <link port="Plot.input" relation="relation4"/>
    <link port="Expression2.output" relation="relation4"/>
    <link port="Expression2.slow" relation="relation"/>
    <link port="Expression2.fast" relation="relation3"/>
</entity>
}}
