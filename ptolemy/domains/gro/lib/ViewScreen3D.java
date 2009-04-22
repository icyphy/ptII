package ptolemy.domains.gro.lib;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GLCanvas;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.domains.gr.kernel.SceneGraphToken;
import ptolemy.domains.gro.JavaRenderer;
import ptolemy.domains.gro.kernel.GRODirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class ViewScreen3D extends TypedAtomicActor {

    boolean bQuit = false;

    public ViewScreen3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        GLPipelineObject = new TypedIOPort(this, "GLPipelineObject");
        GLPipelineObject.setInput(true);
        GLPipelineObject.setTypeEquals(SceneGraphToken.TYPE);
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
        if (_debugging) {
            _debug("Called fire()");
        }
    }
    
    /** Initialize the execution.  Create the ViewScreen frame along with the canvas and
        simple universe..
     *  @exception IllegalActionException If the base class throws it.
     */
    
    public void initialize() throws IllegalActionException {
        Frame frame = new Frame("Jogl 3D Shape/Rotation");
        GLCanvas canvas = new GLCanvas();
        
        
        
        canvas.addGLEventListener((GRODirector)getDirector());
        
        frame.add(canvas);
        frame.setSize(320, 240);
        frame.setUndecorated(true);
        int size = frame.getExtendedState();
        //size |= Frame.MAXIMIZED_BOTH;
        frame.setExtendedState(size);
        frame.setResizable(true);
       
        
        
        
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                bQuit = true;
            }
        });
        frame.setVisible(true);
//      frame.show();
        canvas.requestFocus();
        while( !bQuit ) {
            canvas.display();
        }
    }
}
