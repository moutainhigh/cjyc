package com.cjyc.common.model.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;

/**
 * Java 1.8日期工具类
 * @author JPG
 */
public class LocalDateTimeUtil {


    /**
     * 获取当前时间的LocalDateTime对象
     * LocalDateTime.now();
     *
     * 根据年月日构建LocalDateTime
     * LocalDateTime.of();
     *
     * 比较日期先后
     * LocalDateTime.now().isBefore();
     * LocalDateTime.now().isAfter();
     */
    /**
     * Long转换为LocalDateTime
     * @author JPG
     * @param timeStamp
     * @return
     */
    public static LocalDateTime convertLongToLDT(Long timeStamp) {
        return LocalDateTime.ofInstant(new Date(timeStamp).toInstant(), ZoneId.systemDefault());
    }
    /**
     * Date转换为LocalDateTime
     * @param date 被转换时间
     * @return
     */
    public static LocalDateTime convertDateToLDT(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }



    /**
     * LocalDateTime转换为Date
     * @param localDateTime 被转换时间
     * @return
     */
    public static Date convertLDTToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 获取指定日期的毫秒
     * @param localDateTime 被格式化时间
     * @return
     */
    public static Long getMillisByLDT(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 获取指定日期的秒
     * @param localDateTime 被格式化时间
     * @return
     */
    public static Long getSecondsByLDT(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
    }

    /**
     * 获取当前时间的指定格式
     * @param pattern 格式
     * @return
     */
    public static String formatLDTNow(String pattern) {
        return  formatLDT(LocalDateTime.now(), pattern);
    }
    /**
     * 获取指定时间的指定格式
     * @param localDateTime 被格式化时间
     * @param pattern 格式
     * @return
     */
    public static String formatLDT(LocalDateTime localDateTime,String pattern) {
        return localDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 获取指定时间的指定格式
     * @param pattern 格式
     * @return
     */
    public static String formatLong(Long time,String pattern) {
        return convertLongToLDT(time).format(DateTimeFormatter.ofPattern(pattern));
    }


    /**
     * 日期加上一个数,根据temporalUnit不同加不同值,temporalUnit为ChronoUnit.*
     * @param localDateTime 被加时间
     * @param number 被加数
     * @param temporalUnit 单位(年月日时分秒)
     * @return
     */
    public static LocalDateTime plus(LocalDateTime localDateTime, long number, TemporalUnit temporalUnit) {
        return localDateTime.plus(number, temporalUnit);
    }

    /**
     * 日期减去一个数,根据temporalUnit不同减不同值,temporalUnit参数为ChronoUnit.*
     * @param localDateTime 被减时间
     * @param number 减数
     * @param temporalUnit 单位(年月日时分秒)
     * @return
     */
    public static LocalDateTime minus(LocalDateTime localDateTime, long number, TemporalUnit temporalUnit){
        return localDateTime.minus(number, temporalUnit);
    }

    /**
     * 获取两个日期的差  field参数为ChronoUnit.*
     * @param startLocalDateTime 开始时间
     * @param endLocalDateTime 结束时间
     * @param chronoUnit  单位(年月日时分秒)
     * @return
     */
    public static long betweenTwoLDT(LocalDateTime startLocalDateTime, LocalDateTime endLocalDateTime, ChronoUnit chronoUnit) {
        Period period = Period.between(LocalDate.from(startLocalDateTime), LocalDate.from(endLocalDateTime));
        if (chronoUnit == ChronoUnit.YEARS) {
            return period.getYears();
        }
        if (chronoUnit == ChronoUnit.MONTHS) {
            return period.getYears() * 12 + period.getMonths();
        }
        return chronoUnit.between(startLocalDateTime, endLocalDateTime);
    }

    /**
     * 获取一天的开始时间，2017,7,22 00:00
     * @param localDateTime 参数
     * @return
     */
    public static LocalDateTime getDayStartByLDT(LocalDateTime localDateTime) {
        return localDateTime.withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }

    /**
     * 获取一天的开始时间，2017,7,22 00:00
     * @param timeStamp 长整型时间戳
     * @return
     */
    public static LocalDateTime getDayStartByLong(Long timeStamp) {
        return convertLongToLDT(timeStamp).withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
    }

    /**
     * 获取一天的结束时间，2017,7,22 23:59:59.999999999
     * @param localDateTime 参数
     * @return
     */
    public static LocalDateTime getDayEndByLDT(LocalDateTime localDateTime) {
        return localDateTime.withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(999999999);
    }

    /**
     * 将日期格式的字符串转换为LocalDate
     *
     * @param dateStr
     * @param format
     * @return
     */
    public static LocalDate convertToLocalDate(String dateStr, String format) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(format);
        LocalDate localDate = LocalDate.parse(dateStr, fmt);
        return localDate;
    }

    /**
     * 获取num个月后的日期
     * @param localDate
     * @param num
     * @return
     */
    public static LocalDate getNextMouth(LocalDate localDate,int num){
        return localDate.plusMonths(num);
    }

    /**
     * LocalDate转时间戳
     * @param localDate
     * @return
     */
    public static Long convertToLong(LocalDate localDate){
        return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().getEpochSecond();
    }

    /**
     * 将日期格式的字符串转换为Long
     *
     * @param dateStr
     * @param format
     * @return
     */
    public static Long convertDateStrToLong (String dateStr, String format) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(format);
        LocalDateTime dateTime = LocalDateTime.parse(dateStr, fmt);
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

}
