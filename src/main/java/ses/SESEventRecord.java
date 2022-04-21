package ses;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SESEventRecord {
    @SerializedName("Records")
    public List<Record> records = new ArrayList<>();

    public static class Record {
        @SerializedName("eventSource")
        public String eventSource;

        @SerializedName("eventVersion")
        public String eventVersion;

        @SerializedName("ses")
        public SES ses;
    }

    public static class SES {
        @SerializedName("mail")
        public Mail mail;

        @SerializedName("receipt")
        public Receipt receipt;
    }

    public static class Mail {
        @SerializedName("timestamp")
        public String timestamp;

        @SerializedName("source")
        public String source;

        @SerializedName("messageId")
        public String messageId;

        @SerializedName("destination")
        public List<String> destinations = new ArrayList<>();

        @SerializedName("headersTruncated")
        public boolean headersTruncated;

        @SerializedName("headers")
        public List<Header> headers = new ArrayList<>();

        @SerializedName("commonHeaders")
        public CommonHeaders commonHeaders;
    }

    public static class CommonHeaders {
        @SerializedName("returnPath")
        public String returnPath;

        @SerializedName("from")
        public List<String> from = new ArrayList<>();

        @SerializedName("date")
        public String date;

        @SerializedName("to")
        public List<String> to = new ArrayList<>();

        @SerializedName("messageId")
        public String messageId;

        @SerializedName("subject")
        public String subject;
    }

    public static class Receipt {
        @SerializedName("timestamp")
        public String timestamp;

        @SerializedName("processingTimeMillis")
        public BigDecimal processingTimeMillis;

        @SerializedName("recipients")
        public List<String> recipients = new ArrayList<>();

        @SerializedName("spamVerdict")
        public Verdict spamVerdict;

        @SerializedName("virusVerdict")
        public Verdict virusVerdict;

        @SerializedName("spfVerdict")
        public Verdict spfVerdict;

        @SerializedName("dkimVerdict")
        public Verdict dkimVerdict;

        @SerializedName("dmarcVerdict")
        public Verdict dmarcVerdict;

        @SerializedName("action")
        public Action action;
    }

    public static class Header {
        @SerializedName("name")
        public String name;

        @SerializedName("value")
        public String value;
    }

    public static class Verdict {
        @SerializedName("status")
        public String status;
    }

    public static class Action {
        @SerializedName("type")
        public String type;

        @SerializedName("functionArn")
        public String functionArn;

        @SerializedName("invocationType")
        public String invocationType;
    }
}