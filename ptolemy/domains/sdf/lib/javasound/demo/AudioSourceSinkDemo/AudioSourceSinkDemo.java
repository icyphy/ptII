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
import ptolemy.domains.sdf.lib.javasound.*;
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
	    //_sdfDirector.iterations.setToken(new IntToken(1000));

	    // Set the sampling rate to use.
	    int sampleRate = 44100;

	    int channels = 2;
	    // Set the token consumption rate and production rate to use.
	    // Larger values may speed up execution.
	    int cPRate = 256;

	    // Size of internal Java Sound buffer (in samples) to use 
	    //for real-time capture/playback.
	    int buffSize = 4096;

	    AudioSource soundSource = new AudioSource(this, "soundSource");
	    // Set the production rate(a performance optimization).
	    soundSource.tokenProductionRate.setToken(new IntToken(cPRate));

            // Read audio data from a local file.
            //soundSource.source.setToken(new StringToken("file"));
	    //soundSource.pathName.setToken(new StringToken("22-new.aif"));
	    // *** OR ***

	    // Read audio data from a URL.
	    //soundSource.source.setToken(new StringToken("URL"));
	    //soundSource.pathName.setToken(new StringToken("http://some-web-site/1-welcome.wav"));	
	   
	    // *** OR ***

	    // Read audio form microphone (real-time capture).
	    soundSource.source.setToken(new StringToken("mic"));
	    soundSource.sampleRate.setToken(new IntToken(sampleRate));
	    soundSource.channels.setToken(new IntToken(channels));
	    soundSource.bufferSize.setToken(new IntToken(buffSize));

            AudioSink soundSink = new AudioSink(this, "soundSink");
	  soundSink.pathName.setToken(new StringToken("outputFile.wav"));
	  soundSink.sink.setToken(new StringToken("speaker"));
	  //soundSink.sink.setToken(new StringToken("file"));
	  soundSink.sampleRate.setToken(new IntToken(sampleRate));
	  soundSink.sampleSizeInBits.setToken(new IntToken(16));
	  soundSink.tokenConsumptionRate.setToken(new IntToken(cPRate));
	  soundSink.channels.setToken(new IntToken(channels));
	  soundSink.bufferSize.setToken(new IntToken(buffSize));

	  //this.connect(soundSource.output, soundSink.input);
	  TypedIORelation rel1 =
	    (TypedIORelation)this.newRelation("relation1");
	  rel1.setWidth(2);
	  soundSource.output.link(rel1);
	  soundSink.input.link(rel1);

        } catch (Exception ex) {
            System.err.println("Setup failed:" + ex);
        }
    }
}

