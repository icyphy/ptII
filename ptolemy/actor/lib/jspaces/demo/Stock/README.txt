$Id$

Here are some tips for running the stock price demo

1) You need to start the Jini and javaSpace Servers.
   Please refer to $PTII/ptolemy/actor/lib/jspaces/README
   for more information

2) server is a script to start the stock quote server

3) reader is a script to start the stock quote reader.

4) You can start as many servers and readers as you like.
   But please don't start servers with the same stock name.

5) Don't forget to run 
   $PTII/ptolemy/actor/lib/jspaces/util/cleanspace
   between two runs of the demo.


