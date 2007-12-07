// $Id$
// The .cpp and .h files in this directory are from salma-hayek, found at
// http://software.jessies.org/terminator/
// salma-hayek is LGPL'd, see the launcher-copyright.htm file

#ifndef UNIX_EXCEPTION_H_included
#define UNIX_EXCEPTION_H_included

#include <errno.h>
#include <stdexcept>
#include <sstream>
#include <string.h>
#include <string>

class unix_exception : public std::runtime_error {
public:
    unix_exception(const std::string& message)
    : std::runtime_error(message + (errno ? ": (" + errnoToString() + ")" : ""))
    , m_errno(errno)
    {
    }

    int getErrno() const {
        return m_errno;
    }

private:
    int gnuCompatibleStrerror(int (*strerror_r_fn)(int, char*, size_t), int errorNumber, char* messageBuffer, size_t bufferSize) {
        return strerror_r_fn(errorNumber, messageBuffer, bufferSize);
    }

    int gnuCompatibleStrerror(char* (*strerror_r_fn)(int, char*, size_t), int errorNumber, char* messageBuffer, size_t bufferSize) {
        const char* intermediateBuffer = strerror_r_fn(errorNumber, messageBuffer, bufferSize);
        // strncpy doesn't support copying over oneself.
        if (intermediateBuffer != messageBuffer) {
            strncpy(messageBuffer, intermediateBuffer, bufferSize);
            messageBuffer[bufferSize - 1] = 0;
        }
        return 0;
    }

    /** Converts the current value of 'errno' to a string. */
    std::string errnoToString() {
        // I'm concerned that errno may have changed by the time this function is called.
        // I suppose there's no sense in paying for the solution for that problem until we have the problem.
        int errorNumber = errno;
        char messageBuffer[1024];
        if (gnuCompatibleStrerror(&strerror_r, errorNumber, &messageBuffer[0], sizeof(messageBuffer)) == -1) {
            int decodingError = errno;
            std::ostringstream os;
            switch (decodingError) {
            case EINVAL:
                os << "The value " << errorNumber << " is not a valid error number.";
                break;
            case ERANGE:
                os << sizeof(messageBuffer) << " bytes was not enough to contain the error description string for error number " << errorNumber << ".";
                break;
            default:
                os << "Decoding error number " << errorNumber << " produced error " << decodingError << ".";
                break;
            }
            return os.str();
        } else {
            return messageBuffer;
        }
    }

    int m_errno;
};

#endif
