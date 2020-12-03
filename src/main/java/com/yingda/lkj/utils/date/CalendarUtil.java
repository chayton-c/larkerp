package com.yingda.lkj.utils.date;

import com.yingda.lkj.utils.JsonUtils;

import javax.xml.crypto.Data;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author hood  2019/12/4
 */
@SuppressWarnings("unused")
public class CalendarUtil {

    public static Timestamp getBeginningOfTheYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return new Timestamp(calendar.getTimeInMillis());
    }

    public static Timestamp getEndOfTheYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_YEAR, calendar.getMaximum(Calendar.DAY_OF_YEAR));
        calendar.set(Calendar.HOUR, calendar.getMaximum(Calendar.HOUR));
        calendar.set(Calendar.MINUTE, calendar.getMaximum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getMaximum(Calendar.SECOND));
        return new Timestamp(calendar.getTimeInMillis());
    }

    public static int getYear(Timestamp timestamp) {
        return getYear(new Date(timestamp.getTime()));
    }

    public static int getMonth(Timestamp timestamp) {
        return getMonth(new Date(timestamp.getTime()));
    }

    public static int getDate(Timestamp timestamp) {
        return getDate(new Date(timestamp.getTime()));
    }

    public static int getDayOfYear(Timestamp timestamp) {
        return getDayOfYear(new Date(timestamp.getTime()));
    }

    public static int getHour(Timestamp timestamp) {
        return getHourOfDay(new Date(timestamp.getTime()));
    }

    public static int getYear(Date date) {
        Calendar calender = Calendar.getInstance();
        calender.setTime(date);
        return calender.get(Calendar.YEAR);
    }

    /**
     * 这里加一了，即date为 2019-01-01会返回1，不会返回0
     */
    public static int getMonth(Date date) {
        Calendar calender = Calendar.getInstance();
        calender.setTime(date);
        return calender.get(Calendar.MONTH) + 1;
    }

    public static int getDate(Date date) {
        Calendar calender = Calendar.getInstance();
        calender.setTime(date);
        return calender.get(Calendar.DATE);
    }

    public static int getDayOfYear(Date date) {
        Calendar calender = Calendar.getInstance();
        calender.setTime(date);
        return calender.get(Calendar.DAY_OF_YEAR);
    }

    public static int getHourOfDay(Date date) {
        Calendar calender = Calendar.getInstance();
        calender.setTime(date);
        return calender.get(Calendar.HOUR_OF_DAY);
    }

    public static Date getMonthFirstDay() {
        return getMonthFirstDay(new Date());
    }

    public static Date getMonthFirstDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.MONTH, 0);
        return calendar.getTime();
    }

    public static Date getMonthLastDay() {
        return getMonthLastDay(new Date());
    }

    public static Date getMonthLastDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 0);
        calendar.add(Calendar.MONTH, 1);
        return calendar.getTime();
    }

    public static Date getNextMonthFirstDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.MONTH, 1);
        return calendar.getTime();
    }

    public static Date getNextMonthLastDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 0);
        calendar.add(Calendar.MONTH, 2);

        return calendar.getTime();
    }

    public static Date getNextMonthFirstDay() {
        return getNextMonthFirstDay(new Date());
    }

    public static Date getNextMonthLastDay() {
        return getNextMonthLastDay(new Date());
    }

    public static boolean isLeapYear(Date date) {
        int year = getYear(date);
        return isLeapYear(year);
    }

    public static boolean isLeapYear(Timestamp timestamp) {
        int year = getYear(timestamp);
        return isLeapYear(year);
    }

    public static boolean isLeapYear(int year) {
        if (year % 4 == 0 && year % 100 != 0)
            return true;
        return year % 400 == 0;
    }

    public static void main(String[] args) {
        System.out.println(getBeginningOfTheYear());

    }

}
