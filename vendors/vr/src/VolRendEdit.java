/*
 *	@(#)VolRendEdit.java 1.3 00/09/25 13:58:50
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

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.color.ColorSpace;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.universe.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.io.*;
import java.util.Enumeration;
import com.sun.j3d.utils.behaviors.mouse.*;
import java.net.*;
import java.lang.String.*;


public class VolRendEdit extends JFrame implements ItemListener, ActionListener{

    // parameters settable by setting corresponding property
    boolean 		debug = false;
    boolean 		timing = false;
    int 		guiType;

    VolRend		volRend;

    Canvas3D 		canvas;

    String 		save = "Save Settings...";
    String 		restore = "Restore Settings...";
    String 		loadData = "Load Data File...";
    String		cmapEdit = "Edit Colormap";
    String		annoEdit = "Edit Annotations";
    String 		exit = "Exit";
    File   		settingsFile = null;

    String 		dataSuffix = "vol";
    String 		sessionSuffix = "vrs";
    ExtensionFileFilter sessionFileFilter;
    ExtensionFileFilter dataFileFilter;

    SegyCmapEditDialog	segyCmapEditDialog;
    AnnotationsEditDialog annoEditDialog;

    public VolRendEdit(String[] args) {
	super("Java3D Volume Rendering Editor");
	debug = Boolean.getBoolean("debug");
	timing = Boolean.getBoolean("timing");

	volRend = new VolRend(timing, debug);

	volRend.initContext(getCodeBase()); // initializes the renderers

	// Setup the frame
	getContentPane().setLayout(new BorderLayout());

	canvas = volRend.getCanvas();
	canvas.setSize(600, 600);
	getContentPane().add(canvas, BorderLayout.CENTER);

	// setup the filters for the file selection popups
	setupFilters();

	getContentPane().add(setupPanelGUI(), BorderLayout.EAST);

	pack();
	show();

	// setup the inital data file or settings
	if (args.length == 1) {
	    String filename = args[0];
	    if (filename.endsWith("." + dataSuffix)) {
		volRend.dataFileAttr.set(filename);
		volRend.update();
	    } else {
		volRend.restoreContext(filename);
		settingsFile = new File(filename);
	    }
	}
    }

    URL getCodeBase() {
	String directory = System.getProperty("user.dir");
	String separator = System.getProperty("file.separator");
	URL codebase = null;
	try {
	    if (directory.startsWith("/")) { // fix UNIX case
		 directory = "/" + directory;
	    }
	    String urlString = "file:/" + directory + separator;
	    codebase = new URL(urlString);
	} catch (MalformedURLException exx) {
	    System.out.println("codebase URL error");
	    System.out.println(exx.getMessage());
	}
	return codebase;
    }

    void setupFilters() {
	// these are used by the file menu popups
	sessionFileFilter = new ExtensionFileFilter(sessionSuffix);
	sessionFileFilter.setDescription("VolRend Session");
	dataFileFilter = new ExtensionFileFilter(dataSuffix);
	dataFileFilter.setDescription("VolRend Data");
    }

    JPanel setupPanelGUI() {
	JPanel panel = new JPanel();
	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	panel.setBorder(new BevelBorder(BevelBorder.RAISED));

	// save
	panel.add(new JLabel("Session"));
	JButton saveButton = new JButton("Save Settings");
	saveButton.setName(save);
	saveButton.addActionListener(this);
	panel.add(saveButton);

	// restore
	JButton restoreButton = new JButton("Restore Settings");
	restoreButton.setName(restore);
	restoreButton.addActionListener(this);
	panel.add(restoreButton);

	// load data
	panel.add(new JLabel("Data"));
	JButton loadButton = new JButton(loadData);
	loadButton.setName(loadData);
	loadButton.addActionListener(this);
	panel.add(loadButton);

	// Make the buttons the same size
	Dimension minSize = saveButton.getMinimumSize();
	Dimension maxSize = new Dimension(Short.MAX_VALUE, minSize.height);
	saveButton.setMaximumSize(maxSize);
	restoreButton.setMaximumSize(maxSize);
	loadButton.setMaximumSize(maxSize);

	AttrComponent cm = new JPanelChoice(this, panel, volRend.colorModeAttr);
	JButton cmapEditButton = new JButton(cmapEdit);
	cmapEditButton.setName(cmapEdit);
	cmapEditButton.addActionListener(this);
	panel.add(cmapEditButton);
	AttrComponent tc = new JPanelToggle(this, panel,
					volRend.texColorMapAttr);

	AttrComponent dm = new JPanelChoice(this, panel, volRend.rendererAttr);
	if (debug) {
	    AttrComponent da = new JPanelChoiceAxis(this,panel,
		volRend.displayAxisAttr);
	    AttrComponent dw = new JPanelToggle(this, panel,
		volRend.axisDepthWriteAttr);
	}

	JButton annoEditButton = new JButton(annoEdit);
	annoEditButton.setName(annoEdit);
	annoEditButton.addActionListener(this);
	panel.add(annoEditButton);

	// use this to put these items at the bottom of the panel
	panel.add(Box.createGlue());
	panel.add(new JLabel("View Options"));
	AttrComponent db = new JPanelToggle(this, panel,
					volRend.doubleBufferAttr);
	AttrComponent pr = new JPanelToggle(this, panel,
					volRend.perspectiveAttr);
	AttrComponent cs = new JPanelToggle(this, panel,
					volRend.coordSysAttr);
	AttrComponent vb = new JPanelToggle(this, panel,
					volRend.annotationsAttr);

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
	if (name == exit) {
	    System.exit(0);
	} else if (name == save) {
	    // need to recreate chooser each time to rescan for files
	    JFileChooser chooser = new JFileChooser(new File("."));
	    chooser.setFileFilter(sessionFileFilter);
	    chooser.setDialogTitle("Save Vol Render Settings");
	    if (settingsFile != null) {
		chooser.setSelectedFile(settingsFile);
	    }
	    int retval = chooser.showSaveDialog(this);
	    if (retval == JFileChooser.APPROVE_OPTION) {
		settingsFile = chooser.getSelectedFile();
		String filename = settingsFile.getName();
		if (!filename.endsWith("." + sessionSuffix)) {
		    filename = filename + "." + sessionSuffix;
		}
		volRend.context.save(filename, null);
	    }
	} else if (name == restore) {
	    // need to recreate chooser each time to rescan for files
	    JFileChooser chooser = new JFileChooser(new File("."));
	    chooser.setFileFilter(sessionFileFilter);
	    chooser.setDialogTitle("Restore Vol Render Settings");
	    if (settingsFile != null) {
		chooser.setSelectedFile(settingsFile);
	    }
	    int retval = chooser.showOpenDialog(this);
	    if (retval == JFileChooser.APPROVE_OPTION) {
		settingsFile = chooser.getSelectedFile();
		String filename = settingsFile.getName();
		if (!filename.endsWith("." + sessionSuffix)) {
		    filename = filename + "." + sessionSuffix;
		}
		volRend.restoreContext(filename);
	    }
	} else if (name == loadData) {
	    // need to recreate chooser each time to rescan for files
	    JFileChooser chooser = new JFileChooser(new File("."));
	    chooser.setFileFilter(dataFileFilter);
	    chooser.setDialogTitle("Load Vol Render Data File");
	    int retval = chooser.showOpenDialog(this);
	    if (retval == JFileChooser.APPROVE_OPTION) {
		String filename = chooser.getSelectedFile().getName();
		if (!filename.endsWith("." + dataSuffix)) {
		    filename = filename + "." + dataSuffix;
		}
		volRend.dataFileAttr.set(filename);
		volRend.update();
	    }
	} else if (name == cmapEdit) {
	    Colormap cmap = volRend.colorModeAttr.getColormap();
	    if (cmap instanceof SegyColormap) {
		SegyColormap segyCmap = (SegyColormap) cmap;
		if (segyCmapEditDialog == null) {
		    segyCmapEditDialog =
				new SegyCmapEditDialog(this, volRend, segyCmap);
		}
		segyCmapEditDialog.show();
	    }
	} else if (name == annoEdit) {
	    if (annoEditDialog == null) {
		annoEditDialog =
			    new AnnotationsEditDialog(this, volRend);
	    }
	    annoEditDialog.show();
	} else {
	    //System.out.println("action:  set attr " + name  + " to value " +
	    //	value);
	    volRend.context.getAttr(name).set(value);
	    volRend.update();
	}
    }

    public static void main(String[] args) {
	VolRendEdit vol = new VolRendEdit(args);
    }
}
