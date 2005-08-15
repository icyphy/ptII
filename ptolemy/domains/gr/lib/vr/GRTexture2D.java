/* An actor that performs volume rendering using 2D textures.

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


import java.awt.image.ColorModel;
import java.awt.Point;
import java.awt.image.ComponentSampleModel;
import javax.imageio.ImageIO;
import java.io.IOException;

import java.awt.Component;
import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.Transparency;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;

import java.net.URL;

import javax.imageio.stream.FileImageInputStream;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TexCoordGeneration;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;
import javax.media.j3d.TextureAttributes;
import com.sun.j3d.utils.image.TextureLoader;
import javax.media.j3d.View;
import java.util.Hashtable;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.data.ImageToken;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.IntToken;

import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.gr.lib.vr.TextureLoader.MyTextureLoader;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// GRTexture2D


/** 
    An actor that performs volume rendering using 2D textures.
    The <i>xResolution</i> and <i>yResolution</i> parameters are used to specifiy
    the resoltion of the image.
    <p>
    
    @author Tiffany Crawford
    @version
    @since
    @Pt.ProposedRating Red
    @Pt.AcceptedRating Red
*/


public class GRTexture2D extends GRGeometry {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public GRTexture2D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        //voxelFile = new FilePortParameter(this, "voxelFile");
        //voxelFile.setExpression("$CLASSPATH/doc/img/brainMRI.jpg");


        //FIXME Should this be one parameter, ie. 256x256
        xResolution = new Parameter(this, "xResolution");
        xResolution.setExpression("256");
        xResolution.setTypeEquals(BaseType.INT);

        yResolution = new Parameter(this, "yResolution");
        yResolution.setExpression("256");
        yResolution.setTypeEquals(BaseType.INT);
        
/*        inputURL = new TypedIOPort(this, "inputURL");
        inputURL.setInput(true);
        inputURL.setTypeEquals(BaseType.OBJECT);*/

    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////
    
    /* Second Input */
//    public TypedIOPort inputURL;
    
    /** x Resolution */
    public Parameter xResolution;

    /** y Resolution */
    public Parameter yResolution;


    

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Initialize variables to parameter values.
     * @exception IllegalActionException If the current director
     *  is not a GRDirector.
     */

    /** The input port that reads a in a URL to the file holding the
     *  volume to be rendered.
     */
    // public FilePortParameter voxelFile;

    public void initialize() throws IllegalActionException {
        super.initialize();
        _sSize = (int) ((IntToken) xResolution.getToken()).intValue();
        _tSize = (int) ((IntToken) yResolution.getToken()).intValue();
        _counter = 0;

    }

    /** Return false if the scene graph is already initialized.
     *  @return False if the scene graph is already initialized.
     *  @exception IllegalActionException Not thrown in this base class
     * @throws
     */
    /*public boolean prefire() throws IllegalActionException {
     if (_debugging) {
     _debug("Called prefire()");
     _debug("_isSceneGraphInitialized = " + _isSceneGraphInitialized);
     _debug("Does port have token?" + _parameterPort.hasToken(0));
     }

     if (_parameterPort.hasToken(0)){
     texture.update();

     /** Set _isSceneGraphInitialized back to false so
     * node can be sent. fire() will set it back to true
     */

    /*       _createModel();
     //FIXME: Problem with name of variable, talk to Edward
     _isSceneGraphInitialized = false;

     if (_debugging) {
     _debug("Prefire returned true");
     _debug("texture = " + texture);
     }
     return true;
     }else {

     return false;
     }
     } */

    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    /** Create the geometry for the Node that will hold the texture.
     * @exception IllegalActionException 
     */
    protected void _createGeometry() throws IllegalActionException {
        _plane = new QuadArray(4, GeometryArray.COORDINATES
                | GeometryArray.TEXTURE_COORDINATE_2);

        if (_debugging) {
            _debug("inside _createGeometry");
            _debug("_axis = " + _axis);
        }

        _quadCoords = new double[12];
        _texCoords = new float[8];

        if (_axis == 1) {
            double curY = _counter * _planeSpacing - .5;

            if (_debugging) {
                _debug("counter = " + _counter);
                _debug("curY = " + curY);

            }

            //Set coordinates for the plane.  These coordinates assume
            //that the the image's origin is at the lower left and rotates
            // it 90 degrees about the x-axis.
            
            // lower left
            _quadCoords[0] = -0.5;
            _quadCoords[1] = curY;
            _quadCoords[2] = 0.5;
            _texCoords[0] = 0;
            _texCoords[1] = 0;

            // lower right
            _quadCoords[3] = 0.5;
            _quadCoords[4] = curY;
            _quadCoords[5] = 0.5;
            _texCoords[2] = 1;
            _texCoords[3] = 0;

            // upper right
            _quadCoords[6] = 0.5;
            _quadCoords[7] = curY;
            _quadCoords[8] = -0.5;
            _texCoords[4] = 1;
            _texCoords[5] = 1;

            // upper left
            _quadCoords[9] = -0.5;
            _quadCoords[10] = curY;
            _quadCoords[11] = -0.5;
            _texCoords[6] = 0;
            _texCoords[7] = 1;
        } else if (_axis == 2) {

            double curZ = _counter * _planeSpacing - .5;
            //Set coordinates for the plane.  These coordinates assume
            //that the the image's origin is at the lower left and the planes
            //are aligned accordingly
            
            // lower left
            _quadCoords[0] = -0.5;
            _quadCoords[1] = -0.5;
            _quadCoords[2] = curZ;
            _texCoords[0] = 0;
            _texCoords[1] = 0;

            // lower right
            _quadCoords[3] = 0.5;
            _quadCoords[4] = -0.5;
            _quadCoords[5] = curZ;
            _texCoords[2] = 1;
            _texCoords[3] = 0;

            // upper right
            _quadCoords[6] = 0.5;
            _quadCoords[7] = 0.5;
            _quadCoords[8] = curZ;
            _texCoords[4] = 1;
            _texCoords[5] = 1;

            // upper left
            _quadCoords[9] = -0.5;
            _quadCoords[10] = 0.5;
            _quadCoords[11] = curZ;

            _texCoords[6]= 0;
            _texCoords[7]= 1;
        }else if (_axis == 0){
            double curX = _counter * _planeSpacing -.5;
            
            //Set coordinates for the plane.  These coordinates assume
            //that the the image's origin is at the lower left and the planes
            //are aligned accordingly
            

            _texCoords[6] = 0;
            _texCoords[7] = 1;
        } else if (_axis == 0) {
            double curX = _counter * _planeSpacing - .5;
            /** Set coordinates for the plane.  These coordinates assume
             * that the the image's origin is at the lower left and the planes
             * are aligned accordingly
             */

            // lower left
            _quadCoords[0] = curX;
            _quadCoords[1] = -0.5;
            _quadCoords[2] = 0.5;
            _texCoords[0] = 0;
            _texCoords[1] = 0;

            // lower right
            _quadCoords[3] = curX;
            _quadCoords[4] = -0.5;
            _quadCoords[5] = -0.5;
            _texCoords[2] = 1;
            _texCoords[3] = 0;

            // upper right
            _quadCoords[6] = curX;
            _quadCoords[7] = 0.5;
            _quadCoords[8] = -0.5;
            _texCoords[4] = 1;
            _texCoords[5] = 1;

            // upper left
            _quadCoords[9] = curX;
            _quadCoords[10] = 0.5;
            _quadCoords[11] = 0.5;
            _texCoords[6] = 0;
            _texCoords[7] = 1;

        } else {
            if (_debugging) {
                _debug("chose none of them");

            }
        }

        _plane.setCoordinates(0, _quadCoords);
        _plane.setTextureCoordinates(0, 0, _texCoords);
        _geometry = _plane;
    }


    /**Read and load the image to be texture mapped. 
     * Set the appearance of this 3D object.
     *  @exception IllegalActionException 
     */


    protected void _createModel() throws IllegalActionException {
        _readImage();
        _counter++;
        super._createModel();
        _loadTexture();
    }

    /** Create the texture used for this 3D object.
     * Define the texture coordinates and textureAttributes.
     * @throws IllegalActionException
     */
    protected void _loadTexture() throws IllegalActionException {
    	//Debug statement
        if (_debugging) {
            _debug("About to loadTexture");
        }

        //Check for valid url, if no get file name as a String
        if (_url == null) {
            throw new IllegalActionException("sourceURL was null");
            }
            _fileRoot = _url.getFile(); 
            
        //Read in image as a BufferedImage
        try {
            _bufferedImage = (BufferedImage)ImageIO.read (new File(_fileRoot));
            } catch (IOException e) {
            System.err.println(e);
            _bufferedImage = null;
        }
        
        //Create WritableRaster
        //FIXME Are the parameters for ComponentSampleModel correct?
        ComponentSampleModel componentSampleModel = 
            new ComponentSampleModel(0, _sSize, _tSize, 0, _sSize, new int[]{0,0,0,0});
        Raster raster = _bufferedImage.getData();
        DataBuffer dataBuffer = raster.getDataBuffer();
        WritableRaster writableRaster = Raster.createWritableRaster(componentSampleModel, 
                dataBuffer, new Point()); 
        
        System.out.println("Data buffer type = " + dataBuffer.getDataType());
        
        
        //Create ColorModel
        //FIXME Are the parameters for ComponentColorModel correct?
        ComponentColorModel componentColorModel = new ComponentColorModel 
         (ColorSpace.getInstance(ColorSpace.CS_sRGB) ,
                    new int[] {8,8,8,8} , // bits
                    true, // alpha
                    false , // alpha pre-multiplied
                    Transparency.TRANSLUCENT ,
                    DataBuffer.TYPE_BYTE );
        Hashtable hashtable = new Hashtable();
        //_bufferedImage = new BufferedImage( componentColorModel, writableRaster,
           //     false, hashtable); 
      
    
        //      Get alpha raster and manipulate values
   /*     int arrayLength = _sSize*_tSize;
        double fraction = 1/255;
        double[] pixelArray = new double[arrayLength*4];
        double[] alphaArray = new double[arrayLength]; */
        
       
        //Create alpha array and set as function of pixel values
      /*  writableRaster.getPixels(writableRaster.getMinX(),writableRaster.getMinY(),
                _sSize,_tSize, pixelArray ); 
        for (int i=1; i == arrayLength; i ++){
        	//alphaArray[i] = 1 - pixelArray[i*4 -2]*fraction;
            alphaArray[i]= 0.5;
        }
        writableRaster.setSamples(0,0,_sSize,_tSize,3, alphaArray);*/
        
        //Create bufferedImage and Load
        _bufferedImage = new BufferedImage(componentColorModel, writableRaster,
                false, hashtable);
        TextureLoader loader = new TextureLoader(_bufferedImage,_viewScreen.getCanvas());
       

        /* Set loaded texture*/
        Texture loadedTexture = loader.getTexture();
        TextureAttributes attributes = null; 
        if (loadedTexture != null) {
            attributes = new TextureAttributes();
            attributes.setTextureMode(TextureAttributes.MODULATE);
            _appearance.setTextureAttributes(attributes);
            _appearance.setTexture(loadedTexture);
        }
        
        /*  MyTextureLoader loader;
        //     if (_token.getClass().toString() == "class ptolemy.data.AWTImageToken"){
        loader = new MyTextureLoader(_image, _viewScreen.getCanvas());
        //   }else {
        //  loader = new MyTextureLoader(_fileRoot, _viewScreen.getCanvas());
        // }*/

    }



    /**Read the image file.
     * @exception IllegalActionException 
     */

    /**Read in file. */
    protected void _readImage() throws IllegalActionException {
       
        _token = input.get(0);
        ObjectToken objectToken = (ObjectToken) _token;
        _url = (URL) objectToken.getValue();
        _fileRoot = _url.getFile();
        
        /**Use if input is an ImageToken */
        /*   _token = input.get(0);
            ImageToken imageToken = (ImageToken) _token;
            _image = imageToken.asAWTImage();
            System.out.println("token = " + _token.getType());
            System.out.println("token = " + _token.getClass().toString());*/
        
        /*
        _url = texture.asURL();
        /**Read in image containing data to be mapped
        if (_url == null) {
        throw new IllegalActionException("sourceURL was null");
        }
        _fileRoot = _url.getFile();
        if (_imagePlus == null) {
        _imagePlus = new ImagePlus(_fileRoot);
        } */


    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** ?????? */
    protected View _view;

    /** Image to be texture mapped */
    protected Image _image;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The URL that specifies where the file is located. */
    private URL _url;

    /** The NodeComponent defining the texture which must be added to the Appearance */
    private Texture2D _texture2D;

    /** ImageComponent. */
    private ImageComponent2D _imageComponent;

    /** QuadArray. */
    private QuadArray _plane;

    /** Buffer of image data */
    private BufferedImage _bufferedImage;

    /**Defines how to translate the pixels into color and alpha components.*/
    private ColorModel _colorModel;

    /** The ColorSpace that defines the color space of the image */
    private ColorSpace _colorSpace;



    //private ImagePlus _imagePlus;

    private String _fileRoot;

    private File _file;

    private FileImageInputStream _fileImageInputStream;

    private Texture2D _texture;

    private Token _token;
    
    private Token _tokenURL;

    private TexCoordGeneration _texCoordGeneration;

    private WritableRaster _alphaRaster;

    private WritableRaster _dataRaster;

    private int[] _intData;

    private double[] _quadCoords;

    private float[] _texCoords;

    //private TexCoord2f[] _texCoords;

    private MyTextureLoader _myTextureLoader;

    private DataBufferInt _dataBufferInt;

    private DataBuffer _dataBuffer;

    private int _sSize;

    private int _tSize;

    private ParameterPort _parameterPort;

    private StringToken _stringToken;

    private int _counter;

    private String _eof;

}
