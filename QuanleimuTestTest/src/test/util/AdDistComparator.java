package test.util;

import java.util.Comparator;

import com.baixing.entity.Ad;

public class AdDistComparator implements Comparator<Ad> {

	public int compare(Ad lhs, Ad rhs) {
		if (lhs.getDistance() > rhs.getDistance()) {
			return 1;
		}
		else if (lhs.getDistance() == rhs.getDistance())
		{
			return 0;
		}
		
		return -1;
	}

}
