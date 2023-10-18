package com.billinghub.radius;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.Assert;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class SeleniumTestMethods {

    static WebDriver driver;
    static String availabledatabeforerun, availabledataafterrun;


    public static void launchChrome() throws InterruptedException {

        System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get("http://multiserver.billinghub.net/jbilling/login/auth");
    }
    public static void userLogin(){
        driver.findElement(By.cssSelector("[name='j_username']")).sendKeys("system admin");
        driver.findElement(By.cssSelector("[name='j_password']")).sendKeys("123qwe");
        driver.findElement(By.xpath("//html/body/div/div[2]/div[2]/div[2]/div/div[2]/form/fieldset/div[1]/div[3]/div/input")).sendKeys("60");
        // driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.findElement(By.id("submitLink")).click();

    }
    public static void validateHomePage(){
        WebElement exp = driver.findElement(By.xpath("/html/body/div[1]/div[1]/div/ul[2]/li[2]/a"));  //driver.findElement(By.id("home-logo"));
        boolean homelogo= exp.isEnabled();
        Assert.assertTrue(homelogo);
        System.out.println("Homo Logo Enabled: "+ homelogo);
    }
    public static void logoutCloseFileAndWindow(){
        driver.findElement(By.xpath("/html/body/div[1]/div[1]/div/ul[2]/li[2]/a/span")).click();
        driver.findElement(By.className("logout")).click();
        driver.close();
    }
    public static void searchSubscriber(String SubNum) throws InterruptedException {
        driver.findElement(By.cssSelector("[href='/jbilling/customer/index']")).click();
        driver.findElement(By.id("filters.CUSTOMER-LIKE_As_subscriberNumber.stringValue")).sendKeys(SubNum);
        driver.findElement(By.cssSelector("[onclick='submitApply();']")).click();
        Thread.sleep(1000);
        String actualsubscribernumber = driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[3]/div[3]/div[1]/div/div[2]/table/tbody/tr/td[3]/a")).getText();
        Assert.assertEquals(actualsubscribernumber,SubNum);
        driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[3]/div[3]/div[1]/div/div[2]/table/tbody/tr/td/a")).click();
        Thread.sleep(1000);

    }
    public static String availableDataBeforeRun() throws InterruptedException {
        Thread.sleep(1000);
//        WebElement ad = driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[3]/div[3]/div[2]/div/table/tbody/tr[1]/td[4]"));
        availabledatabeforerun = driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[3]/div[3]/div[2]/div/table/tbody/tr[1]/td[4]")).getText();
        System.out.println("Available Data Before Run: "+ availabledatabeforerun);
        availabledatabeforerun = availabledatabeforerun.replace(" GB","");
        return availabledatabeforerun;
    }
    public static void availableDataAfterRun() throws InterruptedException {
        Thread.sleep(1000);
        driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[3]/div[3]/div[1]/div/div[2]/table/tbody/tr/td[3]/a")).click();
        Thread.sleep(1000);

        availabledataafterrun = driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[3]/div[3]/div[2]/div/table/tbody/tr[1]/td[4]")).getText();
        System.out.println("Available Data After Run: "+ availabledataafterrun);
        availabledataafterrun = availabledataafterrun.replace(" GB","");
//        float availabledata = Float.parseFloat(availabledataafterrun);
//        float totaldatainiterations = iterations*2;
//        float expecteddata = Float.parseFloat(availabledatabeforerun)-totaldatainiterations;
//        Assert.assertNotEquals(expecteddata,availabledataafterrun);
////        Assert.assertEquals(availabledatabeforerun,availabledataafterrun);
//        System.out.println("Total data consumed: "+ totaldatainiterations+" GB");
    }
    public static void validateRadiusRequests(int iterations){
        float availabledata = Float.parseFloat(availabledataafterrun);
        float totaldatainiterations = iterations*2;
        float expecteddata = Float.parseFloat(availabledatabeforerun)-totaldatainiterations;
        Assert.assertNotEquals(expecteddata,availabledataafterrun);
        System.out.println("Total data consumed: "+ totaldatainiterations+" GB");
    }

    public static void inspectCustomerPage() throws InterruptedException {
        driver.findElement(By.cssSelector("[title='Inspect this customer']")).click();
        driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[4]/div[2]/fieldset/div[4]/div[1]/a/span")).click();
        TimeUnit.SECONDS.sleep(2);
        String availbalequantityincum = driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[4]/div[2]/fieldset/div[4]/div[2]/div/div[1]/div/table/tbody/tr[1]/td[7]")).getText();
        System.out.println("Available quantity in consumption usage map: "+ availbalequantityincum);
        Assert.assertEquals(availbalequantityincum,availabledataafterrun);

    }
    public static void validateBHMR(String sessionid, int iterations){
        driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[4]/div[2]/fieldset/div[4]/div[2]/div/div[1]/div/table/tbody/tr[1]/td[1]/a")).click();
        driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
        List<WebElement> avpstring = driver.findElements(By.xpath("/html/body/div[1]/div[2]/div[4]/div[2]/div[1]/div/table/tbody/tr/td[5]"));
        int count=0;
        for (WebElement e: avpstring) {
            String s= e.getText();
            if (s.contains(sessionid)){
                count++;
            }
        }
        System.out.println("BHMR records created: "+ count);
        Assert.assertEquals(count,iterations);
        String avpstring1 = driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[4]/div[2]/div[1]/div/table/tbody/tr[1]/td[5]")).getText();
        System.out.println("AVP string: "+ avpstring1);
    }
    public static void validateNewCUM(String systemdate){
      String statusold=driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[4]/div[2]/fieldset/div[4]/div[2]/div/div[1]/div/table/tbody/tr[2]/td[8]")).getText();
      Assert.assertEquals(statusold,"FINISHED");
        String statusrenewed=driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[4]/div[2]/fieldset/div[4]/div[2]/div/div[1]/div/table/tbody/tr[1]/td[8]")).getText();
        Assert.assertEquals(statusrenewed,"ACTIVE");
        System.out.println("New consumption usage map is created");
        String startdate=driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[4]/div[2]/fieldset/div[4]/div[2]/div/div[1]/div/table/tbody/tr[1]/td[3]")).getText();
        System.out.println("Consumption usage map start date and time: "+startdate);
        Assert.assertEquals(startdate,systemdate);

    }
    public static void validateAutoRenewal(int iterations){
        float availabledata = Float.parseFloat(availabledataafterrun);
        float totaldatainiterations = iterations*2;
//        float expecteddata = Float.parseFloat(availabledatabeforerun)-totaldatainiterations;
//        Assert.assertNotEquals(expecteddata,availabledataafterrun);
        Assert.assertEquals(availabledatabeforerun,availabledataafterrun);
        System.out.println("Total data consumed: "+ totaldatainiterations+" GB");
    }





}


