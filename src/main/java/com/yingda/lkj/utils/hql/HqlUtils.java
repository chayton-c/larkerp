package com.yingda.lkj.utils.hql;

/**
 * @author hood  2019/11/8
 */
public class HqlUtils {

    public static String getCountSql(String sql) {
        return getCountSql(sql, "SELECT COUNT(1) ");
    }

    public static String getCountSql(String sql, String head) {
        return head + sql.substring(sql.indexOf("FROM"));
    }

    private static String string2builder(String input) {
        String[] split = input.split("\n");
        StringBuilder result = new StringBuilder("StringBuilder sqlBuilder = new StringBuilder();\n");
        for (String line : split) {
            result.append(String.format("sqlBuilder.append(\"%s\\n\");\n", line));
        }

        return result.toString();
    }

    public static void main(String[] args) {
        System.out.println(string2builder("SELECT\n" +
                "\textendValues.device_id as deviceId,\n" +
                "\textendValues.field_value as fieldValue,\n" +
                "\textendFields.name as fieldName\n" +
                "FROM\n" +
                "\tdevice_extend_values extendValues\n" +
                "\tLEFT JOIN device_extend_field extendFields ON extendFields.id = extendValues.device_field_id\n" +
                "\t"));
    }

}
