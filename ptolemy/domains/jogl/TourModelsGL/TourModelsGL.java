package ptolemy.domains.jogl.TourModelsGL;


// TourModelsGL.java
// Andrew Davison, December 2006, ad@fivedots.coe.psu.ac.th

/* This class bears many similarities to TourGL from
   the last chapter.

  There are 4 new features:
     * the loading of OBJ models
     * selection (picking) using the mouse
     * 3D sound
     * fog
*/

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.media.opengl.AWTGraphicsConfiguration;
import javax.media.opengl.AWTGraphicsDevice;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLDrawableFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class TourModelsGL extends JFrame implements WindowListener
{
  private static int DEFAULT_FPS = 80;

  private static final int PWIDTH = 512;   // size of panel
  private static final int PHEIGHT = 512; 


  private TourModelsCanvasGL canvas;


  public TourModelsGL(long period) 
  {
    super("TourModelsGL");

    Container c = getContentPane();
    c.setLayout( new BorderLayout() );
    c.add(makeRenderPanel(period), BorderLayout.CENTER);

    addWindowListener(this);

    pack();
    setVisible(true);
  } // end of TourModelsGL()


  private JPanel makeRenderPanel(long period)
  // construct the canvas
  {
    JPanel renderPane = new JPanel();
    renderPane.setLayout( new BorderLayout() );
    renderPane.setOpaque(false);
    renderPane.setPreferredSize( new Dimension(PWIDTH, PHEIGHT));

    canvas = makeCanvas(period);
    renderPane.add("Center", canvas);

    canvas.setFocusable(true);
    canvas.requestFocus();    // the canvas now has focus, so receives key events

    // detect window resizes, and reshape the canvas accordingly
    renderPane.addComponentListener( new ComponentAdapter() {
      public void componentResized(ComponentEvent evt)
      {  Dimension d = evt.getComponent().getSize();
         // System.out.println("New size: " + d);
         canvas.reshape(d.width, d.height);
      } // end of componentResized()
    });

    return renderPane;
  }  // end of makeRenderPanel()


  private TourModelsCanvasGL makeCanvas(long period)
  {
    // get a configuration suitable for an AWT Canvas (for TourModelsCanvasGL)
    GLCapabilities caps = new GLCapabilities();

    AWTGraphicsDevice dev = new AWTGraphicsDevice(null);
    AWTGraphicsConfiguration awtConfig = (AWTGraphicsConfiguration)
       GLDrawableFactory.getFactory().chooseGraphicsConfiguration(caps, null, dev);

    GraphicsConfiguration config = null;
    if (awtConfig != null)
      config = awtConfig.getGraphicsConfiguration();

    return new TourModelsCanvasGL(period, PWIDTH, PHEIGHT, config, caps);
  } // end of makeCanvas()


  // ----------------- window listener methods -------------

  public void windowActivated(WindowEvent e) 
  { canvas.resumeGame();  }

  public void windowDeactivated(WindowEvent e) 
  {  canvas.pauseGame();  }

  public void windowDeiconified(WindowEvent e) 
  {  canvas.resumeGame();  }

  public void windowIconified(WindowEvent e) 
  {  canvas.pauseGame(); }

  public void windowClosing(WindowEvent e)
  {  canvas.stopGame();  }

  public void windowClosed(WindowEvent e) {}
  public void windowOpened(WindowEvent e) {}

// -----------------------------------------

  public static void main(String[] args)
  { 
      try {
          // Run this in the Swing Event Thread.
          Runnable doActions = new Runnable() {
                  public void run() {
                      try {
                          int fps = DEFAULT_FPS;
                          //    if (args.length != 0)
                          //      fps = Integer.parseInt(args[0]);

                          long period = (long) 1000.0/fps;
                          System.out.println("fps: " + fps + "; period: " + period + " ms");
                          new TourModelsGL(period*1000000L);    // ms --> nanosecs 
                      } catch (Exception ex) {
                          System.err.println(ex.toString());
                          ex.printStackTrace();
                      }
                  }
              };
          SwingUtilities.invokeAndWait(doActions);
      } catch (Exception ex) {
          System.err.println(ex.toString());
          ex.printStackTrace();
      } // end of main()
  }

} // end of TourModelsGL class
