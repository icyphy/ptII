/* The edge controller for links.

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
package ptolemy.vergil.actor;

import java.awt.Color;
import java.util.List;

import ptolemy.actor.PublisherPort;
import ptolemy.actor.SubscriberPort;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.PtolemyPreferences;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.Vertex;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.ContextMenuFactoryCreator;
import ptolemy.vergil.basic.PopupMouseFilter;
import ptolemy.vergil.kernel.Link;
import ptolemy.vergil.toolbox.ConfigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import ptolemy.vergil.toolbox.PtolemyMenuFactory;
import diva.canvas.Figure;
import diva.canvas.Site;
import diva.canvas.connector.Connector;
import diva.canvas.connector.ConnectorAdapter;
import diva.canvas.connector.ConnectorEvent;
import diva.canvas.connector.ConnectorManipulator;
import diva.canvas.connector.ConnectorTarget;
import diva.canvas.connector.ManhattanConnector;
import diva.canvas.connector.PerimeterTarget;
import diva.canvas.connector.Terminal;
import diva.canvas.event.MouseFilter;
import diva.canvas.interactor.ActionInteractor;
import diva.canvas.interactor.SelectionInteractor;
import diva.canvas.interactor.SelectionModel;
import diva.canvas.toolbox.SVGUtilities;
import diva.graph.BasicEdgeController;
import diva.graph.EdgeRenderer;
import diva.graph.GraphController;
import diva.gui.toolbox.MenuCreator;

///////////////////////////////////////////////////////////////////
//// LinkController

/**
 This class provides interaction techniques for edges that are to be
 connected between ports and relations.  Standard interaction
 techniques for an undirected edge are allowed.

 @author Steve Neuendorffer, Contributor: Edward A. Lee, Bert Rodiers
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class LinkController extends BasicEdgeController {
    /** Create a link controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     */
    public LinkController(final GraphController controller) {
        super(controller);

        SelectionModel sm = controller.getSelectionModel();
        SelectionInteractor interactor = (SelectionInteractor) getEdgeInteractor();
        interactor.setSelectionModel(sm);

        // Create and set up the manipulator for connectors
        ConnectorManipulator manipulator = new ConnectorManipulator();
        manipulator.setSnapHalo(4.0);
        manipulator.addConnectorListener(new LinkDropper());
        interactor.setPrototypeDecorator(manipulator);

        // The mouse filter needs to accept regular click or control click
        MouseFilter handleFilter = new MouseFilter(1, 0, 0);
        manipulator.setHandleFilter(handleFilter);

        ConnectorTarget ct = new LinkTarget();
        setConnectorTarget(ct);
        setEdgeRenderer(new LinkRenderer());

        _menuCreator = new MenuCreator(null);
        _menuCreator.setMouseFilter(new PopupMouseFilter());
        interactor.addInteractor(_menuCreator);

        // The contents of the menu is determined by the associated
        // menu factory, which is a protected member of this class.
        // Derived classes can add menu items to it.

        // BEGIN CONFIGURABLE CONTEXT MENUS ////////////////////////////////////
        /** FIXME
         * @todo This location picks up all rt-click menu actions except for
         * those on links (relations) and on the
         */

        List<?> configsList = Configuration.configurations();

        Configuration config = null;
        for (Object name : configsList) {
            config = (Configuration) name;
            if (config != null) {
                break;
            }
        }

        //If a MenuFactory has been defined in the configuration, use this
        //one; otherwise, use the default Ptolemy one:
        if (config != null && cmfCreator == null) {
            cmfCreator = (ContextMenuFactoryCreator) config
                    .getAttribute("contextMenuFactory");
        }
        if (cmfCreator != null) {
            try {
                _menuFactory = (PtolemyMenuFactory) cmfCreator
                        .createContextMenuFactory(controller);
            } catch (Exception ex) {
                //do nothing - will default to ptii right-click menus
                System.out
                .println("Unable to use the alternative right-click menu "
                        + "handler that was specified in the "
                        + "configuration; defaulting to ptii handler. "
                        + "Exception was: " + ex);
            }

        }
        //if the above has failed in any way, _menuFactory will still be null,
        //in which case we should default to ptii context menus
        if (_menuFactory == null) {
            _menuFactory = new PtolemyMenuFactory(controller);
        }

        _configureMenuFactory = new MenuActionFactory(_configureAction);
        _menuFactory.addMenuItemFactory(_configureMenuFactory);
        _menuCreator.setMenuFactory(_menuFactory);

        // Add a double click interactor.
        ActionInteractor doubleClickInteractor = new ActionInteractor(
                _configureAction);
        doubleClickInteractor.setConsuming(false);
        doubleClickInteractor.setMouseFilter(new MouseFilter(1, 0, 0, 2));

        interactor.addInteractor(doubleClickInteractor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the configuration.  This is may be used by derived controllers
     *  to open files or URLs.
     *  @param configuration The configuration.
     */
    public void setConfiguration(Configuration configuration) {
        _configuration = configuration;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Render a visual representation of a link. */
    public static class LinkRenderer implements EdgeRenderer {

        /** Render a visual representation of the given edge.
         *
         *  <p>If a StringAttribute named "_color", or a
         *  ColorAttribute is set then use
         *  that color to draw the line.</p>
         *
         *  <p>If the attribute is named "_color", then the value of
         *  the attribute is passed to
         *  {@link diva.canvas.toolbox.SVGUtilities#getColor(String)}, which
         *  has accepts the following format: If the first character
         *  is "#" or "0", then the value of the attribute is expected
         *  to be in a format suitable for java.awt.Color.decode().
         *  Otherwise, the value of the attribute is passed to checked
         *  against a list of color names defined in
         *  {@link diva.canvas.toolbox.SVGUtilities}, if the color name is
         *  not found, then the value of the attribute is passed to
         *  java.awt.Color.getColor(String) and if there is no match,
         *  then the color black is used.</p>
         *
         *  <p>If the attribute is an instance of
         *  {@link ptolemy.actor.gui.ColorAttribute}, then the
         *  javax.swing.JColorChooser gui will be offered as a way to
         *  edit the color.</p>
         *
         *  <p>If the StringAttribute "_explanation" of the edge is set
         *  then use it to set the tooltip.</p>
         *
         *  <p>If the "_linkBendRadius" preference is read from the
         *  {@link ptolemy.actor.gui.PtolemyPreferences} and used to set
         *  the bend radius.  The default bend radius is 20.</p>
         *
         *  @param edge The edge.
         *  @param tailSite The tail site.
         *  @param headSite The head site.
         *  @return The Connector that represents the edge.
         */
        @Override
        public Connector render(Object edge, Site tailSite, Site headSite) {
            Link link = (Link) edge;
            ManhattanConnector connector = new KielerLayoutConnector(tailSite,
                    headSite, link);

            if (link.getHead() != null && link.getTail() != null) {
                connector.setLineWidth((float) 2.0);
            }

            connector.setUserObject(edge);

            // The default bend radius of 50 is too large...
            // parallel curves look bad.
            connector.setBendRadius(20);

            Relation relation = link.getRelation();

            if (relation != null) {
                String tipText = relation.getName();
                String displayName = relation.getDisplayName();
                if (!tipText.equals(displayName)) {
                    tipText = displayName + " (" + tipText + ")";
                }
                connector.setToolTipText(tipText);

                try {
                    // Old style of colors.
                    // FIXME: This isn't quite right for relation groups.
                    StringAttribute colorAttribute = (StringAttribute) relation
                            .getAttribute("_color", StringAttribute.class);

                    if (colorAttribute != null) {
                        String color = colorAttribute.getExpression();
                        if (color != null && !color.trim().equals("")) {
                            connector.setStrokePaint(SVGUtilities
                                    .getColor(color));
                        }
                    }
                } catch (IllegalActionException e) {
                    // Ignore;
                }
                // New way to specify colors.
                List<ColorAttribute> colorAttributes = relation
                        .attributeList(ColorAttribute.class);
                if (colorAttributes != null && colorAttributes.size() > 0) {
                    // Use the last color added.
                    Color color = colorAttributes.get(
                            colorAttributes.size() - 1).asColor();
                    connector.setStrokePaint(color);
                }

                StringAttribute _explAttr = (StringAttribute) relation
                        .getAttribute("_explanation");

                if (_explAttr != null) {
                    connector.setToolTipText(_explAttr.getExpression());
                }

                // NOTE: The preferences mechanism may set this.
                Token radiusValue = PtolemyPreferences.preferenceValue(
                        relation, "_linkBendRadius");

                if (radiusValue instanceof DoubleToken) {
                    double overrideRadius = ((DoubleToken) radiusValue)
                            .doubleValue();
                    connector.setBendRadius(overrideRadius);
                }
            }

            return connector;
        }
    }

    /** A connector target that returns sites on a link. */
    public static class LinkTarget extends PerimeterTarget {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        /** Accept the head of the connector.
         *  @param c The connector.
         *  @param f The figure.
         *  @return True if the object is a Port, a Vertex or a Locatable
         *  contained by a Port and the super class accepts the head.
         *  Otherwise, return false.
         */
        @Override
        public boolean acceptHead(Connector c, Figure f) {
            Object object = f.getUserObject();

            boolean isPubSubPort = object instanceof PublisherPort
                    || object instanceof SubscriberPort;

            if (object instanceof Port && !isPubSubPort
                    || object instanceof Vertex || object instanceof Link
                    && c != f || object instanceof Locatable
                    && ((Locatable) object).getContainer() instanceof Port) {

                // It is possible to link with an existing link.
                // If this existing link has a vertex as head or tail,
                // we will connect with the vertex, otherwise we will
                // remove the old link, create a new vertex, link the
                // head and tail of the existing link with the
                // vertex and link the new link with the vertex.
                // We don't allow connecting with with yourself, hence the
                // test c != f.

                return super.acceptHead(c, f);
            } else {
                return false;
            }
        }

        /** Accept the tail of the connector.
         *  @param c The connector.
         *  @param f The figure.
         *  @return True if the object is a Port, a Vertex or a Locatable
         *  contained by a Port and the super class accepts the tail
         *  Otherwise, return false.
         */
        @Override
        public boolean acceptTail(Connector c, Figure f) {
            Object object = f.getUserObject();

            boolean isPubSubPort = object instanceof PublisherPort
                    || object instanceof SubscriberPort;

            if (object instanceof Port && !isPubSubPort
                    || object instanceof Vertex || object instanceof Link
                    && c != f || object instanceof Locatable
                    && ((Locatable) object).getContainer() instanceof Port) {

                // It is possible to link with an existing link.
                // If this existing link has a vertex as head or tail,
                // we will connect with the vertex, otherwise we will
                // remove the old link, create a new vertex, link the
                // head and tail of the existing link with the
                // vertex and link the new link with the vertex.
                // We don't allow connecting with with yourself, hence the
                // test c != f.

                return super.acceptTail(c, f);
            } else {
                return false;
            }
        }

        /** Get the head site.
         *  @param f The figure.
         *  @param x The x location.
         *  @param y The y location.
         *  @return The head site.
         */
        @Override
        public Site getHeadSite(Figure f, double x, double y) {
            if (f instanceof Terminal) {
                Site site = ((Terminal) f).getConnectSite();
                return site;
            } else {
                return super.getHeadSite(f, x, y);
            }
        }

        // Tail sites are the same as head sites.
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected members                     ////

    /** The configuration. */
    protected Configuration _configuration;

    /** The configure action, which handles edit parameters requests. */
    protected static ConfigureAction _configureAction = new ConfigureAction(
            "Configure");

    /** The submenu for configure actions. */
    protected MenuActionFactory _configureMenuFactory;

    /** The menu creator. */
    protected MenuCreator _menuCreator;

    /** The factory belonging to the menu creator. */
    protected PtolemyMenuFactory _menuFactory;

    /** An inner class that handles interactive changes to connectivity.
     */
    protected class LinkDropper extends ConnectorAdapter {
        /**
         * Called when a connector end is dropped--attach or
         * detach the edge as appropriate.
         * @param evt The connector event.
         */
        @Override
        public void connectorDropped(ConnectorEvent evt) {
            Connector c = evt.getConnector();
            Figure f = evt.getTarget();
            Link link = (Link) c.getUserObject();
            Object node = f == null ? null : f.getUserObject();
            ActorGraphModel model = (ActorGraphModel) getController()
                    .getGraphModel();

            switch (evt.getEnd()) {
            case ConnectorEvent.HEAD_END:
                if (node == link.getTail()) {
                    MessageHandler
                    .error("Cannot link both ends to the same object.");
                    // FIXME: The panner needs to repaint.  How to get it to do that?
                    return;
                }
                model.getLinkModel().setHead(link, node);
                break;

            case ConnectorEvent.TAIL_END:
                if (node == link.getHead()) {
                    MessageHandler
                    .error("Cannot link both ends to the same object.");
                    // FIXME: The panner needs to repaint.  How to get it to do that?
                    return;
                }
                model.getLinkModel().setTail(link, node);
                break;

            default:
                throw new IllegalStateException(
                        "Cannot handle both ends of an edge being dragged.");
            }

            // Set the width correctly, so we know whether or not it
            // is connected.  Note that this happens *after* the model
            // is modified.
            if (link.getHead() != null && link.getTail() != null) {
                ((ManhattanConnector) c).setLineWidth((float) 2.0);
            } else {
                ((ManhattanConnector) c).setLineWidth((float) 1.0);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private members                       ////

    /** a configurable object that allows a different MenuFactory
     * to be specified instead of the default ptII one.
     * The MenuFactory constructs the right-click context menus
     */
    private static ContextMenuFactoryCreator cmfCreator;

}
