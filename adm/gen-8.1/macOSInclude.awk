BEGIN {sawMacOSInclude = 1}
{
    if ($0 ~/MacOSInclude/) {
        sawMacOSInclude = 1
    }
    if ($0 ~ /\/MacOSInclude/) {
        sawMacOSInclude = 0
        print $0
    }

    if ($0 ~ /<pack name="JRE" required="no">/) {
	sawPackJRE = 1
    } 
    if (sawMacOSInclude == 1 && sawPackJRE == 0) {
        print $0
    }
    if ($0 ~ /<\/pack>/) {
	sawPackJRE = 0
    } 
}