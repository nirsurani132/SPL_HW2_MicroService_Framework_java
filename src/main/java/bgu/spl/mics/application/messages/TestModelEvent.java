package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

public class TestModelEvent implements Event<Model> {
    private Model model;
    private Student st;

    public TestModelEvent(Model model, Student st) {
        this.model = model;
        this.st = st;
    }

    public Model getModel() {
        return model;
    }

    public Student getStudent() {
        return st;
    }
}
