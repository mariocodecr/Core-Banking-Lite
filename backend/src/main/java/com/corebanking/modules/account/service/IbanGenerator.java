package com.corebanking.modules.account.service;

import java.math.BigInteger;

/**
 * Generates and validates Costa Rica IBANs (ISO 13616).
 *
 * Format: CR + 2 check digits + 4-digit bank code + 14-digit account number
 * Total length: 22 characters, no spaces stored.
 *
 * Check digit algorithm: ISO 7064 MOD-97-10
 *   1. Arrange: BBAN + country numeric + "00"  (C=12, R=27 → "1227")
 *   2. checkDigits = 98 - (bigNumber MOD 97)
 */
public final class IbanGenerator {

    private static final String COUNTRY_CODE    = "CR";
    private static final String COUNTRY_NUMERIC = "1227"; // C=12, R=27

    private IbanGenerator() {}

    /**
     * Generates a Costa Rica IBAN.
     *
     * @param bankCode      4-digit bank code, zero-padded (e.g. "0152")
     * @param accountNumber 14-digit account number, zero-padded
     * @return 22-character IBAN string (no spaces)
     */
    public static String generate(String bankCode, String accountNumber) {
        String bban          = bankCode + accountNumber;           // 18 chars
        String numericString = bban + COUNTRY_NUMERIC + "00";     // for MOD-97
        int    checkDigits   = 98 - mod97(numericString);
        return COUNTRY_CODE + String.format("%02d", checkDigits) + bban;
    }

    /**
     * Validates a Costa Rica IBAN using MOD-97. A valid IBAN yields remainder 1.
     */
    public static boolean isValid(String iban) {
        if (iban == null || iban.length() != 22 || !iban.startsWith(COUNTRY_CODE)) {
            return false;
        }
        // Rearrange: move first 4 chars to the end, then convert letters to digits
        String rearranged = iban.substring(4) + iban.substring(0, 4);
        String numeric    = toNumericString(rearranged);
        return mod97(numeric) == 1;
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    /** Converts letter characters to their numeric equivalents (A=10 … Z=35). */
    private static String toNumericString(String input) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isLetter(c)) {
                sb.append(Character.toUpperCase(c) - 'A' + 10);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static int mod97(String numericString) {
        return new BigInteger(numericString).mod(BigInteger.valueOf(97)).intValue();
    }
}
