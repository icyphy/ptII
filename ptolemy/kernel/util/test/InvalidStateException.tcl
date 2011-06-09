# Tests for the InvalidStateException class
#
# @Author: Christopher Hylands
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

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

#
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
} {{Detail String
  in .<Unnamed Object>}}

######################################################################
####
#
test InvalidStateException-5.2 {Create a InvalidStateException with a \
	NamedObj that has a name  and a detail string} {
    set n1 [java::new ptolemy.kernel.util.NamedObj "My NamedObj"]
    set pe [java::new {ptolemy.kernel.util.InvalidStateException \
	    ptolemy.kernel.util.Nameable String} $n1 "Detail String"]
    list [$pe getMessage]
} {{Detail String
  in .My NamedObj}}

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
} {{Detail Message
  in .<Unnamed Object> and .<Unnamed Object>}}

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
} {{Detail Message
  in .NamedObj 1 and .<Unnamed Object>}}

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
} {{Detail Message
  in .<Unnamed Object> and .NamedObj 2}}

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
} {{Detail Message
  in .NamedObj 1 and .NamedObj 2}}

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
} {{Detail Message
  in .n3, .n2, .n1}}


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
} {{  in .NamedObj 1, .NamedObj 2, .NamedObj 3}}

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
} {{Detail Message
  in .NamedObj 1, .NamedObj 2, .NamedObj 3}}

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
} {{Detail Message
  in .NamedObj 1, <Object of class java.lang.Object>, .NamedObj 3}}

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
	    java.util.Collection java.lang.String} \
	    $ll [java::null]]
    list [$pe getMessage]
} {{  in .NamedObj 1, .NamedObj 2, .NamedObj 3}}


 
######################################################################
####
#
test InvalidStateException-12.1 {Create a InvalidStateException with a Nameable, a Cause and a detail message} {
    set n1 [java::new ptolemy.kernel.util.NamedObj "NamedObj 1"]
    set cause [java::new Exception "Cause Exception"]
    set pe [java::new ptolemy.kernel.util.InvalidStateException \
	    $n1 $cause  "Detail Message"]
    list [$pe getMessage]
} {{Detail Message
  in .NamedObj 1
Because:
Cause Exception}}
