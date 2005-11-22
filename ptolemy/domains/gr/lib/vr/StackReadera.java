/* An actor that reads an array of images.   */
 
  
package ptolemy.domains.gr.lib.vr;


import java.awt.Image;
import java.lang.String;
import java.net.URL;


import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ColorProcessor;






import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.CompositeEntity;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.domains.sdf.lib.SDFTransformer;
import ptolemy.data.AWTImageToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.IntToken;
import ptolemy.data.type.BaseType;
import ptolemy.actor.parameters.FilePortParameter;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
////StackReader
/**
 An actor that reads an array of images. 

@see ptolemy.actor.lib.medicalimaging

@author T. Crawford
@version 
@since 
@Pt.ProposedRating Red
@Pt.AcceptedRating Red

*/public class StackReader extends SDFTransformer{
	    /**Construct an actor with the given container and name.
         * @param container The container
         * @param name The name of this actor
         * @exception IllegalActionException If the actor cannot be contained
         *   by the proposed container.
         * @exception NameDuplicationException If the container already has an
         *   actor with this name.
         */
    
	public StackReader(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
         super(container, name);
        
      //  input = new FilePortParameter(this, "input");           
        
        input_tokenConsumptionRate.setExpression("stackSize");
        
        output.setTypeEquals(BaseType.OBJECT);
        
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
    
    public FilePortParameter input;
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
       // output.broadcast(new AWTImageToken(_image));
        output.broadcast(new ObjectToken(_imagePlus));
    }
    
    
    public void initialize() throws IllegalActionException
    {
      _parameterPort =  input.getPort();
      _xResolution = ((IntToken)xResolution.getToken()).intValue();
      _yResolution = ((IntToken)yResolution.getToken()).intValue();
      _stackSize = ((IntToken)stackSize.getToken()).intValue();
      
    }
    
    
    public boolean prefire() throws IllegalActionException {
        super.prefire();
      /*  if (_parameterPort.hasToken(0)){
        	input.update();
            _readImage();
            return true;
        }else {
            return false;*/
        if (!_parameterPort.hasToken(0, _stackSize)) {
            if (_debugging) {
                _debug("Called prefire(), which returns false.");
            }
            _readImage();
            return false;
        } else {
             _imagePlus = new ImagePlus("Image Stack", _imageStack); 
             System.out.println("stackSize = " + _imageStack.getSize());
            return super.prefire();
        }
       
        
       // }  
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private void _readImage() throws IllegalActionException {
          _imageStack = new ImageStack(_xResolution, _yResolution);
        //for(int i = 0; i< _stackSize; i++){
       	    
        if (_parameterPort.hasToken(0)){
            //input.update();
        	_url = input.asURL();
        
        	if (_url == null) {
        		throw new IllegalActionException("sourceURL was null");
        	}
        	_fileRoot = _url.getFile();
        
            
           
        //if (imagePlus == null) {
            //FIXME Should check each image to see if valid
            ImagePlus imagePlus = new ImagePlus(_fileRoot);
            _image = imagePlus.getImage();
            _colorProcessor = new ColorProcessor(_image);
            _imageStack.addSlice(_fileRoot, _colorProcessor);
            imagePlus = null;
            System.out.println("stackSize = " + _stackSize);
        
       }
    //}
       
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    
    //Image that is readin
    private ImagePlus _imagePlus;
    
    //Image that is readin
    private ColorProcessor _colorProcessor;
    
    
    //Image that is readin
    private ImageStack _imageStack;
    
    
//  The URL as a string.
    private String _fileRoot;

    // Image that is read in.
    private Image _image;

    // The URL of the file.
    private URL _url;
    
    private ParameterPort _parameterPort;

    private int _stackSize;
    
    private int _xResolution;
    
    private int _yResolution;
 }
