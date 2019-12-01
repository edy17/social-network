package org.diehl.spatium.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.joda.time.DateTime;

@RegisterForReflection
public class Comment extends AbstractBaseEntity {

    private String content;
    private DateTime instant;
    private String userId;
    private String postId;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public DateTime getInstant() {
        return instant;
    }

    public void setInstant(DateTime instant) {
        this.instant = instant;
    }
}
