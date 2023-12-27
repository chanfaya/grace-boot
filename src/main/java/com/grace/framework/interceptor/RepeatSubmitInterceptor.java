package com.grace.framework.interceptor;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.grace.common.annotation.RepeatSubmit;
import com.grace.common.constant.CacheConstants;
import com.grace.common.core.domain.AjaxResult;
import com.grace.common.filter.RepeatedlyRequestWrapper;
import com.grace.common.utils.ServletUtils;
import com.grace.common.utils.redis.RedisUtils;
import com.grace.framework.web.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 防止重复提交拦截器
 *
 * @author chanfa
 */
@Component
public class RepeatSubmitInterceptor implements HandlerInterceptor {

    public final String REPEAT_PARAMS = "repeatParams";

    public final String REPEAT_TIME = "repeatTime";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            Method method = handlerMethod.getMethod();
            RepeatSubmit annotation = method.getAnnotation(RepeatSubmit.class);
            if (annotation != null) {
                if (this.isRepeatSubmit(request, annotation)) {
                    AjaxResult ajaxResult = AjaxResult.error(annotation.message());
                    ServletUtils.write(response, JSONUtil.toJsonStr(ajaxResult), ContentType.build(ContentType.JSON, CharsetUtil.CHARSET_UTF_8));
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 验证是否重复提交由子类实现具体的防重复提交的规则
     *
     * @param request 请求
     * @return boolean
     */
    public boolean isRepeatSubmit(HttpServletRequest request, RepeatSubmit annotation) {
        String nowParams = StrUtil.EMPTY;
        if (request instanceof RepeatedlyRequestWrapper repeatedlyRequest) {
            nowParams = ServletUtils.getBody(repeatedlyRequest);
        }

        // body参数为空，获取Parameter的数据
        if (StrUtil.isEmpty(nowParams)) {
            nowParams = JSONUtil.toJsonStr(request.getParameterMap());
        }
        JSONObject nowDataMap = JSONUtil.createObj();
        nowDataMap.set(REPEAT_PARAMS, nowParams);
        nowDataMap.set(REPEAT_TIME, System.currentTimeMillis());

        // 请求地址（作为存放cache的key值）
        String url = request.getRequestURI();

        // 唯一值（没有消息头则使用请求地址）
        String submitKey = StrUtil.trimToEmpty(request.getHeader(TokenService.HEADER));

        // 唯一标识（指定key + url + 消息头）
        String cacheRepeatKey = CacheConstants.REPEAT_SUBMIT_KEY + url + submitKey;

        Object sessionObj = RedisUtils.getCacheObject(cacheRepeatKey);
        if (sessionObj != null) {
            JSONObject sessionMap = JSONUtil.parseObj(sessionObj);
            if (sessionMap.containsKey(url)) {
                JSONObject preDataMap = JSONUtil.parseObj(sessionMap.get(url));
                if (compareParams(nowDataMap, preDataMap) && compareTime(nowDataMap, preDataMap, annotation.interval())) {
                    return true;
                }
            }
        }
        Map<String, Object> cacheMap = new HashMap<>(2);
        cacheMap.put(url, nowDataMap);
        RedisUtils.setCacheObject(cacheRepeatKey, cacheMap, Duration.ofMillis(annotation.interval()));
        return false;
    }

    /**
     * 判断参数是否相同
     */
    private boolean compareParams(JSONObject nowMap, JSONObject preMap) {
        String nowParams = nowMap.getStr(REPEAT_PARAMS);
        String preParams = preMap.getStr(REPEAT_PARAMS);
        return nowParams.equals(preParams);
    }

    /**
     * 判断两次间隔时间
     */
    private boolean compareTime(JSONObject nowMap, JSONObject preMap, int interval) {
        long time1 = nowMap.getLong(REPEAT_TIME);
        long time2 = preMap.getLong(REPEAT_TIME);
        return (time1 - time2) < interval;
    }
}
