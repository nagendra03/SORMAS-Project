package org.sormas.e2etests.steps.web.application.statistics;

import static org.sormas.e2etests.pages.application.events.EventDirectoryPage.EVENT_EXPORT_BUTTON;
import static org.sormas.e2etests.pages.application.statistics.StatisticsPage.DATABASE_EXPORT_TAB;
import static org.sormas.e2etests.pages.application.statistics.StatisticsPage.EVENT_GROUPS_CHECKBOX;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import cucumber.api.java8.En;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.sormas.e2etests.helpers.WebDriverHelpers;
import org.testng.asserts.SoftAssert;

@Slf4j
public class StatisticsSteps implements En {

  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  @Inject
  public StatisticsSteps(WebDriverHelpers webDriverHelpers, SoftAssert softly) {
    When(
        "I click on the Database Export tab from Statistics directory",
        () -> webDriverHelpers.clickOnWebElementBySelector(DATABASE_EXPORT_TAB));
    When(
        "I click on the Event Groups checkbox from Statistics directory",
        () -> webDriverHelpers.clickOnWebElementBySelector(EVENT_GROUPS_CHECKBOX));
    When(
        "I click on the Export button from Database Export tab",
        () -> {
          webDriverHelpers.clickOnWebElementBySelector(EVENT_EXPORT_BUTTON);
          TimeUnit.SECONDS.sleep(2);
        });
    When(
        "I unzip a downloaded file from Database export",
        () -> {
          String source = "./downloads/sormas_export_" + LocalDate.now().format(formatter) + ".zip";
          String destination = "./downloads";
          try {
            ZipFile zipFile = new ZipFile(source);
            zipFile.extractAll(destination);
            Path path = Paths.get(source);
            Files.delete(path);
          } catch (ZipException e) {
            e.printStackTrace();
          }
        });
    When(
        "I check if downloaded file generated by Event Groups database export contains required headers",
        () -> {
          String file = "./downloads/eventgroups.csv";
          Path path = Paths.get(file);
          String[] Columns = parseEventGroupsDatabaseExportColumns(file);
          Files.delete(path);
          softly.assertTrue(
              Arrays.asList(Columns).contains("id"), "Downloaded data does not contain id column!");
          softly.assertTrue(
              Arrays.asList(Columns).contains("uuid"),
              "Downloaded data does not contain uuid column!");
          softly.assertTrue(
              Arrays.asList(Columns).contains("name"),
              "Downloaded data does not contain name column!");
          softly.assertTrue(
              Arrays.asList(Columns).contains("changedate"),
              "Downloaded data does not contain changedate column!");
          softly.assertTrue(
              Arrays.asList(Columns).contains("creationdate"),
              "Downloaded data does not contain creationdate column!");
          softly.assertTrue(
              Arrays.asList(Columns).contains("archived"),
              "Downloaded data does not contain archived column!");
          softly.assertTrue(
              Arrays.asList(Columns).contains("sys_period"),
              "Downloaded data does not contain sys_period column!");
          softly.assertTrue(
              Arrays.asList(Columns).contains("change_user_id"),
              "Downloaded data does not contain change_user_id column!");
          softly.assertAll();
        });
  }

  public String[] parseEventGroupsDatabaseExportColumns(String fileName) {
    String[] r = null;
    CSVParser csvParser = new CSVParserBuilder().withSeparator(',').build();
    try (CSVReader reader =
        new CSVReaderBuilder(new FileReader(fileName))
            .withCSVParser(csvParser)
            .withSkipLines(1)
            .build()) {
      r = reader.readNext();
    } catch (IOException e) {
      log.error("IOException parseEventGroupsDatabaseExportColumns: {}", e.getCause());
    } catch (CsvException e) {
      log.error("CsvException parseEventGroupsDatabaseExportColumns: {}", e.getCause());
    }
    return r;
  }
}
