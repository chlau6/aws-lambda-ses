package ses;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.RawMessage;
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest;
import util.Strings;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;

public class AutoReplyHandler implements RequestStreamHandler {
    private static final String NO_REPLY_EMAIL_ADDRESS = System.getenv("NO_REPLY_EMAIL_ADDRESS");
    private static final String ATTACHMENT_PATH = "./img.png";
    private static final String ATTACHMENT_NAME = "aws.png";

    private static final String PLAIN_REPLY = "Dear Customer,\n" +
            "We've already received your request. Thanks for using our service\n";
    private static final String HTML_REPLY = "<div dir=3D\"ltr\">Dear Customer, <br>" +
            "We've already received your request. Thanks for using our service<br></div>";

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final Session session = Session.getDefaultInstance(new Properties());
    private final SesClient sesClient = SesClient.builder().region(Region.US_EAST_1).build();

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) {
        /*
        If you need to refer the content from customer, get the email from S3 and parse it
         */
        String jsonString = Strings.readString(inputStream);

        SESEventRecord record = gson.fromJson(jsonString, SESEventRecord.class);

        MimeMessage mimeMessage;
        try {
            mimeMessage = prepareEmail(record);
            sendEmail(mimeMessage);
        } catch (MessagingException | IOException e) {
            context.getLogger().log(e.getMessage());
            e.printStackTrace();
        }

        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.US_ASCII)));
        writer.write(gson.toJson(new ProcessEmailResponse(ProcessEmailResponse.Disposition.STOP_RULE_SET)));
        writer.flush();
        writer.close();
    }

    private void sendEmail(MimeMessage mimeMessage) throws MessagingException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        mimeMessage.writeTo(outputStream);

        ByteBuffer buffer = ByteBuffer.wrap(outputStream.toByteArray());

        byte[] byteArray = new byte[buffer.remaining()];
        buffer.get(byteArray);

        SdkBytes data = SdkBytes.fromByteArray(byteArray);

        RawMessage rawMessage = RawMessage.builder().data(data).build();

        SendRawEmailRequest rawEmailRequest = SendRawEmailRequest.builder().rawMessage(rawMessage).build();

        sesClient.sendRawEmail(rawEmailRequest);
    }

    private MimeMessage prepareEmail(SESEventRecord record) throws MessagingException, IOException {
        SESEventRecord.Mail customerEmail = record.records.get(0).ses.mail;

        MimeMessage mimeMessage = new MimeMessage(session);

        mimeMessage.setSubject("No-Reply from XXX", "UTF-8");
        mimeMessage.setFrom(new InternetAddress(NO_REPLY_EMAIL_ADDRESS));
        mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(customerEmail.source));

        MimeMultipart messageBody = new MimeMultipart("alternative");

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(PLAIN_REPLY, "text/plain; charset=UTF-8");

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(HTML_REPLY, "text/html; charset=UTF-8");

        messageBody.addBodyPart(textPart);
        messageBody.addBodyPart(htmlPart);

        MimeBodyPart wrap = new MimeBodyPart();
        wrap.setContent(messageBody);

        MimeMultipart message = new MimeMultipart("mixed");
        message.addBodyPart(wrap);

        File file = new java.io.File(ATTACHMENT_PATH);
        byte[] fileContent = Files.readAllBytes(file.toPath());
        DataSource fds = new ByteArrayDataSource(fileContent, "image/png");

        MimeBodyPart attachment = new MimeBodyPart();
        attachment.setDataHandler(new DataHandler(fds));
        attachment.setFileName(ATTACHMENT_NAME);
        message.addBodyPart(attachment);

        mimeMessage.setContent(message);

        return mimeMessage;
    }
}