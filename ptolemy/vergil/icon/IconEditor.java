/*
 * Copyright (c) 1998-2000 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package ptolemy.vergil.icon;

// Diva imports.
import diva.gui.BasicFrame;
import diva.gui.AppContext;
import diva.gui.GUIUtilities;
import diva.gui.ExtensionFileFilter;
import diva.util.java2d.PaintedShape;
import diva.util.java2d.PaintedPath;
import diva.util.java2d.PaintedString;
import diva.util.java2d.PaintedList;
import diva.util.java2d.PaintedObject;
import diva.util.java2d.Polygon2D;
import diva.canvas.JCanvas;
import diva.canvas.GraphicsPane;
import diva.canvas.FigureLayer;
import diva.canvas.interactor.DragInteractor;
import diva.canvas.interactor.SelectionInteractor;
import diva.canvas.interactor.PathManipulator;
import diva.canvas.interactor.BoundsManipulator;
import diva.canvas.interactor.CircleManipulator;
import diva.canvas.interactor.BasicSelectionModel;
import diva.canvas.toolbox.VersatileFigure;

// Javax imports.
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPopupMenu;

// Java imports.
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Component;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;


// Ptolemy imports.
import ptolemy.vergil.toolbox.XMLIcon;
import ptolemy.vergil.toolbox.GraphicElement;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

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
        // window will include a toolbar of different shapes, a toolbar 
        // of different thicknesses, a button to choose the color, 
        // and the main drawing window.
        // For context, I used a BasicFrame with a false argument, 
        // which tells BasicFrame not to set the size of the window or 
        // make it visible.  The string "Edit Icon" is the name of the 
        // window.

        AppContext context = new BasicFrame ("Edit Icon", false);

	// Make a new instance of the IconEditor class.
	new IconEditor(context);
    }
  
    public IconEditor (AppContext context)
        throws NameDuplicationException, IllegalActionException {
        this (context, new XMLIcon (new NamedObj(), "icon"));
    }

    public IconEditor (AppContext context, XMLIcon icon) {

	_context = context;

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
	// New, Open, and Save keyboard shortcuts are registered.
	_canvas.registerKeyboardAction (newIconAction, "New Icon", 
					KeyStroke.getKeyStroke (KeyEvent.VK_N, 2),
					JComponent.WHEN_IN_FOCUSED_WINDOW);
	_canvas.registerKeyboardAction (openIconAction, "Open Icon", 
					KeyStroke.getKeyStroke (KeyEvent.VK_O, 2),
					JComponent.WHEN_IN_FOCUSED_WINDOW);
	_canvas.registerKeyboardAction (saveIconAction, "Save Icon", 
					KeyStroke.getKeyStroke (KeyEvent.VK_S, 2),
					JComponent.WHEN_IN_FOCUSED_WINDOW);
	_canvas.setRequestFocusEnabled (true);


	// Make a toolbar for the color, thicknesses, and shapes windows and 
	// add them to the main _context frame.

	JToolBar thicknessToolBar = new JToolBar (JToolBar.VERTICAL);
	_context.getContentPane ().add ("East", thicknessToolBar);
	JToolBar shapesToolBar = new JToolBar (JToolBar.HORIZONTAL);
	_context.getContentPane ().add ("North", shapesToolBar);
	_colorButton = new JButton ("Colors");
	_colorButton.addActionListener (colorAction);
	_colorButton.setBackground ( (Color) _strokeColor);
	_context.getContentPane ().add ("West", _colorButton);
	

	// When you exit the program, here is what happens.

	_context.setExitAction(exitIconAction);

	// Add "New", "Open", "Save", "Save As", and "Exit" to the "File"
	// menu.

	_context.setJMenuBar(new JMenuBar());
        _menuFile = new JMenu("File");
        _menuFile.setMnemonic('F');
	_itemNew = _menuFile.add (newIconAction);
	_itemNew.setMnemonic ('N');
	_itemNew.setToolTipText ("Create a new icon and discard this one");
	_itemOpen = _menuFile.add (openIconAction);
	_itemOpen.setMnemonic ('O');
	_itemOpen.setToolTipText ("Open an icon from a file");
	_itemSave = _menuFile.add (saveIconAction);
	_itemSave.setMnemonic ('S');
	_itemSave.setToolTipText ("Save this icon");
	_itemSaveAs = _menuFile.add (saveIconAsAction);
	_itemSaveAs.setMnemonic ('A');
	_itemSaveAs.setToolTipText ("Save as ...");
        _itemQuit = _menuFile.add(exitIconAction);
        _itemQuit.setMnemonic('E');
        _itemQuit.setToolTipText("Exit this application");
        _context.getJMenuBar().add(_menuFile);


	// Create an edit menu and add "Cut", "Copy", and "Paste" functions 
	// to that menu.

	JMenuBar menuBar = _context.getJMenuBar ();
	JMenu menuEdit = new JMenu ("Edit");
	menuEdit.setMnemonic ('E');
	menuBar.add (menuEdit);

	GUIUtilities.addMenuItem (menuEdit, cutAction, 'C', 
				 "Cut the selected shape");
	GUIUtilities.addMenuItem (menuEdit, copyAction, 'O', 
				 "Copy the selected shape");
	GUIUtilities.addMenuItem (menuEdit, pasteAction, 'P',
				 "Paste the shape previously cut or copied");

	// Create a help menu and add "About" to that menu.

	JMenu menuHelp = new JMenu ("Help");
	menuHelp.setMnemonic ('H');
	menuBar.add (menuHelp);
	
	GUIUtilities.addMenuItem (menuHelp, helpAction, 'A', 
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
	URL Fill = getClass().getResource("gifs/fill.gif");
	URL Stroke = getClass().getResource("gifs/stroke.gif");
	URL thickness1 = getClass().getResource("gifs/thickness1.gif");
	URL thickness2 = getClass().getResource("gifs/thickness2.gif");
	URL thickness3 = getClass().getResource("gifs/thickness3.gif");
	URL thickness4 = getClass().getResource("gifs/thickness4.gif");
	URL thickness5 = getClass().getResource("gifs/thickness5.gif");
	URL thinner = getClass().getResource("gifs/thinner.gif");
	URL thicker = getClass().getResource("gifs/thicker.gif");

	// Now that I have the names of all the gif files, I need to make them 
	// buttons and add them  to the appropriate toolbars in the main window.

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
	GUIUtilities.addToolBarButton (shapesToolBar, fillAction, 
				       "Fill shape with selected color", 
				       new ImageIcon(Fill));
	GUIUtilities.addToolBarButton (shapesToolBar, strokeAction, 
				       "Fill outline with selected color", 
				       new ImageIcon(Stroke));
	GUIUtilities.addToolBarButton (thicknessToolBar, thickness1Action,
				       "Thickness of Outline", 
				       new ImageIcon(thickness1));
	GUIUtilities.addToolBarButton (thicknessToolBar, thickness2Action,
				       "Thickness of Outline", 
				       new ImageIcon(thickness2));
	GUIUtilities.addToolBarButton (thicknessToolBar, thickness3Action,
				       "Thickness of Outline", 
				       new ImageIcon(thickness3));
	GUIUtilities.addToolBarButton (thicknessToolBar, thickness4Action,
				       "Thickness of Outline", 
				       new ImageIcon(thickness4));
	GUIUtilities.addToolBarButton (thicknessToolBar, thickness5Action,
				       "Thickness of Outline", 
				       new ImageIcon(thickness5));
	GUIUtilities.addToolBarButton (thicknessToolBar, thinnerAction,
				       "Thinner Outline", 
				       new ImageIcon(thinner));
	GUIUtilities.addToolBarButton (thicknessToolBar, thickerAction,
				       "Thicker Outline", 
				       new ImageIcon(thicker));

	// Now we need to get the canvas pane and foreground layer of our 
	// canvas.  Also, here is where I set the halo width (twice the 
	// distance your mouse can be from a figure when you click and still 
	// have the program detect that you are trying to click on the figure).

	_pane = (GraphicsPane) _canvas.getCanvasPane ();
	_layer = _pane.getForegroundLayer ();
	_layer.setPickHalo (MOUSE_SENSITIVITY);
	
	// I have to make the figures drag-able.

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


	// Finally, I need to setup the icon that was passed in, possibly from 
	// another application.  I enumerate over the graphical elements 
	// contained within the XMLIcon and, for each element, I create a 
	// painted figure from it and add it to the FigureLayer.  Also, I set
	// up an appropriate interactor for each figure added so it can be
	// edited.

        Enumeration enum = icon.graphicElements ();
	while (enum.hasMoreElements ()) {
	    GraphicElement nextGraphic = (GraphicElement) enum.nextElement ();
	    VersatileFigure versatileFigure = new VersatileFigure 
	        (nextGraphic.getPaintedObject());
	    Shape nextShape = versatileFigure.getShape ();
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
	    _layer.add (versatileFigure);
	}

	// Sets the size of the main window in pixels.
	_context.setSize (WINDOW_SIZE_HORIZONTAL, WINDOW_SIZE_VERTICAL);

	// Only set the window visible now so the user doesn't see it being 
	// constructed.
	_context.setVisible (true);

    }


    //         StringBufferInputStream xml_stream = null;
    //         xml_stream = new StringBufferInputStream ("<xmlgraphic> <rectangle coords=\"0 0 60 40\" fill=\"white\"/> <polygon coords=\"10 10 50 20 10 30\" fill=\"blue\"/> </xmlgraphic>\n");	       


    ////////////////////////////////////////////////////////////////////////
    ///////////////////      Private variables.       /////////////////////


    // The context of the icon editor application.

    private AppContext _context;

    // The menu bar (contains "File" and "Edit" submenus).

    private JMenu _menuFile;

    // The quit item in the "File" menu.

    private JMenuItem _itemNew;
    private JMenuItem _itemOpen;
    private JMenuItem _itemSave;
    private JMenuItem _itemSaveAs;
    private JMenuItem _itemQuit;

    // Create the file chooser for the "Open" and "Save As" commands.

    private JFileChooser _fileChooser = new JFileChooser ();
    private ExtensionFileFilter _filter = new ExtensionFileFilter ("xml");

    // Create the object for the color chooser.

    private JButton _colorButton;
    private JColorChooser _colorChooser;

    // Here are the interactors for each shape

    private BasicSelectionModel _m = new BasicSelectionModel ();
    private SelectionInteractor _interactor1 = new SelectionInteractor (_m);
    private SelectionInteractor _interactor2 = new SelectionInteractor (_m);
    private SelectionInteractor _interactor3 = new SelectionInteractor (_m);
  
    // This is the current shape, line thickness, and paint color.

    private VersatileFigure _currentFigure = null;
    private float _outlineThickness = 1.0f;
    private Paint _strokeColor = new Color (0, 170, 170);

    // Here is the figure kept in memory for the "cut" or 
    // "pasted" figure.

    private VersatileFigure _cutOrCopiedFigure = null;

    // Window objects

    private GraphicsPane _pane;
    private FigureLayer _layer;
    private JCanvas _canvas;
    private JDialog _dialog;

    // For the help option in the menubar.

    private JFrame _helpFrame;

    // Constants for the program.  Decreasing MOUSE_SENSITIVITY will require 
    // the user to be more precise when trying to click on figures.

    private static final double MOUSE_SENSITIVITY = 3.0;

    // Defines the horizontal and vertical size of the main window.

    private static final int WINDOW_SIZE_HORIZONTAL = 500;
    private static final int WINDOW_SIZE_VERTICAL = 605;

    // Alternatively, the user can be allowed to resize the window if desired.  
    // Now I see no need to allow this option for the user, but it might be 
    // needed as the program is used in different applications.

    private static final boolean WINDOW_RESIZABLE = false;

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

    Action rectangleAction = new AbstractAction ("Rectangle") {
        public void actionPerformed (ActionEvent e) {
	    _currentFigure = new VersatileFigure 
	        (new PaintedShape (new Rectangle2D.Double
				   (8.0, 10.0, 20.0, 20.0), 
				   _outlineThickness, _strokeColor));
	    _layer.add (_currentFigure);
	    _currentFigure.setInteractor (_interactor2);
	}
    };

    // When the straight line button is pressed.

    Action lineAction = new AbstractAction ("Line") {
        public void actionPerformed (ActionEvent e) {
	    _currentFigure = new VersatileFigure 
	        (new PaintedShape (new Line2D.Double
				   (45.0, 10.0, 65.0, 30.0), 
				   _outlineThickness, _strokeColor));
	    _layer.add (_currentFigure);
	    _currentFigure.setInteractor (_interactor1);
	}
    };

    // When the quadratic curve button is pressed.

    Action quadraticAction = new AbstractAction ("Quadratic Curve") {
        public void actionPerformed (ActionEvent e) {
	    _currentFigure = new VersatileFigure 
	        (new PaintedShape (new QuadCurve2D.Double
				   (77.0, 10.0, 87.0, 20.0, 97.0, 30.0), 
				   _outlineThickness, _strokeColor));
	    _layer.add (_currentFigure);
	    _currentFigure.setInteractor (_interactor1);
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
	    _layer.add (_currentFigure);
	    _currentFigure.setInteractor (_interactor1);
	}
    };

    // When the circle button is pressed.

    Action circleAction = new AbstractAction ("Circle") {
        public void actionPerformed (ActionEvent e) {
	    _currentFigure = new VersatileFigure 
	        (new PaintedShape (new Ellipse2D.Double
				   (148.0, 10.0, 20.0, 20.0), 
				   _outlineThickness, _strokeColor)); 
	    _layer.add (_currentFigure);
	    _currentFigure.setInteractor (_interactor3);
	}
    };

    // When the ellipse button is pressed.

    Action ellipseAction = new AbstractAction ("Ellipse") {
        public void actionPerformed (ActionEvent e) {
	    _currentFigure = new VersatileFigure 
	        (new PaintedShape (new Ellipse2D.Double
				   (183.0, 10.0, 20.0, 30.0), 
				   _outlineThickness, _strokeColor)); 
	    _layer.add (_currentFigure);
	    _currentFigure.setInteractor (_interactor2);
	}
    };
  
    // When the fill shape button is pressed.

    Action fillAction = new AbstractAction ("Fill") {
        public void actionPerformed (ActionEvent e) {
	    VersatileFigure versatileFigure = (VersatileFigure) _m.getFirstSelection ();
	    if (versatileFigure != null) {
	        versatileFigure.setFillPaint (_strokeColor);
	    }
	    else {
	        getToolkit ().beep ();
	    }
	}
    };

    // When the paint outline button is pressed.

    Action strokeAction = new AbstractAction ("Stroke") {
        public void actionPerformed (ActionEvent e) {
	    VersatileFigure versatileFigure = (VersatileFigure) _m.getFirstSelection ();
	    if (versatileFigure != null) {
	        versatileFigure.setStrokePaint (_strokeColor);
	    }
	    else {
	        getToolkit ().beep ();
	    }
	}
    };

    // When the first thickness button is pressed.

    Action thickness1Action = new AbstractAction ("Thickness 1") {
        public void actionPerformed (ActionEvent e) {
	    changeThickness (1.0f);
	}
    };

    // When the second thickness button is pressed.

    Action thickness2Action = new AbstractAction ("Thickness 2") {
        public void actionPerformed (ActionEvent e) {
	    changeThickness (3.0f);
	}
    };

    // When the third thickness button is pressed.

    Action thickness3Action = new AbstractAction ("Thickness 3") {
        public void actionPerformed (ActionEvent e) {
	    changeThickness (6.0f);
	}
    };

    // When the fourth thickness button is pressed.

    Action thickness4Action = new AbstractAction ("Thickness 4") {
        public void actionPerformed (ActionEvent e) {
	    changeThickness (10.0f);
	}
    };

    // When the fifth thickness button is pressed.

    Action thickness5Action = new AbstractAction ("Thickness 5") {
        public void actionPerformed (ActionEvent e) {
	    changeThickness (14.0f);
	}
    };

    // When the thinner button is pressed.

    Action thinnerAction = new AbstractAction ("Thinner") {
        public void actionPerformed (ActionEvent e) {
	    VersatileFigure v = (VersatileFigure)_m.getFirstSelection ();
	    if (v != null) {
	        v.setLineWidth (v.getLineWidth () - 1.0f);
	    }
	    else {
	        getToolkit ().beep ();
	    }
	}
    };

    // When the thicker button is pressed.

    Action thickerAction = new AbstractAction ("Thicker") {
        public void actionPerformed (ActionEvent e) {
	    VersatileFigure v = (VersatileFigure)_m.getFirstSelection ();
	    if (v != null) {
	        v.setLineWidth (v.getLineWidth () + 1.0f);
	    }
	    else {
	        getToolkit ().beep ();
	    }
	}
    };

    // When the large color button is pressed.

    Action colorAction = new AbstractAction ("Color") {
        public void actionPerformed (ActionEvent e) {
	    _dialog = JColorChooser.createDialog 
	        (_canvas, "Choose A Color", true, 
		 _colorChooser, okAction, cancelAction);
	    _dialog.setVisible (true);
	}
    };

    // When you click ok in the color window.

    Action okAction = new AbstractAction ("Ok") {
        public void actionPerformed (ActionEvent e) {
	    Color thisColor = _colorChooser.getColor ();
	    _colorButton.setBackground (thisColor);
	    _colorButton.repaint ();
	    _strokeColor = thisColor;
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
	    if (_m.getFirstSelection() != null) {
	        _currentFigure = (VersatileFigure) _m.getFirstSelection ();
		_m.clearSelection ();
		_layer.remove (_currentFigure);
		Clipboard c = getToolkit ().getSystemClipboard ();
		SimpleSelection s = new SimpleSelection (_currentFigure, 
							 dataFlavor);
		c.setContents (s, s);
		_layer.remove (_currentFigure);
	    }
	}
    };

    // When you click copy in the edit menu of the menubar.
    // The copy operation grabs the system clipboard, then puts
    // the currently selected item onto the clipboard.

    Action copyAction = new AbstractAction ("Copy   CTRL+C") {
        public void actionPerformed (ActionEvent e) {
	    if (_m.getFirstSelection() != null) {
	        _currentFigure = (VersatileFigure) _m.getFirstSelection ();
		if (_currentFigure != null) {
		    Clipboard c = getToolkit ().getSystemClipboard ();
		    SimpleSelection s = new SimpleSelection (_currentFigure, 
							     dataFlavor);
		    c.setContents (s, s);
		}
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
	    Clipboard c = getToolkit ().getSystemClipboard ();
	    Transferable t = c.getContents (this);
	    if (t == null) {
	        getToolkit ().beep ();
		return;
	    }
	    try {
	        VersatileFigure v = (VersatileFigure) t.getTransferData (dataFlavor);
		_layer.add ((VersatileFigure) v.clone ());
		repaint ();
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
	    _m.clearSelection();
	    _layer.clear();
	}
    };

    // When you click open in the file menu of the menubar.

    Action openIconAction = new AbstractAction ("Open   CTRL+O") {
        public void actionPerformed (ActionEvent e) {
	    int choice = 
	    _fileChooser.showOpenDialog (_context.makeComponent());
	    
	    if (choice == JFileChooser.CANCEL_OPTION) {
	        System.out.println ("You have cancelled your open file choice");
	    }
	    else {
	        System.out.println ("You chosen to open this file: " + 
				    _fileChooser.getSelectedFile ().getName ());
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
	    }
	}
    };

    // When you click save as in the file menu of the menubar.

    Action saveIconAsAction = new AbstractAction ("Save As...") {
        public void actionPerformed (ActionEvent e) {
	    int choice = 
	    _fileChooser.showSaveDialog (_context.makeComponent());
	    
	    if (choice == JFileChooser.CANCEL_OPTION) {
	      //System.out.println ("You have cancelled your save file as choice");
	    } 
	    else {
	      
	      //System.out.println ("You chose to save this file: " + 
	      //		    _fileChooser.getSelectedFile ().getName ());
	    }
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
	    JButton jButton = new JButton ("Author: Nick Zamora, Last Edited: August 1, 2000");
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
	    
    // Listen for the delete key from the keyboard.  When the delete key is 
    // pressed, the currently selected figure is removed from the _layer and 
    // unselected from the selection model.  Pressing the delete key is unlike 
    // the cut command from the edit menu in the toolbar in that the delete 
    // command is irreversible.  "Paste" will NOT return a figure that has 
    // been deleted from the canvas.

    ActionListener deletionListener = new ActionListener () {
        public void actionPerformed (ActionEvent evt) {
	    VersatileFigure versatileFigure = (VersatileFigure) _m.getFirstSelection ();
	    if (versatileFigure != null) {
	        _m.clearSelection ();
		_layer.remove (versatileFigure);
	    }
	    else {
	        getToolkit ().beep ();
	    }
	}
    };


    //////////////////////////////////////////////////////////////////////
    //////////////////      Private methods.            /////////////////

    // A private method to change the thickness of a selected figure.
    //Parameter float newThickness The new thickness for the selected figure.

    private void changeThickness (float newThickness) {
        _outlineThickness = newThickness;
	VersatileFigure versatileFigure = (VersatileFigure) _m.getFirstSelection ();
	if (versatileFigure != null) {
	    versatileFigure.setLineWidth(newThickness);
	}
	else {
	    getToolkit ().beep ();
	}
    }

    // A private method to change the currently remembered shape for the "cut", 
    // "copy" and "paste" functions.  This method creates a new VersatileFigure 
    // from the current one recently "cut", "copied", or "pasted".  
    //

    private void makeNewCutOrCopiedFigure () {
	_cutOrCopiedFigure = new VersatileFigure
	    (_cutOrCopiedFigure);
	_cutOrCopiedFigure.setInteractor (_currentFigure.getInteractor ());
    }


    //////////////////////////////////////////////////////////////////////
    //////////////////      Inner classes.              /////////////////  

    // SimpleSelection.  The class used to keep track of the clipboard 
    // contents and the type of data being stored.  

    static class SimpleSelection implements Transferable, ClipboardOwner {
        protected Object _selection;
        protected DataFlavor _flavor;
        public SimpleSelection (Object selection, DataFlavor flavor) {
	    _selection = selection;
	    _flavor = flavor;
	}
        public DataFlavor[] getTransferDataFlavors () {
	    return new DataFlavor[] { _flavor };
	}
        public boolean isDataFlavorSupported (DataFlavor f) {
	    return f.equals(_flavor);
	}
        public Object getTransferData (DataFlavor f)
	    throws UnsupportedFlavorException {
	    if (f.equals (_flavor)) {
	        return _selection;
	    }
	    else throw new UnsupportedFlavorException (f);
	}
        public void lostOwnership (Clipboard c, Transferable t) {
	    _selection = null;
	}
    }

  

}

