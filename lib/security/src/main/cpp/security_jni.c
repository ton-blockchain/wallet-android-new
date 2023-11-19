#include <argon2.h>
#include <jni.h>
#include <malloc.h>
#include <string.h>

#define FUNC(FUNC_NAME) Java_org_ton_wallet_lib_security_SecurityUtils_##FUNC_NAME

JNIEXPORT jbyteArray JNICALL
FUNC(nativeGetArgonHash)(
        JNIEnv *env,
        jclass clazz,
        jbyteArray jpassword,
        jbyteArray jsalt,
        jint t_cost,
        jint m_cost,
        jint parallelism,
        jint hash_length
) {
    jbyte *password = (*env)->GetByteArrayElements(env, jpassword, NULL);
    jbyte *salt = (*env)->GetByteArrayElements(env, jsalt, NULL);
    jsize password_length = (*env)->GetArrayLength(env, jpassword);
    jsize salt_length = (*env)->GetArrayLength(env, jsalt);

    char *hash = (char *) malloc(hash_length);
    int result = argon2id_hash_raw(t_cost, m_cost, parallelism, password, password_length, salt, salt_length, hash, hash_length);

    (*env)->ReleaseByteArrayElements(env, jpassword, (jbyte *) password, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, jsalt, (jbyte *) salt, JNI_ABORT);

    if (result == ARGON2_OK) {
        jbyteArray jhash = (*env)->NewByteArray(env, hash_length);
        (*env)->SetByteArrayRegion(env, jhash, 0, hash_length, (jbyte *) hash);
        free(hash);
        return jhash;
    } else {
        return NULL;
    }
}