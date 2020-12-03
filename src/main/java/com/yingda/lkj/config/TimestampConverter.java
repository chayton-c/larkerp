package com.yingda.lkj.config;


import com.yingda.lkj.utils.StringUtils;
import com.yingda.lkj.utils.date.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.ParseException;

/**
 * @author hood  2020/1/3
 */
@Component
public class TimestampConverter implements Converter<String, Timestamp> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimestampConverter.class);

    private static final String YYYY_MM = "yyyy-MM";
    private static final String YYYY_MM_DD = "yyyy-MM-dd";
    private static final String YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    private static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    @Override
    public Timestamp convert(String source) {
        if (StringUtils.isEmpty(source))
            return null;

        source = source.trim();
        try {
            if (source.matches("^\\d{4}-\\d{1,2}$"))
                return DateUtil.toTimestamp(source, YYYY_MM);

            if (source.matches("^\\d{4}-\\d{1,2}-\\d{1,2}$"))
                return DateUtil.toTimestamp(source, YYYY_MM_DD);

            if (source.matches("^\\d{4}-\\d{1,2}-\\d{1,2} {1}\\d{1,2}:\\d{1,2}$"))
                return DateUtil.toTimestamp(source, YYYY_MM_DD_HH_MM);

            if (source.matches("^\\d{4}-\\d{1,2}-\\d{1,2} {1}\\d{1,2}:\\d{1,2}:\\d{1,2}$"))
                return DateUtil.toTimestamp(source, YYYY_MM_DD_HH_MM_SS);

            throw new IllegalArgumentException("TimestampConverter： Invalid false value '" + source + "'");
        } catch (ParseException e) {
            throw new IllegalArgumentException("TimestampConverter: " + e.getLocalizedMessage());
        }
    }
}
