# Tests for the Environ class
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2000 The Regents of the University of California.
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
test Environ-1.1 {Make an empty Environ} {
    set emptyEnv [java::new ptolemy.lang.Environ]
    list [$emptyEnv toString]
} {{[] no parent
}}

######################################################################
####
#
test Environ-1.2 {Make an Environ that contains an environ} {
    set parentEnv [java::new ptolemy.lang.Environ]
    set emptyEnv [java::new ptolemy.lang.Environ $parentEnv]
    list [$emptyEnv toString]
} {{[] has parent
[] no parent
}}

######################################################################
####
#
test Environ-1.3 {Make and Environ with an empty list of Decls} {
    set declList [java::new java.util.LinkedList]
    set parentEnv [java::new ptolemy.lang.Environ]
    set listEnv [java::new ptolemy.lang.Environ $parentEnv $declList]
    list [$listEnv toString]
} {{[] has parent
[] no parent
}}

######################################################################
####
#
test Environ-1.4 {Make an Environ that contains a list of Decls} {
    set anyDecl [java::new ptolemy.lang.Decl \
	    [java::field ptolemy.lang.Decl ANY_NAME] \
	    [java::field ptolemy.lang.Decl CG_ANY]]
    set simpleDecl [java::new ptolemy.lang.Decl "my Decl" 1]
    set simpleDecl2 [java::new ptolemy.lang.Decl "my other Decl" 1]
    set declList [java::new java.util.LinkedList]
    $declList add $anyDecl
    $declList add $simpleDecl
    $declList add $simpleDecl2
    set parentEnv [java::new ptolemy.lang.Environ]
    set listEnv [java::new ptolemy.lang.Environ $parentEnv $declList]
    list [$listEnv toString]
} {{[{*, -1}, {my Decl, 1}, {my other Decl, 1}] has parent
[] no parent
}}


######################################################################
####
#
test Environ-2.1 {Check out parent()} {
    set parentEnv [java::new ptolemy.lang.Environ]
    set emptyEnv [java::new ptolemy.lang.Environ $parentEnv]
    set parent1 [$parentEnv parent] 
    set parent2 [$emptyEnv parent]
    list [java::isnull $parent1] [$parent2 toString]
} {1 {[] no parent
}}

######################################################################
####
#
test Environ-3.1 {add} {
    set declList [java::new java.util.LinkedList]
    set parentEnv [java::new ptolemy.lang.Environ]
    set listEnv [java::new ptolemy.lang.Environ $parentEnv $declList]
    
    set anyDecl [java::new ptolemy.lang.Decl \
	    [java::field ptolemy.lang.Decl ANY_NAME] \
	    [java::field ptolemy.lang.Decl CG_ANY]]
    $listEnv add $anyDecl
    list [$listEnv toString]
} {{[{*, -1}] has parent
[] no parent
}}

######################################################################
####
#
test Environ-4.1 {copyDecl } {
    set declList [java::new java.util.LinkedList]
    set parentEnv [java::new ptolemy.lang.Environ]
    set listEnv [java::new ptolemy.lang.Environ $parentEnv $declList]
    
    set anyDecl [java::new ptolemy.lang.Decl \
	    [java::field ptolemy.lang.Decl ANY_NAME] \
	    [java::field ptolemy.lang.Decl CG_ANY]]
    $listEnv add $anyDecl
    set simpleDecl [java::new ptolemy.lang.Decl "my Decl" 1]
    $listEnv add $simpleDecl
    set r1 [$listEnv toString]

    set newDeclList [java::new java.util.LinkedList]
    set newEnv [java::new ptolemy.lang.Environ $parentEnv $newDeclList]
    set anotherSimpleDecl [java::new ptolemy.lang.Decl "my other Decl" 2]
    $newEnv add $anotherSimpleDecl
    set r2 [$newEnv toString]

    $newEnv copyDeclList $listEnv
    # Note that anotherSimpleDecl should be gone
    list $r1 $r2 [$listEnv toString] [$newEnv toString]
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
test Environ-4.2 {copyDec with envs that share a declList} {
    # This is a little strange, but if two envs share a declList
    # then copyDeclList will effectively set them to null
    set declList [java::new java.util.LinkedList]
    set parentEnv [java::new ptolemy.lang.Environ]
    set listEnv [java::new ptolemy.lang.Environ $parentEnv $declList]
    
    set anyDecl [java::new ptolemy.lang.Decl \
	    [java::field ptolemy.lang.Decl ANY_NAME] \
	    [java::field ptolemy.lang.Decl CG_ANY]]
    $listEnv add $anyDecl
    set simpleDecl [java::new ptolemy.lang.Decl "my Decl" 1]
    $listEnv add $simpleDecl
    set r1 [$listEnv toString]

    set newEnv [java::new ptolemy.lang.Environ $parentEnv $declList]
    set anotherSimpleDecl [java::new ptolemy.lang.Decl "my other Decl" 2]
    $newEnv add $anotherSimpleDecl
    set r2 [$newEnv toString]

    $newEnv copyDeclList $listEnv
    # Note that anotherSimpleDecl should be gone
    list $r1 $r2 [$listEnv toString] [$newEnv toString]
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
test Environ-5.1 {lookup} {
    set declList [java::new java.util.LinkedList]
    set parentEnv [java::new ptolemy.lang.Environ]
    set listEnv [java::new ptolemy.lang.Environ $parentEnv $declList]
    
    set simpleDecl [java::new ptolemy.lang.Decl "my Decl" 1]
    $listEnv add $simpleDecl

    set simpleDeclWithSameName [java::new ptolemy.lang.Decl "my Decl" 1]
    $listEnv add $simpleDeclWithSameName

    set simpleDeclWithDifferentCategory \
	    [java::new ptolemy.lang.Decl "my Decl" 2]
    $listEnv add $simpleDeclWithDifferentCategory \

    set simpleDeclWithDifferentName \
	    [java::new ptolemy.lang.Decl "my other Decl" 1]
    $listEnv add $simpleDeclWithDifferentName

    set simpleDeclWithCategoryZero \
	    [java::new ptolemy.lang.Decl "my Decl" 0]
    $listEnv add $simpleDeclWithCategoryZero

    set anotherSimpleDeclWithCategoryZero \
	    [java::new ptolemy.lang.Decl "my Decl" 0]
    $listEnv add $anotherSimpleDeclWithCategoryZero

    set simpleDeclWithAnyCategory \
	    [java::new ptolemy.lang.Decl "my Decl" \
	    [java::field ptolemy.lang.Decl CG_ANY]]
    $listEnv add $simpleDeclWithAnyCategory

    # Add simpleDecl twice
    $listEnv add $simpleDecl

    # Place this at the end to catch any misses
    set anyDecl [java::new ptolemy.lang.Decl \
	    [java::field ptolemy.lang.Decl ANY_NAME] \
	    [java::field ptolemy.lang.Decl CG_ANY]]
    $listEnv add $anyDecl


    set lookupDecl [$listEnv lookup "my Decl"]
    list [$listEnv toString] \
	    [$lookupDecl toString] \
	    [$lookupDecl equals $simpleDecl] \
	    [$lookupDecl equals $simpleDeclWithSameName]
} {{[{my Decl, 1}, {my Decl, 1}, {my Decl, 2}, {my other Decl, 1}, {my Decl, 0}, {my Decl, 0}, {my Decl, -1}, {my Decl, 1}, {*, -1}] has parent
[] no parent
} {{my Decl, 1}} 1 1} 

######################################################################
####
#
test Environ-5.2 {lookup a decl that does not exist} {
    # Uses setup from Environ-5.1 above
    set lookupDecl [$listEnv lookup "not a Decl"]
    list [$lookupDecl toString] \
	    [$lookupDecl equals $simpleDecl] \
	    [$lookupDecl equals $simpleDeclWithDifferentCategory]
} {{{*, -1}} 1 1}

######################################################################
####
#
test Environ-6.1 {lookup(String, mask)} {
    # Uses setup from Environ-5.1 above
    set lookupDecl [$listEnv {lookup String int} "my Decl" 2]
    list [$lookupDecl toString] \
	    [$lookupDecl equals $simpleDecl] \
	    [$lookupDecl equals $simpleDeclWithDifferentCategory]
} {{{my Decl, 2}} 0 1}


######################################################################
####
#
test Environ-6.2 {lookup(String, more)} {
    # Uses setup from Environ-5.1 above
    set booleanArray [java::new {boolean[]}  {1} {}]
    # Look up myDecl, which exists more than once
    set lookupDecl \
	    [$listEnv {lookup String {boolean[]}} "my Decl" $booleanArray]
    list [$lookupDecl toString] \
	    [$lookupDecl equals $simpleDecl] \
	    [$booleanArray length] \
	    [$booleanArray get 0]
} {{{my Decl, 1}} 1 1 1}

######################################################################
####
#
test Environ-6.3 {lookup(String, mask, more)} {
    # Uses setup from Environ-5.1 above
    set booleanArray [java::new {boolean[]}  {1} {}]
    # Look up my Decl, which exists more than once
    set lookupDecl \
	    [$listEnv {lookup String int {boolean[]}} \
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
test Environ-20.1 {allProperDecls)} {
    # Uses setup from Environ-5.1 above
    set environIter [$listEnv allProperDecls]
    # objectsToStrings and iterToObjects are defined
    # in ptII/util/testsuite/enums.tcl
    list [objectsToStrings [iterToObjects $environIter]]
} {{{{my Decl, 1}} {{my Decl, 1}} {{my Decl, 2}} {{my other Decl, 1}} {{my Decl, 0}} {{my Decl, 0}} {{my Decl, -1}} {{my Decl, 1}} {{*, -1}}}}

######################################################################
####
#
test Environ-21.1 {allProperDecls(mask))} {
    # Uses setup from Environ-5.1 above
    set environIter [$listEnv allProperDecls 1]
    # objectsToStrings and iterToObjects are defined
    # in ptII/util/testsuite/enums.tcl
    list [objectsToStrings [iterToObjects $environIter]]
} {}

######################################################################
####
#
test Environ-21.2 {allProperDecls(mask) with a different mask} {
    # Uses setup from Environ-5.1 above
    set environIter [$listEnv allProperDecls 2]
    # objectsToStrings and iterToObjects are defined
    # in ptII/util/testsuite/enums.tcl
    list [objectsToStrings [iterToObjects $environIter]]
} {}

######################################################################
####
#
test Environ-22.1 {allProperDecls(name))} {
    # Uses setup from Environ-5.1 above
    set environIter [$listEnv allProperDecls "my Decl"]
    # objectsToStrings and iterToObjects are defined
    # in ptII/util/testsuite/enums.tcl
    list [objectsToStrings [iterToObjects $environIter]]
} {{{{my Decl, 1}} {{my Decl, 1}} {{my Decl, 2}} {{my Decl, -1}} {{my Decl, 1}} {{*, -1}}}}


######################################################################
####
#
test Environ-23.1 {moreThanOne(name, mask)} {
    # Uses setup from Environ-5.1 above
    list \
	    [$listEnv moreThanOne "my Decl" 0] \
	    [$listEnv moreThanOne "my Decl" 1] \
	    [$listEnv moreThanOne "my Decl" 2] \
	    [$listEnv moreThanOne "my other Decl" 0] \
	    [$listEnv moreThanOne "my other Decl" 1] \
	    [$listEnv moreThanOne "my other Decl" 2]
} {1 1 1 0 1 0}
