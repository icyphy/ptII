# Test importing of Accessors
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2015-2016 The Regents of the University of California.
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
    set accessorFile [java::call ptolemy.util.FileUtilities nameToFile {$CLASSPATH/org/terraswarm/accessor/accessors/web/test/TestAccessor.js} [java::null]]
    set urlSpec [$accessorFile getCanonicalPath]

    # This call to accessorToMoML will checkout or update the accessor repo and run JSDoc
    set changeRequestText [java::call org.terraswarm.accessor.JSAccessor accessorToMoML $urlSpec]

    java::call org.terraswarm.accessor.JSAccessor handleAccessorMoMLChangeRequest $e1 $urlSpec $e1 $changeRequestText 100 100

    set accessor [$e1 getEntity {TestAccessor}]
    set moml [$accessor exportMoML]
    
    regsub {value=".*/org/terraswarm/accessor/accessors/web/test/TestAccessor.js"} $moml {value="$CLASSPATH/org/terraswarm/accessor/accessors/test/TestAccessor.js"} moml2

    # Deal with backslashes in MS-DOS-based systems.  Why we need to do this in this day and age is beyond me.
    regsub {value=".*\\org\\terraswarm\\accessor\\accessors\\test\\TestAccessor.js"} $moml2 {value="$CLASSPATH/org/terraswarm/accessor/accessors/test/TestAccessor.js"} moml3

    regsub {<property name="version" class="ptolemy.kernel.util.StringAttribute" value="\$\$Id$moml3 {<property name="version" class="ptolemy.kernel.util.StringAttribute" value="$$Id$$">} moml4
    list $moml4
} {{<entity name="TestAccessor" class="org.terraswarm.accessor.JSAccessor">
    <property name="script" class="ptolemy.actor.parameters.PortParameter" value="// Test accessor with various input and output types and handlers.&#10;//&#10;// Copyright (c) 2015-2016 The Regents of the University of California.&#10;// All rights reserved.&#10;//&#10;// Permission is hereby granted, without written agreement and without&#10;// license or royalty fees, to use, copy, modify, and distribute this&#10;// software and its documentation for any purpose, provided that the above&#10;// copyright notice and the following two paragraphs appear in all copies&#10;// of this software.&#10;//&#10;// IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY&#10;// FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES&#10;// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF&#10;// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF&#10;// SUCH DAMAGE.&#10;//&#10;// THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,&#10;// INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF&#10;// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE&#10;// PROVIDED HEREUNDER IS ON AN &quot;AS IS&quot; BASIS, AND THE UNIVERSITY OF&#10;// CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,&#10;// ENHANCEMENTS, OR MODIFICATIONS.&#10;//&#10;&#10;/** Test accessor with various input and output types and handlers.&#10; *  This accessor is designed to be instantiable on any host, including&#10; *  the common host, which does not implement the require() function&#10; *  nor provide any mechanism for loading accessors.&#10; *&#10; *  @accessor test/TestAccessor&#10; *  @parameter p A parameter with default value 42.&#10; *  @input untyped An untyped input that will accept any JavaScript object.&#10; *  @input numeric A numeric input.&#10; *  @input boolean A boolean input.&#10; *  @output typeOfUntyped Produces the type (a string) of the input named 'untyped'.&#10; *  @output jsonOfUntyped Produces a JSON representation of the input named 'untyped',&#10; *   created using JSON.toString().&#10; *  @output numericPlusP Produces the value of the 'numeric' input plus 'p'.&#10; *  @output negation Produces the negation of the 'boolean' input.&#10; *  @author Edward A. Lee&#10; *  @version $$Id$$&#10; */&#10;&#10;// Stop extra messages from jslint.  Note that there should be no&#10;// space between the / and the * and global.&#10;/*globals console, error, exports, require */&#10;/*jshint globalstrict: true*/&#10;&quot;use strict&quot;;&#10;&#10;exports.setup = function () {&#10;    this.input('untyped'); // Untyped input.&#10;    this.input('numeric', {&#10;        'type': 'number',&#10;        'value': 0&#10;    }); // Numeric input.&#10;    this.input('boolean', {&#10;        'type': 'boolean'&#10;    }); // Boolean input.&#10;    this.output('typeOfUntyped', {&#10;        'type': 'string'&#10;    }); // Type of untyped input.&#10;    this.output('jsonOfUntyped', {&#10;        'type': 'string'&#10;    }); // JSON of untyped input.&#10;    this.output('numericPlusP', {&#10;        'type': 'number'&#10;    }); // Numeric input plus p.&#10;    this.output('negation', {&#10;        'type': 'boolean'&#10;    }); // Negation of boolean input.&#10;    this.parameter('p', {&#10;        'value': 42&#10;    }); // Untyped, with numeric value.&#10;};&#10;&#10;// Base class variable that is visible to subclasses through inheritance.&#10;exports.variable = 'hello';&#10;&#10;exports.initialize = function () {&#10;    // Respond to any input by updating them all.&#10;    this.addInputHandler('untyped', function () {&#10;        this.send('typeOfUntyped', typeof this.get('untyped'));&#10;        // Refer to the function using 'this.exports' rather than 'exports'&#10;        // to allow an override. Note that we choose here to invoke formatOutput&#10;        // with 'this' bound to 'this.exports'.&#10;        this.send('jsonOfUntyped', this.exports.formatOutput(this.get('untyped')));&#10;    });&#10;    this.addInputHandler('numeric', function () {&#10;        this.send('numericPlusP', this.get('numeric') + this.getParameter('p'));&#10;    });&#10;    this.addInputHandler('boolean', function () {&#10;        this.send('negation', !this.get('boolean'));&#10;    });&#10;};&#10;&#10;/** Define a function that can be overridden in subclasses. */&#10;exports.formatOutput = function (value) {&#10;    return 'JSON for untyped input: ' + JSON.stringify(value);&#10;};&#10;&#10;exports.fire = function () {&#10;    console.log('TestAccessor.fire() invoked.');&#10;};&#10;">
        <property name="style" class="ptolemy.actor.gui.style.NoteStyle">
            <property name="note" class="ptolemy.kernel.util.StringAttribute" value="NOTE: To see the script, invoke Open Actor">
            </property>
        </property>
    </property>
    <property name="accessorSource" class="org.terraswarm.accessor.JSAccessor$ActionableAttribute" value="$CLASSPATH/org/terraswarm/accessor/accessors/test/TestAccessor.js">
    </property>
    <property name="documentation" class="ptolemy.vergil.basic.DocAttribute">
        <property name="description" class="ptolemy.kernel.util.StringAttribute" value="&lt;p&gt;Test accessor with various input and output types and handlers.&#10; This accessor is designed to be instantiable on any host, including&#10; the common host, which does not implement the require() function&#10; nor provide any mechanism for loading accessors.&lt;/p&gt;">
        </property>
        <property name="author" class="ptolemy.kernel.util.StringAttribute" value="Edward A. Lee">
        </property>
        <property name="version" class="ptolemy.kernel.util.StringAttribute" value="$$Id$$">
        </property>
        <property name="untyped (port)" class="ptolemy.kernel.util.StringAttribute" value="An untyped input that will accept any JavaScript object.">
        </property>
        <property name="numeric (port)" class="ptolemy.kernel.util.StringAttribute" value="A numeric input.">
        </property>
        <property name="boolean (port)" class="ptolemy.kernel.util.StringAttribute" value="A boolean input.">
        </property>
        <property name="typeOfUntyped (port)" class="ptolemy.kernel.util.StringAttribute" value="Produces the type (a string) of the input named 'untyped'.">
        </property>
        <property name="jsonOfUntyped (port)" class="ptolemy.kernel.util.StringAttribute" value="Produces a JSON representation of the input named 'untyped',&#10;  created using JSON.toString().">
        </property>
        <property name="numericPlusP (port)" class="ptolemy.kernel.util.StringAttribute" value="Produces the value of the 'numeric' input plus 'p'.">
        </property>
        <property name="negation (port)" class="ptolemy.kernel.util.StringAttribute" value="Produces the negation of the 'boolean' input.">
        </property>
        <property name="p (parameter)" class="ptolemy.kernel.util.StringAttribute" value="A parameter with default value 42.">
        </property>
    </property>
    <property name="_tableauFactory" class="ptolemy.vergil.toolbox.TextEditorTableauFactory">
        <property name="attributeName" class="ptolemy.kernel.util.StringAttribute" value="script">
        </property>
        <property name="syntaxStyle" class="ptolemy.kernel.util.StringAttribute" value="text/javascript">
        </property>
    </property>
    <property name="_location" class="ptolemy.kernel.util.Location" value="{100.0, 100.0}">
    </property>
    <port name="untyped" class="ptolemy.actor.TypedIOPort">
        <property name="input"/>
        <property name="defaultValue" class="ptolemy.data.expr.Parameter">
        </property>
    </port>
    <port name="numeric" class="ptolemy.actor.parameters.ParameterPort">
        <property name="input"/>
        <property name="defaultValue" class="ptolemy.data.expr.Parameter">
        </property>
    </port>
    <port name="boolean" class="ptolemy.actor.TypedIOPort">
        <property name="input"/>
        <property name="defaultValue" class="ptolemy.data.expr.Parameter">
        </property>
    </port>
    <port name="typeOfUntyped" class="ptolemy.actor.TypedIOPort">
        <property name="output"/>
        <property name="defaultValue" class="ptolemy.data.expr.Parameter">
        </property>
    </port>
    <port name="jsonOfUntyped" class="ptolemy.actor.TypedIOPort">
        <property name="output"/>
        <property name="defaultValue" class="ptolemy.data.expr.Parameter">
        </property>
    </port>
    <port name="numericPlusP" class="ptolemy.actor.TypedIOPort">
        <property name="output"/>
        <property name="defaultValue" class="ptolemy.data.expr.Parameter">
        </property>
    </port>
    <port name="negation" class="ptolemy.actor.TypedIOPort">
        <property name="output"/>
        <property name="defaultValue" class="ptolemy.data.expr.Parameter">
        </property>
    </port>
</entity>
}}

# This is similar to ptolemy/actor/lib/fmi/test/FMUImport.tcl
# @param obeyCheckoutOrUpdateRepositoryParameter If true, then use the value
# of the <i>checkoutOrUpdateRepository</i> parameter.  If false,
# then override the value of the
# <i>checkoutOrUpdateRepository</i> parameter and do not
# checkout or update the repository or invoke JSDoc.  During
#  testing, this parameter is set to false after the first reload
#  of an accessor so as to improve the performance of the tests.
proc importAccessor {accessorFileName obeyCheckoutOrUpdateRepositoryParameter} {
    set e1 [sdfModel 5]
    set accessorFile [java::call ptolemy.util.FileUtilities nameToFile $accessorFileName [java::null]]

    set urlSpec [$accessorFile getCanonicalPath]

    # If the second argument is false, then we don't check out the repo or run JSDoc.
    set changeRequestText [java::call org.terraswarm.accessor.JSAccessor accessorToMoML $urlSpec $obeyCheckoutOrUpdateRepositoryParameter]

    java::call org.terraswarm.accessor.JSAccessor handleAccessorMoMLChangeRequest $e1 $urlSpec $e1 $changeRequestText 100 100

    set accessorActorFileName [lindex [file split $accessorFileName] end]
    set accessorActorName [string range $accessorActorFileName 0 [expr {[string length $accessorActorFileName] -5}]]
    #puts "accessorActorFileName: $accessorActorFileName"
    #puts "accessorActorName: $accessorActorName"
    set entityList [$e1 entityList [java::call Class forName org.terraswarm.accessor.JSAccessor]]
    for {set i 0} {$i < [$entityList size]} {incr i} {
        set accessor [java::cast org.terraswarm.accessor.JSAccessor [$entityList get $i]]
        $accessor reload $obeyCheckoutOrUpdateRepositoryParameter
    }
    #puts [$accessorActor getFullName]
    #puts [$e1 exportMoML]
    #set moml [$accessorActor exportMoML]
    #return $moml
}

set accessorCount 0

# No need to checkout or update the accessors repo again or run JSDoc.
set obeyCheckoutOrUpdateRepositoryParameter 0

proc importAccessors {accessorDirectory} {
    global accessorCount
    global obeyCheckoutOrUpdateRepositoryParameter
    set files [glob $accessorDirectory/*.xml]
    # Download the accessor repo and run JSDoc once.
    foreach file $files {
        incr accessorCount
        test JSAccessor-2.1.$accessorCount "test $file" {
            #puts $file
            importAccessor $file $obeyCheckoutOrUpdateRepositoryParameter
            # Success is not throwing an exception
            list {}
        } {{}}
    }
}

importAccessors $PTII/org/terraswarm/accessor/test/auto/accessors

