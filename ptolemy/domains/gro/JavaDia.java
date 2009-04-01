package ptolemy.domains.gro;

import javax.media.opengl.GLCanvas;
 import java.awt.Frame;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 public class JavaDia implements Runnable {
     static Thread displayT = new Thread(new JavaDia());
     static boolean bQuit = false;
 
     public static void main(String[] args) {
//         displayT.start();
         Frame frame = new Frame("Jogl 3D Shape/Rotation");
         GLCanvas canvas = new GLCanvas();
         GLCanvas canvas2 = new GLCanvas();
         
         
         canvas.addGLEventListener(new JavaRenderer());
         canvas2.addGLEventListener(new JavaRenderer());

         frame.add(canvas);
         frame.add(canvas2);
         
         frame.setSize(640, 480);
         frame.setUndecorated(true);
         int size = frame.getExtendedState();
         size |= Frame.MAXIMIZED_BOTH;
         frame.setExtendedState(size);

         
         
         
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