# Test DesignPatternImporter
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2010-2011 The Regents of the University of California.
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
#                       PT_COPYRIGHT_VERSION_2
#                       COPYRIGHTENDKEY
#######################################################################

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Directory in which the patterns may be found
set p ../patterns/

######################################################################
####
#
test DesignPatternImporter-1.0 {Persistence Problem} {
    # Problem reported by Atul Gulati.
    # The zip file contains a model called Top.xml that uses a
    # DesignPatternImporter.
    # If you right click on the Engineer Icon and select "Class Actions" ->
    # "Create Instance"
    # then an instance is created.
    # If I do Save As, then the resulting page does not have the instance.
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser resetAll
    set toplevel [java::cast ptolemy.actor.TypedCompositeActor [$parser parseFile DesignPatternImporterPersistenceBug.xml]]
    set engineer [$toplevel getEntity Engineer]
    set foo [$engineer instantiate [$engineer getContainer] foo]
    set moml [$toplevel exportMoML]
    $toplevel setContainer [java::null]
    list $moml
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="DesignPatternImporterPersistenceBug" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="8.1.devel">
    </property>
    <property name="DesignPatternImporter" class="ptolemy.actor.gt.controller.DesignPatternImporter">
        <property name="designPatternFile" class="ptolemy.data.expr.FileParameter" value="DesignPatternImporterPersistenceBugPattern.xml">
        </property>
        <property name="_icon" class="ptolemy.vergil.icon.ValueIcon">
            <property name="_color" class="ptolemy.actor.gui.ColorAttribute" value="{0.0, 0.0, 1.0, 1.0}">
            </property>
        </property>
        <property name="_smallIconDescription" class="ptolemy.kernel.util.SingletonConfigurableAttribute">
            <configure>
        <svg>
          <text style="font-size:14; font-family:SansSerif; fill:blue">-D-</text>
        </svg>
      </configure>
        </property>
        <property name="_location" class="ptolemy.kernel.util.Location" value="[40.0, 130.0]">
        </property>
    </property>
    <property name="_windowProperties" class="ptolemy.actor.gui.WindowPropertiesAttribute" value="{bounds={198, 146, 964, 687}, maximized=false}">
    </property>
    <property name="_vergilSize" class="ptolemy.actor.gui.SizeAttribute" value="[749, 580]">
    </property>
    <property name="_vergilZoomFactor" class="ptolemy.data.expr.ExpertParameter" value="1.8691424075531">
    </property>
    <property name="_vergilCenter" class="ptolemy.data.expr.ExpertParameter" value="{201.5839439655172, 121.5625}">
    </property>
    <property name="SDF Director" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="_location" class="ptolemy.kernel.util.Location" value="[70.0, 5.0]">
        </property>
    </property>
    <entity name="foo" class=".DesignPatternImporterPersistenceBug.Engineer">
    </entity>
</entity>
}}
