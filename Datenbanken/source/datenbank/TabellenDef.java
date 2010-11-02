package datenbank;


public class TabellenDef {

	private String schemaName;

	public TabellenDef(String schemaName) {
		this.schemaName = schemaName;
	}

	public String buildCreateTableStatement() {
		String sql = "";
		for (String statement : sqlStatemens)
			sql += statement;
		return sql;
	}

	private final String[] sqlStatemens = new String[] {
	    "CREATE TABLE "
	        + schemaName
	        + ".BUNDESLAND ( ID BIGINT  NOT NULL  GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1, NO CACHE ) , "
	        + "NAME VARCHAR (255)  NOT NULL  , "
	        + "CONSTRAINT CC1288606507352 PRIMARY KEY ( ID)  ) ;",

	    "CREATE TABLE "
	        + schemaName
	        + ".WAHLKREIS ( ID BIGINT  NOT NULL  GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1, NO CACHE ) , "
	        + "BUNDESLANDID BIGINT  NOT NULL , "
	        + "NAME VARCHAR (255)  , "
	        + "CONSTRAINT CC1288606603901 PRIMARY KEY ( ID) , "
	        + "CONSTRAINT CC1288606617285 FOREIGN KEY (BUNDESLANDID) REFERENCES "
	        + schemaName
	        + ".BUNDESLAND (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION  ) "
	        + "ORGANIZE BY DIMENSIONS ( BUNDESLANDID) ;",

	    "CREATE TABLE "
	        + schemaName
	        + ".WAHLBEZIRK ( ID BIGINT  NOT NULL  GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1, NO CACHE ) , "
	        + "WAHLKREISID BIGINT  NOT NULL  , "
	        + "CONSTRAINT CC1288606788792 PRIMARY KEY ( ID) , "
	        + "CONSTRAINT CC1288606799462 FOREIGN KEY (WAHLKREISID) REFERENCES "
	        + schemaName
	        + ".WAHLKREIS (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION  ) "
	        + "ORGANIZE BY DIMENSIONS ( WAHLKREISID) ;",

	    "CREATE TABLE "
	        + schemaName
	        + ".PARTEI ( ID BIGINT  NOT NULL  GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1, NO CACHE ) , "
	        + "NAME VARCHAR (255)  NOT NULL , "
	        + "KUERZEL VARCHAR (63)  NOT NULL  , "
	        + "CONSTRAINT CC1288606983948 PRIMARY KEY ( ID)  ) ;",

	    "CREATE TABLE "
	        + schemaName
	        + ".LANDESLISTE ( ID BIGINT  NOT NULL  GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1, NO CACHE ) , "
	        + "PARTEIID BIGINT  NOT NULL , "
	        + "BUNDESLANDID BIGINT  NOT NULL  , "
	        + "CONSTRAINT CC1288607097080 PRIMARY KEY ( ID) , "
	        + "CONSTRAINT CC1288607102228 FOREIGN KEY (PARTEIID) REFERENCES "
	        + schemaName
	        + ".PARTEI (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION , CONSTRAINT CC1288607115815 FOREIGN KEY (BUNDESLANDID) REFERENCES "
	        + schemaName
	        + ".BUNDESLAND (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION  ) "
	        + "ORGANIZE BY DIMENSIONS ( PARTEIID) ;",

	    "CREATE TABLE "
	        + schemaName
	        + ".DIREKTMANDAT ( ID BIGINT  NOT NULL  GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1, NO CACHE ) , "
	        + "PARTEIID BIGINT  NOT NULL , "
	        + "WAHLKREISID BIGINT  NOT NULL  , "
	        + "CONSTRAINT CC1288607225718 PRIMARY KEY ( ID) , "
	        + "CONSTRAINT CC1288607232192 FOREIGN KEY (PARTEIID) REFERENCES "
	        + schemaName
	        + ".PARTEI (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION , CONSTRAINT CC1288607245171 FOREIGN KEY (WAHLKREISID) REFERENCES "
	        + schemaName
	        + ".WAHLKREIS (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION  ) "
	        + "ORGANIZE BY DIMENSIONS ( PARTEIID) ;",

	    "CREATE TABLE "
	        + schemaName
	        + ".KANDIDAT ( ID BIGINT  NOT NULL  GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1, NO CACHE ) , "
	        + "PARTEIID BIGINT  NOT NULL , "
	        + "LANDESLISTEID BIGINT  NOT NULL ,"
	        + "NAME VARCHAR (255)  NOT NULL , "
	        + "VORNAME VARCHAR (255)  NOT NULL , "
	        + "BERUF VARCHAR (255) , "
	        + "GEBURTSDATUM DATE , "
	        + "GEBURTSORT VARCHAR (255) ,"
	        + "ANSCHRIFT VARCHAR (2047)  , "
	        + "LISTENPLATZ INTEGER ,"
	        + "CONSTRAINT CC1288607383356 PRIMARY KEY ( ID) , "
	        + "CONSTRAINT CC1288607389830 FOREIGN KEY (PARTEIID) REFERENCES "
	        + schemaName
	        + ".PARTEI (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION , CONSTRAINT CC1288607400625 FOREIGN KEY (LANDESLISTEID) REFERENCES "
	        + schemaName
	        + ".LANDESLISTE (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION  ) "
	        + "ORGANIZE BY DIMENSIONS ( PARTEIID) ;",

	    "CREATE TABLE "
	        + schemaName
	        + ".STIMME ( ID BIGINT  NOT NULL  GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1, NO CACHE ) , "
	        + "KANDIDATID BIGINT , "
	        + "PARTEIID BIGINT , "
	        + "WAHLBEZIRKID BIGINT  NOT NULL , "
	        + "GUELTIG SMALLINT  NOT NULL  WITH DEFAULT 0  , "
	        + "CONSTRAINT CC1288610679447 PRIMARY KEY ( ID) , "
	        + "CONSTRAINT CC1288610689165 FOREIGN KEY (KANDIDATID) REFERENCES "
	        + schemaName
	        + ".KANDIDAT (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION , "
	        + "CONSTRAINT CC1288610700714 FOREIGN KEY (PARTEIID) REFERENCES "
	        + schemaName
	        + ".PARTEI (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION , "
	        + "CONSTRAINT CC1288610713433 FOREIGN KEY (WAHLBEZIRKID) REFERENCES "
	        + schemaName
	        + ".WAHLBEZIRK (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION  ) "
	        + "ORGANIZE BY DIMENSIONS ( KANDIDATID, PARTEIID, WAHLBEZIRKID) ;",

	    "CREATE TABLE "
	        + schemaName
	        + ".WAHLERGEBNIS1 ( ID BIGINT  NOT NULL  GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1, NO CACHE ) , "
	        + "KANDIDATID BIGINT  NOT NULL , "
	        + "WAHLBEZIRKID BIGINT  NOT NULL , "
	        + "JAHR INTEGER  NOT NULL  , "
	        + "CONSTRAINT CC1288610976900 PRIMARY KEY ( ID) , "
	        + "CONSTRAINT CC1288610983160 FOREIGN KEY (KANDIDATID) REFERENCES "
	        + schemaName
	        + ".KANDIDAT (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION , "
	        + "CONSTRAINT CC1288610994045 FOREIGN KEY (WAHLBEZIRKID) REFERENCES "
	        + schemaName
	        + ".WAHLBEZIRK (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION  ) "
	        + "ORGANIZE BY DIMENSIONS ( KANDIDATID, WAHLBEZIRKID) ;"
	        + "COMMENT ON TABLE " + schemaName
	        + ".WAHLERGEBNIS1 IS 'Wahlergebnis 1. Stimme';",

	    "CREATE TABLE "
	        + schemaName
	        + ".WAHLERGEBNIS2 ( ID BIGINT  NOT NULL  GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1, NO CACHE ) , "
	        + "PARTEIID BIGINT  NOT NULL , "
	        + "WAHLBEZIRKID BIGINT  NOT NULL ,"
	        + "JAHR INTEGER  NOT NULL  , "
	        + "CONSTRAINT CC1288610976900 PRIMARY KEY ( ID) , "
	        + "CONSTRAINT CC1288610983160 FOREIGN KEY (PARTEIID) REFERENCES "
	        + schemaName
	        + ".PARTEI (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION , "
	        + "CONSTRAINT CC1288610994045 FOREIGN KEY (WAHLBEZIRKID) REFERENCES "
	        + schemaName
	        + ".WAHLBEZIRK (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION  ) "
	        + "ORGANIZE BY DIMENSIONS ( PARTEIID, WAHLBEZIRKID) ;"
	        + "COMMENT ON TABLE " + schemaName
	        + ".WAHLERGEBNIS2 IS 'Wahlergebnis 2. Stimme';",

	};

}
