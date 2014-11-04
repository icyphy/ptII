# Tests for the MoMLParser class
#
# @Author: Edward A. Lee, Contributor: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1997-2014 The Regents of the University of California.
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

# Reset the parser just in case a previous test caused problems.
set parser [java::new ptolemy.moml.MoMLParser]
$parser resetAll
java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
java::call ptolemy.moml.filter.RemoveGraphicalClasses initialize

######################################################################
####
#
test MoMLParser-0.8 {parse text in two different workspaces} {
    set moml_1 "$header
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
    <doc>xxx</doc>
</entity>
"
    set w1 [java::new ptolemy.kernel.util.Workspace w1]
    set parser1 [java::new ptolemy.moml.MoMLParser $w1]
    set url1 [[java::new java.io.File toplevel.xml] toURL]
    set toplevel1 [$parser1 parse $moml_1]
    #$parser1 setContext $toplevel1

    set w2 [java::new ptolemy.kernel.util.Workspace w2]
    set parser2 [java::new ptolemy.moml.MoMLParser $w2]
    set url2 [[java::new java.io.File toplevel.xml] toURL]
    set toplevel2 [$parser2 parse $moml_1]
    #$parser2 setContext $toplevel2

    list "\n" [$toplevel1 getFullName] [[$parser1 getToplevel] getFullName] \
	[[$toplevel1 workspace] toString] "\n" \
	[$toplevel2 getFullName] [[$parser2 getToplevel] getFullName] \
	[[$toplevel2 workspace] toString]
} {{
} .top .top {ptolemy.kernel.util.Workspace {w1}} {
} .top .top {ptolemy.kernel.util.Workspace {w2}}}

######################################################################
####
#
test MoMLParser-0.9 {parse a file in two different workspaces.  Note use of setContext and purgeModelRecord} {
    set moml_1 "$header
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
    <doc>xxx</doc>
</entity>
"
    set w3 [java::new ptolemy.kernel.util.Workspace w3]
    set parser1 [java::new ptolemy.moml.MoMLParser $w3]
    set url1 [[java::new java.io.File toplevel.xml] toURL]
    set toplevel1 [$parser1 {parse java.net.URL java.net.URL} \
	[java::null] $url1]
    $parser1 setContext $toplevel1

    java::call ptolemy.moml.MoMLParser purgeModelRecord $url1

    set w4 [java::new ptolemy.kernel.util.Workspace w4]
    set parser2 [java::new ptolemy.moml.MoMLParser $w4]
    set url2 [[java::new java.io.File toplevel.xml] toURL]
    set toplevel2 [$parser2 {parse java.net.URL java.net.URL} \
	[java::null] $url2]
    $parser2 setContext $toplevel2

    list "\n" [$toplevel1 getFullName] [[$parser1 getToplevel] getFullName] \
	[[$toplevel1 workspace] toString] "\n" \
	[$toplevel2 getFullName] [[$parser2 getToplevel] getFullName] \
	[[$toplevel2 workspace] toString]
} {{
} .top .top {ptolemy.kernel.util.Workspace {w3}} {
} .top .top {ptolemy.kernel.util.Workspace {w4}}}

# call purgeModelRecord in case we run this twice
java::call ptolemy.moml.MoMLParser purgeModelRecord $url1
java::call ptolemy.moml.MoMLParser purgeModelRecord $url2

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
    list [$toplevel getFullName] [[$parser getToplevel] getFullName]
} {.top .top}

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
    # The input and output used to be the same, but now we escape characters
    # in the doc tag
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="3.1-devel">
    </property>
    <doc>&lt;?html &lt;H1&gt;HTML Markup&lt;/H1&gt;&lt;I&gt;italics&lt;/I&gt;.?&gt;</doc>
</entity>
}


######################################################################
####
#
set moml_2_1a [$toplevel exportMoML]

test MoMLParser-1.2.1a {reparse doc tag that has been escaped} {
    $parser reset
    set toplevel [$parser parse $moml_2_1a]
    $toplevel exportMoML
} $moml_2_1a


######################################################################
####
#
set moml_2_2 "$header
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
    <doc><H1>HTML Markup</H1><I>italics</I>.</doc>
</entity>
"

test MoMLParser-1.2.2 {parse simple model with HTML markup} {
    $parser reset
    set toplevel [$parser parse $moml_2_1]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="3.1-devel">
    </property>
    <doc>&lt;?html &lt;H1&gt;HTML Markup&lt;/H1&gt;&lt;I&gt;italics&lt;/I&gt;.?&gt;</doc>
</entity>
}


######################################################################
####
#
set moml_2_2a [$toplevel exportMoML]

test MoMLParser-1.2.2a {reparse doc tag that has been escaped} {
    $parser reset
    set toplevel [$parser parse $moml_2_2a]
    $toplevel exportMoML
} $moml_2_2a

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
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="3.1-devel">
    </property>
    <doc>&lt;H1&gt;HTML &lt;doc name=&quot;foo&quot;&gt;Markup&lt;/doc&gt;&lt;/H1&gt;&lt;I&gt;italics&lt;/I&gt;.</doc>
</entity>
}

######################################################################
####
#
set moml_2_3a [$toplevel exportMoML]

test MoMLParser-1.2.3a {reparse doc tag that has been escaped} {
    $parser reset
    set toplevel [$parser parse $moml_2_3a]
    $toplevel exportMoML
} $moml_2_3a

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
        <property name=\"init\" class=\"ptolemy.actor.parameters.PortParameter\" value=\"0\">
        </property>
    </entity>
</class>
"
# NOTE: result is not the same as what is parsed...
test MoMLParser-1.6 {test with a pre-existing parameter given, with class} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} $moml

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
        <property name="init" class="ptolemy.actor.parameters.PortParameter" value="1">
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
set moml11_2 {
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
   <link port="C" relation="C1.Rxx"/>
</entity>
}
test MoMLParser-1.11.2 {test link errors to cover LinkRequest.toString() } {
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser reset
    set recorderErrorHandler [java::new ptolemy.moml.test.RecorderErrorHandler]
    java::call ptolemy.moml.MoMLParser setErrorHandler $recorderErrorHandler

    set toplevel [$parser parse $moml11_2]
    java::call ptolemy.moml.MoMLParser setErrorHandler [java::null]
    # This used to fail because LevelCrossing Links had problems, now we
    # try to link to a non-existent relation
    list [string range [$recorderErrorHandler getMessages] 0 130]
} {{RecorderErrorHandler: Error encountered in:
link C to C1.Rxx
com.microstar.xml.XmlException: No relation named "C1.Rxx" in .foo in }}



######################################################################
####
#
set moml11_3 {
<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="foo" class="ptolemy.actor.TypedCompositeActor">
   <port name="C" class="ptolemy.actor.TypedIOPort"></port>
   <unlink port="C" relation="Foo"/>
</entity>
}
test MoMLParser-1.11.3 {test link errors to cover UnlinkRequest.toString() } {
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser reset
    set recorderErrorHandler [java::new ptolemy.moml.test.RecorderErrorHandler]
    java::call ptolemy.moml.MoMLParser setErrorHandler $recorderErrorHandler

    set toplevel [$parser parse $moml11_3]
    java::call ptolemy.moml.MoMLParser setErrorHandler [java::null]
    list [string range [$recorderErrorHandler getMessages] 0 124]
} {{RecorderErrorHandler: Error encountered in:
unlink C from Foo
com.microstar.xml.XmlException: No relation named "Foo" in .foo}}

test MoMLParser-1.11.2 {test topObjectsCreated, clearTopObjectsList} {
    $parser reset
    set toplevel [$parser parse $moml]
    set r1 [$parser topObjectsCreated]
    $parser clearTopObjectsList
    set r2 [$parser topObjectsCreated]
    list [java::isnull $r1] [java::isnull $r2]
} {1 0}


test MoMLParser-1.11.3 {Test link to self} {
    # Test for a situation that came up with the Kieler layout code
    $parser reset
    set modelBody {
	<entity name="toplevel2_18" class="ptolemy.actor.TypedCompositeActor">
	<relation name="r" class="ptolemy.actor.TypedIORelation"/>
	</entity>
    }
    set modelMoML "$header $modelBody"
    set toplevel2_18 [$parser parse $modelMoML]

    set manager [java::new ptolemy.actor.Manager [$toplevel2_18 workspace] "w"]
    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel2_18 $toplevel2_18 {
       <entity name=".toplevel2_18">
	<link relation1="r" relation2="r"/>
       </entity>
    }]
    # NOTE: Request is filled immediately because the model is not running.
    catch {$manager requestChange $change} errMsg

    list $errMsg
} {{ptolemy.kernel.util.InternalErrorException: ChangeRequest failed (NOTE: there is no ChangeListener):

       <entity name=".toplevel2_18">
	<link relation1="r" relation2="r"/>
       </entity>
    
  in .toplevel2_18
Because:
CrossRefLink.link: Illegal self-link.}}

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
    java::call ptolemy.moml.MoMLParser purgeAllModelRecords
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
    #string range $msg 0 33
    regsub {in file:[^ ]*} $msg {in file:xxx} msg2
    list [string range $msg2 0 189]
} {{com.microstar.xml.XmlException: XML element "entity" triggers exception. in file:xxx at line 7 and column 32
Caused by:
 ptolemy.kernel.util.IllegalActionException: Cannot find class: master}}

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
} {ptolemy.kernel.util.IllegalActionException: Cannot de}

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
} {ptolemy.kernel.util.IllegalActionException: Cannot de}

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
} {ptolemy.kernel.util.IllegalActionException: Cannot de}

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

test MoMLParser-1.18.6 {test illegal deletion in instatiation of a class} {
    $parser reset
    catch {set toplevel [$parser parse $moml]} msg
    string range $msg 0 52
} {ptolemy.kernel.util.IllegalActionException: Cannot de}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.actor.CompositeActor">
    <class name="master" extends="ptolemy.actor.CompositeActor">
        <port name="p" class="ptolemy.actor.IOPort"/>
    </class>
    <class name="derived" extends=".top.master">
        <relation name="r" class="ptolemy.actor.IORelation"/>
        <link port="p" relation="r"/>
    </class>
</entity>
}

set moml "$header $body"

test MoMLParser-1.18.7 {test link persistence in a subclass} {
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
    </class>
    <class name="derived" extends=".top.master">
        <relation name="r" class="ptolemy.actor.IORelation">
        </relation>
        <link port="p" relation="r"/>
    </class>
</entity>
}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.actor.CompositeActor">
    <class name="master" extends="ptolemy.actor.CompositeActor">
        <port name="p" class="ptolemy.actor.IOPort"/>
    </class>
    <entity name="derived" class=".top.master">
        <relation name="r" class="ptolemy.actor.IORelation"/>
        <link port="p" relation="r"/>
    </entity>
</entity>
}

set moml "$header $body"

test MoMLParser-1.18.8 {test link persistence in a an instance of a class} {
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
    </class>
    <entity name="derived" class=".top.master">
        <relation name="r" class="ptolemy.actor.IORelation">
        </relation>
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
        <link port="p" relation="r"/>        
    </class>
    <entity name="derived" class=".top.master">
        <unlink port="p" relation="r"/>
    </entity>
</entity>
}

set moml "$header $body"

test MoMLParser-1.18.9 {test illegal unlink in instatiation of a class} {
    $parser reset
    catch {set toplevel [$parser parse $moml]} msg
    string range $msg 0 52
} {ptolemy.kernel.util.IllegalActionException: Cannot un}

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
    <entity name="master" class="ptolemy.actor.CompositeActor">
        <relation name="r1" class="ptolemy.actor.IORelation"/>
        <relation name="r2" class="ptolemy.actor.IORelation"/>
        <link relation1="r1" relation2="r2"/>
        <unlink relation1="r1" relation2="r2"/>
    </entity>
</entity>
}

set moml "$header $body"

test MoMLParser-1.18.9.1.1 {test unlink with two relations} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.CompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="6.1.devel">
    </property>
    <entity name="master" class="ptolemy.actor.CompositeActor">
        <relation name="r1" class="ptolemy.actor.IORelation">
        </relation>
        <relation name="r2" class="ptolemy.actor.IORelation">
        </relation>
    </entity>
</entity>
}


######################################################################
####
#
set body {
<entity name="top" class="ptolemy.actor.CompositeActor">
    <entity name="master" class="ptolemy.actor.CompositeActor">
        <relation name="r1" class="ptolemy.actor.IORelation"/>
        <relation name="r2" class="ptolemy.actor.IORelation"/>
        <link relation2="r1" relation="r2"/>
    </entity>
</entity>}

set moml "$header $body"

test MoMLParser-1.18.9.1.2 {test unlink without relation1 relation2} {
    $parser reset
    catch {$parser parse $moml} errMsg
    list [string range $errMsg 0 67]
} {{com.microstar.xml.XmlException: Element link requires two relations.}}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.actor.CompositeActor">
    <entity name="master" class="ptolemy.actor.CompositeActor">
        <relation name="r1" class="ptolemy.actor.IORelation"/>
        <relation name="r2" class="ptolemy.actor.IORelation"/>
        <link relation2="r1" relation1="r2"/>
        <unlink relation1="r1" relation="r2"/>

    </entity>
</entity>}

set moml "$header $body"

test MoMLParser-1.18.9.1.3 {test link without relation1 relation2} {
    $parser reset
    catch {$parser parse $moml} errMsg
    list [string range $errMsg 0 69]
} {{com.microstar.xml.XmlException: Element unlink requires two relations.}}

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
    <class name="derived" extends=".top.master">
        <unlink port="p" relation="r"/>
    </class>
</entity>
}

set moml "$header $body"

test MoMLParser-1.18.9.2 {test illegal unlink in subclass} {
    $parser reset
    catch {set toplevel [$parser parse $moml]} msg
    string range $msg 0 52
} {ptolemy.kernel.util.IllegalActionException: Cannot un}


######################################################################
####
#
set body {
<entity name="top" class="ptolemy.actor.CompositeActor">
    <class name="master" extends="ptolemy.actor.CompositeActor">
        <port name="p" class="ptolemy.actor.IOPort"/>
    </class>
    <entity name="derived" class=".top.master">
        <relation name="r" class="ptolemy.actor.IORelation"/>
        <link port="p" relation="r"/>        
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
    </class>
    <entity name="derived" class=".top.master">
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
        <entity name="e" class="ptolemy.actor.AtomicActor">
            <port name="p" class="ptolemy.actor.IOPort"/>
        </entity>
    </class>
    <entity name="derived" class=".top.master">
        <relation name="r" class="ptolemy.actor.IORelation"/>
        <link port="e.p" relation="r"/>        
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
    </class>
    <entity name="derived" class=".top.master">
        <relation name="r" class="ptolemy.actor.IORelation">
        </relation>
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
	# Should succeed silently.
    set toplevel [$parser parse $incMoml_2_6]
    $toplevel getName
} {top}

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
		entity attribute, check silently succeeds if port name 
		not valid} {
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
    $parser parse $incMoml_2_8_2_b
    $toplevel getName
} {top}


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
test MoMLParser-3.1 {Test bizarre containment} {
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
    set toplevel [$parser parse $incMoml_3_1]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="a" class="ptolemy.actor.TypedCompositeActor">
        <entity name="c" class="ptolemy.actor.TypedCompositeActor">
        </entity>
    </entity>
    <entity name="b" class="ptolemy.actor.TypedCompositeActor">
    </entity>
</entity>
}

######################################################################
####
#
test MoMLParser-3.2 {Test more bizarre containment} {
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
    set toplevel [$parser parse $incMoml_3_2]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="a" class="ptolemy.actor.TypedCompositeActor">
        <entity name="b" class="ptolemy.actor.TypedCompositeActor">
        </entity>
    </entity>
</entity>
}

######################################################################
####
#
test MoMLParser-3.3 {Test more bizarre containment} {
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
    set toplevel [$parser parse $incMoml_3_3]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="a" class="ptolemy.actor.TypedCompositeActor">
        <entity name="b" class="ptolemy.actor.TypedCompositeActor">
        </entity>
    </entity>
</entity>
}

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
    set toplevel [$parser parse $incMoml_3_3]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <entity name="a" class="ptolemy.actor.TypedCompositeActor">
        <entity name="b" class="ptolemy.actor.TypedCompositeActor">
        </entity>
    </entity>
</entity>
}

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
test MoMLParser-5.3.1 {Delete a PortParameter} {
    set incMomlBase "$header
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
    <property name=\"MyPortParameter\"
              class=\"ptolemy.actor.parameters.PortParameter\"/>
    <port name=\"MyPortParameter\"
          class=\"ptolemy.actor.parameters.ParameterPort\"/>
</entity>
"
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $incMomlBase]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="6.1.devel">
    </property>
    <property name="MyPortParameter" class="ptolemy.actor.parameters.PortParameter">
    </property>
    <port name="MyPortParameter" class="ptolemy.actor.parameters.ParameterPort">
        <property name="input"/>
        <property name="defaultValue" class="ptolemy.data.expr.Parameter">
        </property>
    </port>
</entity>
}

######################################################################
####
#
test MoMLParser-5.3.2 {Test property deletion of a PortParameter - get both the PortParameter and the ParameterPort} {
    # uses 5.3.1 above
    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
	<entity name=".top"><deleteProperty name="MyPortParameter"/></entity>
    }]
    $change setUndoable true
    
    # NOTE: Request is filled immediately because the model is not running.
    $toplevel requestChange $change
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="6.1.devel">
    </property>
</entity>
}

######################################################################
####
#
test MoMLParser-5.3.3 {Test property deletion of a PortParameter - get both the PortParameter and the ParameterPort} {
    # Depends on 5.3.1 and 5.3.2
    set undochange [java::new ptolemy.kernel.undo.UndoChangeRequest $toplevel $toplevel]
    $toplevel requestChange $undochange
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="6.1.devel">
    </property>
    <property name="MyPortParameter" class="ptolemy.actor.parameters.PortParameter">
    </property>
    <port name="MyPortParameter" class="ptolemy.actor.parameters.ParameterPort">
        <property name="input"/>
        <property name="defaultValue" class="ptolemy.data.expr.Parameter">
        </property>
    </port>
</entity>
}

######################################################################
####
#
test MoMLParser-5.3.4 {Test property deletion of a ParameterPort - get both the PortParameter and the ParameterPort} {
    # uses 5.3.* above
    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
	<entity name=".top"><deletePort name="MyPortParameter"/></entity>
    }]
    $change setUndoable true
    
    # NOTE: Request is filled immediately because the model is not running.
    $toplevel requestChange $change
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="6.1.devel">
    </property>
</entity>
}

######################################################################
####
#
test MoMLParser-5.3.5 {Test undo of 5.3.4} {
    # Depends on 5.3.*
    set undochange [java::new ptolemy.kernel.undo.UndoChangeRequest $toplevel $toplevel]
    $toplevel requestChange $undochange
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="6.1.devel">
    </property>
    <property name="MyPortParameter" class="ptolemy.actor.parameters.PortParameter">
    </property>
    <port name="MyPortParameter" class="ptolemy.actor.parameters.ParameterPort">
        <property name="input"/>
        <property name="defaultValue" class="ptolemy.data.expr.Parameter">
        </property>
    </port>
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
    # Distributor uses ptolemy.vergil.icon.EditorIcon, so we need to remove it
    removeGraphicalClasses $parser
    set toplevel [java::cast ptolemy.actor.TypedCompositeActor \
            [$parser parse $incMomlBase]]
    # The width is explicitly specified since the filters are not used here
    $parser parse {
<entity name=".top">
<property name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
    <property name="iterations" value="1"/>
</property>
<entity name="source" class="ptolemy.actor.lib.Ramp"/>
<entity name="dist" class="ptolemy.actor.lib.Distributor"/>
<entity name="comm" class="ptolemy.actor.lib.Commutator"/>
<entity name="sink" class="ptolemy.actor.lib.Recorder"/>
<relation name="r1" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
</relation>
<relation name="r2" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
</relation>
<relation name="r3" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
</relation>
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
    <property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"3.1-devel\">
    </property>
</entity>
"
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.actor.TypedCompositeActor \
            [$parser parse $incMomlBase]]
		# The width is explicitly specified since the filters are not used here            
    $parser parse {
<entity name=".top">
<property name="dir" class="ptolemy.domains.sdf.kernel.SDFDirector">
    <property name="iterations" value="1"/>
</property>
<entity name="source" class="ptolemy.actor.lib.Ramp"/>
<entity name="dist" class="ptolemy.actor.lib.Distributor"/>
<entity name="comm" class="ptolemy.actor.lib.Commutator"/>
<entity name="sink" class="ptolemy.actor.lib.Recorder"/>
<relation name="r1" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
</relation>
<relation name="r2" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
</relation>
<relation name="r3" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
</relation>
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
    # The width is explicitly specified since the filters are not used here
    $parser parse {
<entity name=".top">
<relation name="r4" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
</relation>
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
<relation name="r5" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
</relation>
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
<relation name="r6" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
</relation>
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
         <property name="defaultValue" class="ptolemy.data.expr.Parameter">
        </property>
      </port>
   </entity>
   <entity name="C" class="ptolemy.actor.TypedAtomicActor">
      <port name="in" class="ptolemy.actor.TypedIOPort">
         <property name="input"/>
         <property name="defaultValue" class="ptolemy.data.expr.Parameter">
        </property>
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
        <vertex name="v1" value="{0.0, 0.0}">
        </vertex>
        <vertex name="v2" value="{0.0, 0.0}">
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
        <vertex name="v1" value="{0.0, 0.0}">
        </vertex>
        <vertex name="v2" value="{0.0, 0.0}">
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
<entity name="top" class="ptolemy.kernel.CompositeEntity">
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
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <property name="p" class="ptolemy.kernel.util.ConfigurableAttribute">
        <configure>xxx</configure>
    </property>
</entity>
}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.kernel.CompositeEntity">
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
<entity name="top" class="ptolemy.kernel.CompositeEntity">
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
<entity name="top" class="ptolemy.kernel.CompositeEntity">
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
<entity name="top" class="ptolemy.kernel.CompositeEntity">
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
<entity name="top" class="ptolemy.kernel.CompositeEntity">
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
<entity name="top" class="ptolemy.kernel.CompositeEntity">
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
<entity name="top" class="ptolemy.kernel.CompositeEntity">
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
<entity name="top" class="ptolemy.kernel.CompositeEntity">
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

test MoMLParser-14.1 {check that instance of a class defer to a common obj} {
    $parser reset
    set toplevel [java::cast ptolemy.kernel.CompositeEntity \
            [$parser parse $moml]]
    set a [$toplevel getEntity b]
    set b [$toplevel getEntity b]
    # %$(&$%* lame Jacl misunderstanding of OO...
    [java::cast java.lang.Object [$b getParent]] equals [$a getParent]
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
    set r1 [java::call ptolemy.moml.MoMLParser isModified]
    java::call ptolemy.moml.MoMLParser setModified 1
    set r2 [java::call ptolemy.moml.MoMLParser isModified]
    # Resetting should set _modified back to false
    $parser reset
    set r3 [java::call ptolemy.moml.MoMLParser isModified]
    list $r1 $r2 $r3
} {0 1 0}


######################################################################
####
#

test MoMLParser-18.1 {parse testdir.moml and get the filename of the inner part } {
    $parser reset
    # The list of filters is static, so we reset it in case there
    # filters were already added.
    java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
    java::call ptolemy.moml.MoMLParser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    java::call ptolemy.moml.MoMLParser addMoMLFilter [java::new \
	    ptolemy.moml.filter.RemoveGraphicalClasses]
    set toplevel [$parser parseFile "testdir.moml"]
    set compositeEntity [java::cast ptolemy.kernel.CompositeEntity $toplevel]
    set testdir2 [$compositeEntity getEntity testdir2]
    set deferredTo [java::cast ptolemy.kernel.util.NamedObj [$testdir2 getParent]]
    set uriAttribute [$deferredTo getAttribute _uri]
    # This will crap out if testdir/testdir2 does not have a _uri attribute
    set uri [[java::cast ptolemy.kernel.attributes.URIAttribute $uriAttribute] getURI]
    set uriString [$uri -noconvert toString] 

    # Convert %5C characters to /
    set uriString2 [$uriString -noconvert replaceAll "%5C" "/" ]

    $uriString2 endsWith "ptolemy/moml/test/testdir/testdir2.moml"
} {1}

######################################################################
######################################################################
######################################################################
######################################################################
######################################################################
#### The following tests are checking class mechanisms.
#

set baseModel {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <class name="BaseClass" extends="ptolemy.actor.TypedCompositeActor">
    </class>
    <class name="DerivedClass" extends="BaseClass">
        <entity name="A" class="ptolemy.actor.TypedAtomicActor"/>
    </class>
    <entity name="InstanceOfBaseClass" class="BaseClass">
    </entity>
    <entity name="InstanceOfDerivedClass" class="DerivedClass">
    </entity>
</entity>
}

######################################################################
####
#
test MoMLParser-20.1 {Test adding an entity to the base class.} {
    # Create a base model.
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.actor.CompositeActor \
            [$parser parse $baseModel]]
    set baseClass [$toplevel getEntity {BaseClass}]
    # NOTE: Have to have the class definition as the context.
    set change [java::new ptolemy.moml.MoMLChangeRequest $baseClass $baseClass {
        <entity name="B" class="ptolemy.actor.TypedAtomicActor"/>
    }]
    # NOTE: Request is filled immediately because the model is not running.
    $toplevel requestChange $change
    [[java::cast ptolemy.kernel.CompositeEntity [$toplevel getEntity {DerivedClass}]] getEntity {B}] getFullName
} {.top.DerivedClass.B}

test MoMLParser-20.2 {Make sure the derived class doesn't export the added entity.} {
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="3.1-devel">
    </property>
    <class name="BaseClass" extends="ptolemy.actor.TypedCompositeActor">
        <entity name="B" class="ptolemy.actor.TypedAtomicActor">
        </entity>
    </class>
    <class name="DerivedClass" extends="BaseClass">
        <entity name="A" class="ptolemy.actor.TypedAtomicActor">
        </entity>
    </class>
    <entity name="InstanceOfBaseClass" class="BaseClass">
    </entity>
    <entity name="InstanceOfDerivedClass" class="DerivedClass">
    </entity>
</entity>
}

######################################################################
####
#
test MoMLParser-20.2 {Test adding an entity in the subclass} {
    set derivedClass [$toplevel getEntity {DerivedClass}]
    # NOTE: Have to have the class definition as the context.
    set change [java::new ptolemy.moml.MoMLChangeRequest $derivedClass $derivedClass {
        <entity name="C" class="ptolemy.actor.TypedAtomicActor"/>
    }]
    # NOTE: Request is filled immediately because the model is not running.
    $toplevel requestChange $change
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="3.1-devel">
    </property>
    <class name="BaseClass" extends="ptolemy.actor.TypedCompositeActor">
        <entity name="B" class="ptolemy.actor.TypedAtomicActor">
        </entity>
    </class>
    <class name="DerivedClass" extends="BaseClass">
        <entity name="A" class="ptolemy.actor.TypedAtomicActor">
        </entity>
        <entity name="C" class="ptolemy.actor.TypedAtomicActor">
        </entity>
    </class>
    <entity name="InstanceOfBaseClass" class="BaseClass">
    </entity>
    <entity name="InstanceOfDerivedClass" class="DerivedClass">
    </entity>
</entity>
}

######################################################################
####
#
test MoMLParser-20.3 {Test adding an entity in the subclass that is already there in the base class} {
    set derivedClass [$toplevel getEntity {DerivedClass}]
    set change [java::new ptolemy.moml.MoMLChangeRequest $derivedClass $derivedClass {
        <entity name="B" class="ptolemy.actor.TypedAtomicActor"/>
    }]
    $toplevel requestChange $change
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="3.1-devel">
    </property>
    <class name="BaseClass" extends="ptolemy.actor.TypedCompositeActor">
        <entity name="B" class="ptolemy.actor.TypedAtomicActor">
        </entity>
    </class>
    <class name="DerivedClass" extends="BaseClass">
        <entity name="A" class="ptolemy.actor.TypedAtomicActor">
        </entity>
        <entity name="C" class="ptolemy.actor.TypedAtomicActor">
        </entity>
    </class>
    <entity name="InstanceOfBaseClass" class="BaseClass">
    </entity>
    <entity name="InstanceOfDerivedClass" class="DerivedClass">
    </entity>
</entity>
}

######################################################################
####
#
test MoMLParser-20.4 {Test deleting an entity from the base class} {
    set baseClass [$toplevel getEntity {BaseClass}]
    # NOTE: Have to have the class definition as the context.
    set change [java::new ptolemy.moml.MoMLChangeRequest $baseClass $baseClass {
        <deleteEntity name="B"/>
    }]
    $toplevel requestChange $change
    [java::cast ptolemy.kernel.CompositeEntity \
            [$toplevel getEntity {DerivedClass}]] getEntity {B}
} {java0x0}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.actor.CompositeActor">
    <class name="master" extends="ptolemy.actor.CompositeActor">
    </class>
    <entity name="derived" class="master">
        <property name="dir" class="ptolemy.actor.Director"/>
    </entity>
</entity>
}

set moml "$header $body"

test MoMLParser-20.5 {test relative class name} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.CompositeActor">
    <class name="master" extends="ptolemy.actor.CompositeActor">
    </class>
    <entity name="derived" class="master">
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
    <class name="derived" extends="master">
        <property name="dir" class="ptolemy.actor.Director"/>
    </class>
</entity>
}

set moml "$header $body"

test MoMLParser-20.6 {test relative class name for inner class} {
    $parser reset
    set toplevel [$parser parse $moml]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.CompositeActor">
    <class name="master" extends="ptolemy.actor.CompositeActor">
    </class>
    <class name="derived" extends="master">
        <property name="dir" class="ptolemy.actor.Director">
        </property>
    </class>
</entity>
}

######################################################################
#### Test inner classes
#

set baseModel {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="3.1-devel">
    </property>
    <class name="BaseClass" extends="ptolemy.actor.TypedCompositeActor">
        <class name="B" extends="ptolemy.actor.TypedCompositeActor">
        </class>
        <entity name="InstanceOfB" class="B">
        </entity>
    </class>
    <class name="DerivedClass" extends="BaseClass">
        <entity name="A" class="ptolemy.actor.TypedCompositeActor">
        </entity>
        <class name="B">
            <entity name="C" class="ptolemy.actor.TypedCompositeActor"/>
        </class>
    </class>
    <entity name="InstanceOfBaseClass" class="BaseClass">
    </entity>
    <entity name="InstanceOfDerivedClass" class="DerivedClass">
    </entity>
</entity>
}

######################################################################
####
#
test MoMLParser-20.7 {Overriding an inner class.} {
    # Create a base model.
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.actor.CompositeActor \
            [$parser parse $baseModel]]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="3.1-devel">
    </property>
    <class name="BaseClass" extends="ptolemy.actor.TypedCompositeActor">
        <class name="B" extends="ptolemy.actor.TypedCompositeActor">
        </class>
        <entity name="InstanceOfB" class="B">
        </entity>
    </class>
    <class name="DerivedClass" extends="BaseClass">
        <class name="B" extends="ptolemy.actor.TypedCompositeActor">
            <entity name="C" class="ptolemy.actor.TypedCompositeActor">
            </entity>
        </class>
        <entity name="A" class="ptolemy.actor.TypedCompositeActor">
        </entity>
    </class>
    <entity name="InstanceOfBaseClass" class="BaseClass">
    </entity>
    <entity name="InstanceOfDerivedClass" class="DerivedClass">
    </entity>
</entity>
}

######################################################################
####
#
test MoMLParser-20.8 {Test modification to the base inner class.} {
    # NOTE: Have to have the class definition as the context.
    set baseClass [$toplevel getEntity {BaseClass}]
    set change [java::new ptolemy.moml.MoMLChangeRequest $baseClass $baseClass {
        <class name="B"><entity name="D" class="ptolemy.actor.TypedCompositeActor"/></class>
    }]
    # NOTE: Request is filled immediately because the model is not running.
    $toplevel requestChange $change
    set instanceOfDerivedClass [$toplevel getEntity {InstanceOfDerivedClass.InstanceOfB}]
    list [$instanceOfDerivedClass getFullName] [$toplevel exportMoML]
} {.top.InstanceOfDerivedClass.InstanceOfB {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="3.1-devel">
    </property>
    <class name="BaseClass" extends="ptolemy.actor.TypedCompositeActor">
        <class name="B" extends="ptolemy.actor.TypedCompositeActor">
            <entity name="D" class="ptolemy.actor.TypedCompositeActor">
            </entity>
        </class>
        <entity name="InstanceOfB" class="B">
        </entity>
    </class>
    <class name="DerivedClass" extends="BaseClass">
        <class name="B" extends="ptolemy.actor.TypedCompositeActor">
            <entity name="C" class="ptolemy.actor.TypedCompositeActor">
            </entity>
        </class>
        <entity name="A" class="ptolemy.actor.TypedCompositeActor">
        </entity>
    </class>
    <entity name="InstanceOfBaseClass" class="BaseClass">
    </entity>
    <entity name="InstanceOfDerivedClass" class="DerivedClass">
    </entity>
</entity>
}}

######################################################################
#### Test order of deferral lists and delete and undo of entities.
#

set baseModel {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <class name="Ramp" extends="ptolemy.actor.lib.Ramp">
    </class>
    <class name="SubclassOfRamp" extends="Ramp">
    </class>
    <entity name="InstanceOfRamp" class="Ramp">
    </entity>
    <entity name="InstanceOfSubclassOfRamp" class="SubclassOfRamp">
    </entity>
    <relation name="relation" class="ptolemy.actor.TypedIORelation">
    </relation>
    <link port="InstanceOfRamp.output" relation="relation"/>
    <link port="InstanceOfSubclassOfRamp.trigger" relation="relation"/>
</entity>
}

######################################################################
####
#
test MoMLParser-21.1 {Order of heritage list.} {
    # Create a base model.
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.actor.CompositeActor \
            [$parser parse $baseModel]]
    set ramp [$toplevel getEntity Ramp]
    set derivedList [$ramp getDerivedList]
    listToFullNames $derivedList
} {.top.SubclassOfRamp .top.InstanceOfSubclassOfRamp .top.InstanceOfRamp}

test MoMLParser-21.2 {Delete entity.} {
    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <deleteEntity name="SubclassOfRamp"/>
    }]
    $change setUndoable true
    
    # NOTE: Request is filled immediately because the model is not running.
    $toplevel requestChange $change
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="7.2.devel">
    </property>
    <class name="Ramp" extends="ptolemy.actor.lib.Ramp">
    </class>
    <entity name="InstanceOfRamp" class="Ramp">
    </entity>
    <relation name="relation" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <link port="InstanceOfRamp.output" relation="relation"/>
</entity>
}

test MoMLParser-21.3 {Undo delete entity.} {
    set undochange [java::new ptolemy.kernel.undo.UndoChangeRequest $toplevel $toplevel]
    $toplevel requestChange $undochange
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="7.2.devel">
    </property>
    <class name="Ramp" extends="ptolemy.actor.lib.Ramp">
    </class>
    <class name="SubclassOfRamp" extends="Ramp">
    </class>
    <entity name="InstanceOfRamp" class="Ramp">
    </entity>
    <entity name="InstanceOfSubclassOfRamp" class="SubclassOfRamp">
    </entity>
    <relation name="relation" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <link port="InstanceOfRamp.output" relation="relation"/>
    <link port="InstanceOfSubclassOfRamp.trigger" relation="relation"/>
</entity>
}

test MoMLParser-21.4 {Delete base class.} {
    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <deleteEntity name="Ramp"/>
    }]
    $change setUndoable true
    
    # NOTE: Request is filled immediately because the model is not running.
    $toplevel requestChange $change
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="7.2.devel">
    </property>
    <relation name="relation" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
</entity>
}

test MoMLParser-21.5 {Undo delete base class.} {
    set undochange [java::new ptolemy.kernel.undo.UndoChangeRequest $toplevel $toplevel]
    $toplevel requestChange $undochange
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="7.2.devel">
    </property>
    <class name="Ramp" extends="ptolemy.actor.lib.Ramp">
    </class>
    <class name="SubclassOfRamp" extends="Ramp">
    </class>
    <entity name="InstanceOfRamp" class="Ramp">
    </entity>
    <entity name="InstanceOfSubclassOfRamp" class="SubclassOfRamp">
    </entity>
    <relation name="relation" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <link port="InstanceOfRamp.output" relation="relation"/>
    <link port="InstanceOfSubclassOfRamp.trigger" relation="relation"/>
</entity>
}


######################################################################
#### Test order of deferral lists and delete and undo of ports.
#

set baseModel {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <class name="Ramp" extends="ptolemy.actor.TypedAtomicActor">
    	<port name="trigger"><property name="input"/></port>
    	<port name="output"><property name="output"/></port>
    </class>
    <class name="SubclassOfRamp" extends="Ramp">
    </class>
    <entity name="InstanceOfRamp" class="Ramp">
    </entity>
    <entity name="InstanceOfSubclassOfRamp" class="SubclassOfRamp">
    </entity>
    <relation name="relation" class="ptolemy.actor.TypedIORelation">
    </relation>
    <link port="InstanceOfRamp.output" relation="relation"/>
    <link port="InstanceOfSubclassOfRamp.trigger" relation="relation"/>
</entity>
}

######################################################################
####
#
test MoMLParser-22.1 {Order of heritage list.} {
    # Create a base model.
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [java::cast ptolemy.actor.CompositeActor \
            [$parser parse $baseModel]]
    set port [$toplevel getPort Ramp.output]
    set derivedList [$port getDerivedList]
    listToFullNames $derivedList
} {.top.SubclassOfRamp.output .top.InstanceOfSubclassOfRamp.output .top.InstanceOfRamp.output}

test MoMLParser-22.2 {Delete port.} {
    set change [java::new ptolemy.moml.MoMLChangeRequest $toplevel $toplevel {
        <deletePort name="Ramp.output"/>
    }]
    $change setUndoable true
    
    # NOTE: Request is filled immediately because the model is not running.
    $toplevel requestChange $change
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="7.2.devel">
    </property>
    <class name="Ramp" extends="ptolemy.actor.TypedAtomicActor">
        <port name="trigger" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="defaultValue" class="ptolemy.data.expr.Parameter">
            </property>
        </port>
    </class>
    <class name="SubclassOfRamp" extends="Ramp">
    </class>
    <entity name="InstanceOfRamp" class="Ramp">
    </entity>
    <entity name="InstanceOfSubclassOfRamp" class="SubclassOfRamp">
    </entity>
    <relation name="relation" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <link port="InstanceOfSubclassOfRamp.trigger" relation="relation"/>
</entity>
}

test MoMLParser-22.3 {Undo delete port.} {
    set undochange [java::new ptolemy.kernel.undo.UndoChangeRequest $toplevel $toplevel]
    $toplevel requestChange $undochange
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="7.2.devel">
    </property>
    <class name="Ramp" extends="ptolemy.actor.TypedAtomicActor">
        <port name="trigger" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="defaultValue" class="ptolemy.data.expr.Parameter">
            </property>
        </port>
        <port name="output" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
        </port>
    </class>
    <class name="SubclassOfRamp" extends="Ramp">
    </class>
    <entity name="InstanceOfRamp" class="Ramp">
    </entity>
    <entity name="InstanceOfSubclassOfRamp" class="SubclassOfRamp">
    </entity>
    <relation name="relation" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="1">
        </property>
    </relation>
    <link port="InstanceOfRamp.output" relation="relation"/>
    <link port="InstanceOfSubclassOfRamp.trigger" relation="relation"/>
</entity>
}

######################################################################
####
#
set body {
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <class name="a" extends="ptolemy.kernel.CompositeEntity">
        <entity name="d" class="Not.A.Class">
        </entity>
    </class>
</entity>
}

set moml "$header $body"
test MoMLParser-23.1 {ClassNotFound} {
    $parser reset
    catch {set toplevel [$parser parse $moml]} msg
    regsub -all {in file:[^ ]*} $msg {in file:xxx} msg2
    list [string range $msg2 0 194]
} {{com.microstar.xml.XmlException: XML element "entity" triggers exception. in file:xxx at line 6 and column 35
Caused by:
 ptolemy.kernel.util.IllegalActionException: Cannot find class: Not.A.Class}}

test MoMLParser-24.1 {purgeModelRecord} {
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser reset
    # We parse the same file three times and substitute new contents
    # after the first time

    # Parse it once with data from p1.moml, which creates .p1
    file delete -force purgeModelRecordTest.moml

    # Make sure that the file does not exist
    set r0 [file exists	purgeModelRecordTest.moml]

    file copy -force p1.moml purgeModelRecordTest.moml

    set url1 [[java::new java.io.File purgeModelRecordTest.moml] toURL]
    set toplevel1 [$parser {parse java.net.URL java.net.URL} \
	[java::null] $url1]

    # This is .p1
    set r1 [$toplevel1 getFullName]

    # Update the file contents, note that toplevel2 is the same
    # as toplevel1 because we have not called purgeModelRecord.
    file delete -force purgeModelRecordTest.moml

    # Make sure that the file does not exist.
    set r2 [file exists	purgeModelRecordTest.moml]

    file copy -force p2.moml purgeModelRecordTest.moml

    set toplevel2 [$parser {parse java.net.URL java.net.URL} \
	[java::null] $url1]
    # I would expect that this is .p2, but we have not yet purged and reset.
    set r3 [$toplevel2 getFullName]

    java::call ptolemy.moml.MoMLParser purgeModelRecord $url1
    $parser reset
    # Need to purge the record and reset to see the change

    set toplevel3 [$parser {parse java.net.URL java.net.URL} \
	[java::null] $url1]
    set r4 [$toplevel3 getFullName]

    # Do a little cleanup	
    java::call ptolemy.moml.MoMLParser purgeModelRecord $url1
    $parser reset
    file delete -force purgeModelRecordTest.moml


    list $r0 $r1 $r2 $r3 $r4
} {0 .p1 0 .p1 .p2}

######################################################################
####
#
set moml_25 "$classheader
<class name=\"testUserActorLibrary_OK_2_DELETE\" extends=\"ptolemy.moml.EntityLibrary\">
    <configure>
        <group>
           <class name=\"classl25\"
                  extends=\"ptolemy.actor.TypedCompositeActor\">
               <doc>myClass25</doc>
               <property name=\"DocViewerAttribute\" class=\"ptolemy.kernel.util.SingletonAttribute\">
                   <property name=\"_hideName\" class=\"ptolemy.data.expr.Parameter\" value=\"true\">
                   </property>
               </property>
           </class>
        </group>
    </configure>
</class>
"

test MoMLParser-25.1 {Replicated the problems I was having with adding a Sinewave to the user library.  The problem was that Sinewave had a _hideName Parameter that was set to true.  Instead, hideName should be a SingletonAttribute.  This test replicates that} {
    $parser reset
    set recorderErrorHandler [java::new ptolemy.moml.test.RecorderErrorHandler]
    java::call ptolemy.moml.MoMLParser setErrorHandler $recorderErrorHandler
    set toplevel [$parser parse $moml_25]

    # We need a ChangeListener at the top
    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
            {java.io.PrintStream java.io.OutputStream} $stream]
    set listener [java::new ptolemy.kernel.util.StreamChangeListener \
	    $printStream]


    $toplevel addChangeListener $listener

    set change [java::new ptolemy.moml.MoMLChangeRequest \
		    $toplevel $toplevel {
    <entity name="CompositeActor" class="ptolemy.actor.TypedCompositeActor">
        <property name="DocViewerAttribute" class="ptolemy.kernel.util.SingletonAttribute">
            <property name="viewer" class="ptolemy.kernel.util.Attribute">
            </property>
            <property name="_hideName" class="ptolemy.data.expr.Parameter" value="true">
            </property>
        </property>
    </entity>
		    }]
    # This now works because the filter is smarter.

    # It used to fail because _hideName gets converted into a SingletonAttribute
    # by the filter and cannot be set twice	

    catch {$toplevel requestChange $change} errMsg1

    list $errMsg1 \
	    [$stream toString] \
	    [$recorderErrorHandler getMessages]
} {{} {StreamChangeRequest.changeExecuted(): 
    <entity name="CompositeActor" class="ptolemy.actor.TypedCompositeActor">
        <property name="DocViewerAttribute" class="ptolemy.kernel.util.SingletonAttribute">
            <property name="viewer" class="ptolemy.kernel.util.Attribute">
            </property>
            <property name="_hideName" class="ptolemy.data.expr.Parameter" value="true">
            </property>
        </property>
    </entity>
		     succeeded
} {}}


######################################################################
####
#
test MoMLParser-26.1 {changeExecuted} {
    set w26 [java::new ptolemy.kernel.util.Workspace w26]
    set parser26 [java::new ptolemy.moml.MoMLParser $w26]
    # changeExecuted does nothing
    $parser26 changeExecuted [java::null]
} {}

######################################################################
####
#
test MoMLParser-26.2 {changeFailed} {
    # Uses MoMLParser26.1 above
    set toplevel26 [java::new ptolemy.kernel.CompositeEntity $w26]
    set change26 [java::new ptolemy.moml.MoMLChangeRequest \
		    $toplevel26 $toplevel26 {
        <entity name="B26" class="ptolemy.actor.TypedAtomicActor"/>
    }]

    set recorderErrorHandler [java::new ptolemy.moml.test.RecorderErrorHandler]
    java::call ptolemy.moml.MoMLParser setErrorHandler $recorderErrorHandler
    $parser26 changeFailed $change26 [java::new Exception {MoMLParser26.1 testException}]
    java::call ptolemy.moml.MoMLParser setErrorHandler [java::null]
    
    jdkCaptureErr {
	$parser26 changeFailed $change26 [java::new Exception {MoMLParser26.1 testException}]
    } errMsg
    list [string range [$recorderErrorHandler getMessages] 0 73] \
	[string range $errMsg 0 48]

} {{RecorderErrorHandler: Error encountered in:
ptolemy.moml.MoMLChangeRequest} {java.lang.Exception: MoMLParser26.1 testException}}

######################################################################
####
#
test MoMLParser-27.1 {getIconLoader(), setIconLoader()} {
    set w27 [java::new ptolemy.kernel.util.Workspace w27]
    set parser27 [java::new ptolemy.moml.MoMLParser $w27]
    $parser27 reset
    list [java::isnull [java::call ptolemy.moml.MoMLParser getIconLoader]]
} {1}

######################################################################
####
#

set body28 {
<entity name="top" class="ptolemy.kernel.CompositeEntity">
    <entity name="b" class="ptolemy.moml.test.testClass">
        <property name="prop" value="1"/>
    </entity>
<property name="xxx"/>
</entity>
}

set moml28 "$header $body28"

test MoMLParser-28.1 {setIconLoader()} {
    # Get a little better coverage of _loadIconForClass()
    set w28 [java::new ptolemy.kernel.util.Workspace w28]
    set parser28 [java::new ptolemy.moml.MoMLParser $w28]
    $parser28 reset
    java::call ptolemy.moml.MoMLParser setIconLoader [java::new ptolemy.moml.test.TestIconLoader]
    set toplevel28 [java::cast ptolemy.kernel.CompositeEntity \
            [$parser28 parse $moml28]]
    list \
	[[java::call ptolemy.moml.MoMLParser getIconLoader] loadIconForClass \
	     ptolemy.moml.test.testClass $toplevel28] \
	[[java::call ptolemy.moml.MoMLParser getIconLoader] loadIconForClass testClassFoo $toplevel28]
} {1 0}

# Reset the iconLoader in case we run this twice
java::call ptolemy.moml.MoMLParser setIconLoader [java::null]

set moml29 "$header $body28"
test MoMLParser-29.1 {The contents of the icon should not be in the exported Moml} {

    set w29 [java::new ptolemy.kernel.util.Workspace w29]
    set parser29 [java::new ptolemy.moml.MoMLParser $w29]
    $parser29 reset
    set toplevel29 [java::cast ptolemy.kernel.CompositeEntity \
            [$parser29 parse $moml29]]

   # a blank model and drag a MobileModel into it, the contents of
   # MobileModelIcon.xml is visible when we export the MoML.

   # This test is an effort to replicate that bug.
   # It exports the MoML from above and looks for the "height"
   # parameter, which is in testClassIcon.xml  	

   set moml [$toplevel29 exportMoML]
   regexp {<.*height.*>} "$moml" results

   # Should be empty, should not contain the contents of testClassIcon.xml
   list $results
} {{}}


test MoMLParser-31.1 {Make sure that the error message refers to the proper file} {
    $parser reset
    set file [java::new java.io.File NonexistantDirectorTest.xml] 
    set url [[$file toURI] toURL]
    catch {$parser {parse java.net.URL java.net.URL} [java::null] $url} errMsg
    java::call ptolemy.moml.MoMLParser purgeModelRecord $url
    # The error message used to refer to http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd

    string range $errMsg 0 82
} {com.microstar.xml.XmlException: Failed to find class 'ptolemy.domains.DoesNotExist.}

# The list of filters is static, so we reset it
java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
 

test MoMLParser-32.1 {If we read in a file that refers to a second file and the second file uses a missing class, make sure that the exception makes sense} {
    $parser reset
    catch {[$parser parseFile "AltFileNameExceptionTest.xml"]} errMsg
    regsub -all {file:/.*/ptolemy/moml/test} [string range $errMsg 0 502] {file:/XXX/ptolemy/moml/test} result
    set result1 {com.microstar.xml.XmlException: XML element "entity" triggers exception. in file:/XXX/ptolemy/moml/test/ at line 5 and column 70
Caused by:
 ptolemy.kernel.util.IllegalActionException: Cannot find class: ptolemy.moml.test.AltFileNameExceptionTestFile
Because:
XML element "class" triggers exception. in file:/XXX/ptolemy/moml/test/AltFileNameExceptionTestFile.xml at line 4 and column 47
Caused by:
 ptolemy.kernel.util.IllegalActionException: Cannot find class: NotAClass
}
    set result2 {com.microstar.xml.XmlException: XML element "entity" triggers exception. in file:/XXX/ptolemy/moml/test/ at line 5 and column 70
Caused by:
 ptolemy.kernel.util.IllegalActionException: Cannot find class: ptolemy.moml.test.AltFileNameExceptionTestFile
Because:
XML element "class" triggers exception. in file:/XXX/ptolemy/moml/test/AltFileNameExceptionTestFile.xml at line 4 and column 47
Caused by:
 ptolemy.kernel.util.IllegalActionException: Cannot find class: NotAClass
Because:}

    set result3 {com.microstar.xml.XmlException: XML element "entity" triggers exception. in file:/XXX/ptolemy/moml/test/ at line 5 and column 70
Caused by:
 ptolemy.kernel.util.IllegalActionException: Cannot find class: ptolemy.moml.test.AltFileNameExceptionTestFile
Because:
XML element "class" triggers exception. in file:/XXX/ptolemy/moml/test/AltFileNameExceptionTestFile.xml at line 4 and column 47
Caused by:
 ptolemy.ker}

    set result4 {com.microstar.xml.XmlException: XML element "entity" triggers exception. in file:/XXX/ptolemy/moml/test/ at line 5 and column 70
Caused by:
 ptolemy.kernel.util.IllegalActionException: Cannot find class: ptolemy.moml.test.AltFileNameExceptionTestFile
Because:
XML element "class" triggers exception. in file:/XXX/ptolemy/moml/test/AltFileNameExceptionTestFile.xml at line 4 and column 47
Caused by:
 ptolemy.kernel.util.IllegalActionException: Cannot find class: NotAClass
Because:
-}

    set result5 {com.microstar.xml.XmlException: XML element "entity" triggers exception. in file:/XXX/ptolemy/moml/test/ at line 5 and column 70
Caused by:
 ptolemy.kernel.util.IllegalActionException: Cannot find class: ptolemy.moml.test.AltFileNameExceptionTestFile
Because:
XML element "class" triggers exception. in file:/XXX/ptolemy/moml/test/AltFileNameExceptionTestFile.xml at line 4 and column 47
Caused by:
 ptolemy.kernel.util.IllegalActionException: Cannot find c}

    set result6 {com.microstar.xml.XmlException: XML element "entity" triggers exception. in file:/XXX/ptolemy/moml/test/ at line 5 and column 70
Caused by:
 ptolemy.kernel.util.IllegalActionException: Cannot find class: ptolemy.moml.test.AltFileNameExceptionTestFile
Because:
XML element "class" triggers exception. in file:/XXX/ptolemy/moml/test/AltFileNameExceptionTestFile.xml at line 4 and column 47
Caused by:
 ptolemy.kernel.util.IllegalActionException: Cannot find class: }

    if { $result != $result1 && $result != $result2 && $result != $result3 && $result != $result4 && $result != $result5 && $result != $result6
	 && [string first $result $result1] == -1
	 && [string first $result $result2] == -1
	 && [string first $result $result3] == -1
	 && [string first $result $result4] == -1
	 && [string first $result $result5] == -1
	 && [string first $result $result6] == -1
	 && [string first $result1 $result] == -1
	 && [string first $result2 $result] == -1
	 && [string first $result3 $result] == -1
	 && [string first $result4 $result] == -1
	 && [string first $result5 $result] == -1
	 && [string first $result6 $result] == -1 } {
	error "--start--\n$result\n--end--\n\nwas not equal to\n\n--start#1--\n$result1\n--end--\n\nnor\n--start#2---\n$result2\n--end--\n\nor\n\n--start#3---\n$result3\n--end--\n\n or \n\n--start#4--\n$result4\n--end--\n\n or \n\n--start#4--\n$result5\n--end--\n or \n\n--start#6--\n$result6\n--end--\n\n"
    }
} {}

test MoMLParser-32.2 {parse a file with an relative name} {
    java::call ptolemy.moml.MoMLParser purgeAllModelRecords
    $parser reset
    # This assumes that $PTII is a relative path
    set relativePath [file join $PTII ptolemy/moml/test/ testdir.moml]
    set toplevel [$parser parseFile $relativePath]
    list [$toplevel getFullName] 
} {.testdir}

test MoMLParser-32.3 {parse a file with an absolute name} {
    java::call ptolemy.moml.MoMLParser purgeAllModelRecords
    $parser reset
    # This assumes that $PTII is a relative path
    set relativePath [file join $PTII ptolemy/moml/test/ testdir.moml]
    set absolutePath [[java::new java.io.File $relativePath] getCanonicalPath]
    set toplevel [$parser parseFile $absolutePath]
    list [$toplevel getFullName] 
} {.testdir}

test MoMLParser-32.4 {parse a file that does not exist} {
    java::call ptolemy.moml.MoMLParser purgeAllModelRecords
    $parser reset
    # This assumes that $PTII is a relative path
    catch {$parser parseFile "/This File Does Not Exist/Foo.xml"} errMsg
    set cwd [java::call System getProperty {user.dir}]
    # Sigh.  user.dir might be c:\ptII, but the error msg might be C:/ptII
    set cwdUpCased [[[[java::new java.io.File $cwd] getCanonicalFile] toURI] getPath]
    set cwdUpCased [string range $cwdUpCased 0 [expr {[string length $cwdUpCased] - 2}]]
    regsub -all {\\} $errMsg {/} errMsg2
    regsub $cwdUpCased $errMsg2 {XXXCWDXXX} errMsg3
    # Under windows, we might have a leading slash
    regsub [string range $cwdUpCased 1 [string length $cwdUpCased]] $errMsg3 {XXXCWDXXX} errMsg4
    list $errMsg4
} {{java.io.FileNotFoundException: Could not find file "/This File Does Not Exist/Foo.xml", also tried "XXXCWDXXX/This File Does Not Exist/Foo.xml"}}

set inputTopTest {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="inputTestTop" class="ptolemy.actor.TypedCompositeActor">
  <entity name="a" class="ptolemy.actor.TypedCompositeActor">
    <input source="inputTestB.xml"/>
  </entity>
</entity> }

test MoMLParser-33.1 {Use the input statement} {
    $parser reset
    set toplevel [$parser parse $inputTopTest]
    set r [$toplevel exportMoML]
    list $r
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="inputTestTop" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="8.1.devel">
    </property>
    <entity name="a" class="ptolemy.actor.TypedCompositeActor">
        <entity name="inputTestB" class="ptolemy.actor.TypedCompositeActor">
            <entity name="B" class="ptolemy.actor.TypedCompositeActor">
            </entity>
        </entity>
    </entity>
</entity>
}}

set inputTopTest_NotAFile {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="inputTestTop" class="ptolemy.actor.TypedCompositeActor">
  <entity name="a" class="ptolemy.actor.TypedCompositeActor">
    <input source="notAFile.xml"/>
  </entity>
</entity> }

test MoMLParser-33.2 {Use the input statement on a non-existant file} {
    $parser reset
    catch {set toplevel [$parser parse $inputTopTest_NotAFile]} errMsg
    regsub -all -- {-- .*/ptolemy/moml/test} $errMsg {-- XXX/ptolemy/moml/test} result
    list [string range $result 0 176]
} {{com.microstar.xml.XmlException: -- XXX/ptolemy/moml/test/notAFile.xml (No such file or directory)
-- XML file not found relative to classpath.
-- XXX/ptolemy/moml/test/notAFile.}}

set inputTopTest_NoSource {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="inputTestTop" class="ptolemy.actor.TypedCompositeActor">
  <entity name="a" class="ptolemy.actor.TypedCompositeActor">
    <input/>
  </entity>
</entity> }

test MoMLParser-33.3 {input statement with no source} {
    $parser reset
    catch {set toplevel [$parser parse $inputTopTest_NoSource]} errMsg
    regsub -all {file:/.*/ptolemy/moml/test} $errMsg {file:/XXX/ptolemy/moml/test} result
    list $result
} {{com.microstar.xml.XmlException: No source for element "input" in file:/XXX/ptolemy/moml/test/ at line 6 and column 10}}

######################################################################
####
# 
test MoMLParser-34.0 {Test parsing names that have xml chars in them} {
    # See http://chess.eecs.berkeley.edu/ptolemy/listinfo/ptolemy/2010-April/011999.html
    set entity [java::new ptolemy.kernel.Entity "This name has xml < = >"]
    set moml [$entity exportMoML]
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml]
    set r1 [$toplevel exportMoML]
    set token [java::new ptolemy.data.XMLToken $moml]
    list $r1 [$token toString]
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="This name has xml &lt; = &gt;" class="ptolemy.kernel.Entity">
</entity>
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="This name has xml &lt; = &gt;" class="ptolemy.kernel.Entity">
</entity>
}}

######################################################################
####
# 
test MoMLParser-34.1 {Test parsing parameter names that have xml chars in them} {
    # See http://chess.eecs.berkeley.edu/ptolemy/listinfo/ptolemy/2010-April/011999.html
    set ws [java::new ptolemy.kernel.util.Workspace workspace]
    set entity [java::new ptolemy.kernel.CompositeEntity $ws]
    set tok [java::new  {ptolemy.data.DoubleToken double} 4.5]
    set parameter [java::new ptolemy.data.expr.Parameter $entity "This name has xml < = >" $tok]
    set moml [$entity exportMoML]
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml]
    set r1 [$toplevel exportMoML]
    set token [java::new ptolemy.data.XMLToken $moml]
    list $r1 [$token toString]
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="" class="ptolemy.kernel.CompositeEntity">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="8.1.devel">
    </property>
    <property name="This name has xml &lt; = &gt;" class="ptolemy.data.expr.Parameter" value="4.5">
    </property>
</entity>
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="" class="ptolemy.kernel.CompositeEntity">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="8.1.devel">
    </property>
    <property name="This name has xml &lt; = &gt;" class="ptolemy.data.expr.Parameter" value="4.5">
    </property>
</entity>
}}

######################################################################
####
# 
test MoMLParser-34.2 {Test parsing CompositeEntity and StringAttribute names that have xml chars in them} {
    # See http://chess.eecs.berkeley.edu/ptolemy/listinfo/ptolemy/2010-April/011999.html

    set a [java::new ptolemy.kernel.CompositeEntity]
    $a setName "An entity name that has xml < = >"
    set b [java::new ptolemy.kernel.CompositeEntity $a "Another entity with < = >"]
    set p1 [java::new ptolemy.kernel.ComponentPort $a "A port with xml < = >"]
    set p2 [java::new ptolemy.kernel.ComponentPort $b P2]
    $a connect $p2 $p1

    set moml [$a exportMoML]
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml]
    set r1 [$toplevel exportMoML]
    set token [java::new ptolemy.data.XMLToken $moml]
    list $r1 [$token toString]
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="An entity name that has xml &lt; = &gt;" class="ptolemy.kernel.CompositeEntity">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="8.1.devel">
    </property>
    <port name="A port with xml &lt; = &gt;" class="ptolemy.kernel.ComponentPort">
    </port>
    <entity name="Another entity with &lt; = &gt;" class="ptolemy.kernel.CompositeEntity">
        <port name="P2" class="ptolemy.kernel.ComponentPort">
        </port>
    </entity>
    <relation name="_R" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <link port="A port with xml &lt; = &gt;" relation="_R"/>
    <link port="Another entity with &lt; = &gt;.P2" relation="_R"/>
</entity>
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="An entity name that has xml &lt; = &gt;" class="ptolemy.kernel.CompositeEntity">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="8.1.devel">
    </property>
    <port name="A port with xml &lt; = &gt;" class="ptolemy.kernel.ComponentPort">
    </port>
    <entity name="Another entity with &lt; = &gt;" class="ptolemy.kernel.CompositeEntity">
        <port name="P2" class="ptolemy.kernel.ComponentPort">
        </port>
    </entity>
    <relation name="_R" class="ptolemy.kernel.ComponentRelation">
    </relation>
    <link port="A port with xml &lt; = &gt;" relation="_R"/>
    <link port="Another entity with &lt; = &gt;.P2" relation="_R"/>
</entity>
}}


######################################################################
####
# 
test MoMLParser-34.3 {Test parsing CompositeEntity with level crossing links that has port names that have xml chars in them} {
    # See http://chess.eecs.berkeley.edu/ptolemy/listinfo/ptolemy/2010-April/011999.html

    # This structure is the example in the kernel design document.

    set e0 [java::new ptolemy.kernel.CompositeEntity]
    $e0 setName "E0<=>\"&\n\r"

    set e3 [java::new ptolemy.kernel.CompositeEntity $e0 "E3<=>"]
    set e4 [java::new ptolemy.kernel.CompositeEntity $e3 "E4<=>"]

    set e1 [java::new ptolemy.kernel.ComponentEntity $e4 "E1<=>&\""]
    set e2 [java::new ptolemy.kernel.ComponentEntity $e4 "E2<=>"]


    set e6 [java::new ptolemy.kernel.ComponentEntity $e3 "E6<=>&"]


    set p3 [java::cast ptolemy.kernel.ComponentPort [$e2 newPort "P3 with < = > "]]

    set p6 [java::cast ptolemy.kernel.ComponentPort [$e6 newPort "P6 with < = > "]]

    $e3 allowLevelCrossingConnect true
    set r6 [$e3 connect $p3 $p6 "R6 with < = >"]

    set moml [$e0 exportMoML]
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml]
    set r1 [$toplevel exportMoML]
    set token [java::new ptolemy.data.XMLToken $moml]
    list $r1 [$token toString]
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="E0&lt;=&gt;&quot;&amp;&#10;&#13;" class="ptolemy.kernel.CompositeEntity">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="8.1.devel">
    </property>
    <entity name="E3&lt;=&gt;" class="ptolemy.kernel.CompositeEntity">
        <entity name="E4&lt;=&gt;" class="ptolemy.kernel.CompositeEntity">
            <entity name="E1&lt;=&gt;&amp;&quot;" class="ptolemy.kernel.ComponentEntity">
            </entity>
            <entity name="E2&lt;=&gt;" class="ptolemy.kernel.ComponentEntity">
                <port name="P3 with &lt; = &gt; " class="ptolemy.kernel.ComponentPort">
                </port>
            </entity>
        </entity>
        <entity name="E6&lt;=&gt;&amp;" class="ptolemy.kernel.ComponentEntity">
            <port name="P6 with &lt; = &gt; " class="ptolemy.kernel.ComponentPort">
            </port>
        </entity>
        <relation name="R6 with &lt; = &gt;" class="ptolemy.kernel.ComponentRelation">
        </relation>
        <link port="E6&lt;=&gt;&amp;.P6 with &lt; = &gt; " relation="R6 with &lt; = &gt;"/>
        <link port="E4&lt;=&gt;.E2&lt;=&gt;.P3 with &lt; = &gt; " insertAt="0" relation="R6 with &lt; = &gt;"/>
    </entity>
</entity>
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="E0&lt;=&gt;&quot;&amp;&#10;&#13;" class="ptolemy.kernel.CompositeEntity">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="8.1.devel">
    </property>
    <entity name="E3&lt;=&gt;" class="ptolemy.kernel.CompositeEntity">
        <entity name="E4&lt;=&gt;" class="ptolemy.kernel.CompositeEntity">
            <entity name="E1&lt;=&gt;&amp;&quot;" class="ptolemy.kernel.ComponentEntity">
            </entity>
            <entity name="E2&lt;=&gt;" class="ptolemy.kernel.ComponentEntity">
                <port name="P3 with &lt; = &gt; " class="ptolemy.kernel.ComponentPort">
                </port>
            </entity>
        </entity>
        <entity name="E6&lt;=&gt;&amp;" class="ptolemy.kernel.ComponentEntity">
            <port name="P6 with &lt; = &gt; " class="ptolemy.kernel.ComponentPort">
            </port>
        </entity>
        <relation name="R6 with &lt; = &gt;" class="ptolemy.kernel.ComponentRelation">
        </relation>
        <link port="E6&lt;=&gt;&amp;.P6 with &lt; = &gt; " relation="R6 with &lt; = &gt;"/>
        <link port="E4&lt;=&gt;.E2&lt;=&gt;.P3 with &lt; = &gt; " insertAt="0" relation="R6 with &lt; = &gt;"/>
    </entity>
</entity>
}}

######################################################################
####
# 
test MoMLParser-34.4 {Test parsing a FilePortParameter with have xml chars in them} {
    # See http://chess.eecs.berkeley.edu/ptolemy/listinfo/ptolemy/2010-April/011999.html

    set a [java::new ptolemy.actor.TypedCompositeActor]
    set filePortParameter [java::new ptolemy.actor.parameters.FilePortParameter $a "id1<=>&"]
    set parameterPort [java::new ptolemy.actor.parameters.ParameterPort $a "id2<=>&"]

    set moml [$a exportMoML]
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml]
    set r1 [$toplevel exportMoML]
    set token [java::new ptolemy.data.XMLToken $moml]
    list $r1 [$token toString]
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="10.0.devel">
    </property>
    <property name="id1&lt;=&gt;&amp;" class="ptolemy.actor.parameters.FilePortParameter" value="">
    </property>
    <port name="id1&lt;=&gt;&amp;" class="ptolemy.actor.parameters.ParameterPort">
        <property name="input"/>
        <property name="defaultValue" class="ptolemy.data.expr.Parameter">
        </property>
    </port>
    <port name="id2&lt;=&gt;&amp;" class="ptolemy.actor.parameters.ParameterPort">
        <property name="input"/>
    </port>
</entity>
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="10.0.devel">
    </property>
    <property name="id1&lt;=&gt;&amp;" class="ptolemy.actor.parameters.FilePortParameter" value="">
    </property>
    <port name="id1&lt;=&gt;&amp;" class="ptolemy.actor.parameters.ParameterPort">
        <property name="input"/>
        <property name="defaultValue" class="ptolemy.data.expr.Parameter">
        </property>
    </port>
    <port name="id2&lt;=&gt;&amp;" class="ptolemy.actor.parameters.ParameterPort">
        <property name="input"/>
        <property name="defaultValue" class="ptolemy.data.expr.Parameter">
        </property>
    </port>
</entity>
}}

######################################################################
####
# 
set moml35_1 {
<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="foo" class="ptolemy.actor.TypedCompositeActor">
   <port name="C" class="ptolemy.actor.TypedIOPort"></port>
   <relation name="R2" class="ptolemy.actor.TypedIORelation"></relation>
   <entity name="C1" class="NotAClassC1">
       <port name="A" class="ptolemy.actor.TypedIOPort"></port>
       <relation name="R1" class="ptolemy.actor.TypedIORelation"></relation>
       <entity name="C2" class="ptolemy.actor.TypedCompositeActor">
           <port name="B" class="ptolemy.actor.TypedIOPort"></port>
       </entity>
   </entity>
   <entity name="C2" class="NotAClassC2">
   </entity>
   <link port="C" relation="C1.Rxx"/>
</entity>
}

test MoMLParser-35.1 {test missing classes } {
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser reset
    set recorderErrorHandler [java::new ptolemy.moml.test.RecorderErrorHandler]
    java::call ptolemy.moml.MoMLParser setErrorHandler $recorderErrorHandler

    jdkCaptureErr {
	set toplevel [$parser parse $moml35_1]
    } errMsg
    java::call ptolemy.moml.MoMLParser setErrorHandler [java::null]
    # This used to fail because LevelCrossing Links had problems, now we
    # try to link to a non-existent relation
    set err1_6 {Warning: Missing Classes: NotAClassC2, NotAClassC1
}
    set err1_8 {Warning: Missing Classes: NotAClassC1, NotAClassC2
}
    if { $errMsg != $err1_6 && $errMsg != $err1_8 } {
	error "Error message\n$errMsg\n was not the same as the Java 1.6 error:\n$err1_6\n or the Java 1.8 error:\n$err1_8"
    }
    list [string range [$recorderErrorHandler getMessages] 0 130]
} {{RecorderErrorHandler: Error encountered in:
<entity name="C1" class="NotAClassC1">
ptolemy.kernel.util.IllegalActionException: Cann}}

