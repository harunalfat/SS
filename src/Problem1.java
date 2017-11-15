import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

public class Problem1 {

    private static final float[] POWER_TABLE = new float[149];
    private static final Map<String, Short> PROD_SCORE_MAP = new HashMap<>();
    private static String SERIALIZED_MAP_LOCATION = "user_product_order.ser";

    /**
     * For initializing, args need to have 3 items
     *
     * [0] "initialize" String
     * [1] user preference file location. Example : "~/user_preference.txt"
     * [2] product score file location. Example : "~/product_score.txt"
     *
     * For recommendation, args need to have 2 items
     * [0] "recommend" String
     * [1] uid for particular user. Example : "12341"
     *
     * @param args Arguments from command-line
     */
    public static void main(final String[] args) throws IOException, ClassNotFoundException {
        switch (args[0]) {
            case "initialize":
                initialize(args[1], args[2]);
                break;
            case "recommend":
                recommend(args[1]);
                break;
            default:
                throw new RuntimeException("Wrong command!");
        }
    }

    private static void initialize(final String userPreferenceLocation,
                                   final String productScoreLocation) throws IOException {
        initPowerTable();
        writeProdScoreMap(productScoreLocation);
        writeUserProductOrder(userPreferenceLocation);
    }

    /**
     * For save 0.95 power result so app won't recount for every row
     *
     * @throws IOException Will be thrown if app can't write to current location under the name of power_table.txt
     */
    private static void initPowerTable() throws IOException {
        float initial = 1.0f;

        final DecimalFormat df = new DecimalFormat("#.###");
        df.setRoundingMode(RoundingMode.HALF_UP);

        for (int i = 0; i < 149; i++) {
            final BigDecimal bd = new BigDecimal(initial)
                    .setScale(3, RoundingMode.HALF_UP);
            POWER_TABLE[i] = bd.floatValue();
            initial = initial * 0.95f;
        }
    }

    /**
     * Mapping product score for better lookup speed
     *
     * @param productScoreLocation Location of product score file
     * @throws IOException Will be thrown if file doesn't exist
     */
    private static void writeProdScoreMap(final String productScoreLocation) throws IOException {
        final BufferedReader br = new BufferedReader(new FileReader(productScoreLocation));
        String line;

        final int PID = 0;
        final int SCORE = 1;

        while ((line = br.readLine()) != null) {
            final String[] columns = line.split("\t");
            PROD_SCORE_MAP.put(columns[PID], Short.parseShort(columns[SCORE]));
        }
    }

    /**
     * Create list of product by user, sorted by current score
     *
     * @param userPreferenceLocation Location of user preference file
     * @throws IOException Will be thrown if file doesn't exist & can't write serialize map
     */
    private static void writeUserProductOrder(final String userPreferenceLocation) throws IOException {
        final BufferedReader br = new BufferedReader(new FileReader(userPreferenceLocation));
        final Map<String, Map<String, Float>> usersPrefMap = new HashMap<>();
        String line;

        final int UID = 0;
        final int PID = 1;
        final int SCORE = 2;
        final int TIMESTAMP = 3;
        final long currentUnixTimestamp = new Date().getTime() / 1000;

        while ((line = br.readLine()) != null) {
            final String[] columns = line.split("\t");

            final Map<String, Float> userProductsScore = usersPrefMap.getOrDefault(columns[UID], new HashMap<>());
            final long rowUnixTimestamp = Long.parseLong(columns[TIMESTAMP]);

            final int dayDifference = (int) ((currentUnixTimestamp - rowUnixTimestamp) / 24 / 3600);
            final float multiplyFactor = dayDifference > 148 ? 0 : POWER_TABLE[dayDifference];

            final short productScore = PROD_SCORE_MAP.get(columns[PID]);
            final float currentScore = productScore * (Float.parseFloat(columns[SCORE]) * multiplyFactor) + productScore;
            userProductsScore.put(columns[PID], currentScore);
            usersPrefMap.put(columns[UID], userProductsScore);
        }

        final Map<String, List<ProductAndScore>> userProductOrder = getSortedProductByScorePerUser(usersPrefMap);
        final FileOutputStream fos = new FileOutputStream(SERIALIZED_MAP_LOCATION);
        final ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(userProductOrder);
        oos.flush();
        oos.close();
    }

    /**
     * Sort the product by score
     *
     * @param usersPrefMap Previously build for user product preference score updated
     * @return Map of user and sorted list of product by score
     */
    private static Map<String, List<ProductAndScore>> getSortedProductByScorePerUser(final Map<String, Map<String, Float>> usersPrefMap) {
        final Map<String, List<ProductAndScore>> userProductOrder = new HashMap<>();
        for (Map.Entry<String, Map<String, Float>> userEntry : usersPrefMap.entrySet()) {

            final List<ProductAndScore> productAndScoreList = new ArrayList<>();

            for (Map.Entry<String, Float> productEntry : userEntry.getValue().entrySet()) {
                final ProductAndScore ps = new ProductAndScore(productEntry.getKey(), productEntry.getValue());
                productAndScoreList.add(ps);
            }

            productAndScoreList.sort((o1, o2) -> Float.compare(o2.score, o1.score));
            userProductOrder.put(userEntry.getKey(), productAndScoreList);
        }
        return userProductOrder;
    }

    /**
     *
     * @param uid User ID to search for recommendation
     * @throws IOException Will be thrown if the serialized map file can't be found
     * @throws ClassNotFoundException Will be thrown if the serialized map file can't be change to Java object
     */
    private static void recommend(final String uid) throws IOException, ClassNotFoundException {
        final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SERIALIZED_MAP_LOCATION));
        final Map<String, List<ProductAndScore>> userProductOrder = (Map<String, List<ProductAndScore>>) ois.readObject();
        final List<ProductAndScore> productAndScores = userProductOrder.get(uid);

        int counter = 0;
        final Iterator<ProductAndScore> iterator = productAndScores.iterator();
        while (iterator.hasNext() && counter < 5) {
            final ProductAndScore ps = iterator.next();
            System.out.println(ps.getPid());
            counter++;
        }
    }

    private static class ProductAndScore implements Serializable{

        private final String pid;
        private final float score;

        ProductAndScore(String pid, float score) {
            this.pid = pid;
            this.score = score;
        }

        String getPid() {
            return pid;
        }

        public float getScore() {
            return score;
        }

    }

}
