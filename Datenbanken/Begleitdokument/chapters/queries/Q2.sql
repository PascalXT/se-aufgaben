WITH 

ZweitStimmenNachBundesland AS (
	SELECT w2.ParteiID, wk.BundeslandID, sum(w2.Anzahl) as
		AnzahlStimmen
	FROM zweitStimmenNachWahlkreis w2, Wahlkreis wk
	WHERE w2.WahlkreisID = wk.ID 
		AND w2.Jahr = 2009
	GROUP BY wk.BundeslandID, w2.ParteiID), 

ZweitStimmenNachPartei AS (
	SELECT ParteiID, SUM(AnzahlStimmen) AS AnzahlStimmen
	FROM ZweitStimmenNachBundesland
	GROUP BY ParteiID), 

MaxErststimmenNachWahlkreis AS (
	SELECT k.DMWahlkreisID AS WahlkreisID, MAX(v.Anzahl) AS
		MaxStimmen
	FROM erstStimmenNachWahlkreis v, Kandidat k
	WHERE v.KandidatID = k.ID 
		AND v.Jahr = 2009
	GROUP BY k.DMWahlkreisID), 

Direktmandate AS (
	SELECT k.ID AS KandidatID, k.ParteiID, k.DMWahlkreisID
	FROM MaxErststimmenNachWahlkreis e,
		erstStimmenNachWahlkreis v, Kandidat k
	WHERE e.wahlkreisID = v.WahlkreisID 
		AND e.maxStimmen = v.Anzahl 
		AND k.ID = v.KandidatID 
		AND v.Jahr = 2009), 

FuenfProzentParteien AS (
	SELECT p.ID as ParteiID
	FROM Partei p, zweitStimmenNachWahlkreis v
	WHERE v.ParteiID = p.ID 
		AND v.Jahr=2009
	GROUP BY p.ID
	HAVING CAST(SUM(v.Anzahl) AS FLOAT) / ( SELECT
		SUM(AnzahlStimmen) FROM ZweitStimmenNachBundesland)
		>= 0.05), 

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
	SELECT (ROW_NUMBER() OVER (order by w.ID) - 0.5) as Wert
	FROM Wahlkreis w

	UNION

	SELECT (ROW_NUMBER() OVER (order by w.ID) + (
	SELECT COUNT(*)
		FROM Wahlkreis) - 0.5) AS Wert
	FROM Wahlkreis w), 

ZugriffsreihenfolgeSitzeNachPartei AS (
	SELECT p.ParteiID, z.AnzahlStimmen, (z.AnzahlStimmen /
		d.wert) as DivWert, ROW_NUMBER() OVER (ORDER BY
		(z.AnzahlStimmen / d.wert) DESC) as Rang
	FROM ParteienImBundestag p, ZweitStimmenNachPartei z,
		Divisoren d
	WHERE p.ParteiID = z.ParteiID
	ORDER BY DivWert desc), 

SitzeNachPartei AS (
	SELECT ParteiID, COUNT(Rang) as AnzahlSitze
	FROM ZugriffsreihenfolgeSitzeNachPartei
	WHERE Rang <= 598
	GROUP BY ParteiID), 

ListenKandidaten AS (
	SELECT ID
	FROM Kandidat
	WHERE BundeslandID IS NOT NULL EXCEPT
	SELECT KandidatID
	FROM Direktmandate ), 

ListenKandidatenMitRang AS (
	SELECT lk.ID, k.ParteiID, b.ID AS BundeslandID, ROW_NUMBER()
		OVER (PARTITION BY b.ID, k.ParteiID ORDER BY k.Listenplatz)
		AS Rang
	FROM ListenKandidaten lk, Bundesland b, Kandidat k
	WHERE lk.ID = k.ID 
		AND k.BundeslandID = b.ID ), 

Abgeordnete AS (
	SELECT KandidatID
	FROM Direktmandate

	UNION

	SELECT lkr.ID
	FROM ListenKandidatenMitRang lkr, SitzeNachLandeslisten s
	WHERE s.ParteiID = lkr.ParteiID 
		AND s.BundeslandID = lkr.BundeslandID 
		AND lkr.Rang <= s.AnzahlSitze - ( SELECT COUNT(*) FROM
			Direktmandate dm, Wahlkreis w WHERE dm.ParteiID = lkr.ParteiID 
		AND dm.DMWahlkreisID = w.ID 
		AND w.BundeslandID = lkr.BundeslandID ) )
SELECT k.Vorname, k.Nachname, p.Kuerzel
FROM Abgeordnete a, Kandidat k, Partei p
WHERE a.KandidatID = k.ID 
	AND (k.ParteiID IS NULL OR k.ParteiID = p.ID)
ORDER BY k.Vorname, k.Nachname

