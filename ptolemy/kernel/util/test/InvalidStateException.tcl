# Tests for the InvalidStateException class
#
# @Author: Christopher Hylands
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

# Tycho test bed, see $TYCHO/doc/coding/testing.html for more information.

# Load up the test definitions.
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
test InvalidStateException-3.1 {Create a InvalidStateException with a \
	detail message} {
    set pe [java::new {ptolemy.kernel.util.InvalidStateException String} \
	    "A message"]
    list [$pe getMessage] [$pe getLocalizedMessage]
} {{A message} {A message}}

######################################################################
####
#
test InvalidStateException-3.2 {Create a InvalidStateException with a \
	null detail message} {
    set pe [java::new {ptolemy.kernel.util.InvalidStateException String} \
	    [java::null]]
    list [$pe getMessage]
} {{}}

######################################################################
####
test InvalidStateException-3.3 {Create a InvalidStateException with a detail \
	message that is not a String} {
    set n1 [java::new ptolemy.kernel.util.NamedObj]
    # We can't check the error message here because Tcl Blend returns
    # a hex number that changes:
    #   expected object of type
    #  java.lang.String but got "java0x222" (ptolemy.kernel.util.NamedObj)
    catch {set pe \
	    [java::new {ptolemy.kernel.util.InvalidStateException String} \
	    $n1]}
} {1}

######################################################################
####
#
test InvalidStateException-5.1 {Create a InvalidStateException with a \
	 NamedObj that has no name and a detail string} {
    set n1 [java::new ptolemy.kernel.util.NamedObj]
    set pe [java::new {ptolemy.kernel.util.InvalidStateException \
	    ptolemy.kernel.util.Nameable String} $n1 "Detail String"]
    list [$pe getMessage]
} {{.: Detail String}}

######################################################################
####
#
test InvalidStateException-5.2 {Create a InvalidStateException with a \
	NamedObj that has a name  and a detail string} {
    set n1 [java::new ptolemy.kernel.util.NamedObj "My NamedObj"]
    set pe [java::new {ptolemy.kernel.util.InvalidStateException \
	    ptolemy.kernel.util.Nameable String} $n1 "Detail String"]
    list [$pe getMessage]
} {{.My NamedObj: Detail String}}

######################################################################
####
#
test InvalidStateException-7.1 {Create a InvalidStateException with an \
	unamed NamedObj and an unamed NamedObj and a detail message} {
    set n1 [java::new ptolemy.kernel.util.NamedObj]
    set n2 [java::new ptolemy.kernel.util.NamedObj]
    set pe [java::new ptolemy.kernel.util.InvalidStateException $n1 $n2 \
	    "Detail Message"]
    list [$pe getMessage]
} {{. and .: Detail Message}}

######################################################################
####
#
test InvalidStateException-7.2 {Create a InvalidStateException with a \
	named NamedObj and an unamed NamedObj and a detail Message} {
    set n1 [java::new ptolemy.kernel.util.NamedObj "NamedObj 1"]
    set n2 [java::new ptolemy.kernel.util.NamedObj]
    set pe [java::new ptolemy.kernel.util.InvalidStateException $n1 $n2 \
	    "Detail Message"]
    list [$pe getMessage]
} {{.NamedObj 1 and .: Detail Message}}

######################################################################
####
#
test InvalidStateException-7.3 {Create a InvalidStateException with an \
	unamed NamedObj and a named NamedObj and a detail message} {
    set n1 [java::new ptolemy.kernel.util.NamedObj]
    set n2 [java::new ptolemy.kernel.util.NamedObj "NamedObj 2"]
    set pe [java::new ptolemy.kernel.util.InvalidStateException $n1 $n2 \
	    "Detail Message"]
    list [$pe getMessage]
} {{. and .NamedObj 2: Detail Message}}

######################################################################
####
#
test InvalidStateException-7.4 {Create a InvalidStateException with a \
	named NamedObj and a named NamedObj and a detail message} {
    set n1 [java::new ptolemy.kernel.util.NamedObj "NamedObj 1"]
    set n2 [java::new ptolemy.kernel.util.NamedObj "NamedObj 2"]
    set pe [java::new ptolemy.kernel.util.InvalidStateException \
	    $n1 $n2 "Detail Message"]
    list [$pe getMessage]
} {{.NamedObj 1 and .NamedObj 2: Detail Message}}

######################################################################
####
#
test InvalidStateException-8.1 {Create a InvalidStateException with a \
    an Enumeration and a String } {
    set dir [java::new ptolemy.kernel.util.NamedList]
    set n1 [java::new ptolemy.kernel.util.NamedObj "n1"]
    set n2 [java::new ptolemy.kernel.util.NamedObj "n2"]
    set n3 [java::new ptolemy.kernel.util.NamedObj "n3"]
    $dir prepend $n1
    $dir prepend $n2
    $dir prepend $n3
    set pe [java::new {ptolemy.kernel.util.InvalidStateException \
	    java.util.Enumeration java.lang.String} \
	    [$dir elements] \
	    "Detail Message"]
    list [$pe getMessage]
} {{.n3, .n2, .n1: Detail Message}}


######################################################################
####
#
test InvalidStateException-9.1 {Test _getName: \
	Create a TestInvalidStateException with a \
	NamedObj that has no name and a detail string } {
    # We use TestInvalidStateException so that we
    # can test InvalidStateException _getName()
    set n1 [java::new ptolemy.kernel.util.NamedObj]
    set pe [java::new {ptolemy.kernel.util.test.TestInvalidStateException \
	    ptolemy.kernel.util.Nameable String} $n1 "Detail String"]
    list [$pe getMessage] [$pe getName $n1]
} {{.: Detail String} {<Unnamed Object>}}

######################################################################
####
#
test InvalidStateException-9.2 {Test _getName: \
	Create a InvalidStateException with a \
	NamedObj that has a name  and a detail string} {
    set n1 [java::new ptolemy.kernel.util.NamedObj "My NamedObj"]
    set pe [java::new {ptolemy.kernel.util.test.TestInvalidStateException \
	    ptolemy.kernel.util.Nameable String} $n1 "Detail String"]
    list [$pe getMessage] [$pe getName $n1]
} {{.My NamedObj: Detail String} {My NamedObj}}

######################################################################
####   Test enumeration constructor
#
test InvalidStateException-10.1 {Create a InvalidStateException with a \
	Enumeration of NamedObjs and no detail message} {
    set n1 [java::new ptolemy.kernel.util.NamedObj "NamedObj 1"]
    set n2 [java::new ptolemy.kernel.util.NamedObj "NamedObj 2"]
    set n3 [java::new ptolemy.kernel.util.NamedObj "NamedObj 3"]
    set ll [java::new java.util.LinkedList]
    $ll addFirst $n3
    $ll addFirst $n2
    $ll addFirst $n1
    set pe [java::new {ptolemy.kernel.util.InvalidStateException \
	    java.util.Enumeration java.lang.String} \
	    [java::call java.util.Collections enumeration $ll ] \
	    [java::null]]
    list [$pe getMessage]
} {{.NamedObj 1, .NamedObj 2, .NamedObj 3: }}

test InvalidStateException-10.2 {Create a InvalidStateException with a \
	Enumeration of NamedObjs and a detail message} {
    set n1 [java::new ptolemy.kernel.util.NamedObj "NamedObj 1"]
    set n2 [java::new ptolemy.kernel.util.NamedObj "NamedObj 2"]
    set n3 [java::new ptolemy.kernel.util.NamedObj "NamedObj 3"]
    set ll [java::new java.util.LinkedList]
    $ll addFirst $n3
    $ll addFirst $n2
    $ll addFirst $n1
    set pe [java::new {ptolemy.kernel.util.InvalidStateException \
	    java.util.Enumeration java.lang.String} \
	    [java::call java.util.Collections enumeration $ll ] \
	    "Detail Message"]
    list [$pe getMessage]
} {{.NamedObj 1, .NamedObj 2, .NamedObj 3: Detail Message}}

test InvalidStateException-10.3 {Create a InvalidStateException with a \
	Enumeration of NamedObjs and Obj,  and a detail message} {
    set n1 [java::new ptolemy.kernel.util.NamedObj "NamedObj 1"]
    set o2 [java::new java.lang.Object]
    set n3 [java::new ptolemy.kernel.util.NamedObj "NamedObj 3"]
    set ll [java::new java.util.LinkedList]
    $ll addFirst $n3
    $ll addFirst $o2
    $ll addFirst $n1
    set pe [java::new {ptolemy.kernel.util.InvalidStateException \
	    java.util.Enumeration java.lang.String} \
	    [java::call java.util.Collections enumeration $ll ] \
	    "Detail Message"]
    list [$pe getMessage]
} {{.NamedObj 1, <Object of class java.lang.Object>, .NamedObj 3: Detail Message}}

test InvalidStateException-11.1 {Create a InvalidStateException with a \
	List of NamedObjs and no detail message} {
    set n1 [java::new ptolemy.kernel.util.NamedObj "NamedObj 1"]
    set n2 [java::new ptolemy.kernel.util.NamedObj "NamedObj 2"]
    set n3 [java::new ptolemy.kernel.util.NamedObj "NamedObj 3"]
    set ll [java::new java.util.LinkedList]
    $ll addFirst $n3
    $ll addFirst $n2
    $ll addFirst $n1
    set pe [java::new {ptolemy.kernel.util.InvalidStateException \
	    java.util.List java.lang.String} \
	    $ll [java::null]]
    list [$pe getMessage]
} {{.NamedObj 1, .NamedObj 2, .NamedObj 3: }}
