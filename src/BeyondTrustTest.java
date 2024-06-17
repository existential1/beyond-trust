import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.openqa.selenium.support.ui.Select;

public class BeyondTrustTest {

	private WebDriver driver;
	private final String SIMPLE_APP_BASE_URL = "http://localhost";
	private final String SIMPLE_APP_PORT = "4200";
	private final String SIMPLE_APP_PORT_DELIMITER = ":";
	private final String SIMPLE_APP_API = "api";
	private final String HOME_DIR = System.getProperty("user.home");
	private final String CHROMEDRIVER_PATH = "/chromedriver.exe";
	private final String DOCKER_SCRIPT_PATH = "/docker.sh";
	private final String PROJECT_BASE_PATH = "/git/beyond-trust";	
	
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
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
		driver.manage().window().maximize();
		driver.get(SIMPLE_APP_BASE_URL + SIMPLE_APP_PORT_DELIMITER + SIMPLE_APP_PORT);
	}
	
	@Test
	private void test1 () {
		System.out.printf("Test 1\n");
	}
	
	@AfterMethod
	private void teardown() {
		if (driver != null) {
			driver.quit();		
		}
	}

}



