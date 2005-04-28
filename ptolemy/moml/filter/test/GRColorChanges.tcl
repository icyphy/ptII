# Tests for the BackwardCompatibility class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2002-2005 The Regents of the University of California.
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

# Increase timeout from the default set in $PTII/util/testsuite/testDefs.tcl
set timeOutSeconds 6000

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

# Set up parser, add filters
    set parser [java::new ptolemy.moml.MoMLParser]
    # The list of filters is static, so we reset it in case there
    # filters were already added.
    $parser setMoMLFilters [java::null]
    $parser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    $parser addMoMLFilter [java::new \
	    ptolemy.moml.filter.RemoveGraphicalClasses]




######################################################################
####
#

set expressionMoml  "$header 
<entity name=\"ViewScreenProperty\" class=\"ptolemy.actor.TypedCompositeActor\">
    <entity name=\"ViewScreen\" class=\"ptolemy.domains.gr.lib.ViewScreen\">
        <property name=\"backgroundColor\" class=\"ptolemy.data.expr.Parameter\" value=\"\[0.0, 0.0, 0.0\]\">
        </property>
    </entity>
</entity>"

if [catch {java::call Class forName javax.media.j3d.Node} errMsg] {
    puts "Skipping GRColorChanges.tcl backward compat tests."
    puts "  Could not instantiate javax.media.j3d.Node, perhaps Java3D"
    puts "  is not installed?"
} else {

test GRColorChanges-7.3 {Expression: ViewScreen backgroundColor} { 
    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $expressionMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="ViewScreenProperty" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="5.0-beta">
    </property>
    <entity name="ViewScreen" class="ptolemy.domains.gr.lib.ViewScreen3D">
        <property name="backgroundColor" class="ptolemy.actor.gui.ColorAttribute" value="{0.0, 0.0, 0.0}">
        </property>
        <property name="_windowProperties" class="ptolemy.actor.gui.WindowPropertiesAttribute">
        </property>
        <property name="_viewSize" class="ptolemy.actor.gui.SizeAttribute">
        </property>
    </entity>
</entity>
}}

######################################################################
####
#

set expressionMoml  "$header 
<entity name=\"ViewScreenProperty\" class=\"ptolemy.actor.TypedCompositeActor\">
        <entity name=\"Sphere3D\" class=\"ptolemy.domains.gr.lib.Sphere3D\">
            <property name=\"radius\" class=\"ptolemy.data.expr.Parameter\" value=\"0.1\">
            </property>
            <property name=\"shininess\" class=\"ptolemy.actor.parameters.DoubleRangeParameter\" value=\"1.0\">
            </property>
            <property name=\"RGB color\" class=\"ptolemy.data.expr.Parameter\" value=\"\[1.0, 0.1, 0.1\]\">
            </property>
        </entity>
</entity>"

test GRColorChanges-7.4 {Expression: Sphere3D: RGB color} {
    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $expressionMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="ViewScreenProperty" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="5.0-beta">
    </property>
    <entity name="Sphere3D" class="ptolemy.domains.gr.lib.Sphere3D">
        <property name="radius" class="ptolemy.data.expr.Parameter" value="0.1">
        </property>
        <property name="diffuseColor" class="ptolemy.actor.gui.ColorAttribute" value="{1.0, 0.1, 0.1}">
        </property>
        <property name="shininess" class="ptolemy.actor.parameters.DoubleRangeParameter" value="1.0">
        </property>
    </entity>
</entity>
}}


} # else java3d exists
