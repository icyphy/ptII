/*
@Copyright (c) 2009 The Regents of the University of California.
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
/* Utilities class to work with the KIELER graph data structure.
 * 
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
import de.cau.cs.kieler.kiml.layout.klayoutdata.KEdgeLayout;
import de.cau.cs.kieler.kiml.layout.klayoutdata.KLayoutDataFactory;
import de.cau.cs.kieler.kiml.layout.klayoutdata.KPoint;
import de.cau.cs.kieler.kiml.layout.klayoutdata.KShapeLayout;
import de.cau.cs.kieler.kiml.layout.util.KimlLayoutUtil;

//////////////////////////////////////////////////////////////////////////
////KielerGraphUtil
/**
 * Static helper class to work with KIELER graph datastructures. 
 * 
 * @author Hauke Fuhrmann, <haf@informatik.uni-kiel.de>
 *
 */
public class KielerGraphUtil {

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Get the absolute layout of a Kieler KNode, i.e. a layout containing
     * an absolute position of the top left corner of the node instead
     * of something relative to only its parent node.
     * 
     * @param node
     *          The KNode of which to retrieve the absolute layout.
     * @return A shape layout containing the original size of the node
     *          and its location in absolute coordinates (not relative
     *          to its parent)
     */
    protected static KShapeLayout _getAbsoluteLayout(KNode node) {
        KShapeLayout klayout = KimlLayoutUtil.getShapeLayout(node);
        KShapeLayout absoluteLayout = KLayoutDataFactory.eINSTANCE
                .createKShapeLayout();
        absoluteLayout.setHeight(klayout.getHeight());
        absoluteLayout.setWidth(klayout.getWidth());
        float offsetX = 0, offsetY = 0;
        KNode parent = node.getParent();
        while (parent != null) {
            KShapeLayout parentLayout = KimlLayoutUtil.getShapeLayout(parent);
            offsetX += parentLayout.getXpos();
            offsetY += parentLayout.getYpos();
            parent = parent.getParent();
        }
        absoluteLayout.setXpos(klayout.getXpos() + offsetX);
        absoluteLayout.setYpos(klayout.getYpos() + offsetY);
        return absoluteLayout;
    }

    /**
     * Reposition a small object in a big object according to a given direction (NORTH, 
     * EAST, SOUTH, WEST). The small object will be aligned to the big object's 
     * direction side and centered on the other coordinate.
     * @param originalBounds
     *          Big object's bounds
     * @param shrunkBounds
     *          Small object's bounds
     * @param direction
     *          Direction of the small object within the big object given by a SwingConstants
     *          direction constant
     * @return  New location of the small object.
     *          
     */
    protected static Point2D _shrinkCoordinates(Rectangle2D originalBounds,
            Rectangle2D shrunkBounds, int direction) {
        double widthDiff = (originalBounds.getWidth() - shrunkBounds.getWidth()) / 2;
        double heightDiff = (originalBounds.getHeight() - shrunkBounds
                .getHeight()) / 2;
        Point2D.Double location = new Point2D.Double();
        switch (direction) {
        case SwingConstants.NORTH:
            location.x = originalBounds.getMinX() + widthDiff;
            location.y = originalBounds.getMinY();
            break;
        case SwingConstants.EAST:
            location.x = originalBounds.getMaxX() - widthDiff;
            location.y = originalBounds.getMinY() + heightDiff;
            break;
        case SwingConstants.SOUTH:
            location.x = originalBounds.getMinX() + widthDiff;
            location.y = originalBounds.getMaxY() - heightDiff;
            break;
        default:
            location.x = originalBounds.getMinX();
            location.y = originalBounds.getMinY() + heightDiff;
            break;
        }
        return location;
    }

    /**
     * Debug output a KEdge to a String, i.e. will represent all bendpoints in
     * the String.
     * 
     * @param edge 
     *          The edge to be toStringed
     * @return
     *          A String representing the KEdge
     */
    protected static String _toString(KEdge edge) {
        StringBuffer string = new StringBuffer();
        string.append("[E:");
        string.append("Source:" + edge.getSource().hashCode());
        string.append(" Target:" + edge.getTarget().hashCode() + " Bends:");
        KEdgeLayout layout = KimlLayoutUtil.getEdgeLayout(edge);
        for (KPoint point : layout.getBendPoints()) {
            string.append(point.getX() + "," + point.getY() + " ");
        }

        string.append("]");
        return string.toString();
    }

    /**
     * Debug output a KNode to a String, i.e. will represent the
     * whole subgraph starting with this node recursively and
     * also present all outgoing edges of all nodes.
     * 
     * @param knode 
     *          The node to be toStringed
     * @return
     *          A String representing the KNode
     */
    protected static String _toString(KNode knode) {
        return _toString(knode, 0);
    }

    /**
     * Debug output a KNode to a String, i.e. will represent the
     * whole subgraph starting with this node recursively and
     * also present all outgoing edges of all nodes.
     * 
     * @param knode 
     *          The node to be toStringed
     * @param level
     *          Tree level of the currently processed element. Used for
     *          recursive operation.
     * @return
     *          A String representing the KNode
     */
    protected static String _toString(KNode knode, int level) {
        StringBuffer buffer = new StringBuffer();
        KShapeLayout layout = KimlLayoutUtil.getShapeLayout(knode);
        buffer.append("Node: X" + layout.getXpos() + ",Y" + layout.getYpos()
                + ",W" + layout.getWidth() + ",H" + layout.getHeight()
                + " Hash:" + knode.hashCode() + "\n");
        List<KPort> ports = knode.getPorts();
        for (KPort port : ports) {
            buffer.append("      Port: " + port.getType() + port.hashCode()
                    + "\n");
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
     * Write a KGraph (Kieler graph datastructure) to a file in its XMI
     * representation. Can be used for debugging (manually look at it) or
     * loading it elsewhere, e.g. a KIELER Graph viewer. The default filename
     * is kgraph.xmi and will be written to the current working directory.
     * 
     * @param kgraph
     *          The Kieler Graph datastructure given by its root KNode.
     */
    protected static void _writeToFile(KNode kgraph) {
        // Create a resource set.
        ResourceSet resourceSet = new ResourceSetImpl();

        // Register the default resource factory -- only needed for stand-alone!
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put(Resource.Factory.Registry.DEFAULT_EXTENSION,
                        new XMIResourceFactoryImpl());

        try {
            // Get the URI of the model file.
            File file = new File("kgraph.xmi");
            URI fileURI = URI.createFileURI(file.getAbsolutePath());

            // Demand load the resource for this file.
            Resource resource = resourceSet.createResource(fileURI);

            resource.getContents().add(kgraph);

            // Print the contents of the resource to System.out.
            resource.save(Collections.EMPTY_MAP);
        } catch (IOException e) {
        }
    }

}
