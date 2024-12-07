#include <jni.h>
#include <string>
extern "C" JNIEXPORT jstring JNICALL
Java_com_mtai_metronome_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = std::string("Hello world");
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_mtai_metronome_MainActivity_callJNI(
        JNIEnv* env,
        jobject /* this */,jint bmp,jstring beats) {
    const char* beats_cstr = env->GetStringUTFChars(beats, nullptr);
    std::string bmp_str = std::to_string(bmp);

    std::string result = bmp_str + "_" + beats_cstr;
    env->ReleaseStringUTFChars(beats,beats_cstr);
    return env->NewStringUTF(result.c_str());
}

