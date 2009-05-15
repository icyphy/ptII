package ptolemy.domains.gro.lib;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GLCanvas;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
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

        

        //_lastTransform = new Transform3D();
        // TODO Auto-generated constructor stub
    }
    ///////////////////////////////////////////////////////////////////
    ////                     Ports and Parameters                  ////

    /** The input scene graph. Actors that produce 3D objects
     *  can be connected to this port for rendering.
     *  The type of this port is sceneGraph.
     */
    public TypedIOPort GLPipelineObject;

 
    
    
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

        _animator = new Animator(_canvas);
        //_animator.setRunAsFastAsPossible(true);
        _animator.start();
        _canvas.addGLEventListener((GRODirector)getDirector());
        
        _frame.add(_canvas);
        _frame.setSize(500, 500);
        _frame.setUndecorated(true);
        int size = _frame.getExtendedState();
        //size |= Frame.MAXIMIZED_BOTH;
        _frame.setExtendedState(size);
        _frame.setResizable(true);
       
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
