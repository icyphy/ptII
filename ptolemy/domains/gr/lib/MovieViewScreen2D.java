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

import quicktime.qd.*;
import quicktime.*;
import quicktime.std.*;
import quicktime.io.*;
import quicktime.sound.*;
import quicktime.std.image.*;
import quicktime.std.movies.*;
import quicktime.std.movies.media.*;
import quicktime.util.*;

import quicktime.app.display.*;
import quicktime.app.image.*;
import quicktime.app.QTFactory;
import java.awt.*;

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
public class MovieViewScreen2D extends ViewScreen2D implements StdQTConstants, Errors {

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
         
        try {
            QTSession.open();
   
            Frame frame = new Frame("foo");
            QTCanvas canv = new QTCanvas (QTCanvas.kInitialSize, 0.5F, 0.5F);
            frame.add ("Center", canv);
            painter = new Painter();
            qid = new QTImageDrawer (painter, 
                    new Dimension (kWidth, kHeight), Redrawable.kMultiFrame);
            qid.setRedrawing(true);

            canv.setClient (qid, true);
	
            frame.pack();
            QTFile f = new QTFile("c:/foo.mov");
            Movie theMovie = Movie.createMovieFile (f,
                    kMoviePlayer, 
                    createMovieFileDeleteCurFile | createMovieFileDontCreateResFile);

            //
            // add content
            //
            System.out.println ("Doing Video Track");
            int kNoVolume	= 0;
            int kVidTimeScale = 600;
            
            Track vidTrack = theMovie.addTrack (kWidth, kHeight, kNoVolume);
            VideoMedia vidMedia = new VideoMedia (vidTrack, kVidTimeScale);  
            
            vidMedia.beginEdits();
            addVideoSample (vidMedia);
            vidMedia.endEdits();
            
            int kTrackStart	= 0;
            int kMediaTime 	= 0;
            int kMediaRate	= 1;
            vidTrack.insertMedia (kTrackStart, kMediaTime,
                    vidMedia.getDuration(), kMediaRate);

 //            System.out.println ("Doing Audio Track");
//             addAudioTrack( theMovie );

            //
            // save movie to file
            //
            OpenMovieFile outStream = OpenMovieFile.asWrite (f); 
            theMovie.addResource(outStream, movieInDataForkResID, f.getName());
            outStream.close();
            System.out.println ("Finished movie");
        }
        catch (Exception qte) {
            qte.printStackTrace(); 
        }
        QTSession.close();
    }

    private void addVideoSample( VideoMedia vidMedia ) throws QTException {
        QDRect rect = new QDRect (kWidth, kHeight);
        QDGraphics gw = new QDGraphics (rect);
        int size = QTImage.getMaxCompressionSize (gw, 
                rect, 
                gw.getPixMap().getPixelSize(),
                codecNormalQuality, 
                kAnimationCodecType, 
                CodecComponent.anyCodec);
        QTHandle imageHandle = new QTHandle (size, true);
        imageHandle.lock();
        RawEncodedImage compressedImage = RawEncodedImage.fromQTHandle(imageHandle);
        CSequence seq = new CSequence (gw,
                rect, 
                gw.getPixMap().getPixelSize(),
                kAnimationCodecType, 
                CodecComponent.bestFidelityCodec,
                codecNormalQuality, 
                codecNormalQuality, 
                numFrames,	//1 key frame
                null, //cTab,
                0);
        ImageDescription desc = seq.getDescription();

        qid.setRedrawing(true);

        //redraw first...
      	painter.setCurrentFrame (1);
        qid.redraw(null);

        qid.setGWorld (gw);
        qid.setDisplayBounds (rect);
			
        for (int curSample = 0; curSample < numFrames; curSample++) {
            painter.setCurrentFrame (curSample);
        
            qid.redraw(null);
            CompressedFrameInfo info = seq.compressFrame (gw, 
                    rect, 
                    codecFlagUpdatePrevious, 
                    compressedImage);
            boolean isKeyFrame = info.getSimilarity() == 0;
            System.out.println ("f#:" + curSample + ",kf=" + isKeyFrame + ",sim=" + info.getSimilarity());
            vidMedia.addSample (imageHandle, 
                    0, // dataOffset,
                    info.getDataSize(),
                    60, // frameDuration, 60/600 = 1/10 of a second, desired time per frame	
                    desc,
                    1, // one sample
                    (isKeyFrame ? 0 : mediaSampleNotSync)); // no flags
        }
		
 	//print out ImageDescription for the last video media data ->
 	//this has a sample count of 1 because we add each "frame" as an individual media sample
        System.out.println (desc);

 	//redraw after finishing...
    }

    private class Painter implements Paintable {
        private int _frame;
	private Rectangle[] ret = new Rectangle[1];

	public void setCurrentFrame (int frame) {
            _frame = frame;
        }
	public void newSizeNotified (QTImageDrawer drawer, Dimension d) {
            ret[0] = new Rectangle (kWidth, kHeight);
        }
	public Rectangle[] paint (Graphics g) {
            g.drawImage((Image)_images.get(_frame),0,0,null);
            ret[0] = new Rectangle (kWidth, kHeight);
            return ret;
   	}
    }

    /** A list of BufferedImages.
     */
    private List _images = new LinkedList();
    private Painter painter;
    private QTImageDrawer qid;
    private static final int numFrames = 10;
    private int kWidth = 400;
    private int kHeight = 400;
    private File soundFile;

}

