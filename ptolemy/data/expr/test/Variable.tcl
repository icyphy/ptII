# Tests for the Variable class
#
# @Author: Edward A. Lee
#
# @Version $Id$
#
# @Copyright (c) 1997-2000 The Regents of the University of California.
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
} {. . .entity.id2 .entity.id1 4.5}

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
    $p1 setExpression  "P3"
    
    catch {$p1 getToken} errormsg1
    catch {$p3 getToken} errormsg2
    list $value1 $value2 $value3 $errormsg1 $errormsg2
} {1.1 9.9 11.0 {ptolemy.kernel.util.IllegalActionException: Found dependency loop when evaluating .E.P1: P3} {ptolemy.kernel.util.IllegalActionException: Found dependency loop when evaluating .E.P3: P1 + P2}}

#################################
####
test Variable-3.0 {First check for no error message} {
    set e [java::new {ptolemy.kernel.Entity String} E]
    set p1 [java::new ptolemy.data.expr.Variable $e P1]
    $p1 setExpression "P2"
} {}

test Variable-3.1 {Next check for reasonable error message} {
    catch {$p1 getToken} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: Error parsing expression "P2":
The ID P2 is undefined.}}

#################################
####
test Variable-4.0 {Check notification} {
    set e [java::new {ptolemy.kernel.Entity String} E]
    set p1 [java::new ptolemy.data.expr.Variable $e P1]
    $p1 setExpression "P2"
    set p2 [java::new ptolemy.data.expr.Variable $e P2]
    $p2 setExpression "1.0"
    set r1 [[$p1 getToken] stringValue]
    $p2 setExpression "2.0"
    set r2 [[$p1 getToken] stringValue]
    list $r1 $r2
} {1.0 2.0}

test Variable-4.1 {Check notification} {
    set e [java::new {ptolemy.kernel.Entity String} E]
    set p1 [java::new ptolemy.data.expr.Variable $e P1]
    $p1 setExpression "P2"
    set p2 [java::new ptolemy.data.expr.Variable $e P2]
    $p2 setExpression "1.0"
    set r1 [[$p1 getToken] stringValue]
    $p2 setToken [java::new ptolemy.data.DoubleToken 2.0]
    set r2 [[$p1 getToken] stringValue]
    list $r1 $r2
} {1.0 2.0}

#################################
####
test Variable-5.0 {Check types} {
    set e [java::new {ptolemy.kernel.Entity String} E]
    set p1 [java::new ptolemy.data.expr.Variable $e P1]
    $p1 setExpression "1.0"
    set r1 [[$p1 getType] toString]
    $p1 setExpression "2"
    set r2 [[$p1 getType] toString]
    $p1 setExpression "3.0i"
    set r3 [[$p1 getType] toString]
    $p1 setToken [java::new ptolemy.data.DoubleToken 2.0]
    set r4 [[$p1 getType] toString]
    $p1 setToken [java::new ptolemy.data.StringToken foo]
    set r5 [[$p1 getType] toString]
    list $r1 $r2 $r3 $r4 $r5
} {double int complex double string}

test Variable-5.1 {Set types without first clearing} {
    set double [java::new ptolemy.data.DoubleToken 0.0]
    set doubleClass [$double getClass]
    catch {$p1 setTypeEquals $doubleClass} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: .E.P1:
Variable.setTypeEquals(): the currently contained token ptolemy.data.StringToken(foo) is not compatible with the desired type double}}

test Variable-5.2 {Set types with first clearing} {
    $p1 setToken [java::null]
    $p1 setTypeEquals $doubleClass
    [$p1 getType] toString
} {double}

test Variable-5.3 {Check return value is null} {
    string compare [$p1 getToken] [java::null]
} {0}

test Variable-5.4 {Check setting expression to null} {
    $p1 setExpression 1
    set r1 [[$p1 getToken] stringValue]
    $p1 setExpression ""
    set int [java::new ptolemy.data.IntToken 0]
    set intClass [$int getClass]
    $p1 setTypeEquals $intClass
    set r2 [$p1 getToken]
    list $r1 [string compare $r2 [java::null]]
} {1.0 0}

#################################
####
test Variable-6.0 {Check addToScope} {
    set e [java::new {ptolemy.kernel.Entity String} E]
    set p1 [java::new ptolemy.data.expr.Variable $e P1]
    $p1 setExpression {"a"}
    set p2 [java::new ptolemy.data.expr.Variable $e P2]
    $p2 setExpression {"b"}
    set v1 [java::new ptolemy.data.expr.Variable]
    $v1 setName "V1"
    $v1 setExpression {"c"}
    set v2 [java::new ptolemy.data.expr.Variable]
    $v2 setName "V2"
    $v2 setExpression "P1+P2+V1"
    $v2 {addToScope java.util.Enumeration} [$e getAttributes]
    $v2 {addToScope ptolemy.data.expr.Variable} $v1
    [$v2 getToken] stringValue
} {abc}

test Variable-6.1 {Check shadowing} {
    set e [java::new {ptolemy.kernel.Entity String} E]
    set p1 [java::new ptolemy.data.expr.Variable $e P1]
    $p1 setExpression {"a"}
    set p2 [java::new ptolemy.data.expr.Variable $e P2]
    $p2 setExpression {"b"}
    set v1 [java::new ptolemy.data.expr.Variable]
    $v1 setName "P1"
    $v1 setExpression {"c"}
    set v2 [java::new ptolemy.data.expr.Variable]
    $v2 setName "V2"
    $v2 setExpression "P1+P2"
    $v2 {addToScope java.util.Enumeration} [$e getAttributes]
    $v2 {addToScope ptolemy.data.expr.Variable} $v1
    [$v2 getToken] stringValue
} {cb}

test Variable-6.2 {Check shadowing} {
    set e [java::new {ptolemy.kernel.Entity String} E]
    set p1 [java::new ptolemy.data.expr.Variable $e P1]
    $p1 setExpression {"a"}
    set p2 [java::new ptolemy.data.expr.Variable $e P2]
    $p2 setExpression {"b"}
    set v1 [java::new ptolemy.data.expr.Variable]
    $v1 setName "P1"
    $v1 setExpression {"c"}
    set v2 [java::new ptolemy.data.expr.Variable]
    $v2 setName "V2"
    $v2 setExpression "P1+P2"
    $v2 {addToScope ptolemy.data.expr.Variable} $v1
    $v2 {addToScope java.util.Enumeration} [$e getAttributes]
    [$v2 getToken] stringValue
} {ab}

test Variable-6.3 {check getScope} {
    set namelist [$v2 getScope]
    enumToFullNames [$namelist elements]
} {.E.P2 .E.P1}

test Variable-6.4 {Check removeFromScope} {
    set e [java::new {ptolemy.kernel.Entity String} E]
    set p1 [java::new ptolemy.data.expr.Variable $e P1]
    $p1 setExpression {"a"}
    set p2 [java::new ptolemy.data.expr.Variable $e P2]
    $p2 setExpression {"b"}
    set v1 [java::new ptolemy.data.expr.Variable]
    $v1 setName "P1"
    $v1 setExpression {"c"}
    set v2 [java::new ptolemy.data.expr.Variable]
    $v2 setName "V2"
    $v2 setExpression "P1+P2"
    $v2 {addToScope java.util.Enumeration} [$e getAttributes]
    $v2 {addToScope ptolemy.data.expr.Variable} $v1
    set r1 [[$v2 getToken] stringValue]
    $v2 {removeFromScope ptolemy.data.expr.Variable} $v1
    catch {[[$v2 getToken] stringValue]} r2
    list $r1 $r2
} {cb {ptolemy.kernel.util.IllegalActionException: Error parsing expression "P1+P2":
The ID P1 is undefined.}}

test Variable-6.5 {Check that removeFromScope does not remove container's variables} {
    set e [java::new {ptolemy.kernel.Entity String} E]
    set p1 [java::new ptolemy.data.expr.Variable $e P1]
    $p1 setExpression {"a"}
    set p2 [java::new ptolemy.data.expr.Variable $e P2]
    $p2 setExpression {P1}
    $p2 getToken
    $p2 {removeFromScope java.util.Enumeration} [$e getAttributes]
    [$p2 getToken] stringValue
} {a}

#################################
####
test Variable-7.0 {Check clone} {
    set e [java::new {ptolemy.kernel.Entity String} E]
    set p1 [java::new ptolemy.data.expr.Variable $e P1]
    $p1 setExpression {"1.0"}
    set p2 [java::cast ptolemy.data.expr.Variable [$p1 clone]]
    set r1 [[$p2 getToken] stringValue]
    set r2 [$p2 getContainer]
    list $r1 [string compare $r2 [java::null]]
} {1.0 0}

test Variable-7.1 {Check clone} {
    set e [java::new {ptolemy.kernel.Entity String} E]
    set p1 [java::new ptolemy.data.expr.Variable $e P1]
    $p1 setExpression P3
    set p3 [java::new ptolemy.data.expr.Variable $e P3]
    $p3 setExpression 3.0
    set p2 [java::cast ptolemy.data.expr.Variable [$p1 clone]]
    $p2 {addToScope ptolemy.data.expr.Variable} $p3
    set r1 [[$p2 getToken] stringValue]
    set r2 [$p2 getContainer]
    list $r1 [string compare $r2 [java::null]]
} {3.0 0}

#################################
####
test Variable-8.0 {Check getExpression} {
    set p1 [java::new ptolemy.data.expr.Variable]
    $p1 setExpression {"1.0"}
    set r1 [$p1 getExpression]
    $p1 setToken [java::new ptolemy.data.DoubleToken 2.0]
    set r2 [$p1 getExpression]
    list $r1 $r2
} {{"1.0"} {}}

#################################
####
test Variable-9.0 {Check reset} {
    set p1 [java::new ptolemy.data.expr.Variable]
    $p1 setExpression {"1.0"}
    set r1 [$p1 getExpression]
    $p1 setToken [java::new ptolemy.data.DoubleToken 2.0]
    set r2 [$p1 getExpression]
    $p1 reset
    set r3 [$p1 getExpression]
    list $r1 $r2 $r3
} {{"1.0"} {} {"1.0"}}

test Variable-9.1 {Check reset} {
    set p1 [java::new ptolemy.data.expr.Variable]
    $p1 setToken [java::new ptolemy.data.DoubleToken 3.0]
    set r1 [$p1 getExpression]
    $p1 setExpression {1.0}
    set r2 [[$p1 getToken] stringValue]
    $p1 reset
    set r3 [$p1 getExpression]
    set r4 [[$p1 getToken] stringValue]
    list $r1 $r2 $r3 $r4
} {{} 1.0 {} 3.0}

#################################
####
test Variable-10.0 {Check setContainer} {
    set e1 [java::new {ptolemy.kernel.Entity String} E1]
    set e2 [java::new {ptolemy.kernel.Entity String} E2]
    set p1 [java::new ptolemy.data.expr.Variable $e1 P1]
    $p1 setExpression {"a"}
    set p2 [java::new ptolemy.data.expr.Variable $e1 P2]
    $p2 setExpression {P1}
    set r1 [[$p2 getToken] stringValue]
    $p1 setContainer $e2
    catch {[$p2 getToken] stringValue} r2
    $p2 setContainer $e2
    set r3 [[$p2 getToken] stringValue]
    list $r1 $r2 $r3
} {a {ptolemy.kernel.util.IllegalActionException: Error parsing expression "P1":
The ID P1 is undefined.} a}

#################################
####
test Variable-11.0 {Check reach of scope} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    set e2 [java::new ptolemy.kernel.ComponentEntity $e1 E2]
    set p1 [java::new ptolemy.data.expr.Variable $e1 P1]
    $p1 setExpression {"a"}
    set p2 [java::new ptolemy.data.expr.Variable $e2 P2]
    $p2 setExpression {P1}
    [$p2 getToken] stringValue
} {a}

######################################################################
####
#
test Variable-12.0 {Test exportMoML} {
    set n [java::new ptolemy.kernel.util.Workspace "N"]
    set a [java::new ptolemy.kernel.util.NamedObj $n "A"]
    set a1 [java::new ptolemy.data.expr.Variable $a "A1"]
    $a exportMoML
} {<?xml version="1.0" standalone="no"?>
<!DOCTYPE model PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/archive/moml.dtd">
<model name="A" class="ptolemy.kernel.util.NamedObj">
</model>
}

