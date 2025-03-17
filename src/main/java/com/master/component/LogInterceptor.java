package com.master.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.master.dto.ApiMessageDto;
import com.master.service.HttpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class LogInterceptor implements HandlerInterceptor {
    @Autowired
    private HttpService httpService;
    @Autowired
    private ObjectMapper objectMapper;
    private static final List<String> INTERNAL_REQUEST = List.of(
            "/v1/db-config/get-by-name"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (DispatcherType.REQUEST.name().equals(request.getDispatcherType().name())
                && request.getMethod().equals(HttpMethod.GET.name())) {
        }
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);
        log.debug("Starting call url: [" + getUrl(request) + "]");
        if (isAllowed(request, INTERNAL_REQUEST)) {
            httpService.validateInternalRequest(request);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
        long startTime = (Long) request.getAttribute("startTime");
        long endTime = System.currentTimeMillis();
        long executeTime = endTime - startTime;
        log.debug("Complete [" + getUrl(request) + "] executeTime : " + executeTime + "ms");
        if (ex != null) {
            log.error("afterCompletion>> " + ex.getMessage());

        }
    }

    /**
     * get full url request
     * @param req
     * @return
     */
    private static String getUrl(HttpServletRequest req) {
        String reqUrl = req.getRequestURL().toString();
        String queryString = req.getQueryString();   // d=789
        if (!StringUtils.isEmpty(queryString)) {
            reqUrl += "?" + queryString;
        }
        return reqUrl;
    }

    private boolean isAllowed(HttpServletRequest request, List<String> whiteList) {
        AntPathMatcher pathMatcher = new AntPathMatcher();
        return whiteList.stream().anyMatch(pattern -> pathMatcher.match(pattern, request.getRequestURI()));
    }

    private boolean handleUnauthorized(HttpServletResponse response, String message) throws IOException {
        ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
        apiMessageDto.setMessage(message);
        apiMessageDto.setResult(false);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getOutputStream().write(objectMapper.writeValueAsBytes(apiMessageDto));
        response.flushBuffer();
        return false;
    }
}
