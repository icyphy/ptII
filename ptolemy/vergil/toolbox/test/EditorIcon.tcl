# Tests for the XMLIcon class
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
set moml_1 "$header
<class name=\"generic\" extends=\"ptolemy.actor.TypedCompositeActor\">
    <rendition name=\"_icon\" class=\"ptolemy.vergil.toolbox.EditorIcon\">
    </rendition>
</class>
"

test EditorIcon-1.1 {parse and dump an EditorIcon} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml_1]
    $toplevel exportMoML
} $moml_1

test EditorIcon-1.2  {test getLocation when not parsed} {
    set icon [java::cast ptolemy.vergil.toolbox.EditorIcon [$toplevel getAttribute _icon]]
    java::isnull [$icon getLocation]
} {1}

#----------------------------------------------------------------------
set moml_2 "$header
<class name=\"generic\" extends=\"ptolemy.actor.TypedCompositeActor\">
    <rendition name=\"_icon\" class=\"ptolemy.vergil.toolbox.EditorIcon\">
        <location value=\"144.0, 93.0\"/>
    </rendition>
</class>
"

test EditorIcon-2.1 {parse and dump an EditorIcon} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml_2]
    $toplevel exportMoML
} $moml_2

test EditorIcon-2.2  {test getLocation when parsed} {
    set icon [java::cast ptolemy.vergil.toolbox.EditorIcon [$toplevel getAttribute _icon]]
    set location [$icon getLocation]
    jdkPrintArray $location
} {144.0 93.0}

test EditorIcon-2.3 {test createIcon when parsed} {
    java::isnull [$icon createIcon]
} {0}

test EditorIcon-2.4 {test createBackgroundFigure when parsed} {
    java::isnull [$icon createBackgroundFigure]
} {0}

test EditorIcon-2.5 {test createFigure when parsed} {
    java::isnull [$icon createFigure]
} {0}