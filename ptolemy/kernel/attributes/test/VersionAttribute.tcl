# Tests for the Version class
#
# @Author: Christopher Hylands
#
# @Version: $Id$ 
#
# @Copyright (c) 2001-2003 The Regents of the University of California.
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
    set result3 [$v getExpression]
    list $result1 $result2 $result3
} {{ptolemy.kernel.attributes.VersionAttribute {.my NamedObj.my Version}} {ptolemy.kernel.attributes.VersionAttribute {.my NamedObj.my Version}} 4.0-alpha}


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
    list $results
} {{{1.0 4.0-alpha -1 1} {1.0.0 4.0-alpha -1 1} {1.0-beta 4.0-alpha -1 1} {2.0 4.0-alpha -1 1} {2.0-devel 4.0-alpha -1 1} {2.0.alpha 4.0-alpha -1 1} {2.0_beta 4.0-alpha -1 1} {2.0-build003 4.0-alpha -1 1} {2.0-release-1 4.0-alpha -1 1} {3.0 4.0-alpha -1 1} {3.0-devel 4.0-alpha -1 1} {3.0-alpha 4.0-alpha -1 1} {3.1 4.0-alpha -1 1} {4 4.0-alpha -1 1} {4.1 4.0-alpha 1 -1} {5.0 4.0-alpha 1 -1} {4.0-alpha 4.0-alpha 0 0}}}


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
