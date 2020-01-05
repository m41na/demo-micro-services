package works.hop.queue.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TodoAction {

    public final String name;
    public final Boolean completed;
    public final String action;

    @JsonCreator
    public TodoAction(@JsonProperty("name") String name, @JsonProperty("completed") Boolean completed, @JsonProperty("action") String action) {
        this.name = name;
        this.completed = completed;
        this.action = action;
    }
}
