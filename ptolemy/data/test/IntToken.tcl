# Tests for the IntToken class
#
# @Author: Mudit Goel, Neil Smyth
#
# @Version $Id$
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
test IntToken-1.1 {Get information about the class} {
    # If anything changes, we want to know about it so we can write tests.
    set n [java::new pt.data.IntToken]
    list [getJavaInfo $n]
} {{
  class:         pt.data.IntToken
  fields:        
  methods:       {add pt.data.Token} {addR pt.data.Token} byteValue clon
    e {divide pt.data.Token} {divideR pt.data.Token} double
    Value {equality pt.data.Token} {equals java.lang.Object
    } {fromString java.lang.String} getClass getPublisher g
    etValue hashCode intValue isArray longValue {modulo pt.
    data.Token} {moduloR pt.data.Token} {multiply pt.data.T
    oken} {multiplyR pt.data.Token} notify notifyAll notify
    Subscribers one {setPublisher pt.data.TokenPublisher} {
    setValue int} stringValue {subtract pt.data.Token} {sub
    tractR pt.data.Token} toString wait {wait long} {wait l
    ong int} zero
    
  constructors:  pt.data.IntToken {pt.data.IntToken int}
    
  properties:    array class publisher value
    
  superclass:    pt.data.ScalarToken
    
}}

######################################################################
####
# 
test IntToken-2.1 {Create an empty instance} {
    set p [java::new pt.data.IntToken]
    $p toString
} {pt.data.IntToken(0)}

######################################################################
####
# 
test IntToken-2.2 {Create an empty instance and query its value as int} {
    set p [java::new pt.data.IntToken]
    $p intValue
} {0}

######################################################################
####
# 
test IntToken-2.3 {Create a non-empty instance and query its value as int} {
    set p [java::new {pt.data.IntToken int} 12]
    $p intValue
} {12}

######################################################################
####
# 
test IntToken-3.1 {Create an non-empty instance and read it as double} {
    set p [java::new {pt.data.IntToken int} 12]
    list [$p doubleValue]
} {12.0}

######################################################################
####
# 
test IntToken-3.2 {Create an non-empty instance and read it as long} {
    set p [java::new {pt.data.IntToken int} 12]
    list [$p longValue]
} {12}


######################################################################
####
# 
test IntToken-4.1 {Create an empty instance and clone} {
    set p [java::new pt.data.IntToken]
    set q [$p clone]
    list [$q intValue]
} {0}

######################################################################
####
# 
test IntToken-4.2 {Create a non empty instance and clone} {
    set p [java::new {pt.data.IntToken int} 10]
    set q [$p clone]
    list [$p intValue] [$q intValue]
} {10 10}

