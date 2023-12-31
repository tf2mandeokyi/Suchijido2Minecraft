#include <jni.h>

#include <vector>
#include <functional>
#include <iterator>

namespace JavaListHandler
{


template<typename CObject> using CObjToJObjFunc = std::function<jobject(CObject)>;
template<typename CObject> using JObjToCObjFunc = std::function<CObject(jobject)>;


template<typename CObject> jobject vectorToJavaList
(
    JNIEnv *env,
    std::vector<CObject> &vector,
    const CObjToJObjFunc<CObject> &func
)
{
    jclass listClazz = env->FindClass("java/util/ArrayList");

    const jmethodID mConstruct = env->GetMethodID(listClazz, "<init>", "()V");
    const jmethodID mAdd = env->GetMethodID(listClazz, "add", "(Ljava/lang/Object;)Z");

    jobject result = env->NewObject(listClazz, mConstruct);
    for(int i = 0; i < vector.size(); ++i)
    {
        jobject element = func(vector[i]);
        env->CallVoidMethod(result, mAdd, element);
    }

    return result;
}


template<typename CObject>
  struct JavaListIterator
  : public std::iterator<std::bidirectional_iterator_tag, CObject>
{
public:
    JavaListIterator(JNIEnv *env, jobject list, const JObjToCObjFunc<CObject> func, int index)
     : m_env(env), m_list(list), m_func(func)
    {
        if(!static_initialized) static_initialize(env);
        m_size = (int) env->CallIntMethod(m_list, list_size);
        m_index = index == -1 ? m_size : index;
    }

    JavaListIterator(const JavaListIterator&) = default;
    JavaListIterator& operator=(const JavaListIterator&) = default;

    CObject operator*() const { return m_func(m_env->CallObjectMethod(m_list, list_get, m_index)); }

    bool operator== (const JavaListIterator& it) { return m_index == it.m_index; }
    bool operator!= (const JavaListIterator& it) { return m_index != it.m_index; }

    JavaListIterator& operator++() { m_index++; return *this; }
    JavaListIterator operator++(int) { JavaListIterator tmp = *this; operator++(); return tmp; }

    JavaListIterator& operator--() { m_index--; return *this; }
    JavaListIterator operator--(int) { JavaListIterator tmp = *this; operator--(); return tmp; }

    typename JavaListIterator::difference_type operator-(const JavaListIterator &it)
    {
        return m_index - it.m_index;
    }

private:
    static bool static_initialized;
    static jclass list_clazz;
    static jmethodID list_size;
    static jmethodID list_get;

    void static_initialize(JNIEnv *env)
    {
        list_clazz = env->FindClass("java/util/List");
        list_size = env->GetMethodID(list_clazz, "size", "()I");
        list_get = env->GetMethodID(list_clazz, "get", "(I)Ljava/lang/Object;");
        static_initialized = true;
    }

    JNIEnv *m_env;
    jobject m_list;
    JObjToCObjFunc<CObject> m_func;
    int m_index;
    int m_size;
};
template<typename CObject> bool JavaListIterator<CObject>::static_initialized = false;
template<typename CObject> jclass JavaListIterator<CObject>::list_clazz = 0;
template<typename CObject> jmethodID JavaListIterator<CObject>::list_size = 0;
template<typename CObject> jmethodID JavaListIterator<CObject>::list_get = 0;

template<typename CObject> typename JavaListIterator<CObject>::difference_type
  distance(JavaListIterator<CObject> first, JavaListIterator<CObject> last)
{
    return last - first;
}



template<typename CObject>
  struct JavaListIteratorWrapper
{
public:
    using Iter = JavaListIterator<CObject>;

    JavaListIteratorWrapper(JNIEnv *env, jobject list, const JObjToCObjFunc<CObject> func)
     : m_env(env), m_list(list), m_func(func) {}

    Iter begin() { return Iter(m_env, m_list, m_func, 0); }
    Iter end() { return Iter(m_env, m_list, m_func, -1); }

private:
    JNIEnv *m_env;
    jobject m_list;
    JObjToCObjFunc<CObject> m_func;
};

} // namespace JavaListHandler