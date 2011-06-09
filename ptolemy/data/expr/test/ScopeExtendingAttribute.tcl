# Tests for the ScopeExtendingAttribute class
#
# @Author: Edward A. Lee
#
# @Version $Id$
#
# @Copyright (c) 1997-2006 The Regents of the University of California.
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

# Ptolemy II test bed, see $PTII/doc/coding/testing.html for more information.

# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then { 
    source testDefs.tcl
} {}

# Uncomment this to get a full report, or set in your Tcl shell window.
# set VERBOSE 1

# 
#


######################################################################
####
# 
test ScopeExtendingAttribute-1.0 {Check constructors} {
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
    set value3 [[$param3 getToken] toString]
    list $name1 $name2 $name3 $name4 $value3 
} {. . .entity.id2 .entity.id1 4.5}

test ScopeExtendingAttribute-2.0 {Changing container of scopeExtendingAttribute must invalidate the scope of variables that might be shadowed in the new scope.} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    set e2 [java::new ptolemy.kernel.ComponentEntity $e1 "e2"]
    set a [java::new ptolemy.data.expr.ScopeExtendingAttribute $e1 "a"]
 
    set p1 [java::new ptolemy.data.expr.Parameter $e1 "p"]
    set p2 [java::new ptolemy.data.expr.Parameter $a "p"]
    set p3 [java::new ptolemy.data.expr.Parameter $e2 "p3"]

    $p1 setExpression "5"
    $p2 setExpression "7"
    $p3 setExpression "p"
    
    catch {set msg1 [[$p3 getToken] toString]} msg1

    catch {$a setContainer $e2} msg2

    catch {set msg3 [[$p3 getToken] toString]} msg3
    
    list $msg1 $msg2 $msg3
} {5 {} 7}

test ScopeExtendingAttribute-2.1 {Changing container of parameter must be aware of any scopeExtendingAttributes.} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    set e2 [java::new ptolemy.kernel.ComponentEntity $e1 "e2"]
    set a [java::new ptolemy.data.expr.ScopeExtendingAttribute $e2 "a"]
 
    set p1 [java::new ptolemy.data.expr.Parameter $e1 "p"]
    set p2 [java::new ptolemy.data.expr.Parameter $a "p"]
    set p3 [java::new ptolemy.data.expr.Parameter $e1 "p3"]

    $p1 setExpression "5"
    $p2 setExpression "7"
    $p3 setExpression "p"
    
    catch {set msg1 [[$p3 getToken] toString]} msg1

    catch {$p3 setContainer $e2} msg2

    catch {set msg3 [[$p3 getToken] toString]} msg3
    
    list $msg1 $msg2 $msg3
} {5 {} 7}

test ScopeExtendingAttribute-2.2 {Changing container of scopeExtendingAttribute must invalidate the scope of variables that are no longer shadowed in the old scope.} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    set e2 [java::new ptolemy.kernel.ComponentEntity $e1 "e2"]
    set a [java::new ptolemy.data.expr.ScopeExtendingAttribute $e2 "a"]
 
    set p1 [java::new ptolemy.data.expr.Parameter $e1 "p"]
    set p2 [java::new ptolemy.data.expr.Parameter $a "p"]
    set p3 [java::new ptolemy.data.expr.Parameter $e2 "p3"]

    $p1 setExpression "5"
    $p2 setExpression "7"
    $p3 setExpression "p"
    
    catch {set msg1 [[$p3 getToken] toString]} msg1

    catch {$a setContainer $e1} msg2
    set msg2b [[$a getContainer] getFullName]

    # Set the container again, so that we get better coverage
    catch {$a setContainer $e1} msg2c
    set msg2d [[$a getContainer] getFullName]

    catch {set msg3 [[$p3 getToken] toString]} msg3
    
    list $msg1 $msg2 $msg2b $msg2c $msg2d $msg3
} {7 {} . {} . 5}

test ScopeExtendingAttribute-2.3 {Changing container of parameter must shadow a parameter inside a scopeExtendingAttribute.} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    set e2 [java::new ptolemy.kernel.ComponentEntity $e1 "e2"]
    set a [java::new ptolemy.data.expr.ScopeExtendingAttribute $e1 "a"]
 
    set p2 [java::new ptolemy.data.expr.Parameter $a "p"]
    set p3 [java::new ptolemy.data.expr.Parameter $e1 "p3"]

    $p2 setExpression "7"
    $p3 setExpression "p"
    
    catch {set msg1 [[$p3 getToken] toString]} msg1

    set p1 [java::new ptolemy.data.expr.Parameter $e1 "p"]
    $p1 setExpression "5"

    catch {set msg3 [[$p3 getToken] toString]} msg3
    
    list $msg1 $msg3
} {7 5}


test ScopeExtendingAttribute-2.4 {Changing container of scopeExtendingAttribute must invalidate the scope of variables that are no longer shadowed in the old scope.} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    set e2 [java::new ptolemy.kernel.ComponentEntity $e1 "e2"]
    set a [java::new ptolemy.data.expr.ScopeExtendingAttribute $e1 "a"]
 
    set p1 [java::new ptolemy.data.expr.Parameter $e1 "p"]
    set p2 [java::new ptolemy.data.expr.Parameter $a "p"]
    set p3 [java::new ptolemy.data.expr.Parameter $e1 "p3"]

    $p1 setExpression "5"
    $p2 setExpression "7"
    $p3 setExpression "p"
    
    catch {set msg1 [[$p3 getToken] toString]} msg1

    catch {$p1 setContainer [java::null]} msg2

    catch {set msg3 [[$p3 getToken] toString]} msg3
    
    list $msg1 $msg2 $msg3
} {5 {} 7}

test ScopeExtendingAttribute-2.5 {Changing container of scopeExtendingAttribute must invalidate the scope of variables that are no longer shadowed in the old scope.--- Including any scopeExtendingAttributes} {
    set e1 [java::new ptolemy.kernel.CompositeEntity]
    set e2 [java::new ptolemy.kernel.ComponentEntity $e1 "e2"]
    set a [java::new ptolemy.data.expr.ScopeExtendingAttribute $e1 "a"]
    set b [java::new ptolemy.data.expr.ScopeExtendingAttribute $e1 "b"]
 
    set p1 [java::new ptolemy.data.expr.Parameter $e1 "p"]
    set p2 [java::new ptolemy.data.expr.Parameter $a "p"]
    set p3 [java::new ptolemy.data.expr.Parameter $e1 "p3"]

    set pb2 [java::new ptolemy.data.expr.Parameter $b "p"]

    $p1 setExpression "5"
    $p2 setExpression "7"
    $p3 setExpression "p"
    $pb2 setExpression "p"
    
    set r1 [list \
		[[$p1 getToken] toString] \
		[[$p2 getToken] toString] \
		[[$p3 getToken] toString] \
		[[$pb2 getToken] toString]]

    # Increase code coverage for cases where the container contains other
    # ScopeExtendingAttributes.
    $a setContainer $b

    set r2 [list \
		[[$p1 getToken] toString] \
		[[$p2 getToken] toString] \
		[[$p3 getToken] toString] \
		[[$pb2 getToken] toString]]

    list $r1 $r2
} {{5 7 5 5} {5 7 5 7}}
