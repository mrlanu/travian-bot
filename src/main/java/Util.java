import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.*;

class Util {

    static void writeToFile(List<Oasis> oasis, String filePath) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(filePath)){
            gson.toJson(oasis, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static List<Oasis> readFromFile() {
        List<Oasis> oases = new ArrayList<>();

        Gson gson = new GsonBuilder().create();
        try (FileReader reader = new FileReader("/home/lanu/oases.json")){
            oases = gson.fromJson(reader, new TypeToken<List<Oasis>>() {}.getType());
        } catch (IOException e)

    {
        e.printStackTrace();
    }
        return oases;
    }
}
