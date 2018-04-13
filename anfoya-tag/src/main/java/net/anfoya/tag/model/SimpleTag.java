package net.anfoya.tag.model;

import net.anfoya.tag.service.Tag;


@SuppressWarnings("serial")
public class SimpleTag implements Tag, Comparable<SimpleTag> {
	private static final String NOT_DEFINED = "n/d";

	private final String id;
	private final String name;
	private final String path;
	private final boolean system;

	public SimpleTag() {
		this(NOT_DEFINED, NOT_DEFINED, NOT_DEFINED, false);
	}
	public SimpleTag(final String id, final String name, final String path, final boolean system) {
		this.id = id == null? NOT_DEFINED: id;
		this.name = name;
		this.path = path;
		this.system = system;
	}

    @Override
	public String toString() {
    	return getName();
    }

	@Override
	public int hashCode() {
	    return id.hashCode();
	}

    @Override
	public boolean equals(final Object other) {
        if (other == null) {
			return false;
		}
        if (!this.getClass().equals(other.getClass())) {
			return false;
		}
        return ((SimpleTag) other).getId().equals(id);
    }

	@Override
	public int compareTo(final SimpleTag o) {
		return getName().compareTo(o.getName());
	}

	@Override
	public SimpleTag copyWithId(final String id) {
		return new SimpleTag(id, getName(), getPath(), isSystem());
	}

	//TODO should not need an id?
	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public boolean isSystem() {
		return system;
	}
}
