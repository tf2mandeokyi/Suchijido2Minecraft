#include "triangulation.h"

namespace CDTHandler
{

jfieldID getTriangulationPointerFieldID(JNIEnv *env, jobject thisObject)
{
    jclass clazz = env->GetObjectClass(thisObject);
    return env->GetFieldID(clazz, "triangulationPointer", "J");
}


CDT::Triangulation<double>* getTriangulationObject(JNIEnv *env, jobject thisObject)
{
    jfieldID triangulationPointerFID = CDTHandler::getTriangulationPointerFieldID(env, thisObject);
    int64_t triangulationPointer = (int64_t) env->GetLongField(thisObject, triangulationPointerFID);

    return (CDT::Triangulation<double>*) triangulationPointer;
}


jobject toVector2DH(JNIEnv *env, CDT::V2d<double> v2d)
{
    jclass clazz = env->FindClass("com/mndk/scjdmc/util/math/Vector2DH");
    jmethodID constructorMethod = env->GetMethodID(clazz, "<init>", "(DD)V");
    return env->NewObject(clazz, constructorMethod, v2d.x, v2d.y);
}


CDT::V2d<double> toCDTV2d(JNIEnv *env, jobject v2d)
{
    jclass clazz = env->GetObjectClass(v2d);
    jfieldID x_field = env->GetFieldID(clazz, "x", "D"), z_field = env->GetFieldID(clazz, "z", "D");
    double x = env->GetDoubleField(v2d, x_field), z = env->GetDoubleField(v2d, z_field);
    return CDT::V2d<double>{ x, z };
}


CDT::Edge toCDTEdge(JNIEnv *env, jobject indexedEdge)
{
    jclass clazz = env->GetObjectClass(indexedEdge);
    jfieldID v0_field = env->GetFieldID(clazz, "v0", "I"), v1_field = env->GetFieldID(clazz, "v1", "I");
    unsigned int v0 = env->GetIntField(indexedEdge, v0_field), v1 = env->GetIntField(indexedEdge, v1_field);
    return CDT::Edge{ v0, v1 };
}


jobject toJavaIndexedTriangle(JNIEnv *env, CDT::Triangle triangle)
{
    jclass clazz = env->FindClass("com/mndk/scjdmc/cdtlib/IndexedTriangle");
    jmethodID constructorMethod = env->GetMethodID(clazz, "<init>", "(IIIIII)V");
    return env->NewObject(clazz, constructorMethod,
            triangle.vertices [0], triangle.vertices [1], triangle.vertices [2],
            triangle.neighbors[0], triangle.neighbors[1], triangle.neighbors[2]
    );
}

}
