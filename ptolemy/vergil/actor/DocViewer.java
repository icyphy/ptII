/* A viewer for actor documentation.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.HyperlinkEvent;

import ptolemy.actor.IOPort;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.HTMLViewer;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.InstantiableNamedObj;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Instantiable;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.DocAttribute;
import diva.canvas.CanvasUtilities;
import diva.canvas.JCanvas;
import diva.canvas.toolbox.LabelFigure;
import diva.graph.GraphPane;
import diva.graph.GraphViewEvent;
import diva.graph.JGraph;

///////////////////////////////////////////////////////////////////
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
 @since Ptolemy II 5.2
 @see DocManager
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
@SuppressWarnings("serial")
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
     *  @exception ClassNotFoundException If the class cannot be found.
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
        } catch (Throwable throwable) {
            // Should not happen.
            throw new InternalErrorException(null, throwable, "Unexpected exception initializing viewer for " + url);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the configuration specified in the constructor.
     *  @return The configuration controlling this frame, or null
     *   if there isn't one.
     */
    @Override
    public Configuration getConfiguration() {
        return _configuration;
    }

    /** Override the base class to react to links of the form
     *  #parentClass.
     *  @param event The hyperlink event.
     */
    @Override
    public void hyperlinkUpdate(HyperlinkEvent event) {
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED
                && event.getDescription().equals("#parentClass")) {
            // This should only occur if DocManager has already checked that the following will work.
            // Nonetheless, we look for exceptions and report them.
            try {
                NamedObj parent = (NamedObj) ((Instantiable) _target)
                        .getParent();
                List docAttributes = parent.attributeList(DocAttribute.class);
                DocAttribute attribute = (DocAttribute) docAttributes
                        .get(docAttributes.size() - 1);
                Effigy effigy = getEffigy();
                DocEffigy newEffigy = new DocEffigy(
                        (CompositeEntity) effigy.getContainer(), effigy
                                .getContainer().uniqueName("parentClass"));
                newEffigy.setDocAttribute(attribute);
                DocTableau tableau = new DocTableau(newEffigy, "docTableau");
                tableau.show();
            } catch (Exception e) {
                MessageHandler.error("Error following hyperlink", e);
            }
        } else {
            super.hyperlinkUpdate(event);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to do nothing.
     *  The main content pane is added after the top content.
     */
    @Override
    protected void _addMainPane() {
    }

    /** Add a Build menu item.
     */
    @Override
    protected void _addMenus() {
        super._addMenus();

        Tableau tableau = getTableau();
        if (tableau != null) {
            Effigy tableauContainer = (Effigy) tableau.getContainer();
            if (tableauContainer != null) {
                JMenu buildMenu = new JMenu("Build");
                buildMenu.setMnemonic(KeyEvent.VK_B);
                _menubar.add(buildMenu);

                BuildMenuListener buildMenuListener = new BuildMenuListener();
                String name = "Build docs";
                JMenuItem item = new JMenuItem(name);
                item.setActionCommand(name);
                item.setMnemonic(name.charAt(0));
                item.addActionListener(buildMenuListener);
                buildMenu.add(item);
            }
        }
    }

    /** Display the help file given by the configuration, or if there is
     *  none, then the file specified by the public variable helpFile.
     *  To specify a default help file in the configuration, create
     *  a FileParameter named "_helpDocViewer" whose value is the name of the
     *  file.  If the specified file fails to open, then invoke the
     *  _help() method of the superclass.
     *  @see FileParameter
     */
    @Override
    protected void _help() {
        try {
            Configuration configuration = getConfiguration();
            FileParameter helpAttribute = (FileParameter) configuration
                    .getAttribute("_helpDocViewer", FileParameter.class);
            URL doc;

            if (helpAttribute != null) {
                doc = helpAttribute.asURL();
            } else {
                doc = getClass().getClassLoader().getResource(helpFile);
            }

            configuration.openModel(null, doc, doc.toExternalForm());
        } catch (Exception ex) {
            super._help();
        }
    }

    /** Override the base class to do nothing.
     *  @param width The width.
     *  @param height The width.
     */
    @Override
    protected void _setScrollerSize(final int width, final int height) {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Adjust the icon display for the specified target.
     * @param sample The instance whose icon is displayed.
     * @param container The container of the sample instance.
     * @param graphPane The graph pane in which it is displayed.
     * @param jgraph The jgraph.
     * @exception IllegalActionException
     * @exception NameDuplicationException
     */
    private void _adjustIconDisplay(final NamedObj sample,
            final CompositeEntity container, final GraphPane graphPane,
            final JGraph jgraph) throws IllegalActionException,
            NameDuplicationException {
        // Now make appropriate modifications.
        // First, if the object has ports, add parameters to the ports
        // to display them.
        if (sample instanceof Entity) {
            Iterator ports = ((Entity) sample).portList().iterator();
            while (ports.hasNext()) {
                Port port = (Port) ports.next();
                SingletonParameter show = new SingletonParameter(port,
                        "_showName");
                show.setExpression("true");
            }
        }
        // Next, set options to display parameter values.
        StringParameter show = new StringParameter(container, "_showParameters");
        show.setExpression("All");

        // Defer this to get it to happen after rendering.
        Runnable defer = new Runnable() {
            @Override
            public void run() {
                Rectangle2D bounds = graphPane.getForegroundLayer()
                        .getLayerBounds();
                if (!bounds.isEmpty()) {
                    Dimension size = jgraph.getSize();
                    Rectangle2D viewSize = new Rectangle2D.Double(_PADDING,
                            _PADDING, size.getWidth() - 2 * _PADDING,
                            size.getHeight() - 2 * _PADDING);
                    AffineTransform newTransform = CanvasUtilities
                            .computeFitTransform(bounds, viewSize);
                    JCanvas canvas = graphPane.getCanvas();
                    canvas.getCanvasPane().setTransform(newTransform);
                }
            }
        };
        SwingUtilities.invokeLater(defer);
    }

    /** Return HTML that colorizes the rating text.
     *  @param rating The rating text, such as "Red (mrptolemy)"
     *  @return HTML, such as "<td bgcolor="#FF0000">Red (mrptolemy)</td>"
     */
    private String _colorizeRating(String rating) {
        String color = "#FFFFFF";
        if (rating.startsWith("Red")) {
            color = "#FF0000";
        } else if (rating.startsWith("Yellow")) {
            color = "#AAAA00";
        } else if (rating.startsWith("Green")) {
            color = "#00FF00";
        } else if (rating.startsWith("Blue")) {
            color = "#0000FF";
        }
        //return "<td bgcolor=\"" + color + "\">" + rating + "</td>";
        return "<td><font color=\"" + color + "\">" + rating + "</font></td>";
    }

    /** Return a string with parameter table entries.
     *  @param target The target.
     *  @param manager The manager.
     *  @return Parameter table entries, or null if there are no parameters.
     */
    private String _getParameterEntries(NamedObj target, DocManager manager) {
        //check for exclusion attributes in the configuration
        //exclusion attributes can exclude params from the documentation
        //by their name.  an exclusion can be "exact" or "contains".  an "exact"
        //exclusion requires the name on the exclusion list to exactly match
        //the name of the param.  a "contains" exclusion just requires that the
        //name of exclusion is contained in the name of the exclusion.
        Configuration config = getConfiguration();
        Iterator itt = config.attributeList(
                ptolemy.kernel.util.StringAttribute.class).iterator();
        Vector exclusions = new Vector();
        while (itt.hasNext()) {
            NamedObj att = (NamedObj) itt.next();

            if (att.getName().indexOf("docViewerExclude") != -1) {
                String value = ((StringAttribute) att).getExpression();
                exclusions.addElement(value);
            }
        }

        StringBuffer parameters = new StringBuffer();
        parameters.append(_tr);
        parameters.append(_tdColSpan);
        parameters.append("<h2>Parameters</h2>");
        parameters.append(_tde);
        parameters.append(_tre);
        boolean foundOne = false;
        Iterator attributes = target.attributeList(Settable.class).iterator();
        while (attributes.hasNext()) {
            Settable parameter = (Settable) attributes.next();
            if (_isHidden((NamedObj)parameter)) {
                    continue;
            }
            if (parameter instanceof PortParameter) {
                // Skip this one.
                continue;
            }

            String parameterName = parameter.getName();
            //check to see if this param is on the exclusion list
            for (int i = 0; i < exclusions.size(); i++) {
                String exclusion = (String) exclusions.elementAt(i);
                String type = exclusion.substring(0, exclusion.indexOf(":"));
                exclusion = exclusion.substring(exclusion.indexOf(":") + 1,
                        exclusion.length());
                if (type.equals("contains")) {
                    if (parameterName.indexOf(exclusion) != -1) {
                        parameter.setVisibility(Settable.NONE);
                    }
                } else if (type.equals("exact")) {
                    if (parameterName.equals(exclusion)) {
                        parameter.setVisibility(Settable.NONE);
                    }
                }
            }

            String doc = manager.getPropertyDoc(parameter.getName());
            if (doc == null) {
                doc = "No description.";
                // See if the next tier has documentation.
                DocManager nextTier = manager.getNextTier();
                if (nextTier != null) {
                    String nextDoc = nextTier.getPropertyDoc(parameter
                            .getName());
                    if (nextDoc != null) {
                        doc = nextDoc;
                    }
                }
            }
            if (parameter.getVisibility() == Settable.FULL) {
                parameters.append(_tr);
                parameters.append(_td);
                parameters.append("<i>" + parameter.getDisplayName() + "</i>");
                parameters.append(_tde);
                parameters.append(_td);
                parameters.append(doc);
                parameters.append(_tde);
                parameters.append(_tre);
                foundOne = true;
            }
        }
        if (foundOne) {
            return parameters.toString();
        } else {
            return null;
        }
    }

    /** Return a string with port table entries.
     *  @param target The target.
     *  @param manager The manager.
     *  @return Port table entries, or null if there are no ports.
     */
    private String _getPortEntries(NamedObj target, DocManager manager) {
        if (!(target instanceof Entity)) {
            return null;
        }
        StringBuffer result = new StringBuffer();
        boolean foundOne = false;
        boolean foundInput = false;
        boolean foundOutput = false;
        boolean foundInputOutput = false;
        boolean foundNeither = false;
        StringBuffer inputPorts = new StringBuffer();
        StringBuffer outputPorts = new StringBuffer();
        StringBuffer inputOutputPorts = new StringBuffer();
        StringBuffer neitherPorts = new StringBuffer();
        Iterator ports = ((Entity) target).portList().iterator();
        while (ports.hasNext()) {
            Port port = (Port) ports.next();
            if (_isHidden(port)) {
                    continue;
            }
            if (port instanceof ParameterPort) {
                // Skip this one.
                continue;
            }
            String portName = "<i>" + port.getDisplayName() + "</i>";
            String doc = manager.getPortDoc(port.getName());
            if (doc == null) {
                doc = "No port description.";
                // See if the next tier has documentation.
                DocManager nextTier = manager.getNextTier();
                if (nextTier != null) {
                    String nextDoc = nextTier.getPortDoc(port.getName());
                    if (nextDoc != null) {
                        doc = nextDoc;
                    }
                }
            }
            if (port instanceof IOPort) {
                if (((IOPort) port).isInput() && !((IOPort) port).isOutput()) {
                    inputPorts.append(_tr);
                    inputPorts.append(_td);
                    inputPorts.append(portName);
                    inputPorts.append(_tde);
                    inputPorts.append(_td);
                    inputPorts.append(doc);
                    inputPorts.append(_tde);
                    inputPorts.append(_tre);
                    foundInput = true;
                    foundOne = true;
                } else if (((IOPort) port).isOutput()
                        && !((IOPort) port).isInput()) {
                    outputPorts.append(_tr);
                    outputPorts.append(_td);
                    outputPorts.append(portName);
                    outputPorts.append(_tde);
                    outputPorts.append(_td);
                    outputPorts.append(doc);
                    outputPorts.append(_tde);
                    outputPorts.append(_tre);
                    foundOutput = true;
                    foundOne = true;
                } else if (((IOPort) port).isOutput()
                        && ((IOPort) port).isInput()) {
                    inputOutputPorts.append(_tr);
                    inputOutputPorts.append(_td);
                    inputOutputPorts.append(portName);
                    inputOutputPorts.append(_tde);
                    inputOutputPorts.append(_td);
                    inputOutputPorts.append(doc);
                    inputOutputPorts.append(_tde);
                    inputOutputPorts.append(_tre);
                    foundInputOutput = true;
                    foundOne = true;
                } else {
                    neitherPorts.append(_tr);
                    neitherPorts.append(_td);
                    neitherPorts.append(portName);
                    neitherPorts.append(_tde);
                    neitherPorts.append(_td);
                    neitherPorts.append(doc);
                    neitherPorts.append(_tde);
                    neitherPorts.append(_tre);
                    foundNeither = true;
                    foundOne = true;
                }
            } else {
                neitherPorts.append(_tr);
                neitherPorts.append(_td);
                neitherPorts.append(portName);
                neitherPorts.append(_tde);
                neitherPorts.append(_td);
                neitherPorts.append(doc);
                neitherPorts.append(_tde);
                neitherPorts.append(_tre);
                foundNeither = true;
                foundOne = true;
            }
        }
        if (foundInput) {
            result.append(_tr);
            result.append(_tdColSpan);
            result.append("<h2>Input Ports</h2>");
            result.append(_tde);
            result.append(_tre);
            result.append(inputPorts);
        }
        if (foundOutput) {
            result.append(_tr);
            result.append(_tdColSpan);
            result.append("<h2>Output Ports</h2>");
            result.append(_tde);
            result.append(_tre);
            result.append(outputPorts);
        }
        if (foundInputOutput) {
            result.append(_tr);
            result.append(_tdColSpan);
            result.append("<h2>Input/Output Ports</h2>");
            result.append(_tde);
            result.append(_tre);
            result.append(inputOutputPorts);
        }
        if (foundNeither) {
            result.append(_tr);
            result.append(_tdColSpan);
            result.append("<h2>Ports (Neither Input nor Output)</h2>");
            result.append(_tde);
            result.append(_tre);
            result.append(neitherPorts);
        }
        if (foundOne) {
            return result.toString();
        } else {
            return null;
        }
    }

    /** Return a string with port-parameter table entries.
     *  @param target The target.
     *  @param manager The manager.
     *  @return Port-parameter table entries, or null if there are no port-parameters.
     */
    private String _getPortParameterEntries(NamedObj target, DocManager manager) {
        StringBuffer parameters = new StringBuffer();
        parameters.append(_tr);
        parameters.append(_tdColSpan);
        parameters.append("<h2>Port-Parameters</h2>");
        parameters.append(_tde);
        parameters.append(_tre);
        boolean foundOne = false;
        Iterator attributes = target.attributeList(PortParameter.class)
                .iterator();
        while (attributes.hasNext()) {
            Settable parameter = (Settable) attributes.next();
            if (_isHidden((NamedObj)parameter)) {
                    continue;
            }
            String doc = manager.getPropertyDoc(parameter.getName());
            if (doc == null) {
                doc = "No description.";
                // See if the next tier has documentation.
                DocManager nextTier = manager.getNextTier();
                if (nextTier != null) {
                    String nextDoc = nextTier.getPropertyDoc(parameter
                            .getName());
                    if (nextDoc != null) {
                        doc = nextDoc;
                    }
                }
            }
            if (parameter.getVisibility() == Settable.FULL) {
                parameters.append(_tr);
                parameters.append(_td);
                parameters.append("<i>" + parameter.getDisplayName() + "</i>");
                parameters.append(_tde);
                parameters.append(_td);
                parameters.append(doc);
                parameters.append(_tde);
                parameters.append(_tre);
                foundOne = true;
            }
        }
        if (foundOne) {
            return parameters.toString();
        } else {
            return null;
        }
    }

    /** Append to the specified buffer any locally defined base classes
     *  that are needed to define the specified target.
     *  @param target The target whose parent may need to be included.
     *  @param buffer The buffer to append the definition to.
     */
    private void _includeClassDefinitions(NamedObj target, StringBuffer buffer) {
        if (target instanceof Instantiable) {
            NamedObj parent = (NamedObj) ((Instantiable) target).getParent();
            if (parent != null && target.toplevel().deepContains(parent)) {
                // Parent is locally defined. Include its definition.
                // First recursively take care of the parent.
                if (parent instanceof Instantiable) {
                    NamedObj parentsParent = (NamedObj) ((Instantiable) parent)
                            .getParent();
                    if (parentsParent != null
                            && parent.toplevel().deepContains(parentsParent)) {
                        _includeClassDefinitions(parent, buffer);
                    }
                }
                buffer.append(parent.exportMoML());
                // Add an attribute to hide it.
                buffer.append("<");
                buffer.append(parent.getElementName());
                buffer.append(" name=\"");
                buffer.append(parent.getName());
                buffer.append("\"><property name=\"_hide\" class=\"ptolemy.data.expr.ExpertParameter\" value=\"true\"/></");
                buffer.append(parent.getElementName());
                buffer.append(">");
            }
        }
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
    private void _init(final NamedObj target, Configuration configuration,
            String className, URL url) throws ClassNotFoundException {
        _configuration = configuration;
        _target = target;

        // Override the default value of the help file as defined in
        // TableauFrame.  This is the name of the default file to open
        // when Help is invoked.  This file should be relative to the
        // home installation directory.
        helpFile = "ptolemy/vergil/actor/docViewerHelp.htm";

        // We handle the applicationName specially so that we open
        // only the docs for the app we are running.
        try {
            StringAttribute applicationNameAttribute = (StringAttribute) configuration
                    .getAttribute("_applicationName", StringAttribute.class);

            if (applicationNameAttribute != null) {
                _applicationName = applicationNameAttribute.getExpression();
            }
        } catch (Throwable throwable) {
            // Ignore and use the default applicationName: "",
            // which means we look in doc.codeDoc.
        }

        // Start by creating a doc manager.
        final DocManager manager;
        if (target != null) {
            manager = new DocManager(_configuration, target);
        } else if (className != null) {
            manager = new DocManager(_configuration, Class.forName(className));
        } else if (url != null) {
            manager = new DocManager(_configuration, url);
        } else {
            throw new InternalErrorException(
                    "Need to specify one of target, className, or url!");
        }
        className = manager.getClassName();
        final String rootName;
        int lastPeriod = className.lastIndexOf(".");
        if (lastPeriod >= 0) {
            rootName = className.substring(lastPeriod + 1);
        } else {
            rootName = className;
        }
        // Need to set the base for relative URL references.
        // If the url argument is given, then use that.
        // Otherwise, set it to the directory in which the
        // Javadoc file is normally be found.
        if (url != null) {
            setBase(url);
        } else {
            String javaDocDirectory = "doc.codeDoc" + _applicationName + "."
                    + className;
            int lastDot = javaDocDirectory.lastIndexOf(".");
            javaDocDirectory = javaDocDirectory.substring(0, lastDot);
            URL base = getClass().getClassLoader().getResource(
                    javaDocDirectory.replace('.', '/') + "/");
            setBase(base);
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
        // The instance has its own documentation.
        if (target instanceof InstantiableNamedObj
                && ((InstantiableNamedObj) target).isClassDefinition()) {
            // FIXME: getFullName() isn't right here.  How to get the full class name?
            title = target.getName() + "&nbsp; &nbsp; &nbsp; ("
                    + target.getFullName() + ")";
        } else {
            if (manager.isInstanceDoc()) {
                title = target.getName() + "&nbsp; &nbsp; &nbsp; (Instance of "
                        + className + ")";
            } else {
                title = rootName + "&nbsp; &nbsp; &nbsp; (" + className + ")";
            }
        }
        JEditorPane titlePane = new JEditorPane();
        titlePane.setContentType("text/html");
        titlePane.setEditable(false);
        titlePane.setText(_HTML_HEADER + "<H2>&nbsp; " + title + "</H2>"
                + _HTML_TAIL);
        // Set the view to the start of the text.
        titlePane.getCaret().setDot(0);
        Dimension titleSize = new Dimension(_DESCRIPTION_WIDTH
                + _ICON_WINDOW_WIDTH + _SPACING, 40);
        titlePane.setPreferredSize(titleSize);
        titlePane.setSize(titleSize);
        titlePane.setBorder(BorderFactory
                .createEtchedBorder(EtchedBorder.RAISED));
        titlePanel.add(Box.createRigidArea(horizontalSpace));
        titlePanel.add(titlePane);
        titlePanel.add(Box.createRigidArea(horizontalSpace));

        // Panel for icon and description.
        JPanel descriptionPanel = new JPanel();
        descriptionPanel.setLayout(new BoxLayout(descriptionPanel,
                BoxLayout.X_AXIS));
        contentPane.add(Box.createRigidArea(verticalSpace));
        contentPane.add(descriptionPanel);
        descriptionPanel.add(Box.createRigidArea(horizontalSpace));
        // Construct a blank composite actor into which to put
        // an instance of the actor.
        _iconContainer = new CompositeEntity();
        final ActorEditorGraphController controller = new ActorEditorGraphController();
        controller.setConfiguration(getConfiguration());
        // Create a modified graph model with alternative error reporting.
        ActorGraphModel graphModel = new ActorGraphModel(_iconContainer) {
            /** Override the base class to give a useful message.
             *  @param change The change that has failed.
             *  @param exception The exception that was thrown.
             */
            @Override
            public void changeFailed(ChangeRequest change, Exception exception) {
                if (_graphPane == null) {
                    super.changeFailed(change, exception);
                    return;
                }
                LabelFigure newFigure = new LabelFigure("No icon available",
                        _font);
                _graphPane.getForegroundLayer().add(newFigure);
                CanvasUtilities.translateTo(newFigure, 100.0, 100.0);
                controller.dispatch(new GraphViewEvent(this,
                        GraphViewEvent.NODE_DRAWN, newFigure));
            }
        };
        _graphPane = new GraphPane(controller, graphModel);
        _jgraph = new JGraph(_graphPane);
        _jgraph.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        // The icon window is fixed size.
        _jgraph.setMinimumSize(new Dimension(_ICON_WINDOW_WIDTH,
                _ICON_WINDOW_HEIGHT));
        _jgraph.setMaximumSize(new Dimension(_ICON_WINDOW_WIDTH,
                _ICON_WINDOW_HEIGHT));
        _jgraph.setPreferredSize(new Dimension(_ICON_WINDOW_WIDTH,
                _ICON_WINDOW_HEIGHT));
        _jgraph.setSize(_ICON_WINDOW_WIDTH, _ICON_WINDOW_HEIGHT);
        _jgraph.setBackground(BasicGraphFrame.BACKGROUND_COLOR);
        descriptionPanel.add(_jgraph);
        descriptionPanel.add(Box.createRigidArea(horizontalSpace));
        // Create a pane in which to display the description.
        final JEditorPane descriptionPane = new JEditorPane();
        descriptionPane.addHyperlinkListener(this);
        descriptionPane.setContentType("text/html");
        descriptionPane.setEditable(false);
        JScrollPane scroller = new JScrollPane(descriptionPane);
        scroller.setPreferredSize(new Dimension(_DESCRIPTION_WIDTH,
                _ICON_WINDOW_HEIGHT));
        scroller.setBorder(BorderFactory
                .createEtchedBorder(EtchedBorder.RAISED));
        descriptionPanel.add(scroller);
        descriptionPanel.add(Box.createRigidArea(horizontalSpace));

        // Add the main content pane now.
        JPanel middle = new JPanel();
        middle.setLayout(new BoxLayout(middle, BoxLayout.X_AXIS));
        contentPane.add(Box.createRigidArea(verticalSpace));
        contentPane.add(middle);
        _scroller = new JScrollPane(pane);
        // Default, which can be overridden by calling setSize().
        _scroller.setPreferredSize(new Dimension(_MAIN_WINDOW_WIDTH,
                _MAIN_WINDOW_HEIGHT));
        _scroller.setBorder(BorderFactory
                .createEtchedBorder(EtchedBorder.RAISED));
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
        Dimension authorSize = new Dimension(_AUTHOR_WINDOW_WIDTH,
                _BOTTOM_HEIGHT);
        authorScroller.setPreferredSize(authorSize);
        authorScroller.setSize(authorSize);
        authorScroller.setBorder(BorderFactory
                .createEtchedBorder(EtchedBorder.RAISED));
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
        seeAlsoScroller.setBorder(BorderFactory
                .createEtchedBorder(EtchedBorder.RAISED));
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
        // Set the view to the start of the text.
        descriptionPane.getCaret().setDot(0);

        // Create an instance to display.
        // Note that this will display something that looks just
        // like the object we are asking about, including any customizations
        // that are applicable only to this instance.
        // If the target is given, then export MoML from it to use.
        // Otherwise, use the class name.
        String moml = null;
        if (target != null) {
            StringBuffer buffer = new StringBuffer("<group>");
            // If the target is an instance of a locally defined class,
            // then we need to include the class as well.
            _includeClassDefinitions(target, buffer);

            // Need to use a
            buffer.append(target.exportMoMLPlain());
            // Have to override the hide attribute in the derived class.
            buffer.append("<");
            buffer.append(target.getElementName());
            buffer.append(" name=\"");
            buffer.append(target.getName());
            buffer.append("\"><property name=\"_hide\" class=\"ptolemy.data.expr.ExpertParameter\" value=\"false\"/></");
            buffer.append(target.getElementName());
            buffer.append(">");

            buffer.append("</group>");
            moml = buffer.toString();
        } else if (!manager.hadException()) {
            // NOTE: This will not work if a URL was specified and the parse failed.
            // No target is given. Try to create an instance from the class name.
            // This is a bit tricky, as we have to know what subclass of NamedObj
            // it is, and whether it has an appropriate constructor.
            if (manager.isTargetInstantiableAttribute()) {
                // To make it visible, need to include a location attribute.
                moml = "<property name=\""
                        + rootName
                        + "\" class=\""
                        + className
                        + "\">"
                        + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{50, 50}\"/>"
                        + "</property>";
            } else if (manager.isTargetInstantiableEntity()) {
                moml = "<entity name=\"" + rootName + "\" class=\"" + className
                        + "\"/>";
            } else if (manager.isTargetInstantiablePort()) {
                // NOTE: The port has to be an input or an output or it can't be rendered.
                // Since we aren't dealing with a specific instance, we make it an input.
                moml = "<port name=\"" + rootName + "\" class=\"" + className
                        + "\">" + "<property name=\"input\"/></port>";
            }
        }
        if (moml != null) {
            MoMLChangeRequest request = new MoMLChangeRequest(this,
                    _iconContainer, moml) {
                @Override
                protected void _execute() throws Exception {
                    super._execute();
                    NamedObj sample = null;
                    String name = rootName;
                    if (target != null) {
                        name = target.getName();
                    }
                    if (manager.isTargetInstantiableAttribute()) {
                        sample = _iconContainer.getAttribute(name);
                    } else if (manager.isTargetInstantiableEntity()) {
                        sample = _iconContainer.getEntity(name);
                    } else if (manager.isTargetInstantiablePort()) {
                        sample = _iconContainer.getPort(name);
                    }
                    if (sample != null) {
                        _populatePortsAndParametersTable(sample, manager);
                        _adjustIconDisplay(sample, _iconContainer, _graphPane,
                                _jgraph);
                    }
                }
            };
            _iconContainer.requestChange(request);
        }

        if (target != null) {
            _populatePortsAndParametersTable(target, manager);
        }

        // Populate the author window.
        StringBuffer info = new StringBuffer();
        info.append(_HTML_HEADER);
        // Author(s)
        info.append(_tableOpening);
        info.append(_tr);
        info.append(_td20);
        info.append("<i>Author:</i> ");
        info.append(_tde);
        info.append(_td);
        info.append(manager.getAuthor());
        if (manager.isInstanceDoc()) {
            DocManager nextTier = manager.getNextTier();
            if (nextTier != null) {
                String nextTierAuthor = nextTier.getAuthor();
                if (!nextTierAuthor.equals("No author given")) {
                    info.append(" (<i>Class author:</i> ");
                    info.append(nextTierAuthor);
                }
            }
        }
        info.append(_tde);
        info.append(_tre);
        // Version
        String version = manager.getVersion();
        if (version != null) {
            info.append(_tr);
            info.append(_td20);
            info.append("<i>Version:</i> ");
            info.append(_tde);
            info.append(_td);
            info.append(version);
            info.append(_tde);
            info.append(_tre);
        }
        // Since
        String since = manager.getSince();
        if (since != null) {
            info.append(_tr);
            info.append(_td20);
            info.append("<i>Since:</i> ");
            info.append(_tde);
            info.append(_td);
            info.append(since);
            info.append(_tde);
            info.append(_tre);
        }
        // Rating
        String rating = manager.getAcceptedRating();
        if (rating != null) {
            info.append(_tr);
            info.append(_td20);
            info.append("<i>Rating:</i> ");
            info.append(_tde);
            info.append(_colorizeRating(rating));
            info.append(_tre);
        }
        // End of table
        info.append(_tableClosing);
        info.append(_HTML_TAIL);
        authorPane.setText(info.toString());
        // Set the view to the start of the text.
        authorPane.getCaret().setDot(0);

        // Populate the "See Also" window.
        seeAlsoPane.setText(_HTML_HEADER + manager.getSeeAlso() + _HTML_TAIL);
        // Set the view to the start of the text.
        seeAlsoPane.getCaret().setDot(0);
    }

    /** Return true if the specified object is intended to be hidden.
     *  @param object The object.
     *  @param name The property name.
     *  @return True if the property is set.
     */
    private boolean _isHidden(NamedObj object) {
        Attribute attribute = object.getAttribute("_hide");

        if (attribute == null) {
            return false;
        }

        if (attribute instanceof Parameter) {
            try {
                Token token = ((Parameter) attribute).getToken();

                if (token instanceof BooleanToken) {
                    if (!((BooleanToken) token).booleanValue()) {
                        return false;
                    }
                }
            } catch (IllegalActionException e) {
                // Ignore, using default of true.
            }
        }

        return true;
    }

    /** Populate the window displaying ports and parameters.
     *  @param target The target object whose ports and parameters
     *  will be described.
     *  @param manager The doc manager.
     */
    private void _populatePortsAndParametersTable(NamedObj target,
            DocManager manager) {
        // Create tables to contain the information about parameters and ports.
        // Start with parameters.
        boolean foundOne = false;
        StringBuffer table = new StringBuffer();
        String parameterTableEntries = _getParameterEntries(target, manager);
        if (parameterTableEntries != null) {
            foundOne = true;
            table.append(parameterTableEntries);
        }
        // Next do the port-parameters.
        String portParameterTableEntries = _getPortParameterEntries(target,
                manager);
        if (portParameterTableEntries != null) {
            foundOne = true;
            table.append(portParameterTableEntries);
        }
        // Next do the ports.
        String portTableEntries = _getPortEntries(target, manager);
        if (portTableEntries != null) {
            foundOne = true;
            table.append(portTableEntries);
        }
        // Finally, insert all.
        StringBuffer info = new StringBuffer();
        info.append(_HTML_HEADER);
        if (foundOne) {
            info.append(_tableOpening);
            info.append(table);
            info.append(_tableClosing);
        } else {
            info.append("No ports or parameters.");
        }
        info.append(_HTML_TAIL);

        setText(info.toString());
        // Set the view to the start of the text.
        pane.getCaret().setDot(0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private inner class               ////

    /** Listener for build menu commands. */
    private class BuildMenuListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                Effigy effigy = (Effigy) getTableau().getContainer();
                Tableau tableau = new DocBuilderTableau(effigy,
                        "DocBuilderTableau");
                tableau.show();
            } catch (Throwable throwable) {
                MessageHandler.error("Cannot create build", throwable);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The name of the application, usually from the _applicationName
     *  StringAttribute in configuration.xml.
     *  If the value is the empty string, then use the default
     *  documentation in doc/codeDoc.
     */
    private String _applicationName = "";

    /** Author window width. */
    private static int _AUTHOR_WINDOW_WIDTH = 350;

    /** The configuration specified in the constructor. */
    private Configuration _configuration;

    /** Bottom window height. */
    private static int _BOTTOM_HEIGHT = 150;

    /** Width of the description pane. */
    private static int _DESCRIPTION_WIDTH = 500;

    /** The font to use for No icon available message. */
    private Font _font = new Font("SansSerif", Font.PLAIN, 14);

    /** The graph pane. */
    private GraphPane _graphPane;

    /** HTML Header information. */
    private static String _HTML_HEADER = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\""
            + "\"http://www.w3.org/TR/html4/loose.dtd\">"
            + "\n<html>\n<head>\n"
            + "<title>Ptolemy II Documentation</title>"
            + "<STYLE TYPE=\"text/css\">\n"
            + "<!--\n"
            + "h1, h2, h3, td, tr, body, p {font-family: Arial, Helvetica, sans-serif;}\n"
            + "-->\n" + "</STYLE>" + "</head><body>";

    private static String _HTML_TAIL = "</body></html>";

    /** The composite entity containing the icon. */
    private CompositeEntity _iconContainer;

    /** Icon window width. */
    private static int _ICON_WINDOW_HEIGHT = 200;

    /** Icon window width. */
    private static int _ICON_WINDOW_WIDTH = 200;

    /** The jgraph. */
    private JGraph _jgraph;

    /** Main window height. */
    private static int _MAIN_WINDOW_HEIGHT = 250;

    /** Main window width. */
    private static int _MAIN_WINDOW_WIDTH = 700;

    /** Padding in icon window. */
    private static int _PADDING = 10;

    /** Width of the see also pane. */
    private static int _SEE_ALSO_WIDTH = 350;

    /** Spacing between subwindows. */
    private static int _SPACING = 5;

    /** The target given in the constructor, if any. */
    private NamedObj _target;

    private static String _tr = "<tr valign=top>\n";

    private static String _tre = "</tr>\n";

    private static String _td = "<td>";

    private static String _td20 = "<td width=20%>";

    private static String _tdColSpan = "<td colspan=2>";

    private static String _tde = "</td>";

    private static String _tableOpening = "<table cellspacing=2 cellpadding=2>\n";

    private static String _tableClosing = "</table>";
}
