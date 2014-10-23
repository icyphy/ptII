/* Static helper class to work with KIELER graph datastructures. */
/*
@Copyright (c) 2009-2014 The Regents of the University of California.
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
package ptolemy.vergil.basic.layout.kieler;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingConstants;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import de.cau.cs.kieler.core.kgraph.KEdge;
import de.cau.cs.kieler.core.kgraph.KNode;
import de.cau.cs.kieler.core.kgraph.KPort;
import de.cau.cs.kieler.core.math.KVector;
import de.cau.cs.kieler.kiml.klayoutdata.KEdgeLayout;
import de.cau.cs.kieler.kiml.klayoutdata.KPoint;
import de.cau.cs.kieler.kiml.klayoutdata.KShapeLayout;
import de.cau.cs.kieler.kiml.util.KimlUtil;

///////////////////////////////////////////////////////////////////
////KielerGraphUtil
/**
 * Static helper class to work with KIELER graph datas structures.
 *
 * @author Hauke Fuhrmann (<a href="mailto:haf@informatik.uni-kiel.de">haf</a>)
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public final class KielerGraphUtil {

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Get the parent node of an KIELER KEdge. That is the KIELER KNode that
     * graphically contains the edge. In particular that is the parent node of
     * the source node of the edge. If the source node is null, then the result
     * is also null.
     *
     * @param edge The KIELER edge to determine the parent node.
     * @return The parent KIELER node of the given edge or null if the source
     *         of the edge is undefined.
     */
    protected static KNode _getParent(KEdge edge) {
        KNode source = edge.getSource();
        if (source == null) {
            return null;
        }
        if (KimlUtil.isDescendant(edge.getTarget(), source)) {
            return source;
        }
        return source.getParent();
    }

    /**
     * Get the upper left corner of the real bounding box of the contents of a
     * given KIELER node. Calculate the minimal x and y coordinates of all
     * nodes contained in the given node.
     *
     * @param parent The composite KIELER node that contains other nodes.
     * @return The minimal x and y coordinates of all contained nodes. Might be
     *         Float.MAX_VALUE, if the parent does not contain any children.
     */
    protected static KVector _getUpperLeftCorner(KNode parent) {
        KVector v = new KVector(Double.MAX_VALUE, Double.MAX_VALUE);
        for (KNode kNode : parent.getChildren()) {
            KShapeLayout layout = kNode.getData(KShapeLayout.class);
            if (layout.getXpos() < v.x) {
                v.x = layout.getXpos();
            }
            if (layout.getYpos() < v.y) {
                v.y = layout.getYpos();
            }
        }
        return v;
    }

    /**
     * Reposition a small object in a big object according to a given direction
     * (NORTH, EAST, SOUTH, WEST). The small object will be aligned to the big
     * object's direction side and centered on the other coordinate.
     *
     * @param originalBounds Big object's bounds
     * @param shrunkBounds Small object's bounds
     * @param direction Direction of the small object within the big object
     *            given by a SwingConstants direction constant
     * @param offset Offset of the lower bound of the port
     * @return New location of the small object.
     */
    protected static Point2D _shrinkCoordinates(Rectangle2D originalBounds,
            Rectangle2D shrunkBounds, int direction, float offset) {
        double widthDiff = originalBounds.getWidth() - shrunkBounds.getWidth();
        double heightDiff = originalBounds.getHeight()
                - shrunkBounds.getHeight();
        Point2D.Double location = new Point2D.Double();
        switch (direction) {
        case SwingConstants.NORTH:
            location.x = originalBounds.getMinX() + widthDiff - offset;
            location.y = originalBounds.getMinY();
            break;
        case SwingConstants.EAST:
            location.x = originalBounds.getMaxX() - widthDiff;
            location.y = originalBounds.getMinY() + offset;
            break;
        case SwingConstants.SOUTH:
            location.x = originalBounds.getMinX() + offset;
            location.y = originalBounds.getMaxY() - heightDiff;
            break;
        default:
            location.x = originalBounds.getMinX();
            location.y = originalBounds.getMinY() + heightDiff - offset;
            break;
        }
        return location;
    }

    /**
     * Debug output a KEdge to a String, i.e. will represent all bend points in
     * the String.
     *
     * @param edge The edge to be toStringed
     * @return A String representing the KEdge
     */
    protected static String _toString(KEdge edge) {
        StringBuffer string = new StringBuffer();
        string.append("[E:");
        string.append("Source:"
                + (edge.getSource() == null ? "null" : edge.getSource()
                        .hashCode()));
        string.append(" Target:"
                + (edge.getSource() == null ? "null" : edge.getTarget()
                        .hashCode()) + " Bends:");
        KEdgeLayout layout = edge.getData(KEdgeLayout.class);
        for (KPoint point : layout.getBendPoints()) {
            string.append(point.getX() + "," + point.getY() + " ");
        }

        string.append("]");
        return string.toString();
    }

    /**
     * Debug output a KNode to a String, i.e. will represent the whole subgraph
     * starting with this node recursively and also present all outgoing edges
     * of all nodes.
     *
     * @param knode The node to be toStringed
     * @return A String representing the KNode
     */
    protected static String _toString(KNode knode) {
        return _toString(knode, 0);
    }

    /**
     * Debug output a KNode to a String, i.e. will represent the whole subgraph
     * starting with this node recursively and also present all outgoing edges
     * of all nodes.
     *
     * @param knode The node to be toStringed
     * @param level Tree level of the currently processed element. Used for
     *            recursive operation.
     * @return A String representing the KNode
     */
    protected static String _toString(KNode knode, int level) {
        StringBuffer buffer = new StringBuffer();
        KShapeLayout layout = knode.getData(KShapeLayout.class);
        buffer.append("Node: X" + layout.getXpos() + ",Y" + layout.getYpos()
                + ",W" + layout.getWidth() + ",H" + layout.getHeight()
                + " Hash:" + knode.hashCode() + "\n");
        List<KPort> ports = knode.getPorts();
        for (KPort port : ports) {
            buffer.append("      Port: " + port.hashCode() + "\n");
        }
        List<KEdge> edges = knode.getOutgoingEdges();
        for (KEdge edge : edges) {
            buffer.append(_toString(edge) + "\n");
        }
        List<KNode> children = knode.getChildren();
        for (KNode node : children) {
            buffer.append(+level + " " + _toString(node, level + 1));
        }
        return buffer.toString();
    }

    /**
     * Write a KGraph (KIELER graph data structure) to a file in its XMI
     * representation. Can be used for debugging (manually look at it) or
     * loading it elsewhere, e.g. a KIELER Graph viewer. The default filename is
     * kgraph.xmi and will be written to the current working directory.
     *
     * @param kgraph The KIELER graph data structure given by its root KNode.
     */
    protected static void _writeToFile(KNode kgraph) {
        // Create a resource set.
        ResourceSet resourceSet = new ResourceSetImpl();

        // Register the default resource factory -- only needed for stand-alone!
        resourceSet
        .getResourceFactoryRegistry()
        .getExtensionToFactoryMap()
        .put(Resource.Factory.Registry.DEFAULT_EXTENSION,
                new XMIResourceFactoryImpl());

        try {
            // Get the URI of the model file.
            File file = new File("kgraph.xmi");
            URI fileURI = URI.createFileURI(file.getAbsolutePath());

            // Demand load the resource for this file.
            Resource resource = resourceSet.createResource(fileURI);

            if (resource == null) {
                throw new NullPointerException(
                        "Could not create a resource for \"" + fileURI + "\"");
            } else {
                resource.getContents().add(kgraph);

                // Print the contents of the resource to System.out.
                resource.save(Collections.EMPTY_MAP);
            }
        } catch (IOException e) {
        }
    }

}
