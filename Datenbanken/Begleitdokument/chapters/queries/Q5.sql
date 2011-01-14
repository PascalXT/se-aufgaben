WITH 

MaxZweitStimmenNachWahlkreis AS (
	SELECT we.WahlkreisID, MAX(we.Anzahl) AS MaxStimmen
	FROM zweitStimmenNachWahlkreis we
	WHERE we.Jahr = 2009
	GROUP BY WahlkreisID), 

MaxErststimmenNachWahlkreis AS (
	SELECT k.DMWahlkreisID AS WahlkreisID, MAX(v.Anzahl) AS
		MaxStimmen
	FROM erstStimmenNachWahlkreis v, Kandidat k
	WHERE v.KandidatID = k.ID 
		AND v.Jahr = 2009
	GROUP BY k.DMWahlkreisID), 

GewinnerZweitstimmen AS (
	SELECT we.WahlkreisID, we.ParteiID
	FROM zweitStimmenNachWahlkreis we,
		MaxZweitStimmenNachWahlkreis ms
	WHERE we.WahlkreisID = ms.WahlkreisID 
		AND we.Anzahl = ms.MaxStimmen 
		AND we.Jahr = 2009), 

GewinnerErststimmen AS (
	SELECT we.WahlkreisID, we.KandidatID
	FROM erstStimmenNachWahlkreis we, MaxErststimmenNachWahlkreis ms
	WHERE we.WahlkreisID = ms.WahlkreisID 
		AND we.Anzahl = ms.MaxStimmen 
		AND we.Jahr = 2009), 

WahlkreisSieger AS (
	SELECT g1.WahlkreisID, wk.BundeslandID, p1.Kuerzel AS P1,
		p2.Kuerzel AS P2
	FROM GewinnerErststimmen g1, GewinnerZweitstimmen g2,
		Partei p1, Partei p2, Kandidat k, Wahlkreis wk
	WHERE g1.WahlkreisID = g2.WahlkreisID 
		AND g1.KandidatID = k.ID 
		AND k.ParteiID = p1.ID 
		AND g2.ParteiID = p2.ID 
		AND wk.ID = g1.WahlkreisID)SELECT *
FROM WahlkreisSieger

WITH GewinnerErststimmen(BundeslandID, Partei, GewonneneWahlkreise) AS (
	SELECT BundeslandID, P1, COUNT(*)
	FROM WahlkreisSieger
	GROUP BY BundeslandID, P1 ), GewinnerZweitStimmen(BundeslandID,
		Partei, GewonneneWahlkreise) AS (
	SELECT BundeslandID, P2, COUNT(*)
	FROM WahlkreisSieger
	GROUP BY BundeslandID, P2 ), GewinnerGesamt(BundeslandID,
		Partei, GewonneneWahlkreise) AS (
	SELECT g1.BundeslandID, g1.Partei, g1.GewonneneWahlkreise +
		g2.GewonneneWahlkreise
	FROM GewinnerErststimmen g1, GewinnerZweitStimmen g2 )
	
SELECT g.BundeslandID, g.Partei
FROM GewinnerGesamt g
WHERE NOT EXISTS ( SELECT * FROM GewinnerGesamt g0 WHERE
	g0.BundeslandID = g.BundeslandID 
	AND g0.GewonneneWahlkreise > g.GewonneneWahlkreise ) 

