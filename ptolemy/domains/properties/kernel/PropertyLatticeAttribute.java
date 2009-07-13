/*
 * An attribute that contains a property lattice model graph.
 * 
 * Copyright (c) 2008-2009 The Regents of the University of California. All
 * rights reserved. Permission is hereby granted, without written agreement and
 * without license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies of
 * this software.
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
 * 
 * PT_COPYRIGHT_VERSION_2 COPYRIGHTENDKEY
 * 
 */
package ptolemy.domains.properties.kernel;

import java.util.List;

import ptolemy.data.properties.lattice.PropertyLattice;
import ptolemy.domains.properties.LatticeElement;
import ptolemy.domains.properties.PropertyLatticeComposite;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// PropertyLatticeAttribute

/**
 * An attribute that contains a property lattice model graph.
 * 
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class PropertyLatticeAttribute extends ModelAttribute {

    /**
     * Construct a property lattice attribute with the specified container and
     * name.
     * @param container The specified container.
     * @param name The specified name.
     * @exception IllegalActionException If the attribute is not of an
     * acceptable class for the container, or if the name contains a period.
     * @exception NameDuplicationException If the name coincides with an
     * attribute already in the container.
     */
    public PropertyLatticeAttribute(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        _attachText("_iconDescription", _ICON);
    }

    /**
     * Construct an attribute in the specified workspace with an empty string as
     * a name.
     * @param workspace The specified workspace.
     */
    public PropertyLatticeAttribute(Workspace workspace) {
        super(workspace);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the property lattice by traversing the contained model graph.
     * @return A property lattice.
     */
    public PropertyLattice getPropertyLattice() {
        PropertyLatticeComposite latticeModel = (PropertyLatticeComposite) getContainedModel();

        List<LatticeElement> elements = latticeModel.deepEntityList();

        PropertyLattice lattice = new PropertyLatticeComposite.Lattice(elements) {
            public String toString() {
                return PropertyLatticeAttribute.this.getName();
            }
        };

        return lattice;
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected methods                    ////

    /**
     * Return the class name of the contained model top-level. A property
     * lattice graph is contained by a PropertyLatticeComposite, so this returns
     * the string "ptolemy.domains.properties.PropertyLatticeComposite".
     * @return the class name of the contained model top-level.
     */
    protected String _getContainedModelClassName() {
        return "ptolemy.domains.properties.PropertyLatticeComposite";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /**
     * The icon description used for rendering.
     */
    private static final String _ICON = "<svg>"
            + "<line x1=\"0\" y1=\"-30\" x2=\"18\" y2=\"0\""
            + "  style=\"stroke:#303030; stroke-width:3\"/>"
            + "<line x1=\"0\" y1=\"-30\" x2=\"-18\" y2=\"0\""
            + "  style=\"stroke:#303030; stroke-width:3\"/>"
            + "<line x1=\"0\" y1=\"-30\" x2=\"0\" y2=\"0\""
            + "  style=\"stroke:#303030; stroke-width:3\"/>"
            + "<line x1=\"0\" y1=\"30\" x2=\"18\" y2=\"0\""
            + "  style=\"stroke:#303030; stroke-width:3\"/>"
            + "<line x1=\"0\" y1=\"30\" x2=\"-18\" y2=\"0\""
            + "  style=\"stroke:#303030; stroke-width:3\"/>"
            + "<line x1=\"0\" y1=\"30\" x2=\"0\" y2=\"0\""
            + "  style=\"stroke:#303030; stroke-width:3\"/>"
            + "<circle cx=\"0\" cy=\"-30\" r=\"6\" style=\"fill:blue\"/>"
            + "<circle cx=\"0\" cy=\"30\" r=\"6\" style=\"fill:red\"/>"
            + "<circle cx=\"18\" cy=\"0\" r=\"6\" style=\"fill:white\"/>"
            + "<circle cx=\"-18\" cy=\"0\" r=\"6\" style=\"fill:white\"/>"
            + "<circle cx=\"0\" cy=\"0\" r=\"6\" style=\"fill:white\"/>"
            + "<line x1=\"12\" y1=\"42\" x2=\"12\" y2=\"36\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>"
            + "<line x1=\"9\" y1=\"42\" x2=\"15\" y2=\"42\""
            + "  style=\"stroke:#303030; stroke-width:2\"/>" + "</svg>";
}
