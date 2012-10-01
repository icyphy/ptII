# Tests for the LibraryAttribute class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2002-2012 The Regents of the University of California.
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
set libraryMoML "<?xml version=\"1.0\" standalone=\"no\"?>
<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\"
    \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">
<entity name=\"N0\" class=\"ptolemy.kernel.CompositeEntity\">
<property name=\"_library\" class=\"ptolemy.moml.LibraryAttribute\">
  <configure>
    <entity name=\"state library\" class=\"ptolemy.kernel.CompositeEntity\">
        <property name=\"myAttribute\" class=\"ptolemy.kernel.util.Attribute\"/>
    </entity>
  </configure>
</property>
</entity>"


######################################################################
####
#

test LibraryAttribute-1.1 {parse some moml like annotation.ml, call getLibrary} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $libraryMoML]
    set libraryAttribute [java::cast ptolemy.moml.LibraryAttribute \
	    [$toplevel getAttribute "_library"]]
    set compositeEntity [$libraryAttribute getLibrary]
    list [$libraryAttribute exportMoML] \
	    [$compositeEntity exportMoML]
} {{<property name="_library" class="ptolemy.moml.LibraryAttribute">
    <configure>
    <entity name="state library" class="ptolemy.kernel.CompositeEntity"><property name="myAttribute" class="ptolemy.kernel.util.Attribute"></property></entity>
  </configure>
</property>
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="state library" class="ptolemy.kernel.CompositeEntity">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2">
    </property>
    <property name="myAttribute" class="ptolemy.kernel.util.Attribute">
    </property>
    <property name="_libraryMarker" class="ptolemy.kernel.util.SingletonAttribute">
    </property>
</entity>
}}

######################################################################
####
#

test LibraryAttribute-2.1 {No arg constructor} {
    set libraryAttribute [java::new ptolemy.moml.LibraryAttribute]
    set library [$libraryAttribute getLibrary]
    list [$libraryAttribute exportMoML] [java::isnull $library]
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE property PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<property name="" class="ptolemy.moml.LibraryAttribute">
</property>
} 1}


######################################################################
####
#

test LibraryAttribute-3.1 {Workspace constructor} {
    set w [java::new ptolemy.kernel.util.Workspace "ws"]
    set libraryAttribute [java::new ptolemy.moml.LibraryAttribute $w]
    set library [$libraryAttribute getLibrary]
    list [$libraryAttribute exportMoML] [java::isnull $library]
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE property PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<property name="" class="ptolemy.moml.LibraryAttribute">
</property>
} 1}


######################################################################
####
#

# badLibraryMoML has an AtomicEntity inside its configure instead of
# CompositeEntity
set badLibraryMoML "<?xml version=\"1.0\" standalone=\"no\"?>
<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\"
    \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">
<entity name=\"N0\" class=\"ptolemy.kernel.CompositeEntity\">
<property name=\"_library\" class=\"ptolemy.moml.LibraryAttribute\">
  <configure>
    <entity name=\"should be CompositeEntity\" class=\"ptolemy.kernel.ComponentEntity\">
        <property name=\"myAttribute\" class=\"ptolemy.kernel.util.Attribute\"/>
    </entity>
  </configure>
</property>
</entity>"

test LibraryAttribute-4.1 {call getLibrary on a LibraryAttribute that does not have a CompositeEntity inside its configure  } {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $badLibraryMoML]
    set libraryAttribute [java::cast ptolemy.moml.LibraryAttribute \
	    [$toplevel getAttribute "_library"]]
    catch {$libraryAttribute getLibrary} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Expected library to be in an instance of CompositeEntity, but it is: ptolemy.kernel.ComponentEntity
  in .N0._library}}

test LibraryAttribute-5.1 {setLibrary to null} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $libraryMoML]
    set libraryAttribute [java::cast ptolemy.moml.LibraryAttribute \
	    [$toplevel getAttribute "_library"]]
    set r1 [$libraryAttribute exportMoML]
    set r2 [[$libraryAttribute getLibrary] exportMoML]
    set r3 [java::isnull [$libraryAttribute getLibrary]]
    $libraryAttribute setLibrary [java::null]
    set r4 [java::isnull [$libraryAttribute getLibrary]]
    set r5 [$libraryAttribute exportMoML]
    # Even though the library is now null, libraryAttribute has the same MoML
    list $r3 $r4 [expr {$r1 == $r5}]
} {0 1 1}

test LibraryAttribute-5.2 {setLibrary} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $libraryMoML]
    set libraryAttribute [java::cast ptolemy.moml.LibraryAttribute \
	    [$toplevel getAttribute "_library"]]

    set r1 [[$libraryAttribute getLibrary] exportMoML]

    set workspace [$libraryAttribute workspace]
    set library [java::new ptolemy.kernel.CompositeEntity $workspace]
    $library setName myLibrary
    $libraryAttribute setLibrary $library
    set r2 [[$libraryAttribute getLibrary] exportMoML]
    list $r1 $r2
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="state library" class="ptolemy.kernel.CompositeEntity">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="5.1-alpha">
    </property>
    <property name="myAttribute" class="ptolemy.kernel.util.Attribute">
    </property>
    <property name="_libraryMarker" class="ptolemy.kernel.util.SingletonAttribute">
    </property>
</entity>
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="myLibrary" class="ptolemy.kernel.CompositeEntity">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="5.1-alpha">
    </property>
    <property name="_libraryMarker" class="ptolemy.kernel.util.SingletonAttribute">
    </property>
</entity>
}}

test LibraryAttribute-5.3 {setLibrary with a marker _libraryMarker} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $libraryMoML]
    set libraryAttribute [java::cast ptolemy.moml.LibraryAttribute \
	    [$toplevel getAttribute "_library"]]

    set r1 [[$libraryAttribute getLibrary] exportMoML]

    set workspace [$libraryAttribute workspace]
    set library [java::new ptolemy.kernel.CompositeEntity $workspace]
    $library setName myLibrary2
    set marker [java::new ptolemy.kernel.util.SingletonAttribute $library _libraryMarker]
    $libraryAttribute setLibrary $library
    set r2 [[$libraryAttribute getLibrary] exportMoML]
    list $r2
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="myLibrary2" class="ptolemy.kernel.CompositeEntity">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="5.1-alpha">
    </property>
    <property name="_libraryMarker" class="ptolemy.kernel.util.SingletonAttribute">
    </property>
</entity>
}}
