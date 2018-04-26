#include <jni.h>

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_test_workstation_sensorsreader_DataCollectonService_getAv(JNIEnv *env, jobject instance,
                                                                    jobject dataList, jint size) {


    static jclass java_util_ArrayList;
    jmethodID java_util_ArrayList_size;
    jmethodID java_util_ArrayList_get;
    java_util_ArrayList = static_cast<jclass>(env->NewGlobalRef(
            env->FindClass("java/util/ArrayList")));
    java_util_ArrayList_size = env->GetMethodID(java_util_ArrayList, "size", "()I");
    java_util_ArrayList_get = env->GetMethodID(java_util_ArrayList, "get", "(I)Ljava/lang/Object;");

    jint len = env->CallIntMethod(dataList, java_util_ArrayList_size);

    jfloat average[size];
    for (int j = 0; j < size; j++) {
        average[j] = 0;
    }

    for (jint i = 1; i < len - 1; i++) {
        jfloatArray element = static_cast<jfloatArray>(
                env->CallObjectMethod(dataList, java_util_ArrayList_get, i));

        jfloat* buf = env->GetFloatArrayElements(element,0);

        for (int j = 0; j < size; j++) {
            average[j] = average[j] + buf[j];
        }
    }


    for (int j = 0; j < size; j++) {
        average[j] = average[j] / (len - 2);
    }

    jfloatArray result = env->NewFloatArray(size);
    env->SetFloatArrayRegion(result, 0, size, average);


    return result;
}