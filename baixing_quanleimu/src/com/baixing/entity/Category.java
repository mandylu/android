//liuchong@baixing.com
package com.baixing.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author liuchong
 *
 */
public class Category implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private String englishName;
	private String level;
	private String parentEnglishName;
	
	private List<Category> children = new ArrayList<Category>();
	private Category parent;
	
	public String getName() {
		return name;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getParentEnglishName() {
		return parentEnglishName;
	}

	public void setParentEnglishName(String parentEnglishName) {
		this.parentEnglishName = parentEnglishName;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEnglishName() {
		return englishName;
	}

	public void setEnglishName(String englishName) {
		this.englishName = englishName;
	}

	public List<Category> getChildren() {
		return children;
	}

	public void setChildren(List<Category> children) {
		if (children == null) {
			this.children.clear();
		}
		
		for(Category cat : children) {
			this.addChild(cat);
		}
	}
	
	public void addChild(Category cat) {
		if (cat == null)
		{
			return;
		}
		
		this.children.add(cat);
		cat.setParent(cat);
		cat.setParentEnglishName(this.englishName);
	}

	public Category getParent() {
		return parent;
	}
//
	public void setParent(Category parent) {
		this.parent = parent;
	}
	
	public Category findCategoryByEnglishName(String eName) {
		if (eName.equals(this.englishName)) {
			return this;
		}
		else {
			for (Category child : this.children) {
				Category cat = child.findCategoryByEnglishName(eName);
				if (cat != null) {
					return cat;
				}
			}
		}
		
		return null;
	}
	
	public Category findChildByName(String name) {
		for (Category child : children) {
			if (child.getName().equals(name)) {
				return child;
			}
		}
		
		return null;
	}

	@Override
	public String toString() {
//		return "FirstStepCate [name=" + name + ", englishName=" + englishName
//				+ ", children=" + children + ", parentEnglishName="
//				+ parentEnglishName + "]";
		return name;
	}
}
