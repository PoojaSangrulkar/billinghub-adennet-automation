package com.billinghub.radius;

import lombok.extern.slf4j.Slf4j;
import org.tinyradius.attribute.VendorSpecificAttribute;
import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.AccountingRequest;
import org.tinyradius.util.RadiusException;
import org.tinyradius.util.RadiusClient;
import org.tinyradius.attribute.RadiusAttribute;
import org.tinyradius.dictionary.AttributeType;
import org.tinyradius.dictionary.DefaultDictionary;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import static com.billinghub.radius.SeleniumTestMethods.*;
import static com.billinghub.radius.TestWalletTopUp.*;



@Slf4j
public class RadiusTestClientSelenium {
    private static final String RADIUS_SERVER_HOSTNAME = "192.168.1.113";//10.5.4.2 //192.168.1.181//10.6.0.1//10.6.0.5
    static final String SHARED_SECRET = "Adennet@123456789"; //Adennet-env: Adennet@123456789 Sarathi-env: sarathi
    private static String USER_NAME = "798901235"; //797979869
    private static final String USER_PASSWORD = "Jbilling#987"; //Adennet-env: Jbilling#987 Sarathi-env: sarathi
    //    private static final String SESSION_ID = "4b8e69fe-7ff9-4d93-afec-be6780053a74";//UUID.randomUUID().toString();
    private static String SESSION_ID; //="a0473ee3-17e6-405a-87c0-2fe64a5677bf";
    private static String APN = "aden.com"; // Individual : aden.com VIP: adennet.vip, Employee: test.net
    private static Long cumulativeAcctInputOctets = 0L;
    private static Long cumulativeAcctOutputOctets = 0L;
    private static Long ACCT_INPUT_OCTETS = Long.valueOf(1073741824L); //25165824L:7MB
    private static Long ACCT_OUTPUT_OCTETS = Long.valueOf(1073741824L); //1026MB
    private static final int RAT_TYPE = 6;
    private static final byte[] TIMEZONE = new byte[] {(byte) 0x21, (byte) 0x00};
    private static final byte[] USER_LOCATION_INFO = new byte[] {(byte) 0x82, (byte) 0x24, (byte) 0xf1, (byte) 0x50, (byte) 0x03, (byte) 0xeb, (byte) 0x24, (byte) 0xf1, (byte) 0x50, (byte) 0x00, (byte) 0x02, (byte) 0xd3, (byte) 0x0a};

    /*private static Long ACCT_INPUT_OCTETS = Long.valueOf(5242880L); //5242880L:5MB
    private static Long ACCT_OUTPUT_OCTETS = Long.valueOf(24117248L); //23MB
*/
    private static long _4_GB_IN_MB = 4294967296L;

    private static final RadiusClient RADIUS_CLIENT = new RadiusClient(RADIUS_SERVER_HOSTNAME, SHARED_SECRET);

    public static void main(String[] args) throws Exception {
        launchChrome();
        userLogin();
        validateHomePage();
        searchSubscriber(USER_NAME);
        availableDataBeforeRun();
        TimeUnit.SECONDS.sleep(2);
        int j=1;
        for (int i = 1; i <= 1; i++) {
            sendAccessRequest();
            sendAccountingRequest(1);   //start
            for (j=1; j <=2 ; j++) {                                  //((numberofpackets/2)-1)
                sendAccountingRequest(3);   //Interim-Update
            }
            sendAccountingRequest(2);
        }
        TimeUnit.SECONDS.sleep(2);
        SeleniumTestMethods.availableDataAfterRun();
        SeleniumTestMethods.validateRadiusRequests(j);
        SeleniumTestMethods.inspectCustomerPage();
        validateBHMR(SESSION_ID,j);
        SeleniumTestMethods.logoutCloseFileAndWindow();
    }

    private static void sendAccessRequest() throws IOException, RadiusException, InterruptedException {
        Thread.sleep(1000);
        SESSION_ID = UUID.randomUUID().toString();
        try {
            AccessRequest accessRequest = new AccessRequest(USER_NAME, USER_PASSWORD);
            accessRequest.setAuthProtocol(AccessRequest.AUTH_PAP); // or AUTH_CHAP
            accessRequest.addAttribute("NAS-Identifier", "this.is.my.nas-identifier.de");
            accessRequest.addAttribute("NAS-IP-Address", "192.168.0.100");
            long timeInSeconds = Calendar.getInstance().getTimeInMillis() / 1000;
            accessRequest.addAttribute("Acct-Multi-Session-Id", SESSION_ID);
            accessRequest.addAttribute("Event-Timestamp", Long.toString(timeInSeconds));
            accessRequest.addAttribute("Calling-Station-Id", USER_NAME);
            accessRequest.addAttribute("Called-Station-Id", APN);
            accessRequest.addAttribute("Service-Type", "Login-User");
//            accessRequest.addAttribute("WISPr-Redirection-URL", "http://www.sourceforge.net/");
//            accessRequest.addAttribute("WISPr-Location-ID", "net.sourceforge.ap1");
            VendorSpecificAttribute vsa = new VendorSpecificAttribute(10415);
            vsa.addSubAttribute("3GPP-RAT-Type", String.valueOf(RAT_TYPE));

            AttributeType type = DefaultDictionary.getDefaultDictionary().getAttributeTypeByName("3GPP-User-Location-Info");
            RadiusAttribute attribute = RadiusAttribute.createRadiusAttribute(DefaultDictionary.getDefaultDictionary(), type.getVendorId(), type.getTypeCode());
            attribute.setAttributeData(USER_LOCATION_INFO);
            vsa.addSubAttribute(attribute);

            AttributeType type2 = DefaultDictionary.getDefaultDictionary().getAttributeTypeByName("3GPP-MS-TimeZone");
            RadiusAttribute attribute2 = RadiusAttribute.createRadiusAttribute(DefaultDictionary.getDefaultDictionary(), type2.getVendorId(), type2.getTypeCode());
            attribute2.setAttributeData(TIMEZONE);
            vsa.addSubAttribute(attribute2);

            accessRequest.addAttribute(vsa);
            RADIUS_CLIENT.authenticate(accessRequest);
//            log.info("Access request response={}", RADIUS_CLIENT.authenticate(accessRequest));
        }
        finally {
            RADIUS_CLIENT.close();
        }
    }

    private static void sendAccountingRequest(int acctStatusType) throws IOException, RadiusException, InterruptedException {
        Thread.sleep(1000);
        try {
            AccountingRequest accountingRequest = new AccountingRequest(USER_NAME, acctStatusType);

            if (acctStatusType > 1) {
                cumulativeAcctInputOctets = cumulativeAcctInputOctets+(ACCT_INPUT_OCTETS); //random.nextInt(1000000000)
                cumulativeAcctOutputOctets = cumulativeAcctOutputOctets+(ACCT_INPUT_OCTETS); //random.nextInt(1000000000)
            }

            int inputGigawords = (int) (cumulativeAcctInputOctets/_4_GB_IN_MB);
            int outputGigawords = (int) (cumulativeAcctOutputOctets/_4_GB_IN_MB);

            if(inputGigawords > 0 || outputGigawords > 0){

             /*   if(accountingRequest.getAttribute("Acct-Input-Gigawords")!=null){
                    accountingRequest.removeAttribute(accountingRequest.getAttribute("Acct-Input-Gigawords"));
                }

                if(accountingRequest.getAttribute("Acct-Output-Gigawords")!=null){
                    accountingRequest.removeAttribute(accountingRequest.getAttribute("Acct-Output-Gigawords"));
                }*/

                accountingRequest.addAttribute("Acct-Input-Gigawords", String.valueOf(inputGigawords));
                accountingRequest.addAttribute("Acct-Output-Gigawords", String.valueOf(outputGigawords));
            }


            accountingRequest.addAttribute("Acct-Multi-Session-Id", SESSION_ID);
            accountingRequest.addAttribute("Acct-Session-Id", SESSION_ID);
            long timeInSeconds = Calendar.getInstance(TimeZone.getTimeZone("Asia/Aden")).getTimeInMillis() / 1000;

//            log.info("Calendar.getInstance(TimeZone.getTimeZone(\"Asia/Aden\"))={}", Calendar.getInstance(TimeZone.getTimeZone("Asia/Aden")).getTime());

            accountingRequest.addAttribute("Event-Timestamp", Long.toString(timeInSeconds));
            accountingRequest.addAttribute("NAS-Identifier", "this.is.my.nas-identifier.de");
            accountingRequest.addAttribute("NAS-Port", "0");
            accountingRequest.addAttribute("Acct-Input-Octets", cumulativeAcctInputOctets.toString());
            accountingRequest.addAttribute("Acct-Output-Octets", cumulativeAcctOutputOctets.toString());
            accountingRequest.addAttribute("Calling-Station-Id", USER_NAME);
            accountingRequest.addAttribute("Called-Station-Id", APN);

//            VendorSpecificAttribute attribute = new VendorSpecificAttribute();
//            attribute.setAttributeType(26);
//            attribute.setVendorId(-1);
//            attribute.setChildVendorId(14122);
//            attribute.addSubAttribute("WISPr-Location-ID", "101010");
//            accountingRequest.addAttribute(attribute);
//
//            VendorSpecificAttribute attribute2 = new VendorSpecificAttribute();
//            attribute2.setAttributeType(26);
//            attribute2.setVendorId(-1);
//            attribute2.setChildVendorId(14122);
//            attribute2.addSubAttribute("WISPr-Location-Name", "Test-Location");
//            accountingRequest.addAttribute(attribute2);
            VendorSpecificAttribute vsa = new VendorSpecificAttribute(10415);
            vsa.addSubAttribute("3GPP-RAT-Type", String.valueOf(RAT_TYPE));

            AttributeType type = DefaultDictionary.getDefaultDictionary().getAttributeTypeByName("3GPP-User-Location-Info");
            RadiusAttribute attribute = RadiusAttribute.createRadiusAttribute(DefaultDictionary.getDefaultDictionary(), type.getVendorId(), type.getTypeCode());
            attribute.setAttributeData(USER_LOCATION_INFO);
            vsa.addSubAttribute(attribute);

            AttributeType type2 = DefaultDictionary.getDefaultDictionary().getAttributeTypeByName("3GPP-MS-TimeZone");
            RadiusAttribute attribute2 = RadiusAttribute.createRadiusAttribute(DefaultDictionary.getDefaultDictionary(), type2.getVendorId(), type2.getTypeCode());
            attribute2.setAttributeData(TIMEZONE);
            vsa.addSubAttribute(attribute2);

            accountingRequest.addAttribute(vsa);

            accountingRequest.addAttribute("Acct-Input-Packets", "101966");
            accountingRequest.addAttribute("Acct-Output-Packets", "192112");
            accountingRequest.addAttribute("Framed-IP-Address", "192.168.1.127");
            accountingRequest.addAttribute("NAS-IP-Address", "192.168.1.127");
            RADIUS_CLIENT.account(accountingRequest);
//            log.info("Accounting Response={}", RADIUS_CLIENT.account(accountingRequest));

        }
        finally {
            RADIUS_CLIENT.close();
        }
    }
}
