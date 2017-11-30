package Actions;

import Classes.ConsoleOutput;
import SavedVariables.DynamicVariables;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;


/**
 *  <h3>Created by Marius Baltramaitis on 01 november 2017</h3>
 *  <p>Class with methods to deliver sms messages(gsm only)</p>
 *
 *  <p>In this project i have been using twilio.com service, together with external libraries</p>
 */
public class SmsOutPut {

    private String textToSend;
    private String destinationNumber;
    private String sendingNumber;

    /**
     * Constructor
     * @param textToSend    text to send
     * @param destinationNumber destination number (numbers of wired phone wont work)
     */
    public SmsOutPut(String textToSend, String destinationNumber) {
        this.textToSend = textToSend;
        this.destinationNumber = destinationNumber;
    }

    /**
     * Method for delivering message generated in constructor
     */
    public void sendShortMessage()
    {
        if(destinationNumber.contains("+370"))
            sendingNumber = DynamicVariables.LITHUANIAN_NUMBER;

        if(destinationNumber.contains("+47"))
            sendingNumber = DynamicVariables.NORWEGIAN_NUMBER;

        if(textToSend != null && textToSend.length() > 180)
            throw new UnsupportedOperationException("Short messages are under 180 characters set!");

        Twilio.init(DynamicVariables.ACCOUNT_SID,DynamicVariables.AUTH_TOKEN);

        Message message = Message.creator(new PhoneNumber(destinationNumber),new PhoneNumber(sendingNumber),textToSend).create();

        ConsoleOutput.print("Message status " + message.getSid());


    }

}
