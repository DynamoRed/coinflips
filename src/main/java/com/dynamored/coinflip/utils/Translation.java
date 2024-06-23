package com.dynamored.coinflip.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class Translation {

	private HashMap<String, Object> translations;

    public Translation(File file) throws IOException {
        String content = Json.readJsonFile(file);
		this.translations = Json.parseJsonToMap(content);
    }

    public Object getTranslation(String key) {
        return translations.getOrDefault(key, translations.getOrDefault("_Missing_Translation_", "Missing translations"));
    }
}
