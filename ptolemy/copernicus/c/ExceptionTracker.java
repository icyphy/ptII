/*
A class that keeps track of exceptions and traps.

Copyright (c) 2001-2002 The University of Maryland.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.


@ProposedRating Red (ankush@glue.umd.edu)
@AcceptedRating Red (ankush@glue.umd.edu)

*/

package ptolemy.copernicus.c;

import soot.Trap;
import soot.Unit;
import soot.Body;

import soot.util.Chain;

import java.util.Iterator;
import java.util.LinkedList;


public class ExceptionTracker
{
/*
A class that keeps track of Exceptions and Traps
@author Ankush Varma
@version $Id$
@since Ptolemy II 2.0
*/

	public void ExceptionTracker()
	{
		//do nothing

	}

	public boolean isBeginUnit(Unit u)
	{
		return(_beginUnitList.contains(u));
	}

	public boolean isEndUnit(Unit u)
	{
		return(_endUnitList.contains(u));
	}

	public boolean isHandlerUnit(Unit u)
	{
		return(_handlerUnitList.contains(u));
	}

	public int beginIndexOf(Unit u)
	{
	   return(_beginUnitList.indexOf(u));
        }

        public int endIndexOf(Unit u)
        {
            return(_endUnitList.indexOf(u));
        }

        public int handlerIndexOf(Unit u)
        {
            return(_handlerUnitList.indexOf(u));
        }


	public void init(Body body)
	{
		_trapChain = body.getTraps();
		Iterator i = _trapChain.iterator();
		_epc = 0;

		_beginUnitList   = new LinkedList();
		_endUnitList     = new LinkedList();
		_handlerUnitList = new LinkedList();

                _currently_active_traps = new LinkedList();
                _trapsForEachEpc = new LinkedList();


		while (i.hasNext())
		{
			Trap CurrentTrap = (Trap)i.next();

			Unit BeginUnit   = CurrentTrap.getBeginUnit();
			Unit EndUnit     = CurrentTrap.getEndUnit();
			Unit HandlerUnit = CurrentTrap.getHandlerUnit();

			_beginUnitList.add(BeginUnit);
			_endUnitList.add(EndUnit);
			_handlerUnitList.add(HandlerUnit);

		}
	}



	public boolean trapsExist()
	{
		return(_beginUnitList.size() != 0);
	}

	public int numberOfTraps()
	{
		return(_beginUnitList.size());
	}

	public int getEpc()
	{
	   return _epc;
        }

	public void beginUnitEncountered(Unit u)
	{
           _storeState();

           _epc++;
           Iterator i = _trapChain.iterator();

           //record all traps beginning here as active
           while (i.hasNext())
           {
               Trap ThisTrap = (Trap)i.next();
               if (ThisTrap.getBeginUnit() == u)
                   _currently_active_traps.addFirst(ThisTrap);
                   //so that traps are stored in the reverse order in which they
                   //are encountered. Most recent trap first.
           }
	}

        public void endUnitEncountered(Unit u)
        {
            _storeState();

            _epc++;
            Iterator i = _trapChain.iterator();

            //record all traps ending here as inactive
            while (i.hasNext())
            {
                Trap ThisTrap = (Trap)i.next();
                if (ThisTrap.getEndUnit() == u)
                   _currently_active_traps.remove(ThisTrap);
            }

        }

        public LinkedList getHandlerUnitList(int epc)
        //returns a list of all handlers associated with a given epc
        {
            LinkedList ListOfTraps = (LinkedList)_trapsForEachEpc.get(epc);
            LinkedList ListOfHandlers = new LinkedList();
            Iterator i = ListOfTraps.listIterator();

            while (i.hasNext())
            {
                Trap t = (Trap)i.next();
                ListOfHandlers.add(t.getHandlerUnit());
            }

            return ListOfHandlers;
        }

        public LinkedList getTrapsForEpc(int epc)
        //returns a list of traps corresponding to the given epc
        {
            return (LinkedList)_trapsForEachEpc.get(epc);
        }

        public Chain getTrapChain()
        {
            return _trapChain;
        }

        protected void _storeState()
        {
            _trapsForEachEpc.add(_currently_active_traps.clone());
            //yes, this is a list of lists
        }



	protected int _epc;

        protected Chain _trapChain;

	protected LinkedList _beginUnitList;
	protected LinkedList _endUnitList;
	protected LinkedList _handlerUnitList;

        protected LinkedList _currently_active_traps;
        protected LinkedList _trapsForEachEpc;
        //list containing the list of traps for each epc
        //the index in this list is the epc, so the
        //epc is not explicitly stored in this data
        //structure


}


