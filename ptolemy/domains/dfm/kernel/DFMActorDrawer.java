/* Drawing tool to draw various stage of the actor.
 
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
//// DFMActorDrawer
/**
  The drawing tools to draw various state of the actor.
*/

public class DFMActorDrawer {

      public DFMActorDrawer(int ulx, int uly, int lrx, int lry, facetImage facet){
           _ulx = ulx;
           _uly = uly;
           _lrx = lrx;
           _lry = lry;
           _wid = _lrx - _ulx;
           _len = _lry - _uly;
           _facet = facet;
      }

      public void drawActor(Graphics graphics){
           Color drawColor;
           int state = _state; 
           if (state == 0){
               drawColor = Color.red; 
           } else if (state == 1){
               drawColor = Color.yellow; 
           } else if (state == 2){
               drawColor = Color.blue; 
           } else {
               drawColor = Color.green;
           }
           Color savec = graphics.getColor();
           graphics.setColor(drawColor);
           graphics.drawRect(_ulx+5, _uly+5, _wid-10, _len-10); 
           graphics.setColor(savec);
      }

      public void draw(int state){
           _state = state;
           _facet.repaint();
      }

      private int _state;
      private int _ulx, _uly;
      private int _lrx, _lry;
      private int _len, _wid;
      private facetImage _facet;
}
