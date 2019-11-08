#include <MsTimer2.h>



#include <ArduinoJson.h>
#include <Wire.h>
#include <SoftwareSerial.h>

#include <Adafruit_GFX_rus.h>
#include <Adafruit_PCD8544_rus.h>
#include <SPI.h>
#define mosi   9
#define clk   10
#define dc    11
#define cs    12
#define reset 13
#define timeout 10
#define batery  A1
#define MIN_VALTEGE 2.75
#define MAX_VALTEGE 4.25
#define BL 8
bool onDisplay = false;

static unsigned char PROGMEM const logo16_glcd_bmp[] =
{ B00000000, B11000000,
  B00000001, B11000000,
  B00000001, B11000000,
  B00000011, B11100000,
  B11110011, B11100000,
  B11111110, B11111000,
  B01111110, B11111111,
  B00110011, B10011111,
  B00011111, B11111100,
  B00001101, B01110000,
  B00011011, B10100000,
  B00111111, B11100000,
  B00111111, B11110000,
  B01111100, B11110000,
  B01110000, B01110000,
  B00000000, B00110000 };

Adafruit_PCD8544 display = Adafruit_PCD8544(clk, mosi, dc, cs, reset);
StaticJsonDocument<200> parser;


int16_t i;
void setup() {
  pinMode(3, INPUT_PULLUP);
  pinMode(BL, OUTPUT);
  digitalWrite(BL, HIGH);
  
  // put your setup code here, to run once:
  display.begin();
  display.setContrast(60);
  Serial.begin(9600);
  display.clearDisplay();
  display.display();
  delay(500);
  attachInterrupt(1, wakeUp, FALLING);
  waitingConnection();
  analogReference(INTERNAL);
  onDisplay = true;
  MsTimer2::set(10000, wakeUpTimer);
  MsTimer2::start();
}
void loop() {
  // put your main code here, to run repeatedly:
  if (!onDisplay){
      digitalWrite(BL, LOW);
      display.clearDisplay();
      display.display();
    }
  
  while(Serial.available() > 0){
    Serial.println(onDisplay);
    String str = Serial.readStringUntil('\n');

    if(str == "goodbye"){
      waitingConnection();
    }
    
    DeserializationError error = deserializeJson(parser, str);//смотрим есть ли ошибки, заодно и парсим json
    //после, можно будет использовать как массив
    if(error){
      return;
    }

    String name = parser["name"];

    if(name == "time" && onDisplay){
      digitalWrite(BL, HIGH);
      String time = parser["time"];
      String date = parser["date"];
      drawTimeAndDate(time, date);
    }
    else if(name == "notify"){
      
      String text = parser["text"];
      if(text == "null"){
        break;
      }else{
      digitalWrite(BL, HIGH);
      String appName = parser["applicationName"];
      
      drawNotify(appName, text);
      MsTimer2::start();
      }
    }
  }
}

void drawTimeAndDate(String t, String d){
  display.clearDisplay();
  display.setTextSize(1); 
  display.setTextColor(BLACK);
  display.setCursor(0, 20);
  display.println("  " + d);
  display.setCursor(0, 30);
  display.println("     " + t);
  
  if(analogRead(batery)>0){
    display.setCursor(45, 0);
    String p = String(chargeLevel());
    display.println(p + '%');
  }

  display.display();
}

void waitingConnection(){
  while(!(Serial.available() > 0)){
    
    display.clearDisplay();
    display.setTextSize(1); 
    display.setTextColor(BLACK);
    display.setCursor(0, 10);
    display.println("Жду соединения");
    display.display();
    
    for(i = 0; i <= 10; i++){
      display.drawLine(display.width() / 2 - 5, 21, i + (display.width() / 2 - 5), 21, BLACK);
      display.display();
      delay(timeout);
      }

    for(i = 0; i <= 10; i++){
      display.drawLine(display.width() / 2 + 5, 21, display.width() / 2 + 5, 21 + i, BLACK);
      display.display();
      delay(timeout);
      }

      for(i = 0; i <= 10; i++){
      display.drawLine(display.width() / 2 + 5, 31, (display.width() / 2 + 5) - i, 31, BLACK);
      display.display();
      delay(timeout);
      }

      for(i = 0; i <= 10; i++){
      display.drawLine(display.width() / 2 - 5, 31, display.width() / 2 - 5, 31 - i, BLACK);
      display.display();
      delay(timeout);
      }
      
    }
}

void drawNotify(String appname, String text){
  display.clearDisplay();
  display.setTextSize(1); 
  display.setTextColor(BLACK);
  display.setCursor(0, 0);
  display.println(appname);
  text.replace(": ", "\n");
  display.println(text);
  display.display();
  delay(10000);
} 
float chargeLevel(){
  
  float Vbat = (analogRead(1) * 1.1) / 1023;
  float del = 0.24935732647814910025706940874036; // R2/(R1+R2)  0.99кОм / (9.88кОм + 0.99кОм)
  float Vin = Vbat / del;
  // уровень заряда в процентах
  float proc = ((Vin - MIN_VALTEGE) / (MAX_VALTEGE - MIN_VALTEGE)) * 100;
  return proc;

}
void wakeUp(){
  onDisplay = !onDisplay;
  MsTimer2::start();
}
void wakeUpTimer(){
  onDisplay = false;
  MsTimer2::stop();
}
