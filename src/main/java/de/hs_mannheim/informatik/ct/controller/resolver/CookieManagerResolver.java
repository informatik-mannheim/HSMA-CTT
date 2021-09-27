package de.hs_mannheim.informatik.ct.controller.resolver;

import de.hs_mannheim.informatik.ct.util.CookieManager;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieManagerResolver
        implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.getParameter().getType() == CookieManager.class;
    }

    @Override
    public Object resolveArgument(
            MethodParameter methodParameter,
            ModelAndViewContainer modelAndViewContainer,
            NativeWebRequest nativeWebRequest,
            WebDataBinderFactory webDataBinderFactory) throws Exception {

        HttpServletRequest request = (HttpServletRequest) nativeWebRequest.getNativeRequest();
        HttpServletResponse response = (HttpServletResponse) nativeWebRequest.getNativeResponse();

        return new CookieManager(request, response);
    }
}