# Tests for the EntityLibrary class.
#
# @Author: Edward A. Lee, Contributor: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1997-2002 The Regents of the University of California.
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
                <property name="_libraryMarker" class="ptolemy.kernel.util.Attribute">
                </property>
                <entity name="a" class="ptolemy.actor.AtomicActor">
                </entity>
                <entity name="b" class="ptolemy.actor.AtomicActor">
                </entity>
            </group>
        </configure>
    </entity>
</entity>
}

######################################################################
####
#
test EntityLibrary-1.2 {Test EntityLibrary at top level} {
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
    <property name="_createdBy" class="ptolemy.kernel.util.VersionAttribute" value="2.1-devel">
    </property>
    <configure>
        <group>
            <property name="_libraryMarker" class="ptolemy.kernel.util.Attribute">
            </property>
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
