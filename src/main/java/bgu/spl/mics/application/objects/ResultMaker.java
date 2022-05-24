package bgu.spl.mics.application.objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.LinkedBlockingQueue;

public class ResultMaker {

    LinkedBlockingQueue<Student> students;
    LinkedBlockingQueue<ConfrenceInformation> conferences;
    int cpuTimeUsed;
    int gpuTimeUsed;
    int batchesProcessed;

    private static class WriterHolder{
        private static ResultMaker instance = new ResultMaker();
    }

    private ResultMaker(){
        students = new LinkedBlockingQueue<>();
        conferences = new LinkedBlockingQueue<>();
        cpuTimeUsed = 0;
        gpuTimeUsed = 0;
        batchesProcessed = 0;
    }

    public static ResultMaker getInstance(){
        return WriterHolder.instance;
    }

    public void addStudent(Student student){
        students.add(student);
    }

    public void addConference(ConfrenceInformation conference){
        conferences.add(conference);
    }

    public void setCpuTimeUsed(int time){
        cpuTimeUsed = time;
    }
    public void setGpuTimeUsed(int time){
        gpuTimeUsed = time;
    }
    public void setBatchesProcessed(int batches){
        batchesProcessed = batches;
    }

    // generate json file using GSON library
    public void generateOutputFile() throws IOException {
        JsonObject output = new JsonObject();
        JsonArray studentsJson = new JsonArray();
        for (Student student : students) {
            JsonObject studentJson = new JsonObject();
            studentJson.addProperty("name", student.getName());
            studentJson.addProperty("department", student.getDepartment());
            studentJson.addProperty("status", student.getStatus().toString());
            studentJson.addProperty("publications", student.getPublications());
            studentJson.addProperty("papersRead", student.getPapersRead());
            JsonArray modelsArray = new JsonArray();
            for (Model model : student.getModels()) {
                if(model.getStatus() == Model.ModelStatus.Tested){
                    JsonObject modelJson = new JsonObject();
                    modelJson.addProperty("name", model.getName());
                    JsonObject dataJson = new JsonObject();
                    dataJson.addProperty("type",model.getData().getType().toString());
                    dataJson.addProperty("size",model.getData().getSize());
                    modelJson.add("data",dataJson);
                    modelJson.addProperty("status",model.getStatus().toString());
                    modelJson.addProperty("results", model.getResult().toString());
                    modelsArray.add(modelJson);
                }
            }
            studentJson.add("trainedModels",modelsArray);
            studentsJson.add(studentJson);
        }
        JsonArray conferencesArray = new JsonArray();
        for (ConfrenceInformation conference : conferences) {
            JsonObject conferenceJson = new JsonObject();
            conferenceJson.addProperty("name", conference.getName());
            conferenceJson.addProperty("date", conference.getDate());
            JsonArray publicationsArray = new JsonArray();
            for (Model model : conference.getModels()) {
                JsonObject publicationJson = new JsonObject();
                publicationJson.addProperty("name", model.getName());
                JsonObject dataJson = new JsonObject();
                dataJson.addProperty("type",model.getData().getType().toString());
                dataJson.addProperty("size",model.getData().getSize());
                publicationJson.add("data",dataJson);
                publicationJson.addProperty("status", model.getStatus().toString());
                publicationJson.addProperty("result", model.getResult().toString());
                publicationsArray.add(publicationJson);
            }
            conferenceJson.add("publications",publicationsArray);
            conferencesArray.add(conferenceJson);
        }
        output.add("students", studentsJson);
        output.add("conferences", conferencesArray);
        output.addProperty("cpuTimeUsed", cpuTimeUsed);
        output.addProperty("gpuTimeUsed", gpuTimeUsed);
        output.addProperty("batchesProcessed", batchesProcessed);
        try (Writer writer = new FileWriter("Output.json")) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder = gsonBuilder.setPrettyPrinting();   //Sets pretty formatting
            Gson gson = gsonBuilder.create();                //Create Gson reference
            gson.toJson(output, writer);
        }
    }
}
