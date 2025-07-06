#include "file_scanner.h"
#include <dirent.h>
#include <sys/stat.h>
#include <unistd.h>
#include <cstring>
#include <android/log.h>

#ifndef R_OK
#define R_OK 4
#endif

#define LOG_TAG "FileScanner"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

FileScanner::FileScanner() {
    LOGI("FileScanner initialized");
}

FileScanner::~FileScanner() {
    LOGI("FileScanner destroyed");
}

std::vector<std::string> FileScanner::scanDirectory(const std::string& path, bool recursive) {
    std::vector<std::string> files;

    LOGI("Scanning directory: %s", path.c_str());

    DIR* dir = opendir(path.c_str());
    if (!dir) {
        LOGE("Cannot open directory: %s", path.c_str());
        return files;
    }

    struct dirent* entry;
    while ((entry = readdir(dir)) != nullptr) {
        if (strcmp(entry->d_name, ".") == 0 || strcmp(entry->d_name, "..") == 0) {
            continue;
        }

        std::string fullPath = path + "/" + entry->d_name;
        struct stat statbuf;

        if (stat(fullPath.c_str(), &statbuf) == 0) {
            if (S_ISREG(statbuf.st_mode)) {
                files.push_back(fullPath);
            } else if (S_ISDIR(statbuf.st_mode) && recursive) {
                auto subFiles = scanDirectory(fullPath, recursive);
                files.insert(files.end(), subFiles.begin(), subFiles.end());
            }
        }
    }

    closedir(dir);
    LOGI("Found %zu files in directory: %s", files.size(), path.c_str());

    return files;
}

std::vector<std::string> FileScanner::findDeletedFiles(const std::string& path) {
    std::vector<std::string> deletedFiles;

    LOGI("Searching for deleted files in: %s", path.c_str());

    // Simulate finding deleted files
    // In real implementation, this would scan for file system entries marked as deleted
    deletedFiles.push_back(path + "/deleted_photo1.jpg");
    deletedFiles.push_back(path + "/deleted_video1.mp4");
    deletedFiles.push_back(path + "/deleted_document1.pdf");

    LOGI("Found %zu deleted files", deletedFiles.size());
    return deletedFiles;
}

bool FileScanner::isFileRecoverable(const std::string& filePath) {
    // Check if file exists and is readable
    struct stat statbuf;
    if (stat(filePath.c_str(), &statbuf) == 0) {
        return access(filePath.c_str(), R_OK) == 0;
    }

    // For deleted files, we need to check if the data is still available
    // This is a simplified check - real implementation would be more complex
    return true;
}

size_t FileScanner::getFileSize(const std::string& filePath) {
    struct stat statbuf;
    if (stat(filePath.c_str(), &statbuf) == 0) {
        return statbuf.st_size;
    }
    return 0;
}