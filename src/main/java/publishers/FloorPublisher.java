package publishers;

import java.util.Random;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

public class FloorPublisher {
   public static volatile boolean keepRunning = true;

   public static void main(String[] args) {
      while (keepRunning && ! Thread.currentThread().isInterrupted()) {

         String broker = "tcp://broker.hivemq.com:1883";
         String clientId = "FloorPublisher";
         MqttClient mqttClient = null;// create a new MQTT client object

         try {
            // create a new MQTT client object with the broker and client ID
            mqttClient = new MqttClient(broker, clientId);
            // create a new connection options object and set the will message to be sent when the client disconnects
            MqttConnectOptions options = new MqttConnectOptions();
            options.setWill("floor/disconnect", "Floor sensor disconnected".getBytes(), 0, false);
            mqttClient.connect(options);// connect the client to the broker with the connection options

            Random random = new Random();// create a new random object

            while (keepRunning && ! Thread.currentThread().isInterrupted()) {

               // generate random values for light and window status (ON/OFF, OPEN/CLOSED)
               String lightID = random.nextBoolean() ? "ON" : "OFF";
               String window = random.nextBoolean() ? "OPEN" : "CLOSED";

               // create topics for the light and window status values
               String lightTopic = "floor/light/ID";
               String windowTopic = "floor/window/status";

               // publish the light and window status values to the respective topics
               FloorPublisher.publishMessage(mqttClient, lightTopic, lightID);

               // publish the window status value to the window topic
               FloorPublisher.publishMessage(mqttClient, windowTopic, window);
               Thread.sleep(1000);

            }// end of while loop
            // handle exceptions that may occur during the execution of the program
         } catch (MqttException | InterruptedException e) {
            if (mqttClient != null) {// check if the MQTT client object is not null before disconnecting
               String errorTopic = "floor/error";// create a topic for error messages
               String errorMessage = e.getMessage();// get the error message
               try {
                  // publish the error message to the error topic using the publishMessage method
                  FloorPublisher.publishMessage(mqttClient, errorTopic, errorMessage);
                  // handle any exceptions that may occur during the publishing of the error message
               } catch (MqttException me) {
                  me.printStackTrace();// print the stack trace of the exception
               }// end of try-catch block
            }// end of if block
         }// end of try-catch block
      }// end of while loop
   }// end of main method

   /*
    * This method publishes a message to a given topic
    */
   private static void publishMessage(MqttClient mqttClient, String topic, String payload) throws MqttException {
      if (mqttClient != null && mqttClient.isConnected()) {
         MqttMessage message = new MqttMessage();
         message.setPayload(payload.getBytes());
         mqttClient.publish(topic, message);
         //System.out.println("Published message: " + payload + " to " + topic);
      }
   }// end of publishMessage method

}// end of class FloorPublisher