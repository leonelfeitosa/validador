package br.socket.domain;

import java.io.*;
import java.net.Socket;
import java.util.Properties;


public class ContainerProxy extends AbstractProxy {


	public static void main(String[] args) throws IOException {

		Properties prop = new Properties();

		String currentDir = System.getProperty("user.dir");
		String configPath = currentDir + "/container.properties";

		FileInputStream file = new FileInputStream(configPath);
		prop.load(file);


		System.out.println(prop.getProperty("sourcePort"));

		Integer containerPort = Integer.parseInt(prop.getProperty("containerPort"));
		Integer sourcePort = Integer.parseInt(prop.getProperty("sourcePort"));
		double serviceTime = Double.parseDouble(prop.getProperty("serviceTime"));
		String containerName = prop.getProperty("containerName");
		String targetIp = prop.getProperty("targetIp");
		boolean targetIsSource = Boolean.parseBoolean(prop.getProperty("targetIsSource"));


		new ContainerProxy(containerName, containerPort, new TargetAddress(targetIp,sourcePort), serviceTime, targetIsSource).start();
	}

	private double seviceTime;
	private boolean targetIsSource;


	public ContainerProxy(String name, Integer localPort, TargetAddress targetAddress, double seviceTime, boolean targetIsSource) {
		this.targetIsSource = targetIsSource;
		this.targetAddress = targetAddress;
		this.name = name;
		this.localPort = localPort;
		this.seviceTime = seviceTime;
	}


	@Override
	public void run() {

		new ConnectionEstablishmentOriginThread().start();
		new ConnectionEstablishmentDestinyThread().start();
		System.out.println("Starting "+ name);


		while (true) {
			if (hasSomethingToProcess()){
				contentToProcess += System.currentTimeMillis()+";";
				try {Thread.sleep((long) seviceTime);} catch (InterruptedException e) {e.printStackTrace();} //SE DESEJADO PODES SUBSTINUIR PELO SEU PROCESSAMENTO ESPECÍFICO

				if (targetIsSource)
					contentToProcess = registerTimeWhenGoOut(contentToProcess);
				try {sendMessageToDestiny(contentToProcess+ "\n");} catch (IOException e) {throw new RuntimeException(e);}

				contentToProcess = null;
			}
		}
	}


	protected void createConnectionWithDestiny() throws IOException {
		connectionDestinySocket = new Socket(targetAddress.getIp(),targetAddress.getPort());
	}

	@Override
	protected void receivingMessages(Socket socket) throws IOException {


		System.err.println(name+" enabled to receive messages.");

		String receivedMessage = null;
		DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
		while (true) {
			if (dataInputStream.available() > 0) {
				receivedMessage = dataInputStream.readLine();
			}

			if (receivedMessage!= null && receivedMessage.equals("ping")){
				ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
				if(hasSomethingToProcess()){
					oos.writeObject("busy");
				}else {
					oos.writeObject("free");
				}
				receivedMessage = null;
			}

			if (receivedMessage!= null && !receivedMessage.equals("ping")){
				receivedMessage = registerTimeWhenArrives(receivedMessage);
				setContentToProcess(receivedMessage);
				receivedMessage = null;
			}
		}
	}

	private static String registerTimeWhenArrives(String receivedMessage) {
		String lastRegisteredTimeStampString;
		String[] stringSplited;
		stringSplited = receivedMessage.split(";");
		lastRegisteredTimeStampString = stringSplited[stringSplited.length - 1];
		long timeNow = System.currentTimeMillis();
		receivedMessage += timeNow + ";" + (timeNow - Long.parseLong(lastRegisteredTimeStampString.trim())) + ";";
		return receivedMessage;
	}

	private String registerTimeWhenGoOut(String receivedMessage) {
		//REGISTRAR TEMPO DE PROCESSAMENTO
		receivedMessage += System.currentTimeMillis()+";";
		Long ultimo = Long.valueOf(receivedMessage.split(";")[receivedMessage.split(";").length - 1]);
		Long penultimo = Long.valueOf(receivedMessage.split(";")[receivedMessage.split(";").length - 2]);
		receivedMessage +=  (ultimo - penultimo) + ";";

		return receivedMessage;
	}


}
