# Tests for the NamedObj class
#
# @Author: Christopher Hylands, Edward A. Lee
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

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

if {[info procs enumToObjects] == "" } then {
     source enums.tcl
}

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
} {{ptolemy.kernel.util.IllegalActionException: .null:
Cannot set a name with a period: This.name.has.dots}}

######################################################################
####
# FIXME:  test addParam, removeParam, getParam, getParams
# test NamedObj-3.1 {Experiment with Parameters} {
#     set n [java::new ptolemy.kernel.util.NamedObj]
#     set a1 [java::new ptolemy.data.Parameter A1 1]
#     set a2 [java::new ptolemy.data.Parameter A2 2]
#     $n addParameter $a1
#     set result [enumToFullNames [$n getParameters]]
# } {{first parameter} 42 {second parameter} -4}

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
} {foo foo.bar}

######################################################################
####
#
test NamedObj-6.1 {Test toString} {
    set n [java::new ptolemy.kernel.util.Workspace "foo"]
    set a [java::new ptolemy.kernel.util.NamedObj]
    set b [java::new ptolemy.kernel.util.NamedObj $n ""]
    set c [java::new ptolemy.kernel.util.NamedObj $n "car" ]
    list [$a toString] [$b toString] [$c toString]
} {{ptolemy.kernel.util.NamedObj {.}} {ptolemy.kernel.util.NamedObj {foo.}} {ptolemy.kernel.util.NamedObj {foo.car}}}

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
 ptolemy.kernel.util.NamedObj {foo.} attributes {
}
 ptolemy.kernel.util.NamedObj {foo.car} attributes {
}
 ptolemy.kernel.util.Workspace {foo} directory {
    {ptolemy.kernel.util.NamedObj {foo.} attributes {
    }}
    {ptolemy.kernel.util.NamedObj {foo.car} attributes {
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
} {ptolemy.kernel.util.NamedObj {N.A} attributes {
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
} {Cloned N.A into workspace: N
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
} {{} {Added attribute X to N.A
Removed attribute X from N.A
}}

test NamedObj-8.6 {Test RecorderListener: set container to null} {
    set n [java::new ptolemy.kernel.util.Workspace "N"]
    set a [java::new ptolemy.kernel.util.NamedObj $n "A" ]
    set listener [java::new ptolemy.kernel.util.RecorderListener]
    $a addDebugListener $listener
    set b [java::new ptolemy.kernel.util.Attribute $a "X"]
    $b setContainer [java::null]
    $listener getMessages
} {Added attribute X to N.A
Removed attribute X from N.A
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


# Capture output to System.out
proc jdkCapture {script varName} {
    upvar $varName output
    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
            {java.io.PrintStream java.io.OutputStream} $stream]
    set stdout [java::field System out]
    java::call System setOut $printStream
    set result [uplevel $script]
    java::call System setOut $stdout
    $printStream flush
    # This hack is necessary because of problems with crnl under windows
    regsub -all [java::call System getProperty "line.separator"] \
	        [$stream toString] "\n" output
    return $result
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
    set n [java::new ptolemy.kernel.util.Workspace "N"]
    set a [java::new ptolemy.kernel.util.NamedObj $n "A"]
    set a1 [java::new ptolemy.kernel.util.Attribute $a "A1"]
    set a2 [java::new ptolemy.kernel.util.Attribute $a1 "A2"]
    set gotten [$a getAttribute "A1.A2"]
    $gotten getFullName
} {N.A.A1.A2}
