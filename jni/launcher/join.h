// $Id$
// The .cpp and .h files in this directory are from salma-hayek, found at
// http://software.jessies.org/terminator/
// salma-hayek is LGPL'd, see the launcher-copyright.htm file

#ifndef JOIN_H_included
#define JOIN_H_included

template <class ValueInitializer, class Container>
inline typename Container::value_type join(const ValueInitializer& separatorInitializer, const Container& container) {
  typename Container::value_type separator(separatorInitializer);
  typename Container::value_type joined;
  if (container.empty()) {
    return joined;
  }
  typename Container::const_iterator it = container.begin();
  joined = *it;
  ++ it;
  while (it != container.end()) {
    joined.insert(joined.end(), separator.begin(), separator.end());
    joined.insert(joined.end(), it->begin(), it->end());
    ++ it;
  }
  return joined;
}

#endif
