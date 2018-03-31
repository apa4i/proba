package helpers;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverEventListener;

import java.io.IOException;

public class MyEventListener implements WebDriverEventListener {
    private int currentStep = 0;


    @Override
    public void afterChangeValueOf(WebElement arg0, WebDriver arg1, CharSequence[] charSequences) {
        try {
            String stepName = null;
            Screenshot screenshot = new Screenshot();
            String element = "";
            /**
             * TODO fix name of the remote and local webdriver
             */
            if (arg0.toString().replaceAll("[\\W]", "_").length() >= 100 || arg0.toString().contains("RemoteWebDriver")
                    || arg0.toString().contains("on XP")) {
                if (arg0.toString().split("->").length == 1) {
                    element = arg0.toString().replaceAll("[\\W]", "_").substring(72);
                } else if (arg0.toString().split("->").length >= 2) {
                    element = arg0.toString().split("->")[1].replaceAll("[\\W]", "_");
                } else {
                    System.out.println("null");
                    System.out.println(arg0.toString());
                }

            } else {
                element = arg0.toString().replaceAll("[\\W]", "_");
            }
            stepName = "afterChangeValueOf" + element;
            if (arg1.toString().contains("Firefox")) {
                stepName = stepName + "Firefox";
            } else if (arg1.toString().contains("Chrome")) {
                stepName = stepName + "Chrome";
            }
            if (stepName.length() >= 108) {
                stepName = stepName.substring(0, 103);
            }

            currentStep++;
            screenshot.makeScreenshotByStepName(currentStep + "_" + stepName, arg1);
        } catch (Exception e) {
            System.out.println("beforeClickOn: " + e.getMessage());
        }
    }

    @Override
    public void beforeScript(String s, WebDriver webDriver) {

    }

    @Override
    public void beforeClickOn(WebElement arg0, WebDriver arg1) {
        try {
            Screenshot screenshot = new Screenshot();
            String element = "";
            String stepName;
            /**
             * TODO fix name of the remote and local webdriver
             */
            if (arg0.toString().replaceAll("[\\W]", "_").length() >= 100 || arg0.toString().contains("RemoteWebDriver")
                    || arg0.toString().contains("on XP")) {
                if (arg0.toString().split("->").length == 1) {
                    element = arg0.toString().replaceAll("[\\W]", "_").substring(72);
                } else if (arg0.toString().split("->").length >= 2) {
                    element = arg0.toString().split("->")[1].replaceAll("[\\W]", "_");
                } else {
                    System.out.println("null");
                    System.out.println(arg0.toString());
                }

            } else {
                element = arg0.toString().replaceAll("[\\W]", "_");
            }
            stepName = "beforeClickOn_" + element;
            if (arg1.toString().contains("Firefox")) {
                stepName = stepName + "Firefox";
            } else if (arg1.toString().contains("Chrome")) {
                stepName = stepName + "Chrome";
            }

            if (stepName.length() >= 108) {
                stepName = stepName.substring(0, 103);
            }
            currentStep++;
            screenshot.makeScreenshotByStepName(currentStep + "_" + stepName, arg1);
        } catch (Exception e) {
            System.out.println("beforeClickOn: " + e.getMessage());
        }
    }

    @Override
    public void afterClickOn(WebElement arg0, WebDriver arg1) {
        try {
            Screenshot screenshot = new Screenshot();
            String element = null;
            if (arg0.toString().replaceAll("[\\W]", "_").length() >= 100 || arg0.toString().contains("RemoteWebDriver") || arg0.toString().contains("on XP")) {


                if (arg0.toString().split("->").length == 1) {
                    element = arg0.toString().replaceAll("[\\W]", "_").substring(72);
                } else if (arg0.toString().split("->").length >= 2) {
                    element = arg0.toString().split("->")[1].replaceAll("[\\W]", "_");
                } else {
                    System.out.println("nullll");
                    System.out.println(arg0.toString());
                }

            } else {
                element = arg0.toString().replaceAll("[\\W]", "_");
            }
            String stepName = "afterClickOn_" + element;
            if (stepName.length() >= 108) {
                stepName = stepName.substring(0, 103);
            }
            if (arg1.toString().contains("Firefox")) {
                stepName = stepName + "Firefox";
            } else if (arg1.toString().contains("Chrome")) {
                stepName = stepName + "Chrome";
            }
            currentStep++;
            Thread.sleep(1000);
            screenshot.makeScreenshotByStepName(currentStep + "_" + stepName, arg1);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void beforeChangeValueOf(WebElement webElement, WebDriver webDriver, CharSequence[] charSequences) {

    }

    @Override
    public void afterScript(String arg0, WebDriver arg1) {
    }

    @Override
    public void onException(Throwable arg0, WebDriver arg1) {
        try {
            Screenshot screenshot = new Screenshot();
            String element = "";
            String stepName;
            /**
             * TODO fix name of the remote and local webdriver
             */
            if (arg0.toString().replaceAll("[\\W]", "_").length() >= 100 || arg0.toString().contains("RemoteWebDriver")
                    || arg0.toString().contains("on XP")) {
                if (arg0.toString().split("->").length == 1) {
                    element = arg0.toString().replaceAll("[\\W]", "_").substring(72);
                } else if (arg0.toString().split("->").length >= 2) {
                    element = arg0.toString().split("->")[1].replaceAll("[\\W]", "_");
                } else {
                    System.out.println("null");
                    System.out.println(arg0.toString());
                }

            } else {
                element = arg0.toString().replaceAll("[\\W]", "_");
            }
            stepName = "EXCEPTION_" + element;
            if (arg1.toString().contains("Firefox")) {
                stepName = stepName + "Firefox";
            } else if (arg1.toString().contains("Chrome")) {
                stepName = stepName + "Chrome";
            }
            if (stepName.length() >= 108) {
                stepName = stepName.substring(0, 103);

            }
            currentStep++;
            screenshot.makeScreenshotByStepName(currentStep + "_" + stepName, arg1);
        } catch (Exception e) {
            System.out.println("beforeClickOn: " + e.getMessage());
        }
    }

    @Override
    public void afterFindBy(By arg0, WebElement arg1, WebDriver arg2) {
    }

    @Override
    public void afterNavigateBack(WebDriver arg0) {

    }

    @Override
    public void beforeNavigateForward(WebDriver webDriver) {

    }

    @Override
    public void afterNavigateForward(WebDriver arg0) {

    }

    @Override
    public void beforeNavigateRefresh(WebDriver webDriver) {

    }

    @Override
    public void afterNavigateRefresh(WebDriver webDriver) {

    }

    @Override
    public void beforeFindBy(By by, WebElement webElement, WebDriver webDriver) {

    }

    @Override
    public void beforeAlertAccept(WebDriver webDriver) {

    }

    @Override
    public void afterAlertAccept(WebDriver webDriver) {

    }

    @Override
    public void afterAlertDismiss(WebDriver webDriver) {

    }

    @Override
    public void beforeAlertDismiss(WebDriver webDriver) {

    }

    @Override
    public void beforeNavigateTo(String s, WebDriver webDriver) {

    }

    @Override
    public void afterNavigateTo(String arg0, WebDriver arg1) {

    }

    @Override
    public void beforeNavigateBack(WebDriver webDriver) {

    }
}
