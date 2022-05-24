package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */

public class DataBatch {
    Data data;
    int startIndex;
//    Model model;

    public DataBatch(Data data,int startIndex){
        this.data = data;
        this.startIndex = 0;
//        this.model = model;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public Data.Type getType (){
        return data.getType();
    }

    public Data getData() {
        return data;
    }
}


