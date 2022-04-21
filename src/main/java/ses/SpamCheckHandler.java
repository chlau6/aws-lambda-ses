package ses;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import util.Strings;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class SpamCheckHandler implements RequestStreamHandler {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) {
        context.getLogger().log("Processing Email");
        String jsonString = Strings.readString(inputStream);

        SESEventRecord record = gson.fromJson(jsonString, SESEventRecord.class);

        ProcessEmailResponse response = checkSpamEmail(record, context);

        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.US_ASCII)));
        writer.write(gson.toJson(response));
        writer.flush();
        writer.close();
    }

    private ProcessEmailResponse checkSpamEmail(SESEventRecord record, Context context) {
        SESEventRecord.SES ses = record.records.get(0).ses;

        context.getLogger().log(gson.toJson(ses.mail.messageId));
        context.getLogger().log(ses.receipt.spfVerdict.status);
        context.getLogger().log(ses.receipt.dkimVerdict.status);
        context.getLogger().log(ses.receipt.spamVerdict.status);
        context.getLogger().log(ses.receipt.virusVerdict.status);

        if (ses.receipt.spfVerdict.status.equals("FAIL")
                || ses.receipt.dkimVerdict.status.equals("FAIL")
                || ses.receipt.spamVerdict.status.equals("FAIL")
                || ses.receipt.virusVerdict.status.equals("FAIL")) {
            // Stop processing rule set, dropping message

            context.getLogger().log("Dropping Spam");
            return new ProcessEmailResponse(ProcessEmailResponse.Disposition.STOP_RULE);
        } else {
            return new ProcessEmailResponse(ProcessEmailResponse.Disposition.CONTINUE);
        }
    }
}