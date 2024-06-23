package com.dynamored.coinflip.models;

public enum Lang {
	FR("fr_fr"),
	EN("en_us");

	public final String label;

	private Lang(String label) { this.label = label; }

	public static Lang findByLabel(String label){
		for(Lang lang : values()){
			if(lang.label.equals(label))
				return lang;
		}

		return null;
	}
}
