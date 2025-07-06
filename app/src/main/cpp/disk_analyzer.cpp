#include "disk_analyzer.h"
#include <android/log.h>
#include <algorithm>
#include <cstring>

#define LOG_TAG "DiskAnalyzer"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

DiskAnalyzer::DiskAnalyzer() {
}

DiskAnalyzer::~DiskAnalyzer() {
}

std::vector<uint8_t> DiskAnalyzer::carveFile(const uint8_t* sectorData, size_t length) {
    LOGI("Starting file carving on %zu bytes", length);
    
    std::vector<uint8_t> carvedData;
    std::vector<FileCluster> clusters = identifyClusters(sectorData, length);
    
    for (const auto& cluster : clusters) {
        if (cluster.isDeleted) {
            // Extract data from deleted cluster
            size_t startOffset = cluster.startSector * 512; // Assuming 512-byte sectors
            size_t endOffset = cluster.endSector * 512;
            
            if (endOffset <= length) {
                carvedData.insert(carvedData.end(),
                                sectorData + startOffset,
                                sectorData + endOffset);
            }
        }
    }
    
    LOGI("File carving completed. Recovered %zu bytes", carvedData.size());
    return carvedData;
}

std::vector<DiskAnalyzer::FileCluster> DiskAnalyzer::identifyClusters(
    const uint8_t* data, size_t length) {
    
    std::vector<FileCluster> clusters;
    const size_t sectorSize = 512;
    
    for (size_t offset = 0; offset < length; offset += sectorSize) {
        if (isValidFileHeader(data, offset)) {
            FileCluster cluster;
            cluster.startSector = offset / sectorSize;
            cluster.fileType = detectFileType(data, offset);
            cluster.isDeleted = true; // Assume deleted for carving
            
            // Find end of file (simplified)
            size_t endOffset = offset + sectorSize;
            while (endOffset < length && endOffset < offset + (1024 * 1024)) { // Max 1MB
                endOffset += sectorSize;
            }
            
            cluster.endSector = endOffset / sectorSize;
            clusters.push_back(cluster);
        }
    }
    
    return clusters;
}

bool DiskAnalyzer::isValidFileHeader(const uint8_t* data, size_t offset) {
    if (offset + 8 >= 1024 * 1024) return false; // Safety check
    
    // Check for common file signatures
    const uint8_t* header = data + offset;
    
    // JPEG
    if (header[0] == 0xFF && header[1] == 0xD8 && header[2] == 0xFF) {
        return true;
    }
    
    // PNG
    if (header[0] == 0x89 && header[1] == 0x50 && 
        header[2] == 0x4E && header[3] == 0x47) {
        return true;
    }
    
    // PDF
    if (header[0] == 0x25 && header[1] == 0x50 && 
        header[2] == 0x44 && header[3] == 0x46) {
        return true;
    }
    
    return false;
}

std::string DiskAnalyzer::detectFileType(const uint8_t* data, size_t offset) {
    const uint8_t* header = data + offset;
    
    // JPEG
    if (header[0] == 0xFF && header[1] == 0xD8 && header[2] == 0xFF) {
        return "jpg";
    }
    
    // PNG
    if (header[0] == 0x89 && header[1] == 0x50 && 
        header[2] == 0x4E && header[3] == 0x47) {
        return "png";
    }
    
    // PDF
    if (header[0] == 0x25 && header[1] == 0x50 && 
        header[2] == 0x44 && header[3] == 0x46) {
        return "pdf";
    }
    
    return "unknown";
}

bool DiskAnalyzer::analyzeMFT(const char* devicePath) {
    LOGI("Analyzing MFT for device: %s", devicePath);
    // MFT analysis implementation would go here
    // This is a complex operation requiring root access
    return false;
}

std::vector<std::string> DiskAnalyzer::findDeletedEntries(const char* devicePath) {
    LOGI("Finding deleted entries for device: %s", devicePath);
    std::vector<std::string> deletedFiles;
    
    // Implementation for finding deleted directory entries
    // This would require parsing file system structures
    
    return deletedFiles;
}