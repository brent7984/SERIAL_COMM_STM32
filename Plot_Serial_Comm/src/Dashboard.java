import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JOptionPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import com.fazecast.jSerialComm.*;
import java.io.*;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import org.jfree.chart.renderer.xy.XYItemRenderer;
public class Dashboard extends javax.swing.JFrame {
    
private static final int MAX = 100;
static int COMM_PORT;
static int series_index=0;
private static final int baud = 9600;
static  DefaultXYDataset dataset ;
static JFreeChart chart;
static ValueMarker marker;
static XYPlot plot;
static SerialPort comPort = SerialPort.getCommPorts()[COMM_PORT];  
static InputStream in;
static OutputStream out;
static OutputStreamWriter osw;
float MaxV=0;
float Increment=0;
  float Rate;
  int index=0;
  static int MaxPoints=0;
    public Dashboard() {//Runs at start-up
        initComponents();

        setTitle("Mbed Platform - Version 1");

        this.setLocationRelativeTo(null);
}
 
   public Dashboard(String title) {
        super(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dataset = new DefaultXYDataset();
        dataset.addSeries("Series0", createSeries());
        chart = createChart(dataset);
        ChartPanel chartPanel = new ChartPanel(chart, false);
        chartPanel.setPreferredSize(new Dimension(640, 480));
        this.add(chartPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JTextField  textField= new JTextField();
        JLabel Rate_label = new JLabel("Rate = "+String.format ("%.3f", 1/Rate)+" point/s");
        JLabel Delta_label = new JLabel("Delta(s) = ");

        
             JButton clrButton = new JButton("Clear Graph");
        buttonPanel.add(clrButton);
        clrButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            
                       for(int i=0;i<=MaxPoints;i++)
                       {
            dataset.removeSeries("Series" + i);
                       }
                       series_index=0;
        }
        });
        
        JButton remButton = new JButton("Remove Last Point");
        buttonPanel.add(remButton);
        remButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            int n = dataset.getSeriesCount() - 1;
            dataset.removeSeries("Series" + series_index);
            if(series_index>0)
            series_index--;
        }
        });
        this.add(buttonPanel, BorderLayout.SOUTH);
        
        JButton speedupButton = new JButton("Increase Speed");
        buttonPanel.add(speedupButton);
        speedupButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {        
        try {
            float DeltaRate;
            int valid=0;
            try
            {
                DeltaRate = Float.parseFloat(textField.getText());
                if(DeltaRate<=0)
                {
                    JOptionPane.showMessageDialog(null,"Error - Check TextBox");
                    return;
                }
                if(Rate-DeltaRate>0.29999)
                {
                    Rate-=DeltaRate;
                    Rate_label.setText("Rate = "+String.format ("%.3f", 1/Rate)+" point/s");
                    valid=1;
                }
            }catch(Exception exc)
            {
                JOptionPane.showMessageDialog(null,"Error - Check TextBox");
                return;
            }
       if(valid==1){
            out = comPort.getOutputStream();
            osw = new OutputStreamWriter(out);
            String SendData="+"+String.format ("%.3f", DeltaRate)+'s';         
            osw.write(SendData, 0, SendData.length());
            osw.flush();
        }
            }catch(Exception ex){
               System.out.println(e);
            }
            }
        });
        this.add(buttonPanel, BorderLayout.SOUTH);
        
        JButton speedDownButton = new JButton("Decrease Speed");
        buttonPanel.add(speedDownButton);
        speedDownButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                try {
                    
                    float DeltaRate;
                    try
                        {
                            DeltaRate = Float.parseFloat(textField.getText());
                            if(DeltaRate<=0)
                            {
                                JOptionPane.showMessageDialog(null,"Error - Check TextBox");
                            return;
                            }
                            Rate+=DeltaRate;
                            Rate_label.setText("Rate = "+String.format ("%.3f", 1/Rate)+" point/s");
                        }catch(Exception exc)
                        {
                            JOptionPane.showMessageDialog(null,"Error - Check TextBox");
                            return;
                        }
             out = comPort.getOutputStream();
            osw = new OutputStreamWriter(out);
   String SendData="-"+String.format ("%.3f", DeltaRate)+'s';         
            osw.write(SendData, 0, SendData.length());
    osw.flush();

                }catch(Exception ex){
                   System.out.println("Error1");
                }

            }
        });
        this.add(buttonPanel, BorderLayout.SOUTH);
    buttonPanel.add(Delta_label);
buttonPanel.add(textField);
textField.setText("0.100");
buttonPanel.add(Rate_label);     
    }

   private int ErrorCheck()
   {
       int error=1;
       while(error==1)
            {
                try{
                 MaxV= Float.parseFloat(JOptionPane.showInputDialog (null, "Enter Max Voltage(V):"));
                error=0;
                if(MaxV<=0)
                {
                    JOptionPane.showMessageDialog(null,"Input Error - Max V '<=0' !");
                    error=1;
                    continue;
                }
                }catch(Exception e){
                JOptionPane.showMessageDialog(null,"Input Error - Example: 2.5");
                error=1;
                }
            }
       error=1;
       while(error==1)
       {
            try{
                Increment= Float.parseFloat(JOptionPane.showInputDialog (null, "Enter Increment Voltage(V):"));
                error=0;
                    if(Increment<=0)
                {
                    JOptionPane.showMessageDialog(null,"Input Error - Increment V '<=0' !");
                    error=1;
                    continue;
                }
            }catch(Exception e){
                JOptionPane.showMessageDialog(null,"Input Error - Example: 0.5");
                error=1;
            }
         }
                if(Increment>MaxV)
                {
                    JOptionPane.showMessageDialog(null,"Input Error - Increment>MaxV !");
                    error=1;
                    return error;
                }
  
       return 0;
   }
   
   
     private double[][] createSeries() {
        double[][] series = new double[2][MAX];
        int i=0;
         try {
//change COMM PORT here. Numbers are in ascending order of available COMM PORTS, for ex [6] could be PORT8
            SerialPort comPort = SerialPort.getCommPorts()[COMM_PORT];    
            comPort.openPort();
            comPort.setBaudRate(baud);
            comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);         
            InputStream in = comPort.getInputStream();
            OutputStream out = comPort.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(out);
            char c=0;
       //     String word="";
            String SendData="";
            while(c!='?')
            {
                c = (char)in.read();
          //      word+=c;
            }
            int error=1;
            while(ErrorCheck()==1);

      SendData= Float.toString(MaxV);

      SendData+='V';
osw.write(SendData, 0, SendData.length());
    osw.flush();
    

    c=0;
    SendData="";
    while(c!='?')
    {
        c = (char)in.read();
    }
      SendData= Float.toString(Increment);
      SendData+='V';
Thread.sleep(100); 

osw.write(SendData, 0, SendData.length());
    osw.flush();

     c=0;
    SendData="";
    while(c!='?')
    {
        c = (char)in.read();
    }
    error=1;
  
    while(error==1)
    {
        try{
         Rate= Float.parseFloat(JOptionPane.showInputDialog (null, "Enter Plot Interval(s):(min 0.3s)"));
        error=0;
        }catch(Exception e){
            JOptionPane.showMessageDialog(null,"Input Error - Example: 0.4");
        error=1;
        }
    }
    if(Rate<0.3)
        Rate= (float)0.3;
      SendData= Float.toString(Rate);
      SendData+='s';
osw.write(SendData, 0, SendData.length());
    osw.flush();
    
    c = (char)in.read();
            String line="";
            while(c!='X' && c!='x')
            {
                if(c==',')
                {
                 try{
                  series[0][i]= Double.parseDouble(line); 
                 }
                 catch(Exception e)
                 {
                       System.out.println("Error a");
                 }
                  line="";    
            }
                else if(c=='\r')
                {
                  try{
                  series[1][i]= Double.parseDouble(line); 
                   i++;
                  }
                 catch(Exception e)
                 {
                      System.out.println("Error3");
                 }
                  line="";
                }
               else{
                  line+=c;
               }
               c = (char)in.read();
            }

      in.close();
      comPort.closePort();

      }
       catch (Exception ex) 
    {
        JOptionPane.showMessageDialog(this,"Serial Comm Error"); 
        System.out.println(ex);
    }   
        return series;
    }

    private JFreeChart createChart(XYDataset dataset) {
        // create the chart...
        JFreeChart chart = ChartFactory.createScatterPlot(
        "ADC vs DAC", 
        "DAC (V)", "ADC (V)", dataset, PlotOrientation.VERTICAL,
false, // include legend
true, // tooltips
false );  //url      
        chart.setBackgroundPaint(Color.white);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(192, 192, 160));

 NumberAxis domain = (NumberAxis) plot.getDomainAxis();
        domain.setRange(0.00, MaxV+0.1);
       // domain.setTickUnit(new NumberTickUnit(0.1));
        domain.setVerticalTickLabels(true);
        NumberAxis range = (NumberAxis) plot.getRangeAxis();
        range.setRange(0.0, MaxV+0.1);
       // range.setTickUnit(new NumberTickUnit(0.1));
 
       try{
               XYItemRenderer renderer = plot.getRenderer();
    renderer.setSeriesPaint(0, Color.blue);
    double size = 3.0;
    double delta = size / 2.0;
    Shape shape1 = new Ellipse2D.Double(-delta, -delta, size, size);
    renderer.setSeriesShape(0, shape1);
        renderer.setSeriesPaint(1, Color.blue);

        renderer.setSeriesShape(1, shape1);
               }catch(Exception err){
                   System.out.println("Error b");
               }       
        return chart;
    }

 
   
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLayeredPane1 = new javax.swing.JLayeredPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        HelpFileButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(640, 460));
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(102, 102, 102));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 11, Short.MAX_VALUE)
        );

        jPanel3.setBackground(new java.awt.Color(102, 102, 102));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 630, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );

        jLabel5.setFont(new java.awt.Font("Algerian", 0, 42)); // NOI18N
        jLabel5.setText("SMART NANo GAS SENSOR");

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/csir_logo - Copy.jpg"))); // NOI18N
        jLabel4.setText("jLabel4");

        jPanel4.setBackground(new java.awt.Color(102, 102, 102));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 630, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 42, Short.MAX_VALUE)
        );

        jLabel6.setFont(new java.awt.Font("Arial Rounded MT Bold", 0, 24)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(204, 204, 204));
        jLabel6.setText("E.I.S.");

        HelpFileButton.setText("Help");
        HelpFileButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        HelpFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HelpFileButtonActionPerformed(evt);
            }
        });

        jButton1.setFont(new java.awt.Font("Algerian", 0, 24)); // NOI18N
        jButton1.setText("Plot Graph");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(530, 530, 530)
                        .addComponent(HelpFileButton, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(50, 50, 50)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 530, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(210, 210, 210)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(295, 295, 295)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(HelpFileButton)
                .addGap(7, 7, 7)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(50, 50, 50)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(60, 60, 60)
                        .addComponent(jLabel6)))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jLayeredPane1.add(jPanel1);
        jPanel1.setBounds(0, 0, 630, 440);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLayeredPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 627, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLayeredPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 440, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

 /**
 * Displays Help File
 */
    private void HelpFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HelpFileButtonActionPerformed
    new Helpframe().setVisible(true);         // TODO add your handling code here:
    }//GEN-LAST:event_HelpFileButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
     EventQueue.invokeLater(new Runnable() {
     public void run() {
        JOptionPane.showMessageDialog (null, "Press STM32 Reset Button Again After Clicking OK!");
        Thread thread = new Thread("New Thread") {//thread
        public void run(){
          jButton1.setEnabled(false);

              Dashboard  demo = new Dashboard("Serial Comm Plotting");
                  demo.pack();
                 demo.setLocationRelativeTo(null);
                  demo.setVisible(true);
            int j;
    double temp_marker=0;
 try {
//change COMM PORT here. Numbers are in ascending order of available COMM PORTS, for ex [6] could be PORT8
            comPort = SerialPort.getCommPorts()[COMM_PORT];    
            comPort.openPort();
            comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
            in = comPort.getInputStream();
      while(true){
          
            int i=0;
            double[][] series = new double[2][MAX];

            char c = (char)in.read();
            String line="";
            while(c!='X' && c!='x')
            {
                if(c==',')
                {
                 try{
                  series[0][i]= Double.parseDouble(line); 
                 }
                 catch(Exception e)
                 {
                  System.out.println("TestError");
                  System.out.println(e);              
                 }
                  line="";    
            }
                else if(c=='\n')
                {
                  try{
                  series[1][i]= Double.parseDouble(line); 
                   i++;
                

                  }
                 catch(Exception e)
                 {
                  //System.out.println("YVal");
                  //System.out.println(e);
                     System.out.println("Error d");
                 }
                  line="";
                }
               else{
                  line+=c;
               }
               c = (char)in.read();
               
           if(series[0][i]!=0){
                temp_marker=series[0][i];
           }
               
            }
             try{
                plot.clearDomainMarkers();
                }
                catch(Exception e)
                {
                    System.out.println("Error f");
                }
          j = dataset.getSeriesCount();
            try{
               XYItemRenderer renderer = plot.getRenderer();
    renderer.setSeriesPaint(j, Color.blue);
    double size = 3.0;
    double delta = size / 2.0;
    Shape shape1 = new Ellipse2D.Double(-delta, -delta, size, size);
    renderer.setSeriesShape(j, shape1);
               }catch(Exception err){
                   System.out.println("Error g");
               }
          
          series_index++;
          
          if(series_index>MaxPoints)
          {
              MaxPoints=series_index;
          }
               dataset.addSeries("Series"+series_index, series);
               index=j;
             
         try{

           marker = new ValueMarker(temp_marker);  // position is the value on the axis
           marker.setPaint(Color.red);
           //marker.setLabel("here"); // see JavaDoc for labels, colors, strokes

           plot = (XYPlot) chart.getPlot();
           plot.addDomainMarker(marker);

           
       }catch(Exception e){ 
           System.out.println(e);
        }
          
}
            //in.close();
         //comPort.closePort();            
}
     catch (Exception ex) 
    {
        JOptionPane.showMessageDialog(null,"Serial Comm Error");   
    } 

      }
   };

   thread.start();

            }
         });
            // TODO add your handling code here:

    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Dashboard().setVisible(true);
                try{
                    COMM_PORT = Integer.parseInt(JOptionPane.showInputDialog (null, "Enter COMM_PORT:"));
                }
            catch(Exception e){
                JOptionPane.showMessageDialog (null, "ERROR!");
            System.exit(0);
            }
                JOptionPane.showMessageDialog (null, "Press STM32 Reset Button!");
            
            
            
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton HelpFileButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    // End of variables declaration//GEN-END:variables
}
