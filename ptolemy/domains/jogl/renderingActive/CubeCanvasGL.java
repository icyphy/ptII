package ptolemy.domains.jogl.renderingActive;


// CubeCanvasGL.java
// Andrew Davison, November 2006, ad@fivedots.coe.psu.ac.th

/* Animate the rotating cube using active rendering.
   A single thread is spawned which initialises the rendering
   and then loops, carrying out update, render, sleep with
   a fixed period.

   I borrowed some ideas from the SingleThreadedGlCanvas class by
   Markus_Persson, January 23, 2006
   http://www.javagaming.org/forums/index.php?topic=12094.15

   and other from the active rendering framework in chapter 2
   from "Killing Game Programming in Java" (KGPJ). There's a 
   version online at http://fivedots.coe.psu.ac.th/~ad/jg/ch1/.

   The animation can be paused and resumed. The window can be resized.

   The statistics code is lifted from chapter 3 of KGPJ (p.54-56), which
   is online at http://fivedots.coe.psu.ac.th/~ad/jg/ch02/.
   The time calculations in this version use System.nanoTime() rather 
   than J3DTimer.getValue(), so require J2SE 5.0.
*/

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.DecimalFormat;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.sun.opengl.util.*;


public class CubeCanvasGL extends Canvas implements Runnable
{
  private static final float INCR_MAX = 10.0f;   // for rotation increments
  private static final double Z_DIST = 7.0;      // for the camera position

  // statistic constants
  private static long MAX_STATS_INTERVAL = 1000000000L;
    // record stats every 1 second (roughly)

  private static final int NO_DELAYS_PER_YIELD = 16;
  /* Number of iterations with a sleep delay of 0 ms before the animation 
     thread yields to other running threads. */

  private static int MAX_RENDER_SKIPS = 5;   // was 2;
    // no. of renders that can be skipped in any one animation loop
    // i.e the games state is updated but not rendered

  private static int NUM_FPS = 10;
     // number of FPS values stored to get an average


  // used for gathering statistics
  private long statsInterval = 0L;    // in ns
  private long prevStatsTime;   
  private long totalElapsedTime = 0L;
  private long gameStartTime;
  private int timeSpentInGame = 0;    // in seconds

  private long frameCount = 0;
  private double fpsStore[];
  private long statsCount = 0;
  private double averageFPS = 0.0;

  private long rendersSkipped = 0L;
  private long totalRendersSkipped = 0L;
  private double upsStore[];
  private double averageUPS = 0.0;

  private DecimalFormat df = new DecimalFormat("0.##");  // 2 dp
  private DecimalFormat timedf = new DecimalFormat("0.####");  // 4 dp

  // used at game termination
  private volatile boolean gameOver = false;

  // vertices for a cube of sides 2 units, centered on (0,0,0)
  private float[][] verts = { 
     {-1.0f,-1.0f, 1.0f},  // vertex 0
     {-1.0f, 1.0f, 1.0f},  // 1
     { 1.0f, 1.0f, 1.0f},  // 2
     { 1.0f,-1.0f, 1.0f},  // 3
     {-1.0f,-1.0f,-1.0f},  // 4
     {-1.0f, 1.0f,-1.0f},  // 5
     { 1.0f, 1.0f,-1.0f},  // 6
     { 1.0f,-1.0f,-1.0f},  // 7
  };

  int cubeDList;   // display list for displaying the cube

  private CubeGL top;   // reference back to top-level JFrame

  private long period;                // period between drawing in _nanosecs_

  private Thread animator;             // the thread that performs the animation
  private volatile boolean isRunning = false;   // used to stop the animation thread
  private volatile boolean isPaused = false;


  // OpenGL
  private GLDrawable drawable;   // the rendering 'surface'
  private GLContext context;     // the rendering context (holds rendering state info)
  private GL gl;
  private GLU glu;

  // rotation variables
  private float rotX, rotY, rotZ;     // total rotations in x,y,z axes
  private float incrX, incrY, incrZ;  // increments for x,y,z rotations

  // window sizing
  private boolean isResized = false;
  private int panelWidth, panelHeight;



  public CubeCanvasGL(CubeGL top, long period, int width, int height,
                         GraphicsConfiguration config, GLCapabilities caps)
  { 
    super(config);

    this.top = top;
    this.period = period;
    panelWidth = width;
    panelHeight = height;

    // get a rendering surface and a context for this canvas
    drawable = GLDrawableFactory.getFactory().getGLDrawable(this, caps, null);
    context = drawable.createContext(null);

    // initialize the rotation variables
    rotX = 0; rotY = 0; rotZ = 0;
    Random random = new Random();
    incrX = random.nextFloat()*INCR_MAX;   // 0 - INCR_MAX degrees
    incrY = random.nextFloat()*INCR_MAX; 
    incrZ = random.nextFloat()*INCR_MAX; 

    // statistics initialization
    fpsStore = new double[NUM_FPS];
    upsStore = new double[NUM_FPS];
    for (int i=0; i < NUM_FPS; i++) {
      fpsStore[i] = 0.0;
      upsStore[i] = 0.0;
    }
  } // end of CubeCanvasGL()



  public void addNotify()
  // wait for the canvas to be added to the JPanel before starting
  { 
    super.addNotify();      // make the component displayable
    drawable.setRealized(true);   // the canvas can now be rendering into

    // initialise and start the animation thread 
    if (animator == null || !isRunning) {
      animator = new Thread(this);
	  animator.start();
    }
  } // end of addNotify()


  // ------------- game life cycle methods ------------
  // called by the JFrame's window listener methods

  public void resumeGame()
  // called when the JFrame is activated / deiconified
  {  isPaused = false;  } 

  public void pauseGame()
  // called when the JFrame is deactivated / iconified
  { isPaused = true;   } 

  public void stopGame() 
  // called when the JFrame is closing
  {  isRunning = false;   }

  // ----------------------------------------------

  public void reshape(int w, int h)
  /* called by the JFrame's ComponentListener when the window is resized
     (similar to the reshape() callback in GLEventListener) */
  {
     isResized = true;
     if (h == 0)
       h = 1;   // to avoid division by 0 in aspect ratio in resizeView()
     panelWidth = w; panelHeight = h;
  }  // end of reshape()


  public void update(Graphics g) { }

  public void paint(Graphics g) { }


  public void run()
  // initialize rendering and start frame generation
  {
    // makeContentCurrent();

    initRender();
    renderLoop();

    // discard the rendering context and exit
    // context.release();
    context.destroy();
    System.exit(0);
  } // end of run()


  private void makeContentCurrent()
  // make the rendering context current for this thread
  {
    try {
      while (context.makeCurrent() == GLContext.CONTEXT_NOT_CURRENT) {
        System.out.println("Context not yet current...");
        Thread.sleep(100);
      }
    }
    catch (InterruptedException e)
    { e.printStackTrace(); }
  }  // end of makeContentCurrent()


  private void initRender()
  /* rendering initialization (similar to the init() callback
     in GLEventListener) */
  {
    makeContentCurrent();

    gl = context.getGL();
    glu = new GLU();

    resizeView();

    gl.glClearColor(0.17f, 0.65f, 0.92f, 0.0f);  // sky colour background

    // z- (depth) buffer initialization for hidden surface removal
    gl.glEnable(GL.GL_DEPTH_TEST); 
    // gl.glClearDepth(1.0f);
    // gl.glDepthFunc(GL.GL_LEQUAL);   // type of depth testing
    // gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);

    // create a display list for drawing the cube
    cubeDList = gl.glGenLists(1);
    gl.glNewList(cubeDList, GL.GL_COMPILE);
      drawColourCube(gl);
    gl.glEndList();

    /* release the context, otherwise the AWT lock on X11
       will not be released */
    context.release();
  }  // end of initRender()


  private void resizeView()
  {
    gl.glViewport(0, 0, panelWidth, panelHeight);  // size of drawing area 

    gl.glMatrixMode(GL.GL_PROJECTION);  
    gl.glLoadIdentity(); 
    glu.gluPerspective(45.0, (float)panelWidth/(float)panelHeight,  1, 100); // 5, 100); 
              // fov, aspect ratio, near & far clipping planes
  }  // end of resizeView()


  // ---------------- frame-based rendering -----------------------


  private void renderLoop()
  /* Repeatedly update, render, add to canvas, and sleep, keeping to a fixed
     period as closely as possible. Gather and report statistics.
  */
  {
    // timing-related variables
    long beforeTime, afterTime, timeDiff, sleepTime;
    long overSleepTime = 0L;
    int noDelays = 0;
    long excess = 0L;

    gameStartTime = System.nanoTime();     // J3DTimer.getValue();
    prevStatsTime = gameStartTime;
    beforeTime = gameStartTime;

    isRunning = true;

    while(isRunning) {
      makeContentCurrent();
      gameUpdate();

      renderScene();           // rendering
      drawable.swapBuffers();  // put the scene onto the canvas
          // swap front and back buffers, making the new rendering visible

      afterTime = System.nanoTime(); 
      timeDiff = afterTime - beforeTime;
      sleepTime = (period - timeDiff) - overSleepTime;  

      if (sleepTime > 0) {   // some time left in this cycle
        try {
          Thread.sleep(sleepTime/1000000L);  // nano -> ms
        }
        catch(InterruptedException ex){}
        overSleepTime = (System.nanoTime() - afterTime) - sleepTime;
      }
      else {    // sleepTime <= 0; this cycle took longer than the period
        excess -= sleepTime;  // store excess time value
        overSleepTime = 0L;

        if (++noDelays >= NO_DELAYS_PER_YIELD) {
          Thread.yield();   // give another thread a chance to run
          noDelays = 0;
        }
      }

      beforeTime = System.nanoTime();    // J3DTimer.getValue();

      /* If the rendering is taking too long, update the game state
         without rendering it, to get the updates/sec nearer to
         the required FPS. */
      int skips = 0;
      while((excess > period) && (skips < MAX_RENDER_SKIPS)) {
        excess -= period;
	    gameUpdate();    // update state but don't render
        skips++;
      }
      rendersSkipped += skips;

      /* release the context, otherwise the AWT lock on X11
         will not be released */
      context.release();

      storeStats();
	}

    printStats();
  } // end of renderLoop()


  private void gameUpdate() 
  { if (!isPaused && !gameOver) {
      // update the rotations
      rotX = (rotX + incrX) % 360.0f;
      rotY = (rotY + incrY) % 360.0f;
      rotZ = (rotZ + incrZ) % 360.0f;
      top.setRots(rotX, rotY, rotZ);
    }
  }  // end of gameUpdate()



  // ------------------ rendering methods -----------------------------


  private void renderScene() 
  { 
    if (context.getCurrent() == null) {
      System.out.println("Current context is null");
      System.exit(0);
    }

    if (isResized) {    // resize the drawable if necessary
      resizeView();
      isResized = false;
    }

    // clear colour and depth buffers
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

    gl.glMatrixMode(GL.GL_MODELVIEW);  
    gl.glLoadIdentity(); 

    glu.gluLookAt(0,0,Z_DIST, 0,0,0, 0,1,0);   // position camera

    // apply rotations to the x,y,z axes
    gl.glRotatef(rotX, 1.0f, 0.0f, 0.0f);
    gl.glRotatef(rotY, 0.0f, 1.0f, 0.0f);
    gl.glRotatef(rotZ, 0.0f, 0.0f, 1.0f);
    gl.glCallList(cubeDList);  // execute display list for drawing cube
    // drawColourCube(gl);

    if (gameOver)
      System.out.println("Game Over");
  } // end of renderScene()



  private void drawColourCube(GL gl)
  // six-sided cube, with a different colour on each face
  {
    gl.glColor3f(1.0f, 0.0f, 0.0f);   // red
    drawPolygon(gl, 0, 3, 2, 1);      // front face

    gl.glColor3f(0.0f, 1.0f, 0.0f);   // green
    drawPolygon(gl, 2, 3, 7, 6);      // right

    gl.glColor3f(0.0f, 0.0f, 1.0f);   // blue
    drawPolygon(gl, 3, 0, 4, 7);      // bottom

    gl.glColor3f(1.0f, 1.0f, 0.0f);   // yellow
    drawPolygon(gl, 1, 2, 6, 5);      // top

    gl.glColor3f(0.0f, 1.0f, 1.0f);   // light blue
    drawPolygon(gl, 4, 5, 6, 7);      // back

    gl.glColor3f(1.0f, 0.0f, 1.0f);   // purple
    drawPolygon(gl, 5, 4, 0, 1);      // left
  } // end of drawColourCube()


  private void drawPolygon(GL gl, int vIdx0, int vIdx1, int vIdx2, int vIdx3)
  // the polygon verticies come from the verts[] array
  {
    gl.glBegin(GL.GL_POLYGON);
      gl.glVertex3f( verts[vIdx0][0], verts[vIdx0][1], verts[vIdx0][2] );
      gl.glVertex3f( verts[vIdx1][0], verts[vIdx1][1], verts[vIdx1][2] );
      gl.glVertex3f( verts[vIdx2][0], verts[vIdx2][1], verts[vIdx2][2] );
      gl.glVertex3f( verts[vIdx3][0], verts[vIdx3][1], verts[vIdx3][2] );
    gl.glEnd();
  }  // end of drawPolygon()


  // ----------------- statistics methods ------------------------

  private void storeStats()
  /* The statistics:
       - the summed periods for all the iterations in this interval
         (period is the amount of time a single frame iteration should take), 
         the actual elapsed time in this interval, 
         the error between these two numbers;

       - the total frame count, which is the total number of calls to run();

       - the frames skipped in this interval, the total number of frames
         skipped. A frame skip is a game update without a corresponding render;

       - the FPS (frames/sec) and UPS (updates/sec) for this interval, 
         the average FPS & UPS over the last NUM_FPSs intervals.

     The data is collected every MAX_STATS_INTERVAL  (1 sec).
     ----- CHANGES ----
     Do not show any output; leave it to printStats().
  */
  { 
    frameCount++;
    statsInterval += period;

    if (statsInterval >= MAX_STATS_INTERVAL) {     // record stats every MAX_STATS_INTERVAL
      long timeNow = System.nanoTime();    // J3DTimer.getValue();
      timeSpentInGame = (int) ((timeNow - gameStartTime)/1000000000L);  // ns --> secs

      long realElapsedTime = timeNow - prevStatsTime;   // time since last stats collection
      totalElapsedTime += realElapsedTime;

      double timingError = 
         ((double)(realElapsedTime - statsInterval) / statsInterval) * 100.0;

      totalRendersSkipped += rendersSkipped;

      double actualFPS = 0;     // calculate the latest FPS and UPS
      double actualUPS = 0;
      if (totalElapsedTime > 0) {
        actualFPS = (((double)frameCount / totalElapsedTime) * 1000000000L);
        actualUPS = (((double)(frameCount + totalRendersSkipped) / totalElapsedTime) 
                                                             * 1000000000L);
      }

      // store the latest FPS and UPS
      fpsStore[ (int)statsCount%NUM_FPS ] = actualFPS;
      upsStore[ (int)statsCount%NUM_FPS ] = actualUPS;
      statsCount = statsCount+1;

      double totalFPS = 0.0;     // total the stored FPSs and UPSs
      double totalUPS = 0.0;
      for (int i=0; i < NUM_FPS; i++) {
        totalFPS += fpsStore[i];
        totalUPS += upsStore[i];
      }

      if (statsCount < NUM_FPS) { // obtain the average FPS and UPS
        averageFPS = totalFPS/statsCount;
        averageUPS = totalUPS/statsCount;
      }
      else {
        averageFPS = totalFPS/NUM_FPS;
        averageUPS = totalUPS/NUM_FPS;
      }
/*
      System.out.println(timedf.format( (double) statsInterval/1000000000L) + " " + 
                    timedf.format((double) realElapsedTime/1000000000L) + "s " + 
			        df.format(timingError) + "% " + 
                    frameCount + "c " +
                    rendersSkipped + "/" + totalRendersSkipped + " skip; " +
                    df.format(actualFPS) + " " + df.format(averageFPS) + " afps; " + 
                    df.format(actualUPS) + " " + df.format(averageUPS) + " aups" );
*/
      rendersSkipped = 0;
      prevStatsTime = timeNow;
      statsInterval = 0L;   // reset
    }
  }  // end of storeStats()


  private void printStats()
  {
    // System.out.println("Frame Count/Loss: " + frameCount + " / " + totalRendersSkipped);
	System.out.println("Average FPS: " + df.format(averageFPS));
	System.out.println("Average UPS: " + df.format(averageUPS));
    System.out.println("Time Spent: " + timeSpentInGame + " secs");
  }  // end of printStats()

} // end of CubeCanvasGL class

