package ru.monyamau.cloudfilestorage.util;

import lombok.experimental.UtilityClass;
import org.mindrot.jbcrypt.BCrypt;

@UtilityClass
public final class PassHashUtil {
    private static final int COST_FACTOR = 12;

    public static String hash(String password) {
        String salt = BCrypt.gensalt(COST_FACTOR);
        return BCrypt.hashpw(password, salt);
    }

    public static boolean check(String verificationPassword, String savedHash) {
        return BCrypt.checkpw(verificationPassword, savedHash);
    }
}
