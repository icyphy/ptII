# Tests for the Version class
#
# @Author: Christopher Hylands
#
# @Version: $Id$ 
#
# @Copyright (c) 2001-2013 The Regents of the University of California.
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

# Tycho test bed, see $PTII/doc/coding/testing.htm for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1


######################################################################
####
#
test VersionAttribute-1.0 {Constructor} {
    set n [java::new ptolemy.kernel.util.NamedObj "my NamedObj"]
    set v [java::new ptolemy.kernel.attributes.VersionAttribute $n "my Version"]
    set result1 [$v toString]
    set CURRENT_VERSION [java::field \
	    ptolemy.kernel.attributes.VersionAttribute CURRENT_VERSION]
    $v setExpression [$CURRENT_VERSION getExpression]

    set result2 [$v toString]
    # When we build an installer, the version number is 10.0_YYYYMMDD, so we get rid of that.
    regsub -all {_201[0-9][0-9][0-9][0-9]} [$v getExpression] {} result3
    list $result1 $result2 $result3
} {{ptolemy.kernel.attributes.VersionAttribute {.my NamedObj.my Version}} {ptolemy.kernel.attributes.VersionAttribute {.my NamedObj.my Version}} 11.0.devel}


test VersionAttribute-2.0 {compareTo} {
    set n [java::new ptolemy.kernel.util.NamedObj "my NamedObj"]
    set v [java::new ptolemy.kernel.attributes.VersionAttribute $n \
	    "testValue"]

    set CURRENT_VERSION [java::field \
	    ptolemy.kernel.attributes.VersionAttribute CURRENT_VERSION]

    set results {}
    set testValues [list "1.0" "1.0.0" "1.0-beta" \
	    "2.0" "2.0-devel" "2.0.alpha" "2.0_beta" "2.0-build003" \
	    "2.0-release-1" \
	    "3.0" "3.0-devel" "3.0-alpha" \
	    "3.1" \
	    "4" \
	    "4.1" \
	    "5.0" \
	    "5.1" "5.1-alpha" "5.1-beta" \
	    "5.2" "5.2-alpha" "5.2-beta" \
	    "6.0-devel" "6.0-alpha" "6.0.beta" "6.0.1" \
	    "7.0-devel" "7.0-alpha" "7.0.beta" "7.0.1" \
	    "8.0-devel" "8.0-alpha" "8.0.beta" "8.0.1" \
	    "8.1-devel" "8.1-alpha" "8.1.beta" "8.1.1" \
	    "9.0-devel" "9.0-alpha" "9.0.beta" "9.0.1" \
	    "9.1-devel" "9.1-alpha" "9.1.beta" "9.1.1" \
	    "9.2-devel" "9.2-alpha" "9.2.beta" "9.2.1" \
	    "10.0-devel" "10.0-alpha" "10.0.beta" "10.0.1" \
	    [$CURRENT_VERSION getExpression] \
	    ]
    foreach testValue $testValues {
	$v setExpression $testValue
	lappend results \
		[list \
		[$v getExpression] \
		[$CURRENT_VERSION getExpression] \
		[$v compareTo $CURRENT_VERSION] \
		[$CURRENT_VERSION compareTo $v]]
    }
    # When we build an installer, the version number is 10.0_YYYYMMDD, so we get rid of that.
    regsub -all {_201[0-9][0-9][0-9][0-9]} $results {} results2
    list $results2
} {{{1.0 11.0.devel -1 1} {1.0.0 11.0.devel -1 1} {1.0-beta 11.0.devel -1 1} {2.0 11.0.devel -1 1} {2.0-devel 11.0.devel -1 1} {2.0.alpha 11.0.devel -1 1} {2.0_beta 11.0.devel -1 1} {2.0-build003 11.0.devel -1 1} {2.0-release-1 11.0.devel -1 1} {3.0 11.0.devel -1 1} {3.0-devel 11.0.devel -1 1} {3.0-alpha 11.0.devel -1 1} {3.1 11.0.devel -1 1} {4 11.0.devel -1 1} {4.1 11.0.devel -1 1} {5.0 11.0.devel -1 1} {5.1 11.0.devel -1 1} {5.1-alpha 11.0.devel -1 1} {5.1-beta 11.0.devel -1 1} {5.2 11.0.devel -1 1} {5.2-alpha 11.0.devel -1 1} {5.2-beta 11.0.devel -1 1} {6.0-devel 11.0.devel -1 1} {6.0-alpha 11.0.devel -1 1} {6.0.beta 11.0.devel -1 1} {6.0.1 11.0.devel -1 1} {7.0-devel 11.0.devel -1 1} {7.0-alpha 11.0.devel -1 1} {7.0.beta 11.0.devel -1 1} {7.0.1 11.0.devel -1 1} {8.0-devel 11.0.devel -1 1} {8.0-alpha 11.0.devel -1 1} {8.0.beta 11.0.devel -1 1} {8.0.1 11.0.devel -1 1} {8.1-devel 11.0.devel -1 1} {8.1-alpha 11.0.devel -1 1} {8.1.beta 11.0.devel -1 1} {8.1.1 11.0.devel -1 1} {9.0-devel 11.0.devel -1 1} {9.0-alpha 11.0.devel -1 1} {9.0.beta 11.0.devel -1 1} {9.0.1 11.0.devel -1 1} {9.1-devel 11.0.devel -1 1} {9.1-alpha 11.0.devel -1 1} {9.1.beta 11.0.devel -1 1} {9.1.1 11.0.devel -1 1} {9.2-devel 11.0.devel -1 1} {9.2-alpha 11.0.devel -1 1} {9.2.beta 11.0.devel -1 1} {9.2.1 11.0.devel -1 1} {10.0-devel 11.0.devel -1 1} {10.0-alpha 11.0.devel -1 1} {10.0.beta 11.0.devel -1 1} {10.0.1 11.0.devel -1 1} {11.0.devel 11.0.devel 0 0}}}


test VersionAttribute-3.0 {clone: This used to throw an exception because of NamedObj.clone() was not checking for final fields.} {
    set n [java::new ptolemy.kernel.util.NamedObj "my NamedObj"]
    set va [java::new ptolemy.kernel.attributes.VersionAttribute "2.0-beta"]
    set vaClone [$va clone]
    # Unfortunately, there seem to be problems with ptjacl and accessing
    # static fields of clones, so we can't do this test
    #set vaCurrentVersion [ java::field $va CURRENT_VERSION]
    #set vaCloneCurrentVersion [ java::field $vaClone CURRENT_VERSION]
    #list [$vaClone toString] [$vaCurrentVersion equals $vaCloneCurrentVersion]
    list {}
} {{}}

test VersionAttribute-3.0 {majorCurrentVersion} {
    java::call ptolemy.kernel.attributes.VersionAttribute majorCurrentVersion
} {11.0}

test VersionAttribute-4.0 {Delete a RequireVersion when we have a VersionAttribute and a RequireVersion} {
    # VersionAttribute.equals() has a bug where if we have two VersionAttributes, then only the
    # first one was deleted.
    # http://bugzilla.ecoinformatics.org/show_bug.cgi?id=3984
    set n [java::new ptolemy.kernel.util.NamedObj "my NamedObj"]
    set versionAttribute [java::new ptolemy.kernel.attributes.VersionAttribute $n "versionAttribute"]
    set CURRENT_VERSION [java::field \
	    ptolemy.kernel.attributes.VersionAttribute CURRENT_VERSION]
    $versionAttribute setExpression [$CURRENT_VERSION getExpression]
    set requireVersion [java::new ptolemy.kernel.attributes.RequireVersion $n "requireVersion"]
    $requireVersion setExpression [$CURRENT_VERSION getExpression]
    $requireVersion setContainer [java::null]
    set results [$n exportMoML]
    regsub -all {_201[0-9][0-9][0-9][0-9]} $results {} results2
    list $results2
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="my NamedObj" class="ptolemy.kernel.util.NamedObj">
    <property name="versionAttribute" class="ptolemy.kernel.attributes.VersionAttribute" value="11.0.devel">
    </property>
</entity>
}}
