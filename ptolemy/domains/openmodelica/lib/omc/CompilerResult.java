/* Fetch the results and errors of calling sendExpression() to OpenModelica Compiler(OMC) server.
 * 
 * Copyright (c) 2012-2013,
 * Programming Environment Laboratory (PELAB),
 * Department of Computer and getInformation Science (IDA),
 * Linkoping University (LiU).
 *
 * All rights reserved.
 *
 * (The new BSD license, see also
 * http://www.opensource.org/licenses/bsd-license.php)
 *
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
 * * Neither the name of Linkopings universitet nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
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

package ptolemy.domains.openmodelica.lib.omc;

/**
  It gets the first and multiple result from calling sendExpression("command") to OpenModelica Compiler(OMC).
  It also fetches the error resulted from calling sendExpression("command") to OMC as well as trimming the result. 
  
  @author Andreas Remar
  @version $Id$
  @since Ptolemy II 9.1
  @Pt.ProposedRating Red (cxh)
  @Pt.AcceptedRating Red (cxh)
 */
public class CompilerResult implements ICompilerResult {
    /** Construct the compiler result with the result and error of sendExpression("command") to OpenModelica Compiler(OMC).
     * @param result The result of calling sendExpression("command") to OMC.
     * @param error The error resulted from calling sendExpression("command") to OMC.
     */
    public CompilerResult(String[] result, String error) {
        this._result = result;
        this._error = error;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Fetch the error resulted from calling sendExpression("command") to OpenModelica Compiler(OMC).
     * @return The error resulted from calling sendExpression("command") to OMC.
     */
    public String getError() {
        return _error;
    }

    /**
     * Fetch the first result of calling sendExpression("command") to OpenModelica Compiler(OMC).
     * @return The first result of calling sendExpression("command") to OMC.
     */
    public String getFirstResult() {
        return _result[0];
    }

    /**
     * Fetch multiple results of calling sendExpression("command") to OpenModelica Compiler(OMC).
     * @return The multiple result of calling sendExpression("command") to OMC.
     */
    public String[] getResult() {
        return _result;
    }

    /** Make the compiler result with the result and error of sendExpression("command") to OpenModelica Compiler(OMC).
     * @param result The result of calling sendExpression("command") to OMC.
     * @param error The error resulted from calling sendExpression("command") to OMC.
     * @return The compiler result along with errors if there is any.  
     */
    public static CompilerResult makeResult(String[] result, String error) {
        return new CompilerResult(result, error);
    }

    /**
     * Trim the first compiler result.
     */
    public void trimFirstResult() {
        _result[0] = _result[0].trim();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private String _error;
    private String[] _result;
}
