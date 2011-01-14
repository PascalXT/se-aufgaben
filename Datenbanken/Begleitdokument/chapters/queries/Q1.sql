WITH 

ZweitStimmenNachBundesland AS (
	SELECT w2.ParteiID, wk.BundeslandID, sum(w2.Anzahl) as
		AnzahlStimmen
	FROM zweitStimmenNachWahlkreis w2, Wahlkreis wk
	WHERE w2.WahlkreisID = wk.ID AND w2.Jahr = 2009
	GROUP BY wk.BundeslandID, w2.ParteiID), 

ZweitStimmenNachPartei AS (
	SELECT ParteiID, SUM(AnzahlStimmen) AS AnzahlStimmen
	FROM ZweitStimmenNachBundesland
	GROUP BY ParteiID), 

MaxErststimmenNachWahlkreis AS (
	SELECT k.DMWahlkreisID AS WahlkreisID, MAX(v.Anzahl) AS
		MaxStimmen
	FROM erstStimmenNachWahlkreis v, Kandidat k
	WHERE v.KandidatID = k.ID AND v.Jahr = 2009
	GROUP BY k.DMWahlkreisID), 

Direktmandate AS (
	SELECT k.ID AS KandidatID, k.ParteiID, k.DMWahlkreisID
	FROM MaxErststimmenNachWahlkreis e, erstStimmenNachWahlkreis v,
		Kandidat k
	WHERE e.wahlkreisID = v.WahlkreisID AND
	e.maxStimmen = v.Anzahl
		AND k.ID = v.KandidatID AND v.Jahr = 2009), 

FuenfProzentParteien AS (
	SELECT p.ID as ParteiID
	FROM Partei p, zweitStimmenNachWahlkreis v
	WHERE v.ParteiID = p.ID AND v.Jahr=2009
	GROUP BY p.ID
	HAVING CAST(SUM(v.Anzahl) AS FLOAT) / ( SELECT
		SUM(AnzahlStimmen) FROM ZweitStimmenNachBundesland) >= 0.05), 

DreiDirektMandatParteien AS (
	SELECT dm.ParteiID
	FROM Direktmandate dm 
	GROUP BY dm.ParteiID
	HAVING COUNT(*) >= 3), 

ParteienImBundestag AS (
	SELECT *
	FROM FuenfProzentParteien

	UNION

	SELECT *
	FROM DreiDirektMandatParteien), 

Divisoren AS (
	SELECT (ROW_NUMBER() OVER (order by w.ID) -
		0.5) as Wert
	FROM Wahlkreis w

	UNION

	SELECT (ROW_NUMBER() OVER (order by w.ID) + ( SELECT COUNT(*)
		FROM Wahlkreis) - 0.5) AS Wert
	FROM Wahlkreis w), 

ZugriffsreihenfolgeSitzeNachPartei AS (
	SELECT p.ParteiID, z.AnzahlStimmen, (z.AnzahlStimmen / d.wert)
		as DivWert, ROW_NUMBER() OVER (ORDER
BY (z.AnzahlStimmen /
		d.wert) DESC) as Rang
	FROM ParteienImBundestag p, ZweitStimmenNachPartei z,
		Divisoren d
	WHERE p.ParteiID = z.ParteiID ORDER BY DivWert desc), 

SitzeNachPartei AS (
	SELECT ParteiID, COUNT(Rang) as AnzahlSitze
	FROM ZugriffsreihenfolgeSitzeNachPartei
	WHERE Rang <= 598
	GROUP BY ParteiID), 

ZugriffsreihenfolgeSitzeNachLandeslisten AS (
	SELECT p.ParteiID, z.BundeslandID, z.AnzahlStimmen,
		(z.AnzahlStimmen / d.wert) as DivWert, ROW_NUMBER() OVER
		(PARTITION BY p.ParteiID ORDER BY (z.AnzahlStimmen /
		d.wert) DESC) as Rang
	FROM ParteienImBundestag p, ZweitStimmenNachBundesland z,
		Divisoren d
	WHERE p.ParteiID = z.ParteiID ORDER BY ParteiID,
		DivWert DESC), 

SitzeNachLandeslisten AS (
	SELECT z.ParteiID, BundeslandID, COUNT(Rang) as
		AnzahlSitze
	FROM ZugriffsreihenfolgeSitzeNachLandeslisten z,
		SitzeNachPartei s
	WHERE z.ParteiID = s.ParteiID AND z.Rang <=
		s.AnzahlSitze
	GROUP BY z.ParteiID, z.BundeslandID, s.ParteiID), 

DirektMandateProParteiUndBundesland AS (
	SELECT k.ParteiID, w.BundeslandID, COUNT(*) AS
		AnzahlDirektmandate
	FROM Direktmandate dm, Kandidat k, Wahlkreis w
	WHERE dm.KandidatID = k.ID AND w.ID = k.DMWahlkreisID
	GROUP BY k.ParteiID, w.BundeslandID), 

Ueberhangsmandate AS (
	SELECT b.ID AS BundeslandID, b.Name, p.ID AS ParteiID,
		p.Kuerzel, dmpb.AnzahlDirektmandate - s.AnzahlSitze AS
		AnzahlUeberhangsmandate
	FROM DirektMandateProParteiUndBundesland dmpb,
		SitzeNachLandeslisten s, Partei p, Bundesland b
	WHERE dmpb.BundeslandID = s.BundeslandID AND dmpb.ParteiID =
		s.ParteiID AND dmpb.ParteiID = p.ID AND dmpb.BundeslandID =
		b.ID AND dmpb.AnzahlDirektmandate - s.AnzahlSitze > 0), 

SumUeberhang AS (
	SELECT ParteiID, SUM(AnzahlUeberhangsmandate) AS
		AnzahlUeberhangsmandate
	FROM Ueberhangsmandate
	GROUP BY ParteiID )

SELECT p.Kuerzel, (s.AnzahlSitze
	+ COALESCE(u.AnzahlUeberhangsmandate, 0)) AS AnzahlSitze
FROM Partei p, SitzeNachPartei s LEFT OUTER JOIN SumUeberhang u
	ON s.ParteiID = u.ParteiID
WHERE p.ID = s.ParteiID

