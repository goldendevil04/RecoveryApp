
#ifndef FILE_RECOVERY_ENGINE_H
#define FILE_RECOVERY_ENGINE_H

#include <vector>
#include <string>
#include <cstdint>

class FileRecoveryEngine {
public:
    FileRecoveryEngine();
    ~FileRecoveryEngine();
    
    std::vector<int> performDeepScan(const char* path, bool isRooted);
    bool detectRootAccess();
    int identifyFileType(const uint8_t* signature, size_t length);
    
private:
    bool checkSuBinary();
    bool checkRootFiles();
    bool checkSystemPartition();
    int detectFileTypeBySignature(const uint8_t* data, size_t size);
};

// File type constants
enum FileType {
    UNKNOWN = 0,
    JPEG = 1,
    PNG = 2,
    GIF = 3,
    MP4 = 4,
    MP3 = 5,
    PDF = 6,
    ZIP = 7,
    DOC = 8,
    DOCX = 9,
    XLS = 10,
    XLSX = 11
};

#endif // FILE_RECOVERY_ENGINE_H
