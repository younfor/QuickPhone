package com.seven.adapter;

import java.util.Comparator;

import com.seven.bean.ContactBean;

/**
 * 
 * @author xiaanming
 *
 *//*
public class PinyinComparator implements Comparator<SortModel> {

	public int compare(SortModel o1, SortModel o2) {
		if (o1.getSortLetters().equals("@")
				|| o2.getSortLetters().equals("#")) {
			return -1;
		} else if (o1.getSortLetters().equals("#")
				|| o2.getSortLetters().equals("@")) {
			return 1;
		} else {
			return o1.getSortLetters().compareTo(o2.getSortLetters());
		}
	}

}*/
public class PinyinComparator implements Comparator<ContactBean> {

	public int compare(ContactBean o1, ContactBean o2) {
		if (o1.getSortKey().equals("@")
				|| o2.getSortKey().equals("#")) {
			return -1;
		} else if (o1.getSortKey().equals("#")
				|| o2.getSortKey().equals("@")) {
			return 1;
		} else {
			return o1.getSortKey().compareTo(o2.getSortKey());
		}
	}


}
