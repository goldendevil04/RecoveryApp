cmake_minimum_required(VERSION 3.22.1)

project("datarescue")

add_library(
    datarescue
    SHARED
    datarescue.cpp
    file_scanner.cpp
    disk_analyzer.cpp
)

find_library(
    log-lib
    log
)

target_link_libraries(
    datarescue
    ${log-lib}
)