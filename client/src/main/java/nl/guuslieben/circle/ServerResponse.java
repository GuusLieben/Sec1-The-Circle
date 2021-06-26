package nl.guuslieben.circle;

import lombok.Getter;

@Getter
public class ServerResponse<T> {

    private T object;
    private String message;

    public ServerResponse(T object) {
        this.object = object;
    }

    public ServerResponse(String message) {
        this.message = message;
    }

    public boolean accepted() {
        return this.object != null;
    }
}
