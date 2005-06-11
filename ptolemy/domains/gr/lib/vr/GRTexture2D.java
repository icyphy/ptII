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

import java.io.IOException;
import java.net.URL;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.ImageComponent;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.OrderedGroup;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Switch;
import javax.media.j3d.TexCoordGeneration;
import javax.media.j3d.Texture;
import javax.vecmath.TexCoord2f;
import javax.media.j3d.TextureAttributes;
import com.sun.j3d.utils.image.TextureLoader;
import javax.media.j3d.TransparencyAttributes;
import javax.swing.ImageIcon;
import javax.vecmath.Color3f;
import javax.vecmath.Vector4f;
import javax.media.j3d.View;
import javax.media.j3d.Shape3D;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.actor.parameters.DoubleRangeParameter;
import ptolemy.actor.parameters.IntRangeParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.ImageToken;
import ptolemy.data.StringToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.actor.parameters.FilePortParameter;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.gr.kernel.GRActor;
import ptolemy.domains.gr.kernel.GRActor3D;
import ptolemy.domains.gr.kernel.SceneGraphToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import java.io.FileNotFoundException;
import ptolemy.kernel.util.Workspace;

import javax.media.j3d.TexCoordGeneration;
import com.sun.j3d.utils.image.TextureLoader;
import javax.media.j3d.Texture2D;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.QuadArray;

//Used in filling in texture
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.awt.image.Raster;
import java.awt.color.ColorSpace;

import javax.vecmath.Point3d;

import vendors.vr.ColormapChoiceAttr;
import vendors.vr.Context;
import vendors.vr.VolFile;
import vendors.vr.Volume;
import vendors.vr.Texture2DVolume;
import vendors.vr.Axis2DRenderer;
import vendors.vr.StringAttr;
import vendors.vr.CoordAttr;
import vendors.vr.VolRend;

import java.awt.Image;
import java.awt.Transparency;

import javax.swing.ImageIcon;
import java.io.File;

import javax.imageio.ImageIO;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.ImageReadParam;


import javax.imageio.stream.FileImageInputStream;
import ij.ImagePlus;

import SourceCode.MyTextureLoader;

//////////////////////////////////////////////////////////////////////////
//// GRTexture2D

/** An abstract base class for GR Actors that have material and color
    properties.

    The <i>texture</i> parameter can be used to specify an image file.
    The specified image will be mapped onto the shape.
    <p>
    The parameter <i>transparency</i> determines the transparency of the
    object. It ranges from 0.0 (the default) to 1.0, meaning opaque to
    fully transparent (which makes the object invisible).
    <p>
    The <i>wireFrame</i> parameter can be used to view only the lines
    that outline the polygons of the object and not the surface.
    The <i>flat</i> parameter can be set to make rendered polygons
    flat rather than rounded at the corners.
    <p>
    The <i>allowRuntimeChanges</i> parameter, if true, specifies
    that changes to parameter values during the execution of the model
    take effect immediately. By default, this parameter is false,
    which means that changes to parameter values take effect only
    on the next run of the model. A value of false yields better
    performance, but less interactivity.  Changing this to true will
    only have an effect on the next run of the model.

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
     
        //FIXME Should this be one paramter, ie. 256x256
        xResolution = new Parameter(this, "xResolution");
        xResolution.setExpression("256");
        xResolution.setTypeEquals(BaseType.INT);
        
        yResolution = new Parameter(this, "yResolution");
        yResolution.setExpression("256");
        yResolution.setTypeEquals(BaseType.INT);  
        
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////
    /** This parameter provides a third dimension to the images.  The
     * value is a scaled version of the actual known slice depth.
     */
    
    /* The parameter that chooses along which axis the volume will be built.
     * The options are xAxis, yAxis, or zAxis.
     */  
    public Parameter xResolution;
    
    public Parameter yResolution;
    
    /** The input port that reads a in a URL to the file holding the 
     *  volume to be rendered.
     */
   // public FilePortParameter voxelFile;
    
    
     
    public void initialize() throws IllegalActionException {
        super.initialize();
        /** Initialize some variables */
       // _parameterPort = voxelFile.getPort();_parameterPort = voxelFile.getPort();
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
     */
    protected void _createGeometry() throws IllegalActionException {          
    	_plane = new QuadArray(4, GeometryArray.COORDINATES|
                GeometryArray.TEXTURE_COORDINATE_2);
        
        if (_debugging) {
            _debug("inside _createGeometry");
            _debug("_axis = " + _axis);
           }
        
        _quadCoords = new double [12];
        _texCoords = new float [8];
        
        if(_axis == 1){
        double curY = _counter * _planeSpacing -.5;
        
        if (_debugging) {
         _debug("counter = " + _counter);   
         _debug("curY = " + curY);
         
        }
        
        /** Set coordinates for the plane.  These coordinates assume 
         * that the the image's origin is at the lower left and rotates
         * it 90 degrees about the x-axis.
         */
        // lower left
        _quadCoords[0] = -0.5;
        _quadCoords[1] = curY;
        _quadCoords[2] = 0.5;
        _texCoords[0]= 0;
        _texCoords[1]= 0;
       
        
        // lower right
        _quadCoords[3] = 0.5;
        _quadCoords[4] = curY;
        _quadCoords[5] = 0.5;
        _texCoords[2]= 1;
        _texCoords[3]= 0;
      
        
        // upper right
        _quadCoords[6] = 0.5;
        _quadCoords[7] = curY;
        _quadCoords[8] = -0.5;
        _texCoords[4]= 1;
        _texCoords[5]= 1;
     
        
        // upper left
        _quadCoords[9] = -0.5;
        _quadCoords[10] = curY;
        _quadCoords[11] = -0.5;
        _texCoords[6]= 0;
        _texCoords[7]= 1;
        } else if (_axis == 2){
            
            double curZ = _counter * _planeSpacing - .5;
            /** Set coordinates for the plane.  These coordinates assume 
             * that the the image's origin is at the lower left and the planes 
             * are aligned accordingly
             */
            // lower left
            _quadCoords[0] = -0.5;
            _quadCoords[1] = -0.5;
            _quadCoords[2] = curZ;
            _texCoords[0]= 0;
            _texCoords[1]= 0;
           
            
            // lower right
            _quadCoords[3] = 0.5;
            _quadCoords[4] = -0.5;
            _quadCoords[5] = curZ;
            _texCoords[2]= 1;
            _texCoords[3]= 0;
          
            
            // upper right
            _quadCoords[6] = 0.5;
            _quadCoords[7] = 0.5;
            _quadCoords[8] = curZ;
            _texCoords[4]= 1;
            _texCoords[5]= 1;
         
            
            // upper left
            _quadCoords[9] = -0.5;
            _quadCoords[10] = 0.5;
            _quadCoords[11] = curZ;
            _texCoords[6]= 0;
            _texCoords[7]= 1;
        }else if (_axis == 0){
            double curX = _counter * _planeSpacing -.5;
            /** Set coordinates for the plane.  These coordinates assume 
             * that the the image's origin is at the lower left and the planes 
             * are aligned accordingly
             */
            // lower left
            _quadCoords[0] = curX;
            _quadCoords[1] = -0.5;
            _quadCoords[2] = 0.5;
            _texCoords[0]= 0;
            _texCoords[1]= 0;
           
            
            // lower right
            _quadCoords[3] = curX;
            _quadCoords[4] = -0.5;
            _quadCoords[5] = -0.5;
            _texCoords[2]= 1;
            _texCoords[3]= 0;
          
            
            // upper right
            _quadCoords[6] = curX;
            _quadCoords[7] = 0.5;
            _quadCoords[8] = -0.5;
            _texCoords[4]= 1;
            _texCoords[5]= 1;
         
            
            // upper left
            _quadCoords[9] = curX;
            _quadCoords[10] = 0.5;
            _quadCoords[11] = 0.5;
            _texCoords[6]= 0;
            _texCoords[7]= 1;
            
            
        }else {
            if (_debugging) {
                _debug("chose none of them");
                
               }
        }
            
        
        _plane.setCoordinates(0, _quadCoords);
        _plane.setTextureCoordinates(0, 0,_texCoords); 
        _geometry = _plane; 
    }
    
    protected void _createModel()throws IllegalActionException {
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

    if (_debugging) {
        _debug("About to loadTexture");
    }
    
     TextureAttributes attributes = null;
     
    /*int arrayLength = _sSize*_tSize;
     double fraction = 1/255;
     
     if (_debugging) {
        _debug("arrayLength = " + arrayLength);
        _debug("fraction = " + fraction);
     }
     double[] pixelArray = new double[arrayLength];
     double[] alphaArray = new double[arrayLength]; */
     
   /*  try {
		_bufferedImage = (BufferedImage)ImageIO.read (new File(_fileRoot));
	} catch (IOException e) {
        System.err.println(e);
        _bufferedImage = null;
	}*/
    /* _alphaRaster = _bufferedImage.getAlphaRaster();
     if (_debugging){
        _debug("_bufferedImage = " + _bufferedImage); 
        _debug("Number of bands in _alphaRaster = " + _alphaRaster.getNumBands());   
     }
     
    _dataRaster = (WritableRaster)_bufferedImage.getData();
     _dataRaster.getPixels(0,0,_sSize,_tSize,pixelArray );
     
     if (_debugging){
        _debug("Number of bands in _dataRaster = " + _dataRaster.getNumBands());
     }
     
     for (int i=0; i < arrayLength; i ++){ 
        alphaArray[i] = 1 - pixelArray[i]*fraction;
     }
     
     _dataRaster.setSamples(0,0,_sSize,_tSize,4, alphaArray); */
     
    //if (_fileURL != null) {
    /*     MyTextureLoader loader;
         //String format = "LUMINANCE_ALPHA";
         loader = new MyTextureLoader(_bufferedImage,_viewScreen.getCanvas());*/
         if (_debugging) {
             _debug("Loaded texture");
         }
         MyTextureLoader loader;
    //     if (_token.getClass().toString() == "class ptolemy.data.AWTImageToken"){
         	loader = new MyTextureLoader(_image, _viewScreen.getCanvas());
      //   }else {
          //  loader = new MyTextureLoader(_fileRoot, _viewScreen.getCanvas());  
        // }
            
         /* Get the Loaded Texture */
         Texture loadedTexture = loader.getTexture();
         if (_debugging) {
             _debug("got texture");
             _debug("Texture format is = " + loadedTexture.getFormat());
         }
         
         
         
         
         
         if (loadedTexture != null) {
             attributes = new TextureAttributes();
             attributes.setTextureMode(TextureAttributes.MODULATE);
            
             _appearance.setTextureAttributes(attributes);

             _appearance.setTexture(loadedTexture);
         }  
     //}
     
   }
    

    /**Read in file. */
    protected void _readImage() throws IllegalActionException {
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
        
      _token = input.get(0);
      
     if (_token.getClass().toString() == "class ptolemy.data.AWTImageToken"){
      System.out.println("If returned true");
      ImageToken imageToken;
      imageToken = (ImageToken) _token;
      _image = imageToken.asAWTImage();
      System.out.println("token = " + _token.getType());
      System.out.println("token = " + _token.getClass().toString());
     }
     ImageToken imageToken;
     imageToken = (ImageToken) _token;
     _image = imageToken.asAWTImage();
     System.out.println("token = " + _token.getType());
     System.out.println("token = " + _token.getClass().toString());
      /* }/*else{
        StringToken stringToken;
        stringToken = (StringToken) _token;
        _fileRoot = stringToken.stringValue();
      }*/
    }
        
        
         
        
        
 

    
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

  
    /** ?????? */
    protected View _view; 
    
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
    
    private Shape3D _texturedImage;
    
    private ImagePlus _imagePlus;
    
    private String _fileRoot;

    private File _file;
    
    private FileImageInputStream _fileImageInputStream;
    
    private Texture2D _texture;  
    
    private Token _token;

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
