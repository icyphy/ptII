# Tests for the EntityLibrary class.
#
# @Author: Edward A. Lee, Contributor: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1997-2003 The Regents of the University of California.
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
test EntityLibrary-1.1 {Test EntityLibrary class with configure element} {
    set moml_1 "$header
<entity name=\"top\" class=\"ptolemy.kernel.CompositeEntity\">
</entity>
"
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml_1]
    $parser parse {
<entity name=".top">
    <entity name="lib" class="ptolemy.moml.EntityLibrary">
        <configure>
            <?moml
                <group>
                    <entity name="a" class="ptolemy.actor.AtomicActor"/>
                    <entity name="b" class="ptolemy.actor.AtomicActor"/>
                </group>
            ?>
        </configure>
    </entity>
</entity>
}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <entity name="lib" class="ptolemy.moml.EntityLibrary">
        <configure>
            <group>
                <entity name="a" class="ptolemy.actor.AtomicActor">
                </entity>
                <entity name="b" class="ptolemy.actor.AtomicActor">
                </entity>
            </group>
        </configure>
    </entity>
</entity>
}

set moml_1 "$header
<entity name=\"top\" class=\"ptolemy.moml.EntityLibrary\">
    <configure>
        <?moml
            <group>
                <entity name=\"a\" class=\"ptolemy.actor.AtomicActor\">
                </entity>
                <entity name=\"b\" class=\"ptolemy.actor.AtomicActor\">
                </entity>
            </group>
        ?>
    </configure>
</entity>
"

######################################################################
####
#
test EntityLibrary-1.2 {Test EntityLibrary at top level} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml_1]
    set entityLibrary [java::cast ptolemy.moml.EntityLibrary $toplevel]
    list [$toplevel exportMoML] "\n" \
	    [$entityLibrary -noconvert getSource] "\n" \
	    [$entityLibrary getText] "\n" \
	    [$entityLibrary numEntities]
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.moml.EntityLibrary">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2">
    </property>
    <configure>
        <group>
            <entity name="a" class="ptolemy.actor.AtomicActor">
            </entity>
            <entity name="b" class="ptolemy.actor.AtomicActor">
            </entity>
        </group>
    </configure>
</entity>
} {
} java0x0 {
} {<group>
    <entity name="a" class="ptolemy.actor.AtomicActor">
    </entity>
    <entity name="b" class="ptolemy.actor.AtomicActor">
    </entity>
</group>} {
} 2}


######################################################################
####
#
test EntityLibrary-1.3 {clone} {
    # Uses setup from 1.2 above

    # FIXME: we should try calling clone while populating a large library
    set clonedEntityLibrary [java::cast ptolemy.moml.EntityLibrary \
	    [$entityLibrary clone]] 
    list \
	    [$clonedEntityLibrary -noconvert getSource] "\n" \
	    [$clonedEntityLibrary getText] "\n" \
	    [$clonedEntityLibrary numEntities]
} {java0x0 {
} {<group>
    <entity name="a" class="ptolemy.actor.AtomicActor">
    </entity>
    <entity name="b" class="ptolemy.actor.AtomicActor">
    </entity>
</group>} {
} 2}

######################################################################
####
#
test EntityLibrary-1.4 {deepEntityList} {
    # Uses setup from 1.2 above
    # listToNames is defined in $PTII/util/testsuite/enums.tcl
    listToNames [$entityLibrary deepEntityList]
} {a b}

######################################################################
####
#
test EntityLibrary-1.5 {deepEntityList with nonexistant file in configure so populate throws and exception} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml_1]
    set entityLibrary [java::cast ptolemy.moml.EntityLibrary $toplevel]

    puts "This test will print an error message to stderr which can be ignored"

    $entityLibrary configure [java::null] "file:./EntityLibary.tcl" \
	    {EntityLibrary Test Configure}
    catch {[$entityLibrary deepEntityList]} errMsg
    # Under JDK1.4.1, we get a different errMsg
    # Solaris: The system cannot find the file specified
    # XP: No such file or directory
    # So much for write once, run everywhere.
    regsub {The system cannot find the file specified} $errMsg {No such file or directory} errMsg2
    # Backslashes too!
    regsub {\\} $errMsg2 {/} errMsg3
    list $errMsg3

} {{ptolemy.kernel.util.InvalidStateException: Failed to populate Library
  in .top
Because:
./EntityLibary.tcl (No such file or directory)}}

######################################################################
####
#
test EntityLibrary-2.1 {Constructor: EntityLibrary()} {
    set entityLibrary [java::new ptolemy.moml.EntityLibrary]
    list [$entityLibrary exportMoML] "\n" \
	    [$entityLibrary -noconvert getSource] "\n" \
	    [$entityLibrary getText] "\n" \
	    [$entityLibrary numEntities]
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="" class="ptolemy.moml.EntityLibrary">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2">
    </property>
    <configure>
        <group>
            <property name="_libraryMarker" class="ptolemy.kernel.util.Attribute">
            </property>
        </group>
    </configure>
</entity>
} {
} java0x0 {
} {<group>
</group>} {
} 0}


######################################################################
####
#
test EntityLibrary-2.2 {Constructor: EntityLibrary(workspace)} {
    set w [java::new ptolemy.kernel.util.Workspace W]
    set entityLibrary [java::new ptolemy.moml.EntityLibrary $w]
    list [$entityLibrary exportMoML] "\n" \
	    [$entityLibrary -noconvert getSource] "\n" \
	    [$entityLibrary getText] "\n" \
	    [$entityLibrary numEntities]
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="" class="ptolemy.moml.EntityLibrary">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2">
    </property>
    <configure>
        <group>
            <property name="_libraryMarker" class="ptolemy.kernel.util.Attribute">
            </property>
        </group>
    </configure>
</entity>
} {
} java0x0 {
} {<group>
</group>} {
} 0}
