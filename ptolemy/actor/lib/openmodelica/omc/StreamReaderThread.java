/**One line description of the class.
 * This file is part of Modelica Development Tooling (MDT).
 * The Modelica Development Tooling (MDT) software is 
 * distributed under the conditions specified below.
 *
 * Copyright (c) 2005-2007,
 * The MDT Team: 
 * @author Adrian Pop [adrpo@ida.liu.se], 
 * @author Elmir Jagudin, 
 * @author Andreas Remar, 
 * Programming Environments Laboratory (PELAB),
 * Department of Computer and Information Science (IDA),
 * Linköping University (LiU).
 * 
 * All rights reserved.
 *
 * (The new BSD license, see also 
 *  http://www.opensource.org/licenses/bsd-license.php)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * * Neither the name of Authors nor the name of Linköpings University nor 
 *   the names of its contributors may be used to endorse or promote products 
 *   derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ptolemy.actor.lib.openmodelica.omc;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
Describe your class here, in complete sentences.
What does it do?  What is its intended use?

@author yourname
@version $Id: JavaTemplate.java 57040 2010-01-27 20:52:32Z cxh $
@see classname (refer to relevant classes, but not the base class)
@since Ptolemy II x.x
@Pt.ProposedRating Red (yourname)
@Pt.AcceptedRating Red (reviewmoderator)
*/

public class StreamReaderThread extends Thread {

    /** Create an instance with ... (describe the properties of the
     *  instance). Use the imperative case here.
     *  @param parameterName Description of the parameter.
     *  @exception ExceptionClass If ... (describe what
     *   causes the exception to be thrown).
     */
    InputStreamReader mIn;
    PrintStream mOut;

    public StreamReaderThread(InputStream in, PrintStream out) {
        mOut = out;
        mIn = new InputStreamReader(in);
    }

    public void run() {
        int ch;
        try {
            while (-1 != (ch = mIn.read())) {
                mOut.append((char) ch);
                mOut.flush();
            }
        } catch (Exception e) {
            mOut.append("\nRead error:" + e.getMessage());
        }
    }
}
