Documentation:
--------------

This is an implementation of the Master Algorithm (MA) that was defined in

	/*
	 *  David Broman, Christopher Brooks, Lev Greenberg, Edward A.
	 *  Lee, Michael Masin, Stavros Tripakis, Michael Wetter.
	 *  "Determinate Composition of FMUs for Co-Simulation". 13th International
	 *  Conference on Embedded Software (EMSOFT), Montreal, 29, September, 2013.
	 *  http://chess.eecs.berkeley.edu/pubs/1002.html
	 */

Author: Fabio Cremona 
	
Date: 11/15/2014

Notes:
	This is an extension to FMI2.0 of the original work by Fabian Stahnke for FMI2.0RC1

The entry point to the master algorithm is defined in main2.c.

How to compile:
	$ make
	
How to run:
	$ make run 		/* example #1, inc20pt -> scale20pt -> out20pt */


