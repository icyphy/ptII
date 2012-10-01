# Tests for the AddIcon class
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2011-2012 The Regents of the University of California.
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

proc filterTest {moml} {
    set parser [java::new ptolemy.moml.MoMLParser]

    # The list of filters is static, so we reset it in case there
    # filters were already added.
    java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]

    #$parser addMoMLFilters \
    #	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    set filter [java::new ptolemy.moml.filter.AddIcon]

    java::call ptolemy.moml.MoMLParser addMoMLFilter $filter
    set toplevel [$parser parse $moml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
}
######################################################################
####
#
test AddIcon-1.1 {A model with a Const and no _icon} {
    set constMoML  "$header 
<entity name=\"AddIconTest\" class=\"ptolemy.actor.TypedCompositeActor\">
    <entity name=\"Const\" class=\"ptolemy.actor.lib.Const\">
    </entity>
</entity>
"
    filterTest $constMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="AddIconTest" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="9.0.devel">
    </property>
    <entity name="Const" class="ptolemy.actor.lib.Const">
        <property name="_icon" class="ptolemy.vergil.icon.BoxedValueIcon">
            <property name="attributeName" class="ptolemy.kernel.util.StringAttribute" value="value">
            </property>
            <property name="displayWidth" class="ptolemy.data.expr.Parameter" value="40">
            </property>
        </property>
    </entity>
</entity>
}}

######################################################################
####
#
test AddIcon-2.1 {A model with an Expression and no _icon} {
    set expressionMoML  "$header 
<entity name=\"AddIconTest2-1\" class=\"ptolemy.actor.TypedCompositeActor\">
    <entity name=\"Expression\" class=\"ptolemy.actor.lib.Expression\">
    </entity>
</entity>"
    filterTest $expressionMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="AddIconTest2-1" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="9.0.devel">
    </property>
    <entity name="Expression" class="ptolemy.actor.lib.Expression">
        <property name="_icon" class="ptolemy.vergil.icon.BoxedValueIcon">
            <property name="attributeName" class="ptolemy.kernel.util.StringAttribute" value="expression">
            </property>
            <property name="displayWidth" class="ptolemy.data.expr.Parameter" value="60">
            </property>
        </property>
    </entity>
</entity>
}}


######################################################################
####
#
test AddIcon-3.1 {A model with an MathFunction and no _icon} {
    set mathFunctionMoML  "$header 
<entity name=\"AddIconTest3-1\" class=\"ptolemy.actor.TypedCompositeActor\">
    <entity name=\"MathFunction\" class=\"ptolemy.actor.lib.MathFunction\">
    </entity>
</entity>"
    filterTest $mathFunctionMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="AddIconTest3-1" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="9.0.devel">
    </property>
    <entity name="MathFunction" class="ptolemy.actor.lib.MathFunction">
        <property name="_icon" class="ptolemy.vergil.icon.AttributeValueIcon">
            <property name="attributeName" class="ptolemy.kernel.util.StringAttribute" value="function">
            </property>
        </property>
    </entity>
</entity>
}}


######################################################################
####
#
test AddIcon-4.1 {A model with an Scale and no _icon} {
    set scaleMoML  "$header 
<entity name=\"AddIconTest4-1\" class=\"ptolemy.actor.TypedCompositeActor\">
    <entity name=\"Scale\" class=\"ptolemy.actor.lib.Scale\">
    </entity>
</entity>"
    filterTest $scaleMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="AddIconTest4-1" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="9.0.devel">
    </property>
    <entity name="Scale" class="ptolemy.actor.lib.Scale">
        <property name="_icon" class="ptolemy.vergil.icon.AttributeValueIcon">
            <property name="attributeName" class="ptolemy.kernel.util.StringAttribute" value="factor">
            </property>
        </property>
    </entity>
</entity>
}}

######################################################################
####
#
test AddIcon-5.1 {A model with an TrigFunction and no _icon} {
    set trigFunctionMoML  "$header 
<entity name=\"AddIconTest5-1\" class=\"ptolemy.actor.TypedCompositeActor\">
    <entity name=\"TrigFunction\" class=\"ptolemy.actor.lib.TrigFunction\">
    </entity>
</entity>"
    filterTest $trigFunctionMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="AddIconTest5-1" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="9.0.devel">
    </property>
    <entity name="TrigFunction" class="ptolemy.actor.lib.TrigFunction">
        <property name="_icon" class="ptolemy.vergil.icon.AttributeValueIcon">
            <property name="attributeName" class="ptolemy.kernel.util.StringAttribute" value="function">
            </property>
        </property>
    </entity>
</entity>
}}

