import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;

public class oled{
	private int flag = 0;
	static int count = 0;
	
	static GpioController gpioController= GpioFactory.getInstance();
	
	static GpioPinDigitalOutput DC= gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_07);
	static GpioPinDigitalOutput SDK= gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_21);
	static GpioPinDigitalOutput SCLK= gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_22);
	static GpioPinDigitalOutput RESET= gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_09);
	static GpioPinDigitalOutput led= gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_08);
	
	static String SSD1306_PIXEL_WIDTH="80";//inttohex(128);
	static String  SSD1306_PIXEL_HEIGHT="40";//inttohex(64);
	static String  SSD1306_PAGE_COUNT="08";//inttohex(8);
	static String  SSD1306_PAGE_HEIGHT="02";//inttohex(2);
	//static String  SSD1306_SEGMENT_COUNT= inttohex(7808);//7808 //check
	static String  SSD1306_FONT_WIDTH="01";//inttohex(1);

	
	//fundamental commands
	static String  SSD1306_SET_CONTRAST        ="81";//inttohex(129);
	static String  SSD1306_CONTRAST_DEFAULT    ="7f";//inttohex(127);  //  0b01111111
	static String  SSD1306_DISPLAY             ="a4";//inttohex(164); //           0b10100100
	static String  SSD1306_DISPLAY_RESET       ="a4";//inttohex(164); //         SSD1306_DISPLAY
	static String  SSD1306_DISPLAY_ALLON       ="a5";//inttohex(165); //        SSD1306_DISPLAY | 0b01
	static String  SSD1306_DISPLAY_NORMAL      ="a6";//inttohex(166); //       SSD1306_DISPLAY | 0b10
	static String  SSD1306_DISPLAY_INVERSE     ="a6";//inttohex(166); //      SSD1306_DISPLAY | 0b11
	static String  SSD1306_DISPLAY_SLEEP       ="ae";//inttohex(174); //      SSD1306_DISPLAY | 0b1110
	static String  SSD1306_DISPLAY_ON          ="af";//inttohex(175); //     SSD1306_DISPLAY | 0b1111
	static String  SSD1306_DISPLAYALLON_RESUME ="a4";//inttohex(164); // 0xA4

	// addressing
	static String  SSD1306_ADDRESSING              ="20";//inttohex(32);  // 0x20
	static String  SSD1306_ADDRESSING_HORIZONTAL   ="00";//inttohex(0);  //
	static String  SSD1306_ADDRESSING_VERTICAL     ="01";//inttohex(1);  //
	static String  SSD1306_ADDRESSING_PAGE         ="02";//inttohex(2);  //
	static String  SSD1306_SET_COLUMN              ="21";//inttohex(33);  // 0x21
	static String  SSD1306_SET_PAGE               ="22";//inttohex(34);  // 0x22

	// hardware configuration
	static String  SSD1306_SET_START_LINE          ="40";//inttohex(64);  // 0x40
	static String  SSD1306_START_LINE_DEFAULT       ="00";//inttohex(0);  //
	static String  SSD1306_SET_SEG_SCAN             ="a0";//inttohex(160);  // 0xA0
	static String  SSD1306_SET_SEG_SCAN_DEFAULT    ="a0";//inttohex(160);  // SSD1306_SET_SEG_SCAN | 0b00
	static String  SSD1306_SET_SEG_SCAN_REVERSE     ="a1";//inttohex(161);  //SSD1306_SET_SEG_SCAN | 0b01
	static String  SSD1306_SET_MULTIPLEX_RATIO      ="a8";//inttohex(168);  //0b10101000 // 0xA8
	static String  SSD1306_MULTIPLEX_RATIO_DEFAULT ="3f";//inttohex(63);  // 0b00111111
	static String  SSD1306_SET_COM_SCAN             ="c0";//inttohex(192);  // 0xC0
	static String  SSD1306_SET_COM_SCAN_DEFAULT     ="c0";//inttohex(192);  //SSD1306_SET_COM_SCAN | 0b0000
	static String  SSD1306_SET_COM_SCAN_REVERSE     ="c8";//inttohex(200);  //SSD1306_SET_COM_SCAN | 0b1000
	static String  SSD1306_SET_DISPLAY_OFFSET       ="d3";//inttohex(211);  // // 0xD3
	static String  SSD1306_DISPLAY_OFFSET_DEFAULT  ="00";//inttohex(0);  // 0b00000000
	static String  SSD1306_SET_COM_PINS             ="da";//inttohex(218);  //// 0xDA
	static String  SSD1306_COM_PINS_DEFAULT         ="12";//inttohex(18);  //

	// timing and driving
	static String  SSD1306_SET_CLOCK_FREQUENCY    ="d5";//inttohex(213);  // 0xD5
	static String  SSD1306_CLOCK_FREQUENCY_DEFAULT ="80";//inttohex(128);  //
	static String  SSD1306_SET_PRECHARGE           ="d9";//inttohex(217);  //0xD9
	static String  SSD1306_PRECHARGE_DEFAULT       ="22";//inttohex(34);  //
	static String  SSD1306_SET_VCOMH_DESELECT      ="db";//inttohex(219);  // 0xDB
	static String  SSD1306_VCOMH_DESELECT_DEFAULT  ="20";//inttohex(32);  //
	static String  SSD1306_SET_CHARGE_PUMP         ="8d";//inttohex(141);  // 0x8D
	static String  SSD1306_CHARGE_PUMP_ENABLE      ="14";//inttohex(20);  //
	static String  SSD1306_NOP ="e3";//inttohex(227);  //0xE3
	
//	public static void main(String[] args) {
//		System.out.println("oled code started!");
//		
//		led.high();
//		try {
//			TimeUnit.MICROSECONDS.sleep(1000000);
//		} catch(InterruptedException e) {
//			e.printStackTrace();
//		}
//		
//		led.low();
//		try {
//			TimeUnit.MICROSECONDS.sleep(1000000);
//		} catch(InterruptedException e) {
//			e.printStackTrace();
//		}
//		
//		oledInit();
//		System.out.println("oled initialization done");
//		oledClear();
//		System.out.println("oled cleared");
//		oledWriteString(1,1,"안녕하세요 방문자1님");
//	}
	oled(){
		oledInit();
		oledClear();
	}
	
	void oledInit() {
		
		led.high();
		try {
			TimeUnit.MICROSECONDS.sleep(1000000);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		led.low();
		try {
			TimeUnit.MICROSECONDS.sleep(1000000);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		RESET.low();//칩 초기화
		
		try {
			TimeUnit.MICROSECONDS.sleep(5);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		RESET.high();//칩 가동
		oledWriteCmd(SSD1306_SET_MULTIPLEX_RATIO);
		System.out.println("reached here 1");
		oledWriteCmd(SSD1306_MULTIPLEX_RATIO_DEFAULT);
		System.out.println("reached here 2");
		oledWriteCmd(SSD1306_DISPLAY_RESET); 
		System.out.println("reached here 3");
		oledWriteCmd(SSD1306_SET_DISPLAY_OFFSET);
		System.out.println("reached here 4");
		oledWriteCmd(SSD1306_DISPLAY_OFFSET_DEFAULT); 
		System.out.println("reached here 5");
		oledWriteCmd(SSD1306_START_LINE_DEFAULT); //check
		System.out.println("reached here 6");
		oledWriteCmd(SSD1306_SET_SEG_SCAN_REVERSE);
		System.out.println("reached here 7");
		oledWriteCmd(SSD1306_SET_COM_SCAN_REVERSE);
		System.out.println("reached here 8");
		oledWriteCmd(SSD1306_SET_COM_PINS); 
		
		oledWriteCmd(SSD1306_COM_PINS_DEFAULT);
		
		oledWriteCmd(SSD1306_SET_CONTRAST);
		
		String var1 = "ff";//255
		oledWriteCmd(var1);
		String var2 = "a4";//164
		oledWriteCmd(var2);
		String var3 = "a6";//166
		oledWriteCmd(var3);
		oledWriteCmd(SSD1306_SET_CLOCK_FREQUENCY);
		String var4 = "f2";//242
		oledWriteCmd(var4);
		oledWriteCmd(SSD1306_SET_CHARGE_PUMP);
		oledWriteCmd(SSD1306_CHARGE_PUMP_ENABLE);
		oledWriteCmd(SSD1306_SET_PRECHARGE);  
		oledWriteCmd(SSD1306_PRECHARGE_DEFAULT);
		oledWriteCmd(SSD1306_SET_VCOMH_DESELECT);
		oledWriteCmd(SSD1306_VCOMH_DESELECT_DEFAULT); 
		oledWriteCmd(SSD1306_DISPLAY_ON); 
		oledWriteCmd(SSD1306_DISPLAY_RESET);
		System.out.println("completed initialization");
	}
	
	public void oledClear() {
		if(flag == 1)//oled화면 깨짐 방지
			oledSetXY(1,1);
		else
			flag = 1;
//		oledSetXY(1,1);
		oledWriteCmd(SSD1306_DISPLAY_SLEEP);
		oledWriteCmd(SSD1306_ADDRESSING);
		oledWriteCmd(SSD1306_ADDRESSING_HORIZONTAL);
		for(int i=0;i<1024;i++)
			oledWriteData("00");
		oledWriteCmd(SSD1306_ADDRESSING);
		oledWriteCmd(SSD1306_ADDRESSING_PAGE);
		oledWriteCmd(SSD1306_DISPLAY_ON);
		oledWriteCmd(SSD1306_DISPLAY_RESET);
		System.out.println("oled clear");
	}

	public String inttohex(int data) {//int->hex변경
		String hex_data = Integer.toHexString(data);
		if(hex_data.length()==1) {
			return "0"+hex_data;
		}
		return hex_data;
	}
	
	public String intto4bit(String part) {
		if(part.equals("0"))
			return "0000";
		else if(part.equals("1"))
			return "0001";
		else if(part.equals("2"))
			return "0010";
		else if(part.equals("3"))
			return "0011";
		else if(part.equals("4"))
			return "0100";
		else if(part.equals("5"))
			return "0101";
		else if(part.equals("6"))
			return "0110";
		else if(part.equals("7"))
			return "0111";
		else if(part.equals("8"))
			return "1000";
		else if(part.equals("9"))
			return "1001";
		else if(part.equals("a"))
			return "1010";
		else if(part.equals("b"))
			return "1011";
		else if(part.equals("c"))
			return "1100";
		else if(part.equals("d"))
			return "1101";
		else if(part.equals("e"))
			return "1110";
		else //if(part.equals("f"))
			return "1111";
	}
	
	public void oledSetXY(int x, int y) {
		oledWriteCmd(SSD1306_SET_COLUMN);
		String xx = inttohex(x);
		String yy = inttohex(y);
		oledWriteCmd(xx);
		oledWriteCmd("7f");//127
		oledWriteCmd(SSD1306_SET_PAGE);
		oledWriteCmd(yy);
		oledWriteCmd("3f");//63
	}
	
	public void oledWriteCmd(String ledcmd) {
		DC.low();
		String part1 = ledcmd.substring(0,1);
		String part2 = ledcmd.substring(1,2);
		
		String finalpart1=intto4bit(part1);
		String finalpart2=intto4bit(part2);
		String finalpart = finalpart1+finalpart2;
		
		int countcmd = 0;
		while(countcmd<8) {
			int sdkcmd = Integer.parseInt(finalpart.substring(countcmd, countcmd+1));
			if(sdkcmd==1)
				SDK.high();
			else
				SDK.low();
			
			SCLK.high();
			try {
				TimeUnit.MICROSECONDS.sleep(0);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			
			SCLK.low();
			try {
				TimeUnit.MICROSECONDS.sleep(0);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			
			countcmd++;
		}
	}
	
	public void oledWriteData(String leddata) {
		DC.high();
		String part1=leddata.substring(0,1);
		String part2=leddata.substring(1,2);
		
		String finalpart1=intto4bit(part1);
		String finalpart2=intto4bit(part2);
		String finalpart = finalpart1+finalpart2;
		
		count = 0;
		while(count<8) {
			int sdkdata = Integer.parseInt(finalpart.substring(count,count+1));
			if(sdkdata==1) {
				SDK.high();
				led.high();
			}
			else
			{
				SDK.low();
				led.low();
			}
			
			SCLK.high();
			/*try {
				TimeUnit.MICROSECONDS.sleep(0);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}*/
			
			SCLK.low();
			/*try {
				TimeUnit.MICROSECONDS.sleep(0);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}*/
			
			count++;
		}
	}
	
	public void oledWriteChar(String string)
	{
	    switch(string)
	    {
		    case "안":
		    	oledWriteData("ee");
		    	oledWriteData("91");
		    	oledWriteData("8e");
		    	oledWriteData("9f");
		    	oledWriteData("84");
		    	break;

		    case "녕":
		    	oledWriteData("5f");
		    	oledWriteData("b0");
		    	oledWriteData("aa");
		    	oledWriteData("aa");
		    	oledWriteData("5f");
		    	break;

		    case "하":
		    	oledWriteData("7a");
		    	oledWriteData("87");
		    	oledWriteData("7a");
		    	oledWriteData("ff");
		    	oledWriteData("08");
		    	break;
		    

		    case "세":
		    	oledWriteData("20");
		    	oledWriteData("1f");
		    	oledWriteData("28");
		    	oledWriteData("ff");
		    	oledWriteData("ff");
		    	break;
		    

		    case "요":
		    	oledWriteData("82");
		    	oledWriteData("f5");
		    	oledWriteData("85");
		    	oledWriteData("f5");
		    	oledWriteData("82");
		    	break;
	    
		    case "방":
		    	oledWriteData("4f");
		    	oledWriteData("aa");
		    	oledWriteData("af");
		    	oledWriteData("af");
		    	oledWriteData("44");
		    	break;
		    	
		    case "문":
		    	oledWriteData("d7");
		    	oledWriteData("95");
		    	oledWriteData("b5");
		    	oledWriteData("95");
		    	oledWriteData("97");
		    	break;
		    	
		    case "객":
		    	oledWriteData("31");
		    	oledWriteData("2f");
		    	oledWriteData("3f");
		    	oledWriteData("24");
		    	oledWriteData("ff");
		    	break;
		    	
		    case "님":
		    	oledWriteData("ef");
		    	oledWriteData("a8");
		    	oledWriteData("a8");
		    	oledWriteData("a0");
		    	oledWriteData("ef");
		    	break;
			
			case "비":
				oledWriteData("ff");
				oledWriteData("88");
				oledWriteData("ff");
				oledWriteData("00");
				oledWriteData("ff");
				break;
				
			case "인":
				oledWriteData("e6");
				oledWriteData("89");
				oledWriteData("86");
				oledWriteData("80");
				oledWriteData("9f");
				break;
				
			case "가":
				oledWriteData("41");
				oledWriteData("3f");
				oledWriteData("00");
				oledWriteData("ff");
				oledWriteData("08");
				break;
				
			case "자":
				oledWriteData("41");
				oledWriteData("3f");
				oledWriteData("41");
				oledWriteData("ff");
				oledWriteData("08");
				break;

			case "입":
				oledWriteData("f6");
                oledWriteData("a9");
                oledWriteData("a6");
                oledWriteData("a0");
                oledWriteData("ff");
				break;

		    case "니":
                oledWriteData("7f");
                oledWriteData("40");
                oledWriteData("00");
                oledWriteData("ff");
                oledWriteData("00");
				break;

		    case "다":
                oledWriteData("7f");
                oledWriteData("41");
                oledWriteData("00");
                oledWriteData("ff");
                oledWriteData("08");
				break;

			case "히":
		    	oledWriteData("7a");
		    	oledWriteData("87");
		    	oledWriteData("7a");
		    	oledWriteData("ff");
		    	oledWriteData("00");
		    	break;

			case "이":
				oledWriteData("1e");
				oledWriteData("31");
				oledWriteData("1e");
				oledWriteData("ff");
				oledWriteData("00");
				break;

			case "미":
				oledWriteData("7f");
				oledWriteData("41");
				oledWriteData("7f");
				oledWriteData("ff");
				oledWriteData("00");
				break;

			case "확":
				oledWriteData("2a");
				oledWriteData("77");
				oledWriteData("6a");
				oledWriteData("df");
				oledWriteData("04");
				break;

			case "되":
				oledWriteData("5f");
				oledWriteData("71");
				oledWriteData("51");
				oledWriteData("40");
				oledWriteData("ff");
				break;

			case "었":
		    	oledWriteData("8e");
		    	oledWriteData("51");
		    	oledWriteData("8e");
		    	oledWriteData("44");
		    	oledWriteData("bf");
		    	break;

			case "습":
		    	oledWriteData("0c");
		    	oledWriteData("fa");
		    	oledWriteData("a9");
		    	oledWriteData("fa");
		    	oledWriteData("0c");
		    	break;

		    case "!":
		    	oledWriteData(inttohex(0));
		    	oledWriteData(inttohex(0));
		    	oledWriteData(inttohex(95));
		    	oledWriteData(inttohex(0));
		    	oledWriteData(inttohex(0));
		    	break;
		    	
	        case "#":
	        	oledWriteData(inttohex(20));
	            oledWriteData(inttohex(127));
	            oledWriteData(inttohex(20));
	            oledWriteData(inttohex(127));
	            oledWriteData(inttohex(20));
	            break;

	        case "$":
	        	oledWriteData(inttohex(36));
	        	oledWriteData(inttohex(42));
	        	oledWriteData(inttohex(127));
	        	oledWriteData(inttohex(42));
	        	oledWriteData(inttohex(18));
	        	break;

	         case "%":
	        	 oledWriteData(inttohex(35));
		         oledWriteData(inttohex(19));
		         oledWriteData(inttohex(8));
		         oledWriteData(inttohex(100));
		         oledWriteData(inttohex(98));
		         break;


	         case "&":
	        	 oledWriteData(inttohex(54));
	        	 oledWriteData(inttohex(73));
	        	 oledWriteData(inttohex(85));
	        	 oledWriteData(inttohex(34));
	        	 oledWriteData(inttohex(80)); 
	        	 break;
	         
	         case "(":
	        	 oledWriteData(inttohex(0));
	        	 oledWriteData(inttohex(28));
	        	 oledWriteData(inttohex(34));
	        	 oledWriteData(inttohex(65));
	        	 oledWriteData(inttohex(0));
	        	 break;
	     
	         case ")":
		          oledWriteData(inttohex(0));
		          oledWriteData(inttohex(65));
		          oledWriteData(inttohex(34));
		          oledWriteData(inttohex(28));
		          oledWriteData(inttohex(0));
		          break;
	       
	          case "*":
		          oledWriteData(inttohex(20));
		          oledWriteData(inttohex(8));
		          oledWriteData(inttohex(62));
		          oledWriteData(inttohex(8));
		          oledWriteData(inttohex(20));
		          break;
	    
	          case "+":
		          oledWriteData(inttohex(8));
		          oledWriteData(inttohex(8));
		          oledWriteData(inttohex(62));
		          oledWriteData(inttohex(8));
		          oledWriteData(inttohex(8));
		          break;

	          case ",":
		          oledWriteData(inttohex(0));
		          oledWriteData(inttohex(80));
		          oledWriteData(inttohex(48));
		          oledWriteData(inttohex(0));
		          oledWriteData(inttohex(0));
		          break;
	          
	          case "-":
		          oledWriteData(inttohex(8));
		          oledWriteData(inttohex(8));
		          oledWriteData(inttohex(8));
		          oledWriteData(inttohex(8));
		          oledWriteData(inttohex(8));
		          break;
		      
	          case ".":
		          oledWriteData(inttohex(0));
		          oledWriteData(inttohex(96));
		          oledWriteData(inttohex(96));
		          oledWriteData(inttohex(0));
		          oledWriteData(inttohex(0));
		          break;
	        
	          case "/":
		          oledWriteData(inttohex(32));
		          oledWriteData(inttohex(16));
		          oledWriteData(inttohex(8));
		          oledWriteData(inttohex(4));
		          oledWriteData(inttohex(2));
		          break;

	          case "0":
		          oledWriteData(inttohex(62));
		          oledWriteData(inttohex(81));
		          oledWriteData(inttohex(73));
		          oledWriteData(inttohex(69));
		          oledWriteData(inttohex(62));
		          break;
	          
	          case "1":
		          oledWriteData(inttohex(0));
		          oledWriteData(inttohex(66));
		          oledWriteData(inttohex(127));
		          oledWriteData(inttohex(64));
		          oledWriteData(inttohex(0));
		          break;

	          case "2":
		          oledWriteData("46");
		          oledWriteData("61");
		          oledWriteData("51");
		          oledWriteData("49");
		          oledWriteData("46");
		          break;
		          
	          case "3":
		          oledWriteData(inttohex(33));
		          oledWriteData(inttohex(65));
		          oledWriteData(inttohex(69));
		          oledWriteData(inttohex(75));
		          oledWriteData(inttohex(49));
		          break;
	       
	          case "4":
		          oledWriteData(inttohex(24));
		          oledWriteData(inttohex(20));
		          oledWriteData(inttohex(18));
		          oledWriteData(inttohex(127));
		          oledWriteData(inttohex(16));
		          break;

	          case "5":
		          oledWriteData(inttohex(39));
		          oledWriteData(inttohex(69));
		          oledWriteData(inttohex(69));
		          oledWriteData(inttohex(69));
		          oledWriteData(inttohex(57));
		          break;
		          
	          case "6":
		          oledWriteData(inttohex(60));
		          oledWriteData(inttohex(74));
		          oledWriteData(inttohex(73));
		          oledWriteData(inttohex(73));
		          oledWriteData(inttohex(48));
		          break;

	          case "7":
		          oledWriteData(inttohex(1));
		          oledWriteData(inttohex(113));
		          oledWriteData(inttohex(9));
		          oledWriteData(inttohex(5));
		          oledWriteData(inttohex(3));
		          break;

	          case "8":
		          oledWriteData(inttohex(54));
		          oledWriteData(inttohex(73));
		          oledWriteData(inttohex(73));
		          oledWriteData(inttohex(73));
		          oledWriteData(inttohex(54));
		          break;

	     
	          case "9":
		          oledWriteData(inttohex(6));
		          oledWriteData(inttohex(73));
		          oledWriteData(inttohex(73));
		          oledWriteData(inttohex(41));
		          oledWriteData(inttohex(30));
		          break;

	          case ":":
		          oledWriteData(inttohex(0));
		          oledWriteData(inttohex(54));
		          oledWriteData(inttohex(54));
		          oledWriteData(inttohex(0));
		          oledWriteData(inttohex(0));
		          break;


	          case ";":
		          oledWriteData(inttohex(0));
		          oledWriteData(inttohex(86));
		          oledWriteData(inttohex(54));
		          oledWriteData(inttohex(0));
		          oledWriteData(inttohex(0));
		          break;	
	    
	      
	          case "<":
		          oledWriteData(inttohex(8));
		          oledWriteData(inttohex(20));
		          oledWriteData(inttohex(34));
		          oledWriteData(inttohex(65));
		          oledWriteData(inttohex(0));
		          break;

	          case "=":
		          oledWriteData(inttohex(20));
		          oledWriteData(inttohex(20));
		          oledWriteData(inttohex(20));
		          oledWriteData(inttohex(20));
		          oledWriteData(inttohex(20));
		          break;
		          
	          case ">":
		          oledWriteData(inttohex(0));
		          oledWriteData(inttohex(65));
		          oledWriteData(inttohex(34));
		          oledWriteData(inttohex(20));
		          oledWriteData(inttohex(8));
		          break;
	        
	          case "?":
		          oledWriteData(inttohex(2));
		          oledWriteData(inttohex(1));
		          oledWriteData(inttohex(81));
		          oledWriteData(inttohex(9));
		          oledWriteData(inttohex(6));
		          break;

	          case "@":
		          oledWriteData(inttohex(50));
		          oledWriteData(inttohex(73));
		          oledWriteData(inttohex(121));
		          oledWriteData(inttohex(65));
		          oledWriteData(inttohex(62));
		          break;
		          
	          case "A":
		          oledWriteData(inttohex(126));
		          oledWriteData(inttohex(17));
		          oledWriteData(inttohex(17));
		          oledWriteData(inttohex(17));
		          oledWriteData(inttohex(126));
		          break;
	        
	          case "B":
		          oledWriteData(inttohex(127));
		          oledWriteData(inttohex(73));
		          oledWriteData(inttohex(73));
		          oledWriteData(inttohex(73));
		          oledWriteData(inttohex(54));
		          break;
		          
	          case "C":
		          oledWriteData(inttohex(62));
		          oledWriteData(inttohex(65));
		          oledWriteData(inttohex(65));
		          oledWriteData(inttohex(65));
		          oledWriteData(inttohex(34));
		         break;
		         
	          case "D":
		          oledWriteData(inttohex(127));
		          oledWriteData(inttohex(65));
		          oledWriteData(inttohex(65));
		          oledWriteData(inttohex(34));
		          oledWriteData(inttohex(28));
		         break;

	          case "E":
		          oledWriteData(inttohex(127));
		          oledWriteData(inttohex(73));
		          oledWriteData(inttohex(73));
		          oledWriteData(inttohex(73));
		          oledWriteData(inttohex(65));
		          break;

	          case "F":
		          oledWriteData(inttohex(127));
		          oledWriteData(inttohex(9));
		          oledWriteData(inttohex(9));
		          oledWriteData(inttohex(9));
		          oledWriteData(inttohex(1));
		          break;
		          
	          case "G":
		          oledWriteData(inttohex(62));
		          oledWriteData(inttohex(65));
		          oledWriteData(inttohex(73));
		          oledWriteData(inttohex(73));
		          oledWriteData(inttohex(122));
		          break;
	       
	          case "H":
		          oledWriteData(inttohex(127));
		          oledWriteData(inttohex(8));
		          oledWriteData(inttohex(8));
		          oledWriteData(inttohex(8));
		          oledWriteData(inttohex(127));
		          break;
	         
	          case "I":
		          oledWriteData(inttohex(0));
		          oledWriteData(inttohex(65));
		          oledWriteData(inttohex(127));
		          oledWriteData(inttohex(65));
		          oledWriteData(inttohex(0));
		          break;

	          case "J":
		          oledWriteData(inttohex(32));
		          oledWriteData(inttohex(64));
		          oledWriteData(inttohex(65));
		          oledWriteData(inttohex(63));
		          oledWriteData(inttohex(1));
	  	          break;
	  	          
	          case "K":	        
		          oledWriteData(inttohex(127));
		          oledWriteData(inttohex(8));
		          oledWriteData(inttohex(20));
		          oledWriteData(inttohex(34));
		          oledWriteData(inttohex(65));		        
		          break;

	          case "L":		        
		          oledWriteData(inttohex(127));
		          oledWriteData(inttohex(64));
		          oledWriteData(inttohex(64));
		          oledWriteData(inttohex(64));
		          oledWriteData(inttohex(64));		        
		          break;

		      case "M":
		          oledWriteData(inttohex(127));
		          oledWriteData(inttohex(2));
		          oledWriteData(inttohex(12));
		          oledWriteData(inttohex(2));
		          oledWriteData(inttohex(127));
		          break;

		      case "N":
		          oledWriteData(inttohex(127));
		          oledWriteData(inttohex(4));
		          oledWriteData(inttohex(8));
		          oledWriteData(inttohex(16));
		          oledWriteData(inttohex(127));
		          break;
	    
	          case "O":
		          oledWriteData(inttohex(62));
		          oledWriteData(inttohex(65));
		          oledWriteData(inttohex(65));
		          oledWriteData(inttohex(65));
		          oledWriteData(inttohex(62));
		          break;

	         case "P":
		          oledWriteData(inttohex(127));
		          oledWriteData(inttohex(9));
		          oledWriteData(inttohex(9));
		          oledWriteData(inttohex(9));
		          oledWriteData(inttohex(6));
		          break;

	         case "Q":
		          oledWriteData(inttohex(62));
		          oledWriteData(inttohex(65));
		          oledWriteData(inttohex(81));
		          oledWriteData(inttohex(33));
		          oledWriteData(inttohex(94));
		          break;
		          
	         case "R":
		          oledWriteData(inttohex(127));
		          oledWriteData(inttohex(9));
		          oledWriteData(inttohex(25));
		          oledWriteData(inttohex(41));
		          oledWriteData(inttohex(70));
		         break;

	         case "S":
		          oledWriteData(inttohex(70));
		          oledWriteData(inttohex(73));
		          oledWriteData(inttohex(73));
		          oledWriteData(inttohex(73));
		          oledWriteData(inttohex(49));
		          break;

	         case "T":
		          oledWriteData(inttohex(1));
		          oledWriteData(inttohex(1));
		          oledWriteData(inttohex(127));
		          oledWriteData(inttohex(1));
		          oledWriteData(inttohex(1));
		         break;

	          case "U":
		          oledWriteData(inttohex(63));
		          oledWriteData(inttohex(64));
		          oledWriteData(inttohex(64));
		          oledWriteData(inttohex(64));
		          oledWriteData(inttohex(63));
		         break;
	   
	          case "V":
		          oledWriteData(inttohex(31));
		          oledWriteData(inttohex(32));
		          oledWriteData(inttohex(64));
		          oledWriteData(inttohex(32));
		          oledWriteData(inttohex(31));
		         break;

	           case "W":
		          oledWriteData(inttohex(63));
		          oledWriteData(inttohex(64));
		          oledWriteData(inttohex(56));
		          oledWriteData(inttohex(64));
		          oledWriteData(inttohex(63));
		         break;

	          case "X":
		          oledWriteData(inttohex(99));
		          oledWriteData(inttohex(20));
		          oledWriteData(inttohex(8));
		          oledWriteData(inttohex(20));
		          oledWriteData(inttohex(99));
		          break;

	          case "Y":
		          oledWriteData(inttohex(7));
		          oledWriteData(inttohex(8));
		          oledWriteData(inttohex(112));
		          oledWriteData(inttohex(8));
		          oledWriteData(inttohex(7));
		          break;

	          case "Z":
		          oledWriteData(inttohex(97));
		          oledWriteData(inttohex(81));
		          oledWriteData(inttohex(73));
		          oledWriteData(inttohex(69));
		          oledWriteData(inttohex(67));
		          break;
		          
	          case "[":
		          oledWriteData(inttohex(0));
		          oledWriteData(inttohex(127));
		          oledWriteData(inttohex(65));
		          oledWriteData(inttohex(65));
		          oledWriteData(inttohex(0));
		          break;
	         
	          case "]":
		          oledWriteData(inttohex(0));
		          oledWriteData(inttohex(65));
		          oledWriteData(inttohex(65));
		          oledWriteData(inttohex(127));
		          oledWriteData(inttohex(0));
		          break;
	          
	          case "^":
		          oledWriteData(inttohex(4));
		          oledWriteData(inttohex(2));
		          oledWriteData(inttohex(1));
		          oledWriteData(inttohex(2));
		          oledWriteData(inttohex(4));
		          break;
	         
	          case "_":
		          oledWriteData(inttohex(64));
		          oledWriteData(inttohex(64));
		          oledWriteData(inttohex(64));
		          oledWriteData(inttohex(64));
		          oledWriteData(inttohex(64));
		          break;

	          case "`":
		          oledWriteData(inttohex(0));
		          oledWriteData(inttohex(1));
		          oledWriteData(inttohex(2));
		          oledWriteData(inttohex(4));
		          oledWriteData(inttohex(0));
		          break;
		          
	          case "a":
	        	  oledWriteData(inttohex(32)); //0x20
			      oledWriteData(inttohex(84)); //0x54 
			      oledWriteData(inttohex(84)); //0x54
			      oledWriteData(inttohex(84)); //0x54
			      oledWriteData(inttohex(120)); //0x78
		          break;
		          
	          case "b":
		          oledWriteData(inttohex(127));
		          oledWriteData(inttohex(72));
		          oledWriteData(inttohex(68));
		          oledWriteData(inttohex(68));
		          oledWriteData(inttohex(56));
		          break;

	          case "c":
		          oledWriteData(inttohex(56));
		          oledWriteData(inttohex(68));
		          oledWriteData(inttohex(68));
		          oledWriteData(inttohex(68));
		          oledWriteData(inttohex(32));
		          break;

	          case "d":
		          oledWriteData(inttohex(56));
		          oledWriteData(inttohex(68));
		          oledWriteData(inttohex(68));
		          oledWriteData(inttohex(72));
		          oledWriteData(inttohex(127));
		          break;
	         
	          case "e":
		          oledWriteData(inttohex(56));
		          oledWriteData(inttohex(84));
		          oledWriteData(inttohex(84));
		          oledWriteData(inttohex(84));
		          oledWriteData(inttohex(24));
		          break;
		          
	          case "f":
		          oledWriteData(inttohex(8));
		          oledWriteData(inttohex(126));
		          oledWriteData(inttohex(9));
		          oledWriteData(inttohex(1));
		          oledWriteData(inttohex(2));
		          break;

	          case "g":
		          oledWriteData(inttohex(12));
		          oledWriteData(inttohex(82));
		          oledWriteData(inttohex(82));
		          oledWriteData(inttohex(82));
		          oledWriteData(inttohex(62));
		          break;

	          case "h":
		          oledWriteData(inttohex(127));
		          oledWriteData(inttohex(8));
		          oledWriteData(inttohex(4));
		          oledWriteData(inttohex(4));
		          oledWriteData(inttohex(120));
		          break;

	          case "i":
		          oledWriteData(inttohex(0));
		          oledWriteData(inttohex(68));
		          oledWriteData(inttohex(125));
		          oledWriteData(inttohex(64));
		          oledWriteData(inttohex(0));
		          break;

	          case "j":
		          oledWriteData(inttohex(32));
		          oledWriteData(inttohex(64));
		          oledWriteData(inttohex(68));
		          oledWriteData(inttohex(61));
		          oledWriteData(inttohex(0));
		          break;

	          case "k":
		          oledWriteData(inttohex(127));
		          oledWriteData(inttohex(16));
		          oledWriteData(inttohex(40));
		          oledWriteData(inttohex(68));
		          oledWriteData(inttohex(0));
		          break;

	          case "l":
		          oledWriteData(inttohex(0));
		          oledWriteData(inttohex(65));
		          oledWriteData(inttohex(127));
		          oledWriteData(inttohex(64));
		          oledWriteData(inttohex(0));
		          break;


	          case "m":
		          oledWriteData(inttohex(124));
		          oledWriteData(inttohex(4));
		          oledWriteData(inttohex(24));
		          oledWriteData(inttohex(4));
		          oledWriteData(inttohex(120));
		          break;


	          case "n":
		          oledWriteData(inttohex(124));
		          oledWriteData(inttohex(8));
		          oledWriteData(inttohex(4));
		          oledWriteData(inttohex(4));
		          oledWriteData(inttohex(120));
		          break;

	          case "o":
		          oledWriteData(inttohex(56));
		          oledWriteData(inttohex(68));
		          oledWriteData(inttohex(68));
		          oledWriteData(inttohex(68));
		          oledWriteData(inttohex(56));
		          break;

	          case "p":
		          oledWriteData(inttohex(124));
		          oledWriteData(inttohex(20));
		          oledWriteData(inttohex(20));
		          oledWriteData(inttohex(20));
		          oledWriteData(inttohex(8));
		          break;

	          case "q":
		          oledWriteData(inttohex(8));
		          oledWriteData(inttohex(20));
		          oledWriteData(inttohex(20));
		          oledWriteData(inttohex(24));
		          oledWriteData(inttohex(124));
		          break;

	          case "r":
		          oledWriteData(inttohex(124));
		          oledWriteData(inttohex(8));
		          oledWriteData(inttohex(4));
		          oledWriteData(inttohex(4));
		          oledWriteData(inttohex(8));
		          break;

	          case "s":
		          oledWriteData(inttohex(72));
		          oledWriteData(inttohex(84));
		          oledWriteData(inttohex(84));
		          oledWriteData(inttohex(84));
		          oledWriteData(inttohex(32));
		          break;

	          case "t":
		          oledWriteData(inttohex(4));
		          oledWriteData(inttohex(63));
		          oledWriteData(inttohex(68));
		          oledWriteData(inttohex(64));
		          oledWriteData(inttohex(32));
		          break;

	          case "u":
		          oledWriteData(inttohex(60));
		          oledWriteData(inttohex(64));
		          oledWriteData(inttohex(64));
		          oledWriteData(inttohex(32));
		          oledWriteData(inttohex(124));
		          break;

	          case "v":
		          oledWriteData(inttohex(28));
		          oledWriteData(inttohex(32));
		          oledWriteData(inttohex(64));
		          oledWriteData(inttohex(32));
		          oledWriteData(inttohex(24));
		          break;

             case "w":
		          oledWriteData(inttohex(60));
		          oledWriteData(inttohex(64));
		          oledWriteData(inttohex(48));
		          oledWriteData(inttohex(64));
		          oledWriteData(inttohex(60));
		          break;
		          
	         case "x":
		          oledWriteData(inttohex(68));
		          oledWriteData(inttohex(40));
		          oledWriteData(inttohex(16));
		          oledWriteData(inttohex(40));
		          oledWriteData(inttohex(68));
		          break;
		          
	          case "y":
		          oledWriteData(inttohex(12));
		          oledWriteData(inttohex(80));
		          oledWriteData(inttohex(80));
		          oledWriteData(inttohex(80));
		          oledWriteData(inttohex(60));
		          break;

	          case "z":
		          oledWriteData(inttohex(68));
		          oledWriteData(inttohex(100));
		          oledWriteData(inttohex(84));
		          oledWriteData(inttohex(76));
		          oledWriteData(inttohex(68));
		          break;

	          case "{":
		          oledWriteData(inttohex(0));
		          oledWriteData(inttohex(8));
		          oledWriteData(inttohex(54));
		          oledWriteData(inttohex(65));
		          oledWriteData(inttohex(0));
		          break;

	          case "|":
		          oledWriteData(inttohex(0));
		          oledWriteData(inttohex(0));
		          oledWriteData(inttohex(127));
		          oledWriteData(inttohex(0));
		          oledWriteData(inttohex(0));
		          break;
	          
	          case "}":
		          oledWriteData(inttohex(0));
		          oledWriteData(inttohex(65));
		          oledWriteData(inttohex(54));
		          oledWriteData(inttohex(8));
		          oledWriteData(inttohex(0));
		          break;
	    }
	}



	public void oledWriteString(int x, int y, String characters){
		for(int k=0;k<characters.length();k++) {
			oledSetXY(x,y);
			oledWriteChar(characters.substring(k,k+1));
			x=x+7;
		}
	}
}
