package com.cjyc.common.model.util;

import com.cjyc.common.model.constant.TimePatternConstant;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Objects;

/**
 * @ClassName DataLongSerizlizer
 * @Description: 时间返回类型处理为Long类型
 * @Author dongy
 * @Date 2019/9/25 13:36
 **/
public class DataLongSerizlizer extends JsonSerializer<Long> {
    @Override
    public void serialize(Long date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        //将毫秒值转换成秒变成char型数据返回
        if(!Objects.isNull(date)){
            String dateStr = LocalDateTimeUtil.formatLDT(LocalDateTimeUtil.getDayStartByLong(date), TimePatternConstant.COMPLEX_TIME_FORMAT);
            jsonGenerator.writeString(dateStr);
        }else{
            jsonGenerator.writeString("");
        }
    }
}
