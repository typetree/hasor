/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dataql.fx.web;
import com.alibaba.fastjson.JSON;
import net.hasor.dataql.UdfSourceAssembly;
import net.hasor.utils.StringUtils;
import net.hasor.web.Invoker;

import javax.inject.Singleton;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * Web 函数库。函数库引入 <code>import 'net.hasor.dataql.fx.web.WebUdfSource' as webData;</code>
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-03-29
 */
@Singleton
public class WebUdfSource implements UdfSourceAssembly {
    /** jsonBody */
    public static Object jsonBody() {
        Invoker invoker = FxWebInterceptor.invoker();
        if (invoker == null) {
            return null;
        }
        return JSON.parse(invoker.getJsonBodyString());
    }
    // --------------------------------------------------------------------------------------------

    /** cookieMap */
    public static Map<String, String> cookieMap() {
        return FxWebInterceptor.cookieMap();
    }

    /** cookieMap,Value是数组 */
    public static Map<String, List<String>> cookieArrayMap() {
        return FxWebInterceptor.cookieArrayMap();
    }

    /** 获取Cookie */
    public static String getCookie(String cookieName) {
        if (StringUtils.isBlank(cookieName)) {
            return null;
        }
        return cookieMap().get(cookieName);
    }

    /** 获取所有名字相同的 Cookie */
    public static List<String> getCookieArray(String cookieName) {
        if (StringUtils.isBlank(cookieName)) {
            return null;
        }
        return cookieArrayMap().get(cookieName);
    }

    /** 设置 Cookie,MaxAge = -1 */
    public static boolean addCookie(String cookieName, String value) {
        HttpServletResponse httpResponse = FxWebInterceptor.invoker().getHttpResponse();
        Cookie cookie = new Cookie(cookieName, value);
        cookie.setMaxAge(-1);
        httpResponse.addCookie(cookie);
        return true;
    }

    /** 批量设置 Cookie */
    public static boolean addCookieAll(Map<String, String> cookieMap) {
        if (cookieMap != null) {
            cookieMap.forEach(WebUdfSource::addCookie);
            return true;
        }
        return false;
    }

    /** 存储 Cookie */
    public static boolean storeCookie(String cookieName, String value, int maxAge) {
        HttpServletResponse httpResponse = FxWebInterceptor.invoker().getHttpResponse();
        Cookie cookie = new Cookie(cookieName, value);
        if (maxAge <= 0) {
            maxAge = -1;
        }
        cookie.setMaxAge(maxAge);
        httpResponse.addCookie(cookie);
        return true;
    }

    /** 批量设置 Cookie */
    public static boolean storeCookieAll(Map<String, String> cookieMap, int maxAge) {
        if (cookieMap != null) {
            cookieMap.forEach((cookieName, value) -> {
                storeCookie(cookieName, value, maxAge);
            });
            return true;
        }
        return false;
    }

    /** 删除 Cookie */
    public static boolean removeCookie(String cookieName) {
        HttpServletResponse httpResponse = FxWebInterceptor.invoker().getHttpResponse();
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setMaxAge(0);
        httpResponse.addCookie(cookie);
        return true;
    }
    // --------------------------------------------------------------------------------------------

    /** headerMap */
    public static Map<String, String> headerMap() {
        return FxWebInterceptor.headerMap();
    }

    /** headerMap,Value是数组 */
    public static Map<String, List<String>> headerArrayMap() {
        return FxWebInterceptor.headerArrayMap();
    }

    /** 获取 Header */
    public static String getHeader(String headerName) {
        if (StringUtils.isBlank(headerName)) {
            return null;
        }
        return FxWebInterceptor.invoker().getHttpRequest().getHeader(headerName);
    }

    /** 获取所有名字相同的 Header */
    public static List<String> getHeaderArray(String headerName) {
        if (StringUtils.isBlank(headerName)) {
            return null;
        }
        List<String> headerList = new ArrayList<>();
        Enumeration<String> headers = FxWebInterceptor.invoker().getHttpRequest().getHeaders(headerName);
        while (headers.hasMoreElements()) {
            headerList.add(headers.nextElement());
        }
        return headerList;
    }

    /** 设置 Header */
    public static boolean setHeader(String headerName, String value) {
        if (StringUtils.isBlank(headerName)) {
            return false;
        }
        FxWebInterceptor.invoker().getHttpResponse().setHeader(headerName, value);
        return true;
    }

    /** 添加 Header */
    public static boolean addHeader(String headerName, String value) {
        if (StringUtils.isBlank(headerName)) {
            return false;
        }
        FxWebInterceptor.invoker().getHttpResponse().addHeader(headerName, value);
        return true;
    }

    /** 设置 HeaderMap */
    public static boolean addHeaderAll(Map<String, String> headerMap) {
        if (headerMap != null) {
            HttpServletResponse httpResponse = FxWebInterceptor.invoker().getHttpResponse();
            headerMap.forEach(httpResponse::setHeader);
            return true;
        }
        return false;
    }
    // --------------------------------------------------------------------------------------------

    /** sessionKeys */
    public static List<String> sessionKeys() {
        HttpSession httpSession = FxWebInterceptor.servletSession();
        if (httpSession == null) {
            return Collections.emptyList();
        }
        Enumeration<String> attributeNames = httpSession.getAttributeNames();
        List<String> names = new ArrayList<>();
        while (attributeNames.hasMoreElements()) {
            names.add(attributeNames.nextElement());
        }
        return names;
    }

    /** 获取Session */
    public static Object getSession(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        HttpSession httpSession = FxWebInterceptor.servletSession();
        if (httpSession == null) {
            return null;
        }
        return httpSession.getAttribute(key);
    }

    /** 设置Session */
    public static Object setSession(String key, Object newValue) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        HttpSession httpSession = FxWebInterceptor.servletSession();
        if (httpSession == null) {
            httpSession = FxWebInterceptor.invoker().getHttpRequest().getSession(true);
        }
        Object oldValue = httpSession.getAttribute(key);
        httpSession.setAttribute(key, newValue);
        return oldValue;
    }

    /** 删除Session */
    public static boolean removeSession(String key) {
        if (StringUtils.isBlank(key)) {
            return false;
        }
        HttpSession httpSession = FxWebInterceptor.servletSession();
        if (httpSession == null) {
            return false;
        }
        httpSession.removeAttribute(key);
        return true;
    }

    /** 删除所有Key */
    public static boolean cleanSession() {
        HttpSession httpSession = FxWebInterceptor.servletSession();
        if (httpSession == null) {
            return false;
        }
        Enumeration<String> attributeNames = httpSession.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            httpSession.removeAttribute(attributeNames.nextElement());
        }
        return true;
    }

    /** Invalidates this session then unbinds any objects bound to it. */
    public static boolean sessionInvalidate() {
        HttpSession httpSession = FxWebInterceptor.servletSession();
        if (httpSession == null) {
            return false;
        }
        httpSession.invalidate();
        return true;
    }

    /** Session ID. */
    public static String sessionId() {
        HttpSession httpSession = FxWebInterceptor.servletSession();
        if (httpSession == null) {
            return null;
        }
        return httpSession.getId();
    }

    /** 返回客户端发送与之关联的请求的最后一次时间. */
    public static long sessionLastAccessedTime() {
        HttpSession httpSession = FxWebInterceptor.servletSession();
        if (httpSession == null) {
            return 0;
        }
        return httpSession.getLastAccessedTime();
    }
}