/* Code generator helper class associated with the GIOTTODirector class.

 Copyright (c) 2005-2006 The Regents of the University of California.
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
package ptolemy.codegen.c.domains.giotto.kernel;

import ptolemy.codegen.kernel.Director;

// this is a copy of the SDF constructor renamed for giotto.. it may be inappropriate..
//I'm not sure at the moment
//@author Ye Zhou, Gang Zhou
//@version $Id: SDFDirector.java 50300 2008-08-07 01:25:05Z cxh $
//@since Ptolemy II 6.0
//@Pt.ProposedRating Yellow (zgang)
//@Pt.AcceptedRating Red (eal)

public class GIOTTODirector extends Director {

   /** Construct the code generator helper associated with the given
    *  GIOTTODirector.
    *  @param giottoDirector The associated
    *  ptolemy.domains.sdf.kernel.SDFDirector
    */    
   public GIOTTODirector(/*ptolemy.domains.giotto.kernel.GIOTTODirector giottoDirector*/) {
       super(/*giottoDirector*/null);
   }
}