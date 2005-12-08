/* An actor that reads DICOM files.    

Copyright (c) 1998-2005 The Regents of the University of California.
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


import java.awt.Image;
import java.lang.String;
import java.net.URL;


import ij.ImagePlus;
import ij.IJ;
import ij.plugin.DICOM;




import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.FilePortParameter;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.data.AWTImageToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


//////////////////////////////////////////////////////////////////////////
////MedicalImageReader
/**
   An actor that reads image files including .dcm, using NIH ImageJ.

   @author T. Crawford
   @version
   @since
   @Pt.ProposedRating Red
   @Pt.AcceptedRating Red

*/public class MedicalImageReader extends TypedAtomicActor{
    /**Construct an actor with the given container and name.
      
     
     * @param container The container
     * @param name The name of this actor
     * @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     * @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */

    public MedicalImageReader(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        fileOrURL = new FilePortParameter(this, "fileOrURL");
        fileOrURL.setExpression("null");

        
        StringOrURL = new Parameter(this, "URLOrString");
        StringOrURL.setTypeEquals(BaseType.BOOLEAN);
        StringOrURL.setExpression("True");

        output = new TypedIOPort(this, "output");
        output.setOutput(true);
        output.setTypeEquals(BaseType.OBJECT);

    }

    ////////////////////////////////////////////////////////////////////
    ////////               ports and parameters                  ////////
    /** The directory name or URL from which to read.  This is a string with
     *  any form accepted by FileParameter.
     *  @see FileParameter
     */
    public FilePortParameter fileOrURL;
    
    /** The output port */
    public TypedIOPort output;
    
    /** A boolean that is true if input is a URL */
    public Parameter StringOrURL;

    ////////////////////////////////////////////////////////////////////
    ////////                public methods                     ////////


    /** Output the data read in the prefire() method.
     *  @exception IllegalActionException If there's no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        output.broadcast(new AWTImageToken(_image));
    }

    /** Access ParameterPort assoicated with FilePortParamter
     *  @exception IllegalActionException If there's no director.
     */
    public void initialize() throws IllegalActionException
    {
        _parameterPort =  fileOrURL.getPort();
        //_parameterPort.setTypeEquals(BaseType.STRING);
    }


    /** Check to see if data is present at port if so update paramter,
     *  read in image and return true. If not return false.
     *  @exception IllegalActionException If there's no director.
     */
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
    
    /** Read the image from the URL or String provided */
    protected void _readImage() throws IllegalActionException {
        if (_isURL)
        {
        	_url = fileOrURL.asURL();

        	if (_url == null) {
        		throw new IllegalActionException("sourceURL was null");
        	}
        	_fileRoot = _url.getFile();
        }else{
        	_fileRoot = fileOrURL.stringValue();
        }
        
        //if (_imagePlus == null) {
        if (_imagePlus == null) {
            _image = ((ImagePlus) IJ.runPlugIn("ij.plugin.DICOM", _fileRoot))
                    .getImage();
        }

      /*      _imagePlus = new ImagePlus(_fileRoot);
            _image = _imagePlus.getImage();*/
            
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    
    //  The URL as a string.
    private String _fileRoot;

    // Image that is sent to output port.
    private Image _image;
    
    //  Image that is read in, in ImageJ format
    private ImagePlus _imagePlus;
    
    //  Boolean identifying input type
    private boolean _isURL;
    
    //  Port associated with the FilePortParameter
    private ParameterPort _parameterPort;
    
    //  The URL of the file.
    private URL _url;
}
