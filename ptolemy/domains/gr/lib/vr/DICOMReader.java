/* An actor that reads DICOM files.    */
 
  
package ptolemy.domains.gr.lib.vr;


import java.awt.Image;
import java.lang.String;
import java.net.URL;


import ij.ImagePlus;
import ij.IJ;
import ij.plugin.DICOM;

import vendors.ImageJ.ij.ImagePlus;



import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.CompositeEntity;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.AWTImageToken;
import ptolemy.data.type.BaseType;
import ptolemy.actor.parameters.FilePortParameter;
import ptolemy.actor.parameters.ParameterPort;

//////////////////////////////////////////////////////////////////////////
////DICOMReader
/**
 An actor that reads DICOM files. 

@see ptolemy.actor.lib.medicalimaging

@author T. Crawford
@version 
@since 
@Pt.ProposedRating Red
@Pt.AcceptedRating Red

*/public class DICOMReader extends TypedAtomicActor{
	    /**Construct an actor with the given container and name.
         * @param container The container
         * @param name The name of this actor
         * @exception IllegalActionException If the actor cannot be contained
         *   by the proposed container.
         * @exception NameDuplicationException If the container already has an
         *   actor with this name.
         */
    
	public DICOMReader(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
         super(container, name);
        
        fileOrURL = new FilePortParameter(this, "fileOrURL");           
        
        output = new TypedIOPort(this, "output");
        output.setOutput(true);
        output.setTypeEquals(BaseType.OBJECT);
              
    }

    ////////////////////////////////////////////////////////////////////
    ////////               ports and parameters                  ////////     
    
    public FilePortParameter fileOrURL;
    public TypedIOPort output;
       
    ////////////////////////////////////////////////////////////////////
    ////////                public methods                     ////////     

    
    /** Output the data read in the prefire.
     *  @exception IllegalActionException If there's no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        output.broadcast(new AWTImageToken(_image));
    }
    
    
    public void initialize() throws IllegalActionException
    {
      _parameterPort =  fileOrURL.getPort();  
    }
    
    
    public boolean prefire() throws IllegalActionException {
        super.prefire();
        if (_parameterPort.hasToken(0)){
        	fileOrURL.update();
            _readImage();
            return true;
        }else {
            return false;
        }  
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private void _readImage() throws IllegalActionException {
        _url = fileOrURL.asURL();
        
        if (_url == null) {
            throw new IllegalActionException("sourceURL was null");
        }
        _fileRoot = _url.getFile();
        if (_imagePlus == null) {
            //_imagePlus = new ImagePlus(_fileRoot);
            //_image = _imagePlus.getImage();
            DICOM _dicom;
            _image = ((ImagePlus)IJ.runPlugIn("ij.plugin.DICOM", _fileRoot)).getImage();
           
        }

          
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    
    //Image that is readin
    private ImagePlus _imagePlus;
//  The URL as a string.
    private String _fileRoot;

    // Image that is read in.
    private Image _image;

    // The URL of the file.
    private URL _url;
    
    private ParameterPort _parameterPort;

 }
