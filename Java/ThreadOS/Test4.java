import java.util.Random;
import java.util.Date;

/**
 * Test4.java
 * Class created to test implementation of Cache.java
 */
public class Test4 extends Thread{

    private boolean enabled; //Is cache enabled?
    private String testType; //What test to run?
    private Random rand; //Used to run random tests
    private byte[] read; //Read from this array
    private byte[] write; //Write to this array
    private int[] locations; //Used to store read/write locations for tests

    public Test4(String[] args) {
        if(args[0].equals("enabled")) {
            //Enable use of cache
            enabled = true;
        } else { //Don't use cache
            enabled = false;
        }
        read = new byte[1024];
        write = new byte[1024];
        rand = new Random();
        rand.nextBytes(write);      //Fill write block with random values

        testType = args[1];
        locations = new int[500];
    }

    //Runs test based on test type passed in command line arguments
    public void run() {
        SysLib.flush();
        //Choose which test to run based on test type
        if(testType.equalsIgnoreCase("random")) {
            random();
        } else if (testType.equalsIgnoreCase("localized")) {
            localized();
        } else if(testType.equalsIgnoreCase("mixed")) {
            mixed();
        } else if(testType.equalsIgnoreCase("adversary")) {
            adversary();
        } else {
            Syslib.cout("Invalid test type\n");
        }
        sync();
        SysLib.exit();
    }

    //read and write many blocks randomly across the disk.
    public void random() {
        SysLib.cout("Random Access Test: \n");
        for(int i = 0; i < locations.length; i++) {
            locations[i] = rand.nextInt(512);    //Fill locations array with random locations
        }
        testReadAndWrite("Random");
    }

    //90% of the total disk operations should be localized accesses and 10% should be random accesses.
    public void mixed() {
        SysLib.cout("Mixed access test: \n");
        for(int i = 0; i < locations.length; i++) {
            int randInt = rand.nextInt(10); //Random number between 0 and 9
            if(randInt < 9) { //localized access
                locations[i] = rand.nextInt(15);
            } else { //Completely random access
                locations[i] = rand.nextInt(512);
            }
        }
        testReadAndWrite("Mixed");
    }

    //generate disk accesses that do not make good use of the disk cache at all.
    public void adversary() {
        SysLib.cout("Adversary Access Test: \n");
        for(int i = 0; i < locations.length; i++){
            locations[i] = i;
        }
        testReadAndWrite("Adversary");
    }

    //read and write a small selection of blocks many times to get a high ratio of cache hits.
    public void localized() {
        SysLib.cout("Localized Access Test: \n");
        for(int i = 0; i < locations.length; i++){
            locations[i] = rand.nextInt(15);
        }
        testReadAndWrite("Localized");
    }

    //Will use csync() if cache enabled will use sync() if not
    private void sync() {
        if(enabled) {
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

    public void testReadAndWrite(String testType) {
        long startRead = new Date().getTime();
        for(int i = 0; i < locations.length; i++) {
            read(locations[i], read);
        }
        long readElapsed = new Date().getTime() - startRead;

        SysLib.cout(testType + " read elapsed time = " + readElapsed + "ms.\n");
        long startWrite = new Date().getTime();
        for(int i = 0; i < locations.length; i++) {
            write(locations[i], write);
        }
        long writeElapsed = new Date().getTime() - startWrite;
        SysLib.cout(testType + " write elapsed time = " + writeElapsed + "ms.\n");
    }
}
