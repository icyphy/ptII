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
    $parser reset
    set toplevel [$parser parse $moml_2]
    $toplevel exportMoML
} $moml_2

#----------------------------------------------------------------------
set moml_2_1 "$header
<model name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
    <doc><?html <H1>HTML Markup</H1><I>italics</I>.?></doc>
</model>
"
test MoMLParser-1.2.1 {parse simple model with HTML markup in CDATA} {
    $parser reset
    set toplevel [$parser parse $moml_2_1]
    $toplevel exportMoML
} $moml_2_1

#----------------------------------------------------------------------
set moml_3 "$header
<class name=\"top\" extends=\"ptolemy.actor.TypedCompositeActor\">
    <doc>xxx</doc>
</class>
"
test MoMLParser-1.3 {parse simple class with doc only} {
    $parser reset
    set toplevel [$parser parse $moml_3]
    $toplevel exportMoML
} $moml_3

#----------------------------------------------------------------------
set moml_3_1 "$header
<class name=\"top\" extends=\"ptolemy.actor.TypedCompositeActor\">
    <doc>xxx</doc>
    <doc name=\"foo\">yyy</doc>
</class>
"
test MoMLParser-1.3.1 {parse simple class with two doc tags} {
    $parser reset
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
    $parser reset
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
    $parser reset
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
    $parser reset
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
    $parser reset
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
    $parser reset
    catch {$parser parse $moml_3_6]} msg
    string range $msg 0 29
} {com.microstar.xml.XmlException}

#----------------------------------------------------------------------
set moml_4 {    <class name="top" extends="ptolemy.actor.TypedCompositeActor">
        <doc>xxx</doc>
    </class>
}
test MoMLParser-1.4 {produce class without header} {
    $parser reset
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
    $parser reset
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
    $parser reset
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
    $parser reset
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
    $parser reset
    catch {set toplevel [$parser parse $moml]} msg
    string range $msg 0 29
} {com.microstar.xml.XmlException}

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
    $parser reset
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
    $parser reset
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
    $parser reset
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
    $parser reset
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
    <import base="../../.." source="ptolemy/moml/test/testClass2.xml"/>
    <entity name="c" class=".a"/>
</model>
}

set moml "$header $body"

test MoMLParser-1.12.1 {test instantiation of a class} {
    $parser reset
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
    <!-- Note that here, we are searching using the classpath -->
    <import source="ptolemy/moml/test/testClass.xml"/>
    <entity name="b" class=".a"/>
    <import source="testClass2.xml"/>
    <entity name="c" class=".a"/>
</model>
}

set moml "$header $body"

test MoMLParser-1.12.2 {test import with a relative source } {
    $parser reset
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
    <!-- Note that here, we are searching relative to the cwd -->
    <import source="testClass.xml"/>
    <entity name="b" class=".a"/>
    <import source="testClass2.xml"/>
    <entity name="c" class=".a"/>
</model>
}

set moml "$header $body"

test MoMLParser-1.12.3 {test import with a relative source } {
    $parser reset
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
    $parser reset
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
    $parser reset
    catch {set toplevel [$parser parse $moml]} msg
    string range $msg 0 29
} {com.microstar.xml.XmlException}

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
    $parser reset
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
    $parser reset
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
    $parser reset
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
    $parser reset
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
    $parser reset
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
    $parser reset
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
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} $moml


#----------------------------------------------------------------------
set moml "$header
<class name=\"top\" extends=\"ptolemy.actor.TypedCompositeActor\">
    <entity name=\"Sender\" class=\"ptolemy.actor.TypedCompositeActor\">
        <entity name=\"Connect\" class=\"ptolemy.actor.TypedCompositeActor\">
            <port name=\"expired\" class=\"ptolemy.actor.TypedIOPort\">
                <property name=\"input\"/>
            </port>
            <entity name=\"Init\" class=\"ptolemy.actor.TypedAtomicActor\">
                <port name=\"Outgoing\" class=\"ptolemy.actor.TypedIOPort\">
                </port>
            </entity>
        </entity>
    </entity>
</class>
"
test MoMLParser-1.23 {Simulate a problem we found with FSM, where pure properties cause problems } {
    # The bug was that the following xml would throw an exception
    # because of problems with handling 'pure properties' like
    #   <property name="input"
    #
    #<?xml version="1.0" standalone="no"?>
    #<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    #    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
    #<model name="topLevel" class="ptolemy.actor.TypedCompositeActor">
    #    <director name="DEDirector" class="ptolemy.domains.de.kernel.DEDirector">
    #    </director>
    #    <entity name="Sender" class="ptolemy.actor.TypedCompositeActor">
    #        <entity name="Connect" class="ptolemy.domains.fsm.demo.ABP.DEFSMActor">
    #            <port name="expired" class="ptolemy.actor.TypedIOPort">
    #                <property name="input"/>
    #            </port>
    #            <entity name="Init" class="ptolemy.domains.fsm.kernel.FSMState">
    #                <port name="Outgoing" class="ptolemy.kernel.ComponentPort">
    #                </port>
    #            </entity>
    #        </entity>
    #     </entity>
    #</model>

    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} $moml

#----------------------------------------------------------------------
test MoMLParser-2.1 {Test incremental parsing: add entity} {
    set incMomlBase "$header
<model name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
</model>
"

# NOTE: Here is the incremental MoML
    set incMoml_1 "<entity name=\".top\">
<entity name=\"inside\" class=\"ptolemy.actor.TypedCompositeActor\"/>
</entity>
"
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $incMomlBase]
    set toplevel [$parser parse $incMoml_1]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="inside" class="ptolemy.actor.TypedCompositeActor">
    </entity>
</model>
}

#----------------------------------------------------------------------
test MoMLParser-2.2 {Test incremental parsing: add entity deeper} {
    set incMoml_2 "<entity name=\".top.inside\">
<property name=\"prop\" class=\"ptolemy.data.expr.Parameter\"/>
</entity>
"
    set toplevel [$parser parse $incMoml_2]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="inside" class="ptolemy.actor.TypedCompositeActor">
        <property name="prop" class="ptolemy.data.expr.Parameter">
        </property>
    </entity>
</model>
}

#----------------------------------------------------------------------
test MoMLParser-2.3 {Test incremental parsing: add port} {
    set incMoml_2_3 "<entity name=\".top.inside\">
<port name=\"input\" class=\"ptolemy.actor.TypedIOPort\"/>
</entity>
"
    set toplevel [$parser parse $incMoml_2_3]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="inside" class="ptolemy.actor.TypedCompositeActor">
        <property name="prop" class="ptolemy.data.expr.Parameter">
        </property>
        <port name="input" class="ptolemy.actor.TypedIOPort">
        </port>
    </entity>
</model>
}

#----------------------------------------------------------------------
test MoMLParser-2.4 {Test incremental parsing: add another port, relation, and link} {
    set incMoml_2_4 "<entity name=\".top\">
    <entity name=\"a\" class=\"ptolemy.actor.TypedCompositeActor\">
        <port name=\"output\" class=\"ptolemy.actor.TypedIOPort\"/>
    </entity>
    <relation name=\"r\" class=\"ptolemy.actor.TypedIORelation\"/>
    <link relation=\"r\" port=\"a.output\"/>
    <link relation=\"r\" port=\"inside.input\"/>
</entity>
"
    set toplevel [$parser parse $incMoml_2_4]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="inside" class="ptolemy.actor.TypedCompositeActor">
        <property name="prop" class="ptolemy.data.expr.Parameter">
        </property>
        <port name="input" class="ptolemy.actor.TypedIOPort">
        </port>
    </entity>
    <entity name="a" class="ptolemy.actor.TypedCompositeActor">
        <port name="output" class="ptolemy.actor.TypedIOPort">
        </port>
    </entity>
    <relation name="r" class="ptolemy.actor.TypedIORelation">
    </relation>
    <link port="inside.input" relation="r"/>
    <link port="a.output" relation="r"/>
</model>
}

#----------------------------------------------------------------------
test MoMLParser-2.5 {Test incremental parsing: remove an entity} {
    set incMoml_2_5 "<entity name=\".top\">
    <deleteEntity name=\"a\"/>
</entity>
"
    set toplevel [$parser parse $incMoml_2_5]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="inside" class="ptolemy.actor.TypedCompositeActor">
        <property name="prop" class="ptolemy.data.expr.Parameter">
        </property>
        <port name="input" class="ptolemy.actor.TypedIOPort">
        </port>
    </entity>
    <relation name="r" class="ptolemy.actor.TypedIORelation">
    </relation>
    <link port="inside.input" relation="r"/>
</model>
}

#----------------------------------------------------------------------
test MoMLParser-2.5.1 {Test incremental parsing: remove a link} {
    set incMoml_2_5_1 {<entity name=".top">
    <unlink port="inside.input" relation="r"/>
</entity>}
    set toplevel [$parser parse $incMoml_2_5_1]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="inside" class="ptolemy.actor.TypedCompositeActor">
        <property name="prop" class="ptolemy.data.expr.Parameter">
        </property>
        <port name="input" class="ptolemy.actor.TypedIOPort">
        </port>
    </entity>
    <relation name="r" class="ptolemy.actor.TypedIORelation">
    </relation>
</model>
}

#----------------------------------------------------------------------
test MoMLParser-2.6 {Test incremental parsing: remove nonexistent entity} {
    set incMoml_2_6 "<entity name=\".top\">
    <deleteEntity name=\"a\"/>
</entity>
"
    catch {$parser parse $incMoml_2_6} msg
    string range $msg 0 52
} {com.microstar.xml.XmlException: No such entity to del}

#----------------------------------------------------------------------
test MoMLParser-2.7 {Test incremental parsing: remove a relation} {
    set incMoml_2_7 "<entity name=\".top\">
    <deleteRelation name=\"r\"/>
</entity>
"
    # Test using a new parser.
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser setToplevel $toplevel
    $parser parse $incMoml_2_7
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="inside" class="ptolemy.actor.TypedCompositeActor">
        <property name="prop" class="ptolemy.data.expr.Parameter">
        </property>
        <port name="input" class="ptolemy.actor.TypedIOPort">
        </port>
    </entity>
</model>
}

#----------------------------------------------------------------------
test MoMLParser-2.8 {Test incremental parsing: remove a port} {
    set incMoml_2_8 {<entity name=".top">
    <deletePort name="inside.input"/>
</entity>}
    # Test using a new parser.
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser setToplevel $toplevel
    $parser parse $incMoml_2_8
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="inside" class="ptolemy.actor.TypedCompositeActor">
        <property name="prop" class="ptolemy.data.expr.Parameter">
        </property>
    </entity>
</model>
}

#----------------------------------------------------------------------
test MoMLParser-2.9 {Test link with insertAt attribute, inside links} {
    $parser reset
    set toplevel [$parser parse {
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <port name="p" class="ptolemy.kernel.ComponentPort"/>
    <entity name="a" class="ptolemy.kernel.CompositeEntity">
        <port name="p" class="ptolemy.kernel.ComponentPort"/>
    </entity>
    <relation name="r" class="ptolemy.kernel.ComponentRelation"/>
    <link port="p" relation="r" insertAt="1"/>
</model>
}]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <port name="p" class="ptolemy.kernel.ComponentPort">
    </port>
    <entity name="a" class="ptolemy.kernel.CompositeEntity">
        <port name="p" class="ptolemy.kernel.ComponentPort">
        </port>
    </entity>
    <relation name="r" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <link port="p" insertAt="1" relation="r"/>
</model>
}

#----------------------------------------------------------------------
test MoMLParser-2.10 {Test link with insertAt attribute, inside links} {
   $parser parse {
<model name="top">
   <link port="p" relation="r" insertAt="0"/>
</model>
}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <port name="p" class="ptolemy.kernel.ComponentPort">
    </port>
    <entity name="a" class="ptolemy.kernel.CompositeEntity">
        <port name="p" class="ptolemy.kernel.ComponentPort">
        </port>
    </entity>
    <relation name="r" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <link port="p" relation="r"/>
    <link port="p" insertAt="2" relation="r"/>
</model>
}

#----------------------------------------------------------------------
test MoMLParser-2.11 {Test link with insertAt attribute} {
   $parser parse {
<model name=".top">
   <link port="a.p" relation="r" insertAt="0"/>
</model>
}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <port name="p" class="ptolemy.kernel.ComponentPort">
    </port>
    <entity name="a" class="ptolemy.kernel.CompositeEntity">
        <port name="p" class="ptolemy.kernel.ComponentPort">
        </port>
    </entity>
    <relation name="r" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <link port="p" relation="r"/>
    <link port="p" insertAt="2" relation="r"/>
    <link port="a.p" relation="r"/>
</model>
}

#----------------------------------------------------------------------
test MoMLParser-2.12 {Test link with insertAt attribute} {
   $parser parse {
<model name=".top">
   <link port="a.p" insertAt="2" relation="r"/>
</model>
}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <port name="p" class="ptolemy.kernel.ComponentPort">
    </port>
    <entity name="a" class="ptolemy.kernel.CompositeEntity">
        <port name="p" class="ptolemy.kernel.ComponentPort">
        </port>
    </entity>
    <relation name="r" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <link port="p" relation="r"/>
    <link port="p" insertAt="2" relation="r"/>
    <link port="a.p" relation="r"/>
    <link port="a.p" insertAt="2" relation="r"/>
</model>
}

#----------------------------------------------------------------------
test MoMLParser-2.13 {Test unlink with index} {
   $parser parse {
<model name=".top">
   <unlink port="a.p" index="0"/>
</model>
}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <port name="p" class="ptolemy.kernel.ComponentPort">
    </port>
    <entity name="a" class="ptolemy.kernel.CompositeEntity">
        <port name="p" class="ptolemy.kernel.ComponentPort">
        </port>
    </entity>
    <relation name="r" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <link port="p" relation="r"/>
    <link port="p" insertAt="2" relation="r"/>
    <link port="a.p" insertAt="1" relation="r"/>
</model>
}

#----------------------------------------------------------------------
test MoMLParser-2.14 {Test unlink with index} {
   $parser parse {
<model name=".top">
   <unlink port="a.p" index="1"/>
</model>
}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <port name="p" class="ptolemy.kernel.ComponentPort">
    </port>
    <entity name="a" class="ptolemy.kernel.CompositeEntity">
        <port name="p" class="ptolemy.kernel.ComponentPort">
        </port>
    </entity>
    <relation name="r" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <link port="p" relation="r"/>
    <link port="p" insertAt="2" relation="r"/>
</model>
}

#----------------------------------------------------------------------
test MoMLParser-2.15 {Test unlink with index} {
   $parser parse {
<model name=".top">
   <unlink port="p" insideIndex="0"/>
</model>
}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <port name="p" class="ptolemy.kernel.ComponentPort">
    </port>
    <entity name="a" class="ptolemy.kernel.CompositeEntity">
        <port name="p" class="ptolemy.kernel.ComponentPort">
        </port>
    </entity>
    <relation name="r" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <link port="p" insertAt="1" relation="r"/>
</model>
}

#----------------------------------------------------------------------
test MoMLParser-2.16 {Test unlink with index} {
   $parser parse {
<model name=".top">
   <unlink port="p" insideIndex="0"/>
</model>
}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <port name="p" class="ptolemy.kernel.ComponentPort">
    </port>
    <entity name="a" class="ptolemy.kernel.CompositeEntity">
        <port name="p" class="ptolemy.kernel.ComponentPort">
        </port>
    </entity>
    <relation name="r" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <link port="p" relation="r"/>
</model>
}

#----------------------------------------------------------------------
test MoMLParser-2.17 {Test unlink with index} {
   $parser parse {
<model name=".top">
   <unlink port="p" insideIndex="1"/>
</model>
}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <port name="p" class="ptolemy.kernel.ComponentPort">
    </port>
    <entity name="a" class="ptolemy.kernel.CompositeEntity">
        <port name="p" class="ptolemy.kernel.ComponentPort">
        </port>
    </entity>
    <relation name="r" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <link port="p" relation="r"/>
</model>
}

#----------------------------------------------------------------------
test MoMLParser-2.18 {Test unlink with index} {
   $parser parse {
<model name=".top">
   <unlink port="p" insideIndex="0"/>
</model>
}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <port name="p" class="ptolemy.kernel.ComponentPort">
    </port>
    <entity name="a" class="ptolemy.kernel.CompositeEntity">
        <port name="p" class="ptolemy.kernel.ComponentPort">
        </port>
    </entity>
    <relation name="r" class="ptolemy.kernel.ComponentRelation">
    </relation>
</model>
}

#----------------------------------------------------------------------
test MoMLParser-3.1 {Test invalid containment} {
    set incMoml_3_1 "$header
<model name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
  <entity name=\"a\" class=\"ptolemy.actor.TypedCompositeActor\"/>
  <entity name=\"b\" class=\"ptolemy.actor.TypedCompositeActor\">
     <entity name=\".top.a\">
        <entity name=\"c\" class=\"ptolemy.actor.TypedCompositeActor\"/>
     </entity>
  </entity>
</model>
"
    set parser [java::new ptolemy.moml.MoMLParser]
    catch {$parser parse $incMoml_3_1} msg
    string range $msg 0 51
} {com.microstar.xml.XmlException: Reference to an exis}

#----------------------------------------------------------------------
test MoMLParser-3.2 {Test invalid containment} {
    set incMoml_3_2 "$header
<model name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
  <entity name=\"a\" class=\"ptolemy.actor.TypedCompositeActor\">
     <entity name=\"b\" class=\"ptolemy.actor.TypedCompositeActor\">
        <entity name=\".top.a\"/>
     </entity>
  </entity>
</model>
"
    $parser reset
    catch {$parser parse $incMoml_3_2} msg
    string range $msg 0 51
} {com.microstar.xml.XmlException: Reference to an exis}

#----------------------------------------------------------------------
test MoMLParser-3.3 {Test invalid containment} {
    set incMoml_3_3 "$header
<model name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
  <entity name=\"a\" class=\"ptolemy.actor.TypedCompositeActor\">
     <entity name=\"b\" class=\"ptolemy.actor.TypedCompositeActor\">
        <entity name=\".top\"/>
     </entity>
  </entity>
</model>
"
    $parser reset
    catch {$parser parse $incMoml_3_3} msg
    string range $msg 0 51
} {com.microstar.xml.XmlException: Reference to an exis}

#----------------------------------------------------------------------
test MoMLParser-3.4 {Test invalid containment} {
    set incMoml_3_4 "$header
<model name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
  <entity name=\"a\" class=\"ptolemy.actor.TypedCompositeActor\">
     <entity name=\"b\" class=\"ptolemy.actor.TypedCompositeActor\">
        <entity name=\".top\"/>
     </entity>
  </entity>
  <entity name=\"a.b\"/>
</model>
"
    $parser reset
    catch {$parser parse $incMoml_3_4} msg
    string range $msg 0 51
} {com.microstar.xml.XmlException: Reference to an exis}

#----------------------------------------------------------------------
test MoMLParser-4.1 {Test doc element addition} {
    set incMomlBase "$header
<model name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
</model>
"
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $incMomlBase]
    $parser parse {<model name=".top"><doc name="foo">xxx</doc></model>}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.actor.TypedCompositeActor">
    <doc name="foo">xxx</doc>
</model>
}

#----------------------------------------------------------------------
test MoMLParser-4.2 {Test doc element modifications} {
    $parser parse {<model name=".top"><doc name="foo">yyy</doc></model>}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.actor.TypedCompositeActor">
    <doc name="foo">yyy</doc>
</model>
}

#----------------------------------------------------------------------
test MoMLParser-4.3 {Test doc element removal} {
    $parser parse {<model name=".top"><doc name="foo"/></model>}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.actor.TypedCompositeActor">
</model>
}

#----------------------------------------------------------------------
test MoMLParser-4.4 {Test doc element removal with default name} {
    $parser parse {<model name=".top"><doc>zzz</doc></model>}
    $parser parse {<model name=".top"><doc></doc></model>}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.actor.TypedCompositeActor">
</model>
}

#----------------------------------------------------------------------
test MoMLParser-5.1 {Test property deletion} {
    set incMomlBase "$header
<model name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
   <property name=\"foo\"/>
</model>
"
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $incMomlBase]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="foo" class="ptolemy.kernel.util.Attribute">
    </property>
</model>
}

#----------------------------------------------------------------------
test MoMLParser-5.2 {Test property deletion} {
    $parser parse {<model name=".top"><deleteProperty name="foo"/></model>}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.actor.TypedCompositeActor">
</model>
}

#----------------------------------------------------------------------
test MoMLParser-6.1 {Test indexed I/O with actor model.} {
    set incMomlBase "$header
<model name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
</model>
"
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.actor.TypedCompositeActor \
            [$parser parse $incMomlBase]]
    $parser parse {
<model name=".top">
<director name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
    <property name="iterations" value="1"/>
</director>
<entity name="source" class="ptolemy.actor.lib.Ramp"/>
<entity name="dist" class="ptolemy.actor.lib.Distributor"/>
<entity name="comm" class="ptolemy.actor.lib.Commutator"/>
<entity name="sink" class="ptolemy.actor.lib.Recorder"/>
<relation name="r1" class="ptolemy.actor.TypedIORelation"/>
<relation name="r2" class="ptolemy.actor.TypedIORelation"/>
<relation name="r3" class="ptolemy.actor.TypedIORelation"/>
<link port="source.output" relation="r1"/>
<link port="dist.input" relation="r1"/>
<link port="dist.output" relation="r2"/>
<link port="comm.input" relation="r2"/>
<link port="comm.output" relation="r3"/>
<link port="sink.input" relation="r3"/>
</model>
}
    set manager [java::new ptolemy.actor.Manager \
            [$toplevel workspace] "manager"]
    $toplevel setManager $manager
    $manager execute
    set recorder [java::cast ptolemy.actor.lib.Recorder \
            [$toplevel getEntity "sink"]]
    enumToTokenValues [$recorder getRecord 0]
} {0}

#----------------------------------------------------------------------
test MoMLParser-6.1 {Test indexed I/O with actor model.} {
    set incMomlBase "$header
<model name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
</model>
"
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.actor.TypedCompositeActor \
            [$parser parse $incMomlBase]]
    $parser parse {
<model name=".top">
<director name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
    <property name="iterations" value="1"/>
</director>
<entity name="source" class="ptolemy.actor.lib.Ramp"/>
<entity name="dist" class="ptolemy.actor.lib.Distributor"/>
<entity name="comm" class="ptolemy.actor.lib.Commutator"/>
<entity name="sink" class="ptolemy.actor.lib.Recorder"/>
<relation name="r1" class="ptolemy.actor.TypedIORelation"/>
<relation name="r2" class="ptolemy.actor.TypedIORelation"/>
<relation name="r3" class="ptolemy.actor.TypedIORelation"/>
<link port="source.output" relation="r1"/>
<link port="dist.input" relation="r1"/>
<link port="dist.output" relation="r2"/>
<link port="comm.input" relation="r2"/>
<link port="comm.output" relation="r3"/>
<link port="sink.input" relation="r3"/>
</model>
}
    set manager [java::new ptolemy.actor.Manager \
            [$toplevel workspace] "manager"]
    $toplevel setManager $manager
    $manager execute
    set recorder [java::cast ptolemy.actor.lib.Recorder \
            [$toplevel getEntity "sink"]]
    enumToTokenValues [$recorder getRecord 0]
} {0}

#----------------------------------------------------------------------
test MoMLParser-6.2 {Straight with blocksize 2.} {
    $parser parse {
<model name=".top">
<relation name="r4" class="ptolemy.actor.TypedIORelation"/>
<link port="dist.output" relation="r4"/>
<link port="comm.input" relation="r4"/>
</model>
}
#     set dir [java::cast ptolemy.domains.sdf.kernel.SDFDirector \
#             [$toplevel getDirector]]
#     set sch [java::cast ptolemy.domains.sdf.kernel.SDFScheduler \
#             [$dir getScheduler]]
#     $sch addDebugListener [java::new ptolemy.kernel.util.StreamListener]
    $manager execute
    enumToTokenValues [$recorder getRecord 0]
} {0 1}

#----------------------------------------------------------------------
test MoMLParser-6.3 {Reverse with blocksize 2.} {
    $parser parse {
<model name=".top">
<unlink port="dist.output" relation="r4"/>
<unlink port="comm.input" index="1"/>
<link port="dist.output" relation="r4"/>
<link port="comm.input" relation="r4" insertAt="0"/>
</model>
}
    $manager execute
    enumToTokenValues [$recorder getRecord 0]
} {1 0}

#----------------------------------------------------------------------
test MoMLParser-6.4 {Reverse with blocksize 3.} {
    $parser parse {
<model name=".top">
<relation name="r5" class="ptolemy.actor.TypedIORelation"/>
<link port="dist.output" relation="r5" insertAt="0"/>
<link port="comm.input" relation="r5"/>
</model>
}
    $manager execute
    enumToTokenValues [$recorder getRecord 0]
} {2 1 0}

#----------------------------------------------------------------------
test MoMLParser-6.5 {Reverse with blocksize 4 and gaps.} {
    $parser parse {
<model name=".top">
<relation name="r6" class="ptolemy.actor.TypedIORelation"/>
<link port="dist.output" relation="r6" insertAt="10"/>
<link port="comm.input" relation="r6" insertAt="0"/>
</model>
}
    $manager execute
    enumToTokenValues [$recorder getRecord 0]
} {3 2 1 0}

#----------------------------------------------------------------------
test MoMLParser-6.6 {Delete the gaps, having no effect.} {
    $parser parse {
<model name=".top">
<unlink port="dist.output" index="8"/>
</model>
}
    $manager execute
    enumToTokenValues [$recorder getRecord 0]
} {3 2 1 0}

#----------------------------------------------------------------------
test MoMLParser-7.1 {Test setContext()} {
    set incMomlBase "$header
<model name=\"top\" class=\"ptolemy.kernel.CompositeEntity\">
</model>
"
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.kernel.CompositeEntity \
            [$parser parse $incMomlBase]]
    $parser parse {
<model name=".top">
   <entity name="a" class="ptolemy.kernel.CompositeEntity"/>
</model>
}
    $parser setContext [$toplevel getEntity "a"]
    $parser parse {<entity name="b" class="ptolemy.kernel.CompositeEntity"/>}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <entity name="a" class="ptolemy.kernel.CompositeEntity">
        <entity name="b" class="ptolemy.kernel.CompositeEntity">
        </entity>
    </entity>
</model>
}

#----------------------------------------------------------------------
set body {
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <!-- Note that here, we are searching using the classpath -->
    <input source="ptolemy/moml/test/testClass2.xml"/>
</model>
}

set moml "$header $body"

test MoMLParser-8.1 {test input with a relative source } {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="a" extends="ptolemy.kernel.CompositeEntity">
        <property name="x" class="ptolemy.kernel.util.Attribute">
        </property>
    </class>
</model>
}

#----------------------------------------------------------------------
set body {
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <!-- Note that here, we are searching relative to the cwd -->
    <input source="testClass2.xml"/>
</model>
}

set moml "$header $body"

test MoMLParser-8.2 {test input with a relative source } {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="a" extends="ptolemy.kernel.CompositeEntity">
        <property name="x" class="ptolemy.kernel.util.Attribute">
        </property>
    </class>
</model>
}

