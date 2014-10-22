/* A tableau representing a Doc Builder window.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.vergil.actor;

import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.MoMLApplication;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// DocBuilderTableau

/**
 A tableau representing a documentation builder.

 <p> The constructor of this class creates the window. The text window
 itself is an instance of DocBuilderGUI, and can be accessed using the
 getFrame() method.  As with other tableaux, this is an entity that is
 contained by an effigy of a model.  There can be any number of
 instances of this class in an effigy.

 @author  Christopher Brooks
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 @see Effigy
 @see DocBuilderGUI
 @see MoMLApplication#specToURL(String)
 */
public class DocBuilderTableau extends Tableau {

    /** Construct a new tableau for the model represented by the given effigy.
     *  This creates an instance of DocBuildGUI.  It does not make the frame
     *  visible.  To do that, call show().
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container does not accept
     *   this entity (this should not occur).
     *  @exception NameDuplicationException If the name coincides with an
     *   attribute already in the container.
     */
    public DocBuilderTableau(Effigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        try {
            DocBuilder docBuilder = new DocBuilder(container, "myDocBuilder");
            DocBuilderGUI frame = new DocBuilderGUI(docBuilder, this);
            setFrame(frame);
            frame.setTableau(this);
        } catch (Exception ex) {
            throw new IllegalActionException(this, container, ex,
                    "Malformed URL");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A factory that creates DocBuilderGUI tableaux for Ptolemy models.
     */
    public static class Factory extends TableauFactory {
        /** Create a factory with the given name and container.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the container is incompatible
         *   with this attribute.
         *  @exception NameDuplicationException If the name coincides with
         *   an attribute already in the container.
         */
        public Factory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** If the specified effigy already contains a tableau named
         *  "DocBuilderTableau", then return that tableau; otherwise, create
         *  a new instance of DocBuilderTableau in the specified
         *  effigy, and name it "DocBuilderTableau".  If the specified
         *  effigy is not an instance of DocBuilderEffigy, then do not
         *  create a tableau and return null.  It is the
         *  responsibility of callers of this method to check the
         *  return value and call show().
         *
         *  @param effigy The effigy.
         *  @return A DocBuilder tableau, or null if one cannot be
         *    found or created.
         *  @exception Exception If the factory should be able to create a
         *   tableau for the effigy, but something goes wrong.
         */
        @Override
        public Tableau createTableau(Effigy effigy) throws Exception {
            // Indicate to the effigy that this factory contains effigies
            // offering multiple views of the effigy data.
            effigy.setTableauFactory(this);

            // First see whether the effigy already contains an
            // DocBuilderTableau.
            DocBuilderTableau tableau = (DocBuilderTableau) effigy
                    .getEntity("DocBuilderTableau");

            if (tableau == null) {
                tableau = new DocBuilderTableau(effigy, "DocBuilderTableau");
            }
            // Don't call show() here.  If show() is called here,
            // then you can't set the size of the window after
            // createTableau() returns.  This will affect how
            // centering works.
            return tableau;
        }
    }
}
