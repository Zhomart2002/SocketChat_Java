import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Server {

	private static ArrayList<Client> clients = new ArrayList<>();
	private static SimpleDateFormat format = new SimpleDateFormat("HH:mm");
	private static Date date = new Date();

	public static void main(String[] args) {
		new Thread(() -> {
			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket(8000);
				System.out.println("Server opened");
				while (!serverSocket.isClosed()){
					Socket socket = serverSocket.accept();
					System.out.println("Connected");
					Client client = new Client(socket);
					clients.add(client);
					client.start();
				}
			}catch (IOException e){
				System.out.println("Closed");
			}

		}).start();
	}

	private synchronized static void sendToClients(String message, Client cl){
		for(Client client: clients){
			if (cl != client)
				client.write(message);
		}
	}

	private static class Client extends Thread {

		private Socket socket;
		private DataInputStream fromClient;
		private DataOutputStream toClient;

		public Client(Socket socket) {
			this.socket = socket;			
		}

		@Override
		public void run() {
			try {
				fromClient = new DataInputStream(socket.getInputStream());
				toClient = new DataOutputStream(socket.getOutputStream());

				String s;
				while (true) 
					if ((s = fromClient.readUTF()) != null){
						sendToClients("[" + format.format(date) + "] " + s, this);
					}
			
			} catch (IOException e) {
				System.out.println("Disconnected");
				clients.remove(this);
			}
		}
		
		public void write(String message){
			try {
				toClient.writeUTF(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

}