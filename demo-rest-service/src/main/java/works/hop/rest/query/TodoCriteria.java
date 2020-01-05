package works.hop.rest.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TodoCriteria {

    public final String name;
    public final Boolean completed;
    public final Integer limit;
    public final Integer offset;
    public final String action;

    @JsonCreator
    public TodoCriteria(@JsonProperty("name") String name, @JsonProperty("completed") Boolean completed, @JsonProperty("limit") Integer limit, @JsonProperty("offset") Integer offset, @JsonProperty("action") String action) {
        this.name = name;
        this.completed = completed;
        this.limit = limit;
        this.offset = offset;
        this.action = action;
    }
}
