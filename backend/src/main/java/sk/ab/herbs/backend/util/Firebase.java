package sk.ab.herbs.backend.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import retrofit2.Call;
import sk.ab.common.Constants;
import sk.ab.common.entity.Count;
import sk.ab.common.entity.Plant;
import sk.ab.common.entity.PlantHeader;
import sk.ab.common.entity.PlantList;
import sk.ab.common.entity.request.ListRequest;
import sk.ab.common.service.FirebaseClient;
import sk.ab.common.service.HerbCloudClient;

/**
 *
 * Created by adrian on 4.5.2016.
 */
public class Firebase {
    private static String PATH_TO_PLANTS = "C:/Dev/Projects/abherbs/backend/txt/plants.csv";
//    private static String PATH_TO_PLANTS = "/home/adrian/Dev/projects/abherbs/backend/txt/plants.csv";

    private static final String[] COLORS = {"white", "yellow", "red", "blue", "green"};
    private static final String[] HABITATS = {"meadows or grassland", "gardens or fields", "moorlands or wetlands", "woodlands or forests", "rocks or mountains", "trees or bushes"};
    private static final String[] PETALS = {"4 or less", "5", "Many", "Bisymetric"};

    private static final String[] FILTER_ATTRIBUTES = {Constants.COLOR_OF_FLOWERS, Constants.HABITAT, Constants.NUMBER_OF_PETALS};


    public static void main(String[] params) {

        firebaseSynchronization();
    }

    private static void firebaseSynchronization() {

        final HerbCloudClient herbCloudClient = new HerbCloudClient();
        final FirebaseClient firebaseClient = new FirebaseClient();

        //synchronizeCountsAndLists(herbCloudClient, firebaseClient);

        synchronizeDetailsAndNames(herbCloudClient, firebaseClient);
    }

    private static void synchronizeCountsAndLists(HerbCloudClient herbCloudClient, FirebaseClient firebaseClient) {
        try {
            Map<String, String> filter = new HashMap<>();
            getAndSave(herbCloudClient, firebaseClient, filter);

            for (String color : COLORS) {
                filter.put(Constants.COLOR_OF_FLOWERS, color);
                getAndSave(herbCloudClient, firebaseClient, filter);

                for (String habitat : HABITATS) {
                    filter.put(Constants.HABITAT, habitat);
                    getAndSave(herbCloudClient, firebaseClient, filter);

                    for (String petal : PETALS) {
                        filter.put(Constants.NUMBER_OF_PETALS, petal);
                        getAndSave(herbCloudClient, firebaseClient, filter);

                    }
                    filter.remove(Constants.NUMBER_OF_PETALS);
                }
                filter.remove(Constants.HABITAT);
            }

            filter.clear();
            for (String habitat : HABITATS) {
                filter.put(Constants.HABITAT, habitat);
                getAndSave(herbCloudClient, firebaseClient, filter);

                for (String petal : PETALS) {
                    filter.put(Constants.NUMBER_OF_PETALS, petal);
                    getAndSave(herbCloudClient, firebaseClient, filter);

                }
                filter.remove(Constants.NUMBER_OF_PETALS);
            }

            filter.clear();
            for (String petal : PETALS) {
                filter.put(Constants.NUMBER_OF_PETALS, petal);
                getAndSave(herbCloudClient, firebaseClient, filter);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void synchronizeDetailsAndNames(HerbCloudClient herbCloudClient, FirebaseClient firebaseClient) {
        try {
            File file = new File(PATH_TO_PLANTS);

            Map<String, Map<String, List<PlantHeader>>> names = new HashMap<>();

            Scanner scan = new Scanner(file);
            while(scan.hasNextLine()) {

                final String[] plantLine = scan.nextLine().split(";");
                System.out.println(plantLine[0]);

                Call<Plant> callCloudPlant = herbCloudClient.getApiService().getDetail(plantLine[0]);
                Plant plant = callCloudPlant.execute().body();

//                Call<Plant> callFirebase = firebaseClient.getApiService().savePlant(plantLine[0], plant);
//                callFirebase.execute().body();

                Call<PlantHeader> callCloudPlantHeader = herbCloudClient.getApiService().getHeader(plantLine[0]);
                PlantHeader plantHeader = callCloudPlantHeader.execute().body();

                //labels
                for (Map.Entry<String, String> entry : plant.getLabel().entrySet()) {
                    String language = entry.getKey();
                    String plantNameInLanguage = entry.getValue();

                    Map<String, List<PlantHeader>> namesInLanguage = names.get(language);
                    if (namesInLanguage == null) {
                        namesInLanguage = new HashMap<>();
                        names.put(language, namesInLanguage);
                    }

                    processName(namesInLanguage, plantHeader, plantNameInLanguage);
                }

                // aliases
                for (Map.Entry<String, ArrayList<String>> entry : plant.getNames().entrySet()) {
                    String language = entry.getKey();
                    ArrayList<String> plantNamesInLanguage = entry.getValue();

                    Map<String, List<PlantHeader>> namesInLanguage = names.get(language);
                    if (namesInLanguage == null) {
                        namesInLanguage = new HashMap<>();
                        names.put(language, namesInLanguage);
                    }

                    for (String realName : plantNamesInLanguage) {
                        processName(namesInLanguage, plantHeader, realName);
                    }
                }
            }

            for (Map.Entry<String, Map<String, List<PlantHeader>>> entry : names.entrySet()) {
                String language = entry.getKey();
                Map<String, List<PlantHeader>> singleName = entry.getValue();

                for (Map.Entry<String, List<PlantHeader>> entryName : singleName.entrySet()) {
                    String name = entryName.getKey();
                    PlantList plantList = new PlantList();
                    plantList.setItems(entryName.getValue());

                    // name
                    Call<PlantList> callFirebaseList = firebaseClient.getApiService().saveName(language, name, plantList);
                    callFirebaseList.execute().body();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processName(Map<String, List<PlantHeader>> namesInLanguage,
                             PlantHeader plantHeader, String realName) {
        String name = realName.toLowerCase();
        List<PlantHeader> existingNames = namesInLanguage.get(name);
        if (existingNames == null) {
            existingNames = new ArrayList<>();
            namesInLanguage.put(name, existingNames);
        }
        existingNames.add(plantHeader);

        String[] parts = name.split(" ");
        if (parts.length > 1) {
            for (int i = 1; i < parts.length; i++) {
                existingNames = namesInLanguage.get(parts[i]);
                if (existingNames == null) {
                    existingNames = new ArrayList<>();
                    namesInLanguage.put(name, existingNames);
                }
                existingNames.add(plantHeader);
            }
        }
    }

    private static void getAndSave(HerbCloudClient herbCloudClient,
                                   FirebaseClient firebaseClient,
                                   Map<String, String> filter) throws IOException {

        String filterKey = getFilterKey(filter);
        System.out.println(filterKey);

        // count
        Call<Count> callCloudCount = herbCloudClient.getApiService().getCount(new ListRequest(Constants.PLANT, filter));
        Count count = callCloudCount.execute().body();
        Call<Count> callFirebaseCount = firebaseClient.getApiService().saveCount(filterKey, count);
        callFirebaseCount.execute().body();

        // list
        Call<PlantList> callCloudList = herbCloudClient.getApiService().getList(new ListRequest(Constants.PLANT, filter));
        PlantList list = callCloudList.execute().body();
        Call<PlantList> callFirebaseList = firebaseClient.getApiService().saveList(filterKey, list);
        callFirebaseList.execute().body();
    }


    private static String getFilterKey(Map<String, String> filter) {
        StringBuilder filterKey = new StringBuilder();

        String separator = "";
        for (String filterAttribute : FILTER_ATTRIBUTES) {
            filterKey.append(separator);
            filterKey.append(getOrDefault(filter, filterAttribute, ""));

            separator = "_";
        }

        return filterKey.toString();
    }

    private static String getOrDefault(Map<String, String> filter, String key, String defValue) {
        String value = filter.get(key);
        if (value == null) {
            value = defValue;
        }
        return value;
    }
}
