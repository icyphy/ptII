# Tests for the MoMLParser class
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997-1999 The Regents of the University of California.
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

# If a file contains non-graphical tests, then it should be named .tcl
# If a file contains graphical tests, then it should be called .itcl
#
# It would be nice if the tests would work in a vanilla itkwish binary.
# Check for necessary classes and adjust the auto_path accordingly.
#

######################################################################
####
#

set header {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/archive/moml.dtd">}

#----------------------------------------------------------------------
test MoMLParser-1.1 {parse incorrect MoML} {
    set moml_1 "$header
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
    <doc>xxx</doc>
</entity>
"
    set parser [java::new ptolemy.moml.MoMLParser]
    catch {$parser parse $moml_1} msg
    list $msg
} {{com.microstar.xml.XmlException: Element "entity" found inside an element that is not a CompositeEntity. It is: null}}

#----------------------------------------------------------------------
set moml_2 "$header
<model name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
    <doc>xxx</doc>
</model>
"
test MoMLParser-1.2 {parse simple model with doc only} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml_2]
    $toplevel exportMoML
} $moml_2

#----------------------------------------------------------------------
set moml_3 "$header
<class name=\"top\" extends=\"ptolemy.actor.TypedCompositeActor\">
    <doc>xxx</doc>
</class>
"
test MoMLParser-1.3 {parse simple class with doc only} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml_3]
    $toplevel exportMoML
} $moml_3

#----------------------------------------------------------------------
set moml_4 {    <class name="top" extends="ptolemy.actor.TypedCompositeActor">
        <doc>xxx</doc>
    </class>
}
test MoMLParser-1.4 {produce class without header} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml_4]
    set output [java::new java.io.StringWriter]
    $toplevel exportMoML $output 1
    $output toString
} $moml_4

#----------------------------------------------------------------------
set moml "$header
<class name=\"top\" extends=\"ptolemy.actor.TypedCompositeActor\">
    <entity name=\"a\" class=\"ptolemy.actor.lib.Ramp\">
    </entity>
</class>
"
set result {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/archive/moml.dtd">
<class name="top" extends="ptolemy.actor.TypedCompositeActor">
    <entity name="a" class="ptolemy.actor.lib.Ramp">
        <property name="firingCountLimit" class="ptolemy.data.expr.Parameter" value="0">
        </property>
        <property name="init" class="ptolemy.data.expr.Parameter" value="0">
        </property>
        <property name="step" class="ptolemy.data.expr.Parameter" value="1">
        </property>
        <port name="output" class="ptolemy.actor.TypedIOPort">
        </port>
        <port name="trigger" class="ptolemy.actor.TypedIOPort">
        </port>
    </entity>
</class>
}
# NOTE: result is not the same as what is parsed...
test MoMLParser-1.5 {test with an actor} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} $result

#----------------------------------------------------------------------
set moml "$header
<class name=\"top\" extends=\"ptolemy.actor.TypedCompositeActor\">
    <entity name=\"a\" class=\"ptolemy.actor.lib.Ramp\">
        <property name=\"init\" value=\"0\" class=\"ptolemy.data.expr.Parameter\">
        </property>
    </entity>
</class>
"
# NOTE: result is not the same as what is parsed...
test MoMLParser-1.6 {test with a pre-existing parameter given, with class} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} $result

#----------------------------------------------------------------------
set moml "$header
<class name=\"top\" extends=\"ptolemy.actor.TypedCompositeActor\">
    <entity name=\"a\" class=\"ptolemy.actor.lib.Ramp\">
        <port name=\"output\">
        </port>
    </entity>
</class>
"
# NOTE: result is not the same as what is parsed...
test MoMLParser-1.7 {test with a pre-existing port given, without class} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} $result

#----------------------------------------------------------------------
set moml "$header
<class name=\"top\" extends=\"ptolemy.actor.TypedCompositeActor\">
    <entity name=\"a\" class=\"ptolemy.actor.lib.Ramp\">
        <port name=\"output\" class=\"ptolemy.actor.lib.Ramp\">
        </port>
    </entity>
</class>
"
# NOTE: result is not the same as what is parsed...
test MoMLParser-1.7 {test with a pre-existing port given, with wrong class} {
    set parser [java::new ptolemy.moml.MoMLParser]
    catch {set toplevel [$parser parse $moml]} msg
    list $msg
} {{com.microstar.xml.XmlException: port named "output" exists and is not an instance of ptolemy.actor.lib.Ramp}}

#----------------------------------------------------------------------
set moml "$header
<class name=\"top\" extends=\"ptolemy.actor.TypedCompositeActor\">
    <entity name=\"a\" class=\"ptolemy.actor.lib.Ramp\">
        <property name=\"init\" value=\"1\">
        </property>
    </entity>
</class>
"
set result {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/archive/moml.dtd">
<class name="top" extends="ptolemy.actor.TypedCompositeActor">
    <entity name="a" class="ptolemy.actor.lib.Ramp">
        <property name="firingCountLimit" class="ptolemy.data.expr.Parameter" value="0">
        </property>
        <property name="init" class="ptolemy.data.expr.Parameter" value="1">
        </property>
        <property name="step" class="ptolemy.data.expr.Parameter" value="1">
        </property>
        <port name="output" class="ptolemy.actor.TypedIOPort">
        </port>
        <port name="trigger" class="ptolemy.actor.TypedIOPort">
        </port>
    </entity>
</class>
}
# NOTE: result is not the same as what is parsed...
test MoMLParser-1.9 {test with changed parameter value from default} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} $result

