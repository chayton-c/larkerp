package com.yingda.lkj.utils;

import com.google.gson.*;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author hood  2019/12/13
 */
@SuppressWarnings("unused")
public class JsonUtils {

    /**
     * json转list，不行试试下面两个被注释了的
     */
    public static <T> List<T> parseList(String jsonStr, Class<?> clas) {
        Type type = new ParameterizedTypeImpl(clas);
        return new Gson().fromJson(jsonStr, type);
    }

    /**
     * json转pojo
     */
    public static <T> T parse(String jsonStr, Class<T> tClass) {
        return new Gson().fromJson(jsonStr, tClass);
    }


    private static class ParameterizedTypeImpl implements ParameterizedType {
        private Class<?> clazz;

        public ParameterizedTypeImpl(Class<?> clz) {
            clazz = clz;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return new Type[]{clazz};
        }

        @Override
        public Type getRawType() {
            return List.class;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }

    public static void main(String[] args) {
        // 我的BFEBFBFF000906EA
        // BFEBFBFF000506E3
        try {
            long start = System.currentTimeMillis();
            Process process = Runtime.getRuntime().exec(new String[]{"wmic", "cpu", "get", "ProcessorId"});
            process.getOutputStream().close();
            Scanner sc = new Scanner(process.getInputStream());
            String property = sc.next();
            String serial = sc.next();
            System.out.println(property + ": " + serial);
            System.out.println("time:" + (System.currentTimeMillis() - start));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
