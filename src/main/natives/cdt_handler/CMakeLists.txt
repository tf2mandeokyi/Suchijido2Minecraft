cmake_minimum_required(VERSION 3.13)

set(CMAKE_CXX_STANDARD 11)
project(cdt_handler)

SET(CDT_HOME "./CDT" CACHE STRING "Path to CDT repository")


### initialize library ###
set(cdt_handler_files
    triangulation.cpp
    main.cpp
)
add_library(cdt_handler SHARED ${cdt_handler_files})


### CDT library ###
add_subdirectory("${CDT_HOME}/CDT" CDT)
target_link_libraries(cdt_handler PUBLIC CDT)


### remove prefix ###
set_target_properties(cdt_handler PROPERTIES PREFIX "")


### including java ###
message(STATUS "*****************************")
message(STATUS "** Java path: $ENV{JAVA_HOME}")
message(STATUS "** System name: ${CMAKE_SYSTEM_NAME}")
message(STATUS "*****************************")

target_include_directories(cdt_handler PRIVATE "$ENV{JAVA_HOME}/include")
if(CMAKE_SYSTEM_NAME STREQUAL "Windows")
    target_include_directories(cdt_handler PRIVATE "$ENV{JAVA_HOME}/include/win32")
elseif(CMAKE_SYSTEM_NAME STREQUAL "Linux")
    target_include_directories(cdt_handler PRIVATE "$ENV{JAVA_HOME}/include/linux")
elseif(CMAKE_SYSTEM_NAME STREQUAL "Darwin")
    target_include_directories(cdt_handler PRIVATE "$ENV{JAVA_HOME}/include/darwin")
elseif(CMAKE_SYSTEM_NAME STREQUAL "FreeBSD")
    target_include_directories(cdt_handler PRIVATE "$ENV{JAVA_HOME}/include/freebsd")
endif()