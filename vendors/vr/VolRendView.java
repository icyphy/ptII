/*
 *	@(#)VolRendView.java 1.2 00/09/21 14:20:04
 *
 * Copyright (c) 1996-2000 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */

/* Revision history:

Original Code by Doug Gehringer.
Version of 20000121 appletized by LMDorman, but app usage is broken.
Version of 20000409 generalized file selection to allow changing directories.lmd
Version of 20000429 Changed VolFile.java to use serial read instead of RandomAccessFile.  lmd
Version of 20000914 ddg: Changed to split out applet case into separate class
*/

package vendors.vr;

import java.applet.Applet;
import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.color.ColorSpace;
import java.net.URL.*;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.universe.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.io.*;
import com.sun.j3d.utils.behaviors.mouse.*;
import java.net.*;
import java.lang.Boolean.*;
import java.util.Enumeration;


public class VolRendView extends JApplet implements ItemListener, ActionListener {

    // parameters settable by setting corresponding property
    public boolean 		timing = false;
    public boolean 		debug = true;

    Canvas3D		canvas;
    VolRend 		volRend;

    //dummy for use if called as an applet
    String[]		args;

    public void start () {
    }

    public void init() {
	String timingstr = getParameter("TIMING");
	System.out.println("main: timingstr = " + timingstr );
	timing = Boolean.valueOf(timingstr).booleanValue();
	debug = Boolean.valueOf(getParameter("DEBUG")).booleanValue();
	System.out.println("main: debug = " + debug );

	volRend = new VolRend(timing, debug);

	volRend.initContext(getCodeBase()); // initializes the renderer

	// setup the inital data file or settings
        // get filename from applet PARAM
	String filename = getParameter("VRSFILENAME");
	System.out.println("VolRendView: VRSFILENAME = " + filename);
	volRend.restoreContext(filename);

	// Setup the frame
	getContentPane().setLayout(new BorderLayout());

	canvas = volRend.getCanvas();
	canvas.setSize(600, 600);

	getContentPane().add(canvas, BorderLayout.CENTER);

	if ( debug == true) System.out.println(" setting up GUI " );
        getContentPane().add(setupPanelGUI(), BorderLayout.SOUTH);

	validate();

	volRend.update();

    }


    JPanel setupPanelGUI() {
	if ( debug ) System.out.println (" in setupPanelGUI()");
	JPanel panel = new JPanel();
	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	panel.setBorder(new BevelBorder(BevelBorder.RAISED));

	panel.add(
		new JLabel("Press the mouse button and drag to move the model"));

	Colormap cmap = volRend.colorModeAttr.getColormap();
	if (cmap instanceof SegyColormap) {
	    SegyColormap segyCmap = (SegyColormap)cmap;
	    panel.add(new SegyCmapEditor(volRend, segyCmap));
	}
	panel.validate();

	return  panel;
    }

    public void itemStateChanged(ItemEvent e) {
	String name = ((Component)e.getItemSelectable()).getName();
	boolean value = (e.getStateChange() == ItemEvent.SELECTED);
	ToggleAttr attr = (ToggleAttr) volRend.context.getAttr(name);
	attr.set(value);
	volRend.update();
    }

    public void actionPerformed(ActionEvent e) {
	String name = ((Component)e.getSource()).getName();
	String value = e.getActionCommand();

	//System.out.println("action:  set attr " + name  + " to value " +
	//	value);
	volRend.context.getAttr(name).set(value);
	volRend.update();
    }
}
