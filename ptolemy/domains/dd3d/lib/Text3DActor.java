package ptolemy.domains.dd3d.lib;

import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.domains.dt.kernel.DTDebug;
import ptolemy.domains.dd3d.kernel.*;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.GraphicsConfiguration;
import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.universe.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.awt.Font;


public class Text3DActor extends Shaded3DActor {

    public Text3DActor(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);
        text = new Parameter(this, "text", new StringToken("Ptolemy"));
    }
    
    public Parameter text;
    
    protected void _createModel() throws IllegalActionException {
        super._createModel();
        Font3D font3D = new Font3D(new Font("Helvetica", Font.PLAIN, 1),
                                    new FontExtrusion());
        Text3D textGeom = new Text3D(font3D, new String(_getText()));
        textGeom.setAlignment(Text3D.ALIGN_CENTER);
        obj = new Shape3D();
        obj.setGeometry(textGeom);
        obj.setAppearance(_appearance);
    }

    
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Text3DActor newobj = (Text3DActor) super.clone(workspace);
        return newobj;
    }
    
    
    public Node getNodeObject() {
        return (Node) obj;
    }
   

    private String _getText() throws IllegalActionException {
        return ((StringToken) text.getToken()).stringValue();
    }

    private Shape3D obj;
}
