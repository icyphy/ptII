/* A viewer for actor documentation.

 Copyright (c) 2000-2005 The Regents of the University of California.
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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;

import ptolemy.actor.IOPort;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.HTMLViewer;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.basic.BasicGraphFrame;
import diva.canvas.CanvasUtilities;
import diva.canvas.JCanvas;
import diva.graph.GraphPane;
import diva.graph.JGraph;

//////////////////////////////////////////////////////////////////////////
//// DocViewer

/**
 This class defines a specialized window for displaying Ptolemy II actor
 documentation. The three versions of the constructor offer mechanisms
 to display documentation for a particular actor instance or a specified
 actor class name, or to display a specified documentation file.
 The documentation file is expected to be an XML file using the
 DocML schema, as defined in the DocManager class.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.1
 @see DocManager
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class DocViewer extends HTMLViewer {
    
    /** Construct a documentation viewer for the specified target.
     *  @param target The object to get documentation for.
     *  @param configuration The configuration in charge of this viewer.
     */
    public DocViewer(NamedObj target, Configuration configuration) {
        super();
        try {
            _init(target, configuration, target.getClassName(), null);
        } catch (ClassNotFoundException e) {
            // Should not happen.
            throw new InternalErrorException("Unexpected exception");
        }
    }

    /** Construct a documentation viewer for the specified class name.
     *  @param className The class name to get documentation for.
     *  @param configuration The configuration in charge of this viewer.
     *  @throws ClassNotFoundException If the class cannot be found.
     */
    public DocViewer(String className, Configuration configuration)
            throws ClassNotFoundException {
        super();
        _init(null, configuration, className, null);
    }

    /** Construct a documentation viewer for the specified documentation file.
     *  @param url The URL at which to find the documentation.
     *  @param configuration The configuration in charge of this viewer.
     */
    public DocViewer(URL url, Configuration configuration) {
        super();
        try {
            _init(null, configuration, null, url);
        } catch (ClassNotFoundException e) {
            // Should not happen.
            throw new InternalErrorException("Unexpected exception");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the configuration specified in the constructor.
     *  @return The configuration controlling this frame, or null
     *   if there isn't one.
     */
    public Configuration getConfiguration() {
        return _configuration;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Override the base class to do nothing.
     *  The main content pane is added after the top content.
     */
    protected void _addMainPane() {
    }
    
    /** Override the base class to do nothing.
     *  @param width The width.
     *  @param height The width.
     */
    protected void _setScrollerSize(final int width, final int height) {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    /** Adjust the icon display for the specified target.
     * @param sample The instance whose icon is displayed.
     * @param container The container of the sample instance.
     * @param graphPane The graph pane in which it is displayed.
     * @param jgraph The jgraph.
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    private void _adjustIconDisplay(
            final NamedObj sample,
            final CompositeEntity container,
            final GraphPane graphPane,
            final JGraph jgraph)
            throws IllegalActionException, NameDuplicationException {
        // Now make appropriate modifications.
        // First, if the object has ports, add parameters to the ports
        // to display them.
        if (sample instanceof Entity) {
            Iterator ports = ((Entity)sample).portList().iterator();
            while (ports.hasNext()) {
                Port port = (Port)ports.next();
                SingletonParameter show = new SingletonParameter(port, "_showName");
                show.setExpression("true");
            }
        }
        // Next, set options to display parameter values.
        StringParameter show = new StringParameter(container, "_showParameters");
        show.setExpression("All");
        
        // Defer this to get it to happen after rendering.
        Runnable defer = new Runnable() {
            public void run() {
                Rectangle2D bounds = graphPane.getForegroundLayer().getLayerBounds();
                if (!bounds.isEmpty()) {
                    Dimension size = jgraph.getSize();
                    Rectangle2D viewSize = new Rectangle2D.Double(
                            _PADDING, _PADDING,
                            size.getWidth() - 2 * _PADDING, size.getHeight() - 2 * _PADDING);
                    AffineTransform newTransform = CanvasUtilities.computeFitTransform(
                            bounds, viewSize);
                    JCanvas canvas = graphPane.getCanvas();
                    canvas.getCanvasPane().setTransform(newTransform);
                }                        
            }
        };
        SwingUtilities.invokeLater(defer);
    }

    /** Construct a documentation viewer for the specified target,
     *  class name, or URL. Normally, one of these three arguments
     *  will be non-null.
     *  @param target The object to get documentation for, or null
     *   to base this on the specified class name.
     *  @param configuration The configuration in charge of this viewer.
     *  @param className The class name of the target, or null if a target
     *   is given.
     *  @param url The URL from which to read the doc file, or null to
     *   infer it from the target or className.
     */
    private void _init(
            final NamedObj target,
            Configuration configuration,
            String className,
            URL url)
            throws ClassNotFoundException {
        _configuration = configuration;
        
        // Start by creating a doc manager.
        DocManager manager;
        if (target != null) {
            manager = new DocManager(target);
        } else if (className != null) {
            manager = new DocManager(Class.forName(className));
        } else if (url != null) {
            manager = new DocManager(url);
        } else {
            throw new InternalErrorException("Need to specify one of target, className, or url!");
        }
        className = manager.getClassName();
        final String rootName;
        int lastPeriod = className.lastIndexOf(".");
        if (lastPeriod >= 0) {
            rootName = className.substring(lastPeriod + 1);
        } else {
            rootName = className;
        }

        // Spacer at the top.
        Container contentPane = getContentPane();
        Dimension horizontalSpace = new Dimension(_SPACING, 0);
        Dimension verticalSpace = new Dimension(0, _SPACING);
        contentPane.add(Box.createRigidArea(verticalSpace));
        
        // Panel for title.
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
        contentPane.add(titlePanel);
        
        // Create a title area.
        String title = className;
        if (manager.isInstanceDoc()) {
            title = target.getName()
                    + "&nbsp; &nbsp; &nbsp; (Instance of " + className + ")";
        } else {
            int lastDot = className.lastIndexOf(".");
            if (lastDot > 0) {
                title = className.substring(lastDot + 1) 
                + "&nbsp; &nbsp; &nbsp; (" + className + ")";
            }
        }
        JEditorPane titlePane = new JEditorPane();
        titlePane.setContentType("text/html");
        titlePane.setEditable(false);
        titlePane.setText(_HTML_HEADER + "<H2>&nbsp; " + title + "</H2>" + _HTML_TAIL);
        Dimension titleSize = new Dimension(_DESCRIPTION_WIDTH + _ICON_WINDOW_WIDTH + _SPACING, 40);
        titlePane.setPreferredSize(titleSize);
        titlePane.setSize(titleSize);
        titlePane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        titlePanel.add(Box.createRigidArea(horizontalSpace));
        titlePanel.add(titlePane);
        titlePanel.add(Box.createRigidArea(horizontalSpace));
        
        // Panel for icon and description.
        JPanel descriptionPanel = new JPanel();
        descriptionPanel.setLayout(new BoxLayout(descriptionPanel, BoxLayout.X_AXIS));
        contentPane.add(Box.createRigidArea(verticalSpace));
        contentPane.add(descriptionPanel);
        descriptionPanel.add(Box.createRigidArea(horizontalSpace));
        // Construct a blank composite entity into which to put
        // an instance of the actor. 
        final CompositeEntity container = new CompositeEntity();
        ActorEditorGraphController controller = new ActorEditorGraphController();
        controller.setConfiguration(getConfiguration());
        ActorGraphModel graphModel = new ActorGraphModel(container);
        final GraphPane graphPane = new GraphPane(controller, graphModel);
        final JGraph jgraph = new JGraph(graphPane);
        jgraph.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        // The icon window is fixed size.
        jgraph.setMinimumSize(new Dimension(_ICON_WINDOW_WIDTH, _ICON_WINDOW_HEIGHT));
        jgraph.setMaximumSize(new Dimension(_ICON_WINDOW_WIDTH, _ICON_WINDOW_HEIGHT));
        jgraph.setPreferredSize(new Dimension(_ICON_WINDOW_WIDTH, _ICON_WINDOW_HEIGHT));
        jgraph.setSize(_ICON_WINDOW_WIDTH, _ICON_WINDOW_HEIGHT);
        jgraph.setBackground(BasicGraphFrame.BACKGROUND_COLOR);
        descriptionPanel.add(jgraph);
        descriptionPanel.add(Box.createRigidArea(horizontalSpace));
        // Create a pane in which to display the description.
        final JEditorPane descriptionPane = new JEditorPane();
        descriptionPane.addHyperlinkListener(this);
        descriptionPane.setContentType("text/html");
        descriptionPane.setEditable(false);
        JScrollPane scroller = new JScrollPane(descriptionPane);
        scroller.setPreferredSize(new Dimension(_DESCRIPTION_WIDTH, _ICON_WINDOW_HEIGHT));
        scroller.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        descriptionPanel.add(scroller);
        descriptionPanel.add(Box.createRigidArea(horizontalSpace));
        
        // Add the main content pane now.
        JPanel middle = new JPanel();
        middle.setLayout(new BoxLayout(middle, BoxLayout.X_AXIS));
        contentPane.add(Box.createRigidArea(verticalSpace));
        contentPane.add(middle);
        _scroller = new JScrollPane(pane);
        // Default, which can be overridden by calling setSize().
        _scroller.setPreferredSize(new Dimension(_MAIN_WINDOW_WIDTH, _MAIN_WINDOW_HEIGHT));
        _scroller.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        middle.add(Box.createRigidArea(horizontalSpace));
        middle.add(_scroller);
        middle.add(Box.createRigidArea(horizontalSpace));

        // Panel for added sections at the bottom.
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        contentPane.add(Box.createRigidArea(verticalSpace));
        contentPane.add(bottom);
        contentPane.add(Box.createRigidArea(verticalSpace));
        bottom.add(Box.createRigidArea(horizontalSpace));
        // Pane for author, etc.
        JEditorPane authorPane = new JEditorPane();
        authorPane.addHyperlinkListener(this);
        authorPane.setContentType("text/html");
        authorPane.setEditable(false);
        JScrollPane authorScroller = new JScrollPane(authorPane);
        Dimension authorSize = new Dimension(_AUTHOR_WINDOW_WIDTH, _BOTTOM_HEIGHT);
        authorScroller.setPreferredSize(authorSize);
        authorScroller.setSize(authorSize);
        authorScroller.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        bottom.add(authorScroller);
        bottom.add(Box.createRigidArea(horizontalSpace));
        // Pane for "see also" information.
        JEditorPane seeAlsoPane = new JEditorPane();
        seeAlsoPane.addHyperlinkListener(this);
        seeAlsoPane.setContentType("text/html");
        seeAlsoPane.setEditable(false);
        JScrollPane seeAlsoScroller = new JScrollPane(seeAlsoPane);
        Dimension seeAlsoSize = new Dimension(_SEE_ALSO_WIDTH, _BOTTOM_HEIGHT);
        seeAlsoScroller.setPreferredSize(seeAlsoSize);
        seeAlsoScroller.setSize(seeAlsoSize);
        seeAlsoScroller.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        bottom.add(seeAlsoScroller);
        bottom.add(Box.createRigidArea(horizontalSpace));

        //////////////////////////////////////////////////////
        // Create the content.
        
        // Now generate the body of the documentation.
        StringBuffer html = new StringBuffer();
        html.append(_HTML_HEADER);
        String description = manager.getDescription();
        html.append(description);
        html.append(_HTML_TAIL);
        descriptionPane.setText(html.toString());

        // Create an instance to display.
        // Note that this will display something that looks just
        // like the object we are asking about, including any customizations
        // that are applicable only to this instance.
        // FIXME: Will this work for a class instance?
        // If the target is given, then export MoML from it to use.
        // Otherwise, use the class name.
        String moml = null;
        if (target != null) {
            moml = target.exportMoML();
        } else if (!manager.hadException()) {
            // NOTE: This will not work if a URL was specified and the parse failed.
            // No target is given. Try to create an instance from the class name.
            // FIXME: This only works if the class is an entity.
            moml = "<entity name=\"" + rootName + "\" class=\"" + className + "\"/>";
        }
        if (moml != null) {
            MoMLChangeRequest request = new MoMLChangeRequest(this, container, moml) {
                protected void _execute() throws Exception {
                    super._execute();
                    NamedObj sample = null;
                    if (target == null) {
                        // FIXME: This only works if the class is an entity.
                        sample = container.getEntity(rootName);
                    } else if (target instanceof ComponentEntity) {
                        sample = container.getEntity(target.getName());
                    } else {
                        // FIXME: Other cases.
                    }
                    _adjustIconDisplay(sample, container, graphPane, jgraph);
                }
            };
            container.requestChange(request);
        }

        // Create tables to contain the information about parameters and ports.
        String tr = "<tr valign=top>\n";
        String tre = "</tr>\n";
        String td = "<td>";
        String tdColSpan = "<td colspan=2>";
        String tde = "</td>";
        String tableOpening = "<table cellspacing=2 cellpadding=2>\n";
        String tableClosing = "</table>";
        // Start with parameters.
        boolean foundOne = false;
        boolean foundParameter = false;
        StringBuffer table = new StringBuffer();
        StringBuffer parameters = new StringBuffer();
        if (target != null) {
            Iterator attributes = target.attributeList(Settable.class).iterator();
            while (attributes.hasNext()) {
                Settable parameter = (Settable)attributes.next();
                String doc = manager.getPropertyDoc(parameter.getName());
                if (doc == null) {
                    doc = "No description.";
                }
                if (parameter.getVisibility() == Settable.FULL) {
                    parameters.append(tr);
                    parameters.append(td);
                    parameters.append("<i>" + parameter.getName() + "</i>");
                    parameters.append(tde);
                    parameters.append(td);
                    parameters.append(doc);
                    parameters.append(tde);
                    parameters.append(tre);
                    foundOne = true;
                    foundParameter = true;
                }
            }
        } else {
            // There is no target, so just list all the attributes in the doc file.
            // FIXME: Should use the sample... So this needs to be done as part of
            // the change request above!
        }
        if (foundParameter) {
            table.append(tr);
            table.append(tdColSpan);
            table.append("<h2>Parameters</h2>");
            table.append(tde);
            table.append(tre);
            table.append(parameters);
        }
        // Next do the ports.
        if (target instanceof Entity) {
            boolean foundInput = false;
            boolean foundOutput = false;
            boolean foundInputOutput = false;
            boolean foundNeither = false;
            StringBuffer inputPorts = new StringBuffer();
            StringBuffer outputPorts = new StringBuffer();
            StringBuffer inputOutputPorts = new StringBuffer();
            StringBuffer neitherPorts = new StringBuffer();
            Iterator ports = ((Entity)target).portList().iterator();
            while (ports.hasNext()) {
                Port port = (Port)ports.next();
                String portName = "<i>" + port.getName() + "</i>";
                String doc = manager.getPortDoc(port.getName());
                if (doc == null) {
                    doc = "No port description.";
                }
                if (port instanceof IOPort) {
                    if (((IOPort)port).isInput() && !((IOPort)port).isOutput()) {
                        inputPorts.append(tr);
                        inputPorts.append(td);
                        inputPorts.append(portName);
                        inputPorts.append(tde);
                        inputPorts.append(td);
                        inputPorts.append(doc);
                        inputPorts.append(tde);
                        inputPorts.append(tre);
                        foundInput = true;
                        foundOne = true;
                    } else if (((IOPort)port).isOutput() && !((IOPort)port).isInput()) {
                        outputPorts.append(tr);
                        outputPorts.append(td);
                        outputPorts.append(portName);
                        outputPorts.append(tde);
                        outputPorts.append(td);
                        outputPorts.append(doc);
                        outputPorts.append(tde);
                        outputPorts.append(tre);
                        foundOutput = true;                        
                        foundOne = true;
                    } else if (((IOPort)port).isOutput() && ((IOPort)port).isInput()) {
                        inputOutputPorts.append(tr);
                        inputOutputPorts.append(td);
                        inputOutputPorts.append(portName);
                        inputOutputPorts.append(tde);
                        inputOutputPorts.append(td);
                        inputOutputPorts.append(doc);
                        inputOutputPorts.append(tde);
                        inputOutputPorts.append(tre);
                        foundInputOutput = true;                    
                        foundOne = true;
                    } else {
                        neitherPorts.append(tr);
                        neitherPorts.append(td);
                        neitherPorts.append(portName);
                        neitherPorts.append(tde);
                        neitherPorts.append(td);
                        neitherPorts.append(doc);
                        neitherPorts.append(tde);
                        neitherPorts.append(tre);
                        foundNeither = true;                    
                        foundOne = true;
                    }
                } else {
                    neitherPorts.append(tr);
                    neitherPorts.append(td);
                    neitherPorts.append(portName);
                    neitherPorts.append(tde);
                    neitherPorts.append(td);
                    neitherPorts.append(doc);
                    neitherPorts.append(tde);
                    neitherPorts.append(tre);
                    foundNeither = true;                    
                    foundOne = true;
                }
            }
            if (foundInput) {
                table.append(tr);
                table.append(tdColSpan);
                table.append("<h2>Input Ports</h2>");
                table.append(tde);
                table.append(tre);
                table.append(inputPorts);
            }
            if (foundOutput) {
                table.append(tr);
                table.append(tdColSpan);
                table.append("<h2>Output Ports</h2>");
                table.append(tde);
                table.append(tre);
                table.append(outputPorts);
            }
            if (foundInputOutput) {
                table.append(tr);
                table.append(tdColSpan);
                table.append("<h2>Input/Output Ports</h2>");
                table.append(tde);
                table.append(tre);
                table.append(inputOutputPorts);
            }
            if (foundNeither) {
                table.append(tr);
                table.append(tdColSpan);
                table.append("<h2>Ports (Neither Input nor Output)</h2>");
                table.append(tde);
                table.append(tre);
                table.append(neitherPorts);
            }
        } else {
            // Target is not an instance of entity (may be null), so just
            // list all the ports in the doc file.
            // FIXME: Should use the sample... So this needs to be done as part of
            // the change request above!
        }
        // Finally, insert all.
        StringBuffer info = new StringBuffer();
        info.append(_HTML_HEADER);
        if (foundOne) {
            info.append(tableOpening);
            info.append(table);
            info.append(tableClosing);
        } else {
            info.append("No ports or parameters.");
        }
        info.append(_HTML_TAIL);

        setText(info.toString());
        
        // Populate the author window.
        info = new StringBuffer();
        info.append(_HTML_HEADER);
        // Author(s)
        info.append(tableOpening);
        info.append(tr);
        info.append(td);
        info.append("<i>Authors:</i> ");
        info.append(tde);
        info.append(td);
        info.append(manager.getAuthor());
        info.append(tde);
        info.append(tre);
        // Version
        String version = manager.getVersion();
        if (version != null) {
            info.append(tr);
            info.append(td);
            info.append("<i>Version:</i> ");
            info.append(tde);
            info.append(td);
            info.append(version);
            info.append(tde);
            info.append(tre);
        }
        // Since
        String since = manager.getSince();
        if (since != null) {
            info.append(tr);
            info.append(td);
            info.append("<i>Since:</i> ");
            info.append(tde);
            info.append(td);
            info.append(since);
            info.append(tde);
            info.append(tre);
        }
        // Rating
        String rating = manager.getAcceptedRating();
        if (rating != null) {
            info.append(tr);
            info.append(td);
            info.append("<i>Rating:</i> ");
            info.append(tde);
            info.append(td);
            info.append(rating);
            info.append(tde);
            info.append(tre);
        }
        // End of table
        info.append(tableClosing);
        info.append(_HTML_TAIL);
        authorPane.setText(info.toString());

        // Populate the "See Also" window.
        seeAlsoPane.setText(_HTML_HEADER + manager.getSeeAlso() + _HTML_TAIL);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** Author window width. */
    private static int _AUTHOR_WINDOW_WIDTH = 300;

    /** The configuration specified in the constructor. */
    private Configuration _configuration;
    
    /** Bottom window height. */
    private static int _BOTTOM_HEIGHT = 150;

    /** Width of the description pane. */
    private static int _DESCRIPTION_WIDTH = 500;
    
    /** HTML Header information. */
    private static String _HTML_HEADER
            = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\""
            + "\"http://www.w3.org/TR/html4/loose.dtd\">"
            + "\n<html>\n<head>\n"
            + "<title>Ptolemy II Documentation</title>"
            + "<STYLE TYPE=\"text/css\">\n"
            + "<!--\n"
            + "h1, h2, h3, td, tr, body, p {font-family: Arial, Helvetica, sans-serif;}\n"
            + "-->\n"
            + "</STYLE>"
            + "</head><body>";

    private static String _HTML_TAIL
            = "</body></html>";

    /** Icon window width. */
    private static int _ICON_WINDOW_HEIGHT = 200;
    
    /** Icon window width. */
    private static int _ICON_WINDOW_WIDTH = 200;
    
    /** Main window height. */
    private static int _MAIN_WINDOW_HEIGHT = 300;

    /** Main window width. */
    private static int _MAIN_WINDOW_WIDTH = 700;
    
    /** Padding in icon window. */
    private static int _PADDING = 10;

    /** Width of the see also pane. */
    private static int _SEE_ALSO_WIDTH = 400;

    /** Spacing between subwindows. */
    private static int _SPACING = 5;
}
