/* An actor that writes frames to a video file.

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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.jmf;

import ptolemy.actor.lib.Sink;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.gui.MessageHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

import java.awt.Dimension;
import java.awt.Image;

import java.io.IOException;
import java.io.File;

import java.net.MalformedURLException;

import java.util.ArrayList;
import java.util.Iterator;

import javax.media.Buffer;
import javax.media.ConfigureCompleteEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.DataSink;
import javax.media.EndOfMediaEvent;
import javax.media.Format;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.PrefetchCompleteEvent;
import javax.media.Processor;
import javax.media.RealizeCompleteEvent;
import javax.media.ResourceUnavailableEvent;
import javax.media.Time;
import javax.media.control.TrackControl;
import javax.media.datasink.DataSinkErrorEvent;
import javax.media.datasink.DataSinkEvent;
import javax.media.datasink.DataSinkListener;
import javax.media.datasink.EndOfStreamEvent;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.FileTypeDescriptor;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;

//FIXME: This actor only works properly when a model containing this actor
//       is run for the first time.  Vergil has to be reloaded each time
//       for this actor to work.

//FIXME: MPEG isn't working.  It's not letting the processor configure
//       with the MPEG file descriptor.

//////////////////////////////////////////////////////////////////////////
//// MovieWriter
/**
   This actor writes a video file (MPEG, AVI, or Quicktime).  It receives
   JMFImageTokens at the input, and queues them up for writing.
   <p>
   The file is specified by the <i>fileName</i> attribute
   using any form acceptable to FileParameter.
   <p>
   If the <i>confirmOverwrite</i> parameter has value <i>false</i>,
   then this actor will overwrite the specified file if it exists
   without asking.  If <i>true</i> (the default), then if the file
   exists, then this actor will ask for confirmation before overwriting.

   @see FileParameter
   @author James Yeh
   @version $Id$
   @since Ptolemy II 3.1
*/

public class MovieWriter extends Sink
    implements ControllerListener, DataSinkListener {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public MovieWriter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.OBJECT);

        fileOrURL = new FileParameter(this, "fileOrURL");

        confirmOverwrite = new Parameter(this, "confirmOverwrite");
        confirmOverwrite.setTypeEquals(BaseType.BOOLEAN);
        confirmOverwrite.setToken(BooleanToken.TRUE);

        fileType = new StringAttribute(this, "fileType");
        fileType.setExpression("QUICKTIME");
        _fileType = _QUICKTIME;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** If <i>false</i>, then overwrite the specified file if it exists
     *  without asking.  If <i>true</i> (the default), then if the file
     *  exists, ask for confirmation before overwriting.
     */
    public Parameter confirmOverwrite;

    /** The file name or URL from which to read.  This is a string with
     *  any form accepted by File Attribute.
     *  @see FileParameter
     */
    public FileParameter fileOrURL;

    /** The type of file to write.  There are three different file
     *  types that this actor can write.  They are AVI, MPEG, and
     *  QUICKTIME.
     */
    public StringAttribute fileType;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** An attempt is made to acquire the file name.  If it is
     *  successful, create the DataSource that encapsulates the file.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the URL is null, or
     *  invalid.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == fileOrURL) {
            _file = fileOrURL.asFile();
            try {
                _fileRoot = _file.toURL().toString();
            } catch (MalformedURLException ex) {
                throw new IllegalActionException(this, ex, "URL malformed");
            }
        } else if (attribute == confirmOverwrite){
            _confirmOverwrite =
                ((BooleanToken)confirmOverwrite.getToken()).booleanValue();
        } else if (attribute == fileType) {
            String typeName = fileType.getExpression();
            if (typeName.equals("AVI")) {
                _fileType = _AVI;
            } else if (typeName.equals("MPEG")) {
                _fileType = _MPEG;
            } else if (typeName.equals("QUICKTIME")) {
                _fileType = _QUICKTIME;
            } else {
                throw new IllegalActionException(this,
                        "Unrecognized file type: " + typeName + ", must be "
                        + "on of AVI, MPEG, or QUICKTIME");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** The controller listener.  This method controls the
     *  initializing of the processor.
     *  @param event The controller event.
     */
    public void controllerUpdate(ControllerEvent event) {
        if (event instanceof ConfigureCompleteEvent ||
                event instanceof RealizeCompleteEvent ||
                event instanceof PrefetchCompleteEvent) {
            synchronized (_waitSync) {
                _stateTransitionOK = true;
                _waitSync.notifyAll();
            }
        } else if (event instanceof ResourceUnavailableEvent) {
            synchronized (_waitSync) {
                _stateTransitionOK = false;
                _waitSync.notifyAll();
            }
        } else if (event instanceof EndOfMediaEvent) {
            event.getSourceController().stop();
            event.getSourceController().close();
        }
    }

    /** The data sink listener.  This method controls the
     *  closing of the data sink.  It closes the data sink
     *  when it detects that the stream has ended.
     *  @param event The controller event.
     */
    public void dataSinkUpdate(DataSinkEvent event) {
        if (event instanceof EndOfStreamEvent) {
            synchronized (_waitFileSync) {
                _fileDone = true;
                _waitFileSync.notifyAll();
            }
        } else if (event instanceof DataSinkErrorEvent) {
            synchronized (_waitFileSync) {
                _fileDone = true;
                _fileSuccess = false;
                _waitFileSync.notifyAll();
            }
        }
    }

    /** Initialize this actor.
     *  Create a new ArrayList to store the Buffers that the incoming
     *  JMFImageTokens contain.
     *  @exception IllegalActionException If a contained method throws it.
     */
    public void initialize() throws IllegalActionException {
        if(_debugging) {
            _debug("I am in initialize");
        }
        _bufferArrayList = new ArrayList();
    }

    /** Fire this actor.
     *  Accept JMFImageTokens, and queue them up for saving.
     *  @exception IllegalActionException If a contained method throws it,
     *   or if the buffer of the incoming JMFImageToken cannot be added
     *   to the queue.
     *  @return true
     */
    public boolean postfire() throws IllegalActionException {
        _jmfImageToken = (JMFImageToken) input.get(0);
        Buffer buffer;
        buffer = _jmfImageToken.getValue();
        if(!_bufferArrayList.add(buffer)) {
            throw new IllegalActionException("Could not add buffer "
                    + "to the array list");
        }
        _dimensionSet = true;
        return true;
    }

    /** Save the file.
     *  @exception IllegalActionException If a contained method throws it.
     */
    public void wrapup() throws IllegalActionException {
        _bufferIterator = _bufferArrayList.iterator();
        if(_file.exists()) {
            if(_debugging) {
                _debug("file exists!");
            }
            if (_confirmOverwrite) {
                if (!MessageHandler.yesNoQuestion(
                        "OK to overwrite " + _file + "?")) {
                    throw new IllegalActionException(this,
                            "Please select another file name.");
                }
            }
        }

        MediaLocator mediaLocator = new MediaLocator(_fileRoot);
        if (mediaLocator == null) {
            throw new IllegalActionException("Could not create "
                    + "MediaLocator from the given URL: " + _fileRoot);
        }
        //get dimensions
        Image image = _jmfImageToken.asAWTImage();
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        ImageDataSource imageDataSource = new ImageDataSource(width, height);
        Processor processor;

        try {
            processor = Manager.createProcessor(imageDataSource);
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Can't create processor");
        }

        processor.addControllerListener(this);

        processor.configure();

        if (!_waitForState(processor, processor.Configured)) {
            throw new IllegalActionException("Failed to configure processor.");
        }

        if (_fileType == _QUICKTIME) {
            processor.setContentDescriptor(new ContentDescriptor
                (FileTypeDescriptor.QUICKTIME));
        } else if (_fileType == _AVI) {
            processor.setContentDescriptor(new ContentDescriptor
                (FileTypeDescriptor.MSVIDEO));
        } else if (_fileType == _MPEG) {
            processor.setContentDescriptor(new ContentDescriptor
                (FileTypeDescriptor.MPEG));
        } else {
            throw new InternalErrorException(this,
                    "type = " + _fileType + ", which is not one of "
                    + _QUICKTIME + "(QUICKTIME), "
                    + _AVI + "(AVI) or "
                    + _MPEG + "(MPEG).");
        }

        TrackControl trackControl[] = processor.getTrackControls();
        Format format[] = trackControl[0].getSupportedFormats();

        if (format == null || format.length <= 0) {
            throw new IllegalActionException("Cannot support input format");
        }

        trackControl[0].setFormat(format[0]);

        processor.realize();

        if (!_waitForState(processor, processor.Realized)) {
            throw new IllegalActionException("Failed to realize processor");
        }

        DataSource dataSource = processor.getDataOutput();
        if (dataSource == null) {
            throw new IllegalActionException("Processor does not have "
                    + "output DataSource");
        }

        if(_debugging) {
            _debug(_fileRoot);
        }
        DataSink dataSink;

        try {
            dataSink = Manager.createDataSink(dataSource, mediaLocator);
            dataSink.open();
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Couldn't create the data sink");
        }

        dataSink.addDataSinkListener(this);

        try {
            processor.start();
            dataSink.start();
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Could not start processor and datasink");
        }

        if(!_waitForFileDone()) {
            throw new IllegalActionException("Could not write the file");
        }

        try {
            dataSink.close();
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "can't close data sink");
        }
        processor.stop();
        processor.removeControllerListener(this);
        dataSink.removeDataSinkListener(this);
        processor.close();
        dataSink.close();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    protected boolean _waitForFileDone() throws IllegalActionException {
        synchronized(_waitFileSync) {
            try {
                while (!_fileDone) {
                    _waitFileSync.wait();
                }
            } catch (Exception e) {
                throw new IllegalActionException(null, e,
                        "Failed block the processor until it state"
                        + " transition completed.");
            }
        }
        return _fileSuccess;
    }

    /** Block until the processor has transitioned to the given state.
     *  @return false if the transition failed.
     */
    protected boolean _waitForState(Processor processor, int state)
            throws IllegalActionException {
        synchronized (_waitSync) {
            try {
                while (processor.getState() < state && _stateTransitionOK)
                    _waitSync.wait();
            } catch (Exception e) {
                throw new IllegalActionException(null, e,
                        "Failed block the processor until it state"
                        + " transition completed.");
            }
        }
        return _stateTransitionOK;
    }


    ///////////////////////////////////////////////////////////////
    ////                       public inner class              ////

    private class ImageDataSource extends PullBufferDataSource {
        public ImageDataSource(int width, int height) {
            _imageSourceStream[0] = new ImageSourceStream(width, height);
        }
        public void connect() {
        }

        public void disconnect() {
        }

        public String getContentType() {
            return ContentDescriptor.RAW;
        }

        public Object getControl(String type) {
            return null;
        }

        public Object[] getControls() {
            return new Object[0];
        }

        public Time getDuration() {
            return DURATION_UNKNOWN;
        }

        public MediaLocator getLocator() {
            return null;
        }

        public PullBufferStream[] getStreams() {
            return _imageSourceStream;
        }

        public void setLocator(MediaLocator source) {
        }

        public void start() {
        }

        public void stop() {
        }
    }

    private class ImageSourceStream implements PullBufferStream {
        public ImageSourceStream(int width, int height) {
//              _videoFormat = new VideoFormat(VideoFormat.JPEG,
//                      new Dimension(width, height),
//                      Format.NOT_SPECIFIED,
//                      Format.byteArray,
//                      (float)_frameRate);
            _videoFormat = (VideoFormat)(_jmfImageToken.getValue()).getFormat();
        }

        public boolean endOfStream() {
            return _ended;
        }

        public ContentDescriptor getContentDescriptor() {
            return new ContentDescriptor(ContentDescriptor.RAW);
        }

        public long getContentLength() {
            return 0;
        }

        public Object getControl(String type) {
            return null;
        }

        public Object[] getControls() {
            return new Object[0];
        }

        public Format getFormat() {
            return _videoFormat;
        }

        public void read(Buffer buffer) {
            if(_bufferIterator.hasNext()) {
                //buffer = (Buffer)_bufferIterator.next();
                //buffer.copy((Buffer)_bufferIterator.next(), false);
                if(_debugging) {
                    _debug("I about to write 1 frame of data");
                }
                buffer.setData(((Buffer)_bufferIterator.next()).getData());
                _bufferIterator.remove();
            }
            else {
                if(_debugging) {
                    _debug("I am about to write the eom!");
                }
                buffer.setEOM(true);
                buffer.setOffset(0);
                buffer.setLength(0);
                _ended = true;
            }
        }

        public boolean willReadBlock() {
            return false;
        }
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    //private Buffer _buffer;

    private ArrayList _bufferArrayList;

    private Iterator _bufferIterator;

    private boolean _confirmOverwrite;

    private boolean _dimensionSet;

    private boolean _ended = false;

    private File _file;

    private boolean _fileDone = false;

    private String _fileRoot;

    private boolean _fileSuccess = true;

    private int _fileType;

    private ImageSourceStream[] _imageSourceStream = new ImageSourceStream[1];

    private JMFImageToken _jmfImageToken;

    // Boolean that keeps track of whether the player initialization
    // has gone through smoothly.
    private boolean _stateTransitionOK = true;

    private VideoFormat _videoFormat;

    private Object _waitFileSync = new Object();

    // Object to allow synchronization in this actor.
    private Object _waitSync = new Object();

    // FIXME: Use a type safe enumeration.
    private final int _AVI = 0;
    private final int _MPEG = 1;
    private final int _QUICKTIME = 2;
}
