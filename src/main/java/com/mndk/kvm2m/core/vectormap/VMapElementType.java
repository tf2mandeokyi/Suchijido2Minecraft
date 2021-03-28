package com.mndk.kvm2m.core.vectormap;

// TODO translate this
public enum VMapElementType {
	
	// A타입 - 교통
	도로경계("A001", Category.교통),
	도로중심선("A002", Category.교통),
	보도("A003", Category.교통),
	횡단보도("A004", Category.교통),
	안전지대("A005", Category.교통),
	육교("A006", Category.교통),
	교량("A007", Category.교통),
	교차로("A008", Category.교통),
	입체교차부("A009", Category.교통),
	인터체인지("A010", Category.교통),
	터널("A011", Category.교통),
	터널입구("A012", Category.교통),
	정거장("A013", Category.교통),
	정류장("A014", Category.교통),
	철도("A015", Category.교통),
	철도경계("A016", Category.교통),
	철도중심선("A017", Category.교통),
	철도전차대("A018", Category.교통),
	승강장("A019", Category.교통),
	승강장_지붕("A020", Category.교통),
	나루("A021", Category.교통),
	나루노선("A022", Category.교통),
	
	// B타입 - 건물
	건물("B001", Category.건물),
	담장("B002", Category.건물),
	
	// C타입 - 시설
	댐("C001", Category.시설),
	부두("C002", Category.시설),
	선착장("C003", Category.시설),
	선거("C004", Category.시설),
	제방("C005", Category.시설),
	수문("C006", Category.시설),
	암거("C007", Category.시설),
	잔교("C008", Category.시설),
	우물("C009", Category.시설),
	관정("C010", Category.시설),
	분수("C011", Category.시설),
	온천("C012", Category.시설),
	양식장("C013", Category.시설),
	낚시터("C014", Category.시설),
	해수욕장("C015", Category.시설),
	등대("C016", Category.시설),
	저장조("C017", Category.시설),
	탱크("C018", Category.시설),
	광산("C019", Category.시설),
	적치장("C020", Category.시설),
	채취장("C021", Category.시설),
	조명("C022", Category.시설),
	전력주("C023", Category.시설),
	맨홀("C024", Category.시설),
	소화전("C025", Category.시설),
	관측소("C026", Category.시설),
	야영지("C027", Category.시설),
	묘지("C028", Category.시설),
	묘지계("C029", Category.시설),
	유적지("C030", Category.시설),
	문화재("C031", Category.시설),
	성("C032", Category.시설),
	비석("C033", Category.시설),
	탑("C034", Category.시설),
	동상("C035", Category.시설),
	공중전화("C036", Category.시설),
	우체통("C037", Category.시설),
	놀이시설("C038", Category.시설),
	계단("C039", Category.시설),
	게시판("C040", Category.시설),
	표지("C041", Category.시설),
	주유소("C042", Category.시설),
	주차장("C043", Category.시설),
	휴게소("C044", Category.시설),
	지하도("C045", Category.시설),
	지하도입구("C046", Category.시설),
	지하환기구("C047", Category.시설),
	굴뚝("C048", Category.시설),
	신호등("C049", Category.시설),
	차단기("C050", Category.시설),
	도로반사경("C051", Category.시설),
	도로분리대("C052", Category.시설),
	방지책("C053", Category.시설),
	요금징수소("C054", Category.시설),
	헬기장("C055", Category.시설),
	
	// D타입 - 식생
	경지계("D001", Category.식생),
	지류계("D002", Category.식생),
	독립수("D003", Category.식생),
	목장("D004", Category.식생),
	
	// E타입 - 수계
	하천경계("E001", Category.수계),
	하천중심선("E002", Category.수계),
	실폭하천("E003", Category.수계),
	유수방향("E004", Category.수계),
	호수("E005", Category.수계),
	용수로("E006", Category.수계),
	폭포("E007", Category.수계),
	해안선("E008", Category.수계),
	등심선("E009", Category.수계),
	
	// F타입 - 지형
	등고선("F001", Category.지형),
	표고점("F002", Category.지형),
	절토("F003", Category.지형),
	옹벽("F004", Category.지형),
	동굴입구("F005", Category.지형),
	
	// G타입 - 경계
	시도_행정경계("G001", Category.경계),
	시군구_행정경계("G002", Category.경계),
	읍면동_행정경계("G003", Category.경계),
	수부지형경계("G004", Category.경계),
	기타경계("G005", Category.경계),
	
	// H타입 - 주기
	도곽선("H001", Category.주기),
	기준점("H002", Category.주기),
	격자("H003", Category.주기),
	지명("H004", Category.주기),
	산("H005", Category.주기);
	
	@Deprecated
	public enum FillType {
		AREA, LINE, POINT
	}
	
	public enum Category {
		교통('A'), 건물('B'), 시설('C'), 식생('D'), 수계('E'), 지형('F'), 경계('G'), 주기('H');
		private final char character;
		private Category(char c) {
			this.character = c;
		}
		public static Category valueOf(char firstChar) {
			for(Category c : values()) {
				if(c.character == firstChar) return c;
			}
			return null;
		}
	}
	
	final String layerHeader;
	private final Category category;
	
	private VMapElementType(String layerHeader, Category category) {
		this.layerHeader = layerHeader;
		this.category = category;
	}
	
	public static VMapElementType getTypeFromLayerName(String layerName) {
		for(VMapElementType t : values()) {
			if(layerName.startsWith(t.layerHeader)) {
				return t;
			}
		}
		return null;
	}
	
	public Category getCategory() {
		return category;
	}
}
