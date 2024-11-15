package com.cloudexpo;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

public class CloudExpo {
    public static String BASE_URL = "https://cloudexpo.nl/exposanten/";
    public static WebDriver driver;

    public static void main(String[] args) throws IOException {
        String tasksFile = "cloud_expo.csv";
        List<String> list = new LinkedList<>();
        BufferedWriter taskWriter = Files.newBufferedWriter(Paths.get(tasksFile));
        CSVPrinter taskPrinter = new CSVPrinter(taskWriter, CSVFormat.DEFAULT.builder().setHeader("CompanyURL", "CompanyName", "CompanyDescription").setTrim(true).build());

        By cookieBtn = By.xpath("//button[@data-cky-tag='accept-button']");
        By langCode = By.xpath("//span[@class='gt-lang-code']");
        By engLang = By.xpath("//a[@data-gt-lang='en']");
        By companies = By.xpath("//*[contains(@data-url,'https://cloudexpo.nl/exposanten/')]");
        By title = By.xpath("//h1[@class='elementor-heading-title elementor-size-default']");
        By desc = By.xpath("//div[@data-widget_type='theme-post-content.default']");

//        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.get(BASE_URL);
        waitForElement(cookieBtn).click();
        waitForElement(langCode).click();
        waitForElement(engLang).click();

        List<WebElement> companyList = driver.findElements(companies);
        for (; companyList.size() != 167; ) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.scrollBy(0, 800)", "");
            sleep(2);
            companyList = driver.findElements(companies);
        }

        System.out.println(companyList.size());
        for (WebElement company : companyList) {
            list.add(company.getAttribute("data-url"));
        }

        int i = 0;
        driver.switchTo().newWindow(WindowType.TAB);
        for (String str : list) {
            System.out.println(i++ + ". " + str);
            driver.get(str);
            String titleName = !driver.findElements(title).isEmpty() ? driver.findElement(title).getText() : "";
            String descName = !driver.findElements(desc).isEmpty() ? driver.findElement(desc).getText() : "";
            taskPrinter.printRecord(str, titleName, descName);
        }
        taskPrinter.flush();
        driver.quit();
    }

    public static WebElement waitForElement(By by) {
        FluentWait<WebDriver> wait = new FluentWait<>(driver);
        wait.withTimeout(Duration.ofSeconds(30));
        wait.pollingEvery(Duration.ofMillis(500));
        wait.ignoring(NoSuchElementException.class);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public static void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
