# Tests for the MoMLParser class
#
# @Author: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 1997-2000 The Regents of the University of California.
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
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">}

#----------------------------------------------------------------------
test MoMLParser-1.1 {parse tolerated incorrect MoML} {
    set moml_1 "$header
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
    <doc>xxx</doc>
</entity>
"
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml_1]
    $toplevel getFullName
} {.top}

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
set moml_3_1 "$header
<class name=\"top\" extends=\"ptolemy.actor.TypedCompositeActor\">
    <doc>xxx</doc>
    <doc>yyy</doc>
</class>
"
test MoMLParser-1.3.1 {parse simple class with two doc tags} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml_3_1]
    $toplevel exportMoML
} $moml_3_1

#----------------------------------------------------------------------
set moml_3_2 "$header
<class name=\"top\" extends=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"xxx\"/>
</class>
"
test MoMLParser-1.3.2 {parse class with a property with no class} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml_3_2]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<class name="top" extends="ptolemy.actor.TypedCompositeActor">
    <property name="xxx" class="ptolemy.kernel.util.Attribute">
    </property>
</class>
}

#----------------------------------------------------------------------
set moml_3_3 "$header
<model name=\"lib\" class=\"ptolemy.actor.TypedCompositeActor\">
<class name=\"top\" extends=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"xxx\"/>
</class>
<class name=\"top\" extends=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"yyy\"/>
</class>
<entity name=\"test\" class=\"top\"/>
</model>
"
test MoMLParser-1.3.3 {check overriding class definition} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.actor.TypedCompositeActor \
            [$parser parse $moml_3_3]]
    set test [$toplevel getEntity test]
    $test exportMoML
} {<entity name="test" class=".lib.top">
    <property name="xxx" class="ptolemy.kernel.util.Attribute">
    </property>
    <property name="yyy" class="ptolemy.kernel.util.Attribute">
    </property>
</entity>
}

#----------------------------------------------------------------------
set moml_3_4 "$header
<model name=\"lib\" class=\"ptolemy.actor.TypedCompositeActor\">
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"xxx\"/>
</entity>
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"yyy\"/>
</entity>
</model>
"
test MoMLParser-1.3.4 {check overriding class definition} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.actor.TypedCompositeActor \
            [$parser parse $moml_3_4]]
    set test [$toplevel getEntity top]
    $test exportMoML
} {<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="xxx" class="ptolemy.kernel.util.Attribute">
    </property>
    <property name="yyy" class="ptolemy.kernel.util.Attribute">
    </property>
</entity>
}

#----------------------------------------------------------------------
set moml_3_5 "$header
<model name=\"lib\" class=\"ptolemy.actor.TypedCompositeActor\">
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"xxx\"/>
</entity>
<entity name=\".lib.top\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"yyy\"/>
</entity>
</model>
"
test MoMLParser-1.3.5 {check multiple reference with absolute name} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.actor.TypedCompositeActor \
            [$parser parse $moml_3_5]]
    set test [$toplevel getEntity top]
    $test exportMoML
} {<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="xxx" class="ptolemy.kernel.util.Attribute">
    </property>
    <property name="yyy" class="ptolemy.kernel.util.Attribute">
    </property>
</entity>
}

#----------------------------------------------------------------------
set moml_3_6 "$header
<model name=\"lib\" class=\"ptolemy.actor.TypedCompositeActor\">
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"xxx\"/>
</entity>
<entity name=\"another\" class=\"ptolemy.actor.TypedCompositeActor\">
<entity name=\".lib.top\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"yyy\"/>
</entity>
</entity>
</model>
"
test MoMLParser-1.3.6 {check multiple reference with absolute name} {
    set parser [java::new ptolemy.moml.MoMLParser]
    catch {$parser parse $moml_3_6]} msg
    list $msg
} {{com.microstar.xml.XmlException: Sorry: Ptolemy II does not support multiple containment.  Attempt to place .lib.top into .lib.another}}

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
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<class name="top" extends="ptolemy.actor.TypedCompositeActor">
    <entity name="a" class="ptolemy.actor.lib.Ramp">
        <property name="firingCountLimit" class="ptolemy.data.expr.Parameter" value="0">
        </property>
        <property name="init" class="ptolemy.data.expr.Parameter" value="0">
        </property>
        <property name="step" class="ptolemy.data.expr.Parameter" value="1">
        </property>
        <port name="output" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
        <port name="trigger" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="multiport"/>
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
test MoMLParser-1.8 {test with a pre-existing port given, with wrong class} {
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
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<class name="top" extends="ptolemy.actor.TypedCompositeActor">
    <entity name="a" class="ptolemy.actor.lib.Ramp">
        <property name="firingCountLimit" class="ptolemy.data.expr.Parameter" value="0">
        </property>
        <property name="init" class="ptolemy.data.expr.Parameter" value="1">
        </property>
        <property name="step" class="ptolemy.data.expr.Parameter" value="1">
        </property>
        <port name="output" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
        <port name="trigger" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="multiport"/>
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

#----------------------------------------------------------------------
set moml {
<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="foo" class="ptolemy.actor.TypedCompositeActor">
   <port name="C" class="ptolemy.actor.TypedIOPort"></port>
   <relation name="R2" class="ptolemy.actor.TypedIORelation"></relation>
   <entity name="C1" class="ptolemy.actor.TypedCompositeActor">
       <port name="A" class="ptolemy.actor.TypedIOPort"></port>
       <relation name="R1" class="ptolemy.actor.TypedIORelation"></relation>
       <entity name="C2" class="ptolemy.actor.TypedCompositeActor">
           <port name="B" class="ptolemy.actor.TypedIOPort"></port>
       </entity>
   </entity>
   <link port="C" relation="R2"/>
   <link port="C1.A" relation="R2"/>
   <link port="C1.A" relation="C1.R1"/>
   <link port="C1.C2.B" relation="C1.R1"/>
</model>
}
test MoMLParser-1.10 {test with hierarchy} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="foo" class="ptolemy.actor.TypedCompositeActor">
    <port name="C" class="ptolemy.actor.TypedIOPort">
    </port>
    <entity name="C1" class="ptolemy.actor.TypedCompositeActor">
        <port name="A" class="ptolemy.actor.TypedIOPort">
        </port>
        <entity name="C2" class="ptolemy.actor.TypedCompositeActor">
            <port name="B" class="ptolemy.actor.TypedIOPort">
            </port>
        </entity>
        <relation name="R1" class="ptolemy.actor.TypedIORelation">
        </relation>
        <link port="A" relation="R1"/>
        <link port="C2.B" relation="R1"/>
    </entity>
    <relation name="R2" class="ptolemy.actor.TypedIORelation">
    </relation>
    <link port="C" relation="R2"/>
    <link port="C1.A" relation="R2"/>
</model>
}

#----------------------------------------------------------------------
set body {
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="a" extends="ptolemy.kernel.CompositeEntity">
        <property name="p" class="ptolemy.data.expr.Parameter">
        </property>
        <port name="port" class="ptolemy.kernel.ComponentPort">
        </port>
        <entity name="c" class="ptolemy.kernel.ComponentEntity">
        </entity>
    </class>
    <entity name="b" class="a">
    </entity>
</model>
}

set moml "$header $body"

test MoMLParser-1.11 {test instantiation of a class} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="a" extends="ptolemy.kernel.CompositeEntity">
        <property name="p" class="ptolemy.data.expr.Parameter">
        </property>
        <port name="port" class="ptolemy.kernel.ComponentPort">
        </port>
        <entity name="c" class="ptolemy.kernel.ComponentEntity">
        </entity>
    </class>
    <entity name="b" class=".top.a">
        <property name="p" class="ptolemy.data.expr.Parameter">
        </property>
    </entity>
</model>
}

#----------------------------------------------------------------------
set body {
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <import base="../../.." source="ptolemy/moml/test/testClass.xml"/>
    <entity name="b" class=".a"/>
</model>
}

set moml "$header $body"

# NOTE: The port and contained entity are not exported because they
# are presumably defined in the imported file.  Note further that we
# cannot practically ask for a top-level exportMoML here because the
# exported information will include an import state where the file location
# depends on the type of system the test is run on.
test MoMLParser-1.12 {test instantiation of a class} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.kernel.CompositeEntity \
            [$parser parse $moml]]
    set b [$toplevel getEntity b]
    $b exportMoML
} {<entity name="b" class=".a">
    <property name="prop" class="ptolemy.data.expr.Parameter">
    </property>
</entity>
}

#----------------------------------------------------------------------
set body {
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <import base="../../.." source="ptolemy/moml/test/testClass.xml"/>
    <entity name="b" class=".a"/>
    <import source="testClass2.xml"/>
    <entity name="c" class=".a"/>
</model>
}

set moml "$header $body"

test MoMLParser-1.12.1 {test instantiation of a class} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.kernel.CompositeEntity \
            [$parser parse $moml]]
    set b [$toplevel getEntity b]
    set c [$toplevel getEntity c]
    list [$b exportMoML] [$c exportMoML]
} {{<entity name="b" class=".a">
    <property name="prop" class="ptolemy.data.expr.Parameter">
    </property>
</entity>
} {<entity name="c" class=".a">
    <property name="x" class="ptolemy.kernel.util.Attribute">
    </property>
</entity>
}}

#----------------------------------------------------------------------
set body {
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="master" extends="ptolemy.kernel.ComponentEntity">
        <port name="p" class="ptolemy.kernel.ComponentPort">
        </port>
    </class>
    <class name="derived" extends="master">
        <port name="q" class="ptolemy.kernel.ComponentPort">
        </port>
    </class>
</model>
}

set moml "$header $body"

test MoMLParser-1.13 {test extension of a class} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="master" extends="ptolemy.kernel.ComponentEntity">
        <port name="p" class="ptolemy.kernel.ComponentPort">
        </port>
    </class>
    <class name="derived" extends="ptolemy.kernel.ComponentEntity">
        <port name="p" class="ptolemy.kernel.ComponentPort">
        </port>
        <port name="q" class="ptolemy.kernel.ComponentPort">
        </port>
    </class>
</model>
}

#----------------------------------------------------------------------
set body {
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <entity name="master" class="ptolemy.kernel.ComponentEntity">
    </entity>
    <entity name="derived" class="master">
    </entity>
</model>
}

set moml "$header $body"

test MoMLParser-1.14 {test that instantiation of an entity fails} {
    set parser [java::new ptolemy.moml.MoMLParser]
    catch {set toplevel [$parser parse $moml]} msg
    list $msg
} {{com.microstar.xml.XmlException: Attempt to extend an entity that is not a class: .top.master}}

#----------------------------------------------------------------------
set body {
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="master" extends="ptolemy.kernel.CompositeEntity">
        <entity name="e1" class="ptolemy.kernel.ComponentEntity">
        </entity>
    </class>
    <class name="derived" extends="master">
        <entity name="e2" class="ptolemy.kernel.ComponentEntity">
        </entity>
    </class>
</model>
}

set moml "$header $body"

test MoMLParser-1.15 {test extension of a composite class} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="master" extends="ptolemy.kernel.CompositeEntity">
        <entity name="e1" class="ptolemy.kernel.ComponentEntity">
        </entity>
    </class>
    <class name="derived" extends="ptolemy.kernel.CompositeEntity">
        <entity name="e1" class="ptolemy.kernel.ComponentEntity">
        </entity>
        <entity name="e2" class="ptolemy.kernel.ComponentEntity">
        </entity>
    </class>
</model>
}

#----------------------------------------------------------------------
set body {
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="master" extends="ptolemy.kernel.CompositeEntity">
        <entity name="e1" class="ptolemy.kernel.ComponentEntity">
        </entity>
    </class>
    <class name="derived" extends="master">
        <entity name="e2" class="ptolemy.kernel.ComponentEntity">
        </entity>
    </class>
</model>
}

set moml "$header $body"

test MoMLParser-1.15 {test extension of a composite class} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="master" extends="ptolemy.kernel.CompositeEntity">
        <entity name="e1" class="ptolemy.kernel.ComponentEntity">
        </entity>
    </class>
    <class name="derived" extends="ptolemy.kernel.CompositeEntity">
        <entity name="e1" class="ptolemy.kernel.ComponentEntity">
        </entity>
        <entity name="e2" class="ptolemy.kernel.ComponentEntity">
        </entity>
    </class>
</model>
}

#----------------------------------------------------------------------
set body {
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="master" extends="ptolemy.kernel.CompositeEntity">
        <entity name="e1" class="ptolemy.kernel.ComponentEntity">
        </entity>
    </class>
    <entity name="derived" class="master">
        <entity name="e2" class="ptolemy.kernel.ComponentEntity">
        </entity>
    </entity>
</model>
}

set moml "$header $body"

test MoMLParser-1.16 {test extension of a composite class} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="master" extends="ptolemy.kernel.CompositeEntity">
        <entity name="e1" class="ptolemy.kernel.ComponentEntity">
        </entity>
    </class>
    <entity name="derived" class="ptolemy.kernel.CompositeEntity">
        <entity name="e1" class="ptolemy.kernel.ComponentEntity">
        </entity>
        <entity name="e2" class="ptolemy.kernel.ComponentEntity">
        </entity>
    </entity>
</model>
}

#----------------------------------------------------------------------
set body {
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="master" extends="ptolemy.kernel.CompositeEntity">
        <entity name="e1" class="ptolemy.kernel.ComponentEntity">
        </entity>
    </class>
    <class name="derived" extends="master">
    </class>
</model>
}

set moml "$header $body"

test MoMLParser-1.17 {test extension of a composite class} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="master" extends="ptolemy.kernel.CompositeEntity">
        <entity name="e1" class="ptolemy.kernel.ComponentEntity">
        </entity>
    </class>
    <class name="derived" extends=".top.master">
    </class>
</model>
}

#----------------------------------------------------------------------
set body {
<model name="top" class="ptolemy.actor.CompositeActor">
    <director name="dir" class="ptolemy.actor.Director"/>
</model>
}

set moml "$header $body"

test MoMLParser-1.18 {test director persistence} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.actor.CompositeActor">
    <director name="dir" class="ptolemy.actor.Director">
    </director>
</model>
}

#----------------------------------------------------------------------
set moml "$header
<model name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"a\" class=\"ptolemy.data.expr.Parameter\"
         value=\"&quot;x&quot;\">
    </property>
</model>
"
set result {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="a" class="ptolemy.data.expr.Parameter" value="&quot;x&quot;">
    </property>
</model>
}
test MoMLParser-1.19 {test quoted parameter values} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} $result

test MoMLParser-1.20 {test quote resolution} {
    set prop [java::cast ptolemy.data.expr.Parameter \
            [$toplevel getAttribute a]]
    $prop stringRepresentation
} {"x"}

test MoMLParser-1.21 {test quote resolution in reverse} {
    $prop setExpression {foo + "y"}
    $prop exportMoML
} {<property name="a" class="ptolemy.data.expr.Parameter" value="foo + &quot;y&quot;">
</property>
}

#----------------------------------------------------------------------
set moml "$header
<class name=\"top\" extends=\"ptolemy.actor.CompositeActor\">
    <entity name=\"a\" class=\"ptolemy.actor.AtomicActor\">
        <port name=\"p\" class=\"ptolemy.actor.IOPort\">
            <property name=\"multiport\"/>
        </port>
    </entity>
</class>
"
test MoMLParser-1.22 {test with an actor} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} $moml
