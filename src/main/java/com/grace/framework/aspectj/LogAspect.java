package com.grace.framework.aspectj;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.grace.common.annotation.Log;
import com.grace.common.core.domain.LoginUser;
import com.grace.common.enums.BusinessStatus;
import com.grace.common.utils.SecurityUtils;
import com.grace.common.utils.ServletUtils;
import com.grace.framework.manager.AsyncManager;
import com.grace.framework.manager.factory.AsyncFactory;
import com.grace.web.monitor.domain.MonOplog;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Collection;
import java.util.Map;

/**
 * 操作日志记录处理
 *
 * @author chanfa
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    /**
     * 处理完请求后执行
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Log controllerLog, Object jsonResult) {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e) {
        handleLog(joinPoint, controllerLog, e, null);
    }

    protected void handleLog(final JoinPoint joinPoint, Log controllerLog, final Exception e, Object jsonResult) {
        try {
            // *========数据库日志=========*//
            MonOplog monOplog = new MonOplog();
            monOplog.setStatus(BusinessStatus.SUCCESS.ordinal());
            // 请求的地址
            String ip = ServletUtils.getClientIP(ServletUtils.getRequest());
            monOplog.setOplogIp(ip);
            monOplog.setOplogUrl(StrUtil.sub(ServletUtils.getRequest().getRequestURI(), 0, 255));
            // 获取当前的用户
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (loginUser != null) {
                monOplog.setOplogName(loginUser.getUsername());
                monOplog.setDeptName(loginUser.getDeptName());
            }

            if (e != null) {
                monOplog.setStatus(BusinessStatus.FAIL.ordinal());
                monOplog.setErrorMsg(StrUtil.sub(e.getMessage(), 0, 2000));
            }
            // 设置方法名称
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            monOplog.setMethod(className + StrUtil.DOT + methodName + "()");
            // 设置请求方式
            monOplog.setRequestMethod(ServletUtils.getRequest().getMethod());
            // 处理设置注解上的参数
            getControllerMethodDescription(joinPoint, controllerLog, monOplog, jsonResult);
            // 保存数据库
            AsyncManager.me().execute(AsyncFactory.recordOplog(monOplog));
        } catch (Exception exp) {
            // 记录本地异常日志
            log.error("==前置通知异常==");
            log.error("异常信息:{}", exp.getMessage());
        }
    }

    /**
     * 获取注解中对方法的描述信息 用于Controller层注解
     *
     * @param log      日志
     * @param monOplog 操作日志
     */
    public void getControllerMethodDescription(JoinPoint joinPoint, Log log, MonOplog monOplog, Object jsonResult) {
        // 设置action动作
        monOplog.setBusinessType(log.businessType().ordinal());
        // 设置标题
        monOplog.setTitle(log.title());
        // 设置操作人类别
        monOplog.setOperatorType(log.operatorType().ordinal());
        // 是否需要保存request，参数和值
        if (log.isSaveRequestData()) {
            // 获取参数的信息，传入到数据库中。
            setRequestValue(joinPoint, monOplog);
        }
        // 是否需要保存response，参数和值
        if (log.isSaveResponseData() && ObjectUtil.isNotNull(jsonResult)) {
            monOplog.setJsonResult(StrUtil.sub(JSONUtil.toJsonStr(jsonResult), 0, 2000));
        }
    }

    /**
     * 获取请求的参数，放到log中
     *
     * @param monOplog 操作日志
     */
    private void setRequestValue(JoinPoint joinPoint, MonOplog monOplog) {
        String requestMethod = monOplog.getRequestMethod();
        if (HttpMethod.PUT.matches(requestMethod) || HttpMethod.POST.matches(requestMethod)) {
            String params = argsArrayToString(joinPoint.getArgs());
            monOplog.setOplogParam(StrUtil.sub(params, 0, 2000));
        } else {
            Map<?, ?> paramsMap = (Map<?, ?>) ServletUtils.getRequest().getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            monOplog.setOplogParam(StrUtil.sub(paramsMap.toString(), 0, 2000));
        }
    }

    /**
     * 参数拼装
     */
    private String argsArrayToString(Object[] paramsArray) {
        StringBuilder params = new StringBuilder();
        if (paramsArray != null) {
            for (Object o : paramsArray) {
                if (ObjectUtil.isNotNull(o) && !isFilterObject(o)) {
                    Object jsonObj = JSONUtil.toJsonStr(o);
                    params.append(jsonObj.toString()).append(StrUtil.SPACE);
                }
            }
        }
        return params.toString().trim();
    }

    /**
     * 判断是否需要过滤的对象。
     *
     * @param o 对象信息。
     * @return 如果是需要过滤的对象，则返回true；否则返回false。
     */
    public boolean isFilterObject(final Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection<?> collection = (Collection<?>) o;
            for (Object value : collection) {
                return value instanceof MultipartFile;
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map<?, ?> map = (Map<?, ?>) o;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                return entry.getValue() instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse || o instanceof BindingResult;
    }
}
