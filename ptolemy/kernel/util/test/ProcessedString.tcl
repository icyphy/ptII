# Tests for the ProcessedString class
#
# @Author: Steve Neuendorffer
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

######################################################################
####
#

set header {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">}

#----------------------------------------------------------------------
test ProcessedString-1.1 {test export moml.} {
    set n0 [java::new ptolemy.kernel.util.NamedObj]
    $n0 setName N0
    set p1 [java::new ptolemy.kernel.util.ProcessedString $n0 P1]
    $p1 setString {My Test String}
    $p1 setInstruction {instruction}
    $n0 exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="N0" class="ptolemy.kernel.util.NamedObj">
    <property name="P1" class="ptolemy.kernel.util.ProcessedString">
        <configure><?instruction
My Test String?>
        </configure>
    </property>
</model>
}

test ProcessedString-1.2 {test methods.} {
    list [$p1 getInstruction] [$p1 getString]
} {instruction {My Test String}}

test ProcessedString-1.3 {test parse moml.} {
    set moml_1 "$header
<model name=\"top\" class=\"ptolemy.kernel.CompositeEntity\">
</model>
"
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml_1]
    $parser parse {
<model name=".top">
    <property name="myAttribute" class="ptolemy.kernel.util.ProcessedString">
        <configure><?testML
The Quick brown
Fox Jumped over
The lazy Dog.?>
        </configure>
    </property>
</model>
}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <property name="myAttribute" class="ptolemy.kernel.util.ProcessedString">
        <configure><?testML
The Quick brown
Fox Jumped over
The lazy Dog.?>
        </configure>
    </property>
</model>
}

test ProcessedString-1.4 {test parse moml} {
    set e1 [java::cast ptolemy.kernel.util.ProcessedString [$toplevel getAttribute myAttribute]]
    list [$e1 getInstruction] [$e1 getString]
} {testML {The Quick brown
Fox Jumped over
The lazy Dog.}}
   
test ProcessedString-1.5 {test parse moml with no PI} {
    set moml_1 "$header
<model name=\"top\" class=\"ptolemy.kernel.CompositeEntity\">
</model>
"
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml_1]
    $parser parse {
<model name=".top">
    <property name="myAttribute" class="ptolemy.kernel.util.ProcessedString">
        <configure>
The Quick brown
Fox Jumped over
The lazy Dog.
        </configure>
    </property>
</model>
}
   $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <property name="myAttribute" class="ptolemy.kernel.util.ProcessedString">
        <configure>
The Quick brown
Fox Jumped over
The lazy Dog.
        </configure>
    </property>
</model>
}

test ProcessedString-1.6 {test parse moml with no PI} {
    set e1 [java::cast ptolemy.kernel.util.ProcessedString [$toplevel getAttribute myAttribute]]
    list [$e1 getInstruction] [$e1 getString]
} {{} {The Quick brown
Fox Jumped over
The lazy Dog.}}

test ProcessedString-1.7 {test parse moml with no PI} {
    set n0 [java::new ptolemy.kernel.util.NamedObj]
    $n0 setName N0
    set p1 [java::new ptolemy.kernel.util.ProcessedString $n0 P1]
    $p1 setString {My String}
    $p1 setInstruction [java::null]
    $n0 exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="N0" class="ptolemy.kernel.util.NamedObj">
    <property name="P1" class="ptolemy.kernel.util.ProcessedString">
        <configure>
My String
        </configure>
    </property>
</model>
} 
