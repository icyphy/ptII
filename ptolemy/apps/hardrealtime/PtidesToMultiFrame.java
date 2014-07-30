/* An attribute that manages the translation of Ptides models to multiframe tasks.

@Copyright (c) 2013 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                                                PT_COPYRIGHT_VERSION_2
                                                COPYRIGHTENDKEY


 */

package ptolemy.apps.hardrealtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.actor.lib.DiscreteClock;
import ptolemy.actor.lib.TimeDelay;
import ptolemy.actor.lib.aspect.EDFScheduler;
import ptolemy.actor.lib.aspect.ExecutionTimeAttributes;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.DecoratorAttributes;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// PtidesToMultiFrame

/**
 When configured this attribute generates a multiframe task that simulates
 the behavior of the Ptides platform it is contained in.

 @author Christos Stergiou
 @version $Id$
 @Pt.ProposedRating Red (chster)
 @Pt.AcceptedRating Red (chster)
 */
public class PtidesToMultiFrame extends Attribute implements Decorator {
    /** Construct an attribute with the given name contained by the specified
     *  entity.
     *  @param container The container.
     *  @param name The name of this attributed.
     *  @throws IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @throws NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public PtidesToMultiFrame(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        new PtidesToMultiFrameEditorFactory(this, "_editorFactory");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the decorated attributes for the target NamedObj.
     *  If the specified target is not an Actor, return null.
     *  @param target The NamedObj that will be decorated.
     *  @return The decorated attributes for the target NamedObj, or
     *   null if the specified target is not an Actor.
     */
    @Override
    public DecoratorAttributes createDecoratorAttributes(NamedObj target) {
        if (target instanceof IOPort && ((IOPort) target).isInput()) {
            try {
                return new InputModelAttributes(target, this);
            } catch (KernelException ex) {
                // This should not occur.
                throw new InternalErrorException(ex);
            }
        } else {
            return null;
        }
    }

    /** Return a list of the entities deeply contained by the container
     *  of this resource scheduler.
     *  @return A list of the objects decorated by this decorator.
     */
    @Override
    public List<NamedObj> decoratedObjects() {
        CompositeEntity container = (CompositeEntity) getContainer();
        List<NamedObj> toDecorate = new ArrayList<NamedObj>();
        List entities = container.entityList();
        for (Object entity : entities) {
            if (entity instanceof IOPort && ((IOPort) entity).isInput()) {
                toDecorate.add((NamedObj) entity);
            }
        }
        return toDecorate;
    }

    /** Construct a multiframe task system that has the same execution profile as the
     *  container Ptides system.
     *  @param directorClass The class of the director that will control the execution of the multiframe task system.
     *  @return A multiframe task system that has the same execution profile as the container Ptides system.
     *  @throws IllegalActionException If preinitializing the Ptides platform, if adding task frames,
     *    or if accessing the minimum interarrival time of a Ptides input port throws it, or
     *    if the minimum interarrival time is not sufficiently large.
     * @throws NameDuplicationException If creating a multiframe task throws it.
     */
    public TypedCompositeActor generateMultiFrameSystem(
            Class<? extends Director> directorClass)
                    throws IllegalActionException, NameDuplicationException {
        _multiFrameSystem = new TypedCompositeActor();
        // Call preinitialize of the Ptides platform to calculate
        // delay offsets and relative deadlines of ports.
        ((TypedCompositeActor) getContainer()).preinitialize();
        // Add EDF director to multiframe system.
        try {
            Director multiFrameScheduler = directorClass
                    .getDeclaredConstructor(CompositeEntity.class, String.class)
                    .newInstance(_multiFrameSystem, "EDF");
            multiFrameScheduler.stopTime
            .setExpression(_MULTIFRAME_TASK_SYSTEM_STOP_TIME + "");
        } catch (Throwable ex) {
            throw new InternalErrorException(
                    "Could not create a director of type " + directorClass);
        }

        for (Channel inputChannel : _getProgramInputChannels()) {
            _addMultiFrameTask(inputChannel);
            List<Channel> pendingChannels = new ArrayList<Channel>();
            List<Double> pendingPathDelays = new ArrayList<Double>();

            pendingChannels.add(inputChannel);
            pendingPathDelays.add(0.0);
            double time = 0.0;
            while (pendingChannels.size() > 0) {
                // Find pending channel that has the minimum release time.
                int mininumReleaseTimeIndex = _findMinimumReleaseTime(
                        pendingChannels, pendingPathDelays);
                Channel minimumReleaseTimeChannel = pendingChannels
                        .get(mininumReleaseTimeIndex);
                Double minimumReleaseTimePathDelay = pendingPathDelays
                        .get(mininumReleaseTimeIndex);
                double minimumReleaseTime = _getChannelPathReleaseTime(
                        minimumReleaseTimeChannel, minimumReleaseTimePathDelay);
                pendingChannels.remove(mininumReleaseTimeIndex);
                pendingPathDelays.remove(mininumReleaseTimeIndex);

                // Instantiate task frame that corresponds to path to channel.
                Actor downstreamActor = (Actor) minimumReleaseTimeChannel._port
                        .getContainer();
                double actorOffset = 0.0;
                double actorRelativeDeadline = 0.0;
                try {
                    actorOffset = ((DoubleToken) ((Parameter) minimumReleaseTimeChannel._port
                            .getAttribute("delayOffset")).getToken())
                            .doubleValue();
                    actorRelativeDeadline = ((DoubleToken) ((Parameter) minimumReleaseTimeChannel._port
                            .getAttribute("relativeDeadline")).getToken())
                            .doubleValue();
                } catch (IllegalActionException ex) {
                    // We will get here if accessing the delay offset of the port or its relative deadline fails.
                    // Since we explicitly preinitialize the Ptides platform, we should not get here.
                    throw new InternalErrorException(ex);
                }
                double deadline = actorOffset + actorRelativeDeadline;
                double actorExecutionTime = _getActorExecutionTime(downstreamActor);
                _addTaskFrame(downstreamActor, minimumReleaseTime - time,
                        actorExecutionTime, deadline);
                time = minimumReleaseTime;

                // Add actor output channels to pending set.
                for (Channel outputChannel : _getActorOutputChannels(downstreamActor)) {
                    pendingChannels.add(outputChannel);
                    pendingPathDelays.add(minimumReleaseTimePathDelay
                            + _getActorDelay(downstreamActor));
                }
            }
            double minimumInterarrivalTime = _getInputChannelMinimumInterarrivalTime(inputChannel);
            if (minimumInterarrivalTime < time) {
                throw new IllegalActionException(
                        "Cannot translate to multiframe task, minimum interarrival time input port is not sufficiently large.");
            }
            _linkTaskFrames(_lastTaskFrame, _firstTaskFrame,
                    (int) Math.round(minimumInterarrivalTime - time));
        }
        return _multiFrameSystem;
    }

    /** Return true to indicate that this decorator should
     *  decorate objects across opaque hierarchy boundaries.
     *  @return False.
     */
    @Override
    public boolean isGlobalDecorator() {
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Construct a multiframe task that will model the execution of the events that
     *  arrive at the argument input channel, and initialize the state required by
     *  the transformation algorithm.
     *  @param inputChannel The input channel of the Ptides platform that the multiframe
     *    task will model.
     *  @throws IllegalActionException If the constructor of the MultiFrameTask throws it
     *    or if _getInputPortFromInputChannel throws it.
     *  @throws NameDuplicationException If the constructor of the MultiFrameTask throws it.
     */
    private void _addMultiFrameTask(Channel inputChannel)
            throws IllegalActionException, NameDuplicationException {
        IOPort ptidesInputPort = _getInputPortFromInputChannel(inputChannel);
        _multiFrameTask = new MultiFrameTask(_multiFrameSystem,
                ptidesInputPort.getName());
        _multiFrameTaskActorNames = new HashMap<String, Integer>();
        _lastTaskFrame = _firstTaskFrame = null;
    }

    /**
     * @param actor
     * @param separation
     * @param executionTime
     * @param relativeDeadline
     * @throws IllegalActionException If attributedChanged throws it.
     */
    private void _addTaskFrame(Actor actor, double separation,
            double executionTime, double relativeDeadline)
                    throws IllegalActionException {
        String taskFrameName = actor.getName();
        if (_multiFrameTaskActorNames.containsKey(actor.getName())) {
            int index = _multiFrameTaskActorNames.get(actor.getName());
            taskFrameName += "#" + index;
            _multiFrameTaskActorNames.put(actor.getName(), index + 1);
        } else {
            _multiFrameTaskActorNames.put(taskFrameName, 1);
        }
        TaskFrame taskFrame;
        try {
            taskFrame = new TaskFrame(_multiFrameTask, taskFrameName);
        } catch (KernelException ex) {
            // We should not get here.
            throw new RuntimeException(ex);
        }
        taskFrame.executionTime.setExpression(Math.round(executionTime) + "");
        taskFrame.deadline.setExpression(Math.round(relativeDeadline) + "");
        if (_lastTaskFrame != null) {
            // If this is not the first frame
            taskFrame.initial.setExpression("false");
            // Link with the last frame
            _linkTaskFrames(_lastTaskFrame, taskFrame,
                    (int) Math.round(separation));
        } else {
            // If it is the fist frame, set initialFrame to true
            _firstTaskFrame = taskFrame;
            taskFrame.initial.setExpression("true");
        }
        taskFrame.attributeChanged(taskFrame.executionTime);
        taskFrame.attributeChanged(taskFrame.deadline);
        taskFrame.attributeChanged(taskFrame.initial);
        _lastTaskFrame = taskFrame;
    }

    private int _findMinimumReleaseTime(List<Channel> channels,
            List<Double> pathDelays) {
        Channel minimumReleaseTimeChannel = channels.get(0);
        Double minimumReleaseTimePathDelay = pathDelays.get(0);
        double minimumReleaseTime = _getChannelPathReleaseTime(
                minimumReleaseTimeChannel, minimumReleaseTimePathDelay);
        int minimumReleaseTimeIndex = 0;
        for (int i = 0; i < channels.size(); ++i) {
            Channel pendingChannel = channels.get(i);
            Double pendingPathDelay = pathDelays.get(i);
            double channelPathReleaseTime = 0.0;
            channelPathReleaseTime = _getChannelPathReleaseTime(pendingChannel,
                    pendingPathDelay);
            if (channelPathReleaseTime < minimumReleaseTime) {
                minimumReleaseTimeChannel = pendingChannel;
                minimumReleaseTimePathDelay = pendingPathDelay;
                minimumReleaseTime = channelPathReleaseTime;
                minimumReleaseTimeIndex = i;
            }
        }
        return minimumReleaseTimeIndex;
    }

    private double _getActorDelay(Actor actor) {
        double delay = 0.0;
        if (actor instanceof TimeDelay) {
            try {
                delay = ((DoubleToken) ((TimeDelay) actor).minimumDelay
                        .getToken()).doubleValue();
            } catch (IllegalActionException e) {
                delay = 0.0;
            }
        }
        return delay;
    }

    private double _getActorExecutionTime(Actor actor) {
        double executionTime = 0.0;
        for (ExecutionTimeAttributes resourceAttributes : ((NamedObj) actor)
                .attributeList(ExecutionTimeAttributes.class)) {
            try {
                EDFScheduler scheduler = ((TypedCompositeActor) getContainer())
                        .entityList(EDFScheduler.class).get(0);
                if (resourceAttributes.getDecorator().equals(scheduler)) {
                    Token token = resourceAttributes.executionTime.getToken();
                    if (token != null) {
                        executionTime = ((DoubleToken) token).doubleValue();
                    }
                    break;
                }
            } catch (IllegalActionException e) {
                executionTime = 0.0;
            }
        }
        return executionTime;
    }

    private List<Channel> _getActorOutputChannels(Actor actor) {
        List<Channel> outputChannels = new ArrayList<Channel>();
        for (Object outputPort : actor.outputPortList()) {
            for (Object orelation : ((Port) outputPort).linkedRelationList()) {
                IORelation relation = (IORelation) orelation;
                for (IOPort downstreamInputPort : relation
                        .linkedDestinationPortList()) {
                    if (downstreamInputPort.getContainer() != getContainer()) {
                        outputChannels.add(new Channel(downstreamInputPort,
                                relation));
                    }
                }
            }
        }
        return outputChannels;
    }

    /** Calculate the relative release time of the execution of an actor in response to an input
     *  event that followed a specific path to arrive at an input channel of the actor.
     *  Expects that the port of the Ptides actor is annotated with a delayOffset, or that the
     *  Ptides platform has been preinitialized.
     *  @param channel The input channel of the actor.
     *  @param pathDelay The delay of the path that the input event follows to reach the actor.
     *  @return The relative release time of the actor.s
     *  @throws IllegalActionException If getting the delay offset of the port of the channel throws it.
     */
    private double _getChannelPathReleaseTime(Channel channel, double pathDelay) {
        try {
            double portDelayOffset = ((DoubleToken) ((Parameter) channel._port
                    .getAttribute("delayOffset")).getToken()).doubleValue();
            return pathDelay - portDelayOffset;
        } catch (IllegalActionException ex) {
            throw new RuntimeException(ex);
        }
    }

    private double _getInputChannelMinimumInterarrivalTime(Channel inputChannel)
            throws IllegalActionException {
        IOPort ptidesInputPort = _getInputPortFromInputChannel(inputChannel);

        for (InputModelAttributes attribute : ptidesInputPort
                .attributeList(InputModelAttributes.class)) {
            Token token = attribute.minimumInterarrivalTime.getToken();
            if (token != null) {
                return ((DoubleToken) attribute.minimumInterarrivalTime
                        .getToken()).doubleValue();

            }
        }
        // Best effort: go up the hierarchy and check if input port is connected to a DiscreteClock and get it's period.
        IOPort mirrorPort = (IOPort) ptidesInputPort.deepConnectedPortList()
                .get(0);
        List<Object> outputPorts = mirrorPort.deepConnectedPortList();
        if (outputPorts.size() == 1) {
            IOPort outputPort = (IOPort) outputPorts.get(0);
            if (outputPort.getContainer() instanceof DiscreteClock) {
                DiscreteClock clock = (DiscreteClock) outputPort.getContainer();
                Token periodToken = clock.period.getToken();
                if (periodToken != null) {
                    return ((DoubleToken) periodToken).doubleValue();
                }
            }
        }
        throw new IllegalActionException(ptidesInputPort,
                "Minimum inter-arrival time is not set for the port.");
    }

    private IOPort _getInputPortFromInputChannel(Channel inputChannel)
            throws IllegalActionException {
        List<IOPort> upstreamPorts = inputChannel._relation
                .linkedSourcePortList();
        if (upstreamPorts.size() != 1) {
            throw new IllegalActionException(
                    "Input channel is not connected to a single Ptides input port");
        }
        return upstreamPorts.get(0);
    }

    /** Return the set of actor input ports that are connected to input ports
     *  of the Ptides platform.
     *  @return A set of actor input ports connected to platform inputs
     */
    private List<Channel> _getProgramInputChannels() {
        List<Channel> programInputChannels = new ArrayList();
        TypedCompositeActor ptidesPlatform = (TypedCompositeActor) getContainer();
        for (Object object : ptidesPlatform.inputPortList()) {
            TypedIOPort port = (TypedIOPort) object;
            List relationList = port.insideRelationList();
            if (relationList.size() == 1) {
                TypedIORelation relation = (TypedIORelation) relationList
                        .get(0);
                for (IOPort downstreamPort : relation
                        .linkedDestinationPortList()) {
                    programInputChannels.add(new Channel(downstreamPort,
                            relation));
                }
            }
        }
        return programInputChannels;
    }

    private static TypedIORelation _linkTaskFrames(TaskFrame source,
            TaskFrame target, int separation) {
        CompositeEntity container = (CompositeEntity) source.getContainer();
        String relationName = source.getName() + target.getName();
        TypedIORelation relation;
        try {
            relation = new TypedIORelation(container, relationName);
            source.output.link(relation);
            target.input.link(relation);
            (new Parameter(relation, "separation")).setExpression(separation
                    + "");
        } catch (KernelException ex) {
            // We should not get here:
            throw new RuntimeException(ex);
        }
        return relation;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private TaskFrame _firstTaskFrame;

    private TaskFrame _lastTaskFrame;

    private TypedCompositeActor _multiFrameSystem;

    private MultiFrameTask _multiFrameTask;

    private Map<String, Integer> _multiFrameTaskActorNames;

    private static final double _MULTIFRAME_TASK_SYSTEM_STOP_TIME = 200.0;
}
