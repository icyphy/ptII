/* A trivial example of how to test a model execution.

 Copyright (c) 2019 The Regents of the University of California; SupportSquare NV.
 
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
package org.ptolemy.core.test.command;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.ptolemy.core.test.AllTests;

/**
 * A basic implementation of a Karaf shell command to launch Junit tests for ptII model definition and execution.
 *
 * @author ErwinDL
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Red (ErwinDL)
 * @Pt.AcceptedRating Red (ErwinDL)
 */
@Service
@Command(scope = "ptII", name = "run-unittests", description = "Run unit tests on model definitions and executions")
public class RunTestsCommand implements Action {

  @Override
  public Object execute() throws Exception {
    junit.textui.TestRunner.run(AllTests.suite());
    return null;
  }

}
