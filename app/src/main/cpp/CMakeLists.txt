
cmake_minimum_required(VERSION 3.18.1)

project("datarescuepro")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Find required packages
find_library(log-lib log)
find_library(android-lib android)

# Add library
add_library(
    datarescuepro
    SHARED
    native-lib.cpp
    file_scanner.cpp
    disk_scanner.cpp
    file_recovery_engine.cpp
)

# Include directories
target_include_directories(datarescuepro PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
)

# Link libraries
target_link_libraries(
    datarescuepro
    ${log-lib}
    ${android-lib}
)
