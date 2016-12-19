/*
 * Copyright (c) 2010 The Regents of the University of California.
 * All rights reserved.
 *
 * '$Author$'
 * '$Date$' 
 * '$Revision$'
 * 
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies
 * of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 * CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 * ENHANCEMENTS, OR MODIFICATIONS.
 *
 */

package ptolemy.actor.lib.r;

import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;

/**
 * A simple buffering console that is used to cache the output from an R session
 * and then can be used to return the console output as a string.
 * 
 * @author Matt Jones
 */
public class RConsole implements RMainLoopCallbacks {
	/**
	 * A buffer which caches the output of the R session standard output.
	 */
	private StringBuffer consoleText = null;

	/**
	 * Construct the R Console class and initialize the buffer containing the
	 * text.
	 */
	public RConsole() {
		super();
		consoleText = new StringBuffer();
	}

	public void clear() {
		consoleText = new StringBuffer();
	}

	/**
	 * After an R session has ended, get a String representation of the output
	 * of the R session.
	 * 
	 * @return String containing the text of the R session output
	 */
	public String getConsoleOutput() {
		return consoleText.toString();
	}

	/**
	 * Callback that is called when text is available from the R Engine and
	 * should be written to the console.
	 */
	public void rWriteConsole(Rengine re, String text, int oType) {
		consoleText.append(text);
	}

	//
	// The remaining callback methods are not used, but need to have
	// implementations to satisfy the interface definition.
	//
	public void rBusy(Rengine re, int which) {
		System.out.println("rBusy(" + which + ")");
	}

	public String rReadConsole(Rengine re, String prompt, int addToHistory) {
		return null;
	}

	public void rShowMessage(Rengine re, String message) {
		System.out.println("rShowMessage \"" + message + "\"");
	}

	public String rChooseFile(Rengine re, int newFile) {
		return "";
	}

	public void rFlushConsole(Rengine re) {
	}

	public void rLoadHistory(Rengine re, String filename) {
	}

	public void rSaveHistory(Rengine re, String filename) {
	}
}
