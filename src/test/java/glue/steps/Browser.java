package glue.steps;

import helpers.MyEventListener;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;

import java.net.MalformedURLException;
import java.net.URL;

public class Browser {

	public static WebDriver driver;
	WebDriverEventListener eventListener = new MyEventListener();

	public void openRemoteWebDriver(String browser, String address) throws MalformedURLException {
		driver = getRemoteChromeDriver(browser, new URL(address));
	}

	public void openLocalWebDriver(String browser) {
		driver = getBrowser(browser);
	}

	public WebDriver getBrowser(String browser) {
		if (browser.equalsIgnoreCase("chrome")) {
			System.setProperty("webdriver.chrome.driver", "browsers\\chromedriver.exe");
			driver = new EventFiringWebDriver(new ChromeDriver()).register(eventListener);
			driver.manage().window().maximize();
		} else if (browser.equalsIgnoreCase("firefox")) {
			System.setProperty("webdriver.gecko.driver", "browsers\\geckodriver.exe");
			driver = new EventFiringWebDriver(new FirefoxDriver()).register(eventListener);
			driver.manage().window().maximize();
		}
		return driver;
	}


	private WebDriver getRemoteChromeDriver(String browser, URL remoteAddress) {
		DesiredCapabilities capability = null;
		if (browser.equalsIgnoreCase("chrome")) {
			capability = DesiredCapabilities.chrome();
		} else if (browser.equalsIgnoreCase("firefox")) {
			capability = DesiredCapabilities.firefox();

		}
		driver = new RemoteWebDriver(remoteAddress, capability);
		driver.manage().window().maximize();
		return driver;
	}

	public WebDriver driver() {
		return driver;
	}
	public void quit() {
		driver.quit();
	}
}
