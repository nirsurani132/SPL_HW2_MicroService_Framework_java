package bgu.spl.mics.application.objects;

import java.util.Locale;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Data {
    /**
     * Enum representing the Data type.
     */
    public enum Type {
        Images, Text, Tabular
    }
    private Type type;
    private int processed;
    private int size;

    public Data (String type,int size){
        switch (type.toLowerCase(Locale.ROOT).trim()){
            case "images":
                this.type = Type.Images;
                break;
            case "text":
                this.type = Type.Text;
                break;
            case "tabular":
                this.type = Type.Tabular;
                break;
            default:
                throw new IllegalArgumentException("Invalid data type");
        }
        this.size = size;
        this.processed = 0;

    }

    public int getProcessed() {
        return processed;
    }

    public boolean raiseProcess(){
        // FIXME: Might be a bug
        processed = processed + 1000;
        return size <= processed;
    }

    @Override
    public String toString() {
        return "Data{" +
                "type=" + type +
                '}';
    }

    public int getSize() {
        return size;
    }

    public Type getType() {
        return type;
    }

}
