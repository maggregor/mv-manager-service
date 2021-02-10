package com.alwaysmart.optimizer;

public class MViewField {

	public enum FieldType {
		REFERENCE, AGGREGATE
	}

	public enum FieldOrigin {
		SELECT, WHERE, GROUP_BY
	}

	private String content;
	private FieldType type;
	private FieldOrigin origin;

	public MViewField(String content, FieldType type, FieldOrigin origin) {
		this.content = content;
		this.type = type;
		this.origin = origin;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public FieldType getType() {
		return type;
	}

	public void setType(FieldType type) {
		this.type = type;
	}

	public FieldOrigin getOrigin() {
		return origin;
	}

	public void setOrigin(FieldOrigin origin) {
		this.origin = origin;
	}
}
