package ptolemy.domains.gr.lib;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;
import ptolemy.actor.lib.gui.Display;
import ptolemy.actor.gui.Placeable;
import ptolemy.actor.lib.*;
import ptolemy.domains.dt.kernel.DTDebug;

import javax.swing.JTextArea;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import java.util.Enumeration;

import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import com.sun.j3d.utils.geometry.*;

import com.sun.j3d.loaders.objectfile.ObjectFile;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.Scene;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import com.sun.j3d.utils.universe.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.io.*;
import com.sun.j3d.utils.behaviors.mouse.*;

public class Loader3D extends GRShadedShape {

    public Loader3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);
        filename = new Parameter(this, "filename", new StringToken("chopper.obj"));
    }
    
    public Parameter filename;
    
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Loader3D newobj = (Loader3D)super.clone(workspace);
        return newobj;
    }
    
    

   
   public Node getNodeObject() {
       return (Node) obj.getSceneGroup();
   }

    protected void _createModel() throws IllegalActionException {
        String fileName = (String) ((StringToken) filename.getToken()).stringValue();
        
        //Appearance ap = new Appearance();
        //ap.setColoringAttributes(new ColoringAttributes(_color.x,_color.y,_color.z,ColoringAttributes.SHADE_GOURAUD));
        
    	int flags = ObjectFile.RESIZE;
    	//if (!noTriangulate) flags |= ObjectFile.TRIANGULATE;
	    //if (!noStripify) flags |= ObjectFile.STRIPIFY;
    	ObjectFile f = new ObjectFile(flags,(float)(creaseAngle * Math.PI / 180.0));
    	Scene s = null;
    	try {
	      s = f.load(fileName);
    	}
	    catch (FileNotFoundException e) {
    	  System.err.println(e);
    	  throw new IllegalActionException("File not found!");
	    }
    	catch (ParsingErrorException e) {
	      System.err.println(e);
    	  throw new IllegalActionException("File is not a valid 3D OBJ file");
	    }
    	catch (IncorrectFormatException e) {
	      System.err.println(e);
          throw new IllegalActionException("File is not a valid 3D OBJ file");
	    }
    	obj = s;
   }

    private boolean spin = false;
    private boolean noTriangulate = false;
    private boolean noStripify = false;
    private double creaseAngle = 60.0;
    private Scene obj;
}
