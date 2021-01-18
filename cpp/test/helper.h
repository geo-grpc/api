//
// Created by David Raleigh on 1/18/21.
//

#ifndef CPP_HELPER_H
#define CPP_HELPER_H

#include <stdint.h>

struct MemoryStruct {
    char *memory;
    uint64_t size;
};

char* get_nsl_access_token();

#endif //CPP_HELPER_H
