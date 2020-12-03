package com.yingda.lkj.utils.pojo;

import com.yingda.lkj.beans.pojo.device.Semaphore;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author hood  2019/12/24
 */
public class CollectUtils {


    public static <K, V> Map<K, List<V>> groupMap(Function<? super V, K> keyExtractor, List<V> vList) {
        if (vList.isEmpty())
            return new HashMap<>();

        Map<K, List<V>> result = new LinkedHashMap<>();

        for (V v : vList) {
            K k = keyExtractor.apply(v);
            List<V> collect = result.get(k);
            if (collect == null)
                collect = new ArrayList<>();

            collect.add(v);
            result.put(k, collect);
        }

        return result;
    }

    public static <K, V> List<List<V>> groupList(Function<? super V, K> keyExtractor, List<V> vList) {
        if (vList.isEmpty())
            return new ArrayList<>();

        Map<K, List<V>> kListMap = groupMap(keyExtractor, vList);

        return new ArrayList<>(kListMap.values());
    }

    /**
     *
     * model List<G> 转为Map, key为model G中 keyFieldName对应的字段， value为List<G>的每一项(即model G)
     *
     * @param rawList model List 如List<JzUser>
     * @param modelClass model的类型
     * @param keyFieldName key的字段名
     * @return List<G> -> Map<K, V>
     */
    public static <K, V> Map<K, V> modelToMap(Collection<V> rawList, Class<V> modelClass, String keyFieldName) throws ReflectiveOperationException {
        return modelToMap(rawList, modelClass, keyFieldName, "");
    }

    /**
     *
     * <p>model List<G> 转为Map, key为model G中 keyFieldName对应的字段， value为G中 valueFieldName对应的字段</p>
     * <p>如果valueFieldName为空，表示把整个model作为value</p>
     *
     * @param rawList model List 如List<JzUser>
     * @param modelClass model的类型
     * @param keyFieldName key的字段名
     * @param valueFieldName value的字段名，如果valueFieldName为空，表示把整个model作为value
     * @return List<G> -> Map<K, V>
     */
    @SuppressWarnings("unchecked")
    public static <K, V, G> Map<K, V> modelToMap(Collection<G> rawList, Class<G> modelClass, String keyFieldName, String valueFieldName) throws ReflectiveOperationException {
        if (rawList == null)
            return null;
        if (rawList.size() == 0)
            return new HashMap<>();

        Map<K, V> wrappedMap = new HashMap<>();

        for (G g : rawList) {
            Field field = modelClass.getDeclaredField(keyFieldName);
            field.setAccessible(true);
            K key = (K) field.get(g);

            // 如果valueFieldName为空，把整个model作为value
            if (valueFieldName == null || valueFieldName.equals("")) {
                wrappedMap.put(key, (V) g);
                continue;
            }

            field = modelClass.getDeclaredField(valueFieldName);
            field.setAccessible(true);
            V value = (V) field.get(g);
            wrappedMap.put(key, value);
        }

        return wrappedMap;
    }

    /**
     *
     * <p>model List<K> 转为List<V>, V为model K中 keyFieldName对应的字段的值</p>
     * <p>如果valueFieldName为空，表示把整个model作为value</p>
     *
     * @param rawList model List 如List<JzUser>
     * @param modelClass model的类型
     * @param fieldName V的字段名
     */
    @SuppressWarnings("unchecked")
    public static <K, V> List<V> modelToList(Collection<K> rawList, Class<K> modelClass, String fieldName) throws ReflectiveOperationException {
        List<V> wrappedList  = new ArrayList<>();

        for (K k : rawList) {
            Field field = modelClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            V fieldValue = (V) field.get(k);
            wrappedList.add(fieldValue);
        }

        return wrappedList;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void setLength(List raw, int length) {
        for (int i = 0; i < length; i++)
            raw.add(new Object());
    }
}
