# Tests for the ConfigurableAttribute class
#
# @Author: Steve Neuendorffer, Edward A. Lee, Christopher Hylands
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
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">}

######################################################################
####
#
test ConfigurableAttribute-1.1 {test export moml.} {
    set n0 [java::new ptolemy.kernel.util.NamedObj]
    $n0 setName N0
    set p1 [java::new ptolemy.kernel.util.ConfigurableAttribute $n0 P1]
    $p1 configure [java::null] [java::null] {My Test String}
    $n0 exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="N0" class="ptolemy.kernel.util.NamedObj">
    <property name="P1" class="ptolemy.kernel.util.ConfigurableAttribute">
        <configure>My Test String</configure>
    </property>
</entity>
}

######################################################################
####
#
test ConfigurableAttribute-1.5 {test value, getConfigureText, getConfigureSource methods.} {
    # Uses Test 1.1 above 
    list [$p1 value] [$p1 getConfigureText] [$p1 getConfigureSource]
} {{My Test String} {My Test String} {}}


######################################################################
####
#
test ConfigurableAttribute-1.6 {test export moml with null text} {
    set n0 [java::new ptolemy.kernel.util.NamedObj]
    $n0 setName N0
    set p1 [java::new ptolemy.kernel.util.ConfigurableAttribute $n0 P1]
    $p1 configure [java::null] "NotASource.xml" [java::null]
    $n0 exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="N0" class="ptolemy.kernel.util.NamedObj">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="2.1-devel-2">
    </property>
    <property name="P1" class="ptolemy.kernel.util.ConfigurableAttribute">
        <configure source="NotASource.xml"/>
    </property>
</entity>
}

######################################################################
####
#
test ConfigurableAttribute-2.1 {addValueListener} {
    set n [java::new ptolemy.kernel.util.NamedObj "my NamedObj"]
    set s2_1 [java::new ptolemy.kernel.util.ConfigurableAttribute $n "my ConfigurableAttribute"]
    set stream [java::new java.io.ByteArrayOutputStream]
    set printStream [java::new \
            {java.io.PrintStream java.io.OutputStream} $stream]
    set listener [java::new ptolemy.kernel.util.test.StreamValueListener \
	    $printStream]

    # Try removing the listener before adding it.
    $s2_1 removeValueListener $listener

    $s2_1 addValueListener $listener

    # Add the listener twice to get coverage of a basic block.
    $s2_1 addValueListener $listener

    $s2_1 setExpression "a string"

    # Remove the listener and verify that we are not updating it.
    $s2_1 removeValueListener $listener
    $s2_1 setExpression "another string"
    $printStream flush
    regsub -all [java::call System getProperty "line.separator"] \
	        [$stream toString] "\n" output
    list $output
} {{ptolemy.kernel.util.ConfigurableAttribute {.my NamedObj.my ConfigurableAttribute} changed, new expression: a string
}}


test ConfigurableAttribute-3.1 {getVisibility, setVisibility, getBase} {
    set c1 [java::new ptolemy.kernel.util.ConfigurableAttribute]
    set full [java::field ptolemy.kernel.util.Settable NONE]
    set r1 [expr {[$c1 getVisibility] == $full}]

    set none [java::field ptolemy.kernel.util.Settable FULL]
    $c1 setVisibility $none

    list $r1 [expr {[$c1 getVisibility] == $none}] \
	    [expr {[$c1 getBase] == [java::null]}] 
} {1 1 1}

test ConfigurableAttribute-4.1 {setExpression, getExpression} {
    set c1 [java::new ptolemy.kernel.util.ConfigurableAttribute]
    set s1 [java::new ptolemy.kernel.util.StringAttribute $c1 "_s1"]
    set s2 [java::new ptolemy.kernel.util.StringAttribute $c1 "_s2"]
    $c1 configure [java::null] [java::null] {My Test String}
    set r1 [$c1 getExpression]
    $c1 setExpression "Another Test String"
    list $r1 [$c1 getExpression]
} {{My Test String} {Another Test String}}

test ConfigurableAttribute-4.1.1 {value, getConfigureText, getConfigureSource} {
    # Uses 4.1 above, testing that the StringAttributes don't show up
    list [$c1 value] [$c1 getConfigureText] [$c1 getConfigureSource]
} {{Another Test String} {Another Test String} {}}

test ConfigurableAttribute-4.2 {getExpression exception, check out getConfigureSource} {
    set c1 [java::new ptolemy.kernel.util.ConfigurableAttribute]
    $c1 configure [java::null] "NotAFile" {My Test String}
    set r1 [$c1 getConfigureSource]
    catch {$c1 getExpression} errMsg
    list $r1 $errMsg [$c1 getConfigureSource]
} {NotAFile {java.net.MalformedURLException: no protocol: NotAFile} NotAFile}


test ConfigurableAttribute-4.3 {getExpression from a real file} {
    set c1 [java::new ptolemy.kernel.util.ConfigurableAttribute]
    $c1 configure [java::null] "file:./ConfigurableAttribute.txt" {My Test String}
    $c1 getExpression
} {Test file for ConfigurableAttribute
My Test String}
