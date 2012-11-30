package com.baixing.entity;

import java.util.List;

public class Filters {
	List<Filterss> filterssList;

	public List<Filterss> getFilterssList() {
		return filterssList;
	}

	public void setFilterssList(List<Filterss> filterssList) {
		this.filterssList = filterssList;
	}

	@Override
	public String toString() {
		return "Filters [filterssList=" + filterssList + "]";
	}

}
