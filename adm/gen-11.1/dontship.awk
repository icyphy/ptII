#! /usr/bin/awk
# Script that strips out text between <!-- dontship --> and <!-- /dontship -->
{
    if ($0 ~/dontship/) {
	sawDontShip = 1;
	if ($0 ~/\/dontship/) {
	    sawDontShip = 0;
	}
    }
    if (sawDontShip == 0)  {
	print $0;
    }
}
