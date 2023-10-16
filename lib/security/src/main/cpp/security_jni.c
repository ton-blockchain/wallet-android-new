#include <argon2.h>
#include <jni.h>
#include <malloc.h>
#include <sodium.h>
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

JNIEXPORT jbyteArray JNICALL
FUNC(nativeCryptoBoxInitKeys)(JNIEnv *env, jclass clazz) {
    unsigned char public_key[crypto_box_PUBLICKEYBYTES];
    unsigned char secret_key[crypto_box_SECRETKEYBYTES];
    int ret = crypto_box_keypair(public_key, secret_key);
    if (ret == 0) {
        jbyteArray result = (*env)->NewByteArray(env, crypto_box_PUBLICKEYBYTES + crypto_box_SECRETKEYBYTES);
        (*env)->SetByteArrayRegion(env, result, 0, crypto_box_PUBLICKEYBYTES, (const jbyte *) public_key);
        (*env)->SetByteArrayRegion(env, result, crypto_box_PUBLICKEYBYTES, crypto_box_SECRETKEYBYTES, (const jbyte *) secret_key);
        return result;
    } else {
        return NULL;
    }
}

JNIEXPORT jbyteArray JNICALL
FUNC(nativeCryptoBox)(JNIEnv *env, jclass clazz, jbyteArray jmessage, jbyteArray jpublic_key, jbyteArray jsecret_key) {
    size_t message_length = (*env)->GetArrayLength(env, jmessage);
    jbyte *message = (*env)->GetByteArrayElements(env, jmessage, NULL);
    jbyte *public_key = (*env)->GetByteArrayElements(env, jpublic_key, NULL);
    jbyte *secret_key = (*env)->GetByteArrayElements(env, jsecret_key, NULL);

    uint8_t nonce[crypto_box_NONCEBYTES];
    randombytes(nonce, crypto_box_NONCEBYTES);

    size_t m_padded_size = message_length + crypto_box_ZEROBYTES;
    uint8_t *m_padded = (uint8_t *) malloc(m_padded_size);
    memset(m_padded, 0, crypto_box_ZEROBYTES);
    memcpy(m_padded + crypto_box_ZEROBYTES, message, message_length);

    uint8_t *cipher_padded = (uint8_t *) malloc(m_padded_size);
    int ret = crypto_box(
            cipher_padded,
            m_padded,
            m_padded_size,
            nonce,
            (const unsigned char *) public_key,
            (const unsigned char *) secret_key
    );

    (*env)->ReleaseByteArrayElements(env, jmessage, (jbyte *) message, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, jpublic_key, (jbyte *) public_key, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, jsecret_key, (jbyte *) secret_key, JNI_ABORT);
    free(m_padded);

    jbyteArray result = NULL;
    if (ret == 0) {
        jsize cipher_with_nonce_size = crypto_box_NONCEBYTES + m_padded_size - crypto_box_BOXZEROBYTES;
        result = (*env)->NewByteArray(env, cipher_with_nonce_size);
        if (result) {
            uint8_t *cipher_with_nonce = (uint8_t *) malloc(cipher_with_nonce_size);
            memcpy(cipher_with_nonce, nonce, crypto_box_NONCEBYTES);
            memcpy(cipher_with_nonce + crypto_box_NONCEBYTES, cipher_padded + crypto_box_BOXZEROBYTES, m_padded_size - crypto_box_BOXZEROBYTES);
            (*env)->SetByteArrayRegion(env, result, 0, cipher_with_nonce_size, (jbyte *) cipher_with_nonce);
        }
    }
    free(cipher_padded);

    return result;
}

JNIEXPORT jbyteArray JNICALL
FUNC(nativeCryptoBoxOpen)(JNIEnv *env, jclass clazz, jbyteArray jcipher_with_nonce, jbyteArray jpublic_key, jbyteArray jsecret_key) {
    size_t cipher_with_nonce_length = (*env)->GetArrayLength(env, jcipher_with_nonce);
    jbyte *cipher_with_nonce = (*env)->GetByteArrayElements(env, jcipher_with_nonce, NULL);
    jbyte *public_key = (*env)->GetByteArrayElements(env, jpublic_key, NULL);
    jbyte *secret_key = (*env)->GetByteArrayElements(env, jsecret_key, NULL);

    uint8_t nonce[crypto_box_NONCEBYTES];
    memcpy(nonce, cipher_with_nonce, crypto_box_NONCEBYTES);

    size_t padded_size = cipher_with_nonce_length - crypto_box_NONCEBYTES + crypto_box_BOXZEROBYTES;
    uint8_t *cipher_padded = (uint8_t *) malloc(padded_size);
    memset(cipher_padded, 0, crypto_box_BOXZEROBYTES);
    memcpy(cipher_padded + crypto_box_BOXZEROBYTES, cipher_with_nonce + crypto_box_NONCEBYTES, cipher_with_nonce_length - crypto_box_NONCEBYTES);

    uint8_t plaintext_padded[padded_size];
    int ret = crypto_box_open(plaintext_padded, cipher_padded, padded_size, nonce, (const unsigned char *) public_key, (const unsigned char *) secret_key);
    free(cipher_padded);

    (*env)->ReleaseByteArrayElements(env, jcipher_with_nonce, (jbyte *) cipher_with_nonce, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, jpublic_key, (jbyte *) public_key, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, jsecret_key, (jbyte *) secret_key, JNI_ABORT);

    jbyteArray result = NULL;
    if (ret == 0) {
        size_t result_size = padded_size - crypto_box_ZEROBYTES;
        result = (*env)->NewByteArray(env, (jsize) result_size);
        (*env)->SetByteArrayRegion(env, result, 0, (jsize) result_size, (jbyte *) plaintext_padded + crypto_box_ZEROBYTES);
    }

    return result;
}

JNIEXPORT void JNICALL
FUNC(nativeCryptoBoxTest)(JNIEnv *env, jclass clazz, jbyteArray jmessage, jbyteArray jpublic_key_sender, jbyteArray jsecret_key_sender, jbyteArray jpublic_key_receiver, jbyteArray jsecret_key_receiver) {
    size_t message_length = (*env)->GetArrayLength(env, jmessage);
    jbyte *message = (*env)->GetByteArrayElements(env, jmessage, NULL);
    jbyte *public_key_sender = (*env)->GetByteArrayElements(env, jpublic_key_sender, NULL);
    jbyte *secret_key_sender = (*env)->GetByteArrayElements(env, jsecret_key_sender, NULL);
    jbyte *public_key_receiver = (*env)->GetByteArrayElements(env, jpublic_key_receiver, NULL);
    jbyte *secret_key_receiver = (*env)->GetByteArrayElements(env, jsecret_key_receiver, NULL);

    uint8_t nonce[crypto_box_NONCEBYTES];
    randombytes(nonce, crypto_box_NONCEBYTES);

    size_t m_padded_size = message_length + crypto_box_ZEROBYTES;
    uint8_t *m_padded = (uint8_t *) malloc(m_padded_size);
    memset(m_padded, 0, crypto_box_ZEROBYTES);
    memcpy(m_padded + crypto_box_ZEROBYTES, message, message_length);

    uint8_t *cipher_padded = (uint8_t *) malloc(m_padded_size);
    int box_ret = crypto_box(
            cipher_padded,
            m_padded,
            m_padded_size,
            nonce,
            (const unsigned char *) public_key_receiver,
            (const unsigned char *) secret_key_sender
    );

    uint8_t plaintext_padded[m_padded_size];
    int open_ret = crypto_box_open(
            plaintext_padded,
            cipher_padded,
            m_padded_size,
            nonce,
            (const unsigned char *) public_key_sender,
            (const unsigned char *) secret_key_receiver
    );
    free(cipher_padded);
    free(m_padded);

    (*env)->ReleaseByteArrayElements(env, jmessage, (jbyte *) message, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, jpublic_key_sender, (jbyte *) public_key_sender, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, jsecret_key_sender, (jbyte *) secret_key_sender, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, jpublic_key_receiver, (jbyte *) public_key_receiver, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, jsecret_key_receiver, (jbyte *) secret_key_receiver, JNI_ABORT);
}