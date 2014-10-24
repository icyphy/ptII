#include "ep/ep_stat.h"

/** Return true if the severity is less than warn. */
int ep_stat_isok(EP_STAT ep_stat) {
    fprintf(stderr, "ep_stat_isok: %lx\n", ep_stat.code);
    return EP_STAT_ISOK(ep_stat);
}
