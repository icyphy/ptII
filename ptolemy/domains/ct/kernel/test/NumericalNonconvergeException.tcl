# Tests for the NumericalNonconvergeException class
#
# @Author: Haiyang Zheng
#
# @Version: $Id$
#
# @Copyright (c) 1998-2005 The Regents of the University of California.
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

######################################################################
####
#
test NumericalNonconvergeException-3.1 {Create a NumericalNonconvergeException with a \
	detail message} {
    set pe [java::new {ptolemy.domains.ct.kernel.NumericalNonconvergeException String} \
	    "A message"]
    list [$pe getMessage] [$pe getLocalizedMessage]
} {{A message} {A message}}

######################################################################
####
#
test NumericalNonconvergeException-3.2 {Create a NumericalNonconvergeException with a \
	null detail message} {
    set pe [java::new {ptolemy.domains.ct.kernel.NumericalNonconvergeException String} \
	    [java::null]]
    list [$pe getMessage]
} {{}}

######################################################################
####
test NumericalNonconvergeException-3.3 {Create a NumericalNonconvergeException with a detail \
	message that is not a String} {
    set n1 [java::new ptolemy.kernel.util.NamedObj]
    # We can't check the error message here because Tcl Blend returns
    # a hex number that changes:
    #   expected object of type
    #  java.lang.String but got "java0x222" (ptolemy.kernel.util.NamedObj)
    catch {set pe \
	    [java::new {ptolemy.kernel.util.NumericalNonconvergeException String} \
	    $n1]}
} {1}

######################################################################
####
#
test NumericalNonconvergeException-5.1 {Create a NumericalNonconvergeException with a \
	 NamedObj that has no name and a detail string} {
    set n1 [java::new ptolemy.kernel.util.NamedObj]
    set pe [java::new {ptolemy.domains.ct.kernel.NumericalNonconvergeException \
	    ptolemy.kernel.util.Nameable String} $n1 "Detail String"]
    list [$pe getMessage]
} {{Detail String
  in .<Unnamed Object>}}

######################################################################
####
#
test NumericalNonconvergeException-5.2 {Create a NumericalNonconvergeException with a \
	NamedObj that has a name  and a detail string} {
    set n1 [java::new ptolemy.kernel.util.NamedObj "My NamedObj"]
    set pe [java::new {ptolemy.domains.ct.kernel.NumericalNonconvergeException \
	    ptolemy.kernel.util.Nameable String} $n1 "Detail String"]
    list [$pe getMessage]
} {{Detail String
  in .My NamedObj}}

######################################################################
####
#
test NumericalNonconvergeException-7.1 {Create a NumericalNonconvergeException with an \
	unamed NamedObj and an unamed NamedObj and a detail message} {
    set n1 [java::new ptolemy.kernel.util.NamedObj]
    set n2 [java::new ptolemy.kernel.util.NamedObj]
    set pe [java::new ptolemy.domains.ct.kernel.NumericalNonconvergeException $n1 $n2 \
	    "Detail Message"]
    list [$pe getMessage]
} {{Detail Message
  in .<Unnamed Object> and .<Unnamed Object>}}

######################################################################
####
#
test NumericalNonconvergeException-7.2 {Create a NumericalNonconvergeException with a \
	named NamedObj and an unamed NamedObj and a detail Message} {
    set n1 [java::new ptolemy.kernel.util.NamedObj "NamedObj 1"]
    set n2 [java::new ptolemy.kernel.util.NamedObj]
    set pe [java::new ptolemy.domains.ct.kernel.NumericalNonconvergeException $n1 $n2 \
	    "Detail Message"]
    list [$pe getMessage]
} {{Detail Message
  in .NamedObj 1 and .<Unnamed Object>}}

######################################################################
####
#
test NumericalNonconvergeException-7.3 {Create a NumericalNonconvergeException with an \
	unamed NamedObj and a named NamedObj and a detail message} {
    set n1 [java::new ptolemy.kernel.util.NamedObj]
    set n2 [java::new ptolemy.kernel.util.NamedObj "NamedObj 2"]
    set pe [java::new ptolemy.domains.ct.kernel.NumericalNonconvergeException $n1 $n2 \
	    "Detail Message"]
    list [$pe getMessage]
} {{Detail Message
  in .<Unnamed Object> and .NamedObj 2}}

######################################################################
####
#
test NumericalNonconvergeException-7.4 {Create a NumericalNonconvergeException with a \
	named NamedObj and a named NamedObj and a detail message} {
    set n1 [java::new ptolemy.kernel.util.NamedObj "NamedObj 1"]
    set n2 [java::new ptolemy.kernel.util.NamedObj "NamedObj 2"]
    set pe [java::new ptolemy.domains.ct.kernel.NumericalNonconvergeException \
	    $n1 $n2 "Detail Message"]
    list [$pe getMessage]
} {{Detail Message
  in .NamedObj 1 and .NamedObj 2}}
