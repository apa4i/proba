package pages;

import helpers.WaitTool;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.List;

public class HomePage {
    WebDriver driver;
    public void verifyAllButtons(){
        List<WebElement> buttonsHomePage = new ArrayList<>();
       // WaitTool waitTool = new WaitTool();
        buttonsHomePage.addAll(driver.findElements(By.cssSelector("[id$=menu-item]")));

        for(WebElement ele: buttonsHomePage){
            System.out.print(ele.getText());
            Assert.assertTrue(ele.isDisplayed());
            //Moga da izpolzvam assertvam s isExists() (ili isDisplayed, kakto napravih na posledniq red gore.....
            // Posle moga da proverqvam s if dali skritite buttoni se pokazvat ("isDisplayed")
        }
    }
}
