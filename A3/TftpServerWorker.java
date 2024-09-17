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
                //Need to make an errror packet. ///////
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

                //creates the packet to be ready to get sent
                DatagramPacket sendPacket = new DatagramPacket(dataPacket, dataPacket.length, req.getAddress(), req.getPort());
                //sends the datapacket through the socket
                ds.send(sendPacket);
                
                //ensures that the blocknum never goes above 255(1 byte)
                blocknum = (blocknum + 1) % 256;


                //need to check size of last packet sent and handle the final packet. 



                
                //create the ack datagram 
                byte[] ackBuffer = new byte[2];
                DatagramPacket ackPacket = new DatagramPacket(ackBuffer, ackBuffer.length);

                //set timeout for waiting to send another packet
                ds.setSoTimeout(1000);

                boolean ackRecieved = false; 

                int retransmission = 0;

                while(retransmission < 5 && !ackRecieved){
                    try{
                        ds.receive(ackPacket);
                        //checks that the first byte is a ACK and then the second is the correct byte in order we should be getting. 
                        if(ackBuffer[0] == 3 && ackBuffer[1] == (byte) (blocknum)){
                            ackRecieved = true;
                        }
                        
                    }
                    //catches it if there is a timeout and then retransmists it. 
                    catch(SocketTimeoutException ex){
                        ds.send(sendPacket);
                        retransmission++;

                    }

                }
                //checks if retransmissions is over 5 then terminates project 
                if(retransmission >= 5 && !ackRecieved){
                    System.err.println("Over 5 attemps and still no ACK, terminates project.");
                    return;
                }



            }



        }
        catch(Exception ex){
            System.out.println("something has gone wrong:" + ex.toString());

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

        //checks that that it is a RRQ through the first byte
        if (data[0] == 1){

            //might redo don't like string builders 
            StringBuilder fileNameBuilder = new StringBuilder();

            //to store where in the data packet we are
            int index = 1;

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
