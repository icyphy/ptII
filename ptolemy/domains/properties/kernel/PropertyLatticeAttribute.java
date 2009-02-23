/*

 Copyright (c) 2008 The Regents of the University of California.
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
package ptolemy.domains.properties.kernel;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.util.List;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.data.properties.lattice.PropertyLattice;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.fsm.kernel.Configurer;
import ptolemy.domains.properties.LatticeElement;
import ptolemy.domains.properties.PropertyLatticeComposite;
import ptolemy.graph.DirectedAcyclicGraph;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Configurable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import ptolemy.vergil.gt.TransformationAttributeEditorFactory;
import ptolemy.vergil.properties.ModelAttributeController;

//////////////////////////////////////////////////////////////////////////
//// PropertyLatticeAttribute

/**


 @author Man-Kit Leung
 @version $Id: TransformationAttribute.java 52184 2009-01-25 01:32:08Z tfeng $
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class PropertyLatticeAttribute extends ModelAttribute {

    public PropertyLatticeAttribute(NamedObj container, String name)
    throws NameDuplicationException, IllegalActionException {
        super(container, name);
        
        _attachText("_iconDescription", _ICON);
    }

    public PropertyLatticeAttribute(Workspace workspace) {
        super(workspace);
    }

    public PropertyLattice getPropertyLattice() {
        PropertyLatticeComposite latticeModel = 
            (PropertyLatticeComposite) getContainedModel();

        List<LatticeElement> elements = (List<LatticeElement>) latticeModel.deepEntityList();

        //DirectedAcyclicGraph graph = latticeModel.toGraph(, true);
        
        PropertyLattice lattice = new PropertyLatticeComposite.Lattice(elements) {
            public String toString() {
                return PropertyLatticeAttribute.this.getName();
            }
        };

        //lattice.setBasicLattice(graph);

        return lattice;        
    }

    protected String _getContainedModelClassName() {
        return "ptolemy.domains.properties.PropertyLatticeComposite";
    }

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
        + "  style=\"stroke:#303030; stroke-width:2\"/>"
        + "</svg>";

}
