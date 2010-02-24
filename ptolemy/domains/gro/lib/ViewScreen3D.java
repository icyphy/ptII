package ptolemy.domains.gro.lib;

import java.awt.Frame;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GLCanvas;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.gro.kernel.GRODirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.sun.opengl.util.Animator;

public class ViewScreen3D extends TypedAtomicActor {

    boolean bQuit = false;

    public ViewScreen3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        GLPipelineObject = new TypedIOPort(this, "GLPipelineObject");
        GLPipelineObject.setInput(true);
        GLPipelineObject.setMultiport(true);
        GLPipelineObject.setTypeEquals(BaseType.OBJECT);

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
    public TypedIOPort GLPipelineObject;
    public Parameter position;
    public Parameter showAxis;
    
    /** Fire this actor.*/
    
    public void fire() throws IllegalActionException {
        //_canvas.display();
    }
    
    /** Initialize the execution.  Create the ViewScreen frame along with the canvas and
        simple universe..
     *  @exception IllegalActionException If the base class throws it.
     */
    
    public void initialize() throws IllegalActionException {
        
        
        
        _frame = new Frame("Jogl 3D Shape/Rotation");
        _canvas = new GLCanvas();
      
        ArrayToken positionToken = ((ArrayToken) position.getToken());
        
        _animator = new Animator(_canvas);
        _animator.setRunAsFastAsPossible(true);
        _animator.start();
        
        _canvas.addGLEventListener((GRODirector)getDirector());
        _canvas.setLocation(((DoubleToken) positionToken.getElement(0)).intValue(), 
                ((DoubleToken) positionToken.getElement(1)).intValue());
        
        _frame.add(_canvas);
        _frame.setSize(500, 500);
        _frame.setUndecorated(true);
        int size = _frame.getExtendedState();
        //size |= Frame.MAXIMIZED_BOTH;
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
        _animator.stop();
        _frame.dispose();
    }
    
    Animator _animator;
    Frame _frame;
    GLCanvas _canvas;
}
