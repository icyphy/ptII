# Tests for the IllegalActionException class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1997- The Regents of the University of California.
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
test IllegalActionException-1.1 {Get information about an instance of IllegalActionException} {
    # If anything changes, we want to know about it so we can write tests.
    set n [java::new pt.kernel.util.IllegalActionException]
    list [getJavaInfo $n]
} {{
  class:         pt.kernel.util.IllegalActionException
  fields:        
  methods:       {equals java.lang.Object} fillInStackTrace getClass get
    LocalizedMessage getMessage hashCode notify notifyAll p
    rintStackTrace {printStackTrace java.io.PrintStream} {p
    rintStackTrace java.io.PrintWriter} toString wait {wait
     long} {wait long int}
    
  constructors:  pt.kernel.util.IllegalActionException {pt.kernel.util.I
    llegalActionException java.lang.String} {pt.kernel.util
    .IllegalActionException pt.kernel.util.Nameable} {pt.ke
    rnel.util.IllegalActionException pt.kernel.util.Nameabl
    e java.lang.String} {pt.kernel.util.IllegalActionExcept
    ion pt.kernel.util.Nameable pt.kernel.util.Nameable} {p
    t.kernel.util.IllegalActionException pt.kernel.util.Nam
    eable pt.kernel.util.Nameable java.lang.String}
    
  properties:    class localizedMessage message
    
  superclass:    pt.kernel.util.KernelException
    
}}

######################################################################
####
# 
test IllegalActionException-2.1 {Create a IllegalActionException} {
    set pe [java::new pt.kernel.util.IllegalActionException]
    list [$pe getMessage] [$pe getLocalizedMessage]
} {{} {}}

######################################################################
####
# 
test IllegalActionException-3.1 {Create a IllegalActionException with a detail message} {
    set pe [java::new {pt.kernel.util.IllegalActionException String} \
	    "A message"]
    list [$pe getMessage] [$pe getLocalizedMessage]
} {{A message} {A message}}

######################################################################
####
# 
test IllegalActionException-3.2 {Create a IllegalActionException with a null detail message} {
    set pe [java::new {pt.kernel.util.IllegalActionException String} \
	    [java::null]]
    list [$pe getMessage]
} {{}}

######################################################################
####
test IllegalActionException-3.3 {Create a IllegalActionException with a \
	detail message that is not a String} {
    set n1 [java::new pt.kernel.util.NamedObj]
    # We can't check the error message here because Tcl Blend returns
    # a hex number that changes:
    #   expected object of type java.lang.String but 
    #   got "java0x1fc" (pt.kernel.util.NamedObj
    catch {set pe [java::new \
	    {pt.kernel.util.IllegalActionException String} $n1]}
} {1}

######################################################################
####
# 
test IllegalActionException-4.1 {Create a IllegalActionException with a \
	Nameable that has no name} {
    set n1 [java::new pt.kernel.util.NamedObj]
    set pe [java::new {pt.kernel.util.IllegalActionException \
	    pt.kernel.util.Nameable} $n1]
    list [$pe getMessage]
} {.}

######################################################################
####
# 
test IllegalActionException-4.2 {Create a IllegalActionException with a \
	NamedObj that has a name} {
    set n1 [java::new pt.kernel.util.NamedObj "My NamedObj"]
    set pe [java::new {pt.kernel.util.IllegalActionException \
	    pt.kernel.util.Nameable} $n1]
    list [$pe getMessage]
} {{.My NamedObj}}

######################################################################
####
# 
test IllegalActionException-4.3 {Create a IllegalActionException with a \
	null NamedObj} { 
    set pe [java::new {pt.kernel.util.IllegalActionException \
	    pt.kernel.util.Nameable} \
	    [java::null]]
    list [$pe getMessage]
} {{}}

######################################################################
####
# 
test IllegalActionException-5.1 {Create a IllegalActionException with a \
	NamedObj that has no name and a detail string} {
    set n1 [java::new pt.kernel.util.NamedObj]
    set pe [java::new {pt.kernel.util.IllegalActionException \
	    pt.kernel.util.Nameable String} $n1 "Detail String"]
    list [$pe getMessage]
} {{.: Detail String}}

######################################################################
####
# 
test IllegalActionException-5.2 {Create a IllegalActionException with a \
	NamedObj that has a name  and a detail string} {
    set n1 [java::new pt.kernel.util.NamedObj "My NamedObj"]
    set pe [java::new {pt.kernel.util.IllegalActionException \
	    pt.kernel.util.Nameable String} $n1 "Detail String"]
    list [$pe getMessage]
} {{.My NamedObj: Detail String}}

######################################################################
####
# 
test IllegalActionException-6.1 {Create a IllegalActionException with an \
	unamed NamedObj and an unamed NamedObj} {
    set n1 [java::new pt.kernel.util.NamedObj]
    set n2 [java::new pt.kernel.util.NamedObj]
    set pe [java::new {pt.kernel.util.IllegalActionException \
	    pt.kernel.util.Nameable pt.kernel.util.Nameable} \
	    $n1 $n2]
    list [$pe getMessage]
} {{. and .}}

######################################################################
####
# 
test IllegalActionException-6.2 {Create a IllegalActionException with a \
	named NamedObj and an unamed NamedObj} {
    set n1 [java::new pt.kernel.util.NamedObj "NamedObj 1"]
    set n2 [java::new pt.kernel.util.NamedObj]
    set pe [java::new {pt.kernel.util.IllegalActionException \
	    pt.kernel.util.Nameable pt.kernel.util.Nameable} \
	    $n1 $n2]
    list [$pe getMessage]
} {{.NamedObj 1 and .}}

######################################################################
####
# 
test IllegalActionException-6.3 {Create a IllegalActionException with an \
	unamed NamedObj and a named NamedObj} {
    set n1 [java::new pt.kernel.util.NamedObj]
    set n2 [java::new pt.kernel.util.NamedObj "NamedObj 2"]
    set pe [java::new {pt.kernel.util.IllegalActionException \
	    pt.kernel.util.Nameable pt.kernel.util.Nameable} \
	    $n1 $n2]
    list [$pe getMessage]
} {{. and .NamedObj 2}}

######################################################################
####
# 
test IllegalActionException-6.4 {Create a IllegalActionException with a \
	named NamedObj and a named NamedObj} {
    set n1 [java::new pt.kernel.util.NamedObj "NamedObj 1"]
    set n2 [java::new pt.kernel.util.NamedObj "NamedObj 2"]
    set pe [java::new {pt.kernel.util.IllegalActionException \
	    pt.kernel.util.Nameable pt.kernel.util.Nameable} \
	    $n1 $n2]
    list [$pe getMessage]
} {{.NamedObj 1 and .NamedObj 2}}


######################################################################
####
# 
test IllegalActionException-7.1 {Create a IllegalActionException with an \
	unamed NamedObj and an unamed NamedObj and a detail message} {
    set n1 [java::new pt.kernel.util.NamedObj]
    set n2 [java::new pt.kernel.util.NamedObj]
    set pe [java::new pt.kernel.util.IllegalActionException \
	    $n1 $n2 "Detail Message"]
    list [$pe getMessage]
} {{. and .: Detail Message}}

######################################################################
####
# 
test IllegalActionException-7.2 {Create a IllegalActionException with a \
	named NamedObj \
	and an unamed NamedObj and a detail Message} {
    set n1 [java::new pt.kernel.util.NamedObj "NamedObj 1"]
    set n2 [java::new pt.kernel.util.NamedObj]
    set pe [java::new pt.kernel.util.IllegalActionException \
	    $n1 $n2 "Detail Message"]
    list [$pe getMessage]
} {{.NamedObj 1 and .: Detail Message}}

######################################################################
####
# 
test IllegalActionException-7.3 {Create a IllegalActionException with an \
	unamed NamedObj and a named NamedObj and a detail message} {
    set n1 [java::new pt.kernel.util.NamedObj]
    set n2 [java::new pt.kernel.util.NamedObj "NamedObj 2"]
    set pe [java::new pt.kernel.util.IllegalActionException \
	    $n1 $n2 "Detail Message"]
    list [$pe getMessage]
} {{. and .NamedObj 2: Detail Message}}

######################################################################
####
# 
test IllegalActionException-7.4 {Create a IllegalActionException with a \
	named NamedObj and a named NamedObj and a detail message} {
    set n1 [java::new pt.kernel.util.NamedObj "NamedObj 1"]
    set n2 [java::new pt.kernel.util.NamedObj "NamedObj 2"]
    set pe [java::new pt.kernel.util.IllegalActionException \
	    $n1 $n2 "Detail Message"]
    list [$pe getMessage]
} {{.NamedObj 1 and .NamedObj 2: Detail Message}}
