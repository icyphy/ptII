/* An Ontology graph view for Ptolemy models.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.vergil.ontologies;

import java.awt.Color;

import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.data.ontologies.Ontology;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;

///////////////////////////////////////////////////////////////////
//// OntologyGraphTableau

/** An editor tableau for Ontology models.

 @author  Charles Shelton
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cshelton)
 @Pt.AcceptedRating Red (cshelton)
 */
public class OntologyGraphTableau extends Tableau {

    /** Create a new Ontology editor tableau with the specified container
     *  and name, with no default library.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the model associated with
     *   the container effigy is not an FSMActor.
     *  @exception NameDuplicationException If the container already
     *   contains an object with the specified name.
     */
    public OntologyGraphTableau(PtolemyEffigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        this(container, name, null);
    }

    /** Create a new Ontology editor tableau with the specified container,
     *  name, and default library.
     *  @param container The container.
     *  @param name The name.
     *  @param defaultLibrary The default library, or null to not specify one.
     *  @exception IllegalActionException If the model associated with
     *   the container effigy is not an FSMActor.
     *  @exception NameDuplicationException If the container already
     *   contains an object with the specified name.
     */
    public OntologyGraphTableau(PtolemyEffigy container, String name,
            LibraryAttribute defaultLibrary) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);

        NamedObj model = container.getModel();

        if (!(model instanceof Ontology)) {
            throw new IllegalActionException(this,
                    "Cannot edit a model that is not an Ontology.");
        }

        createGraphFrame((Ontology) model, defaultLibrary);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create the graph frame that displays the model associated with
     *  this tableau. This method creates a FSMGraphFrame. If subclass
     *  uses another frame, this method should be overridden to create
     *  that frame.
     *  @param model The Ptolemy II model to display in the graph frame.
     */
    public void createGraphFrame(CompositeEntity model) {
        createGraphFrame(model, null);
    }

    /** Create the graph frame that displays the model associated with
     *  this tableau together with the specified library.
     *  This method creates a FSMGraphFrame. If a subclass
     *  uses another frame, this method should be overridden to create
     *  that frame.
     *  @param model The Ptolemy II model to display in the graph frame.
     *  @param defaultLibrary The default library, or null to not specify
     *   one.
     */
    public void createGraphFrame(CompositeEntity model,
            LibraryAttribute defaultLibrary) {
        OntologyGraphFrame frame = new OntologyGraphFrame(model, this,
                defaultLibrary);

        try {
            setFrame(frame);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex);
        }

        frame.setBackground(BACKGROUND_COLOR);
        frame.pack();
        frame.centerOnScreen();
        frame.setVisible(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                           public fields                   ////

    /** The default background color.  To change the background color,
     *  use a {@link ptolemy.actor.gui.PtolemyPreferences}.
     */
    public static final Color BACKGROUND_COLOR = new Color(0xe5e5e5);

    ///////////////////////////////////////////////////////////////////
    ////                     public inner classes                  ////

    /** A factory that creates graph editing tableaux for Ptolemy models.
     */
    public static class Factory extends TableauFactory {
        /** Create an factory with the given name and container.
         *  @param container The container.
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

        /** Create an instance of OntologyGraphTableau for the specified effigy,
         *  if it is an effigy for an instance of Ontology.
         *  @param effigy The effigy for an Ontology.
         *  @return A new OntologyGraphTableau, if the effigy is a PtolemyEffigy
         *   that references an Ontology, or null otherwise.
         *  @exception Exception If an exception occurs when creating the
         *   tableau.
         */
        @Override
        public Tableau createTableau(Effigy effigy) throws Exception {
            if (!(effigy instanceof PtolemyEffigy)) {
                return null;
            }

            Tableau tableau = (Tableau) effigy
                    .getEntity("OntologyGraphTableau");
            if (tableau != null) {
                return tableau;
            }

            NamedObj model = ((PtolemyEffigy) effigy).getModel();

            if (model instanceof Ontology) {
                // Check to see whether this factory contains a
                // default library.
                LibraryAttribute library = (LibraryAttribute) getAttribute(
                        "_library", LibraryAttribute.class);

                tableau = new OntologyGraphTableau((PtolemyEffigy) effigy,
                        "OntologyGraphTableau", library);
                return tableau;
            } else {
                return null;
            }
        }
    }
}
