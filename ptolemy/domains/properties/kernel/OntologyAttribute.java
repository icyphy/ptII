/*
Below is the copyright agreement for the Ptolemy II system.
Version: $Id$

Copyright (c) 2009 The Regents of the University of California.
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

public class OntologyAttribute extends ModelAttribute {

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

    public void parseSpecificationRules() {

    }

    public Object executeRules() {
        return null;
    }

    protected String _getContainedModelClassName() {
        return getClass().getName() + "$OntologyComposite";
    }

    public static class OntologyComposite extends CompositeActor {

        public static final String RULES = "_rules";

        public OntologyComposite(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        public OntologyComposite(Workspace workspace) {
            super(workspace);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        protected void _addEntity(ComponentEntity entity)
        throws IllegalActionException, NameDuplicationException {

            if (entity.getAttribute(RULES) == null) {
                StringAttribute userRules = new StringAttribute(entity, RULES);
                userRules.setVisibility(Settable.EXPERT);
            }

            if (entity.getAttribute("_tableauFactory") == null) {
                TextEditorTableauFactory factory =
                    new TextEditorTableauFactory(entity, "_tableauFactory");
                factory.attributeName.setExpression(RULES);

            }
            super._addEntity(entity);
        }


    }
}
