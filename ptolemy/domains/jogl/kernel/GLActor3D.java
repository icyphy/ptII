package ptolemy.domains.jogl.kernel;


import javax.media.opengl.GL;
import ptolemy.kernel.util.IllegalActionException;

/** 
 * Interface GLActor3D is implemented by JOGL 3D object classes
 * (Line3D, Cube3D, Point3D) to draw 3D objects.
 */
 

public abstract interface GLActor3D {
    

    /** Abstract render method is used to render 3D objects.
     */

    public abstract void render( GL object) throws IllegalActionException;
    
   


}
