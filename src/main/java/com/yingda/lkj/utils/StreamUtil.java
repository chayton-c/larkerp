package com.yingda.lkj.utils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author hood  2020/2/5
 */
public class StreamUtil {
    public static <T> Predicate<T> distinct(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    /**
     * 注意去重了
     */
    public static <K, V> List<K> getList(List<V> vList, Function<? super V, K> mapper) {
        return vList.stream().map(mapper).distinct().collect(Collectors.toList());
    }

    public static <K, V, G> Map<K, V> getMap(List<G> gList, Function<? super G, K> keyMapper, Function<? super G, V> valueMapper) {
        return gList.stream().distinct().filter(StreamUtil.distinct(keyMapper)).collect(Collectors.toMap(keyMapper, valueMapper));
    }

    public static <K, V> Map<K, List<V>> groupList(List<V> vList, Function<? super V, K> mapper) {
        return vList.stream().distinct().collect(Collectors.groupingBy(mapper));
    }

    public static void main(String[] args) {
        List<Integer> akagi = List.of(1, 2, 3, 4);
        Map<Double, String> map = getMap(akagi, Integer::doubleValue, x -> x + "akagi");
        System.out.println(map);
    }

}
