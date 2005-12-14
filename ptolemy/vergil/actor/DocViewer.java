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

import java.awt.BorderLayout;
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
 FIXME.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class DocViewer extends HTMLViewer {
    
    /** Construct a documentation viewer.
     */
    public DocViewer(final NamedObj target, Configuration configuration) {
        super();
        
        _configuration = configuration;
        
        // Panel for added sections.
        JPanel topHalf = new JPanel();
        topHalf.setLayout(new BoxLayout(topHalf, BoxLayout.Y_AXIS));
        getContentPane().add(topHalf, BorderLayout.NORTH);
        
        // Panel for title.
        JPanel topPanel = new JPanel();
        topHalf.add(topPanel);
        // Create a title area.
        String className = target.getClassName();
        String title = className;
        int lastDot = className.lastIndexOf(".");
        if (lastDot > 0) {
            title = className.substring(lastDot + 1) 
                    + "&nbsp; &nbsp; &nbsp; (" + className + ")";
        }
        JEditorPane titlePane = new JEditorPane();
        titlePane.setContentType("text/html");
        titlePane.setEditable(false);
        titlePane.setText(_HTML_HEADER + "<H2>&nbsp; " + title + "</H2>" + _HTML_TAIL);
        Dimension titleSize = new Dimension(_DESCRIPTION_WIDTH + _ICON_WINDOW_WIDTH + 5, 40);
        titlePane.setMinimumSize(titleSize);
        titlePane.setPreferredSize(titleSize);
        titlePane.setSize(titleSize);
        titlePane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        topPanel.add(titlePane, BorderLayout.NORTH);
        
        // Panel for icon and description.
        JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.X_AXIS));
        topHalf.add(middlePanel, BorderLayout.CENTER);
        topHalf.add(Box.createRigidArea(new Dimension(0,5)));
        middlePanel.add(Box.createRigidArea(new Dimension(5,0)));
        // Construct a blank composite entity into which to put
        // an instance of the actor. 
        final CompositeEntity container = new CompositeEntity();
        ActorEditorGraphController controller = new ActorEditorGraphController();
        controller.setConfiguration(getConfiguration());
        ActorGraphModel graphModel = new ActorGraphModel(container);
        final GraphPane graphPane = new GraphPane(controller, graphModel);
        final JGraph jgraph = new JGraph(graphPane);
        jgraph.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        jgraph.setMinimumSize(new Dimension(_ICON_WINDOW_WIDTH, _ICON_WINDOW_HEIGHT));
        jgraph.setPreferredSize(new Dimension(_ICON_WINDOW_WIDTH, _ICON_WINDOW_HEIGHT));
        jgraph.setSize(_ICON_WINDOW_WIDTH, _ICON_WINDOW_HEIGHT);
        jgraph.setBackground(BasicGraphFrame.BACKGROUND_COLOR);
        middlePanel.add(jgraph);
        middlePanel.add(Box.createRigidArea(new Dimension(5,0)));
        
        // Create an instance to display.
        // Note that this will display something that looks just
        // like the object we are asking about, including any customizations
        // that are applicable only to this instance.
        // FIXME: Will this work for a class instance?
        String moml = target.exportMoML();
        MoMLChangeRequest request = new MoMLChangeRequest(this, container, moml) {
            protected void _execute() throws Exception {
                super._execute();
                // Now make appropriate modifications.
                // Unfortunately, this code depends on the type of object
                // this is.
                if (target instanceof ComponentEntity) {
                    Entity sample = container.getEntity(target.getName());
                    Iterator ports = sample.portList().iterator();
                    while (ports.hasNext()) {
                        Port port = (Port)ports.next();
                        SingletonParameter show = new SingletonParameter(port, "_showName");
                        show.setExpression("true");
                    }
                } else {
                    // FIXME: Other cases
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
        };
        container.requestChange(request);
        
        // Now generate the body of the documentation.
        StringBuffer html = new StringBuffer();
        html.append(_HTML_HEADER);
        // Start by looking for a doc file.
        String description = "";
        URL toRead = getClass().getClassLoader().getResource(
                className.replace('.', '/') + "Doc.xml");
        try {
            if (toRead != null) {
                DocManager manager = new DocManager(target);
                manager.parse(null, toRead.openStream());
                html.append(manager.getDescription());
            }
        } catch (Exception ex) {
            html.append("Error reading URL: "
                    + toRead.toExternalForm()
                    + "\n<pre>\n"
                    + ex
                    + "\n</pre>\n");
        }
        // Next see whether there is Javadoc, and link to it if there is.
        html.append("\n<p><i>See Also:</i> ");
        String docName = "doc.codeDoc." + className;
        try {
            toRead = getClass().getClassLoader().getResource(
                    docName.replace('.', '/') + ".html");
            
            if (toRead != null) {
                html.append("<a href=\""
                    + toRead.toExternalForm()
                    + "\">Javadoc documentation</a>");
            } else {
                // FIXME: Make this a hyperlink to a doc on how
                // to create the javadocs.
                html.append("No javadocs found.");
            }
        } catch (Exception ex) {
            html.append("Error opening javadoc file:\n<pre>"
                    + ex
                    + "/n</pre>\n");
        }
        html.append("</p>");
        // Finally, create a pane in which to display it.
        // Create the port and parameter information display.
        JEditorPane descriptionPane = new JEditorPane();
        
        descriptionPane.addHyperlinkListener(this);
        descriptionPane.setContentType("text/html");
        descriptionPane.setEditable(false);
        JScrollPane scroller = new JScrollPane(descriptionPane);
        scroller.setPreferredSize(new Dimension(_DESCRIPTION_WIDTH, _ICON_WINDOW_HEIGHT));
        scroller.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        middlePanel.add(scroller);
        middlePanel.add(Box.createRigidArea(new Dimension(5,0)));
        html.append(_HTML_TAIL);
        descriptionPane.setText(html.toString());

        // Create tables to contain the information about parameters and ports.
        StringBuffer info = new StringBuffer();
        info.append(_HTML_HEADER);
        String tr = "<tr>\n";
        String tre = "</tr>\n";
        String td1 = "<td width=\"20%\">";
        String td2 = "<td width=\"80%\">";
        String tde = "</td>";
        // Start with parameters.
        boolean foundOne = false;
        StringBuffer parameterTable = new StringBuffer();
        parameterTable.append("<table width=\"100%\"  border=\"2\" cellspacing=\"1\" cellpadding=\"1\">\n");
        Iterator attributes = target.attributeList(Settable.class).iterator();
        while (attributes.hasNext()) {
            Settable parameter = (Settable)attributes.next();
            if (parameter.getVisibility() == Settable.FULL) {
                parameterTable.append(tr);
                parameterTable.append(td1);
                parameterTable.append(parameter.getName());
                parameterTable.append(tde);
                parameterTable.append(td2);
                parameterTable.append("FIXME");
                parameterTable.append(tde);
                parameterTable.append(tre);
                foundOne = true;
            }
        }
        parameterTable.append("</table>");
        if (foundOne) {
            info.append("<H2>Parameters</H2>\n");
            info.append(parameterTable);
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
            inputPorts.append("<table width=\"100%\"  border=\"1\" cellspacing=\"1\" cellpadding=\"1\">\n");
            outputPorts.append("<table width=\"100%\"  border=\"1\" cellspacing=\"1\" cellpadding=\"1\">\n");
            inputOutputPorts.append("<table width=\"100%\"  border=\"1\" cellspacing=\"1\" cellpadding=\"1\">\n");
            neitherPorts.append("<table width=\"100%\"  border=\"1\" cellspacing=\"1\" cellpadding=\"1\">\n");
            Iterator ports = ((Entity)target).portList().iterator();
            while (ports.hasNext()) {
                Port port = (Port)ports.next();
                if (port instanceof IOPort) {
                    if (((IOPort)port).isInput() && !((IOPort)port).isOutput()) {
                        inputPorts.append(tr);
                        inputPorts.append(td1);
                        inputPorts.append(port.getName());
                        inputPorts.append(tde);
                        inputPorts.append(td2);
                        inputPorts.append("FIXME");
                        inputPorts.append(tde);
                        inputPorts.append(tre);
                        foundInput = true;                                            
                    } else if (((IOPort)port).isOutput() && !((IOPort)port).isInput()) {
                        outputPorts.append(tr);
                        outputPorts.append(td1);
                        outputPorts.append(port.getName());
                        outputPorts.append(tde);
                        outputPorts.append(td2);
                        outputPorts.append("FIXME");
                        outputPorts.append(tde);
                        outputPorts.append(tre);
                        foundOutput = true;                        
                    } else if (((IOPort)port).isOutput() && ((IOPort)port).isInput()) {
                        inputOutputPorts.append(tr);
                        inputOutputPorts.append(td1);
                        inputOutputPorts.append(port.getName());
                        inputOutputPorts.append(tde);
                        inputOutputPorts.append(td2);
                        inputOutputPorts.append("FIXME");
                        inputOutputPorts.append(tde);
                        inputOutputPorts.append(tre);
                        foundInputOutput = true;                    
                    } else {
                        neitherPorts.append(tr);
                        neitherPorts.append(td1);
                        neitherPorts.append(port.getName());
                        neitherPorts.append(tde);
                        neitherPorts.append(td2);
                        neitherPorts.append("FIXME");
                        neitherPorts.append(tde);
                        neitherPorts.append(tre);
                        foundNeither = true;                    
                    }
                } else {
                    neitherPorts.append(tr);
                    neitherPorts.append(td1);
                    neitherPorts.append(port.getName());
                    neitherPorts.append(tde);
                    neitherPorts.append(td2);
                    neitherPorts.append("FIXME");
                    neitherPorts.append(tde);
                    neitherPorts.append(tre);
                    foundNeither = true;                    
                }
            }
            inputPorts.append("</table>");
            outputPorts.append("</table>");
            inputOutputPorts.append("</table>");
            neitherPorts.append("</table>");
            if (foundInput) {
                info.append("<H2>Input Ports</H2>\n");
                info.append(inputPorts);
            }
            if (foundOutput) {
                info.append("<H2>Output Ports</H2>\n");
                info.append(outputPorts);
            }
            if (foundInputOutput) {
                info.append("<H2>Input/Output Ports</H2>\n");
                info.append(inputOutputPorts);
            }
            if (foundNeither) {
                info.append("<H2>Ports (Neither Input nor Output)</H2>\n");
                info.append(neitherPorts);
            }
        }
        // Finally, insert all.
        info.append(_HTML_TAIL);
        // FIXME: this line does not compile
        //setText(info.toString());
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
    ////                         private variables                 ////
    
    /** The configuration specified in the constructor. */
    private Configuration _configuration;
    
    /** Width of the description pane. */
    private static int _DESCRIPTION_WIDTH = 600;
    
    /** HTML Header information. */
    // FIXME: Font doesn't affect anything much!!!!
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
            + "</head><body>"
            + "<font face=\"Arial, Helvetica, sans-serif\">";

    private static String _HTML_TAIL
            = "</font></body></html>";

    /** Icon window width. */
    private static int _ICON_WINDOW_HEIGHT = 200;
    
    /** Icon window width. */
    private static int _ICON_WINDOW_WIDTH = 200;
    
    /** Padding in icon window. */
    private static int _PADDING = 10;
}
