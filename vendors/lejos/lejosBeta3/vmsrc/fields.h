#ifndef _FIELDS_H
#define _FIELDS_H

// For instance fields:

#define get_pgfield_type(HIBYTE_)            ((HIBYTE_) >> 4)
#define get_pgfield_offset(HIBYTE_,LOBYTE_)  (((TWOBYTES) ((HIBYTE_) & 0x0F) << 8) | (LOBYTE_))



#endif _FIELDS_H
