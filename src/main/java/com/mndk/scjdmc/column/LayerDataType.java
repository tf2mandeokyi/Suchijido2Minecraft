package com.mndk.scjdmc.column;

import com.mndk.scjdmc.column.type.*;
import lombok.Getter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum LayerDataType {

	// A타입 - 교통
	도로경계("road_boundary", "A001", A001RoadBoundary.class),
	도로중심선("road_centerline", "A002", A002RoadCenterline.class),
	보도("sidewalk", "A003", A003Sidewalk.class),
	횡단보도("crosswalk", "A004", A004Crosswalk.class),
	안전지대("safe_zone", "A005", A005Safezone.class),
	육교("pedestrian_overpass", "A006", A006PedestrianOverpass.class),
	교량("bridge", "A007", A007Bridge.class),
	교차로("crossroad", "A008"),
	입체교차부("intersection_3d", "A009", A009Intersection3d.class),
	인터체인지("interchange", "A010"),
	터널("tunnel", "A011", A011Tunnel.class),
	터널입구("tunnel_entrance", "A012"),
	정거장("bus_station", "A013"),
	정류장("train_station", "A014"),
	철도("railway", "A015"),
	철도경계("railway_boundary", "A016", A016RailwayBoundary.class),
	철도중심선("railway_centerline", "A017"),
	철도전차대("railway_turntable", "A018"),
	승강장("railway_platform", "A019", A019RailwayPlatform.class),
	승강장_지붕("railway_platform_roof", "A020"),
	나루("river_port", "A021"),
	나루노선("ferry_route", "A022"),

	// B타입 - 건물
	건물("building", "B001", B001Building.class),
	담장("wall", "B002", B002Wall.class),

	// C타입 - 시설
	댐("dam", "C001", C001Dam.class),
	부두("wharf", "C002"),
	선착장("dock", "C003"),
	선거("boat_stop", "C004"),
	제방("embankment", "C005"),
	수문("sluice_gate", "C006"),
	암거("culvert", "C007"),
	잔교("pier", "C008"),
	우물("well", "C009"),
	관정("tubular_well", "C010"),
	분수("fountain", "C011"),
	온천("hot_spring", "C012"),
	양식장("aquaculture", "C013"),
	낚시터("fishing_ground", "C014"),
	해수욕장("seaside_resort", "C015"),
	등대("lighthouse", "C016"),
	저장조("storage_tank", "C017"),
	탱크("tank", "C018"),
	광산("mine", "C019"),
	적치장("mine_yard", "C020"),
	채취장("quarry", "C021"),
	조명("lamp", "C022"),
	전력주("utility_pole", "C023"),
	맨홀("manhole", "C024"),
	소화전("fire_hydrant", "C025"),
	관측소("observatory", "C026"),
	야영지("campsite", "C027"),
	묘지("cemetery", "C028"),
	묘지계("graveyard", "C029"),
	유적지("historical_site", "C030"),
	문화재("cultural_heritage", "C031"),
	성("castle", "C032"),
	비석("tombstone", "C033"),
	탑("tower", "C034"),
	동상("statue", "C035"),
	공중전화("public_telephone", "C036"),
	우체통("mailbox", "C037"),
	놀이시설("amusement_ride", "C038"),
	계단("stairs", "C039"),
	게시판("notice_board", "C040"),
	표지("sign", "C041"),
	주유소("gas_station", "C042"),
	주차장("parking_lot", "C043"),
	휴게소("rest_area", "C044"),
	지하도("underpass", "C045"),
	지하도입구("underpass_entrance", "C046"),
	지하환기구("underground_ventilation", "C047"),
	굴뚝("chimney", "C048"),
	신호등("traffic_light", "C049"),
	차단기("crossing_gate", "C050"),
	도로반사경("road_reflector", "C051"),
	도로분리대("road_separator", "C052"),
	방지책("rockfall_protection", "C053"),
	요금징수소("tollgate", "C054"),
	헬기장("heliport", "C055"),

	// D타입 - 식생
	경지계("ground_boundaries", "D001"),
	지류계("waterflow_boundary", "D002"),
	독립수("tree", "D003"),
	목장("pasture", "D004"),

	// E타입 - 수계
	하천경계("river_boundary", "E001"),
	하천중심선("river_centerline", "E002"),
	실폭하천("river", "E003", E003River.class),
	유수방향("flow_direction", "E004"),
	호수("lake", "E005", E005Lake.class),
	용수로("aqueduct", "E006"),
	폭포("waterfall", "E007"),
	해안선("coastline", "E008", E008Coastline.class),
	등심선("water_contour_line", "E009"),

	// F타입 - 지형
	등고선("contour_line", "F001", F001Contour.class),
	표고점("elevation_point", "F002", F002ElevationPoint.class),
	절토("scarp", "F003"),
	옹벽("retaining_wall", "F004"),
	동굴입구("cave_entrance", "F005"),

	// G타입 - 경계
	시도_행정경계("province_administrative_boundary", "G001", G001AdministrativeBoundary.class),
	시군구_행정경계("district_administrative_boundary", "G010"),
	읍면동_행정경계("eup_myeon_dong_administrative_boundary", "G011"),
	수부지형경계("watery_boundary", "G002"),
	기타경계("other_boundaries", "G003"),

	// H타입 - 주기
	도곽선("map_boundary", "H001", H001MapBoundary.class),
	기준점("datum_point", "H002"),
	격자("grid", "H003"),
	지명("place_name", "H004"),
	산("mountain", "H005");



	private final @Getter String englishName;
	private final @Getter String layerName;
	private final @Getter String layerNameHeader;
	private final @Getter Class<? extends ScjdElement> elementClass;
	private final @Getter Constructor<? extends ScjdElement> elementConstructor;
	private final @Getter SimpleFeatureType keyFeatureType, nameFeatureType, osmFeatureType;

	LayerDataType(
			String englishName, String layerNameHeader, Class<? extends ScjdElement> elementClass
	) {
		try {
			this.englishName = englishName;
			this.layerName = layerNameHeader + "0000";
			this.layerNameHeader = layerNameHeader;
			this.elementClass = elementClass;
			if(elementClass != null) {
				this.elementConstructor = elementClass.getConstructor(SimpleFeature.class);
				this.keyFeatureType = ScjdElement.getSimpleFeatureType(elementClass, englishName, ColumnStoredType.KEY);
				this.nameFeatureType = ScjdElement.getSimpleFeatureType(elementClass, englishName, ColumnStoredType.NAME);
				this.osmFeatureType = ScjdElement.getOsmSimpleFeatureType(elementClass, englishName);
			}
			else {
				this.elementConstructor = null;
				this.keyFeatureType = null;
				this.nameFeatureType = null;
				this.osmFeatureType = null;
			}
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	LayerDataType(String englishName, String layerNameHeader) {
		this(englishName, layerNameHeader, null);
	}

	public boolean hasElementClass() {
		return this.elementClass != null;
	}

	public SimpleFeatureType getScjdFeatureType(ColumnStoredType type) {
		return type == ColumnStoredType.KEY ? keyFeatureType : nameFeatureType;
	}

	@SuppressWarnings("unchecked")
	public <E extends ScjdElement> E toElementObject(SimpleFeature feature) {
		try {
			return (E) this.elementConstructor.newInstance(feature);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public SimpleFeature toOsmStyleFeature(SimpleFeature feature, String id) {
		return toElementObject(feature).toOsmStyleFeature(this.osmFeatureType, id);
	}

	public Category getCategory() {
		return Category.valueOf(layerNameHeader.charAt(0));
	}

	private static final Pattern LAYER_TYPE_PATTERN = Pattern.compile("([A-H]\\d{3})\\d{4}");

	public static String findLayerTypeString(String name) {
		if(name == null) return null;
		Matcher matcher = LAYER_TYPE_PATTERN.matcher(name);
		if(matcher.find()) {
			return matcher.group();
		}
		return null;
	}

	public static LayerDataType fromLayerName(String layerName) {
		if(layerName == null) return null;
		Matcher matcher = LAYER_TYPE_PATTERN.matcher(layerName);
		if(!matcher.find()) return null;
		String match = matcher.group(1);
		for (LayerDataType t : values()) {
			if (match.startsWith(t.layerNameHeader)) {
				return t;
			}
		}
		return null;
	}

	public enum Category {
		교통('A'), 건물('B'), 시설('C'), 식생('D'), 수계('E'), 지형('F'), 경계('G'), 주기('H');
		private final char character;
		Category(char c) {
			this.character = c;
		}
		public static Category valueOf(char firstChar) {
			for(Category c : values()) {
				if(c.character == firstChar) return c;
			}
			return null;
		}
	}
}
