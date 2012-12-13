/**
 * Copyright (c) 2012-2013,
 * @author Mana Mirzaei [manmi478@student.liu.se],
 * Programming Environments Laboratory (PELAB),
 * Department of Computer and getInformation Science (IDA),
 * Linkoping University (LiU).
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
 * * Neither the name of Authors nor the name of Linkopings University nor
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

package ptolemy.domains.openmodelica.lib.omc;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class OMCLogger {

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    public String _loggerName = "ptLogger";
    public Logger _ptLogger = Logger.getLogger("ptLogger");

    public OMCLogger() {
        OMCProxy.os = OMCProxy.getOs();
        try {
            switch (OMCProxy.os) {
            case UNIX:
                fileHandler = new FileHandler(filePath + "/omcLog.txt");
                break;
            case MAC:
                fileHandler = new FileHandler(filePath + "/omcLog.txt");
                break;
            case WINDOWS:
                fileHandler = new FileHandler(filePath + "omcLog.txt");
                break;
            }

            fileHandler.setFormatter(new Formatter() {
                public String format(LogRecord rec) {
                    StringBuffer buf = new StringBuffer(1000);
                    buf.append(new java.util.Date());
                    buf.append(' ');
                    buf.append(rec.getLevel());
                    buf.append(' ');
                    buf.append(formatMessage(rec));
                    buf.append('\n');
                    return buf.toString();
                }
            });
            _ptLogger.addHandler(fileHandler);
        } catch (SecurityException e) {
            _ptLogger.severe("Security error related to the file handler!");
        } catch (IOException e) {
            _ptLogger.severe("Unable to create file handler!");
        }
    }

    public void getInfo(String msg) {

        _ptLogger.info(msg);
    }

    public void getWarning(String msg) {

        _ptLogger.warning(msg);
    }

    public void getSever(String msg) {

        _ptLogger.severe(msg);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    FileHandler fileHandler = null;
    String filePath = System.getProperty("java.io.tmpdir");
}
