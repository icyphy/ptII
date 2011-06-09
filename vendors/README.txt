$Id$

The $PTII/vendors directory contains code from vendors that
we cannot redistribute because of licensing reasons.

In general, subdirectories below this point are _not_ checked in to
CVS, though there are exceptions.  

Please do not add large subdirectories to CVS without checking with
the ptresearch email alias.  If large directories are checked in, then
it will greatly increase the time it takes remote users to do cvs
updates.  Rather than checking in the source for a large package,
you may want to check in only the runtime libraries by using
'cvs add -kb filename'.

Each sudirectory takes the name of a vendor. If we are likely to use
multiple packages from a vendor, then create a new directory for that
vendor.

Each package should be in a directory that includes the version number
so that we can support multiple versions. 

If we are likely to use only one package and one version from a vendor,
then put the package in the misc directory under the vendor name.

Do not break the nightly build by requiring that the nightly build
use files from the vendors directory.  Remember that we are not
shipping the vendors directory, so the build needs to work without it
being present.  The way around this is to modify $PTII/configure.in
so that it looks for the package in question and then sets makefile
variables appropriately.  Usually, the best thing to do is to
send email to cxh and ask him to make the changes to configure.in
for you.  $PTII/doc/coding/develsetup.htm includes some instructions
about how to do this.



