
* * *

* *The text above was downloaded from https://www.terraswarm.org/accessors/wiki/Version0/OptionalJavaScriptModules?action=markdown -O OptionalJavaScriptModules.md *
* *The text below is from $PTII/doc/jsdoc/topREADMEEnd.md *

* * *

Where to find this page on the web
----------------------------------
[https://chess.eecs.berkeley.edu/ptexternal/src/ptII/doc/codeDoc/js/index.html](https://chess.eecs.berkeley.edu/ptexternal/src/ptII/doc/codeDoc/js/index.html)

How to get the list of Accessors from the TerraSwarm accessors wiki
-------------------------------------------------------------------
Currently, https://www.terraswarm.org/accessors/wiki/Version0/OptionalJavaScriptModules is not world readable.

However, we use the contents of that URL for the first part of this page.  What we do is use wget to get a markdown version of that page using cookies and then concatenate it with $PTII/doc/jsdoc/topREADMEEnd.md to create $PTII/doc/jsdoc/topREADME.md.  Running "cd $PTII; ant jsdoc" reads topREADME.md.

The script $PTII/doc/jsdoc/makeptjsdocREADME does this for us and is invoked by the continuous integration build.

If you invoke this script by hand, then you will need ~/.terracookies.  To create this, install the Cookies Export plugin into Firefox, log on to the TerraSwarm website and export your cookies to that file.  Note that the account that you use to log on should remain logged in for the wget command to work.

Then run $PTII/doc/jsdoc/makeptjsdocREADME.


How to update the JSDoc output in your ptII tree
------------------------------------------------

The JSDoc output is in $PTII/doc/js.

To regenerate that directory:

cd $PTII/vendors; git clone https://github.com/jsdoc3/jsdoc.git
cd $PTII
./configure
ant jsdoc

How to update the JSDoc output on the web
-----------------------------------------
The ptII continuous integration build at [http://terra.eecs.berkeley.edu:8080/job/ptIIci/](http://terra.eecs.berkeley.edu:8080/job/ptIIci/) checks the ptII svn repository every 5 minutes and if there is a change, then "ant jsdoc" is run and then the codeDoc/ directory is copied to [https://chess.eecs.berkeley.edu/ptexternal/src/ptII/doc/codeDoc/js/index.html](https://chess.eecs.berkeley.edu/ptexternal/src/ptII/doc/codeDoc/js/index.html).

See Also
--------
* [https://chess.eecs.berkeley.edu/ptexternal/wiki/Main/JSDocSystems](https://chess.eecs.berkeley.edu/ptexternal/wiki/Main/JSDocSystems) - Overview of JSDoc systems
* [https://www.terraswarm.org/accessors/wiki/Main/JSDoc](https://www.terraswarm.org/accessors/wiki/Main/JSDoc) - Information for Accessor writers (TerraSwarm membership required)
* [https://www.terraswarm.org/accessors/doc/jsdoc/index.html](https://www.terraswarm.org/accessors/doc/jsdoc/index.html) - JavaScript documentation of Accessors.

How to update this file
-----------------------
The source for this file is at $PTII/doc/jsdoc/topREADME.md.

It is copied to $PTII/doc/jsdoc/index.html when JSDoc is invoked with -R $PTII/doc/jsdoc/topREADME.md

$Id: topREADME.md 72695 2015-06-29 19:27:50Z cxh $

