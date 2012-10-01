# Tests for the AddEditorFactory class
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

    set filter [java::new ptolemy.moml.filter.AddMissingParameter]

    java::call ptolemy.moml.MoMLParser addMoMLFilter $filter
    set toplevel [$parser parse $moml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
}

######################################################################
####
#
test AddEditorFactory-1.1 {A model with a Parameter and no _editorParameter} {
    set parameterMoML  "$header 
<entity name=\"AddEditorFactoryTest\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"MyParameter\" class=\"ptolemy.data.expr.Parameter\" value=\"42\">
    </property>
</entity>
"
    filterTest $parameterMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="AddEditorFactoryTest" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="9.0.devel">
    </property>
    <property name="MyParameter" class="ptolemy.data.expr.Parameter" value="42">
    </property>
</entity>
}}

######################################################################
####
#
test AddEditorFactory-2.1 {A model with a Parameter and different _editorFactory} {
    set parameterWithEditorFactoryMoML  "$header 
<entity name=\"AddEditorFactoryTest2_1\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"MyParameter\" class=\"ptolemy.data.expr.Parameter\" value=\"42\">
        <property name=\"_editorFactory\" class=\"ptolemy.data.expr.Parameter\" value=\"2\">
        </property>
    </property>
</entity>
"
    filterTest $parameterWithEditorFactoryMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="AddEditorFactoryTest2_1" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="9.0.devel">
    </property>
    <property name="MyParameter" class="ptolemy.data.expr.Parameter" value="42">
        <property name="_editorFactory" class="ptolemy.data.expr.Parameter" value="2">
        </property>
    </property>
</entity>
}}
