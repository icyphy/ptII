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

import de.cau.cs.kieler.core.alg.IKielerProgressMonitor;
import de.cau.cs.kieler.core.kgraph.KEdge;
import de.cau.cs.kieler.core.kgraph.KNode;
import de.cau.cs.kieler.core.kgraph.KPort;
import de.cau.cs.kieler.kiml.layout.klayoutdata.KEdgeLayout;
import de.cau.cs.kieler.kiml.layout.klayoutdata.KLayoutDataFactory;
import de.cau.cs.kieler.kiml.layout.klayoutdata.KPoint;
import de.cau.cs.kieler.kiml.layout.klayoutdata.KShapeLayout;
import de.cau.cs.kieler.kiml.layout.util.KimlLayoutUtil;

public class KielerGraphUtil {
    /**
     * Print some status information about the layout run. I.e. the runtime of
     * the layout algorithm given by the progress monitor
     * 
     * @param kgraph
     * @param progressMonitor
     */
    protected static void _printStatus(KNode kgraph,
            IKielerProgressMonitor progressMonitor) {
        System.out.println("KIELER Execution Time: "
                + (progressMonitor.getExecutionTime() * 1000) + " ms");
    }
    
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
     * @return
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

    protected static String _toString(KNode knode) {
        return _toString(knode, 0);
    }

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
     * loading it elsewhere, e.g. a KIELER Graph viewer.
     * 
     * @param kgraph
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
