package helpers;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.Augmenter;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Screenshot {

    public Screenshot screenshot;
    public static String mainPath = System.getProperty("user.dir");


    public void makeScreenshotByStepName(String screenshotStepName, WebDriver driver) throws IOException {
        try {
            screenshot = new Screenshot();
            String dest = createFolder("newScreenshots\\");
            screenshot.createScreenCaptureJPEG(screenshotStepName, dest, driver);
        } catch (Exception e) {
            System.out.println("makeScreenshotByStepName: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String createFolder(String dest) {
        boolean success = (new File(dest)).mkdirs();
        if (!success) {
            if (!(new File(dest)).isDirectory()) {
                System.out.println("Directory creation failed");
            }
        }
        return dest;
    }

    public void createScreenCaptureJPEG(String filename, String dest, WebDriver driver) {
        if (driver instanceof InternetExplorerDriver) {
            try {
                createScreenCaptureJPEGForIE(filename, dest, driver);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        ru.yandex.qatools.ashot.Screenshot myScreenshot = new AShot()
                .shootingStrategy(ShootingStrategies.viewportPasting(600))
                .takeScreenshot(driver);
        boolean success = (new File(mainPath + dest.replace(':', '-'))).mkdirs();
        if (!success) {
            if (!(new File(mainPath + dest.replace(':', '-'))).isDirectory()) {
                System.out.println("Directory creation failed");
            }
        }
        try {
            File output = new File(mainPath +"\\"+ dest.replaceAll(":", "-") + "\\" + filename.replaceAll(" ", "") + ".jpg");
            ImageIO.write(myScreenshot.getImage(), "jpg", output);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print(e.getMessage());
        }
    }

    public void createScreenCaptureJPEGForIE(String filename, String dest, WebDriver driver) throws IOException {
        try {
            Thread.sleep(1000);
        } catch (Exception e) {

        }
        WebDriver augmentedDriver = new Augmenter().augment(driver);
        File screenshot = null;
        if (augmentedDriver instanceof TakesScreenshot) {
            screenshot = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);
        } else {
            System.out.println("screenshot not taken");
        }
        //To debug from TESTNG just uncomment and edit the mainPath
//        mainPath = "C:\\Users\\alexb\\workspace3\\PPP.automation.regression\\";
        mainPath = System.getProperty("user.dir") + "\\";
        boolean success = (new File(mainPath + dest.replace(':', '-'))).mkdirs();
        if (!success) {
            if (!(new File(mainPath + dest.replace(':', '-'))).isDirectory()) {
                System.out.println("Directory creation failed");
            }
        }
        File output = new File(mainPath + dest.replaceAll(":", "-") + "\\" + filename.replaceAll(" ", "") + ".jpg");
        if (screenshot != null) {
            FileUtils.copyFile(screenshot, output);
            FileUtils.forceDeleteOnExit(screenshot);
        } else {
            System.out.println("screenshot was not taken");
        }
    }
}
