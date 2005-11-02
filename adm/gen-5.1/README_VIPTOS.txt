Viptos is an interface between TinyOS and Ptolemy II.

To get started, you must have the AVR tools, TinyOS and Nesc installed
and some environment variables must be set.
For details, see ptolemy/domains/ptinyos/util/nc2moml/index.htm.

The quick installation guide:
1. Set the PTII environment variable to this directory.  See
   doc/install.htm for details.
2. Install the AVR tools, TinyOS, Nesc
   as per ptolemy/domains/ptinyos/util/nc2moml/index.htm.
3. Be sure the following environment variables are set:
   TOSDIR, PTINYOS_MOMLROOT, NC2MOMLLIB_NESC and TOSROOT
4. Run     ./configure
5. Run     make fast
6. Run     bin/viptos
