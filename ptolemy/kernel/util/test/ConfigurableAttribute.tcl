# Tests for the ConfigurableAttribute class
#
# @Author: Steve Neuendorffer and Edward A. Lee
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
test ConfigurableAttribute-1.1 {test export moml.} {
    set n0 [java::new ptolemy.kernel.util.NamedObj]
    $n0 setName N0
    set p1 [java::new ptolemy.kernel.util.ConfigurableAttribute $n0 P1]
    $p1 configure [java::null] [java::null] {My Test String}
    $n0 exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="N0" class="ptolemy.kernel.util.NamedObj">
    <property name="P1" class="ptolemy.kernel.util.ConfigurableAttribute">
        <configure>My Test String</configure>
    </property>
</model>
}

test ConfigurableAttribute-1.2 {test value method.} {
    $p1 value
} {My Test String}

test ConfigurableAttribute-1.3 {test parse moml.} {
    set moml_1 "$header
<model name=\"top\" class=\"ptolemy.kernel.CompositeEntity\">
</model>
"
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml_1]
    $parser parse {
<model name=".top">
    <property name="myAttribute" class="ptolemy.kernel.util.ConfigurableAttribute">
        <configure><?testML xxx ?></configure>
    </property>
</model>
}
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.kernel.CompositeEntity">
    <property name="myAttribute" class="ptolemy.kernel.util.ConfigurableAttribute">
        <configure><?testML xxx ?></configure>
    </property>
</model>
}

test ConfigurableAttribute-1.4 {test value method with parse moml} {
    set e1 [java::cast ptolemy.kernel.util.ConfigurableAttribute [$toplevel getAttribute myAttribute]]
    $e1 value
} {<?testML xxx ?>}

test ConfigurableAttribute-1.5 {test with weird configure text} {
    $parser reset
    set toplevel [$parser parse {
<model name="top" class="ptolemy.kernel.util.NamedObj">
    <property name="myAttribute" class="ptolemy.kernel.util.ConfigurableAttribute">
<configure><?svg
<!--<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 20001102//EN" 
  "http://www.w3.org/TR/2000/CR-SVG-20001102/DTD/svg-20001102.dtd">-->
<svg>
  <rect x="0" y="0" width="20" height="20" style="fill:blue;stroke:green;stroke-width:30"/>
  <circle cx="0" cy="0" r="20" style="fill:blue;stroke:green;stroke-width:30"/>
  <ellipse cx="0" cy="0" rx="20" ry="30" style="fill:blue;stroke:green;stroke-width:30"/>
  <polygon points="10,30 50,10 50,30" style="fill:blue;stroke:green;stroke-width:30"/>
  <polyline points="10,30 50,10 50,30" style="stroke:green;stroke-width:30"/>
  <line x1="10" y1="20" x2="30" y2="40" style="stroke:green;stroke-width:30"/>
</svg> ?>
</configure>
    </property>
</model>
}]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<model name="top" class="ptolemy.kernel.util.NamedObj">
    <property name="myAttribute" class="ptolemy.kernel.util.ConfigurableAttribute">
        <configure><?svg <!--<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 20001102//EN" 
  "http://www.w3.org/TR/2000/CR-SVG-20001102/DTD/svg-20001102.dtd">-->
<svg>
  <rect x="0" y="0" width="20" height="20" style="fill:blue;stroke:green;stroke-width:30"/>
  <circle cx="0" cy="0" r="20" style="fill:blue;stroke:green;stroke-width:30"/>
  <ellipse cx="0" cy="0" rx="20" ry="30" style="fill:blue;stroke:green;stroke-width:30"/>
  <polygon points="10,30 50,10 50,30" style="fill:blue;stroke:green;stroke-width:30"/>
  <polyline points="10,30 50,10 50,30" style="stroke:green;stroke-width:30"/>
  <line x1="10" y1="20" x2="30" y2="40" style="stroke:green;stroke-width:30"/>
</svg> ?>
</configure>
    </property>
</model>
}


   