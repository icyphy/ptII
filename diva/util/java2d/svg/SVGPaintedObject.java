/**
 *    '$RCSfile$'
 *
 *     '$Author$'
 *       '$Date$'
 *   '$Revision$'
 *
 *  For Details: http://kepler.ecoinformatics.org
 *
 * Copyright (c) 2003-2013 The Regents of the University of California.
 * All rights reserved.
 *
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the
 * above copyright notice and the following two paragraphs appear in
 * all copies of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN
 * IF THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY
 * OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
 * UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */

package diva.util.java2d.svg;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.swing.svg.GVTTreeBuilderAdapter;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
import org.apache.batik.swing.svg.JSVGComponent;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;

import diva.util.java2d.PaintedObject;

/** A utility class that paints a rendering of an SVG document
 * This class is intended for use as a low-level class to simplify
 * construction of drawn SVG graphics, while leveraging the functionality of
 * Batik to provide more-encompassing rendering abilities than those native to
 * Diva.
 *
 * @version        $Id$
 * @author         Matthew Brooke
 */
public class SVGPaintedObject implements PaintedObject {

    // Note that the PaintedObject interface was deprecated becase we
    // were to use diva.compat.canvas instead.  However, the Ptolemy
    // sources do not include diva.compat.canvas, so cxh made this
    // class undeprecated on 7/05

    /** Create a painted object from the given valid SVG DOM document.
     *
     * @param doc valid <code>org.w3c.dom.Document</code> containing SVG to be
     *            rendered
     */
    public SVGPaintedObject(Document doc) {

        // Has to be an SVGDocument, not just a Document - otherwise we get
        // a ClassCastException in the Batik code
        this.svgDoc = (SVGDocument) doc;

        // Set the component's maximum size so that content
        // bigger than the screen does not cause the creation
        // of unnecessary large images.
        // NOTE that the rendered SVG can never be larger than the initial value
        // of PreferredSize for some bizzarro reason, so we set PreferredSize to
        //the screen size, initially...
        //
        svgComponent = new JSVGComponent(null, false, false) {
            // new JSVGComponent(SVGUserAgent, eventsEnabled, selectableText)

            Dimension screenSize;

            {
                screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                setMaximumSize(screenSize);
                setPreferredSize(screenSize);
            }

            public Dimension getPreferredSize() {
                Dimension s = super.getPreferredSize();
                if (s.width > screenSize.width) {
                    s.width = screenSize.width;
                }
                if (s.height > screenSize.height) {
                    s.height = screenSize.height;
                }
                return s;
            }

            /**
             * This method is called when the component knows the desired
             * size of the window (based on width/height of outermost SVG
             * element). We override it so we can call container.doLayout()
             * to size this component correctly
             *
             * @param d Dimension
             */
            public void setMySize(Dimension d) {
                setPreferredSize(d);
                invalidate();
                container.doLayout();
            }
        };

        _addListeners();

        //SVGComponent must be added to a java.awt.Container to
        // be initialized, sized and renderable
        container.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        container.add(svgComponent);
        container.doLayout();

        //not sure if this is required, but looks feasible
        svgComponent.setRecenterOnResize(true);
        svgComponent.setDoubleBuffered(false);
        svgComponent.setDoubleBufferedRendering(false);
        svgComponent.setIgnoreRepaint(true);
        svgComponent.setDocumentState(JSVGComponent.ALWAYS_STATIC);

        //load the document ***AFTER*** we've added all the listeners...
        svgComponent.setSVGDocument(svgDoc);
    }

    /** Get the bounding box of the object when painted. Implementations of this
     * method should take account of the thickness of the stroke, if there is one.
     *
     * @return Rectangle2D bounding box
     */
    public Rectangle2D getBounds() {

        // svgBounds will be null until the gvtBuildCompleted() callback occurs,
        // after the SVG tree has been constructed and Batik knows the size of
        // the SVG root element
        if (svgBounds == null) {
            svgBounds = _DEFAULT_SVG_BOUNDS;
        }

        return svgBounds;
    }

    /** Paint the shape. Implementations are expected to redraw the entire
     * object. Whether or not the paint overwrites fields in the graphics
     * context such as the current paint, stroke, and composite, depends on the
     * implementing class.
     *
     * @param g2d Graphics2D
     */
    public void paint(Graphics2D g2d) {

        GraphicsNode gNode = svgComponent.getGraphicsNode();
        if (gNode != null) {
            gNode.paint(g2d);
        }
    }

    /**
     * add a listener to receive a callback after Batik has finished rendering
     * the SVG document
     *
     * @param listener SVGRenderingListener
     */
    public void addSVGRenderingListener(SVGRenderingListener listener) {

        if (listener == null) {
            return;
        }

        svgrListenerList.add(listener);
    }

    /**
     * remove an SVGRenderingListener
     *
     * @param listener SVGRenderingListener
     */
    public void removeSVGRenderingListener(SVGRenderingListener listener) {

        if (listener == null) {
            return;
        }

        if (svgrListenerList.contains(listener)) {
            svgrListenerList.remove(listener);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    ///////////////////////////////////////////////////////////////////

    private void _addListeners() {

        // First, this gets called back after GVT tree building done,
        // so we can use it to determine the size of the rendered SVG
        // (size not available until the gvt tree has been built)...
        svgComponent.addGVTTreeBuilderListener(new GVTTreeBuilderAdapter() {

            public void gvtBuildCompleted(GVTTreeBuilderEvent evt) {

                svgBounds = evt.getGVTRoot().getBounds();

                // notify listeners so they can update the UI...
                _notifySVGRenderingListeners();
            }
        });
    }

    private void _notifySVGRenderingListeners() {
        Iterator it = svgrListenerList.iterator();
        while (it.hasNext()) {
            SVGRenderingListener l = (SVGRenderingListener) (it.next());
            if (l != null) {
                l.svgRenderingComplete();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    ///////////////////////////////////////////////////////////////////

    //listeners awaiting callback when svg rendering is finished
    //use Vector instead of ArrayListy since it's synchronized
    private List svgrListenerList = new Vector();

    private final SVGDocument svgDoc;

    private final JSVGComponent svgComponent;

    private final Container container = new Container();

    private static final Rectangle2D _DEFAULT_SVG_BOUNDS = new Rectangle(20, 20);

    private Rectangle2D svgBounds;
}
