import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BeyondTrustTest {

	private WebDriver driver;
	ChromeOptions options = new ChromeOptions();
	List<String> requiredFields = new ArrayList<>(Arrays.asList("title", "applicationArea", "description", "dateTime"));
	List<String> requiredFieldValues = new ArrayList<>(Arrays.asList("Title", "Settings", "Description", "01/01/1970"));
	private final String SIMPLE_APP_BASE_URL = "http://localhost";
	private final String SIMPLE_APP_PORT = "4200";
	private final String SIMPLE_APP_PORT_DELIMITER = ":";
	private final String SIMPLE_APP_API = "api";
	private final String HOME_DIR = System.getProperty("user.home");
	private final String CHROMEDRIVER_PATH = "/chromedriver.exe";
	private final String DOCKER_SCRIPT_PATH = "/docker.sh";
	private final String PROJECT_BASE_PATH = "/git/beyond-trust";
	private final String CREATE_TICKET_XPATH = "//*[@href='/create']";
	private final String SUBMIT_TICKET_XPATH = "/html/body/app-root/app-item/app-item-form/form/button";
	private final String STATUS_MESSAGE_CSS_SELECTOR = "div.mat-mdc-snack-bar-label.mdc-snackbar__label";
	private final String CREATE_SUCCESS_MESSAGE = "Item created successfully";
	private final String DELETE_SUCCESS_MESSAGE = "Item deleted successfully";
	private final String ERROR_MESSAGE = "Error occurred, check the network tab on your browser's developer tools for more details";
	private final String BAD_VIDEO_URL_INPUT = "blah";
	private static final int POLLING = 60;
	private WebDriverWait wait;
	
	@BeforeClass
	private void init() throws IOException, InterruptedException {
		boolean isWindows = System.getProperty("os.name")
				  .toLowerCase().startsWith("windows");
		System.setProperty("webdriver.chrome.driver", HOME_DIR + PROJECT_BASE_PATH + CHROMEDRIVER_PATH);
		ProcessBuilder builder = new ProcessBuilder();
		if (isWindows) {
			builder.command("cmd.exe", "/c", HOME_DIR + PROJECT_BASE_PATH + DOCKER_SCRIPT_PATH);
		} else {
			builder.command("sh", "-c", "./docker.sh");
		}
		builder.directory(new File(HOME_DIR + PROJECT_BASE_PATH));
		Process process = builder.start();
		StreamInput streamInput = 
		  new StreamInput(process.getInputStream(), System.out::println);
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		executorService.submit(streamInput);
		process.waitFor();
	}
	
	@BeforeMethod
	private void setup() {
	
	}
	
	@Test
	private void createTicket_succeed_required_fields_filled () {
		initializeDriver();
		driver.findElement(By.xpath(CREATE_TICKET_XPATH)).click();
		populateRequiredFields(-1);
		driver.findElement(By.xpath(SUBMIT_TICKET_XPATH)).click();
		Assert.assertFalse(checkForRequiredFieldError());
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(STATUS_MESSAGE_CSS_SELECTOR)));
		String message = driver.findElement(By.cssSelector(STATUS_MESSAGE_CSS_SELECTOR)).getText();
		Assert.assertEquals(message, CREATE_SUCCESS_MESSAGE);
	}
	
	@Test
	private void createTicket_fail_required_field_empty () {
		initializeDriver();
		driver.findElement(By.xpath(CREATE_TICKET_XPATH)).click();
		populateRequiredFields(1);
		driver.findElement(By.xpath(SUBMIT_TICKET_XPATH)).click();
		Assert.assertTrue(checkForRequiredFieldError());					
	}
	
	@Test
	private void createTicket_fail_optional_field_invalid () {
		initializeDriver();
		driver.findElement(By.xpath(CREATE_TICKET_XPATH)).click();
		populateRequiredFields(-1);
		setTicketField("videoUrl", BAD_VIDEO_URL_INPUT);
		driver.findElement(By.xpath(SUBMIT_TICKET_XPATH)).click();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(STATUS_MESSAGE_CSS_SELECTOR)));
		String message = driver.findElement(By.cssSelector(STATUS_MESSAGE_CSS_SELECTOR)).getText();
		Assert.assertEquals(message, ERROR_MESSAGE);		
	}
	
	private void setTicketField(String field, String value) {
		WebElement el = driver.findElement(By.xpath("//*[@formcontrolname='" + field + "']"));
		el.click();
		switch (field) {
		case "title":
		case "description":
		case "dateTime":
		case "videoUrl":
		case "contactEmail":
			el.sendKeys(value);
			break;
		case "applicationArea":
			driver.findElement(By.xpath("//span[text() = '" + value + "']")).click();
			break;
		case "tags":
			el.sendKeys(Keys.ENTER);
			break;
		}
	}
	
	private void initializeDriver() {
		options.addArguments("--remote-allow-origins=*");
		driver = new ChromeDriver(options);
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(POLLING));
		this.wait = new WebDriverWait(driver, Duration.ofSeconds(POLLING));
		driver.manage().window().maximize();
		driver.get(SIMPLE_APP_BASE_URL + SIMPLE_APP_PORT_DELIMITER + SIMPLE_APP_PORT);		
	}
	
	private void populateRequiredFields(int skip) {
		for(int i = 0; i < requiredFields.size(); i++) {
			if(i == skip) {
				continue;
			}
			setTicketField(requiredFields.get(i),requiredFieldValues.get(i)); 
		}
	}
	
	private boolean checkForRequiredFieldError() {
		boolean error = false;
		
		WebElement wel = driver.findElement(By.xpath("/html/body/app-root/app-item/app-item-form/form"));
		if(wel.getAttribute("class").contains("ng-invalid")) {
			List<WebElement> fields = driver.findElements(By.tagName("mat-form-field"));
			for (WebElement field : fields) {
				if(field.getAttribute("class").contains("ng-invalid")) {
					error = true;
				}
			}
		}
		
		return(error);
	}
	
	@AfterMethod
	private void teardown() {
		if (driver != null) {
			driver.quit();		
		}
	}

}



