# Tests for the Parameter class
#
# @Author: Daniel Crawl
#
# @Version $Id$
#
# @Copyright (c) 2012 The Regents of the University of California.
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

# MessageHandler tends to pop up messages about dependencies
java::call System setProperty ptolemy.ptII.batchMode true

######################################################################
#### Renaming tests
#
# see http://bugzilla.ecoinformatics.org/show_bug.cgi?id=5723
#
test Rename-18.1 {Renaming a parameter} {
    set e1 [java::new ptolemy.kernel.Entity]
    set p1 [java::new ptolemy.data.expr.Parameter $e1 "p1"]
    set p2 [java::new ptolemy.data.expr.Parameter $e1 "p2"]
    $p1 setExpression {p2}
    $p1 validate
    set change "<property name=\"p2\">
<rename name=\"p3\"/>
</property>"
    $e1 requestChange [java::new ptolemy.moml.MoMLChangeRequest $e1 $e1 $change]
    $p1 getExpression
} {p3}

test Rename-18.2 {Renaming a referenced parameter in quotes} {
    
    # See " references to parameter are not always renamed"
    # http://bugzilla.ecoinformatics.org/show_bug.cgi?id=5723
    
    # This tests parameter substituion inside double quotes.
    # The currently documented behavior is likely that 
    # parameter substitution does not occur within double quotes.

    set e1 [java::new ptolemy.kernel.Entity]
    set p1 [java::new ptolemy.data.expr.StringParameter $e1 "p1"]
    set p2 [java::new ptolemy.data.expr.StringParameter $e1 "p2"]
    $p1 setExpression {"$p2"}
    $p1 validate
    set r1 [$p1 getExpression]
    set change "<property name=\"p2\">
<rename name=\"p3\"/>
</property>"
    $e1 requestChange [java::new ptolemy.moml.MoMLChangeRequest $e1 $e1 $change]
    set r2 [$p1 getExpression]
    list $r1 $r2
} {{"$p2"} {"$p3"}}

test Rename-18.3 {Renaming a parameter referenced by a parameter in string mode} {

    # See " references to parameter are not always renamed"
    # http://bugzilla.ecoinformatics.org/show_bug.cgi?id=5723

    # This tests parameter substitution in string mode
    # The currently documented behavior of string mode in Variable.java: 

    #  "If the variable is in string mode, then when setting the value
    # of this variable, the string that you pass to
    # setExpression(String) is taken to be literally the value of the
    # instance of StringToken that represents the value of this
    # parameter. It is not necessary"

    set e1 [java::new ptolemy.kernel.Entity]
    set p1 [java::new ptolemy.data.expr.StringParameter $e1 "p1"]
    set p2 [java::new ptolemy.data.expr.StringParameter $e1 "p2"]

    # The only difference between this test and the one above is that this
    # test has no double quotes in the next line.


    $p1 setExpression {$p2}
    $p1 validate
    set r1 [$p1 getExpression]
    set change "<property name=\"p2\">
<rename name=\"p3\"/>
</property>"
    $e1 requestChange [java::new ptolemy.moml.MoMLChangeRequest $e1 $e1 $change]
    set r2 [$p1 getExpression]
    list $r1 $r2
} {{$p2} {$p3}}

test Rename-18.4 {Renaming a parameter in quotes referenced by a parameter in string mode} {
    # See " references to parameter are not always renamed"
    # http://bugzilla.ecoinformatics.org/show_bug.cgi?id=5723

    # This tests parameter substitution in string mode
    # The currently documented behavior of string mode in Variable.java: 

    #  "If the variable is in string mode, then when setting the value
    # of this variable, the string that you pass to
    # setExpression(String) is taken to be literally the value of the
    # instance of StringToken that represents the value of this
    # parameter. It is not necessary"

    set e1 [java::new ptolemy.kernel.Entity]
    set p1 [java::new ptolemy.data.expr.Parameter $e1 "p1"]
    set p2 [java::new ptolemy.data.expr.Parameter $e1 "p2"]
    $p1 setStringMode true
    # This is an string that has $p2 inside, which will not be dereferenced (eal-5/16/13)
    $p1 setExpression {"$p2"}
    $p1 validate
    set r1 [$p1 getExpression]
    set change "<property name=\"p2\">
<rename name=\"p3\"/>
</property>"
    $e1 requestChange [java::new ptolemy.moml.MoMLChangeRequest $e1 $e1 $change]
    list $r1 [$p1 getExpression]
} {{"$p2"} {"$p2"}}
