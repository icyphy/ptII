/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2010-2011 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
*/
package ptolemy.domains.jogl.TourModelsGL;

import static javax.media.opengl.GL.GL_COLOR_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_TEST;
import static javax.media.opengl.GL.GL_LEQUAL;
import static javax.media.opengl.GL.GL_MODELVIEW;
import static javax.media.opengl.GL.GL_NICEST;
import static javax.media.opengl.GL.GL_PERSPECTIVE_CORRECTION_HINT;
import static javax.media.opengl.GL.GL_PROJECTION;
import static javax.media.opengl.GL.GL_QUADS;
import static javax.media.opengl.GL.GL_SMOOTH;
import static javax.media.opengl.GL.GL_TRIANGLES;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.sun.opengl.util.FPSAnimator;

/**
 * NeHe Lesson 5: 3D Shape
 */
public class cubeTriangle extends JPanel implements GLEventListener {
    private static final int REFRESH_FPS = 60; // Display refresh frames per second
    private GLU glu; // For the GL Utility
    final FPSAnimator animator; // Used to drive display()

    static float anglePyramid = 0; // rotational angle in degree for pyramid
    static float angleCube = 0; // rotational angle in degree for cube
    static float speedPyramid = 2.0f; // rotational speed for pyramid
    static float speedCube = -1.5f; // rotational speed for cube

    // Constructor
    public cubeTriangle() {
        GLCanvas canvas = new GLCanvas();
        this.setLayout(new BorderLayout());
        this.add(canvas, BorderLayout.CENTER);
        canvas.addGLEventListener(this);

        // Run the animation loop using the fixed-rate Frame-per-second animator,
        // which calls back display() at this fixed-rate (FPS).
        animator = new FPSAnimator(canvas, REFRESH_FPS, true);
    }

    // Main program
    public static void main(String[] args) {
        final int WINDOW_WIDTH = 320;
        final int WINDOW_HEIGHT = 240;
        final String WINDOW_TITLE = "Display3D";

        try {
            // Run this in the Swing Event Thread.
            Runnable doActions = new Runnable() {
                public void run() {
                    try {
                        JFrame frame = new JFrame();
                        final cubeTriangle joglMain = new cubeTriangle();
                        frame.setContentPane(joglMain);
                        frame.addWindowListener(new WindowAdapter() {
                            @Override
                            public void windowClosing(WindowEvent e) {
                                // Use a dedicate thread to run the stop() to ensure that the
                                // animator stops before program exits.
                                new Thread() {
                                    @Override
                                    public void run() {
                                        joglMain.animator.stop(); // stop the animator loop
                                        System.exit(0);
                                    }
                                }.start();
                            }
                        });
                        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
                        frame.setTitle(WINDOW_TITLE);
                        frame.setVisible(true);
                        joglMain.animator.start(); // start the animation loop
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

        }
    }

    // ------ Implement methods declared in GLEventListener ------

    /**
     * Called back immediately after the OpenGL context is initialized. Can be used
     * to perform one-time initialization. Run only once.
     */

    public void init(GLAutoDrawable drawable) {
        GL gl = drawable.getGL(); // Get the OpenGL graphics context
        glu = new GLU(); // GL Utilities
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f); // Set background (clear) color
        gl.glClearDepth(1.0f); // Set clear depth value to farthest
        gl.glEnable(GL_DEPTH_TEST); // Enables depth testing
        gl.glDepthFunc(GL_LEQUAL); // The type of depth test to do
        // Do the best perspective correction
        gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
        // Enable smooth shading, which blends colors nicely, and smoothes out lighting.
        gl.glShadeModel(GL_SMOOTH);

        // ----- Your OpenGL initialization code here -----
    }

    /**
     * Call-back handler for window re-size event. Also called when the drawable is
     * first set to visible.
     */

    public void reshape(GLAutoDrawable drawable, int x, int y, int width,
            int height) {
        GL gl = drawable.getGL(); // Get the OpenGL graphics context

        if (height == 0) {
            height = 1; // prevent divide by zero
        }
        float aspect = (float) width / height;

        // Set the viewport (display area) to cover the entire window
        gl.glViewport(0, 0, width, height);

        // Setup perspective projection, with aspect ratio matches viewport
        gl.glMatrixMode(GL_PROJECTION); // Choose projection matrix
        gl.glLoadIdentity(); // Reset projection matrix
        glu.gluPerspective(45.0, aspect, 0.1, 100.0); // fovy, aspect, zNear, zFar

        // Enable the model-view transform
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity(); // reset

    }

    /**
     * Called back by the animator to perform rendering.
     */

    public void display(GLAutoDrawable drawable) {
        GL gl = drawable.getGL(); // Get the OpenGL graphics context
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Clear color and depth buffers

        // ----- Render the Pyramid -----
        gl.glLoadIdentity(); // reset the model-view matrix
        gl.glTranslatef(-1.5f, 0.0f, -6.0f); // translate left and into the screen
        gl.glRotatef(anglePyramid, 0.1f, 1.0f, -0.1f); // rotate about the y-axis

        gl.glBegin(GL_TRIANGLES); // of the pyramid

        // Font-face triangle
        gl.glColor3f(1.0f, 0.0f, 0.0f); // Red
        gl.glVertex3f(0.0f, 1.0f, 0.0f);
        gl.glColor3f(0.0f, 1.0f, 0.0f); // Green
        gl.glVertex3f(-1.0f, -1.0f, 1.0f);
        gl.glColor3f(0.0f, 0.0f, 1.0f); // Blue
        gl.glVertex3f(1.0f, -1.0f, 1.0f);

        // Right-face triangle
        gl.glColor3f(1.0f, 0.0f, 0.0f); // Red
        gl.glVertex3f(0.0f, 1.0f, 0.0f);
        gl.glColor3f(0.0f, 0.0f, 1.0f); // Blue
        gl.glVertex3f(1.0f, -1.0f, 1.0f);
        gl.glColor3f(0.0f, 1.0f, 0.0f); // Green
        gl.glVertex3f(1.0f, -1.0f, -1.0f);

        // Back-face triangle
        gl.glColor3f(1.0f, 0.0f, 0.0f); // Red
        gl.glVertex3f(0.0f, 1.0f, 0.0f);
        gl.glColor3f(0.0f, 1.0f, 0.0f); // Green
        gl.glVertex3f(1.0f, -1.0f, -1.0f);
        gl.glColor3f(0.0f, 0.0f, 1.0f); // Blue
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);

        // Left-face triangle
        gl.glColor3f(1.0f, 0.0f, 0.0f); // Red
        gl.glVertex3f(0.0f, 1.0f, 0.0f);
        gl.glColor3f(0.0f, 0.0f, 1.0f); // Blue
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);
        gl.glColor3f(0.0f, 1.0f, 0.0f); // Green
        gl.glVertex3f(-1.0f, -1.0f, 1.0f);

        gl.glEnd(); // of the pyramid

        // ----- Render the Color Cube -----
        gl.glLoadIdentity(); // reset the current model-view matrix
        gl.glTranslatef(1.5f, 0.0f, -7.0f); // translate right and into the screen
        gl.glRotatef(angleCube, 1.0f, 1.0f, 1.0f); // rotate about the x, y and z-axes

        gl.glBegin(GL_QUADS); // of the color cube

        // Top-face
        gl.glColor3f(0.0f, 1.0f, 0.0f); // green
        gl.glVertex3f(1.0f, 1.0f, -1.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f);
        gl.glVertex3f(1.0f, 1.0f, 1.0f);

        // Bottom-face
        gl.glColor3f(1.0f, 0.5f, 0.0f); // orange
        gl.glVertex3f(1.0f, -1.0f, 1.0f);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f);

        // Front-face
        gl.glColor3f(1.0f, 0.0f, 0.0f); // red
        gl.glVertex3f(1.0f, 1.0f, 1.0f);
        gl.glVertex3f(-1.0f, 1.0f, 1.0f);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f);
        gl.glVertex3f(1.0f, -1.0f, 1.0f);

        // Back-face
        gl.glColor3f(1.0f, 1.0f, 0.0f); // yellow
        gl.glVertex3f(1.0f, -1.0f, -1.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);
        gl.glVertex3f(1.0f, 1.0f, -1.0f);

        // Left-face
        gl.glColor3f(0.0f, 0.0f, 1.0f); // blue
        gl.glVertex3f(-1.0f, 1.0f, 1.0f);
        gl.glVertex3f(-1.0f, 1.0f, -1.0f);
        gl.glVertex3f(-1.0f, -1.0f, -1.0f);
        gl.glVertex3f(-1.0f, -1.0f, 1.0f);

        // Right-face
        gl.glColor3f(1.0f, 0.0f, 1.0f); // violet
        gl.glVertex3f(1.0f, 1.0f, -1.0f);
        gl.glVertex3f(1.0f, 1.0f, 1.0f);
        gl.glVertex3f(1.0f, -1.0f, 1.0f);
        gl.glVertex3f(1.0f, -1.0f, -1.0f);

        gl.glEnd(); // of the color cube

        // Update the rotational angle after each refresh.
        anglePyramid += speedPyramid;
        angleCube += speedCube;
    }

    /**
     * Called back when the display mode (eg. resolution) has been changed.
     * (not implemented by JOGL)
     */

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
            boolean deviceChanged) {
    }
}
