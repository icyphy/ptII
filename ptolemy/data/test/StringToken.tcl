# Tests for the StringToken class
#
# @Author: Edward A. Lee, Yuhong Xiong
#
# @Version: $Id$
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
test StringToken-2.1 {Create an empty instance} {
    set p [java::new ptolemy.data.StringToken]
    $p toString
} {""}

######################################################################
####
# 
test StringToken-2.2 {Create an empty instance and query its value} {
    set p [java::new ptolemy.data.StringToken]
    $p stringValue
} {}

######################################################################
####
# 
test StringToken-2.3 {Create an non-empty instance} {
    set p [java::new ptolemy.data.StringToken foo]
    list [$p toString]
} {{"foo"}}

######################################################################
####
# 
test StringToken-3.0 {Test adding Strings} {
    set p1 [java::new ptolemy.data.StringToken foo]
    set p2 [java::new ptolemy.data.StringToken bar]
    set res [$p1 add $p2]

    list [$res toString]
} {{"foobar"}}

######################################################################
####
# 
test StringToken-3.1 {Test adding String and boolean} {
    set tok1 [java::new {ptolemy.data.StringToken} foo]
    set tok2 [java::new {ptolemy.data.BooleanToken boolean} true]
    set res1 [$tok1 add $tok2]
    set res2 [$tok1 addReverse $tok2]

    set res3 [$tok2 add $tok1]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {{"footrue"} {"truefoo"} {"truefoo"}}

######################################################################
####
# 
test StringToken-3.2 {Test adding String and long} {
    set tok1 [java::new {ptolemy.data.StringToken} foo]
    set tok2 [java::new {ptolemy.data.LongToken long} 3]
    set res1 [$tok1 add $tok2]
    set res2 [$tok1 addReverse $tok2]

    set res3 [$tok2 add $tok1]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {{"foo3"} {"3foo"} {"3foo"}}

######################################################################
####
# 
test StringToken-3.3 {Test adding String and int} {
    set tok1 [java::new {ptolemy.data.StringToken} foo]
    set tok2 [java::new {ptolemy.data.IntToken int} 4]
    set res1 [$tok1 add $tok2]
    set res2 [$tok1 addReverse $tok2]

    set res3 [$tok2 add $tok1]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {{"foo4"} {"4foo"} {"4foo"}}

######################################################################
####
# 
test StringToken-3.4 {Test adding String and Complex} {
    set tok1 [java::new {ptolemy.data.StringToken} foo]
    set c [java::new {ptolemy.math.Complex double double} 3.3 4.4]
    set tok2 [java::new {ptolemy.data.ComplexToken ptolemy.math.Complex} $c]

    set res1 [$tok1 add $tok2]
    set res2 [$tok1 addReverse $tok2]

    set res3 [$tok2 add $tok1]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {{"foo3.3 + 4.4i"} {"3.3 + 4.4ifoo"} {"3.3 + 4.4ifoo"}}

######################################################################
####
# 
test StringToken-3.5 {Test adding String and double} {
    set tok1 [java::new {ptolemy.data.StringToken} foo]
    set tok2 [java::new {ptolemy.data.DoubleToken double} 2.5]
    set res1 [$tok1 add $tok2]
    set res2 [$tok1 addReverse $tok2]

    set res3 [$tok2 add $tok1]

    list [$res1 toString] [$res2 toString] [$res3 toString]
} {{"foo2.5"} {"2.5foo"} {"2.5foo"}}

######################################################################
####
# 
test StringToken-4.0 {Test isEqualTo} {
    set tok1 [java::new {ptolemy.data.StringToken} foo]
    set tok2 [java::new {ptolemy.data.StringToken} foo]
    set tok3 [java::new {ptolemy.data.StringToken} bar]

    set res1 [$tok1 {isEqualTo ptolemy.data.Token} $tok2]
    set res2 [$tok1 {isEqualTo ptolemy.data.Token} $tok3]
    list [$res1 toString] [$res2 toString]
} {true false}

######################################################################
####
# 
test StringToken-4.0 {Test isEqualTo} {
    set tok1 [java::new {ptolemy.data.StringToken} 33]
    set tok2 [java::new {ptolemy.data.IntToken int} 33]

    catch {[$tok1 {isEqualTo ptolemy.data.Token} $tok2]} msg
    list $msg
} {{ptolemy.kernel.util.IllegalActionException: equality method not supported between ptolemy.data.StringToken and ptolemy.data.IntToken}}
