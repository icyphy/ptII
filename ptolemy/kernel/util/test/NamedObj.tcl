# Tests for the NamedObj class
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
test NamedObj-1.1 {Get information about an instance of NamedObj} {
    # If anything changes, we want to know about it so we can write tests.
    set n [java::new pt.kernel.NamedObj]
    list [getJavaInfo $n]
} {{
  class:         pt.kernel.NamedObj
  fields:        
  methods:       getClass hashCode {equals java.lang.Object} toString notify notifyAll {wait long} {wait long int} wait getName {setName java.lang.String} getParam
  constructors:  pt.kernel.NamedObj {pt.kernel.NamedObj java.lang.String}
  properties:    class name param
  superclass:    java.lang.Object
}}

######################################################################
####
# 
test NamedObj-2.1 {Create a NamedObj, set the name, change it} {
    set n [java::new pt.kernel.NamedObj]
    set result1 [$n getName]
    $n setName "A Named Obj"
    set result2 [$n getName]
    $n setName "A different Name"
    set result3 [$n getName]
    $n setName {}
    set result4 [$n getName]
    list $result1 $result2 $result3 $result4
} {{} {A Named Obj} {A different Name} {}}

######################################################################
####
# 
test NamedObj-2.2 {Create a NamedObj, set the name, change it} {
    set n [java::new pt.kernel.NamedObj "name set in constructor"]
    set result1 [$n getName]
    $n setName "A Named Obj"
    set result2 [$n getName]
    $n setName "A different Name"
    set result3 [$n getName]
    $n setName {}
    set result4 [$n getName]
    list $result1 $result2 $result3 $result4
} {{name set in constructor} {A Named Obj} {A different Name} {}}


######################################################################
####
# 
test NamedObj-2.3 { Check names with dots} {
    catch {set n [java::new pt.kernel.NamedObj "This.name.has.dots"]} errMsg
    list $errMsg
} {{java.lang.IllegalArgumentException: NamedObj name (This.name.has.dots) has illegal embedded dot (.)}}

######################################################################
####
# 
test NamedObj-3.1 {Experiment with Parameters} {
    set n [java::new pt.kernel.NamedObj]
    set paramlist [ $n getParam]
    list ""
} {{}}
