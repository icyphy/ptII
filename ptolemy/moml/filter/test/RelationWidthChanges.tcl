# Tests for the BackwardCompatibility class
#
# @Author: Bert Rodiers
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

# Increase timeout from the default set in $PTII/util/testsuite/testDefs.tcl
# set timeOutSeconds 6000

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
    $parser reset
    java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
    java::call ptolemy.moml.MoMLParser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]



######################################################################
####
#

test RelationWidhtChanges-1.0 {Version 7.0, width not set} {
    set expressionMoml  "$header 
      <entity name=\"model\" class=\"ptolemy.actor.TypedCompositeActor\">
        <property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"7.0\">
        </property>
        <relation name=\"relation\" class=\"ptolemy.actor.TypedIORelation\">
        </relation>
      </entity>"
    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $expressionMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="model" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="9.0.devel">
    </property>
    <relation name="relation" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
</entity>
}}


######################################################################
####
#

test RelationWidhtChanges-2.0 {Version 8.0, width not set} {
    set expressionMoml  "$header 
      <entity name=\"model\" class=\"ptolemy.actor.TypedCompositeActor\">
        <property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"8.0\">
        </property>
        <relation name=\"relation\" class=\"ptolemy.actor.TypedIORelation\">
        </relation>
      </entity>"
    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $expressionMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="model" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="9.0.devel">
    </property>
    <relation name="relation" class="ptolemy.actor.TypedIORelation">
    </relation>
</entity>
}}


######################################################################
####
#

test RelationWidhtChanges-3.0 {Version 7.0, width set to 0} {
    set expressionMoml  "$header 
      <entity name=\"model\" class=\"ptolemy.actor.TypedCompositeActor\">
        <property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"7.0\">
        </property>
        <relation name=\"relation\" class=\"ptolemy.actor.TypedIORelation\">
          <property name=\"width\" class=\"ptolemy.data.expr.Parameter\" value=\"0\">
          </property>        
        </relation>
      </entity>"
    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $expressionMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="model" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="9.0.devel">
    </property>
    <relation name="relation" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="-1">
        </property>
    </relation>
</entity>
}}


######################################################################
####
#

test RelationWidhtChanges-4.0 {Version 8.0, width set to 0} {
    set expressionMoml  "$header 
      <entity name=\"model\" class=\"ptolemy.actor.TypedCompositeActor\">
        <property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"8.0\">
        </property>
        <relation name=\"relation\" class=\"ptolemy.actor.TypedIORelation\">
          <property name=\"width\" class=\"ptolemy.data.expr.Parameter\" value=\"0\">
          </property>        
        </relation>
      </entity>"
    set parser [java::new ptolemy.moml.MoMLParser]
    # Note that 1.1 added the filter for all the parsers
    set toplevel [$parser parse $expressionMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="model" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="9.0.devel">
    </property>
    <relation name="relation" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="0">
        </property>
    </relation>
</entity>
}}


################################################################################
# Paste
set topModel {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" value="2"/>
    </property>
</entity>
}

test RelationWidthChanges5.1 { test paste that wrongly sets the width to 1. See http://bugzilla.ecoinformatics.org/show_bug.cgi?id=4804} {
    $parser resetAll
    set toplevel5_1 [java::cast ptolemy.actor.TypedCompositeActor \
            [$parser parse $topModel]]
    set manager [java::new ptolemy.actor.Manager [$toplevel5_1 workspace] "w"]
    $toplevel5_1 setManager $manager

    # A change that pastes to composites with a relation.
    # The bug is that the width should not be set to 1
    set changeMoML5_1 {
<group>
  <entity name="CompositeActor" class="ptolemy.actor.TypedCompositeActor">
    <port name="port" class="ptolemy.actor.TypedIOPort">
        <property name="output"/>
        <property name="multiport"/>
        <property name="width" class="ptolemy.data.expr.Parameter" value="-1">
        </property>
    </port>
  </entity>
  <entity name="CompositeActor2" class="ptolemy.actor.TypedCompositeActor">
    <port name="port" class="ptolemy.actor.TypedIOPort">
        <property name="input"/>
        <property name="multiport"/>
        <property name="width" class="ptolemy.data.expr.Parameter" value="-1">
        </property>
    </port>
  </entity>
  <relation name="relation" class="ptolemy.actor.TypedIORelation">
  </relation>
  <link port="CompositeActor.port" relation="relation"/>
  <link port="CompositeActor2.port" relation="relation"/>
</group>
    }

    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel5_1 $toplevel5_1 $changeMoML5_1]

    $manager requestChange $change
    list [$toplevel5_1 exportMoML]
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="9.0.devel">
    </property>
    <property name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="2">
        </property>
    </property>
    <entity name="CompositeActor" class="ptolemy.actor.TypedCompositeActor">
        <port name="port" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
            <property name="multiport"/>
            <property name="width" class="ptolemy.data.expr.Parameter" value="-1">
            </property>
        </port>
    </entity>
    <entity name="CompositeActor2" class="ptolemy.actor.TypedCompositeActor">
        <port name="port" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="multiport"/>
            <property name="width" class="ptolemy.data.expr.Parameter" value="-1">
            </property>
        </port>
    </entity>
    <relation name="relation" class="ptolemy.actor.TypedIORelation">
    </relation>
    <link port="CompositeActor.port" relation="relation"/>
    <link port="CompositeActor2.port" relation="relation"/>
</entity>
}}


# The list of filters is static, so we reset it
java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
