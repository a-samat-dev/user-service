package kz.smarthealth.userservice.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link MessageSource}
 *
 * Created by Samat Abibulla on 2022-10-11
 */
class MessageSourceTest {

    @Test
    void getText_returnsFormattedText() {
        // given
        String testEmail = "test@gmail.com";
        // when
        String text = MessageSource.EMAIL_IN_USE.getText(testEmail);
        // then
        assertNotNull(text);
        assertEquals(text, testEmail + " is already in use, please provide another email address.");
    }
}