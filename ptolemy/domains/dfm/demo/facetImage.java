/* Facet for DFMSimple.
 
 Copyright (c) 1998-1999 The Regents of the University of California.
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
 

package ptolemy.domains.dfm.demo;

import ptolemy.domains.dfm.kernel.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.applet.*;


//////////////////////////////////////////////////////////////////////////
//// facetImage 
/** 
 Facet for DFMSimple.
@author  William Wu (wbwu@eecs.berkeley.edu) 
@version $id$
*/

public class facetImage extends Panel implements MouseListener{

       Image facet;

       public facetImage(String imagefile, int width, int height){

             Toolkit toolkit = Toolkit.getDefaultToolkit();
             facet = toolkit.getImage(imagefile);
//             facet = toolkit.getImage("/home/wbwu/work/ptolemy/dfm/demo/facet1.gif");
             this.addMouseListener(this);
             this.setSize(new Dimension(width, height));
             _width = width;
             _height = height;
       }

       public facetImage(Image im, int width, int height){
             facet = im;
             this.addMouseListener(this);
             this.setSize(new Dimension(width, height));
             _width = width;
             _height = height;
       }
    
       public void mouseClicked(MouseEvent e){

             int x = e.getX();
             int y = e.getY();
System.out.println("x = "+x+" y = "+y);
       }

       public void mouseEntered(MouseEvent e){
            return;
       }

       public void mouseExited(MouseEvent e){
            return;
       }

       public void mousePressed(MouseEvent e){
            return;
       }

       public void mouseReleased(MouseEvent e){
            return;
       }

       public void destroy() { facet.flush();}

       public void update(Graphics g){ paint(g); }

       public void setActorDrawers(DFMActorDrawer [] drawers){
            _actordrawers = drawers;
       }

       public void setPortDrawers(DFMPortDrawer [] drawers){
            _portdrawers = drawers;
       }

       public Dimension getPreferredSize(){
            return new Dimension(_width, _height);
       }

       public void paint(Graphics g){

            g.drawImage(facet, 0, 0, this);

            if (_actordrawers!= null){
                for (int i = 0; i<_actordrawers.length;i++){
                    _actordrawers[i].drawActor(g);
                }
            }
            if (_portdrawers!= null){
                for (int i = 0; i<_portdrawers.length;i++){
                    _portdrawers[i].drawToken(g);
                }
            }
       }

       public void clearPortDrawerTokens(){
           for (int i = 0; i<_portdrawers.length;i++){
               _portdrawers[i].clearToken();
           }
            repaint();
       }
 
       private int _width;
       private int _height;
       private boolean _lock;
       private DFMActorDrawer [] _actordrawers;
       private DFMPortDrawer [] _portdrawers;
   }



