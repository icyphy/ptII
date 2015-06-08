$Id$

This directory contains files used by JSDoc, which generates documentation for JavaScript.

Updating The JSDoc output
-------------------------

The JSDoc output is in $PTII/doc/codeDoc/js.

To regenerate that directory:

cd $PTII/vendors; git clone https://github.com/jsdoc3/jsdoc.git    
cd $PTII
./configure
ant jsdoc

See Also
--------
* https://chess.eecs.berkeley.edu/ptexternal/wiki/Main/JSDocSystems - Overview of JSDoc systems
* https://www.terraswarm.org/accessors/wiki/Main/JSDoc - Information for Accessor writers (TerraSwarm membership required)

