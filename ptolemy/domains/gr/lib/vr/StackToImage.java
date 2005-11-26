/* An actor that reads an array of images.

 @Copyright (c) 2005 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */

package ptolemy.domains.gr.lib.vr;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

import java.awt.Image;

import ptolemy.data.AWTImageToken;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.lib.SDFTransformer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
////StackReader
/**
 An actor that reads an array of images.

 @see ptolemy.actor.lib.medicalimaging

 @author T. Crawford
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red
 @Pt.AcceptedRating Red

 */
public class StackToImage extends SDFTransformer {
    /**Construct an actor with the given container and name.
     * @param container The container
     * @param name The name of this actor
     * @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     * @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */

    public StackToImage(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input_tokenConsumptionRate.setExpression("1");
        
        
        output_tokenProductionRate.setExpression("stackSize");

        xResolution = new Parameter(this, "xResolution");
        xResolution.setExpression("256");
        xResolution.setTypeEquals(BaseType.INT);

        yResolution = new Parameter(this, "yResolution");
        yResolution.setExpression("256");
        yResolution.setTypeEquals(BaseType.INT);

        stackSize = new Parameter(this, "stackSize");
        stackSize.setExpression("50");
        stackSize.setTypeEquals(BaseType.INT);

    }

    ////////////////////////////////////////////////////////////////////
    ////////               ports and parameters                  ////////

    //public FilePortParameter input;
    public Parameter xResolution;

    public Parameter yResolution;

    public Parameter stackSize;

    ////////////////////////////////////////////////////////////////////
    ////////                public methods                     ////////

    /** Output the data read in the prefire.
     *  @exception IllegalActionException If there's no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        ObjectToken objectToken = (ObjectToken) input.get(0);
        //_currentImagePlus = (ImagePlus) objectToken.getValue();
        _currentImageStack = (ImageStack) objectToken.getValue();
        AWTImageToken[] _awtImageToken = new AWTImageToken[_stackSize];
       // AWTImageToken _awtImageToken = new AWTImageToken;
        for (int i = 1; i <= _stackSize; i++) {
            System.out.println("i = " + i);
            //_currentImagePlus.setSlice(i);
          //  System.out.println("Output Slice " + _currentImagePlus.getCurrentSlice());
          ImageProcessor imageProcessor;
          imageProcessor = _currentImageStack.getProcessor(i);
          _currentImagePlus = new ImagePlus("Image" + i, imageProcessor);
         
            //_currentImagePlus.repaintWindow();
            _image = null;
            _image = _currentImagePlus.getImage();
            //_currentImageStack.deleteSlice(i);
            
            if(_debugging){
                _debug("Image = " + _image);   
               }
            System.out.println("Image = " + _image);
            //FIXME Output an array
            _awtImageToken[i-1] = new AWTImageToken(_image);
            //_awtImageToken = new AWTImageToken(_image);
            //output.broadcast(new AWTImageToken(_image));
            if(_debugging){
             _debug("Output  = " + _image);   
            }
           // _currentImagePlus = null;
          //  _currentImagePlus = (ImagePlus) objectToken.getValue();

           // _currentImagePlus.setSlice(_index);
            //System.out.println("Output Slice " + _index);
            //Image[] image = new Image[_stackSize];
            //image[_index] = _currentImagePlus.getImage();
           //  _imagePlus = new ImagePlus("Image Stack", _imageStack);
           //  System.out.println("stackSize = " + _imageStack.getSize());
              
        }
        output.send(0, _awtImageToken, _awtImageToken.length);
            //output.send(0, _awtImageToken);
    }
    
  /*  public boolean postfire() throws IllegalActionException{
    	if(_index < _stackSize){
    		    return true  ;
    	}else{
    		return false;  
    	}
    }*/
    

    public void initialize() throws IllegalActionException {
        // _parameterPort =  input.getPort();
        _xResolution = ((IntToken) xResolution.getToken()).intValue();
        _yResolution = ((IntToken) yResolution.getToken()).intValue();
        _stackSize = ((IntToken) stackSize.getToken()).intValue();
      //  AWTImageToken[] _awtImageToken = new AWTImageToken[_stackSize];
        //AWTImageToken[] _awtImageToken = null;
    }

    /*public boolean prefire() throws IllegalActionException {
     ObjectToken objectToken = (ObjectToken)input.get(0);
     _currentImagePlus = (ImagePlus)objectToken.getValue();
     Token rateToken = input_tokenConsumptionRate.getToken();
     int required = ((IntToken) rateToken).intValue();

     // Derived classes may convert the input port to a multiport.
     for (int i = 0; i < input.getWidth(); i++) {
     if (!input.hasToken(i, required)) {
     if (_debugging) {
     _debug("Called prefire(), which returns false");
     }

     return false;
     }
     }

     return super.prefire();

     }*/

    public boolean postfire() throws IllegalActionException {
        if (!_stopRequested && _index < _stackSize) {
            if (_debugging) {
                _debug("Called postfire(), which returns true");
            }
            return true;
        } else {
            if (_debugging) {
                _debug("Called postfire(), which returns false");
            }
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    
    private AWTImageToken[] _awtImageToken;
    //private AWTImageToken _awtImageToken;
    
    //Image that is readin
    private ImagePlus _imagePlus;
    

    private ImagePlus _currentImagePlus;
    private ImageStack _currentImageStack;

    //Image that is readin
    private ColorProcessor _colorProcessor;

    //Image that is readin
    private ImageStack _imageStack;

    // Image that is read in.
    private Image _image;

    private int _stackSize;

    private int _xResolution;

    private int _yResolution;

    private int _index = 0;
}
