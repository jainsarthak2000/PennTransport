import com.twilio.http.TwilioRestClient;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import static spark.Spark.*;

import static spark.Spark.get;
import static spark.Spark.post;

public class SMSBackend {
    private static final String TWILIO_ACCOUNT_SID = "AC0de9b4a951e7a02ad6ec2a320b30bce7";
    private static final String TWILIO_AUTH_TOKEN = "252553562d1ea3092c95c5364c7069eb";
    private static final String YOUR_TWILIO_PHONE_NUMBER = "6502156634";

    public static void main(String[] args) {
        get("/", (req, res) -> "Hello, World! :)");


        TwilioRestClient client = new TwilioRestClient.Builder(System.getenv(TWILIO_ACCOUNT_SID),
                System.getenv(TWILIO_AUTH_TOKEN)).build();

        post("/sms", (req, res) -> {
            String body = req.queryParams("Body");
            String to = req.queryParams("To");
            String from = YOUR_TWILIO_PHONE_NUMBER;

            Message message = new MessageCreator(
                    new PhoneNumber(to),
                    new PhoneNumber(from),
                    body).create(client);

            System.out.println(body);
            return message.getSid();
        });
    }
}
