package tests;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import com.applitools.eyes.BatchInfo;
import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.Region;
import com.applitools.eyes.selenium.Eyes;
import com.applitools.eyes.selenium.StitchMode;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.List;

public class CCTest {
	private static final Logger log = LogManager.getLogger(CCTest.class.getName());

    private BatchInfo batch;
    private ThreadLocal<WebDriver> thDriver = new ThreadLocal<WebDriver>();
    private ThreadLocal<Eyes> thEyes = new ThreadLocal<Eyes>();
    private ThreadLocal<String> url = new ThreadLocal<String>();
    private ThreadLocal<String> pageName = new ThreadLocal<String>();
    private ThreadLocal<Integer> numCards = new ThreadLocal<Integer>();
    static String driverPath = "/Users/aodiyo/Downloads/ApplitoolsPOC-CreditCards/";

    @BeforeSuite
    public void setUpSuite() {
        batch = new BatchInfo("CreditCards");
    }
    
    private synchronized Eyes getEyes()
    {
        return thEyes.get();
    }
    
    private synchronized WebDriver getDriver()
    {
        return thDriver.get();
    }
    
    private synchronized String getPageName()
    {
        return pageName.get();
    }
    private synchronized String getURL()
    {
        return url.get();
    }

    @Parameters({"url", "pageName", "browser"})
    @BeforeMethod
    public void setupMethod(@Optional String url, @Optional String pageName, @Optional String browser) throws InterruptedException {
    	
        this.url.set(url);
        this.pageName.set(pageName);

        // Open a Browser – if using CBT, change webDriver to RemoteWebDriver with CBT capabilities
	
        if (browser.equalsIgnoreCase("chrome")) {
        	System.setProperty("webdriver.chrome.driver", "chromedriver_2.38");
        	
        	 System.out.println("Launching google chrome with new profile..");
             this.thDriver.set(new ChromeDriver());
          
        } else if (browser.equalsIgnoreCase("Firefox")) {
        	System.setProperty("webdriver.gecko.driver",  "geckodriver_20.1");
        	
       	    System.out.println("Launching firefox with new profile..");
            this.thDriver.set(new FirefoxDriver());
        }
        // Initialize the eyes SDK
        thEyes.set(setupEyes());
       
    }

    // Applitools Settings
    private Eyes setupEyes() {
        Eyes eyes = new Eyes();
       
        eyes.setApiKey("API_KEY");
        eyes.setBatch(batch);
        eyes.setForceFullPageScreenshot(true);
        eyes.setStitchMode(StitchMode.CSS);
 
        return eyes;
    }

   @Test
    public void testFullPage() throws InterruptedException{
    	 // Go to url and load
	    getDriver().manage().window().maximize();
        getDriver().get(getURL());
        loadMore();

        try {
            fullPageTest();
        } finally {
            // If the test was aborted before eyes.close was called, ends the test as aborted.
            getEyes().abortIfNotClosed();
        }
    }
    @Test
    public void testCardDetails() throws InterruptedException {
        for (int i = 0; i < numCards.get(); i++) {
            try {
                getDriver().get(getURL());
                loadMore();
                detailsTest(i);
            } finally {
                // If the test was aborted before eyes.close was called, ends the test as aborted.
                getEyes().abortIfNotClosed();
            }
        }
    }
    private void fullPageTest() {
    	log.info("Starting the full page Test");
        getEyes().open(getDriver(), "CreditCards.com", this.pageName + "Full Page",
                new RectangleSize(1280, 800));
  
        // Navigate the browser to the "hello world!" web-site.
	    log.info("Navigating to the URL");
        getDriver().get(getURL());
        log.info("completed navigating to the url");
        
        log.info("Start to get body, height and width and where to split the page");
        int width = getDriver().findElement(By.cssSelector("body")).getSize().getWidth();
        int height = getDriver().findElement(By.cssSelector(".editorial-piece-hldr>h2")).getLocation().getY(); // TODO: CSS selector where you want to split
        int bottom = getDriver().findElement(By.cssSelector("body")).getSize().getHeight();
     
        // Full page check (split into 2 steps)
        getEyes().checkRegion(new Region(0,0, width, height), 2, "Zero-Interest Cards");
        getEyes().checkRegion(new Region(0, height, width, bottom), 2, "Zero-Interest Bottom");

        // make list of card elements
        List<WebElement> cardList = getDriver().findElements(By.cssSelector(".product-box-container"));
        numCards.set(cardList.size());
        
        // End the tests.
        getEyes().close(false);
        log.info("Full page test is complete");
    }
    private void detailsTest(int e) {
    	log.info("Starting the PDP detail Test");
        WebElement card = getDriver().findElements(By.cssSelector(".product-box-container")).get(e);
    
       // TODO: get card name information from card element (or children)
        
        String cardName = getDriver().findElement(By.cssSelector(".product-box__title__link")).getText();
        
        getEyes().open(getDriver(), "CreditCards.com", getPageName() + ": " + cardName,
                new RectangleSize(1280, 800));

        // TODO: click show more details button

        // Check details Card
        getEyes().checkRegion(card, "first card");

        getDriver().findElement(By.cssSelector(".product__card-details-link")).click(); // TODO: find Selector linking to card details

        // Check details page
        getEyes().checkWindow("card details");

        getEyes().close(false);
    	log.info("PDP detail Test complete");
    }


	@AfterMethod
    public void tearDown() {
        // Close the browser.
        getDriver().quit();
    }

    private void loadMore() throws InterruptedException {
    	
    	  scrollElementIntoView(".product__show-more-button");
    	  
    	  List<WebElement> showMore = getDriver().findElements(By.cssSelector(".product__show-more-button"));
    	  Iterator<WebElement> itr = showMore.iterator();
    	  while(itr.hasNext()) {
    		    WebElement showMoreButton = itr.next();
    		     log.info("start to click the show more button");
    		     showMoreButton.click();
    		     Thread.sleep(2000);
    		     log.info("show more button is clicked");
    		     
    		     
    		}
    	log.info("Completed loading more cards");
    	 
    	
      /** By showMore = By.cssSelector(".product__show-more-button");
        while(getDriver().findElement(showMore).size() != 0) {
            getDriver().findElement(showMore).click();
            // Might need to put a wait in here
            thDriver.wait(5000);
        }**/
    }
    private void scrollElementIntoView (String showMore){
    	WebElement element = getDriver().findElement(By.cssSelector(showMore));
    	WebElement header = getDriver().findElement(By.cssSelector(".boxy__menu"));

    	System.out.println("Element is: " + element);
    	
    	JavascriptExecutor jse = (JavascriptExecutor)getDriver();

    	jse.executeScript("arguments[0].scrollIntoView()", element); 
    	jse.executeScript("window.scrollBy(0, -arguments[1].offsetHeight)", element, header);
    
    }
    
    
}
