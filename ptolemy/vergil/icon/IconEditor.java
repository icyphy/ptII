/*
 * Copyright (c) 1998-2000 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package ptolemy.vergil.icon;

// *** Note *** Imports are all in alphabetical order

// Diva imports.
import diva.canvas.FigureLayer;
import diva.canvas.GraphicsPane;
import diva.canvas.interactor.BasicSelectionModel;
import diva.canvas.interactor.BoundsManipulator;
import diva.canvas.interactor.CircleManipulator;
import diva.canvas.interactor.DragInteractor;
import diva.canvas.interactor.PathManipulator;
import diva.canvas.interactor.SelectionDragger;
import diva.canvas.interactor.SelectionInteractor;
import diva.canvas.JCanvas;
import diva.canvas.toolbox.VersatileFigure;
import diva.gui.AppContext;
import diva.gui.BasicFrame;
import diva.gui.ExtensionFileFilter;
import diva.gui.GUIUtilities;
import diva.util.java2d.PaintedList;
import diva.util.java2d.PaintedObject;
import diva.util.java2d.PaintedPath;
import diva.util.java2d.PaintedShape;
import diva.util.java2d.PaintedString;
import diva.util.java2d.Polygon2D;

// Java imports.
import java.awt.Color;
import java.awt.Component;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.Paint;
import java.awt.print.PrinterJob;
import java.awt.Shape;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

// Javax imports.
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

// Ptolemy imports.
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.vergil.toolbox.GraphicElement;
import ptolemy.vergil.toolbox.XMLIcon;

/**
 * IconEditor.java
 * @author Nick Zamora (nzamor@uclink4.berkeley.edu) 
 * @author Steve Neuendorffer  (neuendor@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 *
 */

/**
 * IconEditor class.  This class is a stand-alone application that 
 * creates java2d shapes.  Future work on this class will be to 
 * integrate this application into vergil and use the xml icon 
 * format for run-time reading and writing of icons.
 */

public class IconEditor extends Component {

    // Control begins here.

    public static void main(String argv[])

        throws NameDuplicationException, IllegalActionException {

	// Setup the window for the icon editor application.  This 
        // window will include a toolbar of different shapes, a pull-down 
        // menu of different thicknesses, pull down menus for the colors, 
        // and the main drawing window.
        // For context, I used a BasicFrame with a false argument, 
        // which tells BasicFrame not to set the size of the window or 
        // make it visible.  The string "Edit Icon" is the name of the 
        // default window.

        AppContext context = new BasicFrame ("Icon Editor", false);

	// Make a new instance of the IconEditor class.

	IconEditor iconEditor = new IconEditor(context);

    }
  
    public IconEditor (AppContext context)

        throws NameDuplicationException, IllegalActionException {

        // Make a new instance of the IconEditor class with 
        // a new XMLIcon named "icon".

        this (context, new XMLIcon (new NamedObj(), "icon"));

    }

    public IconEditor (AppContext context, XMLIcon icon) {

        // First point the local context and icon to the ones being 
        // passed in.

	_context = context;

	_icon = icon;

        // Instantiate the color chooser for the color button.

        _colorChooser = new JColorChooser ();
      
        // Make a _canvas for the _context to use for drawing.

        _canvas = new JCanvas ();

	_context.getContentPane ().add ("Center", _canvas);


	// Register the delete keyboard key press from the user and 
	// listen for it.

	_canvas.registerKeyboardAction (deletionListener, "Delete",
					KeyStroke.getKeyStroke (KeyEvent.VK_DELETE, 0),
					JComponent.WHEN_IN_FOCUSED_WINDOW);
	
	// Cut, Copy, and Paste keyboard shortcuts are registered.

	_canvas.registerKeyboardAction (cutAction, "Cut Figure", 
					KeyStroke.getKeyStroke (KeyEvent.VK_X, 2),
					JComponent.WHEN_IN_FOCUSED_WINDOW);

	_canvas.registerKeyboardAction (copyAction, "Copy Figure", 
					KeyStroke.getKeyStroke (KeyEvent.VK_C, 2),
					JComponent.WHEN_IN_FOCUSED_WINDOW);

	_canvas.registerKeyboardAction (pasteAction, "Paste Figure", 
				       KeyStroke.getKeyStroke (KeyEvent.VK_V, 2),
				       JComponent.WHEN_IN_FOCUSED_WINDOW);
	
	// New, Open, Save, and Print keyboard shortcuts are registered.

	_canvas.registerKeyboardAction (newIconAction, "New Icon", 
					KeyStroke.getKeyStroke (KeyEvent.VK_N, 2),
					JComponent.WHEN_IN_FOCUSED_WINDOW);

	_canvas.registerKeyboardAction (openIconAction, "Open Icon", 
					KeyStroke.getKeyStroke (KeyEvent.VK_O, 2),
					JComponent.WHEN_IN_FOCUSED_WINDOW);

	_canvas.registerKeyboardAction (saveIconAction, "Save Icon", 
					KeyStroke.getKeyStroke (KeyEvent.VK_S, 2),
					JComponent.WHEN_IN_FOCUSED_WINDOW);

	_canvas.registerKeyboardAction (printIconAction, "Print document", 
					KeyStroke.getKeyStroke (KeyEvent.VK_P, 2), 
					JComponent.WHEN_IN_FOCUSED_WINDOW);

	_canvas.setRequestFocusEnabled (true);


	// Make a toolbar for the different colors and shapes and 
	// add it to the main _context frame.  Also, make another 
	// toolbar for the different thicknesses and add that to the 
	// main _context frame.

	JToolBar thicknessToolBar = new JToolBar (JToolBar.VERTICAL);
	
	_context.getContentPane ().add ("East", thicknessToolBar);
	
	JToolBar shapesToolBar = new JToolBar (JToolBar.HORIZONTAL);
	
	_context.getContentPane ().add ("North", shapesToolBar);

	// When you exit the program, here is what happens.

	_context.setExitAction (exitIconAction);

	// Create a menu bar and put it into the main context window.

	_menuBar = new JMenuBar ();
	
	_context.setJMenuBar (_menuBar);

	// Create a "File" menu.

        _menuFile = new JMenu ("File");

	_menuEdit = new JMenu ("Edit");

	_menuHelp = new JMenu ("Help");

	// Add the file, edit, and help menus to the menu bar.

	_menuBar.add (_menuFile);

	_menuBar.add (_menuEdit);

	_menuBar.add (_menuHelp);

        _menuFile.setMnemonic ('F');

	_menuEdit.setMnemonic ('E');

	_menuHelp.setMnemonic ('H');

	// Add "New", "Open", "Save", "Save As", "Print", and "Exit" 
	// to the "File" menu.

	GUIUtilities.addMenuItem (_menuFile, newIconAction, 'N', 
				  "Create a new icon and discard this one");
	
	GUIUtilities.addMenuItem (_menuFile, openIconAction, 'O', 
				  "Open an icon from a file");
	
	GUIUtilities.addMenuItem (_menuFile, saveIconAction, 'S', 
				  "Save this icon");
	
	GUIUtilities.addMenuItem (_menuFile, saveIconAsAction, 'A', 
				  "Save as ...");
	
	GUIUtilities.addMenuItem (_menuFile, printIconAction, 'P', 
				  "Print this icon");
	
	GUIUtilities.addMenuItem (_menuFile, exitIconAction, 'E', 
				  "Close the " + _context.getTitle () + " window");
        
	// Add "Cut", "Copy", and "Paste" functions to the edit menu.

	GUIUtilities.addMenuItem (_menuEdit, cutAction, 'C', 
				 "Cut the selected shape");

	GUIUtilities.addMenuItem (_menuEdit, copyAction, 'O', 
				 "Copy the selected shape");

	GUIUtilities.addMenuItem (_menuEdit, pasteAction, 'P',
				 "Paste the shape previously cut or copied");

	// Add "About" to the help menu.
	
	GUIUtilities.addMenuItem (_menuHelp, helpAction, 'A', 
				  "About the Icon Editor");
	
    
	// Set up the buttons for the multiple toolbars.  These buttons 
	// are instantiated with gif image files and these files must be 
	// located in a sub-directory from this one named "gifs".

	URL White = getClass ().getResource ("gifs/white.gif");

	URL LightGray = getClass ().getResource ("gifs/lightgray.gif");

	URL Gray = getClass ().getResource ("gifs/gray.gif");

	URL DarkGray = getClass ().getResource ("gifs/darkgray.gif");

	URL Black = getClass ().getResource ("gifs/black.gif");

	URL Blue = getClass ().getResource ("gifs/blue.gif");

	URL Cyan = getClass ().getResource ("gifs/cyan.gif");

	URL Green = getClass ().getResource ("gifs/green.gif");

	URL Magenta = getClass ().getResource ("gifs/magenta.gif");

	URL Orange = getClass ().getResource ("gifs/orange.gif");

	URL Pink = getClass ().getResource ("gifs/pink.gif");

	URL Red = getClass ().getResource ("gifs/red.gif");

	URL Yellow = getClass ().getResource ("gifs/yellow.gif");

	URL Rectangle = getClass ().getResource ("gifs/rect.gif");

	URL Line = getClass ().getResource ("gifs/line.gif");

	URL Quad = getClass ().getResource ("gifs/quad.gif");

	URL Cubic = getClass ().getResource ("gifs/cubic.gif");

	URL Circle = getClass ().getResource ("gifs/circle.gif");

	URL Ellipse = getClass ().getResource ("gifs/ellipse.gif");

	URL Fill = getClass ().getResource ("gifs/fill.gif");

	URL Stroke = getClass ().getResource ("gifs/stroke.gif");

	URL More = getClass ().getResource ("gifs/more.gif");

	URL thickness1 = getClass ().getResource ("gifs/thickness1.gif");

	URL thickness2 = getClass ().getResource ("gifs/thickness2.gif");

	URL thickness3 = getClass ().getResource ("gifs/thickness3.gif");

	URL thickness4 = getClass ().getResource ("gifs/thickness4.gif");

	URL thickness5 = getClass ().getResource ("gifs/thickness5.gif");

	URL thinner = getClass ().getResource ("gifs/thinner.gif");

	URL thicker = getClass ().getResource ("gifs/thicker.gif");

	// Now that I have the names of all the gif files, I need to 
	// add them to the appropriate tool bars with the appropriate 
	// actions.  All these actions are defined as anonymous inner 
	// classes within this class.

	GUIUtilities.addToolBarButton (shapesToolBar, rectangleAction,
				       "Rectangle", new ImageIcon(Rectangle));

	GUIUtilities.addToolBarButton (shapesToolBar, lineAction,
				       "Straight Line", new ImageIcon(Line));

	GUIUtilities.addToolBarButton (shapesToolBar, quadraticAction,
				       "Quadratic Curve", new ImageIcon(Quad));

	GUIUtilities.addToolBarButton (shapesToolBar, cubicAction,
				       "Cubic Curve", new ImageIcon(Cubic));

	GUIUtilities.addToolBarButton (shapesToolBar, circleAction, 
				       "Circle", new ImageIcon(Circle));

	GUIUtilities.addToolBarButton (shapesToolBar, ellipseAction, 
				       "Ellipse", new ImageIcon(Ellipse));

	// Now I add the pull-down menus for the colors of the outline and 
	// fill of the shapes and the thickness of the outline.

	_fillComboBox = new JComboBox ();

	_outlineComboBox = new JComboBox ();

	_thicknessComboBox = new JComboBox ();

	shapesToolBar.add (_fillComboBox);

	shapesToolBar.add (_outlineComboBox);

	thicknessToolBar.add (_thicknessComboBox);

	// And I need to fill up the thickness pull-down menu 
	// with the appropriate images.

	_thicknessComboBox.addItem (new ImageIcon (thickness1));

	_thicknessComboBox.addItem (new ImageIcon (thickness2));

	_thicknessComboBox.addItem (new ImageIcon (thickness3));

	_thicknessComboBox.addItem (new ImageIcon (thickness4));

	_thicknessComboBox.addItem (new ImageIcon (thickness5));
	
	// Similarly for the outline color pull-down menu.

	_outlineComboBox.addItem (new ImageIcon(White));

	_outlineComboBox.addItem (new ImageIcon(LightGray));

	_outlineComboBox.addItem (new ImageIcon(Gray));

	_outlineComboBox.addItem (new ImageIcon(DarkGray));

	_outlineComboBox.addItem (new ImageIcon(Black));

	_outlineComboBox.addItem (new ImageIcon(Blue));

	_outlineComboBox.addItem (new ImageIcon(Cyan));

	_outlineComboBox.addItem (new ImageIcon(Green));

	_outlineComboBox.addItem (new ImageIcon(Magenta));

	_outlineComboBox.addItem (new ImageIcon(Orange));

	_outlineComboBox.addItem (new ImageIcon(Pink));

	_outlineComboBox.addItem (new ImageIcon(Red));

	_outlineComboBox.addItem (new ImageIcon(Yellow));

	_outlineComboBox.addItem (new ImageIcon (More));

	// And the fill color pull-down menu.

	_fillComboBox.addItem (new ImageIcon (White));
	
	_fillComboBox.addItem (new ImageIcon (LightGray));

	_fillComboBox.addItem (new ImageIcon (Gray));
	
	_fillComboBox.addItem (new ImageIcon (DarkGray));

	_fillComboBox.addItem (new ImageIcon (Black));

	_fillComboBox.addItem (new ImageIcon (Blue));

	_fillComboBox.addItem (new ImageIcon (Cyan));

	_fillComboBox.addItem (new ImageIcon (Green));

	_fillComboBox.addItem (new ImageIcon (Magenta));

	_fillComboBox.addItem (new ImageIcon (Orange));

	_fillComboBox.addItem (new ImageIcon (Pink));

	_fillComboBox.addItem (new ImageIcon (Red));

	_fillComboBox.addItem (new ImageIcon (Yellow));

	_fillComboBox.addItem (new ImageIcon (More));

	// A pull-down menu needs a tool tip and an associated 
	// action.  Here I define those two things for all 3 
	// of my pull-down menus.  Remember, all the actions 
	// are defined as anonymous inner classes within this 
	// class.

	_outlineComboBox.setToolTipText 
	    ("Choose a color to be the outline color of the selected shape(s)");

	_outlineComboBox.addActionListener (outlineAction);

	_fillComboBox.setToolTipText 
	    ("Choose a color to be the fill color of the selected shape(s)");

	_fillComboBox.addActionListener (fillAction);

	_thicknessComboBox.setToolTipText 
	    ("Choose a thickness for the outline(s) of the selected shape(s)");

	_thicknessComboBox.addActionListener (thicknessAction);

	// In addition to the thickness pull-down menu, there is also 
	// an option to increment or decrement the thickness of a shape's
	// outline.  Here are the buttons associated with those functions.

	GUIUtilities.addToolBarButton 
	    (thicknessToolBar, thinnerAction,
	     "Thinner Outline(s) for the Selected Shape(s)", 
	     new ImageIcon(thinner));

	GUIUtilities.addToolBarButton 
	    (thicknessToolBar, thickerAction,
	     "Thicker Outline(s) for the Selected Shape(s)", 
	     new ImageIcon(thicker));

	// Now we need to get the canvas pane and foreground layer of our 
	// canvas.  Also, here is where I set the halo width (twice the 
	// distance your mouse can be from a figure when you click and still 
	// have the program detect that you are trying to click on the figure).

	_pane = (GraphicsPane) _canvas.getCanvasPane ();

	_layer = _pane.getForegroundLayer ();

	_layer.setPickHalo (MOUSE_SENSITIVITY);
	
	// I have to make the figures "drag-able".

	_interactor1.addInteractor (new DragInteractor ());

	_interactor2.addInteractor (new DragInteractor ());

	_interactor3.addInteractor (new DragInteractor ());

	// When they are selected, put grab handles on them.

	_interactor1.setPrototypeDecorator (new PathManipulator ());

	_interactor2.setPrototypeDecorator (new BoundsManipulator ());

	_interactor3.setPrototypeDecorator (new CircleManipulator ());
	
	// Set-up the possible file extensions for opening and saving icons.

	_filter.addExtension (FILE_FORMAT_EXTENSION);

	_filter.setDescription (FILE_FORMAT_EXTENSION + " extension only.");

	_fileChooser.setFileFilter (_filter);

	// Begin with the elements specified in the icon passed into 
	// the constructor.

	addXMLIcon (icon);

	// This next part allows the user to select multiple figures 
	// with the mouse by dragging a rectangle box around the figures 
	// the user wishes to be selected.

	_selectionDragger = new SelectionDragger (_pane);

	_selectionDragger.addSelectionInteractor (_interactor1);

	_selectionDragger.addSelectionInteractor (_interactor2);

	_selectionDragger.addSelectionInteractor (_interactor3);

	// Sets the size of the main window in pixels.

	_context.setSize (WINDOW_SIZE_HORIZONTAL, WINDOW_SIZE_VERTICAL);

	showEditorDialog ();

    }

    //         StringBufferInputStream xml_stream = null;
    //         xml_stream = new StringBufferInputStream ("<xmlgraphic> <rectangle coords=\"0 0 60 40\" fill=\"white\"/> <polygon coords=\"10 10 50 20 10 30\" fill=\"blue\"/> </xmlgraphic>\n");	       


    ////////////////////////////////////////////////////////////////////////
    ///////////////////      Private variables.       /////////////////////


    // The context of the icon editor application.

    private AppContext _context;

    // The icon of the icon editor application.
  
    private XMLIcon _icon;

    // The menu bar (contains "File" , "Edit", and "Help" submenus).

    private JMenu _menuFile;
    private JMenu _menuEdit;
    private JMenu _menuHelp;
    private JMenuBar _menuBar;

    // Create the file chooser for the "Open" and "Save As" commands.

    private JFileChooser _fileChooser = new JFileChooser ();

    private ExtensionFileFilter _filter = new ExtensionFileFilter ("xml");

    // Create the combo box for the toolbars (pull-down menus)

    private JComboBox _thicknessComboBox;

    private JComboBox _fillComboBox;

    private JComboBox _outlineComboBox;

    // Used to distinguish which color we are changing, the fill of 
    // the shape or the outline of the shape.

    private boolean _changingFill;

    // The color chooser.

    private JColorChooser _colorChooser;

    // Here are the interactors for each shape

    private BasicSelectionModel _m = new BasicSelectionModel ();

    private SelectionInteractor _interactor1 = new SelectionInteractor (_m);

    private SelectionInteractor _interactor2 = new SelectionInteractor (_m);

    private SelectionInteractor _interactor3 = new SelectionInteractor (_m);
  
    // For dragging

    private SelectionDragger _selectionDragger;

    // This is the current shape, line thickness, and paint colors.

    private VersatileFigure _currentFigure = null;

    private float _outlineThickness = 3.0f;

    // Blue and Gold (Go Bears!)

    private Paint _strokeColor = new Color (255, 213, 20);

    private Paint _fillColor = new Color (0, 0, 170);

    // Here is the figure kept in memory for the "cut" or 
    // "pasted" figure.

    private VersatileFigure _cutOrCopiedFigure = null;

    // Window objects

    private GraphicsPane _pane;

    private FigureLayer _layer;

    private JCanvas _canvas;

    private JDialog _dialog;

    // The help "About" frame.

    private JFrame _helpFrame;

    // Constants for the program.  Decreasing MOUSE_SENSITIVITY will require 
    // the user to be more precise when trying to click on figures.

    private static final double MOUSE_SENSITIVITY = 4.0;

    // Defines the horizontal and vertical size of the main window.

    private static final int WINDOW_SIZE_HORIZONTAL = 600;

    private static final int WINDOW_SIZE_VERTICAL = 300;

    // This is the extension we allow for opening and saving files within the 
    // program.

    private static final String FILE_FORMAT_EXTENSION = "xml";

    // The type of data that is operable via the cut, copy, and paste commands.
    
    public static final DataFlavor dataFlavor = 
        new DataFlavor (VersatileFigure.class, "Versatile Figure");

    ////////////////////////////////////////////////////////////////////////
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

    Action rectangleAction = new AbstractAction ("Rectangle") {

        public void actionPerformed (ActionEvent e) {

	    // Create a new figure with the given outline 
	    // thickness and stroke color.  This figure 
	    // is placed on the canvas, underneath the 
	    // rectangle button in the toolbar.

	    _currentFigure = new VersatileFigure 
	        (new PaintedShape (new Rectangle2D.Double
				   (8.0, 10.0, 20.0, 20.0), 
				   _outlineThickness, _strokeColor));

	    // This figure begins with a fill color that is 
	    // currently selected.

	    _currentFigure.setFillPaint (_fillColor);

	    // Add this figure to the figure layer.

	    _layer.add (_currentFigure);

	    // Set the appropriate interactor for this shape.  
	    // For a rectangle, this interactor is a 
	    // BoundsManipulator.

	    _currentFigure.setInteractor (_interactor2);

	    // Unselect any currently selected figures.

	    _selectionDragger.clearSelection ();

	    // Add this figure to the selection dragger.

	    _selectionDragger.expandSelection (_interactor2, _currentFigure);
	}
    };

    // When the straight line button is pressed.

    Action lineAction = new AbstractAction ("Line") {

        public void actionPerformed (ActionEvent e) {

	    _currentFigure = new VersatileFigure 
	        (new PaintedShape (new Line2D.Double
				   (45.0, 10.0, 65.0, 30.0), 
				   _outlineThickness, _strokeColor)); 

	    _currentFigure.setFillPaint (_fillColor);

	    _layer.add (_currentFigure);

	    _currentFigure.setInteractor (_interactor1);

	    _selectionDragger.clearSelection ();

	    _selectionDragger.expandSelection (_interactor1, _currentFigure);
	}
    };

    // When the quadratic curve button is pressed.

    Action quadraticAction = new AbstractAction ("Quadratic Curve") {

        public void actionPerformed (ActionEvent e) {

	    _currentFigure = new VersatileFigure 
	        (new PaintedShape (new QuadCurve2D.Double
				   (77.0, 10.0, 87.0, 20.0, 97.0, 30.0), 
				   _outlineThickness, _strokeColor));

	    _currentFigure.setFillPaint (_fillColor);

	    _layer.add (_currentFigure);

	    _currentFigure.setInteractor (_interactor1);

	    _selectionDragger.clearSelection ();

	    _selectionDragger.expandSelection (_interactor1, _currentFigure);

	}
    };

    // When the cubic curve button is pressed.

    Action cubicAction = new AbstractAction ("Cubic Curve") {

        public void actionPerformed (ActionEvent e) {

	    _currentFigure = new VersatileFigure 
	        (new PaintedShape (new CubicCurve2D.Double
				   (110.0, 10.0, 117.0, 17.0, 
				    123.0, 23.0, 130.0, 30.0),
				   _outlineThickness, _strokeColor));

	    _currentFigure.setFillPaint (_fillColor);

	    _layer.add (_currentFigure);

	    _currentFigure.setInteractor (_interactor1);

	    _selectionDragger.clearSelection ();

	    _selectionDragger.expandSelection (_interactor1, _currentFigure);

	}
    };

    // When the circle button is pressed.

    Action circleAction = new AbstractAction ("Circle") {

        public void actionPerformed (ActionEvent e) {

	    _currentFigure = new VersatileFigure 
	        (new PaintedShape (new Ellipse2D.Double
				   (148.0, 10.0, 20.0, 20.0), 
				   _outlineThickness, _strokeColor));

	    _currentFigure.setFillPaint (_fillColor);

	    _layer.add (_currentFigure);

	    _currentFigure.setInteractor (_interactor3);

	    _selectionDragger.clearSelection ();

	    _selectionDragger.expandSelection (_interactor3, _currentFigure);

	}
    };

    // When the ellipse button is pressed.

    Action ellipseAction = new AbstractAction ("Ellipse") {

        public void actionPerformed (ActionEvent e) {

	    _currentFigure = new VersatileFigure 
	        (new PaintedShape (new Ellipse2D.Double
				   (183.0, 10.0, 20.0, 30.0), 
				   _outlineThickness, _strokeColor));

	    _currentFigure.setFillPaint (_fillColor);

	    _layer.add (_currentFigure);

	    _currentFigure.setInteractor (_interactor2);

	    _selectionDragger.clearSelection ();

	    _selectionDragger.expandSelection (_interactor2, _currentFigure);

	}
    };
  
    // When the fill color combo box is chosen.

    ActionListener fillAction = new ActionListener () {

        public void actionPerformed (ActionEvent e) {

	    int selection = _fillComboBox.getSelectedIndex ();

	    int itemCount = _fillComboBox.getItemCount ();

	    System.out.println("how many times?");

	    if (selection == itemCount - 1) {

	        _changingFill = true;

	        _dialog = JColorChooser.createDialog 
		    (_canvas, "Choose A Fill Color", true, 
		     _colorChooser, okAction, cancelAction);

		_dialog.setVisible (true);

	    }

	    else if (selection == itemCount - 2) {

	        changeFillColor (Color.yellow);

	    }

	    else if (selection == itemCount - 3) {

	        changeFillColor (Color.red);

	    }

	    else if (selection == itemCount - 4) {

	        changeFillColor (Color.pink);

	    }  

	    else if (selection == itemCount - 5) {

	        changeFillColor (Color.orange);

	    }  

	    else if (selection == itemCount - 6) {

	        changeFillColor (Color.magenta);

	    }  

	    else if (selection == itemCount - 7) {

	        changeFillColor (Color.green);

	    }  

	    else if (selection == itemCount - 8) {

	        changeFillColor (Color.cyan);

	    }  

	    else if (selection == itemCount - 9) {

	        changeFillColor (Color.blue);

	    }  

	    else if (selection == itemCount - 10) {

	        changeFillColor (Color.black);

	    }  

	    else if (selection == itemCount - 11) {

	        changeFillColor (Color.darkGray);

	    }    

	    else if (selection == itemCount - 12) {

	        changeFillColor (Color.gray);

	    }  

	    else if (selection == itemCount - 13) {

	        changeFillColor (Color.lightGray);

	    }  

	    else if (selection == itemCount - 14) {

	        changeFillColor (Color.white);

	    }

	    else {

	    }

	}
    };

    // When the outline color combo box is chosen.

    ActionListener outlineAction = new ActionListener () {

        public void actionPerformed (ActionEvent e) {

	    int selection = _outlineComboBox.getSelectedIndex ();

	    int itemCount = _outlineComboBox.getItemCount ();

	    if (selection == itemCount - 1) {

	        _changingFill = false;

	        _dialog = JColorChooser.createDialog 
		    (_canvas, "Choose An Outline Color", true, 
		     _colorChooser, okAction, cancelAction);

		_dialog.setVisible (true);

	    }

	    else if (selection == itemCount - 2) {

	        changeOutlineColor (Color.yellow);

	    }

	    else if (selection == itemCount - 3) {

	        changeOutlineColor (Color.red);

	    }

	    else if (selection == itemCount - 4) {

	        changeOutlineColor (Color.pink);

	    }  

	    else if (selection == itemCount - 5) {

	        changeOutlineColor (Color.orange);

	    }  

	    else if (selection == itemCount - 6) {

	        changeOutlineColor (Color.magenta);

	    }  

	    else if (selection == itemCount - 7) {

	        changeOutlineColor (Color.green);

	    }  

	    else if (selection == itemCount - 8) {

	        changeOutlineColor (Color.cyan);

	    }  

	    else if (selection == itemCount - 9) {

	        changeOutlineColor (Color.blue);

	    }  

	    else if (selection == itemCount - 10) {

	        changeOutlineColor (Color.black);

	    }  

	    else if (selection == itemCount - 11) {

	        changeOutlineColor (Color.darkGray);

	    }    

	    else if (selection == itemCount - 12) {

	        changeOutlineColor (Color.gray);

	    }  

	    else if (selection == itemCount - 13) {

	        changeOutlineColor (Color.lightGray);

	    }  

	    else if (selection == itemCount - 14) {

	        changeOutlineColor (Color.white);

	    }

	    else {

	    }

	}
    };

    // When the thinner button is pressed.

    Action thinnerAction = new AbstractAction ("Thinner") {

        public void actionPerformed (ActionEvent e) {

	    Iterator iter = _m.getSelection ();

	    if (iter.hasNext ()) {

	        VersatileFigure v = null;

		while (iter.hasNext ()) {

		    v = (VersatileFigure) iter.next ();

		    v.setLineWidth (v.getLineWidth () - 1.0f);

		}

		_outlineThickness = v.getLineWidth ();

	    }

	    else {

	        getToolkit ().beep ();

	    }

	}
    };
	
    // When the thicker button is pressed.

    Action thickerAction = new AbstractAction ("Thicker") {

        public void actionPerformed (ActionEvent e) {

	    Iterator iter = _m.getSelection ();

	    if (iter.hasNext ()) {

	        VersatileFigure v = null;

		while (iter.hasNext ()) {

		    v = (VersatileFigure) iter.next ();

		    v.setLineWidth (v.getLineWidth () + 1.0f);

		}

		_outlineThickness = v.getLineWidth ();

	    }

	    else {

	        getToolkit ().beep ();

	    }

	}
    };

    // When you click ok in the color window.

    Action okAction = new AbstractAction ("Ok") {

        public void actionPerformed (ActionEvent e) {

	    Color thisColor = _colorChooser.getColor ();

	    if (_changingFill) {

	        _fillColor = thisColor;

	    }

	    else {

	        _strokeColor = thisColor;

	    }

	    Iterator iter = _m.getSelection ();

	    if (iter.hasNext ()) {

	        while (iter.hasNext ()) {

		    VersatileFigure v = (VersatileFigure) iter.next ();

		    if (_changingFill) {

		        v.setFillPaint (_fillColor);

		    }

		    else {

		        v.setStrokePaint (_strokeColor);

		    }

		}

	    }

	    else {

	        getToolkit ().beep ();

	    }

    	}
    };

    // When you click cancel in the color window.

    Action cancelAction = new AbstractAction ("Cancel") {

        public void actionPerformed (ActionEvent e) {

	}
    };

    // When you click cut in the edit menu of the menubar.
    // The cut operation grabs the system clipboard, then puts 
    // the currently selected item onto the clipboard, and removes 
    // the currently selected item from the canvas.

    Action cutAction = new AbstractAction ("Cut    CTRL+X") {

        public void actionPerformed (ActionEvent e) {

	    Iterator iter = _m.getSelection ();

	    Vector vector = new Vector ();

	    if (iter.hasNext ()) {

	        while (iter.hasNext ()) {

		    _currentFigure = (VersatileFigure) iter.next ();

		    iter.remove ();

		    _m.removeSelection (_currentFigure);

		    _layer.remove (_currentFigure);

		    vector.add (_currentFigure);

		}

		Clipboard c = getToolkit ().getSystemClipboard ();

		SimpleSelection s = new SimpleSelection (vector, 
							 dataFlavor);

		c.setContents(s, s);

	    }

	    else {

	        getToolkit ().beep ();

	    }

	}
    };

    // When you click copy in the edit menu of the menubar.
    // The copy operation grabs the system clipboard, then puts
    // the currently selected item onto the clipboard.

    Action copyAction = new AbstractAction ("Copy   CTRL+C") {

        public void actionPerformed (ActionEvent e) {

	    Iterator iter = _m.getSelection ();

	    Vector v = new Vector ();

	    if (iter.hasNext ()) {

	        while (iter.hasNext ()) {

		    _currentFigure = (VersatileFigure) iter.next ();

		    v.add (_currentFigure);

		}

		Clipboard c = getToolkit ().getSystemClipboard ();

		SimpleSelection s = new SimpleSelection (v, 
							 dataFlavor);

		c.setContents(s, s);

	    }

	    else {

	        getToolkit ().beep ();

	    }

	}
    };

    // When you click paste in the edit menu of the menubar.
    // The paste operation grabs the system clipboard, then gets the 
    // current data object on the clipboard, makes a copy of it, 
    // and adds it to the figure layer of the canvas.
    // If something goes wrong, the machine should beep.

    Action pasteAction = new AbstractAction ("Paste  CTRL+V") {

        public void actionPerformed (ActionEvent e) {

	    _m.clearSelection ();

	    Clipboard c = getToolkit ().getSystemClipboard ();

	    Transferable t = c.getContents (this);

	    if (t == null) {

	        getToolkit ().beep ();

		return;

	    }

	    try {

	        Vector v = (Vector) t.getTransferData (dataFlavor);

		Enumeration enum = v.elements ();

		if (enum.hasMoreElements ()) {

		    while (enum.hasMoreElements ()) {

		        VersatileFigure vf = (VersatileFigure) enum.nextElement ();

			VersatileFigure vf2 = (VersatileFigure) vf.clone ();

			_layer.add (vf2);

			if (vf2.getInteractor () instanceof SelectionInteractor) {

			  _selectionDragger.expandSelection

			      ((SelectionInteractor) vf2.getInteractor (), vf2);

			} 

		    }

		    repaint ();

		}

		else {

		    getToolkit ().beep ();

		}

	    }

	    catch (UnsupportedFlavorException ufe) {

	        getToolkit ().beep ();

	    }

	    catch (Exception ex) {

	        getToolkit ().beep ();

	    }

	}
    };
	
    // When you click new in the file menu of the menubar.

    Action newIconAction = new AbstractAction ("New    CTRL+N") {

        public void actionPerformed (ActionEvent e) {

	    clear ();

	}
    };

    // When you click open in the file menu of the menubar.

    Action openIconAction = new AbstractAction ("Open   CTRL+O") {

        public void actionPerformed (ActionEvent e) {

	    int choice = 
	    _fileChooser.showOpenDialog (_context.makeComponent());
	    
	    if (choice == JFileChooser.CANCEL_OPTION) {

	        //System.out.println ("You have cancelled your open file choice");

	    }

	    else {

	        //System.out.println ("You have chosen to open this file: " + 
	        //		    _fileChooser.getSelectedFile ().getName ());

	        clear ();

		//FIXME: Here is where I would import an xml file to this 
	        //canvas.

	    }

	}
    };

    // When you click save in the file menu of the menubar.

    Action saveIconAction = new AbstractAction ("Save   CTRL+S") {

        public void actionPerformed (ActionEvent e) {

	    System.out.println ("Save");

	    Iterator iter = _layer.figures ();

	    while (iter.hasNext ()) {

	        VersatileFigure nextFigure = (VersatileFigure) iter.next ();

		// FIXME: Now I should be changing the XMlIcon icon.

	    }

	}
    };

    // When you click save as in the file menu of the menubar.

    Action saveIconAsAction = new AbstractAction ("Save As...") {

        public void actionPerformed (ActionEvent e) {

	    int choice = 
	    _fileChooser.showSaveDialog (_context.makeComponent());
	    
	    if (choice == JFileChooser.CANCEL_OPTION) {

	        //System.out.println ("You have cancelled your 
	        //                     save choice.");

	    } 

	    else {

	      //System.out.println ("You chose to save this file: " + 
	      //		    _fileChooser.getSelectedFile ().getName ());

	    }

	}
    };

  Action printIconAction = new AbstractAction ("Print  CTRL+P") {

      public void actionPerformed (ActionEvent e) {

	  print ();

      }
  };
    
    // When you click exit in the file menu of the menubar.

    Action exitIconAction = new AbstractAction ("Exit") {

        public void actionPerformed (ActionEvent e) {

	    _context.setVisible(false);

	}
    };

    // When you click about in the help menu of the menubar.

    Action helpAction = new AbstractAction ("About") {

        public void actionPerformed (ActionEvent e) {

	    _helpFrame = new JFrame ("About Icon Editor");

	    JButton jButton = new JButton 
	        ("Author: Nick Zamora, Last Edited: August 1, 2000");

	    jButton.addActionListener (helpOkAction);

	    _helpFrame.getContentPane ().add (jButton);

	    _helpFrame.getContentPane ().doLayout ();

	    _helpFrame.setResizable (false);

	    _helpFrame.setLocation (100, 100);

	    _helpFrame.setSize (500, 150);

	    _helpFrame.setVisible (true);

	}
    };

    // When you click the button in the about window.

    Action helpOkAction = new AbstractAction ("OK Button") {

        public void actionPerformed (ActionEvent e) {

	    _helpFrame.setVisible (false);

	}
    };

    // When the thickness combo box is chosen.

    ActionListener thicknessAction = new ActionListener () {

        public void actionPerformed (ActionEvent e) {

	    int chosenThickness = _thicknessComboBox.getSelectedIndex ();

	    switch (chosenThickness) {

	    case 0:
	        changeThickness (1.0f);

		break;

	    case 1:
	        changeThickness (3.0f);

		break;

	    case 2:
	        changeThickness (6.0f);

		break;

	    case 3:
		changeThickness (10.0f);

		break;

	    case 4:
		changeThickness (14.0f);

		break;

	    default:
	        break;

	    }

	}
    };
    
	    
    // Listen for the delete key from the keyboard.  When the delete key is 
    // pressed, the currently selected figure is removed from the _layer and 
    // unselected from the selection model.  Pressing the delete key is unlike 
    // the cut command from the edit menu in the toolbar in that the delete 
    // command is irreversible.  "Paste" will NOT return a figure that has 
    // been deleted from the canvas.

    ActionListener deletionListener = new ActionListener () {

        public void actionPerformed (ActionEvent evt) {

	    Iterator iter = _m.getSelection ();

	    VersatileFigure v = null;

	    if (iter.hasNext ()) {

	        while (iter.hasNext ()) {

		    v = (VersatileFigure) iter.next ();

		    iter.remove ();

		    _m.removeSelection (v);

		    _layer.remove (v);

		}

	    }

	    else {

	      getToolkit ().beep ();

	    }

	}
    };


    //////////////////////////////////////////////////////////////////////
    //////////////////      Private methods.            /////////////////

    // A private method to change the thickness of the selected figure(s).
    //Parameter float newThickness The new thickness for the selected figure(s).

    private void changeThickness (float newThickness) {

        _outlineThickness = newThickness;

	Iterator iter = _m.getSelection ();

	if (iter.hasNext ()) {

	    while (iter.hasNext ()) {

	        VersatileFigure v = (VersatileFigure) iter.next ();

		v.setLineWidth (newThickness);

	    }

	}

	else {

	    getToolkit ().beep ();

	}

    }

    // A private method to change the outline color of the selected figure(s).
    // Parameter Color c The new color for the selected figure(s).

    private void changeOutlineColor (Color c) {

        Iterator iter = _m.getSelection ();

	if (iter.hasNext ()) {

	    while (iter.hasNext ()) {

	        VersatileFigure v = (VersatileFigure) iter.next ();

		v.setStrokePaint (c);

	    }

	}

	else {

	    getToolkit ().beep ();

	}

	_strokeColor = c;

    }

    // A private method to change the fill color of the selected figure(s).
    // Parameter Color c The new color for the selected figure(s).

    private void changeFillColor (Color c) {

        Iterator iter = _m.getSelection ();

	if (iter.hasNext ()) {

	    while (iter.hasNext ()) {

	        VersatileFigure v = (VersatileFigure) iter.next ();

		v.setFillPaint (c);

	    }

	}

	else {

	    getToolkit ().beep ();

	}

	_fillColor = c;

    }

    //////////////////////////////////////////////////////////////////////
    //////////////////      Inner Classes.              /////////////////  

    //////////////////////////////////////////////////////////////////////
    /////////////////      Public Methods               /////////////////

    public void print () {

	PrinterJob job = PrinterJob.getPrinterJob ();

	job.setPrintable (_canvas);

	_selectionDragger.clearSelection ();

	if (job.printDialog ()) {

	    try {

	        job.print ();

	    }

	    catch (Exception ex) {

	        JOptionPane.showMessageDialog (this, 

		"Printing failed:\n" + ex.toString (), 

		"Print Error", JOptionPane.WARNING_MESSAGE);

	    }

	}

    }

    public void clear () {

        _m.clearSelection ();

	_layer.clear ();
	
	Enumeration enum = _icon.graphicElements ();
	
	while (enum.hasMoreElements ()) {
	    
	    GraphicElement element = (GraphicElement) enum.nextElement ();

	    try {
	    
	        _icon.removeGraphicElement (element);
	    }
	    
	    catch (IllegalActionException ex) {

	        System.out.println 
		    ("Illegal Action Exception\n " + ex.toString ());

	    }

	}
    }

    public AppContext getAppContext () {

        return _context;

    }

    // Fetch the icon

    public XMLIcon getXMLIcon () {

        return _icon;

    }

    // Assign the title of the context.

    public void setTitle (String title) {

        _context.setTitle (title);

    }

    // Fetch the title of the context.

    public String getTitle () {

        return _context.getTitle ();

    }

    // Makes the icon editing window visible on the screen.

    public void showEditorDialog () {

        // Only set the window visible now.

        _context.setVisible (true);

    }


    // I need to setup the icon that was passed in, possibly from 
    // another application.  I enumerate over the graphical elements 
    // contained within the XMLIcon and, for each element, I create a 
    // versatile figure from it and add it to the FigureLayer.  Also, 
    // I set up an appropriate interactor for each figure so that the 
    // figure can be edited.
    //
    // Note that this icon being added adds its graphic elements to 
    // the _icon member associated with this instance.

    public void addXMLIcon (XMLIcon my_icon) {

        // First get an enumeration over all the elements in the icon.

        Enumeration enum = my_icon.graphicElements ();

	// And as long as the icon has more elements...

	while (enum.hasMoreElements ()) {

	    // Get the next graphic element.

	    GraphicElement nextGraphic = (GraphicElement) enum.nextElement ();

	    // Add this graphic element to this icon.

	    //try {

	    //  _icon.addGraphicElement (nextGraphic);
	    
	    //}
	    
	    //catch (IllegalActionException ex) {
	        
	    //  System.out.println 
	    //  ("Illegal Action Exception\n " + ex.toString ());

	    //}

	    // Create a new figure represented by this graphic element.

	    VersatileFigure versatileFigure = new VersatileFigure 
	        (nextGraphic.getPaintedObject());

	    // Get the shape of this figure.

	    Shape nextShape = versatileFigure.getShape ();

	    // Now assign an appropriate interactor for the shape.

	    if (nextShape instanceof RectangularShape) {

	        versatileFigure.setInteractor (_interactor2);

	    }

	    else if (nextShape instanceof Ellipse2D) {

	        versatileFigure.setInteractor (_interactor3);

	    }

	    else if (nextShape instanceof GeneralPath) {

	        versatileFigure.setInteractor (_interactor1);

	    }

	    else if (nextShape instanceof Polygon2D) {

		versatileFigure.setInteractor (_interactor2);

	    }

      	    // Finally, add the figure to the figure layer.

	    _layer.add (versatileFigure);

	}

    }

}

