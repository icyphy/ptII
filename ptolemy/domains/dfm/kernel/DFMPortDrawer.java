/* Drawing tool to draw various outport on the port of the actor.
 
 Copyright (c) 1998 The Regents of the University of California.
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
 
package ptolemy.domains.dfm.kernel;

import ptolemy.domains.dfm.demo.*; 
import java.awt.*;

//////////////////////////////////////////////////////////////////////////
//// DFMPortDrawer
/**
  The drawing tools to draw various output  state of the actor.
*/
public class DFMPortDrawer {
     public DFMPortDrawer(int x, int y, facetImage facet, boolean drawValue){
          _x = x;
          _y = y;
          _facet = facet;
          _drawValue = drawValue;
     }

     public void draw(String tag, String val){
          _tag = tag;
          _val = val;
          _facet.repaint();
     }

     public void clearToken(){
          _tag = null;
          _val = null;
     }
 
     public void drawToken(Graphics graphics){
          Color color;

          if (_tag == null){ return; }
          if (_tag.equals("New")){
               color = Color.cyan;
          } else if (_tag.equals("Annotate")){
               color = Color.red;
          } else if (_tag.equals("PreviousResultValid")) {
               color = Color.blue;
          } else { 
               color = Color.white;
          }

          graphics.setColor(color);
          graphics.fillOval(_x, _y, 8, 8);
          if (((_tag.equals("New")) || (_tag.equals("Annotate"))) && (_drawValue)){
              graphics.setColor(Color.black);
              graphics.drawString(_val, _x+10, _y); 
          }

     } 

     private facetImage _facet;    
     private String _val; 
     private String _tag; 
     private int _x, _y;
     private boolean _drawValue = true;
}
