
#ifndef FILE_RECOVERY_ENGINE_H
#define FILE_RECOVERY_ENGINE_H

#include <vector>
#include <string>
#include <cstdint>

// File type constants
enum FileType {
    UNKNOWN = 0,
    JPEG = 1,
    PNG = 2,
    GIF = 3,
    PDF = 4,
    ZIP = 5,
    MP3 = 6,
    MP4 = 7,
    DOC = 8,
    XLS = 9
};

class FileRecoveryEngine {
public:
    FileRecoveryEngine();
    ~FileRecoveryEngine();

    // Legacy methods
    std::vector<int> performDeepScan(const char* path, bool isRooted);
    bool detectRootAccess();
    int identifyFileType(const uint8_t* signature, size_t length);

    // Enhanced methods
    std::vector<int> performEnhancedScan(const char* path, bool isRooted);
    std::vector<uint8_t> recoverDeletedFile(const char* filePath);
    std::vector<std::string> scanFreeClusters(const char* devicePath);

private:
    // Enhanced scanning methods
    std::vector<int> scanForDeletedEntries(const char* path, bool isRooted);
    std::vector<int> scanForFileSignatures(const char* path);
    std::vector<int> scanSystemAreas(const char* basePath);

    // Recovery methods
    std::vector<uint8_t> recoverFromJournal(const char* filePath);
    bool isFileDeleted(const char* filePath);
    bool isFileCorrupted(const char* filePath);

    // Root detection methods
    bool checkSuBinary();
    bool checkRootFiles();
    bool checkSystemPartition();

    // File type detection
    int detectFileTypeBySignature(const uint8_t* data, size_t size);
};

#endif // FILE_RECOVERY_ENGINE_H
