/* An application for editing icons.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.icon;

// Diva imports.
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Paint;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import diva.gui.AppContext;
import diva.gui.BasicFrame;
import diva.gui.ExtensionFileFilter;
import diva.gui.GUIUtilities;
import diva.util.java2d.PaintedShape;

// Java imports.

// Javax imports.

// Ptolemy imports.

// FIXME this should be an application that uses documents.

//////////////////////////////////////////////////////////////////////////
//// IconEditor
/**
IconEditor class.  This class is a stand-alone application that
creates java2d shapes.

@author Nick Zamora (nzamor@uclink4.berkeley.edu)
@author Steve Neuendorffer  (neuendor@eecs.berkeley.edu)
@version $Id$
@since Ptolemy II 1.0
*/
public class IconEditor {
    /** Setup the window for the icon editor application.  This
     * window will include a toolbar of different shapes, a pull-down
     * menu of different thicknesses, pull down menus for the colors,
     * and the main drawing window.
     */
    public static void main(String argv[])
            throws NameDuplicationException, IllegalActionException {
        AppContext context = new BasicFrame("Icon Editor", false);
        // Make a new instance of the IconEditor class.
        new IconEditor(context);
    }

    /**
     * Create a new icon editor acting on an empty icon.
     */
    public IconEditor(AppContext context)
            throws NameDuplicationException, IllegalActionException {
        this(context, new XMLIcon(new NamedObj(), "icon"));
    }

    /**
     * Create a new icon editor acting on the given icon.
     */
    public IconEditor(AppContext context, XMLIcon icon) {
        // First point the local context and icon to the ones being
        // passed in.
        _context = context;
        _icon = icon;

        _editorPane = new IconEditorPane(icon);
        _context.getContentPane().add("Center", _editorPane);

        // Register the delete keyboard key press from the user and
        // listen for it.
        GUIUtilities.addHotKey(_editorPane, deletionListener,
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));

        // Cut, Copy, and Paste keyboard shortcuts are registered.
        GUIUtilities.addHotKey(_editorPane, cutAction,
                KeyStroke.getKeyStroke(KeyEvent.VK_X, 2));

        GUIUtilities.addHotKey(_editorPane, copyAction,
                KeyStroke.getKeyStroke(KeyEvent.VK_C, 2));

        GUIUtilities.addHotKey(_editorPane, pasteAction,
                KeyStroke.getKeyStroke(KeyEvent.VK_V, 2));

        // New, Open, Save, and Print keyboard shortcuts are registered.
        GUIUtilities.addHotKey(_editorPane, newIconAction,
                KeyStroke.getKeyStroke(KeyEvent.VK_N, 2));

        GUIUtilities.addHotKey(_editorPane, openIconAction,
                KeyStroke.getKeyStroke(KeyEvent.VK_O, 2));

        GUIUtilities.addHotKey(_editorPane, saveIconAction,
                KeyStroke.getKeyStroke(KeyEvent.VK_S, 2));

        GUIUtilities.addHotKey(_editorPane, printIconAction,
                KeyStroke.getKeyStroke(KeyEvent.VK_P, 2));

        _editorPane.setRequestFocusEnabled(true);

        // Make a toolbar for the different colors and shapes and
        // add it to the main _context frame.  Also, make another
        // toolbar for the different thicknesses and add that to the
        // main _context frame.
        JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
        _context.getContentPane().add("North", toolBar);

        // When you exit the program, here is what happens.
        _context.setExitAction(exitIconAction);

        // Create a menu bar and put it into the main context window.
        _menuBar = new JMenuBar();
        _context.setJMenuBar(_menuBar);

        // Create a "File" menu.
        _menuFile = new JMenu("File");
        _menuFile.setMnemonic('F');
        _menuEdit = new JMenu("Edit");
        _menuEdit.setMnemonic('E');
        _menuHelp = new JMenu("Help");
        _menuHelp.setMnemonic('H');

        // Add the file, edit, and help menus to the menu bar.
        _menuBar.add(_menuFile);
        _menuBar.add(_menuEdit);
        _menuBar.add(_menuHelp);

        // Add "New", "Open", "Save", "Save As", "Print", and "Exit"
        // to the "File" menu.
        GUIUtilities.addMenuItem(_menuFile, newIconAction, 'N',
                "Create a new icon and discard this one");
        GUIUtilities.addMenuItem(_menuFile, openIconAction, 'O',
                "Open an icon from a file");
        GUIUtilities.addMenuItem(_menuFile, saveIconAction, 'S',
                "Save this icon");
        GUIUtilities.addMenuItem(_menuFile, saveIconAsAction, 'A',
                "Save as ...");
        GUIUtilities.addMenuItem(_menuFile, printIconAction, 'P',
                "Print this icon");
        GUIUtilities.addMenuItem(_menuFile, exitIconAction, 'E',
                "Close the " + _context.getTitle() +
                " window");

        // Add "Cut", "Copy", and "Paste" functions to the edit menu.
        GUIUtilities.addMenuItem(_menuEdit, cutAction, 'C',
                "Cut the selected shape");
        GUIUtilities.addMenuItem(_menuEdit, copyAction, 'O',
                "Copy the selected shape");
        GUIUtilities.addMenuItem(_menuEdit, pasteAction, 'P',
                "Paste the shape previously cut or copied");

        // Add "About" to the help menu.
        GUIUtilities.addMenuItem(_menuHelp, helpAction, 'A',
                "About the Icon Editor");

        // Set up the buttons for the multiple toolbars.  These buttons
        // are instantiated with gif image files and these files must be
        // located in a sub-directory from this one named "gifs".

        URL Rectangle = getClass().getResource("gifs/rect.gif");
        URL Line = getClass().getResource("gifs/line.gif");
        URL Quad = getClass().getResource("gifs/quad.gif");
        URL Cubic = getClass().getResource("gifs/cubic.gif");
        URL Circle = getClass().getResource("gifs/circle.gif");
        URL Ellipse = getClass().getResource("gifs/ellipse.gif");
        //URL Fill = getClass().getResource("gifs/fill.gif");
        //URL Stroke = getClass().getResource("gifs/stroke.gif");
        URL More = getClass().getResource("gifs/more.gif");
        URL thickness1 = getClass().getResource("gifs/thickness1.gif");
        URL thickness2 = getClass().getResource("gifs/thickness2.gif");
        URL thickness3 = getClass().getResource("gifs/thickness3.gif");
        URL thickness4 = getClass().getResource("gifs/thickness4.gif");
        URL thickness5 = getClass().getResource("gifs/thickness5.gif");
        URL thinner = getClass().getResource("gifs/thinner.gif");
        URL thicker = getClass().getResource("gifs/thicker.gif");

        // Now that I have the names of all the gif files,
        // add them to the appropriate tool bars with the appropriate
        // actions.
        GUIUtilities.addToolBarButton(toolBar, rectangleAction,
                "Rectangle", new ImageIcon(Rectangle));
        GUIUtilities.addToolBarButton(toolBar, lineAction,
                "Straight Line", new ImageIcon(Line));
        GUIUtilities.addToolBarButton(toolBar, quadraticAction,
                "Quadratic Curve", new ImageIcon(Quad));
        GUIUtilities.addToolBarButton(toolBar, cubicAction,
                "Cubic Curve", new ImageIcon(Cubic));
        GUIUtilities.addToolBarButton(toolBar, circleAction,
                "Circle", new ImageIcon(Circle));
        GUIUtilities.addToolBarButton(toolBar, ellipseAction,
                "Ellipse", new ImageIcon(Ellipse));

        // Now I add the pull-down menus for the colors of the outline and
        // fill of the shapes and the thickness of the outline.
        BasicComboBoxRenderer renderer = new BasicComboBoxRenderer();
        renderer.setPreferredSize(new Dimension(27, 27));

        _fillComboBox = new JComboBox();
        _outlineComboBox = new JComboBox();
        _thicknessComboBox = new JComboBox();

        _fillComboBox.setRenderer(renderer);
        _outlineComboBox.setRenderer(renderer);
        _thicknessComboBox.setRenderer(renderer);

        toolBar.add(_fillComboBox);
        toolBar.add(_outlineComboBox);
        toolBar.add(_thicknessComboBox);

        // And I need to fill up the thickness pull-down menu
        // with the appropriate images.
        _thicknessComboBox.addItem(new ImageIcon(thickness1));
        _thicknessComboBox.addItem(new ImageIcon(thickness2));
        _thicknessComboBox.addItem(new ImageIcon(thickness3));
        _thicknessComboBox.addItem(new ImageIcon(thickness4));
        _thicknessComboBox.addItem(new ImageIcon(thickness5));

        // Similarly for the outline color pull-down menu.
        for (int i = 0; i < _colors.length; i++) {
            Icon bicon = new BlockIcon(_colors[i]);
            _outlineComboBox.addItem(bicon);
            _fillComboBox.addItem(bicon);
        }
        // The last entry is for "more colors"
        _outlineComboBox.addItem(new ImageIcon(More));
        _fillComboBox.addItem(new ImageIcon(More));

        // A pull-down menu needs a tool tip and an associated
        // action.
        _outlineComboBox.setToolTipText
            ("Choose a color to be the outline color of the selected shape(s)");
        _outlineComboBox.addActionListener(outlineAction);
        _outlinePaint = _colors[_outlineComboBox.getSelectedIndex()];
        _fillComboBox.setToolTipText
            ("Choose a color to be the fill color of the selected shape(s)");
        _fillComboBox.addActionListener(fillAction);
        _fillPaint = _colors[_fillComboBox.getSelectedIndex()];
        _thicknessComboBox.setToolTipText
            ("Choose a thickness for the outline(s) of the selected shape(s)");
        _thicknessComboBox.addActionListener(thicknessAction);

        // In addition to the thickness pull-down menu, there is also
        // an option to increment or decrement the thickness of a shape's
        // outline.  Here are the buttons associated with those functions.
        GUIUtilities.addToolBarButton
            (toolBar, thinnerAction,
                    "Thinner Outline(s) for the Selected Shape(s)",
                    new ImageIcon(thinner));
        GUIUtilities.addToolBarButton
            (toolBar, thickerAction,
                    "Thicker Outline(s) for the Selected Shape(s)",
                    new ImageIcon(thicker));


        // Set-up the possible file extensions for opening and saving icons.
        _filter.addExtension(FILE_FORMAT_EXTENSION);
        _filter.setDescription(FILE_FORMAT_EXTENSION + " extension only.");
        _fileChooser.setFileFilter(_filter);


        // Sets the size of the main window in pixels.
        _context.setSize(WINDOW_SIZE_HORIZONTAL, WINDOW_SIZE_VERTICAL);
        showEditorDialog();

    }

    //         StringBufferInputStream xml_stream = null;
    //         xml_stream = new StringBufferInputStream("<xmlgraphic> <rectangle coords=\"0 0 60 40\" fill=\"white\"/> <polygon coords=\"10 10 50 20 10 30\" fill=\"blue\"/> </xmlgraphic>\n");

    // When you click exit in the file menu of the menubar.
    Action exitIconAction = new AbstractAction("Exit") {
            public void actionPerformed(ActionEvent e) {
                _context.setVisible(false);
            }
        };

    ///////////////////////////////////////////////////////////////////
    ///////////////////      Private variables.       /////////////////////

    // The context of the icon editor application.
    private AppContext _context;

    // The editor pane.
    private IconEditorPane _editorPane;

    // The icon of the icon editor application.
    private XMLIcon _icon;

    // The menu bar(contains "File" , "Edit", and "Help" submenus).
    private JMenu _menuFile;
    private JMenu _menuEdit;
    private JMenu _menuHelp;
    private JMenuBar _menuBar;

    // Create the file chooser for the "Open" and "Save As" commands.
    private JFileChooser _fileChooser = new JFileChooser();
    private ExtensionFileFilter _filter = new ExtensionFileFilter("xml");

    // Create the combo box for the toolbars(pull-down menus)
    private JComboBox _thicknessComboBox;
    private JComboBox _fillComboBox;
    private JComboBox _outlineComboBox;

    // Used to distinguish which color we are changing, the fill of
    // the shape or the outline of the shape.
    private boolean _changingFill;

    // The color chooser.
    private JColorChooser _colorChooser = new JColorChooser();

    // This is the current shape, line thickness, and paint colors.
    private float _outlineThickness = 3.0f;

    // Blue and Gold(Go Bears!)
    private Paint _outlinePaint = new Color(255, 213, 20);
    private Paint _fillPaint = new Color(0, 0, 170);

    // Here is the figure kept in memory for the "cut" or
    // "pasted" figure.
    //private VersatileFigure _cutOrCopiedFigure = null;

    private JDialog _dialog;

    // The help "About" frame.
    private JFrame _helpFrame;

    // Constants for the program.  Decreasing MOUSE_SENSITIVITY will require
    // the user to be more precise when trying to click on figures.
    //private static final double MOUSE_SENSITIVITY = 4.0;

    // Defines the horizontal and vertical size of the main window.
    private static final int WINDOW_SIZE_HORIZONTAL = 600;
    private static final int WINDOW_SIZE_VERTICAL = 300;

    // This is the extension we allow for opening and saving files within the
    // program.
    private static final String FILE_FORMAT_EXTENSION = "xml";

    private Color _colors[] = {Color.black,
                               Color.blue,
                               Color.cyan,
                               Color.darkGray,
                               Color.gray,
                               Color.green,
                               Color.lightGray,
                               Color.magenta,
                               Color.orange,
                               Color.pink,
                               Color.red,
                               Color.white,
                               Color.yellow};

    // The type of data that is operable via the cut, copy, and paste commands.
    public static final DataFlavor dataFlavor =
    new DataFlavor(VersatileFigure.class, "Versatile Figure");

    ///////////////////////////////////////////////////////////////////
    /////////////////////    Anonymous Classes     ////////////////////////

    // Here are the definitions for all the actions that take place
    // when a button is clicked in one of the active windows, or a
    // menu item is selected from the toolbar at the top of the
    // window.  Also, when you click "OK" or "Cancel" in the color
    // window or file window, then that action code becomes invoked.
    // Each of these inner classes define the response for
    // exactly one button or selection.

    // When the rectangle button is pressed.
    // Because this action is similar to the lineAction,
    // quadraticAction, cubicAction, circleAction, and
    // ellipseAction, only this action is commented.
    Action rectangleAction = new AbstractAction("Rectangle") {
            public void actionPerformed(ActionEvent e) {
                // Create a new figure with the given outline
                // thickness and stroke color.  This figure
                // is placed on the canvas, underneath the
                // rectangle button in the toolbar.
                VersatileFigure figure = new VersatileFigure
                    (new PaintedShape(new Rectangle2D.Double
                        (8.0, 10.0, 20.0, 20.0),
                            _outlineThickness, _outlinePaint));

                // This figure begins with a fill color that is
                // currently selected.
                figure.setFillPaint(_fillPaint);

                // Add it to the editor pane.
                _editorPane.addFigure(figure);
            }
        };

    // When the straight line button is pressed.
    Action lineAction = new AbstractAction("Line") {
            public void actionPerformed(ActionEvent e) {
                VersatileFigure figure = new VersatileFigure
                    (new PaintedShape(new Line2D.Double
                        (45.0, 10.0, 65.0, 30.0),
                            _outlineThickness, _outlinePaint));
                figure.setFillPaint(_fillPaint);
                _editorPane.addFigure(figure);
            }
        };

    // When the quadratic curve button is pressed.
    Action quadraticAction = new AbstractAction("Quadratic Curve") {
            public void actionPerformed(ActionEvent e) {
                VersatileFigure figure = new VersatileFigure
                    (new PaintedShape(new QuadCurve2D.Double
                        (77.0, 10.0, 87.0, 20.0, 97.0, 30.0),
                            _outlineThickness, _outlinePaint));

                figure.setFillPaint(_fillPaint);
                _editorPane.addFigure(figure);
            }
        };

    // When the cubic curve button is pressed.
    Action cubicAction = new AbstractAction("Cubic Curve") {
            public void actionPerformed(ActionEvent e) {
                VersatileFigure figure = new VersatileFigure
                    (new PaintedShape(new CubicCurve2D.Double
                        (110.0, 10.0, 117.0, 17.0,
                                123.0, 23.0, 130.0, 30.0),
                            _outlineThickness, _outlinePaint));

                figure.setFillPaint(_fillPaint);
                _editorPane.addFigure(figure);
            }
        };

    // When the circle button is pressed.
    Action circleAction = new AbstractAction("Circle") {
            public void actionPerformed(ActionEvent e) {
                VersatileFigure figure = new VersatileFigure
                    (new PaintedShape(new Ellipse2D.Double
                        (148.0, 10.0, 20.0, 20.0),
                            _outlineThickness, _outlinePaint));

                figure.setFillPaint(_fillPaint);
                _editorPane.addFigure(figure);
            }
        };

    // When the ellipse button is pressed.
    Action ellipseAction = new AbstractAction("Ellipse") {
            public void actionPerformed(ActionEvent e) {
                VersatileFigure figure = new VersatileFigure
                    (new PaintedShape(new Ellipse2D.Double
                        (183.0, 10.0, 20.0, 30.0),
                            _outlineThickness, _outlinePaint));
                figure.setFillPaint(_fillPaint);
                _editorPane.addFigure(figure);
            }
        };

    // When the fill color combo box is chosen.
    Action fillAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                int selection = _fillComboBox.getSelectedIndex();
                int itemCount = _fillComboBox.getItemCount();
                if (selection == itemCount - 1) {
                    _changingFill = true;
                    _dialog = JColorChooser.createDialog
                        (_editorPane, "Choose A Fill Color", true,
                                _colorChooser, okAction, cancelAction);
                    _dialog.setVisible(true);
                } else {
                    _fillPaint = _colors[selection];
                    _editorPane.setFillPaint(_fillPaint);
                }
            }
        };

    // When the outline color combo box is chosen.
    Action outlineAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                int selection = _outlineComboBox.getSelectedIndex();
                int itemCount = _outlineComboBox.getItemCount();
                if (selection == itemCount - 1) {
                    _changingFill = false;
                    _dialog = JColorChooser.createDialog
                        (_editorPane, "Choose An Outline Color", true,
                                _colorChooser, okAction, cancelAction);
                    _dialog.setVisible(true);
                } else {
                    _outlinePaint = _colors[selection];
                    _editorPane.setOutlinePaint(_outlinePaint);
                }
            }
        };

    // When the thinner button is pressed.
    Action thinnerAction = new AbstractAction("Thinner") {
            public void actionPerformed(ActionEvent e) {
                float oldWidth = _editorPane.getThickness();
                if (oldWidth != 0.0 && oldWidth > 1.0) {
                    _outlineThickness = oldWidth - 1.0f;
                    _editorPane.setThickness(_outlineThickness);

                }
            }
        };

    // When the thicker button is pressed.
    Action thickerAction = new AbstractAction("Thicker") {
            public void actionPerformed(ActionEvent e) {
                float oldWidth = _editorPane.getThickness();
                if (oldWidth != 0.0) {
                    _outlineThickness = oldWidth + 1.0f;
                    _editorPane.setThickness(_outlineThickness);
                }
            }
        };

    // When you click ok in the color window.
    Action okAction = new AbstractAction("Ok") {
            public void actionPerformed(ActionEvent e) {
                Color thisColor = _colorChooser.getColor();
                if (_changingFill) {
                    _fillPaint = thisColor;
                    _editorPane.setFillPaint(thisColor);
                    // FIXME        _fillComboBox.setSelectedIndex(-1);
                } else {
                    _outlinePaint = thisColor;
                    _editorPane.setFillPaint(thisColor);
                }
            }
        };

    // When you click cancel in the color window.
    Action cancelAction = new AbstractAction("Cancel") {
            public void actionPerformed(ActionEvent e) {
            }
        };

    // When you click cut in the edit menu of the menubar.
    // The cut operation grabs the system clipboard, then puts
    // the currently selected item onto the clipboard, and removes
    // the currently selected item from the canvas.
    Action cutAction = new AbstractAction("Cut") {
            public void actionPerformed(ActionEvent e) {
                Clipboard c = _editorPane.getToolkit().getSystemClipboard();
                _editorPane.cut(c);
            }
        };

    // When you click copy in the edit menu of the menubar.
    // The copy operation grabs the system clipboard, then puts
    // the currently selected item onto the clipboard.
    Action copyAction = new AbstractAction("Copy") {
            public void actionPerformed(ActionEvent e) {
                Clipboard c = _editorPane.getToolkit().getSystemClipboard();
                _editorPane.copy(c);
            }
        };

    // When you click paste in the edit menu of the menubar.
    // The paste operation grabs the system clipboard, then gets the
    // current data object on the clipboard, makes a copy of it,
    // and adds it to the figure layer of the canvas.
    // If something goes wrong, the machine should beep.
    Action pasteAction = new AbstractAction("Paste") {
            public void actionPerformed(ActionEvent e) {
                Clipboard c = _editorPane.getToolkit().getSystemClipboard();
                _editorPane.paste(c);
            }
        };

    // When you click new in the file menu of the menubar.
    Action newIconAction = new AbstractAction("New") {
            public void actionPerformed(ActionEvent e) {
                _editorPane.clear();
            }
        };

    // When you click open in the file menu of the menubar.
    Action openIconAction = new AbstractAction("Open") {
            public void actionPerformed(ActionEvent e) {
                int choice =
                    _fileChooser.showOpenDialog(_context.makeComponent());
                if (choice == JFileChooser.CANCEL_OPTION) {
                    //System.out.println("You have cancelled your open file choice");
                } else {
                    //System.out.println("You have chosen to open this file: " +
                    //                    _fileChooser.getSelectedFile().getName());
                    _editorPane.clear();
                    //FIXME: Here is where I would import an xml file to this
                    //canvas.
                }
            }
        };

    // When you click save in the file menu of the menubar.
    Action saveIconAction = new AbstractAction("Save") {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Save");
                // FIXME
            }
        };

    // When you click save as in the file menu of the menubar.
    Action saveIconAsAction = new AbstractAction("Save As...") {
            public void actionPerformed(ActionEvent e) {
                int choice =
                    _fileChooser.showSaveDialog(_context.makeComponent());
                if (choice == JFileChooser.CANCEL_OPTION) {
                    //System.out.println("You have cancelled your
                    //                     save choice.");
                } else {
                    //System.out.println("You chose to save this file: " +
                    //                    _fileChooser.getSelectedFile().getName());
                }
            }
        };

    Action printIconAction = new AbstractAction("Print") {
            public void actionPerformed(ActionEvent e) {
                _editorPane.print();
            }
        };

    // When you click about in the help menu of the menubar.
    Action helpAction = new AbstractAction("About") {
            public void actionPerformed(ActionEvent e) {
                // FIXME Dialog.
                _helpFrame = new JFrame("About Icon Editor");
                JButton jButton = new JButton
                    ("Author: Nick Zamora, Version: " +
                            "$version$");
                jButton.addActionListener(helpOkAction);
                _helpFrame.getContentPane().add(jButton);
                _helpFrame.getContentPane().doLayout();
                _helpFrame.setResizable(false);
                _helpFrame.setLocation(100, 100);
                _helpFrame.setSize(500, 150);
                _helpFrame.setVisible(true);
            }
        };

    // When you click the button in the about window.
    Action helpOkAction = new AbstractAction("OK Button") {
            public void actionPerformed(ActionEvent e) {
                _helpFrame.setVisible(false);
            }
        };

    // When the thickness combo box is chosen.
    Action thicknessAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                int chosenThickness = _thicknessComboBox.getSelectedIndex();
                switch(chosenThickness) {
                case 0:
                    _editorPane.setThickness(1.0f);
                    _outlineThickness = 1.0f;
                    break;
                case 1:
                    _editorPane.setThickness(3.0f);
                    _outlineThickness = 3.0f;
                    break;
                case 2:
                    _editorPane.setThickness(6.0f);
                    _outlineThickness = 6.0f;
                    break;
                case 3:
                    _editorPane.setThickness(10.0f);
                    _outlineThickness = 10.0f;
                    break;
                case 4:
                    _editorPane.setThickness(14.0f);
                    _outlineThickness = 14.0f;
                    break;
                default:
                    break;
                }
            }
        };

    public class BlockIcon implements Icon {
        BlockIcon(Color c) {
            _color = c;
        }
        public int getIconWidth() {
            return 25;
        }
        public int getIconHeight() {
            return 25;
        }
        public void paintIcon(Component c,
                Graphics g,
                int x,
                int y) {
            g.setColor(_color);
            g.fillRect(x, y, getIconHeight(), getIconWidth());
        }
        private Color _color;
    }

    // Listen for the delete key from the keyboard.  When the delete key is
    // pressed, the currently selected figure is removed from the _layer and
    // unselected from the selection model.  Pressing the delete key is unlike
    // the cut command from the edit menu in the toolbar in that the delete
    // command is irreversible.  "Paste" will NOT return a figure that has
    // been deleted from the canvas.
    Action deletionListener = new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                _editorPane.delete();
            }
        };

    public AppContext getAppContext() {
        return _context;
    }

    // Assign the title of the context.
    public void setTitle(String title) {
        _context.setTitle(title);
    }

    // Fetch the title of the context.
    public String getTitle() {
        return _context.getTitle();
    }

    public void showEditorDialog() {
        // Only set the window visible now.
        _context.setVisible(true);
    }
}

