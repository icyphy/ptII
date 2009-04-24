package ptolemy.domains.gro.lib;

import javax.media.opengl.GL;

import ptolemy.actor.TypedIOPort;
import ptolemy.domains.gro.kernel.GROActor;
import ptolemy.domains.gro.kernel.GRODirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import java.lang.Math;

public class Ellipse3D extends GROActor {

    public Ellipse3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
    }
public TypedIOPort GLPipelineObject;
    
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called fire()");
        }
    GL gl = ((GRODirector) getDirector()).getGL();
    gl.glColor3f(1,1,1);
    double x,y,z;
    int t;
    gl.glBegin(GL.GL_POINTS);
    gl.glColor3f(0.3f, 0.7f, 0.3f); 
    for(t = 0; t <= 360; t +=1)
    {
      x = 5*Math.sin(t);
      y = 4*Math.cos(t);
      z = 0;
      gl.glVertex3f((float) x,(float) y, (float) z);
   }
   gl.glEnd();
    }

}
