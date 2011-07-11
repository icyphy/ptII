/* Determine the bit width of the JVM.

 Copyright (c) 2010 The Regents of the University of California.
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

package ptolemy.util;

///////////////////////////////////////////////////////////////////
//// 32Or64bitJVM

/** Determine the bit width (32 or 64) of the JVM.
 *  @author Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red
 */
public class JVMBitWidth {
    /** Return true if this is a 32bit JVM.
     *  @return true if this is a 32bit JVM.
     */
    public static boolean is32Bit() {
        String dataModelProperty = StringUtilities
                .getProperty("sun.arch.data.model");
        // FIXME: it is difficult to detect if we are under a
        // 64bit JVM.  See
        // http://forums.sun.com/thread.jspa?threadID=5306174
        if (dataModelProperty.indexOf("64") != -1) {
            return false;
        } else {
            String javaVmNameProperty = StringUtilities
                    .getProperty("java.vm.name");
            if (javaVmNameProperty.indexOf("64") != -1) {
                return false;
            }
        }
        return true;
    }
}
