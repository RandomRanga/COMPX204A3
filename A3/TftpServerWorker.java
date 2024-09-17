import java.net.*;
import java.io.*;
import java.util.*;

class TftpServerWorker extends Thread
{
    private DatagramPacket req;
    private static final byte RRQ = 1;
    private static final byte DATA = 2;
    private static final byte ACK = 3;
    private static final byte ERROR = 4;

    private static int MAX_BYTES = 514;

    private void sendfile(String filename)
    {
        /*
         * open the file using a FileInputStream and send it, one block at
         * a time, to the receiver.
         */
        
        try{
            FileInputStream fis = null;

            File file = new File(filename);
            if(!file.exists()){
                //Send an error message  
                System.err.println("File not found");
            }

            //open the file 
            fis = new FileInputStream(file);
            byte[] buffer = new byte[MAX_BYTES];
            int blocknum = 1;
            int bytesRead; 
            boolean transmissionComplete = false;     

            //keep transmissing until finished. 
            while(!transmissionComplete){
                bytesRead = fis.read(buffer);
                if (bytesRead == -1){
                    buffer = new byte[0];
                    bytesRead = 0;
                    transmissionComplete = true; 
                }

                


            }



        }
        catch(Exception ex){
            System.out.println("something has gone wrong please try agian.");

        }



	return;
    }

    public void run()
    {
        /*
         * parse the request packet, ensuring that it is a RRQ
         * and then call sendfile
         */

        if (type = RRQ){

        }
        else{
            System.err.println("bad things");
        }



	return;
    }

    public TftpServerWorker(DatagramPacket req)
    {
	this.req = req;
    }
}
