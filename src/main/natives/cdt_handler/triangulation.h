#pragma once

#include <jni.h>
#include <CDTUtils.h>
#include <Triangulation.h>

namespace CDTHandler
{
    /** Get triangulation pointer field */
    jfieldID getTriangulationPointerFieldID(JNIEnv*, jobject);

    /** Get triangulation object */
    CDT::Triangulation<double>* getTriangulationObject(JNIEnv*, jobject);

    /** CDT::V2d<double> to Java Vector2DH */
    jobject toVector2DH(JNIEnv*, CDT::V2d<double>);

    /** Java V2d to CDT::V2d<double> */
    CDT::V2d<double> toCDTV2d(JNIEnv*, jobject);

    /** Java IndexedEdge to CDT::Edge */
    CDT::Edge toCDTEdge(JNIEnv*, jobject);

    /** CDT::Triangle to Java IndexedTriangle */
    jobject toJavaIndexedTriangle(JNIEnv*, CDT::Triangle);
}
