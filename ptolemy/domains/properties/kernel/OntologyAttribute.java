/*
 * An attribute that contains a ontology model graph.
 * 
 * Below is the copyright agreement for the Ptolemy II system.
 * 
 * Copyright (c) 2009 The Regents of the University of California. All rights
 * reserved.
 * 
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
package ptolemy.domains.properties.kernel;

import ptolemy.actor.CompositeActor;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.vergil.toolbox.TextEditorTableauFactory;

/**
 * An attribute that contains a ontology model graph.
 * 
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class OntologyAttribute extends ModelAttribute {

    /**
     * Construct an ontology attribute with the specified container and name.
     * @param container The specified container.
     * @param name The specified name.
     * @exception IllegalActionException If the attribute is not of an
     * acceptable class for the container, or if the name contains a period.
     * @exception NameDuplicationException If the name coincides with an
     * attribute already in the container.
     */
    public OntologyAttribute(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"100\" height=\"40\" "
                + "style=\"fill:yellow\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:black\">"
                + "User-defined\nOntology</text></svg>");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    //    public void parseSpecificationRules() {
    //    }
    //
    //    public Object executeRules() {
    //        return null;
    //    }

    /**
     * Return the class name of the contained model top-level. A property
     * lattice graph is contained by a OntologyComposite, so this returns the
     * string
     * "ptolemy.domains.properties.kernel.OntologyAttribute$OntologyComposite".
     * @return the class name of the contained model top-level.
     */
    protected String _getContainedModelClassName() {
        return getClass().getName() + "$OntologyComposite";
    }

    /**
     * A composite actor that contains definition of an ontology.
     */
    public static class OntologyComposite extends CompositeActor {

        /**
         * Construct an ontology composite with the specified container and
         * name.
         * @param container The specified container.
         * @param name The specified name.
         * @exception IllegalActionException If the composite is not of an
         * acceptable class for the container, or if the name contains a period.
         * @exception NameDuplicationException If the name coincides with an
         * attribute already in the container.
         */
        public OntologyComposite(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        /**
         * Construct an OntologyComposite in the specified workspace with an
         * empty string as a name.
         * @param workspace The specified workspace.
         */
        public OntologyComposite(Workspace workspace) {
            super(workspace);
        }

        ///////////////////////////////////////////////////////////////////
        ////                          public fields                    ////

        /**
         * The name for the rules attributes.
         */
        public static final String RULES = "_rules";

        /**
         * Add an entity to this container. An entity is a representative of its
         * type in an ontology composite. Each entity is added an "_rules"
         * attribute that specifies the property resolution constraints for this
         * type of entities.
         * @param entity Entity to contain.
         * @exception IllegalActionException If the "_rules" attribute or the
         * entity cannot be added.
         * @exception NameDuplicationException If the name collides with a name
         * already on the actor contents list.
         */
        protected void _addEntity(ComponentEntity entity)
                throws IllegalActionException, NameDuplicationException {

            if (entity.getAttribute(RULES) == null) {
                StringAttribute userRules = new StringAttribute(entity, RULES);
                userRules.setVisibility(Settable.EXPERT);
            }

            if (entity.getAttribute("_tableauFactory") == null) {
                // FIXME: This class uses classes from vergil, which means the backend
                // and the gui are too tighly intertwined.
                TextEditorTableauFactory factory = new TextEditorTableauFactory(
                        entity, "_tableauFactory");
                factory.attributeName.setExpression(RULES);

            }
            super._addEntity(entity);
        }
    }
}
