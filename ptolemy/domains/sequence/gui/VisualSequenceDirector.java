/* A sequence director that supports visually editing the sequence.

 Copyright (c) 2010-2014 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

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

package ptolemy.domains.sequence.gui;

import java.awt.Frame;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import ptolemy.actor.Actor;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Schedule;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sequence.kernel.PartialSequenceScheduler;
import ptolemy.domains.sequence.kernel.SequenceAttribute;
import ptolemy.domains.sequence.kernel.SequenceDirector;
import ptolemy.gui.ComponentDialog;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.CancelException;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.icon.TableIcon;

///////////////////////////////////////////////////////////////////
//// VisualSequenceDirector

/**
 * A director that executes actors in a sequence explicitly specified
 * by the model. This extends the base class by providing an interface
 * for editing the sequence.
 *
 * @author Bastian Ristau, Yasemin Demir, and Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (beth)
 * @Pt.AcceptedRating Red (beth)
 */
public class VisualSequenceDirector extends SequenceDirector {

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *   The SequenceDirector will have a default scheduler of type
     *   SequenceScheduler.
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  May be thrown in a derived class.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public VisualSequenceDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _scheduler = new PartialSequenceScheduler(this,
                uniqueName("SequenceScheduler"));
        setScheduler(_scheduler);

        // The following attribute (an instance of an inner class)
        // provides a customized dialog when you double click on the
        // icon.
        new SequenceConfigureFactory(this, "factory");

        scheduleText = new Parameter(this, "scheduleText");
        scheduleText
                .setExpression("{{actor=\"(double-click to\n edit schedule)\",sequenceNumber=\"\"}}");

        // The following attribute provides a custom icon that
        // displays the current schedule information.
        icon = new TableIcon(this, "_icon");
        icon.variableName.setExpression("scheduleText");
        icon.fields.setExpression("{\"actor\", \"sequenceNumber\"}");
    }

    /** Specifies the sequential schedule as an array of records.*/
    public Parameter scheduleText;

    /** Displays the sequential schedule in the icon of this director. */
    public TableIcon icon;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** An interactive editor that displays the sequence. */
    public class SequenceConfigureFactory extends EditorFactory {

        /** Construct a factory with the specified container and name.
         *  @param container The container.
         *  @param name The name of the factory.
         *  @exception IllegalActionException If the factory is not of an
         *   acceptable attribute for the container.
         *  @exception NameDuplicationException If the name coincides with
         *   an attribute already in the container.
         */
        public SequenceConfigureFactory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        /** Create a top-level viewer for the specified object with the
         *  specified parent window.
         *  @param object The object to configure, which is required to
         *   contain a parameter with name matching <i>parameterName</i>
         *   and value that is an array of records.
         *  @param parent The parent window, which is required to be an
         *   instance of TableauFrame.
         */
        @Override
        public void createEditor(NamedObj object, Frame parent) {
            Schedule schedule = null;
            SequentialScheduleEditorPane pane = null;
            try {
                // FIXME: The following is probably not right because we might be running already!!!!!
                preinitialize();

                schedule = _scheduler.getSchedule(_sequencedList);
                pane = new SequentialScheduleEditorPane(schedule);
            } catch (Throwable e) {
                try {
                    Vector<Actor> orderedActors = ((PartialSequenceScheduler) _scheduler)
                            .estimateSequencedSchedule(_sequencedList);
                    pane = new SequentialScheduleEditorPane(orderedActors);
                } catch (NotSchedulableException e2) {
                    MessageHandler.error("Failed to order actors.", e2);
                }
            }
            if (pane != null) {
                try {
                    String[] buttons = { "Cancel", "Done" };

                    ComponentDialog dialog = new ComponentDialog(parent,
                            object.getFullName(), pane, buttons, null, true);

                    String response = dialog.buttonPressed();

                    // If the window is closed by clicking on the X or by typing ESC,
                    // the response is "".
                    if ("Cancel".equals(response) || "".equals(response)) {
                        throw new CancelException();
                    } else if ("Done".equals(response)) {
                        // update the displayed as well as the "real" schedule
                        _updateSchedule(pane);
                    }

                } catch (Throwable e) {
                    //                    MessageHandler.error("Failed to get schedule.", e);
                }
            }
        }

        private void _updateSchedule(SequentialScheduleEditorPane pane) {
            Iterator orderedActors = pane.getOrderedActors().iterator();
            int i = 1;
            StringBuffer newScheduleText = new StringBuffer();
            newScheduleText.append("{");
            //String newScheduleText = new String("{");
            // FIXME: Currently all previous values of sequence numbers are
            // ignored. Probably this is not desired.
            // FIXME: Also SequenceNumbers of non-opaque actors are not taken into
            // account during renumbering, so duplicates may occur.
            while (orderedActors.hasNext()) {
                if (i > 1) {
                    newScheduleText.append(",");
                }
                Actor actor = (Actor) orderedActors.next();
                List<SequenceAttribute> seqAttributes = ((Entity) actor)
                        .attributeList(SequenceAttribute.class);//
                if (seqAttributes.size() > 0) {
                    SequenceAttribute seqAttribute = seqAttributes.get(0);
                    seqAttribute.setExpression(Integer.toString(i));
                } else {
                    try {
                        SequenceAttribute seqAttribute = new SequenceAttribute(
                                (NamedObj) actor, uniqueName("sequenceNumber"));
                        seqAttribute.setExpression(Integer.toString(i));
                    } catch (IllegalActionException e) {
                        e.printStackTrace();
                    } catch (NameDuplicationException e) {
                        e.printStackTrace();
                    }
                }
                newScheduleText.append("{actor=\"" + actor.getDisplayName()
                        + "\", sequenceNumber=\"" + i + "\"}");
                i++;
            }
            newScheduleText.append("}");
            scheduleText.setExpression(newScheduleText.toString());
        }

    }
}
