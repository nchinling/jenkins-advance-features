package sg.edu.nus.iss.lovecalculator.controllertests;


import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;


import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import sg.edu.nus.iss.lovecalculator.controller.AppController;
import sg.edu.nus.iss.lovecalculator.model.Calculator;
import sg.edu.nus.iss.lovecalculator.service.CalculatorService;

@WebMvcTest(AppController.class)
class AppControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CalculatorService calculatorService;

    @InjectMocks
    private AppController appController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetScoreSuccess() throws Exception {
        // Given
        Calculator calculator = new Calculator("John", "Jane", "85", "Love Match");
        when(calculatorService.getResult("John", "Jane")).thenReturn(Optional.of(calculator));

        // When & Then
        mockMvc.perform(get("/calculate")
                .param("fname", "John")
                .param("sname", "Jane"))
                .andExpect(status().isOk());
                // .andExpect(view().name("calculator"))
                // .andExpect(model().attributeExists("calculatorresults"))
                // .andExpect(model().attribute("calculatorresults", calculator));
    }


}
