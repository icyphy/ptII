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
Note that AudioSource will not work unless the Java 1.3 SDK
is used.
// FIXME: currently requies that a soundfile with name 
// "1-welcome.wav", mono, 11 kHz sample rate, be in
// current directory.
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

	    // Set the sampling rate to use.
	    int sampleRate = 22050;

	    // Set the token consumption rate and production rate to use.
	    // Larger values may speed up execution.
	    int cPRate = 512;

	    AudioSource soundSource = new AudioSource(this, "soundSource");
	    // Set the production rate(a performance optimization).
	    soundSource.tokenProductionRate.setToken(new IntToken(cPRate));

            // Read audio data from a local file.
            //soundSource.source.setToken(new StringToken("file"));
	    //soundSource.pathName.setToken(new StringToken("1-welcome.wav"));

	    // *** OR ***

	    // Read audio data from a URL.
	    //soundSource.source.setToken(new StringToken("URL"));
	    //soundSource.pathName.setToken(new StringToken("http://209.233.16.71/1-welcome.wav"));	
	   
	    // *** OR ***

	    // Read audio form microphone (real-time capture).
	    soundSource.source.setToken(new StringToken("mic"));
	    soundSource.sampleRate.setToken(new IntToken(sampleRate));

            AudioSink soundSink = new AudioSink(this, "soundSink");
	  soundSink.fileName.setToken(new StringToken("outputFile.au"));  // FIXME: Does nothing.
         
	  soundSink.sampRate.setToken(new IntToken(sampleRate));

            this.connect(soundSource.output, soundSink.input);
	   
        } catch (Exception ex) {
            System.err.println("Setup failed:" + ex);
        }
    }
}

