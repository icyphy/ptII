/* A job specification of a multiframe task.

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

import ptolemy.actor.IOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;

/**
 As described in {@link MultiFrameTask}, a multiframe task rotates through
 a set of different job specifications. A task frame describes one of those possible
 job specifications.

 @author Christos Stergiou
 @version $Id$
 @Pt.ProposedRating Red (chster)
 @Pt.AcceptedRating Red (chster)
 */

public class TaskFrame extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @throws IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @throws NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TaskFrame(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        deadline = new Parameter(this, "deadline");
        deadline.setTypeEquals(BaseType.INT);
        deadline.setExpression("10");

        executionTime = new Parameter(this, "executionTime");
        executionTime.setTypeEquals(BaseType.INT);
        executionTime.setExpression("10");

        initial = new Parameter(this, "initialFrame");
        initial.setTypeEquals(BaseType.BOOLEAN);
        initial.setExpression("true");

        input.setTypeEquals(BaseType.NIL);
        output.setTypeEquals(BaseType.NIL);
        _drawIcon();
    }

    ///////////////////////////////////////////////////////////////////
    ////                       parameters                          ////

    /** The relative deadline of jobs released by the task frame. */
    public Parameter deadline;

    /** The execution requirement of jobs released by the task frame. */
    public Parameter executionTime;

    /** Whether the task frame is the initial frame in the multi-frame task. */
    public Parameter initial;

    ///////////////////////////////////////////////////////////////////
    ////                       public methods                      ////

    /** Update the deadline, the execution time, or the initial predicate, if the
     *  corresponding attributes change and redraw the icon of the frame.
     *  @param attribute The attribute that changed.
     *  @throws IllegalActionException If getting a token of a parameter throws it.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == deadline) {
            _deadline = ((IntToken) deadline.getToken()).intValue();
        } else if (attribute == executionTime) {
            _executionTime = ((IntToken) executionTime.getToken()).intValue();
        } else if (attribute == initial) {
            _initial = ((BooleanToken) initial.getToken()).booleanValue();
        }
        _drawIcon();
    }

    /** Return the task frame that follows this in the multiframe task.
     *  @return The next frame in the multiframe task.
     */
    public TaskFrame getNextFrame() {
        return _nextFrame;
    }

    /** The separation time between this frame and the next in the multiframe task,
     *  i.e. the separation time between a job release by this frame and a job release by the next frame.
     *  @return The separation time between this frame and the next.
     */
    public int getSeparationUntilNextFrame() {
        return _separationUntilNextFrame;
    }

    /** Set the parent multiframe task that contains this frame,
     *  the next frame, and the separation time between this and the next frame,
     *  @throws IllegalActionException If the container is not a multiframe task,
     *   if the frame is not connected to exactly one upstream and one downstream frame,
     *   if the separation time is not set or is not an integer.
     */
    @Override
    public void initialize() throws IllegalActionException {

        // Get parent multi-frame task
        NamedObj multiFrameTask = getContainer();
        if (!(multiFrameTask instanceof MultiFrameTask)) {
            throw new IllegalActionException(this,
                    "Can only be inside a MultiFrame task composite");
        }
        _multiFrameTask = (MultiFrameTask) multiFrameTask;

        if (outputPortList().size() != 1 || inputPortList().size() != 1) {
            throw new IllegalActionException(this,
                    "A task frame must have exactly one input and one output port.");
        }
        if (output.linkedRelationList().size() != 1) {
            throw new IllegalActionException(this,
                    "A task frame must be connected to exactly one other task frame");
        }

        // Get separation time from the output relation
        Relation relation = (Relation) output.linkedRelationList().get(0);
        Attribute separationAttribute = relation.getAttribute("separation");
        if (separationAttribute == null
                || ((Parameter) separationAttribute).getToken() == null) {
            throw new IllegalActionException(relation,
                    "The connection between two task frames has to include a separation time");
        }
        Token separationToken = ((Parameter) separationAttribute).getToken();
        if (separationToken.getType() != BaseType.INT) {
            throw new IllegalActionException(relation,
                    "Separation time can only be an integer value.");
        }
        _separationUntilNextFrame = ((IntToken) ((Parameter) separationAttribute)
                .getToken()).intValue();

        // Get downstream task frame
        if (relation.linkedPortList(output).size() != 1) {
            throw new IllegalActionException(this,
                    "A task frame must be connected to exactly one other task frame, "
                            + "instead connected to "
                            + relation.linkedPortList(output));
        }
        IOPort downstreamPort = (IOPort) relation.linkedPortList(output).get(0);
        Object container = downstreamPort.getContainer();
        if (!(container instanceof TaskFrame)) {
            throw new IllegalActionException((Nameable) container,
                    "Task frames can only be connected to other task frames.");
        }
        _nextFrame = (TaskFrame) container;
        _multiFrameTask._addTaskFrame(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The relative deadline of the jobs released by the frame. */
    protected int _deadline;

    /** The execution time of the jobs released by the frame. */
    protected int _executionTime;

    /** True if the task frame is the first frame in the multi-frame task. */
    protected boolean _initial;

    /** The separation time between a job released by this frame and a job released by the next frame. */
    private int _separationUntilNextFrame;

    ///////////////////////////////////////////////////////////////////
    ////                       private methods                     ////

    private void _drawIcon() {
        String textFormat = "<text x=\"%d\" y=\"%d\" style=\"font-size:16; font-family:SansSerif; fill:blue\">%s</text>";
        int yPosition = 45;
        int yJump = 20;
        String iconText = "<svg><circle cx=\"50\" cy=\"50\" r=\"30\"  style=\"fill:white\"/>";
        if (_initial) {
            iconText += "<circle cx=\"50\" cy=\"50\" r=\"27\"  style=\"fill:white\"/>";
        }
        iconText += String.format(textFormat, 35, yPosition, "C: "
                + _executionTime);
        yPosition += yJump;
        iconText += String.format(textFormat, 35, yPosition, "D: " + _deadline);
        iconText += "</svg>";
        _attachText("_iconDescription", iconText);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private MultiFrameTask _multiFrameTask;
    private TaskFrame _nextFrame;

}
