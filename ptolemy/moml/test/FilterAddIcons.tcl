# Tests for the FilterAddIcons class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2002 The Regents of the University of California.
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
test FilterAddIcons-1.1 {filterAttributeValue} { 
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser addMoMLFilter [java::new ptolemy.moml.FilterAddIcons]
    set toplevel [$parser parseFile "./FilterAddIcons.xml"]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {}

set header {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">}

set constMoml  "$header 
<entity name=\"FilterAddIcons\" class=\"ptolemy.actor.TypedCompositeActor\">
  <entity name=\"Const\" class=\"ptolemy.actor.lib.Const\">
  </entity>
</entity>"



test FilterAddIcons-2.1 {Const} { 
    set parser [java::new ptolemy.moml.MoMLParser]
    #$parser addMoMLFilter [java::new ptolemy.moml.FilterAddIcons]
    set toplevel [$parser parse $constMoml]
    set newMoML [$toplevel exportMoML]
    list $newMoML
} {}