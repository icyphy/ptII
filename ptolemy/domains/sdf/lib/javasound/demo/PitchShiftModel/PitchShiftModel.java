/* A model that uses Ptolemy II SDF domain to perform
 * real-time pitch shifting of audio signals.

 Copyright (c) 1999 The Regents of the University of California.
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

@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating 
*/

package ptolemy.domains.sdf.lib.javasound.demo.PitchShiftModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


import ptolemy.domains.sdf.lib.*;
import ptolemy.domains.sdf.lib.javasound.*;
import ptolemy.domains.sdf.lib.javasound.demo.PitchShiftModel.*;


import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.net.*;
import java.applet.Applet;
import java.util.Enumeration;

import ptolemy.gui.*;
import ptolemy.gui.QueryListener;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.gui.*;
import ptolemy.domains.sdf.gui.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.sdf.lib.*;
import ptolemy.plot.*;


//////////////////////////////////////////////////////////////////////////
//// PitchShiftModel
/**
A simple model demonstrating the use of the AudioSource and 
AudioSink actors. This demo performs pitch shifting on a
soundfile in the current directory.
Note that AudioSource will not work unless the Java 1.3 SDK
is used.
// FIXME: currently requies that a soundfile with name 
// "1-welcome.wav", mono, 11 kHz sample rate, be in
// current directory.
@author Brian K. Vogel
@version $Id$
*/
public class PitchShiftModel extends TypedCompositeActor {   
    

    public PitchShiftModel() {
        super();
	create();
    }

    public PitchShiftModel(Workspace workspace) {
	super(workspace);
	create();
    }

    public PitchShiftModel(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
	create();
    }


    public void create() {

	try {

	    System.out.println("Create invoked");

	    //this.setName("topLevel");
	    //this.setManager(_manager);
	     // Initialization
            SDFDirector _sdfDirector = new SDFDirector(this, "SDFDirector");


	    // Begin debug.
	    //StreamListener sa2 = new StreamListener();
	    //_sdfDirector.addDebugListener(sa2);
	    // End debug.

            //Parameter iterparam = _sdfDirector.iterations;

	    // Why is this 0????
            //iterparam.setToken(new IntToken(0));
	    


            SDFScheduler scheduler = new SDFScheduler(_workspace);

            _sdfDirector.setScheduler(scheduler);
            _sdfDirector.setScheduleValid(false);

	    //_sdfDirector.iterations.setToken(new IntToken(0));

	    // gui stuff goes here.

	    //Create the top-level container and add contents to it.
	    //  JFrame frame = new JFrame("Real-time Pitch Shifter");
	    
	    //JPanel controlpanel = new JPanel();

	    //controlpanel.setLayout(new BorderLayout());

	    
	     // PtolemyQuery _ptQuery = new PtolemyQuery();
	    
	     //controlpanel.add("West", _ptQuery);
           
	     //_ptQuery.addSlider("pitchSlider", "Pitch Scale Factor",
	     //	     1000, 400, 3000);

            //_ptQuery.addQueryListener(new ParameterListener());
            //_query.setBackground(_getBackground());

	    //frame.getContentPane().add(controlpanel, BorderLayout.CENTER);
	    //Finish setting up the frame, and show it.
	    // frame.addWindowListener(new WindowAdapter() {
	    //    public void windowClosing(WindowEvent e) {
	    //	System.exit(0);
	    //    }
	    //});
	    // frame.pack();
	    //frame.setVisible(true);

	   


	    // End of gui stuff.

	    // Set the sampling rate to use.
	    int sampleRate = 11025;

	    // Set the token consumption rate and production rate to use.
	    // Larger values may speed up execution.
	    int cPRate = 512;

	    AudioSource soundSource = new AudioSource(this, "soundSource");
	    // Specify where to get the sound file.
	    //soundSource.pathName.setToken(new StringToken("NylonGtrSusB2.aiff"));
	    soundSource.pathName.setToken(new StringToken("1-welcome.wav"));
	    // soundSource.pathName.setToken(new StringToken("suzanne.aiff"));
	    //soundSource.pathName.setToken(new StringToken("chamel.aiff"));
	    //soundSource.pathName.setToken(new StringToken("3violin1.aiff"));
	    
            // Read audio data from a local file instread of a URL.
            soundSource.isURL.setToken(new BooleanToken(false));
	
	   
	    
	    

	   

            AudioSink soundSink = new AudioSink(this, "soundSink");
	  soundSink.fileName.setToken(new StringToken("outputFile.au"));  // FIXME: Does nothing.
	  
         
	  soundSink.sampRate.setToken(new IntToken(sampleRate));
	  

            this.connect(soundSource.output, soundSink.input);
	    
	    

	    //_ptQuery.attachParameter(soundSink.fileName, "pitchSlider");


	   
        } catch (Exception ex) {
            System.err.println("Setup failed:" + ex);
        }
    }

 
}

