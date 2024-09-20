import java.net.*;
import java.io.*;
import java.util.*;

class TftpClient{

	private DatagramPacket req;
    private static final byte RRQ = 1;
    private static final byte DATA = 2;
    private static final byte ACK = 3;
    private static final byte ERROR = 4;


    public static void main(String []args) throws IOException {
		try {
			// check we have the correct arugumments
			if (args.length != 3) {
				System.err.println("Please ensure the correct amount of inputs and name correctly.");
				return;
			}
			// get an IP address for the hostname
			InetAddress ia = InetAddress.getByName(args[0]);
			// get the port to send to
			int port = Integer.parseInt(args[1]);
			// create the byte array with the string to be sent (from the third argument)
			byte []fileNameBuf = args[2].getBytes();
			//add the RRQ byte to the start of filename
			byte []arrayCopy = new byte [fileNameBuf.length + 1];
			System.arraycopy(fileNameBuf, 0, arrayCopy, 1, fileNameBuf.length);
			arrayCopy[0] = RRQ;
			//creates new file to store the recived packets into. 
			FileOutputStream fos = new FileOutputStream(new File(new String("transmission" + args[2])));
			
			// create datagram socket that will be used to send packet
			DatagramSocket ds = new DatagramSocket();

			// construct the DatagramPacket which has the contents of
			// the byte array, the IP address and port to send to
			DatagramPacket dp = new DatagramPacket(arrayCopy, arrayCopy.length, ia, port);
			// send the DatagramPacket via DatagramSocket
			ds.send(dp);

			
			//recivies data and sends acks 
			while(true){
				//creates a datagram packet
				dp = new DatagramPacket(new byte[514], 514);
				//recives the datagram packet through the socket
				ds.receive(dp);

				//If a error packet gets sent then will enter this. 
				if(dp.getData()[0] == ERROR){
					//error message so client knows what is going on. 
					System.out.println("Error Packet recieved, File not found.");
					//close to stop leakage
					fos.close();
					ds.close();
					//deletes the file if there is an error. 
					File f = new File ("transmission" + args[2]);
					f.delete();
					//exits the method. 
					System.exit(1);
				}

				

                //adds the pakcet into the new file while getting rid of headers.
                fos.write(dp.getData(), 2, dp.getLength() - 2);
                
				//gets new port and ia 
				ia = dp.getAddress();
				port = dp.getPort();

					

				//gets teh block number from the dp and uses logic and get get ride of unnessarary numbers. 
				int blockNum = dp.getData()[1] & 0xFF;	

				//sends the ack to correct location 
				sendACK(blockNum, ds, ia, port);

				//if the packet length less that 514 then breaks out of teh loop. 
				if(dp.getLength() < 514){
					break;
				}
				

			}
			fos.close();
			ds.close();
			
		}
		//catches any exceptions
		catch(Exception e) {
			System.err.println(e.toString());
		}
		
		return;

	}

    //constructs the ACKs
	public static void sendACK(int blockNum, DatagramSocket ds, InetAddress ia, int port) throws IOException{
			  
		//create the ack datagram 
		byte[] ackBuffer = new byte[2];
		//makes byte 1 ack and 2 the block num. 
		ackBuffer[0] = ACK;
		ackBuffer[1] = (byte)blockNum;
		//creates the ack datagram packet 
		DatagramPacket ackPacket = new DatagramPacket(ackBuffer, ackBuffer.length, ia, port);
		//sends it to the server. 
		ds.send(ackPacket);      

	}

}