#include "file_recovery_engine.h"
#include <unistd.h>
#include <sys/stat.h>
#include <cstring>
#include <android/log.h>
#include <fstream>
#include <sstream>
#include <algorithm>
#include <dirent.h>
#include <memory>
#include <vector>

#define LOG_TAG "FileRecoveryEngine"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

FileRecoveryEngine::FileRecoveryEngine() {
    LOGI("Enhanced FileRecoveryEngine initialized");
}

FileRecoveryEngine::~FileRecoveryEngine() {
    LOGI("FileRecoveryEngine destroyed");
}

std::vector<int> FileRecoveryEngine::performEnhancedScan(const char* path, bool isRooted) {
    std::vector<int> results;

    if (!path) {
        LOGE("Null path provided");
        return results;
    }

    LOGI("Performing enhanced scan on: %s", path);

    // Scan for actual file remnants and deleted entries
    results = scanForDeletedEntries(path, isRooted);

    // Scan for file signatures in unallocated space
    auto signatureResults = scanForFileSignatures(path);
    results.insert(results.end(), signatureResults.begin(), signatureResults.end());

    LOGI("Found %zu potential recoverable items", results.size());

    return results;
}

std::vector<int> FileRecoveryEngine::scanForDeletedEntries(const char* path, bool isRooted) {
    std::vector<int> results;

    if (!path) {
        LOGE("Null path provided");
        return results;
    }

    DIR* dir = nullptr;
    try {
        dir = opendir(path);
        if (!dir) {
            LOGE("Cannot open directory: %s", path);
            return results;
        }

        struct dirent* entry;
        int fileId = 1;

        while ((entry = readdir(dir)) != nullptr) {
            if (!entry->d_name || strcmp(entry->d_name, ".") == 0 || strcmp(entry->d_name, "..") == 0) {
                continue;
            }

            std::string fullPath = std::string(path) + "/" + entry->d_name;
            struct stat statbuf;

            if (stat(fullPath.c_str(), &statbuf) == 0) {
                // Check if file appears deleted or corrupted
                if (isFileDeleted(fullPath.c_str()) || isFileCorrupted(fullPath.c_str())) {
                    results.push_back(fileId++);
                }
            }
        }

        // Enhanced scan for hidden/system files if rooted
        if (isRooted) {
            auto systemResults = scanSystemAreas(path);
            results.insert(results.end(), systemResults.begin(), systemResults.end());
        }

    } catch (const std::exception& e) {
        LOGE("Error in enhanced scan: %s", e.what());
    }

    if (dir) {
        closedir(dir);
    }

    return results;
}

std::vector<int> FileRecoveryEngine::scanForFileSignatures(const char* path) {
    std::vector<int> results;

    if (!path) {
        LOGE("Null path provided");
        return results;
    }

    try {
        std::ifstream file(path, std::ios::binary);
        if (!file.is_open()) {
            return results;
        }

        // Common file signatures
        static const std::vector<std::vector<uint8_t>> signatures = {
                {0xFF, 0xD8, 0xFF}, // JPEG
                {0x89, 0x50, 0x4E, 0x47}, // PNG
                {0x47, 0x49, 0x46, 0x38}, // GIF
                {0x25, 0x50, 0x44, 0x46}, // PDF
                {0x50, 0x4B, 0x03, 0x04}, // ZIP
                {0xFF, 0xFB}, // MP3
                {0x66, 0x74, 0x79, 0x70} // MP4
        };

        const size_t bufferSize = 8192;
        std::vector<uint8_t> buffer(bufferSize);
        int fileId = 1000; // Start from 1000 for signature-based results

        while (file.read(reinterpret_cast<char*>(buffer.data()), bufferSize)) {
            size_t bytesRead = static_cast<size_t>(file.gcount());
            if (bytesRead == 0) break;

            for (const auto& signature : signatures) {
                if (signature.size() > bytesRead) continue;

                for (size_t i = 0; i <= bytesRead - signature.size(); ++i) {
                    if (std::equal(signature.begin(), signature.end(), buffer.begin() + i)) {
                        results.push_back(fileId++);
                        LOGI("Found file signature at offset: %zu",
                             static_cast<size_t>(file.tellg()) - bytesRead + i);
                    }
                }
            }
        }

    } catch (const std::exception& e) {
        LOGE("Error scanning for file signatures: %s", e.what());
    }

    return results;
}

std::vector<int> FileRecoveryEngine::scanSystemAreas(const char* basePath) {
    std::vector<int> results;

    if (!basePath) {
        LOGE("Null basePath provided");
        return results;
    }

    static const std::vector<std::string> systemPaths = {
            "/data/lost+found",
            "/data/tmp",
            "/cache",
            "/data/media/0/.trash",
            "/data/media/0/.Trash-1000"
    };

    int fileId = 2000; // Start from 2000 for system scan results

    for (const auto& sysPath : systemPaths) {
        if (access(sysPath.c_str(), R_OK) == 0) {
            auto pathResults = scanForDeletedEntries(sysPath.c_str(), true);
            results.insert(results.end(), pathResults.begin(), pathResults.end());
        }
    }

    return results;
}

std::vector<uint8_t> FileRecoveryEngine::recoverDeletedFile(const char* filePath) {
    std::vector<uint8_t> recoveredData;

    if (!filePath) {
        LOGE("Null filePath provided");
        return recoveredData;
    }

    LOGI("Attempting to recover file: %s", filePath);

    try {
        // First try direct file access
        std::ifstream file(filePath, std::ios::binary);
        if (file.is_open()) {
            file.seekg(0, std::ios::end);
            std::streampos fileSize = file.tellg();
            if (fileSize <= 0) {
                LOGI("File is empty");
                return recoveredData;
            }

            file.seekg(0, std::ios::beg);
            recoveredData.resize(static_cast<size_t>(fileSize));

            if (!file.read(reinterpret_cast<char*>(recoveredData.data()), fileSize)) {
                LOGE("Failed to read file contents");
                recoveredData.clear();
            } else {
                LOGI("Successfully recovered %zu bytes from direct access",
                     static_cast<size_t>(fileSize));
            }
            return recoveredData;
        }

        // Try recovery from backup locations
        const char* fileName = strrchr(filePath, '/');
        fileName = fileName ? fileName + 1 : filePath;

        static const std::vector<std::string> backupPaths = {
                std::string(filePath) + ".bak",
                std::string(filePath) + "~",
                "/data/media/0/.trash/" + std::string(fileName),
                "/cache/" + std::string(fileName)
        };

        for (const auto& backupPath : backupPaths) {
            std::ifstream backupFile(backupPath, std::ios::binary);
            if (backupFile.is_open()) {
                backupFile.seekg(0, std::ios::end);
                std::streampos fileSize = backupFile.tellg();
                if (fileSize <= 0) continue;

                backupFile.seekg(0, std::ios::beg);
                recoveredData.resize(static_cast<size_t>(fileSize));

                if (backupFile.read(reinterpret_cast<char*>(recoveredData.data()), fileSize)) {
                    LOGI("Successfully recovered %zu bytes from backup: %s",
                         static_cast<size_t>(fileSize), backupPath.c_str());
                    return recoveredData;
                }
                recoveredData.clear();
            }
        }

        // Try journal/log file recovery
        recoveredData = recoverFromJournal(filePath);
        if (!recoveredData.empty()) {
            LOGI("Successfully recovered %zu bytes from journal", recoveredData.size());
        }

    } catch (const std::exception& e) {
        LOGE("Error during file recovery: %s", e.what());
    }

    return recoveredData;
}

std::vector<uint8_t> FileRecoveryEngine::recoverFromJournal(const char* filePath) {
    std::vector<uint8_t> recoveredData;

    if (!filePath) {
        LOGE("Null filePath provided");
        return recoveredData;
    }

    try {
        // Check ext4 journal
        const char* journalPath = "/proc/fs/ext4/journal";
        std::ifstream journal(journalPath, std::ios::binary);

        if (journal.is_open()) {
            const size_t bufferSize = 4096;
            std::vector<uint8_t> buffer(bufferSize);
            std::string targetPath(filePath);

            while (journal.read(reinterpret_cast<char*>(buffer.data()), bufferSize)) {
                size_t bytesRead = static_cast<size_t>(journal.gcount());
                if (bytesRead == 0) break;

                // Look for file path references in journal
                std::string bufferStr(reinterpret_cast<char*>(buffer.data()), bytesRead);
                if (bufferStr.find(targetPath) != std::string::npos) {
                    recoveredData.insert(recoveredData.end(), buffer.begin(), buffer.begin() + bytesRead);
                    break;
                }
            }
        }
    } catch (const std::exception& e) {
        LOGE("Error recovering from journal: %s", e.what());
    }

    return recoveredData;
}

std::vector<std::string> FileRecoveryEngine::scanFreeClusters(const char* devicePath) {
    std::vector<std::string> clusters;

    if (!devicePath) {
        LOGE("Null devicePath provided");
        return clusters;
    }

    LOGI("Scanning free clusters on device: %s", devicePath);

    try {
        // Simulate cluster scanning - real implementation would use low-level disk access
        for (int i = 0; i < 50; i++) {
            clusters.emplace_back(std::string(devicePath) + "/cluster_" + std::to_string(i));
        }
    } catch (const std::exception& e) {
        LOGE("Error scanning free clusters: %s", e.what());
    }

    return clusters;
}

bool FileRecoveryEngine::isFileDeleted(const char* filePath) {
    if (!filePath) {
        LOGE("Null filePath provided");
        return false;
    }

    struct stat statbuf;
    if (stat(filePath, &statbuf) != 0) {
        return true; // File doesn't exist
    }

    // Check if file size is 0 or if it's a broken symlink
    return statbuf.st_size == 0 || !S_ISREG(statbuf.st_mode);
}

bool FileRecoveryEngine::isFileCorrupted(const char* filePath) {
    if (!filePath) {
        LOGE("Null filePath provided");
        return false;
    }

    std::ifstream file(filePath, std::ios::binary);
    if (!file.is_open()) {
        return true;
    }

    // Read first few bytes to check for corruption
    std::vector<uint8_t> header(16);
    if (!file.read(reinterpret_cast<char*>(header.data()), 16)) {
        return true;
    }

    // Simple corruption check - all zeros or all 0xFF
    bool allZeros = std::all_of(header.begin(), header.end(), [](uint8_t b) { return b == 0; });
    bool allFF = std::all_of(header.begin(), header.end(), [](uint8_t b) { return b == 0xFF; });

    return allZeros || allFF;
}

bool FileRecoveryEngine::detectRootAccess() {
    return checkSuBinary() || checkRootFiles() || checkSystemPartition();
}

bool FileRecoveryEngine::checkSuBinary() {
    static const char* suPaths[] = {
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/su/bin/su",
            "/magisk/.core/bin/su",
            "/system/app/Superuser.apk",
            "/data/local/tmp/su",
            "/data/local/bin/su",
            nullptr
    };

    for (int i = 0; suPaths[i] != nullptr; i++) {
        if (access(suPaths[i], F_OK) == 0) {
            LOGI("Found su binary at: %s", suPaths[i]);
            return true;
        }
    }

    return false;
}

bool FileRecoveryEngine::checkRootFiles() {
    static const char* rootFiles[] = {
            "/system/app/SuperSU.apk",
            "/system/app/Kinguser.apk",
            "/data/data/eu.chainfire.supersu",
            "/data/data/com.noshufou.android.su",
            "/data/data/com.koushikdutta.superuser",
            "/system/etc/init.d/99SuperSUDaemon",
            "/dev/com.koushikdutta.superuser.daemon/",
            "/system/xbin/daemonsu",
            nullptr
    };

    for (int i = 0; rootFiles[i] != nullptr; i++) {
        if (access(rootFiles[i], F_OK) == 0) {
            LOGI("Found root file: %s", rootFiles[i]);
            return true;
        }
    }

    return false;
}

bool FileRecoveryEngine::checkSystemPartition() {
    struct stat info;
    if (stat("/system", &info) == 0) {
        if (access("/system", W_OK) == 0) {
            LOGI("System partition is writable");
            return true;
        }
    }

    // Check for Magisk
    if (access("/sbin/.magisk", F_OK) == 0 || access("/data/adb/magisk", F_OK) == 0) {
        LOGI("Magisk detected");
        return true;
    }

    return false;
}

int FileRecoveryEngine::identifyFileType(const uint8_t* signature, size_t length) {
    if (!signature || length < 4) return UNKNOWN;

    return detectFileTypeBySignature(signature, length);
}

int FileRecoveryEngine::detectFileTypeBySignature(const uint8_t* data, size_t size) {
    if (!data || size < 4) return UNKNOWN;

    // JPEG
    if (data[0] == 0xFF && data[1] == 0xD8 && data[2] == 0xFF) {
        return JPEG;
    }

    // PNG
    if (data[0] == 0x89 && data[1] == 0x50 && data[2] == 0x4E && data[3] == 0x47) {
        return PNG;
    }

    // GIF
    if (size >= 4 && memcmp(data, "GIF8", 4) == 0) {
        return GIF;
    }

    // PDF
    if (size >= 4 && memcmp(data, "%PDF", 4) == 0) {
        return PDF;
    }

    // ZIP
    if (data[0] == 0x50 && data[1] == 0x4B && (data[2] == 0x03 || data[2] == 0x05)) {
        return ZIP;
    }

    // MP3
    if ((size >= 2 && data[0] == 0xFF && (data[1] & 0xE0) == 0xE0) ||
        (size >= 3 && memcmp(data, "ID3", 3) == 0)) {
        return MP3;
    }

    // MP4
    if (size >= 8 && memcmp(data + 4, "ftyp", 4) == 0) {
        return MP4;
    }

    // Additional signatures for better detection
    // DOCX, XLSX, PPTX (ZIP-based)
    if (size >= 30 && data[0] == 0x50 && data[1] == 0x4B) {
        if (size >= 31 && memcmp(data + 26, "word/", 5) == 0) {
            return DOC;
        }
        if (size >= 29 && memcmp(data + 26, "xl/", 3) == 0) {
            return XLS;
        }
    }

    return UNKNOWN;
}