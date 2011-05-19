# Tests for the XMLIcon class
#
# @Author: Steve Neuendorffer
#
# @Version: $Id$
#
# @Copyright (c) 1997-2005 The Regents of the University of California.
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

######################################################################
####
#
set moml_1 {<?xml version="1.0" standalone="no"?>
<!DOCTYPE class PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<class name="generic" extends="ptolemy.actor.TypedCompositeActor">
<property name="_iconDescription" class="ptolemy.kernel.util.TransientSingletonConfigurableAttribute">
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
</class>
}

test XMLIcon-1.1 {parse and dump an XMLIcon} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml_1]
    set icon [java::new ptolemy.vergil.toolbox.XMLIcon $toplevel _icon]
    $toplevel exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE class PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<class name="generic" extends="ptolemy.actor.TypedCompositeActor">
</class>
}

test XMLIcon-2.4 {test createBackgroundFigure when parsed} {
    java::isnull [$icon createBackgroundFigure]
} {0}

test XMLIcon-2.5 {test createFigure when parsed} {
    java::isnull [$icon createFigure]
} {0}
