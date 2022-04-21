package ses;

import com.google.gson.annotations.SerializedName;

public class ProcessEmailResponse {
    @SerializedName("disposition")
    public Disposition disposition;

    public ProcessEmailResponse(Disposition disposition) {
        this.disposition = disposition;
    }

    public enum Disposition {
        @SerializedName("STOP_RULE_SET")
        STOP_RULE_SET,

        @SerializedName("STOP_RULE")
        STOP_RULE,

        @SerializedName("CONTINUE")
        CONTINUE
    }
}
