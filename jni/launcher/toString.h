// $Id$
// The .cpp and .h files in this directory are from salma-hayek, found at
// http://software.jessies.org/terminator/
// salma-hayek is LGPL'd, see the launcher-copyright.htm file

#ifndef TO_STRING_H_included
#define TO_STRING_H_included

#include <sstream>
#include <stdexcept>
#include <string>

// Based on code by Kevlin Henney, shown in "Exceptional C++ Style".
template <typename T>
inline std::string toString(const T& value) {
  std::stringstream interpreter;
  std::string result;
  if (!(interpreter << value) || !(interpreter >> result) || !(interpreter >> std::ws).eof()) {
    throw std::runtime_error("bad lexical cast");
  }
  return result;
}

#endif
