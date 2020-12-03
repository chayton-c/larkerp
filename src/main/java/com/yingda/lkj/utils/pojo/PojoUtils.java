package com.yingda.lkj.utils.pojo;

import com.yingda.lkj.beans.entity.backstage.organization.Organization;
import com.yingda.lkj.beans.system.Json;
import com.yingda.lkj.beans.system.JsonMessage;
import com.yingda.lkj.utils.StringUtils;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author hood  2019/12/13
 */
@SuppressWarnings("unused")
public class PojoUtils {

    /**
     * 深拷贝
     */
    public static <K, V> V copyPojo(K k, V v) {
        BeanUtils.copyProperties(k, v);
        return v;
    }

    /**
     * 深拷贝
     * <p>创建包含kList.size()个ClassV实例的list,</p>
     * <p>并调用org.springframework.beans.BeanUtils，</p>
     * <p>复制kList的每一项到list中</p>
     *
     * @param kList  source
     * @param classV 生成的list中每一项的类型
     */
    public static <K, V> List<V> copyPojolList(Iterable<K> kList, Class<V> classV) throws ReflectiveOperationException {
        List<V> vList = new ArrayList<>();
        for (K k : kList)
            vList.add(copyPojo(k, classV.getConstructor().newInstance()));
        return vList;
    }

    public static <T> Map<String, Object> getParams(T t, Map<String, String> conditions) throws IllegalAccessException {
        Map<String, Object> params = new HashMap<>();

        for (Field field : t.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            // 非final 非静态 且 不为逻辑空的字段
            if (!Modifier.isFinal(field.getModifiers()) && !Modifier.isStatic(field.getModifiers()) && StringUtils.isNotEmpty(field.get(t))) {
                if (conditions == null) {
                    params.put(field.getName(), field.get(t));
                    continue;
                }
                if (conditions.containsKey(field.getName())) {
                    String condition = conditions.get(field.getName());
                    params.put(field.getName(), condition.equals("like") ? "%" + field.get(t) + "%" : field.get(t)); // like 查询，参数两旁加 %
                }
            }
        }

        return params;
    }

    /**
     * <p>参数非空判断</p>
     * <p>如果conditions中声明的字段，在checked中为空，那么会返回{msg=**不能为空, success=false, obj=null, errorCode=5001}</p>
     * <p>**为conditions的value</p>
     *
     * @param checked 被检查的pojo
     * @param conditions key:字段，value:字段名 如 {userName=用户名, addTime=添加时间}
     * @throws ReflectiveOperationException 如果conditions中声明的字段，在checked中没有这个字段，会抛出NoSuchFieldException
     */
    public static <T> Json checkParams(T checked, Map<String, String> conditions) throws ReflectiveOperationException {
        Map<String, Object> params = new HashMap<>();

        for (Map.Entry<String, String> entry : conditions.entrySet()) {
            String conditionKey = entry.getKey();
            String conditionValue = entry.getValue();

            Field declaredField = checked.getClass().getDeclaredField(conditionKey);
            declaredField.setAccessible(true);

            if (StringUtils.isEmpty(declaredField.get(checked)))
                return new Json(JsonMessage.PARAM_INVALID, conditionValue + "不能为空");

        }
        return new Json(JsonMessage.SUCCESS);
    }

    public static void main(String[] args) throws ReflectiveOperationException {
        Organization organization = new Organization();
        System.out.println(StringUtils.isEmpty(organization.getLevel()));
        organization.setHasSlave(true);
        organization.setName("akagi");

        System.out.println(getParams(organization, null));
        System.out.println(checkParams(organization, Map.of("name", "姓名", "addTime", "添加时间")));
    }

    /**
     * 判断是否是自定义class
     */
    private static boolean isJavaClass(Class<?> clz) {
        return clz != null && clz.getClassLoader() == null;
    }

}
