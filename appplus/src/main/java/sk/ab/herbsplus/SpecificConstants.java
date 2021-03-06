package sk.ab.herbsplus;

import java.util.Arrays;
import java.util.List;

import sk.ab.common.Constants;

/**
 *
 * Created by adrian on 11. 3. 2017.
 */

public class SpecificConstants {
    public static final String PACKAGE = "sk.ab.herbsplus";
    public final static String STORAGE = "gs://abherbs-resources";
    public final static String PHOTOS = "/photos";
    public final static String FAMILIES = "/families";

    public final static String VERSION_2_0_0 = "216";
    public final static String VERSION_2_0_0_a = "216a";
    public final static String VERSION_2_0_0_b = "216b";

    public final static int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 0;

    public final static String OFFLINE_MODE_KEY = "offline_mode";
    public final static String OFFLINE_PLANT_KEY = "offline_plant";
    public final static String OFFLINE_FAMILY_KEY = "offline_family";

    public final static String[] FILTERS = {"filter_1", "filter_2", "filter_3", "filter_4"};
    public final static String[] AVAILABLE_FILTERS = {"available_filter_1", "available_filter_2", "available_filter_3"};
    public static final String[] FILTER_ATTRIBUTES = {Constants.COLOR_OF_FLOWERS, Constants.HABITAT, Constants.NUMBER_OF_PETALS, Constants.DISTRIBUTION};
}
