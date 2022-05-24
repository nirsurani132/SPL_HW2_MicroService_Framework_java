package bgu.spl.mics.application.objects;

import java.util.LinkedList;
import java.util.Locale;

/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {
    /**
     * Enum representing the Degree the student is studying for.
     */
    enum Degree {
        MSc, PhD
    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", department='" + department + '\'' +
                ", status=" + status +
                ", publications=" + publications +
                ", papersRead=" + papersRead +
                ", models=" + models +
                '}';
    }

    private String name; // TODO: it was int wtf??
    private String department;
    private Degree status;
    private int publications;
    private int papersRead;
    private LinkedList<Model> models;

    public Student(String name, String department, String status){
        this.name = name;
        this.department = department;
        switch(status.toLowerCase(Locale.ROOT).trim()){
            case "msc":
                this.status = Degree.MSc;
                break;
            case "phd":
                this.status = Degree.PhD;
                break;
        }
        publications = 0;
        papersRead = 0;
        models = new LinkedList<>();
    }

    public String getName (){
        return name;
    }

    public LinkedList<Model> getModels() {
        return models;
    }

    public void addModel(Model model){
        models.add(model);
    }

    public Degree getStatus() {
        return status;
    }

    public void increasePublications(){
        publications++;
    }

    public void increasePapersRead(){
        papersRead++;
    }

    public int getPublications(){
        return publications;
    }

    public int getPapersRead() {
        return papersRead;
    }

    public String getDepartment(){return department; }
}
