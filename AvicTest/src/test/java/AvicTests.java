import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class AvicTests {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeTest
    public void profileSetUp() {
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");
    }

    @BeforeMethod
    public void testsSetUp() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();
        driver.get("https://avic.ua/ua");

        wait.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
    }

    @Test(priority = 1)
    public void checkThatNumberOfPartnersOnMainPageIsEqualFive() {
        List<WebElement> partnersList = driver.findElements(By.xpath("//div[@class= \"partner-box height\"]"));

        Assert.assertEquals(partnersList.size(), 5);
    }

    @Test(priority = 1)
    public void checkThatUrlContainsSearchWord() {
        driver.findElement(By.xpath("//input[@id = 'input_search']")).sendKeys("iphone 13", Keys.ENTER);

        Assert.assertTrue(driver.getCurrentUrl().contains("query=iphone"));
    }

    @Test(priority = 1)
    public void verifyThatPriceIsCorrectOnLightningEarpods() {
        driver.findElement(By.xpath("//input[@id = 'input_search']")).sendKeys("Наушники Apple EarPods with Lighting Connector (MMTN2)");
        driver.findElement(By.xpath("//button[@class = 'button-reset search-btn']")).click();
        WebElement webElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@data-product = '21135']//div[@class = 'prod-cart__prise-new']")));

        Assert.assertEquals(webElement.getText(), "699 грн");
    }

    @Test(priority = 1)
    public void checkThatEmailWidgetGotValidBackgroundColor() {
        driver.findElement(By.xpath("//a[@href = '/ua/brand-apple']")).click();
        WebElement webElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class = 'email-widget-bubble pp-visible']")));

        Assert.assertEquals(webElement.getCssValue("background-color"), "rgba(255, 90, 2, 1)");
    }

    @Test
    public void checkThatPriceIsAddingCorrectlyInCart() {
        driver.findElement(By.xpath("//input[@id = 'input_search']")).sendKeys("Razer mouse");
        driver.findElement(By.xpath("//button[@class = 'button-reset search-btn']")).click();

        WebElement buyButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[@class = 'prod-cart__buy']")));
        buyButton.click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text() = '+']")));

        int currentValue = Integer.parseInt(driver.findElement(By.xpath("//div[@class = 'item-total']//span[@class = 'prise']")).getText().replaceAll("[^0-9]", ""));
        String value;
        for (int i = 2; i < 11; i++) {
            value = currentValue * i + " грн";
            driver.findElement(By.xpath("//span[text() = '+']")).click();
            wait.until(ExpectedConditions.textToBe(By.xpath("//div[@class = 'item-total']//span[@class = 'prise']"), value));

        }
        WebElement pricePerOneItemElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class = 'total-h']//span")));
        WebElement pricePerAllItemsElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class = 'item-total']//span[@class = 'prise']")));

        int pricePerOneItemValue = Integer.parseInt(pricePerOneItemElement.getText().replaceAll("[^0-9]", ""));
        int pricePerTenItemsValue = Integer.parseInt(pricePerAllItemsElement.getText().replaceAll("[^0-9]", ""));

        Assert.assertEquals(pricePerTenItemsValue, pricePerOneItemValue * 10);

    }

    @Test(priority = 1)
    public void checkThatNewPriceIsLowerThanOldPrice() throws InterruptedException {
        List<WebElement> itemsOnSaleOldPriceList = driver.findElements(By.xpath("//div[@class ='prod-cart__prise-old']"));
        List<WebElement> itemsOnSaleNewPriceList = driver.findElements(By.xpath("//div[@class ='prod-cart__prise-new']"));
        List<Integer> oldPriceIntegerList = itemsOnSaleOldPriceList.stream()
                .filter(e -> !e.getText().equals(""))
                .map(e -> Integer.parseInt(e.getText().replaceAll("[^0-9]", "")))
                .collect(Collectors.toList());

        List<Integer> newPriceIntegerList = itemsOnSaleNewPriceList.stream()
                .filter(e -> !e.getText().equals(""))
                .map(e -> Integer.parseInt(e.getText().replaceAll("[^0-9]", "")))
                .collect(Collectors.toList());

        System.out.println();
        if (itemsOnSaleOldPriceList.size() != itemsOnSaleNewPriceList.size()) throw new IllegalArgumentException();
        for (int i = 0; i < oldPriceIntegerList.size(); i++) {
            Assert.assertTrue(newPriceIntegerList.get(i) < oldPriceIntegerList.get(i));
        }
    }


    @Test
    public void checkThatPriceFilterOptionIsWorking() throws InterruptedException {
        driver.findElement(By.xpath("//input[@id = 'input_search']")).sendKeys("Iphone", Keys.ENTER);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class = 'row-m']")));

        final int MIN_LIMIT = 60650;
        final int MAX_LIMIT = 72600;

        WebElement minPriceInputForm = driver.findElement(By.xpath("//input[@class = 'form-control form-control-min']"));
        WebElement maxPriceInputForm = driver.findElement(By.xpath("//input[@class = 'form-control form-control-max']"));

        new Actions(driver).doubleClick(minPriceInputForm).sendKeys(minPriceInputForm, String.valueOf(MIN_LIMIT)).build().perform();
        new Actions(driver).doubleClick(maxPriceInputForm).sendKeys(maxPriceInputForm, String.valueOf(MAX_LIMIT)).build().perform();

        WebElement showResults = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//div[@class = 'form-group filter-group js_filter_parent open-filter-tooltip']//a[@class='filter-tooltip js_filters_accept']")));

        showResults.click();

        List<WebElement> priceListWebElement = driver.findElements(By.xpath("//div[@class = 'prod-cart__prise-new']"));
        List<Integer> priceListInteger = priceListWebElement
                .stream()
                .map(webElement -> Integer.parseInt(webElement.getText().replaceAll("[^0-9]", "")))
                .collect(Collectors.toList());


        for (int price : priceListInteger) {
            Assert.assertTrue(price > MIN_LIMIT && price < MAX_LIMIT);
        }

    }


    @AfterMethod
    public void tearDown() {
        driver.quit();
    }

}
