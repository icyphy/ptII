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

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.data.AWTImageToken;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.gr.lib.vr.TextureLoader.MyTextureLoader;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.sun.j3d.utils.image.TextureLoader;

import java.awt.Component;
import java.awt.Image;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TexCoordGeneration;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.View;

//////////////////////////////////////////////////////////////////////////
//// GRTexture2D

/**
 An actor that performs volume rendering using 2D textures.
 The <i>xResolution</i> and <i>yResolution</i> parameters are used to specifiy
 the resolution of the image.
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
            double curY = (_counter * _planeSpacing) - .5;

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
            double curZ = (_counter * _planeSpacing) - .5;

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

            _texCoords[6] = 0;
            _texCoords[7] = 1;
        } else if (_axis == 0) {
            double curX = (_counter * _planeSpacing) - .5;

            //Set coordinates for the plane.  These coordinates assume
            //that the the image's origin is at the lower left and the planes
            //are aligned accordingly
            _texCoords[6] = 0;
            _texCoords[7] = 1;
        } else if (_axis == 0) {
            double curX = (_counter * _planeSpacing) - .5;

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
        int arrayLength = _sSize * _tSize;

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
            _bufferedImage = (BufferedImage) ImageIO.read(new File(_fileRoot));
        } catch (IOException e) {
            System.err.println(e);
            _bufferedImage = null;
        }

        System.out.println("AlphaRaster = " + _bufferedImage.getAlphaRaster());
        System.out.println("PixelStride = "
                + ((ComponentSampleModel) _bufferedImage.getSampleModel())
                        .getPixelStride());
        System.out.println("ScanlineStride = "
                + ((ComponentSampleModel) _bufferedImage.getSampleModel())
                        .getScanlineStride());

        //Create WritableRaster
        //FIXME Are the parameters for ComponentSampleModel correct?
        int pixelStride = 3;
        int scanlineStride = _sSize * 3;
        ComponentSampleModel componentSampleModel = new ComponentSampleModel(0,
                _sSize, _tSize, pixelStride, scanlineStride, new int[] { 0, 0,
                        0, 0 });

        System.out.println("# dataElements = "
                + componentSampleModel.getNumDataElements());
        System.out.println("# Bands = " + componentSampleModel.getNumBands());
        System.out.println("componentSampleModel width = "
                + componentSampleModel.getWidth());
        System.out.println("componentSampleModel height = "
                + componentSampleModel.getHeight());
        System.out.println("componentSampleModel dataType = "
                + componentSampleModel.getDataType());
        System.out.println("componentSampleModel transferType = "
                + componentSampleModel.getTransferType());
        System.out.println("componentSampleModel size = "
                + componentSampleModel.getSampleSize(0)
                + componentSampleModel.getSampleSize(1)
                + componentSampleModel.getSampleSize(2)
                + componentSampleModel.getSampleSize(3));
        System.out.println("componentSampleModel  = " + componentSampleModel);
        System.out.println("componentSampleModel pixelStride = "
                + ((ComponentSampleModel) componentSampleModel)
                        .getPixelStride());
        System.out.println("componentSampleModel scanlineStride = "
                + ((ComponentSampleModel) componentSampleModel)
                        .getScanlineStride());

        int[] bankIndices1 = ((ComponentSampleModel) componentSampleModel)
                .getBankIndices();
        System.out.println("componentSampleModel # bank Indices = "
                + bankIndices1[0] + ", " + bankIndices1[1] + ", "
                + bankIndices1[2] + ", " + bankIndices1[3]);

        int[] offset1 = ((ComponentSampleModel) componentSampleModel)
                .getBandOffsets();
        System.out.println("componentSampleModel band offsets = " + offset1[0]
                + ", " + offset1[1] + ", " + offset1[2] + ", " + offset1[3]);

        //Create ColorModel and componentSampleModel
        ComponentColorModel componentColorModelwoAlpha = new ComponentColorModel(
                ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] { 8, 8,
                        8, 8 }, // bits
                true, // alpha
                false, // alpha pre-multiplied
                Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
        ComponentColorModel componentColorModel = new ComponentColorModel(
                ColorSpace.getInstance(ColorSpace.CS_sRGB),
                //new int[] {64,64,64,64} , // bits
                true, // alpha
                false, // alpha pre-multiplied
                Transparency.TRANSLUCENT,
                //DataBuffer.TYPE_BYTE 
                // DataBuffer.TYPE_FLOAT
                DataBuffer.TYPE_DOUBLE);

        //Create Writable Raster
        Raster raster = _bufferedImage.getData();
        DataBuffer dataBuffer = raster.getDataBuffer();
        WritableRaster writableRaster = Raster.createWritableRaster(
                componentSampleModel, dataBuffer, new Point());
        System.out.println("DataBufferSize = " + dataBuffer.getSize());

        Hashtable hashtable = new Hashtable();

        BufferedImage bufferedImagewoAlpha = new BufferedImage(
                componentColorModelwoAlpha, writableRaster, false, hashtable);

        //      Get alpha raster and manipulate values
        double fraction = 1 / 255;

        //float[] pixelArray = new float[arrayLength*4];
        double[] pixelArray = new double[arrayLength * 4];

        //double[] alphaArray = new double[arrayLength]; 
        double[] alphaArray = new double[arrayLength];

        //Create alpha array and set as function of pixel values
        System.out.println("pixelArray length = " + pixelArray.length);
        System.out.println("min, max = " + writableRaster.getMinX() + ", "
                + writableRaster.getMinY());
        writableRaster.getPixels(writableRaster.getMinX(), writableRaster
                .getMinY(), _sSize, _tSize, pixelArray);
        System.out.println("pixelArray length = " + pixelArray.length);

        for (int i = 1; i <= arrayLength; i++) {
            pixelArray[(i * 4) - 1] = 0.0; //when left uncommented only, gives black (alpha channel)

            //pixelArray[i*4 - 2]= 0; //when left uncommented only, gives green (blue)
            // pixelArray[i*4 - 3]= 0; //when left uncommented only, gives purple (green)
            //pixelArray[i*4 - 4]= 0; //when left uncommented only, gives yellowish green (red)
            //pixelArray[i*4-1] = 1 - pixelArray[i*4 -1]*fraction;
            //alphaArray[i] = 1 - pixelArray[i*4 -2]*fraction;
            //alphaArray[i]= 0.5;
            //Normalize values
            // pixelArray[i*4-2] = pixelArray[i*4-2]* fraction; 
            //   pixelArray[i*4-3] = pixelArray[i*4-3]* fraction;
            //   pixelArray[i*4-4] = pixelArray[i*4-4]* fraction;
            //pixelArray[i*4] = pixelArray[i*4]* fraction; 
            //pixelArray[i*4+1] = pixelArray[i*4+1]* fraction;
            //pixelArray[i*4+2] = pixelArray[i*4+2]* fraction;
        }

        //float[] dataArray =  new float[arrayLength*4];
        SampleModel sampleModel = componentColorModel
                .createCompatibleSampleModel(_sSize, _tSize);
        System.out.println("# dataElements = "
                + sampleModel.getNumDataElements());
        System.out.println("# Bands = " + sampleModel.getNumBands());
        System.out.println("SampleModel width = " + sampleModel.getWidth());
        System.out.println("SampleModel height = " + sampleModel.getHeight());
        System.out.println("SampleModel dataType = "
                + sampleModel.getDataType());
        System.out.println("SampleModel transferType = "
                + sampleModel.getTransferType());
        System.out.println("SampleModel size = " + sampleModel.getSampleSize(0)
                + sampleModel.getSampleSize(1) + sampleModel.getSampleSize(2)
                + sampleModel.getSampleSize(3));
        System.out.println("SampleModel  = " + sampleModel);
        System.out.println("SampleModel pixelStride = "
                + ((ComponentSampleModel) sampleModel).getPixelStride());
        System.out.println("SampleModel scanlineStride = "
                + ((ComponentSampleModel) sampleModel).getScanlineStride());

        int[] bankIndices = ((ComponentSampleModel) sampleModel)
                .getBankIndices();
        System.out.println("SampleModel # bank Indices = " + bankIndices[0]
                + ", " + bankIndices[1] + ", " + bankIndices[2] + ", "
                + bankIndices[3]);

        int[] offset = ((ComponentSampleModel) sampleModel).getBandOffsets();
        System.out.println("SampleModel band offsets = " + offset[0] + ", "
                + offset[1] + ", " + offset[2] + ", " + offset[3]);

        //Create DataBuffer with alpha channel
        DataBufferDouble dataBufferDouble = new DataBufferDouble(pixelArray,
                pixelArray.length);

        System.out.println("pixel array values = " + pixelArray[20480] + ", "
                + pixelArray[20481] + ", " + pixelArray[20482] + ", "
                + pixelArray[20483]);

        System.out.println("DataBuffer values = "
                + dataBufferDouble.getElemDouble(20480) + ", "
                + dataBufferDouble.getElemDouble(20481) + ", "
                + dataBufferDouble.getElemDouble(20482) + ", "
                + dataBufferDouble.getElemDouble(20483));

        System.out.println("DataBuffer values = "
                + dataBufferDouble.getElemDouble(30720) + ", "
                + dataBufferDouble.getElemDouble(30721) + ", "
                + dataBufferDouble.getElemDouble(30722) + ", "
                + dataBufferDouble.getElemDouble(30723));

        System.out.println("DataBuffer values = "
                + dataBufferDouble.getElemDouble(21504) + ", "
                + dataBufferDouble.getElemDouble(21505) + ", "
                + dataBufferDouble.getElemDouble(21506) + ", "
                + dataBufferDouble.getElemDouble(21507));

        System.out.println("DataBuffer values = "
                + dataBufferDouble.getElemDouble(31744) + ", "
                + dataBufferDouble.getElemDouble(31745) + ", "
                + dataBufferDouble.getElemDouble(31746) + ", "
                + dataBufferDouble.getElemDouble(31747));

        //Create Writable RAaster with new DataBuffer

        /*writableRaster = Raster.createWritableRaster(componentSampleModelwAlpha,
         dataBufferDouble, new Point());*/
        ComponentSampleModel componentSampleModelwAlpha = new ComponentSampleModel(
                5, _sSize, _tSize, pixelStride + 1, scanlineStride + 256,
                new int[] { 0, 0, 0, 0 }, new int[] { 0, 1, 2, 3 });
        int[] offset2 = ((ComponentSampleModel) componentSampleModelwAlpha)
                .getBandOffsets();
        System.out.println("componentSampleModelwAlpha band offsets = "
                + offset2[0] + ", " + offset2[1] + ", " + offset2[2] + ", "
                + offset2[3]);
        writableRaster = Raster.createWritableRaster(
                componentSampleModelwAlpha, dataBufferDouble, new Point());

        //writableRaster.setSamples(0,0,_sSize,_tSize,0, alphaArray);
        //Create bufferedImage and Load
        _bufferedImage = new BufferedImage(componentColorModel, writableRaster,
                false, hashtable);

        TextureLoader loader = new TextureLoader(bufferedImagewoAlpha,
                _viewScreen.getCanvas());
        System.out.println("AlphaRaster = " + _bufferedImage.getAlphaRaster());
        System.out.println("Alpha Raster values = "
                + _bufferedImage.getAlphaRaster().getSample(2, 2, 0));
        System.out.println("AlphaArray[4] = " + alphaArray[4]);

        /* Set loaded texture*/
        Texture loadedTexture = loader.getTexture();
        TextureAttributes attributes = null;

        if (loadedTexture != null) {
            attributes = new TextureAttributes();
            attributes.setTextureMode(TextureAttributes.MODULATE);
            _appearance.setTextureAttributes(attributes);
            _appearance.setTexture(loadedTexture);
        }
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

        /*    _token = input.get(0);
         AWTImageToken imageToken = (AWTImageToken) _token;
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
