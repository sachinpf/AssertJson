package testData.pojo.random;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Random;

public class RandomGenerator {

    public static String randomString(int length) {
        byte[] array = new byte[length]; // length is bounded by 7
        new Random().nextBytes(array);
        return new String(array, Charset.forName("UTF-8"));
    }

    public static int randomInt(int length) {
        Random r = new Random();
        String returnValue = "";
        int j = 0;
        for (int i = 1; i <= length; i++) {
            j = r.nextInt(length);
            returnValue = returnValue + ((j == 0 && i == 1) ? 1 : j);
        }
        return Integer.parseInt(returnValue);
    }

    public static boolean randomBoolean(){
        return new Random().nextBoolean();
    }

    /*public static void main(String args[]) {
        System.out.println(randomInt(9));
        System.out.println(randomString());
    }*/

}

