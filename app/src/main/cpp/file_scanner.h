#ifndef FILE_SCANNER_H
#define FILE_SCANNER_H

#include <vector>
#include <string>
#include <cstdint>

class FileScanner {
public:
    FileScanner();
    ~FileScanner();
    
    std::vector<int> scanDirectory(const char* path, bool isRooted);
    bool recoverFile(const char* sourcePath, const char* destPath);
    
private:
    struct FileSignature {
        std::vector<uint8_t> signature;
        std::string extension;
        int offset;
    };
    
    std::vector<FileSignature> fileSignatures;
    
    void initializeSignatures();
    bool matchesSignature(const uint8_t* data, const FileSignature& sig);
    std::string identifyFileType(const uint8_t* data, size_t size);
    std::vector<int> scanForDeletedFiles(const char* path, bool isRooted);
};

#endif // FILE_SCANNER_H