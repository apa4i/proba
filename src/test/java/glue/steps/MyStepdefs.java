package glue.steps;

import cucumber.api.PendingException;
import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import helpers.WaitTool;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.testng.asserts.SoftAssert;
import pages.HomePage;
import pages.Search;
import pages.SearchResults;

import java.util.List;
public class MyStepdefs extends Browser {


    @Given("^browser \"([^\"]*)\" with remote address: \"([^\"]*)\"$")
    public void browserWithRemoteAddress(String browserName, String address) throws Throwable {
        openRemoteWebDriver(browserName, address);
    }

    @Given("^browser \"([^\"]*)\"$")
    public void browser(String browserName) throws Throwable {
        openLocalWebDriver(browserName);
    }

    @And("^website loaded this address: \"([^\"]*)\"$")
    public void websiteLoadedThisAddress(String url) throws Throwable {
        driver.get(url);
    }

    @When("^I execute a search for \"([^\"]*)\"$")
    public void iExecuteASearchFor(String textForSearching) throws Throwable {
        Search searchInStore = new Search();
        searchInStore.searchInStore(textForSearching);
    }

    @After
    public void tearDown() {
        driver.close();
    }

    @Then("^I should expect there is a result$")
    public void iShouldExpectThereIsAResult() throws Throwable {
        SearchResults searchResults = new SearchResults();
        searchResults.verifyResults();
    }

    @Then("^I should verify all buttons exist$")
    public void iShouldVerifyAllButtonsExist() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        HomePage homePage = new HomePage();
        homePage.verifyAllButtons();
    }
}
