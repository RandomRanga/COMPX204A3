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
            

            //open the file 
            FileInputStream fis = new FileInputStream(file);
            
            //to hold each chunk of data from the file
            byte[] buffer = new byte[512];
            int blocknum = 1;
            int bytesRead; 

            boolean transmissionComplete = false;    
            
            //create the datagram socket 
            DatagramSocket ds = new DatagramSocket();

            int retransmission = 0;

            //keep transmitting until finished. 
            while(!transmissionComplete){
                //reads from the file
                bytesRead = fis.read(buffer);


                //handles the final packet for when it is smaller then 512 or exsactly 512
                if (bytesRead < 512){
                    transmissionComplete = true; 
                    if(bytesRead == -1){
                        bytesRead = 0;
                    }

                }

                //create the data packet 
                byte[] dataPacket = new byte[2 + bytesRead];
                //checks it is data
                dataPacket[0] = DATA;
                dataPacket[1] = (byte) (blocknum);

                for(int i = 0; i < bytesRead; i++){
                    dataPacket[2 + i] = buffer[i];
                }


                //creates the packet to be ready to get sent
                DatagramPacket sendPacket = new DatagramPacket(dataPacket, dataPacket.length, req.getAddress(), req.getPort());
                //sends the datapacket through the socket
                ds.send(sendPacket);
                
                //ensures that the blocknum never goes above 255(1 byte)
                blocknum = (blocknum + 1) % 256;


                //create the ack datagram 
                byte[] ackBuffer = new byte[2];
                DatagramPacket ackPacket = new DatagramPacket(ackBuffer, ackBuffer.length);

                //set timeout for waiting to send another packet
                ds.setSoTimeout(1000);
   
                try{
                    //success found the ack
                    ds.receive(ackPacket);
                
                    retransmission = 0;
                    
                }
                //catches it if there is a timeout and then retransmists it. 
                catch(SocketTimeoutException ex){
                    ds.send(sendPacket);
                    retransmission++;

                }

                
                //checks if retransmissions is over 5 then terminates project 
                if(retransmission > 5){
                    System.err.println("Over 5 attemps and still no ACK, terminates project.");
                    return;
                }

            

            }


        }
        catch(Exception ex){
            System.out.println("something has gone wrong please try again:" + ex.toString());
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
        

       //gets the length takes off the rrq then loops through untill has the whole file name in a byte array. 
       int filelength = req.getLength() - 1; 
       byte []fileByteArray = new byte[filelength];
       for(int i = 0; i < filelength; i++){
            fileByteArray[i] = data[i + 1];
       }
       //turns the file name into a string 
       String fileNameString = new String(fileByteArray);
       //turns the string into a file 
       File fileName = new File(fileNameString);
       

        if(!fileName.exists()){
            //Send an error message packet
            
            InetAddress ia = req.getAddress();
            int port = req.getPort();

            sendErrorPacket("file not found", ia, port);

            return;

        }


        //checks that that it is a RRQ through the first byte
        if (data[0] == RRQ){
            //to store where in the data packet we are
            int index = 1;
            //calls send file with the whole file name
         
            sendfile(fileNameString);
          


        }
        else{
            System.out.println("this not a RRQ please try again.");
        }
	    return;
    }

    //create error packet to be sent whe
    public void sendErrorPacket(String error, InetAddress ia, int port){
        try{
            //copies error into a byte array
            byte[] errorBuffer = error.getBytes();
            byte [] copy = new byte[errorBuffer.length + 1];
            System.arraycopy(errorBuffer, 0, copy, 1, errorBuffer.length);
            //makes the first byte error
            copy[0] = ERROR;
            //create a datagramPacket
            DatagramPacket dp = new DatagramPacket(copy, copy.length, ia, port);
    
            //create the datagram socket 
            DatagramSocket ds = new DatagramSocket();
            ds.send(dp);
        }
        catch(Exception ex){
            System.out.println(ex.toString());
        }

    }

    public TftpServerWorker(DatagramPacket req)
    {
	this.req = req;
    }
}
