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


    private void sendfile(String filename)
    {
        /*
         * open the file using a FileInputStream and send it, one block at
         * a time, to the receiver.
         */
        
        try{
           

            File file = new File(filename);
            if(!file.exists()){
                //Send an error message  
                System.err.println("File not found");
                return;
            }

            //open the file 
            FileInputStream fis = new FileInputStream(file);
            //to hold each chunk of data from the file
            byte[] buffer = new byte[512];
            int blocknum = 1;
            int bytesRead; 
            

            boolean transmissionComplete = false;    
            
            //create the datagram socket 
            DatagramSocket ds = new DatagramSocket();

            //possibly get ip and port from client(req)



            //keep transmissing until finished. 
            while(!transmissionComplete){
                bytesRead = fis.read(buffer);
                //if it is end of the file send empty packet and stop transmission
                if (bytesRead == -1){
                    // buffer = new byte[0];
                    bytesRead = 0;
                    transmissionComplete = true; 
                }

                //create the data packet 
                byte[] dataPacket = new byte[2 + bytesRead];
                //checks it is data
                dataPacket[0] = 2;
                dataPacket[1] = (byte) (blocknum);

                for(int i = 0; i < bytesRead; i++){
                    dataPacket[2 + i] = buffer[i];
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
        // gets the req from the datagram packet 
        byte[] data = req.getData();

        //checks that that it is a RRQ through the first 2 bytes
        if (data[0] == 0 && data[1] == 1){

            StringBuilder fileNameBuilder = new StringBuilder();

            //to store where in the data packet we are
            int index = 2;

            //loops through until end of file
            while(data[index] != 0){
                //builds the file name
                fileNameBuilder.append((char) data[index]);

                index++;

            }
            

            //calls send file with the whole file name
            sendfile(fileNameBuilder.toString());


        }
        else{
            System.out.println("this not a RRQ please try again.");
        }




	return;
    }

    public TftpServerWorker(DatagramPacket req)
    {
	this.req = req;
    }
}
