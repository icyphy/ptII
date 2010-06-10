/* A sequence director that supports visually editing the sequence. */
package ptolemy.domains.sequence.kernel;

import java.awt.Frame;
import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.sched.Schedule;
import ptolemy.data.expr.Parameter;
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
* @since Ptolemy II 8.0
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

        // The following attribute (an instance of an inner class)
        // provides a customized dialog when you double click on the
        // icon.
        new SequenceConfigureFactory(this, "factory");

        scheduleText = new Parameter(this, "scheduleText");
        scheduleText
                .setExpression("{{actor=\"(double-click to\n edit schedule)\"}}");

        // The following attribute provides a custom icon that
        // displays the current schedule information.
        icon = new TableIcon(this, "_icon");
        icon.variableName.setExpression("scheduleText");
        icon.fields.setExpression("{\"actor\"}"); //, \"sequenceNumber\"}");
    }

    public Parameter scheduleText;
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
        public void createEditor(NamedObj object, Frame parent) {
            try {
                // FIXME: The following is probably not right because we might be running already!!!!!
                preinitialize();
                Schedule schedule = _scheduler.getSchedule(_sequencedList);

                String[] buttons = { "Cancel", "Done" };
                SequentialScheduleEditorPane pane = new SequentialScheduleEditorPane(
                        schedule);

                ComponentDialog dialog = new ComponentDialog(parent, object
                        .getFullName(), pane, buttons, null, true);

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
                // FIXME: It would be better here to come up a viable schedule.
                MessageHandler.error("Failed to get schedule.", e);
            }
        }

        private void _updateSchedule(SequentialScheduleEditorPane pane) {
            Iterator oActors = pane.getOrderedActors().iterator();
            int i = 1;
            String newScheduleText = new String("{");
            // FIXME: Currently all previous values of sequence numbers are 
            // ignored. Probably this is not desired.
            while (oActors.hasNext()) {
                if (i > 1) {
                    newScheduleText += ",";
                }
                Actor oActor = (Actor) oActors.next();
                SequenceAttribute seqAttribute = (SequenceAttribute) ((Entity) oActor)
                        .attributeList(SequenceAttribute.class).get(0);
                seqAttribute.setExpression(Integer.toString(i));
                newScheduleText += "{actor=\"" + oActor.getDisplayName()
                        + "\"}"; // , sequenceNumber=\"" + i + "\"}";
                i++;
            }
            newScheduleText += "}";
            scheduleText.setExpression(newScheduleText);
        }

    }
}
