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
import diva.util.java2d.PaintedShape;
import diva.util.java2d.PaintedPath;

import diva.canvas.JCanvas;
import diva.canvas.GraphicsPane;
import diva.canvas.FigureLayer;
import diva.canvas.Figure;
import diva.canvas.interactor.DragInteractor;
import diva.canvas.interactor.SelectionInteractor;
import diva.canvas.interactor.PathManipulator;
import diva.canvas.interactor.BoundsManipulator;
import diva.canvas.interactor.CircleManipulator;
import diva.canvas.interactor.BasicSelectionModel;
import diva.canvas.toolbox.PaintedFigure;
import diva.canvas.toolbox.BasicFigure;


// Java imports.
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.JMenu;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JColorChooser;
import javax.swing.JDialog;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.*;

import java.io.StringBufferInputStream;
import java.net.URL;
import java.util.Enumeration;

// Ptolemy imports.
import ptolemy.vergil.toolbox.XMLIcon;
import ptolemy.vergil.toolbox.GraphicElement;
import ptolemy.kernel.util.NamedObj;

/**
 * IconEditor.java
 * @author Nick Zamora (nzamor@uclink4.berkeley.edu) 
 * @author Steve Neuendorffer  (neuendor@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 *
 */



/**
 * IconEditor class.  This class is a stand-alone application that creates java2d 
 * shapes.  Future work on this class will be to integrate this application into 
 * vergil and use the xml icon format for run-time reading and writing of icons.
 */

public class IconEditor {

    /**
     * Construct a new instance of the icon editor.
     *
     */

    // Control begins here.
    public static void main(String argv[]) {

        /** 
	 * Setup the window for the icon editor application.  This window includes 
	 * a toolbar of shapes, a toolbar of thicknesses, a button to choose the color, 
	 * and the main drawing window and a save button.
	 *
	 * For drawing_board, I used a BasicFrame with a false argument, which tells 
	 * BasicFrame not to set the size of the window or make it visible.  The string
	 * "Edit Icon" is the name of the window.
	 *
	 */
        BasicFrame drawing_board = new BasicFrame("Edit Icon", false);

	// Make a new instance of the IconEditor class with the drawing_board.
	new IconEditor(drawing_board);
    }
  
    public IconEditor(BasicFrame drawing_board) {

        // Instantiate the color chooser for the color button.
        colorChooser = new JColorChooser();
      
        // Make a canvas for the drawing_board to use for drawing.
        canvas = new JCanvas();
	drawing_board.getContentPane().add("Center", canvas);

	// Create the save button and add it to the drawing_board.
	JButton but = new JButton("Save");
	but.addActionListener(saveAction);
	drawing_board.getContentPane().add("South", but);
	

	/** 
	 * Listen for the delete key from the keyboard.  This doesn't do anything useful 
	 * now, but ideally any shape selected should be removed when this action is invoked.
	 *
	 */

	ActionListener deletionListener = new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
	        m.clearSelection();
	        System.out.println("Delete!");
	    }
	};
    
	canvas.registerKeyboardAction(deletionListener, "Delete",
				      KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
				      JComponent.WHEN_IN_FOCUSED_WINDOW);
	canvas.setRequestFocusEnabled(true);
    

	/**
	 * Make a toolbar for the colors, thickness, and shapes windows and add them to the 
	 * frames.
	 *
	 */
	
	//	JToolBar colorToolBar = new JToolBar(JToolBar.VERTICAL);
	//drawing_board.getContentPane().add("West", colorToolBar);
	//colors.getContentPane().add("North", colorToolBar);
	JToolBar thicknessToolBar = new JToolBar(JToolBar.VERTICAL);
	drawing_board.getContentPane().add("East", thicknessToolBar);
	//thickness.getContentPane().add("North", thicknessToolBar);
	JToolBar shapesToolBar = new JToolBar(JToolBar.HORIZONTAL);
	drawing_board.getContentPane().add("North", shapesToolBar);
	//shapes.getContentPane().add("North", shapesToolBar);


	// Create the File menu on the drawing_board frame.
	JMenuBar menuBar = drawing_board.getJMenuBar();
	JMenu menuFile = new JMenu("Tools");
	menuFile.setMnemonic('T');
	menuBar.add(menuFile);

	// A layout action which, as of now, just does the same action as clicking the save button.
	Action action = saveAction;
	GUIUtilities.addMenuItem(menuFile, action, 'L', 
				 "Automatically layout the model");
    

	/** 
	 * Set up the buttons for the multiple windows.  These buttons are instantiated
	 * with gif image files and these files must be located in a sub-directory 
	 * named "gifs".
	 *
	 */

	String my_rectangle = "gifs/rect.gif";
	String my_line = "gifs/line.gif";
	String my_quad = "gifs/quad.gif";
	String my_cubic = "gifs/cubic.gif";
	String my_circle = "gifs/circle.gif";
	String my_ellipse = "gifs/ellipse.gif";
	String my_fill = "gifs/fill.gif";
	String my_thickness1 = "gifs/my_thickness1.gif";
	String my_thickness2 = "gifs/my_thickness2.gif";
	String my_thickness3 = "gifs/my_thickness3.gif";
	String my_thickness4 = "gifs/my_thickness4.gif";
	String my_thickness5 = "gifs/my_thickness5.gif";
						
	/**
	 * Now that I have the names of all the gif files, I need to make them buttons and add them  
	 * to the appropriate toolbars in the main window.
	 *
	 */

	GUIUtilities.addToolBarButton(shapesToolBar, rectangleAction,
				      "Rectangle", new ImageIcon(my_rectangle));
	GUIUtilities.addToolBarButton(shapesToolBar, lineAction,
				      "Straight Line", new ImageIcon(my_line));
	GUIUtilities.addToolBarButton(shapesToolBar, quadraticAction,
				      "Quadratic Curve", new ImageIcon(my_quad));
	GUIUtilities.addToolBarButton(shapesToolBar, cubicAction,
				      "Cubic Curve", new ImageIcon(my_cubic));
	GUIUtilities.addToolBarButton(shapesToolBar, circleAction, 
				      "Circle", new ImageIcon(my_circle));
	GUIUtilities.addToolBarButton(shapesToolBar, ellipseAction, 
				      "Ellipse", new ImageIcon(my_ellipse));
	GUIUtilities.addToolBarButton(shapesToolBar, fillAction, 
				      "Fill Shape", new ImageIcon(my_fill));
	
	GUIUtilities.addToolBarButton(thicknessToolBar, thickness1Action,
				      "Thickness of Outline", new ImageIcon(my_thickness1));
	GUIUtilities.addToolBarButton(thicknessToolBar, thickness2Action,
				      "Thickness of Outline", new ImageIcon(my_thickness2));
	GUIUtilities.addToolBarButton(thicknessToolBar, thickness3Action,
				      "Thickness of Outline", new ImageIcon(my_thickness3));
	GUIUtilities.addToolBarButton(thicknessToolBar, thickness4Action,
				      "Thickness of Outline", new ImageIcon(my_thickness4));
	GUIUtilities.addToolBarButton(thicknessToolBar, thickness5Action,
				      "Thickness of Outline", new ImageIcon(my_thickness5));
	

	colorButton = new JButton("Colors");
	colorButton.addActionListener(colorAction);
	colorButton.setBackground((Color)strokeColor);
	drawing_board.getContentPane().add("West", colorButton);
	
	// OK, now let's add a few figures into the foreground of the canvas
	pane = (GraphicsPane)canvas.getCanvasPane();
	layer = pane.getForegroundLayer();
	layer.setPickHalo(3.0);
	
	// Make them draggable.
	interactor1.addInteractor(new DragInteractor());
	interactor2.addInteractor(new DragInteractor());
	interactor3.addInteractor(new DragInteractor());
	// When they are selected, put grab handles on them.
	interactor1.setPrototypeDecorator(new PathManipulator());
	interactor2.setPrototypeDecorator(new BoundsManipulator());
	interactor3.setPrototypeDecorator(new CircleManipulator());

	/**
	 * This is the call to getFigure() which reads in some xml code and returns
	 * a new figure represented by the xml.
	 *
	 * Figure f = getFigure(null);
	 * layer.add(f);
	 * f.setInteractor(interactor1);
	 *
	 */

	// Sets the size of the window in pixels.
	drawing_board.setSize(500, 605);

	// This layout makes the windows appear in the top-left corner of the screen.
	drawing_board.setLocation(0, 75);
	
	// There is no need for the user to be able to resize the window.
	drawing_board.setResizable(false);
	
	// Only set the window visible now so the user doesn't see it being constructed.
	drawing_board.setVisible(true);
    }

    /**
     * The getFigure() method, which I'm keeping around in case I need it for the xml stuff.
     * Eventually, this entire method will be removed.
     *
     *
     *
     *     public Figure getFigure(String xml_string) {
     *         PaintedFigure my_figure = new PaintedFigure();
     *         StringBufferInputStream xml_stream = null;
     *         Enumeration enum = null;
     *         URL base = null;
     *         xml_stream = new StringBufferInputStream("<xmlgraphic> <rectangle coords=\"0 0 60 40\" fill=\"white\"/> <polygon coords=\"10 10 50 20 10 30\" fill=\"blue\"/> </xmlgraphic>\n");	       
     *         try {
     *             base = new URL("http://hkn.eecs.berkeley.edu/~nickeyz/test.html");
     *         }
     *         catch (MalformedURLException e) {
     *  	   System.out.println("Bad URL");
     *         }
     *         try {
     *  	   XMLIcon xml_icon = new XMLIcon(new NamedObj());
     *  	   xml_icon.configure(base, xml_stream);
     *  	   enum = xml_icon.graphicElements();
     *         }
     *         catch (Exception e) {
     *   	   System.out.println("Error in IconEditor.java dealing with xml_icon");
     *         }
     *         GraphicElement element = (GraphicElement) enum.nextElement();
     *         my_figure.add(element.getPaintedObject());
     *         return my_figure;
     *     }
     *
     */

    // Create the object for the color chooser.
    JButton colorButton;
    JColorChooser colorChooser;
    JCanvas canvas;
    JDialog dialog;

    // Here are the interactors for each shape
    BasicSelectionModel m = new BasicSelectionModel();
    SelectionInteractor interactor1 = new SelectionInteractor(m);
    SelectionInteractor interactor2 = new SelectionInteractor(m);
    SelectionInteractor interactor3 = new SelectionInteractor(m);
  
    // This is the current shape, line thickness, and paint color.
    BasicFigure current_shape = null;
    float outline_thickness = 1.0f;
    Paint strokeColor = new Color(0, 170, 170);//Color.black;

    // Window objects
    GraphicsPane pane;
    FigureLayer layer;
    Figure figure;

    /**
     * All the actions possible from clicking all the different buttons.
     * These actions have inner classes defined below, each class giving
     * explicitly the action taken for each pushed button.
     *
     */
    private SaveAction saveAction = new SaveAction();
    private RectangleAction rectangleAction = new RectangleAction();
    private LineAction lineAction = new LineAction();
    private QuadraticAction quadraticAction = new QuadraticAction();
    private CubicAction cubicAction = new CubicAction();
    private CircleAction circleAction = new CircleAction();
    private EllipseAction ellipseAction = new EllipseAction();
    private FillAction fillAction = new FillAction();
    private Thickness1Action thickness1Action = new Thickness1Action();
    private Thickness2Action thickness2Action = new Thickness2Action();
    private Thickness3Action thickness3Action = new Thickness3Action();
    private Thickness4Action thickness4Action = new Thickness4Action();
    private Thickness5Action thickness5Action = new Thickness5Action();
    private ColorAction colorAction = new ColorAction();
    private OkAction okAction = new OkAction();
    private CancelAction cancelAction = new CancelAction();

    /**
     * Here are the definitions for all the actions that take place 
     * when a button is clicked in one of the active windows.  Each of these 
     * inner classes define the response for exactly one button.  Currently
     * each response includes printing a statement to standard output for 
     * debugging purposes.
     *
     */

    private class SaveAction extends AbstractAction {
        public SaveAction() {
            super("Save");
	}
        public void actionPerformed(ActionEvent e) {
	    System.out.println("Save button!");
	}
    }

    private class RectangleAction extends AbstractAction {
        public RectangleAction() {
            super("Rectangle");
	}
        public void actionPerformed(ActionEvent e) {
	    current_shape = new BasicFigure(new PaintedShape(new Rectangle2D.Double(8.0, 10.0, 20.0, 20.0), 
							     outline_thickness, strokeColor));
	    System.out.println("You have chosen a rectangle");
	    layer.add(current_shape);
	    current_shape.setInteractor(interactor2);
	}
    }

    private class LineAction extends AbstractAction {
        public LineAction() {
            super("Straight Line");
	}
        public void actionPerformed(ActionEvent e) {
	    current_shape = new BasicFigure(new PaintedPath(new Line2D.Double(45.0, 10.0, 65.0, 30.0), 
							     outline_thickness, strokeColor));
	    System.out.println("You have chosen a straight line");
	    layer.add(current_shape);
	    current_shape.setInteractor(interactor1);
	}
    }

    private class QuadraticAction extends AbstractAction {
        public QuadraticAction() {
            super("Quadratic Curve");
	}
        public void actionPerformed(ActionEvent e) {
            current_shape = new BasicFigure(new PaintedPath(new QuadCurve2D.Double(77.0, 10.0, 87.0, 20.0, 97.0, 30.0), 
							     outline_thickness, strokeColor));
	    System.out.println("You have chosen a quadratic curve");
	    layer.add(current_shape);
	    current_shape.setInteractor(interactor1);
	}
    }

    private class CubicAction extends AbstractAction {
        public CubicAction() {
            super("Cubic Curve");
	}
        public void actionPerformed(ActionEvent e) {
            current_shape = new BasicFigure(new PaintedPath(new CubicCurve2D.Double(110.0, 10.0, 117.0, 17.0, 123.0, 23.0, 130.0, 30.0), 
							     outline_thickness, strokeColor));
	    System.out.println("You have chosen a cubic curve");
	    layer.add(current_shape);
	    current_shape.setInteractor(interactor1);
	}
    }
  
    private class CircleAction extends AbstractAction {
        public CircleAction() {
            super("Circle");
	}
        public void actionPerformed(ActionEvent e) {
	    current_shape = new BasicFigure(new PaintedShape(new Ellipse2D.Double(148.0, 10.0, 20.0, 20.0), 
							    outline_thickness, strokeColor)); 
	    System.out.println("You have chosen a circle");
	    layer.add(current_shape);
	    current_shape.setInteractor(interactor3);
	}
    }

    private class EllipseAction extends AbstractAction {
        public EllipseAction() {
            super("Ellipse");
	}
        public void actionPerformed(ActionEvent e) {
	    current_shape = new BasicFigure(new PaintedShape(new Ellipse2D.Double(183.0, 10.0, 20.0, 30.0), 
							     outline_thickness, strokeColor)); 
	    System.out.println("You have chosen an ellipse");
	    layer.add(current_shape);
	    current_shape.setInteractor(interactor2);
	}
    }
  
    private class FillAction extends AbstractAction {
        public FillAction() {
            super("Fill Shape");
	}
        public void actionPerformed(ActionEvent e) {
            System.out.println("You have chosen to fill a shape");
	}
    }
  
    private class Thickness1Action extends AbstractAction {
        public Thickness1Action() {
            super("Thickness");
	}
        public void actionPerformed(ActionEvent e) {
	    outline_thickness = 1.0f;
	    System.out.println("Thickness 1");
	}
    }

    private class Thickness2Action extends AbstractAction {
        public Thickness2Action() {
            super("Thickness");
	}
        public void actionPerformed(ActionEvent e) {
	    outline_thickness = 3.0f;
	    System.out.println("Thickness 2");
	}
    }

    private class Thickness3Action extends AbstractAction {
        public Thickness3Action() {
            super("Thickness");
	}
        public void actionPerformed(ActionEvent e) {
	    outline_thickness = 5.0f;
	    System.out.println("Thickness 3");
	}
    }

    private class Thickness4Action extends AbstractAction {
        public Thickness4Action() {
            super("Thickness");
	}
        public void actionPerformed(ActionEvent e) {
	    outline_thickness = 7.0f;
	    System.out.println("Thickness 4");
	}
    }

    private class Thickness5Action extends AbstractAction {
        public Thickness5Action() {
            super("Thickness");
	}
        public void actionPerformed(ActionEvent e) {
	    outline_thickness = 9.0f;
	    System.out.println("Thickness 5");
	}
    }
  /*
    private class BlackAction extends AbstractAction {
        public BlackAction() {
            super("Color");
	}
        public void actionPerformed(ActionEvent e) {
	    dialog = JColorChooser.createDialog(canvas, "Choose A Color", true, colorChooser, okAction, cancelAction);
	    dialog.setVisible(true);
	    //strokeColor = Color.black;
	    System.out.println("Black");
	}
    }
  
    private class DarkGrayAction extends AbstractAction {
        public DarkGrayAction() {
            super("Color");
	}
        public void actionPerformed(ActionEvent e) {
	    strokeColor = Color.darkGray;
	    System.out.println("Dark Gray");
	}
    }
  
    private class GrayAction extends AbstractAction {
        public GrayAction() {
	    super("Color");
	}
        public void actionPerformed(ActionEvent e) {
	    strokeColor = Color.gray;
	    System.out.println("Gray");
	}
    }

    private class LightGrayAction extends AbstractAction {
        public LightGrayAction() {
	    super("Color");
	}
        public void actionPerformed(ActionEvent e) {
	    strokeColor = Color.lightGray;
	    System.out.println("Light Gray");
	}
    }
  
    private class WhiteAction extends AbstractAction {
        public WhiteAction() {
            super("Color");
	}
        public void actionPerformed(ActionEvent e) {
	    strokeColor = Color.white;
	    System.out.println("White");
	}
    }

    private class BlueAction extends AbstractAction {
        public BlueAction() {
	    super("Color");
	}
        public void actionPerformed(ActionEvent e) {
	    strokeColor = Color.blue;
	    System.out.println("Blue");
	}
  }
  
    private class CyanAction extends AbstractAction {
        public CyanAction() {
	    super("Color");
	}
        public void actionPerformed(ActionEvent e) {
	    strokeColor = Color.cyan;
	    System.out.println("Cyan");
	}
    }
  
    private class GreenAction extends AbstractAction {
        public GreenAction() {
	    super("Color");
	}
        public void actionPerformed(ActionEvent e) {
	    strokeColor = Color.green;
	    System.out.println("Green");
	}
    }
  
    private class MagentaAction extends AbstractAction {
        public MagentaAction() {
	    super("Color");
	}
        public void actionPerformed(ActionEvent e) {
	    strokeColor = Color.magenta;
	    System.out.println("Magenta");
	}
    }

    private class OrangeAction extends AbstractAction {
        public OrangeAction() {
	    super("Color");
	}
        public void actionPerformed(ActionEvent e) {
	    strokeColor = Color.orange;
	    System.out.println("Orange");
	}
    }

    private class PinkAction extends AbstractAction {
        public PinkAction() {
	    super("Color");
	}
        public void actionPerformed(ActionEvent e) {
	    strokeColor = Color.pink;
	    System.out.println("Pink");
	}
    }

    private class RedAction extends AbstractAction {
        public RedAction() {
            super("Color");
	}
        public void actionPerformed(ActionEvent e) {
	    strokeColor = Color.red;
	    System.out.println("Red");
	}
    }

    private class YellowAction extends AbstractAction {
        public YellowAction() {
	    super("Color");
	}
        public void actionPerformed(ActionEvent e) {
	    strokeColor = Color.yellow;
	    System.out.println("Yellow");
	}
    }
  */
    private class ColorAction extends AbstractAction {
        public ColorAction() {
	    super("Color");
	}
        public void actionPerformed(ActionEvent e) {
	    dialog = JColorChooser.createDialog(canvas, "Choose A Color", true, colorChooser, okAction, cancelAction);
	    dialog.setVisible(true);
	}
    }
  
    private class OkAction extends AbstractAction {
        public OkAction() {
	    super("Ok");
	}
        public void actionPerformed(ActionEvent e) {
	    Color thisColor = colorChooser.getColor();
	    colorButton.setBackground(thisColor);
	    colorButton.repaint();
	    strokeColor = thisColor;
	    System.out.println("Ok!");
	}
    }


    private class CancelAction extends AbstractAction {
        public CancelAction() {
	    super("Ok");
	}
        public void actionPerformed(ActionEvent e) {
	    System.out.println("Cancel!");
	}
    }

}






