
#ifndef FILE_SCANNER_H
#define FILE_SCANNER_H

#include <string>
#include <vector>

class FileScanner {
public:
    FileScanner();
    ~FileScanner();
    
    std::vector<std::string> scanDirectory(const std::string& path, bool recursive = true);
    std::vector<std::string> findDeletedFiles(const std::string& path);
    bool isFileRecoverable(const std::string& filePath);
    size_t getFileSize(const std::string& filePath);
};

#endif // FILE_SCANNER_H
