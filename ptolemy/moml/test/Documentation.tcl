# Tests for the Documentation class
#
# @Author: Christopher Hylands, Based on MoMLParser.tcl by Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 2002-2003 The Regents of the University of California.
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

# The classheader and moml_3_1 values are from MoMLParser.tcl
set classheader {<?xml version="1.0" standalone="no"?>
<!DOCTYPE class PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">}

set moml_3_1 "$classheader
<class name=\"top\" extends=\"ptolemy.actor.TypedCompositeActor\">
    <doc>aaa doc</doc>
    <doc name=\"bar\">bar doc</doc>
    <property name=\"xxx\" class=\"ptolemy.kernel.util.Attribute\">
        <doc>xxx doc</doc>
    </property>
    <property name=\"yyy\" class=\"ptolemy.kernel.util.Attribute\">
        <doc name=\"foo\">yyy doc</doc>
    </property>
</class>
"
test Documentation-1.3.1 {parse simple class with two doc tags} {
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser reset
    set toplevel [$parser parse $moml_3_1]
    $toplevel exportMoML
} $moml_3_1

test Documentation-2.0 {call consolidate} {
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser reset
    set toplevel [$parser parse $moml_3_1]
    java::call ptolemy.moml.Documentation consolidate $toplevel
} {aaa doc
bar doc}

test Documentation-2.1 {call consolidate on a NamedObj w/o documentation}  {
    set n0 [java::new ptolemy.kernel.util.NamedObj]
    # If we do not use -noconvert here, then we get the empty string
    # instead of null
    expr {[java::call -noconvert ptolemy.moml.Documentation consolidate $n0] \
	    == [java::null]}
} {1}

test Documentation-3.0 {call toString, getValue, setValue} {
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser reset
    set toplevel [$parser parse $moml_3_1]
    set documentation [java::cast ptolemy.moml.Documentation \
	    [$toplevel getAttribute "_doc"]]
    set r1 [$documentation toString]
    set r2 [$documentation getValue]
    $documentation setValue "new doc"
    set r3 [$documentation getValue]
    list $r1 $r2 $r3
} {{(ptolemy.moml.Documentation, aaa doc)} {aaa doc} {new doc}}


test Documentation-4.1 {setValue with a unescaped ampersand, as per Ned Stoffel} {
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser reset
    set toplevel [$parser parse $moml_3_1]
    set documentation [java::cast ptolemy.moml.Documentation \
	    [$toplevel getAttribute "_doc"]]
    set r1 [$documentation toString]
    set r2 [$documentation getValue]
    $documentation setValue \
	{    
	    if (Is_sin =1 & Is_noise=0) then \{
                 CC output will be set to Sinusoidal;
	     \} else if  (Is_sin=0 & Is_noise =1) then \{
                 CC output will be set to Gaussian Noise;
             \}
        }

    set moml_4_1 [$toplevel exportMoML]
    list [$documentation getValue] \
	[$documentation exportMoML]
} {{    
	    if (Is_sin =1 & Is_noise=0) then \{
                 CC output will be set to Sinusoidal;
	     \} else if  (Is_sin=0 & Is_noise =1) then \{
                 CC output will be set to Gaussian Noise;
             \}
        } {<doc>    &#10;	    if (Is_sin =1 &amp; Is_noise=0) then \{&#10;                 CC output will be set to Sinusoidal;&#10;	     \} else if  (Is_sin=0 &amp; Is_noise =1) then \{&#10;                 CC output will be set to Gaussian Noise;&#10;             \}&#10;        </doc>
}}

test Documentation-4.2 {parse the moml from 4.1 above} {
    set parser [java::new ptolemy.moml.MoMLParser]
    $parser reset
    set toplevel_4_2 [$parser parse $moml_4_1]
    set documentation [java::cast ptolemy.moml.Documentation \
	    [$toplevel_4_2 getAttribute "_doc"]]
    set r1 [$documentation toString]
    set r2 [$documentation getValue]
    set r3 [$documentation exportMoML]
    list $r1 $r2 $r3
} {{(ptolemy.moml.Documentation,     
	    if (Is_sin =1 & Is_noise=0) then \{
                 CC output will be set to Sinusoidal;
	     \} else if  (Is_sin=0 & Is_noise =1) then \{
                 CC output will be set to Gaussian Noise;
             \}
        )} {    
	    if (Is_sin =1 & Is_noise=0) then \{
                 CC output will be set to Sinusoidal;
	     \} else if  (Is_sin=0 & Is_noise =1) then \{
                 CC output will be set to Gaussian Noise;
             \}
        } {<doc>    &#10;	    if (Is_sin =1 &amp; Is_noise=0) then \{&#10;                 CC output will be set to Sinusoidal;&#10;	     \} else if  (Is_sin=0 &amp; Is_noise =1) then \{&#10;                 CC output will be set to Gaussian Noise;&#10;             \}&#10;        </doc>
}}
