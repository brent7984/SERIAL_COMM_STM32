//12 February 2019
//Brenton Chetty
//This code, uses the DAC to generate voltage, it also reads in the ADC values
//Both DAC and ADC values are outputted via USART, 9600bps
//UART interrupt is also used to adjust Rate (values/sec) in real time

//Code Trace
//This Program sends a String via UART "Enter Max V?"
//The UART is polled, until it receives a valid voltage..
//..example 2.5V
//The String is then converted to float, NB. no error checks are performed in this code
//The UART is polled again, until it receives a valid Interval Increment voltage..
//..example 0.5V
//The UART is polled again, until it receives a valid Plt Point Interval..
//..example 0.5s

//The UART interrupt is then activated..
//.. The Plot point interval changes here..
//.. for e.g. if "+0.5s" is received, then the time between generating DAC values..
//.. is decreased by 0.5s
//.. if "-0.5s" is received, then the time between generating DAC values..
//.. is increased by 0.5s

#include "mbed.h"
Serial pc(SERIAL_TX, SERIAL_RX, 9600);
AnalogIn in(A0);

#if !DEVICE_ANALOGOUT
#error You cannot use this example as the AnalogOut is not supported on this device.
#else
AnalogOut out(PA_4);
#endif

DigitalOut led(LED2);
const int buffer_size = 255;
char rx_buffer[buffer_size+1];
int rx_in=0;
int rx_out=0;
float Interval=0;
char Interval_Delta[5]="";
int index=0;
char c=0;
// Interupt Routine to read in data from serial port
void Rx_interrupt() {
// Loop just in case more than one character is in UART's receive FIFO buffer
// Stop if buffer full
    while ((pc.readable()) && (((rx_in + 1) % buffer_size) != rx_out)) {
        rx_buffer[rx_in] = pc.getc();
        if(rx_buffer[rx_in]=='-')
        {
            while(c!='s' && c!='S') // s or S is Stop bit
            {
                c=pc.getc();
                if(c!='s' && c!='S')
            Interval_Delta[index++]=c;  //A Rx Buffer, to be converted to float
            else
            Interval+=atof(Interval_Delta);
            }
  
        }
          if(rx_buffer[rx_in]=='+')
        {
            while(c!='s' && c!='S') // s or S is Stop bit
            {
                c=pc.getc();
                if(c!='s' && c!='S')
            Interval_Delta[index++]=c;
            else
            Interval-=atof(Interval_Delta);
            }
  
        }
        rx_in = (rx_in + 1) % buffer_size;  //prevents overflow
    }

  //  reset values for next interrut occurence
    rx_in=0;
    index=0;
    c=0;
    Interval_Delta[0]=0;
    rx_buffer[0]=0;
    return;
}
 
int main()
{
    char c=0;
    char MaxV[6]={0};
    char Increment_V[6]={0};
    char Point_Interval[6]={0};

    int i=0;
    float num;
    float increment=0.1;    //default value, not really used
    pc.printf("Enter Max Voltage(V)? (e.g. 3.3V)\n");
    while(c!='v' && c!='V')
    {
        if(c!=0)
        {
            MaxV[i++]=c;
        }
            c=pc.getc();
    }

    num = atof(MaxV);   //convert string to float
    i=0;
    c=0;
    pc.printf("Enter Increment MaxV(V)? (e.g. 0.1V)\n");
    /*poll until received valid voltage*/
    while(c!='v' && c!='V')     //v is stop bit
    {
        if(c!=0)
        {
            Increment_V[i++]=c;
        }
                c=pc.getc();
    }

    increment = (atof(Increment_V))/3.3;    //converts voltage to Mbed required Ratio ("0-1"...<=>..."0-3.3V")
    i=0;
    c=0;
    pc.printf("Enter Plot Point Interval(s)? (e.g. 0.5s)\n");
    while(c!='s' && c!='S')
    {
        if(c!=0)
        {
            Point_Interval[i++]=c;
        }
                c=pc.getc();
    }

    Interval = atof(Point_Interval);
  
  // Setup a serial interrupt function to receive data
    pc.attach(&Rx_interrupt, Serial::RxIrq);
  
   // printf("*** Connect A0 and A2 pins together ***\n");
    float out_value;    //DAC output
    float in_value;     //ADC input
   
    while(1) 
    {  
        for (out_value = 0.0f; out_value <= (num/3.3); out_value += increment) 
        {
            out.write(out_value);
            wait(0.1);
            in_value = in.read();
            //Send DAC and ADC values as String, with 3 decimal places
            printf("%.3f,%.3f\r\n",out_value*3.3,in_value*3.3);
            //Interval Delay was split in 2 halves, to ensure Stop bit is sent and processed without error 
            wait(Interval/2);
            printf("X\r\n");
            wait(Interval/2);
            led = !led;     //toggle LED
        }
    }
}


