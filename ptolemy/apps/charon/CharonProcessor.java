/*
 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Red (hyzheng@eecs.berkeley.edu)
@AcceptedRating Red (hyzheng@eecs.berkeley.edu)
*/
package ptolemy.apps.charon;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.ct.lib.Integrator;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.IllegalActionException;

import java.io.Reader;
import java.io.Writer;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.LinkedList;
import java.util.ListIterator;

public class CharonProcessor {

  public CharonProcessor(String inputFileName, String outputFileName) {
    _inputFileName = inputFileName;
    _outputFileName = outputFileName;

    _makeST(_inputFileName);
  }

  public void process() throws IllegalActionException {

    System.out.println("parsing input file....");
    _parser();

    System.out.println("constructing output file....");
    _constructor();

    try {
      System.out.println("writing output file....");
      _writer();
    } catch (IOException e) {
      throw new IllegalActionException(e.getMessage());
    }

  }

  private void _constructor() throws IllegalActionException {
    if(agentsList.size() > 0) {
      Agent agent = (Agent) agentsList.get(0);
      topLevel = agent.constructor(new Workspace());
    } else {
      throw new IllegalActionException(" not a good Charon file! ");
    }
  }

  private void _writer() throws IOException {
/*    if (topLevel != null) {
      System.out.println(topLevel.getName());
      System.out.println(topLevel.entityList().size());
    }
*/
    try {
//      FileOutputStream fos = new FileOutputStream(_outputFileName);
      FileOutputStream fos = new FileOutputStream("test.xml");
      _writer = new OutputStreamWriter(fos);
      String resultXML = topLevel.exportMoML();
      _writer.write(resultXML);
      _writer.flush();
    } catch (IOException e) {

      System.err.println ("Error in IO operations: " + e.getMessage ());
      System.exit(1);
    }

  }

  private void _parser() throws IllegalActionException {
    Agent agent;
    String agentBlockString = "";
    String modeBlockString = "";

    // get the list of agents
    // the order of the list is subtle that
    // an element with a small index may use an element with a bigger index

    while (true) {
      agentBlockString = _readBlock("agent", "{", "}", _input);
//      System.out.println("    blockString size: " + agentBlockString.length());
      if (agentBlockString != "") {
        agent = new Agent(agentBlockString);
	agentsList.add(0, agent);
      } else {
        break;
      }
    }

    // reconstruct _input
    _makeST(_inputFileName);

    Agent mode;
    while (true) {
      modeBlockString = _readBlock("mode", "{", "}", _input);
//      System.out.println("    blockString size: " + modeBlockString.length());
      if (modeBlockString != "") {
        mode = new Agent(modeBlockString);
	modesList.add(0, mode);
      } else {
        break;
      }
    }

/*    ListIterator agents = agentsList.listIterator();
    while (agents.hasNext()) {
      agent = (Agent) agents.next();
      String agentModeName = agent.getName() + "TopMode";
      _makeST(_inputFileName);
      modeBlockString = _readBlock("mode " + agentModeName, "{", "}", _input);
      agent.addMode(modeBlockString);
    }
*/
  }

  /**
   *
   * /
  /* The blockName indicates the beginning of the block.
   * Since there may be several delimiters in the block body,
   * A counter is used to calculate the end of the block.
   * E.g. "{" and "}" are delimters and counter is initialized to 0.
   * When "{" is detected, counter is increased by 1; when "}" is detected,
   * counter is decreased by 1. When counter meets 0, the whole block is read.
   */
  protected String _readBlock(String blockName, String leftDelimiter, String rightDelimiter, StreamTokenizer input)
	throws IllegalActionException {

//    System.out.println("reading block: " + blockName);
    StreamTokenizer st = input;

    st.resetSyntax ();
    st.wordChars (' ', '~');
    st.whitespaceChars ('\n', '\n');

    String blockString = "";
    String line = "";
    boolean firstLine = true;
    _counter = 0;

    do {
      try {
	st.nextToken ();
      } catch (IOException e) {
	throw new IllegalActionException ("File error!");
      }

      switch (st.ttype) {
	case StreamTokenizer.TT_EOF:
          if (_counter != 0) throw new IllegalActionException ("Not a good Charon file!");
	  return blockString;

	case StreamTokenizer.TT_WORD:

	  line = st.sval;
	  // System.out.println("      ====> " + line);
	  // limitation that one line must not contain content of two blocks.


	  if (line.startsWith(blockName) && firstLine) {
//            System.out.println("  reading one line: " + line);
	    blockString += line + "\n";
	    break;
	  }

	  // since the first line does not contain the delimiters
	  if (blockString.length() != 0) firstLine = false;

	  if (!line.startsWith(blockName) && firstLine) {
	    break;
	  }

	  if (!firstLine) {
//            System.out.println("  reading one line: " + line);
	    blockString += line + "\n";
	    _leftDelimiterChecker(line, leftDelimiter);
	    _rightDelimiterChecker(line, rightDelimiter);
	  }

	  break;

	default:
	  throw new IllegalActionException ("Unexpected input character: " + (char) st.ttype);
      }
    } while (_counter != 0 || firstLine);


    return blockString;
  }

  private void _leftDelimiterChecker(String line, String delimiter) {
//    System.out.println("checking left delimiter: " + delimiter);
    int delimiterIndex = 0;
    int counter = 0;
    do {
      delimiterIndex = line.indexOf(delimiter, (delimiterIndex));
      if (delimiterIndex != -1) {
	_counter++;
	delimiterIndex++;
      }
    } while (delimiterIndex != -1 && delimiterIndex < line.length());
  }

  private void _rightDelimiterChecker(String line, String delimiter) {
//    System.out.println("checking right delimiter: " + delimiter);
    int delimiterIndex = 0;
    int counter = 0;
    do {
      delimiterIndex = line.indexOf(delimiter, (delimiterIndex));
      if (delimiterIndex != -1) {
	_counter--;
	delimiterIndex++;
      }
    } while (delimiterIndex != -1 && delimiterIndex < line.length());
  }

  private void _makeST (String fileName) {
    try {
      FileInputStream fis = new FileInputStream(fileName);
      _reader = new InputStreamReader(fis);
    } catch (IOException e) {
      System.err.println ("Error in IO operations: " + e.getMessage ());
      System.exit(1);
    }

    _input = new StreamTokenizer(_reader);

    _input.resetSyntax ();
    _input.wordChars (' ', '~');
    _input.whitespaceChars ('\n', '\n');
  }

  // may be used for modal model
  protected StreamTokenizer _input;
  public static LinkedList agentsList = new LinkedList();
  public static LinkedList modesList = new LinkedList();
  protected TypedCompositeActor topLevel;
  private int _counter =0;
  private Reader _reader;
  private Writer _writer;
  private String _inputFileName;
  private String _outputFileName;
}