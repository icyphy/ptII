/* A model that uses Ptolemy II SDF domain to capture and playback
   audio data.

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

package ptolemy.domains.sdf.lib.javasound.demo.AudioSourceSinkDemo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


import ptolemy.domains.sdf.lib.*;
import ptolemy.actor.lib.javasound.*;
import ptolemy.domains.sdf.lib.javasound.demo.AudioSourceSinkDemo.*;


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
//// AudioSourceSinkDemo
/**
A simple model demonstrating the use of the AudioSource and 
AudioSink actors. The model consists of an AudioSource connected
to an AudioSink.
<p>
Note: Requires Java 2 v1.3.0 RC1 or later.

@author Brian K. Vogel
@version $Id$
*/
public class AudioSourceSinkDemo extends TypedCompositeActor {   
    

    public AudioSourceSinkDemo() {
        super();
	create();
    }

    public AudioSourceSinkDemo(Workspace workspace) {
	super(workspace);
	create();
    }

    public AudioSourceSinkDemo(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
	create();
    }


    public void create() {

	try {

	    System.out.println("Create invoked");

            SDFDirector _sdfDirector = new SDFDirector(this, "SDFDirector");

            SDFScheduler scheduler = new SDFScheduler(_workspace);

            _sdfDirector.setScheduler(scheduler);
            _sdfDirector.setScheduleValid(false);

	    // ********** FOR DEBUF ONLY ************
	    //_sdfDirector.iterations.setToken(new IntToken(10000));

	    // This is Experimental!
	    _sdfDirector.vectorizationFactor.setToken(new IntToken(64));

	    // Set the sampling rate to use.
	    int sampleRate = 8000;

	    int channels = 1;

	    // Size of internal Java Sound buffer (in samples) to use 
	    //for real-time capture/playback.
	    int buffSize = 4096;

	    Ramp ramp = new Ramp(this, "ramp");
	    // Set the production rate(a performance optimization).
	    ramp.step.setToken(new DoubleToken(0.000125));

	    Sine sine1 = new Sine(this, "sine1");
	    
	    sine1.omega.setToken(new DoubleToken(2500));
	    sine1.amplitude.setToken(new DoubleToken(0.5));
	   

	     AudioSink soundSink = new AudioSink(this, "soundSink");
	    //Recorder soundSink = new Recorder(this, "soundSink");

	    //soundSink.pathName.setToken(new StringToken("outputFile.wav"));
	    soundSink.sink.setToken(new StringToken("live"));
	    soundSink.sampleRate.setToken(new IntToken(sampleRate));
	    //soundSink.sampleSizeInBits.setToken(new IntToken(16));
	    //soundSink.channels.setToken(new IntToken(channels));
	    //soundSink.bufferSize.setToken(new IntToken(buffSize));

	    this.connect(ramp.output, sine1.input);
	    //TypedIORelation rel1 =
	    //(TypedIORelation)this.newRelation("rel1");
	    //rel1.setWidth(2);
	    //ramp.output.link(rel1);
	    //sine1.input.link(rel1);

	    this.connect(sine1.output, soundSink.input);
	  //TypedIORelation rel2 =
	  //(TypedIORelation)this.newRelation("rel2");
	  //rel2.setWidth(2);
	  //sine1.output.link(rel2);
	  //soundSink.input.link(rel2);

        } catch (Exception ex) {
            System.err.println("Setup failed:" + ex);
        }
    }
}

