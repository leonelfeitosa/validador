package br.socket.domain;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ServerProxy extends  AbstractProxy{


	public static void main(String[] args) {
		String serverName = args[0];
		Integer serverPort = Integer.valueOf(args[1]);
		String containerIp = args[2];
		Integer containerPort = Integer.valueOf(args[3]);
		Integer queueServerMaxSize = Integer.valueOf(args[4]);
		Integer qtdContainers = Integer.valueOf(args[5]); //as portas destinos deverão estar em sequência. Ex: containerPort1 3000, containerPort2 3001, etc...

		if(qtdContainers>1) {
			List<TargetAddress> lt = new ArrayList<TargetAddress>();
			Integer port = containerPort;
			while(qtdContainers>0) {
				TargetAddress ta = new TargetAddress(containerIp, port);

				lt.add(ta);

				port++;
				qtdContainers--;
			}

			new ServerProxy(serverName, serverPort, lt, queueServerMaxSize).start();

		}else {
			new ServerProxy(serverName, serverPort, Arrays.asList(new TargetAddress(containerIp, containerPort)), queueServerMaxSize).start();
		}
	}



	List <TargetAddress> containerAddresses;

	Integer queueServerMaxSize;

	List<String> queue;

	List<Socket> connectionDestinySockets;

	public ServerProxy(String name,  Integer localPort, List<TargetAddress> containerAddresses, Integer queueServerMaxSize) {
		this.name = name;
		this.localPort = localPort;
		this.containerAddresses = containerAddresses;
		this.queue = new ArrayList<>();
		this.queueServerMaxSize = queueServerMaxSize;
		targetAddress = containerAddresses.get(0);
		connectionDestinySockets = new ArrayList<>();
	}

	@Override
	public void createConnectionWithDestiny() throws IOException {
		for (TargetAddress targetAddress: containerAddresses) {
			connectionDestinySockets.add(new Socket(targetAddress.getIp(),targetAddress.getPort()));
		}
	}

	@Override
	public synchronized boolean hasSomethingToProcess() {
		return !this.queue.isEmpty();
	}

	public void run() {
		try {
			new ConnectionEstablishmentOriginThread().start();
			new ConnectionEstablishmentDestinyThread().start();
			System.out.println("Starting " + name);

			String msg = null;
			while (true) {
				Thread.sleep((int) (1));//se tirar isso o código não funciona
				if (hasSomethingToProcess()) {


					boolean notSentYet = true;
					while (notSentYet) {
							for (Socket socket : connectionDestinySockets) {
								if (isDestinyFree(socket)) {
									msg = queue.remove(0);
									sendMessageToDestiny(msg, socket);
									notSentYet = false;
									break;
								}
							}
						}

				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void receivingMessages(Socket socket) throws IOException, InterruptedException {
		String receivedMessage = null;


		System.err.println(name+" enabled to receive messages.");

		DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
		while (true) {
			if (dataInputStream.available() > 0) {
				receivedMessage = dataInputStream.readLine();
			}

			if (receivedMessage!= null && receivedMessage.equals("ping")){
				ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
				if(this.queue.size() < queueServerMaxSize){
					oos.writeObject("free");
				}else{
					oos.writeObject("busy");
				}
				receivedMessage = null;
			}

			if (receivedMessage!= null && !receivedMessage.equals("ping")){
				receivedMessage = Utils.registerTime(receivedMessage);
				receivedMessage += System.currentTimeMillis() + ";" + "\n";

				this.queue.add(receivedMessage);
				receivedMessage = null;
			}
		}
	}


}
