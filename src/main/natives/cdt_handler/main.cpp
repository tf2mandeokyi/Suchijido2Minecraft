#include <cstdio>
#include <vector>

#include <CDTUtils.h>
#include <Triangulation.h>

#include "main.h"
#include "triangulation.h"
#include "javalist.h"


/** Triangulation::new() */
JNIEXPORT void JNICALL Java_com_mndk_scjdmc_cdtlib_Triangulation_construct__
  (JNIEnv *env, jobject thisObject)
{
    jfieldID triangulationPointerFID = CDTHandler::getTriangulationPointerFieldID(env, thisObject);

    CDT::Triangulation<double> *cdt = new CDT::Triangulation<double>();
    env->SetLongField(thisObject, triangulationPointerFID, (jlong) cdt);
}


/** Triangulation::getVertices() */
JNIEXPORT jobject JNICALL Java_com_mndk_scjdmc_cdtlib_Triangulation_getVertices
  (JNIEnv *env, jobject thisObject)
{
    CDT::Triangulation<double> *cdt = CDTHandler::getTriangulationObject(env, thisObject);
    return JavaListHandler::vectorToJavaList<CDT::V2d<double>>(
        env,
        cdt->vertices,
        [env](CDT::V2d<double> v2d) -> jobject { return CDTHandler::toVector2DH(env, v2d); }
    );
}


/** Triangulation::getTriangles() */
JNIEXPORT jobject JNICALL Java_com_mndk_scjdmc_cdtlib_Triangulation_getTriangles
  (JNIEnv *env, jobject thisObject)
{
    CDT::Triangulation<double> *cdt = CDTHandler::getTriangulationObject(env, thisObject);
    return JavaListHandler::vectorToJavaList<CDT::Triangle>(
        env,
        cdt->triangles,
        [env](CDT::Triangle triangle) -> jobject { return CDTHandler::toJavaIndexedTriangle(env, triangle); }
    );
}


/** Triangulation::insertVertices(List<V2d> vertices) */
JNIEXPORT void JNICALL Java_com_mndk_scjdmc_cdtlib_Triangulation_insertVertices
  (JNIEnv *env, jobject thisObject, jobject vertices)
{
    CDT::Triangulation<double> *cdt = CDTHandler::getTriangulationObject(env, thisObject);

    auto itWrapper = JavaListHandler::JavaListIteratorWrapper<CDT::V2d<double>>(
        env,
        vertices,
        [env](jobject obj) -> CDT::V2d<double> { return CDTHandler::toCDTV2d(env, obj); }
    );

    cdt->insertVertices(
        itWrapper.begin(), itWrapper.end(), CDT::getX_V2d<double>, CDT::getY_V2d<double>
    );
}


/** Triangulation::insertEdges(List<IndexedEdge> edges) */
JNIEXPORT void JNICALL Java_com_mndk_scjdmc_cdtlib_Triangulation_insertEdges
  (JNIEnv *env, jobject thisObject, jobject edges)
{
    CDT::Triangulation<double> *cdt = CDTHandler::getTriangulationObject(env, thisObject);

    auto itWrapper = JavaListHandler::JavaListIteratorWrapper<CDT::Edge>(
        env,
        edges,
        [env](jobject obj) -> CDT::Edge { return CDTHandler::toCDTEdge(env, obj); }
    );

    cdt->insertEdges(
        itWrapper.begin(), itWrapper.end(), CDT::edge_get_v1, CDT::edge_get_v2
    );
}


/** Triangulation::eraseSuperTriangle() */
JNIEXPORT void JNICALL Java_com_mndk_scjdmc_cdtlib_Triangulation_eraseSuperTriangle
  (JNIEnv *env, jobject thisObject)
{
    CDT::Triangulation<double> *cdt = CDTHandler::getTriangulationObject(env, thisObject);
    cdt->eraseSuperTriangle();
}