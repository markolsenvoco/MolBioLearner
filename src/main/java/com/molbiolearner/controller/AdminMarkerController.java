package com.molbiolearner.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminMarkerController {

    @GetMapping("/admin/marker")
    public String markerPage() {
        return "admin/marker";
    }
}
