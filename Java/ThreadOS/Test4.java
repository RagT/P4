import java.util.Random;

/**
 * Test4.java
 * Class created to test implementation of Cache.java
 */
public class Test4 extends Thread{

    private boolean enabled; //Is cache enabled?
    private int testType; //What test to run?
    private Random rand; //Used to run random tests

    public Test4(String[] args) {
        if(args[0].equals("-enabled")) {
            //Enable use of cache
            enabled = true;
        } else { //Don't use cache
            enabled = false;
        }
        testType = Integer.parseInt(args[1]);
        rand = new Random();
    }

    //Runs test based on test type passed in command line arguments
    public void run() {
        //Choose which test to run based on test type
        switch (testType) {
            case 1:
                random();
                break;
            case 2:
                localized();
                break;
            case 3:
                mixed();
                break;
            case 4:
                adversary();
                break;
            default:
                SysLib.cout("Invalid test type.");
                break;
        }
    }

    //read and write many blocks randomly across the disk.
    public void random() {

    }

    //90% of the total disk operations should be localized accesses and 10% should be random accesses.
    public void mixed() {

    }

    //generate disk accesses that do not make good use of the disk cache at all.
    public void adversary() {

    }

    //read and write a small selection of blocks many times to get a high ratio of cache hits.
    public void localized() {

    }

    //Will use csync() if cache enabled will use sync() if not
    private void sync() {
        if(enabled) {
            SysLib.csync();
        } else {
            SysLib.sync();
        }
    }

    //Will use cflush() if cache enabled will use flush() if not
    private void flush() {
        if(enabled){
            SysLib.csync();
        } else {
            SysLib.sync();
        }
    }

    //Will use cread() if cache enabled will use read() if not
    public void read(int bId, byte buffer[]){
        if (enabled){
            SysLib.cread(bId, buffer);
        } else {
            SysLib.rawread(bId, buffer);
        }
    }

    //Will use cwrite() if cache enabled will use write() if not
    public void write(int bId, byte buffer[]){
        if (enabled){
            SysLib.cwrite(bId, buffer);
        } else {

            SysLib.rawwrite(bId, buffer);
        }
    }
}
