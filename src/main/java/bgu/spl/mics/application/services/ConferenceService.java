package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.ResultMaker;

/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the {@link PublishConfrenceBroadcast},
 * after publishing results the conference will unregister from the system.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ConferenceService extends MicroService {

    ConfrenceInformation conference;
    int myLife;


    public ConferenceService(ConfrenceInformation conference) {
        super(conference.getName());
        this.conference = conference;
        this.myLife = conference.getDate();
    }

    @Override
    protected void initialize() {
//        System.out.println("🤓 ConferenceService " + getName() + " I am alive!!");
        this.subscribeBroadcast(TickBroadcast.class, ev ->  {
            this.myLife--;
            if(this.myLife ==0) {
//                System.out.println("🤓 ConferenceService " + getName() + " I am dead!!");
                this.sendBroadcast(new PublishConferenceBroadcast(this.conference.getModels()));
                this.terminate();
            }
        });
        this.subscribeEvent(PublishResultsEvent.class, ev->{
//            System.out.println("🤓 ConferenceService " + getName() + " got a result!!");
            conference.addModel(ev.getModel());
            complete(ev,ev.getModel());
        });
    }

    @Override
    protected void shutdown() {
        ResultMaker wr = ResultMaker.getInstance();
        wr.addConference(this.conference);
    }
}
