/* Java Tank Controller

 Copyright (c) 1999-2000 The Regents of the University of California.
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

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/


package ptolemy.kvm.tankremote;

import java.net.*;
import java.io.*;
import com.sun.kjava.*;

/**
 * This class implements the functionality of a "soft"
 * remote control for the Lego tanks, using the palm.
 * It provides a simple GUI with "up", "down", "left",
 * "right", "stop", and "fire" buttons. The palm does
 * not communicate with the tank directly, but rather
 * sends the commands to a desktop first (where cmds
 * are queued and sequentialized for later transmission
 * to the tanks). The reason for this is potential 
 * interference between two palms trying to issue commands
 * at the same time. The desktop, as a point of synchronization,
 * eliminates that problem by providing only a single device
 * for IR transmissions.
 * 

 * Communication between the desktop and the palm is handled
 * through sockets.
 * 

 * On a desktop, this code would define at least one interface,
 * and define a bunch of symbolic constants. Unfortunately, we
 * currently don't have space for that.

 * TankRemote.java came from
 * http://developer.java.sun.com/developer/technicalArticles/ConsumerProducts/JavaTanks/TankRemote.html
 */
public class TankRemote extends Spotlet {

    /** tank has bumped into something */
    public static final byte B_OBSTRUCTED = (byte)1;

    /** tank has been hit by laser */
    public static final byte B_HIT = (byte)1 << 1;

    /** an error occured on the desktop side */
    public static final byte B_ERROR = (byte)1 << 2;

    /** the palm registered successfully with a tank */
    public static final byte B_REGISTER = (byte)1 << 3;
        
    private OutputStream out;
    private InputStream in;
    private String cmd;
    private Socket socket;
    private Button exitButton;
    private Button forward;
    private Button backward;
    private Button left;
    private Button right;
    private Button stop;
    private Button status;
    private Button fire;
        
    /** system entry point */
    public static void main(String[] args) {
        (new TankRemote()).register(NO_EVENT_OPTIONS);
    }
        
    /** 
     * Create a new TankRemote instance. This brings up the GUI
     * and initializes the socket connection.
     */
    public TankRemote() {
        // up arrow bitmap
        byte[] up = {
            (byte)0, (byte)0, (byte)1, (byte)128, 
            (byte)3, (byte)192, (byte)7, (byte)224, (byte)15, 
            (byte)240, (byte)31, (byte)248, (byte)63, (byte)252, 
            (byte)127, (byte)254, (byte)0, (byte)0
        };
        forward = new Button(new Bitmap((short)2, up), 70, 40);
        up = null;
        System.gc();
                
        // down arrow bitmap
        byte[] down = {
            (byte)0, (byte)0, (byte)127, (byte)254, 
            (byte)63, (byte)252, (byte)31, (byte)248, (byte)15, 
            (byte)240, (byte)7, (byte)224, (byte)3, (byte)192, 
            (byte)1, (byte)128, (byte)0, (byte)0
        };
        backward = new Button(new Bitmap((short)2, down), 70, 80);
        down = null;
        System.gc();
                
        // right arrow bitmap
        byte[] rturn = {
            (byte)0, (byte)0, (byte)96, (byte)0, 
            (byte)126, (byte)0, (byte)127, (byte)224, (byte)127, 
            (byte)254, (byte)127, (byte)224, (byte)126, (byte)0, 
            (byte)96, (byte)0, (byte)0, (byte)0
        };
        right = new Button(new Bitmap((short)2, rturn), 105, 60);
        rturn = null;
        System.gc();
                
        // left arrow bitmap
        byte[] lturn = {
            (byte)0, (byte)0, (byte)0, (byte)6, 
            (byte)0, (byte)126, (byte)7, (byte)254, (byte)127, 
            (byte)254, (byte)7, (byte)254, (byte)0, (byte)126, 
            (byte)0, (byte)6, (byte)0, (byte)0
        };
        left = new Button(new Bitmap((short)2, lturn), 35, 60);
        lturn = null;
        System.gc();
                
        // stop circle bitmap
        byte[] halt = {
            (byte)0, (byte)0, (byte)3, (byte)192, 
            (byte)31, (byte)248, (byte)63, (byte)252, (byte)63, 
            (byte)252, (byte)63, (byte)252, (byte)31, (byte)248, 
            (byte)3, (byte)192, (byte)0, (byte)0
        };
        stop = new Button(new Bitmap((short)2, halt), 70, 60);
        halt = null;
        System.gc();
                
        // status query bitmap
        byte[] why = {
            (byte)0, (byte)0, (byte)3, (byte)192, (byte)6, 
            (byte)96, (byte)0, (byte)192, (byte)1, (byte)128, (byte)1, 
            (byte)128, (byte)0, (byte)0, (byte)1, (byte)128, (byte)0, (byte)0
        };
        status = new Button(new Bitmap((short)2, why), 105, 105);
        why = null;
        System.gc();
                
        // fire bang bitmap
        byte[] bang = {
            (byte)  0, (byte)  0, (byte)  1, (byte)128, (byte)  1, 
            (byte)128, (byte)  1, (byte)128, (byte)  1, (byte)128, (byte)  1, 
            (byte)128, (byte)  0, (byte)  0, (byte)  1, (byte)128, (byte)  0, (byte)0
        };
        fire = new Button(new Bitmap((short)2, bang), 35, 105);
        bang = null;
        System.gc();
                
        exitButton = new Button("Exit", 120, 145);
                
        Graphics.clearScreen();
        paint();
                
        byte newline;
                
        try {
            // make the connection to the desktop
            socket = new Socket("129.146.42.139", 9595);
            out = socket.getOutputStream();
            in = socket.getInputStream();
                        
            for (int i = 0; i < 3; i++) {
                                // try to register with a tank
                out.write("register\n".toByteArray());

                displayStatus((byte)in.read());
                newline = (byte) in.read();
                Thread.sleep(20);
            }
        } catch(Exception e) {
            abort(e.toString());
            try { socket.close(); } 
            catch (Exception x) {}
            System.exit(1);
        }
    }
        
        
    /** 
     * inform user of interesting or important status 
     * feedback from the tank.
     */
    private void displayStatus(byte status) {
        Graphics.drawRectangle(7, 2, 152, 35, Graphics.ERASE, 0);
        if ((status & B_ERROR) != 0) abort("An error occured");
        if ((status & B_HIT) != 0) abort("Aaargh. I'm hit!");
        if ((status & B_OBSTRUCTED) != 0) Graphics.drawString("Bumped into something.", 50, 20);
        if ((status & B_REGISTER) != 0) Graphics.drawString("Register succeeded.", 50, 20);
    }
        
    /** Inform user that a fatal eror has occured and bail */
    private void abort(String msg) {
        Graphics.drawString(msg + ". Aborting...", 50, 20);
        try { socket.close(); }
        catch (Exception e) {}
        try {
            Thread.sleep(1000);
        } catch (Exception e) {}

        System.exit(1);
    }
        
    /** redraw the GUI */
    private void paint() {
        Graphics.resetDrawRegion();
        Graphics.drawBorder(6, 1, 153, 133, Graphics.PLAIN, Graphics.SIMPLE);
        Graphics.drawRectangle(0, 140, 160, 20, Graphics.ERASE, 0);
                
        exitButton.paint();
        forward.paint();
        backward.paint();
        left.paint();
        right.paint();
        stop.paint();
        status.paint();
        fire.paint();
                
        Graphics.setDrawRegion(6, 1, 153, 133);
    }
        
    /** 
     * Call back method after a pen down event
     * has been detected by the palm
     */
    public void penDown(int x, int y) {
        String cmd = null;
        byte reply;
        byte newline;
                
        System.gc();
                
        try {
            if (exitButton.pressed(x, y)) {
                socket.close();
                System.exit(1);
            }
                        
            if (forward.pressed(x, y)) cmd = "forward";
            if (backward.pressed(x, y)) cmd = "backward";
            if (left.pressed(x, y)) cmd = "left";
            if (right.pressed(x, y)) cmd = "right";
            if (stop.pressed(x, y)) cmd = "stop";
            if (status.pressed(x, y)) cmd = "status";
            if (fire.pressed(x, y)) cmd = "fire";
                        
            if (cmd == null) return ;
                        
            out.write((cmd + "\n").toByteArray());
            reply = (byte) in.read();
            newline = (byte) in.read();
            displayStatus(reply);
        } catch (Exception e) {
            abort(e.toString());
            try {socket.close();} catch(Exception ee) {}
            System.exit(1);
        }
    }
}
