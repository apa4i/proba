package cucumber.runner;

import cucumber.api.CucumberOptions;
import cucumber.api.testng.AbstractTestNGCucumberTests;
import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;
import net.masterthought.cucumber.Reportable;
import org.testng.annotations.AfterSuite;

import java.io.File;
import java.util.ArrayList;

@CucumberOptions(
        features = {
                "src/test/resources/features/searchForLocalBrowsers.feature",
        },
        glue = {"glue.steps"},
        plugin = {"pretty", "html:target", "json:target/cucumber.json"})
public class Main extends AbstractTestNGCucumberTests {

    static ArrayList<String> jsonFiles = new ArrayList<>();

    @AfterSuite(alwaysRun = true)
    public void tearDown() {
        File reportOutputDirectory = new File("target");
        listSubFolders("target\\cucumber-parallel\\", jsonFiles);

        String buildNumber = "1";
        String projectName = "cucumberProject";
        boolean runWithJenkins = false;
        boolean parallelTesting = true;

        Configuration configuration = new Configuration(reportOutputDirectory, projectName);
// optional configuration
        configuration.setParallelTesting(parallelTesting);
        configuration.setRunWithJenkins(runWithJenkins);
        configuration.setBuildNumber(buildNumber);
// addidtional metadata presented on main page
        configuration.addClassifications("Platform", "Windows");
        configuration.addClassifications("Browser", "Chrome");
        configuration.addClassifications("Branch", "release/1.0");
//
//// optionally add metadata presented on main page via properties file
//        List<String> classificationFiles = new ArrayList<>();
//        classificationFiles.add("properties-1.properties");
//        classificationFiles.add("properties-2.properties");
//        configuration.addClassificationFiles(classificationFiles);

        ReportBuilder reportBuilder = new ReportBuilder(jsonFiles, configuration);
        Reportable result = reportBuilder.generateReports();

// and here validate 'result' to decide what to do
// if report has failed features, undefined steps etc
    }


    public ArrayList<String> listSubFolders(String directoryName, ArrayList<String> files) {
        File directory = new File(directoryName);

        // get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                files.add(file.toString());
            } else if (file.isDirectory()) {
                listSubFolders(file.toString(), files);
            }
        }
        return files;
    }
}
