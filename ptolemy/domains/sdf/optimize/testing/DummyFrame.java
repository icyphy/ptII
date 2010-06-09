/* DummyFrame mimics a shared data object.
 * It is used for testing the OptimizingSDFDirector

 Copyright (c) 1997-2010 The Regents of the University of California.
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

/**
 * 
 */
package ptolemy.domains.sdf.optimize.testing;

/**
<h1>Class comments</h1>
A DummyFrame mimics a shared data object.
It is used for testing the OptimizingSDFDirector. The shared data object holds an
integer value. 
<p>
See {@link ptolemy.domains.sdf.optimize.OptimizingSDFDirector} for more information.
</p>
@see ptolemy.domains.sdf.optimize.OptimizingSDFDirector

@author Marc Geilen
@version $Id$
@since Ptolemy II 0.2
@Pt.ProposedRating Red (mgeilen)
@Pt.AcceptedRating Red ()
*/
public class DummyFrame {
    public int value;

    @Override
    public String toString() {
        return "Frame(" + Integer.toString(value) + ")";
    }
    
    public DummyFrame clone(){
        DummyFrame f = new DummyFrame();
        f.value = value;
        return f;
    }

}
