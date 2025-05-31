package com.dogood.dogoodbackend.domain.volunteering;

import com.dogood.dogoodbackend.domain.volunteerings.BarcodeHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BarcodeHandlerTests {
    private BarcodeHandler handler;
    @BeforeEach
    public void setUp() {
        handler = new BarcodeHandler();
    }

    @Test
    public void whenCodeValid_givenValidCode_shouldReturnTrue() {
        String code = handler.generateCode();
        Assertions.assertTrue(handler.codeValid(code));
    }

    @Test
    public void whenCodeValid_givenValidConstantCode_shouldReturnTrue() {
        String code = handler.generateConstantCode();
        Assertions.assertTrue(handler.codeValid(code));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"AAAAA-FFFFFF-CCCCC-HHHHH", ""})
    public void whenCodeValid_givenInvalidCode_shouldReturnFalse(String invalidCode) {
        handler.generateConstantCode();
        Assertions.assertFalse(handler.codeValid(invalidCode));
    }

    @Test
    public void whenCodeValid_givenTimedOutCode_shouldReturnFalse() throws InterruptedException {
        String code = handler.generateCode();
        Thread.sleep(15000);
        Assertions.assertFalse(handler.codeValid(code));
    }

    @Test
    public void whenCodeValid_givenTimedOutConstantCode_shouldReturnTrue() throws InterruptedException {
        String code = handler.generateConstantCode();
        Thread.sleep(15000);
        Assertions.assertTrue(handler.codeValid(code));
    }

    @Test
    public void whenClearConstantCodes_shouldClearConstantCodes() {
        String code = handler.generateConstantCode();
        Assertions.assertEquals(1,handler.getConstantCodes().size());
        Assertions.assertEquals(code, handler.getConstantCodes().get(0).getCode());
        handler.clearConstantCodes();
        Assertions.assertEquals(0,handler.getConstantCodes().size());
    }

    @Test
    public void whenGetRecentCodes_shouldNotIncludeExpiredCodes() throws InterruptedException {
        String code1 = handler.generateCode();
        Thread.sleep(8000);
        String code2 = handler.generateCode();
        Thread.sleep(8000);
        Assertions.assertEquals(1,handler.getRecentCodes().size());
    }
}
