# Tests for the MoMLParser class
#
# @Author: Edward A. Lee
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

# Test MoML
set moml_1 {<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <doc>xxx</doc>
</entity>
}

set moml_2 {<model name="top" class="ptolemy.actor.TypedCompositeActor">
    <doc>xxx</doc>
</model>
}

set moml_3 {<class name="top" extends="ptolemy.actor.TypedCompositeActor">
    <doc>xxx</doc>
</class>
}

#----------------------------------------------------------------------
test MoMLParser-1.1 {parse incorrect MoML} {
    set parser [java::new ptolemy.moml.MoMLParser]
    catch {$parser parse $moml_1} msg
    list $msg
} {{com.microstar.xml.XmlException: Element "entity" found inside an element that is not a CompositeEntity. It is: null}}

test MoMLParser-1.2 {parse simple model with doc only} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml_2]
    $toplevel exportMoML
} $moml_2

test MoMLParser-1.3 {parse simple class with doc only} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml_3]
    $toplevel exportMoML
} $moml_3

#----------------------------------------------------------------------
set moml_4 {<<?xml version="1.0" standalone="no"?>
<!DOCTYPE model SYSTEM "../moml.dtd">
<class name="top" extends="ptolemy.actor.TypedCompositeActor">
    <doc>xxx</doc>
</class>
}
#----------------------------------------------------------------------

test MoMLParser-1.3 {parse simple class with doc only} {
    set parser [java::new ptolemy.moml.MoMLParser]
    set toplevel [$parser parse $moml_3]
    $toplevel exportMoML
} $moml_3


