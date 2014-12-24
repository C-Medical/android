package jp.sakuramochi702.colorrecognize.common;

import jp.sakuramochi702.colorrecognize.R;

public class ShapeArray {
	
	public static final int RED = 0;
	public static final int BLUE = 1;
	public static final int GREEN = 2;
	public static final int YELLOW = 3;
	public static final int PURPLE = 4;
	
	public static final int CIRCLE = 0;
	public static final int MOON = 1;
	public static final int STAR = 2;
	public static final int FLOWER = 3;
	public static final int HEART = 4;
	
	
	public static int getRId(int color, int shape) {
		switch (color) {
		case RED:
			switch (shape) {
			case CIRCLE:
				return R.drawable.circle_red;
			case MOON:
				return R.drawable.moon_red;
			case STAR:
				return R.drawable.star_red;
			case FLOWER:
				return R.drawable.flower_red;
			case HEART:
				return R.drawable.heart_red;
			}
			break;
		case BLUE:
			switch (shape) {
			case CIRCLE:
				return R.drawable.circle_blue;
			case MOON:
				return R.drawable.moon_blue;
			case STAR:
				return R.drawable.star_blue;
			case FLOWER:
				return R.drawable.flower_blue;
			case HEART:
				return R.drawable.heart_blue;
			}
			break;
		case GREEN:
			switch (shape) {
			case CIRCLE:
				return R.drawable.circle_green;
			case MOON:
				return R.drawable.moon_green;
			case STAR:
				return R.drawable.star_green;
			case FLOWER:
				return R.drawable.flower_green;
			case HEART:
				return R.drawable.heart_green;
			}
			break;
		case YELLOW:
			switch (shape) {
			case CIRCLE:
				return R.drawable.circle_yellow;
			case MOON:
				return R.drawable.moon_yellow;
			case STAR:
				return R.drawable.star_yellow;
			case FLOWER:
				return R.drawable.flower_yellow;
			case HEART:
				return R.drawable.heart_yellow;
			}
			break;
		case PURPLE:
			switch (shape) {
			case CIRCLE:
				return R.drawable.circle_purple;
			case MOON:
				return R.drawable.moon_purple;
			case STAR:
				return R.drawable.star_purple;
			case FLOWER:
				return R.drawable.flower_purple;
			case HEART:
				return R.drawable.heart_purple;
			}
			break;
		}
		return -1;
	}

}
