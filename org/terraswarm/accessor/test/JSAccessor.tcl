# Test importing of Accessors
#
# @Author: Christopher Brooks
#
# @Version: $Id: FMUImport.tcl 71772 2015-03-18 22:34:22Z cxh $
#
# @Copyright (c) 2015 The Regents of the University of California.
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

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

#set VERBOSE 1
if {[string compare sdfModel [info procs sdfModel]] != 0} \
        then {
    source [file join $PTII util testsuite models.tcl]
} {}

# The list of filters is static, so we reset it in case there
# filters were already added.
java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
java::call ptolemy.moml.MoMLParser addMoMLFilters \
    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]


######################################################################
####
#
test JSAccessor-1.1 {Test out importing of accessors} {
    # This is similar to ptolemy/actor/lib/fmi/test/FMUImport.tcl

    set e1 [sdfModel 5]
    set accessorFile [java::call ptolemy.util.FileUtilities nameToFile {$CLASSPATH/org/terraswarm/accessor/test/auto/accessors/Accessor1.xml} [java::null]]
    set urlSpec [$accessorFile getCanonicalPath]
    set changeRequestText [java::call org.terraswarm.accessor.JSAccessor accessorToMoML $urlSpec]

    java::call org.terraswarm.accessor.JSAccessor handleAccessorMoMLChangeRequest $e1 $urlSpec $e1 $changeRequestText 100 100

    set accessor [$e1 getEntity {Accessor}]
    set moml [$accessor exportMoML]
    regsub {value=".*/org/terraswarm/accessor/test/auto/accessors/Accessor1.xml"} $moml {value="$CLASSPATH/org/terraswarm/accessor/test/auto/accessors/Accessor1.xml"} moml2
    # Deal with backslashes in MS-DOS-based systems.  Why we need to do this in this day and age is beyond me.
    regsub {value=".*\\org\\terraswarm\\accessor\\test\\auto\\accessors\Accessor1.xml"} $moml2 {value="$CLASSPATH/org/terraswarm/accessor/test/auto/accessors/Accessor1.xml"} moml3
    list $moml3
} {{<entity name="Accessor" class="org.terraswarm.accessor.JSAccessor">
    <property name="script" class="ptolemy.actor.parameters.PortParameter" value="&#10;    // &#10;	function fire() {&#10;	  var stringValue = get(stringInput);&#10;	  send(stringValue, stringOutput);&#10;	  var numericValue = get(numericInput);&#10;	  send(numericValue, numericOutput);&#10;	  stringValue = get(stringInputWithoutValue);&#10;	  send(stringValue, stringOutputWithoutValue);&#10;	  send(stringValue == null, inputIsAbsent);&#10;	}&#10;	// &#10;  ">
    </property>
    <property name="accessorSource" class="ptolemy.kernel.util.StringAttribute" value="$CLASSPATH/org/terraswarm/accessor/test/auto/accessors/Accessor1.xml">
    </property>
    <property name="documentation" class="ptolemy.vergil.basic.DocAttribute">
        <property name="description" class="ptolemy.kernel.util.StringAttribute" value="&#10;    &#10;This is a test accessor used to test Import--&gt;Accessor.&#10;It also tests handling of absent inputs and sending null to an output.&#10;	&#10;  ">
        </property>
        <property name="author" class="ptolemy.kernel.util.StringAttribute" value="Edward A. Lee">
        </property>
        <property name="version" class="ptolemy.kernel.util.StringAttribute" value="0.1">
        </property>
        <property name="error (port)" class="ptolemy.kernel.util.StringAttribute" value="The error message if an error occurs. If this port is not connected and an error occurs, then an exception is thrown instead.">
        </property>
        <property name="stringInput (port-parameter)" class="ptolemy.kernel.util.StringAttribute" value="String input.">
        </property>
        <property name="numericInput (port-parameter)" class="ptolemy.kernel.util.StringAttribute" value="Numeric input.">
        </property>
        <property name="stringInputWithoutValue (port)" class="ptolemy.kernel.util.StringAttribute" value="String input without a value attribute.">
        </property>
        <property name="stringOutput (port)" class="ptolemy.kernel.util.StringAttribute" value="String output.">
        </property>
        <property name="numericOutput (port)" class="ptolemy.kernel.util.StringAttribute" value="Numeric output.">
        </property>
        <property name="stringOutputWithoutValue (port)" class="ptolemy.kernel.util.StringAttribute" value="String output for input without a value field.">
        </property>
        <property name="inputIsAbsent (port)" class="ptolemy.kernel.util.StringAttribute" value="Output used to indicate that an input is missing.">
        </property>
    </property>
    <property name="stringInput" class="ptolemy.actor.parameters.PortParameter" value="&quot;Foo&quot;">
    </property>
    <property name="numericInput" class="ptolemy.actor.parameters.PortParameter" value="0">
    </property>
    <property name="_location" class="ptolemy.kernel.util.Location" value="{100.0, 100.0}">
    </property>
    <port name="stringInput" class="ptolemy.actor.parameters.ParameterPort">
        <property name="input"/>
        <property name="_showName" class="ptolemy.data.expr.SingletonParameter" value="true">
        </property>
        <property name="defaultValue" class="ptolemy.data.expr.Parameter">
        </property>
    </port>
    <port name="numericInput" class="ptolemy.actor.parameters.ParameterPort">
        <property name="input"/>
        <property name="_showName" class="ptolemy.data.expr.SingletonParameter" value="true">
        </property>
        <property name="defaultValue" class="ptolemy.data.expr.Parameter">
        </property>
    </port>
    <port name="stringInputWithoutValue" class="ptolemy.actor.TypedIOPort">
        <property name="input"/>
        <property name="_type" class="ptolemy.actor.TypeAttribute" value="string">
        </property>
    </port>
    <port name="stringOutput" class="ptolemy.actor.TypedIOPort">
        <property name="output"/>
        <property name="_type" class="ptolemy.actor.TypeAttribute" value="string">
        </property>
    </port>
    <port name="numericOutput" class="ptolemy.actor.TypedIOPort">
        <property name="output"/>
        <property name="_type" class="ptolemy.actor.TypeAttribute" value="double">
        </property>
    </port>
    <port name="stringOutputWithoutValue" class="ptolemy.actor.TypedIOPort">
        <property name="output"/>
        <property name="_type" class="ptolemy.actor.TypeAttribute" value="string">
        </property>
    </port>
    <port name="inputIsAbsent" class="ptolemy.actor.TypedIOPort">
        <property name="output"/>
        <property name="_type" class="ptolemy.actor.TypeAttribute" value="boolean">
        </property>
    </port>
</entity>
}}

# This is similar to ptolemy/actor/lib/fmi/test/FMUImport.tcl
proc importAccessor {accessorFileName} {
    set e1 [sdfModel 5]
    set accessorFile [java::call ptolemy.util.FileUtilities nameToFile $accessorFileName [java::null]]

    set urlSpec [$accessorFile getCanonicalPath]
    set changeRequestText [java::call org.terraswarm.accessor.JSAccessor accessorToMoML $urlSpec]

    java::call org.terraswarm.accessor.JSAccessor handleAccessorMoMLChangeRequest $e1 $urlSpec $e1 $changeRequestText 100 100

    set accessorActorFileName [lindex [file split $accessorFileName] end]
    set accessorActorName [string range $accessorActorFileName 0 [expr {[string length $accessorActorFileName] -5}]]
    #puts "accessorActorFileName: $accessorActorFileName"
    #puts "accessorActorName: $accessorActorName"
    set entityList [$e1 entityList [java::call Class forName org.terraswarm.accessor.JSAccessor]]
    set accessorActor [java::cast org.terraswarm.accessor.JSAccessor [$entityList get 0]]
    #puts [$accessorActor getFullName]
    #puts [$e1 exportMoML]
    #set moml [$accessorActor exportMoML]
    #return $moml
}

set accessorCount 0

proc importAccessors {accessorDirectory} {
    global accessorCount
    set files [glob $accessorDirectory/*.xml]
    foreach file $files {
        incr accessorCount
        test JSAccessor-2.1.$accessorCount "test $file" {
            #puts $file
            importAccessor $file
            # Success is not throwing an exception
            list {}
        } {{}}
    }
}

importAccessors $PTII/org/terraswarm/accessor/test/auto/accessors

