package org.ta.dani.mwpl.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.ta.dani.mwpl.excepion.DateAlreadyProcessedException;
import org.ta.dani.mwpl.nse.processor.NseMwplProcessor;

@Component
public class ScheduledTasks {
	
	@Autowired
	private NseMwplProcessor nseMwplProcessor;
	
	@Scheduled(cron = "0 0 2 1/1 * TUE-SAT")
	public void executeTask() throws DateAlreadyProcessedException {
		nseMwplProcessor.processMwpl(null, false);
	}

}
