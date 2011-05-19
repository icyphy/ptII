BEGIN {sawMacOSInclude = 1}
{
    if ($0 ~/MacOSInclude/) {
        sawMacOSInclude = 1
    }
    if ($0 ~ /\/MacOSInclude/) {
        sawMacOSInclude = 0
        print $0
    }
    if (sawMacOSInclude == 1) {
        print $0
    }
}