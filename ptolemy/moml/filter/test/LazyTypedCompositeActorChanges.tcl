# Tests for the LazyTypedCompositeActorChanges class
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

if {[string compare test [info procs jdkCaptureErr]] == 1} then {
   source [file join $PTII util testsuite jdktools.tcl]
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


set header {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">}


set hideMoml  "$header 
<entity name=\"LazyTypedCompositeActorChangesTest\" class=\"ptolemy.actor.TypedCompositeActor\">
    <entity name=\"MyComposite\" class=\"ptolemy.actor.TypedCompositeActor\">
        <property name=\"Foo\" class=\"ptolemy.data.expr.Parameter\" value=\"0\">
         </property>
        <entity name=\"MyInnerComposite\" class=\"ptolemy.actor.TypedCompositeActor\">
            <property name=\"Bar\" class=\"ptolemy.data.expr.Parameter\" value=\"0\"/>
            <entity name=\"Const\" class=\"ptolemy.actor.lib.Const\"/>
        </entity>
    </entity>
</entity>
"

######################################################################
####
#
test LazyTypedCompositeActorChanges-1.1 {Two levels of hierarchy} { 
    set parser [java::new ptolemy.moml.MoMLParser]

    # The list of filters is static, so we reset it in case there
    # filters were already added.
    java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]

    #$parser addMoMLFilters \
    #	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    set filter [java::new ptolemy.moml.filter.LazyTypedCompositeActorChanges]

    java::call ptolemy.moml.MoMLParser addMoMLFilter $filter
    set toplevel [$parser parse $hideMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="LazyTypedCompositeActorChangesTest" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="9.0.devel">
    </property>
    <property name="bidirectionalTypeInference" class="ptolemy.actor.parameters.SharedParameter" value="true">
    </property>
    <entity name="MyComposite" class="ptolemy.actor.LazyTypedCompositeActor">
        <property name="bidirectionalTypeInference" class="ptolemy.actor.parameters.SharedParameter" value="true">
        </property>
        <property name="Foo" class="ptolemy.data.expr.Parameter" value="0">
        </property>
        <configure>
            <group>
                <entity name="MyInnerComposite" class="ptolemy.actor.LazyTypedCompositeActor">
                    <property name="bidirectionalTypeInference" class="ptolemy.actor.parameters.SharedParameter" value="true">
                    </property>
                    <property name="Bar" class="ptolemy.data.expr.Parameter" value="0">
                    </property>
                    <configure>
                        <group>
                            <entity name="Const" class="ptolemy.actor.lib.Const">
                                <property name="bidirectionalTypeInference" class="ptolemy.actor.parameters.SharedParameter" value="true">
                                </property>
                            </entity>
                        </group>
                    </configure>
                </entity>
            </group>
        </configure>
    </entity>
</entity>
}}
# The list of filters is static, so we reset it
java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]



