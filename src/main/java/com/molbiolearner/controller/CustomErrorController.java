package com.molbiolearner.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status  = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object uri     = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        Object exc     = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        int statusCode = status != null ? Integer.parseInt(status.toString()) : 0;
        HttpStatus httpStatus = null;
        try { httpStatus = HttpStatus.valueOf(statusCode); } catch (Exception ignored) {}

        model.addAttribute("statusCode",   statusCode > 0 ? statusCode : "—");
        model.addAttribute("statusText",   httpStatus != null ? httpStatus.getReasonPhrase() : "Unknown Error");
        model.addAttribute("requestUri",   uri != null ? uri.toString() : "—");
        model.addAttribute("errorMessage", message != null && !message.toString().isBlank() ? message.toString() : null);
        model.addAttribute("exceptionType",    exc != null ? exc.getClass().getName() : null);
        model.addAttribute("exceptionMessage", exc instanceof Throwable t ? t.getMessage() : null);

        model.addAttribute("emoji", switch (statusCode) {
            case 403 -> "🔒";
            case 404 -> "🔭";
            case 500 -> "💥";
            case 503 -> "🔧";
            default  -> "⚠️";
        });

        return "error";
    }
}
