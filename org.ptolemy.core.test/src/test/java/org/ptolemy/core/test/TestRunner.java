/* One line description of the class.

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
package org.ptolemy.core.test;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

/**
 *    Describe your class here, in complete sentences.
 *    What does it do?  What is its intended use?
 *
 * @author erwinDL
 * @version $Id$
 * @since Ptolemy II 10.1
 * @Pt.ProposedRating Yellow (erwinDL)
 * @Pt.AcceptedRating Red (reviewmoderator)
 */
public class TestRunner implements CommandProvider {

  /**
   * Show help text indicating the supported commands for the equinox console
   */
	public String getHelp() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("\n---Ptolemy core test---\n");
		buffer.append("\trunCoreTests\n");
		return buffer.toString();
	}

	/**
	 * Run core tests.
	 * Remark that the leading underscore is required for eclipse's CommandProvider mechanism to work!
	 *
	 * @param ci the equinox console's command interpreter from which (optional) arguments can be obtained for the command etc
	 */
	public void _runCoreTests(CommandInterpreter ci) {
		junit.textui.TestRunner.run(AllTests.suite());
	}
}
