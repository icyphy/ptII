package ptolemy.domains.gro;

import javax.media.opengl.GL;
 import javax.media.opengl.GLEventListener;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.glu.GLU;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 public class JavaRenderer 
 implements GLEventListener, KeyListener {
    private float rotateT = 0.0f;
    private static final GLU glu = new GLU();
 
    public void display(GLAutoDrawable gLDrawable) {
        final GL gl = gLDrawable.getGL();
        final GL gl2 = gLDrawable.getGL();
        render(gl, 0);
        render(gl2, 1);
 
        gl.glEnd();
 
        rotateT += 0.2f;
    }

    private void render(final GL gl, int id) {
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        gl.glTranslatef(0.0f, 0.0f, -5.0f);
 
        gl.glRotatef(rotateT, /*1.0f*/ (float) id, 0.0f, 0.0f);
        gl.glRotatef(rotateT, 0.0f, /*1.0f*/ (float) id, 0.0f);
        gl.glRotatef(rotateT, 0.0f, 0.0f, /*1.0f*/ (float) id);
        gl.glRotatef(rotateT, 0.0f, /*1.0f*/ (float) id, 0.0f);
 
        gl.glBegin(GL.GL_TRIANGLES);
 
        // Front
        gl.glColor3f(0.0f, 1.0f, 1.0f); 
        gl.glVertex3f(0.0f, 1.0f, 0.0f);
        gl.glColor3f(0.0f, 0.0f, 1.0f); 
        gl.glVertex3f(-1.0f, -1.0f, 1.0f);
        gl.glColor3f(0.0f, 0.0f, 0.0f); 
        gl.glVertex3f(1.0f, -1.0f, 1.0f);
 
        // Right Side Facing Front
        gl.glColor3f(0.0f, 1.0f, 1.0f); 
        gl.glVertex3f(0.0f, 1.0f, 0.0f);
        gl.glColor3f(0.0f, 0.0f, 1.0f); 
        gl.glVertex3f(1.0f, -1.0f, 1.0f);
        gl.glColor3f(0.0f, 0.0f, 0.0f); 
        gl.glVertex3f(0.0f, -1.0f, -1.0f);
 
        // Left Side Facing Front
        gl.glColor3f(0.0f, 1.0f, 1.0f); 
        gl.glVertex3f(0.0f, 1.0f, 0.0f);
        gl.glColor3f(0.0f, 0.0f, 1.0f); 
        gl.glVertex3f(0.0f, -1.0f, -1.0f);
        gl.glColor3f(0.0f, 0.0f, 0.0f); 
        gl.glVertex3f(-1.0f, -1.0f, 1.0f);
 
        // Bottom
        gl.glColor3f(0.0f, 0.0f, 0.0f); 
        gl.glVertex3f(-1.0f, -1.0f, 1.0f);
        gl.glColor3f(0.1f, 0.1f, 0.1f); 
        gl.glVertex3f(1.0f, -1.0f, 1.0f);
        gl.glColor3f(0.2f, 0.2f, 0.2f); 
        gl.glVertex3f(0.0f, -1.0f, -1.0f);
    }
 
    public void displayChanged(GLAutoDrawable gLDrawable, 
      boolean modeChanged, boolean deviceChanged) {
    }
 
    public void init(GLAutoDrawable gLDrawable) {
        final GL gl = gLDrawable.getGL();
        gl.glShadeModel(GL.GL_SMOOTH);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glClearDepth(1.0f);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, 
        GL.GL_NICEST);
        gLDrawable.addKeyListener(this);
    }
 
    public void reshape(GLAutoDrawable gLDrawable, int x, 
    int y, int width, int height) {
        final GL gl = gLDrawable.getGL();
        if(height <= 0) {
            height = 1;
        }
        final float h = (float)width / (float)height;
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(50.0f, h, 1.0, 1000.0);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
    }
 
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            JavaDia.bQuit = true;
            JavaDia.displayT = null;
            System.exit(0);
        }
    }
 
    public void keyReleased(KeyEvent e) {
    }
 
    public void keyTyped(KeyEvent e) {
    }
 }