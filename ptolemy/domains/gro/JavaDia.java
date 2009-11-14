/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2009 The Regents of the University of California.
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
*/
package ptolemy.domains.gro;

import javax.media.opengl.GLCanvas;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
 
//////////////////////////////////////////////////////////////////////////
//// JavaDia

/**
 * JavaDia.
 *
 * @author Jasmine Demir
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class JavaDia implements Runnable {
    /**  Create a window that contains a JavaRenderer.
     *   @param args Ignored.   
     */
    public static void main(String[] args) {
         displayThread.start();
         Frame frame = new Frame("Jogl 3D Shape/Rotation");
         GLCanvas canvas = new GLCanvas();
         //Frame frame2 = new Frame("Jogl 3D Shape/Rotation");
         
         
         canvas.addGLEventListener(new JavaRenderer());

         frame.add(canvas);
         
         frame.setSize(640, 480);
         frame.setUndecorated(true);
         //int size = frame.getExtendedState();
         //size |= Frame.MAXIMIZED_BOTH;
         //frame.setExtendedState(size);

         
         frame.addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                 quit = true;
             }
         });
         frame.setVisible(true);
 //      frame.show();
         canvas.requestFocus();
         while( !quit ) {
             canvas.display();
         }

     }
 
    /** Display the window.
     */
    public void run() {
         Frame frame = new Frame("Jogl 3D Shape/Rotation");
         GLCanvas canvas = new GLCanvas();
         canvas.addGLEventListener(new JavaRenderer());
         frame.add(canvas);
         frame.setSize(640, 480);
         frame.setUndecorated(true);
         int size = frame.getExtendedState();
         size |= Frame.MAXIMIZED_BOTH;
         frame.setExtendedState(size);
 
         frame.addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                 quit = true;
             }
         });
         frame.setVisible(true);
 //      frame.show();
         canvas.requestFocus();
         while( !quit ) {
             canvas.display();
         }
     }

 
    ///////////////////////////////////////////////////////////////////
    ////                         package protected variables       ////

    static Thread displayThread = new Thread(new JavaDia());
    static boolean quit = false;
}
