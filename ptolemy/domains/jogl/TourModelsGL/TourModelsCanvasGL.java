package ptolemy.domains.jogl.TourModelsGL;


// TourModelsCanvasGL.java
// Andrew Davison, December 2006, ad@fivedots.coe.psu.ac.th

/* This class bears many similarities to TourCanvasGL from
   the last chapter.

   A single thread is spawned which initialises the rendering
   and then loops, carrying out update, render, sleep with
   a fixed period.

   The active rendering framework comes from chapter 2
   of "Killing Game Programming in Java" (KGPJ). There's a
   version online at http://fivedots.coe.psu.ac.th/~ad/jg/ch1/.

   The statistics code is lifted from chapter 3 of KGPJ (p.54-56), which
   is online at http://fivedots.coe.psu.ac.th/~ad/jg/ch02/.
   The time calculations in this version use System.nanoTime() rather
   than J3DTimer.getValue(), so require J2SE 5.0.

   The canvas displays a 3D world consisting of:

     * a green and blue checkboard floor with a red square at its center
       and numbers along its z- and z- axes

     * user navigation using keys to move forward, backwards, left,
       right, and turn left and right. The user cannot move
       off the checkboard. Unlike in TourGL (the example from the last
       chapter), the user cannot move up or down.

     * the user can quit the game by pressing 'q', ctrl-c, the 'esc' key,
       or by clicking the close box

    ------
    Features not in TourCanvasGL:

     * the loading, positioning and drawing of 4 OBJ models:
         - a racing car, a rose in a vase, a penguin, and a couch
         - the loading is done inside initRender(), and the
           drawing in drawModels();

     * the ability to select (pick) the penguin or the couch
       with the mouse (see pickModels());

     * the use of JOAL (3D sound) and my JOALSoundMan class to
       make the penguin chirp. The sound is loaded, positioned, and
       started inside initRender().
       The listener angle is initialized in initViewerPosn() and
       updated in processKey(). The listener is positioned and
       orientated in renderScene().


     * a deep, red-ish fog (see addFog())
*/

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.IntBuffer;
import java.text.DecimalFormat;

import javax.media.opengl.GL;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawable;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.glu.GLU;

import ptolemy.domains.jogl.objLoader.OBJModel;

import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.GLUT;


public class TourModelsCanvasGL extends Canvas implements Runnable
{
  // for the floor
  private final static int FLOOR_LEN = 20;  // should be even
  private final static int BLUE_TILE = 0;   // floor tile colour types
  private final static int GREEN_TILE = 1;

  // camera related
  private final static double SPEED = 0.4;   // for camera movement
  private final static double LOOK_AT_DIST = 100.0;
  private final static double Z_POS = 9.0;
  private final static double ANGLE_INCR = 5.0;   // degrees

  // picking related
  private static final int BUFSIZE = 512;   // size of selection buffer
  private static final int COUCH_ID = 1;    // names (IDs) for pickable models
  private static final int PENGUIN_ID = 2;


  // statistics constants
  private static long MAX_STATS_INTERVAL = 1000000000L;
  // private static long MAX_STATS_INTERVAL = 1000L;
    // record stats every 1 second (roughly)

  private static final int NO_DELAYS_PER_YIELD = 16;
  /* Number of renders with a sleep delay of 0 ms before the
     animation thread yields to other running threads. */

  private static final int MAX_RENDER_SKIPS = 5;   // was 2;
    // no. of renders that can be skipped in any one animation loop
    // i.e the games state is updated but not rendered

  private static final int NUM_FPS = 10;
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
  private DecimalFormat df4 = new DecimalFormat("0.####");  // 4 dp

  // used at game termination
  private volatile boolean gameOver = false;
  //private int score = 0;

  private long period;        // period between drawing in _nanosecs_

  private Thread animator;              // the thread that performs the animation
  private volatile boolean isRunning = false;   // used to stop the animation thread
  private volatile boolean isPaused = false;

  // OpenGL
  private GLDrawable drawable;  // the rendering 'surface'
  private GLContext context;    // the rendering context (holds rendering state info)
  private GL gl;
  private GLU glu;
  private GLUT glut;

  // camera movement
  private double xPlayer, yPlayer, zPlayer;
  private double xLookAt, yLookAt, zLookAt;
  private double xStep, zStep;
  private double viewAngle;

  // window sizing
  private boolean isResized = false;
  private int panelWidth, panelHeight;

  // the four OBJ models
  private OBJModel couchModel, carModel, penguinModel, roseVaseModel;

  // 3D sound
  //private JOALSoundMan soundMan;
  private double listenerAngle;

  // picking
  private boolean inSelectionMode = false;
  private int xCursor, yCursor;
  private IntBuffer selectBuffer;



  public TourModelsCanvasGL(long period, int width, int height,
                         GraphicsConfiguration config, GLCapabilities caps)
  {
    super(config);

    this.period = period;
    panelWidth = width;
    panelHeight = height;

    setBackground(Color.white);

    //soundMan = new JOALSoundMan();

    // get a rendering surface and a context for this canvas
    drawable = GLDrawableFactory.getFactory().getGLDrawable(this, caps, null);
    context = drawable.createContext(null);

    initViewerPosn();

        addKeyListener( new KeyAdapter() {
       public void keyPressed(KeyEvent e)
       { processKey(e);  }
     });

    addMouseListener( new MouseAdapter() {   // used for picking
      public void mousePressed(MouseEvent e)
      { mousePress(e); }
    });

    // statistics initialization
    fpsStore = new double[NUM_FPS];
    upsStore = new double[NUM_FPS];
    for (int i=0; i < NUM_FPS; i++) {
      fpsStore[i] = 0.0;
      upsStore[i] = 0.0;
    }
  } // end of TourModelsCanvasGL()



  private void initViewerPosn()
  /* Specify the camera (player) position, the x- and z- step
     distance, and the position being looked at. */
  {
    xPlayer = 0; yPlayer = 1; zPlayer =  Z_POS;    // camera posn

    viewAngle = -90.0;   // along -z axis
    xStep = Math.cos( Math.toRadians(viewAngle));  // step distances
    zStep = Math.sin( Math.toRadians(viewAngle));

    xLookAt = xPlayer + (LOOK_AT_DIST * xStep);   // look-at posn
    yLookAt = 0;
    zLookAt = zPlayer + (LOOK_AT_DIST * zStep);

    // listener starts by looking along the -z axis by default
    listenerAngle = 0;
  }  // end of initViewerPosn()


  public void addNotify()
  // wait for the canvas to be added to the JPanel before starting
  {
    super.addNotify();      // creates the peer
    drawable.setRealized(true);  // the canvas can now be rendering into

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
     if (h==0)
       h = 1;  // to avoid division by 0 in aspect ratio in resizeView()
     panelWidth = w; panelHeight = h;
  }  // end of reshape()


  public void update(Graphics g) { }


  private void processKey(KeyEvent e)
  // handles termination, and the game-play keys
  {
    int keyCode = e.getKeyCode();

    // termination keys
        // listen for esc, q, end, ctrl-c on the canvas
    if ((keyCode == KeyEvent.VK_ESCAPE) || (keyCode == KeyEvent.VK_Q) ||
        (keyCode == KeyEvent.VK_END) ||
        ((keyCode == KeyEvent.VK_C) && e.isControlDown()) )
      isRunning = false;

    // game-play keys
    if (isRunning) {
      // move based on the arrow key pressed
      if (keyCode == KeyEvent.VK_LEFT) {    // left
        if (e.isControlDown()) {   // translate left
          xPlayer += zStep * SPEED;
          zPlayer -= xStep * SPEED;
        }
        else {  // turn left
          viewAngle -= ANGLE_INCR;
          xStep = Math.cos( Math.toRadians(viewAngle));
          zStep = Math.sin( Math.toRadians(viewAngle));
          listenerAngle += ANGLE_INCR;
        }
      }
      else if (keyCode == KeyEvent.VK_RIGHT) {  // right
        if (e.isControlDown()) {   // translate right
          xPlayer -= zStep * SPEED;
          zPlayer += xStep * SPEED;
        }
        else {  // turn right
          viewAngle += ANGLE_INCR;
          xStep = Math.cos( Math.toRadians(viewAngle));
          zStep = Math.sin( Math.toRadians(viewAngle));
          listenerAngle -= ANGLE_INCR;
        }
      }
      else if (keyCode == KeyEvent.VK_UP) {   // move forward
        xPlayer += xStep * SPEED;
        zPlayer += zStep * SPEED;
      }
      else if (keyCode == KeyEvent.VK_DOWN) {  // move backwards
        xPlayer -= xStep * SPEED;
        zPlayer -= zStep * SPEED;
      }

      // don't allow player to walk off the edge of the world
      if (xPlayer < -FLOOR_LEN/2)
        xPlayer = -FLOOR_LEN/2;
      else if (xPlayer > FLOOR_LEN/2)
        xPlayer = FLOOR_LEN/2;

      if (zPlayer < -FLOOR_LEN/2)
        zPlayer = -FLOOR_LEN/2;
      else if (zPlayer > FLOOR_LEN/2)
        zPlayer = FLOOR_LEN/2;

      // new look-at point
      xLookAt = xPlayer + (xStep * LOOK_AT_DIST);
      zLookAt = zPlayer + (zStep * LOOK_AT_DIST);
    }
  }  // end of processKey()




  public void run()
  // initialize rendering and start frame generation
  {
    initRender();
    renderLoop();

    // stop the 3D sound at the end
    //soundMan.cleanUp();

    // discard the rendering context and exit
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
    glut = new GLUT();

    resizeView();

    // various background colours
    // gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);     // black
    // gl.glClearColor(0.17f, 0.65f, 0.92f, 1.0f);  // sky blue
    gl.glClearColor(0.7f, 0.6f, 0.6f, 1.0f);        // same colour as the fog

    // z- (depth) selectBuffer initialization for hidden surface removal
    gl.glEnable(GL.GL_DEPTH_TEST);

    gl.glShadeModel(GL.GL_SMOOTH);    // use smooth shading

    addLight();
    //addFog();

    // load the four OBJ models
    couchModel = new OBJModel("couch", 2.0f, gl, false);
    carModel = new OBJModel("formula", 4.0f, gl, false);
    penguinModel = new OBJModel("penguin", gl);
    roseVaseModel = new OBJModel("rose+vase", 3.2f, gl, false);

    // position the penguin sound source at (2,0,0), and start it playing
//    if (!soundMan.load("penguin", 2, 0, 0, true))
//      System.out.println("Penguin sound not found");
//    else
//      soundMan.play("penguin");


    /* release the context, otherwise the AWT lock on X11
       will not be released */
    context.release();
  }  // end of initRender()


  private void resizeView()
  {
    gl.glViewport(0, 0, panelWidth, panelHeight);    // size of drawing area

    gl.glMatrixMode(GL.GL_PROJECTION);
    gl.glLoadIdentity();
    glu.gluPerspective(45.0, (float)panelWidth/(float)panelHeight, 1, 100);
              // fov, aspect ratio, near & far clipping planes
       /* if you change the arguments of gluPerspective() here, then make
          sure to do the same to its call in startPicking() */
  }  // end of resizeView()



  private void addLight()
  /* set up a point source with ambient, diffuse, and specular
     colour components */
  {
    // enable a single light source
    gl.glEnable(GL.GL_LIGHTING);
    gl.glEnable(GL.GL_LIGHT0);

    float[] grayLight = {0.1f, 0.1f, 0.1f, 1.0f};  // weak gray ambient
    gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, grayLight, 0);

    float[] whiteLight = {1.0f, 1.0f, 1.0f, 1.0f};  // bright white diffuse & specular
    gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, whiteLight, 0);
    gl.glLightfv(GL.GL_LIGHT0, GL.GL_SPECULAR, whiteLight, 0);

    float lightPos[] = {1.0f, 1.0f, 1.0f, 0.0f};  // top right front _direction_
    gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, lightPos, 0);
  }  // end of addLight()


//  private void addFog()
//  {
//    gl.glEnable(GL.GL_FOG);
//
//    gl.glFogi(GL.GL_FOG_MODE, GL.GL_EXP2);
//      // possible modes are: GL.GL_LINEAR, GL.GL_EXP, GL.GL_EXP2
//
//    float[] fogColor = {0.7f, 0.6f, 0.6f, 1.0f};  // same colour as background
//    gl.glFogfv(GL.GL_FOG_COLOR, fogColor, 0);
//
//    gl.glFogf(GL.GL_FOG_DENSITY, 0.35f);
//
//    gl.glFogf(GL.GL_FOG_START, 1.0f);  // start depth
//    gl.glFogf(GL.GL_FOG_END, 5.0f);           // end depth
//
//    gl.glHint(GL.GL_FOG_HINT, GL.GL_DONT_CARE);
//      // possible hints are: GL.GL_DONT_CARE, GL.GL_NICEST or GL.GL_FASTEST
//  }  // end of addFog()


  // ---------------- frame-based rendering -----------------------


  private void renderLoop()
  /* Repeatedly update, render, and sleep, keeping to a fixed
     period as closely as possible. gather and report statistics.
  */
  {
    // timing-related variables
    long beforeTime, afterTime, timeDiff, sleepTime;
    long overSleepTime = 0L;
    int noDelays = 0;
    long excess = 0L;

    gameStartTime = System.nanoTime();
    prevStatsTime = gameStartTime;
    beforeTime = gameStartTime;

    isRunning = true;

    while (isRunning) {
      makeContentCurrent();
      gameUpdate();

      renderScene();          // rendering
      // drawable.swapBuffers();
             // this call has been moved into renderScene()

      afterTime = System.nanoTime();
      timeDiff = afterTime - beforeTime;
      sleepTime = (period - timeDiff) - overSleepTime;

      if (sleepTime > 0) {   // some time left in this cycle
        try {
          Thread.sleep(sleepTime/1000000L);  // nano -> ms
        }
        catch (InterruptedException ex){}
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
      while ((excess > period) && (skips < MAX_RENDER_SKIPS)) {
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
      // do nothing;
    }
  }  // end of gameUpdate()



  // ------------------ rendering methods -----------------------------


  private void renderScene()
  {
    if (GLContext.getCurrent() == null) {
      System.out.println("Current context is null");
      System.exit(0);
    }

    if (isResized) {
      resizeView();
      isResized = false;
    }

    // clear colour and depth buffers
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glLoadIdentity();

    glu.gluLookAt(xPlayer, yPlayer, zPlayer,
                  xLookAt, yLookAt, zLookAt, 0,1,0);    // position camera

    // position and orientate the audio listener
    //soundMan.setListenerPos( (float)xPlayer, (float)zPlayer );
    //soundMan.setListenerOri( (int) listenerAngle );    // double-->int rounding


    if (inSelectionMode)
      pickModels();
    else {   // normal rendering
      drawFloor();
      drawModels();
      drawable.swapBuffers(); // put the scene onto the canvas
          // swap front and back buffers, making the new rendering visible
       /* This call was moved here from renderLoop() so it's only called
          when the 'real' scene needs to be shown; the 'pick' scene is
          never swapped. If it is then there's a very distinct flicker as
          rendering switches between the two scenes.
      */
    }

    if (gameOver)
      System.out.println("Game Over!!");
  } // end of renderScene()


  private void drawModels()
  /* Draw four OBJ models (a couch, a racing car, a penguin,
     and a rose in a vase). The couch and penguin code is in
     separate methods so it can also be employed in pickModels().
  */
  {
    drawCouch();

    // the racing car
    gl.glPushMatrix();
      gl.glTranslatef(-3.0f, 0.5f, -3.0f);   // up, left, back
      carModel.draw(gl);
    gl.glPopMatrix();

    drawPenguin();

    // the rose vase
    gl.glPushMatrix();
      gl.glTranslatef(0f, 1.6f, 0f);   // up
      roseVaseModel.draw(gl);
    gl.glPopMatrix();

  }  // end of drawModels()


  private void drawCouch()
  {
    gl.glPushMatrix();
      gl.glTranslatef(4.0f, 0.5f, -4.0f);   // up, right, back
      gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);  // rotate the model backwards
      // gl.glScalef(0.5f, 0.5f, 0.5f);    // reduce rendering size
      couchModel.draw(gl);
    gl.glPopMatrix();
  }  // end of drawCouch()


  private void drawPenguin()
  {
    gl.glPushMatrix();
      gl.glTranslatef(2.0f, 0.5f, 0f);   // up, right
      gl.glRotatef(-90.0f, 0.0f, 1.0f, 0.0f);  // rotate the model to face left
      penguinModel.draw(gl);
    gl.glPopMatrix();
  }  // end of drawPenguin()


  // ------------------ the checkboard floor --------------------
  /* This code is almost a direct translation of the CheckerFloor class
     in the Java 3D Checkers3D example in chapter 15 of KGPJ;
     online at http://fivedots.coe.psu.ac.th/~ad/jg/ch8/).
  */

  private void drawFloor()
  /* Create tiles, the origin marker, then the axes labels.
     The tiles are in a checkboard pattern, alternating between
     green and blue.
  */
  {
    gl.glDisable(GL.GL_LIGHTING);

    drawTiles(BLUE_TILE);   // blue tiles
    drawTiles(GREEN_TILE);  // green
    addOriginMarker();
    labelAxes();

    gl.glEnable(GL.GL_LIGHTING);
  }  // end of CheckerFloor()


  private void drawTiles(int drawType)
  /* Create a series of quads, all with the same colour. They are
     spaced out over a FLOOR_LEN*FLOOR_LEN area, with the area centered
     at (0,0) on the XZ plane, and y==0.
  */
  {
    if (drawType == BLUE_TILE)
      gl.glColor3f(0.0f, 0.1f, 0.4f);
    else  // green
      gl.glColor3f(0.0f, 0.5f, 0.1f);

    gl.glBegin(GL.GL_QUADS);
    boolean aBlueTile;
    for (int z=-FLOOR_LEN/2; z <= (FLOOR_LEN/2)-1; z++) {
      aBlueTile = (z%2 == 0)? true : false;    // set colour type for new row
      for (int x=-FLOOR_LEN/2; x <= (FLOOR_LEN/2)-1; x++) {
        if (aBlueTile && (drawType == BLUE_TILE))  // blue tile and drawing blue
          drawTile(x, z);
        else if (!aBlueTile && (drawType == GREEN_TILE))   // green
          drawTile(x, z);
        aBlueTile = !aBlueTile;
      }
    }
    gl.glEnd();
  }  // end of drawTiles()


  private void drawTile(int x, int z)
  /* Coords for a single blue or green square;
    its top left hand corner at (x,0,z). */
  {
    // points created in counter-clockwise order
    gl.glVertex3f(x, 0.0f, z+1.0f);   // bottom left point
    gl.glVertex3f(x+1.0f, 0.0f, z+1.0f);
    gl.glVertex3f(x+1.0f, 0.0f, z);
    gl.glVertex3f(x, 0.0f, z);
  }  // end of drawTile()


  private void addOriginMarker()
  /* A red square centered at (0,0.01,0), of length 0.5, lieing
     flat on the XZ plane. */
  {
    gl.glColor3f(0.8f, 0.4f, 0.3f);   // medium red
    gl.glBegin(GL.GL_QUADS);

    // points created counter-clockwise, a bit above the floor
    gl.glVertex3f(-0.25f, 0.01f, 0.25f);  // bottom left point
    gl.glVertex3f(0.25f, 0.01f, 0.25f);
    gl.glVertex3f(0.25f, 0.01f, -0.25f);
    gl.glVertex3f(-0.25f, 0.01f, -0.25f);

    gl.glEnd();
  } // end of addOriginMarker();


  private void labelAxes()
  /* Place numbers along the X- and Z-axes at the integer positions.
     The axes are drawn using a stroke font, so the shape can be scaled,
     and the thickness of the line increased (to make it look 'bold').
  */
  {
    gl.glColor3f(1.0f, 1.0f, 1.0f);   // white
    gl.glLineWidth(3.0f);  // thicken the line

    for (int i=-FLOOR_LEN/2; i <= FLOOR_LEN/2; i++)
      drawAxisText(""+i, (float)i, 0.0f, 0.0f);  // along x-axis


    for (int i=-FLOOR_LEN/2; i <= FLOOR_LEN/2; i++)
      drawAxisText(""+i, 0.0f, 0.0f, (float)i);  // along z-axis

    gl.glLineWidth(1.0f);  // reset line width
  }  // end of labelAxes()



  private void drawAxisText(String txt, float x, float y, float z)
  /* Draw txt at (x,y,z), with the text centered in the x-direction,
     facing along the +z axis.
  */
  {
    gl.glPushMatrix();
    gl.glTranslatef(x, y, z);    // position the text
    gl.glScalef(0.0015f, 0.0015f, 0.0015f);    // reduce rendering size

    // center text on the x-axis
    float width = glut.glutStrokeLength(GLUT.STROKE_MONO_ROMAN, txt);
    gl.glTranslatef(-width/2.0f, 0, 0);

    // render the text using a stroke font
    for (int i = 0; i < txt.length(); i++) {
      char ch = txt.charAt(i);
      glut.glutStrokeCharacter(GLUT.STROKE_MONO_ROMAN, ch);
    }

    gl.glPopMatrix();   // restore model view
  } // end of drawAxisText()



  // ------------------ picking methods --------------------------


  private void mousePress(MouseEvent e)
  /* record the cursor's (x,y) position, and switch to
     selection mode when the next render occurs */
  {
    xCursor = e.getX();
    yCursor = e.getY();
    // System.out.println("cursor (x,y): (" + xCursor + "," + yCursor + ")");
    inSelectionMode = true;
  }  // end of mousePress()


  private void pickModels()
  // draw the couch and penguin models in selection mode
  {
    startPicking();

    gl.glPushName(COUCH_ID);
    drawCouch();
    gl.glPopName();

    gl.glPushName(PENGUIN_ID);
    drawPenguin();
    gl.glPopName();

    endPicking();
  }  // end of pickModels()


  private void startPicking()
  /* Switch to selection mode, initialize necessary data structures
     and create a 'picking' area using the viewport. */
  {
    // initialize the selection buffer
    // int selectBuf[] = new int[BUFSIZE];
    selectBuffer = BufferUtil.newIntBuffer(BUFSIZE);
    gl.glSelectBuffer(BUFSIZE, selectBuffer);

    gl.glRenderMode(GL.GL_SELECT);  // switch to selection mode

    gl.glInitNames();   // make an empty name stack
    // gl.glPushName(-1);  // not needed

    /* redefine the viewing volume so it only renders a
       small area around the place where the mouse was clicked */

    // save the original projection matrix
    gl.glMatrixMode(GL.GL_PROJECTION);
    gl.glPushMatrix();
    gl.glLoadIdentity();

    // get the current viewport
    int viewport[] = new int[4];
    gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);

    // create a 5x5 pixel picking area near the cursor location
    glu.gluPickMatrix((double) xCursor,
                      (double) (viewport[3] - yCursor),
                      5.0, 5.0, viewport, 0);
      /* The y-value uses an 'inverted' yCursor to transform the y-coordinates
         origin from the upper left corner into the bottom left corner. */

    /* set projection (perspective or orthogonal) exactly as it is in
       normal rendering (i.e. duplicate the gluPerspective() call
       in resizeView()) */
    glu.gluPerspective(45.0, (float)panelWidth/(float)panelHeight, 1, 100);

    gl.glMatrixMode(GL.GL_MODELVIEW);   // restore model view
  }  // end of startPicking()



  private void endPicking()
  /* Switch back to normal rendering, and extract 'hit information
     generated because of picking. */
  {
    // restore original projection matrix
    gl.glMatrixMode(GL.GL_PROJECTION);
    gl.glPopMatrix();
    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glFlush();

    // return to normal rendering mode, and process hits
    int numHits = gl.glRenderMode(GL.GL_RENDER);
    processHits(numHits);

    inSelectionMode = false;
  }  // end of endPicking()



  public void processHits(int numHits)
  /* Display all the hit records, and report the name of
     the 'thing' that was picked closest to the viewport.

     Each hit record contains:
       - the number of different names for the thing hit (usually only 1)
       - minimum and maximum depths of the hit
       - the names for the thing hit (stored on the name stack)
  */
  {
    if (numHits == 0)
      return;   // no hits to process

    System.out.println("No. of hits: " + numHits);

    // storage for the name ID closest to the viewport
    int selectedNameID = -1;    // dummy initial values
    float smallestZ = -1.0f;

    boolean isFirstLoop = true;
    int offset = 0;

    /* iterate through the hit records, saving the smallest z value
       and the name ID associated with it */
    for (int i=0; i < numHits; i++) {
      System.out.println("Hit: " + (i + 1));

      int numNames = selectBuffer.get(offset);
      offset++;
      // System.out.println(" No. of names: " + numNames);

      // minZ and maxZ are taken from the Z buffer
      float minZ = getDepth(offset);
      offset++;

      // store the smallest z value
      if (isFirstLoop) {
        smallestZ = minZ;
        isFirstLoop = false;
      }
      else {
        if (minZ < smallestZ)
          smallestZ = minZ;
      }

      float maxZ = getDepth(offset);
      offset++;

      System.out.println(" minZ: " + df4.format(minZ) +
                        "; maxZ: " + df4.format(maxZ));

      // print name IDs stored on the name stack
      System.out.print(" Name(s): ");
      int nameID;
      for (int j=0; j < numNames; j++) {
        nameID = selectBuffer.get(offset);
        System.out.print( idToString(nameID) );
        if (j == (numNames-1)) {  // if the last one (the top element on the stack)
          if (smallestZ == minZ)    // is this the smallest min z?
            selectedNameID = nameID;  // then store it's name ID
        }
        System.out.print(" ");
        offset++;
      }
      System.out.println();
    }

    System.out.println("Picked the " + idToString(selectedNameID));
    System.out.println("-------------");
  } // end of processHits()


  private float getDepth(int offset)
  /* A depth is in the range 0 to 1, but is stored
     after being multiplied by 2^32 -1 and rounded to
     the nearest integer. The number will be negative due to
     the multiplication and being stored as an integer.
  */
  {
    long depth = (long) selectBuffer.get(offset);  // large -ve number
    return  (1.0f + ((float) depth / 0x7fffffff));
                            // return as a float between 0 and 1
  }  // end of getDepth()



  private String idToString(int nameID)
  // convert name ID integer to a string
  {
    if (nameID == COUCH_ID)
      return "couch";
    else if (nameID == PENGUIN_ID)
      return "penguin";

    // we should not reach this point
    return "nameID " + nameID;
  }  // end of idToString()


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
  */
  {
    frameCount++;
    statsInterval += period;

    if (statsInterval >= MAX_STATS_INTERVAL) {     // record stats every MAX_STATS_INTERVAL
      long timeNow = System.nanoTime();    // J3DTimer.getValue();
      timeSpentInGame = (int) ((timeNow - gameStartTime)/1000000000L);  // ns --> secs

      long realElapsedTime = timeNow - prevStatsTime;   // time since last stats collection
      totalElapsedTime += realElapsedTime;

      //double timingError =
      //   ((double)(realElapsedTime - statsInterval) / statsInterval) * 100.0;

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
      System.out.println(df4.format( (double) statsInterval/1000000000L) + " " +
                    df4.format((double) realElapsedTime/1000000000L) + "s " +
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

} // end of TourModelsCanvasGL class

