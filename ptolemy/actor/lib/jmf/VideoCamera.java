/* An actor that produces a sequence of frames from a video camera.

@Copyright (c) 2001 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

						PT_COPYRIGHT_VERSION 2
						COPYRIGHTENDKEY
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/


/* Some of this code is copied from Sun's sample code.  Hence:
 *
 * @(#)FrameAccess.java	1.5 01/03/13
 *
 * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.
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

package ptolemy.actor.lib.jmf;

// FIXME: Replace with per-class imports.
import java.awt.*;
import javax.media.*;
import javax.media.control.TrackControl;
import javax.media.Format;
import javax.media.format.*;

import ptolemy.actor.lib.Source;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.Token;
import ptolemy.data.StringToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import javax.media.util.BufferToImage;
import javax.swing.ImageIcon;
import java.util.Iterator;
import java.util.Vector;


//////////////////////////////////////////////////////////////////////////
//// VideoCamera
/**
An actor that produces a sequence of frames from a video camera.
This actor requires the Java Media Framework, and has been tested
at least with version 2.1.1.

FIXME: more info.

@author  Christopher Hylands, Edward Lee, James Yeh
@version $Id$
*/
public class VideoCamera extends Source implements ControllerListener {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public VideoCamera(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // FIXME: output should perhaps be named "video"?
        // In case there is audio track.
        // Don't derive from source in this case.
        output.setTypeEquals(BaseType.OBJECT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Open the file at the URL, and set the width of the output.
     *  @throws IllegalActionException If there are no video capture
     *   devices.
     */
    public void initialize() throws IllegalActionException {
	CaptureDeviceManager captureManager = new CaptureDeviceManager();
        // FIXME: Format should be a parameter?
        VideoFormat format = new RGBFormat();
	Vector deviceList = captureManager.getDeviceList(format);

        if (deviceList.size() == 0) {
            throw new IllegalActionException(this,
            "No video capture devices found by the Java Media Framework.");
        }
        if (_debugging) {
            _debug("--- Capture devices found:");
            Iterator devices = deviceList.iterator();
            while (devices.hasNext()) {
                CaptureDeviceInfo device = (CaptureDeviceInfo)devices.next();
                _debug(device.getName());
            }
            _debug("---");
        }
        // FIXME: Need a mechanism for selecting the device.
	CaptureDeviceInfo captureDeviceInfo
               = (CaptureDeviceInfo) deviceList.get(0);
	MediaLocator locator = captureDeviceInfo.getLocator();

	try {
	    _processor = Manager.createProcessor(locator);
	} catch (Exception ex) {
	    throw new IllegalActionException(null, ex,
                    "Failed to create a processor for the media locator: "
                    + locator);
	}

	_processor.addControllerListener(this);

	// Put the Processor into configured state.
	_processor.configure();
	if (!_waitForState(_processor.Configured)) {
	    throw new IllegalActionException(
                    "Failed to configure the processor.");
	}

	// So I can use it as a player.
        // FIXME: What does this mean?
	_processor.setContentDescriptor(null);

	// Obtain the track controls.
	TrackControl trackControls[] = _processor.getTrackControls();

	if (trackControls == null) {
	    throw new IllegalActionException(
                    "Failed to obtain track controls from the processor.");
	}

	// Search for the track control for the video track (vs. audio).
        // FIXME: We want the audio track too on a separate output, maybe.
	TrackControl videoTrack = null;
	for (int i = 0; i < trackControls.length; i++) {
	    if (trackControls[i].getFormat() instanceof VideoFormat) {
		videoTrack = trackControls[i];
		break;
	    }
	}

	if (videoTrack == null) {
	    throw new IllegalActionException(
                    "The input media does not contain a video track.");
	}

	if (_debugging) {
            _debug("Video format: " + videoTrack.getFormat());
        }

	// Instantiate and set the frame access codec to the data flow path.
        // FIXME: Why two codecs? 
	try {
	    Codec codec[] = { new PreAccessCodec(), new PostAccessCodec()};
	    videoTrack.setCodecChain(codec);
	} catch (UnsupportedPlugInException e) {
	    throw new IllegalActionException(
                   "The process does not support codec plug ins.");
	}

	// Realize the processor.
        // After this is called, cannot make modifications to the processor,
        // such as format changes?
	_processor.prefetch();
	if (!_waitForState(_processor.Prefetched)) {
	    throw new IllegalActionException(
                    "Failed to realize the processor.");
	}

        // NOTE: Can get a visual component (which displays the video)
        // by calling _processor.getVisualComponent(), and a small control
        // panell with a pause button by calling
        // _processor.getControlPanelComponent().

	// Start the processor.
        // This will trigger callbacks to the codecs.
	_processor.start();
    }

    /** Capture a frame and send a java.awt.Image object
     *  to the output port.	
     *  @exception IllegalActionException If there's no director.
     */
    public synchronized void fire() throws IllegalActionException {
        while (_image == null) {
            try {
                wait();
            } catch (InterruptedException ex) {
                throw new IllegalActionException(this,
                "Interrupted while waiting for the first video frame.");
            }
        }
	output.send(0, new ObjectToken(_image));
    }

    /** Close the media processor.
     */
    public void wrapup() {
        if (_processor != null) {
            _processor.close();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Block until the processor has transitioned to the given state.
     *  Return false if the transition failed.
     */
    protected boolean _waitForState(int state) {
	synchronized (waitSync) {
	    try {
		while (_processor.getState() != state && stateTransitionOK)
		    waitSync.wait();
	    } catch (Exception e) {}
	}
	return stateTransitionOK;
    }

    // FIXME: Got to here.

    /**
     * Controller Listener.
     */
    public void controllerUpdate(ControllerEvent evt) {

	if (evt instanceof ConfigureCompleteEvent ||
	    evt instanceof RealizeCompleteEvent ||
	    evt instanceof PrefetchCompleteEvent) {
	    synchronized (waitSync) {
		stateTransitionOK = true;
		waitSync.notifyAll();
	    }
	} else if (evt instanceof ResourceUnavailableEvent) {
	    synchronized (waitSync) {
		stateTransitionOK = false;
		waitSync.notifyAll();
	    }
	} else if (evt instanceof EndOfMediaEvent) {
	    _processor.close();
	    //System.exit(0);
	}
    }



    /**
     * Main program
     */
//      public static void main(String [] args) {

//  	if (args.length == 0) {
//  	    prUsage();
//  	    System.exit(0);
//   	}

//  	String url = args[0];

//  	if (url.indexOf(":") < 0) {
//  	    prUsage();
//  	    System.exit(0);
//  	}

//  	MediaLocator locator;

//  	if ((locator = new MediaLocator(url)) == null) {
//  	    System.err.println("Cannot build media locator from: " + url);
//  	    System.exit(0);
//  	}

//  	FrameAccess fa = new FrameAccess();

//  	if (!fa.open(locator))
//  	    System.exit(0);
//      }

//      static void prUsage() {
//  	System.err.println("Usage: java FrameAccess <url>");
//      }



    /*********************************************************
     * Inner class.
     *
     * A pass-through codec to access to individual frames.
     *********************************************************/

    public class PreAccessCodec implements Codec {

	/**
         * Callback to access individual video frames.
         */
	void accessFrame(Buffer frame) {

	    // For demo, we'll just print out the frame #, time &
	    // data length.

	    long t = (long)(frame.getTimeStamp()/10000000f);

	    System.err.println("Pre: frame #: " + frame.getSequenceNumber() + 
			", time: " + ((float)t)/100f + 
			", len: " + frame.getLength());
	}


	/**
 	 * The code for a pass through codec.
	 */

	// We'll advertize as supporting all video formats.
	protected Format supportedIns[] = new Format [] {
	    new VideoFormat(null)
	};

	// We'll advertize as supporting all video formats.
	protected Format supportedOuts[] = new Format [] {
	    new VideoFormat(null)
	};

	Format input = null, output = null;

	public String getName() {
	    return "Pre-Access Codec";
	}

	// No op.
        public void open() {
	}

	// No op.
	public void close() {
	}

	// No op.
	public void reset() {
	}

	public Format [] getSupportedInputFormats() {
	    return supportedIns;
	}

	public Format [] getSupportedOutputFormats(Format in) {
	    if (in == null)
		return supportedOuts;
	    else {
		// If an input format is given, we use that input format
		// as the output since we are not modifying the bit stream
		// at all.
		Format outs[] = new Format[1];
		outs[0] = in;
		return outs;
	    }
	}

	public Format setInputFormat(Format format) {
	    input = format;
	    return input;
	}

	public Format setOutputFormat(Format format) {
	    output = format;
	    return output;
	}

	public int process(Buffer in, Buffer out) {

	    // This is the "Callback" to access individual frames.
	    accessFrame(in);

	    // Swap the data between the input & output.
	    Object data = in.getData();
	    in.setData(out.getData());
	    out.setData(data);

	    // Copy the input attributes to the output
	    out.setFormat(in.getFormat());
	    out.setLength(in.getLength());
	    out.setOffset(in.getOffset());

	    return BUFFER_PROCESSED_OK;
	}

	public Object[] getControls() {
	    return new Object[0];
	}

	public Object getControl(String type) {
	    return null;
	}
    }

    public class PostAccessCodec extends PreAccessCodec {
	// We'll advertize as supporting all video formats.
	public PostAccessCodec() {
	    supportedIns = new Format [] {
		new RGBFormat()
	    };
	}

	/**
         * Callback to access individual video frames.
         */
	void accessFrame(Buffer frame) {

	    // For demo, we'll just print out the frame #, time &
	    // data length.

	    long time = (long)(frame.getTimeStamp()/10000000f);


	    VideoFormat videoFormat = (VideoFormat)frame.getFormat();

	    System.err.println("Post: frame #: " + frame.getSequenceNumber() + 
			       ", time: " + ((float)time)/100f + 
			       ", length: " + frame.getLength()
			       + ", offset: " + frame.getOffset()
			       + ", format: " + videoFormat
			       + ", format.getEncoding: "
			       + videoFormat.getEncoding()
			       + ", format.getdataType: "
			       + videoFormat.getDataType()
			       );
			       
	    BufferToImage bufferToImage = new BufferToImage(videoFormat);
	    _image = bufferToImage.createImage(frame); 
	}

	public String getName() {
	    return "Post-Access Codec";
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The java.awt.Image that we are producing
    private Image _image;

    // The video processor.
    Processor _processor;

    Object waitSync = new Object();
    boolean stateTransitionOK = true;
}
