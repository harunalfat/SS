import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Problem2 {

    /**
     * For arguments, it need to have the age list file location.
     * Example to run the program : java Problem2 ~/age_file.txt
     *
     * @param args Arguments from command-line
     * @throws IOException Will be thrown if file of age list is not found
     */
    public static void main(final String[] args) throws IOException {
        final BufferedReader br = new BufferedReader(new FileReader(args[0]));
        String line;
        /*
        We will use counting sort (O(n) complexity) to solve this problem

        For range of age in whole world population, we can safely assume it will between 0-150?
        Heck, even assume it to be 0-200, it'll still fit in our 1 GB memory limitation.

        We use long to make sure the counter can fit the max amount of people (7 billion) in case
        people around the world have the same age (That maybe one of your test case)

        Approximation for memory usage are, 8 bytes * 201 = 1608 bytes
        around 1.6 MB. Win!
         */
        long[] ageRange = new long[201];
        while ((line = br.readLine()) != null) {
            final short age = Short.parseShort(line);
            ageRange[age]++;
        }

        for (short i = 0; i < 201; i++) {
            for (short j = 0; j < ageRange[i]; j++) {
                System.out.println(i);
            }
        }
    }

}
