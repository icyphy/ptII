# Tests for the NamedObj class
#
# @Author: Christopher Hylands, Edward A. Lee
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

if {[info procs enumToObjects] == "" } then { 
     source enums.tcl
}

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
  methods:       {addParameter pt.data.Parameter} clone {clone pt.kernel
    .Workspace} {description int} {equals java.lang.Object}
     getClass getContainer getFullName getName {getParamete
    r java.lang.String} getParameters hashCode notify notif
    yAll {removeParameter java.lang.String} {setName java.l
    ang.String} toString wait {wait long} {wait long int} w
    orkspace
    
  constructors:  pt.kernel.NamedObj {pt.kernel.NamedObj java.lang.String
    } {pt.kernel.NamedObj pt.kernel.Workspace java.lang.Str
    ing}
    
  properties:    class container fullName name parameters
    
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
    # In early versions of the kernel, we prohibited names with dots
    # Now, dots are permitted.
    set n [java::new pt.kernel.NamedObj "This.name.has.dots"]
    list [ $n getName]
} {This.name.has.dots}

######################################################################
####
# FIXME:  test addParam, removeParam, getParam, getParams
# test NamedObj-3.1 {Experiment with Parameters} {
#     set n [java::new pt.kernel.NamedObj]
#     set a1 [java::new pt.data.Parameter A1 1]
#     set a2 [java::new pt.data.Parameter A2 2]
#     $n addParameter $a1
#     set result [enumToFullNames [$n getParameters]]
# } {{first parameter} 42 {second parameter} -4}

######################################################################
####
# 
test NamedObj-4.1 {Set the name to null in the constructor} {
    set n [java::new pt.kernel.NamedObj [java::null]]
    $n getName
} {}

######################################################################
####
# 
test NamedObj-4.2 {Set the name to null after construction} {
    set n [java::new pt.kernel.NamedObj "foo"]
    $n setName [java::null]
    $n getName
} {}

######################################################################
####
# 
test NamedObj-5.1 {Test getFullName} {
    set n [java::new pt.kernel.Workspace "foo"]
    set b [java::new pt.kernel.NamedObj $n "bar"]
    list [$n getFullName] [$b getFullName]
} {foo foo.bar}

######################################################################
####
# 
test NamedObj-6.1 {Test toString} {
    set n [java::new pt.kernel.Workspace "foo"]
    set a [java::new pt.kernel.NamedObj]
    set b [java::new pt.kernel.NamedObj $n ""]
    set c [java::new pt.kernel.NamedObj $n "car" ]
    list [$a toString] [$b toString] [$c toString]
} {{pt.kernel.NamedObj {.}} {pt.kernel.NamedObj {foo.}} {pt.kernel.NamedObj {foo.car}}}

######################################################################
####
# 
test NamedObj-6.2 {Test description} {
    set n [java::new pt.kernel.Workspace "foo"]
    set a [java::new pt.kernel.NamedObj]
    set b [java::new pt.kernel.NamedObj $n ""]
    set c [java::new pt.kernel.NamedObj $n "car" ]
    list "[$a description [java::field pt.kernel.Nameable NAME]]\n\
	    [$b description [java::field pt.kernel.Nameable NAME]]\n\
	    [$c description [java::field pt.kernel.Nameable NAME]]\n\
	    [$n description [java::field pt.kernel.Nameable NAME]]"
} {{{.}
 {foo.}
 {foo.car}
 {foo}}}

test NamedObj-6.3 {Test description} {
    set n [java::new pt.kernel.Workspace "foo"]
    set a [java::new pt.kernel.NamedObj]
    set b [java::new pt.kernel.NamedObj $n ""]
    set c [java::new pt.kernel.NamedObj $n "car" ]
    list "[$a description 3]\n\
	    [$b description 3]\n\
	    [$c description 3]\n\
	    [$n description 3]"
} {{pt.kernel.NamedObj {.}
 pt.kernel.NamedObj {foo.}
 pt.kernel.NamedObj {foo.car}
 pt.kernel.Workspace {foo}}}

######################################################################
####
# 
test NamedObj-7.1 {Test clone} {
    set n [java::new pt.kernel.Workspace "N"]
    set a [java::new pt.kernel.NamedObj $n "A" ]
    set b [$a clone]
    $b description 3
} {pt.kernel.NamedObj {N.A}}

######################################################################
####
# 
test NamedObj-8.1 {Test Parameters} {
    set n [java::new pt.kernel.Workspace "N"]
    set a [java::new pt.kernel.NamedObj $n "A" ]
    set p [java::new {pt.data.Parameter pt.kernel.NamedObj java.lang.String java.lang.String} $a "P" 1]
    $a addParameter $p
    set c [$a getParameter "P"]
    expr {$c == $p}
} {1}

# NOTE: Builds on previous example.
test NamedObj-8.2 {Test Parameters} {
    set q [java::new {pt.data.Parameter pt.kernel.NamedObj java.lang.String java.lang.String} $a "Q" 2]
    $a addParameter $q
    enumToFullNames [$a getParameters]
} {N.A.P N.A.Q}

# NOTE: Builds on previous example.
test NamedObj-8.3 {Test Parameters} {
    $a removeParameter P
    enumToFullNames [$a getParameters]
} {N.A.Q}
