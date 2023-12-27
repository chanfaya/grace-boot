package com.grace.framework.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.ser.std.NumberSerializer;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 数值类型序列化
 *
 * <p>
 * Java中，Long类型占64位二进制bit，最大值长度约19位
 * 而Js中，由于Number类型的值包含了小数，最大值长度约16位
 * 因此当Java返回超过16位的Long型字段转为json时，前端Js得到的数据将由于溢出而导致精度丢失
 * </p>
 *
 * @author chanfa
 */
@JacksonStdImpl
public class BigNumberSerializer extends NumberSerializer {

    /**
     * 根据 JS Number.MAX_SAFE_INTEGER 与 Number.MIN_SAFE_INTEGER 得来
     */
    private static final long MAX_SAFE_INTEGER = 9007199254740991L;

    private static final long MIN_SAFE_INTEGER = -9007199254740991L;

    /**
     * 提供实例
     */
    public static final BigNumberSerializer INSTANCE = new BigNumberSerializer(Number.class);

    public BigNumberSerializer(Class<? extends Number> rawType) {
        super(rawType);
    }

    @Override
    public void serialize(Number value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value instanceof BigDecimal bigDecimal) {
            // 小数设置精度：四舍五入保留2位
            bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_UP);
            // 返回
            gen.writeString(bigDecimal.toPlainString());
        } else {
            // 整数
            if (value.longValue() < MIN_SAFE_INTEGER || value.longValue() > MAX_SAFE_INTEGER) {
                // 超出范围：序列化为字符串
                gen.writeString(value.toString());
            } else {
                // 其他类型
                super.serialize(value, gen, provider);
            }
        }
    }
}
