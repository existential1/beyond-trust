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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BeyondTrustTest {

	private WebDriver driver;
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
	private static final int POLLING = 60;
	private WebDriverWait wait;
	
	@BeforeClass
	private void init() throws IOException, InterruptedException {
		boolean isWindows = System.getProperty("os.name")
				  .toLowerCase().startsWith("windows");
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
		System.setProperty("webdriver.chrome.driver", HOME_DIR + PROJECT_BASE_PATH + CHROMEDRIVER_PATH);
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--remote-allow-origins=*");
		driver = new ChromeDriver(options);
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(POLLING));
		this.wait = new WebDriverWait(driver, Duration.ofSeconds(POLLING));
		driver.manage().window().maximize();
		driver.get(SIMPLE_APP_BASE_URL + SIMPLE_APP_PORT_DELIMITER + SIMPLE_APP_PORT);
	}
	
	@Test
	private void createTicket_required_fields_filled () {
		System.out.printf("Test 1\n");
		
		driver.findElement(By.xpath(CREATE_TICKET_XPATH)).click();
		
		for(int i = 0; i < requiredFields.size(); i++) {
			setTicketField(requiredFields.get(i),requiredFieldValues.get(i)); 
		}
		driver.findElement(By.xpath(SUBMIT_TICKET_XPATH)).click();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("mdc-list-item__content")));
		//wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text() = ' Dismiss ']")));
		//wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("mdc-button__label")));
	}
	
	@Test
	private void createTicket_required_field_empty () {
		System.out.printf("Test 2\n");
	}
	
	@Test
	private void createTicket_optional_field_invalid () {
		System.out.printf("Test 3\n");
	}
	
	private void setTicketField(String field, String value) {
		WebElement el = driver.findElement(By.xpath("//*[@formcontrolname='" + field + "']"));
		el.click();
		el.sendKeys(value);
		switch (field) {
		case "title":
		case "description":
		case "dateTime":
		case "videoUrl":
		case "contactEmail":
			break;
		case "applicationArea":
			driver.findElement(By.xpath("//span[text() = '" + value + "']")).click();
			break;
		case "tags":
			el.sendKeys(Keys.ENTER);
			break;
		}
	}
	
	@AfterMethod
	private void teardown() {
		if (driver != null) {
			driver.quit();		
		}
	}

}



