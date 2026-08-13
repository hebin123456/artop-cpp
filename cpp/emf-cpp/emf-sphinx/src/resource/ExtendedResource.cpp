// ExtendedResource.cpp
#include "emf/sphinx/resource/ExtendedResource.h"
namespace emf::sphinx::resource {
const std::regex ExtendedResource::URI_QUERY_FIELD_PATTERN(
    R"((\w+)=([^&]*))", std::regex::optimize);
}
