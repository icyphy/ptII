# Tests for the InvalidStateException class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1997 The Regents of the University of California.
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
test InvalidStateException-1.1 {Get information about an instance of InvalidStateException} {
    # If anything changes, we want to know about it so we can write tests.
    set n [java::new pt.kernel.InvalidStateException]
    list [getJavaInfo $n]
} {{
  class:         pt.kernel.InvalidStateException
  fields:        
  methods:       {equals java.lang.Object} fillInStackTrace getClass get
    LocalizedMessage getMessage hashCode notify notifyAll p
    rintStackTrace {printStackTrace java.io.PrintStream} {p
    rintStackTrace java.io.PrintWriter} toString wait {wait
     long} {wait long int}
    
  constructors:  pt.kernel.InvalidStateException {pt.kernel.InvalidState
    Exception java.lang.String} {pt.kernel.InvalidStateExce
    ption pt.kernel.Nameable} {pt.kernel.InvalidStateExcept
    ion pt.kernel.Nameable java.lang.String} {pt.kernel.Inv
    alidStateException pt.kernel.Nameable pt.kernel.Nameabl
    e} {pt.kernel.InvalidStateException pt.kernel.Nameable 
    pt.kernel.Nameable java.lang.String}
    
  properties:    class localizedMessage message
    
  superclass:    pt.kernel.KernelException
    
}}

######################################################################
####
# 
test InvalidStateException-2.1 {Create a InvalidStateException} {
    set pe [java::new pt.kernel.InvalidStateException]
    list [$pe getMessage] [$pe getLocalizedMessage]
} {{Serious internal error!} {Serious internal error!}}

######################################################################
####
# 
test InvalidStateException-3.1 {Create a InvalidStateException with a detail message} {
    set pe [java::new {pt.kernel.InvalidStateException String} "A message"]
    list [$pe getMessage] [$pe getLocalizedMessage]
} {{A message} {A message}}

######################################################################
####
# 
test InvalidStateException-3.2 {Create a InvalidStateException with a null detail message} {
    set pe [java::new {pt.kernel.InvalidStateException String} [java::null]]
    list [$pe getMessage]
} {{}}

######################################################################
####
# 
test InvalidStateException-3.3 {Create a InvalidStateException with a detail message \
	that is not a String} {
    set n1 [java::new pt.kernel.NamedObj]
    catch {set pe [java::new {pt.kernel.InvalidStateException String} $n1]} errmsg
    list $errmsg
} {{expected object of type java.lang.String but got "java0x222" (pt.kernel.NamedObj)}}

######################################################################
####
# 
test InvalidStateException-4.1 {Create a InvalidStateException with a Nameable \
	that has no name} {
    set n1 [java::new pt.kernel.NamedObj]
    set pe [java::new {pt.kernel.InvalidStateException pt.kernel.Nameable} $n1]
    list [$pe getMessage]
} {{<Unnamed Object>: Serious internal error!}}

######################################################################
####
# 
test InvalidStateException-4.2 {Create a InvalidStateException with a NamedObj \
	that has a name} {
    set n1 [java::new pt.kernel.NamedObj "My NamedObj"]
    set pe [java::new {pt.kernel.InvalidStateException pt.kernel.Nameable} $n1]
    list [$pe getMessage]
} {{My NamedObj: Serious internal error!}}

######################################################################
####
# 
test InvalidStateException-4.3 {Create a InvalidStateException with a null NamedObj} {
    set pe [java::new {pt.kernel.InvalidStateException pt.kernel.Nameable} \
	    [java::null]]
    list [$pe getMessage]
} {{Serious internal error!}}

######################################################################
####
# 
test InvalidStateException-5.1 {Create a InvalidStateException with a NamedObj \
	that has no name and a detail string} {
    set n1 [java::new pt.kernel.NamedObj]
    set pe [java::new {pt.kernel.InvalidStateException pt.kernel.Nameable String} $n1 "Detail String"]
    list [$pe getMessage]
} {{<Unnamed Object>: Detail String}}

######################################################################
####
# 
test InvalidStateException-5.2 {Create a InvalidStateException with a NamedObj \
	that has a name  and a detail string} {
    set n1 [java::new pt.kernel.NamedObj "My NamedObj"]
    set pe [java::new {pt.kernel.InvalidStateException pt.kernel.Nameable String} $n1 "Detail String"]
    list [$pe getMessage]
} {{My NamedObj: Detail String}}

######################################################################
####
# 
test InvalidStateException-6.1 {Create a InvalidStateException with an unamed NamedObj \
	and an unamed NamedObj} {
    set n1 [java::new pt.kernel.NamedObj]
    set n2 [java::new pt.kernel.NamedObj]
    set pe [java::new \
	    {pt.kernel.InvalidStateException pt.kernel.Nameable pt.kernel.Nameable}\
	    $n1 $n2]
    list [$pe getMessage]
} {{<Unnamed Object> and <Unnamed Object>: Serious internal error!}}

######################################################################
####
# 
test InvalidStateException-6.2 {Create a InvalidStateException with a named NamedObj \
	and an unamed NamedObj} {
    set n1 [java::new pt.kernel.NamedObj "NamedObj 1"]
    set n2 [java::new pt.kernel.NamedObj]
    set pe [java::new \
	    {pt.kernel.InvalidStateException pt.kernel.Nameable pt.kernel.Nameable}\
	    $n1 $n2]
    list [$pe getMessage]
} {{NamedObj 1 and <Unnamed Object>: Serious internal error!}}

######################################################################
####
# 
test InvalidStateException-6.3 {Create a InvalidStateException with an unamed NamedObj \
	and a named NamedObj} {
    set n1 [java::new pt.kernel.NamedObj]
    set n2 [java::new pt.kernel.NamedObj "NamedObj 2"]
    set pe [java::new \
	    {pt.kernel.InvalidStateException pt.kernel.Nameable pt.kernel.Nameable}\
	    $n1 $n2]
    list [$pe getMessage]
} {{<Unnamed Object> and NamedObj 2: Serious internal error!}}

######################################################################
####
# 
test InvalidStateException-6.4 {Create a InvalidStateException with a named NamedObj \
	and a named NamedObj} {
    set n1 [java::new pt.kernel.NamedObj "NamedObj 1"]
    set n2 [java::new pt.kernel.NamedObj "NamedObj 2"]
    set pe [java::new \
	    {pt.kernel.InvalidStateException pt.kernel.Nameable pt.kernel.Nameable}\
	    $n1 $n2]
    list [$pe getMessage]
} {{NamedObj 1 and NamedObj 2: Serious internal error!}}


######################################################################
####
# 
test InvalidStateException-7.1 {Create a InvalidStateException with an unamed NamedObj \
	and an unamed NamedObj and a detail message} {
    set n1 [java::new pt.kernel.NamedObj]
    set n2 [java::new pt.kernel.NamedObj]
    set pe [java::new pt.kernel.InvalidStateException $n1 $n2 "Detail Message"]
    list [$pe getMessage]
} {{<Unnamed Object> and <Unnamed Object>: Detail Message}}

######################################################################
####
# 
test InvalidStateException-7.2 {Create a InvalidStateException with a named NamedObj \
	and an unamed NamedObj and a detail Message} {
    set n1 [java::new pt.kernel.NamedObj "NamedObj 1"]
    set n2 [java::new pt.kernel.NamedObj]
    set pe [java::new pt.kernel.InvalidStateException $n1 $n2 "Detail Message"]
    list [$pe getMessage]
} {{NamedObj 1 and <Unnamed Object>: Detail Message}}

######################################################################
####
# 
test InvalidStateException-7.3 {Create a InvalidStateException with an unamed NamedObj \
	and a named NamedObj and a detail message} {
    set n1 [java::new pt.kernel.NamedObj]
    set n2 [java::new pt.kernel.NamedObj "NamedObj 2"]
    set pe [java::new pt.kernel.InvalidStateException $n1 $n2 "Detail Message"]
    list [$pe getMessage]
} {{<Unnamed Object> and NamedObj 2: Detail Message}}

######################################################################
####
# 
test InvalidStateException-7.4 {Create a InvalidStateException with a named NamedObj \
	and a named NamedObj and a detail message} {
    set n1 [java::new pt.kernel.NamedObj "NamedObj 1"]
    set n2 [java::new pt.kernel.NamedObj "NamedObj 2"]
    set pe [java::new pt.kernel.InvalidStateException $n1 $n2 "Detail Message"]
    list [$pe getMessage]
} {{NamedObj 1 and NamedObj 2: Detail Message}}

