/* A SDF actor that gets stock quotes from Yahoo and write them to
   a Java Space.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Red (yuhong@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.actor.lib.jspaces;

import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.Token;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.domains.sdf.kernel.SDFAtomicActor;
import ptolemy.domains.sdf.kernel.SDFScheduler;
import ptolemy.domains.sdf.kernel.SDFIOPort;

import java.util.StringTokenizer;

//////////////////////////////////////////////////////////////////////////
//// StockServer
/**
A SDF actor that gets stock quotes from Yahoo and write them to
a Java Space. The stock tickers are stored in the parameter
<code>tickers</code>. Each quote is handled by a StockQuote actor
and a Publisher. There will be as many StockQuote/Publisher pairs
as the number of tickers. This is a SDF composite actor so all the quotes
are queried at the same rate, and all the actors run in the same thread.

@author Yuhong Xiong
@version $Id$
@see ptolemy.actor.lib.jspaces.StockQuote
@see ptolemy.actor.lib.jspaces.Publisher
*/
public class StockServer extends TypedCompositeActor {

    /** Construct a StockServer with no container and empty string as its
     *  name.
     */
    public StockServer() {
        super();

	try {
	    _init();
	} catch (IllegalActionException iae) {
	    throw new InternalErrorException(iae.getMessage());
	} catch (NameDuplicationException nde) {
	    throw new InternalErrorException(nde.getMessage());
	}
    }

    /** Construct a StockServer in the specified workspace with no
     *  container and an empty string as a name.
     *  @param workspace The workspace that will list the actor.
     */
    public StockServer(Workspace workspace) {
	super(workspace);

	try {
	    _init();
	} catch (IllegalActionException iae) {
	    throw new InternalErrorException(iae.getMessage());
	} catch (NameDuplicationException nde) {
	    throw new InternalErrorException(nde.getMessage());
	}
    }

    /** Construct a StockServer with a name and a container.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *
     *  @param container The container actor.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public StockServer(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

    }

    ///////////////////////////////////////////////////////////////////
    ////                       public parameters                   ////

    /** Stock ticker symbols. This parameter must contain a StringToken.
     *  The string must contain a list of tickers separated by white
     *  space. E.g. "YHOO AOL CSCO". The default value is "YHOO".
     */
    public Parameter tickers;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the <code>tickers</code>
     *  public members to the parameters of the new actor.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        StockServer newobj = (StockServer)super.clone(ws);
        newobj.tickers = (Parameter)newobj.getAttribute("tickers");
        return newobj;
    }

    /** Parse the tickers string and creat StockQuote/Publisher pairs
     *  to handle each ticker, then call the super class method.
     *  If this actor already contains StockQuote/Publisher pairs, i.e.,
     *  this is not the first time this actor is executed, remove all
     *  the existing pairs before creating new ones.
     *  @exception IllegalActionException If the supper class method
     *   throws it.
     */
    public void preinitialize() throws IllegalActionException {
	try {
	    removeAllEntities();
	    removeAllRelations();

	    // create a dummy sink that will be connected to all the
	    // StockQuote actors so that the SDF graph is connected.
	    DummySink sink = new DummySink(this, "sink");

	    String allTickers = ((StringToken)tickers.getToken()).toString();
	    StringTokenizer st = new StringTokenizer(allTickers);
	    int numQuotes = 0;
	    while (st.hasMoreTokens()) {
	        String symbol = st.nextToken();
	        StockQuote quote = new StockQuote(this, "quote" + numQuotes);
	        Publisher publisher = new Publisher(this,
						"publisher" + numQuotes);
	        numQuotes++;

	        ComponentRelation relation = connect(quote.output,
						publisher.input);
	        sink.input.link(relation);

	        StringToken symbolToken = new StringToken(symbol);
	        quote.ticker.setToken(symbolToken);
	        publisher.entryName.setToken(symbolToken);

	        // lease quote in space for 1 min.
	        publisher.leaseTime.setToken(new LongToken(60*1000));
	    }

	    sink.input.tokenConsumptionRate.setToken(new IntToken(numQuotes));
	    super.preinitialize();

        } catch (NameDuplicationException nde) {
	    throw new InternalErrorException(nde.getMessage());
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Initialize parameters and create director.
    private void _init()
	    throws IllegalActionException, NameDuplicationException {
	tickers = new Parameter(this, "tickers", new StringToken("YHOO"));
	tickers.setTypeEquals(BaseType.STRING);

	// create a SDF director and set its iteration to 0 (run forever).
	SDFDirector director = new SDFDirector(this, "director");
	director.iterations.setToken(new IntToken(0));
    }

    ///////////////////////////////////////////////////////////////////
    ////                       inner class                         ////

    private class DummySink extends SDFAtomicActor {
	private DummySink(TypedCompositeActor container, String name)
		throws IllegalActionException, NameDuplicationException {
	    super(container, name);
	    input = new SDFIOPort(this, "input", true, false);
	    input.setMultiport(true);
	}

	/** Read out all tokens in the input port and discard.
	 */
	public void fire() throws IllegalActionException {
	    int width = input.getWidth();
	    for (int i=0; i<width; i++) {
		while (input.hasToken(i)) {
		    Token token = input.get(i);
		}
	    }
	}

	private SDFIOPort input;

    }
}

