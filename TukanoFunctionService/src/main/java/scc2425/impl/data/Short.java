package scc2425.impl.data;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import scc2425.impl.Token;

/**
 * Represents a Short video uploaded by an user.
 * 
 * A short has an unique shortId and is owned by a given user; 
 * Comprises of a short video, stored as a binary blob at some bloburl;.
 * A post also has a number of likes, which can increase or decrease over time. It is the only piece of information that is mutable.
 * A short is timestamped when it is created.
 *
 */
@Entity
public class Short {
	
	@Id
	String id;
	String ownerId;
	String blobUrl;
	long timestamp;
	int totalLikes;
	long totalViews;

	public Short() {}

	public Short(String shortId, String ownerId, String blobUrl, long timestamp, int totalLikes, long totalViews) {
		super();
		this.id = shortId;
		this.ownerId = ownerId;
		this.blobUrl = blobUrl;
		this.timestamp = timestamp;
		this.totalLikes = totalLikes;
		this.totalViews = totalViews;
	}

	public Short(String shortId, String ownerId, String blobUrl) {
		this( shortId, ownerId, blobUrl, System.currentTimeMillis(), 0, 0);
	}

	public String getId() {
		return id;
	}

	public void setId(String shortId) {
		this.id = shortId;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public String getBlobUrl() {
		return blobUrl;
	}

	public void setBlobUrl(String blobUrl) {
		this.blobUrl = blobUrl;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getTotalLikes() {
		return totalLikes;
	}

	public void setTotalLikes(int totalLikes) {
		this.totalLikes = totalLikes;
	}

	public long getTotalViews() {
		return totalViews;
	}

	public void setTotalViews(int totalViews) {
		this.totalViews = totalViews;
	}

	public void incrementTotalViews() {
		this.totalViews = this.totalViews + 1;
	}

	@Override
	public String toString() {
		return "Short [shortId=" + id + ", ownerId=" + ownerId + ", blobUrl=" + blobUrl + ", timestamp="
				+ timestamp + ", totalLikes=" + totalLikes + ", totalViews=" + totalViews + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Short aShort = (Short) o;
		return timestamp == aShort.timestamp && totalLikes == aShort.totalLikes && Objects.equals(id, aShort.id) && Objects.equals(ownerId, aShort.ownerId) && Objects.equals(blobUrl, aShort.blobUrl);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, ownerId, blobUrl, timestamp, totalLikes);
	}

	public Short copyWithLikes_And_Token(long totLikes) {
		var urlWithToken = String.format("%s?token=%s", blobUrl, Token.get(blobUrl));
		return new Short( id, ownerId, urlWithToken, timestamp, (int)totLikes, this.totalViews);
	}
}