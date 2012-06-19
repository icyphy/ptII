# Tests for a JVM Bug.
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2009 The Regents of the University of California.
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

if {[string compare test [info procs jdkCaptureErr]] == 1} then {
   source [file join $PTII util testsuite jdktools.tcl]
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


set header {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">}

set moml_1 "$header
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
    <doc>xxx</doc>
</entity>"
 
test  aJVMBug-1.0 {Test for a jvm bug} {
    # This test should be first.

    # This code throws a java.lang.ExceptionInInitializer because
    # of strangeness with TypeLattice where we call
    # _basicLattice.addNodeWeight(BaseType.ACTOR); 
    set result [java::call ptolemy.data.expr.UtilityFunctions parseMoML $moml_1]

    set parser [java::new ptolemy.moml.MoMLParser]

    set toplevel [$parser parseFile parseMoMLConst.xml]
    list [$toplevel exportMoML]
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="parseMoMLConst" class="ptolemy.actor.TypedCompositeActor">
    <property name="bidirectionalTypeInference" class="ptolemy.actor.parameters.SharedParameter" value="true">
    </property>
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="9.0.devel">
    </property>
    <entity name="Const" class="ptolemy.actor.lib.Const">
        <property name="bidirectionalTypeInference" class="ptolemy.actor.parameters.SharedParameter" value="true">
        </property>
        <property name="value" class="ptolemy.data.expr.Parameter" value="parseMoML(&quot;&lt;entity name=\&quot;foo\&quot; class=\&quot;ptolemy.actor.TypedCompositeActor\&quot;/&gt;&quot;)">
        </property>
    </entity>
</entity>
}}
