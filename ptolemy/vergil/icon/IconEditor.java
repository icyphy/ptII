
package ptolemy.vergil.icon;

import diva.gui.*;
import diva.canvas.*;
import diva.canvas.interactor.*;
import diva.canvas.toolbox.*;
import diva.graph.layout.GlobalLayout;
import diva.graph.layout.GridAnnealingLayout;
import diva.graph.layout.LevelLayout;
import diva.graph.layout.RandomLayout;
import diva.graph.toolbox.DeletionListener;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

import ptolemy.vergil.*;
import ptolemy.vergil.toolbox.*;
import java.net.*;

/**


 * @author Nick Zamora (nzamor@eecs.berkeley.edu) 
 * @author Steve Neuendorffer  (neuendor@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public class IconEditor {
    /**
     * Construct a new instance of the icon editor.
     */
    public static void main(String argv[]) {
	AppContext context = new BasicFrame("IconEditor");
	new IconEditor(context);
    }
    
    public IconEditor(AppContext context) {
	JCanvas canvas = new JCanvas();
        
	context.getContentPane().add("Center", canvas);

        JButton but = new JButton("Layout");
        but.addActionListener(_layoutAction);
        context.getContentPane().add("South", but);

	ActionListener deletionListener = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
		System.out.println("Delete!");
	    }
        };

        canvas.registerKeyboardAction(deletionListener, "Delete",
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        canvas.setRequestFocusEnabled(true);
        
	JMenuBar menuBar = context.getJMenuBar();
	JToolBar toolBar = new JToolBar();
	context.getContentPane().add("North", toolBar);

	
	// Create the File menu
        JMenu menuFile = new JMenu("Tools");
        menuFile.setMnemonic('T');
        menuBar.add(menuFile);

	Action action = _layoutAction;
       	GUIUtilities.addMenuItem(menuFile, action, 'L', 
				 "Automatically layout the model");
	URL url = null;
	try {
	    // How's this for an Icon?  :)
	    url = new URL("http://www.eecs.berkeley.edu/~neuendor/id.gif");
	} catch (MalformedURLException ex) {
	    ex.printStackTrace();
	}
	GUIUtilities.addToolBarButton(toolBar, action,
				      "Layout", new ImageIcon(url));

	// OK, now let's add a few figures into the foreground of the canvas
	GraphicsPane pane = (GraphicsPane)canvas.getCanvasPane();
	FigureLayer layer = pane.getForegroundLayer();
	Figure figure;

	// Here are the interactors for each rectangle
	SelectionInteractor interactor = new SelectionInteractor();
	// Make them draggable.
	interactor.addInteractor(new DragInteractor());
	// When they are selected, put grab handles on them.
	interactor.setPrototypeDecorator(new BoundsManipulator());
      
	figure = new BasicRectangle(50, 50, 20, 20, Color.red);
	layer.add(figure);
	figure.setInteractor(interactor);

	figure = new BasicRectangle(80, 40, 10, 30, Color.orange);
	layer.add(figure);
	figure.setInteractor(interactor);

        context.setSize(600, 400);
        context.setVisible(true);
    }

    private LayoutAction _layoutAction = new LayoutAction();

    private class LayoutAction extends AbstractAction {
	public LayoutAction() {
	    super("Layout");
	}
	
	public void actionPerformed(ActionEvent e) {
	    System.out.println("Layout!");
	}   
    }
}






