package sg.edu.nus.iss.lovecalculator.modeltests;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.json.JsonObject;
import sg.edu.nus.iss.lovecalculator.model.Calculator;


class CalculatorTests {

    @Test
    void testCreateUserObject() throws IOException {
        // Given
        String json = "{\"fname\":\"John\",\"sname\":\"Jane\",\"percentage\":\"85\",\"result\":\"Love Match\"}";

        // When
        Calculator calculator = Calculator.createUserObject(json);

        // Then
        assertNotNull(calculator);
        assertEquals("John", calculator.getFname());
        assertEquals("Jane", calculator.getSname());
        assertEquals("85", calculator.getPercentage());
        assertEquals("Love Match", calculator.getResult());
        assertNotNull(calculator.getDataId());  // Ensure a dataId is generated
    }

    @Test
    void testCreateUserObjectFromRedis() throws IOException {
        // Given
        String json = "{\"dataId\":\"12345678\",\"fname\":\"John\",\"sname\":\"Jane\",\"percentage\":\"85\",\"result\":\"Love Match\"}";

        // When
        Calculator calculator = Calculator.createUserObjectFromRedis(json);

        // Then
        assertNotNull(calculator);
        assertEquals("12345678", calculator.getDataId());
        assertEquals("John", calculator.getFname());
        assertEquals("Jane", calculator.getSname());
        assertEquals("85", calculator.getPercentage());
        assertEquals("Love Match", calculator.getResult());
    }

    @Test
    void testToJSON() {
        // Given
        Calculator calculator = new Calculator("12345678", "John", "Jane", "85", "Love Match");

        // When
        JsonObject json = calculator.toJSON();

        // Then
        assertEquals("12345678", json.getString("dataId"));
        assertEquals("John", json.getString("fname"));
        assertEquals("Jane", json.getString("sname"));
        assertEquals("85", json.getString("percentage"));
        assertEquals("Love Match", json.getString("result"));
    }


    @Test
    void testToString() {
        // Given
        Calculator calculator = new Calculator("12345678", "John", "Jane", "85", "Love Match");

        // When
        String result = calculator.toString();

        // Then
        assertEquals("fname=John, sname=Jane, percentage=85, result=Love Match, dataId=12345678]", result);
    }
}
