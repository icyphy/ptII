/*** arraycopyBlock($type) ***/
void $type_arraycopy($type *src, int srcPos, $type *dest, int destPos, int length) {
    int i;
    for (i = 0; i < length; i++) {
        dest[destPos + i] = src[srcPos + i];
    }
}
/**/
