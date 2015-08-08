/* Helper for the cameras JavaScript module.

   Copyright (c) 2014-2015 The Regents of the University of California.
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

 */
package ptolemy.actor.lib.jjs.modules.cameras;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamListener;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.actor.lib.jjs.HelperBase;
import ptolemy.data.AWTImageToken;
import ptolemy.kernel.util.IllegalActionException;

/** Helper for the cameras JavaScript module.  This is based on
 *  the webcam-capture package by Bartosz Firyn (SarXos), available from:
 *    https://github.com/sarxos/webcam-capture
 *
 *  Note that you can use the Ptolemy II ImageDisplay actor to display image sequences
 *  captured using this module, but this does not work well when executed within Eclipse.
 *  In Eclipse, the Swing event thread blocks "waiting for: OGLRenderQueue$QueueFluher",
 *  and spends most of its time blocked rather than rendering. Hence, we do not get
 *  smooth video. Perhaps this is an Eclipse bug?
 *
 *  See the documentation for the JavaScript cameras module to see how to use this.
 *
 *  <p> If invoking the constructor generates a message like:</p>
 *  <pre>
 *  Caused by: java.lang.RuntimeException: Library 'OpenIMAJGrabber' was not loaded successfully from file '/tmp/BridJExtractedLibraries5717506824090765864/OpenIMAJGrabber.so'
 *  </pre>
 *  <p>Then under Red Hat Linux, install lib4l for 64-bit JVMs, run:</p>
 *
 *  <pre>
 *  sudo yum install libstdc++.x86_64
 *  </pre>
 *
 *  <p> The way to diagnose these problems under Linux is to unjar the
 *  webcam-capture jar and run ldd:</p>
 *
 *  <pre>
 *  cd /tmp
 *  jar -xf $PTII/lib/webcam-capture-0.3.10.jar 
 *  ldd ./com/github/sarxos/webcam/ds/buildin/lib/linux_x64/OpenIMAJGrabber.so
 *  </pre>
 *
 *  <p>Note that the webcam-capture jar contains both 32-bit and 64-bit shared 
 *  libraries, be sure to run the ldd command on the version appropriate for your
 *  JVM.</p>
 *
 *  <p>Then look for libraries that are listed as "not found", then use
 *  <code>yum search xxx</code> to find them and then install them.</p>
 *
 *  @author Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 11.0
 *  @Pt.ProposedRating Yellow (eal)
 *  @Pt.AcceptedRating Red (cxh)
 *
 */
public class CameraHelper extends HelperBase implements WebcamListener {

    /** Create the system default camera.
     *  @exception IOException If there is no such camera.
     *  @param currentObj The JavaScript object that this is helping.
     */
    public CameraHelper(ScriptObjectMirror currentObj) throws IOException {
        this(currentObj, null);
    }

    /** Create a camera with the specified name. The name is
     *  required to be one of those returned by {@link #cameras()},
     *  or else a exception will be thrown.
     *  @param name The name of the camera.
     *  @param currentObj The JavaScript object that this is helping.
     *  @exception IOException If the camera does not exist.
     */
    public CameraHelper(ScriptObjectMirror currentObj, String name)
            throws IOException {
        super(currentObj);
        if (name == null) {
            _webcam = Webcam.getDefault();
        } else {
            if (_webcams == null) {
                // Refresh the list.
                cameras();
            }
            if (_webcams == null) {
                System.err.println("ptolemy/actor/lib/jjs/modules/cameras/CameraHelper.java: No Cameras were found.");
                return;
            }
            _webcam = _webcams.get(name);
        }
        if (_webcam == null) {
            throw new IOException("No such camera: " + name);
        }
        _webcam.addWebcamListener(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return an array of camera names that are available on this host.
     *  This method refreshes the list.
     *  @return A list of camera names, or null if there none.
     */
    public static String[] cameras() {
        List<Webcam> webcams = Webcam.getWebcams();
        if (webcams.size() == 0) {
            // There are no cameras.
            _webcams = null;
            return null;
        }
        _webcams = new HashMap<String, Webcam>();
        _webcamNames = new String[webcams.size() + 1];
        _webcamNames[0] = "default camera";
        _webcams.put("default camera", Webcam.getDefault());
        int i = 1;
        for (Webcam webcam : webcams) {
            _webcams.put(webcam.getName(), webcam);
            _webcamNames[i++] = webcam.getName();
        }
        return _webcamNames;
    }

    /** Close the camera.
     */
    public void close() {
        if (_webcam != null) {
            _webcam.close();
        }
    }

    /** Return the system default camera name.
     *  @return The system default camera, or null if there aren't any.
     */
    public static String defaultCamera() {
        Webcam camera = Webcam.getDefault();
        if (camera != null) {
            return camera.getName();
        }
        return null;
    }

    /** Return the current view size for this camera,
     *  given as a JSON formatted string with fields "width"
     *  and "height", for example '{"width":176, "height":144}'.
     *  @return A string representing the current view size.
     *  @see #setViewSize(Map)
     */
    public String getViewSize() {
        Dimension size = null;
        if (_webcam != null) {
            size = _webcam.getViewSize();
        } else {
            size = new Dimension(0,0);
        }
        String result = "{\"width\":" + size.width + ", \"height\":"
                + size.height + "}";
        return result;
    }

    /** Open the camera. This starts a sequence of notifications that
     *  call webcamImageObtained() and trigger emission of the "image" event
     *  for the corresponding JavaScript object.
     */
    public void open() {
        // The true argument opens the camera in "asynchronous" mode, which
        // notifies this object of each new image obtained.
        if (_webcam != null) {
            _webcam.open(true);
        }
    }

    /** Set the current view size for this camera, representing the desired size
     *  as a Map with integer valued fields "width" and "height".
     *  @exception IllegalActionException If either field is absent.
     *  @see #getViewSize()
     */
    public void setViewSize(Map spec) throws IllegalActionException {
        int width = 0, height = 0;
        Object widthSpec = spec.get("width");
        if (widthSpec instanceof Integer) {
            width = ((Integer) widthSpec).intValue();
        } else {
            throw new IllegalActionException(_actor,
                    "Expected integer width specification for the view size. Got: "
                            + widthSpec);
        }
        Object heightSpec = spec.get("height");
        if (heightSpec instanceof Integer) {
            height = ((Integer) heightSpec).intValue();
        } else {
            throw new IllegalActionException(_actor,
                    "Expected integer height specification for the view size. Got: "
                            + heightSpec);
        }
        if (_webcam != null) {
            _webcam.setViewSize(new Dimension(width, height));
        }
    }

    /** Return the most recent image obtained by the camera, or null
     *  if no image has been obtained.
     *  @return A Ptolemy II token containing the image, or null if there
     *   is no image to return.
     */
    public AWTImageToken snapshot() {
        if (_webcam == null) {
            return null;
        }
        if (!_webcam.isOpen()) {
            // open();
        }
        if (_image != null) {
            return new AWTImageToken(_image);
        } else {
            _image = _webcam.getImage();
            if (_image != null) {
                return new AWTImageToken(_image);
            }
        }
        return null;
    }

    /** Return an array of view sizes supported by this camera,
     *  each given as a string of the form "width=176,height=144", for example.
     *  @return An array of strings representing available view sizes.
     */
    public String[] viewSizes() {
        if (_webcam == null) {
            return new String [] {"{ \"width\"=0, \"height\"=0 }"};
        }
        Dimension[] sizes = _webcam.getViewSizes();
        String[] result = new String[sizes.length];
        for (int i = 0; i < sizes.length; i++) {
            result[i] = "{\"width\":" + sizes[i].width + ", \"height\":"
                    + sizes[i].height + "}";
        }
        return result;
    }

    /** Notify this object that its camera has been closed.
     *  @param event The event that closed the camera.
     */
    @Override
    public void webcamClosed(WebcamEvent event) {
        _currentObj.callMember("emit", "closed", new AWTImageToken(_image));
    }

    /** Notify this object that its camera has been disposed.
     *  @param event The event that disposed the camera.
     */
    @Override
    public void webcamDisposed(WebcamEvent event) {
        // FIXME: What does it mean for the camera to be disposed?
        // This emission is not documented.
        _currentObj.callMember("emit", "disposed", new AWTImageToken(_image));
    }

    /** Notify this object that a new image has been obtained.
     *  This causes the associated JavaScript Camera object to emit
     *  an "image" event with the image as an argument.
     *  @param event The event of obtaining the image.
     */
    @Override
    public void webcamImageObtained(WebcamEvent event) {
        _image = event.getImage();
        _currentObj.callMember("emit", "image", new AWTImageToken(_image));
        // FIXME: Time stamp the image.
    }

    /** Notify this object that its camera has been opened.
     *  @param event The event that opened the camera.
     */
    @Override
    public void webcamOpen(WebcamEvent event) {
        _currentObj.callMember("emit", "opened", new AWTImageToken(_image));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The latest image obtained. */
    private BufferedImage _image;

    /** The camera associated with this instance. */
    private Webcam _webcam;

    /** Cache of discovered local webcam names.
     *  This will be refreshed whenever {@link #cameras()} is called.
     */
    private static String[] _webcamNames;

    /** Cache of discovered local webcams indexed by name.
     *  This will be refreshed whenever {@link #cameras()} is called.
     */
    private static Map<String, Webcam> _webcams;
}
