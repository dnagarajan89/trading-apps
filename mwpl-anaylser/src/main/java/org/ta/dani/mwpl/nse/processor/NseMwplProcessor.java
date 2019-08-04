package org.ta.dani.mwpl.nse.processor;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.ta.dani.mwpl.excepion.DateAlreadyProcessedException;
import org.ta.dani.mwpl.excepion.MwplProcessException;
import org.ta.dani.mwpl.nse.model.CombinedVolAndOI;
import org.ta.dani.mwpl.nse.respository.CombinedVolAndOIRepository;
import org.ta.dani.mwpl.utils.MwplUtils;

@Component
public class NseMwplProcessor {

	@Autowired
	CombinedVolAndOIRepository combinedVolAndOIRepository;

	@Value("${ta.mwpl.nse.combined_vol_and_oi.url}")
	private String combinedVolAndOIUrl;

	@Value("${ta.mwpl.nse.combined_vol_and_oi.localFilePath}")
	private String localFilePath;

	@Value("${ta.mwpl.nse.combined_vol_and_oi.urlDateFormat}")
	private String urlDateFormat;

	private static Logger logger = LoggerFactory.getLogger(NseMwplProcessor.class);

	public List<CombinedVolAndOI> processMwpl(LocalDate date, boolean deleteEntriesIfExists)
			throws DateAlreadyProcessedException {
		String dateInString = null;
		if (date != null) {
			dateInString = MwplUtils.localDateToString(date, urlDateFormat);
		} else {
			LocalDate yesterday = LocalDate.now().minusDays(1);
			int weekendOffset = 0;
			if (yesterday.getDayOfWeek() == DayOfWeek.SUNDAY) {
				weekendOffset = 2;
			} else if (yesterday.getDayOfWeek() == DayOfWeek.SATURDAY) {
				weekendOffset = 1;
			}
			if (weekendOffset > 0) {
				yesterday = yesterday.minusDays(weekendOffset);
			}
			dateInString = MwplUtils.localDateToString(yesterday, urlDateFormat);
		}
		String url = combinedVolAndOIUrl + dateInString + ".zip";
		String filename = "combinedoi_" + dateInString + ".zip";
		String filePath = localFilePath + dateInString;
		File file = new File(filePath);
		if (file.exists()) {
			if (deleteEntriesIfExists) {
				try {
					FileUtils.deleteDirectory(new File(filePath));
				} catch (IOException e) {
					logger.error("Unable to delete file", e);
					throw new MwplProcessException("Error deleting directory " + filePath, e);
				}
			} else {
				throw new DateAlreadyProcessedException("Mwpl already processed for " + date);
			}
		}

		if (deleteEntriesIfExists) {
			List<CombinedVolAndOI> existingEntries = combinedVolAndOIRepository.findByDate(date);
			if (!CollectionUtils.isEmpty(existingEntries)) {
				combinedVolAndOIRepository.deleteByDate(date);
			}
		}

		downloadFile(url, filePath, filename);
		String unzipUrl = filePath + File.separator + "unzipped";
		String zipFile = filePath + File.separator + filename;
		String csvFile = unZipAndGetCsvFileName(zipFile, unzipUrl);
		String csvFilePath = unzipUrl + File.separator + csvFile;

		List<CombinedVolAndOI> combinedVolAndOIs = readMwplDataFromCsv(csvFilePath);
		combinedVolAndOIs = (List<CombinedVolAndOI>) combinedVolAndOIRepository.saveAll(combinedVolAndOIs);
		return combinedVolAndOIs;
	}

	private List<CombinedVolAndOI> readMwplDataFromCsv(String csvFilePath) {
		List<CombinedVolAndOI> combinedVolAndOIs = new ArrayList<>();
		try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(csvFilePath), "UTF8"))) {
			String line;
			int lineNumber = 1;
			while ((line = in.readLine()) != null) {
				if (lineNumber > 1) {
					String[] data = line.split(",");
					CombinedVolAndOI combinedVolAndOI = new CombinedVolAndOI();
					combinedVolAndOI.setDate(MwplUtils.stringToLocalDate(data[0], "dd-MMM-yyyy"));
					combinedVolAndOI.setISIN(data[1]);
					combinedVolAndOI.setScriptName(data[2]);
					combinedVolAndOI.setNseSymbol(data[3]);
					if (StringUtils.isNumeric(data[4])) {
						combinedVolAndOI.setMwpl(Long.valueOf(data[4]));
					}
					if (StringUtils.isNumeric(data[5])) {
						combinedVolAndOI.setOpenInterest(Long.valueOf(data[5]));
					}
					if (StringUtils.isNumeric(data[6])) {
						combinedVolAndOI.setLimitForNextDay(Long.valueOf(data[6]));
					}
					if (combinedVolAndOI.getMwpl() == null || combinedVolAndOI.getOpenInterest() == null
							|| combinedVolAndOI.getLimitForNextDay() == null) {
						combinedVolAndOI.setNoFreshPositions(true);
					}
					combinedVolAndOIs.add(combinedVolAndOI);
				}
				lineNumber++;
			}
		} catch (IOException e) {
			logger.error("Error reading mwpl data from CSV", e);
			throw new MwplProcessException("Error reading mwpl data from CSV " + csvFilePath, e);
		} catch (Exception e) {
			logger.error("Error reading mwpl data from CSV", e);
			throw new MwplProcessException("Error reading mwpl data from CSV " + csvFilePath, e);
		}
		return combinedVolAndOIs;
	}

	public void downloadFile(String url, String filePath, String filename) {
		File file = new File(filePath);
		if (!file.exists()) {
			file.mkdirs();
		}
		try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
				FileOutputStream fileOutputStream = new FileOutputStream(filePath + File.separator + filename)) {
			byte dataBuffer[] = new byte[1024];
			int bytesRead;
			while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
				fileOutputStream.write(dataBuffer, 0, bytesRead);
			}
		} catch (IOException e) {
			logger.error("Error downloading the file" + url, e);
			throw new MwplProcessException("Error downloading the file " + url, e);
		} catch (Exception e) {
			logger.error("Error downloading the file" + url, e);
			throw new MwplProcessException("Error downloading the file " + url, e);
		}
	}

	public String unZipAndGetCsvFileName(String zipFile, String outputFolder) {
		byte[] buffer = new byte[1024];
		File folder = new File(outputFolder);
		if (!folder.exists()) {
			folder.mkdir();
		}
		String xmlFile = null;
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
			ZipEntry ze = zis.getNextEntry();
			while (ze != null) {
				String fileName = ze.getName();
				File newFile = new File(outputFolder + File.separator + fileName);
				System.out.println("file unzip : " + newFile.getAbsoluteFile());
				try (FileOutputStream fos = new FileOutputStream(newFile)) {
					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (fileName.endsWith(".csv")) {
					xmlFile = fileName;
				}
				ze = zis.getNextEntry();
			}
			zis.closeEntry();
		} catch (IOException e) {
			logger.error("Error when unziping " + zipFile, e);
			throw new MwplProcessException("Error when unziping " + zipFile, e);
		} catch (Exception e) {
			logger.error("Error when unziping " + zipFile, e);
			throw new MwplProcessException("Error when unziping " + zipFile, e);
		}
		return xmlFile;
	}

	public List<CombinedVolAndOI> readMwplData(LocalDate date) {
		if(date == null) {
			LocalDate yesterday = LocalDate.now().minusDays(1);
			int weekendOffset = 0;
			if (yesterday.getDayOfWeek() == DayOfWeek.SUNDAY) {
				weekendOffset = 2;
			} else if (yesterday.getDayOfWeek() == DayOfWeek.SATURDAY) {
				weekendOffset = 1;
			}
			if (weekendOffset > 0) {
				date = yesterday.minusDays(weekendOffset);
			}
		}
		List<CombinedVolAndOI> combinedVolAndOIs = combinedVolAndOIRepository.findByDate(date);
		Collections.sort(combinedVolAndOIs, (a, b) -> a.getNseSymbol().compareTo(b.getNseSymbol()));
		return combinedVolAndOIs;
	}

}
