# Test the Test actor.
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2004-2008 The Regents of the University of California.
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


# Load up the test definitions.
if {[string compare test [info procs test]] == 1} then {
    source testDefs.tcl
}

# Get the value of ptolemy.ptII.isRunningNightlyBuild and save it,
# then reset the property to the empty string.
# If we are running as the nightly build, we usually want to
# throw an exception if the trainingMode parameter is set to true.
# However, while testing the Test actor itself, we want to 
# be able to set the trainingMode parameter to true

set oldIsRunningNightlyBuild \
    [java::call ptolemy.util.StringUtilities getProperty \
     "ptolemy.ptII.isRunningNightlyBuild"]
java::call System setProperty "ptolemy.ptII.isRunningNightlyBuild" ""

######################################################################
#### Test the Test actor in an SDF model
#
test TestExceptionHandler-1.1 {test with the default output values} {
    set e0 [deModel 5]

    set const [java::new ptolemy.actor.lib.Ramp $e0 const]
    set testExceptionHandler [java::new ptolemy.actor.lib.TestExceptionHandler $e0 testExceptionHandler]
    $e0 connect \
	[java::field [java::cast ptolemy.actor.lib.Source $const] output] \
	[java::field [java::cast ptolemy.actor.lib.Source $const] trigger]

    set trainingMode [getParameter $testExceptionHandler trainingMode]
    $trainingMode setExpression "true" 
    puts " The next command will produce a warning about training mode,"
    puts "   which may be ignored."
    [$e0 getManager] execute
    set trainingMode [getParameter $testExceptionHandler trainingMode]
    $trainingMode setExpression "false" 
    list \
	[[getParameter $testExceptionHandler trainingMode] getExpression] \
	[[getParameter $testExceptionHandler correctExceptionMessage] getExpression]
} {false {Found a zero delay loop containing .top.const
  in .top and .top.const}}

######################################################################
#### 
#
test TestExceptionHandler-1.2 {Run again, but with trainingMode false} {
    # Uses 1.1 above
    [$e0 getManager] execute

    list \
	[[getParameter $testExceptionHandler trainingMode] getExpression] \
	[[getParameter $testExceptionHandler correctExceptionMessage] getExpression]
} {false {Found a zero delay loop containing .top.const
  in .top and .top.const}}

######################################################################
#### 
#
test TestExceptionHandler-1.3 {Run again, but with a different Exception} {
    set correctExceptionMessage [getParameter $testExceptionHandler correctExceptionMessage]
    $correctExceptionMessage setExpression {This is not the exception}

    # Uses 1.1 above
    # This fails because we are catching a different exception
    catch {[$e0 getManager] execute} errMsg

    list \
	$errMsg \
	[[getParameter $testExceptionHandler trainingMode] getExpression] \
	[[getParameter $testExceptionHandler correctExceptionMessage] getExpression]
} {{ptolemy.kernel.util.IllegalActionException:   in .top.testExceptionHandler
Because:
Found a zero delay loop containing .top.const
  in .top and .top.const} false {This is not the exception}}

# Reset the isRunningNightlyBuild property
java::call System setProperty "ptolemy.ptII.isRunningNightlyBuild" \
    $oldIsRunningNightlyBuild 
