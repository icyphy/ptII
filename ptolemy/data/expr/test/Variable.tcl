# Tests for the Variable class
#
# @Author: Edward A. Lee
#
# @Version $Id$
#
# @Copyright (c) 1997-1999 The Regents of the University of California.
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
test Variable-1.0 {Check constructors} {
    set e [java::new {ptolemy.kernel.Entity String} entity]
    set tok [java::new  {ptolemy.data.DoubleToken double} 4.5]
    set ws [java::new ptolemy.kernel.util.Workspace workspace]

    set param1 [java::new ptolemy.data.expr.Variable]
    set param2 [java::new ptolemy.data.expr.Variable $ws]
    set param4 [java::new ptolemy.data.expr.Variable $e id1]
    set param3 [java::new ptolemy.data.expr.Variable $e id2 $tok]    
    
    set name1 [$param1 getFullName]
    set name2 [$param2 getFullName]    
    set name3 [$param3 getFullName]
    set name4 [$param4 getFullName]
    set value3 [[$param3 getToken] stringValue]
    list $name1 $name2 $name3 $name4 $value3 
} {. workspace. .entity.id2 .entity.id1 4.5}

#################################
####
#
test Variable-2.0 {Check addition of variables to the scope} {
    set e [java::new {ptolemy.kernel.Entity String} parent]
    set tok1 [java::new  {ptolemy.data.IntToken int} 1]
    set tok2 [java::new  {ptolemy.data.IntToken int} 2]
    set param [java::new ptolemy.data.expr.Parameter $e param $tok1]
    set var1 [java::new ptolemy.data.expr.Variable $e var1 $tok2]
    set var2 [java::new ptolemy.data.expr.Variable $e var2]
    $var2 setExpression "param+var1"
    set list [java::new ptolemy.kernel.util.NamedList]
    $list prepend $var1
    $var2 addToScope $list
    $var2 evaluate
    set tok [$var2 getToken]
    $tok stringValue
} {3}

