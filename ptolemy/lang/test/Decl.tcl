# Tests for the Decl class
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
test Decl-1.1 {make a simple Decl} {
    set decl [java::new ptolemy.lang.test.TestDecl "my Decl" 0]
    list [$decl toString]
} {{{my Decl, 0}}}

######################################################################
####
#
test Decl-1.2 {make a simple Decl with ANY_NAME and CG_ANY Category} {
    set decl [java::new ptolemy.lang.test.TestDecl \
	    [java::field ptolemy.lang.Decl ANY_NAME] \
	    [java::field ptolemy.lang.Decl CG_ANY]]
    list [$decl toString]
} {{{*, -1}}}

######################################################################
####
#
test Decl-2.1 {test equals} {
    set simpleDecl [java::new ptolemy.lang.test.TestDecl "my Decl" 0]
    set anyDecl [java::new ptolemy.lang.test.TestDecl \
	    [java::field ptolemy.lang.Decl ANY_NAME] \
	    [java::field ptolemy.lang.Decl CG_ANY]]
    list [$simpleDecl equals $simpleDecl] \
	    [$anyDecl equals $anyDecl] \
	    [$anyDecl equals $simpleDecl] \
	    [$simpleDecl equals $anyDecl]
} {1 1 0 0}

######################################################################
####
#
test Decl-2.2 {test equals on a non-decl} {
    set simpleDecl [java::new ptolemy.lang.test.TestDecl "my Decl" 0]
    set object [java::new Object]
    catch {$simpleDecl equals $object} result
    list $result
} {{java.lang.RuntimeException: cannot compare a Decl with a non-Decl}}

######################################################################
####
#
test Decl-2.3 {test equals on various names and categoriies} {
    set simpleDecl [java::new ptolemy.lang.test.TestDecl "my Decl" 1]
    set simpleDeclWithSameName [java::new ptolemy.lang.test.TestDecl "my Decl" 1]
    set simpleDeclWithDifferentCategory \
	    [java::new ptolemy.lang.test.TestDecl "my Decl" 2]
    set simpleDeclWithDifferentName \
	    [java::new ptolemy.lang.test.TestDecl "my other Decl" 1]

    set simpleDeclWithCategoryZero \
	    [java::new ptolemy.lang.test.TestDecl "my Decl" 0]
    set anotherSimpleDeclWithCategoryZero \
	    [java::new ptolemy.lang.test.TestDecl "my Decl" 0]

    list \
	    [$simpleDecl equals $simpleDeclWithSameName] \
	    [$simpleDecl equals $simpleDeclWithDifferentCategory] \
	    [$simpleDecl equals $simpleDeclWithDifferentName] \
	    [$simpleDecl equals $simpleDeclWithCategoryZero] \
	    [$simpleDeclWithCategoryZero equals \
	     $anotherSimpleDeclWithCategoryZero]
} {1 0 0 0 1}

######################################################################
####
#
test Decl-2.3 {test equals with CG_ANY} {
    set anyDecl [java::new ptolemy.lang.test.TestDecl \
	    [java::field ptolemy.lang.Decl ANY_NAME] \
	    [java::field ptolemy.lang.Decl CG_ANY]]
    set anotherAnyDecl [java::new ptolemy.lang.test.TestDecl \
	    [java::field ptolemy.lang.Decl ANY_NAME] \
	    [java::field ptolemy.lang.Decl CG_ANY]]
    set simpleDecl [java::new ptolemy.lang.test.TestDecl "my Decl" 1]

    set simpleDeclWithAnyCategory \
	    [java::new ptolemy.lang.test.TestDecl "my Decl" \
	    [java::field ptolemy.lang.Decl CG_ANY]]

    list \
	    [$anyDecl equals $simpleDecl] \
	    [$simpleDecl equals $anyDecl] \
	    [$anyDecl equals $anotherAnyDecl] \
	    [$anyDecl equals $simpleDeclWithSameName] \
	    [$anyDecl equals $simpleDeclWithAnyCategory] \
	    [$simpleDecl equals $simpleDeclWithSameName] \
	    [$simpleDecl equals $simpleDeclWithAnyCategory]
} {1 1 1 1 1 1 1}

######################################################################
####
#
test Decl-3.1 {getName, setName} {
    set simpleDecl [java::new ptolemy.lang.test.TestDecl "my Decl" 0]
    set r1 [$simpleDecl toString]
    set r2 [$simpleDecl getName]
    $simpleDecl setName "new Decl name"
    set r3 [$simpleDecl toString]
    set r4 [$simpleDecl getName]
    list $r1 $r2 $r3 $r4
} {{{my Decl, 0}} {my Decl} {{new Decl name, 0}} {new Decl name}}
