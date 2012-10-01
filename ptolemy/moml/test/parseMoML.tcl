# Tests for the parseMoML() expression language method
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2009 The Regents of the University of California.
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

######################################################################
####
#

set header {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">}

    set moml_1 "$header
<entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">
    <doc>xxx</doc>
</entity>
"

######################################################################
####
#
test parseMoML-1.0 {call UtilitiyFunctions.parseMoML() directly} {

    #set parser [java::new ptolemy.data.expr.PtParser]
    #set tree [$parser generateParseTree "parseMoML($moml_1)"]
    #set result [$tree evaluateParseTree]
    set result [java::call ptolemy.data.expr.UtilityFunctions parseMoML $moml_1]
    list [$result toString]
} {{parseMoML("    <entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">\n        <doc>xxx</doc>\n    </entity>\n")}}

######################################################################
####
#
test parseMoML-2.0 {create a  parse tree} {
    set parser [java::new ptolemy.data.expr.PtParser]

    # Need to use backslashes here.
    set tree [$parser generateParseTree {parseMoML("    <entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">\n        <doc>xxx</doc>\n    </entity>\n")} ]
    set result [$tree evaluateParseTree]
    list [$result toString]
} {{parseMoML("    <entity name=\"top\" class=\"ptolemy.actor.TypedCompositeActor\">\n        <doc>xxx</doc>\n    </entity>\n")}}


######################################################################
####
#
test parseMoML-3.0 {parse a file that has Const that calls parseMoML} {
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser reset
    # The list of filters is static, so we reset it in case there
    # filters were already added.
    java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
    java::call ptolemy.moml.MoMLParser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    java::call ptolemy.moml.MoMLParser addMoMLFilter [java::new \
	    ptolemy.moml.filter.RemoveGraphicalClasses]

    #######
    # FIXME! This is very odd.  If we don't access BaseType ACTOR,
    # then "$PTII/bin/ptjacl parseMoML.tcl" will fail because 
    # BaseType.ACTOR is null in TypeLattice.
    # ????
    set baseTypeActor [java::field ptolemy.data.type.BaseType ACTOR]

    set toplevel [$parser parseFile parseMoMLConst.xml]

    list [$toplevel exportMoML]
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="parseMoMLConst" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="8.1.devel">
    </property>
    <entity name="Const" class="ptolemy.actor.lib.Const">
        <property name="value" class="ptolemy.data.expr.Parameter" value="parseMoML(&quot;&lt;entity name=\&quot;foo\&quot; class=\&quot;ptolemy.actor.TypedCompositeActor\&quot;/&gt;&quot;)">
        </property>
    </entity>
</entity>
}}

$parser resetAll
java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
java::call ptolemy.moml.filter.RemoveGraphicalClasses initialize
