package com.ohmdb.tutorial;

public class Post {

	public long id;

	public String content;

	// public Visibility visibility = Visibility.FRIENDS;

	public Post() {
		super();
	}

	public Post(String content) {
		super();
		this.content = content;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		/*
		 * result = prime * result + ((visibility == null) ? 0 :
		 * visibility.hashCode());
		 */
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Post other = (Post) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (id != other.id)
			return false;
		/*
		 * if (visibility != other.visibility) return false;
		 */
		return true;
	}

	@Override
	public String toString() {
		return "Post [id=" + id + ", content=" + content + "]";
	}

}
