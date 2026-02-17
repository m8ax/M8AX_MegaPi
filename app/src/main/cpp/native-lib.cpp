#include <jni.h>

extern "C" {
int calcular_pi(long int d, int out);
}
extern "C" JNIEXPORT void JNICALL
Java_com_m8ax_1megapi_MainActivity_startCalculation(JNIEnv *env, jobject thiz, jlong cantidad) {
    calcular_pi((long int) cantidad, 1);
}