/* A simple tree view for Ptolemy models

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.tree;

import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

//////////////////////////////////////////////////////////////////////////
//// TreeTableau
/**
This class provides a tree view for ptolemy models.

@author  Steve Neuendorffer and Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
*/
public class TreeTableau extends Tableau {

    public TreeTableau(PtolemyEffigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        NamedObj model = container.getModel();
        if (!(model instanceof CompositeEntity)) {
            throw new IllegalActionException(this,
                    "Cannot have a tree view of a model that is "
                    + "not a CompositeEntity.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the top-level window associated with this tableau.
     *  @param frame The top-level window associated with the tableau.
     *  @throws IllegalActionException If the frame is not an instance
     *   of PlotTableauFrame.
     */
    public void setFrame(JFrame frame) throws IllegalActionException {
        if (!(frame instanceof TreeFrame)) {
            throw new IllegalActionException(this,
                    "Frame for PlotTableau must be an instance of "
                    + "PlotTableauFrame.");
        }
        super.setFrame(frame);
        ((TreeFrame)frame).setTableau(this);
    }

    /** Make this tableau visible by calling setVisible(true), and
     *  raising or deiconifying its window.
     *  If no frame has been set, then create one, an instance of
     *  PlotTableauFrame.  If a URL has been specified but not yet
     *  processed, then process it.
     */
    public void show() {
        JFrame frame = getFrame();
        if (frame == null) {
            PtolemyEffigy container = (PtolemyEffigy)getContainer();
            CompositeEntity model = (CompositeEntity)container.getModel();

            frame = new TreeFrame(model);
            frame.setBackground(BACKGROUND_COLOR);
            // Give a reasonable default size.
            size.setExpression("300x500");
            ((TreeFrame)frame).setTableau(this);
            frame.pack();
            ((TreeFrame)frame).centerOnScreen();
            frame.setVisible(true);

            try {
                setFrame(frame);
            } catch (IllegalActionException ex) {
                throw new InternalErrorException(ex);
            }
        }
        super.show();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The background color.
    private static Color BACKGROUND_COLOR = new Color(0xe5e5e5);

    ///////////////////////////////////////////////////////////////////
    ////                     public inner classes                  ////

    /** This is a top-level window containing a tree-view of a composite
     *  entity.
     */
    public static class TreeFrame extends PtolemyFrame {

        /** Construct a TreeFrame containing a tree view of the specified
         *  composite entity.
         *  @param entity The composite entity to view as a tree.
         */
        public TreeFrame(CompositeEntity entity) {
            super(entity);
            PTree pane = new PTree(new FullTreeModel(entity));
            getContentPane().add(new JScrollPane(pane), BorderLayout.CENTER);
        }

        ///////////////////////////////////////////////////////////////
        ////                     protected methods                 ////

        /** Write the model to the specified file.
         *  @param file The file to write to.
         *  @exception IOException If the write fails.
         */
        protected void _writeFile(File file) throws IOException {
            java.io.FileWriter fout = new java.io.FileWriter(file);
            getModel().exportMoML(fout);
            fout.close();
        }
    }

    /** This is a factory that creates tree-view tableaux for Ptolemy models.
     */
    public static class Factory extends TableauFactory {

        /** Create an factory with the given name and container.
         *  @param container The container entity.
         *  @param name The name of the entity.
         *  @exception IllegalActionException If the container is incompatible
         *   with this attribute.
         *  @exception NameDuplicationException If the name coincides with
         *   an attribute already in the container.
         */
        public Factory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        ///////////////////////////////////////////////////////////////
        ////                     public methods                    ////

        /** If the effigy is an instance of PtolemyEffigy referencing
         *  an instance of CompositeEntity, then create a TreeTableau
         *  contained by the effigy. The tableau will assigned the
         *  name "treeTableau".  If there is already such a tableau in
         *  the effigy, then just show it instead of creating a new
         *  one.  If the effigy is not an instance of PtolemyEffigy
         *  referencing an instance of CompositeEntity, and there no
         *  pre-existing tableau named "treeTableau", then return
         *  null.  It is the responsibility of callers of this method
         *  to check the return value and call show().
         *
         *  @param effigy An effigy of a Ptolemy model.
         *  @return A new tree-view tableau, or null if the effigy is not
         *   that of a composite entity.
         *  @exception Exception If the effigy is a PtolemyEffigy, but
         *   construction of the tree view fails for some reason.  */
        public Tableau createTableau(Effigy effigy) throws Exception {
            if (effigy instanceof PtolemyEffigy) {
                // First see whether the effigy already contains a TreeTableau.
                TreeTableau previous =
                    (TreeTableau)effigy.getEntity("treeTableau");

                if (previous != null) {
                    return previous;
                } else {
                    PtolemyEffigy ptEffigy = (PtolemyEffigy)effigy;
                    NamedObj model = ptEffigy.getModel();
                    if (model instanceof CompositeEntity) {
                        return new TreeTableau(ptEffigy, "treeTableau");
                    }
                }
            }
            return null;
        }
    }
}
