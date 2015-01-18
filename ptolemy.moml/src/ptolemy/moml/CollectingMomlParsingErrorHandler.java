/* Utility to collect all parsing errors during the parsing of a moml.

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
package ptolemy.moml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.ErrorHandler;

/**
 * Utility to collect all parsing errors during the parsing of a moml.

   @author erwinDL
   @version $Id$
   @since Ptolemy II 10.1
   @Pt.ProposedRating Red (erwinDL)
   @Pt.AcceptedRating Red (reviewmoderator)
 */
public class CollectingMomlParsingErrorHandler implements ErrorHandler, Iterable<CollectingMomlParsingErrorHandler.ErrorItem> {

  /**
   * A plain container for error info that can be stored during parsing.
  
     @author erwinDL
   */
  public static class ErrorItem {
    public String element;
    public NamedObj context;
    public Throwable exception;

    public ErrorItem(String element, NamedObj context, Throwable exception) {
      this.element = element;
      this.context = context;
      this.exception = exception;
    }
  }

  public void enableErrorSkipping(boolean enable) {
  }

  public int handleError(String element, NamedObj context, Throwable exception) {
    _errorItems.add(new ErrorItem(element, context, exception));
    return CONTINUE;
  }

  public boolean hasErrors() {
    return !_errorItems.isEmpty();
  }

  public Iterator<ErrorItem> iterator() {
    return _errorItems.iterator();
  }

  // private stuff

  private List<ErrorItem> _errorItems = new ArrayList<ErrorItem>();
}
