package com.mndk.kvm2m.core.vmap;

import com.mndk.kvm2m.core.db.common.TableColumn;
import com.mndk.kvm2m.core.db.common.TableColumns;
import lombok.Getter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum VMapElementDataType {
	
	// A타입 - 교통
	도로경계("A001", new TableColumns()),
	도로중심선("A002", new TableColumns(
			new TableColumn("RDNU", "도로번호", new TableColumn.VarCharType(30)),
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("RDDV", "도로구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("STPT", "시점", new TableColumn.VarCharType(100)),
			new TableColumn("EDPT", "종점", new TableColumn.VarCharType(100)),
			new TableColumn("PVQT", "포장재질", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("DVYN", "분리대유무", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("RDLN", "차로수", new TableColumn.NumericType(2)),
			new TableColumn("RVWD", "도로폭", new TableColumn.NumericType(5,2)),
			new TableColumn("ONSD", "일방통행", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50)),
			new TableColumn("RDNM", "도로명", new TableColumn.VarCharType(30))
	)),
	보도("A003", new TableColumns(
			new TableColumn("WIDT", "폭", new TableColumn.NumericType(5,2)),
			new TableColumn("QUAL", "재질", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("BYYN", "자전거도로유무", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("KIND", "종류", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),
	횡단보도("A004", new TableColumns()),
	안전지대("A005", new TableColumns(
			new TableColumn("STRU", "구조", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100))
	)),
	육교("A006", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("LENG", "연장", new TableColumn.NumericType(7,2)),
			new TableColumn("WIDT", "폭", new TableColumn.NumericType(5,2)),
			new TableColumn("HEIG", "높이", new TableColumn.NumericType(5,2)),
			new TableColumn("TYPE", "형태", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50))
	)),
	교량("A007", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("KIND", "종류", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("LENG", "연장", new TableColumn.NumericType(7,2)),
			new TableColumn("WIDT", "폭", new TableColumn.NumericType(5,2)),
			new TableColumn("EYMD", "설치연도", new TableColumn.VarCharType(30)),
			new TableColumn("QUAL", "재질", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("RVNM", "하천명", new TableColumn.VarCharType(30)),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50))
	)),
	교차로("A008", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("KIND", "종류", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),
	입체교차부("A009", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("LENG", "연장", new TableColumn.NumericType(7,2)),
			new TableColumn("HEIG", "높이", new TableColumn.NumericType(5,2)),
			new TableColumn("PSLD", "통과하중", new TableColumn.NumericType(5,2)),
			new TableColumn("QUAL", "재질", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("SDWK", "보도", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),
	인터체인지("A010", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),
	터널("A011", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("LENG", "연장", new TableColumn.NumericType(7,2)),
			new TableColumn("WIDT", "폭", new TableColumn.NumericType(5,2)),
			new TableColumn("HEIG", "높이", new TableColumn.NumericType(5,2))
	)),
	터널입구("A012", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100))
	)),
	정거장("A013", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100))
	)),
	정류장("A014", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),
	철도("A015", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50))
	)),
	철도경계("A016", new TableColumns()),
	철도중심선("A017", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("STRU", "구조", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50))
	)),
	철도전차대("A018", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50))
	)),
	승강장("A019", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100))
	)),
	승강장_지붕("A020", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100))
	)),
	나루("A021", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DSNA", "행선지", new TableColumn.VarCharType(50)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50))
	)),
	나루노선("A022", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100))
	)),



	// B타입 - 건물
	건물("B001", new TableColumns(
			// new TableColumn("BJCD", "법정동코드", new TableColumn.VarCharType(10)),
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(254)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(20)),
			new TableColumn("KIND", "종류", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("ANNO", "주기", new TableColumn.VarCharType(254)),
			new TableColumn("NMLY", "층수", new TableColumn.NumericType(3))
			// new TableColumn("RDNM", "도로명", new TableColumn.VarCharType(50)),
			// new TableColumn("BONU", "건물번호본번", new TableColumn.NumericType(4)),
			// new TableColumn("BUNU", "건물번호부번", new TableColumn.NumericType(4)),
			// new TableColumn("POST", "우편번호", new TableColumn.VarCharType(10))
	)),
	담장("B002", new TableColumns(
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("QUAL", "재질", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),



	// C타입 - 시설
	댐("C001", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("WIDT", "폭", new TableColumn.NumericType(5,2)),
			new TableColumn("HEIG", "높이", new TableColumn.NumericType(5,2)),
			new TableColumn("MARA", "면적", new TableColumn.NumericType(9,2)),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(30))
	)),
	부두("C002", new TableColumns(
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("QUAL", "재질", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(30))
	)),
	선착장("C003", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(50))
	)),
	선거("C004", new TableColumns(
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(50)),
			new TableColumn("QUAL", "재질", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(30))
	)),
	제방("C005", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(8), TableColumn.NOT_NULL),
			new TableColumn("QUAL", "재질", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("UDDI", "상하구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("LENG", "연장", new TableColumn.NumericType(7,2)),
			new TableColumn("HEIG", "높이", new TableColumn.NumericType(5,2)),
			new TableColumn("SCLS", "통합코드", new TableColumn.VarCharType(8)),
			new TableColumn("FMTA", "제작정보", new TableColumn.VarCharType(9))
	)),
	수문("C006", new TableColumns(
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("QUAL", "재질", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),
	암거("C007", new TableColumns()),
	잔교("C008", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(50)),
			new TableColumn("QUAL", "재질", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("SCLS", "통합코드", new TableColumn.VarCharType(8), TableColumn.NOT_NULL),
			new TableColumn("FMTA", "제작정보", new TableColumn.VarCharType(9))
	)),
	우물("C009", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("TYPE", "형태", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),
	관정("C010", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(50))
	)),
	분수("C011", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100))
	)),
	온천("C012", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("PRIN", "주성분", new TableColumn.VarCharType(50)),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50))
	)),
	양식장("C013", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(50))
	)),
	낚시터("C014", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100))
	)),
	해수욕장("C015", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100))
	)),
	등대("C016", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("TYPE", "형태", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(30))
	)),
	저장조("C017", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(50)),
			new TableColumn("MARA", "면적", new TableColumn.NumericType(9,2)),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(30))
	)),
	탱크("C018", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(50)),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(30))
	)),
	광산("C019", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("STAT", "상태", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("PRMR", "주생산광물", new TableColumn.VarCharType(50))
	)),
	적치장("C020", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(50)),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(30))
	)),
	채취장("C021", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),
	조명("C022", new TableColumns(
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),
	전력주("C023", new TableColumns(
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("QUAL", "재질", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),
	맨홀("C024", new TableColumns(
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),
	소화전("C025", new TableColumns(
			new TableColumn("TYPE", "형태", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(30))
	)),
	관측소("C026", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),
	야영지("C027", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50))
	)),
	묘지("C028", new TableColumns()),
	묘지계("C029", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(30))
	)),
	유적지("C030", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50))
	)),
	문화재("C031", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("TYPE", "형태", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),
	성("C032", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("QUAL", "재질", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),
	비석("C033", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("HEIG", "높이", new TableColumn.NumericType(5,2)),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50))
	)),
	탑("C034", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("QUAL", "재질", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50))
	)),
	동상("C035", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("QUAL", "재질", new TableColumn.VarCharType(50))
	)),
	공중전화("C036", new TableColumns()),
	우체통("C037", new TableColumns()),
	놀이시설("C038", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),
	계단("C039", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("STRU", "구조", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("WIDT", "폭", new TableColumn.NumericType(7,2))
	)),
	게시판("C040", new TableColumns(
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),
	표지("C041", new TableColumns(
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),
	주유소("C042", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("KIND", "종류", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),
	주차장("C043", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("PKNU", "수용량", new TableColumn.NumericType(5))
	)),
	휴게소("C044", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),
	지하도("C045", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("LENG", "연장", new TableColumn.NumericType(7,2)),
			new TableColumn("WIDT", "폭", new TableColumn.NumericType(5,2)),
			new TableColumn("HEIG", "높이", new TableColumn.NumericType(5,2)),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50))
	)),
	지하도입구("C046", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(30))
	)),
	지하환기구("C047", new TableColumns(
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(30))
	)),
	굴뚝("C048", new TableColumns()),
	신호등("C049", new TableColumns(
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),
	차단기("C050", new TableColumns(
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(30)),
			new TableColumn("BJCD", "법정동코드", new TableColumn.VarCharType(10))
	)),
	도로반사경("C051", new TableColumns()),
	도로분리대("C052", new TableColumns(
			new TableColumn("STRU", "구조", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),
	방지책("C053", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100), TableColumn.NOT_NULL),
			new TableColumn("QUAL", "재질", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),
	요금징수소("C054", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100))
	)),
	헬기장("C055", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(50))
	)),


	
	// D타입 - 식생
	경지계("D001", new TableColumns(
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),
	지류계("D002", new TableColumns(
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),
	독립수("D003", new TableColumns(
			new TableColumn("MNNU", "관리번호", new TableColumn.VarCharType(30)),
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("RADI", "직경", new TableColumn.NumericType(7,2)),
			new TableColumn("HEIG", "높이", new TableColumn.NumericType(5,2)),
			new TableColumn("TRAG", "수령", new TableColumn.NumericType(5)),
			new TableColumn("TRKI", "수종", new TableColumn.VarCharType(50)),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50))
	)),
	목장("D004", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50))
	)),


	
	// E타입 - 수계
	하천경계("E001", new TableColumns(), -1),
	하천중심선("E002", new TableColumns(
			new TableColumn("RVNU", "하천번호", new TableColumn.NumericType(9)),
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("TYPE", "형태", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("STAT", "상태", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	), -1),
	실폭하천("E003", new TableColumns(), -1),
	유수방향("E004", new TableColumns(
			new TableColumn("ANGL", "방향각도", new TableColumn.NumericType(3))
	), -1),
	호수("E005", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(50)),
			new TableColumn("MARA", "면적", new TableColumn.NumericType(11,2)),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(30))
	), -1),
	용수로("E006", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("TYPE", "형태", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("LENG", "연장", new TableColumn.NumericType(7,2)),
			new TableColumn("WIDT", "폭", new TableColumn.NumericType(5,2)),
			new TableColumn("MNGT", "관리기관", new TableColumn.VarCharType(30))
	), -1),
	폭포("E007", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("HEIG", "높이", new TableColumn.NumericType(5,2)),
			new TableColumn("REST", "기타", new TableColumn.VarCharType(50))
	), -1),
	해안선("E008", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	), -1),
	등심선("E009", new TableColumns(
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(50)),
			new TableColumn("CONT", "등심수치", new TableColumn.NumericType(7,2))
	), -1),



	// F타입 - 지형
	등고선("F001", new TableColumns(
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("CONT", "등고수치", new TableColumn.NumericType(7,2))
	)),
	표고점("F002", new TableColumns(
			new TableColumn("NUME", "수치", new TableColumn.NumericType(7,2))
	)),
	절토("F003", new TableColumns(
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("UDDI", "상하구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),
	옹벽("F004", new TableColumns(
			new TableColumn("KIND", "종류", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("UDDI", "상하구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("LENG", "연장", new TableColumn.NumericType(7,2)),
			new TableColumn("HEIG", "높이", new TableColumn.NumericType(5,2)),
			new TableColumn("QUAL", "재질", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),
	동굴입구("F005", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("TYPE", "형태", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("LENG", "연장", new TableColumn.NumericType(7,2)),
			new TableColumn("HEIG", "높이", new TableColumn.NumericType(5,2))
	)),



	// G타입 - 경계
	시도_행정경계("G001", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),
	시군구_행정경계("G010", new TableColumns(
			new TableColumn("BJCD", "법정동코드", new TableColumn.VarCharType(10), TableColumn.NOT_NULL),
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),
	읍면동_행정경계("G011", new TableColumns(
			new TableColumn("BJCD", "법정동코드", new TableColumn.VarCharType(10), TableColumn.NOT_NULL),
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(20), TableColumn.NOT_NULL)
	)),
	수부지형경계("G002", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("TYPE", "형태", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),
	기타경계("G003", new TableColumns(
			new TableColumn("SERV", "용도", new TableColumn.VarCharType(100))
	)),



	// H타입 - 주기
	도곽선("H001", new TableColumns(
			new TableColumn("DYCD", "도엽코드", new TableColumn.VarCharType(10), TableColumn.NOT_NULL),
			new TableColumn("DYNM", "도엽명", new TableColumn.VarCharType(30), TableColumn.NOT_NULL)/*,
			new TableColumn("PRDT", "제작방법", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("SCAL", "축척", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("MCOM", "작업기관", new TableColumn.VarCharType(50)),
			new TableColumn("MYMD", "제작년도", new TableColumn.VarCharType(4)),
			new TableColumn("MZON", "지구명", new TableColumn.VarCharType(20)),
			new TableColumn("PYMD", "촬영년도", new TableColumn.VarCharType(4)),
			new TableColumn("SCLS", "통합코드", new TableColumn.VarCharType(9), TableColumn.NOT_NULL)*/
	)),
	기준점("H002", new TableColumns(
			new TableColumn("DYCD", "도엽코드", new TableColumn.VarCharType(10)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("CLAS", "등급", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("NUMB", "번호", new TableColumn.VarCharType(25)),
			new TableColumn("COOR", "좌표", new TableColumn.VarCharType(40)),
			new TableColumn("ALTI", "표고", new TableColumn.NumericType(7,2)),
			new TableColumn("BJCD", "법정동코드", new TableColumn.VarCharType(10), TableColumn.NOT_NULL),
			new TableColumn("PRDT", "제작방법", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("SCAL", "축척", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("MCOM", "작업기관", new TableColumn.VarCharType(50)),
			new TableColumn("MYMD", "제작년도", new TableColumn.VarCharType(4)),
			new TableColumn("MZON", "지구명", new TableColumn.VarCharType(20)),
			new TableColumn("PYMD", "촬영년도", new TableColumn.VarCharType(4)),
			new TableColumn("SCLS", "통합코드", new TableColumn.VarCharType(8), TableColumn.NOT_NULL)
	)),
	격자("H003", new TableColumns()),
	지명("H004", new TableColumns(
			new TableColumn("NAME", "명칭", new TableColumn.VarCharType(100)),
			new TableColumn("DIVI", "구분", new TableColumn.VarCharType(6), TableColumn.NOT_NULL),
			new TableColumn("TYPE", "형태", new TableColumn.VarCharType(6), TableColumn.NOT_NULL)
	)),
	산("H005", new TableColumns(
			new TableColumn("MTNM", "산명", new TableColumn.VarCharType(30)),
			new TableColumn("HEIG", "높이", new TableColumn.NumericType(7,2))
	));



	private final @Getter String layerNameHeader;
	private final @Getter int priority;
	private final @Getter TableColumns columns;
	



	VMapElementDataType(String layerNameHeader, TableColumns columns) {
		this(layerNameHeader, columns, 0);
	}



	VMapElementDataType(String layerNameHeader, TableColumns columns, int priority) {
		this.layerNameHeader = layerNameHeader;
		this.columns = columns;
		this.columns.setParentType(this);
		this.priority = priority;
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
