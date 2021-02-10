package com.alwaysmart.optimizer;

import javax.persistence.GeneratedValue;
import java.util.LinkedList;

public class MView {

	@GeneratedValue
	private String id;
	private LinkedList<MViewField> fields = new LinkedList<MViewField>();

	public MView() {

	}

	public void addField(MViewField field) {
		this.fields = fields;
	}


}
