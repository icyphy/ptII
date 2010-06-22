package ptolemy.domains.jogl.lib;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GLCanvas;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.AbstractPlaceableActor;
import ptolemy.domains.jogl.kernel.JoglDirector;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class ViewScene3D extends AbstractPlaceableActor{

    boolean bQuit = false;

    public ViewScene3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        input = new TypedIOPort(this, "input", false, true);
        
        input.setTypeEquals(BaseType.OBJECT);

        showAxis = new Parameter(this, "showAxis");
        showAxis.setTypeEquals(BaseType.BOOLEAN);
        showAxis.setToken(BooleanToken.FALSE);
        
        position = new Parameter(this, "position");
        position.setExpression("{0, 0}");
        
        // TODO Auto-generated constructor stub
    }
    ///////////////////////////////////////////////////////////////////
    ////                     Ports and Parameters                  ////

    /** The input scene graph. Actors that produce 3D objects
     *  can be connected to this port for rendering.
     *  The type of this port is sceneGraph.
     */
    public TypedIOPort input;
    public Parameter position;
    public Parameter showAxis;
    
    /** Fire this actor.*/
    
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called fire()");
        }
       
        _frame.setTitle(getName()); 
        
        
        _frame.setVisible(true);
       
    }
    
    /** Initialize the execution.  Create the ViewScreen frame along with the canvas and
        simple universe..
     *  @exception IllegalActionException If the base class throws it.
     */
    
    public void initialize() throws IllegalActionException {
        
        
        
        _frame = new Frame("Jogl 3D Shape/Rotation");
        _canvas = new GLCanvas();
      
        ArrayToken positionToken = ((ArrayToken) position.getToken());
        
        _canvas.addGLEventListener((JoglDirector)getDirector());
        _canvas.setLocation(((DoubleToken) positionToken.getElement(0)).intValue(), 
                ((DoubleToken) positionToken.getElement(1)).intValue());
        
        _frame.add(_canvas);
        _frame.setSize(500, 500);
        _frame.setUndecorated(true);
        int size = _frame.getExtendedState();
        _frame.setExtendedState(size);
        _frame.setResizable(true);
        _frame.setLocationRelativeTo(_canvas);
       
        _frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                bQuit = true;
            }
        });
        _frame.setVisible(true);
        _canvas.requestFocus();
    }

    public void wrapup() throws IllegalActionException {
        
        _frame.dispose();
    }
    
    
    Frame _frame;
    GLCanvas _canvas;

   
    private Container _container;

    @Override
    public void place(Container container) {
        _container = container;

        if (_container == null) {
            // Reset everything.
            // NOTE: _remove() doesn't work here.  Why?
            if (_frame != null) {
                _frame.dispose();
            }
        }        
    }
}
