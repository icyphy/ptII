/* Abstract base class for version specifications.

   Copyright (c) 2014 The Regents of the University of California; iSencia Belgium NV.
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
package org.ptolemy.commons;

import java.util.Arrays;

/**
 * Abstract base class for version specifications.
 *
 * <p> The most important thing for a version specification is that it
 * can be compared to another one.</p>
 *
 * <p> Versions can be specified in two formats : a simple 3-digit
 * spec (+ qualifiers) or a code/tag (not yet ;-) ).  </p>
 *
 * @author ErwinDL
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Yellow (ErwinDL)
 * @Pt.AcceptedRating Yellow (ErwinDL)
 */
public abstract class VersionSpecification implements Comparable<VersionSpecification> {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Parses the given version String, using '.' , '-' , '_' as potential delimiters.
     *
     * <p> For 3-digit version spec, currently the only supported
     * format, the first 3 version ids are mandatory and should be
     * integer numbers.  Extra (optional) trailing ids can be
     * textual. Spaces are not allowed in a version string.
     * E.g. "1.2_3-hello.world" is a valid version identifier.
     * </p>
     *
     * @param version The version
     * @return the version specification based on the passed string representation
     * @exception IllegalArgumentException when the overall string format is not valid
     * @exception NumberFormatException if one of the first 3 segments is not an integer
     */
    public static VersionSpecification parse(String version) {
        VersionSpecification versionSpec = null;

        String[] versionIds = version.split("[\\.\\-_]");

        if (versionIds.length < 3) {
            throw new IllegalArgumentException("Version must consist of minimally 3 digits <" + version + ">");
        } else {
            if (version.indexOf(' ') != -1) {
                throw new IllegalArgumentException("3-digit Version can not contain spaces <" + version + ">");
            }

            int major = Integer.parseInt(versionIds[0]);
            int minor = Integer.parseInt(versionIds[1]);
            int micro = Integer.parseInt(versionIds[2]);
            if (versionIds.length == 3) {
                versionSpec = new ThreeDigitVersionSpecification(major, minor, micro);
            } else {
                versionSpec = new ThreeDigitVersionSpecification(major, minor, micro,
                        Arrays.copyOfRange(versionIds, 3, versionIds.length));
            }
            versionSpec._versionString = version;
        }
        return versionSpec;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected ariables                ////

    /** The version string. */
    protected String _versionString;
}
