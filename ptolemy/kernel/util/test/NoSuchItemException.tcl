# Tests for the NoSuchItemException class
#
# @Author: Edward A. Lee
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
test NoSuchItemException-1.1 {Get information about an instance of NoSuchItemException} {
    # If anything changes, we want to know about it so we can write tests.
    set n [java::new pt.kernel.NoSuchItemException dummy]
    list [getJavaInfo $n]
} {{
  class:         pt.kernel.NoSuchItemException
  fields:        
  methods:       {equals java.lang.Object} fillInStackTrace getClass get
    LocalizedMessage getMessage hashCode notify notifyAll p
    rintStackTrace {printStackTrace java.io.PrintStream} {p
    rintStackTrace java.io.PrintWriter} toString wait {wait
     long} {wait long int}
    
  constructors:  {pt.kernel.NoSuchItemException java.lang.String} {pt.ke
    rnel.NoSuchItemException pt.kernel.Nameable java.lang.S
    tring}
    
  properties:    class localizedMessage message
    
  superclass:    pt.kernel.KernelException
    
}}

######################################################################
####
# 
test NoSuchItemException-3.1 {Create a NoSuchItemException with a detail message} {
    set pe [java::new {pt.kernel.NoSuchItemException String} "A message"]
    list [$pe getMessage] [$pe getLocalizedMessage]
} {{A message} {A message}}

######################################################################
####
# 
test NoSuchItemException-3.2 {Create a NoSuchItemException with a null detail message} {
    set pe [java::new {pt.kernel.NoSuchItemException String} [java::null]]
    list [$pe getMessage]
} {{}}

######################################################################
####
# 
# THIS TEST IS NO GOOD.  The string changes on every run.
# test NoSuchItemException-3.3 {Create a NoSuchItemException with a detail message \
# 	that is not a String} {
#     set n1 [java::new pt.kernel.NamedObj]
#     catch {set pe [java::new {pt.kernel.NoSuchItemException String} $n1]} errmsg
#     list $errmsg
# } {{expected object of type java.lang.String but got "java0x2c4" (pt.kernel.NamedObj)}}

######################################################################
####
# 
test NoSuchItemException-5.1 {Create a NoSuchItemException with a NamedObj \
	that has no name and a detail string} {
    set n1 [java::new pt.kernel.NamedObj]
    set pe [java::new {pt.kernel.NoSuchItemException pt.kernel.Nameable String} $n1 "Detail String"]
    list [$pe getMessage]
} {{<Unnamed Object>: Detail String}}

######################################################################
####
# 
test NoSuchItemException-5.2 {Create a NoSuchItemException with a NamedObj \
	that has a name  and a detail string} {
    set n1 [java::new pt.kernel.NamedObj "My NamedObj"]
    set pe [java::new {pt.kernel.NoSuchItemException pt.kernel.Nameable String} $n1 "Detail String"]
    list [$pe getMessage]
} {{My NamedObj: Detail String}}
