/* A GR scene viewer

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red (chf@eecs.berkeley.edu)
@AcceptedRating Red (chf@eecs.berkeley.edu)
*/
package ptolemy.domains.gr.lib;

import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.canvas.GraphicsPane;
import diva.canvas.JCanvas;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.Placeable;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.gr.kernel.GRActor2D;
import ptolemy.domains.gr.kernel.GRUtilities2D;
import ptolemy.domains.gr.kernel.Scene2DToken;
import ptolemy.domains.gr.kernel.ViewScreenInterface;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.media.*;
import javax.media.protocol.*;
import javax.media.format.*;
import javax.media.util.*;
import javax.media.control.*;
import java.io.*;
//////////////////////////////////////////////////////////////////////////
//// MovieViewScreen2D

/** 
A sink actor that renders a two-dimensional scene into a display screen, and
saves it as a movie using JMF.

@author Steve Neuendorffer
@version $Id$
@since Ptolemy II 1.0
*/
public class MovieViewScreen2D extends ViewScreen2D implements ControllerListener {

    /** Construct a ViewScreen2D in the given container with the given name.
     *  If the container argument is null, a NullPointerException will
     *  be thrown. If the name argument is null, then the name is set
     *  to the empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this ViewScreen2D.
     *  @exception IllegalActionException If this actor
     *   is not compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public MovieViewScreen2D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

    }


    ///////////////////////////////////////////////////////////////////
    ////                     Ports and Parameters                  ////


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire this actor. 
     */
    public void fire() throws IllegalActionException {
        super.fire();

        // Render the canvas into an image.
        BufferedImage image = 
            new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = (Graphics2D)image.getGraphics();
        getCanvas().paint(graphics);
        _images.add(image);
    }

    /** Initialize the execution.  Create the MovieViewScreen2D frame if 
     *  it hasn't been set using the place() method.
     *  @exception IllegalActionException If the base class throws it.
     */
    public void initialize() throws IllegalActionException {

        super.initialize();
        _images.clear();

    }

    /** Wrapup an execution
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _doIt();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    protected void _doIt() {
        // This method is largely copied from Sun's example
        // JpegImageToMovie.java
	GraphicsDataSource ids = new GraphicsDataSource();

	Processor p;

	try {
	    System.err.println("- create processor for the image datasource ...");
	    p = Manager.createProcessor(ids);
	} catch (Exception e) {
	    System.err.println("Yikes!  Cannot create a processor from the data source.");
	    return;
	}

        p.addControllerListener(this);

        try {
            ids.connect();
        } catch (Exception ex) {
            System.out.println("foo");
            return;
        }

	// Put the Processor into configured state so we can set
	// some processing options on the processor.
	p.configure();
	if (!waitForState(p, p.Configured)) {
	    System.err.println("Failed to configure the processor.");
	    return;
	}

	// Set the output content descriptor to QuickTime. 
	p.setContentDescriptor(new ContentDescriptor(FileTypeDescriptor.QUICKTIME));
     
	// Query for the processor for supported formats.
	// Then set it on the processor.
	TrackControl tcs[] = p.getTrackControls();
	Format f[] = tcs[0].getSupportedFormats();
	if (f == null || f.length <= 0) {
	    System.err.println("The mux does not support the input format: " + tcs[0].getFormat());
	    return;
	}

	tcs[0].setFormat(f[0]);
        for(int i = 0; i < f.length; i++) {
            System.out.println("format = " + f[i]);
        }
	System.err.println("Setting the track format to: " + f[0]);

	// We are done with programming the processor.  Let's just
	// realize it.
	p.realize();
	if (!waitForState(p, p.Realized)) {
	    System.err.println("Failed to realize the processor.");
	    return;
	}

	// Generate the output media locators.
	MediaLocator oml;

        //FIXME: parameter
	if ((oml = new MediaLocator("file://c:/foo.mov")) == null) {
	    System.err.println("Cannot build media locator");
	    System.exit(0);
	}

	// Now, we'll need to create a DataSink.
        DataSource output = p.getDataOutput();
	DataSink dsink;
	try {
	    System.err.println("- create DataSink for: " + oml);
	    dsink = Manager.createDataSink(output, oml);
	    dsink.open();
	} catch (Exception e) {
	    System.err.println("Cannot create the DataSink: " + e);
	    return;
	}

        //	dsink.addDataSinkListener(this);
	fileDone = false;

	System.err.println("start processing...");

	// OK, we can now start the actual transcoding.
	try {
       	    p.start();
	    dsink.start();
	} catch (IOException e) {
	    System.err.println("IO error during processing");
	    return;
	}

       
	// Wait for EndOfStream event.
	waitForFileDone();

	// Cleanup.
	try {
	    dsink.close();
	} catch (Exception e) {}
        p.removeControllerListener(this);

	System.err.println("...done processing.");

	return;
    }

    /**
     * Block until file writing is done. 
     */
    boolean waitForFileDone() {
	synchronized (waitFileSync) {
	    try {
		while (!fileDone)
		    waitFileSync.wait();
	    } catch (Exception e) {}
	}
	return fileSuccess;
    }

    /**
     * Block until the processor has transitioned to the given state.
     * Return false if the transition failed.
     */
    boolean waitForState(Processor p, int state) {
	synchronized (waitSync) {
	    try {
		while (p.getState() < state && stateTransitionOK)
		    waitSync.wait();
	    } catch (Exception e) {}
	}
	return stateTransitionOK;
    }

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
	    evt.getSourceController().stop();
	    evt.getSourceController().close();
	}
    }

    /**
     * A DataSource to read from a list of JPEG image files and
     * turn that into a stream of JMF buffers.
     * The DataSource is not seekable or positionable.
     */
    private class GraphicsDataSource extends PullBufferDataSource {

	private GraphicsSourceStream streams[];
        private boolean connected = false;
        private boolean started = false;

	GraphicsDataSource() {
	    streams = new GraphicsSourceStream[1];
	    streams[0] = new GraphicsSourceStream();
	}

	public void setLocator(MediaLocator source) {
	}

	public MediaLocator getLocator() {
	    return null;
	}

	/**
	 * Content type is of RAW since we are sending buffers of video
	 * frames without a container format.
	 */
	public String getContentType() {
	    return ContentDescriptor.RAW;
	}
        
        public void connect() throws IOException {
            if (connected)
                return;
            connected = true;
        }
        
        public void disconnect() {
            try {
                if (started)
                    stop();
            } catch (IOException e) {}
            connected = false;
        }

        public void start() throws IOException {
            // we need to throw error if connect() has not been called
            if (!connected)
                throw new java.lang.Error("DataSource must be connected before it can be started");
            if (started)
                return;
            started = true;
        //     for(int i = 0; i < 10; i++) {
//                 transferHandler.transferData(streams[0]);
//                 try {
//                     Thread.currentThread().sleep( 10 );
//                 } catch (InterruptedException ise) {
//                 }
//             }
            //     streams[0].start(true);
        }
        
        public void stop() throws IOException {
            if ((!connected) || (!started))
                return;
            started = false;
            //      streams[0].start(false);
        }
        
	/**
	 * Return the ImageSourceStreams.
	 */
	public PullBufferStream[] getStreams() {
	    return streams;
	}

	/**
	 * We could have derived the duration from the number of
	 * frames and frame rate.  But for the purpose of this program,
	 * it's not necessary.
	 */
	public Time getDuration() {
	    return DURATION_UNKNOWN;
	}

	public Object[] getControls() {
	    return new Object[0];
	}

	public Object getControl(String type) {
	    return null;
	}
    }


    /**
     * The source stream to go along with ImageDataSource.
     */
    private class GraphicsSourceStream implements PullBufferStream {

        // FIXME: make parameters?
	private int width = 400;
        private int height = 400;
        private int frameRate = 30;
	private VideoFormat format;

	private int imageCount = 0;	// index of the next image to be read.
	private boolean ended = false;
    

	public GraphicsSourceStream() {
            
// 	    format = new VideoFormat(VideoFormat.JPEG,
// 				new Dimension(width, height),
// 				Format.NOT_SPECIFIED,
// 				Format.byteArray,
// 				(float)frameRate);
	}

	/**
	 * We should never need to block assuming data are read from files.
	 */
	public boolean willReadBlock() {
	    return false;
	}

	/**
	 * This is called from the Processor to read a frame worth
	 * of video data.
	 */
 	public void read(Buffer buf) throws IOException {

	    // Check if we've finished all the frames.
	    if (imageCount >= _images.size()) {
		// We are done.  Set EndOfMedia.
		System.err.println("Done reading all images.");
		buf.setEOM(true);
		buf.setOffset(0);
		buf.setLength(0);
		ended = true;
		return;
	    }

	    Image image = (Image)_images.get(imageCount);
	  
	    System.err.println("  - reading image file: " + imageCount);

            Buffer buffer = ImageToBuffer.createBuffer(image, frameRate);

	    // Check the input buffer type & size.
            buf.setFormat( buffer.getFormat());
            buf.setTimeStamp((long)(imageCount * (1000 / frameRate) * 1000000));
            buf.setSequenceNumber( imageCount );
	    buf.setLength(buffer.getLength());
	    buf.setFlags(0);
            // buf.setHeader( null );

            buf.setOffset(0);
            //            buf.setFlags(buf.getFlags() | buf.FLAG_KEY_FRAME);

            System.out.println("format = " + buffer.getFormat());
            imageCount++;
	}

	/**
	 * Return the format of each video frame.  That will be JPEG.
	 */
	public Format getFormat() {
            Image image = (Image)_images.get(0);
            Buffer buffer = ImageToBuffer.createBuffer(image, frameRate);
            System.out.println("buffer = " + buffer.getFormat());
            return buffer.getFormat();
	}

        public BufferTransferHandler getTransferHandler() {
            return transferHandler;
        }

        public void setTransferHandler(BufferTransferHandler transferHandler) {
            synchronized (this) {
                transferHandler = transferHandler;
                notifyAll();
            }
        }

	public ContentDescriptor getContentDescriptor() {
	    return new ContentDescriptor(ContentDescriptor.RAW);
	}

	public long getContentLength() {
	    return 0;
	}

	public boolean endOfStream() {
	    return ended;
	}

	public Object[] getControls() {
	    return new Object[0];
	}

	public Object getControl(String type) {
	    return null;
	}
    } 

    /** A list of BufferedImages.
     */
    private List _images = new LinkedList();

    Object waitSync = new Object();
    boolean stateTransitionOK = true;
    Object waitFileSync = new Object();
    boolean fileDone = false;
    boolean fileSuccess = true;
   private BufferTransferHandler transferHandler;

}

