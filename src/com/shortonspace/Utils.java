package com.shortonspace;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

public class Utils {
    public static DecimalFormat countFormatter = new DecimalFormat("#,###");
    public static DecimalFormat formatter = new DecimalFormat("#.#");    

    public static String stringFromFileSize(long theSize) {
    	return stringFromFileSize(theSize, false, false);
    }
    
    public static String stringFromFileSize(long theSize, boolean showInBytes, boolean showUnitForBytes) {
		double floatSize = theSize;
	
		if (theSize < 1023 || showInBytes) {
	        String value = formatter.format(floatSize);
			return value + (showUnitForBytes ? " bytes" : "");
	    }
	    
		floatSize = floatSize / 1024;
		if (floatSize < 1023) {
	        String value = formatter.format(floatSize);
			return value + " KB";
	    }
		floatSize = floatSize / 1024;
		if (floatSize < 1023) {
	        String value = formatter.format(floatSize);
			return value + " MB";
	    }
		floatSize = floatSize / 1024;
	    String value = formatter.format(floatSize);
		return value + " GB";
	}

	public static boolean isSymlink(File file) throws IOException {
		File canon;
		if (file.getParent() == null) {
			canon = file;
		} else {
			File canonDir = file.getParentFile().getCanonicalFile();
			canon = new File(canonDir, file.getName());
		}
		return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
	}
}