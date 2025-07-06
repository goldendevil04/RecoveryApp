
#include <jni.h>
#include <string>
#include <android/log.h>
#include <fstream>
#include <vector>
#include <dirent.h>
#include <sys/stat.h>
#include <unistd.h>
#include "file_recovery_engine.h"

#define LOG_TAG "DataRescuePro"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_coderx_datarescuepro_core_FileRecoveryEngine_nativeGetVersion(
        JNIEnv *env,
        jobject /* this */) {
    std::string version = "DataRescue Pro Native v2.0 - Enhanced Recovery";
    return env->NewStringUTF(version.c_str());
}

extern "C" JNIEXPORT jintArray JNICALL
Java_com_coderx_datarescuepro_core_FileRecoveryEngine_nativeDeepScan(
        JNIEnv *env,
        jobject /* this */,
        jstring path,
        jboolean isRooted) {
    
    const char* pathStr = env->GetStringUTFChars(path, nullptr);
    LOGI("Starting enhanced deep scan on path: %s, rooted: %d", pathStr, isRooted);
    
    FileRecoveryEngine engine;
    std::vector<int> results = engine.performEnhancedScan(pathStr, isRooted);
    
    env->ReleaseStringUTFChars(path, pathStr);
    
    jintArray resultArray = env->NewIntArray(results.size());
    env->SetIntArrayRegion(resultArray, 0, results.size(), results.data());
    
    return resultArray;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_coderx_datarescuepro_core_FileRecoveryEngine_nativeDetectRoot(
        JNIEnv *env,
        jobject /* this */) {
    
    FileRecoveryEngine engine;
    bool isRooted = engine.detectRootAccess();
    
    LOGI("Root detection result: %d", isRooted);
    return static_cast<jboolean>(isRooted);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_coderx_datarescuepro_core_FileRecoveryEngine_nativeIdentifyFileType(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray signature) {
    
    jsize length = env->GetArrayLength(signature);
    jbyte* bytes = env->GetByteArrayElements(signature, nullptr);
    
    FileRecoveryEngine engine;
    int fileType = engine.identifyFileType(reinterpret_cast<uint8_t*>(bytes), length);
    
    env->ReleaseByteArrayElements(signature, bytes, JNI_ABORT);
    
    return fileType;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_coderx_datarescuepro_core_FileRecoveryEngine_nativeRecoverDeletedFile(
        JNIEnv *env,
        jobject /* this */,
        jstring filePath) {
    
    const char* pathStr = env->GetStringUTFChars(filePath, nullptr);
    LOGI("Attempting to recover deleted file: %s", pathStr);
    
    FileRecoveryEngine engine;
    std::vector<uint8_t> recoveredData = engine.recoverDeletedFile(pathStr);
    
    env->ReleaseStringUTFChars(filePath, pathStr);
    
    if (recoveredData.empty()) {
        return nullptr;
    }
    
    jbyteArray result = env->NewByteArray(recoveredData.size());
    env->SetByteArrayRegion(result, 0, recoveredData.size(), 
                           reinterpret_cast<const jbyte*>(recoveredData.data()));
    
    return result;
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_coderx_datarescuepro_core_FileRecoveryEngine_nativeScanFreeClusters(
        JNIEnv *env,
        jobject /* this */,
        jstring devicePath) {
    
    const char* pathStr = env->GetStringUTFChars(devicePath, nullptr);
    LOGI("Scanning free clusters on device: %s", pathStr);
    
    FileRecoveryEngine engine;
    std::vector<std::string> clusters = engine.scanFreeClusters(pathStr);
    
    env->ReleaseStringUTFChars(devicePath, pathStr);
    
    jobjectArray result = env->NewObjectArray(clusters.size(), 
                                             env->FindClass("java/lang/String"), 
                                             env->NewStringUTF(""));
    
    for (size_t i = 0; i < clusters.size(); i++) {
        env->SetObjectArrayElement(result, i, env->NewStringUTF(clusters[i].c_str()));
    }
    
    return result;
}
