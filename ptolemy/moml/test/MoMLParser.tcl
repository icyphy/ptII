# Tests for the MoMLParser class
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
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">}

set classheader {<?xml version="1.0" standalone="no"?>
<!DOCTYPE class PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">}

######################################################################
####
#
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

######################################################################
####
#
set moml_2 "$header
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
    <doc>xxx</doc>
</entity>
"
test MoMLParser-1.2 {parse simple model with doc only} {
    $parser reset
    set toplevel [$parser parse $moml_2]
    $toplevel exportMoML
} $moml_2

######################################################################
####
#
set moml_2_1 "$header
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
    <doc><?html <H1>HTML Markup</H1><I>italics</I>.?></doc>
</entity>
"
test MoMLParser-1.2.1 {parse simple model with HTML markup in processing instruction} {
    $parser reset
    set toplevel [$parser parse $moml_2_1]
    $toplevel exportMoML
} $moml_2_1

######################################################################
####
#
set moml_2_1 "$header
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
    <doc><H1>HTML Markup</H1><I>italics</I>.</doc>
</entity>
"
test MoMLParser-1.2.2 {parse simple model with HTML markup} {
    $parser reset
    set toplevel [$parser parse $moml_2_1]
    $toplevel exportMoML
} $moml_2_1

######################################################################
####
#
# NOTE: If no name is given to the inside doc element, then it will
# be assigned the MoML default name "_doc".
set moml_2_1 "$header
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
    <doc><H1>HTML <doc name=\"foo\">Markup</doc></H1><I>italics</I>.</doc>
</entity>
"
test MoMLParser-1.2.3 {parse simple model with nested doc tag} {
    $parser reset
    set toplevel [$parser parse $moml_2_1]
    $toplevel exportMoML
} $moml_2_1


######################################################################
####
#
set moml_3 "$classheader
<class name=\"top\" extends=\"ptolemy.actor.TypedCompositeActor\">
    <doc>xxx</doc>
</class>
"
test MoMLParser-1.3 {parse simple class with doc only} {
    $parser reset
    set toplevel [$parser parse $moml_3]
    $toplevel exportMoML
} $moml_3

######################################################################
####
#
set moml_3_1 "$classheader
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

######################################################################
####
#
set moml_3_2 "$classheader
<class name=\"top\" extends=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"xxx\"/>
</class>
"
test MoMLParser-1.3.2 {parse class with a property with no class} {
    $parser reset
    set toplevel [$parser parse $moml_3_2]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE class PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<class name="top" extends="ptolemy.actor.TypedCompositeActor">
    <property name="xxx" class="ptolemy.kernel.util.Attribute">
    </property>
</class>
}

######################################################################
####
#
set moml_3_3 "$header
<entity name=\"lib\" class=\"ptolemy.actor.TypedCompositeActor\">
<class name=\"top\" extends=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"xxx\" class=\"ptolemy.kernel.util.StringAttribute\"/>
</class>
<class name=\"top\" extends=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"xxx\" value=\"a\"/>
    <property name=\"yyy\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"b\"/>
</class>
<entity name=\"test\" class=\".lib.top\">
    <property name=\"yyy\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"c\"/>
</entity>
</entity>
"
test MoMLParser-1.3.3 {check overriding class definition} {
    $parser reset
    set toplevel [java::cast ptolemy.actor.TypedCompositeActor \
            [$parser parse $moml_3_3]]
    set test [$toplevel getEntity test]
    $test exportMoML
} {<entity name="test" class=".lib.top">
    <property name="yyy" class="ptolemy.kernel.util.StringAttribute" value="c">
    </property>
</entity>
}

######################################################################
####
#
set moml_3_4 "$header
<entity name=\"lib\" class=\"ptolemy.actor.TypedCompositeActor\">
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"xxx\"/>
</entity>
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"yyy\"/>
</entity>
</entity>
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

######################################################################
####
#
set moml_3_5 "$header
<entity name=\"lib\" class=\"ptolemy.actor.TypedCompositeActor\">
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"xxx\"/>
</entity>
<entity name=\".lib.top\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"yyy\"/>
</entity>
</entity>
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

######################################################################
####
#
set moml_3_6 "$header
<entity name=\"lib\" class=\"ptolemy.actor.TypedCompositeActor\">
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"xxx\"/>
</entity>
<entity name=\"another\" class=\"ptolemy.actor.TypedCompositeActor\">
<entity name=\".lib.top\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"yyy\"/>
</entity>
</entity>
</entity>
"
test MoMLParser-1.3.6 {check multiple reference with absolute name} {
    $parser reset
    catch {$parser parse $moml_3_6]} msg
    string range $msg 0 29
} {com.microstar.xml.XmlException}

######################################################################
####
#
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

######################################################################
####
#
set moml "$header
<class name=\"top\" extends=\"ptolemy.actor.TypedCompositeActor\">
    <entity name=\"a\" class=\"ptolemy.actor.lib.Ramp\">
    </entity>
</class>
"
set result {<?xml version="1.0" standalone="no"?>
<!DOCTYPE class PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<class name="top" extends="ptolemy.actor.TypedCompositeActor">
    <entity name="a" class="ptolemy.actor.lib.Ramp">
    </entity>
</class>
}
# NOTE: result is not the same as what is parsed...
test MoMLParser-1.5 {test with an actor} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} $result

######################################################################
####
#
set moml "$classheader
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

######################################################################
####
#
set moml "$classheader
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

######################################################################
####
#
set moml "$classheader
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

######################################################################
####
#
set moml "$classheader
<class name=\"top\" extends=\"ptolemy.actor.TypedCompositeActor\">
    <entity name=\"a\" class=\"ptolemy.actor.lib.Ramp\">
        <property name=\"init\" value=\"1\">
        </property>
    </entity>
</class>
"
set result {<?xml version="1.0" standalone="no"?>
<!DOCTYPE class PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<class name="top" extends="ptolemy.actor.TypedCompositeActor">
    <entity name="a" class="ptolemy.actor.lib.Ramp">
        <property name="init" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </entity>
</class>
}
# NOTE: result is not the same as what is parsed...
test MoMLParser-1.9 {test with changed parameter value from default} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} $result

######################################################################
####
#
set moml {
<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="foo" class="ptolemy.actor.TypedCompositeActor">
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
</entity>
}
test MoMLParser-1.10 {test with hierarchy} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="foo" class="ptolemy.actor.TypedCompositeActor">
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
</entity>
}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="a" extends="ptolemy.kernel.CompositeEntity">
        <property name="p" class="ptolemy.data.expr.Parameter">
        </property>
        <port name="port" class="ptolemy.kernel.ComponentPort">
        </port>
        <entity name="c" class="ptolemy.kernel.ComponentEntity">
        </entity>
    </class>
    <entity name="b" class=".top.a">
    </entity>
</entity>
}

set moml "$header $body"

test MoMLParser-1.11 {test instantiation of a class} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="a" extends="ptolemy.kernel.CompositeEntity">
        <property name="p" class="ptolemy.data.expr.Parameter">
        </property>
        <port name="port" class="ptolemy.kernel.ComponentPort">
        </port>
        <entity name="c" class="ptolemy.kernel.ComponentEntity">
        </entity>
    </class>
    <entity name="b" class=".top.a">
    </entity>
</entity>
}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <entity name="b" class="ptolemy.moml.test.testClass">
        <property name="prop" value="1"/>
    </entity>
<property name="xxx"/>
</entity>
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
} {<entity name="b" class="ptolemy.moml.test.testClass">
    <property name="prop" class="ptolemy.data.expr.Parameter" value="1">
    </property>
</entity>
}

######################################################################
####
#
set body {
<entity name="yyy" class="ptolemy.kernel.CompositeEntity">
    <entity name="b" class="ptolemy.moml.test.testClass"/>
    <entity name="c" class="ptolemy.moml.test.testClass2">
    <property name="y" class="ptolemy.kernel.util.StringAttribute">
    </property>
    </entity>
</entity>
}

set moml "$header $body"

test MoMLParser-1.12.1 {test instantiation of a class} {
    $parser reset
    set foo [java::cast ptolemy.kernel.CompositeEntity \
            [$parser parse $moml]]
    set b [$foo getEntity b]
    set c [$foo getEntity c]
    list [$b exportMoML] [$c exportMoML]
} {{<entity name="b" class="ptolemy.moml.test.testClass">
</entity>
} {<entity name="c" class="ptolemy.moml.test.testClass2">
    <property name="y" class="ptolemy.kernel.util.StringAttribute">
    </property>
</entity>
}}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <entity name="b" class="testClass" source="testClass.xml"/>
    <entity name="c" class="testClass2" source="testClass2.xml"/>
</entity>
}

set moml "$header $body"

test MoMLParser-1.12.3 {test class instances with no override} {
    $parser reset
    set toplevel [java::cast ptolemy.kernel.CompositeEntity \
            [$parser parse $moml]]
    set b [$toplevel getEntity b]
    set c [$toplevel getEntity c]
    list [$b exportMoML] [$c exportMoML]
} {{<entity name="b" class="testClass" source="testClass.xml">
</entity>
} {<entity name="c" class="testClass2" source="testClass2.xml">
</entity>
}}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="master" extends="ptolemy.kernel.ComponentEntity">
        <port name="p" class="ptolemy.kernel.ComponentPort"/>
    </class>
    <entity name="derived" class=".top.master"/>
</entity>
}

set moml "$header $body"

test MoMLParser-1.13.1 {test mutation after class instantiation} {
    $parser reset
    set toplevel [$parser parse $moml]
    $parser parse {<entity name=".top.derived">
    <port name="q" class="ptolemy.kernel.ComponentPort"/>
</entity>}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="master" extends="ptolemy.kernel.ComponentEntity">
        <port name="p" class="ptolemy.kernel.ComponentPort">
        </port>
    </class>
    <entity name="derived" class=".top.master">
        <port name="q" class="ptolemy.kernel.ComponentPort">
        </port>
    </entity>
</entity>
}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="master" extends="ptolemy.kernel.ComponentEntity">
        <port name="p" class="ptolemy.kernel.ComponentPort">
        </port>
    </class>
    <class name="derived" extends=".top.master">
        <port name="q" class="ptolemy.kernel.ComponentPort">
        </port>
    </class>
</entity>
}

set moml "$header $body"

test MoMLParser-1.13.2 {test extension of a class} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="master" extends="ptolemy.kernel.ComponentEntity">
        <port name="p" class="ptolemy.kernel.ComponentPort">
        </port>
    </class>
    <class name="derived" extends=".top.master">
        <port name="q" class="ptolemy.kernel.ComponentPort">
        </port>
    </class>
</entity>
}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <entity name="master" class="ptolemy.kernel.ComponentEntity">
    </entity>
    <entity name="derived" class="master">
    </entity>
</entity>
}

set moml "$header $body"

test MoMLParser-1.14 {test that instantiation of an entity fails} {
    $parser reset
    catch {set toplevel [$parser parse $moml]} msg
    string range $msg 0 29
} {com.microstar.xml.XmlException}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="master" extends="ptolemy.kernel.CompositeEntity">
        <entity name="e1" class="ptolemy.kernel.ComponentEntity">
        </entity>
    </class>
    <class name="derived" extends=".top.master">
        <entity name="e2" class="ptolemy.kernel.ComponentEntity">
        </entity>
    </class>
</entity>
}

set moml "$header $body"

test MoMLParser-1.15 {test extension of a composite class} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="master" extends="ptolemy.kernel.CompositeEntity">
        <entity name="e1" class="ptolemy.kernel.ComponentEntity">
        </entity>
    </class>
    <class name="derived" extends=".top.master">
        <entity name="e2" class="ptolemy.kernel.ComponentEntity">
        </entity>
    </class>
</entity>
}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="master" extends="ptolemy.kernel.CompositeEntity">
        <entity name="e1" class="ptolemy.kernel.ComponentEntity">
        </entity>
    </class>
    <entity name="derived" class=".top.master">
        <entity name="e2" class="ptolemy.kernel.ComponentEntity">
        </entity>
    </entity>
</entity>
}

set moml "$header $body"

test MoMLParser-1.16 {test instatiation of a composite class} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="master" extends="ptolemy.kernel.CompositeEntity">
        <entity name="e1" class="ptolemy.kernel.ComponentEntity">
        </entity>
    </class>
    <entity name="derived" class=".top.master">
        <entity name="e2" class="ptolemy.kernel.ComponentEntity">
        </entity>
    </entity>
</entity>
}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="master" extends="ptolemy.kernel.CompositeEntity">
        <entity name="e1" class="ptolemy.kernel.ComponentEntity">
        </entity>
    </class>
    <class name="derived" extends=".top.master">
    </class>
</entity>
}

set moml "$header $body"

test MoMLParser-1.17 {test extension of a composite class} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="master" extends="ptolemy.kernel.CompositeEntity">
        <entity name="e1" class="ptolemy.kernel.ComponentEntity">
        </entity>
    </class>
    <class name="derived" extends=".top.master">
    </class>
</entity>
}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.actor.CompositeActor">
    <property name="dir" class="ptolemy.actor.Director"/>
</entity>
}

set moml "$header $body"

test MoMLParser-1.18 {test property persistence} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.CompositeActor">
    <property name="dir" class="ptolemy.actor.Director">
    </property>
</entity>
}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.actor.CompositeActor">
    <class name="master" extends="ptolemy.actor.CompositeActor">
    </class>
    <entity name="derived" class=".top.master">
        <property name="dir" class="ptolemy.actor.Director"/>
    </entity>
</entity>
}

set moml "$header $body"

test MoMLParser-1.18.1 {test property persistence in instatiation of a class} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.CompositeActor">
    <class name="master" extends="ptolemy.actor.CompositeActor">
    </class>
    <entity name="derived" class=".top.master">
        <property name="dir" class="ptolemy.actor.Director">
        </property>
    </entity>
</entity>
}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.actor.CompositeActor">
    <class name="master" extends="ptolemy.actor.CompositeActor">
    </class>
    <entity name="derived" class=".top.master">
        <relation name="rel" class="ptolemy.actor.IORelation"/>
    </entity>
</entity>
}

set moml "$header $body"

test MoMLParser-1.18.2 {test relation persistence in instatiation of a class} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.CompositeActor">
    <class name="master" extends="ptolemy.actor.CompositeActor">
    </class>
    <entity name="derived" class=".top.master">
        <relation name="rel" class="ptolemy.actor.IORelation">
        </relation>
    </entity>
</entity>
}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.actor.CompositeActor">
    <class name="master" extends="ptolemy.actor.CompositeActor">
        <entity name="e" class="ptolemy.actor.CompositeActor"/>
    </class>
    <entity name="derived" class=".top.master">
        <deleteEntity name="e"/>
    </entity>
</entity>
}

set moml "$header $body"

test MoMLParser-1.18.3 {test illegal deletion in instatiation of a class} {
    $parser reset
    catch {set toplevel [$parser parse $moml]} msg
    string range $msg 0 52
} {com.microstar.xml.XmlException: XML element "deleteEn}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.actor.CompositeActor">
    <class name="master" extends="ptolemy.actor.CompositeActor">
        <port name="p" class="ptolemy.actor.IOPort"/>
    </class>
    <entity name="derived" class=".top.master">
        <deletePort name="p"/>
    </entity>
</entity>
}

set moml "$header $body"

test MoMLParser-1.18.4 {test illegal deletion in instatiation of a class} {
    $parser reset
    catch {set toplevel [$parser parse $moml]} msg
    string range $msg 0 52
} {com.microstar.xml.XmlException: XML element "deletePo}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.actor.CompositeActor">
    <class name="master" extends="ptolemy.actor.CompositeActor">
        <property name="a" class="ptolemy.data.expr.Parameter"/>
        <property name="b" class="ptolemy.data.expr.Parameter"/>
    </class>
    <entity name="derived" class=".top.master">
        <deleteProperty name="a"/>
    </entity>
</entity>
}

set moml "$header $body"

test MoMLParser-1.18.5 {test illegal deletion in instatiation of a class} {
    $parser reset
    catch {set toplevel [$parser parse $moml]} msg
    string range $msg 0 52
} {com.microstar.xml.XmlException: XML element "deletePr}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.actor.CompositeActor">
    <class name="master" extends="ptolemy.actor.CompositeActor">
        <relation name="r" class="ptolemy.actor.IORelation"/>
    </class>
    <entity name="derived" class=".top.master">
        <deleteRelation name="r"/>
    </entity>
</entity>
}

set moml "$header $body"

test MoMLParser-1.18.6 {test deletion persistence in instatiation of a class} {
    $parser reset
    catch {set toplevel [$parser parse $moml]} msg
    string range $msg 0 52
} {com.microstar.xml.XmlException: XML element "deleteRe}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.actor.CompositeActor">
    <class name="master" extends="ptolemy.actor.CompositeActor">
        <port name="p" class="ptolemy.actor.IOPort"/>
        <relation name="r" class="ptolemy.actor.IORelation"/>
    </class>
    <entity name="derived" class=".top.master">
        <link port="p" relation="r"/>
    </entity>
</entity>
}

set moml "$header $body"

test MoMLParser-1.18.7 {test link persistence in instatiation of a class} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.CompositeActor">
    <class name="master" extends="ptolemy.actor.CompositeActor">
        <port name="p" class="ptolemy.actor.IOPort">
        </port>
        <relation name="r" class="ptolemy.actor.IORelation">
        </relation>
    </class>
    <entity name="derived" class=".top.master">
        <link port="p" relation="r"/>
    </entity>
</entity>
}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.actor.CompositeActor">
    <class name="master" extends="ptolemy.actor.CompositeActor">
        <port name="p" class="ptolemy.actor.IOPort"/>
        <relation name="r" class="ptolemy.actor.IORelation"/>
    </class>
    <entity name="derived" class=".top.master">
        <link port="p" relation="r" insertAt="0"/>
    </entity>
</entity>
}

set moml "$header $body"

test MoMLParser-1.18.8 {test link persistence in instatiation of a class} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.CompositeActor">
    <class name="master" extends="ptolemy.actor.CompositeActor">
        <port name="p" class="ptolemy.actor.IOPort">
        </port>
        <relation name="r" class="ptolemy.actor.IORelation">
        </relation>
    </class>
    <entity name="derived" class=".top.master">
        <link port="p" relation="r" insertAt="0"/>
    </entity>
</entity>
}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.actor.CompositeActor">
    <class name="master" extends="ptolemy.actor.CompositeActor">
        <port name="p" class="ptolemy.actor.IOPort"/>
        <relation name="r" class="ptolemy.actor.IORelation"/>
        <link port="p" relation="r"/>        
    </class>
    <entity name="derived" class=".top.master">
        <unlink port="p" relation="r"/>
    </entity>
</entity>
}

set moml "$header $body"

test MoMLParser-1.18.9 {test unlink persistence in instatiation of a class} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.CompositeActor">
    <class name="master" extends="ptolemy.actor.CompositeActor">
        <port name="p" class="ptolemy.actor.IOPort">
        </port>
        <relation name="r" class="ptolemy.actor.IORelation">
        </relation>
        <link port="p" relation="r"/>
    </class>
    <entity name="derived" class=".top.master">
        <unlink port="p" relation="r"/>
    </entity>
</entity>
}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.actor.CompositeActor">
    <entity name="master" class="ptolemy.actor.CompositeActor">
        <port name="p" class="ptolemy.actor.IOPort"/>
        <relation name="r" class="ptolemy.actor.IORelation"/>
        <link port="p" relation="r"/>        
        <unlink port="p" relation="r"/>
    </entity>
</entity>
}

set moml "$header $body"

test MoMLParser-1.18.9.1 {test unlink inside by relation} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.CompositeActor">
    <entity name="master" class="ptolemy.actor.CompositeActor">
        <port name="p" class="ptolemy.actor.IOPort">
        </port>
        <relation name="r" class="ptolemy.actor.IORelation">
        </relation>
    </entity>
</entity>
}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.actor.CompositeActor">
    <class name="master" extends="ptolemy.actor.CompositeActor">
        <port name="p" class="ptolemy.actor.IOPort"/>
        <relation name="r" class="ptolemy.actor.IORelation"/>
        <link port="p" relation="r"/>        
    </class>
    <entity name="derived" class=".top.master">
        <unlink port="p" insideIndex="0"/>
    </entity>
</entity>
}

set moml "$header $body"

test MoMLParser-1.18.10 {test unlink persistence in instatiation of a class} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.CompositeActor">
    <class name="master" extends="ptolemy.actor.CompositeActor">
        <port name="p" class="ptolemy.actor.IOPort">
        </port>
        <relation name="r" class="ptolemy.actor.IORelation">
        </relation>
        <link port="p" relation="r"/>
    </class>
    <entity name="derived" class=".top.master">
        <unlink port="p" insideIndex="0"/>
    </entity>
</entity>
}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.actor.CompositeActor">
    <class name="master" extends="ptolemy.actor.CompositeActor">
        <entity name="e" class="ptolemy.actor.AtomicActor">
            <port name="p" class="ptolemy.actor.IOPort"/>
        </entity>
        <relation name="r" class="ptolemy.actor.IORelation"/>
        <link port="e.p" relation="r"/>        
    </class>
    <entity name="derived" class=".top.master">
        <unlink port="e.p" index="0"/>
    </entity>
</entity>
}

set moml "$header $body"

test MoMLParser-1.18.11 {test unlink persistence in instatiation of a class} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.CompositeActor">
    <class name="master" extends="ptolemy.actor.CompositeActor">
        <entity name="e" class="ptolemy.actor.AtomicActor">
            <port name="p" class="ptolemy.actor.IOPort">
            </port>
        </entity>
        <relation name="r" class="ptolemy.actor.IORelation">
        </relation>
        <link port="e.p" relation="r"/>
    </class>
    <entity name="derived" class=".top.master">
        <unlink port="e.p" index="0"/>
    </entity>
</entity>
}

######################################################################
####
#
set moml "$header
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"a\" class=\"ptolemy.data.expr.Parameter\"
         value=\"&quot;x&quot;\">
    </property>
</entity>
"
set result {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="a" class="ptolemy.data.expr.Parameter" value="&quot;x&quot;">
    </property>
</entity>
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

######################################################################
####
#
set moml "$classheader
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


######################################################################
####
#
set moml "$classheader
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
    #<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    #    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
    #<entity name="topLevel" class="ptolemy.actor.TypedCompositeActor">
    #    <property name="DEDirector" class="ptolemy.domains.de.kernel.DEDirector">
    #    </property>
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
    #</entity>

    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} $moml

######################################################################
####
#
test MoMLParser-2.1 {Test incremental parsing: add entity} {
    set incMomlBase "$header
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
</entity>
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
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="inside" class="ptolemy.actor.TypedCompositeActor">
    </entity>
</entity>
}

######################################################################
####
#
test MoMLParser-2.2 {Test incremental parsing: add entity deeper} {
    set incMoml_2 "<entity name=\".top.inside\">
<property name=\"prop\" class=\"ptolemy.data.expr.Parameter\"/>
</entity>
"
    set toplevel [$parser parse $incMoml_2]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="inside" class="ptolemy.actor.TypedCompositeActor">
        <property name="prop" class="ptolemy.data.expr.Parameter">
        </property>
    </entity>
</entity>
}

######################################################################
####
#
test MoMLParser-2.3 {Test incremental parsing: add port} {
    set incMoml_2_3 "<entity name=\".top.inside\">
<port name=\"input\" class=\"ptolemy.actor.TypedIOPort\"/>
</entity>
"
    set toplevel [$parser parse $incMoml_2_3]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="inside" class="ptolemy.actor.TypedCompositeActor">
        <property name="prop" class="ptolemy.data.expr.Parameter">
        </property>
        <port name="input" class="ptolemy.actor.TypedIOPort">
        </port>
    </entity>
</entity>
}

######################################################################
####
#
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
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
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
</entity>
}

######################################################################
####
#
test MoMLParser-2.5 {Test incremental parsing: remove an entity} {
    set incMoml_2_5 "<entity name=\".top\">
    <deleteEntity name=\"a\"/>
</entity>
"
    set toplevel [$parser parse $incMoml_2_5]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="inside" class="ptolemy.actor.TypedCompositeActor">
        <property name="prop" class="ptolemy.data.expr.Parameter">
        </property>
        <port name="input" class="ptolemy.actor.TypedIOPort">
        </port>
    </entity>
    <relation name="r" class="ptolemy.actor.TypedIORelation">
    </relation>
    <link port="inside.input" relation="r"/>
</entity>
}

######################################################################
####
#
test MoMLParser-2.5.1 {Test incremental parsing: remove a link} {
    set incMoml_2_5_1 {<entity name=".top">
    <unlink port="inside.input" relation="r"/>
</entity>}
    set toplevel [$parser parse $incMoml_2_5_1]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="inside" class="ptolemy.actor.TypedCompositeActor">
        <property name="prop" class="ptolemy.data.expr.Parameter">
        </property>
        <port name="input" class="ptolemy.actor.TypedIOPort">
        </port>
    </entity>
    <relation name="r" class="ptolemy.actor.TypedIORelation">
    </relation>
</entity>
}

######################################################################
####
#
test MoMLParser-2.6 {Test incremental parsing: remove nonexistent entity} {
    set incMoml_2_6 "<entity name=\".top\">
    <deleteEntity name=\"a\"/>
</entity>
"
    catch {$parser parse $incMoml_2_6} msg
    string range $msg 0 52
} {com.microstar.xml.XmlException: No such entity to del}

######################################################################
####
#
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
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="inside" class="ptolemy.actor.TypedCompositeActor">
        <property name="prop" class="ptolemy.data.expr.Parameter">
        </property>
        <port name="input" class="ptolemy.actor.TypedIOPort">
        </port>
    </entity>
</entity>
}

######################################################################
####
#
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
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="inside" class="ptolemy.actor.TypedCompositeActor">
        <property name="prop" class="ptolemy.data.expr.Parameter">
        </property>
    </entity>
</entity>
}

######################################################################
####
#
test MoMLParser-2.8.1 {Test incremental parsing: remove a port using entity attribute} {
    # First add the port back in
    set incMoml_2_8_1_a {<entity name=".top">
    <entity name="inside">
        <port name="input" class="ptolemy.actor.TypedIOPort">
        </port>
    </entity>
</entity>}
    # Test using a new parser.
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser setToplevel $toplevel
    $parser parse $incMoml_2_8_1_a

    # Then delete it
    set incMoml_2_8_1_b {<entity name=".top">
    <deletePort name="input" entity="inside"/>
</entity>}
    $parser parse $incMoml_2_8_1_b
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="inside" class="ptolemy.actor.TypedCompositeActor">
        <property name="prop" class="ptolemy.data.expr.Parameter">
        </property>
    </entity>
</entity>
}

######################################################################
####
#
test MoMLParser-2.8.2 {Test incremental parsing: remove a port using 
		entity attribute, check exception is thrown if port name 
		not immediate} {
    # First add the port back in
    set incMoml_2_8_2_a {<entity name=".top">
    <entity name="inside">
        <port name="input" class="ptolemy.actor.TypedIOPort">
        </port>
    </entity>
</entity>}
    # Test using a new parser.
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser setToplevel $toplevel
    $parser parse $incMoml_2_8_2_a

    # Then delete it
    set incMoml_2_8_2_b {<entity name=".top">
    <deletePort name="inside.input" entity="inside"/>
</entity>}
    catch {$parser parse $incMoml_2_8_2_b} msg
    string range $msg 0 51
} {com.microstar.xml.XmlException: Invalid port name: i}


######################################################################
####
#
test MoMLParser-2.9 {Test link with insertAt attribute, inside links} {
    $parser reset
    set toplevel [$parser parse {
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <port name="p" class="ptolemy.kernel.ComponentPort"/>
    <entity name="a" class="ptolemy.kernel.CompositeEntity">
        <port name="p" class="ptolemy.kernel.ComponentPort"/>
    </entity>
    <relation name="r" class="ptolemy.kernel.ComponentRelation"/>
    <link port="p" relation="r" insertAt="1"/>
</entity>
}]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <port name="p" class="ptolemy.kernel.ComponentPort">
    </port>
    <entity name="a" class="ptolemy.kernel.CompositeEntity">
        <port name="p" class="ptolemy.kernel.ComponentPort">
        </port>
    </entity>
    <relation name="r" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <link port="p" insertAt="1" relation="r"/>
</entity>
}

######################################################################
####
#
test MoMLParser-2.10 {Test link with insertAt attribute, inside links} {
   $parser parse {
<entity name="top">
   <link port="p" relation="r" insertAt="0"/>
</entity>
}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
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
</entity>
}

######################################################################
####
#
test MoMLParser-2.11 {Test link with insertAt attribute} {
   $parser parse {
<entity name=".top">
   <link port="a.p" relation="r" insertAt="0"/>
</entity>
}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
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
</entity>
}

######################################################################
####
#
test MoMLParser-2.12 {Test link with insertAt attribute} {
   $parser parse {
<entity name=".top">
   <link port="a.p" insertAt="2" relation="r"/>
</entity>
}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
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
</entity>
}

######################################################################
####
#
test MoMLParser-2.13 {Test unlink with index} {
   $parser parse {
<entity name=".top">
   <unlink port="a.p" index="0"/>
</entity>
}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
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
</entity>
}

######################################################################
####
#
test MoMLParser-2.14 {Test unlink with index} {
   $parser parse {
<entity name=".top">
   <unlink port="a.p" index="1"/>
</entity>
}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
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
</entity>
}

######################################################################
####
#
test MoMLParser-2.15 {Test unlink with index} {
   $parser parse {
<entity name=".top">
   <unlink port="p" insideIndex="0"/>
</entity>
}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <port name="p" class="ptolemy.kernel.ComponentPort">
    </port>
    <entity name="a" class="ptolemy.kernel.CompositeEntity">
        <port name="p" class="ptolemy.kernel.ComponentPort">
        </port>
    </entity>
    <relation name="r" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <link port="p" insertAt="1" relation="r"/>
</entity>
}

######################################################################
####
#
test MoMLParser-2.16 {Test unlink with index} {
   $parser parse {
<entity name=".top">
   <unlink port="p" insideIndex="0"/>
</entity>
}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <port name="p" class="ptolemy.kernel.ComponentPort">
    </port>
    <entity name="a" class="ptolemy.kernel.CompositeEntity">
        <port name="p" class="ptolemy.kernel.ComponentPort">
        </port>
    </entity>
    <relation name="r" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <link port="p" relation="r"/>
</entity>
}

######################################################################
####
#
test MoMLParser-2.17 {Test unlink with index} {
   $parser parse {
<entity name=".top">
   <unlink port="p" insideIndex="1"/>
</entity>
}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <port name="p" class="ptolemy.kernel.ComponentPort">
    </port>
    <entity name="a" class="ptolemy.kernel.CompositeEntity">
        <port name="p" class="ptolemy.kernel.ComponentPort">
        </port>
    </entity>
    <relation name="r" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <link port="p" relation="r"/>
</entity>
}

######################################################################
####
#
test MoMLParser-2.18 {Test unlink with index} {
   $parser parse {
<entity name=".top">
   <unlink port="p" insideIndex="0"/>
</entity>
}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <port name="p" class="ptolemy.kernel.ComponentPort">
    </port>
    <entity name="a" class="ptolemy.kernel.CompositeEntity">
        <port name="p" class="ptolemy.kernel.ComponentPort">
        </port>
    </entity>
    <relation name="r" class="ptolemy.kernel.ComponentRelation">
    </relation>
</entity>
}

######################################################################
####
#
test MoMLParser-3.1 {Test invalid containment} {
    set incMoml_3_1 "$header
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
  <entity name=\"a\" class=\"ptolemy.actor.TypedCompositeActor\"/>
  <entity name=\"b\" class=\"ptolemy.actor.TypedCompositeActor\">
     <entity name=\".top.a\">
        <entity name=\"c\" class=\"ptolemy.actor.TypedCompositeActor\"/>
     </entity>
  </entity>
</entity>
"
    set parser [java::new ptolemy.moml.MoMLParser]
    catch {$parser parse $incMoml_3_1} msg
    string range $msg 0 51
} {com.microstar.xml.XmlException: Reference to an exis}

######################################################################
####
#
test MoMLParser-3.2 {Test invalid containment} {
    set incMoml_3_2 "$header
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
  <entity name=\"a\" class=\"ptolemy.actor.TypedCompositeActor\">
     <entity name=\"b\" class=\"ptolemy.actor.TypedCompositeActor\">
        <entity name=\".top.a\"/>
     </entity>
  </entity>
</entity>
"
    $parser reset
    catch {$parser parse $incMoml_3_2} msg
    string range $msg 0 51
} {com.microstar.xml.XmlException: Reference to an exis}

######################################################################
####
#
test MoMLParser-3.3 {Test invalid containment} {
    set incMoml_3_3 "$header
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
  <entity name=\"a\" class=\"ptolemy.actor.TypedCompositeActor\">
     <entity name=\"b\" class=\"ptolemy.actor.TypedCompositeActor\">
        <entity name=\".top\"/>
     </entity>
  </entity>
</entity>
"
    $parser reset
    catch {$parser parse $incMoml_3_3} msg
    string range $msg 0 51
} {com.microstar.xml.XmlException: Reference to an exis}

######################################################################
####
#
test MoMLParser-3.4 {Test invalid containment} {
    set incMoml_3_4 "$header
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
  <entity name=\"a\" class=\"ptolemy.actor.TypedCompositeActor\">
     <entity name=\"b\" class=\"ptolemy.actor.TypedCompositeActor\">
        <entity name=\".top\"/>
     </entity>
  </entity>
  <entity name=\"a.b\"/>
</entity>
"
    $parser reset
    catch {$parser parse $incMoml_3_4} msg
    string range $msg 0 51
} {com.microstar.xml.XmlException: Reference to an exis}

######################################################################
####
#
test MoMLParser-4.1 {Test doc element addition} {
    set incMomlBase "$header
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
</entity>
"
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $incMomlBase]
    $parser parse {<entity name=".top"><doc name="foo">xxx</doc></entity>}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <doc name="foo">xxx</doc>
</entity>
}

######################################################################
####
#
test MoMLParser-4.2 {Test doc element modifications} {
    $parser parse {<entity name=".top"><doc name="foo">yyy</doc></entity>}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <doc name="foo">yyy</doc>
</entity>
}

######################################################################
####
#
test MoMLParser-4.3 {Test doc element removal} {
    $parser parse {<entity name=".top"><doc name="foo"/></entity>}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
</entity>
}

######################################################################
####
#
test MoMLParser-4.4 {Test doc element removal with default name} {
    $parser parse {<entity name=".top"><doc>zzz</doc></entity>}
    $parser parse {<entity name=".top"><doc></doc></entity>}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
</entity>
}

######################################################################
####
#
test MoMLParser-5.1 {Test property deletion} {
    set incMomlBase "$header
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
   <property name=\"foo\"/>
</entity>
"
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $incMomlBase]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="foo" class="ptolemy.kernel.util.Attribute">
    </property>
</entity>
}

######################################################################
####
#
test MoMLParser-5.2 {Test property deletion using absolute names} {
    $parser parse {<entity name=".top"><deleteProperty name="foo"/></entity>}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
</entity>
}

######################################################################
####
#
test MoMLParser-5.2.1 {Test property deletion} {
    # Add the property back in
    $parser parse {<entity name=".top"><property name="foo"/></entity>}

    # Then delete it
    $parser parse {<entity name=".top"><deleteProperty name=".top.foo"/></entity>}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
</entity>
}

######################################################################
####
#
test MoMLParser-6.1 {Test indexed I/O with actor model.} {
    set incMomlBase "$header
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
</entity>
"
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.actor.TypedCompositeActor \
            [$parser parse $incMomlBase]]
    $parser parse {
<entity name=".top">
<property name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
    <property name="iterations" value="1"/>
</property>
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
</entity>
}
    set manager [java::new ptolemy.actor.Manager \
            [$toplevel workspace] "manager"]
    $toplevel setManager $manager
    $manager execute
    set recorder [java::cast ptolemy.actor.lib.Recorder \
            [$toplevel getEntity "sink"]]
    enumToTokenValues [$recorder getRecord 0]
} {0}

######################################################################
####
#
test MoMLParser-6.1 {Test indexed I/O with actor model.} {
    set incMomlBase "$header
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
</entity>
"
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.actor.TypedCompositeActor \
            [$parser parse $incMomlBase]]
    $parser parse {
<entity name=".top">
<property name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
    <property name="iterations" value="1"/>
</property>
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
</entity>
}
    set manager [java::new ptolemy.actor.Manager \
            [$toplevel workspace] "manager"]
    $toplevel setManager $manager
    $manager execute
    set recorder [java::cast ptolemy.actor.lib.Recorder \
            [$toplevel getEntity "sink"]]
    enumToTokenValues [$recorder getRecord 0]
} {0}

######################################################################
####
#
test MoMLParser-6.2 {Straight with blocksize 2.} {
    $parser parse {
<entity name=".top">
<relation name="r4" class="ptolemy.actor.TypedIORelation"/>
<link port="dist.output" relation="r4"/>
<link port="comm.input" relation="r4"/>
</entity>
}
#     set dir [java::cast ptolemy.domains.sdf.kernel.SDFDirector \
#             [$toplevel getDirector]]
#     set sch [java::cast ptolemy.domains.sdf.kernel.SDFScheduler \
#             [$dir getScheduler]]
#     $sch addDebugListener [java::new ptolemy.kernel.util.StreamListener]
    $manager execute
    enumToTokenValues [$recorder getRecord 0]
} {0 1}

######################################################################
####
#
test MoMLParser-6.3 {Reverse with blocksize 2.} {
    $parser parse {
<entity name=".top">
<unlink port="dist.output" relation="r4"/>
<unlink port="comm.input" index="1"/>
<link port="dist.output" relation="r4"/>
<link port="comm.input" relation="r4" insertAt="0"/>
</entity>
}
    $manager execute
    enumToTokenValues [$recorder getRecord 0]
} {1 0}

######################################################################
####
#
test MoMLParser-6.4 {Reverse with blocksize 3.} {
    $parser parse {
<entity name=".top">
<relation name="r5" class="ptolemy.actor.TypedIORelation"/>
<link port="dist.output" relation="r5" insertAt="0"/>
<link port="comm.input" relation="r5"/>
</entity>
}
    $manager execute
    enumToTokenValues [$recorder getRecord 0]
} {2 1 0}

######################################################################
####
#
test MoMLParser-6.5 {Reverse with blocksize 4 and gaps.} {
    $parser parse {
<entity name=".top">
<relation name="r6" class="ptolemy.actor.TypedIORelation"/>
<link port="dist.output" relation="r6" insertAt="10"/>
<link port="comm.input" relation="r6" insertAt="0"/>
</entity>
}
    $manager execute
    enumToTokenValues [$recorder getRecord 0]
} {3 2 1 0}

######################################################################
####
#
test MoMLParser-6.6 {Delete the gaps, having no effect.} {
    $parser parse {
<entity name=".top">
<unlink port="dist.output" index="8"/>
</entity>
}
    $manager execute
    enumToTokenValues [$recorder getRecord 0]
} {3 2 1 0}

######################################################################
####
#
test MoMLParser-7.1 {Test setContext()} {
    set incMomlBase "$header
<entity name=\"top\" class=\"ptolemy.kernel.CompositeEntity\">
</entity>
"
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.kernel.CompositeEntity \
            [$parser parse $incMomlBase]]
    $parser parse {
<entity name=".top">
   <entity name="a" class="ptolemy.kernel.CompositeEntity"/>
</entity>
}
    $parser setContext [$toplevel getEntity "a"]
    $parser parse {<entity name="b" class="ptolemy.kernel.CompositeEntity"/>}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <entity name="a" class="ptolemy.kernel.CompositeEntity">
        <entity name="b" class="ptolemy.kernel.CompositeEntity">
        </entity>
    </entity>
</entity>
}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <!-- Note that here, we are searching using the classpath -->
    <input source="ptolemy/moml/test/testClass2.xml"/>
</entity>
}

set moml "$header $body"

test MoMLParser-8.1 {test input with a relative source } {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="testClass2" extends="ptolemy.kernel.CompositeEntity">
        <property name="x" class="ptolemy.kernel.util.StringAttribute">
        </property>
    </class>
</entity>
}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <!-- Note that here, we are searching relative to the cwd -->
    <input source="testClass2.xml"/>
</entity>
}

set moml "$header $body"

test MoMLParser-8.2 {test input with a relative source } {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="testClass2" extends="ptolemy.kernel.CompositeEntity">
        <property name="x" class="ptolemy.kernel.util.StringAttribute">
        </property>
    </class>
</entity>
}

set body {
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <!-- Note that here, we are searching relative to the cwd -->
    <input source="testClass2DotMoml.moml"/>
</entity>
}
set moml "$header $body"

test MoMLParser-8.3 {test input with a relative source with a file ending in .moml} {
    # RIM uses .moml files, so leave them in.
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="testClass2" extends="ptolemy.kernel.CompositeEntity">
        <property name="x" class="ptolemy.kernel.util.Attribute">
        </property>
    </class>
</entity>
}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.kernel.CompositeEntity">
  <group name="a">
    <entity name="b" class="ptolemy.kernel.CompositeEntity">
      <entity name="c" class="ptolemy.kernel.CompositeEntity"/>
    </entity>
  </group>
</entity>
}

set moml "$header $body"

test MoMLParser-9.1 {test namespaces} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <entity name="a:b" class="ptolemy.kernel.CompositeEntity">
        <entity name="c" class="ptolemy.kernel.CompositeEntity">
        </entity>
    </entity>
</entity>
}

set body {
<entity name="top" class="ptolemy.kernel.CompositeEntity">
  <entity name="b" class="ptolemy.kernel.CompositeEntity"/>
  <group name="auto">
    <entity name="b" class="ptolemy.kernel.CompositeEntity">
      <entity name="c" class="ptolemy.kernel.CompositeEntity"/>
    </entity>
  </group>
</entity>
}

set moml "$header $body"

test MoMLParser-9.2 {test namespace with auto naming} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <entity name="b" class="ptolemy.kernel.CompositeEntity">
    </entity>
    <entity name="b2" class="ptolemy.kernel.CompositeEntity">
        <entity name="c" class="ptolemy.kernel.CompositeEntity">
        </entity>
    </entity>
</entity>
}

set body {
<entity name="top" class="ptolemy.kernel.CompositeEntity">
  <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2"/>
  <entity name="b" class="ptolemy.kernel.CompositeEntity">
     <port name="p" class="ptolemy.kernel.ComponentPort"/>
  </entity>
  <relation name="r" class="ptolemy.kernel.ComponentRelation"/>
  <link port="b.p" relation="r"/>
  <group name="auto">
    <entity name="b" class="ptolemy.kernel.CompositeEntity">
      <entity name="c" class="ptolemy.kernel.CompositeEntity"/>
      <port name="p" class="ptolemy.kernel.ComponentPort"/>
    </entity>
    <relation name="r" class="ptolemy.kernel.ComponentRelation"/>
    <link port="b.p" relation="r"/>
  </group>
</entity>
}

set moml "$header $body"

test MoMLParser-9.3 {test namespace with auto naming} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2">
    </property>
    <entity name="b" class="ptolemy.kernel.CompositeEntity">
        <port name="p" class="ptolemy.kernel.ComponentPort">
        </port>
    </entity>
    <entity name="b2" class="ptolemy.kernel.CompositeEntity">
        <port name="p" class="ptolemy.kernel.ComponentPort">
        </port>
        <entity name="c" class="ptolemy.kernel.CompositeEntity">
        </entity>
    </entity>
    <relation name="r" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <relation name="r2" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <link port="b.p" relation="r"/>
    <link port="b2.p" relation="r2"/>
</entity>
}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
   <entity name="A" class="ptolemy.actor.TypedAtomicActor">
      <port name="out" class="ptolemy.actor.TypedIOPort">
         <property name="output"/>
      </port>
   </entity>
   <entity name="B" class="ptolemy.actor.TypedAtomicActor">
      <port name="in" class="ptolemy.actor.TypedIOPort">
         <property name="input"/>
      </port>
   </entity>
   <entity name="C" class="ptolemy.actor.TypedAtomicActor">
      <port name="in" class="ptolemy.actor.TypedIOPort">
         <property name="input"/>
      </port>
   </entity>

   <relation name="r" class="ptolemy.actor.TypedIORelation">
       <vertex name="v1"/>
       <vertex name="v2" pathTo="v1"/>
   </relation>

   <link port="A.out" relation="r" vertex="v1"/>
   <link port="B.in" relation="r" vertex="v1"/>
   <link port="C.in" relation="r" vertex="v2"/>
</entity>
}

set moml "$header $body"

test MoMLParser-10.1 {test vertex} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="A" class="ptolemy.actor.TypedAtomicActor">
        <port name="out" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
    </entity>
    <entity name="B" class="ptolemy.actor.TypedAtomicActor">
        <port name="in" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
        </port>
    </entity>
    <entity name="C" class="ptolemy.actor.TypedAtomicActor">
        <port name="in" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
        </port>
    </entity>
    <relation name="r" class="ptolemy.actor.TypedIORelation">
        <vertex name="v1">
        </vertex>
        <vertex name="v2">
        </vertex>
    </relation>
    <link port="A.out" relation="r"/>
    <link port="B.in" relation="r"/>
    <link port="C.in" relation="r"/>
</entity>
}

test MoMLParser-10.2 {exportMoML and then parse it - a good test for SaveAs } {
    # Depends on MoMLParser-10.1 above
    $parser reset
    set toplevel2 [$parser parse [$toplevel exportMoML]]
    $toplevel2 exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="A" class="ptolemy.actor.TypedAtomicActor">
        <port name="out" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
    </entity>
    <entity name="B" class="ptolemy.actor.TypedAtomicActor">
        <port name="in" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
        </port>
    </entity>
    <entity name="C" class="ptolemy.actor.TypedAtomicActor">
        <port name="in" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
        </port>
    </entity>
    <relation name="r" class="ptolemy.actor.TypedIORelation">
        <vertex name="v1">
        </vertex>
        <vertex name="v2">
        </vertex>
    </relation>
    <link port="A.out" relation="r"/>
    <link port="B.in" relation="r"/>
    <link port="C.in" relation="r"/>
</entity>
}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.kernel.util.NamedObj">
   <property name="p" class="ptolemy.kernel.util.ConfigurableAttribute">
      <configure>xxx</configure>
   </property>
</entity>
}

set moml "$header $body"

test MoMLParser-11.1 {test configuration} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.util.NamedObj">
    <property name="p" class="ptolemy.kernel.util.ConfigurableAttribute">
        <configure>xxx</configure>
    </property>
</entity>
}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.kernel.util.NamedObj">
    <property name="p" class="ptolemy.kernel.util.ConfigurableAttribute">
        <configure>
<h1>Header</h1>
<p>Paragraph.</p>
        </configure>
    </property>
</entity>
}

set moml "$header $body"

test MoMLParser-11.2 {test configuration with embedded HTML} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.util.NamedObj">
    <property name="p" class="ptolemy.kernel.util.ConfigurableAttribute">
        <configure>
<h1>Header</h1>
<p>Paragraph.</p>
        </configure>
    </property>
</entity>
}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.kernel.util.NamedObj">
    <property name="p" class="ptolemy.kernel.util.ConfigurableAttribute">
        <configure>
<h1>Header</h1>
<configure>yyy</configure>
<p>Paragraph.</p>
</configure>
    </property>
</entity>
}

set moml "$header $body"

test MoMLParser-11.3 {test configuration with embedded configure tag} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.util.NamedObj">
    <property name="p" class="ptolemy.kernel.util.ConfigurableAttribute">
        <configure>
<h1>Header</h1>
<configure>yyy</configure>
<p>Paragraph.</p>
</configure>
    </property>
</entity>
}

######################################################################
####
#
test MoMLParser-11.4 {test configuration value() method} {
    set attr [java::cast  ptolemy.kernel.util.ConfigurableAttribute \
             [$toplevel getAttribute p]]
    $attr value
} {
<h1>Header</h1>
<configure>yyy</configure>
<p>Paragraph.</p>
}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.kernel.util.NamedObj">
    <property name="p" class="ptolemy.kernel.util.ConfigurableAttribute">
        <configure source="test.xml">
<h1>Header</h1>
</configure>
    </property>
</entity>
}

set moml "$header $body"

test MoMLParser-11.5 {test configuration with external source} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.util.NamedObj">
    <property name="p" class="ptolemy.kernel.util.ConfigurableAttribute">
        <configure source="test.xml">
<h1>Header</h1>
</configure>
    </property>
</entity>
}

######################################################################
####
#
    set incMomlBase "$header
<entity name=\"top\" class=\"ptolemy.kernel.ComponentEntity\">
</entity>
"
test MoMLParser-12.1 {test rename} {
    $parser reset
    set toplevel [$parser parse $incMomlBase]
    $parser parse "<entity name=\".top\">
<rename name=\"foo\"/>
</entity>
"
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="foo" class="ptolemy.kernel.ComponentEntity">
</entity>
}

######################################################################
####
#
test MoMLParser-13.1 {test parse moml of ConfigurableAttribute} {
    set moml_1 "$header
<entity name=\"top\" class=\"ptolemy.kernel.CompositeEntity\">
</entity>
"
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml_1]
    $parser parse {
<entity name=".top">
    <property name="myAttribute" class="ptolemy.kernel.util.ConfigurableAttribute">
        <configure><?testML xxx ?></configure>
    </property>
</entity>
}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <property name="myAttribute" class="ptolemy.kernel.util.ConfigurableAttribute">
        <configure><?testML xxx ?></configure>
    </property>
</entity>
}

test MoMLParser-13.2 {test value method with parse moml} {
    # Uses 13.1 setup
    set e1 [java::cast ptolemy.kernel.util.ConfigurableAttribute [$toplevel getAttribute myAttribute]]
    $e1 value
} {<?testML xxx ?>}

test MoMLParser-13.3 {test with weird configure text} {
    # Uses 13.1 setup
    $parser reset
    set toplevel [$parser parse {
<entity name="top" class="ptolemy.kernel.util.NamedObj">
    <property name="myAttribute" class="ptolemy.kernel.util.ConfigurableAttribute">
<configure>
<svg>
  <rect x="0" y="0" width="20" height="20" style="fill:blue;stroke:green;stroke-width:30"/>
  <circle cx="0" cy="0" r="20" style="fill:blue;stroke:green;stroke-width:30"/>
  <ellipse cx="0" cy="0" rx="20" ry="30" style="fill:blue;stroke:green;stroke-width:30"/>
  <polygon points="10,30 50,10 50,30" style="fill:blue;stroke:green;stroke-width:30"/>
  <polyline points="10,30 50,10 50,30" style="stroke:green;stroke-width:30"/>
  <line x1="10" y1="20" x2="30" y2="40" style="stroke:green;stroke-width:30"/>
</svg>
</configure>
    </property>
</entity>
}]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.util.NamedObj">
    <property name="myAttribute" class="ptolemy.kernel.util.ConfigurableAttribute">
        <configure>
<svg>
  <rect x="0" y="0" width="20" height="20" style="fill:blue;stroke:green;stroke-width:30"></rect>
  <circle cx="0" cy="0" r="20" style="fill:blue;stroke:green;stroke-width:30"></circle>
  <ellipse cx="0" cy="0" rx="20" ry="30" style="fill:blue;stroke:green;stroke-width:30"></ellipse>
  <polygon points="10,30 50,10 50,30" style="fill:blue;stroke:green;stroke-width:30"></polygon>
  <polyline points="10,30 50,10 50,30" style="stroke:green;stroke-width:30"></polyline>
  <line x1="10" y1="20" x2="30" y2="40" style="stroke:green;stroke-width:30"></line>
</svg>
</configure>
    </property>
</entity>
}

test MoMLParser-13.4 {test with weird configure text containing escaped tags with no processing instruction} {
    # Uses 13.1 setup
    $parser reset
    set toplevel [$parser parse {
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="utilities" extends="ptolemy.moml.EntityLibrary">
      <configure>       
          <group>
    <!-- Blank composite actor. -->
    <entity name="actor" class="ptolemy.actor.TypedCompositeActor">
      <property name="ParamWithEscapedValue" class="ptolemy.data.expr.Parameter" value="&quot;hello&quot;"/>
    </entity>
          </group>
      </configure>
    </class>
</entity>
}]
    set attrib [$toplevel getAttribute "utilities.actor.ParamWithEscapedValue"]
    list [$toplevel exportMoML] [$attrib toString]
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="utilities" extends="ptolemy.moml.EntityLibrary">
        <configure>
            <group>
                <entity name="actor" class="ptolemy.actor.TypedCompositeActor">
                    <property name="ParamWithEscapedValue" class="ptolemy.data.expr.Parameter" value="&quot;hello&quot;">
                    </property>
                </entity>
            </group>
        </configure>
    </class>
</entity>
} {ptolemy.data.expr.Parameter {.top.utilities.actor.ParamWithEscapedValue} "hello"}}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <entity name="a" class="ptolemy.moml.test.testClass"/>
    <entity name="b" class="ptolemy.moml.test.testClass"/>
</entity>
}

set moml "$header $body"

test MoMLParser-1.14 {check that instance of a class defer to a common obj} {
    $parser reset
    set toplevel [java::cast ptolemy.kernel.CompositeEntity \
            [$parser parse $moml]]
    set a [$toplevel getEntity b]
    set b [$toplevel getEntity b]
    if [expr "\"[java::field [$b getMoMLInfo] deferTo]\"==\"[java::field [$a getMoMLInfo] deferTo]\""] {list {same}} {list {do not defer to the same master}}
    [java::field [$b getMoMLInfo] deferTo] equals \
            [java::field [$a getMoMLInfo] deferTo]
} {1}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.actor.CompositeActor">
    <port name="p" class="ptolemy.actor.IOPort"/>
    <relation name="r" class="ptolemy.actor.IORelation"/>
    <link port="p" relation="r" insertAt="0"/>
    <link port="p" insertInsideAt="0"/>
</entity>
}

set moml "$header $body"

test MoMLParser-15.1 {test link persistence in instatiation of a class} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.CompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2">
    </property>
    <port name="p" class="ptolemy.actor.IOPort">
    </port>
    <relation name="r" class="ptolemy.actor.IORelation">
    </relation>
    <link port="p" insertAt="1" relation="r"/>
</entity>
}


######################################################################
####
#

# This header has a bogus xml version number so that we can get
# XmlParser to call MoMLParser.error() 

set badHeader {<?xml version="-0.1" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">}

set moml_16 "$badHeader
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
    <doc>xxx</doc>
</entity>
"
test MoMLParser-16.1 {get XmlParser to call MoMLParser.error() by trying to parse a bogus xml version in the header} {
    $parser reset
    catch {$parser parse $moml_16} errMsg

    # Just get the first few characters
    string range $errMsg 0 90
} {com.microstar.xml.XmlException: unsupported XML version (found "-0.1") (expected "1.0") in }

######################################################################
####
#

test MoMLParser-17.1 {Call isModified and setModified} {
    # isModified and setModified are called by the filter code
    $parser reset
    set r1 [$parser isModified]
    $parser setModified 1
    set r2 [$parser isModified]
    # Resetting should set _modified back to false
    $parser reset
    set r3 [$parser isModified]
    list $r1 $r2 $r3
} {0 1 0}


######################################################################
####
#

test MoMLParser-18.1 {parse testdir.moml and get the filename of the inner part } {
    $parser reset
    # The list of filters is static, so we reset it in case there
    # filters were already added.
    $parser setMoMLFilters [java::null]
    $parser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    $parser addMoMLFilter [java::new \
	    ptolemy.moml.filter.RemoveGraphicalClasses]
    set toplevel [$parser parseFile "testdir.moml"]
    set compositeEntity [java::cast ptolemy.kernel.CompositeEntity $toplevel]
    set testdir2 [$compositeEntity getEntity testdir2]
    set uriAttribute [$testdir2 getAttribute _uri]
    # This will crap out if testdir/testdir2 does not have a _uri attribute
    set uri [[java::cast ptolemy.kernel.attributes.URIAttribute $uriAttribute] getURI]
    set uriString [$uri -noconvert toString] 
    $uriString endsWith "ptolemy/moml/test/testdir/testdir2.moml"
} {1}
