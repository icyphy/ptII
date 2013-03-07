/* An interface for objects storing a location.

 Copyright (c) 2002-2012 The Regents of the University of California.
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
package ptolemy.vergil.basic;

import ptolemy.kernel.util.RelativeLocation;

///////////////////////////////////////////////////////////////////
//// RelativeLocatable

/**
 An interface for objects that can be located visually relative to
 another object. This is a marker interface. Classes that implement
 this interface will be assigned by Vergil an attribute of type
 the {@link RelativeLocation}, which provides a location relative
 to another object.

 @author Edward A. Lee, Christian Motika, Miro Spoenemann
 @version $Id$
 @since Ptolemy II 9.0
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public interface RelativeLocatable {
}
