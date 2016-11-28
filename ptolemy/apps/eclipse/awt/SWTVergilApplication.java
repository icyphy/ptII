//Rough prototype of Vergil in SWT
// From http://dev.eclipse.org/viewcvs/index.cgi/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet135.java?view=co

/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * example snippet: embed Swing/AWT in SWT
 *
 * For a list of all SWT example snippets see
 * http://www.eclipse.org/swt/snippets/
 *
 * @since 3.0
 */

package ptolemy.apps.eclipse.awt;

import java.awt.Component;
import java.io.File;
import java.net.URL;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.UIManager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.ConfigurationApplication;
import ptolemy.actor.gui.PtolemyPreferences;
import ptolemy.gui.GraphicalMessageHandler;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.util.FileUtilities;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.VergilErrorHandler;
import ptolemy.vergil.actor.ActorEditorGraphController;
import ptolemy.vergil.actor.ActorGraphModel;
import diva.canvas.DamageRegion;
import diva.canvas.event.LayerAdapter;
import diva.canvas.event.LayerEvent;
import diva.graph.GraphPane;
import diva.graph.JGraph;

/**
 * Display a Vergil model inside an Eclipse SWT window.
 * <p>This class is a very rough proof of concept.
 * <p>Not much works here.
 * @author cxh
 * @version $Id$
 * @since Ptolemy II 7.1
 */
public class SWTVergilApplication {

    /** Display a model inside an Eclipse SWT window.
     *
     * @param args  Currently ignored
     * @exception Exception If there is a problem opening the configuration or model.
     */
    public SWTVergilApplication(String[] args) throws Exception {
        // FIXME: This constructor is way too long and does too much.

        // FIXME: Code duplicated from MoMLApplication.  Perhaps refactoring
        // would help here?

        // Create register an error handler with the parser so that
        // MoML errors are tolerated more than the default.
        MoMLParser.setErrorHandler(new VergilErrorHandler());

        // The Java look & feel is pretty lame, so we use the native
        // look and feel of the platform we are running on.
        // NOTE: This creates the only dependence on Swing in this
        // class.  Should this be left to derived classes?
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Ignore exceptions, which only result in the wrong look and feel.
        }

        // Create a parser to use.
        _parser = new MoMLParser();

        // We set the list of MoMLFilters to handle Backward Compatibility.
        MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());

        // 2/03: Moved the setMessageHandler() to before parseArgs() so
        // that if we get an error in parseArgs() we will get a graphical
        // stack trace.   Such an error could be caused by specifying a model
        // as a command line argument and the model has an invalid parameter.
        MessageHandler.setMessageHandler(new GraphicalMessageHandler());

        // Even if the user is set up for foreign locale, use the US locale.
        // This is because certain parts of Ptolemy (like the expression
        // language) are not localized.
        // FIXME: This is a workaround for the locale problem, not a fix.
        // FIXME: In March, 2001, Johan Ecker writes
        // Ptolemy gave tons of exception when started on my laptop
        // which has Swedish settings as default. The Swedish standard
        // for floating points are "2,3", i.e. using a comma as
        // delimiter. However, I think most Swedes are adaptable and
        // do not mind using a dot instead since this more or less has
        // become the world standard, at least in engineering. The
        // problem is that I needed to change my global settings to
        // start Ptolemy and this is quite annoying. I guess that the
        // expression parser should just ignore the delimiter settings
        // on the local computer and always use dot, otherwise Ptolemy
        // will crash using its own init files.
        try {
            java.util.Locale.setDefault(java.util.Locale.US);
        } catch (java.security.AccessControlException accessControl) {
            // FIXME: If the application is run under Web Start, then this
            // exception will be thrown.
        }

        // End of code duplication from MoMLApplication

        // Start of code from Snippet135
        final Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setText("SWT and Swing/AWT Example with Vergil!");

        Listener exitListener = new Listener() {
            public void handleEvent(Event e) {
                MessageBox dialog = new MessageBox(shell, SWT.OK | SWT.CANCEL
                        | SWT.ICON_QUESTION);
                dialog.setText("Question");
                dialog.setMessage("Exit?");
                if (e.type == SWT.Close) {
                    e.doit = false;
                }
                if (dialog.open() != SWT.OK) {
                    return;
                }
                shell.dispose();
            }
        };
        Listener aboutListener = new Listener() {
            public void handleEvent(Event e) {
                final Shell s = new Shell(shell, SWT.DIALOG_TRIM
                        | SWT.APPLICATION_MODAL);
                s.setText("About");
                GridLayout layout = new GridLayout(1, false);
                layout.verticalSpacing = 20;
                layout.marginHeight = layout.marginWidth = 10;
                s.setLayout(layout);
                Label label = new Label(s, SWT.NONE);
                label.setText("SWT and AWT Example.");
                Button button = new Button(s, SWT.PUSH);
                button.setText("OK");
                GridData data = new GridData();
                data.horizontalAlignment = GridData.CENTER;
                button.setLayoutData(data);
                button.addListener(SWT.Selection, new Listener() {
                    public void handleEvent(Event event) {
                        s.dispose();
                    }
                });
                s.pack();
                Rectangle parentBounds = shell.getBounds();
                Rectangle bounds = s.getBounds();
                int x = parentBounds.x + (parentBounds.width - bounds.width)
                        / 2;
                int y = parentBounds.y + (parentBounds.height - bounds.height)
                        / 2;
                s.setLocation(x, y);
                s.open();
                while (!s.isDisposed()) {
                    if (!display.readAndDispatch()) {
                        display.sleep();
                    }
                }
            }
        };
        // FIXME: The run choice does not work because the
        // plotter wants a top level effigy.
        Listener runListener = new Listener() {
            public void handleEvent(Event e) {
                try {
                    TypedCompositeActor toplevel = ((TypedCompositeActor) _toplevel);
                    Manager manager = new Manager(_toplevel.workspace(),
                            "myManager");

                    toplevel.setManager(manager);
                    manager.execute();
                } catch (Throwable throwable) {
                    MessageHandler.error("Failed to run model", throwable);
                }

            }
        };

        // Set up the menus
        shell.addListener(SWT.Close, exitListener);
        Menu mb = new Menu(shell, SWT.BAR);
        MenuItem fileItem = new MenuItem(mb, SWT.CASCADE);
        fileItem.setText("&File");
        Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
        fileItem.setMenu(fileMenu);
        MenuItem exitItem = new MenuItem(fileMenu, SWT.PUSH);
        exitItem.setText("&Exit\tCtrl+X");
        exitItem.setAccelerator(SWT.CONTROL + 'X');
        exitItem.addListener(SWT.Selection, exitListener);
        MenuItem aboutItem = new MenuItem(fileMenu, SWT.PUSH);
        aboutItem.setText("&About\tCtrl+A");
        aboutItem.setAccelerator(SWT.CONTROL + 'A');
        aboutItem.addListener(SWT.Selection, aboutListener);

        MenuItem runItem = new MenuItem(fileMenu, SWT.PUSH);
        runItem.setText("&Run\tCtrl+R");
        runItem.setAccelerator(SWT.CONTROL + 'R');
        runItem.addListener(SWT.Selection, runListener);

        shell.setMenuBar(mb);

        RGB color = shell.getBackground().getRGB();
        Label separator1 = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
        Label locationLb = new Label(shell, SWT.NONE);
        locationLb.setText("Location:");
        Composite locationComp = new Composite(shell, SWT.EMBEDDED);
        ToolBar toolBar = new ToolBar(shell, SWT.FLAT);

        // The Run toolItem
        ToolItem runToolItem = new ToolItem(toolBar, SWT.PUSH);
        runToolItem.setText("&Run");
        runToolItem.addListener(SWT.Selection, runListener);

        // The Exit toolItem
        ToolItem exitToolItem = new ToolItem(toolBar, SWT.PUSH);
        exitToolItem.setText("&Exit");
        exitToolItem.addListener(SWT.Selection, exitListener);

        // The About toolItem
        ToolItem aboutToolItem = new ToolItem(toolBar, SWT.PUSH);
        aboutToolItem.setText("&About");
        aboutToolItem.addListener(SWT.Selection, aboutListener);

        // FIXME: The left hand should have our navigator
        Label separator2 = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
        final Composite comp = new Composite(shell, SWT.NONE);
        final Tree fileTree = new Tree(comp, SWT.SINGLE | SWT.BORDER);
        Sash sash = new Sash(comp, SWT.VERTICAL);
        Composite tableComp = new Composite(comp, SWT.EMBEDDED);
        Label separator3 = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
        Composite statusComp = new Composite(shell, SWT.EMBEDDED);

        java.awt.Frame locationFrame = SWT_AWT.new_Frame(locationComp);
        final java.awt.TextField locationText = new java.awt.TextField();
        locationFrame.add(locationText);

        java.awt.Frame fileTableFrame = SWT_AWT.new_Frame(tableComp);
        java.awt.Panel panel = new java.awt.Panel(new java.awt.BorderLayout());
        fileTableFrame.add(panel);

        // Open the model, add the graph viewer
        _toplevel = _openModel("$CLASSPATH/ptolemy/domains/sdf/demo/Butterfly/Butterfly.xml");
        final JComponent rightComponent = _createRightComponent(_toplevel);
        panel.add(rightComponent);

        java.awt.Frame statusFrame = SWT_AWT.new_Frame(statusComp);
        statusFrame.setBackground(new java.awt.Color(color.red, color.green,
                color.blue));
        final java.awt.Label statusLabel = new java.awt.Label();
        statusFrame.add(statusLabel);
        statusLabel.setText("This is a Ptolemy model");

        // Handle dragging the sash
        sash.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                if (e.detail == SWT.DRAG) {
                    return;
                }
                GridData data = (GridData) fileTree.getLayoutData();
                Rectangle trim = fileTree.computeTrim(0, 0, 0, 0);
                data.widthHint = e.x - trim.width;
                comp.layout();
            }
        });

        // FIXME: The file browser code is from the Snippet135 example.
        // The file browser code should be removed.
        File[] roots = File.listRoots();
        for (int i = 0; i < roots.length; i++) {
            File file = roots[i];
            TreeItem treeItem = new TreeItem(fileTree, SWT.NONE);
            treeItem.setText(file.getAbsolutePath());
            treeItem.setData(file);
            new TreeItem(treeItem, SWT.NONE);
        }
        fileTree.addListener(SWT.Expand, new Listener() {
            public void handleEvent(Event e) {
                TreeItem item = (TreeItem) e.item;
                if (item == null) {
                    return;
                }
                if (item.getItemCount() == 1) {
                    TreeItem firstItem = item.getItems()[0];
                    if (firstItem.getData() != null) {
                        return;
                    }
                    firstItem.dispose();
                } else {
                    return;
                }
                File root = (File) item.getData();
                File[] files = root.listFiles();
                if (files == null) {
                    return;
                }
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    if (file.isDirectory()) {
                        TreeItem treeItem = new TreeItem(item, SWT.NONE);
                        treeItem.setText(file.getName());
                        treeItem.setData(file);
                        new TreeItem(treeItem, SWT.NONE);
                    }
                }
            }
        });

        GridLayout layout = new GridLayout(4, false);
        layout.marginWidth = layout.marginHeight = 0;
        layout.horizontalSpacing = layout.verticalSpacing = 1;
        shell.setLayout(layout);
        GridData data;
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 4;
        separator1.setLayoutData(data);
        data = new GridData();
        data.horizontalSpan = 1;
        data.horizontalIndent = 10;
        locationLb.setLayoutData(data);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        data.heightHint = locationText.getPreferredSize().height;
        locationComp.setLayoutData(data);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 1;
        toolBar.setLayoutData(data);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 4;
        separator2.setLayoutData(data);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 4;
        comp.setLayoutData(data);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 4;
        separator3.setLayoutData(data);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 4;
        data.heightHint = statusLabel.getPreferredSize().height;
        statusComp.setLayoutData(data);

        layout = new GridLayout(3, false);
        layout.marginWidth = layout.marginHeight = 0;
        layout.horizontalSpacing = layout.verticalSpacing = 1;
        comp.setLayout(layout);
        data = new GridData(GridData.FILL_VERTICAL);
        data.widthHint = 200;
        fileTree.setLayoutData(data);
        data = new GridData(GridData.FILL_VERTICAL);
        sash.setLayoutData(data);
        data = new GridData(GridData.FILL_BOTH);
        tableComp.setLayoutData(data);

        shell.open();
        while (!shell.isDisposed()) {
            try {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            } catch (Throwable throwable) {
                MessageHandler.error("Problem with Display?", throwable);
            }
        }
        display.dispose();
    }

    /** Display a model inside an Eclipse SWT window.
     *
     * @param args  Currently ignored
     * @exception Exception If there is a problem opening the configuration or model.
     */
    public static void main(final String[] args) {
        // Note that because we are using SWT, we don't run this
        // in the SwingEvent thread like we do in VergilApplication.
        // If we do run this in the SwingEvent thread, then the
        // actor graph pane will not render.
        try {
            new SWTVergilApplication(args);
        } catch (Throwable throwable2) {
            // We are not likely to get here, but just to be safe
            // we try to print the error message and display it in a
            // graphical widget.
            _errorAndExit("Command failed", args, throwable2);
        }
    }

    /** Set the JGraph instance that this view uses to represent the
     *  ptolemy model.
     *
     *  @param jgraph The JGraph.
     *  @see ptolemy.vergil.basic.BasicGraphFrame#setJGraph(JGraph)
     */
    public void setJGraph(JGraph jgraph) {
        _jgraph = jgraph;
    }

    /**
     * Create a new graph pane. Note that this method is called in constructor
     * of the base class, so it must be careful to not reference local variables
     * that may not have yet been created.
     *
     * @param entity
     *        The object to be displayed in the pane.
     * @return The pane that is created.
     * @see ptolemy.vergil.actor.ActorGraphFrame#_createGraphPane(NamedObj)
     * @exception Exception If the configuration cannot be read.
     */
    protected GraphPane _createGraphPane(NamedObj entity) throws Exception {
        _controller = new ActorEditorGraphController();
        _readConfiguration();
        _controller.setConfiguration(_configuration);
        //_controller.setFrame();

        // The cast is safe because the constructor only accepts
        // CompositeEntity.
        final ActorGraphModel graphModel = new ActorGraphModel(entity);
        return new ActorGraphPane(_controller, graphModel, entity);
    }

    /** Create the component that goes to the right of the library.
     *  @param entity The entity to display in the component.
     *  @return The component that goes to the right of the library.
     *  @see ptolemy.vergil.basic.BasicGraphFrame#_createRightComponent(NamedObj)
     *  @exception Exception If there is a problem creating the graph pane.
     */
    protected JComponent _createRightComponent(NamedObj entity)
            throws Exception {
        GraphPane pane = _createGraphPane(entity);
        pane.getForegroundLayer().setPickHalo(2);
        pane.getForegroundEventLayer().setConsuming(false);
        pane.getForegroundEventLayer().setEnabled(true);
        pane.getForegroundEventLayer().addLayerListener(new LayerAdapter() {
            /** Invoked when the mouse is pressed on a layer
             * or figure.
             */
            public void mousePressed(LayerEvent event) {
                Component component = event.getComponent();

                if (!component.hasFocus()) {
                    component.requestFocus();
                }
            }
        });

        setJGraph(new JGraph(pane));

        //_dropTarget = new EditorDropTarget(_jgraph);
        return _jgraph;
    }

    /** Open a Ptolemy model.
     * @param model The URL that refers to the model.
     * @return A NamedObj containing the Ptolemy model.
     * @exception Exception If the model cannot be found or cannot be parsed.
     */
    protected NamedObj _openModel(String model) throws Exception {
        URL modelURL = FileUtilities.nameToURL(model, null, null);
        return _parser.parse(null, modelURL);
    }

    /** Read the configuration.
     *  @exception Exception If the configuration file cannot be opened
     *  or there is a problem parsing the configuration file.
     */
    protected void _readConfiguration() throws Exception {
        URL configurationURL = FileUtilities
                .nameToURL("$CLASSPATH/ptolemy/configs/full/configuration.xml",
                        null, null);
        System.out.println("ConfigurationURL: " + configurationURL);
        _configuration = ConfigurationApplication
                .readConfiguration(configurationURL);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected variables                   ////

    /** The graph controller. This is created in _createGraphPane(). */
    protected ActorEditorGraphController _controller;

    /** The configuration that describes how the editors are set up etc. */
    protected static Configuration _configuration;

    protected JGraph _jgraph;

    /** The parser used to construct the configuration. */
    protected MoMLParser _parser;

    /** The Ptolemy model.*/
    protected NamedObj _toplevel;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Print out an error message and stack trace on stderr and then
    // display a dialog box.  This method is used as a fail safe
    // in case there are problems with the configuration
    // We use a Throwable here instead of an Exception because
    // we might get an Error or and Exception. For example, if we
    // are using JNI, then we might get a java.lang.UnsatisfiedLinkError,
    // which is an Error, not and Exception.
    private static void _errorAndExit(String message, String[] args,
            Throwable throwable) {
        // FIXME: Duplicated code from VergilApplication.
        StringBuffer argsBuffer = new StringBuffer("Command failed");

        if (args.length > 0) {
            argsBuffer.append("\nArguments: " + args[0]);

            for (int i = 1; i < args.length; i++) {
                argsBuffer.append(" " + args[i]);
            }

            argsBuffer.append("\n");
        }

        // First, print out the stack trace so that
        // if the next step fails the user has
        // a chance of seeing the message.
        System.out.println(argsBuffer.toString());
        throwable.printStackTrace();

        // Display the error message in a stack trace
        // If there are problems with the configuration,
        // then there is a chance that we have not
        // registered the GraphicalMessageHandler yet
        // so we do so now so that we are sure
        // the user can see the message.
        // One way to test this is to run vergil -configuration foo
        MessageHandler.setMessageHandler(new GraphicalMessageHandler());

        MessageHandler.error(argsBuffer.toString(), throwable);

        System.exit(0);
    }

    /**
     * Subclass that updates the background color on each repaint if there is a
     * preferences attribute.
     * @see ptolemy.vergil.actor.ActorGraphFrame#ActorGraphPane(ActorGraphController, ActorGraphModel, NamedObj)
     */
    private static class ActorGraphPane extends GraphPane {
        public ActorGraphPane(ActorEditorGraphController controller,
                ActorGraphModel model, NamedObj entity) {
            super(controller, model);
            _entity = entity;
        }

        public void repaint() {
            _setBackground();
            super.repaint();
        }

        public void repaint(DamageRegion damage) {
            _setBackground();
            super.repaint(damage);
        }

        private void _setBackground() {
            if (_entity != null) {
                List list = _entity.attributeList(PtolemyPreferences.class);
                if (list.size() > 0) {
                    // Use the last preferences.
                    PtolemyPreferences preferences = (PtolemyPreferences) list
                            .get(list.size() - 1);
                    getCanvas().setBackground(
                            preferences.backgroundColor.asColor());
                }
            }
        }

        private NamedObj _entity;
    }
}
