package ptolemy.domains.dd3d.lib;

import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;
import ptolemy.actor.lib.gui.Display;
import ptolemy.actor.gui.Placeable;
import ptolemy.actor.lib.*;
import ptolemy.domains.dt.kernel.DTDebug;

import java.awt.Container;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JTextArea;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import java.util.Enumeration;


import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.GraphicsConfiguration;
import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.universe.*;
import javax.media.j3d.*;
import javax.vecmath.*;


public class BoxActor extends Shaded3DActor {

    public BoxActor(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        xLength = new Parameter(this, "xLength", new DoubleToken(0.5));
        yHeight = new Parameter(this, "yHeight", new DoubleToken(0.5));
        zWidth = new Parameter(this, "zWidth", new DoubleToken(0.5));
    }
    
    public Parameter xLength;
    public Parameter yHeight;
    public Parameter zWidth;
  
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        BoxActor newobj = (BoxActor)super.clone(workspace);
        return newobj;
    }
    
    protected void _createModel() throws IllegalActionException {
        super._createModel();
        obj = new Box((float)_getLength(),(float) _getWidth(),(float) _getHeight(),Box.GENERATE_NORMALS,_appearance);
    }
    
    public Node getNodeObject() {
        return (Node) obj;
    }
    
    private double _getLength() throws IllegalActionException {
        return ((DoubleToken) xLength.getToken()).doubleValue();
    }
    private double _getWidth() throws IllegalActionException {
        return ((DoubleToken) yHeight.getToken()).doubleValue();
    }
    private double _getHeight() throws IllegalActionException  {
        return ((DoubleToken) zWidth.getToken()).doubleValue();
    }    
    
    private Box obj;
}
