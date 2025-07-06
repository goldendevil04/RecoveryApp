
#ifndef DISK_SCANNER_H
#define DISK_SCANNER_H

#include <string>
#include <vector>
#include <cstdint>

enum class FileType {
    UNKNOWN,
    JPEG,
    PNG,
    GIF,
    MP4,
    MP3,
    PDF,
    ZIP,
    DOC,
    DOCX
};

struct FileCluster {
    uint32_t startSector;
    uint32_t endSector;
    size_t size;
    bool isDeleted;
};

struct FileData {
    uint8_t* data;
    size_t size;
    bool isValid;
    
    FileData() : data(nullptr), size(0), isValid(false) {}
    ~FileData() {
        delete[] data;
    }
};

class DiskScanner {
public:
    DiskScanner();
    ~DiskScanner();
    
    bool mountFileSystem(const std::string& device);
    std::vector<FileCluster> scanDeletedClusters();
    FileData reconstructFile(const FileCluster& cluster);
    FileType identifyBySignature(const uint8_t* data, size_t size);
};

#endif // DISK_SCANNER_H
