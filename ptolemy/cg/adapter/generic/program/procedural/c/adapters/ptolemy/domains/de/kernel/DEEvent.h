/* In this file we have defined the structure of a DE Event
 * It contains a pointer to its actor, its destination port
 * and a few fiels (timestamp, microstep, priority, depth)
 *
 * @author : William Lucas
 */

#ifndef DE_EVENT
#define DE_EVENT

typedef struct DEEvent DEEvent;
#include "$ModelName()_types.h"
#include "$ModelName()__Actor.h"
#include "$ModelName()__IOPort.h"

struct DEEvent {
  /** The destination actor. */
  Actor * actor;

  /** The depth of this event. */
  int depth;

  /** The destination IO port. */
  IOPort * ioPort;

  /** The microstep of this event. */
  int microstep;

  /** The priority of the event (used when the timestamp, depth and
   *  microstep cannot resolve a conflict.
   */
  int priority;

  /** The timestamp of the event. */
  Time timestamp;

  /** The Token corresponding if it is a trigger event */
  Token token;
};

DEEvent * newDEEvent();
DEEvent * newDEEventWithParam(Actor* actor, IOPort* ioPort, int depth,
		int microstep, int priority, Time timestamp);
bool DEEventEquals (const DEEvent * e1, const DEEvent * e2);
int DEEventCompare(const DEEvent * e1, const DEEvent * e2);
void DEEventDelete(DEEvent * e);

#endif
