package com.cicad.app.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ThymeleafController {

	@Value("${vite.dev-server:http://localhost:3001}")
	private String viteDevServer;

	@RequestMapping(value = "", method = RequestMethod.GET)
	private String getApp(Model model) {
		model.addAttribute("viteDevServer", viteDevServer);
		return "app";
	}

}
