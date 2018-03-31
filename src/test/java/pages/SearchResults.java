package pages;

import glue.steps.Browser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;

import java.util.ArrayList;
import java.util.List;

public class SearchResults {
    Browser browser = new Browser();

    public void verifyResults() {
        List<WebElement> results = browser.driver.findElements(By.cssSelector("h1"));
        String foundText = "";
        if (results.size() > 1) {
            foundText = results.get(1).getText().toString();
        } else {
            foundText = results.get(0).getText().toString();
        }
        System.out.println(foundText);
        Assert.assertNotNull(foundText);
    }

    public void close() {
        browser.quit();
    }

    public Search Search() {
        Search search = new Search();
        return search;
    }
}