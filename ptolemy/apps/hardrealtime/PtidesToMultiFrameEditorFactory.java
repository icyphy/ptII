/* An attribute that shows a multiframe task that corresponds to a Ptides model.

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

import java.awt.Frame;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.Vertex;

/**
 An attribute that shows a multiframe task that corresponds to a Ptides model.

 @author Christos Stergiou
 @version $Id$
 @Pt.ProposedRating Red (chster)
 @Pt.AcceptedRating Red (chster)
 */
public class PtidesToMultiFrameEditorFactory extends EditorFactory {
    /** Constructs a EditorFactory object for a PtidesToMultiframe attribute.
     *
     *  @param container The container, which is a PtidesToMultiFrame attribute.
     *  @param name The name for this attribute.
     *  @exception IllegalActionException If the factory is not of an
     *  acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *  an attribute already in the container.
     */
    public PtidesToMultiFrameEditorFactory(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Create an editor for configuring the specified object with the
     *  specified parent window.
     *  @param object The object to configure.
     *  @param parent The parent window, or null if there is none.
     */
    @Override
    public void createEditor(NamedObj object, Frame parent) {
        try {
            TypedCompositeActor multiFrameSystem = ((PtidesToMultiFrame) getContainer())
                    .generateMultiFrameSystem(EDF.class);
            _layoutMultiFrameSystem(multiFrameSystem);
            Configuration configuration = ((TableauFrame) parent)
                    .getConfiguration();
            configuration.openInstance(multiFrameSystem);
        } catch (NameDuplicationException ex) {
            // The constructor of actors throws this.
            System.err
            .println("According to open instance this should not be thrown.");
        } catch (IllegalActionException ex) {
            // The EDF construction might throw this.
            System.err.println("Could not open new window");
            throw new InternalErrorException(ex);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private static void _layoutMultiFrameSystem(
            TypedCompositeActor multiFrameSystem)
                    throws IllegalActionException, NameDuplicationException {
        int multiFrameTaskXPos = 0;
        int multiFrameTaskYPos = 100;
        int multiFrameTaskSpacing = 100;

        int taskFrameYPos = 100;
        int taskFrameSpacing = 100;

        for (MultiFrameTask multiFrameTask : multiFrameSystem
                .entityList(MultiFrameTask.class)) {
            (new Location(multiFrameTask, "_location"))
            .setLocation(new double[] { multiFrameTaskXPos,
                    multiFrameTaskYPos });

            TaskFrame initialFrame = null;
            int taskFrameXPos = 0;
            for (TaskFrame taskFrame : multiFrameTask
                    .entityList(TaskFrame.class)) {
                if (taskFrame._initial) {
                    initialFrame = taskFrame;
                    break;
                }
            }
            if (initialFrame != null) {
                TaskFrame currentFrame = initialFrame;
                do {
                    currentFrame.initialize();
                    if (currentFrame.getNextFrame() == initialFrame) {
                        new Vertex((Relation) currentFrame.output
                                .linkedRelationList().get(0), "_vertex");
                    }
                    (new Location(currentFrame, "_location"))
                            .setLocation(new double[] { taskFrameXPos,
                                    taskFrameYPos });
                    currentFrame = currentFrame.getNextFrame();
                    taskFrameXPos += taskFrameSpacing;
                } while (currentFrame != initialFrame && currentFrame != null);
            }
            multiFrameTaskXPos += multiFrameTaskSpacing;
        }
    }
}
