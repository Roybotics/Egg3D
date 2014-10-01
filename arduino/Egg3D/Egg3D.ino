
// Egg3D sketch
// royerloic@gmail.com
//
// Based on the MPU6050 demo sketch class using DMP (MotionApps v2.0) by Jeff Rowberg <jeff@rowberg.net>
// Source: https://github.com/jrowberg/i2cdevlib


#include "Wire.h"
#include "I2Cdev.h"

#include "MPU6050_6Axis_MotionApps20.h"

MPU6050 mpu(0x68);



#define  pinLed 13
#define  pinIrGnd  11
#define  pinIrCtrl  10
#define  pinIrVcc  9
#define  pinIr0  11
#define  pinIr1  10
#define  pinIr2  9

bool blinkState = false;

// MPU control/status vars
bool dmpReady = false;  // set true if DMP init was successful
uint8_t mpuIntStatus;   // holds actual interrupt status byte from MPU
uint8_t devStatus;      // return status after each device operation (0 = success, !0 = error)
uint16_t packetSize;    // expected DMP packet size (default is 42 bytes)
uint16_t fifoCount;     // count of all bytes currently in FIFO
uint8_t fifoBuffer[64]; // FIFO storage buffer

// orientation/motion vars
Quaternion q;           // [w, x, y, z]         quaternion container
VectorInt16 aa;         // [x, y, z]            accel sensor measurements
VectorInt16 aaReal;     // [x, y, z]            gravity-free accel sensor measurements
VectorInt16 aaWorld;    // [x, y, z]            world-frame accel sensor measurements
VectorFloat gravity;    // [x, y, z]            gravity vector
float euler[3];         // [psi, theta, phi]    Euler angle container
float ypr[3];           // [yaw, pitch, roll]   yaw/pitch/roll container and gravity vector

// IR sensor values:

byte a0,a1,a2;


// packet structure for sending over serial (bluetooth)
#define serialMessageLength 19
uint8_t serialPacket[serialMessageLength] = { 
  0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,  '\n' };


// ================================================================
// ===               INTERRUPT DETECTION ROUTINE                ===
// ================================================================

volatile bool mpuInterrupt = false;     // indicates whether MPU interrupt pin has gone high
void dmpDataReady() 
{
  mpuInterrupt = true;
}


// ================================================================
// ===                      INITIAL SETUP                       ===
// ================================================================

void setup() 
{
  // join I2C bus (I2Cdev library doesn't do this automatically)
  Wire.begin();

  Serial.begin(115200);
  while (!Serial);

  // initialize device
  Serial.println(F("Initializing I2C devices..."));
  mpu.initialize();

  // verify connection
  Serial.println(F("Testing device connections..."));
  Serial.println(mpu.testConnection() ? F("MPU6050 connection successful") : F("MPU6050 connection failed"));

  // load and configure the DMP
  Serial.println(F("Initializing DMP..."));
  devStatus = mpu.dmpInitialize();

  //mpu.setRate(19);
  //Serial.print("getRate=");
  //Serial.println(mpu.getRate());

  //mpu.setDLPFMode(3);
  //Serial.print("getDLPFMode=");
  //Serial.println(mpu.getDLPFMode());


  // make sure it worked (returns 0 if so)
  if (devStatus == 0) 
  {
    // turn on the DMP, now that it's ready
    Serial.println(F("Enabling DMP..."));
    mpu.setDMPEnabled(true);

    // enable Arduino interrupt detection
    Serial.println(F("Enabling interrupt detection (Arduino external interrupt 0)..."));
    attachInterrupt(0, dmpDataReady, RISING);
    mpuIntStatus = mpu.getIntStatus();

    // set our DMP Ready flag so the main loop() function knows it's okay to use it
    Serial.println(F("DMP ready! Waiting for first interrupt..."));
    dmpReady = true;

    // get expected DMP packet size for later comparison
    packetSize = mpu.dmpGetFIFOPacketSize();
  } 
  else 
  {
    // ERROR!
    // 1 = initial memory load failed
    // 2 = DMP configuration updates failed
    // (if it's going to break, usually the code will be 1)
    Serial.print(F("DMP Initialization failed (code "));
    Serial.print(devStatus);
    Serial.println(F(")"));
  }

  // configure LED for output
  pinMode(pinLed, OUTPUT);
  
  // Configure IR pins:
  pinMode(pinIrGnd, OUTPUT);
  digitalWrite(pinIrGnd,LOW);
  pinMode(pinIrCtrl, OUTPUT);
  digitalWrite(pinIrCtrl,LOW);
  pinMode(pinIrVcc, OUTPUT);
  digitalWrite(pinIrVcc,HIGH);
}



// ================================================================
// ===                    MAIN PROGRAM LOOP                     ===
// ================================================================

void loop() 
{
  
  
  // if programming failed, don't try to do anything
  if (!dmpReady) return;

  // wait for MPU interrupt or extra packet(s) available
  while (!mpuInterrupt && fifoCount < packetSize) 
  {
    // other code
    
    digitalWrite(pinIrCtrl,LOW);
    int a0amb = analogRead(A0);
    int a1amb = analogRead(A1);
    int a2amb = analogRead(A2);
    
    digitalWrite(pinIrCtrl,HIGH);
    a0 = constrain(a0amb- analogRead(A0),0,255);
    a1 = constrain(a1amb- analogRead(A1),0,255);
    a2 = constrain(a2amb- analogRead(A2),0,255);
    
    

  }

  // reset interrupt flag and get INT_STATUS byte
  mpuInterrupt = false;
  mpuIntStatus = mpu.getIntStatus();

  // get current FIFO count
  fifoCount = mpu.getFIFOCount();

  // check for overflow (this should never happen unless our code is too inefficient)
  if ((mpuIntStatus & 0x10) || fifoCount == 1024) 
  {
    digitalWrite(pinLed, HIGH);
    
    // reset so we can continue cleanly
    mpu.resetFIFO();
    Serial.println(F("FIFO overflow!"));
    
    // otherwise, check for DMP data ready interrupt (this should happen frequently)
  } 
  else if (mpuIntStatus & 0x02) 
  {
    digitalWrite(pinLed, LOW);
    
    // wait for correct available data length, should be a VERY short wait
    while (fifoCount < packetSize) fifoCount = mpu.getFIFOCount();

    // read a packet from FIFO
    mpu.getFIFOBytes(fifoBuffer, packetSize);

    // track FIFO count here in case there is > 1 packet available
    // (this lets us immediately read more without waiting for an interrupt)
    fifoCount -= packetSize;

    /*
    mpu.dmpGetQuaternion(&q, fifoBuffer);
    Serial.print(q.w,4);
    Serial.print("\t");
    Serial.print(q.x,4);
    Serial.print("\t");
    Serial.print(q.y,4);
    Serial.print("\t");
    Serial.println(q.z,4);
    /**/
    
    mpu.dmpGetQuaternion(&q, fifoBuffer);
    mpu.dmpGetAccel(&aa, fifoBuffer);
    mpu.dmpGetGravity(&gravity, &q);
    mpu.dmpGetLinearAccel(&aaReal, &aa, &gravity);
    mpu.dmpGetLinearAccelInWorld(&aaWorld, &aaReal, &q);

    /* ================================================================================================ *
     | Default MotionApps v2.0 42-byte FIFO packet structure:                                           |
     |                                                                                                  |
     | [QUAT W][      ][QUAT X][      ][QUAT Y][      ][QUAT Z][      ][GYRO X][      ][GYRO Y][      ] |
     |   0   1   2   3   4   5   6   7   8   9  10  11  12  13  14  15  16  17  18  19  20  21  22  23  |
     |                                                                                                  |
     | [GYRO Z][      ][ACC X ][      ][ACC Y ][      ][ACC Z ][      ][      ]                         |
     |  24  25  26  27  28  29  30  31  32  33  34  35  36  37  38  39  40  41                          |
     * ================================================================================================ */

    serialPacket[0]++;
    serialPacket[1]  = fifoBuffer[0];  //Quat W
    serialPacket[2]  = fifoBuffer[1];  //Quat W
    serialPacket[3]  = fifoBuffer[4];  //Quat X
    serialPacket[4]  = fifoBuffer[5];  //Quat X
    serialPacket[5]  = fifoBuffer[8];  //Quat Y
    serialPacket[6]  = fifoBuffer[9];  //Quat Y
    serialPacket[7]  = fifoBuffer[12]; //Quat Z
    serialPacket[8]  = fifoBuffer[13]; //Quat Z

    serialPacket[9]  = highByte(aaWorld.x); //Acc X //fifoBuffer[28];
    serialPacket[10] = lowByte(aaWorld.x);  //Acc X //fifoBuffer[29];
    serialPacket[11] = highByte(aaWorld.y); //Acc Y //fifoBuffer[32];
    serialPacket[12] = lowByte(aaWorld.y);  //Acc Y //fifoBuffer[33];
    serialPacket[13] = highByte(aaWorld.z); //Acc Z //fifoBuffer[36];
    serialPacket[14] = lowByte(aaWorld.z);  //Acc Z //fifoBuffer[37];/**/
    
    serialPacket[15] = a0;
    serialPacket[16] = a1;
    serialPacket[17] = a2;
    
    Serial.write(serialPacket, serialMessageLength);/**/

    
  }
}

