package bgu.spl.mics.application.objects;

/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */


public class Model {
    private String name;
    private Data data;
    private Student student;
    private ModelStatus status;
    private ModelResult result;


    public enum ModelStatus {
        PreTrained,
        Training,
        Trained,
        Tested,
    }

    public enum ModelResult {
        None,
        Good,
        Bad
    }

    @Override
    public String toString() {
        return "Model{" +
                "name='" + name + '\'' +
                ", data=" + data +
                ", student='" + student.getName() + '\'' +
                ", status=" + status +
                ", result=" + result +
                '}';
    }

    public int getSize() {
        return data.getSize();
    }

    public String getName() {
        return name;
    }

    public Data getData() {
        return data;
    }

    public Data.Type getDataType() {
        return data.getType();
    }

    public Model(String name, Data data, Student student) {
        this.name = name;
        this.data = data;
        this.status = ModelStatus.PreTrained;
        this.result = ModelResult.None;
        this.student = student;
    }

    public void setStatus(ModelStatus status) {
        this.status = status;
    }

    public void setResult(ModelResult result) {
        this.result = result;
    }

    public ModelResult getResult() {
        return result;
    }
    public ModelStatus getStatus() {
        return status;
    }

    public Student getStudent() {
        return student;
    }
}
