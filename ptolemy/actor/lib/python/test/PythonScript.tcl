# Test PythonScript.tcl
#
# @Author: Christopher Hylands, based on Ramp.tcl by Yuhong Xiong
#
# @Version: $Id$
#
# @Copyright (c) 2003-2005 The Regents of the University of California.
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

test PythonScript-2.1 {a ramp with an PythonScript that doubles} {
    # We use a tcl test here so that we can turn on the listeners
    # and look for deadlock problems		

    set e0 [java::new ptolemy.actor.TypedCompositeActor]
    set manager [java::new ptolemy.actor.Manager]
    set director [java::new ptolemy.domains.sdf.kernel.SDFDirector]
    $e0 setDirector $director
    $e0 setName top
    $e0 setManager $manager

    set iterparam [getParameter $director iterations]
    $iterparam setToken [java::new ptolemy.data.IntToken 10]

    #$director addDebugListener [java::new ptolemy.kernel.util.StreamListener]


    set pythonScript [java::new ptolemy.actor.lib.python.PythonScript $e0 PythonScript]
    set input [java::new ptolemy.actor.TypedIOPort $pythonScript \
		   input true false]

    set output [java::new ptolemy.actor.TypedIOPort $pythonScript \
		   output false true]

    set script [java::field $pythonScript script]
    $script setExpression {
class Main :
  "double"
  def fire(self) :
    if not self.input.hasToken(0) :
      return
    t = self.input.get(0)
    self.output.broadcast(t.add(t))}


    set ramp [java::new ptolemy.actor.lib.Ramp $e0 Ramp]
    set firingCountLimit [getParameter $ramp firingCountLimit]
    $firingCountLimit setExpression 5

    set test2 [java::new ptolemy.actor.lib.Test $e0 Test]
    set correctValues [getParameter $test2 correctValues]
    $correctValues setExpression {{0, 2, 4, 6, 8}}

    #$ramp addDebugListener [java::new ptolemy.kernel.util.StreamListener]

    set recorder [java::new ptolemy.kernel.util.RecorderListener]
    $pythonScript addDebugListener $recorder
	

    #$test2 addDebugListener [java::new ptolemy.kernel.util.StreamListener]

    set r1 [java::new ptolemy.actor.TypedIORelation $e0 relation]
    set r2 [java::new ptolemy.actor.TypedIORelation $e0 relation3]

    $input link $r1
    $output link $r2

    [java::field [java::cast ptolemy.actor.lib.Source $ramp] output] link $r1
    [java::field [java::cast ptolemy.actor.lib.Sink $test2] input] \
	link $r2

    # Run it twice
    [$e0 getManager] execute
    [$e0 getManager] execute

    $recorder getMessages
} {Connections changed on port: input
Connections changed on port: output
Called preinitialize()
set up reference to attribute "script" as "script"
set up reference to attribute "_iconDescription" as "_iconDescription"
set up reference to port "input" as "input"
set up reference to port "output" as "output"
Called stopFire()
Added attribute firingsPerIteration to .top.PythonScript
Called initialize()
Called iterate(1)
Called prefire(), which returns true
Called fire()
Called postfire()
Called iterate(1)
Called prefire(), which returns true
Called fire()
Called postfire()
Called iterate(1)
Called prefire(), which returns true
Called fire()
Called postfire()
Called iterate(1)
Called prefire(), which returns true
Called fire()
Called postfire()
Called iterate(1)
Called prefire(), which returns true
Called fire()
Called postfire()
Called wrapup()
Called preinitialize()
set up reference to attribute "script" as "script"
set up reference to attribute "_iconDescription" as "_iconDescription"
set up reference to attribute "firingsPerIteration" as "firingsPerIteration"
set up reference to port "input" as "input"
set up reference to port "output" as "output"
Called stopFire()
Called initialize()
Called iterate(1)
Called prefire(), which returns true
Called fire()
Called postfire()
Called iterate(1)
Called prefire(), which returns true
Called fire()
Called postfire()
Called iterate(1)
Called prefire(), which returns true
Called fire()
Called postfire()
Called iterate(1)
Called prefire(), which returns true
Called fire()
Called postfire()
Called iterate(1)
Called prefire(), which returns true
Called fire()
Called postfire()
Called wrapup()
}


test PythonScript-2.2 {a ramp with an PythonScript that doubles and calls stop} {
    # Uses test 2.1 above
    $script setExpression {
class Main :
  "double"
  def fire(self) :
    if not self.input.hasToken(0) :
      return
    t = self.input.get(0)
    if t.toString() == "2" :
	self.actor.stop()
    self.output.broadcast(t.add(t))}

    $recorder reset
    [$e0 getManager] execute
    $recorder getMessages
} {Called preinitialize()
set up reference to attribute "script" as "script"
set up reference to attribute "_iconDescription" as "_iconDescription"
set up reference to attribute "firingsPerIteration" as "firingsPerIteration"
set up reference to port "input" as "input"
set up reference to port "output" as "output"
Called stopFire()
Called initialize()
Called iterate(1)
Called prefire(), which returns true
Called fire()
Called postfire()
Called iterate(1)
Called prefire(), which returns true
Called fire()
Called postfire()
Called iterate(1)
Called prefire(), which returns true
Called fire()
Called stop()
Called postfire()
Called wrapup()
}


test PythonScript-2.3 {a ramp with an PythonScript that doubles and calls terminate} {
    # Uses test 2.1 above
    $script setExpression {
class Main :
  "double"
  def fire(self) :
    if not self.input.hasToken(0) :
      return
    t = self.input.get(0)
    if t.toString() == "2" :
	self.actor.terminate()
    self.output.broadcast(t.add(t))}

    $recorder reset
    [$e0 getManager] execute
    $recorder getMessages
} {Called preinitialize()
set up reference to attribute "script" as "script"
set up reference to attribute "_iconDescription" as "_iconDescription"
set up reference to attribute "firingsPerIteration" as "firingsPerIteration"
set up reference to port "input" as "input"
set up reference to port "output" as "output"
Called stopFire()
Called initialize()
Called iterate(1)
Called prefire(), which returns true
Called fire()
Called postfire()
Called iterate(1)
Called prefire(), which returns true
Called fire()
Called postfire()
Called iterate(1)
Called prefire(), which returns true
Called fire()
Called terminate()
Called stop()
Called postfire()
Called wrapup()
}


test PythonScript-2.4 {a ramp with an PythonScript that doubles and calls debug} {
    # Uses test 2.1 above
    $script setExpression {
class Main :
  "double"
  def fire(self) :
    if not self.input.hasToken(0) :
      return
    t = self.input.get(0)
    if self.actor.isDebugging() :
       self.actor.debug(t.toString())
    self.output.broadcast(t.add(t))}

    $recorder reset
    [$e0 getManager] execute
    $recorder getMessages
} {Called preinitialize()
set up reference to attribute "script" as "script"
set up reference to attribute "_iconDescription" as "_iconDescription"
set up reference to attribute "firingsPerIteration" as "firingsPerIteration"
set up reference to port "input" as "input"
set up reference to port "output" as "output"
Called stopFire()
Called initialize()
Called iterate(1)
Called prefire(), which returns true
Called fire()
From script:  0
Called postfire()
Called iterate(1)
Called prefire(), which returns true
Called fire()
From script:  1
Called postfire()
Called iterate(1)
Called prefire(), which returns true
Called fire()
From script:  2
Called postfire()
Called iterate(1)
Called prefire(), which returns true
Called fire()
From script:  3
Called postfire()
Called iterate(1)
Called prefire(), which returns true
Called fire()
From script:  4
Called postfire()
Called wrapup()
}

test PythonScript-2.5 {Bogus script, test _reportScriptError()} {
    # Uses test 2.1 above
    $script setExpression {
class Main :
  "double"
  def fire(self) :
    if not self.input.hasToken(0) :
      return
    t = self.input.get(0)
    ThisIsATypo 	
    self.output.broadcast(t.add(t))}

    catch {[$e0 getManager] execute} errMsg
    list $errMsg
} {{ptolemy.kernel.util.IllegalActionException: Error in invoking the fire method:
line 8, in fire
NameError: ThisIsATypo

  in .top.PythonScript
Because:
Traceback (innermost last):
  File "<string>", line 8, in fire
NameError: ThisIsATypo
}}
