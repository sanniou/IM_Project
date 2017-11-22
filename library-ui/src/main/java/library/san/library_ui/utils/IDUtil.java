package library.san.library_ui.utils;

import java.util.Random;

public class IDUtil {
	
	
    private static long id = 0;
    private static Random randGen = new Random();
    private static char[] numbersAndLetters = ("0123456789abcdefghijklmnopqrstuvwxyz" +
                    "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray();
    private static String randomString(int length) {
        if (length < 1) {
            return null;
        }
        char [] randBuffer = new char[length];
        for (int i=0; i<randBuffer.length; i++) {
            randBuffer[i] = numbersAndLetters[randGen.nextInt(71)];
        }
        return new String(randBuffer);
    }
    private static String prefix = randomString(5) + "-";
    public static synchronized String nextID() {
        return prefix + Long.toString(id++);
    }

}
