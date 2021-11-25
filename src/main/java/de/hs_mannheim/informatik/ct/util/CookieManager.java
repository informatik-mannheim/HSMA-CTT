package de.hs_mannheim.informatik.ct.util;

/*
 * Corona Tracking Tool der Hochschule Mannheim
 * Copyright (C) 2021 Hochschule Mannheim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import lombok.val;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalTime;

public class CookieManager {
    private HttpServletRequest request;
    private HttpServletResponse response;

    public CookieManager(HttpServletRequest request){
        this.request = request;
    }

    public CookieManager(HttpServletRequest request, HttpServletResponse response){
        this(request);
        this.response = response;
    }

    public enum Cookies{
        CHECKED_IN_EMAIL("checkedInEmail");

        String name;

        Cookies(String name) {
            this.name = name;
        }

        String getName(){
            return this.name;
        }
    }

    /**
     * create a new cookie with a cookie factory
     *
     * @param cookieType    cookie type
     * @param value         value of the cookie
     */
    private Cookie createCookie(Cookies cookieType, String value) {
        Cookie cookie = null;
        switch (cookieType){
            case CHECKED_IN_EMAIL:
                cookie = new CookieBuilder(cookieType.getName(), value)
                        .maxAge(getSecondsTill(LocalTime.parse(ScheduledMaintenanceTasks.FORCED_END_TIME)))
                        .build();
                break;
        }
        return cookie;
    }

    /**
     * add a cookie to the http response
     *
     * @param cookieType    cookie type
     * @param value         value of the cookie
     */
    public void addCookie(Cookies cookieType, String value) {
        val cookie = createCookie(cookieType, value);
        response.addCookie(cookie);
    }

    /**
     * remove a cookie from the http response
     *
     * @param cookieType    cookie type
     */
    public void removeCookie(Cookies cookieType) {
        Cookie cookie = new Cookie(cookieType.getName(), "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /**
     * get a cookie from http request
     *
     * @param cookieType    cookie type
     */
    public String getCookieValue(Cookies cookieType){
        val cookie = WebUtils.getCookie(request, cookieType.getName());
        return cookie!=null ? cookie.getValue() : null;
    }

    /**
     * remove a cookie from the http response
     *
     * @param maxAgeEndTime     time when the cookie should be invalid
     * @return                  max age of the cookie
     */
    private int getSecondsTill(LocalTime maxAgeEndTime){
        int now = LocalTime.now().toSecondOfDay();
        int endTime = maxAgeEndTime.toSecondOfDay();
        int nextDayEndTime = ((24 * 60 * 60) + endTime);
        return (now > endTime) ? nextDayEndTime - now : endTime - now;
    }

    private static class CookieBuilder{
        private String name, value, path;
        private int maxAge;

        public CookieBuilder(String name, String value){
            this.name = name;
            this.value = value;
            this.path = "/";
            this.maxAge = 0;
        }

        public CookieBuilder maxAge(int maxAge){
            this.maxAge = maxAge;
            return this;
        }

        public CookieBuilder path(String path){
            this.path = path;
            return this;
        }

        public Cookie build(){
            val cookie = new Cookie(name, value);
            cookie.setPath(path);
            if(maxAge>0){
                cookie.setMaxAge(maxAge);
            }
            return cookie;
        }
    }
}
