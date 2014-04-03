# Test the Test actor.
#
# @Author: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2004-2013 The Regents of the University of California.
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
} {}

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

# We are testing the NonStrict test actor so we set this property
java::call System setProperty ptolemy.ptII.testingMessageHandler "true"

######################################################################
#### Test the Test actor in an SDF model
#
test Test-1.1 {test with the default output values} {
    set e0 [sdfModel 5]
    set ramp [java::new ptolemy.actor.lib.Ramp $e0 ramp]
    set test [java::new ptolemy.actor.lib.Test $e0 test]
    $e0 connect \
	[java::field [java::cast ptolemy.actor.lib.Source $ramp] output] \
	[java::field [java::cast ptolemy.actor.lib.Sink $test] input]
    set trainingMode [getParameter $test trainingMode]
    $trainingMode setExpression "true" 
    puts " The next command will produce a warning about training mode,"
    puts "   which may be ignored."
    [$e0 getManager] execute
    set trainingMode [getParameter $test trainingMode]
    $trainingMode setExpression "false" 
    set correctValues [getParameter $test correctValues]
    list [$correctValues getExpression]
} {{{0, 1, 2, 3, 4}}}

######################################################################
#### 
#
test Test-1.2 {change the step size, force a failure } {
    # Uses 1.1 above

    set step [getParameter $ramp step]
    $step setToken [java::new ptolemy.data.DoubleToken 2.5]

    catch {[$e0 getManager] execute} errMsg

    # Set the step back to the initial default
    set step [getParameter $ramp step]
    $step setToken [java::new ptolemy.data.DoubleToken 1.0]

    #list [string range $errMsg 0 105] 
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Test fails in iteration 1.
Value was: 2.5. Should have been: 1
  in .top.test}}


######################################################################
#### 
#
test Test-1.3 {Adjust the tolerance and run within the tolerance } {
    # Uses 1.1 above

    set tolerance [getParameter $test tolerance]
    set initialTolerance [$tolerance getExpression]
    $tolerance setToken [java::new ptolemy.data.DoubleToken 0.001]

    set step [getParameter $ramp step]
    $step setToken [java::new ptolemy.data.DoubleToken 1.0001]

    # This should work, since we are within the tolerance
    [$e0 getManager] execute

    set step [getParameter $ramp step]
    $step setToken [java::new ptolemy.data.DoubleToken .9999]

    # This should work, since we are within the tolerance
    [$e0 getManager] execute

    set step [getParameter $ramp step]
    $step setToken [java::new ptolemy.data.DoubleToken 1.01]

    # This should fail because we are outside the tolerance
    catch {[$e0 getManager] execute} errMsg

    $step setToken [java::new ptolemy.data.DoubleToken 1.0]

    # Set the tolerance back to the initial value
    $tolerance setToken [java::new ptolemy.data.DoubleToken $initialTolerance]

    list [string range $errMsg 0 106] 
} {{ptolemy.kernel.util.IllegalActionException: Test fails in iteration 1.
Value was: 1.01. Should have been: 1}}


######################################################################
#### 
#
test Test-1.4 {Test actor getting no input.} {
    
    # Create a model that has an unconnected test actor 
    set e4 [sdfModel 5]
    set sdfDirector [$e4 getDirector]

    	
    set allowDisconnectedGraphsParam \
	[getParameter $sdfDirector allowDisconnectedGraphs]
    $allowDisconnectedGraphsParam setToken \
	[java::new ptolemy.data.BooleanToken true]
	
    set ramp4 [java::new ptolemy.actor.lib.Ramp $e4 ramp4]
    set test4 [java::new ptolemy.actor.lib.Test $e4 test4]
    catch {[$e4 getManager] execute} errMsg

    list [string range $errMsg 0 176]
} {{ptolemy.kernel.util.IllegalActionException: Test fails in iteration 0.
Width of input is 0, but correctValues parameter is not an array of arrays.
  in .top.test4}}


######################################################################
#### 
#
test Test-1.4.1 {If fire() is not called, throw an Exception} {
    # It turns out that we can't easily do this in SDF, so we use DE

    # Create a model that has an unconnected test actor 
    set e5 [deModel 5]

    set clock5 [java::new ptolemy.actor.lib.Clock $e5 clock5]
    set test5 [java::new ptolemy.actor.lib.Test $e5 test5]
    catch {[$e5 getManager] execute} errMsg

    list [string range $errMsg 0 176]
} {{ptolemy.kernel.util.IllegalActionException: The fire() method of this actor was never called. Usually, this is an error indicating that starvation is occurring.
  in .top.test5}}

######################################################################
#### 
#
test Test-1.5 {Export} {
    # Uses 1.1 above
    list [$e0 exportMoML]
} {{<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="top" class="ptolemy.actor.TypedCompositeActor">
    <property name="_createdBy" class="ptolemy.kernel.attributes.VersionAttribute" value="9.0.devel">
    </property>
    <property name="" class="ptolemy.domains.sdf.kernel.SDFDirector">
        <property name="localClock" class="ptolemy.actor.LocalClock">
            <property name="globalTimeResolution" class="ptolemy.actor.parameters.SharedParameter" value="1E-10">
            </property>
            <property name="clockRate" class="ptolemy.data.expr.Parameter" value="1.0">
            </property>
        </property>
        <property name="iterations" class="ptolemy.data.expr.Parameter" value="5">
        </property>
        <property name="vectorizationFactor" class="ptolemy.data.expr.Parameter" value="1">
        </property>
        <property name="allowDisconnectedGraphs" class="ptolemy.data.expr.Parameter" value="false">
        </property>
        <property name="allowRateChanges" class="ptolemy.data.expr.Parameter" value="false">
        </property>
        <property name="constrainBufferSizes" class="ptolemy.data.expr.Parameter" value="true">
        </property>
        <property name="period" class="ptolemy.data.expr.Parameter" value="0.0">
        </property>
        <property name="synchronizeToRealTime" class="ptolemy.data.expr.Parameter" value="false">
        </property>
        <property name="startTime" class="ptolemy.data.expr.Parameter">
        </property>
        <property name="stopTime" class="ptolemy.data.expr.Parameter">
        </property>
        <property name="Scheduler" class="ptolemy.domains.sdf.kernel.SDFScheduler">
            <property name="constrainBufferSizes" class="ptolemy.data.expr.Parameter" value="constrainBufferSizes">
            </property>
        </property>
    </property>
    <entity name="ramp" class="ptolemy.actor.lib.Ramp">
        <property name="NONE" class="ptolemy.data.expr.Parameter" value="0">
        </property>
        <property name="init" class="ptolemy.actor.parameters.PortParameter" value="0">
        </property>
        <property name="step" class="ptolemy.actor.parameters.PortParameter" value="1.0">
        </property>
        <property name="firingCountLimit" class="ptolemy.data.expr.Parameter" value="NONE">
        </property>
        <port name="output" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
            <property name="defaultValue" class="ptolemy.data.expr.Parameter">
            </property>
        </port>
        <port name="trigger" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="multiport"/>
            <property name="defaultValue" class="ptolemy.data.expr.Parameter">
            </property>
            <property name="_showName" class="ptolemy.data.expr.SingletonParameter" value="true">
            </property>
        </port>
        <port name="init" class="ptolemy.actor.parameters.ParameterPort">
            <property name="input"/>
            <property name="defaultValue" class="ptolemy.data.expr.Parameter">
            </property>
            <property name="_showName" class="ptolemy.data.expr.Parameter" value="true">
            </property>
        </port>
        <port name="step" class="ptolemy.actor.parameters.ParameterPort">
            <property name="input"/>
            <property name="defaultValue" class="ptolemy.data.expr.Parameter">
            </property>
            <property name="_showName" class="ptolemy.data.expr.Parameter" value="true">
            </property>
        </port>
    </entity>
    <entity name="test" class="ptolemy.actor.lib.Test">
        <property name="correctValues" class="ptolemy.data.expr.Parameter" value="{0, 1, 2, 3, 4}">
        </property>
        <property name="tolerance" class="ptolemy.data.expr.Parameter" value="1.0E-9">
        </property>
        <property name="requireAllCorrectValues" class="ptolemy.actor.parameters.SharedParameter" value="true">
        </property>
        <property name="trainingMode" class="ptolemy.actor.parameters.SharedParameter" value="false">
        </property>
        <port name="input" class="ptolemy.actor.TypedIOPort">
            <property name="input"/>
            <property name="multiport"/>
            <property name="defaultValue" class="ptolemy.data.expr.Parameter">
            </property>
        </port>
        <port name="output" class="ptolemy.actor.TypedIOPort">
            <property name="output"/>
            <property name="defaultValue" class="ptolemy.data.expr.Parameter">
            </property>
        </port>
    </entity>
    <relation name="_R" class="ptolemy.actor.TypedIORelation">
        <property name="width" class="ptolemy.data.expr.Parameter" value="Auto">
        </property>
    </relation>
    <link port="ramp.output" relation="_R"/>
    <link port="test.input" relation="_R"/>
</entity>
}}


######################################################################
#### 
#
test Test-2.1 {Test the Test actor in an SDF model with two ramps} {
    set e1 [sdfModel 5]
    set ramp1 [java::new ptolemy.actor.lib.Ramp $e1 ramp1]
    set ramp2 [java::new ptolemy.actor.lib.Ramp $e1 ramp2]
    set test [java::new ptolemy.actor.lib.Test $e1 test]
    $e1 connect \
	[java::field [java::cast ptolemy.actor.lib.Source $ramp1] output] \
	[java::field [java::cast ptolemy.actor.lib.Sink $test] input]

    $e1 connect \
	[java::field [java::cast ptolemy.actor.lib.Source $ramp2] output] \
	[java::field [java::cast ptolemy.actor.lib.Sink $test] input]

    set step2 [getParameter $ramp2 step]
    $step2 setToken [java::new ptolemy.data.DoubleToken 2.0]

    set trainingMode [getParameter $test trainingMode]
    $trainingMode setExpression "true" 

    puts " The next command will produce a warning about training mode,"
    puts "   which may be ignored."
    [$e1 getManager] execute

    set trainingMode [getParameter $test trainingMode]
    $trainingMode setExpression "false" 

    set correctValues [getParameter $test correctValues]
    list [$correctValues getExpression]
} {{{{0.0, 0.0}, {1.0, 2.0}, {2.0, 4.0}, {3.0, 6.0}, {4.0, 8.0}}}}


######################################################################
#### 
#
test Test-2.2 {Test the Test actor in an SDF model with a const that produces an array} {
    set e22 [sdfModel 2]
    set const [java::new ptolemy.actor.lib.Const $e22 const]
    set test [java::new ptolemy.actor.lib.Test $e22 test]
    $e22 connect \
	[java::field [java::cast ptolemy.actor.lib.Source $const] output] \
	[java::field [java::cast ptolemy.actor.lib.Sink $test] input]


    set value [getParameter $const value]
    $value setToken [java::new ptolemy.data.ArrayToken "{1.0, 2.0, 3.0}"]

    set trainingMode [getParameter $test trainingMode]
    $trainingMode setExpression "true" 

    puts " The next command will produce a warning about training mode,"
    puts "   which may be ignored."
    [$e22 getManager] execute

    set trainingMode [getParameter $test trainingMode]
    $trainingMode setExpression "false" 

    [$e22 getManager] execute
    set correctValues [getParameter $test correctValues]
    list [$correctValues getExpression]
} {{{{{1.0, 2.0, 3.0}}, {{1.0, 2.0, 3.0}}}}}


######################################################################
#### 
#
test Test-2.2.2 {Fail the array test} {
    # Uses 2.2 above
    set value [getParameter $const value]
    $value setToken [java::new ptolemy.data.ArrayToken "{1.0, 42.0, 3.0}"]
    catch {[$e22 getManager] execute} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Test fails in iteration 0.
Value was: {1.0, 42.0, 3.0}. Should have been: {1.0, 2.0, 3.0}
  in .top.test}}


######################################################################
#### 
#
test Test-2.2.3 {Fail the array test with insufficient data} {
    # Uses 2.2 above
    set value [getParameter $const value]
    $value setToken [java::new ptolemy.data.ArrayToken "{1.0, 2.0, 3.0}"]
   
    set correctValues [getParameter $test correctValues]
    $correctValues setExpression "{{{1.0, 2.0, 3.0}}, {{1.0, 2.0, 3.0}}, {{1.0, 2.0, 3.0}}}"
    set requireAllCorrectValues [getParameter $test requireAllCorrectValues]
    $requireAllCorrectValues setExpression "true" 
    puts [$e22 exportMoML]
    catch {[$e22 getManager] execute} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: The test produced only 2 tokens, yet the correctValues parameter was expecting 3 tokens.
  in .top.test}}

######################################################################
#### 
#
test Test-3.0 {Test case where training and we get no data} {
    # Thomas Mandl pointed out this bug and supplied a fix
    set workspace [java::new ptolemy.kernel.util.Workspace "Test3_0"]
    set parser [java::new ptolemy.moml.MoMLParser $workspace]
    $parser reset
    $parser purgeAllModelRecords
    $parser setMoMLFilters [java::null]
    $parser addMoMLFilters \
	    [java::call ptolemy.moml.filter.BackwardCompatibility allFilters]

    $parser addMoMLFilter [java::new \
	    ptolemy.moml.filter.RemoveGraphicalClasses]
    #set model3_0 \
    #	 [java::cast ptolemy.actor.TypedCompositeActor [$parser parseFile BooleanSwitch_RegressionTest.xml]]
    set model3_0 \
	 [java::cast ptolemy.actor.TypedCompositeActor [$parser parseFile BooleanSwitch_RegressionTest_WrongTrainingValues.xml]]
    set manager [java::new ptolemy.actor.Manager $workspace "test3_0Manager"]
    $model3_0 setManager $manager 
    $manager execute

    # Get the corrrectValues parameter, which should be {}
    set test2 [$model3_0 getEntity Test2]
    set correctValues [java::cast ptolemy.data.expr.Parameter [$test2 getAttribute correctValues]]

    list [$correctValues getExpression]

} {{{}}}



# Reset the isRunningNightlyBuild property
java::call System setProperty "ptolemy.ptII.isRunningNightlyBuild" \
    $oldIsRunningNightlyBuild 

# The list of filters is static, so we reset it
java::call ptolemy.moml.MoMLParser setMoMLFilters [java::null]
    
