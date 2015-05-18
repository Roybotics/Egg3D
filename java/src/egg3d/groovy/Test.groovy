#!/usr/bin/env groovy

Random rnd = new Random()

String hostname = "localhost"
if(args.length!=0) hostname= args[0]

/*socket = new Socket(hostname, 4444);

double theta =0

for(int i=0; i<1000; i++)
{

  	double m = Math.cos(theta/2)
  	double u = 0*Math.sin(theta/2)
  	double v = 0*Math.sin(theta/2)
  	double w = 1*Math.sin(theta/2)

  	socket << "[${m},${u},${v},${w}]\n"
	theta+=0.03

	Thread.sleep(10)
}


socket.close()
/**/


server = new ServerSocket(4444)

println("Waiting for connection")

double theta =0

while(true) 
{
    server.accept() 
    { socket ->
        try 
        {
        	theta=0
        	for(int i=0; i<10000000; i++)
			{

			  	double m = Math.cos(theta/2)
			  	double u = 0*Math.sin(theta/2)
			  	double v = 0*Math.sin(theta/2)
			  	double w = 1*Math.sin(theta/2)

			  	socket << "[${m},${u},${v},${w}]\n"
				theta+=0.01

				if(((int)theta)%10==0) println theta

				Thread.sleep(10)
			}
			socket.close()
        }
        catch(Exception e) 
        {
        	println e
        }
    }
}/**/

/*
socket = new DatagramSocket(4444)

println("Waiting for connection")

double theta =0

while(true) 
{
    try 
    {
    	theta=0
    	for(int i=0; i<1000; i++)
		{

		  	double m = theta //Math.cos(theta/2)
		  	double u = 0*Math.sin(theta/2)
		  	double v = 0*Math.sin(theta/2)
		  	double w = 1*Math.sin(theta/2)

		  	String message = "[${m},${u},${v},${w}]\n"
    		outgoing = new DatagramPacket(message.bytes, message.size(), incoming.address, incoming.port)
    		socket.send(outgoing)

			theta+=0.03

			println theta

			Thread.sleep(10)
		}
		
    }
    catch(Exception e) 
    {
    	println e
    }
    
}
socket.close()
/*
//----------------------------------------------------------------------------------
// UDP server
socket = new DatagramSocket(4444)
buffer = (' ' * 4096) as byte[]
while(true) {
    incoming = new DatagramPacket(buffer, buffer.length)
    socket.receive(incoming)
    s = new String(incoming.data, 0, incoming.length)
    String reply = "Client said: '$s'"
    outgoing = new DatagramPacket(reply.bytes, reply.size(),
            incoming.address, incoming.port);
    socket.send(outgoing)
}

// UDP client
data = "Original Message".getBytes("ASCII")
addr = InetAddress.getByName("localhost")
port = 5000
packet = new DatagramPacket(data, data.length, addr, port)
socket = new DatagramSocket()
socket.send(packet)
socket.setSoTimeout(30000) // block for no more than 30 seconds
buffer = (' ' * 4096) as byte[]
response = new DatagramPacket(buffer, buffer.length)
socket.receive(response)
s = new String(response.data, 0, response.length)
println "Server said: '$s'"
// => Server said: 'Client said: 'Original Message''
//----------------------------------------------------------------------------------
/**/