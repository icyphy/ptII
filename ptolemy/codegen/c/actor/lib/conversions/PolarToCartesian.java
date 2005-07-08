/*
 @Copyright (c) 2005 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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
/*
 * Created on 2005¦~4¤ë19¤é
 *
 */
package ptolemy.codegen.c.actor.lib.conversions;

import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.kernel.CCodeGeneratorHelper;

/**
 * @author Jackie
 * @version $Id$
 */
public class PolarToCartesian extends CCodeGeneratorHelper {
    /**
     * Constructor method for the PolarToCartesian helper
     * @param actor the associated actor
     */
    public PolarToCartesian(ptolemy.actor.lib.conversions.PolarToCartesian actor) {
        super(actor);
    }

    /** Get the files needed by the code generated for the
     *  PolarToCartesian actor.
     *  @return A set of strings that are names of the files
     *   needed by the code generated for the PolarToCartesian actor.
     */
    public Set getIncludingFiles() {
        Set files = new HashSet();
        files.add("\"math.h\"");
        return files;
    }
}
