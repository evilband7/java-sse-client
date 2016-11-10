package io.prime.sse.client;

import java.util.ArrayList;
import java.util.List;

public class SseMessage 
{
    private List<String> data = new ArrayList<>();
    private String id;
    private String event;
    private String retry;
    private String origin;

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SseMessage that = (SseMessage) o;

        if (data != null ? !data.equals(that.data) : that.data != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (origin != null ? !origin.equals(that.origin) : that.origin != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = data != null ? data.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (origin != null ? origin.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MessageEvent{" +
        		"event='" + event + '\'' +
                ", data='" + data + '\'' +
                ", eventId='" + id + '\'' +
                ", origin='" + origin + '\'' +
                '}';
    }

	public List<String> getData() {
		return data;
	}

	public void setData(List<String>  data) {
		this.data = data;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String getRetry() {
		return retry;
	}

	public void setRetry(String retry) {
		this.retry = retry;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}
}
