package me.oldboy.unit.validation_tests;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.test_config.TestConstantFields;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/* Эти тесты не проверяют логику программы, а показывают, в принципе, как можно валидировать данные */
class ValidationTest {
    private DetailsCreateDto testDetailsCreateDto;
    private ClientCreateDto testClientCreateDto, notValidCreateClientDto;

    @BeforeEach
    void setUp(){
        testDetailsCreateDto = new DetailsCreateDto(TestConstantFields.TEST_CLIENT_NAME,
                                                    TestConstantFields.TEST_CLIENT_SUR_NAME,
                                                    TestConstantFields.TEST_AGE);

        testClientCreateDto = new ClientCreateDto(TestConstantFields.TEST_EMAIL,
                                                  TestConstantFields.TEST_PASS,
                                                  testDetailsCreateDto);

        notValidCreateClientDto = new ClientCreateDto("sd","1", testDetailsCreateDto);

    }

    /* Один из вариантов проверки валидности получаемых данных */

    @Test
    void shouldReturn_ValidationErrors_AndSizeOfViolationsSetTest() throws Exception {

        ValidatorFactory validatorFactory = Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory();

        Validator validator = validatorFactory.usingContext()
                .messageInterpolator(new ParameterMessageInterpolator())
                .getValidator();

        Set<ConstraintViolation<ClientCreateDto>> violations = validator.validate(notValidCreateClientDto);

        assertEquals(2, violations.size());
    }

    @Test
    void shouldReturn_ValidationOk_AndZeroSizeSet_Test() throws Exception {

        ValidatorFactory validatorFactory = Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory();

        Validator validator = validatorFactory.usingContext()
                .messageInterpolator(new ParameterMessageInterpolator())
                .getValidator();

        Set<ConstraintViolation<ClientCreateDto>> violations = validator.validate(testClientCreateDto);

        assertEquals(0, violations.size());
    }
}