# Tests for the NamedObj class
#
# @Author: Christopher Hylands, Edward A. Lee
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

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs enumToObjects] == "" } then {
     source enums.tcl
}

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

######################################################################
####
#
test NamedObj-2.1 {Create a NamedObj, set the name, change it} {
    set n [java::new ptolemy.kernel.util.NamedObj]
    set result1 [$n getName]
    $n setName "A Named Obj"
    set result2 [$n getName]
    $n setName "A different Name"
    set result3 [$n getName]
    $n setName {}
    set result4 [$n getName]
    set result5 [$n description]
    list $result1 $result2 $result3 $result4 $result5
} {{} {A Named Obj} {A different Name} {} {ptolemy.kernel.util.NamedObj {.} attributes {
}}}

######################################################################
####
#
test NamedObj-2.2 {Create a NamedObj, set the name, change it} {
    set n [java::new ptolemy.kernel.util.NamedObj "name set in constructor"]
    set result1 [$n getName]
    $n setName "A Named Obj"
    set result2 [$n getName]
    $n setName "A different Name"
    set result3 [$n getName]
    $n setName {}
    set result4 [$n getName]
    list $result1 $result2 $result3 $result4
} {{name set in constructor} {A Named Obj} {A different Name} {}}


######################################################################
####
#
test NamedObj-2.3 { Check names with dots} {
    # In early versions of the kernel, we prohibited names with dots
    # Now, dots are permitted.
    catch {java::new ptolemy.kernel.util.NamedObj "This.name.has.dots"} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Cannot set a name with a period: This.name.has.dots
  in .<Unnamed Object>}}

######################################################################
####
#
test NamedObj-4.1 {Set the name to null in the constructor} {
    set n [java::new ptolemy.kernel.util.NamedObj [java::null]]
    $n getName
} {}

######################################################################
####
#
test NamedObj-4.2 {Set the name to null after construction} {
    set n [java::new ptolemy.kernel.util.NamedObj "foo"]
    $n setName [java::null]
    $n getName
} {}

######################################################################
####
#
test NamedObj-5.1 {Test getFullName} {
    set n [java::new ptolemy.kernel.util.Workspace "foo"]
    set b [java::new ptolemy.kernel.util.NamedObj $n "bar"]
    list [$n getFullName] [$b getFullName]
} {foo .bar}

test NamedObj-5.2 {Test getName(parent)} {
    set n [java::new ptolemy.kernel.util.Workspace]
    set a [java::new ptolemy.kernel.util.NamedObj $n "A"]
    set a1 [java::new ptolemy.kernel.util.Attribute $a "A1"]
    set a2 [java::new ptolemy.kernel.util.Attribute $a1 "A2"]
    catch {$a2 getName $a2} msg1
    catch {$a2 getName [java::null]} msg2
    list $msg1 $msg2 [$a2 getName $a1] [$a2 getName $a]
} {.A.A1.A2 .A.A1.A2 A2 A1.A2}

######################################################################
####
#
test NamedObj-6.1 {Test toString} {
    set n [java::new ptolemy.kernel.util.Workspace "foo"]
    set a [java::new ptolemy.kernel.util.NamedObj]
    set b [java::new ptolemy.kernel.util.NamedObj $n ""]
    set c [java::new ptolemy.kernel.util.NamedObj $n "car" ]
    list [$a toString] [$b toString] [$c toString]
} {{ptolemy.kernel.util.NamedObj {.}} {ptolemy.kernel.util.NamedObj {.}} {ptolemy.kernel.util.NamedObj {.car}}}

######################################################################
####
#
test NamedObj-6.2 {Test description} {
    set n [java::new ptolemy.kernel.util.Workspace "foo"]
    set a [java::new ptolemy.kernel.util.NamedObj]
    set b [java::new ptolemy.kernel.util.NamedObj $n ""]
    set c [java::new ptolemy.kernel.util.NamedObj $n "car" ]
    list "[$a description [java::field ptolemy.kernel.util.NamedObj COMPLETE]]\n\
	    [$b description [java::field ptolemy.kernel.util.NamedObj COMPLETE]]\n\
	    [$c description [java::field ptolemy.kernel.util.NamedObj COMPLETE]]\n\
	    [$n description [java::field ptolemy.kernel.util.NamedObj COMPLETE]]"
} {{ptolemy.kernel.util.NamedObj {.} attributes {
}
 ptolemy.kernel.util.NamedObj {.} attributes {
}
 ptolemy.kernel.util.NamedObj {.car} attributes {
}
 ptolemy.kernel.util.Workspace {foo} directory {
    {ptolemy.kernel.util.NamedObj {.} attributes {
    }}
    {ptolemy.kernel.util.NamedObj {.car} attributes {
    }}
}}}

######################################################################
####
#
test NamedObj-7.1 {Test clone} {
    set n [java::new ptolemy.kernel.util.Workspace "N"]
    set a [java::new ptolemy.kernel.util.NamedObj $n "A" ]
    set b [java::cast ptolemy.kernel.util.NamedObj [$a clone]]
    $b description [java::field ptolemy.kernel.util.NamedObj COMPLETE]
} {ptolemy.kernel.util.NamedObj {.A} attributes {
}}

######################################################################
####
#
test NamedObj-8.1 {Test RecorderListener: call clone} {
    set n [java::new ptolemy.kernel.util.Workspace "N"]
    set a [java::new ptolemy.kernel.util.NamedObj $n "A" ]
    set listener [java::new ptolemy.kernel.util.RecorderListener]
    $a addDebugListener $listener
    $a clone
    $listener getMessages
} {Cloned .A into workspace: N
}

test NamedObj-8.1.1 {Test RecorderListener: call clone with a null arg} {
    # Note that this test used the three arg _debug() method
    # which needed testing anyway
    set n [java::new ptolemy.kernel.util.Workspace "N"]
    set a [java::new ptolemy.kernel.util.NamedObj $n "AA" ]
    set listener [java::new ptolemy.kernel.util.RecorderListener]
    $a addDebugListener $listener
    $a clone [java::null]
    $listener getMessages
} {Cloned .AA into default workspace.
}


test NamedObj-8.2 {Test RecorderListener: call setName} {
    set a [java::new ptolemy.kernel.util.NamedObj]
    set listener [java::new ptolemy.kernel.util.RecorderListener]
    $a addDebugListener $listener
    $a setName "B"
    $listener getMessages
} {Changed name from . to .B
}

test NamedObj-8.3 {Test RecorderListener: call setName 2x} {
    set a [java::new ptolemy.kernel.util.NamedObj]
    set listener [java::new ptolemy.kernel.util.RecorderListener]
    $a addDebugListener $listener
    $a setName "B"
    $a setName "C"
    $listener getMessages
} {Changed name from . to .B
Changed name from .B to .C
}

test NamedObj-8.4 {Test Recorderlistener: setName, then reset, then setName} {
    set a [java::new ptolemy.kernel.util.NamedObj]
    set listener [java::new ptolemy.kernel.util.RecorderListener]
    $a addDebugListener $listener
    $a setName "B"
    $listener reset
    $a setName "C"
    $listener getMessages
} {Changed name from .B to .C
}


test NamedObj-8.5 {Test RecorderListener: reset, getMessages, setName} {
    set n [java::new ptolemy.kernel.util.Workspace "N"]
    set a [java::new ptolemy.kernel.util.NamedObj $n "A" ]
    set listener [java::new ptolemy.kernel.util.RecorderListener]
    $a addDebugListener $listener
    
    # Check to make sure that calling reset on a empty buffer
    # does not cause problems
    $listener reset

    set result1 [$listener getMessages]
    set b [java::new ptolemy.kernel.util.Attribute $a "X"]
    $b setContainer [java::null]
    list $result1 [$listener getMessages]
} {{} {Added attribute X to .A
Removed attribute X from .A
}}

test NamedObj-8.6 {Test RecorderListener: set container to null} {
    set n [java::new ptolemy.kernel.util.Workspace "N"]
    set a [java::new ptolemy.kernel.util.NamedObj $n "A" ]
    set listener [java::new ptolemy.kernel.util.RecorderListener]
    $a addDebugListener $listener
    set b [java::new ptolemy.kernel.util.Attribute $a "X"]
    $b setContainer [java::null]
    $listener getMessages
} {Added attribute X to .A
Removed attribute X from .A
}

test NamedObj-8.7 {Test RecorderListener: add then remove a listenert} {
    set a [java::new ptolemy.kernel.util.NamedObj]
    set listener [java::new ptolemy.kernel.util.RecorderListener]
    $a addDebugListener $listener
    $a setName "B"
    set result1 [$listener getMessages]

    $a removeDebugListener $listener

    # Call it twice, just to be sure we don't crash
    $a removeDebugListener $listener

    $a setName "C"
    list $result1 [$listener getMessages]
} {{Changed name from . to .B
} {Changed name from . to .B
}}


test NamedObj-8.8 {Test RecorderListener: call message() directly} {
    set a [java::new ptolemy.kernel.util.NamedObj]
    set listener [java::new ptolemy.kernel.util.RecorderListener]
    $a addDebugListener $listener
    $listener message \
	    "This is the first Message, no trailing newline."
    $listener message \
	    "This is the second Message, trailing newline.\n"
    $listener message \
	    "This is the third Message, no trailing newline."
    $listener getMessages
} {This is the first Message, no trailing newline.
This is the second Message, trailing newline.

This is the third Message, no trailing newline.
}

test NamedObj-8.9 {Test RecorderListener: call event() directly} {
    set a [java::new ptolemy.kernel.util.NamedObj]
    set listener [java::new ptolemy.kernel.util.RecorderListener]
    $a addDebugListener $listener
    set source [java::new ptolemy.kernel.util.NamedObj "event source"]
    set debugEvent [java::new ptolemy.kernel.util.test.TestDebugEvent $source]
    $listener event $debugEvent
    $listener getMessages
} {ptolemy.kernel.util.NamedObj {.event source}
}
test NamedObj-8.9.1 {Test NamedObj.event()} {
    set a [java::new ptolemy.kernel.util.NamedObj]
    set listener [java::new ptolemy.kernel.util.RecorderListener]
    $a addDebugListener $listener
    set source [java::new ptolemy.kernel.util.NamedObj "event source"]
    set debugEvent [java::new ptolemy.kernel.util.test.TestDebugEvent $source]
    $a event $debugEvent
    #$listener event $debugEvent
    $listener getMessages
} {ptolemy.kernel.util.NamedObj {.event source}
}

test NamedObj-9.1 {Test StreamListener} {
    set a [java::new ptolemy.kernel.util.NamedObj]
    jdkCapture {
	set listener [java::new ptolemy.kernel.util.StreamListener]
	$a addDebugListener $listener
	$a setName "B"
	$a setName "C"
    } stdoutResults
    list $stdoutResults
} {{Changed name from . to .B
Changed name from .B to .C
}}

test NamedObj-9.2 {Test StreamListener: ByteArrayOutputStream} {
    set a [java::new ptolemy.kernel.util.NamedObj]
    set byteArrayOutputStream [java::new java.io.ByteArrayOutputStream]
    set listener [java::new ptolemy.kernel.util.StreamListener \
	    $byteArrayOutputStream]
    $a addDebugListener $listener
    $a setName "B"
    $a setName "C"
    # This is necessary so that we get the same results under windows.
    regsub -all [java::call System getProperty "line.separator"] \
	        [$byteArrayOutputStream toString] "\n" output
    list $output
} {{Changed name from . to .B
Changed name from .B to .C
}}

######################################################################
####
#
test NamedObj-10.1 {Test getAttribute} {
    set n [java::new ptolemy.kernel.util.Workspace]
    set a [java::new ptolemy.kernel.util.NamedObj $n "A"]
    set a1 [java::new ptolemy.kernel.util.Attribute $a "A1"]
    set a2 [java::new ptolemy.kernel.util.Attribute $a1 "A2"]
    set gotten [$a getAttribute "A1.A2"]
    $gotten getFullName
} {.A.A1.A2}

######################################################################
####
#
test NamedObj-10.2 {Test toplevel} {
    set n [java::new ptolemy.kernel.util.Workspace]
    set a [java::new ptolemy.kernel.util.NamedObj $n "A"]
    set a1 [java::new ptolemy.kernel.util.Attribute $a "A1"]
    set a2 [java::new ptolemy.kernel.util.Attribute $a1 "A2"]
    list [[$a toplevel] getFullName] [[$a2 toplevel] getFullName]
} {.A .A}

######################################################################
####
#
test NamedObj-11.1 {Test exportMoML} {
    set n [java::new ptolemy.kernel.util.Workspace]
    set a [java::new ptolemy.kernel.util.NamedObj $n "A"]
    set a1 [java::new ptolemy.kernel.util.Attribute $a "A1"]
    set a2 [java::new ptolemy.kernel.util.Attribute $a1 "A2"]
    $a exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="A" class="ptolemy.kernel.util.NamedObj">
    <property name="A1" class="ptolemy.kernel.util.Attribute">
        <property name="A2" class="ptolemy.kernel.util.Attribute">
        </property>
    </property>
</entity>
}

test NamedObj-11.1.1 {Test exportMoML(String)} {
    set n [java::new ptolemy.kernel.util.Workspace]
    set a [java::new ptolemy.kernel.util.NamedObj $n "A"]
    # Use a StringAttribute here so that this gets exported in test 11.2
    set a1 [java::new ptolemy.kernel.util.StringAttribute $a "A1"]
    set a2 [java::new ptolemy.kernel.util.Attribute $a1 "A2"]
    $a exportMoML "NewName"
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="NewName" class="ptolemy.kernel.util.NamedObj">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2">
    </property>
    <property name="A1" class="ptolemy.kernel.util.StringAttribute">
        <property name="A2" class="ptolemy.kernel.util.Attribute">
        </property>
    </property>
</entity>
}

test NamedObj-11.2 {Test deferMoMLDefinitionTo} {
    # The following causes clones of $a to defer their MoML definition to $a.
    # NOTE: This is basically what the MoML parser has to do to deal with
    # clones and instances and inheritance.
    java::field [$a getMoMLInfo] elementName "class"
    java::field [$a getMoMLInfo] className ".A"
    java::field [$a getMoMLInfo] superclass "ptolemy.kernel.util.NamedObj"
    # Old version
    # $a setMoMLElementName "class"
    set b [java::cast ptolemy.kernel.util.NamedObj [$a clone]]
    $b setDeferMoMLDefinitionTo $a
    java::field [$b getMoMLInfo] superclass ".A"
    $b exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE class PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<class name="A" extends=".A">
    <property name="A1" class="ptolemy.kernel.util.StringAttribute">
        <property name="A2" class="ptolemy.kernel.util.Attribute">
        </property>
    </property>
</class>
}

test NamedObj-11.4 {Test deferredMoMLDefinitionFrom} {
    listToFullNames [java::field [$a getMoMLInfo] deferredFrom]
} {.A}

# NOTE: This test no longer makes sense, since such removal is
# no longer possible.
# test NamedObj-11.5 {Test removal of deferral} {
#     $b deferMoMLDefinitionTo [java::null]
#     listToFullNames [$a deferredMoMLDefinitionFrom]
# } {}

test NamedObj-11.6 {Test exportMoML Writer} {
    set n [java::new ptolemy.kernel.util.Workspace]
    set a [java::new ptolemy.kernel.util.NamedObj $n "AA"]
    set a1 [java::new ptolemy.kernel.util.Attribute $a "AA1"]
    set a2 [java::new ptolemy.kernel.util.Attribute $a1 "AA2"]
    jdkCapture {
	set writer [java::new java.io.OutputStreamWriter \
		[java::field System out]]
	$a exportMoML $writer
	$writer flush
    } stdoutResults
    list $stdoutResults
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="AA" class="ptolemy.kernel.util.NamedObj">
    <property name="AA1" class="ptolemy.kernel.util.Attribute">
        <property name="AA2" class="ptolemy.kernel.util.Attribute">
        </property>
    </property>
</entity>
}}


######################################################################
####
#
test NamedObj-12.1 {Test uniqueName} {
    set n [java::new ptolemy.kernel.util.Workspace]
    set a [java::new ptolemy.kernel.util.NamedObj $n "A"]
    set n1 [$a uniqueName A]
    set a1 [java::new ptolemy.kernel.util.Attribute $a $n1]
    set n2 [$a uniqueName A]
    set a2 [java::new ptolemy.kernel.util.Attribute $a $n2]
    list $n1 $n2
} {A A2}

test NamedObj-12.2 {Test uniqueName} {
    # NOTE: Depends on the previous.
    set n3 [$a uniqueName A2]
    set a3 [java::new ptolemy.kernel.util.Attribute $a $n3]
    list $n3
} {A3}

test NamedObj-12.3 {Test uniqueName} {
    # NOTE: Depends on the previous.
    set a22 [java::new ptolemy.kernel.util.Attribute $a "A22"]
    set n4 [$a uniqueName A22]
    set a4 [java::new ptolemy.kernel.util.Attribute $a $n4]
    list $n4
} {A4}

test NamedObj-12.4 {Test uniqueName} {
    set n [java::new ptolemy.kernel.util.Workspace]
    set a [java::new ptolemy.kernel.util.NamedObj $n "A"]
    set n1 [$a uniqueName ""]
    set a1 [java::new ptolemy.kernel.util.Attribute $a $n1]
    set n2 [$a uniqueName "3"]
    set a2 [java::new ptolemy.kernel.util.Attribute $a $n2]
    list $n1 $n2
} {{} 2}

######################################################################
####
#
test NamedObj-13.1 {Test attributeList} {
    set n [java::new ptolemy.kernel.util.Workspace]
    set a [java::new ptolemy.kernel.util.NamedObj $n "A"]
    set a1 [java::new ptolemy.kernel.util.Attribute $a "A0"]
    set a2 [java::new ptolemy.kernel.util.Attribute $a "A2"]
    listToNames [$a attributeList]
} {A0 A2}

test NamedObj-13.2 {Test attributeList with filter} {
    set n [java::new ptolemy.kernel.util.Workspace]
    set a [java::new ptolemy.kernel.util.NamedObj $n "A"]
    set a1 [java::new ptolemy.kernel.util.Attribute $a "A0"]
    set a2 [java::new ptolemy.kernel.util.Attribute $a "A2"]
    listToNames [$a attributeList [$a1 getClass]]
} {A0 A2}

test NamedObj-13.4 {Test getAttributes, which is deprecated } {
    set n [java::new ptolemy.kernel.util.Workspace]
    set a [java::new ptolemy.kernel.util.NamedObj $n "A"]
    set a1 [java::new ptolemy.kernel.util.Attribute $a "A0"]
    set a2 [java::new ptolemy.kernel.util.Attribute $a "A2"]
    enumToNames [$a getAttributes]
} {A0 A2}

######################################################################
####

test NamedObj-14.1 {Test depthInHierarchy} {
    set n [java::new ptolemy.kernel.util.Workspace "N"]
    set a [java::new ptolemy.kernel.util.NamedObj $n "A" ]
    $a depthInHierarchy
} {0}

test NamedObj-14.2 {Test depthInHierarchy} {
    set b [java::new ptolemy.kernel.util.Attribute $a "A" ]
    $b depthInHierarchy
} {1}


######################################################################
####

test NamedObj-15.1 {Test getModelErrorHandler, setModelErrorHandler} {
    set n [java::new ptolemy.kernel.util.Workspace "N"]
    set a [java::new ptolemy.kernel.util.NamedObj $n "A"]
    set r1 [$a getModelErrorHandler]
    set handler [java::new ptolemy.kernel.util.BasicModelErrorHandler]
    $a setModelErrorHandler $handler
    list $r1 [expr {[java::cast \
	    ptolemy.kernel.util.BasicModelErrorHandler \
	    [$a getModelErrorHandler]] == $handler}]
} {java0x0 1}


######################################################################
####

test NamedObj-15.1 {Test addChangeListener, removeChangeListener } {
    # FIXME: not much can be done here without defining another class

    set n [java::new ptolemy.kernel.util.Workspace "N"]
    set a [java::new ptolemy.kernel.util.NamedObj $n "A"]

    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
            {java.io.PrintStream java.io.OutputStream} $stream]
    set listener [java::new ptolemy.kernel.util.StreamChangeListener \
	    $printStream]

    # Try removing the listener before adding it.
    $a removeChangeListener $listener    

    $a addChangeListener $listener

    # Add the listener twice to get coverage of a basic block.
    $a addChangeListener $listener

    $printStream flush
    regsub -all [java::call System getProperty "line.separator"] \
	        [$stream toString] "\n" output
    list $output 
} {{}}
