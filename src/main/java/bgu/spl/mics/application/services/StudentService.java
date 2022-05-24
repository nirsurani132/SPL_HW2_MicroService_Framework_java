package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;
import bgu.spl.mics.application.objects.ResultMaker;


/**
 * Student is responsible for sending the {@link TrainModelEvent},
 * {@link TestModelEvent} and {@link PublishResultsEvent}.
 * In addition, it must sign up for the conference publication broadcasts.
 * This class may not hold references for objects which it is not responsible for.
 * <p>
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class StudentService extends MicroService {

    Student myStudent;
    long tickTime;
    int currModel;
    Future currFuture;

    public StudentService(Student student) {
        super(student.getName());
        this.myStudent = student;
        currModel = 0;
        currFuture = null;
    }

    @Override
    protected void initialize() throws InterruptedException {
//        System.out.println("👨🏻‍🎓 hii I am student : " + Thread.currentThread().getName());
        subscribeBroadcast(PublishConferenceBroadcast.class, (PublishConferenceBroadcast message) -> {
            for (Model model : message.getModels()) {
                if (model.getStudent() == myStudent)
                    this.myStudent.increasePublications();
                else
                    this.myStudent.increasePapersRead();
            }
        });

        subscribeBroadcast(TickBroadcast.class, (TickBroadcast message) -> {
            if (currFuture == null && currModel < myStudent.getModels().size())
                currFuture = sendEvent(new TrainModelEvent(myStudent.getName(), myStudent.getModels().get(currModel)));
            else if (currFuture != null && currFuture.isDone()) {
                Model modelResult = (Model) currFuture.get();
                Future testFuture = sendEvent(new TestModelEvent(modelResult, this.myStudent));
                modelResult = (Model) testFuture.get();
                System.out.println("👨🏻‍🎓" + Thread.currentThread().getName() + ": I got model result from test: " + modelResult.getName());
                if (modelResult.getResult() == Model.ModelResult.Good)
                    currFuture = sendEvent(new PublishResultsEvent(this.myStudent, modelResult));
                currModel++;
                if (currModel < myStudent.getModels().size())
                    currFuture = sendEvent(new TrainModelEvent(myStudent.getName(), myStudent.getModels().get(currModel)));
                else
                    currFuture = null;
            }
        });
    }


    @Override
    protected void shutdown() {
        ResultMaker resultMaker = ResultMaker.getInstance();
        resultMaker.addStudent(myStudent);
    }
}
