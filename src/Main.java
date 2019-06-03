import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.openmuc.jsml.structures.EMessageBody;
import org.openmuc.jsml.structures.SmlFile;
import org.openmuc.jsml.structures.SmlListEntry;
import org.openmuc.jsml.structures.SmlMessage;
import org.openmuc.jsml.structures.responses.SmlGetListRes;
import org.openmuc.jsml.transport.SerialReceiver;
import org.pmw.tinylog.Logger;

import com.fazecast.jSerialComm.SerialPort;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import it.sauronsoftware.cron4j.Scheduler;

public class Main {
	
	private static byte[] OBIS_BEZUG = { 1, 0, 1, 8, 0, (byte) 0xff};
	private static byte[] OBIS_P = { 1, 0, 16, 7, 0, (byte) 0xff};
	private static byte[] OBIS_PL1 = { 1, 0, 36, 7, 0, (byte) 0xff};
	private static byte[] OBIS_PL2 = { 1, 0, 56, 7, 0, (byte) 0xff};
	private static byte[] OBIS_PL3 = { 1, 0, 76, 7, 0, (byte) 0xff};
	private static byte[] OBIS_LIEFERUNG = { 1, 0, 2, 8, 0, (byte) 0xff};
	
	private static Stack<SmlFile> smlFiles = new Stack<SmlFile>();
	
	// get a handle to the GPIO controller
	final static GpioController gpio = GpioFactory.getInstance();
	final static GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "PinLED", PinState.LOW);
	
	public static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public static void blink() {
    	pin.pulse(900, true);
    	sleep(300);
	}
	
	public static void blink(int cnt) {
		for (int i = 0; i < cnt; i++) {
			blink();
		}
	}
	
	public static void enterPin(int pinNumber) {
		int tausender = pinNumber / 1000;
		int hunderter = pinNumber / 100 % 10;
		int zehner = pinNumber / 10 % 10;
		int einer = pinNumber % 10;
		
		Logger.info("Try PIN " + tausender + "" + hunderter + "" + zehner + "" + einer);
		
		blink();
		sleep(200);
		blink();
		sleep(500);
		
		blink(tausender);
		sleep(4000);
		blink(hunderter);
		sleep(4000);
		blink(zehner);
		sleep(4000);
		blink(einer);
		sleep(4000);
	}
	
	public static boolean checkData() {
		try {
			Logger.debug("entries in stack: " + smlFiles.size());
	    	SmlFile smlFile = smlFiles.pop();
	    	smlFiles.clear();

	        for (SmlMessage msg : smlFile.getMessages()) {
	        	EMessageBody messageBody = msg.getMessageBody().getTag();
	        	
	            switch (messageBody) {
	
	            case GET_LIST_RESPONSE:
	            	SmlGetListRes smlListRes = msg.getMessageBody().getChoice();
	            	
	            	// ggf. anpassen, hier wird davon aus gegangen, dass der Zähler im
	            	// Fall einer erfolgreichen PIN Eingabe mehr als 9 Werte liefert
	            	if (smlListRes.getValList().getValListEntry().length > 9) return true;
	            	
	                break;
	            default:
	            	break;
	            }
	        }
		} catch (Throwable t) {
			Logger.error(t);
		}
		return false;
	}
	
	public static void brutePin(int start) {
		Logger.info("Brute Force started...");
		for (int i = start; i < 10000; i++) {
			enterPin(i);
			sleep(500);
			if (checkData()) {
				Logger.info("found PIN! " + i);
				break;
			}
			sleep(1000);
		}
	}

	public static void main(String[] args) throws IOException {
		Runnable smlRunnable = new Runnable() {
			
			@Override
			public void run() {
				try {
					SerialPort comPort;
					comPort = SerialPort.getCommPort("/dev/ttyAMA0");
			    	comPort.setBaudRate(9600);
			    	comPort.openPort(SerialPort.TIMEOUT_SCANNER, 0, 0);
	
			    	ComPortConverter convertedPort = new ComPortConverter(comPort);
			    	SerialReceiver receiver = new SerialReceiver(convertedPort);
			    	
					while(true) {
						try {
							smlFiles.push(receiver.getSMLFile());
						} catch (Throwable t) {
							Logger.error(t);
						}
					}
				} catch (Throwable t) {
					Logger.error(t);
				}
			}
		};
		
		Thread smlThread = new Thread(smlRunnable);
		smlThread.start();
		
        for (String arg : args) {
        	if (arg.contains("-b")) {
        		brutePin(0);
        		return;
        	}
        }
    	
		Scheduler importScheduler = new Scheduler();
		
		// Setzt einen cronTrigger für einen stündlichen Durchlauf
		importScheduler.schedule(ConfigManager.getInstance().getProperties().getProperty(ConfigManager.CONFIG_KEY_CRONTRIGGER_IMPORT, "*/1 * * * *"), new Runnable() {
			public void run() {
				try {
			    	Builder dataPointBuilder = Point.measurement("SmartMeter").time(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
			    	
			    	Logger.debug("entries in stack: " + smlFiles.size());
			    	SmlFile smlFile = smlFiles.pop();
			    	smlFiles.clear();
			    	
			    	if (smlFile != null) {
				        for (SmlMessage msg : smlFile.getMessages()) {
				        	EMessageBody messageBody = msg.getMessageBody().getTag();
				        	
				            switch (messageBody) {
					            case GET_LIST_RESPONSE:
					            	SmlGetListRes smlListRes = msg.getMessageBody().getChoice();
					            	for (SmlListEntry listItem : smlListRes.getValList().getValListEntry()) {
					            		byte[] objName = listItem.getObjName().getValue();
					            		
					            		if (Arrays.equals(objName, OBIS_BEZUG)) {
					            			Logger.debug(Integer.valueOf(listItem.getValue().toString()) + "Wh");
					            			BigDecimal val = new BigDecimal(listItem.getValue().toString());
					            			val = val.divide(new BigDecimal(10000));
					            			dataPointBuilder = dataPointBuilder.addField("E-Total", val);
					            		} else if (Arrays.equals(objName, OBIS_P)) {
					            			Logger.debug(Integer.valueOf(listItem.getValue().toString()) + "W");
					            			dataPointBuilder = dataPointBuilder.addField("P", Integer.valueOf(listItem.getValue().toString()));
					            		} else if (Arrays.equals(objName, OBIS_PL1)) {
					            			dataPointBuilder = dataPointBuilder.addField("PL1", Integer.valueOf(listItem.getValue().toString()));
					            		} else if (Arrays.equals(objName, OBIS_PL2)) {
					            			dataPointBuilder = dataPointBuilder.addField("PL2", Integer.valueOf(listItem.getValue().toString()));
					            		} else if (Arrays.equals(objName, OBIS_PL3)) {
					            			dataPointBuilder = dataPointBuilder.addField("PL3", Integer.valueOf(listItem.getValue().toString()));
					            		} else if (Arrays.equals(objName, OBIS_LIEFERUNG)) {
					            			BigDecimal val = new BigDecimal(listItem.getValue().toString());
					            			val = val.divide(new BigDecimal(10000));
					            			dataPointBuilder = dataPointBuilder.addField("Gen-Total", val);
					            		}
					            	}
					                break;
				            default:
				            	//System.out.println("type not found");
				            }
				        }
				        DBHelper.getInstance().writeInflux(dataPointBuilder.build());
					}
				} catch (Throwable t) {
					Logger.error(t);
				}
			}
		});
		importScheduler.start();
	}

}
