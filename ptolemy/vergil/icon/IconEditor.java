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
//import diva.util.java2d.PaintedPath;

import diva.canvas.JCanvas;
import diva.canvas.GraphicsPane;
import diva.canvas.FigureLayer;
//import diva.canvas.Figure;
import diva.canvas.interactor.DragInteractor;
import diva.canvas.interactor.SelectionInteractor;
import diva.canvas.interactor.PathManipulator;
import diva.canvas.interactor.BoundsManipulator;
import diva.canvas.interactor.CircleManipulator;
import diva.canvas.interactor.BasicSelectionModel;
//import diva.canvas.toolbox.PaintedFigure;
import diva.canvas.toolbox.BasicFigure;

// Java imports.
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
//import javax.swing.JFrame;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
//import java.awt.geom.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

//import java.io.StringBufferInputStream;
//import java.net.URL;
//import java.util.Enumeration;

// Ptolemy imports.
import ptolemy.vergil.toolbox.XMLIcon;
//import ptolemy.vergil.toolbox.GraphicElement;
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

public class IconEditor {

    // Control begins here.
    public static void main(String argv[]) 
        throws NameDuplicationException, IllegalActionException {
	// Setup the window for the icon editor application.  This 
        // window will include a toolbar of different shapes, a toolbar 
        // of different thicknesses, a button to choose the color, 
        // and the main drawing window.
        // For _drawingFrame, I used a BasicFrame with a false argument, 
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
	// add them to the _context frame.

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
    
	// Set up the buttons for the multiple toolbars.  These buttons 
	// are instantiated with gif image files and these files must be 
	// located in a sub-directory from this one named "gifs".

	String Rectangle = "gifs/rect.gif";
	String Line = "gifs/line.gif";
	String Quad = "gifs/quad.gif";
	String Cubic = "gifs/cubic.gif";
	String Circle = "gifs/circle.gif";
	String Ellipse = "gifs/ellipse.gif";
	String Fill = "gifs/fill.gif";
	String Stroke = "gifs/stroke.gif";
	String thickness1 = "gifs/thickness1.gif";
	String thickness2 = "gifs/thickness2.gif";
	String thickness3 = "gifs/thickness3.gif";
	String thickness4 = "gifs/thickness4.gif";
	String thickness5 = "gifs/thickness5.gif";


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

	// Now we need to get the canvas pane and foreground layer of our 
	// canvas.  Also, here is where I set the halo width (twice the 
	// distance your mouse can be from a figure when you click and still 
	// have the program detect that you are trying to click on the figure).
	_pane = (GraphicsPane) _canvas.getCanvasPane ();
	_layer = _pane.getForegroundLayer ();
	_layer.setPickHalo (MOUSE_SENSITIVITY);
	
	// Make them draggable.
	_interactor1.addInteractor (new DragInteractor ());
	_interactor2.addInteractor (new DragInteractor ());
	_interactor3.addInteractor (new DragInteractor ());
	// When they are selected, put grab handles on them.
	_interactor1.setPrototypeDecorator (new PathManipulator ());
	_interactor2.setPrototypeDecorator (new BoundsManipulator ());
	_interactor3.setPrototypeDecorator (new CircleManipulator ());

	// This is the call to getFigure() which reads in some xml code and 
	// returns a new figure represented by the xml.  For now, I've 
	// commented this out.  I'll uncomment it when we need to integrate 
	// this program into vergil.
	// Figure f = getFigure (null);
	// _layer.add (f);
	// f.setInteractor (_interactor1);

	// Sets the size of the main window in pixels.
	_context.setSize (WINDOW_SIZE_HORIZONTAL, WINDOW_SIZE_VERTICAL);

	// This layout makes the windows appear in the top-left corner of the screen.
	//_context.setLocation (0, 75);
	
	// There is no need for the user to be able to resize the window.
	//_context.setResizable (WINDOW_RESIZABLE);
	
	// Only set the window visible now so the user doesn't see it being 
	// constructed.
	_context.setVisible (true);
	
	// Set-up the possible file extensions for opening and saving icons.
	_filter.addExtension (FILE_FORMAT_EXTENSION);
	_filter.setDescription (FILE_FORMAT_EXTENSION + " extension only.");
	_fileChooser.setFileFilter (_filter);
    }

    // The getFigure() method, which I'm keeping around in case I need it for 
    // the xml stuff.
    // Eventually, this entire method will be removed.
    //     public Figure getFigure (String xml_string) {
    //         PaintedFigure my_figure = new PaintedFigure ();
    //         StringBufferInputStream xml_stream = null;
    //         Enumeration enum = null;
    //         URL base = null;
    //         xml_stream = new StringBufferInputStream ("<xmlgraphic> <rectangle coords=\"0 0 60 40\" fill=\"white\"/> <polygon coords=\"10 10 50 20 10 30\" fill=\"blue\"/> </xmlgraphic>\n");	       
    //         try {
    //             base = new URL ("http://hkn.eecs.berkeley.edu/~nickeyz/test.html");
    //         }
    //         catch (MalformedURLException e) {
    //             System.out.println ("Bad URL");
    //         }
    //         try {
    //  	   XMLIcon xml_icon = new XMLIcon (new NamedObj());
    //  	   xml_icon.configure (base, xml_stream);
    //  	   enum = xml_icon.graphicElements ();
    //         }
    //         catch (Exception e) {
    //   	   System.out.println ("Error in IconEditor.java dealing with xml_icon");
    //         }
    //         GraphicElement element = (GraphicElement) enum.nextElement ();
    //         my_figure.add (element.getPaintedObject ());
    //         return my_figure;
    //     }
    //


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
    private BasicFigure _currentFigure = null;
    private BasicFigure _cutOrCopiedFigure = null;
    private float _outlineThickness = 1.0f;
    private Paint _strokeColor = new Color (0, 170, 170);

    // Window objects
    private GraphicsPane _pane;
    private FigureLayer _layer;
    private JCanvas _canvas;
    private JDialog _dialog;

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
    // This is the extension we allow for opening and saving files within the program.
    private static final String FILE_FORMAT_EXTENSION = "xml";


    ////////////////////////////////////////////////////////////////////////
    /////////////////////    Anonymous Classes     ////////////////////////

    // Here are the definitions for all the actions that take place 
    // when a button is clicked in one of the active windows, or a 
    // menu item is selected from the toolbar at the top of the 
    // window.  Also, when you click "OK" or "Cancel" in the color 
    // window or file window, then that action code becomes invoked.
    // Each of these inner classes define the response for 
    // exactly one button or selection.

    Action saveAction = new AbstractAction ("Save") {
        public void actionPerformed (ActionEvent e) {
	    System.out.println ("Save button!");
	}
    };
    Action rectangleAction = new AbstractAction ("Rectangle") {
        public void actionPerformed (ActionEvent e) {
	    _currentFigure = new BasicFigure 
	        (new PaintedShape (new Rectangle2D.Double
				   (8.0, 10.0, 20.0, 20.0), 
				   _outlineThickness, _strokeColor));
	    _layer.add (_currentFigure);
	    _currentFigure.setInteractor (_interactor2);
	}
    };
    Action lineAction = new AbstractAction ("Line") {
        public void actionPerformed (ActionEvent e) {
	    _currentFigure = new BasicFigure 
	        (new PaintedShape (new Line2D.Double
				   (45.0, 10.0, 65.0, 30.0), 
				   _outlineThickness, _strokeColor));
	    _layer.add (_currentFigure);
	    _currentFigure.setInteractor (_interactor1);
	}
    };
    Action quadraticAction = new AbstractAction ("Quadratic Curve") {
        public void actionPerformed (ActionEvent e) {
	    _currentFigure = new BasicFigure 
	        (new PaintedShape (new QuadCurve2D.Double
				   (77.0, 10.0, 87.0, 20.0, 97.0, 30.0), 
				   _outlineThickness, _strokeColor));
	    _layer.add (_currentFigure);
	    _currentFigure.setInteractor (_interactor1);
	}
    };
    Action cubicAction = new AbstractAction ("Cubic Curve") {
        public void actionPerformed (ActionEvent e) {
	    _currentFigure = new BasicFigure 
	        (new PaintedShape (new CubicCurve2D.Double
				   (110.0, 10.0, 117.0, 17.0, 
				    123.0, 23.0, 130.0, 30.0),
				   _outlineThickness, _strokeColor));
	    _layer.add (_currentFigure);
	    _currentFigure.setInteractor (_interactor1);
	}
    };
    Action circleAction = new AbstractAction ("Circle") {
        public void actionPerformed (ActionEvent e) {
	    _currentFigure = new BasicFigure 
	        (new PaintedShape (new Ellipse2D.Double
				   (148.0, 10.0, 20.0, 20.0), 
				   _outlineThickness, _strokeColor)); 
	    _layer.add (_currentFigure);
	    _currentFigure.setInteractor (_interactor3);
	}
    };
    Action ellipseAction = new AbstractAction ("Ellipse") {
        public void actionPerformed (ActionEvent e) {
	    _currentFigure = new BasicFigure 
	        (new PaintedShape (new Ellipse2D.Double
				   (183.0, 10.0, 20.0, 30.0), 
				   _outlineThickness, _strokeColor)); 
	    _layer.add (_currentFigure);
	    _currentFigure.setInteractor (_interactor2);
	}
    };
    Action fillAction = new AbstractAction ("Fill") {
        public void actionPerformed (ActionEvent e) {
	    BasicFigure basicFigure = (BasicFigure) _m.getFirstSelection ();
	    if (basicFigure != null) {
	        basicFigure.setFillPaint (_strokeColor);
		basicFigure.repaint ();
	    }
	}
    };
    Action strokeAction = new AbstractAction ("Stroke") {
        public void actionPerformed (ActionEvent e) {
	    BasicFigure basicFigure = (BasicFigure) _m.getFirstSelection ();
	    if (basicFigure != null) {
	        basicFigure.setStrokePaint (_strokeColor);
		basicFigure.repaint ();
	    }
	}
    };
    Action thickness1Action = new AbstractAction ("Thickness 1") {
        public void actionPerformed (ActionEvent e) {
	    changeThickness (1.0f);
	}
    };
    Action thickness2Action = new AbstractAction ("Thickness 2") {
        public void actionPerformed (ActionEvent e) {
	    changeThickness (3.0f);
	}
    };
    Action thickness3Action = new AbstractAction ("Thickness 3") {
        public void actionPerformed (ActionEvent e) {
	    changeThickness (5.0f);
	}
    };
    Action thickness4Action = new AbstractAction ("Thickness 4") {
        public void actionPerformed (ActionEvent e) {
	    changeThickness (7.0f);
	}
    };
    Action thickness5Action = new AbstractAction ("Thickness 5") {
        public void actionPerformed (ActionEvent e) {
	    changeThickness (9.0f);
	}
    };
    Action colorAction = new AbstractAction ("Color") {
        public void actionPerformed (ActionEvent e) {
	    _dialog = JColorChooser.createDialog 
	        (_canvas, "Choose A Color", true, 
		 _colorChooser, okAction, cancelAction);
	    _dialog.setVisible (true);
	}
    };
    Action okAction = new AbstractAction ("Ok") {
        public void actionPerformed (ActionEvent e) {
	    Color thisColor = _colorChooser.getColor ();
	    _colorButton.setBackground (thisColor);
	    _colorButton.repaint ();
	    _strokeColor = thisColor;
    	}
    };
    Action cancelAction = new AbstractAction ("Cancel") {
        public void actionPerformed (ActionEvent e) {
	}
    };
    Action cutAction = new AbstractAction ("Cut    CTRL+X") {
        public void actionPerformed (ActionEvent e) {
	    _currentFigure = (BasicFigure) _m.getFirstSelection ();
	    _m.clearSelection ();
	    if (_currentFigure != null) {
		_layer.remove (_currentFigure);
	    }
	    changeCutOrCopiedFigure (true);
	}
    };
    Action copyAction = new AbstractAction ("Copy   CTRL+C") {
        public void actionPerformed (ActionEvent e) {
	    _currentFigure = (BasicFigure) _m.getFirstSelection ();
	    changeCutOrCopiedFigure (true);
	}
    };
    Action pasteAction = new AbstractAction ("Paste  CTRL+V") {
        public void actionPerformed (ActionEvent e) {
	    if (_cutOrCopiedFigure != null) {
	        _layer.add (_cutOrCopiedFigure);
		changeCutOrCopiedFigure (false);
	    }
	}
    };

    Action newIconAction = new AbstractAction ("New    CTRL+N") {
        public void actionPerformed (ActionEvent e) {
	    System.out.println ("New");
	}
    };
    Action openIconAction = new AbstractAction ("Open   CTRL+O") {
        public void actionPerformed (ActionEvent e) {
	    int choice = 
	    _fileChooser.showOpenDialog (_context.makeComponent());
	    
	    if (choice == JFileChooser.CANCEL_OPTION) {
	        System.out.println ("You have cancelled your open file choice");
	    }
	    else {
	        System.out.println ("You chose to open this file: " + 
				    _fileChooser.getSelectedFile ().getName ());
	    }
	}
    };

    Action saveIconAction = new AbstractAction ("Save   CTRL+S") {
        public void actionPerformed (ActionEvent e) {
	    System.out.println ("Save");
	}
    };

    Action saveIconAsAction = new AbstractAction ("Save As...") {
        public void actionPerformed (ActionEvent e) {
	    int choice = 
	    _fileChooser.showSaveDialog (_context.makeComponent());
	    
	    if (choice == JFileChooser.CANCEL_OPTION) {
	        System.out.println ("You have cancelled your save file as choice");
	    } 
	    else {
	        System.out.println ("You chose to save this file: " + 
				    _fileChooser.getSelectedFile ().getName ());
	    }
	}
    };
    
    Action exitIconAction = new AbstractAction ("Exit") {
        public void actionPerformed(ActionEvent e) {
	    _context.setVisible(false);
	}
    };

    // Listen for the delete key from the keyboard.  When the delete key is 
    // pressed, the currently selected figure is removed from the _layer and 
    // unselected from the selection model.  Pressing the delete key is unlike 
    // the cut command from the edit menu in the toolbar in that the delete 
    // command is irreversible.  "Paste" will NOT return a figure that has 
    // been deleted to the canvas.

    ActionListener deletionListener = new ActionListener () {
        public void actionPerformed (ActionEvent evt) {
	    BasicFigure basicFigure = (BasicFigure) _m.getFirstSelection ();
	    if (basicFigure != null) {
	        _m.clearSelection ();
		_layer.remove (basicFigure);
	    }
	}
    };


    //////////////////////////////////////////////////////////////////////
    //////////////////      Private methods.            /////////////////

    // A private method to change the thickness of a selected figure.
    //Parameter float newThickness The new thickness for the selected figure.

    private void changeThickness (float newThickness) {
        _outlineThickness = newThickness;
	BasicFigure basicFigure = (BasicFigure) _m.getFirstSelection ();
	if (basicFigure != null) {
	    ((BasicFigure) _m.getFirstSelection ()).setLineWidth (_outlineThickness);
	}
    }

    // A private method to change the currently remembered shape for the "cut", 
    // "copy" and "paste" functions.  This method creates a new BasicFigure 
    // from the current one recently "cut", "copied", or "pasted".  
    //
    // Parameter boolean newCopy True if the figure is a new one to copy, false 
    // if the figure was pasted and hence shouldn't copy the selected figure.

    private void changeCutOrCopiedFigure (boolean newCopy) {
        Shape newShape = null;
	if (_currentFigure != null) {
	    if (newCopy) {
	        _cutOrCopiedFigure = _currentFigure;
	    }
	    float lw = _cutOrCopiedFigure.getLineWidth ();
	    Shape oldShape = _cutOrCopiedFigure.getShape ();
	    Paint strokePaint = _cutOrCopiedFigure.getStrokePaint ();
	    Paint fillPaint = _cutOrCopiedFigure.getFillPaint ();
	    if (oldShape instanceof Rectangle2D.Double) {
	        newShape = (Shape)((Rectangle2D.Double) oldShape).clone ();
	    }
	    else if (oldShape instanceof Ellipse2D.Double) {
	        newShape = (Shape)((Ellipse2D.Double) oldShape).clone ();
	    }
	    else if (oldShape instanceof GeneralPath) {
	        newShape = (Shape)((GeneralPath) oldShape).clone ();
	    }
	    _cutOrCopiedFigure = new BasicFigure (newShape, fillPaint, lw);
	    _cutOrCopiedFigure.setInteractor (_currentFigure.getInteractor ());
	    _cutOrCopiedFigure.setStrokePaint (_currentFigure.getStrokePaint ());
	}
    }

}
