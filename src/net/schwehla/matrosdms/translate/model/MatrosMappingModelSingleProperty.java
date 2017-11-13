package net.schwehla.matrosdms.translate.model;

import java.util.ArrayList;
import java.util.List;

public class MatrosMappingModelSingleProperty  {
	
	
	String propertyName;

	public String getPropertyName() {
		return propertyName;
	}


	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	boolean editable;
	boolean propertyRootNode;
	
	
	
	public boolean isPropertyRootNode() {
		return propertyRootNode;
	}


	public boolean isEditable() {
		return editable;
	}


	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public String getText1() {
		return text1;
	}

	public void setText1(String text1) {
		this.text1 = text1;
	}

	public String getUserTranslatedText() {
		return userTranslatedText;
	}

	public void setUserTranslatedText(String text2) {
		this.userTranslatedText = text2;
	}

	public String getStatusText() {
		return statusText;
	}

	public void setStatusText(String text3) {
		this.statusText = text3;
	}

	public String text1;
	public String userTranslatedText;
	public String statusText;

	public MatrosMappingModelSingleProperty parent;

	public List<MatrosMappingModelSingleProperty> children = new ArrayList<MatrosMappingModelSingleProperty>();

	public MatrosMappingModelSingleProperty(MatrosMappingModelSingleProperty parent, String propertyName, String text1, String text2, String text3) {

		this.parent = parent;
		this.propertyName = propertyName;
		this.text1 = text1;
		this.userTranslatedText = text2;
		this.statusText = text3;

		if (parent != null)
			parent.children.add(this);

	}

	public MatrosMappingModelSingleProperty(MatrosMappingModelSingleProperty root, String propertyName, String string, String string2, String string3, boolean editable, boolean propertyRootNode) {

		this(root, propertyName, string, string2, string3);
		this.editable = editable;
		this.propertyRootNode = propertyRootNode;
	}

	public boolean isKidsNotFilled() {
		
		if (children.isEmpty()) {
			return false;
		}
		
		for (MatrosMappingModelSingleProperty kid: children) {
			if (kid.editable && kid.isNotFilled()) {
				return true;
				
			}
		}
		
		return false;
		
	}

	public boolean isNotFilled() {
		return (isEditable() && this.getUserTranslatedText() == null || this.getUserTranslatedText().trim().length() == 0);
	}
}
