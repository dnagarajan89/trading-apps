package org.ta.dani.mwpl.nse.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import io.swagger.models.Model;

@Controller
public class MwplController {

	@GetMapping("/mwpl")
	public String hello(Model model) {
		return "mwpl";
	}

}
