# Tests for the StringUtilities class
#
# @Author: Steve Neuendorffer
#
# @Version: $Id$
#
# @Copyright (c) 1997-2003 The Regents of the University of California.
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

# Load the test definitions.
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

proc checkSubstitute {string old new} {
    set newstring [java::call ptolemy.util.StringUtilities substitute $string $old $new]
    regsub -all $old $string $new checkstring
    string compare $newstring $checkstring
} 

######################################################################
####
#
test StringUtilities-1.1 {substitution checks} {
    checkSubstitute "The quick brown fox jumped over the lazy dog" "fox" "antelope"
} {0}

test StringUtilities-1.2 {substitution checks} {
    checkSubstitute "aa" "a" "amp"
} {0}

test StringUtilities-1.3 {substitution checks} {
    checkSubstitute "aa" "a" "aaa"
} {0}

test StringUtilities-1.4 {substitution checks} {
    checkSubstitute "&&" "&" "aaa"
} {0}

test StringUtilities-1.5 {substitution checks} {
    checkSubstitute "aa" "aa" "aaa"
} {0}

test StringUtilities-1.6 {substitution checks} {
    java::call ptolemy.util.StringUtilities escapeForXML "\"My n&me is <&rf>\""
} {&quot;My n&amp;me is &lt;&amp;rf&gt;&quot;}

test StringUtilities-2.0 {abbreviate short string} {
    java::call ptolemy.util.StringUtilities abbreviate "short string"
} {short string}

test StringUtilities-2.1 {abbreviate long string} {
    java::call ptolemy.util.StringUtilities abbreviate \
	"This string is long, and should be abbreviated, it is more than 80 characters long"
} {This string is long, and should be ab. . .ed, it is more than 80 characters long}


test StringUtilities-2.2 {abbreviate null string} {
    java::call ptolemy.util.StringUtilities abbreviate [java::null]
} {<Unnamed>}

test StringUtilities-3.1 {create a preferences directory} {
    set dir [java::call ptolemy.util.StringUtilities preferencesDirectory]
    file isdirectory $dir	
} {1}


test StringUtilities-4.1 {split short string} {
    java::call ptolemy.util.StringUtilities split "short string"
} {short string}

test StringUtilities-4.2 {split long string} {
    set result [java::call ptolemy.util.StringUtilities split \
	"This string is long, and should be abbreviated, it is more than 80 characters long, is it not?"]
    list $result	
} {{This string is long, and should be abbreviated, it is more than 80 characters
long, is it not?}}

test StringUtilities-4.2.1 {split longer string} {
    set result [java::call ptolemy.util.StringUtilities split \
	"This string is long, and should be abbreviated, it is more than 80 characters long, is it not? And, it has another line, so this long line should be split into at least three smaller lines, right?  I mean right?"]
    list $result	
} {{{This string is long, and should be abbreviated, it is more than 80 characters
long, is it not? And, it has another line, so this long line should be split
into at least three smaller lines, right?  I mean right?}}

test StringUtilities-4.3 {split with null} {
    java::call ptolemy.util.StringUtilities split [java::null]
} {<Unnamed>}


test StringUtilities-5.1 {tokenizeForExec} {
    set command {ls -l "a b" c 'd e' \"f g \" d:\\tmp\\ptII\ 2.0 c:\ptII}
    set results [java::call \
	ptolemy.util.StringUtilities tokenizeForExec $command ]
    $results getrange	
} {ls -l {a b} c 'd e' \\ f\ g\ \\ d:\\\\tmp\\\\ptII\\ 2.0 {c:\ptII}}

test StringUtilities-6.1 {usageString} {
    set commandOptions [java::new {java.lang.String[][]} {2 2} \
        {{{-class}  {<classname>}} \
        {{-<parameter name>} {<parameter value>}}}]

    set commandFlags [java::new {java.lang.String[]} {3} \
	{{-help} {-test} {-version}}]
	
    java::call ptolemy.util.StringUtilities usageString \
	test \
	$commandOptions \
	$commandFlags
} {Usage: test

Options that take values:
 -class <classname>
 -<parameter name> <parameter value>

Boolean flags:
 -help -test -version}
