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
</entity>
}}
