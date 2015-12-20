/* Get the classes for different types of model elements.

   Copyright (c) 2015 The Regents of the University of California; iSencia Belgium NV.
   All rights reserved.

   Permission is hereby granted, without written agreement and without
   license or royalty fees, to use, copy, modify, and distribute this
   software and its documentation for any purpose, provided that the above
   copyright notice and the following two paragraphs appear in all copies
   of this software.

   IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA LIABLE TO ANY PARTY
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

package org.ptolemy.classloading;

import org.ptolemy.commons.VersionSpecification;

import ptolemy.kernel.util.NamedObj;

/**
 * Get the classes for different types of model elements.
 * <p>
 * Most important cases are actors and directors.
 * But also dedicated port or parameter classes can be offered via implementations of this interface.
 * </p>
 *
 * @author ErwinDL
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Yellow (ErwinDL)
 * @Pt.AcceptedRating Yellow (ErwinDL)
 */
public interface ModelElementClassProvider {

    /**
     * Return the requested class for the requested version (if specified).
     * If this provider doesn't have this class available, it should throw a <code>ClassNotFoundException</code>.
     * (Optionally, it could also just return null, for those dvp-ers who don't like exceptions. ;-) )
     *
     * @param className typically a fully qualified Java class name. Mandatory non-null.
     * @param versionSpec optional constraint on desired version for the class that must be provided. If null, no version constraint is imposed.
     * @return the concrete class of the <code>NamedObj</code> matching the given className.
     * @exception ClassNotFoundException if this provider can not provide the requested class for the requested version (if specified)
     *
     * @see VersionSpecification
     */
    Class<? extends NamedObj> getClass(String className, VersionSpecification versionSpec) throws ClassNotFoundException;
}
