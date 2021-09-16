package de.hs_mannheim.informatik.ct.controller.interceptor;

import de.hs_mannheim.informatik.ct.model.RoomVisit;
import de.hs_mannheim.informatik.ct.persistence.services.RoomVisitService;
import de.hs_mannheim.informatik.ct.persistence.services.VisitorService;
import lombok.val;
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

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
        Boolean isCheckedIn = false;
        Cookie cookie = WebUtils.getCookie(request, "checkedInEmail");
        if(cookie!=null){
            isCheckedIn = true;
            val email = cookie.getValue();
            List<RoomVisit> roomVisits = findCurrentRoomVisitsByEmail(email);
            if(roomVisits.size()>0){
                request.setAttribute("checkedInEmail", email);
            }else{
                isCheckedIn = false;
                Cookie c = new Cookie("checkedInEmail", "");
                c.setMaxAge(0);
                c.setPath("/");
                response.addCookie(c);
            }
        }
        request.setAttribute("isCheckedIn", isCheckedIn);
        return true;
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
