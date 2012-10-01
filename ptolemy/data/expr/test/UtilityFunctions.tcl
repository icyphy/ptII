# Tests for the functionality of functions in UtilityFunctions class.
#
# @author: Neil Smyth
#
# @Version $Id$
#
# @Copyright (c) 1997-2012 The Regents of the University of California.
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
test UtilityFunctions-1.3.1 {Check find(ArrayToken)} {

    # 2nd element is true
    set arrayToken1 [java::new {ptolemy.data.ArrayToken String} "{false, true, false}"]
    set r1 [java::call ptolemy.data.expr.UtilityFunctions find $arrayToken1]

    # No elements are true
    set arrayToken2 [java::new {ptolemy.data.ArrayToken String} "{false}"]
    set r2 [java::call ptolemy.data.expr.UtilityFunctions find $arrayToken2]

    # Empty array has no type, which is an error
    set arrayTokenWrong [java::field ptolemy.data.ArrayToken NIL]
    catch {java::call ptolemy.data.expr.UtilityFunctions find $arrayTokenWrong} errMsg

    # Create an empty ArrayToken with type boolean
    set arrayToken3 [java::new {ptolemy.data.ArrayToken ptolemy.data.type.Type} [[java::field ptolemy.data.BooleanToken TRUE] getType]]
    set r3 [java::call ptolemy.data.expr.UtilityFunctions find $arrayToken3]

    list [$r1 toString] [$r2 toString] $errMsg [$r3 toString]
} {{{1}} {{}} {ptolemy.kernel.util.IllegalActionException: The argument must be an array of boolean tokens.} {{}}}

######################################################################
####
# 
test UtilityFunctions-1.3.2 {Check find(ArrayToken, Token)} {

    # Search for 1
    set arrayToken1 [java::new {ptolemy.data.ArrayToken String} "{0, 1, 2, 3}"]
    set intToken1 [java::field ptolemy.data.IntToken ONE]
    set r1 [java::call ptolemy.data.expr.UtilityFunctions \
	find $arrayToken1 $intToken1]

    # No elements are true
    set intToken42 [java::new ptolemy.data.IntToken 42]
    set r2 [java::call ptolemy.data.expr.UtilityFunctions \
	find $arrayToken1 $intToken42]

    # Empty array has no type, which is ok here
    set valArray [java::new {ptolemy.data.Token[]} 0 ]
    set r3 [java::call ptolemy.data.expr.UtilityFunctions \
	find $arrayTokenWrong $intToken1]

    # Create an empty ArrayToken with type Int
    set arrayToken3 [java::new {ptolemy.data.ArrayToken ptolemy.data.type.Type} [[java::field ptolemy.data.IntToken ONE] getType]]
    set r4 [java::call ptolemy.data.expr.UtilityFunctions find \
	$arrayToken3 $intToken1]

    list [$r1 toString] [$r2 toString] [$r3 toString] [$r4 toString]
} {{{1}} {{}} {{}} {{}}}

######################################################################
####
# 
test UtilityFunctions-1.5 {Check readFile method} {
    set parser [java::new ptolemy.data.expr.PtParser]
    
    set tree [$parser generateParseTree "readFile(\"message.txt\")"]

    #$tree displayParseTree " "
    set res [$tree evaluateParseTree]

    # This hack is necessary because of problems with crnl under windows
    # The file messages.txt is checked in -kkv, so sometimes it has \r\n
    # and sometimes it has \n
    regsub -all {\\r\\n} [$res toString] {\\n} output

    list $output
} {{"Greetings...\n"}}


######################################################################
####
# 
test UtilityFunctions-1.1 {Check readFile method on a file that does not exist} {
    set parser [java::new ptolemy.data.expr.PtParser]
    set tree [$parser generateParseTree "readFile(\"not a file\")"]

    #$tree displayParseTree " "
    catch {set res [$tree evaluateParseTree]} msg
    # Use range here because the last part of the message varies
    # depending on the platform
    list [string range $msg 0 260]
} {{ptolemy.kernel.util.IllegalActionException: Error invoking function public static ptolemy.data.StringToken ptolemy.data.expr.UtilityFunctions.readFile(java.lang.String) throws ptolemy.kernel.util.IllegalActionException

Because:
File not found
Because:
not a fi}}

######################################################################
####
# result is 50 as the string for the re-invoked parser is 3+43+4 !
test UtilityFunctions-3.0 {Check recurive calls to the parser with eval method} {
    set parser [java::new ptolemy.data.expr.PtParser]
    
    set tree [$parser generateParseTree "eval(\"3 + 4\" + \"3 + 4\")"]

    set res [$tree evaluateParseTree]

    set value [$res toString]

    list $value
} {50}

######################################################################
####
test UtilityFunctions-4.0 {Test property} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root [ $p1 {generateParseTree String} "property(\"file.separator\")"]
    set res  [ $root evaluateParseTree ]
    set fileSeparator [$res toString]
    set results "not ok"
    if { "$fileSeparator" == "\"/\"" || "$fileSeparator" == "\"\\\\\""} {
	set results "ok"
    }
    list $results
} {ok}

######################################################################
test UtilityFunctions-4.1 {Test property on a parameter that does not exist} {
    set p1 [java::new ptolemy.data.expr.PtParser]
    set root [ $p1 {generateParseTree String} "property(\"not a parameter\")"]
    set res  [ $root evaluateParseTree ]
    $res toString
} {""}

######################################################################
## MatrixParser tests
test UtilityFunctions-2.0 {Check up on matrices } {
    set parser [java::new ptolemy.data.expr.PtParser]   
    set tree [$parser generateParseTree "\[ 0.0, 3.0; 2.0,  0.0 \]"]
    set matrix [$tree evaluateParseTree]
    $matrix toString
} {[0.0, 3.0; 2.0, 0.0]}

test UtilityFunctions-3.0 {Check readmatrix method} {
    set parser [java::new ptolemy.data.expr.PtParser]   
    set tree [$parser generateParseTree "readMatrix('matrix.mat')"]
    set matrix [$tree evaluateParseTree]
    $matrix toString
} {[0.0, 4.0; 2.0, 0.0]}


test UtilityFunctions-3.1 {Check readMatrix method twice} {
    set parser [java::new ptolemy.data.expr.PtParser]   
    set tree [$parser generateParseTree "readMatrix('matrix.mat')"]
    set tree [$parser generateParseTree "readMatrix('matrix1.mat')"]
    set matrix [$tree evaluateParseTree]
    $matrix toString
} {[1.0, 2.0; 3.0, 4.0]}

######################################################################
####
test UtilityFunctions-5.1 {Test zeroMatrix} {
    set parser [java::new ptolemy.data.expr.PtParser]
    set tree [$parser generateParseTree "zeroMatrix(2,3)"]
    set matrix [$tree evaluateParseTree]
    $matrix toString
} {[0.0, 0.0, 0.0; 0.0, 0.0, 0.0]}

######################################################################
####
test UtilityFunctions-6.1 {Test getenv(String)} {
    set ptIIAsFile [[java::new java.io.File $PTII] getCanonicalFile]
    set ptIIAsURL [$ptIIAsFile toURL]
    set parser [java::new ptolemy.data.expr.PtParser]
    set tree [$parser generateParseTree "getenv(\"PTII\")"]
    set results [$tree evaluateParseTree]
    set stringResults [java::cast ptolemy.data.StringToken $results]
    set ptIIFromEnvironmentAsURL [[[java::new java.io.File [$stringResults stringValue]] getCanonicalFile] toURL]

    puts "UtilityFunctions-6.1: [$ptIIAsURL toString] [$ptIIFromEnvironmentAsURL toString]"

    # Sigh.  When running the junit cobertura code coverage target,
    # ptIIFromEnvironmentAsURL will end in 'reports/instrumented'
    set resourceURLString [$ptIIFromEnvironmentAsURL toString]
    regsub {reports/instrumented/} $resourceURLString {} resourceURLString2
    set resourceURL2 [java::new java.net.URL $resourceURLString2]

    $ptIIAsURL sameFile $resourceURL2
} {1}

######################################################################
####
test UtilityFunctions-6.2 {Test getenv(String), non-existent environment variable} {
    set parser [java::new ptolemy.data.expr.PtParser]
    set tree [$parser generateParseTree "getenv(\"NonExisTaNTEnvVariable\")"]
    set res [$tree evaluateParseTree]
    $res toString
} {""}

######################################################################
####
test UtilityFunctions-7.2 {Test getenv()} {
    set parser [java::new ptolemy.data.expr.PtParser]
    set tree [$parser generateParseTree "getenv()"]
    set results [$tree evaluateParseTree]
    set recordTokenResults [java::cast ptolemy.data.RecordToken $results]
    list [expr {[$recordTokenResults length] > 1}]
} {1}

