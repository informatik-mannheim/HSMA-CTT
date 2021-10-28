package de.hs_mannheim.informatik.ct.controller.interceptor;

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

import de.hs_mannheim.informatik.ct.controller.Utilities;
import de.hs_mannheim.informatik.ct.model.RoomVisit;
import de.hs_mannheim.informatik.ct.persistence.services.RoomVisitService;
import de.hs_mannheim.informatik.ct.persistence.services.VisitorService;
import de.hs_mannheim.informatik.ct.util.CookieManager;
import lombok.val;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

public class CheckInInterceptor implements HandlerInterceptor {

    @Autowired
    private VisitorService visitorService;

    @Autowired
    private RoomVisitService roomVisitService;

    @Autowired
    private Utilities util;

    private static final String CHECKED_IN_COOKIE_NAME = "checkedInEmail";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
        val cookieManager = new CookieManager(request, response);
        var isCheckedIn = false;
        val checkedInEmail = cookieManager.getCookieValue(CookieManager.Cookies.CHECKED_IN_EMAIL);
        if(checkedInEmail!=null){
            val checkedInRoom = getCheckedInRoomName(checkedInEmail);
            if(checkedInRoom!=null){
                isCheckedIn = true;
                request.setAttribute("checkedInRoom", checkedInRoom);
            }else{
                cookieManager.removeCookie(CookieManager.Cookies.CHECKED_IN_EMAIL);
            }
        }
        request.setAttribute("checkedInEmail", checkedInEmail);
        request.setAttribute("isCheckedIn", isCheckedIn);
        return true;
    }

    private String getCheckedInRoomName(String email){
        List<RoomVisit> roomVisits = findCurrentRoomVisitsByEmail(email);
        return roomVisits.size()>0 ? roomVisits.get(0).getRoom().getName() : null;
    }

    private List<RoomVisit> findCurrentRoomVisitsByEmail(String email){
        List<RoomVisit> roomVisits = new ArrayList<>();
        val visitor = visitorService.findVisitorByEmail(email);
        if(visitor.isPresent()) {
            for(val roomVisit : roomVisitService.getCheckedInRoomVisits(visitor.get())){
                roomVisits.add(roomVisit);
            }
        }
        return roomVisits;
    }
}