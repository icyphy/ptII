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
#### scope
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
    $var2 {addToScope java.util.Enumeration} [$list elements]
    $var2 evaluate
    set tok [$var2 getToken]
    $tok stringValue
} {3}

test Variable-2.1 {Check scope with sets of params} {
    set e [java::new {ptolemy.kernel.Entity String} E]
    set p1 [java::new ptolemy.data.expr.Parameter $e P1 ]
    set p2 [java::new ptolemy.data.expr.Parameter $e P2 ]
    set p3 [java::new ptolemy.data.expr.Parameter $e P3 ]
    enumToFullNames [[$p3 getScope] elements]
} {.E.P1 .E.P2}

test Variable-2.3 {Check expression evaluation} {
    $p1 setExpression 1.1
    set t [java::new {ptolemy.data.DoubleToken double} 9.9]
    $p2 setExpression 9.8
    $p3 setExpression "P1+P2"
    [$p3 getToken] stringValue
} {10.9}

test Variable-2.4 {Check updating of Variables that refer to others} {
    set e [java::new {ptolemy.kernel.Entity String} E]
    set p1 [java::new ptolemy.data.expr.Variable $e P1]
    $p1 setExpression 1.1

    set tok1 [java::new  {ptolemy.data.DoubleToken double} 9.9]
    set p2 [java::new ptolemy.data.expr.Variable $e P2 $tok1]

    set p3 [java::new ptolemy.data.expr.Variable $e P3]
    $p3 setExpression "P1 + P2"
 
    set name1 [$p1 getFullName]
    set value1 [[$p1 getToken] stringValue]
    set name2 [$p2 getFullName]
    set value2 [[$p2 getToken] stringValue]
    set name3 [$p3 getFullName]
    set value3 [[$p3 getToken] stringValue]
    
    $p1 setExpression  "((true) ? 5.5 : \"string\")"
    set name4 [$p1 getFullName]
    set value4 [[$p1 getToken] stringValue]

    set name5 [$p3 getFullName]
    set value5 [[$p3 getToken] stringValue]

    list $name1 $value1 $name2 $value2 $name3 $value3 $name4 $value4 $name5 $value5 
} {.E.P1 1.1 .E.P2 9.9 .E.P3 11.0 .E.P1 5.5 .E.P3 15.4}

test Variable-2.5 {Check that dependency cycles are flagged as an error} {
    set e [java::new {ptolemy.kernel.Entity String} E]
    set p1 [java::new ptolemy.data.expr.Variable $e P1]
    $p1 setExpression 1.1

    set tok1 [java::new  {ptolemy.data.DoubleToken double} 9.9]
    set p2 [java::new ptolemy.data.expr.Variable $e P2 $tok1]

    set p3 [java::new ptolemy.data.expr.Variable $e P3]
    $p3 setExpression "P1 + P2"
 
    set value1 [[$p1 getToken] stringValue]
    set value2 [[$p2 getToken] stringValue]
    set value3 [[$p3 getToken] stringValue]
    
    catch {$p1 setExpression  "P3"} errormsg
    list $value1 $value2 $value3 $errormsg
} {1.1 9.9 11.0 {ptolemy.data.expr.IllegalExpressionException: Found dependency loop when evaluating .E.P3: P1 + P2}}

#################################
####
test Variable-3.0 {First check for no error message} {
    set e [java::new {ptolemy.kernel.Entity String} E]
    set p1 [java::new ptolemy.data.expr.Variable $e P1]
    $p1 setExpression "P2"
} {}

test Variable-3.1 {Next check for reasonable error message} {
    catch {$p1 evaluate} msg
    list $msg
} {{ptolemy.data.expr.IllegalExpressionException: Error parsing expression "P2":
The ID P2 is undefined.}}

