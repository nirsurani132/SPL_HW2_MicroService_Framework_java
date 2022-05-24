package bgu.spl.mics.application;


import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */


public class CRMSRunner {

    public static void main(String[] args) {
        try {
            LinkedList<Thread> threads = new LinkedList<>();
            InputFile input = InputFile.create(args[0]);
            Registor initHelper = new Registor(input.GPUS.size() + input.CPUS.size());
            for (int i=0; i<input.GPUS.size(); i++)
                threads.add(new Thread(new GPUService(input.GPUS.get(i),initHelper),"GPU: "+ i));
            for (int i=0; i<input.CPUS.size(); i++)
                threads.add(new Thread(new CPUService(input.CPUS.get(i),initHelper),"CPU: "+ i));
            for (ConfrenceInformation confrenceInformation : input.Conferences)
                threads.add(new Thread(new ConferenceService(confrenceInformation),"Conference: "+ confrenceInformation.getName()));
            for (Student student: input.Students)
                threads.add(new Thread(new StudentService(student),"student: "+ student.getName()));
            for (Thread thread : threads)
                thread.start();
            Thread time =  new Thread(new TimeService(input.TickTime,input.Duration));
            time.start();
           for (Thread thread : threads)
               thread.join();
//            System.out.println("end!!");
            ResultMaker wr = ResultMaker.getInstance();
            wr.generateOutputFile();
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



// IGNORE:
// mvn exec:java -Dexec.mainClass=bgu.spl.mics.application.CRMSRunner -Dexec.args="<JSON_PATH>"