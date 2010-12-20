package ptolemy.domains.jogl.kernel;

import javax.media.opengl.GL;

import ptolemy.kernel.util.IllegalActionException;


/** 
 * Interface GLTransform3D is implemented by JOGL 3D transformation classes
 * (Rotate3D, Translate3D) to apply 3D transformations to the scene.
 */

public interface GLTransform3D {


    /** Abstract transformation method is used to translate and rotate 3D objects.
     */
    public abstract void transform( GL object) throws IllegalActionException;




}
