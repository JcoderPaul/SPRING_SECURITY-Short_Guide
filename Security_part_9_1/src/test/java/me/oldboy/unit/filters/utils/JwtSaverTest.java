package me.oldboy.unit.filters.utils;

import me.oldboy.filters.utils.JwtSaver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL;
import static me.oldboy.test_constant.TestConstantFields.TEST_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class JwtSaverTest {

    private JwtSaver jwtSaver;
    private String testKeyEmail, testKeyEmailTwo;
    private String testJwtToken, testJwtTokenTwo;

    @BeforeEach
    void beforeAllSet(){
        jwtSaver = new JwtSaver();

        testKeyEmail = TEST_EMAIL;
        testKeyEmailTwo = EXIST_EMAIL;

        testJwtToken = "test.jwt.token";
        testJwtTokenTwo = "another.jwt.token";
    }

    @Test
    void shouldReturnWhatWasSaved_SaveJwtToken_Test() {
        jwtSaver.saveJwtToken(testKeyEmail, testJwtToken);
        assertThat(jwtSaver.getSavedJwt(testKeyEmail)).isEqualTo(testJwtToken);
    }

    @Test
    void shouldReturnNullIfNoMatchForKey_And_NotNullIfThereIs_GetSavedJwt_Test() {
        String jwtTokenFromEmptyBase = jwtSaver.getSavedJwt(testKeyEmail);
        assertThat(jwtTokenFromEmptyBase).isEqualTo(null);

        jwtSaver.saveJwtToken(testKeyEmail, testJwtToken);
        String jwtTokenNotNull = jwtSaver.getSavedJwt(testKeyEmail);
        assertThat(jwtTokenNotNull).isNotNull();
    }

    @Test
    void shouldReturnNullAfterClearingElement_SetJwtToNull_Test() {
        jwtSaver.saveJwtToken(testKeyEmail, testJwtToken);
        jwtSaver.saveJwtToken(testKeyEmailTwo, testJwtTokenTwo);

        assertAll(
                () -> assertThat(jwtSaver.getSavedJwt(testKeyEmail)).isNotNull(),
                () -> assertThat(jwtSaver.getSavedJwt(testKeyEmailTwo)).isNotNull()
        );

        jwtSaver.setJwtToNull(testKeyEmail);
        assertAll(
                () -> assertThat(jwtSaver.getSavedJwt(testKeyEmail)).isEqualTo(null),
                () -> assertThat(jwtSaver.getSavedJwt(testKeyEmailTwo)).isNotNull()
        );
    }
}