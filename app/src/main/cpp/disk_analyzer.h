#ifndef DISK_ANALYZER_H
#define DISK_ANALYZER_H

#include <vector>
#include <cstdint>
#include <string>

class DiskAnalyzer {
public:
    DiskAnalyzer();
    ~DiskAnalyzer();
    
    std::vector<uint8_t> carveFile(const uint8_t* sectorData, size_t length);
    bool analyzeMFT(const char* devicePath);
    std::vector<std::string> findDeletedEntries(const char* devicePath);
    
private:
    struct FileCluster {
        uint64_t startSector;
        uint64_t endSector;
        std::string fileType;
        bool isDeleted;
    };
    
    std::vector<FileCluster> identifyClusters(const uint8_t* data, size_t length);
    bool isValidFileHeader(const uint8_t* data, size_t offset);
    std::string detectFileType(const uint8_t* data, size_t offset);
};

#endif // DISK_ANALYZER_H