package sk.ab.herbs;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: adrian
 * Date: 11.2.2013
 * Time: 20:02
 */
public class Constants {
    public final static Map<String, Integer> LANGUAGES;
    public final static String LANGUAGE_DEFAULT_KEY = "language_default";
    public final static String LANGUAGE_EN = "en";
    public final static String LANGUAGE_SK = "sk";

    public final static String REST_ENDPOINT = "http://appsresource.appspot.com/rest/";
    //public final static String REST_ENDPOINT = "http://localhost:8880/rest/";
    public final static String REST_COUNT = "count";
    public final static String REST_LIST = "list";
    public final static String REST_DETAIL = "detail";

    public final static int FLOWERS = 12;

    public final static String FAMILY = "family";
    public final static String RESOURCE_SEPARATOR = "_";

    public final static int COLOR_OF_FLOWERS_ID = 224;
    public final static int HABITATS_ID = 226;
    public final static int NUMBER_OF_PETALS_ID = 238;

    public final static int PLANT_NAME = 49;
    public final static int PLANT_PHOTO_URL = 395;
    public final static int PLANT_IMAGE_URL = 293;
    public final static int PLANT_STEM = 290;
    public final static int PLANT_FLOWER = 287;
    public final static int PLANT_FRUIT = 288;
    public final static int PLANT_LEAF = 289;
    public final static int PLANT_HABITAT = 291;
    public final static int PLANT_FLOWER_COLOR = 294;
    public final static int PLANT_NUMBER_OF_PETALS = 301;
    public final static int PLANT_SEPAL = 293;
    public final static int PLANT_INFLORESCENCE = 293;
    public final static int PLANT_LEAF_SHAPE = 293;
    public final static int PLANT_LEAF_MARGIN = 293;
    public final static int PLANT_LEAF_VENATION = 293;
    public final static int PLANT_LEAF_ARRANGEMENT = 293;
    public final static int PLANT_ROOT = 293;
    public final static int PLANT_DOMAIN = 383;
    public final static int PLANT_DOMAIN_LATIN = 293;
    public final static int PLANT_KINGDOM = 384;
    public final static int PLANT_KINGDOM_LATIN = 293;
    public final static int PLANT_SUBKINGDOM = 385;
    public final static int PLANT_SUBKINGDOM_LATIN = 390;
    public final static int PLANT_LINE = 391;
    public final static int PLANT_LINE_LATIN = 390;
    public final static int PLANT_BRANCH = 386;
    public final static int PLANT_BRANCH_LATIN = 390;
    public final static int PLANT_PHYLUM = 391;
    public final static int PLANT_PHYLUM_LATIN = 390;
    public final static int PLANT_CLS = 391;
    public final static int PLANT_CLS_LATIN = 390;
    public final static int PLANT_ORDER = 390;
    public final static int PLANT_ORDER_LATIN = 390;
    public final static int PLANT_FAMILY = 391;
    public final static int PLANT_FAMILY_LATIN = 390;
    public final static int PLANT_GENUS = 392;
    public final static int PLANT_GENUS_LATIN = 392;
    public final static int PLANT_SPECIES = 49;
    public final static int PLANT_SPECIES_LATIN = 286;

    static {
        LANGUAGES = new HashMap<String, Integer>();
        LANGUAGES.put(LANGUAGE_EN, 0);
        LANGUAGES.put(LANGUAGE_SK, 1);
    }

    public static int getLanguage() {
        Integer language = LANGUAGES.get(Locale.getDefault().getLanguage());
        return language == null ? 0 : language;
    }
}
