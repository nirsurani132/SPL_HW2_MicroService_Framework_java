package bgu.spl.mics.application.objects;

public class UnprocessedPair {
    public GPU gpu;
    public DataBatch dataBatch;
    UnprocessedPair(DataBatch dataBatch, GPU gpu){
        this.gpu = gpu;
        this.dataBatch = dataBatch;
    }
}
