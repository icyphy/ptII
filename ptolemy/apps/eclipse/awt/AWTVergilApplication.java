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
import java.util.Date;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.table.AbstractTableModel;

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
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;
import ptolemy.util.FileUtilities;
import ptolemy.vergil.actor.ActorEditorGraphController;
import ptolemy.vergil.actor.ActorGraphModel;
import diva.canvas.DamageRegion;
import diva.canvas.event.LayerAdapter;
import diva.canvas.event.LayerEvent;
import diva.graph.GraphPane;
import diva.graph.JGraph;

public class AWTVergilApplication {

    public static void main(String[] args) {
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
        Listener runListener = new Listener() {
            public void handleEvent(Event e) {
                try {
                    TypedCompositeActor toplevel = ((TypedCompositeActor) _toplevel);
                    Manager manager = new Manager(_toplevel.workspace(),
                            "myManager");

                    toplevel.setManager(manager);
                    manager.execute();
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }

            }
        };
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
        ToolItem exitToolItem = new ToolItem(toolBar, SWT.PUSH);
        exitToolItem.setText("&Exit");
        exitToolItem.addListener(SWT.Selection, exitListener);
        ToolItem aboutToolItem = new ToolItem(toolBar, SWT.PUSH);
        aboutToolItem.setText("&About");
        aboutToolItem.addListener(SWT.Selection, aboutListener);
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
        //        final JTable fileTable = new JTable(new FileTableModel(null));
        //        fileTable.setDoubleBuffered(true);
        //        fileTable.setShowGrid(false);
        //        fileTable.createDefaultColumnsFromModel();
        //        JScrollPane scrollPane = new JScrollPane(fileTable);
        //        panel.add(scrollPane);

        //final Plot plot = new Plot();
        //panel.add(plot);

        _toplevel = _openModel();
        final JComponent rightComponent = _createRightComponent(_toplevel);
        panel.add(rightComponent);

        java.awt.Frame statusFrame = SWT_AWT.new_Frame(statusComp);
        statusFrame.setBackground(new java.awt.Color(color.red, color.green,
                color.blue));
        final java.awt.Label statusLabel = new java.awt.Label();
        statusFrame.add(statusLabel);
        statusLabel.setText("Select a file");

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
        //        fileTree.addListener(SWT.Selection, new Listener() {
        //            public void handleEvent(Event e) {
        //                TreeItem item = (TreeItem) e.item;
        //                if (item == null)
        //                    return;
        //                final File root = (File) item.getData();
        //                EventQueue.invokeLater(new Runnable() {
        //                    public void run() {
        //                        statusLabel.setText(root.getAbsolutePath());
        //                        locationText.setText(root.getAbsolutePath());
        //                        fileTable
        //                                .setModel(new FileTableModel(root.listFiles()));
        //                    }
        //                });
        //            }
        //        });

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
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    /** Set the JGraph instance that this view uses to represent the
     *  ptolemy model.
     *  @param jgraph The JGraph.
     *  @see #getJGraph()
     */
    public static void setJGraph(JGraph jgraph) {
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
     */
    protected static GraphPane _createGraphPane(NamedObj entity) {
        _controller = new ActorEditorGraphController();
        _readConfiguration();
        _controller.setConfiguration(_configuration);
        //_controller.setFrame();

        // The cast is safe because the constructor only accepts
        // CompositeEntity.
        final ActorGraphModel graphModel = new ActorGraphModel(entity);
        return new ActorGraphPane(_controller, graphModel, entity);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Create the component that goes to the right of the library.
     *  @param entity The entity to display in the component.
     *  @return The component that goes to the right of the library.
     */
    protected static JComponent _createRightComponent(NamedObj entity) {
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

    protected static NamedObj _openModel() {
        try {
            URL modelURL = FileUtilities
                    .nameToURL(
                            "$CLASSPATH/ptolemy/domains/sdf/demo/Butterfly/Butterfly.xml",
                            null, null);
            System.out.println("modelURL: " + modelURL);
            MoMLParser parser = new MoMLParser();
            return parser.parse(null, modelURL);
        } catch (Exception ex) {
            System.out.println(ex);
            throw new RuntimeException(ex);
        }
    }

    protected static void _readConfiguration() {
        try {
            URL configurationURL = FileUtilities.nameToURL(
                    "$CLASSPATH/ptolemy/configs/full/configuration.xml", null,
                    null);
            System.out.println("ConfigurationURL: " + configurationURL);
            _configuration = ConfigurationApplication
                    .readConfiguration(configurationURL);
        } catch (Exception ex) {
            System.out.println(ex);
            throw new RuntimeException(ex);
        }

    }

    /** The graph controller. This is created in _createGraphPane(). */
    protected static ActorEditorGraphController _controller;

    protected static Configuration _configuration;

    protected static JGraph _jgraph;

    protected static NamedObj _toplevel;

    /**
     * Subclass that updates the background color on each repaint if there is a
     * preferences attribute.
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

    static class FileTableModel extends AbstractTableModel {
        public FileTableModel(File[] files) {
            this.files = files;
        }

        public Class getColumnClass(int col) {
            if (col == 1) {
                return Long.class;
            }
            if (col == 2) {
                return Date.class;
            }
            return String.class;
        }

        public int getColumnCount() {
            return columnsName.length;
        }

        public String getColumnName(int col) {
            return columnsName[col];
        }

        public int getRowCount() {
            return files == null ? 0 : files.length;
        }

        public Object getValueAt(int row, int col) {
            if (col == 0) {
                return files[row].getName();
            }
            if (col == 1) {
                return new Long(files[row].length());
            }
            if (col == 2) {
                return new Date(files[row].lastModified());
            }
            return "";
        }

        File[] files;

        String[] columnsName = { "Name", "Size", "Date Modified" };
    }

}
