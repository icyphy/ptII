$Id$

This directory contains unmodified versions of Node.js modules that
have no dependencies on Node.js so that they will work in Nashorn. To
add modules, just do:

  cd $PTII/ptolemy/actor/lib/jjs/modules
  npm install moduleName
  svn add moduleName

Make sure before committing that you have verified that the license
for the module is compatible with the BSD license of Ptolemy II (no
GPL'd code!!), added the license to
ptolemy/actor/lib/jjs/jjs-license.htm, that you have tested the module
in Nashorn, and that you have documented it here:



