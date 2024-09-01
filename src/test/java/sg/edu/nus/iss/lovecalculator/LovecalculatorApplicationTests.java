package sg.edu.nus.iss.lovecalculator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;


@SpringBootTest
@TestPropertySource(properties = {
    "LOVE_CALCULATOR_API_URL=http://example.com/api",
	"LOVE_CALCULATOR_API_KEY=exampekey",
	"LOVE_CALCULATOR_API_HOST=examplehost"


})
class LovecalculatorApplicationTests {

	@Test
	void contextLoads() {
	}

}
