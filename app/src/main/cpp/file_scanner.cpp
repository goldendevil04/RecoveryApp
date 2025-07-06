#include "file_scanner.h"
#include <android/log.h>
#include <fstream>
#include <dirent.h>
#include <sys/stat.h>
#include <unistd.h>
#include <cstring>

#define LOG_TAG "FileScanner"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

FileScanner::FileScanner() {
    initializeSignatures();
}

FileScanner::~FileScanner() {
}

void FileScanner::initializeSignatures() {
    // JPEG
    fileSignatures.push_back({
        {0xFF, 0xD8, 0xFF},
        "jpg",
        0
    });
    
    // PNG
    fileSignatures.push_back({
        {0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A},
        "png",
        0
    });
    
    // PDF
    fileSignatures.push_back({
        {0x25, 0x50, 0x44, 0x46},
        "pdf",
        0
    });
    
    // MP4
    fileSignatures.push_back({
        {0x66, 0x74, 0x79, 0x70},
        "mp4",
        4
    });
    
    // MP3
    fileSignatures.push_back({
        {0x49, 0x44, 0x33},
        "mp3",
        0
    });
    
    // ZIP
    fileSignatures.push_back({
        {0x50, 0x4B, 0x03, 0x04},
        "zip",
        0
    });
}

std::vector<int> FileScanner::scanDirectory(const char* path, bool isRooted) {
    LOGI("Scanning directory: %s (Root: %d)", path, isRooted);
    
    std::vector<int> results;
    
    if (isRooted) {
        // Enhanced scanning with root access
        results = scanForDeletedFiles(path, true);
    } else {
        // Standard scanning without root
        results = scanForDeletedFiles(path, false);
    }
    
    LOGI("Scan completed. Found %zu potential files", results.size());
    return results;
}

std::vector<int> FileScanner::scanForDeletedFiles(const char* path, bool isRooted) {
    std::vector<int> foundFiles;
    
    DIR* dir = opendir(path);
    if (!dir) {
        LOGE("Cannot open directory: %s", path);
        return foundFiles;
    }
    
    struct dirent* entry;
    while ((entry = readdir(dir)) != nullptr) {
        if (strcmp(entry->d_name, ".") == 0 || strcmp(entry->d_name, "..") == 0) {
            continue;
        }
        
        std::string fullPath = std::string(path) + "/" + entry->d_name;
        struct stat statbuf;
        
        if (stat(fullPath.c_str(), &statbuf) == 0) {
            if (S_ISREG(statbuf.st_mode)) {
                // Check if file might be recoverable
                foundFiles.push_back(static_cast<int>(statbuf.st_size));
            } else if (S_ISDIR(statbuf.st_mode) && isRooted) {
                // Recursively scan subdirectories if root access
                auto subResults = scanForDeletedFiles(fullPath.c_str(), isRooted);
                foundFiles.insert(foundFiles.end(), subResults.begin(), subResults.end());
            }
        }
    }
    
    closedir(dir);
    return foundFiles;
}

bool FileScanner::matchesSignature(const uint8_t* data, const FileSignature& sig) {
    for (size_t i = 0; i < sig.signature.size(); i++) {
        if (data[sig.offset + i] != sig.signature[i]) {
            return false;
        }
    }
    return true;
}

std::string FileScanner::identifyFileType(const uint8_t* data, size_t size) {
    for (const auto& sig : fileSignatures) {
        if (size > sig.offset + sig.signature.size()) {
            if (matchesSignature(data, sig)) {
                return sig.extension;
            }
        }
    }
    return "unknown";
}

bool FileScanner::recoverFile(const char* sourcePath, const char* destPath) {
    LOGI("Recovering file from %s to %s", sourcePath, destPath);
    
    std::ifstream source(sourcePath, std::ios::binary);
    if (!source) {
        LOGE("Cannot open source file: %s", sourcePath);
        return false;
    }
    
    std::ofstream dest(destPath, std::ios::binary);
    if (!dest) {
        LOGE("Cannot create destination file: %s", destPath);
        return false;
    }
    
    dest << source.rdbuf();
    
    bool success = source.good() && dest.good();
    LOGI("File recovery %s", success ? "successful" : "failed");
    
    return success;
}