import java.util.Scanner;
import java.util.Random;
import java.util.concurrent.atomic.*;

interface Channel{ int notInUse = 0;  //Indicates Channel is free
    int inUse = 1; //Indicates Channel is being used
 }

public class ppersist implements Channel
{
    public static void main(String args[]) 
    {
        int N = 6; //N is the number of channels
        Threads.status = notInUse; //initially channel is free

        Threads arrObj[] = new Threads[N+1];
        int FA  [] = new int[N+1]; //FA is the array of frames

        Scanner sc = new Scanner(System.in);

        for(int i = 1;i<=N;i++)
        {
            System.out.println("Enter the number of frames for Station " + i);
            FA[i] = sc.nextInt();
        }

        for(int i = 1;i<=N;i++)
            arrObj[i] = new Threads("Station "+ Integer.toString(i),FA[i]);

        // wait for all stations to complete transmission
        try {
            for(int i=1;i<=N;i++)
                arrObj[i].t.join();
        }
        catch (InterruptedException e) {
            System.out.println("Main Thread Interrupted");
        }
        System.out.println("Transmission completed!!!");
    }
}
class checkStation implements Channel{
    public static int checking(String station)
    {   int stat;
        switch (station)
        {
            case("Station 1") : stat = 1;
                                break;
            case("Station 2") : stat = 2;
                                break;
            case("Station 3") : stat = 3;
                                break;
            case("Station 4") : stat = 4;
                                break;
            case("Station 5") : stat = 5;
                                break;
            case("Station 6") : stat = 6;
                                break;            
            default : stat = 0;
        }
        return(stat);
    }
}


class Threads implements Runnable, Channel {
    String StationNo;
    Thread t;
    static int distance, stat = 0,frame; //distance is the distance travelled by the frame 
    static int status; //Indicates if channel is being used
    int frameNo, maxFrame; //maxFrame is the maximum number of frames for perticular station
    private AtomicBoolean checkForSuccess; //chack for successful transmission
    static int tfr = 50; //Average Transmission time required to send out a frame
    private int k; //k is the number of maximum attempts of transmission of a frame


    Threads(String threadname, int maxFrame) {
        StationNo = threadname;
        t = new Thread(this, StationNo);
        frameNo = 1;
        this.maxFrame = maxFrame;
        checkForSuccess = new AtomicBoolean();
        t.start();
    }

    public void run() {
        Random rand = new Random();
        while (!checkForSuccess.get()) {
            k++;
            while(frameNo <= maxFrame) {
                if (k < 15) { //15 is the maximum number of retransmission attempts
                    try {
                        if (status == inUse) {

                            System.out.println(StationNo + " is using the channel currently");
                            try {
                                Thread.sleep(rand.nextInt(50)+1000);
                            }
                            catch (InterruptedException e) {
                                System.out.println("Interrupt");
                            }
                        }
                        else {
                            System.out.println(StationNo + " is trying to transmit frame: " + frameNo);

                            if (status == notInUse && distance == 0)
                            {//Successful transmission
                                double R=rand.nextDouble();
                                stat = checkStation.checking(Thread.currentThread().getName());
                                frame = this.frameNo;
                                if(R<=0.4)//p=0.4
                                {
                                status = inUse;//set channel to in use
                                for (; distance < 80000; distance++)
                                    for(int i =0; i < 100000; i++); //simulate transmission over some distance

                                System.out.println(StationNo + " frame " + frameNo + " is successful");
                                checkForSuccess.set(true);
                                frameNo++;
                                distance = 0; //reset distance for next frame's transmission
                                status = notInUse;
                                }
                            }
                            else {
                                //Collision has occurred
                                System.out.println("Collision for frame " + frameNo + " of " +
                                        StationNo + " and frame " + frame + " of Station " + stat);

                                System.out.println("Transmitting frame " + frame + "of station " + stat+ "again...");
                                checkForSuccess.set(false);
                                status = notInUse;

                                k++;

                                try {
                                    int Ra = rand.nextInt((int) (Math.pow(2, k - 1)));
                                    int BackOffTime = Ra * tfr;
                                    Thread.sleep(BackOffTime);
                                } catch (InterruptedException e) {
                                    System.out.println("Interrupted");
                                }
                            }
                            Thread.sleep(1000);


                        }
                    } catch (InterruptedException e) {
                        System.out.println(StationNo + "Main Interrupted");
                    }
                }
                else {
                    checkForSuccess.set(true);
                    System.out.println("Attempts exceeded for frame " + frameNo+ "of " +
                            StationNo + ". Stopping Transmission");
                }

            }
        }
    }
}  
