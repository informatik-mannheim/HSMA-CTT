package de.hs_mannheim.informatik.ct.controller.interceptor;

import de.hs_mannheim.informatik.ct.controller.Utilities;
import de.hs_mannheim.informatik.ct.model.RoomVisit;
import de.hs_mannheim.informatik.ct.persistence.services.RoomVisitService;
import de.hs_mannheim.informatik.ct.persistence.services.VisitorService;
import lombok.val;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
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
        var isCheckedIn = false;
        val checkedInEmail = getEmailFromCheckedInCookie(request);
        if(checkedInEmail!=null){
            val checkedInRoom = getCheckedInRoomName(checkedInEmail);
            if(checkedInRoom!=null){
                isCheckedIn = true;
                request.setAttribute("checkedInRoom", checkedInRoom);
            }else{
                util.removeCookie(response, CHECKED_IN_COOKIE_NAME);
            }
        }
        request.setAttribute("checkedInEmail", checkedInEmail);
        request.setAttribute("isCheckedIn", isCheckedIn);
        return true;
    }

    private String getEmailFromCheckedInCookie(HttpServletRequest request){
        Cookie cookie = WebUtils.getCookie(request, CHECKED_IN_COOKIE_NAME);
        return cookie!=null ? cookie.getValue() : null;
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
