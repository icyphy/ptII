/* An actor that produces a sequence of frames from a video camera.

@Copyright (c) 2002-2003 The Regents of the University of California.
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
@ProposedRating Red
@AcceptedRating Red
*/


/* Some of this code is copied from Sun's sample code.  Hence:
 *
 * @(#)FrameAccess.java        1.5 01/03/13
 *
 * Copyright (c) 2002-2003 Sun Microsystems, Inc. All Rights Reserved.
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
//import java.awt.*;
//import javax.media.*;
import ptolemy.actor.lib.Source;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.util.Iterator;
import java.util.Vector;

import javax.media.Buffer;
import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Codec;
import javax.media.ConfigureCompleteEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.EndOfMediaEvent;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.PrefetchCompleteEvent;
import javax.media.Processor;
import javax.media.RealizeCompleteEvent;
import javax.media.ResourceUnavailableEvent;
import javax.media.UnsupportedPlugInException;
import javax.media.control.TrackControl;
import javax.media.format.VideoFormat;
import javax.media.format.YUVFormat;


//////////////////////////////////////////////////////////////////////////
//// VideoCamera
/**
An actor that produces a sequence of frames from a video camera.
This actor requires the Java Media Framework, and has been tested
at least with version 2.1.1.

FIXME: more info.

@author  Christopher Hylands, Edward Lee, James Yeh, Paul Yang, David Lee
@version $Id$
@since Ptolemy II 3.0
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
        output.setTypeEquals(BaseType.OBJECT);    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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

    /** Capture a frame and send a java.awt.Image object
     *  to the output port.
     *  @exception IllegalActionException If there's no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        //         while (!_newFrame) {
        //             if (_debugging) {
        //                 _debug("image not new");
        //             }
        //             try {
        //                 wait();
        //             } catch (InterruptedException ex) {
        //                 throw new IllegalActionException(this, ex,
        //                 "Interrupted while waiting for the "
        //                 + "first video frame.");
        //             }
        //         }
        //_imageNew = JamesCodec.getFrame();
        //output.send(0, new ObjectToken(frameBuffer));
        _bufferNew = CameraCodec.getFrame();
        if (_bufferNew != null) {
            //_bufferNew.setFormat(new YUVFormat());
            output.send(0, new JMFImageToken(_bufferNew));
        }
    }

    /** Open the file at the URL, and set the width of the output.
     *  @exception IllegalActionException If there are no video capture
     *   devices.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        /*CaptureDeviceManager captureManager = */ new CaptureDeviceManager();
        // FIXME: Format should be a parameter?
        // FIXME: This one too (look below)
        VideoFormat format = new YUVFormat();
        Vector deviceList = CaptureDeviceManager.getDeviceList(format);
        // FIXME: Devicelist should be a static private member


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
                    "Failed to create a processor for the media locator. "
                    + "Note that you may need to run jmfinit, which is found "
                    + "in the JMF directory, for example c:/Program Files/"
                    + "JMF2.1.1/bin.  The orginal exception was: "
                    + locator);
        }

        _processor.addControllerListener(this);

        // Put the Processor into configured state.
        _processor.configure();
        if (!_waitForState(Processor.Configured)) {
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
        try {
            CameraCodec = new PreAccessCodec();
            Codec codec[] = {CameraCodec};
            videoTrack.setCodecChain(codec);
        } catch (UnsupportedPlugInException e) {
            throw new IllegalActionException(
                    "The process does not support codec plug ins.");
        }

        // Realize the processor.
        // After this is called, cannot make modifications to the processor,
        // such as format changes?
        _processor.prefetch();
        if (!_waitForState(Processor.Prefetched)) {
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

    /*********************************************************
     * Inner class.
     *
     * A pass-through codec to access to individual frames.
     *********************************************************/

    public class PreAccessCodec implements Codec {
        public PreAccessCodec() throws IllegalActionException {
            // supportedIns = new Format [] {
            //        new YUVFormat()
            //}
        }
        /**
         * Callback to access individual video frames.
         */

        synchronized void accessFrame(Buffer frame) {
            // long t = (long)(frame.getTimeStamp()/10000000f);
            //VideoFormat videoFormat = (VideoFormat)frame.getFormat();
            //BufferToImage bufferToImage = new BufferToImage(videoFormat);

            //Perhaps the next step is to instead output frames, and
            //implement a transformer
            //_image = bufferToImage.createImage(frame);
            frameBuffer = frame;
            _newFrame = true;
            notifyAll();

        }

        /**
         * The code for a pass through codec.
         */

        synchronized Buffer getFrame() throws IllegalActionException {
            while (!_newFrame) {
                //                try {
                //   wait();
                //} catch (InterruptedException ex) {
                //    throw new IllegalActionException(this,
                //    "blahblahblah");
                //}
                try {
                    wait();
                } catch (InterruptedException ex) {
                    throw new IllegalActionException("Error");}
            }
            _newFrame = false;
            return frameBuffer;
        }

        // We'll advertize as supporting all video formats.
        protected Format _supportedIns[] = new Format [] {
            new YUVFormat()
                };

        // We'll advertize as supporting all video formats.
        protected Format _supportedOuts[] = new Format [] {
            new YUVFormat()
                //new VideoFormat(null)
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
            return _supportedIns;
        }

        public Format [] getSupportedOutputFormats(Format in) {
            if (in == null)
                return _supportedOuts;
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



    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The java.awt.Image that we are producing
    private Buffer _bufferNew;
    private boolean _newFrame = false;
    // The video processor.
    Processor _processor;
    PreAccessCodec CameraCodec;
    Object waitSync = new Object();
    boolean stateTransitionOK = true;
    Buffer frameBuffer = new Buffer();
}
