package ptolemy.domains.gro.lib;

import javax.media.opengl.GL;

import ptolemy.actor.TypedIOPort;
import ptolemy.domains.gro.kernel.GROActor;
import ptolemy.domains.gro.kernel.GRODirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


public class Line3D extends GROActor {

    public Line3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        GLPipelineObject = new TypedIOPort(this, "GLPipelineObject");
        GLPipelineObject.setOutput(true);
        //GLPipelineObject.setTypeEquals(SceneGraphToken.TYPE);
        GLPipelineObject.setMultiport(true);
    }
    public TypedIOPort GLPipelineObject;
    
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called fire()");
        }
        GL gl = ((GRODirector) getDirector()).getGL();
        gl.glBegin(GL.GL_LINES);
        gl.glColor3f(0.3f, 0.7f, 0.3f); 
        gl.glVertex3f(100.0f, 100.0f, 0.0f); // origin of the line
        gl.glVertex3f(200.0f, 140.0f, 5.0f); // ending point of the line
        gl.glEnd( );
        
    }

}
