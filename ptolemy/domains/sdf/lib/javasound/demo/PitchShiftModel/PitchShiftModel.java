/* A model that uses Ptolemy II SDF domain to perform
 * real-time pitch shifting of audio signals.

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
AudioSink actors, using the SDF domain. This demo performs pitch 
shifting on audio captured from the microphone.
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
	     // Initialization
            SDFDirector _sdfDirector = new SDFDirector(this, "SDFDirector");


	    // Begin debug.
	    //StreamListener sa2 = new StreamListener();
	    //_sdfDirector.addDebugListener(sa2);
	    // End debug.

            SDFScheduler scheduler = new SDFScheduler(_workspace);

            _sdfDirector.setScheduler(scheduler);
            _sdfDirector.setScheduleValid(false);

	    // gui stuff goes here.

	    //Create the top-level container and add contents to it.
	    JFrame frame = new JFrame("Real-time Pitch Shifter");
	    
	     JPanel controlpanel = new JPanel();

	     controlpanel.setLayout(new BorderLayout());

	     //PtolemyQuery _ptQuery = new PtolemyQuery(_sdfDirector);
	     PtolemyQuery _ptQuery = new PtolemyQuery();
	    
	    controlpanel.add("West", _ptQuery);

	    _ptQuery.addSlider("pitchSlider", "Pitch Scale Factor",
			     1000, 400, 3000);

	    _ptQuery.addLine("pitchLine", "Pitch Scale Factor x 1000",
			     "1000");

	    frame.getContentPane().add(controlpanel, BorderLayout.CENTER);
	    //Finish setting up the frame, and show it.
	    frame.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
			System.exit(0);
		    }
		});
	    frame.pack();
	    frame.setVisible(true);

	   


	    // End of gui stuff.

	    // Set the sampling rate to use.
	    int sampleRate = 22050;

	    int channels = 1;

	    // Size of internal Java Sound buffer (in samples) to use 
	    //for real-time capture/playback.
	    int buffSize = 4096;

	    // Set the token consumption rate and production rate to use.
	    // Larger values may speed up execution.
	    int cPRate = 256;

	    AudioSource soundSource = new AudioSource(this, "soundSource");
	    // Set the production rate(a performance optimization).
	    soundSource.tokenProductionRate.setToken(new IntToken(cPRate));
	    // Specify where to get the sound file.
	    //soundSource.pathName.setToken(new StringToken("1-welcome.wav"));
	    // Read audio form microphone (real-time capture).
	    soundSource.source.setToken(new StringToken("mic"));
	    soundSource.sampleRate.setToken(new IntToken(sampleRate));
	    soundSource.channels.setToken(new IntToken(channels));
	    soundSource.bufferSize.setToken(new IntToken(buffSize));
	
	    // The slider value updats the value parameter of this
	    // actor to control the pitch scale factor.
	    Const pitchScaleSource =
		new Const(this, "pitchScaleSource");
	    	    pitchScaleSource.value.setTypeEquals(BaseType.DOUBLE);
	    // Set constant pitch scale factor.
	    pitchScaleSource.value.setToken(new DoubleToken(1.0));
		 

	    // Set this actor to have a gain of 1/1000. This is needed
	    // since the default vaule of the Slider is 1000 and I
	    // want the default Slider value to correspond to a pitch
	    // scale factor of 1 (unity pitch scaling). The large
	    // default value of the Slider is needed since the slider
	    // only supports the integer type (IntToken).
	    Scale controlGain =
		new Scale(this, "controlGain");
	    	    controlGain.factor.setTypeEquals(BaseType.DOUBLE);
	    // Set constant pitch scale factor.
	    //pitchScaleSource.value.setToken(new DoubleToken(1.0));
	    controlGain.factor.setExpression("0.001");

	    SDFPitchDetector pitchDetect = 
		new SDFPitchDetector(this, "pitchDetect");
	    // Set the sampling rate to use.
	    pitchDetect.sampleRate.setToken(new DoubleToken(sampleRate));
	    pitchDetect.consumptionProductionRate.setToken(new IntToken(cPRate));

	    SDFPitchShift pitchShift =
		new SDFPitchShift(this, "pitchShift");
	    // Set the sampling rate to use.
	    pitchShift.sampleRate.setToken(new DoubleToken(sampleRate));
	    pitchShift.consumptionProductionRate.setToken(new IntToken(cPRate));

            AudioSink soundSink = new AudioSink(this, "soundSink");
	    //soundSink.pathName.setToken(new StringToken("outputFile.wav"));
	    soundSink.sink.setToken(new StringToken("speaker"));
	    //soundSink.sink.setToken(new StringToken("file"));
	    soundSink.sampleRate.setToken(new IntToken(sampleRate));
	    soundSink.sampleSizeInBits.setToken(new IntToken(16));
	    soundSink.tokenConsumptionRate.setToken(new IntToken(cPRate));
	    soundSink.channels.setToken(new IntToken(channels));
	    soundSink.bufferSize.setToken(new IntToken(buffSize));
	  
	    this.connect(pitchDetect.output, pitchShift.pitchIn);
	    this.connect(pitchScaleSource.output, controlGain.input);
	    this.connect(controlGain.output, pitchShift.scaleFactor);

	   
	    this.connect(pitchShift.output, soundSink.input);
	    

	     //Conect tempAct's components
	    TypedIORelation rel1 =
		(TypedIORelation)this.newRelation("relation1");
	     soundSource.output.link(rel1);
            pitchDetect.input.link(rel1);
            pitchShift.input.link(rel1);

	    _ptQuery.attachParameter(pitchScaleSource.value, "pitchSlider");
	    _ptQuery.attachParameter(pitchScaleSource.value, "pitchLine");
	    //_ptQuery.attachParameter(soundSink.fileName, "pitchSlider");


	   
        } catch (Exception ex) {
            System.err.println("Setup failed:" + ex);
        }
    }

 
}

