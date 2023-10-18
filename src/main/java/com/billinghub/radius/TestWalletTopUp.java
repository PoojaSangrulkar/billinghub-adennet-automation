package com.billinghub.radius;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.concurrent.TimeUnit;

import static com.billinghub.radius.SeleniumTestMethods.*;

public class TestWalletTopUp {

    static String exp="NEW RECHARGE";
    static String expsubscribernumber,totalrechargeamount;
    static String expoperationtype = "Top up";
    static Double tra,wb;



/*    @Test
    public static void searchSubscriber(String SubNum) throws InterruptedException {
        driver.findElement(By.id("filters.CUSTOMER-LIKE_As_subscriberNumber.stringValue")).sendKeys(SubNum);
        driver.findElement(By.cssSelector("[onclick='submitApply();']")).click();
        Thread.sleep(1000);
        expsubscribernumber = driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[3]/div[3]/div[1]/div/div[2]/table/tbody/tr/td[3]/a")).getText();
        driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[3]/div[3]/div[1]/div/div[2]/table/tbody/tr/td/a")).click();
        Thread.sleep(1000);
    } */

    @Test
    public static void clickOnRecharge(){
        driver.manage().timeouts().implicitlyWait(10,TimeUnit.SECONDS);
        driver.findElement(By.linkText("Recharge")).click();
        String act = driver.findElement(By.xpath("//*[@id=\"main\"]/div[4]/div[1]/strong")).getText();
        //System.out.println(act);
        Assert.assertEquals(exp,act);
        System.out.println("Recharge Screen: "+act);
    }
    @Test
    public static void validateRechargeScreen() throws InterruptedException {
        Thread.sleep(1000);

        Double pp,df,ta;
        String actualsubscribernumber = driver.findElement(By.id("subscriberNumber")).getAttribute("value");
        System.out.println("Actual Subscriber Number is: "+actualsubscribernumber);
 //       Assert.assertEquals(actualsubscribernumber,expsubscribernumber);
        String planprice = driver.findElement(By.id("labelPlanFee")).getText();
        pp = Double.parseDouble(planprice);
        System.out.println("Plan Price: "+pp);
        String downgradefee = driver.findElement(By.id("downgradeFees")).getText();
        df = Double.parseDouble(downgradefee);
        System.out.println("Downgrade Fee: "+df);
        String totalamount = driver.findElement(By.id("totalAmount")).getText();
        ta = Double.parseDouble(totalamount);
        System.out.println("Total Amount: "+ta);
        totalrechargeamount = driver.findElement(By.id("rechargeAmount")).getAttribute("value");
        //System.out.println(totalrechargeamount);
        tra = Double.parseDouble(totalrechargeamount);
        System.out.println("Total Recharge Amount: "+tra);
        Assert.assertEquals(ta,(pp+df));
        Assert.assertEquals(ta,tra);
    }

    @Test
    public static void validateWalletTopUp(){
        driver.findElement(By.id("btnRecharge")).click();
        String transactionstatus=driver.findElement(By.id("flash-msg")).getText();
        System.out.println("Transaction Status: "+transactionstatus);

    }
    @Test
    public static void validateReceipt(){
        Double raor;
        String rechargeamountonreceipt = driver.findElement(By.id("valuePaymentAmount")).getText();
        raor = Double.parseDouble(rechargeamountonreceipt);
        System.out.println("Recharge amount on receipt: "+raor);
        Assert.assertEquals(tra,raor);
        String actoperationtype = driver.findElement(By.id("valueOperationType")).getText();
        System.out.println("Operation type: "+actoperationtype);
        Assert.assertEquals(actoperationtype,expoperationtype);
        WebElement printtab = driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[4]/div[2]/form/fieldset/div[2]/ul/li[1]/a/span"));
        Boolean printtabisenabled = printtab.isEnabled();
        System.out.println("Print tab is enabled: " + printtabisenabled);
        WebElement closetab = driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[4]/div[2]/form/fieldset/div[2]/ul/li[2]/a/span"));
        Boolean closetabisenabled =closetab.isEnabled();
        System.out.println("Close Tab is enabled: "+closetabisenabled);
        closetab.click();
    }

    @Test
    public static void validateWalletBalanceAfterTopup() throws InterruptedException {
        driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[3]/div[3]/div[1]/div/div[2]/table/tbody/tr/td/a")).click();
        Thread.sleep(2000);
        String walletbalance = driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[3]/div[3]/div[2]/div/div[9]/div/table/tbody/tr[11]/td[2]")).getText();
        System.out.println("Wallet Balance before run: "+walletbalance);
        Assert.assertEquals(("RY"+tra+"0"),walletbalance.replaceAll(",",""));
    }
    public static void validateWalletBalanceAfterAutorenewal() throws InterruptedException {
        driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[3]/div[3]/div[1]/div/div[2]/table/tbody/tr/td/a")).click();
        Thread.sleep(2000);
        String walletbalance = driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[3]/div[3]/div[2]/div/div[9]/div/table/tbody/tr[11]/td[2]")).getText();
        System.out.println("Wallet Balance after run: "+walletbalance);
        Assert.assertEquals(("RY0.00"),walletbalance);

    }

}