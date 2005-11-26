/* An actor that crops images.

 Copyright (c) 1999-2005 The Regents of the University of California.
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

package ptolemy.domains.gr.lib.vr;

import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.gui.StackWindow;
import ij.process.ImageProcessor;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.Image;
import java.net.URL;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.AWTImageToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// ImageCrop
/**
 Describe your class here, in complete sentences.
 What does it do?  What is its intended use?

 @author Tiffany Crawford
 @version $Id$
 @see classname (refer to relevant classes, but not the base class)
 @since Ptolemy II x.x
 @Pt.ProposedRating Red (yourname)
 @Pt.AcceptedRating Red (reviewmoderator)
 */

public class ImageCrop extends TypedAtomicActor {

    /** Create an instance with ... (describe the properties of the
     *  instance). Use the imperative case here.
     *  @param parameterName Description of the parameter.
     *  @exception ExceptionClass If ... (describe what
     *   causes the exception to be thrown).
     */
    public ImageCrop(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);
        imageInput = new TypedIOPort(this, "imageInput");
        imageInput.setInput(true);
        imageInput.setTypeEquals(BaseType.OBJECT);
        
        initialImage = new FileParameter(this, "initialImage");
        initialImage.setExpression("");

        roi = new TypedIOPort(this, "roi");
        roi.setInput(true);
        roi.setTypeEquals(BaseType.OBJECT);

        output = new TypedIOPort(this, "output");
        output.setOutput(true);
        output.setTypeEquals(BaseType.OBJECT);

        stack = new Parameter(this, "stack");
        stack.setExpression("true");
        stack.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Desription of the variable. */
    public TypedIOPort imageInput;
    
    public FileParameter initialImage;

    public TypedIOPort roi;

    public TypedIOPort output;

    public Parameter stack;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Do something... (Use the imperative case here, such as:
     *  "Return the most recently recorded event.", not
     *  "Returns the most recently recorded event."
     *  @param parameterName Description of the parameter.
     *  @exception ExceptionClass If ... (describe what
     *   causes the exception to be thrown).
     */
    public void fire() throws IllegalActionException {
        super.fire();
        //Get input values from respective tokens
        //Save old ROI to compare in postfire
        _oldRoi = _roi;
    	_readToken();
        
        //Do cropping if no ROI to crop, display entire image
        if (_roi == null){
            if(_debugging){
             _debug("roi = null");   
            }
            //output.broadcast(new ObjectToken(_imagePlus));
            output.broadcast(new AWTImageToken(_imagePlus.getImage()));
        }else{
            if(_debugging){
             _debug("IP = "+ _imagePlus.getProcessor());
             _debug("ROI = " + _roi);
            }
            _imageProcessor = _imagePlus.getProcessor();
            _imageProcessor.setRoi(_roi);
            ImageProcessor croppedProcessor = _imageProcessor.crop();
            _croppedImage = new ImagePlus("Cropped Image", croppedProcessor);
            _image = _croppedImage.getImage();
            
            //FIXME What is the best thing to send through port
            //output.broadcast(new ObjectToken(_imagePlus));  
            output.broadcast(new AWTImageToken(_image));
            //Save cropped image as image to be cropped in next iteration
            if(_debugging){
             _debug("Setting imagePlus to croppedImage");   
            }
            _imagePlus = _croppedImage;
        }
        
        
    }
    
    public void initialize() throws IllegalActionException {
          super.initialize();
         _imageURL =  initialImage.asURL();
         _fileRoot = _imageURL.getFile();
         _imagePlus = new ImagePlus(_fileRoot);
         
    }
    
    //Check for new ROI
    public boolean postfire() throws IllegalActionException {
        //super.postfire();
        
        if (!(_oldRoi == _roi)) {
            if(_debugging){
             _debug("Called postfire(), which returns true");   
            }
            return true;
        }else{
            if(_debugging){
                _debug("Called postfire(), which returns false");   
               }
            
            return false;   
        }
    }

	protected void _readToken() throws IllegalActionException{
	    //_token = imageInput.get(0);
	    //AWTImageToken imageToken = (AWTImageToken) imageInput.get(0);
	    //_imagePlus = new ImagePlus("Image to be Cropped", imageToken.getValue());
	    _token2 = roi.get(0);
	    ObjectToken roiToken = (ObjectToken) _token2;
	    _roi = (Roi) roiToken.getValue();   
	}
    
    public boolean prefire() throws IllegalActionException {
    	//Check for proper inputs and if available return true
        super.prefire();
       /* if (imageInput.hasToken(0) && roi.hasToken(0)) {
            return true;
        }else{
        	return false;   
        }*/
        return true;
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private ImagePlus _croppedImage;
    
    private String _fileRoot;

    private ImagePlus _imagePlus;
    
    private ImageProcessor _imageProcessor;
    
    private Image _image;
    
    private URL _imageURL;
    
    private Roi _oldRoi;

    private Roi _roi;
    
    private Token _token;
    
    private Token _token2;







}
