$Id$
Models for the book:

Claudius Ptolemaeus, Editor, System Design, Modeling, and Simulation Using Ptolemy II, Ptolemy.org, 2013.

As of July, 2013, the book is not yet available.


Below are models that have problems running.

#####
Perhaps models that have InteractiveShells should not be run?

discreteevents/InteractiveShell.xml
discreteevents/ThreadedComposite.xml
export/ChatClient.xml
export/ChatClient2.xml
export/ChatClient3.xml
modal/ErrorTransition.xml
modal/RefinementOutput.xml
modal/Termination.xml
pn/AreYouReady.xml
pn/ChatClient.xml
pn/LocalChat.xml
pn/RemoteChat.xml
types/ExpressionEvaluator.xml
types/ExpressionEvaluatorBackward.xml
types/ExpressionEvaluatorCoerced.xml
types/ExpressionEvaluatorGeneral.xml
types/ExpressionEvaluatorTighter.xml
types/ExpressionEvaluatorTypeConflict.xml

ExportModel invokes invokeAndWait, as does the InteractiveShell.

#####
doc/books/systems/continuous/ContinuousInsideDESimplified.xml
fails to run with

ptolemy.kernel.util.IllegalActionException: Cannot have minimumDelay > delay true. Modify the delay value.
  in .ContinuousInsideDESimplified.TimeDelay
    at ptolemy.actor.lib.TimeDelay.attributeChanged(TimeDelay.java:183)
    at ptolemy.data.expr.Variable._setTokenAndNotify(Variable.java:2045)
    at ptolemy.data.expr.Variable.setToken(Variable.java:1120)
    at ptolemy.actor.parameters.PortParameter.setCurrentValue(PortParameter.java:325)
    at ptolemy.actor.parameters.PortParameter.update(PortParameter.java:437)
    at ptolemy.actor.lib.TimeDelay.postfire(TimeDelay.java:269)
    at ptolemy.domains.de.kernel.DEDirector._fire(DEDirector.java:1840)
    at ptolemy.domains.de.kernel.DEDirector.fire(DEDirector.java:456)
    at ptolemy.actor.CompositeActor.fire(CompositeActor.java:450)
    at ptolemy.actor.Manager.iterate(Manager.java:778)
    at ptolemy.actor.Manager.execute(Manager.java:352)
    at ptolemy.actor.Manager.run(Manager.java:1193)
    at ptolemy.actor.Manager$PtolemyRunThread.run(Manager.java:1736)

#####
doc/books/systems/dataflow IterateFiles.xml  and IterateFilesSolution.xml
Refers to $HOME/tmp/Test.  Should these model be included?  What is the purpose of the model

#####
doc/books/systems/dataflow/PetriNet.xml and PetriNetNotation.xml

Are these two models used?  There is a figure by the name PetriNetNotation in the dataflow chapter.

Error encounterd in
<entity name="Bottom" class="ptolemy.data.ontologies.Concept">
null
null

Java.lang.InstantiationException
    at sun.reflect.InstantiationExceptionConstructorAccessorImpl.newInstance(InstantiationExceptionConstructorAccessorImpl.java:30)
    at java.lang.reflect.Constructor.newInstance(Constructor.java:513)
    at ptolemy.moml.MoMLParser._createInstance(MoMLParser.java:4478)


#####
doc/books/systems/dataflow/FixedPointLimitations.xml

The model does not run, should we include it as an example?

ptolemy.kernel.util.IllegalActionException: Found a zero delay loop containing .FixedPointLimitation.C1
  in .FixedPointLimitation and .FixedPointLimitation.C1
    at ptolemy.actor.util.CausalityInterfaceForComposites._computeOutputPortDepth(CausalityInterfaceForComposites.java:746)
    at ptolemy.actor.util.CausalityInterfaceForComposites._computeInputDepth(CausalityInterfaceForComposites.java:690)
    at ptolemy.actor.util.CausalityInterfaceForComposites._computeActorDepth(CausalityInterfaceForComposites.java:526)
    at ptolemy.actor.util.CausalityInterfaceForComposites.checkForCycles(CausalityInterfaceForComposites.java:95)
    at ptolemy.domains.de.kernel.DEDirector.preinitialize(DEDirector.java:1246)


#####
doc/books/systems/discreteevent/Record2.xml
ptolemy.kernel.util.IllegalActionException: Cannot find class: ptolemy.actor.lib.conversions.json.JSONToRecord
Because:
-- /Users/cxh/ptII/doc/books/systems/discreteevents/ptolemy/actor/lib/conversions/json/JSONToRecord.xml (No such file or directory)
-- XML file not found relative to classpath.
-- /Users/cxh/ptII/doc/books/systems/discreteevents/ptolemy/actor/lib/conversions/json/JSONToRecord.xml

#####
doc/books/systems/discreteevent/Zeno.xml
ptolemy.kernel.util.IllegalActionException: Cannot have minimumDelay > delay true. Modify the delay value.
  in .Zeno.TimeDelay
    at ptolemy.actor.lib.TimeDelay.attributeChanged(TimeDelay.java:183)
    at ptolemy.data.expr.Variable._setTokenAndNotify(Variable.java:2045)
    at ptolemy.data.expr.Variable.setToken(Variable.java:1120)
    at ptolemy.actor.parameters.PortParameter.setCurrentValue(PortParameter.java:325)
    at ptolemy.actor.parameters.PortParameter.update(PortParameter.java:437)
    at ptolemy.actor.lib.TimeDelay.postfire(TimeDelay.java:269)
    at ptolemy.domains.de.kernel.DEDirector._fire(DEDirector.java:1840)


####
doc/books/systems/expression/ActorToken.xml

ptolemy.actor.TypeConflictException: Types resolved to unacceptable types in .ActorToken due to the following objects:
  (variable .ActorToken.PortParameter: unknown)
  (variable .ActorToken.PortParameter: unknown)

  at ptolemy.actor.TypedCompositeActor.resolveTypes(TypedCompositeActor.java:406)
  at ptolemy.actor.Manager.resolveTypes(Manager.java:1142)
  at ptolemy.actor.Manager.preinitializeAndResolveTypes(Manager.java:978)
  at ptolemy.actor.Manager.initialize(Manager.java:661)
  at ptolemy.actor.Manager.execute(Manager.java:340)
  at ptolemy.actor.Manager.run(Manager.java:1193)
  at ptolemy.actor.Manager$PtolemyRunThread.run(Manager.java:1736)

####
doc/books/systems/fsm/HierarchicalFSM_FlattenedWOutReset.xml

ptolemy.kernel.util.IllegalActionException: No initial state has been specified.
  in .HierarchicalFSM_FlattenedWOutReset.ModalModel._Controller
    at ptolemy.domains.modal.kernel.FSMActor.getInitialState(FSMActor.java:1031)
    at ptolemy.domains.modal.kernel.FSMActor.reset(FSMActor.java:1786)
    at ptolemy.domains.modal.kernel.FSMActor.preinitialize(FSMActor.java:1689)

#####
doc/books/systems/fsm/HierarchicalFSM_FlattenedWOutResetOrPreemptive.xml
ptolemy.kernel.util.IllegalActionException: No initial state has been specified.
  in .HierarchicalFSM_FlattenedWOutResetOrPreemptive.ModalModel._Controller
    at ptolemy.domains.modal.kernel.FSMActor.getInitialState(FSMActor.java:1031)
    at ptolemy.domains.modal.kernel.FSMActor.reset(FSMActor.java:1786)

#####
doc/books/systems/fsm/HierarchicalFSM_FlattenedWOutPreemptive.xml
ptolemy.kernel.util.IllegalActionException: Cycle of immediate transitions found.
  in .HierarchicalFSM_FlattenedWOutPreemptive.ModalModel._Controller.heating and .HierarchicalFSM_FlattenedWOutPreemptive.ModalModel._Controller
    at ptolemy.domains.modal.kernel.FSMActor._chooseTransitions(FSMActor.java:2118)
    at ptolemy.domains.modal.kernel.FSMActor.fire(FSMActor.java:878)

#####
doc/book/systems/modal/ModalSDFMultirate.xml
ptolemy.actor.NoRoomException: Queue is at capacity of 20. Cannot put a token.
  in .ModalSDFMultirate.ModalModel.in
    at ptolemy.domains.sdf.kernel.SDFReceiver.put(SDFReceiver.java:308)
    at ptolemy.actor.AbstractReceiver.putToAll(AbstractReceiver.java:398)
    at ptolemy.actor.IOPort.send(IOPort.java:2796)


#####
doc/books/systems/modal/Preemptive.xml
ptolemy.kernel.util.IllegalActionException: Actor is not ready to fire.  Perhaps SampleDelay.prefire() returned false? Try debugging the actor by selecting "Listen to Actor".  Also, for SDF check moml for tokenConsumptionRate on input.
  in .Preemptive.SDF Director and .Preemptive.SampleDelay
    at ptolemy.actor.sched.StaticSchedulingDirector.fire(StaticSchedulingDirector.java:217)
    at ptolemy.domains.sdf.kernel.SDFDirector.fire(SDFDirector.java:492)
    at ptolemy.actor.CompositeActor.fire(CompositeActor.java:450)
    at ptolemy.actor.Manager.iterate(Manager.java:778)
    at ptolemy.actor.Manager.execute(Manager.java:352)
    at ptolemy.actor.Manager.run(Manager.java:1193)
    at ptolemy.actor.Manager$PtolemyRunThread.run(Manager.java:1736)

####
doc/books/systems/modelingtime/Decorator.xml
ptolemy.actor.TypeConflictException: Types resolved to unacceptable types in .Decorator due to the following objects:
  (port .Decorator.Actor1.port: unknown)
  (port .Decorator.Actor2.port: unknown)

    at ptolemy.actor.TypedCompositeActor.resolveTypes(TypedCompositeActor.java:406)
    at ptolemy.actor.Manager.resolveTypes(Manager.java:1142)
    at ptolemy.actor.Manager.preinitializeAndResolveTypes(Manager.java:978)
    at ptolemy.actor.Manager.initialize(Manager.java:661)
    at ptolemy.actor.Manager.execute(Manager.java:340)
    at ptolemy.actor.Manager.run(Manager.java:1193)
    at ptolemy.actor.Manager$PtolemyRunThread.run(Manager.java:1736)


####
doc/books/systems/modelingtime/BusError.xml
ptolemy.kernel.util.IllegalActionException: Test fails in iteration 0.
Value was: 0.0. Should have been: 2.0
  in .BusError.Test2

####
doc/books/systems/pn/ChatClient.xml
The model has this comment:
  This model requires that ChatServer be running providing a chat service
  at the specified URL. Such a chat server is available at

   $PTII/org/ptolemy/ptango/demo/Chat/ChatServer.xml

  which you can access by clicking on this comment.

So, it will not run when we are exporting html.  


#####
doc/books/systems/synchronous/CounterWithPre.xml

ptolemy.kernel.util.IllegalActionException: Unknown inputs remain. Possible causality loop:
input

  in .CounterWithPre.Pre and .CounterWithPre.Pre.input
  at ptolemy.actor.sched.FixedPointDirector.postfire(FixedPointDirector.java:504)
  at ptolemy.domains.sr.kernel.SRDirector.postfire(SRDirector.java:408)
  at ptolemy.actor.CompositeActor.postfire(CompositeActor.java:1604)
  at ptolemy.actor.Manager.iterate(Manager.java:779)
  at ptolemy.actor.Manager.execute(Manager.java:352)
  at ptolemy.actor.Manager.run(Manager.java:1193)
  at ptolemy.actor.Manager$PtolemyRunThread.run(Manager.java:1736)

####
doc/books/systems/synchronous/NonConstructive.xml

ptolemy.kernel.util.IllegalActionException: Unknown inputs remain. Possible causality loop:
input

  in .NonConstructive.NonStrictLogicGate and .NonConstructive.NonStrictLogicGate.input
  at ptolemy.actor.sched.FixedPointDirector.postfire(FixedPointDirector.java:504)
  at ptolemy.domains.sr.kernel.SRDirector.postfire(SRDirector.java:408)
  at ptolemy.actor.CompositeActor.postfire(CompositeActor.java:1604)
  at ptolemy.actor.Manager.iterate(Manager.java:779)
  at ptolemy.actor.Manager.execute(Manager.java:352)
  at ptolemy.actor.Manager.run(Manager.java:1193)
  at ptolemy.actor.Manager$PtolemyRunThread.run(Manager.java:1736)


####
doc/books/systems/synchronous/NotInLoop.xml

ptolemy.kernel.util.IllegalActionException: Unknown inputs remain. Possible causality loop:
input

  in .NotInLoop.Display and .NotInLoop.Display.input
  at ptolemy.actor.sched.FixedPointDirector.postfire(FixedPointDirector.java:504)
  at ptolemy.domains.sr.kernel.SRDirector.postfire(SRDirector.java:408)
  at ptolemy.actor.CompositeActor.postfire(CompositeActor.java:1604)
  at ptolemy.actor.Manager.iterate(Manager.java:779)
  at ptolemy.actor.Manager.execute(Manager.java:352)
  at ptolemy.actor.Manager.run(Manager.java:1193)
  at ptolemy.actor.Manager$PtolemyRunThread.run(Manager.java:1736)

#####
doc/books/systems/types/ArrayLengthError.xml

This model probably supposed to fail.  What should we do about it?

ptolemy.kernel.util.IllegalActionException: add operation not supported between ptolemy.data.ArrayToken '{1, 2}' and ptolemy.data.ArrayToken '{1, 2, 3}'
Because:
The length of the argument (3) is not the same as the length of this token (2).
  in .ArrayLengthError.AddSubtract
Because:
add operation not supported between ptolemy.data.ArrayToken '{1, 2}' and ptolemy.data.ArrayToken '{1, 2, 3}'
Because:
The length of the argument (3) is not the same as the length of this token (2).


#####
doc/books/systems/types/ExpressionEvaluator.xml

ptolemy.actor.TypeConflictException: Types resolved to unacceptable types in .ExpressionEvaluator due to the following objects:
  (port .ExpressionEvaluator.ExpressionToToken.output: unknown)
  (port .ExpressionEvaluator.SampleDelay.input: unknown)

  at ptolemy.actor.TypedCompositeActor.resolveTypes(TypedCompositeActor.java:406)
  at ptolemy.actor.Manager.resolveTypes(Manager.java:1142)
  at ptolemy.actor.Manager.preinitializeAndResolveTypes(Manager.java:978)
  at ptolemy.actor.Manager.initialize(Manager.java:661)
  at ptolemy.actor.Manager.execute(Manager.java:340)
  at ptolemy.actor.Manager.run(Manager.java:1193)
  at ptolemy.actor.Manager$PtolemyRunThread.run(Manager.java:1736)

#####
doc/books/systems/types/ExpressionEvaluatorTypeConflict.xml

This model probably supposed to fail.  What should we do about it?


ptolemy.actor.TypeConflictException: Types resolved to unacceptable types in .ExpressionEvaluatorTypeConflict due to the following objects:
  (port .ExpressionEvaluatorTypeConflict.ExpressionToToken.output: unknown)
  (port .ExpressionEvaluatorTypeConflict.SampleDelay.input: unknown)

  at ptolemy.actor.TypedCompositeActor.resolveTypes(TypedCompositeActor.java:406)
  at ptolemy.actor.Manager.resolveTypes(Manager.java:1142)
  at ptolemy.actor.Manager.preinitializeAndResolveTypes(Manager.java:978)
  at ptolemy.actor.Manager.initialize(Manager.java:661)
  at ptolemy.actor.Manager.execute(Manager.java:340)
  at ptolemy.actor.Manager.run(Manager.java:1193)
  at ptolemy.actor.Manager$PtolemyRunThread.run(Manager.java:1736)

#####
doc/books/systems/types/RecursiveTypes.xml

Caused by: ptolemy.kernel.util.InternalErrorException: ArrayType.clone: Cannot update new instance. Large type structure detected during type resolution.  The structured type arrayType(arrayType(arrayType(arrayType(arrayType(arrayType(arrayType(arrayType(arrayType(arrayType(arrayType(arrayType(arrayType(arrayType(arrayType(arrayType(arrayType(arrayType(arrayType(arrayType(int,1),1),1),1),1),1),1),1),1),1),1),1),1),1),1),1),1),1),1),1) has depth larger than the bound 20.  This may be an indicator of type constraints in a model with no finite solution.

#####
doc/books/systems/types/TypeCompatibility.xml

This simple model with a Const to a Monitor fails:

ptolemy.actor.TypeConflictException: Type conflicts occurred in .TypeCompatibility on the following inequalities:
  (port .TypeCompatibility.Const.output: double) <= (port .TypeCompatibility.MonitorValue.input: int)

#####
doc/books/systems/types/TypeConflict.xml

This gets a type conflict.  What should we do with models that are not supposed to run successfully?

#####
doc/books/systems/types/TypeInference.xml

ptolemy.kernel.util.IllegalActionException: Cannot have minimumDelay > delay true. Modify the delay value.
  in .TypeInference.TimeDelay
  at ptolemy.actor.lib.TimeDelay.attributeChanged(TimeDelay.java:183)
  at ptolemy.data.expr.Variable._setTokenAndNotify(Variable.java:2045)
  at ptolemy.data.expr.Variable.setToken(Variable.java:1120)
  at ptolemy.actor.parameters.PortParameter.setCurrentValue(PortParameter.java:325)
  at ptolemy.actor.parameters.PortParameter.update(PortParameter.java:437)
  at ptolemy.actor.lib.TimeDelay.postfire(TimeDelay.java:269)
  at ptolemy.domains.de.kernel.DEDirector._fire(DEDirector.java:1840)
  at ptolemy.domains.de.kernel.DEDirector.fire(DEDirector.java:456)
  at ptolemy.actor.CompositeActor.fire(CompositeActor.java:450)

#####
doc/books/systems/vergil/AddStringsException.xml

This model probably supposed to fail.  What should we do about it?

Caused by: ptolemy.kernel.util.IllegalActionException: subtract operation not supported between ptolemy.data.StringToken '"0"' and ptolemy.data.StringToken '"Hello World"'
       at ptolemy.data.StringToken._subtract(StringToken.java:360)
       at ptolemy.data.AbstractConvertibleToken.subtractReverse(AbstractConvertibleToken.java:611)
       at ptolemy.data.ScalarToken.subtract(ScalarToken.java:1126)
       at ptolemy.actor.lib.AddSubtract.fire(AddSubtract.java:195)

