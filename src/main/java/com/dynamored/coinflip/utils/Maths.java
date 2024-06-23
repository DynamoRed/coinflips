package com.dynamored.coinflip.utils;

import java.util.Random;

public class Maths {

	public static int random(int min, int max) {
		return new Random().nextInt((max - min) + 1) + min;
	}

	public static int random(int min, int max, boolean exclude) {
		if (exclude) return new Random().nextInt((max - 1 - min) + 1) + min;
		return new Random().nextInt((max - min) + 1) + min;
	}
}
