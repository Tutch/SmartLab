#include "DHT.h"
#include <IRremote.h>

IRsend irsend;

#define DHTTYPE DHT11
#define DHTPIN 6
#define echoPin 2
#define trigPin 3

DHT dht(DHTPIN, DHTTYPE);
int LDR_Pin = A0; //analog pin 0

// codigo do ar Carrier dos laboratorios
unsigned int irSignal[] = {4236, 4488, 396, 1764, 396, 648, 416, 1760, 396, 1764, 392, 652, 416, 648, 416, 1740, 416, 652, 416, 648, 416, 1760, 396, 648, 416, 652, 412, 1744, 412, 1764, 396, 648, 416, 1764, 392, 652, 416, 1760, 396, 1764, 392, 1764, 392, 1768, 392, 648, 416, 1760, 396, 1764, 392, 1760, 396, 652, 416, 648, 420, 648, 416, 648, 416, 1764, 392, 652, 412, 652, 416, 1740, 416, 1764, 392, 1764, 392, 652, 420, 644, 420, 648, 416, 648, 416, 652, 416, 648, 416, 648, 420, 648, 416, 1760, 396, 1764, 396, 1760, 396, 1736, 420, 1760, 396, 5296, 4244, 4484, 396, 1764, 396, 648, 416, 1764, 392, 1764, 392, 652, 416, 648, 416, 1760, 396, 648, 416, 652, 416, 1760, 396, 648, 416, 652, 416, 1760, 396, 1760, 396, 648, 416, 1764, 396, 648, 416, 1760, 396, 1760, 396, 1764, 392, 1764, 396, 648, 416, 1760, 396, 1740, 416, 1764, 392, 652, 416, 652, 412, 652, 416, 648, 416, 1760, 396, 652, 416, 648, 416, 1760, 396, 1764, 392, 1764, 392, 652, 416, 652, 412, 652, 416, 648, 416, 648, 420, 648, 416, 652, 412, 652, 412, 1764, 396, 1760, 396, 1740, 416, 1764, 396, 1760, 396, 5712, 72};
int khz = 38; // 38kHz carrier frequency for the NEC protocol

String messageReceived;
const String TURN_OFF = "shutdown";

boolean waitForContent = true;
int loopCount = 0;


void setup() {
  Serial.begin(9600);
  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);
  dht.begin();
}

void loop() {
  long duration, distance;
  String presence = "false";
   
  int LDRReading = analogRead(LDR_Pin); 
  float t = dht.readTemperature();

  if (isnan(t)) {
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
    presence = "true";
  }

  Serial.print("Temperatura=");
  Serial.print(t);
  Serial.print(";");
  Serial.print("Luz=");
  Serial.print(LDRReading);
  Serial.print(";");
  Serial.print("Pres=");
  Serial.print(presence);
  Serial.print(";");
  Serial.print("Proximidade=");
  Serial.print(distance);
  Serial.println("");

  while(waitForContent){
          Serial.println(loopCount);

    // send data only when you receive data:
    if (Serial.available() > 0) {
      // read the incoming byte:
      messageReceived = Serial.read();
    
      if(messageReceived.equals(TURN_OFF)){
        for (int i = 0; i < 3; i++) {
          irsend.sendRaw(irSignal, sizeof(irSignal) / sizeof(irSignal[0]), khz); //Note the approach used to automatically calculate the size of the array.
        }
      }
    }

    delay(1000);
    loopCount++;

    if(loopCount == 10){
      waitForContent = false;
    }
  }

  waitForContent = true;
  loopCount = 0;

}
