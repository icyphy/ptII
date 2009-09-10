package ptolemy.gui;

import java.util.*;
import java.io.*;
import javax.swing.*;
import java.awt.event.ActionListener;


/**
 An interface to allow certain methods in top to be overriden by a class
 set in the configuration.  The configuration attribute to do this is 
 _alternativeTopHandler.  The useXXX methods tell top whether to use the override
 defined in the implementing class.  

 @author Chad Berkley
 @version $Id: Top.java 55376 2009-08-05 21:12:17Z cxh $
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (berkley)
 @Pt.AcceptedRating Red (berkley)
 */
public interface TopHandler
{
  public boolean saveAs(Top top, JFileChooser fileDialog, File _file);
  
  public void open(Top top);
  
  public void pack(Top top, ActionListener fileMenuListener,  
    ActionListener helpMenuListener);
  
}
