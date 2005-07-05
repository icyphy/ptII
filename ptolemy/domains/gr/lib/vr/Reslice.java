/* An actor that reads an array of images.   */
 
  
package ptolemy.domains.gr.lib.vr;


import java.awt.Image;
import java.lang.String;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;


import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ColorProcessor;
import ij.plugin.Slicer;





import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.CompositeEntity;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.domains.sdf.lib.SDFTransformer;
import ptolemy.data.AWTImageToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.IntToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.actor.parameters.FilePortParameter;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.data.expr.Parameter;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;

//////////////////////////////////////////////////////////////////////////
////Slicer
/**
 An actor that reads an array of images. 

@see ptolemy.actor.lib.medicalimaging

@author T. Crawford
@version 
@since 
@Pt.ProposedRating Red
@Pt.AcceptedRating Red

*/public class Reslice extends TypedAtomicActor{
            /**Construct an actor with the given container and name.
         * @param container The container
         * @param name The name of this actor
         * @exception IllegalActionException If the actor cannot be contained
         *   by the proposed container.
         * @exception NameDuplicationException If the container already has an
         *   actor with this name.
         */
    
        public Reslice(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
         super(container, name);
        
         input = new TypedIOPort(this, "input", true, false);
         input.setTypeEquals(BaseType.OBJECT);
         
         output = new TypedIOPort(this, "output", false, true);
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
    
    //public FilePortParameter input;
    public TypedIOPort input;
    public TypedIOPort output;
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
        ImagePlus  imagePlus = (ImagePlus)objectToken.getValue(); 
        Slicer slicer = new Slicer();
        _imagePlus = slicer.reslice(imagePlus);
        output.broadcast(new ObjectToken(_imagePlus));
    }
    
    
    public void initialize() throws IllegalActionException
    {
    
      _xResolution = ((IntToken)xResolution.getToken()).intValue();
      _yResolution = ((IntToken)yResolution.getToken()).intValue();
      _stackSize = ((IntToken)stackSize.getToken()).intValue();
      
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    
    //Image that is readin
    private ImagePlus _imagePlus;  
    
    private int _stackSize;
    
    private int _xResolution;
    
    private int _yResolution;
 }
