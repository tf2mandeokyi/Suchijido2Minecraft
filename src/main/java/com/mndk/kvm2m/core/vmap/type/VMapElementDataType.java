package com.mndk.kvm2m.core.vmap.type;

import com.google.gson.JsonObject;
import com.mndk.kvm2m.core.db.common.TableColumn;
import com.mndk.kvm2m.core.db.common.TableColumns;
import com.mndk.kvm2m.core.vmap.elem.VMapElement;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// TODO: Migrate these messy columns and functions to classes
public enum VMapElementDataType {
	
	// A타입 - 교통
	도로경계("road_boundary", "A001", new TableColumns(), (e, j) -> {
		j.addProperty("highway", "unclassified");
	}),
	도로중심선("road_centerline", "A002", new TableColumns(
			new TableColumn("RDNU", "도로번호", new TableColumn.VarCharType(30)),
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("RDDV", "도로구분", new TableColumn.VarCharType(6), true),
			new TableColumn("STPT", "시점", new TableColumn.VarCharType(100)),
			new TableColumn("EDPT", "종점", new TableColumn.VarCharType(100)),
			new TableColumn("PVQT", "포장재질", new TableColumn.VarCharType(6), true),
			new TableColumn("DVYN", "분리대유무", new TableColumn.VarCharType(6), true),
			new TableColumn("RDLN", "차로수", new TableColumn.NumericType(2)),
			new TableColumn("RVWD", "도로폭", new TableColumn.NumericType(5,2)),
			new TableColumn("ONSD", "일방통행", new TableColumn.VarCharType(6), true),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50)),
			new TableColumn("RDNM", "도로명", new TableColumn.VarCharType(30))
	), (e, j) -> {
		if(e.getData("도로구분") instanceof String) {
			switch ((String) e.getData("도로구분")) {
				case "고속국도": j.addProperty("highway", "motorway"); break;
				case "일반국도": j.addProperty("highway", "trunk"); break;
				case "지방도": j.addProperty("highway", "primary"); break;
				case "특별시도":
				case "광역시도":
				case "시도": j.addProperty("highway", "secondary"); break;
				case "군도":
				case "면리간도로": j.addProperty("highway", "tertiary"); break;
				case "소로": j.addProperty("highway", "residental"); break;
				case "미분류":
				default: j.addProperty("highway", "unclassified");
			}
		}
		else { j.addProperty("highway", "unclassified"); }

		j.addProperty("lanes", (Number) e.getData("차로수"));
		j.addProperty("width", (Number) e.getData("도로폭"));
		if("일방통행".equals(e.getData("일방통행"))) {
			j.addProperty("oneway", "yes");
		}
	}),
	보도("sidewalk", "A003", new TableColumns(
			new TableColumn("WIDT", "폭", new TableColumn.NumericType(5,2)),
			new TableColumn("QUAL", "재질", new TableColumn.VarCharType(6), true),
			new TableColumn("BYYN", "자전거도로유무", new TableColumn.VarCharType(6), true),
			new TableColumn("KIND", "종류", new TableColumn.VarCharType(6), true)
	), (e, j) -> {
		j.addProperty("highway", "pedestrian");
		if("유".equals(e.getData("자전거도로유무"))) {
			j.addProperty("bicycle", "yes");
		}
	}),
	횡단보도("crosswalk", "A004", new TableColumns(), (e, j) -> {
		j.addProperty("highway", "crossing");
	}),
	안전지대("safe_zone", "A005", new TableColumns(
			new TableColumn("STRU", "구조", new TableColumn.VarCharType(6), true),
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100))
	), (e, j) -> {
		if("교통섬".equals(e.getData("구조"))) {
			j.addProperty("highway", "crossing");
			j.addProperty("crossing:island", "yes");
		}
	}),
	육교("pedestrian_overpass", "A006", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("LENG", "연장", new TableColumn.NumericType(7,2)),
			new TableColumn("WIDT", "폭", new TableColumn.NumericType(5,2)),
			new TableColumn("HEIG", "높이", new TableColumn.NumericType(5,2)),
			new TableColumn("TYPE", "형태", new TableColumn.VarCharType(6), true),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50))
	), (e, j) -> {
		j.addProperty("highway", "footway");
		j.addProperty("man_made", "bridge");
		j.addProperty("layer", 1);
	}),
	교량("bridge", "A007", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("KIND", "종류", new TableColumn.VarCharType(6), true),
			new TableColumn("LENG", "연장", new TableColumn.NumericType(7,2)),
			new TableColumn("WIDT", "폭", new TableColumn.NumericType(5,2)),
			new TableColumn("EYMD", "설치연도", new TableColumn.VarCharType(30)),
			new TableColumn("QUAL", "재질", new TableColumn.VarCharType(6), true),
			new TableColumn("RVNM", "하천명", new TableColumn.VarCharType(30)),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50))
	), (e, j) -> {
		j.addProperty("man_made", "bridge");
		j.addProperty("layer", 1);
	}),
	교차로("crossroad", "A008", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("KIND", "종류", new TableColumn.VarCharType(6), true)
	)),
	입체교차부("intersection_3d", "A009", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), true),
			new TableColumn("LENG", "연장", new TableColumn.NumericType(7,2)),
			new TableColumn("HEIG", "높이", new TableColumn.NumericType(5,2)),
			new TableColumn("PSLD", "통과하중", new TableColumn.NumericType(5,2)),
			new TableColumn("QUAL", "재질", new TableColumn.VarCharType(6), true),
			new TableColumn("SDWK", "보도", new TableColumn.VarCharType(6), true)
	)),
	인터체인지("interchange", "A010", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(6), true)
	)),
	터널("tunnel", "A011", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("LENG", "연장", new TableColumn.NumericType(7,2)),
			new TableColumn("WIDT", "폭", new TableColumn.NumericType(5,2)),
			new TableColumn("HEIG", "높이", new TableColumn.NumericType(5,2))
	)),
	터널입구("tunnel_entrance", "A012", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100))
	)),
	정거장("bus_station", "A013", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100))
	)),
	정류장("train_station", "A014", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(6), true)
	)),
	철도("railway", "A015", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), true),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(6), true),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50))
	)),
	철도경계("railway_boundary", "A016", new TableColumns()),
	철도중심선("railway_centerline", "A017", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), true),
			new TableColumn("STRU", "구조", new TableColumn.VarCharType(6), true),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(6), true),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50))
	)),
	철도전차대("railway_turntable", "A018", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), true),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(6), true),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50))
	)),
	승강장("platform", "A019", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100))
	)),
	승강장_지붕("platform_roof", "A020", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100))
	)),
	나루("river_port", "A021", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DSNA", "행선지", new TableColumn.VarCharType(50)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(6), true),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50))
	)),
	나루노선("ferry_route", "A022", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100))
	)),



	// B타입 - 건물
	건물("building", "B001", new TableColumns(
			// new TableColumn("BJCD", "법정동코드", new TableColumn.VarCharType(10)),
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(254)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(20)),
			new TableColumn("KIND", "종류", new TableColumn.VarCharType(6), true),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(6), true),
			new TableColumn("ANNO", "주기", new TableColumn.VarCharType(254)),
			new TableColumn("NMLY", "층수", new TableColumn.NumericType(3))
			// new TableColumn("RDNM", "도로명", new TableColumn.VarCharType(50)),
			// new TableColumn("BONU", "건물번호본번", new TableColumn.NumericType(4)),
			// new TableColumn("BUNU", "건물번호부번", new TableColumn.NumericType(4)),
			// new TableColumn("POST", "우편번호", new TableColumn.VarCharType(10))
	), (e, j) -> {
		j.remove("area");
		if(e.getData("종류") instanceof String) {
			switch ((String) e.getData("종류")) {
				case "일반주택":
				case "연립주택": j.addProperty("building", "residential"); break;
				case "아파트": j.addProperty("building", "apartments"); break;
				case "주택외건물":
				case "무벽건물":
				case "온실":
				case "공사중건물":
				case "가건물":
				case "미분류":
				default: j.addProperty("building", "yes");
			}
		}
		if(e.getData("명칭") instanceof String) {
			String name = (String) e.getData("명칭");
			if(name.length() != 0) j.addProperty("name", name);
		}
		j.addProperty("building:levels", (Number) e.getData("층수"));
	}),
	담장("wall", "B002", new TableColumns(
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), true),
			new TableColumn("QUAL", "재질", new TableColumn.VarCharType(6), true)
	)),



	// C타입 - 시설
	댐("dam", "C001", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(6), true),
			new TableColumn("WIDT", "폭", new TableColumn.NumericType(5,2)),
			new TableColumn("HEIG", "높이", new TableColumn.NumericType(5,2)),
			new TableColumn("MARA", "면적", new TableColumn.NumericType(9,2)),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(30))
	)),
	부두("wharf", "C002", new TableColumns(
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(6), true),
			new TableColumn("QUAL", "재질", new TableColumn.VarCharType(6), true),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(30))
	)),
	선착장("dock", "C003", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(50))
	)),
	선거("boat_stop", "C004", new TableColumns(
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(50)),
			new TableColumn("QUAL", "재질", new TableColumn.VarCharType(6), true),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(30))
	)),
	제방("embankment", "C005", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(8), true),
			new TableColumn("QUAL", "재질", new TableColumn.VarCharType(6), true),
			new TableColumn("UDDI", "상하구분", new TableColumn.VarCharType(6), true),
			new TableColumn("LENG", "연장", new TableColumn.NumericType(7,2)),
			new TableColumn("HEIG", "높이", new TableColumn.NumericType(5,2)),
			new TableColumn("SCLS", "통합코드", new TableColumn.VarCharType(8)),
			new TableColumn("FMTA", "제작정보", new TableColumn.VarCharType(9))
	)),
	수문("sluice_gate", "C006", new TableColumns(
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), true),
			new TableColumn("QUAL", "재질", new TableColumn.VarCharType(6), true)
	)),
	암거("culvert", "C007", new TableColumns()),
	잔교("pier", "C008", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(50)),
			new TableColumn("QUAL", "재질", new TableColumn.VarCharType(6), true),
			new TableColumn("SCLS", "통합코드", new TableColumn.VarCharType(8), true),
			new TableColumn("FMTA", "제작정보", new TableColumn.VarCharType(9))
	)),
	우물("well", "C009", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("TYPE", "형태", new TableColumn.VarCharType(6), true)
	)),
	관정("tubular_well", "C010", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(50))
	)),
	분수("fountain", "C011", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100))
	)),
	온천("hot_spring", "C012", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("PRIN", "주성분", new TableColumn.VarCharType(50)),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50))
	)),
	양식장("aquaculture", "C013", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(50))
	)),
	낚시터("fishing_ground", "C014", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100))
	)),
	해수욕장("seaside_resort", "C015", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100))
	)),
	등대("lighthouse", "C016", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(6), true),
			new TableColumn("TYPE", "형태", new TableColumn.VarCharType(6), true),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(30))
	)),
	저장조("storage_tank", "C017", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), true),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(50)),
			new TableColumn("MARA", "면적", new TableColumn.NumericType(9,2)),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(30))
	)),
	탱크("tank", "C018", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(50)),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(30))
	)),
	광산("mine", "C019", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("STAT", "상태", new TableColumn.VarCharType(6), true),
			new TableColumn("PRMR", "주생산광물", new TableColumn.VarCharType(50))
	)),
	적치장("mine_yard", "C020", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(50)),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(30))
	)),
	채취장("quarry", "C021", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), true)
	)),
	조명("lamp", "C022", new TableColumns(
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), true)
	)),
	전력주("utility_pole", "C023", new TableColumns(
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), true),
			new TableColumn("QUAL", "재질", new TableColumn.VarCharType(6), true)
	)),
	맨홀("manhole", "C024", new TableColumns(
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), true)
	)),
	소화전("fire_hydrant", "C025", new TableColumns(
			new TableColumn("TYPE", "형태", new TableColumn.VarCharType(6), true),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(30))
	)),
	관측소("observatory", "C026", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(6), true)
	)),
	야영지("campsite", "C027", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50))
	)),
	묘지("cemetery", "C028", new TableColumns()),
	묘지계("graveyard", "C029", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(30))
	)),
	유적지("historical_site", "C030", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50))
	)),
	문화재("cultural_heritage", "C031", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("TYPE", "형태", new TableColumn.VarCharType(6), true)
	)),
	성("castle", "C032", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("QUAL", "재질", new TableColumn.VarCharType(6), true)
	)),
	비석("tombstone", "C033", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), true),
			new TableColumn("HEIG", "높이", new TableColumn.NumericType(5,2)),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50))
	)),
	탑("tower", "C034", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), true),
			new TableColumn("QUAL", "재질", new TableColumn.VarCharType(6), true),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50))
	)),
	동상("statue", "C035", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("QUAL", "재질", new TableColumn.VarCharType(50))
	)),
	공중전화("public_telephone", "C036", new TableColumns()),
	우체통("mailbox", "C037", new TableColumns()),
	놀이시설("amusement_ride", "C038", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), true)
	)),
	계단("stairs", "C039", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("STRU", "구조", new TableColumn.VarCharType(6), true),
			new TableColumn("WIDT", "폭", new TableColumn.NumericType(7,2))
	)),
	게시판("notice_board", "C040", new TableColumns(
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), true)
	)),
	표지("sign", "C041", new TableColumns(
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(6), true)
	)),
	주유소("gas_station", "C042", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("KIND", "종류", new TableColumn.VarCharType(6), true)
	)),
	주차장("parking_lot", "C043", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), true),
			new TableColumn("PKNU", "수용량", new TableColumn.NumericType(5))
	)),
	휴게소("rest_area", "C044", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(6), true)
	)),
	지하도("underpass", "C045", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("LENG", "연장", new TableColumn.NumericType(7,2)),
			new TableColumn("WIDT", "폭", new TableColumn.NumericType(5,2)),
			new TableColumn("HEIG", "높이", new TableColumn.NumericType(5,2)),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50))
	)),
	지하도입구("underpass_entrance", "C046", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(6), true),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(30))
	)),
	지하환기구("underground_ventilation", "C047", new TableColumns(
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(6), true),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(30))
	)),
	굴뚝("chimney", "C048", new TableColumns()),
	신호등("traffic_light", "C049", new TableColumns(
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(6), true)
	)),
	차단기("crossing_gate", "C050", new TableColumns(
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), true),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(30)),
			new TableColumn("BJCD", "법정동코드", new TableColumn.VarCharType(10))
	)),
	도로반사경("road_reflector", "C051", new TableColumns()),
	도로분리대("road_separator", "C052", new TableColumns(
			new TableColumn("STRU", "구조", new TableColumn.VarCharType(6), true)
	)),
	방지책("rockfall_protection", "C053", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100), true),
			new TableColumn("QUAL", "재질", new TableColumn.VarCharType(6), true)
	)),
	요금징수소("tollgate", "C054", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100))
	)),
	헬기장("heliport", "C055", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(50))
	)),


	
	// D타입 - 식생
	경지계("ground_boundaries", "D001", new TableColumns(
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), true)
	)),
	지류계("waterflow_boundary", "D002", new TableColumns(
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), true)
	)),
	독립수("tree", "D003", new TableColumns(
			new TableColumn("MNNU", "관리번호", new TableColumn.VarCharType(30)),
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), true),
			new TableColumn("RADI", "직경", new TableColumn.NumericType(7,2)),
			new TableColumn("HEIG", "높이", new TableColumn.NumericType(5,2)),
			new TableColumn("TRAG", "수령", new TableColumn.NumericType(5)),
			new TableColumn("TRKI", "수종", new TableColumn.VarCharType(50)),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50))
	)),
	목장("pasture", "D004", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(6), true),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50))
	)),


	
	// E타입 - 수계
	하천경계("river_boundary", "E001", new TableColumns(), -1),
	하천중심선("river_centerline", "E002", new TableColumns(
			new TableColumn("RVNU", "하천번호", new TableColumn.NumericType(9)),
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), true),
			new TableColumn("TYPE", "형태", new TableColumn.VarCharType(6), true),
			new TableColumn("STAT", "상태", new TableColumn.VarCharType(6), true)
	), -1),
	실폭하천("river", "E003", new TableColumns(), -1, (e, j) -> {
		j.addProperty("natural", "water");
		j.addProperty("water", "river");
	}),
	유수방향("flow_direction", "E004", new TableColumns(
			new TableColumn("ANGL", "방향각도", new TableColumn.NumericType(3))
	), -1),
	호수("lake", "E005", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(50)),
			new TableColumn("MARA", "면적", new TableColumn.NumericType(11,2)),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(30))
	), -1, (e, j) -> {
		j.addProperty("natural", "water");
		j.addProperty("water", "lake");
		if(e.getData("명칭") instanceof String) {
			j.addProperty("name", (String) e.getData("명칭"));
		}
	}),
	용수로("aqueduct", "E006", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("TYPE", "형태", new TableColumn.VarCharType(6), true),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(6), true),
			new TableColumn("LENG", "연장", new TableColumn.NumericType(7,2)),
			new TableColumn("WIDT", "폭", new TableColumn.NumericType(5,2)),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(30))
	), -1),
	폭포("waterfall", "E007", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("HEIG", "높이", new TableColumn.NumericType(5,2)),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50))
	), -1),
	해안선("coastline", "E008", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), true)
	), -1, (e, j) -> {
		j.addProperty("natural", "coastline");
	}),
	등심선("water_contour_line", "E009", new TableColumns(
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(50)),
			new TableColumn("CONT", "등심수치", new TableColumn.NumericType(7,2))
	), -1),



	// F타입 - 지형
	등고선("contour_line", "F001", new TableColumns(
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), true),
			new TableColumn("CONT", "등고수치", new TableColumn.NumericType(7,2))
	)),
	표고점("elevation_point", "F002", new TableColumns(
			new TableColumn("NUME", "수치", new TableColumn.NumericType(7,2))
	)),
	절토("scarp", "F003", new TableColumns(
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), true),
			new TableColumn("UDDI", "상하구분", new TableColumn.VarCharType(6), true)
	)),
	옹벽("retaining_wall", "F004", new TableColumns(
			new TableColumn("KIND", "종류", new TableColumn.VarCharType(6), true),
			new TableColumn("UDDI", "상하구분", new TableColumn.VarCharType(6), true),
			new TableColumn("LENG", "연장", new TableColumn.NumericType(7,2)),
			new TableColumn("HEIG", "높이", new TableColumn.NumericType(5,2)),
			new TableColumn("QUAL", "재질", new TableColumn.VarCharType(6), true)
	)),
	동굴입구("cave_entrance", "F005", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("TYPE", "형태", new TableColumn.VarCharType(6), true),
			new TableColumn("LENG", "연장", new TableColumn.NumericType(7,2)),
			new TableColumn("HEIG", "높이", new TableColumn.NumericType(5,2))
	)),



	// G타입 - 경계
	시도_행정경계("province_administrative_boundary", "G001", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), true)
	)),
	시군구_행정경계("district_administrative_boundary", "G010", new TableColumns(
			new TableColumn("BJCD", "법정동코드", new TableColumn.VarCharType(10), true),
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), true)
	)),
	읍면동_행정경계("eup_myeon_dong_administrative_boundary", "G011", new TableColumns(
			new TableColumn("BJCD", "법정동코드", new TableColumn.VarCharType(10), true),
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(20), true)
	)),
	수부지형경계("watery_boundary", "G002", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("TYPE", "형태", new TableColumn.VarCharType(6), true)
	)),
	기타경계("other_boundaries", "G003", new TableColumns(
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(100))
	)),



	// H타입 - 주기
	도곽선("map_boundary", "H001", new TableColumns(
			new TableColumn("DYCD", "도엽코드", new TableColumn.VarCharType(10), true),
			new TableColumn("DYNM", "도엽명", new TableColumn.VarCharType(30), true)/*,
			new TableColumn("PRDT", "제작방법", new TableColumn.VarCharType(6), true),
			new TableColumn("SCAL", "축척", new TableColumn.VarCharType(6), true),
			new TableColumn("MCOM", "작업기관", new TableColumn.VarCharType(50)),
			new TableColumn("MYMD", "제작년도", new TableColumn.VarCharType(4)),
			new TableColumn("MZON", "지구명", new TableColumn.VarCharType(20)),
			new TableColumn("PYMD", "촬영년도", new TableColumn.VarCharType(4)),
			new TableColumn("SCLS", "통합코드", new TableColumn.VarCharType(9), true)*/
	)),
	기준점("datum_point", "H002", new TableColumns(
			new TableColumn("DYCD", "도엽코드", new TableColumn.VarCharType(10)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), true),
			new TableColumn("CLAS", "등급", new TableColumn.VarCharType(6), true),
			new TableColumn("NUMB", "번호", new TableColumn.VarCharType(25)),
			new TableColumn("COOR", "좌표", new TableColumn.VarCharType(40)),
			new TableColumn("ALTI", "표고", new TableColumn.NumericType(7,2)),
			new TableColumn("BJCD", "법정동코드", new TableColumn.VarCharType(10), true),
			new TableColumn("PRDT", "제작방법", new TableColumn.VarCharType(6), true),
			new TableColumn("SCAL", "축척", new TableColumn.VarCharType(6), true),
			new TableColumn("MCOM", "작업기관", new TableColumn.VarCharType(50)),
			new TableColumn("MYMD", "제작년도", new TableColumn.VarCharType(4)),
			new TableColumn("MZON", "지구명", new TableColumn.VarCharType(20)),
			new TableColumn("PYMD", "촬영년도", new TableColumn.VarCharType(4)),
			new TableColumn("SCLS", "통합코드", new TableColumn.VarCharType(8), true)
	)),
	격자("grid", "H003", new TableColumns()),
	지명("place_name", "H004", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), true),
			new TableColumn("TYPE", "형태", new TableColumn.VarCharType(6), true)
	)),
	산("mountain", "H005", new TableColumns(
			new TableColumn("MTNM", "산명", new TableColumn.VarCharType(30)),
			new TableColumn("HEIG", "높이", new TableColumn.NumericType(7,2))
	));



	private final @Getter String englishName;
	private final @Getter String layerNameHeader;
	private final @Getter int priority;
	private final @Getter TableColumns columns;
	private final @Getter @Nullable BiConsumer<VMapElement, JsonObject> jsonPropertyFunction;




	VMapElementDataType(String englishName, String layerNameHeader, TableColumns columns) {
		this(englishName, layerNameHeader, columns, 0);
	}



	VMapElementDataType(String englishName, String layerNameHeader, TableColumns columns, int priority) {
		this(englishName, layerNameHeader, columns, priority, null);
	}



	VMapElementDataType(String englishName,
						String layerNameHeader,
						TableColumns columns,
						BiConsumer<VMapElement, JsonObject> jsonPropertyFunction) {
		this(englishName, layerNameHeader, columns, 0, jsonPropertyFunction);
	}



	VMapElementDataType(String englishName,
						String layerNameHeader,
						TableColumns columns,
						int priority,
						BiConsumer<VMapElement, JsonObject> jsonPropertyFunction) {
		this.englishName = englishName;
		this.layerNameHeader = layerNameHeader;
		this.columns = columns;
		this.columns.setParentType(this);
		this.priority = priority;
		this.jsonPropertyFunction = jsonPropertyFunction;
	}



	public Category getCategory() {
		return Category.valueOf(layerNameHeader.charAt(0));
	}



	private static final Pattern layerTypePattern = Pattern.compile("^([A-H]\\d{3})");



	public static VMapElementDataType fromLayerName(String layerName) {
		if(layerName == null) return null;
		Matcher matcher = layerTypePattern.matcher(layerName);
		if(matcher.find()) {
			String match = matcher.group(1);
			for (VMapElementDataType t : values()) {
				if (match.startsWith(t.layerNameHeader)) {
					return t;
				}
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
