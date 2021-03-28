package com.mndk.kvm2m.core.vectormap;

import com.mndk.kvm2m.core.projection.Grs80Projection;
import com.mndk.kvm2m.core.projection.Projections;

public class VMapUtils {

	public static Grs80Projection getProjectionFromMapId(String fileName) {
		char number = fileName.charAt(2);
		if(number == '5') {
			return Projections.GRS80_WEST;
		} else if(number == '6' || number == '7') {
			return Projections.GRS80_MIDDLE;
		} else if(number == '8' || number == '9') {
			return Projections.GRS80_EAST;
		}
		return null;
	}

	public static int getScaleFromMapId(String id) {
		switch(id.length()) {
			case 3: return 250000;
			case 5: return 50000;
			case 6: return 25000;
			case 7: return 10000;
			case 8:
				char last = id.charAt(7);
				if(last >= '0' && last <= '9') // If the last character is v1 number:
					return 5000;
				else // Or else if it's an alphabet
					return 2500;
			case 9: return 1000;
			case 10: return 500;
		}
		return -1;
	}

}
