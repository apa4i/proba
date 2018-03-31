package pages;

import glue.steps.Browser;
import helpers.WaitTool;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class Search {
    Browser browser = new Browser();
    private WebElement search;
    private WebElement searchButton;


    public void searchInStore(String query) {
        WaitTool waitTool = new WaitTool();
        searchButton = waitTool.waitForElement(browser.driver(), By.className("searchlens"), 10);
        search= waitTool.waitForElement(browser.driver, By.id("s"), 10);
        searchButton.click();
        JavascriptExecutor executor = (JavascriptExecutor) browser.driver;
        try {
            executor.executeScript("document.getElementById('s').setAttribute('onfocus', '')");
        } catch (Exception e) {
            System.out.println("Some java script exception");
        }
        try {
            executor.executeScript("document.getElementById('s').setAttribute('value', 'automation')");
        } catch (Exception e) {
            System.out.println("Some java script exception");
        }
        try{
            search.submit();
        }catch (Exception e){

        }
    }
}
