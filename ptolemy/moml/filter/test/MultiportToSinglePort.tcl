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


set autocorrelationMoML  "$header 
<entity name=\"autocorrelationMoML\" class=\"ptolemy.actor.TypedCompositeActor\">
    <entity name=\"Symmetric\" class=\"ptolemy.domains.sdf.lib.Autocorrelation\">
        <property name=\"numberOfInputs\" class=\"ptolemy.data.expr.Parameter\" value=\"10\">
        </property>
        <property name=\"numberOfLags\" class=\"ptolemy.data.expr.Parameter\" value=\"3\">
        </property>
        <property name=\"biased\" class=\"ptolemy.data.expr.Parameter\" value=\"false\">
        </property>
        <property name=\"symmetricOutput\" class=\"ptolemy.data.expr.Parameter\" value=\"true\">
        </property>
        <doc>Autocorrelation estimator</doc>
        <property name=\"_location\" class=\"ptolemy.moml.Location\" value=\"206.0, 201.0\">
        </property>
        <port name=\"input\" class=\"ptolemy.domains.sdf.kernel.SDFIOPort\">
            <property name=\"input\"/>
            <property name=\"tokenConsumptionRate\" class=\"ptolemy.data.expr.Parameter\" value=\"10\">
            </property>
            <property name=\"tokenInitProduction\" class=\"ptolemy.data.expr.Parameter\" value=\"0\">
            </property>
            <property name=\"tokenProductionRate\" class=\"ptolemy.data.expr.Parameter\" value=\"0\">
            </property>
        </port>
        <port name=\"output\" class=\"ptolemy.domains.sdf.kernel.SDFIOPort\">
            <property name=\"output\"/>
            <property name=\"multiport\"/>
            <property name=\"tokenConsumptionRate\" class=\"ptolemy.data.expr.Parameter\" value=\"0\">
            </property>
            <property name=\"tokenInitProduction\" class=\"ptolemy.data.expr.Parameter\" value=\"0\">
            </property>
            <property name=\"tokenProductionRate\" class=\"ptolemy.data.expr.Parameter\" value=\"1\">
            </property>
        </port>
    </entity>
</entity>"


######################################################################
####
#
test MultiportToSinglePort-1.1 {output port should not be a multi port} { 
    set parser [java::new ptolemy.moml.MoMLParser]

    # The list of filters is static, so we reset it in case there
    # filters were already added.
    $parser setMoMLFilters [java::null]

    $parser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    set toplevel [$parser parse $autocorrelationMoML]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="autocorrelationMoML" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="3.1-devel">
    </property>
    <entity name="Symmetric" class="ptolemy.domains.sdf.lib.Autocorrelation">
        <property name="numberOfInputs" class="ptolemy.data.expr.Parameter" value="10">
        </property>
        <property name="numberOfLags" class="ptolemy.data.expr.Parameter" value="3">
        </property>
        <property name="biased" class="ptolemy.data.expr.Parameter" value="false">
        </property>
        <property name="symmetricOutput" class="ptolemy.data.expr.Parameter" value="true">
        </property>
        <doc>Autocorrelation estimator</doc>
        <property name="_location" class="ptolemy.kernel.util.Location" value="206.0, 201.0">
        </property>
    </entity>
</entity>
}}
