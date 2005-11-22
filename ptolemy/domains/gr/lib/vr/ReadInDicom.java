/* An actor that reads DICOM files.    */
 
  
package ptolemy.domains.gr.lib.vr;

import java.net.URL;
import java.lang.String;

import ij.*;
import ij.ImagePlus;
import ij.plugin.filter.Info.*;


import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.image.ImageReader;
import ptolemy.data.AWTImageToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.BaseType;
import ptolemy.actor.parameters.FilePortParameter;

//////////////////////////////////////////////////////////////////////////
////ReadInDicom
/**
 An actor that reads DICOM files. 

@see ptolemy.actor.lib.medicalimaging

@author T. Crawford
@version 
@since 
@Pt.ProposedRating Red
@Pt.AcceptedRating Red

*/public class DICOMReader extends ImageReader {
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
        super(container,name);
        
           
        //FIXME:Why doesn't the base class do this?        
        fileOrURL.setExpression("$CLASSPATH/doc/img/PtolemyII.jpg");
    }

    ////////////////////////////////////////////////////////////////////
    ////////               ports and parameters                  ////////     
    
    
       
    ////////////////////////////////////////////////////////////////////
    ////////                public methods                     ////////     
        
      
    public boolean prefire() throws IllegalActionException {
        if (_url == null) {
            throw new IllegalActionException("sourceURL was null");
        }
        _fileRoot = _url.getFile();
        if (_imagePlus == null) {
            _imagePlus = new ImagePlus(_fileRoot);
        }
        _image = _imagePlus.getImage();
        //FIXME Should not return true
        return true;
         
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    
    //Image that is readin
    private ImagePlus _imagePlus;

    //Metadata
    //private Info _info;
    //private Info _infoData;
 }
