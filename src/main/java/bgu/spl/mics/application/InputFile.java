package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.ConferenceService;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class InputFile {
    List<Student> Students;
    List<GPU> GPUS;
    List<CPU> CPUS;
    List<ConfrenceInformation> Conferences;
    int TickTime;
    int Duration;

    static class GPUDeserializer implements JsonDeserializer<GPU> {
        @Override
        public GPU deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new GPU(json.getAsJsonPrimitive().getAsString());
        }
    }

    static class CPUDeserializer implements JsonDeserializer<CPU> {
        @Override
        public CPU deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new CPU(json.getAsJsonPrimitive().getAsInt());
        }
    }

    static class StudentDeserializer implements JsonDeserializer<Student> {
        @Override
        public Student deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject studentObj = json.getAsJsonObject();
            Student st = new Student(studentObj.get("name").getAsString(),
                    studentObj.get("department").getAsString(),
                    studentObj.get("status").getAsString());
            JsonArray models = studentObj.get("models").getAsJsonArray();
            for (JsonElement model : models) {
                JsonObject modelObj = model.getAsJsonObject();
                st.addModel(new Model(modelObj.get("name").getAsString(), new Data(modelObj.get("type").getAsString(), modelObj.get("size").getAsInt()), st));
            }
            return st;
        }
    }

    static class ConferenceDeserializer implements JsonDeserializer<ConfrenceInformation> {
        @Override
        public ConfrenceInformation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject conferenceObj = json.getAsJsonObject();
            return new ConfrenceInformation(conferenceObj.get("name").getAsString(), conferenceObj.get("date").getAsInt());
        }
    }

    public static InputFile create(String path) throws FileNotFoundException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(GPU.class, new GPUDeserializer());
        gsonBuilder.registerTypeAdapter(CPU.class, new CPUDeserializer());
        gsonBuilder.registerTypeAdapter(Student.class, new StudentDeserializer());
        gsonBuilder.registerTypeAdapter(ConfrenceInformation.class, new ConferenceDeserializer());
        Gson gson = gsonBuilder.create();
        JsonReader reader = new JsonReader(new FileReader(path));
        InputFile input = gson.fromJson(reader, InputFile.class);
        return input;
    }

    @Override
    public String toString() {
        return "InputFile{" +
                "Students=" + Students +
                ", GPUS=" + GPUS +
                ", CPUS=" + CPUS +
                ", Conferences=" + Conferences.toString() +
                ", TickTime=" + TickTime +
                ", Duration=" + Duration +
                '}';
    }


}
