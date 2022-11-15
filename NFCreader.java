import javax.smartcardio.*;
import javax.swing.event.SwingPropertyChangeSupport;

import java.nio.ByteBuffer;
import java.util.*;
import java.text.SimpleDateFormat;

import java.time.LocalTime;  
import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.text.ParseException;
import java.util.Date;
import java.util.Calendar;
import java.nio.file.StandardOpenOption;
import java.io.BufferedWriter;
import java.nio.file.Path;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.io.UnsupportedEncodingException;
import org.eclipse.paho.client.mqttv3.*;

// import org.apache.kafka.common.serialization.StringSerializer;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.apache.kafka.clients.producer.*;
// import org.apache.kafka.clients.producer.KafkaProducer;
// import org.apache.kafka.clients.consumer.ConsumerRecord;
// import org.apache.kafka.clients.consumer.ConsumerRecords;
// import org.apache.kafka.clients.consumer.KafkaConsumer;

public class NFCreader implements MqttCallback, Runnable{
	private oled display;
	private MqttClient client;
	private MqttConnectOptions connOpts;
	// private PahoMqttSubscribe mqtt;
	private SimpleDateFormat dateformat;
	private String timestamp;
	private int status = 1;
	private long unixtime;
	String clientId = MqttClient.generateClientId();
	String serverURI = "tcp://192.168.100.200:9096";
	// String serverURI = "tcp://192.168.0.24:1883";
	String username = "winirnd";
	String password = "winitech@12345";
	String topic = "/mq/v1/test/access-data";
	// String topic = "outTopic";
	
	public NFCreader(){
//		mqtt = new PahoMqttSubscribe();
		display = new oled();
//		oled.oledInit();//OLED초기화
		dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		MqttInitialize();

	}

	private void MqttInitialize() {
		try{
			client = new MqttClient(serverURI, clientId);
			connOpts = setUpConnectionOptions(username, password);
       		client.connect(connOpts);
			client.setCallback(this);
			// client.subscribe("inTopic");
			display.oledWriteString(3, 3,"Setting Complete!");
			display.oledClear();
			display.oledWriteString(1,4,"Tagging Watch!");
		} catch(MqttException e) {e.printStackTrace();}
		
	}

	//유저정보 코드 수정 --------------------------------------------------
	private static MqttConnectOptions setUpConnectionOptions(String user, String pass) {
		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setCleanSession(true);
		connOpts.setUserName(user);
		connOpts.setPassword(pass.toCharArray());
		return connOpts;
	}  
	
	@Override
	public void messageArrived(String message, MqttMessage arrived)throws Exception{
		
	}
	
	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		String complete_time = dateformat.format(System.currentTimeMillis());
		System.out.println("complete_time = "+complete_time);
	}
	
	@Override
	public void connectionLost(Throwable cause) {
		System.out.println("Connection lost..");
		display.oledClear();
		display.oledWriteString(3, 3, "Connection lost..");
	}
	
	public void sendingMessage(String message) {
		try {
			client.publish(topic,message.getBytes(),1,true);
		} catch(MqttException e) {e.printStackTrace();}
	}
	
	private CardTerminal initializeTerminal(){
		TerminalFactory factory = null;
		List<CardTerminal> terminals = null;
		
		try{
			factory = TerminalFactory.getDefault();
			terminals = factory.terminals().list();
			if(status == 0)
			{
				display.oledClear();
				display.oledWriteString(1,4,"Tagging Watch!");
			}
	//			lcd.clearLcd();
			status = 1;
			return terminals.get(0);
		}catch(CardException e){
			if(status==1)
			{
				display.oledClear();
				display.oledWriteString(1, 1, "reader error!");
	//			lcd.errormessage("reader error!");
//				System.out.println("리더기가 없음");
				status = 0;
			}
			return null;
		}
	}
	
	private boolean IsCardPresent(CardTerminal terminal)throws CardException{
		boolean isCard = false;
	
		while(!isCard){
			isCard = terminal.waitForCardPresent(0);
			if(isCard)
				timestamp = dateformat.format(System.currentTimeMillis());
				unixtime = System.currentTimeMillis() / 1000;
		}
		
		return true;
	}
	
	private static String SendCommand(byte[] cmd, CardChannel channel){
		String response = "";
		byte[] bres = new byte[258];
		ByteBuffer bufcmd = ByteBuffer.wrap(cmd);
		ByteBuffer bufResp = ByteBuffer.wrap(bres);
		int output = 0;
		
		try{
			output = channel.transmit(bufcmd, bufResp);
		}catch(CardException ex){
			ex.printStackTrace();
		}
		
		for(int i = 0; i < output; i++)
			response += String.format("%02x",bres[i]);
		
		if(response.length() != 18)
			if(response.length() < 18){
				int count = 18-response.length();
				for(int i = 0; i < count; i++)
					response += "0";
				System.out.println("");
			}
		else
			System.out.println("card err");
		return response;
	}
	
	private void GetCardAndOpenChannel(CardTerminal terminal)throws CardException{
		Card card = terminal.connect("*");
		CardChannel channel = card.getBasicChannel();
		byte[] command = new byte[]{(byte)0xFF, (byte)0xCA, (byte)0x00, (byte)0x00, (byte)0x00};
		String UID = SendCommand(command,channel);
		
		String message = UID+"\t"+timestamp;
		isAccessCard(UID,message);
	}

	
	private void isAccessCard(String target, String message){
		try{
			String inouts="in";
			String greet="";
			String name=null;
			FileWriter fw;
			int cnt;
			List<String> contents = null;
			contents= Files.readAllLines(Paths.get("DATE.txt"));

			for(cnt = 0;cnt<contents.size();cnt++)
			{
				System.out.println("target : "+target+", content : "+contents.get(cnt).split(" ")[0]);
				if(target.equals(((String) contents.get(cnt)).split(" ")[0]))
				{
					System.out.println("found name!");
					name = ((String) contents.get(cnt)).split(" ")[1];
		//		lcd.printNormalInfo(contents.get(cnt).split(" ")[1]);
					break;
				}
			}
			if(cnt == contents.size()){
				name = "unknown";
		//		lcd.printNormalInfo("unknown");
				fw = new FileWriter("unknownlog.txt",true);
			}
			else
				fw = new FileWriter("log.txt",true);
			fw.write(message+ "\t" +name+"\n");
			fw.close();
			System.out.println(name+" : "+message);
			
			//INOUT 판독 코드 + 같은 uid 10초 금지-------------------------------------------------------
			contents= Files.readAllLines(Paths.get("inOut.txt"));
			String dummy = "";
			String intime;
			String indatetime;
			for(cnt = 0;cnt<contents.size();cnt++)
			{
				if(!name.equals("unknown")){
					if(target.equals(((String) contents.get(cnt)).split(" ")[0]))
					{
						intime = (((String) contents.get(cnt)).split(" ")[1] + " " +((String) contents.get(cnt)).split(" ")[2]);
						try {
							SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							Date date = format.parse(intime);
							
							Calendar cal = Calendar.getInstance();
							cal.setTime(date);
							cal.add(Calendar.SECOND, 15);
							indatetime = dateformat.format(cal.getTime());
							if(timestamp.compareTo(indatetime)>=0){
								System.out.println("Matching name!");
								inouts="out";
								greet="안녕히가세요";
							} else{
								System.out.println("Matching name!");
								inouts="checked";
								greet="이미 확인되었습니다";
							}
						} catch(ParseException e) {
							e.printStackTrace();
						}
					} else {
						dummy += (String)contents.get(cnt)+"\n"; 
					}
				}
			}
			if(inouts=="in"){
				greet="안녕하세요";
				if(!name.equals("unknown")){
					fw = new FileWriter("inOut.txt",true);
					fw.write(target+ " " + timestamp+ " " +name+"\n");
					fw.close();
				}
				sendingMessage("{\n\"wth_id\":\"" + name + "\",\n\"eventType\":\"" + inouts + "\",\n\"transmissionTimestamp\":" + unixtime +"\n}");	
			} else if(inouts=="out") {
				try (BufferedWriter bf = Files.newBufferedWriter(Path.of("inOut.txt"),
                StandardOpenOption.TRUNCATE_EXISTING)) {
				} catch (IOException e) {
					e.printStackTrace();
				}
				fw = new FileWriter("inOut.txt",true);
				fw.write(dummy);
				fw.close();
				sendingMessage("{\n\"wth_id\":\"" + name + "\",\n\"eventType\":\"" + inouts + "\",\n\"transmissionTimestamp\":" + unixtime +"\n}");	
			}
			//INOUT 판독 코드 -------------------------------------------------------

			// sendingMessage(message + "\t" + name);	
			// sendingKafka(message + "\t" + name);	//kafka code add
	//		mqtt.sendingMessage(message);
			display.oledClear();
			if(name.equals("unknown")){
				display.oledWriteString(1, 4, "비인가자 입니다.");
			}
			else {
				display.oledWriteString(1, 2, greet+" "+name+"님.");
				display.oledWriteString(1, 4, timestamp.split(" ")[0]);
				display.oledWriteString(1, 5, timestamp.split(" ")[1]);
			}
		}catch(IOException e){
			display.oledClear();
			display.oledWriteString(1, 1, "No file...");
			System.out.println("Nofile...");
	//		lcd.clearLcd();
	//		lcd.errormessage("No file!");
		}
	}


	////////////////////////// kafka code add///////////////////////////

	// public void sendingKafka(String message) {
	// 	try {
	// 		// set kafka properties
	// 		Properties configs = new Properties();
	// 		configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.0.24:9092");  // kafka cluster
	// 		configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());// KEY_SERIALIZER
	// 		configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName()); // VALUE_SERIALIZER

	// 		// init KafkaProducer
	// 		KafkaProducer<String, String> producer = new KafkaProducer<>(configs);

	// 		// set ProducerRecord
	// 		String topic = "jonghun";           // topic name
	// 		String data = (message); ;   		// data

	// 		ProducerRecord<String, String> record = new ProducerRecord<>(topic, data);
	// 		// send record
	// 		producer.send(record);
	// 		System.out.println("producer.send() >> [topic:" + topic + "][data:" + data + "]");
	// 	} catch (Exception e) {
	// 		e.printStackTrace();
	// 	}
	// }

	////////////////////////// kafka code add///////////////////////////
	

	public void start(){
		CardTerminal terminal;
		
		while(true){
			try{
				terminal = initializeTerminal();
				if(status == 1){
					if(IsCardPresent(terminal)){
						GetCardAndOpenChannel(terminal);
					}
					Thread.sleep(2000);
					display.oledClear();
					display.oledWriteString(1,4,"Tagging Watch!");
				}
				
			}
			catch(CardException e){
				e.printStackTrace();
			}
			catch(InterruptedException e){
				e.printStackTrace();
			}
		}
	}

	public void run(){
		System.out.println("ho");
		display.oledClear();
		System.out.println("ri");
		display.oledWriteString(1,4,"System Down...");
		System.out.println("System Down...");
	}

	public static void main(String[] args){
		NFCreader app = new NFCreader();
		Runtime.getRuntime().addShutdownHook(new Thread(app));
		app.start();

	}
}
