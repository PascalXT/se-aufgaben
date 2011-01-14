INSERT INTO TempErstStimmenNachWahlkreis(KandidatID, WahlkreisID, Jahr, Anzahl)
SELECT s.KandidatID, s.WahlkreisID, Jahr, COUNT(*)
FROM Stimme s
WHERE KandidatID is not null 
	AND s.WahlkreisID=213
GROUP BY s.KandidatID, s.WahlkreisID, s.Jahr

INSERT INTO TempZweitStimmenNachWahlkreis(ParteiID, WahlkreisID, Jahr, Anzahl)
SELECT s.ParteiID, s.WahlkreisID, Jahr, COUNT(*)
FROM Stimme s
WHERE ParteiID is not null 
	AND s.WahlkreisID=213
GROUP BY s.ParteiID, s.WahlkreisID, s.Jahr

INSERT INTO TempWahlkreisDaten(WahlkreisID, AnzahlWahlberechtigte, AnzahlUngueltigeErststimmen, AnzahlUngueltigeZweitstimmen, Jahr)
SELECT 213 AS WahlkreisID, ( SELECT COUNT(*) FROM
	Wahlberechtigter w WHERE w.WahlkreisID=213) AS
	AnzahlWahlberechtigte, ( SELECT COUNT(*) FROM Stimme s WHERE
	KandidatID IS NULL AND Jahr=tmp.Jahr AND s.WahlkreisID=213)
	AS AnzahlUngueltigeErststimmen, ( SELECT COUNT(*) FROM Stimme s WHERE ParteiID IS NULL AND Jahr=tmp.Jahr AND s.WahlkreisID=213) AS AnzahlUngueltigeZweitstimmen, tmp.Jahr
FROM (VALUES(2009), (2005)) tmp(Jahr)

SELECT Name
FROM Wahlkreis
WHERE ID = 213

INSERT INTO Direktmandate WITH 
MaxErststimmenNachWahlkreis AS (
	SELECT k.DMWahlkreisID AS WahlkreisID, MAX(v.Anzahl) AS
		MaxStimmen
	FROM TempErstStimmenNachWahlkreis v, Kandidat k
	WHERE v.KandidatID = k.ID 
		AND v.Jahr = 2009
	GROUP BY k.DMWahlkreisID)
SELECT k.ID AS KandidatID, k.ParteiID, k.DMWahlkreisID
FROM MaxErststimmenNachWahlkreis e,
	TempErstStimmenNachWahlkreis v, Kandidat k
WHERE e.wahlkreisID = v.WahlkreisID 
	AND e.maxStimmen = v.Anzahl 
	AND k.ID = v.KandidatID 
	AND v.Jahr = 2009

SELECT (1.0 * sum(w2.Anzahl) / max(wd.AnzahlWahlberechtigte)) as Wahlbeteiligung
FROM TempWahlkreisDaten wd, TempZweitStimmenNachWahlkreis w2
WHERE wd.WahlkreisID = w2.WahlkreisID 
	AND wd.Jahr = w2.jahr 
	AND wd.Jahr = 2009 
	AND wd.WahlkreisID = 213
GROUP BY w2.WahlkreisID

SELECT k.Vorname, k.Nachname, p.Kuerzel
FROM Direktmandate dm, Kandidat k, Partei p
WHERE dm.KandidatID = k.ID 
	AND dm.ParteiID = p.ID 
	AND dm.DMWahlkreisID = 213 

WITH ZweitStimmenWahlkreis2009 AS (
	SELECT ParteiID, Anzahl
	FROM TempZweitStimmenNachWahlkreis
	WHERE WahlkreisID = 213 
		AND Jahr = 2009), ZweitStimmenWahlkreis2005 AS (
	SELECT ParteiID, Anzahl
	FROM TempZweitStimmenNachWahlkreis
	WHERE WahlkreisID = 213 
		AND Jahr = 2005), SummeZweitStimmenWahlkreis2009 AS (
	SELECT SUM(Anzahl) AS Summe
	FROM TempZweitStimmenNachWahlkreis
	WHERE WahlkreisID = 213 
		AND Jahr = 2009
	GROUP BY WahlkreisID ), SummeZweitStimmenWahlkreis2005 AS (
	SELECT SUM(Anzahl) AS Summe
	FROM TempZweitStimmenNachWahlkreis
	WHERE WahlkreisID = 213 
		AND Jahr = 2005
	GROUP BY WahlkreisID )
SELECT p.Kuerzel, COALESCE(w2009.Anzahl, 0) AS Absolut2009,
	CAST(COALESCE(w2009.Anzahl, 0) AS FLOAT) / ( SELECT Summe
	FROM SummeZweitStimmenWahlkreis2009) AS Prozentual2009,
	COALESCE(w2005.Anzahl, 0) AS Absolut2005,
	CAST(COALESCE(w2005.Anzahl, 0) AS FLOAT) / ( SELECT Summe
	FROM SummeZweitStimmenWahlkreis2005) AS Prozentual2005, (COALESCE(w2009.Anzahl, 0) - COALESCE(w2005.Anzahl, 0)) as Aenderung
FROM ZweitStimmenWahlkreis2009 w2009 FULL OUTER JOIN
	ZweitStimmenWahlkreis2005 w2005 ON w2009.ParteiID =
	w2005.ParteiID RIGHT OUTER JOIN Partei p ON p.ID
	= w2009.ParteiID
ORDER BY Absolut2009 DESC, Absolut2005 DESC

