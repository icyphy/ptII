This simple script is here to update HLA/Ptolemy models which contain an HLASubscriber which with a wrong setting for the parameter name (ie none or the default one).

oneModelUpdate.py is the one doing all the job.

Syntax for calling it :
python oneModelUpdate.py old new

Its depencies are
- python (obviously)
- lxml

Since hlacerti has been move from ptolemy/apps to org, the script assumes that HLASubscriber is in org.hlacerti.lib.HlaSubscriber
If it is not the case, just run the following command :
find . -name "*.xml" -exec sed -i -e  "s/ptolemy.apps.hlacerti/org.hlacerti/g" {}

updateHLASubscriber.sh will just find all XML files into the current folder and subdirectory and apply the python script to
all the XML files he has found.