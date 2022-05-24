package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

public class PublishResultsEvent implements Event<Model>{
    private Student sender;
    private Model model;
    public PublishResultsEvent(Student sender, Model model) {
        this.sender = sender;
        this.model = model;
    }

    public Model getModel() {
        return model;
    }

    public String getSenderName() {
        return sender.getName();
    }

    @Override
    public String toString() {
        return "TrainModelEvent{" +
                "senderName='" + sender.getName() + '\'' +
                ", model=" + model +
                '}';
    }


}
