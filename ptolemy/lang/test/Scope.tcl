# Tests for the Scope class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2000-2001 The Regents of the University of California.
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

if {[info procs jdkCapture] == "" } then {
    source [file join $PTII util testsuite jdktools.tcl]
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
test Scope-1.1 {Make an empty Scope} {
    set emptyScope [java::new ptolemy.lang.Scope]
    list [$emptyScope toString]
} {{[] no parent
}}

######################################################################
####
#
test Scope-1.2 {Make an Scope that contains an scope} {
    set parentScope [java::new ptolemy.lang.Scope]
    set emptyScope [java::new ptolemy.lang.Scope $parentScope]
    list [$emptyScope toString]
} {{[] has parent
[] no parent
}}

######################################################################
####
#
test Scope-1.3 {Make and Scope with an empty list of Decls} {
    set declList [java::new java.util.LinkedList]
    set parentScope [java::new ptolemy.lang.Scope]
    set listScope [java::new ptolemy.lang.Scope $parentScope $declList]
    list [$listScope toString]
} {{[] has parent
[] no parent
}}

######################################################################
####
#
test Scope-1.4 {Make an Scope that contains a list of Decls} {
    set anyDecl [java::new ptolemy.lang.test.TestDecl \
	    [java::field ptolemy.lang.Decl ANY_NAME] \
	    [java::field ptolemy.lang.Decl CG_ANY]]
    set simpleDecl [java::new ptolemy.lang.test.TestDecl "my Decl" 1]
    set simpleDecl2 [java::new ptolemy.lang.test.TestDecl "my other Decl" 1]
    set declList [java::new java.util.LinkedList]
    $declList add $anyDecl
    $declList add $simpleDecl
    $declList add $simpleDecl2
    set parentScope [java::new ptolemy.lang.Scope]
    set listScope [java::new ptolemy.lang.Scope $parentScope $declList]
    list [$listScope toString]
} {{[{*, -1}, {my Decl, 1}, {my other Decl, 1}] has parent
[] no parent
}}


######################################################################
####
#
test Scope-2.1 {Check out parent()} {
    set parentScope [java::new ptolemy.lang.Scope]
    set emptyScope [java::new ptolemy.lang.Scope $parentScope]
    set parent1 [$parentScope parent] 
    set parent2 [$emptyScope parent]
    list [java::isnull $parent1] [$parent2 toString]
} {1 {[] no parent
}}

######################################################################
####
#
test Scope-3.1 {add} {
    set declList [java::new java.util.LinkedList]
    set parentScope [java::new ptolemy.lang.Scope]
    set listScope [java::new ptolemy.lang.Scope $parentScope $declList]
    
    set anyDecl [java::new ptolemy.lang.test.TestDecl \
	    [java::field ptolemy.lang.Decl ANY_NAME] \
	    [java::field ptolemy.lang.Decl CG_ANY]]
    $listScope add $anyDecl
    list [$listScope toString]
} {{[{*, -1}] has parent
[] no parent
}}

######################################################################
####
#
test Scope-4.1 {copyDecl } {
    set declList [java::new java.util.LinkedList]
    set parentScope [java::new ptolemy.lang.Scope]
    set listScope [java::new ptolemy.lang.Scope $parentScope $declList]
    
    set anyDecl [java::new ptolemy.lang.test.TestDecl \
	    [java::field ptolemy.lang.Decl ANY_NAME] \
	    [java::field ptolemy.lang.Decl CG_ANY]]
    $listScope add $anyDecl
    set simpleDecl [java::new ptolemy.lang.test.TestDecl "my Decl" 1]
    $listScope add $simpleDecl
    set r1 [$listScope toString]

    set newDeclList [java::new java.util.LinkedList]
    set newScope [java::new ptolemy.lang.Scope $parentScope $newDeclList]
    set anotherSimpleDecl [java::new ptolemy.lang.test.TestDecl "my other Decl" 2]
    $newScope add $anotherSimpleDecl
    set r2 [$newScope toString]

    $newScope copyDeclList $listScope
    # Note that anotherSimpleDecl should be gone
    list $r1 $r2 [$listScope toString] [$newScope toString]
} {{[{*, -1}, {my Decl, 1}] has parent
[] no parent
} {[{my other Decl, 2}] has parent
[] no parent
} {[{*, -1}, {my Decl, 1}] has parent
[] no parent
} {[{*, -1}, {my Decl, 1}] has parent
[] no parent
}}

######################################################################
####
#
test Scope-4.2 {copyDec with scopes that share a declList} {
    # This is a little strange, but if two scopes share a declList
    # then copyDeclList will effectively set them to null
    set declList [java::new java.util.LinkedList]
    set parentScope [java::new ptolemy.lang.Scope]
    set listScope [java::new ptolemy.lang.Scope $parentScope $declList]
    
    set anyDecl [java::new ptolemy.lang.test.TestDecl \
	    [java::field ptolemy.lang.Decl ANY_NAME] \
	    [java::field ptolemy.lang.Decl CG_ANY]]
    $listScope add $anyDecl
    set simpleDecl [java::new ptolemy.lang.test.TestDecl "my Decl" 1]
    $listScope add $simpleDecl
    set r1 [$listScope toString]

    set newScope [java::new ptolemy.lang.Scope $parentScope $declList]
    set anotherSimpleDecl [java::new ptolemy.lang.test.TestDecl "my other Decl" 2]
    $newScope add $anotherSimpleDecl
    set r2 [$newScope toString]

    $newScope copyDeclList $listScope
    # Note that anotherSimpleDecl should be gone
    list $r1 $r2 [$listScope toString] [$newScope toString]
} {{[{*, -1}, {my Decl, 1}] has parent
[] no parent
} {[{*, -1}, {my Decl, 1}, {my other Decl, 2}] has parent
[] no parent
} {[] has parent
[] no parent
} {[] has parent
[] no parent
}}


######################################################################
####
#
test Scope-5.1 {lookup} {
    set declList [java::new java.util.LinkedList]
    set parentScope [java::new ptolemy.lang.Scope]
    set listScope [java::new ptolemy.lang.Scope $parentScope $declList]
    
    set simpleDecl [java::new ptolemy.lang.test.TestDecl "my Decl" 1]
    $listScope add $simpleDecl

    set simpleDeclWithSameName [java::new ptolemy.lang.test.TestDecl "my Decl" 1]
    $listScope add $simpleDeclWithSameName

    set simpleDeclWithDifferentCategory \
	    [java::new ptolemy.lang.test.TestDecl "my Decl" 2]
    $listScope add $simpleDeclWithDifferentCategory \

    set simpleDeclWithDifferentName \
	    [java::new ptolemy.lang.test.TestDecl "my other Decl" 1]
    $listScope add $simpleDeclWithDifferentName

    set simpleDeclWithCategoryZero \
	    [java::new ptolemy.lang.test.TestDecl "my Decl" 0]
    $listScope add $simpleDeclWithCategoryZero

    set anotherSimpleDeclWithCategoryZero \
	    [java::new ptolemy.lang.test.TestDecl "my Decl" 0]
    $listScope add $anotherSimpleDeclWithCategoryZero

    set simpleDeclWithAnyCategory \
	    [java::new ptolemy.lang.test.TestDecl "my Decl" \
	    [java::field ptolemy.lang.Decl CG_ANY]]
    $listScope add $simpleDeclWithAnyCategory

    # Add simpleDecl twice
    $listScope add $simpleDecl

    # Place this at the end to catch any misses
    set anyDecl [java::new ptolemy.lang.test.TestDecl \
	    [java::field ptolemy.lang.Decl ANY_NAME] \
	    [java::field ptolemy.lang.Decl CG_ANY]]
    $listScope add $anyDecl


    set lookupDecl [$listScope lookup "my Decl"]
    list [$listScope toString] \
	    [$lookupDecl toString] \
	    [$lookupDecl equals $simpleDecl] \
	    [$lookupDecl equals $simpleDeclWithSameName]
} {{[{my Decl, 1}, {my Decl, 1}, {my Decl, 2}, {my other Decl, 1}, {my Decl, 0}, {my Decl, 0}, {my Decl, -1}, {my Decl, 1}, {*, -1}] has parent
[] no parent
} {{my Decl, 1}} 1 1} 

######################################################################
####
#
test Scope-5.2 {lookup a decl that does not exist} {
    # Uses setup from Scope-5.1 above
    set lookupDecl [$listScope lookup "not a Decl"]
    list [$lookupDecl toString] \
	    [$lookupDecl equals $simpleDecl] \
	    [$lookupDecl equals $simpleDeclWithDifferentCategory]
} {{{*, -1}} 1 1}

######################################################################
####
#
test Scope-6.1 {lookup(String, mask)} {
    # Uses setup from Scope-5.1 above
    set lookupDecl [$listScope {lookup String int} "my Decl" 2]
    list [$lookupDecl toString] \
	    [$lookupDecl equals $simpleDecl] \
	    [$lookupDecl equals $simpleDeclWithDifferentCategory]
} {{{my Decl, 2}} 0 1}


######################################################################
####
#
test Scope-6.2 {lookup(String, more)} {
    # Uses setup from Scope-5.1 above
    set booleanArray [java::new {boolean[]}  {1} {}]
    # Look up myDecl, which exists more than once
    set lookupDecl \
	    [$listScope {lookup String {boolean[]}} "my Decl" $booleanArray]
    list [$lookupDecl toString] \
	    [$lookupDecl equals $simpleDecl] \
	    [$booleanArray length] \
	    [$booleanArray get 0]
} {{{my Decl, 1}} 1 1 1}

######################################################################
####
#
test Scope-6.3 {lookup(String, mask, more)} {
    # Uses setup from Scope-5.1 above
    set booleanArray [java::new {boolean[]}  {1} {}]
    # Look up my Decl, which exists more than once
    set lookupDecl \
	    [$listScope {lookup String int {boolean[]}} \
	    "my Decl" 2 $booleanArray]
    list [$lookupDecl toString] \
	    [$lookupDecl equals $simpleDecl] \
	    [$lookupDecl equals $simpleDeclWithDifferentCategory] \
	    [$booleanArray length] \
	    [$booleanArray get 0]
} {{{my Decl, 2}} 0 1 1 1}

######################################################################
####
#
test Scope-20.1 {allLocalDecls)} {
    # Uses setup from Scope-5.1 above
    set scopeIter [$listScope allLocalDecls]
    # objectsToStrings and iterToObjects are defined
    # in ptII/util/testsuite/enums.tcl
    list [objectsToStrings [iterToObjects $scopeIter]]
} {{{{my Decl, 1}} {{my Decl, 1}} {{my Decl, 2}} {{my other Decl, 1}} {{my Decl, 0}} {{my Decl, 0}} {{my Decl, -1}} {{my Decl, 1}} {{*, -1}}}}

######################################################################
####
#
test Scope-21.1 {allLocalDecls(mask))} {
    # Uses setup from Scope-5.1 above
    set scopeIter [$listScope allLocalDecls 1]
    # objectsToStrings and iterToObjects are defined
    # in ptII/util/testsuite/enums.tcl
    list [objectsToStrings [iterToObjects $scopeIter]]
} {{{{*, -1}}}}

######################################################################
####
#
test Scope-21.2 {allLocalDecls(mask) with a different mask} {
    # Uses setup from Scope-5.1 above
    set scopeIter [$listScope allLocalDecls 2]
    # objectsToStrings and iterToObjects are defined
    # in ptII/util/testsuite/enums.tcl
    list [objectsToStrings [iterToObjects $scopeIter]]
} {{{{*, -1}}}}

######################################################################
####
#
test Scope-22.1 {allLocalDecls(name))} {
    # Uses setup from Scope-5.1 above
    set scopeIter [$listScope allLocalDecls "my Decl"]
    # objectsToStrings and iterToObjects are defined
    # in ptII/util/testsuite/enums.tcl
    list [objectsToStrings [iterToObjects $scopeIter]]
} {{{{my Decl, 1}} {{my Decl, 1}} {{my Decl, 2}} {{my Decl, -1}} {{my Decl, 1}} {{*, -1}}}}


######################################################################
####
#
test Scope-23.1 {moreThanOne(name, mask)} {
    # Uses setup from Scope-5.1 above
    list \
	    [$listScope moreThanOne "my Decl" 0] \
	    [$listScope moreThanOne "my Decl" 1] \
	    [$listScope moreThanOne "my Decl" 2] \
	    [$listScope moreThanOne "my other Decl" 0] \
	    [$listScope moreThanOne "my other Decl" 1] \
	    [$listScope moreThanOne "my other Decl" 2]
} {1 1 1 0 1 0}
