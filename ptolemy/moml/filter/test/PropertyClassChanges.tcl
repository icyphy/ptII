# Tests for the PropertyClassChanges class
#
# @Author: Christopher Brooks
#
# @Version: $Id: PropertyClassChanges.tcl 47586 2007-12-17 17:12:22Z cxh $
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
<entity name=\"PropertClassChangesTest\" class=\"ptolemy.actor.TypedCompositeActor\">
    <entity name=\"Occupants14\" class=\"ptolemy.actor.lib.Ramp\">
        <property name=\"_rotatePorts\" class=\"ptolemy.data.expr.Parameter\" value=\"0\">
            <property name=\"_editorFactory\" class=\"ptolemy.kernel.util.Attribute\">
            </property>
        </property>
        <property name=\"_hideName\" class=\"ptolemy.data.expr.SingletonParameter\" value=\"true\">
        </property>
    </entity>
</entity>
"

######################################################################
####
#
test PropertyClassChanges-1.1 {A _hideName that is after an Attribute} { 
    set parser [java::new ptolemy.moml.MoMLParser]

    # The list of filters is static, so we reset it in case there
    # filters were already added.
    $parser setMoMLFilters [java::null]

    #$parser addMoMLFilters \
    #	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    set filter [java::new ptolemy.moml.filter.PropertyClassChanges]


    # ptolemy.copernicus.kernel.KernelMain does this
    $filter put "ptolemy.copernicus.kernel.GeneratorAttribute" [java::null]

    # Test out the remove method by adding a class and then removing it
    $filter put "ptolemy.actor.TypedCompositeActor" [java::null]
    $filter remove "ptolemy.actor.TypedCompositeActor"

    $parser addMoMLFilter $filter
    set toplevel [$parser parse $hideMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="PropertClassChangesTest" class="ptolemy.actor.TypedCompositeActor">
    <entity name="Occupants14" class="ptolemy.actor.lib.Ramp">
        <property name="_rotatePorts" class="ptolemy.data.expr.Parameter" value="0">
            <property name="_editorFactory" class="ptolemy.kernel.util.Attribute">
            </property>
        </property>
        <property name="_hideName" class="ptolemy.data.expr.SingletonParameter" value="true">
        </property>
    </entity>
</entity>
}}
# This should be the last test

# test PropertyClassChanges-999 {clear} {
#     # This removes the graphical classes for all subsequent runs
#     set filter [java::new ptolemy.moml.filter.PropertyClassChanges]
#     $filter clear
#     $filter toString
# } {ptolemy.moml.filter.PropertyClassChanges: Update any actor port class names
# that have been renamed.
# Below are the actors that are affected, along with the port name
# and the new classname:}


