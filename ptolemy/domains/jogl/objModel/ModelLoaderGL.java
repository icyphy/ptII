package ptolemy.domains.jogl.objModel;

// ModelLoaderGL.java
// Andrew Davison, November 2006, ad@fivedots.coe.psu.ac.th

/* A JFrame contains a JPanel which holds a GLCanvas. The GLCanvas
   displays a loaded OBJ model, which may be rotating. The model
   is scaled and centered at the origin. The scaling is controlled
   by the maxSize value which specifies the maximum size of the
   model's largest dimension.

   The listener for the canvas is ModelLoaderGLListener, and the updates
   to the canvas' display are triggered by FPSAnimator using
   fixed-rate scheduling.

   The code uses the JSR-231 1.0.0 release build of JOGL,
   14th September 2006.

   Usage:
      runGL modelGL <OBJ-name> [max-size] [-nr]

   The OBJ name is assumed to be for a file in the "models/"
   subdirectory, and the ".OBJ" extension is added automatically.

   If a "max-size" value is not specified, then MAX_SIZE is used.
   "-nr" means "no rotation", so the default action is to rotate the
   model.
*/

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GLCanvas;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.sun.opengl.util.FPSAnimator;

public class ModelLoaderGL extends JFrame {
    private static int DEFAULT_FPS = 80;

    private static final int PWIDTH = 512; // initial size of panel
    private static final int PHEIGHT = 512;

    private static final float MAX_SIZE = 4.0f; // for a model's dimension

    private ModelLoaderGLListener listener;
    private FPSAnimator animator;

    public ModelLoaderGL(String nm, float maxSize, boolean doRotate) {
        super("ModelLoaderGL");

        System.out.println("Max model size: " + maxSize);
        System.out.println("Rotating: " + doRotate);

        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        JPanel p = makeRenderPanel(nm, maxSize, doRotate);
        c.add(p, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e)
            /* The animator must be stopped in a different thread from
               the AWT event queue, to make sure that it completes before
               exit is called. */
            {
                new Thread(new Runnable() {
                    public void run() {
                        animator.stop();
                        System.exit(0);
                    }
                }).start();
            } // end of windowClosing()
        });

        pack();
        setVisible(true);

        animator.start();
    } // end of ModelLoaderGL()

    private JPanel makeRenderPanel(String nm, float maxSize, boolean doRotate)
    /* Construct a GLCanvas in a JPanel, and add a
       listener and animator. */
    {
        JPanel renderPane = new JPanel();
        renderPane.setLayout(new BorderLayout());
        renderPane.setOpaque(false);
        renderPane.setPreferredSize(new Dimension(PWIDTH, PHEIGHT));

        GLCanvas canvas = new GLCanvas();

        listener = new ModelLoaderGLListener(nm, maxSize, doRotate);
        canvas.addGLEventListener(listener);

        animator = new FPSAnimator(canvas, DEFAULT_FPS, true);

        renderPane.add(canvas, BorderLayout.CENTER);
        return renderPane;
    } // end of makeRenderPanel()

    // -----------------------------------------

    public static void main(String[] args) {
        new ModelLoaderGL("penguin", MAX_SIZE, true);

    } // end of main()

    //  private static float getMaxSize(String arg) {
    //    float maxSize = MAX_SIZE;
    //    try {
    //      maxSize = Float.parseFloat(arg);
    //    }
    //    catch (NumberFormatException e)
    //    {  System.out.println(arg + " not a float; using " + MAX_SIZE);  }
    //
    //    return maxSize;
    //  }  // end of getMaxSize()

} // end of ModelLoaderGL class
