package org.sormas.e2etests.steps.web.application.persons;

import static org.sormas.e2etests.pages.application.cases.CaseImportExportPage.BASIC_CASE_EXPORT_BUTTON;
import static org.sormas.e2etests.pages.application.cases.CaseImportExportPage.CASE_EXPORT_BUTTON;
import static org.sormas.e2etests.pages.application.cases.CaseImportExportPage.CUSTOM_CASE_DELETE_BUTTON;
import static org.sormas.e2etests.pages.application.cases.CaseImportExportPage.CUSTOM_CASE_EXPORT_DOWNLOAD_BUTTON;
import static org.sormas.e2etests.pages.application.cases.CaseImportExportPage.EXPORT_CONFIGURATION_DATA_DISTRICT_CHECKBOX;
import static org.sormas.e2etests.pages.application.cases.CaseImportExportPage.NEW_EXPORT_CONFIGURATION_BUTTON;
import static org.sormas.e2etests.pages.application.cases.CaseImportExportPage.NEW_EXPORT_CONFIGURATION_SAVE_BUTTON;
import static org.sormas.e2etests.pages.application.events.EventDirectoryPage.CUSTOM_EXPORT_PARTICIPANT_BUTTON;
import static org.sormas.e2etests.pages.application.events.EventDirectoryPage.DETAILED_EVENT_EXPORT_BUTTON;
import static org.sormas.e2etests.pages.application.events.EventParticipantsPage.EXPORT_EVENT_PARTICIPANT_CONFIGURATION_DATA_REGION_CHECKBOX;

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
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.sormas.e2etests.entities.pojo.web.Person;
import org.sormas.e2etests.enums.DistrictsValues;
import org.sormas.e2etests.enums.RegionsValues;
import org.sormas.e2etests.helpers.RestAssuredClient;
import org.sormas.e2etests.helpers.WebDriverHelpers;
import org.sormas.e2etests.helpers.environmentdata.manager.EnvironmentManager;
import org.sormas.e2etests.state.ApiState;
import org.testng.asserts.SoftAssert;

@Slf4j
public class PersonImportExportSteps implements En {

  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  @Inject
  public PersonImportExportSteps(
      WebDriverHelpers webDriverHelpers,
      ApiState apiState,
      SoftAssert softly,
      RestAssuredClient restAssuredClient) {
    EnvironmentManager manager = new EnvironmentManager(restAssuredClient);
    When(
        "I click on the Export person button",
        () -> {
          TimeUnit.SECONDS.sleep(2); // Wait for filter
          webDriverHelpers.clickOnWebElementBySelector(CASE_EXPORT_BUTTON);
        });
    When(
        "I click on the Basic Person Export button",
        () -> {
          webDriverHelpers.clickOnWebElementBySelector(BASIC_CASE_EXPORT_BUTTON);
          TimeUnit.SECONDS.sleep(5); // Wait for filter
        });
    When(
        "I click on the Detailed Person Export button",
        () -> {
          webDriverHelpers.clickOnWebElementBySelector(DETAILED_EVENT_EXPORT_BUTTON);
          TimeUnit.SECONDS.sleep(5); // Wait for filter
        });
    When(
        "I click on the Custom Person Export button",
        () -> {
          webDriverHelpers.clickOnWebElementBySelector(CUSTOM_EXPORT_PARTICIPANT_BUTTON);
          TimeUnit.SECONDS.sleep(5); // Wait for filter
        });
    When(
        "I check if downloaded data generated by custom person option is correct",
        () -> {
          String file = "./downloads/sormas_persons_" + LocalDate.now().format(formatter) + "_.csv";
          Person reader = parseCustomPersonExport(file);
          Path path = Paths.get(file);
          Files.delete(path);
          softly.assertEquals(
              reader.getRegion(),
              RegionsValues.VoreingestellteBundeslander.getName(),
              "Regions are not equal");
          softly.assertEquals(
              reader.getDistrict().toLowerCase(Locale.GERMANY),
              DistrictsValues.VoreingestellterLandkreis.getName().toLowerCase(Locale.GERMANY),
              "Districts are not equal");
          softly.assertAll();
        });
    When(
        "I select specific data of person to export in Export Configuration",
        () -> {
          webDriverHelpers.clickOnWebElementBySelector(
              EXPORT_EVENT_PARTICIPANT_CONFIGURATION_DATA_REGION_CHECKBOX);
          webDriverHelpers.clickOnWebElementBySelector(EXPORT_CONFIGURATION_DATA_DISTRICT_CHECKBOX);
          webDriverHelpers.clickOnWebElementBySelector(NEW_EXPORT_CONFIGURATION_SAVE_BUTTON);
        });
    When(
        "I download created custom person export file",
        () -> {
          webDriverHelpers.clickOnWebElementBySelector(CUSTOM_CASE_EXPORT_DOWNLOAD_BUTTON);
          TimeUnit.SECONDS.sleep(5); // wait for download
        });
    When(
        "I delete created custom person export file",
        () -> webDriverHelpers.clickOnWebElementBySelector(CUSTOM_CASE_DELETE_BUTTON));
    When(
        "I click on the New Export Configuration button in Custom Person Export popup",
        () -> webDriverHelpers.clickOnWebElementBySelector(NEW_EXPORT_CONFIGURATION_BUTTON));
    When(
        "I check if downloaded data generated by basic person export option is correct",
        () -> {
          String file = "./downloads/sormas_persons_" + LocalDate.now().format(formatter) + "_.csv";
          Person reader = parseBasicPersonExport(file);
          Path path = Paths.get(file);
          Files.delete(path);
          softly.assertEquals(
              reader.getUuid(),
              apiState.getLastCreatedPerson().getUuid(),
              "UUIDs are not equal");
          softly.assertEquals(
              reader.getFirstName(),
              apiState.getLastCreatedPerson().getFirstName(),
              "First names are not equal");
          softly.assertEquals(
              reader.getLastName(),
              apiState.getLastCreatedPerson().getLastName(),
              "Last names are not equal");
          softly.assertEquals(
              reader.getSex().toLowerCase(),
              apiState.getLastCreatedPerson().getSex().toLowerCase(),
              "Sexes are not equal");
          softly.assertEquals(
              reader.getDistrict().toLowerCase(),
              manager
                  .getDistrictName(apiState.getLastCreatedPerson().getAddress().getDistrict())
                  .toLowerCase(),
              "Districts are not equal");
          softly.assertEquals(
              reader.getStreet().toLowerCase(),
              apiState.getLastCreatedPerson().getAddress().getStreet().toLowerCase(),
              "Streets are not equal");
          softly.assertEquals(
              reader.getHouseNumber().toLowerCase(),
              apiState.getLastCreatedPerson().getAddress().getHouseNumber().toLowerCase(),
              "House numbers are not equal");
          softly.assertEquals(
              reader.getPostalCode().toLowerCase(),
              apiState.getLastCreatedPerson().getAddress().getPostalCode().toLowerCase(),
              "Postal codes are not equal");
          softly.assertEquals(
              reader.getCity().toLowerCase(),
              apiState.getLastCreatedPerson().getAddress().getCity().toLowerCase(),
              "Cities are not equal");
          softly.assertEquals(
              reader.getDateOfBirth(),
              LocalDate.of(
                  apiState.getLastCreatedPerson().getBirthdateYYYY(),
                  apiState.getLastCreatedPerson().getBirthdateMM(),
                  apiState.getLastCreatedPerson().getBirthdateDD()),
              "Birthdates are not equal");
          softly.assertAll();
        });

    When(
        "I check if downloaded data generated by detailed person export option is correct",
        () -> {
          String file = "./downloads/sormas_persons_" + LocalDate.now().format(formatter) + "_.csv";
          Person reader = parseDetailedPersonExport(file);
          Path path = Paths.get(file);
          Files.delete(path);
          softly.assertEquals(
              reader.getUuid(),
              apiState.getLastCreatedPerson().getUuid(),
              "UUIDs are not equal");
          softly.assertEquals(
              reader.getFirstName(),
              apiState.getLastCreatedPerson().getFirstName(),
              "First names are not equal");
          softly.assertEquals(
              reader.getLastName(),
              apiState.getLastCreatedPerson().getLastName(),
              "Last names are not equal");
          softly.assertEquals(
              reader.getSex().toLowerCase(),
              apiState.getLastCreatedPerson().getSex().toLowerCase(),
              "Sexes are not equal");
          softly.assertEquals(
              reader.getDistrict().toLowerCase(),
              manager
                  .getDistrictName(apiState.getLastCreatedPerson().getAddress().getDistrict())
                  .toLowerCase(),
              "Districts are not equal");
          softly.assertEquals(
              reader.getStreet().toLowerCase(),
              apiState.getLastCreatedPerson().getAddress().getStreet().toLowerCase(),
              "Streets are not equal");
          softly.assertEquals(
              reader.getHouseNumber().toLowerCase(),
              apiState.getLastCreatedPerson().getAddress().getHouseNumber().toLowerCase(),
              "House numbers are not equal");
          softly.assertEquals(
              reader.getPostalCode().toLowerCase(),
              apiState.getLastCreatedPerson().getAddress().getPostalCode().toLowerCase(),
              "Postal codes are not equal");
          softly.assertEquals(
              reader.getCity().toLowerCase(),
              apiState.getLastCreatedPerson().getAddress().getCity().toLowerCase(),
              "Cities are not equal");
          softly.assertEquals(
              reader.getDateOfBirth(),
              LocalDate.of(
                  apiState.getLastCreatedPerson().getBirthdateYYYY(),
                  apiState.getLastCreatedPerson().getBirthdateMM(),
                  apiState.getLastCreatedPerson().getBirthdateDD()),
              "Birthdates are not equal");
          softly.assertEquals(
              reader.getPresentConditionOfPerson().toLowerCase(),
              apiState.getLastCreatedPerson().getPresentCondition().toLowerCase(),
              "Present conditions are not equal");
          softly.assertEquals(
              reader.getRegion().toLowerCase(),
              manager
                  .getRegionName(apiState.getLastCreatedPerson().getAddress().getRegion())
                  .toLowerCase(),
              "Regions are not equal");
          softly.assertEquals(
              reader.getCommunity().toLowerCase(),
              manager
                  .getCommunityName(apiState.getLastCreatedPerson().getAddress().getCommunity())
                  .toLowerCase(),
              "Communities are not equal");
          softly.assertEquals(
              reader.getFacilityNameAndDescription(),
              apiState.getLastCreatedPerson().getAddress().getFacilityDetails(),
              "Facilities name and description are not equal");
          softly.assertAll();
        });
  }

  public Person parseBasicPersonExport(String fileName) {
    List<String[]> r = null;
    String[] values = new String[] {};
    Person builder = null;
    CSVParser csvParser = new CSVParserBuilder().withSeparator(',').build();
    try (CSVReader reader =
        new CSVReaderBuilder(new FileReader(fileName))
            .withCSVParser(csvParser)
            .withSkipLines(2) // parse only data
            .build()) {
      r = reader.readAll();
    } catch (IOException e) {
      log.error("IOException parseBasicPersonExport: {}", e.getCause());
    } catch (CsvException e) {
      log.error("CsvException parseBasicPersonExport: {}", e.getCause());
    }
    try {
      for (int i = 0; i < r.size(); i++) {
        values = r.get(i);
      }
      builder =
          Person.builder()
              .uuid(values[0])
              .firstName(values[1])
              .lastName(values[2])
              .sex(values[4])
              .district(values[5])
              .street(values[6])
              .houseNumber(values[7])
              .postalCode(values[8])
              .city(values[9])
              .dateOfBirth(
                  LocalDate.parse(
                      values[3].substring(values[3].indexOf("(") + 1, values[3].indexOf(")")),
                      DateTimeFormatter.ofPattern("M/d/yyyy")))
              .build();
    } catch (NullPointerException e) {
      log.error("Null pointer exception parseBasicPersonExport: {}", e.getCause());
    }
    return builder;
  }

  public Person parseDetailedPersonExport(String fileName) {
    List<String[]> r = null;
    String[] values = new String[] {};
    Person builder = null;
    CSVParser csvParser = new CSVParserBuilder().withSeparator(',').build();
    try (CSVReader reader =
        new CSVReaderBuilder(new FileReader(fileName))
            .withCSVParser(csvParser)
            .withSkipLines(2) // parse only data
            .build()) {
      r = reader.readAll();
    } catch (IOException e) {
      log.error("IOException parseDetailedPersonExport: {}", e.getCause());
    } catch (CsvException e) {
      log.error("CsvException parseDetailedPersonExport: {}", e.getCause());
    }
    try {
      for (int i = 0; i < r.size(); i++) {
        values = r.get(i);
      }
      builder =
          Person.builder()
              .uuid(values[0])
              .firstName(values[1])
              .lastName(values[2])
              .sex(values[3])
              .district(values[17])
              .street(values[19])
              .houseNumber(values[20])
              .postalCode(values[21])
              .city(values[22])
              .presentConditionOfPerson(values[11])
              .region(values[16])
              .community(values[18])
              .facility(values[24])
              .facilityNameAndDescription(values[25])
              .dateOfBirth(LocalDate.parse(values[4], DateTimeFormatter.ofPattern("M/d/yyyy")))
              .build();
    } catch (NullPointerException e) {
      log.error("Null pointer exception parseDetailedPersonExport: {}", e.getCause());
    }
    return builder;
  }

  public Person parseCustomPersonExport(String fileName) {
    List<String[]> r = null;
    String[] values = new String[] {};
    Person builder = null;
    CSVParser csvParser = new CSVParserBuilder().withSeparator(',').build();
    try (CSVReader reader =
        new CSVReaderBuilder(new FileReader(fileName))
            .withCSVParser(csvParser)
            .withSkipLines(2) // parse only data
            .build()) {
      r = reader.readAll();
    } catch (IOException e) {
      log.error("IOException parseCustomPersonExport: ", e);
    } catch (CsvException e) {
      log.error("CsvException parseCustomPersonExport: ", e);
    }
    try {
      for (int i = 0; i < r.size(); i++) {
        values = r.get(i);
      }
      builder = Person.builder().region(values[0]).district(values[1]).build();
    } catch (NullPointerException e) {
      log.error("Null pointer exception parseCustomPersonExport: ", e);
    }
    return builder;
  }
}
