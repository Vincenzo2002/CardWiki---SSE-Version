package at.ac.tuwien.sepm.groupphase.backend.unittests;

import at.ac.tuwien.sepm.groupphase.backend.service.impl.SimpleImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SimpleImageServiceTest {

    private SimpleImageService service;

    @BeforeEach
    public void setup() {
        service = new SimpleImageService(".", null, null);
    }

    @ParameterizedTest(name = "byte {0} becomes {1}")
    @CsvSource({
        "0, 00",
        "1, 01",
        "15, 0f",
        "16, 10",
        "127, 7f",
        "128, 80",
        "254, fe",
        "255, ff"
    })
    public void givenSingleByte_whenConvertToHex_thenReturnTwoLowercaseDigits(
        int value, String expected) {
        assertEquals(expected, bytesToHex(new byte[]{(byte) value}));
    }

    @Test
    public void givenEmptyArray_whenConvertToHex_thenReturnEmptyString() {
        assertEquals("", bytesToHex(new byte[0]));
    }

    @Test
    public void givenNullArray_whenConvertToHex_thenThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> bytesToHex(null));
    }

    @Test
    public void givenEveryByteValue_whenConvertToHex_thenPreserveOrderAndUnsignedValues() {
        byte[] input = new byte[256];
        StringBuilder expected = new StringBuilder();
        for (int value = 0; value < input.length; value++) {
            input[value] = (byte) value;
            expected.append(String.format(Locale.ROOT, "%02x", value));
        }

        assertEquals(expected.toString(), bytesToHex(input));
    }

    @Test
    public void givenKnownSha256Hash_whenConvertToHex_thenReturnExpectedFilenameHash()
        throws NoSuchAlgorithmException {
        byte[] hash = MessageDigest.getInstance("SHA-256")
            .digest("abc".getBytes(StandardCharsets.UTF_8));

        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            bytesToHex(hash));
    }

    @Test
    public void givenZeroFilledSha256SizedArray_whenConvertToHex_thenKeepAllLeadingZeros() {
        assertEquals("0".repeat(64), bytesToHex(new byte[32]));
    }

    @Test
    public void givenInputArray_whenConvertToHex_thenLeaveInputUnchanged() {
        byte[] input = {0, 15, 16, 127, (byte) 128, (byte) 255};
        byte[] original = input.clone();

        bytesToHex(input);

        assertArrayEquals(original, input);
    }

    @Test
    public void givenSuccessiveInputs_whenConvertToHex_thenKeepResultsSeparate() {
        String first = bytesToHex(new byte[]{0, (byte) 255});
        String second = bytesToHex(new byte[]{16, (byte) 128});
        String empty = bytesToHex(new byte[0]);

        assertEquals("00ff", first);
        assertEquals("1080", second);
        assertEquals("", empty);
    }

    private String bytesToHex(byte[] hash) {
        return ReflectionTestUtils.invokeMethod(service, "bytesToHex", (Object) hash);
    }
}
