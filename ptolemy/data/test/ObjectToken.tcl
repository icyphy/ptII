# Tests for the ObjectToken class
#
# @Author: Edward A. Lee, Neil Smyth
#
# @Version: $Id$
#
# @Copyright (c) 1997-1998 The Regents of the University of California.
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
test ObjectToken-1.1 {Get information about the class} {
    # If anything changes, we want to know about it so we can write tests.
    set n [java::new pt.data.ObjectToken]
    list [getJavaInfo $n]
} {{
  class:         pt.data.ObjectToken
  fields:        
  methods:       {add pt.data.Token} {addR pt.data.Token} clone {divide 
    pt.data.Token} {divideR pt.data.Token} {equality pt.dat
    a.Token} {equals java.lang.Object} {fromString java.lan
    g.String} getClass getObject getPublisher hashCode isAr
    ray {modulo pt.data.Token} {moduloR pt.data.Token} {mul
    tiply pt.data.Token} {multiplyR pt.data.Token} notify n
    otifyAll notifySubscribers one {setPublisher pt.data.To
    kenPublisher} {setValue java.lang.Object} stringValue {
    subtract pt.data.Token} {subtractR pt.data.Token} toStr
    ing wait {wait long} {wait long int} zero
    
  constructors:  pt.data.ObjectToken {pt.data.ObjectToken java.lang.Obje
    ct}
    
  properties:    array class object publisher value
    
  superclass:    pt.data.Token
    
}}

######################################################################
####
# 
test ObjectToken-2.1 {Create an empty instance} {
    set p [java::new pt.data.ObjectToken]
    $p toString
} {pt.data.ObjectToken}

######################################################################
####
# 
test ObjectToken-2.2 {Create an empty instance and query its value} {
    set p [java::new pt.data.ObjectToken]
    expr { [$p getObject] == [java::null] }
} {1}

######################################################################
####
# 
test ObjectToken-3.1 {Create an empty instance and attempt to init from string} {
    set p [java::new pt.data.ObjectToken]
    catch {$p fromString foo} msg
    list $msg
} {{pt.kernel.util.IllegalActionException: Tokens of class pt.data.ObjectToken cannot be initialized from a string.}}

######################################################################
####
# 
test ObjectToken-4.1 {Create an empty instance and clone} {
    set p [java::new pt.data.ObjectToken]
    set q [$p clone]
    expr { [$q getObject] == [java::null] }
} {1}

######################################################################
####
# 
test ObjectToken-4.2 {Create a non empty instance and clone} {
    set n [java::new {java.lang.StringBuffer String} foo]
    set p [java::new pt.data.ObjectToken $n]
    set q [$p clone]
    list [$p toString] [$q toString]
} {foo foo}

######################################################################
####
# 
test ObjectToken-4.3 {Create a non empty instance, modify object, and clone} {
    set n [java::new {java.lang.StringBuffer String} foo]
    set p [java::new pt.data.ObjectToken $n]
    set q [$p clone]
    $n {append String} " bar"
    list [$p toString] [$q toString]
} {{foo bar} {foo bar}}

