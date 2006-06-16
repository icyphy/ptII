# Tests for the UserActorLibrary
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2006 The Regents of the University of California.
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

# Tycho test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test UserActorLibrary-1.0 {} {
    # Set the user library
    set userLibraryName testUserActorLibrary_OK_2_DELETE
    java::field ptolemy.actor.gui.UserActorLibrary \
    	USER_LIBRARY_NAME $userLibraryName
    set libraryName "[java::call ptolemy.util.StringUtilities preferencesDirectory]${userLibraryName}.xml"
    file delete -force $libraryName
    if [file exists $libraryName] {
	error "$libraryName exists"
    } 
    set configurationURL [java::call ptolemy.util.FileUtilities nameToURL \
			      {$CLASSPATH/ptolemy/actor/gui/test/testConfiguration.xml} \
			      [java::null] \
			      [java::null]]
			  
    # If we run twice, this will throw a NameDuplicationException
    if {[catch {$configuration getName} errMsg]} {
	set configuration [java::call ptolemy.actor.gui.MoMLApplication readConfiguration $configurationURL]
    }
    java::call ptolemy.actor.gui.UserActorLibrary openUserLibrary \
	$configuration

    set parser [java::new ptolemy.moml.MoMLParser]
    set entity [$parser parseFile test.xml]

    java::call ptolemy.actor.gui.UserActorLibrary \
	saveComponentInLibrary \
	$configuration $entity


    # Save the library
    set libraryInstance [$configuration getEntity "actor library.$userLibraryName"]
    set libraryTableau [$configuration openModel $libraryInstance]
    set libraryEffigy [java::cast ptolemy.actor.gui.PtolemyEffigy \
    			   [$libraryTableau getContainer]]
    set file [$libraryEffigy getWritableFile]
    $libraryEffigy writeFile $file

    # Read in the library and check it
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser purgeModelRecord [$file toURL]]
    set readbackEntity [$parser {parse java.net.URL java.net.URL} \
			    [java::null] [$file toURL]]
    $readbackEntity exportMoML

} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE class PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<class name="testUserActorLibrary_OK_2_DELETE" extends="ptolemy.moml.EntityLibrary">
    <configure>
        <group>
            <entity name="test" class="ptolemy.actor.TypedCompositeActor">
                <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2">
                </property>
                <doc>This test has no placeable elements, but writes to standard out.</doc>
                <property name="step" class="ptolemy.data.expr.Parameter" value="1">
                </property>
                <property name="director" class="ptolemy.domains.sdf.kernel.SDFDirector">
                    <property name="iterations" class="ptolemy.data.expr.Parameter" value="3">
                    </property>
                    <property name="vectorizationFactor" class="ptolemy.data.expr.Parameter" value="1">
                    </property>
                </property>
                <entity name="ramp" class="ptolemy.actor.lib.Ramp">
                    <property name="firingCountLimit" class="ptolemy.data.expr.Parameter" value="0">
                    </property>
                    <property name="init" class="ptolemy.data.expr.Parameter" value="0">
                    </property>
                    <property name="step" class="ptolemy.actor.parameters.PortParameter" value="step">
                    </property>
                </entity>
                <entity name="rec" class="ptolemy.actor.lib.Recorder">
                    <property name="capacity" class="ptolemy.data.expr.Parameter" value="-1">
                    </property>
                </entity>
                <relation name="r1" class="ptolemy.actor.TypedIORelation">
                </relation>
                <link port="ramp.output" relation="r1"/>
                <link port="rec.input" relation="r1"/>
            </entity>
        </group>
    </configure>
</class>
}

