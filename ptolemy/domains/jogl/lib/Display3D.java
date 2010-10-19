package ptolemy.domains.jogl.lib;


import java.awt.Container;
import java.awt.Dimension;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.Placeable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import com.sun.opengl.util.Animator;


/**
 * An actor that is used for displaying 3D animation. 
 *
 * @author Yasemin Demir
 * @version $Id: JoglDirector.java 57401 2010-03-03 23:11:41Z ydemir $
 */
public class Display3D extends TypedAtomicActor  implements Placeable, GLEventListener{   
    
    /**
     *  Construct a Display3D object in the given container with the given name.
     *  If the container argument is null, a NullPointerException will
     *  be thrown. If the name argument is null, then the name is set
     *  to the empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this Display3D.
     *  @exception IllegalActionException If this actor
     *  is not compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *  CompositeActor and the name collides with an entity in the container.
     */

    
    public Display3D(CompositeEntity container, String name)
    throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        
        
        
    }
    
    /**
     * Initialize the display buffer and render the frame by firing the graphics actors in the model,
     * according to the order given by the scheduler.
     * 
     * @param gLDrawable
     *            The rendering context of the associated OpenGL object.
     */
    public void display(GLAutoDrawable gLDrawable) {
        
        final GL gl = gLDrawable.getGL();
        
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); 
        
        
        /* 
        *   These methods draw a point at (WIDTH/2, HEIGHT/2). The
        *   coordinates are logical coordinates not directly related
        *   to the canvas size. The width and height in glOrtho() are
        *   actual window size. It is the same as WIDTH and HEIGHT at
        *   the beginning, but if you reshape the window, they will be
        *   different, respectively.  Therefore, if we reshape the
        *   window, the red point moves.
        */   
        gl.glPointSize(10); 
        gl.glBegin(GL.GL_POINTS);
        gl.glVertex2i(_width / 2, _height / 2);
        gl.glEnd();
        gl.glFlush();
           
    }
    
    /**
     * Changing devices is not supported.
     * 
     * @see javax.media.opengl.GLEventListener#displayChanged(javax.media.opengl.GLAutoDrawable,
     *      boolean, boolean)
     */
    public void displayChanged(GLAutoDrawable gLDrawable, boolean arg1, boolean arg2) {
        
      
        
    }

    public void init(GLAutoDrawable gLDrawable) {      
        
        _gl.glShadeModel(GL.GL_SMOOTH);              // Enable Smooth Shading
        _gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);    // Black Background
        _gl.glClearDepth(1.0f);                      // Depth Buffer Setup
        _gl.glEnable(GL.GL_DEPTH_TEST);              // Enables Depth Testing
        _gl.glDepthFunc(GL.GL_LEQUAL);               // The Type Of Depth Testing To Do
        // Really Nice Perspective Calculations
        _gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST); 
       
    }
    
    /**
     * Resizes the screen.
     * 
     * @see javax.media.opengl.GLEventListener#reshape(javax.media.opengl.GLAutoDrawable,
     *      int, int, int, int)
     */
    public void reshape(GLAutoDrawable gLDrawable, int x, int y, int width, 
            int height) {
       
        GLU glu = new GLU();
        if (_height <= 0) // avoid a divide by zero error!
            _height = 1;
        final float h = (float) _width / (float) _height;
        _gl.glViewport(0, 0, _width, _height);
        _gl.glMatrixMode(GL.GL_PROJECTION);
        _gl.glLoadIdentity();
        glu.gluPerspective(45.0f, h, 1.0, 20.0);
        _gl.glMatrixMode(GL.GL_MODELVIEW);
        _gl.glLoadIdentity();
        
    }
    
    
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called fire()");
        }
       
    }


    private Container _container;

    public void place(Container container) {
        _container = container;

        if (_container == null) {
            return;
        }

        Container c = _container.getParent();

        while (c.getParent() != null) {
            c = c.getParent();
        }

        // If we had created a frame before, then blow it away.
        if (_frame != null) {
            _frame.dispose();
            _frame = null;
        }
     
         

     }
    
    public void initialize() throws IllegalActionException {
        super.initialize();

     
        // Make the frame visible.
        if (_frame != null) {
            _frame.setVisible(true);
            _frame.toFront();
        }

        // Create a frame, if placeable was not called.
        if (_container == null) {
            _frame = new JFrame("ViewScreen");
            _frame.setVisible(true);
            _frame.validate();
            _frame.setSize(_width + 50, _height);
            _container = _frame.getContentPane();
        }

        // Set the frame to be visible.
        if (_frame != null) {
            _frame.setVisible(true);
        }

        // Lastly drop the canvas in the frame.
        if (_canvas != null) {
            _container.remove(_canvas);
        }
        
        _capabilities = new GLCapabilities();
        
        _capabilities.setDoubleBuffered(true);
        
        _canvas = new GLCanvas();

        _container.add("Center", _canvas);
        
        _canvas.setSize(new Dimension(_width, _height));
        
        _container.validate();
        
        _gl = _canvas.getGL();
        
        
        /*   An event is a user input or a system state change, which is queued with other events 
         *   to be handled. Here frame is a listener for the 
         *   GL events on canvas. When a specific event happens, it sends canvas to the 
         *   corresponding event handling method and invokes the method. GLEventListener is 
         *   an interface, which only defines methods (init(), reshape(), 
         *   display(), and displaychanged()) methods are actually callback functions handling events.  
         */
        _canvas.addGLEventListener(this);
        
        
       
    }
    
    /** 
     * The input port.  This base class imposes no type constraints except
     * that the type of the input cannot be greater than the type of the
     * output.
     */
    public TypedIOPort input;
    
    /** 
     * The output port. By default, the type of this output is constrained
     * to be at least that of the input.
     */
    public TypedIOPort output;
    
    /**
     * Height of the frame  
     */
    protected int _height = 480;
    
    /**
     * Width of the frame 
     */
    protected int _width = 640;
    
    /**
     * gl is an interface handle to OpenGL methods. All OpenGL
     * commands are prefixed with "gl" as well, so you will see
     * OpenGL method like gl.glColor().
     */    
    protected GL _gl; 
    
    /**
     * Drive display() in a loop 
     */    
    protected Animator _animator; 
    
    /**
     * Drawable part of the frame 
     */  
    protected GLCanvas _canvas;  
    
    /**
     * Specifies the frame associated with the container.
     */ 
    protected JFrame _frame;    
    
    /**
     * Specifies a set of OpenGL capabilities that a rendering 
     * context must support,such as color depth and whether stereo 
     * is enabled. 
     */
    protected GLCapabilities _capabilities;

}
   
    
