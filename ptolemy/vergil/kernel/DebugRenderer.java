/* Debug renderer.

Copyright (c) 1999-2005 The Regents of the University of California.
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
package ptolemy.vergil.kernel;

import java.awt.Color;

import diva.canvas.FigureDecorator;
import diva.canvas.toolbox.BasicHighlighter;


//////////////////////////////////////////////////////////////////////////
//// DebugRenderer

/**
   Highlight objects in magenta, rather than red or yellow.

   @author Elaine Cheong
   @version $Id$
   @since Ptolemy II 2.1
   @Pt.ProposedRating Red (celaine)
   @Pt.AcceptedRating Red (celaine)
*/
public class DebugRenderer extends AnimationRenderer {
    /** Create a new selection renderer with the default prototype
     *  decorator.
     */
    public DebugRenderer() {
        _prototypeDecorator = new BasicHighlighter(Color.magenta, 4.0f);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new renderer with the given prototype decorator.
     *  @param decorator The prototype decorator.
     */
    public DebugRenderer(FigureDecorator decorator) {
        _prototypeDecorator = decorator;
    }
}
