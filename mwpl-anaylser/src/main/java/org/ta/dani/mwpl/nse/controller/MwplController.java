package org.ta.dani.mwpl.nse.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ta.dani.mwpl.excepion.DateAlreadyProcessedException;
import org.ta.dani.mwpl.nse.model.CombinedVolAndOI;
import org.ta.dani.mwpl.nse.processor.NseMwplProcessor;

@RestController
@RequestMapping("/mwpl-api")
public class MwplController {

	@Autowired
	NseMwplProcessor nseMwplProcessor;

	@GetMapping("/today")
	List<CombinedVolAndOI> processMwplForToday() throws DateAlreadyProcessedException {
		return nseMwplProcessor.processMwpl(LocalDate.now(), true);
	}

}
