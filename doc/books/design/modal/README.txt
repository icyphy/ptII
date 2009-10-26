$Id$
Examples for the modal model chapter

These files are available via JNLP at:
http://ptolemy.eecs.berkeley.edu/ptolemyII/ptII8.0/jnlp-books/doc/books/design/modal/
On Bennett, the files are in:
~ptII/ptweb/ptolemyII/ptII8.0/jnlp-books/doc/books/design/modal/

To build all the jnlp files, run 
   make JNLP_MODEL=ExtendedFSM JNLP_MODEL_DIRECTORY=doc/books/design/modal KEYSTORE=/users/ptII/adm/certs/ptkeystore KEYALIAS=ptolemy STOREPASSWORD="-storepass xxxxxx" KEYPASSWORD="-keypass xxxxx" DIST_BASE=ptolemyII/ptII8.0/jnlp-books jnlps

See the jnlps rule in ptII/mk/ptcommon.mk and the book rule in
ptII/mk/jnlp.mk
