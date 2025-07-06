#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include "file_scanner.h"
#include "disk_analyzer.h"

#define LOG_TAG "DataRescue"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jintArray JNICALL
Java_com_coderx_datarescuepro_core_recovery_FileRecoveryEngine_nativeDeepScan(
        JNIEnv *env,
        jobject thiz,
        jstring path,
        jboolean isRooted) {
    
    const char *nativePath = env->GetStringUTFChars(path, 0);
    LOGI("Starting deep scan on path: %s, Root: %d", nativePath, isRooted);
    
    FileScanner scanner;
    std::vector<int> results = scanner.scanDirectory(nativePath, isRooted);
    
    jintArray resultArray = env->NewIntArray(results.size());
    env->SetIntArrayRegion(resultArray, 0, results.size(), results.data());
    
    env->ReleaseStringUTFChars(path, nativePath);
    return resultArray;
}

JNIEXPORT jbyteArray JNICALL
Java_com_coderx_datarescuepro_core_recovery_FileRecoveryEngine_nativeFileCarving(
        JNIEnv *env,
        jobject thiz,
        jbyteArray sectors) {
    
    jsize length = env->GetArrayLength(sectors);
    jbyte *sectorData = env->GetByteArrayElements(sectors, nullptr);
    
    LOGI("Starting file carving on %d bytes", length);
    
    DiskAnalyzer analyzer;
    std::vector<uint8_t> recoveredData = analyzer.carveFile(
        reinterpret_cast<uint8_t*>(sectorData), length);
    
    jbyteArray result = env->NewByteArray(recoveredData.size());
    env->SetByteArrayRegion(result, 0, recoveredData.size(), 
                           reinterpret_cast<jbyte*>(recoveredData.data()));
    
    env->ReleaseByteArrayElements(sectors, sectorData, 0);
    return result;
}

JNIEXPORT jboolean JNICALL
Java_com_coderx_datarescuepro_core_recovery_FileRecoveryEngine_nativeRecoverFile(
        JNIEnv *env,
        jobject thiz,
        jstring filePath,
        jstring destinationPath) {
    
    const char *nativeFilePath = env->GetStringUTFChars(filePath, 0);
    const char *nativeDestPath = env->GetStringUTFChars(destinationPath, 0);
    
    LOGI("Recovering file: %s to %s", nativeFilePath, nativeDestPath);
    
    FileScanner scanner;
    bool success = scanner.recoverFile(nativeFilePath, nativeDestPath);
    
    env->ReleaseStringUTFChars(filePath, nativeFilePath);
    env->ReleaseStringUTFChars(destinationPath, nativeDestPath);
    
    return success;
}

}