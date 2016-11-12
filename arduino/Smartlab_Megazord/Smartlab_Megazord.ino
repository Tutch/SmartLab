#include "DHT.h"
#include <IRremote.h>

IRsend irsend;

#define DHTTYPE DHT11
#define DHTPIN 6
#define echoPin 2
#define trigPin 3
#define TIME_TO_READ_SENSORS 30
#define READ_SENSORS_DELAY 5000

DHT dht(DHTPIN, DHTTYPE);
int LDR_Pin = A0; //analog pin 0

// RAW code for AC shutdown
unsigned int irSignal[] = {4236, 4488, 396, 1764, 396, 648, 416, 1760, 396, 1764, 392, 652, 416, 648, 416, 1740, 416, 652, 416, 648, 416, 1760, 396, 648, 416, 652, 412, 1744, 412, 1764, 396, 648, 416, 1764, 392, 652, 416, 1760, 396, 1764, 392, 1764, 392, 1768, 392, 648, 416, 1760, 396, 1764, 392, 1760, 396, 652, 416, 648, 420, 648, 416, 648, 416, 1764, 392, 652, 412, 652, 416, 1740, 416, 1764, 392, 1764, 392, 652, 420, 644, 420, 648, 416, 648, 416, 652, 416, 648, 416, 648, 420, 648, 416, 1760, 396, 1764, 396, 1760, 396, 1736, 420, 1760, 396, 5296, 4244, 4484, 396, 1764, 396, 648, 416, 1764, 392, 1764, 392, 652, 416, 648, 416, 1760, 396, 648, 416, 652, 416, 1760, 396, 648, 416, 652, 416, 1760, 396, 1760, 396, 648, 416, 1764, 396, 648, 416, 1760, 396, 1760, 396, 1764, 392, 1764, 396, 648, 416, 1760, 396, 1740, 416, 1764, 392, 652, 416, 652, 412, 652, 416, 648, 416, 1760, 396, 652, 416, 648, 416, 1760, 396, 1764, 392, 1764, 392, 652, 416, 652, 412, 652, 416, 648, 416, 648, 420, 648, 416, 652, 412, 652, 412, 1764, 396, 1760, 396, 1740, 416, 1764, 396, 1760, 396, 5712, 72};
int khz = 38; // 38kHz carrier frequency for the NEC protocol

String messageReceived;
const String TURN_OFF = "shutdown";

unsigned long sendStart;
unsigned long loopTime;

unsigned int timesRead = 0;

// Sensor data
long duration, distance;
int LDRReading;
float temperature;
bool presence = false;

// Mediam from readings
long mediamDistance;
long mediamLDR;
float mediamTemperature;
String mediamPresence = "false";


void setup() {
  Serial.begin(9600);
  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);
  dht.begin();  
}

void loop() {

  // Read data in cicles before transmitting content
  readSensors();

  mediamDistance += distance;
  mediamLDR += LDRReading;
  mediamTemperature += temperature;

  // If a single presence check is true, the resulting presence is true.
  if(presence == true){
    mediamPresence = "true";
  }
  
  timesRead++;

  // Calculate mediam and send data
  if(timesRead >= ((TIME_TO_READ_SENSORS/(READ_SENSORS_DELAY/1000))+1 )){
    mediamDistance /= timesRead;
    mediamLDR /= timesRead;
    mediamTemperature /= timesRead;
    
    writeSensorsToSerial();

    timesRead = 0;
    mediamDistance = 0;
    mediamLDR = 0;
    mediamTemperature = 0;
    presence = false;
  }

  sendStart = millis();

  while(loopTime < READ_SENSORS_DELAY){
     // Checks serial data to shutdown the AC    
    if (Serial.available() > 0){
      String message = Serial.readString();
      turnOffAC(message); 
    }

    loopTime = millis() - sendStart;
  }

  loopTime = 0; 
}

// Read sensors connected to the arduino board.
void readSensors(){
  LDRReading = analogRead(LDR_Pin); 
  temperature = dht.readTemperature();
  
  if (isnan(temperature)) {
    Serial.println("Failed to read from DHT sensor!");
    return;
  }
  
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);
  duration = pulseIn(echoPin, HIGH);
  distance = (duration/2) / 29.1;
  
  if(distance < 160){
    presence = true;
  }
}

// Write the mediam of data measured by sensors to the serial port.
void writeSensorsToSerial(){
  Serial.print("Temperatura=");
  Serial.print(mediamTemperature);
  Serial.print(";");
  Serial.print("Luz=");
  Serial.print(mediamLDR);
  Serial.print(";");
  Serial.print("Pres=");
  Serial.print(mediamPresence);
  Serial.print(";");
  Serial.print("Proximidade=");
  Serial.print(mediamDistance);
  Serial.println("");
}

void turnOffAC(String messageReceived){
  Serial.println("AC_SHUTDOWN");
  
  // read the incoming byte:
  if(messageReceived.equals(TURN_OFF)){
    for(int j = 0; j < 3; j++){    
        for (int i = 0; i < 3; i++) {
          irsend.sendRaw(irSignal, sizeof(irSignal) / sizeof(irSignal[0]), khz); //Note the approach used to automatically calculate the size of the array.
        }
      delay(100);
    }
  }

  delay(300);
}
