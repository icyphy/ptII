/*

 Copyright (c) 1997-2005 The Regents of the University of California.
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

package ptolemy.apps.naomi.connector;

import ptolemy.actor.gui.MoMLApplication;
import ptolemy.kernel.util.KernelException;
import ptolemy.util.MessageHandler;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class PtolemyConnector extends MoMLApplication {

    /**
     * @param basePath
     * @param args
     * @throws Exception
     */
    public PtolemyConnector(String basePath, String[] args) throws Exception {
        super(basePath, args);
    }

    /**
     * @param args
     * @throws Exception
     */
    public PtolemyConnector(String[] args) throws Exception {
        super(args);
    }

    public static void main(String[] args) {
        try {
            new PtolemyConnector(args);
        } catch (Throwable t) {
            MessageHandler.error("Command failed", t);
            System.err.print(KernelException.stackTraceToString(t));
            System.exit(1);
        }
    }

    protected void _parseArgs(final String[] args) throws Exception {
        _commandTemplate = "java " + getClass().getName() + " [ options ]";
        System.out.println(_usage());
    }

    protected String _usage() {
        return _configurationUsage(_commandTemplate, _commandOptions,
                new String[] {});
    }
}
