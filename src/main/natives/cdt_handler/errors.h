#pragma once

#include <jni.h>
#include <exception>
#include <stdexcept>

namespace Error2Java
{

jint throwExceptionForJava(JNIEnv *env, const char* className, const char* what)
{
    jclass exceptionClass = env->FindClass(className);
    return env->ThrowNew(exceptionClass, what);
}

jint throwRuntimeException(JNIEnv *env, const std::runtime_error& e)
{
    return throwExceptionForJava(env, "java/lang/RuntimeException", e.what());
}

}